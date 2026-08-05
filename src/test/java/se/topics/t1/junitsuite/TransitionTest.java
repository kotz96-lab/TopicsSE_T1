package se.topics.t1.junitsuite;

import dk.brics.automaton.State;
import dk.brics.automaton.Transition;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * Tests for {@link Transition} — labelled edges in the automaton. Covers
 * the single-char and range constructors, accessors, equality (based on
 * {@code min}, {@code max}, and destination), hashCode, cloning, and
 * {@code toString}.
 */
class TransitionTest {

    @Test
    @DisplayName("single-char constructor sets min == max")
    void singleCharCtor() {
        State to = new State();
        Transition t = new Transition('a', to);
        assertEquals('a', t.getMin());
        assertEquals('a', t.getMax());
        assertSame(to, t.getDest());
    }

    @Test
    @DisplayName("range constructor exposes min/max/destination via accessors")
    void rangeCtor() {
        State to = new State();
        Transition t = new Transition('a', 'z', to);
        assertEquals('a', t.getMin());
        assertEquals('z', t.getMax());
        assertSame(to, t.getDest());
    }

    @Test
    @DisplayName("range constructor swaps min and max when passed in reversed order")
    void reversedRangeIsNormalized() {
        State to = new State();
        Transition t = new Transition('z', 'a', to);
        assertEquals('a', t.getMin());
        assertEquals('z', t.getMax());
    }

    @Test
    @DisplayName("two transitions with same min/max/dest are equal and share hashCode")
    void equalsAndHash() {
        State to = new State();
        Transition t1 = new Transition('a', 'c', to);
        Transition t2 = new Transition('a', 'c', to);
        assertEquals(t1, t2);
        assertEquals(t1.hashCode(), t2.hashCode());
    }

    @Test
    @DisplayName("transitions with different destinations are not equal")
    void notEqualDifferentDest() {
        Transition t1 = new Transition('a', new State());
        Transition t2 = new Transition('a', new State());
        assertNotEquals(t1, t2);
    }

    @Test
    @DisplayName("transitions with different range are not equal")
    void notEqualDifferentRange() {
        State to = new State();
        Transition t1 = new Transition('a', 'b', to);
        Transition t2 = new Transition('a', 'c', to);
        assertNotEquals(t1, t2);
    }

    @Test
    @DisplayName("equals(null) and equals(other-type) are false")
    void equalsBadInputs() {
        Transition t = new Transition('a', new State());
        assertNotEquals(t, null);
        assertNotEquals(t, "string");
    }

    @Test
    @DisplayName("clone returns an equal transition")
    void cloneEqual() {
        State to = new State();
        Transition t = new Transition('a', 'c', to);
        Transition c = t.clone();
        assertEquals(t, c);
    }

    @Test
    @DisplayName("toString is non-null")
    void toStringNonNull() {
        Transition t = new Transition('a', 'z', new State());
        assertNotNull(t.toString());
    }
}
