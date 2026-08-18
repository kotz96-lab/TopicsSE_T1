package se.topics.t1.pict;

import dk.brics.automaton.Automaton;
import dk.brics.automaton.BasicAutomata;
import dk.brics.automaton.RegExp;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * §9 experiment 2: PICT rows crossed with metamorphic properties —
 * the "strong oracle" experiment.
 *
 * <p>The companion class {@link PictRegexTest} uses a deliberately
 * weak oracle (smoke + double-negation on a single input) and
 * showed that increasing interaction strength from 2-wise → 4-wise
 * does not measurably move the mutation score. That is the
 * textbook-predicted null result when the oracle is too broad to
 * distinguish mutants.
 *
 * <p>This class tests the other side of the coin: for each PICT row
 * we build the row's automaton and then subject it to <b>five
 * metamorphic properties</b>, each checked on multiple sampled
 * inputs. If the theory holds — that CIT's value scales with oracle
 * strength — the mutation score at each interaction strength
 * should be materially higher here than in {@link PictRegexTest}.
 *
 * <p>Properties applied per row (each an identity that must hold
 * for any regular language):
 * <ol>
 *   <li>Double complement: {@code ¬¬A} accepts the same strings as A.</li>
 *   <li>Union idempotence: {@code A ∪ A} accepts the same strings as A.</li>
 *   <li>Intersection idempotence: {@code A ∩ A} accepts the same strings as A.</li>
 *   <li>Minimize preserves language: {@code minimize(A)} accepts the same strings as A.</li>
 *   <li>Determinize preserves language: {@code determinize(A)} accepts the same strings as A.</li>
 * </ol>
 *
 * <p>Each property is asserted on 5 sampled inputs drawn from the
 * row's alphabet, so a single mutant that corrupts one operation on
 * one input gets caught immediately.
 *
 * <p>Tagged {@code pict-strong-Nwise} so PIT can be filtered per
 * interaction strength independently of {@link PictRegexTest}'s
 * weak-oracle runs.
 */
class PictMetamorphicTest {

    private static final String CSV_2WISE = "pict/generated/regex-2wise.csv";
    private static final String CSV_3WISE = "pict/generated/regex-3wise.csv";
    private static final String CSV_4WISE = "pict/generated/regex-4wise.csv";

    @Tag("pict-strong-2wise")
    @ParameterizedTest(name = "strong-2wise[{index}] {0}/{1}/{2}/{3}/{4}/{5}/{6}")
    @CsvFileSource(files = CSV_2WISE, numLinesToSkip = 1)
    void strongOraclePairwise(
            String alphabet, String charClassKind, String quantifier,
            int alternationDepth, int nestingDepth,
            String operation, String inputLength) {
        exerciseRow(alphabet, charClassKind, quantifier,
                alternationDepth, nestingDepth, operation, inputLength);
    }

    @Tag("pict-strong-3wise")
    @ParameterizedTest(name = "strong-3wise[{index}] {0}/{1}/{2}/{3}/{4}/{5}/{6}")
    @CsvFileSource(files = CSV_3WISE, numLinesToSkip = 1)
    void strongOracleThreeWise(
            String alphabet, String charClassKind, String quantifier,
            int alternationDepth, int nestingDepth,
            String operation, String inputLength) {
        exerciseRow(alphabet, charClassKind, quantifier,
                alternationDepth, nestingDepth, operation, inputLength);
    }

    @Tag("pict-strong-4wise")
    @ParameterizedTest(name = "strong-4wise[{index}] {0}/{1}/{2}/{3}/{4}/{5}/{6}")
    @CsvFileSource(files = CSV_4WISE, numLinesToSkip = 1)
    void strongOracleFourWise(
            String alphabet, String charClassKind, String quantifier,
            int alternationDepth, int nestingDepth,
            String operation, String inputLength) {
        exerciseRow(alphabet, charClassKind, quantifier,
                alternationDepth, nestingDepth, operation, inputLength);
    }

    // ---------------------------------------------------------------
    // Per-row exercise: build the automaton once, apply all 5
    // metamorphic properties, each on multiple sampled inputs.
    // ---------------------------------------------------------------

