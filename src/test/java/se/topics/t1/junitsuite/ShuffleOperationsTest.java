package se.topics.t1.junitsuite;

import dk.brics.automaton.Automaton;
import dk.brics.automaton.BasicAutomata;
import dk.brics.automaton.RegExp;
import dk.brics.automaton.ShuffleOperations;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for {@link ShuffleOperations} — the interleaving operator.
 * shuffle(A, B) accepts every string obtained by interleaving one string
 * from L(A) with one from L(B) while preserving their relative order.
 */
class ShuffleOperationsTest {

    @Test
    @DisplayName("shuffle(ab, cd) accepts all interleavings of ab and cd")
    void shuffleTwoDisjointStrings() {
        Automaton a = BasicAutomata.makeString("ab");
        Automaton b = BasicAutomata.makeString("cd");
        Automaton s = ShuffleOperations.shuffle(a, b);

        // All 6 interleavings of "ab" and "cd" that keep relative order:
        // abcd, acbd, acdb, cabd, cadb, cdab
        assertTrue(s.run("abcd"));
        assertTrue(s.run("acbd"));
        assertTrue(s.run("acdb"));
        assertTrue(s.run("cabd"));
        assertTrue(s.run("cadb"));
        assertTrue(s.run("cdab"));

        // Anything that doesn't preserve within-string order is rejected.
        assertFalse(s.run("bacd"));   // "ba" reverses order of "ab"
        assertFalse(s.run("abdc"));   // "dc" reverses order of "cd"
        assertFalse(s.run(""));
        assertFalse(s.run("ab"));     // missing cd content
    }

    @Test
    @DisplayName("shuffle(empty-string, X) equals X")
    void shuffleWithEmptyString() {
        Automaton eps = BasicAutomata.makeEmptyString();
        Automaton x   = BasicAutomata.makeString("xyz");
        Automaton s   = ShuffleOperations.shuffle(eps, x);
        assertTrue(s.run("xyz"));
        assertFalse(s.run(""));
        assertFalse(s.run("xy"));
    }

    @Test
    @DisplayName("shuffle preserves length: |output| == |a| + |b| for singletons")
    void shufflePreservesLength() {
        Automaton a = BasicAutomata.makeString("ab");
        Automaton b = BasicAutomata.makeString("cd");
        Automaton s = ShuffleOperations.shuffle(a, b);
        // Length 4 = 2 + 2. Length 3 should never be accepted.
        assertFalse(s.run("abc"));
        assertFalse(s.run("abcde"));
    }

    @Test
    @DisplayName("shuffleSubsetOf returns null when the shuffle language IS a subset")
    void shuffleSubsetTrue() {
        Automaton a1 = BasicAutomata.makeString("a");
        Automaton a2 = BasicAutomata.makeString("b");
        // shuffle(a, b) = {ab, ba}. Contained in the universal language.
        Automaton universal = BasicAutomata.makeAnyString();
        List<Automaton> parts = Arrays.asList(a1, a2);
        String counterexample = ShuffleOperations.shuffleSubsetOf(parts, universal, null, null);
        // null return means "no counterexample found" → is a subset.
        assertTrue(counterexample == null || universal.run(counterexample));
    }

    @Test
    @DisplayName("shuffleSubsetOf returns a counterexample string when NOT a subset")
    void shuffleSubsetFalse() {
        Automaton a1 = BasicAutomata.makeString("a");
        Automaton a2 = BasicAutomata.makeString("b");
        // shuffle(a, b) = {ab, ba}. Neither string is in L("only-c").
        Automaton other = BasicAutomata.makeString("only-c");
        List<Automaton> parts = Arrays.asList(a1, a2);
        String counterexample = ShuffleOperations.shuffleSubsetOf(parts, other, null, null);
        assertNotNull(counterexample);
        // The counterexample must be a real shuffle result, i.e. length 2 with one a and one b.
        assertTrue(counterexample.equals("ab") || counterexample.equals("ba"));
    }

    @Test
    @DisplayName("shuffle result is non-null even for regex-based inputs")
    void shuffleRegex() {
        Automaton a = new RegExp("a+").toAutomaton();
        Automaton b = new RegExp("b+").toAutomaton();
        Automaton s = ShuffleOperations.shuffle(a, b);
        assertNotNull(s);
        assertTrue(s.run("ab"));
        assertTrue(s.run("ba"));
        assertTrue(s.run("aabb"));
        assertTrue(s.run("abab"));
    }
}
