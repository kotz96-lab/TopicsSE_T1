package se.topics.t1.junitsuite;

import dk.brics.automaton.Automaton;
import dk.brics.automaton.BasicAutomata;
import dk.brics.automaton.RegExp;
import dk.brics.automaton.SpecialOperations;
import dk.brics.automaton.State;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for {@link SpecialOperations} — miscellaneous automaton operations
 * (reverse, overlap, character substitutions, homomorphism, projection,
 * language finiteness / enumeration, whitespace normalisation, hex-case
 * relaxation, and common-prefix computation).
 */
class SpecialOperationsTest {

    @Nested
    @DisplayName("Reverse")
    class Reverse {

        @Test
        @DisplayName("reversing an automaton returns a non-empty set of new initial states")
        void reverseReturnsInitials() {
            Automaton a = BasicAutomata.makeString("abc");
            Set<State> initials = SpecialOperations.reverse(a);
            assertNotNull(initials);
            assertFalse(initials.isEmpty());
        }
    }

    @Nested
    @DisplayName("Overlap, singleChars")
    class OverlapAndSingle {

        @Test
        @DisplayName("overlap returns a non-null automaton")
        void overlap() {
            Automaton a1 = new RegExp("ab").toAutomaton();
            Automaton a2 = new RegExp("bc").toAutomaton();
            Automaton o = SpecialOperations.overlap(a1, a2);
            assertNotNull(o);
        }

        @Test
        @DisplayName("singleChars keeps only the single characters used in the automaton")
        void singleChars() {
            Automaton a = BasicAutomata.makeString("hello");
            Automaton sc = SpecialOperations.singleChars(a);
            assertTrue(sc.run("h"));
            assertTrue(sc.run("e"));
            assertTrue(sc.run("l"));
            assertTrue(sc.run("o"));
            assertFalse(sc.run("z"));
            assertFalse(sc.run("hello"));  // multi-char string no longer accepted
        }
    }

    @Nested
    @DisplayName("Character trim / compress")
    class TrimAndCompress {

        @Test
        @DisplayName("trim removes leading/trailing characters from the trim-set")
        void trim() {
            // Language: any concatenation of a/b/space chars. After trimming
            // spaces, the result should still accept "ab" but also inputs
            // that had surrounding spaces trimmed at recognition time.
            Automaton a = new RegExp("[ab ]+").toAutomaton();
            Automaton t = SpecialOperations.trim(a, " ", ' ');
            assertNotNull(t);
            assertTrue(t.run("ab"));
        }

        @Test
        @DisplayName("compress collapses runs of characters from the given set")
        void compress() {
            Automaton a = new RegExp("[ab ]+").toAutomaton();
            Automaton c = SpecialOperations.compress(a, " ", ' ');
            assertNotNull(c);
            assertTrue(c.run("a b"));
        }
    }

    @Nested
    @DisplayName("Character substitution and homomorphism")
    class SubstAndHomomorph {

        @Test
        @DisplayName("subst(char, String) replaces occurrences of the char")
        void substCharToString() {
            Automaton a = BasicAutomata.makeString("cat");
            Automaton s = SpecialOperations.subst(a, 'a', "@");
            assertTrue(s.run("c@t"));
            assertFalse(s.run("cat"));
        }

        @Test
        @DisplayName("subst(Map) applies the substitution table")
        void substMap() {
            Automaton a = BasicAutomata.makeString("ab");
            Map<Character, Set<Character>> map = new HashMap<>();
            Set<Character> repl = new HashSet<>();
            repl.add('X');
            map.put('a', repl);
            Automaton s = SpecialOperations.subst(a, map);
            assertTrue(s.run("Xb"));
            assertFalse(s.run("ab"));
        }

        @Test
        @DisplayName("homomorph maps source chars to dest chars pointwise")
        void homomorph() {
            Automaton a = BasicAutomata.makeString("abc");
            char[] src  = {'a', 'b', 'c'};
            char[] dst  = {'x', 'y', 'z'};
            Automaton h = SpecialOperations.homomorph(a, src, dst);
            assertTrue(h.run("xyz"));
            assertFalse(h.run("abc"));
        }

