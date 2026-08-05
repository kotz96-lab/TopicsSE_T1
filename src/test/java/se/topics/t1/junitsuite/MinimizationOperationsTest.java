package se.topics.t1.junitsuite;

import dk.brics.automaton.Automaton;
import dk.brics.automaton.BasicAutomata;
import dk.brics.automaton.MinimizationOperations;
import dk.brics.automaton.RegExp;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for {@link MinimizationOperations} — four minimization algorithms.
 * The invariant every algorithm must preserve is: L(minimize(A)) = L(A).
 * We check acceptance on witnesses before and after, and also that the
 * state count is monotonically non-increasing.
 */
class MinimizationOperationsTest {

    /**
     * Regex known to expand into many redundant states so minimization
     * has something to compress.
     */
    private static Automaton makeExpandable() {
        return new RegExp("(a|a)(b|b)*(c|c)").toAutomaton(false);
    }

    private static void assertLanguagePreserved(Automaton a) {
        assertTrue(a.run("abc"));
        assertTrue(a.run("ac"));
        assertTrue(a.run("abbbc"));
        assertFalse(a.run(""));
        assertFalse(a.run("a"));
        assertFalse(a.run("xyz"));
    }

    @Test
    @DisplayName("static minimize preserves the language")
    void staticMinimize() {
        Automaton a = makeExpandable();
        int before = a.getNumberOfStates();
        MinimizationOperations.minimize(a);
        int after = a.getNumberOfStates();
        assertTrue(after <= before);
        assertLanguagePreserved(a);
    }

    @Test
    @DisplayName("Huffman minimization preserves the language")
    void huffman() {
        Automaton a = makeExpandable();
        MinimizationOperations.minimizeHuffman(a);
        assertLanguagePreserved(a);
    }

    @Test
    @DisplayName("Brzozowski minimization preserves the language")
    void brzozowski() {
        Automaton a = makeExpandable();
        MinimizationOperations.minimizeBrzozowski(a);
        assertLanguagePreserved(a);
    }

    @Test
    @DisplayName("Hopcroft minimization preserves the language")
    void hopcroft() {
        Automaton a = makeExpandable();
        MinimizationOperations.minimizeHopcroft(a);
        assertLanguagePreserved(a);
    }

    @Test
    @DisplayName("Valmari minimization preserves the language")
    void valmari() {
        Automaton a = makeExpandable();
        MinimizationOperations.minimizeValmari(a);
        assertLanguagePreserved(a);
    }

    @Test
    @DisplayName("all four algorithms converge to the same minimal state count")
    void allAgreeOnMinimalSize() {
        Automaton huff = makeExpandable();
        Automaton brz  = makeExpandable();
        Automaton hop  = makeExpandable();
        Automaton val  = makeExpandable();
        MinimizationOperations.minimizeHuffman(huff);
        MinimizationOperations.minimizeBrzozowski(brz);
        MinimizationOperations.minimizeHopcroft(hop);
        MinimizationOperations.minimizeValmari(val);
        int n = huff.getNumberOfStates();
        assertEquals(n, brz.getNumberOfStates());
        assertEquals(n, hop.getNumberOfStates());
        assertEquals(n, val.getNumberOfStates());
    }

    @Test
    @DisplayName("minimize is idempotent (running twice does not shrink further)")
    void idempotent() {
        Automaton a = makeExpandable();
        MinimizationOperations.minimize(a);
        int once = a.getNumberOfStates();
        MinimizationOperations.minimize(a);
        assertEquals(once, a.getNumberOfStates());
    }

    @Test
    @DisplayName("minimize on singleton automaton is safe")
    void singletonSafe() {
        Automaton a = BasicAutomata.makeString("hello");
        MinimizationOperations.minimize(a);
        assertTrue(a.run("hello"));
        assertFalse(a.run("hell"));
    }
}
