package se.topics.t1.junitsuite;

import dk.brics.automaton.Automaton;
import dk.brics.automaton.RegExp;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for {@link RegExp} — the BRICS regex parser and its conversion to
 * {@link Automaton}. Uses the built automaton's {@code run(String)} as the
 * behavioural oracle: if the regex parsed and the resulting automaton
 * accepts/rejects the expected strings, the parse was correct.
 */
class RegExpTest {

    @Nested
    @DisplayName("Basic literal and empty patterns")
    class Literals {

        @Test
        @DisplayName("single-character literal accepts only that character")
        void singleChar() {
            Automaton a = new RegExp("a").toAutomaton();
            assertTrue(a.run("a"));
            assertFalse(a.run(""));
            assertFalse(a.run("b"));
            assertFalse(a.run("aa"));
        }

        @Test
        @DisplayName("multi-character literal accepts only that exact string")
        void multiChar() {
            Automaton a = new RegExp("abc").toAutomaton();
            assertTrue(a.run("abc"));
            assertFalse(a.run(""));
            assertFalse(a.run("ab"));
            assertFalse(a.run("abcd"));
        }

        @Test
        @DisplayName("empty-language regex accepts nothing")
        void emptyLanguage() {
            Automaton a = new RegExp("#").toAutomaton();
            assertFalse(a.run(""));
            assertFalse(a.run("a"));
            assertTrue(a.isEmpty());
        }

        @Test
        @DisplayName("any-string regex accepts everything")
        void anyString() {
            Automaton a = new RegExp("@").toAutomaton();
            assertTrue(a.run(""));
            assertTrue(a.run("a"));
            assertTrue(a.run("hello world"));
            assertTrue(a.isTotal());
        }
    }

    @Nested
    @DisplayName("Regex operators")
    class Operators {

        @Test
        @DisplayName("alternation | accepts either alternative")
        void alternation() {
            Automaton a = new RegExp("a|b").toAutomaton();
            assertTrue(a.run("a"));
            assertTrue(a.run("b"));
            assertFalse(a.run("c"));
            assertFalse(a.run("ab"));
        }

        @Test
        @DisplayName("Kleene star * accepts zero or more repetitions")
        void star() {
            Automaton a = new RegExp("a*").toAutomaton();
            assertTrue(a.run(""));
            assertTrue(a.run("a"));
            assertTrue(a.run("aaaa"));
            assertFalse(a.run("b"));
            assertFalse(a.run("ab"));
        }

        @Test
        @DisplayName("plus + accepts one or more repetitions")
        void plus() {
            Automaton a = new RegExp("a+").toAutomaton();
            assertFalse(a.run(""));
            assertTrue(a.run("a"));
            assertTrue(a.run("aaaa"));
            assertFalse(a.run("b"));
        }

        @Test
        @DisplayName("optional ? accepts zero or one occurrence")
        void optional() {
            Automaton a = new RegExp("a?").toAutomaton();
            assertTrue(a.run(""));
            assertTrue(a.run("a"));
            assertFalse(a.run("aa"));
        }

        @Test
        @DisplayName("bounded repetition {m,n}")
        void boundedRepetition() {
            Automaton a = new RegExp("a{2,4}").toAutomaton();
            assertFalse(a.run("a"));
            assertTrue(a.run("aa"));
            assertTrue(a.run("aaa"));
            assertTrue(a.run("aaaa"));
            assertFalse(a.run("aaaaa"));
        }

        @Test
        @DisplayName("exact repetition {n}")
        void exactRepetition() {
            Automaton a = new RegExp("a{3}").toAutomaton();
            assertFalse(a.run("aa"));
            assertTrue(a.run("aaa"));
            assertFalse(a.run("aaaa"));
        }

        @Test
        @DisplayName("grouped concatenation with quantifier: (ab)+")
        void groupedRepetition() {
            Automaton a = new RegExp("(ab)+").toAutomaton();
            assertFalse(a.run(""));
            assertTrue(a.run("ab"));
            assertTrue(a.run("abab"));
            assertFalse(a.run("aba"));
            assertFalse(a.run("a"));
        }
    }

