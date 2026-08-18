package se.topics.t1.differential;

import dk.brics.automaton.Automaton;
import dk.brics.automaton.RegExp;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * §11 — Differential testing: BRICS vs {@link java.util.regex.Pattern}.
 *
 * <p><b>Draft owned by Person A</b> to unblock the schedule — Person B
 * per {@code docs/SPLIT.md} owns §11 and is expected to review,
 * extend, or restructure this class.
 *
 * <p><b>Test design.</b> The two regex dialects are close but not
 * identical. We compare only on regex features both support (character
 * literals, classes, ranges, negation, alternation, grouping,
 * quantifiers). We deliberately avoid:
 *
 * <ul>
 *   <li>BRICS-only operators: {@code &} (intersection), {@code ~}
 *       (complement), {@code #}, {@code @}, {@code <name>}.</li>
 *   <li>Java-only features: anchors ({@code ^} {@code $}) — BRICS is
 *       always whole-string. Backreferences ({@code \1}), lookaround
 *       ({@code (?=)}), non-capturing groups ({@code (?:)}).</li>
 *   <li>The {@code .} metacharacter, because BRICS treats it as
 *       "any char including newline" while Java treats it as "any
 *       char except newline" by default — a documented semantic
 *       difference we cover as a *known* disagreement.</li>
 * </ul>
 *
 * <p>Both dialects use whole-string matching in our comparison:
 * BRICS' {@code Automaton.run(s)} matches the entire input; we call
 * Java's {@code Pattern.matcher(s).matches()} which also matches the
 * entire input.
 */
class DifferentialTest {

    // ---------------------------------------------------------------
    // Compatible-subset patterns — should agree on every input
    // ---------------------------------------------------------------

    /**
     * (regex, input, expected-match) tuples. Every pattern here is
     * expressed in the common subset of BRICS and {@code java.util.regex};
     * disagreement between the two dialects is a bug (in one of them).
     */
    static Stream<Arguments> compatibleCases() {
        return Stream.of(
                // Literal
                Arguments.of("abc",       "abc",   true),
                Arguments.of("abc",       "ab",    false),
                Arguments.of("abc",       "abcd",  false),
                Arguments.of("abc",       "",      false),

                // Character range
                Arguments.of("[a-z]",     "a",     true),
                Arguments.of("[a-z]",     "z",     true),
                Arguments.of("[a-z]",     "A",     false),
                Arguments.of("[a-z]",     "aa",    false),

                // Negated range
                Arguments.of("[^0-9]",    "a",     true),
                Arguments.of("[^0-9]",    "5",     false),

                // Alternation
                Arguments.of("cat|dog",   "cat",   true),
                Arguments.of("cat|dog",   "dog",   true),
                Arguments.of("cat|dog",   "bird",  false),
                Arguments.of("cat|dog",   "catdog", false),

                // Star
                Arguments.of("a*",        "",      true),
                Arguments.of("a*",        "aaaa",  true),
                Arguments.of("a*",        "aab",   false),

                // Plus
                Arguments.of("a+",        "",      false),
                Arguments.of("a+",        "a",     true),
                Arguments.of("a+",        "aaaa",  true),

                // Optional
                Arguments.of("colou?r",   "color", true),
                Arguments.of("colou?r",   "colour", true),
                Arguments.of("colou?r",   "colouur", false),

                // Bounded repetition
                Arguments.of("a{2,4}",    "a",     false),
                Arguments.of("a{2,4}",    "aa",    true),
                Arguments.of("a{2,4}",    "aaaa",  true),
                Arguments.of("a{2,4}",    "aaaaa", false),

                // Grouping + quantifier
                Arguments.of("(ab)+",     "ab",    true),
                Arguments.of("(ab)+",     "abab",  true),
                Arguments.of("(ab)+",     "aba",   false),
                Arguments.of("(ab)+",     "",      false),

                // Nested groups + alternation
                Arguments.of("(a|b)(c|d)", "ac",   true),
                Arguments.of("(a|b)(c|d)", "bd",   true),
                Arguments.of("(a|b)(c|d)", "ae",   false),

                // Mixed. We avoid the character '@' here because BRICS
                // treats it as the "any string" metacharacter (see
                // ANYSTRING flag in RegExp), while Java treats it as a
                // literal — a real semantic divergence documented in the
                // "known disagreements" nested class below.
                Arguments.of("[a-z]+X[a-z]+", "abcXdef", true),
                Arguments.of("[a-z]+X[a-z]+", "Xdef",    false),
                Arguments.of("[a-z]+X[a-z]+", "abcX",    false),
                Arguments.of("[a-z]+X[a-z]+", "abc",     false)
        );
    }

    @ParameterizedTest(name = "[{index}] /{0}/ vs {1} → {2}")
    @MethodSource("compatibleCases")
    @DisplayName("BRICS agrees with java.util.regex on the compatible subset")
    void compatibleSubsetAgreement(String regex, String input, boolean expected) {
        boolean bricsSays = runBrics(regex, input);
        boolean javaSays  = runJavaRegex(regex, input);

        assertEquals(expected, bricsSays,
                () -> "BRICS: /" + regex + "/ on '" + input + "' expected " + expected + " got " + bricsSays);
        assertEquals(expected, javaSays,
                () -> "Java:  /" + regex + "/ on '" + input + "' expected " + expected + " got " + javaSays);
        assertEquals(bricsSays, javaSays,
                () -> "Disagreement on /" + regex + "/ vs '" + input + "': brics=" + bricsSays + " java=" + javaSays);
    }

    // ---------------------------------------------------------------
    // Known documented disagreements
    // ---------------------------------------------------------------

    @Nested
    @DisplayName("Known disagreements (documented, not bugs)")
    class KnownDisagreements {

        @Test
        @DisplayName("dot metacharacter: BRICS matches newline, Java does not (by default)")
        void dotAndNewline() {
            // BRICS treats . as "any character including newline".
            // Java's default treats . as "any character except line terminator".
            // Both accept a plain 'a' — the disagreement only shows on newlines.
            assertTrue(runBrics(".",  "\n"));  // BRICS: yes
            assertEquals(false, runJavaRegex(".", "\n"));  // Java: no

            // Symmetry: when the input is not a newline, they agree.
            assertEquals(runBrics(".", "a"), runJavaRegex(".", "a"));
        }

        @Test
        @DisplayName("'@' character: BRICS treats as 'any string' metacharacter, Java as literal")
        void atSymbolIsMetaInBrics() {
            // In BRICS' default regex flags, '@' means ANYSTRING (Σ*).
            // So the pattern "[a-z]+@[a-z]+" in BRICS accepts any string
            // starting AND ending with a lowercase letter — including
            // "abc" (split as 'a' + '' + 'bc') even though there's no @.
            // In Java the pattern requires a literal '@'.
            assertTrue(runBrics("[a-z]+@[a-z]+", "abc"),
                    "BRICS should accept 'abc' because @ = ANYSTRING (can match empty)");
            assertEquals(false, runJavaRegex("[a-z]+@[a-z]+", "abc"),
                    "Java should reject 'abc' because it requires literal @");

            // To get "literal @" behaviour in BRICS, disable the ANYSTRING
            // syntax flag via new RegExp(pattern, RegExp.ALL & ~RegExp.ANYSTRING).
        }

        @Test
        @DisplayName("'#' character: BRICS treats as 'empty language' metacharacter")
        void hashSymbolIsMetaInBrics() {
            // BRICS: '#' matches nothing at all (empty language).
            // Java: '#' is a literal.
            assertEquals(false, runBrics("#", "#"),
                    "BRICS should reject '#' because # = EMPTY (matches nothing)");
            assertTrue(runJavaRegex("#", "#"),
                    "Java should accept '#' as literal");
        }
    }

    // ---------------------------------------------------------------
    // BRICS-only extensions — differential comparison shows the
    // dialect divergence explicitly. Java parses these characters
    // as literals; BRICS treats them as regex-algebra operators.
    // ---------------------------------------------------------------

    @Nested
    @DisplayName("BRICS-only operators diverge from java.util.regex on the same source string")
    class BricsOnlyOperators {

        @Test
        @DisplayName("Intersection operator '&': BRICS returns intersection language, Java treats as literals")
        void intersection() {
            // BRICS parses "(a|b)&(b|c)" as the intersection {a,b} ∩ {b,c} = {b}.
            assertTrue(runBrics("(a|b)&(b|c)", "b"));
            assertEquals(false, runBrics("(a|b)&(b|c)", "a"));
            assertEquals(false, runBrics("(a|b)&(b|c)", "c"));

            // Java parses "(a|b)&(b|c)" as: capture group "(a|b)", then
            // literal '&', then capture group "(b|c)" — i.e. matches
            // the concrete 3-character strings "a&b", "a&c", "b&b", "b&c".
            assertTrue(runJavaRegex("(a|b)&(b|c)", "a&b"));
            assertTrue(runJavaRegex("(a|b)&(b|c)", "b&c"));
            assertEquals(false, runJavaRegex("(a|b)&(b|c)", "b"));

            // Direct differential: the same string is accepted by one and
            // rejected by the other for BOTH interpretations.
            assertEquals(false, runBrics("(a|b)&(b|c)", "a&b"));  // BRICS: no
            assertTrue(runJavaRegex("(a|b)&(b|c)", "a&b"));       // Java:  yes
        }

        @Test
        @DisplayName("Complement operator '~': BRICS returns complement language, Java rejects the ~ prefix")
        void complement() {
            // BRICS: ~(a) = complement of {"a"} = everything except "a".
            assertEquals(false, runBrics("~(a)", "a"));
            assertTrue(runBrics("~(a)", ""));
            assertTrue(runBrics("~(a)", "b"));
            assertTrue(runBrics("~(a)", "aa"));

            // Java: "~(a)" is a literal '~' followed by capture group "(a)".
            // So Java matches only the 2-char string "~a" — and rejects "a".
            assertTrue(runJavaRegex("~(a)", "~a"));
            assertEquals(false, runJavaRegex("~(a)", "a"));
            assertEquals(false, runJavaRegex("~(a)", ""));

            // Direct differential: BRICS accepts "b", Java rejects it.
            assertTrue(runBrics("~(a)", "b"));
            assertEquals(false, runJavaRegex("~(a)", "b"));
        }
    }

    // ---------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------

    private static boolean runBrics(String regex, String input) {
        return new RegExp(regex).toAutomaton().run(input);
    }

    /** Whole-string match, mirroring BRICS' semantics. */
    private static boolean runJavaRegex(String regex, String input) {
        try {
            return Pattern.compile(regex).matcher(input).matches();
        } catch (PatternSyntaxException e) {
            // Java rejected the pattern; that's a disagreement about
            // parseability, not membership. Callers can inspect the
            // returned false to notice.
            return false;
        }
    }
}