    private static void exerciseRow(
            String alphabet, String charClassKind, String quantifier,
            int alternationDepth, int nestingDepth,
            String operation, String inputLength) {

        RegexRowBuilder.Row row = RegexRowBuilder.build(
                alphabet, charClassKind, quantifier,
                alternationDepth, nestingDepth, operation, inputLength);

        // Note: we deliberately compile the *base* automaton (without the
        // row's declared Operation) here — the metamorphic properties test
        // BRICS' union / complement / minimize / determinize directly, so
        // pre-applying the row's operation would bake in an extra layer
        // that just adds noise to what we're measuring.
        Automaton base = new RegExp(row.regex()).toAutomaton();
        assertNotNull(base);

        List<String> inputs = sampledInputs(row);

        doubleComplement(base, inputs, row);
        unionIdempotence(base, inputs, row);
        intersectionIdempotence(base, inputs, row);
        minimizePreservesLanguage(base, inputs, row);
        determinizePreservesLanguage(base, inputs, row);
    }

    // ---------------------------------------------------------------
    // Metamorphic properties
    // ---------------------------------------------------------------

    /** MR-A: ¬¬A accepts the same strings as A. */
    private static void doubleComplement(Automaton base, List<String> inputs, RegexRowBuilder.Row row) {
        Automaton doubleC = base.clone().complement().complement();
        for (String s : inputs) {
            assertEquals(base.run(s), doubleC.run(s),
                    () -> "MR-A double-complement mismatch: regex=" + row.regex()
                            + " input=" + describe(s));
        }
    }

    /** MR-B: A ∪ A accepts the same strings as A. */
    private static void unionIdempotence(Automaton base, List<String> inputs, RegexRowBuilder.Row row) {
        Automaton unionAA = base.clone().union(base.clone());
        for (String s : inputs) {
            assertEquals(base.run(s), unionAA.run(s),
                    () -> "MR-B union-idempotence mismatch: regex=" + row.regex()
                            + " input=" + describe(s));
        }
    }

    /** MR-C: A ∩ A accepts the same strings as A. */
    private static void intersectionIdempotence(Automaton base, List<String> inputs, RegexRowBuilder.Row row) {
        Automaton interAA = base.clone().intersection(base.clone());
        for (String s : inputs) {
            assertEquals(base.run(s), interAA.run(s),
                    () -> "MR-C intersection-idempotence mismatch: regex=" + row.regex()
                            + " input=" + describe(s));
        }
    }

    /** MR-D: minimize(A) accepts the same strings as A. */
    private static void minimizePreservesLanguage(Automaton base, List<String> inputs, RegexRowBuilder.Row row) {
        Automaton min = base.clone();
        min.minimize();
        for (String s : inputs) {
            assertEquals(base.run(s), min.run(s),
                    () -> "MR-D minimize-preserves mismatch: regex=" + row.regex()
                            + " input=" + describe(s));
        }
    }

    /** MR-E: determinize(A) accepts the same strings as A. */
    private static void determinizePreservesLanguage(Automaton base, List<String> inputs, RegexRowBuilder.Row row) {
        Automaton det = base.clone();
        det.determinize();
        for (String s : inputs) {
            assertEquals(base.run(s), det.run(s),
                    () -> "MR-E determinize-preserves mismatch: regex=" + row.regex()
                            + " input=" + describe(s));
        }
    }

    // ---------------------------------------------------------------
    // Sampled inputs per row — chosen to give sharp per-mutant signal.
    // ---------------------------------------------------------------

    /**
     * Return 5 sampled test inputs for this row: empty string,
     * one alphabet char, a short repeat, a moderate repeat, and a
     * character intentionally outside the alphabet (so the assertion
     * catches mutants that misclassify boundary conditions).
     */
    private static List<String> sampledInputs(RegexRowBuilder.Row row) {
        char inAlpha  = alphabetSample(row.alphabet());
        char outAlpha = outsideAlphabet(row.alphabet());
        return List.of(
                "",
                String.valueOf(inAlpha),
                String.valueOf(inAlpha).repeat(3),
                String.valueOf(inAlpha).repeat(8),
                String.valueOf(outAlpha)
        );
    }

    private static char alphabetSample(RegexRowBuilder.Alphabet a) {
        return switch (a) {
            case ascii_lower, single_char -> 'a';
            case ascii_alnum              -> 'A';
            case unicode_bmp              -> 'א';
        };
    }

    private static char outsideAlphabet(RegexRowBuilder.Alphabet a) {
        return switch (a) {
            case ascii_lower  -> 'Z';   // outside [a-z]
            case ascii_alnum  -> '!';   // outside [a-zA-Z0-9]
            case unicode_bmp  -> 'a';   // outside the Hebrew block
            case single_char  -> 'y';   // not 'x'
        };
    }

    /** Renders unusual chars readably in assertion messages. */
    private static String describe(String s) {
        if (s.isEmpty()) return "<empty>";
        return "\"" + s + "\" (len=" + s.length() + ")";
    }
}
