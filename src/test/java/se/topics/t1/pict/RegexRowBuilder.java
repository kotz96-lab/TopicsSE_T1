package se.topics.t1.pict;

import dk.brics.automaton.Automaton;
import dk.brics.automaton.BasicAutomata;
import dk.brics.automaton.RegExp;

/**
 * Deterministic mapping from a PICT row of abstract parameter values
 * (Alphabet, CharClassKind, Quantifier, AlternationDepth, NestingDepth,
 * Operation, InputLength) to a concrete BRICS regex string, a companion
 * automaton to combine with (for union/intersection operations), and a
 * test input string.
 *
 * <p>The mapping is intentionally fixed — one PICT row always produces
 * the same regex — so failures are reproducible and each row is a
 * traceable experiment. Alternative "random-seeded" builders were
 * considered and rejected as harder to debug; see docs/pict-rationale.md.
 */
public final class RegexRowBuilder {

    private RegexRowBuilder() {}

    /** Character-pool identifier from the PICT model. */
    public enum Alphabet { ascii_lower, ascii_alnum, unicode_bmp, single_char }

    /** How the character class is written in the regex source. */
    public enum CharClassKind { single, range, set, negated }

    /** Repetition operator applied to the atom. */
    public enum Quantifier { none, star, plus, optional, bounded }

    /** What we do with the automaton after compiling the regex. */
    public enum Operation { toAutomaton, determinize, minimize, complement, union, intersection }

    /** Length bucket for the test input. */
    public enum InputLength { empty, small_short, medium }  // "short" is a Java keyword — see fromString

    /** Immutable record of the row + the derived regex string. */
    public record Row(
            Alphabet alphabet,
            CharClassKind charClassKind,
            Quantifier quantifier,
            int alternationDepth,
            int nestingDepth,
            Operation operation,
            InputLength inputLength,
            String regex
    ) {}

    // ------------------------------------------------------------
    // 1. Atomic character class
    // ------------------------------------------------------------

    private static String atom(Alphabet alpha, CharClassKind kind) {
        return switch (alpha) {
            case ascii_lower  -> switch (kind) {
                case single  -> "a";
                case range   -> "[a-z]";
                case set     -> "[abc]";
                case negated -> "[^a-z]";
            };
            case ascii_alnum  -> switch (kind) {
                case single  -> "A";
                case range   -> "[a-zA-Z0-9]";
                case set     -> "[aB3]";
                case negated -> "[^a-zA-Z0-9]";
            };
            case unicode_bmp  -> switch (kind) {
                // Hebrew block (U+05D0..U+05F4). Kept as literal chars
                // rather than backslash-u escapes because javac processes
                // backslash-u sequences even inside comments and strings.
                case single  -> "א";
                case range   -> "[֐-׿]";
                case set     -> "[אבג]";
                case negated -> "[^֐-׿]";
            };
            case single_char  -> switch (kind) {
                case single  -> "x";
                case set     -> "[x]";
                // The PICT model constraints forbid range / negated with single_char.
                // If they slip through, treat as "single" — better than throwing.
                case range, negated -> "x";
            };
        };
    }

    // ------------------------------------------------------------
    // 2. Quantifier wrap
    // ------------------------------------------------------------

    private static String quantified(String atomExpr, Quantifier q) {
        return switch (q) {
            case none     -> atomExpr;
            case star     -> atomExpr + "*";
            case plus     -> atomExpr + "+";
            case optional -> atomExpr + "?";
            case bounded  -> atomExpr + "{1,3}";
        };
    }

    // ------------------------------------------------------------
    // 3. Alternation
    // ------------------------------------------------------------

    private static String alternated(String term, int depth) {
        if (depth <= 0) return term;
        StringBuilder b = new StringBuilder(term);
        for (int i = 0; i < depth; i++) {
            // Reuse the same term as each alternative — this exercises
            // BRICS' union-of-equal-branches minimization paths.
            b.append("|").append(term);
        }
        return b.toString();
    }

    // ------------------------------------------------------------
    // 4. Grouping / nesting
    // ------------------------------------------------------------

    private static String nested(String inner, int depth) {
        StringBuilder b = new StringBuilder(inner);
        for (int i = 0; i < depth; i++) {
            b.insert(0, '(').append(')');
        }
        return b.toString();
    }

    // ------------------------------------------------------------
    // 5. Test input construction
    // ------------------------------------------------------------

    /**
     * Build a test input for the given alphabet + length bucket. Content
     * is deterministic so failures are reproducible.
     */
    public static String buildInput(Alphabet alpha, InputLength len) {
        char sample = switch (alpha) {
            case ascii_lower, single_char -> 'a';
            case ascii_alnum              -> 'A';
            case unicode_bmp              -> 'א';
        };
        int n = switch (len) {
            case empty       -> 0;
            case small_short -> 3;
            case medium      -> 10;
        };
        return String.valueOf(sample).repeat(n);
    }

    // ------------------------------------------------------------
    // 6. Public entry — assemble the full Row
    // ------------------------------------------------------------

    public static Row build(
            String alphabet,
            String charClassKind,
            String quantifier,
            int alternationDepth,
            int nestingDepth,
            String operation,
            String inputLength
    ) {
        Alphabet     a  = Alphabet.valueOf(alphabet);
        CharClassKind cc = CharClassKind.valueOf(charClassKind);
        Quantifier   q  = Quantifier.valueOf(quantifier);
        Operation    op = Operation.valueOf(operation);
        InputLength  il = fromInputLengthString(inputLength);

        String atom       = atom(a, cc);
        String quantified = quantified(atom, q);
        String alternated = alternated(quantified, alternationDepth);
        String regex      = nested(alternated, nestingDepth);

        return new Row(a, cc, q, alternationDepth, nestingDepth, op, il, regex);
    }

    /** "short" is a Java keyword so the enum uses {@code small_short}. */
    private static InputLength fromInputLengthString(String s) {
        return switch (s) {
            case "empty"  -> InputLength.empty;
            case "short"  -> InputLength.small_short;
            case "medium" -> InputLength.medium;
            default -> throw new IllegalArgumentException("Unknown InputLength: " + s);
        };
    }

    // ------------------------------------------------------------
    // 7. Automaton to combine with (for union / intersection)
    // ------------------------------------------------------------

    /**
     * For union/intersection we need a *second* automaton. To keep the
     * test outcome deterministic we always use a fixed marker language:
     * the singleton string "marker". This means:
     *   - union    accepts L(regex) ∪ {"marker"};
     *   - intersection accepts L(regex) ∩ {"marker"}.
     * Neither is particularly deep, but both exercise the BasicOperations
     * code paths and keep failures easy to reproduce.
     */
    public static Automaton companion() {
        return BasicAutomata.makeString("marker");
    }

    /** Apply the row's declared Operation to the automaton. */
    public static Automaton applyOperation(Automaton a, Operation op) {
        switch (op) {
            case toAutomaton:
                return a;
            case determinize:
                a.determinize();
                return a;
            case minimize:
                a.minimize();
                return a;
            case complement:
                return a.complement();
            case union:
                return a.union(companion());
            case intersection:
                return a.intersection(companion());
            default:
                throw new IllegalStateException("Unhandled Operation " + op);
        }
    }

    /** Convenience: build a Row, compile it, apply the operation. */
    public static Automaton compile(Row row) {
        Automaton a = new RegExp(row.regex()).toAutomaton();
        return applyOperation(a, row.operation());
    }
}
