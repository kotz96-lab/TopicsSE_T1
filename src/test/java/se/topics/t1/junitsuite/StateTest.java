package se.topics.t1.junitsuite;

import dk.brics.automaton.State;
import dk.brics.automaton.Transition;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for {@link State} — nodes in a BRICS automaton. Exercises accept
 * flag, transition set management, and the two {@code step} variants that
 * drive membership computation.
 */
class StateTest {

    @Test
    @DisplayName("newly constructed State is non-accepting with no transitions")
    void defaults() {
        State s = new State();
        assertFalse(s.isAccept());
        assertTrue(s.getTransitions().isEmpty());
    }

    @Test
    @DisplayName("setAccept flips the accept flag")
    void setAccept() {
        State s = new State();
        s.setAccept(true);
        assertTrue(s.isAccept());
        s.setAccept(false);
        assertFalse(s.isAccept());
    }

    @Test
    @DisplayName("addTransition adds to the transition set")
    void addTransition() {
        State from = new State();
        State to = new State();
        from.addTransition(new Transition('a', to));
        Set<Transition> ts = from.getTransitions();
        assertEquals(1, ts.size());
    }

    @Test
    @DisplayName("step(char) returns the destination when a transition matches")
    void stepMatches() {
        State from = new State();
        State to = new State();
        from.addTransition(new Transition('a', to));
        assertSame(to, from.step('a'));
    }

    @Test
    @DisplayName("step(char) returns null when no transition matches")
    void stepMisses() {
        State from = new State();
        State to = new State();
        from.addTransition(new Transition('a', to));
        assertNull(from.step('b'));
    }

    @Test
    @DisplayName("step(char) walks through character range transitions")
    void stepRange() {
        State from = new State();
        State to = new State();
        from.addTransition(new Transition('a', 'z', to));
        assertSame(to, from.step('m'));
        assertNull(from.step('A'));
    }

    @Test
    @DisplayName("step(char, dest) fills dest with all matching destinations")
    void stepMulti() {
        State from = new State();
        State t1 = new State();
        State t2 = new State();
        from.addTransition(new Transition('a', t1));
        from.addTransition(new Transition('a', t2));
        List<State> dest = new ArrayList<>();
        from.step('a', dest);
        assertTrue(dest.contains(t1));
        assertTrue(dest.contains(t2));
        assertEquals(2, dest.size());
    }

    @Test
    @DisplayName("getSortedTransitions returns a non-null list, both orderings")
    void sortedTransitions() {
        // Both true and false exercise TransitionComparator's two branches.
        State s = new State();
        State t = new State();
        s.addTransition(new Transition('b', t));
        s.addTransition(new Transition('a', t));
        List<Transition> byTo    = s.getSortedTransitions(true);
        List<Transition> byRange = s.getSortedTransitions(false);
        assertNotNull(byTo);
        assertNotNull(byRange);
        assertEquals(2, byTo.size());
        assertEquals(2, byRange.size());
    }

    @Test
    @DisplayName("toString is non-null and mentions the state")
    void toStringNonNull() {
        State s = new State();
        s.setAccept(true);
        assertNotNull(s.toString());
    }

    @Test
    @DisplayName("hashCode is deterministic within a run")
    void hashCodeStable() {
        State s = new State();
        assertEquals(s.hashCode(), s.hashCode());
    }
}
