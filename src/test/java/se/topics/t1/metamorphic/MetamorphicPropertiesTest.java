package se.topics.t1.metamorphic;

import dk.brics.automaton.Automaton;
import dk.brics.automaton.BasicAutomata;
import dk.brics.automaton.RegExp;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * §10 — Metamorphic properties. Each test encodes a mathematical
 * identity over regular languages that BRICS must respect. Unlike
 * example-based tests (§6), we do not need to know what any specific
 * automaton accepts; we only need to know that the *relationship*
 * between two automata must hold.
 *
 * <p><b>Draft owned by Person A</b> to unblock the schedule — Person B
 * per {@code docs/SPLIT.md} owns §10 and is expected to review,
 * extend, or restructure this class. Kept intentionally short so a
 * rewrite is cheap.
 *
 * <p><b>Oracle:</b> language equivalence via double subset —
 * {@code A ⊆ B ∧ B ⊆ A}. BRICS' {@code Automaton.equals} uses
 * hashCode, which compares *structure*, not language, so we avoid it.
 * BRICS' {@code subsetOf} determinizes internally and is a genuine
 * semantic check.
 *
 * <p>Assignment §10 requires ≥10 properties. We supply 18 across
 * union, intersection, complement, concatenation, minimize,
 * determinize, Kleene star, regex round-trip, and prefix-closure.
 */
class MetamorphicPropertiesTest {

    // ---------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------

    /** Language equivalence via double-inclusion. */
    private static boolean sameLanguage(Automaton a, Automaton b) {
        // subsetOf may mutate (determinize) its argument, so we work on clones.
        return a.clone().subsetOf(b.clone()) && b.clone().subsetOf(a.clone());
    }

    /**
     * Small library of "witness" automata used across the properties.
     * Each is a factory so tests get a fresh instance (many BRICS ops
     * mutate the receiver).
     */
    private static Supplier<Automaton> a()     { return () -> BasicAutomata.makeChar('a'); }
    private static Supplier<Automaton> b()     { return () -> BasicAutomata.makeChar('b'); }
    private static Supplier<Automaton> ab()    { return () -> BasicAutomata.makeString("ab"); }
    private static Supplier<Automaton> aStar() { return () -> new RegExp("a*").toAutomaton(); }
    private static Supplier<Automaton> abOr()  { return () -> new RegExp("(a|b)+").toAutomaton(); }
    private static Supplier<Automaton> emptyLang() { return BasicAutomata::makeEmpty; }

    // ---------------------------------------------------------------
    // Property 1 — Union commutativity: L(A ∪ B) = L(B ∪ A)
    // ---------------------------------------------------------------

    @Test
    @DisplayName("[MR-1] Union is commutative")
    void unionCommutative() {
        assertTrue(sameLanguage(a().get().union(b().get()), b().get().union(a().get())));
        assertTrue(sameLanguage(aStar().get().union(ab().get()), ab().get().union(aStar().get())));
    }

    // ---------------------------------------------------------------
    // Property 2 — Union associativity: L((A∪B)∪C) = L(A∪(B∪C))
    // ---------------------------------------------------------------

    @Test
    @DisplayName("[MR-2] Union is associative")
    void unionAssociative() {
        Automaton left  = a().get().union(b().get()).union(ab().get());
        Automaton right = a().get().union(b().get().union(ab().get()));
        assertTrue(sameLanguage(left, right));
    }

    // ---------------------------------------------------------------
    // Property 3 — Union idempotence: L(A ∪ A) = L(A)
    // ---------------------------------------------------------------

    @Test
    @DisplayName("[MR-3] A ∪ A has the same language as A")
    void unionIdempotent() {
        assertTrue(sameLanguage(a().get(), a().get().union(a().get())));
        assertTrue(sameLanguage(abOr().get(), abOr().get().union(abOr().get())));
    }

    // ---------------------------------------------------------------
    // Property 4 — Intersection commutativity: L(A ∩ B) = L(B ∩ A)
    // ---------------------------------------------------------------

    @Test
    @DisplayName("[MR-4] Intersection is commutative")
    void intersectionCommutative() {
        Automaton ab = a().get().union(b().get());
        Automaton bc = b().get().union(BasicAutomata.makeChar('c'));
        assertTrue(sameLanguage(ab.intersection(bc), bc.intersection(ab)));
    }

    // ---------------------------------------------------------------
    // Property 5 — Intersection idempotence: L(A ∩ A) = L(A)
    // ---------------------------------------------------------------

