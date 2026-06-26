package se.topics.t1;

import dk.brics.automaton.Automaton;
import dk.brics.automaton.RegExp;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Sanity check: verifies the BRICS library is on the classpath, compiles,
 * and that a trivial regex parses and matches as expected. If this fails
 * the build is wrong regardless of any other test outcome.
 */
class SmokeTest {

    @Test
    @DisplayName("BRICS RegExp compiles and recognizes a simple language")
    void bricsAcceptsSimpleRegex() {
        Automaton a = new RegExp("a(b|c)*").toAutomaton();
        assertTrue(a.run("a"));
        assertTrue(a.run("abc"));
        assertTrue(a.run("abbcbcbc"));
        assertFalse(a.run(""));
        assertFalse(a.run("xyz"));
    }
}
