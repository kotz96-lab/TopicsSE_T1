package se.topics.t1.junitsuite;

import dk.brics.automaton.AutomatonMatcher;
import dk.brics.automaton.RegExp;
import dk.brics.automaton.RunAutomaton;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for {@link AutomatonMatcher} — the {@code java.util.regex.Matcher}-alike
 * returned by {@link RunAutomaton#newMatcher}. Behaviour tested: iterative
 * {@code find()}, positional {@code start/end}, {@code group}, {@code toMatchResult},
 * and the "no match yet" error paths.
 */
class AutomatonMatcherTest {

    private static RunAutomaton compile(String regex) {
        return new RunAutomaton(new RegExp(regex).toAutomaton());
    }

    @Test
    @DisplayName("find() returns true when the input contains a match and locates it")
    void findLocatesSingleMatch() {
        AutomatonMatcher m = compile("ab").newMatcher("xxabyy");
        assertTrue(m.find());
        assertEquals(2, m.start());
        assertEquals(4, m.end());
        assertEquals("ab", m.group());
    }

    @Test
    @DisplayName("find() advances across multiple matches")
    void findIterates() {
        AutomatonMatcher m = compile("a").newMatcher("aXaXa");
        int hits = 0;
        while (m.find()) hits++;
        assertEquals(3, hits);
    }

    @Test
    @DisplayName("find() returns false when there is no match")
    void findNoMatch() {
        AutomatonMatcher m = compile("zzz").newMatcher("hello world");
        assertFalse(m.find());
    }

    @Test
    @DisplayName("start()/end()/group() throw IllegalStateException before find()")
    void beforeFindThrows() {
        AutomatonMatcher m = compile("a").newMatcher("abc");
        assertThrows(IllegalStateException.class, m::start);
        assertThrows(IllegalStateException.class, m::end);
        assertThrows(IllegalStateException.class, m::group);
    }

    @Test
    @DisplayName("groupCount() is 0 (BRICS does not support capture groups)")
    void groupCountIsZero() {
        AutomatonMatcher m = compile("a").newMatcher("a");
        assertEquals(0, m.groupCount());
    }

    @Test
    @DisplayName("group(0) after find() returns the whole match; other indices throw")
    void groupWithIndex() {
        AutomatonMatcher m = compile("ab").newMatcher("zzab");
        assertTrue(m.find());
        assertEquals("ab", m.group(0));
        assertThrows(IndexOutOfBoundsException.class, () -> m.group(1));
    }

    @Test
    @DisplayName("toMatchResult() returns a snapshot with the same start/end/group")
    void toMatchResultSnapshot() {
        AutomatonMatcher m = compile("ab").newMatcher("zzabyy");
        assertTrue(m.find());
        var snap = m.toMatchResult();
        assertNotNull(snap);
        assertEquals(m.start(), snap.start());
        assertEquals(m.end(), snap.end());
        assertEquals(m.group(), snap.group());
    }
}
