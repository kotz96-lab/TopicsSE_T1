package se.topics.t1.infra;

import dk.brics.automaton.RegExp;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;
import se.topics.t1.pict.RegexRowBuilder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * §13 — Infrastructure validation. Tests for the harness we wrote
 * ourselves (as opposed to BRICS, which lives in the other packages).
 *
 * <p>Right now the substantive Java-side infra is {@link RegexRowBuilder} —
 * the deterministic mapping from a PICT row to a concrete BRICS
 * regex + test input. If that mapping is buggy, every one of the 754
 * PICT-driven tests inherits the bug silently, so validating it here
 * is a load-bearing check.
 *
 * <p>The PowerShell script {@code scripts/generate-pict.ps1} is not
 * unit-testable from Java, so we verify its <em>output</em> instead:
 * for each committed CSV table, we assert the schema and that every
 * row builds through the row builder without error.
 */
class RegexRowBuilderInfraTest {

    @Nested
    @DisplayName("Row → regex construction is deterministic and BRICS-parseable")
    class RowConstruction {

        @Test
        @DisplayName("same row builds the same regex on repeated calls")
        void deterministic() {
            var r1 = RegexRowBuilder.build("ascii_lower", "range", "star", 1, 2, "minimize", "short");
            var r2 = RegexRowBuilder.build("ascii_lower", "range", "star", 1, 2, "minimize", "short");
            assertEquals(r1.regex(), r2.regex());
        }

        @Test
        @DisplayName("built regex parses through BRICS RegExp without exception")
        void regexIsParseable() {
            var r = RegexRowBuilder.build("ascii_alnum", "range", "plus", 2, 1, "toAutomaton", "medium");
            // If this throws, the row builder produced BRICS-invalid regex source.
            new RegExp(r.regex()).toAutomaton();
        }

        @Test
        @DisplayName("compile(row) applies the row's operation and returns non-null")
        void compileAppliesOperation() {
            var r = RegexRowBuilder.build("ascii_lower", "single", "star", 0, 0, "minimize", "empty");
            assertNotNull(RegexRowBuilder.compile(r));
        }
    }

    @Nested
    @DisplayName("Quantifier / alternation / nesting mapping is correct")
    class Structure {

        @Test
        @DisplayName("quantifier=star appends *")
        void starAppendsStar() {
            var r = RegexRowBuilder.build("ascii_lower", "single", "star", 0, 0, "toAutomaton", "empty");
            assertTrue(r.regex().endsWith("*"), "regex should end with *: " + r.regex());
        }

        @Test
        @DisplayName("quantifier=bounded appends {1,3}")
        void boundedAppendsCurlyBrace() {
            var r = RegexRowBuilder.build("ascii_lower", "single", "bounded", 0, 0, "toAutomaton", "empty");
            assertTrue(r.regex().endsWith("{1,3}"), "regex should end with {1,3}: " + r.regex());
        }

        @Test
        @DisplayName("alternationDepth=2 produces two | branches (three alternatives)")
        void alternationDepthProducesRightBranchCount() {
            var r = RegexRowBuilder.build("ascii_lower", "single", "none", 2, 0, "toAutomaton", "empty");
            long pipes = r.regex().chars().filter(c -> c == '|').count();
            assertEquals(2, pipes, "expected 2 | for depth=2 (three alternatives): " + r.regex());
        }

        @Test
        @DisplayName("nestingDepth=2 wraps regex in two pairs of parens")
        void nestingDepthWrapsInParens() {
            var r = RegexRowBuilder.build("ascii_lower", "single", "star", 0, 2, "toAutomaton", "empty");
            assertTrue(r.regex().startsWith("(("), "expected regex to start with ((: " + r.regex());
            assertTrue(r.regex().endsWith("))"),   "expected regex to end with )): "   + r.regex());
        }
    }

    @Nested
    @DisplayName("Input builder respects length bucket + alphabet")
    class InputBuilder {