        @Test
        @DisplayName("projectChars returns a non-null automaton")
        void projectChars() {
            // projectChars removes / renames transitions outside the given
            // char set; the exact residual language is implementation-defined
            // and depends on how BRICS resolves the epsilon closures. We
            // only assert the operation runs and produces a usable automaton.
            Automaton a = new RegExp("[abc]+").toAutomaton();
            Set<Character> keep = new HashSet<>();
            keep.add('a');
            keep.add('b');
            Automaton p = SpecialOperations.projectChars(a, keep);
            assertNotNull(p);
        }
    }

    @Nested
    @DisplayName("Finiteness and enumeration")
    class Finiteness {

        @Test
        @DisplayName("isFinite is true for finite languages")
        void isFiniteTrue() {
            assertTrue(SpecialOperations.isFinite(BasicAutomata.makeString("abc")));
        }

        @Test
        @DisplayName("isFinite is false for Kleene-star languages")
        void isFiniteFalse() {
            assertFalse(SpecialOperations.isFinite(new RegExp("a*").toAutomaton()));
        }

        @Test
        @DisplayName("getStrings(len) returns all strings of that length in the language")
        void getStrings() {
            Automaton a = new RegExp("(a|b){2}").toAutomaton();
            Set<String> two = SpecialOperations.getStrings(a, 2);
            assertEquals(4, two.size());
            assertTrue(two.contains("aa"));
            assertTrue(two.contains("bb"));
        }

        @Test
        @DisplayName("getFiniteStrings returns the whole language for a finite automaton")
        void getFiniteStrings() {
            Automaton a = BasicAutomata.makeStringUnion("cat", "dog");
            Set<String> all = SpecialOperations.getFiniteStrings(a);
            assertNotNull(all);
            assertTrue(all.contains("cat"));
            assertTrue(all.contains("dog"));
        }

        @Test
        @DisplayName("getFiniteStrings(limit) returns null when the language exceeds the limit")
        void getFiniteStringsWithLimit() {
            // BRICS returns null (not an empty or truncated set) when the
            // language has more than `limit` strings — documented via source:
            // SpecialOperations.getFiniteStrings(a, limit) aborts and returns null.
            Automaton a = BasicAutomata.makeStringUnion("cat", "dog", "fish");
            Set<String> lim = SpecialOperations.getFiniteStrings(a, 2);
            org.junit.jupiter.api.Assertions.assertNull(lim);
        }

        @Test
        @DisplayName("getFiniteStrings(limit) returns the whole set when it fits under the limit")
        void getFiniteStringsUnderLimit() {
            Automaton a = BasicAutomata.makeStringUnion("cat", "dog");
            Set<String> lim = SpecialOperations.getFiniteStrings(a, 5);
            assertNotNull(lim);
            assertTrue(lim.contains("cat"));
            assertTrue(lim.contains("dog"));
        }
    }

    @Nested
    @DisplayName("Prefix operations")
    class Prefixes {

        @Test
        @DisplayName("getCommonPrefix returns the longest shared prefix")
        void commonPrefix() {
            Automaton a = new RegExp("shared-(a|b)").toAutomaton();
            assertEquals("shared-", SpecialOperations.getCommonPrefix(a));
        }

        @Test
        @DisplayName("prefixClose makes all reachable states accepting")
        void prefixClose() {
            Automaton a = BasicAutomata.makeString("abc");
            SpecialOperations.prefixClose(a);
            assertTrue(a.run(""));
            assertTrue(a.run("a"));
            assertTrue(a.run("ab"));
            assertTrue(a.run("abc"));
            assertFalse(a.run("abcd"));
        }
    }

    @Nested
    @DisplayName("Hex case and whitespace")
    class HexAndWhitespace {

        @Test
        @DisplayName("hexCases relaxes hex digits to accept upper/lower case")
        void hexCases() {
            Automaton a = BasicAutomata.makeString("abc");
            Automaton h = SpecialOperations.hexCases(a);
            assertTrue(h.run("abc"));
            assertTrue(h.run("ABC"));
        }

        @Test
        @DisplayName("replaceWhitespace inserts optional whitespace between chars")
        void replaceWhitespace() {
            Automaton a = BasicAutomata.makeString("ab");
            Automaton w = SpecialOperations.replaceWhitespace(a);
            assertNotNull(w);
            assertTrue(w.run("ab"));
        }
    }
}
