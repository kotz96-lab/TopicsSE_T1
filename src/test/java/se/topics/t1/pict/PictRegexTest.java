package se.topics.t1.pict;

import dk.brics.automaton.Automaton;
import dk.brics.automaton.RegExp;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Parameterized JUnit5 tests driven by PICT-generated combinations
 * (§9). Every row of every CSV under {@code pict/generated/} produces
 * one test invocation. The rows are consumed via
 * {@link CsvFileSource#files()} which reads them from the working
 * directory Maven runs the tests from (the project root).
 *
 * <p>For each row we:
 * <ol>
 *   <li>Build a concrete regex + test input via {@link RegexRowBuilder};</li>
 *   <li>Compile with {@link RegExp} and apply the row's declared
 *       {@link RegexRowBuilder.Operation};</li>
 *   <li>Assert two properties:
 *     <ul>
 *       <li><b>Smoke</b>: the whole pipeline runs without throwing;</li>
 *       <li><b>Semantic invariant</b>: {@code complement(complement(A))}
 *           agrees with {@code A} on the test input — this is
 *           double-negation and holds for every regular language.</li>
 *     </ul>
 *   </li>
 * </ol>
 *
 * <p>The double-negation property is deliberately weak (it's a single
 * boolean check per row), but that keeps the run cheap enough that
 * the 4-wise table (568 rows) finishes in under a second. Deeper
 * property tests live in the {@code metamorphic/} package (§10).
 */
class PictRegexTest {

    private static final String CSV_2WISE = "pict/generated/regex-2wise.csv";
    private static final String CSV_3WISE = "pict/generated/regex-3wise.csv";
    private static final String CSV_4WISE = "pict/generated/regex-4wise.csv";

    @ParameterizedTest(name = "2-wise[{index}] {0}/{1}/{2}/{3}/{4}/{5}/{6}")
    @CsvFileSource(files = CSV_2WISE, numLinesToSkip = 1)
    void pairwise(
            String alphabet, String charClassKind, String quantifier,
            int alternationDepth, int nestingDepth,
            String operation, String inputLength) {
        exerciseRow(alphabet, charClassKind, quantifier,
                alternationDepth, nestingDepth, operation, inputLength);
    }

    @ParameterizedTest(name = "3-wise[{index}] {0}/{1}/{2}/{3}/{4}/{5}/{6}")
    @CsvFileSource(files = CSV_3WISE, numLinesToSkip = 1)
    void threeWise(
            String alphabet, String charClassKind, String quantifier,
            int alternationDepth, int nestingDepth,
            String operation, String inputLength) {
        exerciseRow(alphabet, charClassKind, quantifier,
                alternationDepth, nestingDepth, operation, inputLength);
    }

    @ParameterizedTest(name = "4-wise[{index}] {0}/{1}/{2}/{3}/{4}/{5}/{6}")
    @CsvFileSource(files = CSV_4WISE, numLinesToSkip = 1)
    void fourWise(
            String alphabet, String charClassKind, String quantifier,
            int alternationDepth, int nestingDepth,
            String operation, String inputLength) {
        exerciseRow(alphabet, charClassKind, quantifier,
                alternationDepth, nestingDepth, operation, inputLength);
    }

    // ---------------------------------------------------------------

    private void exerciseRow(
            String alphabet, String charClassKind, String quantifier,
            int alternationDepth, int nestingDepth,
            String operation, String inputLength) {

        RegexRowBuilder.Row row = RegexRowBuilder.build(
                alphabet, charClassKind, quantifier,
                alternationDepth, nestingDepth, operation, inputLength);

        String testInput = RegexRowBuilder.buildInput(row.alphabet(), row.inputLength());

        // 1) Smoke: the whole pipeline (parse → toAutomaton → apply op → run) runs.
        Automaton a = assertDoesNotThrow(
                () -> RegexRowBuilder.compile(row),
                "Row failed to compile: " + row.regex());
        assertNotNull(a);

        boolean acceptedByOriginal = assertDoesNotThrow(
                () -> a.run(testInput),
                "Original .run() threw for input " + testInput);

        // 2) Double-negation invariant: complement(complement(A)) ≡ A.
        //    We only observe the single test input, so this is a
        //    single-point sanity check (cheap but sharp for row-level
        //    regressions). Full metamorphic tests live under
        //    src/test/java/se/topics/t1/metamorphic/.
        Automaton doubleComplement = assertDoesNotThrow(
                () -> a.complement().complement(),
                "Double-complement threw for regex " + row.regex());
        boolean acceptedByDoubleComplement = doubleComplement.run(testInput);

        assertTrue(
                acceptedByOriginal == acceptedByDoubleComplement,
                () -> "Double-negation broken: regex=" + row.regex()
                        + " op=" + row.operation()
                        + " input=" + testInput
                        + " original=" + acceptedByOriginal
                        + " ~~=" + acceptedByDoubleComplement);
    }
}