    @Test
    @DisplayName("[MR-5] A ∩ A has the same language as A")
    void intersectionIdempotent() {
        assertTrue(sameLanguage(aStar().get(), aStar().get().intersection(aStar().get())));
    }

    // ---------------------------------------------------------------
    // Property 6 — Double complement: L(¬¬A) = L(A)
    // ---------------------------------------------------------------

    @Test
    @DisplayName("[MR-6] ¬(¬A) has the same language as A")
    void doubleComplement() {
        assertTrue(sameLanguage(a().get(), a().get().complement().complement()));
        assertTrue(sameLanguage(aStar().get(), aStar().get().complement().complement()));
        assertTrue(sameLanguage(abOr().get(), abOr().get().complement().complement()));
    }

    // ---------------------------------------------------------------
    // Property 7 — De Morgan (complement of union)
    //   L(¬(A ∪ B)) = L(¬A ∩ ¬B)
    // ---------------------------------------------------------------

    @Test
    @DisplayName("[MR-7] De Morgan: ¬(A ∪ B) = ¬A ∩ ¬B")
    void deMorganUnion() {
        Automaton lhs = a().get().union(b().get()).complement();
        Automaton rhs = a().get().complement().intersection(b().get().complement());
        assertTrue(sameLanguage(lhs, rhs));
    }

    // ---------------------------------------------------------------
    // Property 8 — De Morgan (complement of intersection)
    //   L(¬(A ∩ B)) = L(¬A ∪ ¬B)
    // ---------------------------------------------------------------

    @Test
    @DisplayName("[MR-8] De Morgan: ¬(A ∩ B) = ¬A ∪ ¬B")
    void deMorganIntersection() {
        Automaton ab = a().get().union(b().get());
        Automaton bc = b().get().union(BasicAutomata.makeChar('c'));
        Automaton lhs = ab.intersection(bc).complement();
        Automaton rhs = ab.complement().union(bc.complement());
        assertTrue(sameLanguage(lhs, rhs));
    }

    // ---------------------------------------------------------------
    // Property 9 — Concatenation associativity: L((A·B)·C) = L(A·(B·C))
    // ---------------------------------------------------------------

    @Test
    @DisplayName("[MR-9] Concatenation is associative")
    void concatAssociative() {
        Automaton left  = a().get().concatenate(b().get()).concatenate(BasicAutomata.makeChar('c'));
        Automaton right = a().get().concatenate(b().get().concatenate(BasicAutomata.makeChar('c')));
        assertTrue(sameLanguage(left, right));
    }

    // ---------------------------------------------------------------
    // Property 10 — Minimize preserves language
    // ---------------------------------------------------------------

    @Test
    @DisplayName("[MR-10] minimize(A) has the same language as A")
    void minimizePreservesLanguage() {
        Automaton original = abOr().get();
        Automaton minimized = original.clone();
        minimized.minimize();
        assertTrue(sameLanguage(original, minimized));
    }

    // ---------------------------------------------------------------
    // Property 11 — Minimize idempotence (structural)
    //   |minimize(A)| = |minimize(minimize(A))|
    // ---------------------------------------------------------------

    @Test
    @DisplayName("[MR-11] Running minimize twice does not shrink further")
    void minimizeIdempotent() {
        Automaton once = new RegExp("(a|a)+").toAutomaton(false);
        once.minimize();
        int stateCountOnce = once.getNumberOfStates();
        once.minimize();
        int stateCountTwice = once.getNumberOfStates();
        assertEquals(stateCountOnce, stateCountTwice);
    }

    // ---------------------------------------------------------------
    // Property 12 — Determinize preserves language
    // ---------------------------------------------------------------

    @Test
    @DisplayName("[MR-12] determinize(A) has the same language as A")
    void determinizePreservesLanguage() {
        Automaton original    = new RegExp("(a|b)*a").toAutomaton(false);
        Automaton determinized = original.clone();
        determinized.determinize();
        assertTrue(sameLanguage(original, determinized));
    }

    // ---------------------------------------------------------------
    // Property 13 — Union monotone: A ⊆ (A ∪ B) for any B
    // ---------------------------------------------------------------

    @Test
    @DisplayName("[MR-13] A ⊆ (A ∪ B)")
    void unionMonotone() {
        Automaton unionAB = a().get().union(b().get());
        assertTrue(a().get().subsetOf(unionAB));
    }