    @Nested
    @DisplayName("Character classes and ranges")
    class CharClasses {

        @Test
        @DisplayName("character range [a-c] accepts each char in the range")
        void charRange() {
            Automaton a = new RegExp("[a-c]").toAutomaton();
            assertTrue(a.run("a"));
            assertTrue(a.run("b"));
            assertTrue(a.run("c"));
            assertFalse(a.run("d"));
            assertFalse(a.run(""));
        }

        @Test
        @DisplayName("negated character class [^a-c] rejects chars in the range")
        void negatedCharClass() {
            Automaton a = new RegExp("[^a-c]").toAutomaton();
            assertFalse(a.run("a"));
            assertFalse(a.run("b"));
            assertFalse(a.run("c"));
            assertTrue(a.run("d"));
            assertTrue(a.run("z"));
        }

        @Test
        @DisplayName("dot . accepts any single character")
        void dot() {
            Automaton a = new RegExp(".").toAutomaton();
            assertTrue(a.run("a"));
            assertTrue(a.run("Z"));
            assertTrue(a.run("9"));
            assertFalse(a.run(""));
            assertFalse(a.run("ab"));
        }
    }

    @Nested
    @DisplayName("Advanced BRICS operators (intersection, complement)")
    class Advanced {

        @Test
        @DisplayName("intersection & yields only strings accepted by both")
        void intersection() {
            Automaton a = new RegExp("(a|b)&(b|c)").toAutomaton();
            assertTrue(a.run("b"));
            assertFalse(a.run("a"));
            assertFalse(a.run("c"));
        }

        @Test
        @DisplayName("complement ~ inverts language")
        void complement() {
            Automaton a = new RegExp("~(a)").toAutomaton();
            assertFalse(a.run("a"));
            assertTrue(a.run(""));
            assertTrue(a.run("b"));
            assertTrue(a.run("aa"));
        }

        @Test
        @DisplayName("disabling INTERSECTION via syntax flags treats & as a literal char")
        void syntaxFlagsDisableIntersection() {
            // With intersection enabled, "a&b" means the intersection of {a} and {b}
            // (which is empty). With intersection disabled, "a&b" should be a literal
            // three-character string. We check that the language differs.
            Automaton withIntersection    = new RegExp("a&b", RegExp.ALL).toAutomaton();
            Automaton withoutIntersection = new RegExp("a&b", RegExp.ALL & ~RegExp.INTERSECTION).toAutomaton();
            assertTrue(withIntersection.isEmpty());
            assertTrue(withoutIntersection.run("a&b"));
        }
    }

    @Nested
    @DisplayName("Constructor error handling")
    class Errors {

        @Test
        @DisplayName("dangling closing paren throws IllegalArgumentException")
        void unmatchedParen() {
            assertThrows(IllegalArgumentException.class, () -> new RegExp("a)"));
        }

        @Test
        @DisplayName("dangling opening paren throws IllegalArgumentException")
        void openParen() {
            assertThrows(IllegalArgumentException.class, () -> new RegExp("(a"));
        }
    }

    @Nested
    @DisplayName("Meta methods")
    class Meta {

        @Test
        @DisplayName("toString() returns a non-null representation")
        void toStringNonNull() {
            assertNotNull(new RegExp("a(b|c)*").toString());
        }

        @Test
        @DisplayName("toAutomaton(minimize=false) still produces a working automaton")
        void toAutomatonUnminimized() {
            Automaton a = new RegExp("a|b").toAutomaton(false);
            assertTrue(a.run("a"));
            assertTrue(a.run("b"));
            assertFalse(a.run("c"));
        }

        @Test
        @DisplayName("getIdentifiers() on regex with named automaton reference returns the name")
        void identifiers() {
            Set<String> ids = new RegExp("<foo>").getIdentifiers();
            assertTrue(ids.contains("foo"));
        }
    }
}