        @Test
        @DisplayName("empty input length always yields the empty string")
        void emptyLength() {
            String s = RegexRowBuilder.buildInput(
                    RegexRowBuilder.Alphabet.ascii_lower, RegexRowBuilder.InputLength.empty);
            assertEquals("", s);
        }

        @Test
        @DisplayName("short input length yields a 3-char string")
        void shortLength() {
            String s = RegexRowBuilder.buildInput(
                    RegexRowBuilder.Alphabet.ascii_lower, RegexRowBuilder.InputLength.small_short);
            assertEquals(3, s.length());
        }

        @Test
        @DisplayName("medium input length yields a 10-char string")
        void mediumLength() {
            String s = RegexRowBuilder.buildInput(
                    RegexRowBuilder.Alphabet.ascii_lower, RegexRowBuilder.InputLength.medium);
            assertEquals(10, s.length());
        }
    }

    @Nested
    @DisplayName("single_char + range/negated falls back to the singleton atom")
    class SingleCharFallback {

        // The PICT model (pict/models/regex.pict) constrains single_char
        // to only pair with CharClassKind {single, set} — but build() does
        // not enforce that constraint itself, so this fallback branch in
        // RegexRowBuilder.atom() is reachable from Java even though no
        // currently-generated PICT row exercises it. Untested until now.

        @Test
        @DisplayName("single_char + range falls back to the bare 'x' atom instead of throwing")
        void rangeFallsBackToBareChar() {
            var r = RegexRowBuilder.build("single_char", "range", "none", 0, 0, "toAutomaton", "empty");
            assertEquals("x", r.regex());
        }

        @Test
        @DisplayName("single_char + negated falls back to the bare 'x' atom instead of throwing")
        void negatedFallsBackToBareChar() {
            var r = RegexRowBuilder.build("single_char", "negated", "none", 0, 0, "toAutomaton", "empty");
            assertEquals("x", r.regex());
        }
    }

    @Nested
    @DisplayName("Row builder rejects unknown enum tokens")
    class BadInput {

        @Test
        @DisplayName("unknown Alphabet throws IllegalArgumentException")
        void unknownAlphabet() {
            assertThrows(IllegalArgumentException.class, () ->
                    RegexRowBuilder.build("klingon", "single", "none", 0, 0, "toAutomaton", "empty"));
        }

        @Test
        @DisplayName("unknown InputLength throws IllegalArgumentException")
        void unknownInputLength() {
            assertThrows(IllegalArgumentException.class, () ->
                    RegexRowBuilder.build("ascii_lower", "single", "none", 0, 0, "toAutomaton", "gigantic"));
        }
    }

    // ---------------------------------------------------------------
    // Pipeline validation — every committed CSV row builds cleanly.
    // If the PowerShell generator or the model produce a row shape
    // the Java builder can't handle, one of these will fail.
    // ---------------------------------------------------------------

    @ParameterizedTest(name = "2-wise row {index} is buildable")
    @CsvFileSource(files = "pict/generated/regex-2wise.csv", numLinesToSkip = 1)
    void every2WiseRowIsBuildable(
            String alphabet, String charClassKind, String quantifier,
            int alternationDepth, int nestingDepth,
            String operation, String inputLength) {
        assertNotNull(RegexRowBuilder.build(
                alphabet, charClassKind, quantifier,
                alternationDepth, nestingDepth, operation, inputLength));
    }

    @ParameterizedTest(name = "3-wise row {index} is buildable")
    @CsvFileSource(files = "pict/generated/regex-3wise.csv", numLinesToSkip = 1)
    void every3WiseRowIsBuildable(
            String alphabet, String charClassKind, String quantifier,
            int alternationDepth, int nestingDepth,
            String operation, String inputLength) {
        assertNotNull(RegexRowBuilder.build(
                alphabet, charClassKind, quantifier,
                alternationDepth, nestingDepth, operation, inputLength));
    }
}