    // ---------------------------------------------------------------
    // Property 14 — Intersection contravariant: (A ∩ B) ⊆ A
    // ---------------------------------------------------------------

    @Test
    @DisplayName("[MR-14] (A ∩ B) ⊆ A")
    void intersectionSubset() {
        Automaton ab = a().get().union(b().get());        // {a, b}
        Automaton bc = b().get().union(BasicAutomata.makeChar('c'));  // {b, c}
        Automaton inter = ab.intersection(bc);            // {b}
        assertTrue(inter.subsetOf(ab));
        assertTrue(inter.subsetOf(bc));
    }

    // ---------------------------------------------------------------
    // Property 15 — Empty absorption / identity
    //   L(A ∩ ∅) = ∅   and   L(A ∪ ∅) = L(A)
    // ---------------------------------------------------------------

    @Test
    @DisplayName("[MR-15a] A ∩ ∅ is empty")
    void intersectionWithEmptyIsEmpty() {
        Automaton res = aStar().get().intersection(emptyLang().get());
        assertTrue(res.isEmpty());
    }

    @Test
    @DisplayName("[MR-15b] A ∪ ∅ has the same language as A")
    void unionWithEmptyIsIdentity() {
        assertTrue(sameLanguage(aStar().get(), aStar().get().union(emptyLang().get())));
    }

    // ---------------------------------------------------------------
    // Property 16 — Kleene star idempotence: L((A*)*) = L(A*)
    // ---------------------------------------------------------------

    @Test
    @DisplayName("[MR-16] (A*)* has the same language as A*")
    void kleeneStarIdempotent() {
        // For any language X, applying the Kleene star twice yields the
        // same language as applying it once — (X*)* = X*. This holds
        // whether X is a Kleene-star language already or a plus-form.
        //
        // We test the identity with X = a* (already-star) and with
        // X = (a|b)* (compound already-star), because the identity we
        // want is (X*)* = X*, not something like (X+)* = X+ which is
        // FALSE in general (plus doesn't accept empty, star does).
        Automaton aStar        = aStar().get();
        Automaton aStarStar    = aStar().get().repeat();
        assertTrue(sameLanguage(aStar, aStarStar));

        // Compound star form.
        Automaton abStar       = new RegExp("(a|b)*").toAutomaton();
        Automaton abStarStar   = new RegExp("(a|b)*").toAutomaton().repeat();
        assertTrue(sameLanguage(abStar, abStarStar));
    }

    // ---------------------------------------------------------------
    // Property 17 — Regex-language roundtrip through toString and back
    //   The exact regex string is not preserved (BRICS reformats), but
    //   the *language* recognised must be preserved. So:
    //     RegExp(pattern).toString() → parses back into an equivalent
    //     regex whose automaton has the same language.
    // ---------------------------------------------------------------

    @Test
    @DisplayName("[MR-17] Parsing a regex, printing it, and reparsing yields the same language")
    void regexRoundTrip() {
        String[] patterns = { "a", "a|b", "(a|b)*c", "a{2,5}", "[a-z]+" };
        for (String p : patterns) {
            RegExp original    = new RegExp(p);
            Automaton origAuto = original.toAutomaton();

            String printed         = original.toString();
            RegExp reparsed        = new RegExp(printed);
            Automaton reparsedAuto = reparsed.toAutomaton();

            assertTrue(sameLanguage(origAuto, reparsedAuto),
                    "regex roundtrip broke for pattern: " + p
                    + "  (printed as: " + printed + ")");
        }
    }

    // ---------------------------------------------------------------
    // Property 18 — Prefix-closure includes the empty string
    //   For any non-empty language A, prefixClose(A) must accept the
    //   empty string (which is a prefix of every string).
    // ---------------------------------------------------------------

    @Test
    @DisplayName("[MR-18] prefixClose(A) accepts the empty string for any non-empty A")
    void prefixCloseAcceptsEmpty() {
        Automaton a = BasicAutomata.makeString("abc");
        // Sanity: original doesn't accept the empty string.
        assertTrue(!a.run(""));

        Automaton pc = a.clone();
        pc.prefixClose();
        assertTrue(pc.run(""), "prefix-closure should accept '' — it's a prefix of every string in A");
        assertTrue(pc.run("a"),  "prefix-closure should accept 'a'");
        assertTrue(pc.run("ab"), "prefix-closure should accept 'ab'");
        assertTrue(pc.run("abc"), "prefix-closure should accept 'abc'");
    }
}
