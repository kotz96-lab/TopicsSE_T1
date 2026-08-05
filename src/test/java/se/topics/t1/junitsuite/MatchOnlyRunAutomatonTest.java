package se.topics.t1.junitsuite;

import dk.brics.automaton.MatchOnlyRunAutomaton;
import dk.brics.automaton.RegExp;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for {@link MatchOnlyRunAutomaton} — the linear-time substring
 * matcher (equivalent to matching {@code .*pattern}). Only reports
 * yes/no, no positions.
 */
class MatchOnlyRunAutomatonTest {

    private static MatchOnlyRunAutomaton compile(String regex) {
        return new MatchOnlyRunAutomaton(new RegExp(regex).toAutomaton());
    }

    @Test
    @DisplayName("matches finds the pattern anywhere in the input")
    void findsInMiddle() {
        MatchOnlyRunAutomaton m = compile("abc");
        assertTrue(m.matches("abc"));
        assertTrue(m.matches("xxabc"));
        assertTrue(m.matches("abcyy"));
        assertTrue(m.matches("xxabcyy"));
    }

    @Test
    @DisplayName("matches returns false when pattern is absent")
    void doesNotMatch() {
        MatchOnlyRunAutomaton m = compile("abc");
        assertFalse(m.matches("ab"));
        assertFalse(m.matches("xyz"));
        assertFalse(m.matches(""));
    }

    @Test
    @DisplayName("matches returns false for null input")
    void nullReturnsFalse() {
        MatchOnlyRunAutomaton m = compile("abc");
        assertFalse(m.matches(null));
        assertFalse(m.matches(null, 0));
    }

    @Test
    @DisplayName("matches with positive starting position skips leading characters")
    void matchesFromOffset() {
        MatchOnlyRunAutomaton m = compile("abc");
        // "zzabc" has "abc" starting at index 2 — searching from 2 finds it,
        // searching from 3 doesn't (only "bc" left).
        assertTrue(m.matches("zzabc", 2));
        assertFalse(m.matches("zzabc", 3));
    }

    @Test
    @DisplayName("matches throws IllegalArgumentException for negative pos")
    void negativePosThrows() {
        MatchOnlyRunAutomaton m = compile("abc");
        assertThrows(IllegalArgumentException.class, () -> m.matches("hello", -1));
    }

    @Test
    @DisplayName("matches throws IllegalArgumentException for pos beyond input length")
    void tooLargePosThrows() {
        MatchOnlyRunAutomaton m = compile("abc");
        assertThrows(IllegalArgumentException.class, () -> m.matches("hi", 5));
    }

    @Test
    @DisplayName("Construction rejects null automaton (NullPointerException)")
    void nullAutomatonRejected() {
        assertThrows(NullPointerException.class, () -> new MatchOnlyRunAutomaton(null));
    }

    @Test
    @DisplayName("store/load round-trips a MatchOnlyRunAutomaton")
    void storeLoadRoundtrip() throws IOException, ClassNotFoundException {
        MatchOnlyRunAutomaton original = compile("hello");
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        original.store(out);

        MatchOnlyRunAutomaton loaded =
                MatchOnlyRunAutomaton.load(new ByteArrayInputStream(out.toByteArray()));
        assertNotNull(loaded);
        assertTrue(loaded.matches("say hello world"));
        assertFalse(loaded.matches("goodbye"));
    }

    @Test
    @DisplayName("toString delegates to the underlying RunAutomaton")
    void toStringNonNull() {
        assertNotNull(compile("a").toString());
    }
}
