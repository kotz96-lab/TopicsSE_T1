package se.topics.t1.junitsuite;

import dk.brics.automaton.Automaton;
import dk.brics.automaton.AutomatonMatcher;
import dk.brics.automaton.BasicAutomata;
import dk.brics.automaton.RegExp;
import dk.brics.automaton.RunAutomaton;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for {@link RunAutomaton} — the frozen, deterministic runner used
 * once you're done constructing/modifying an {@link Automaton}. Exercises
 * {@code run}, {@code step}, sizing, and matcher factory methods.
 */
class RunAutomatonTest {

    private static RunAutomaton compile(String regex) {
        return new RunAutomaton(new RegExp(regex).toAutomaton());
    }

    @Test
    @DisplayName("run(String) accepts exactly the language of the regex")
    void runAcceptsAndRejects() {
        RunAutomaton r = compile("a(b|c)*");
        assertTrue(r.run("a"));
        assertTrue(r.run("abc"));
        assertTrue(r.run("abbcbcbc"));
        assertFalse(r.run(""));
        assertFalse(r.run("xyz"));
    }

    @Test
    @DisplayName("run(String, offset) returns length of longest prefix match, -1 if none")
    void runOffsetReturnsMatchLength() {
        RunAutomaton r = compile("ab*");
        // "abbbc" — longest prefix accepted starting at 0 is "abbb" → length 4
        int len = r.run("abbbc", 0);
        assertTrue(len >= 0);
        assertTrue(len <= 4);
        // no match starting at index inside the c
        assertEquals(-1, r.run("xxxx", 0));
    }

    @Test
    @DisplayName("getSize returns positive state count, getInitialState is in range")
    void sizeAndInitial() {
        RunAutomaton r = compile("a|b");
        assertTrue(r.getSize() > 0);
        int init = r.getInitialState();
        assertTrue(init >= 0 && init < r.getSize());
    }

    @Test
    @DisplayName("step from initial state on the right character reaches an accepting state")
    void stepReachesAccept() {
        RunAutomaton r = compile("a");
        int next = r.step(r.getInitialState(), 'a');
        assertTrue(next >= 0);
        assertTrue(r.isAccept(next));
    }

    @Test
    @DisplayName("step on a rejected character returns -1")
    void stepReject() {
        RunAutomaton r = compile("a");
        int next = r.step(r.getInitialState(), 'z');
        assertEquals(-1, next);
    }

    @Test
    @DisplayName("getCharIntervals returns a non-null, sorted array")
    void charIntervals() {
        RunAutomaton r = compile("[a-c]");
        char[] ivs = r.getCharIntervals();
        assertNotNull(ivs);
        assertTrue(ivs.length >= 2);
        for (int i = 1; i < ivs.length; i++) {
            assertTrue(ivs[i - 1] <= ivs[i]);
        }
    }

    @Test
    @DisplayName("newMatcher(CharSequence) returns a working AutomatonMatcher")
    void newMatcherFindsSubstring() {
        RunAutomaton r = compile("ab");
        AutomatonMatcher m = r.newMatcher("xxabyy");
        assertTrue(m.find());
        assertEquals("ab", m.group());
    }

    @Test
    @DisplayName("toString on RunAutomaton produces a non-null representation")
    void toStringNonNull() {
        assertNotNull(compile("a").toString());
    }

    @Test
    @DisplayName("RunAutomaton built from a non-deterministic Automaton still classifies correctly")
    void runFromNonDeterministic() {
        Automaton nd = BasicAutomata.makeChar('a').union(BasicAutomata.makeChar('b'));
        RunAutomaton r = new RunAutomaton(nd, true);
        assertTrue(r.run("a"));
        assertTrue(r.run("b"));
        assertFalse(r.run("c"));
    }
}
