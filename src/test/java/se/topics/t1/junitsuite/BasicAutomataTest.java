package se.topics.t1.junitsuite;

import dk.brics.automaton.Automaton;
import dk.brics.automaton.BasicAutomata;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for {@link BasicAutomata} — the factory-method entry point for
 * building common automata directly. Each test exercises one factory and
 * checks membership on both accepted and rejected strings so that mutants
 * flipping accept/reject conditions get killed.
 */
class BasicAutomataTest {

    @Nested
    @DisplayName("Empty / universal automata")
    class EmptyAndUniversal {

        @Test
        @DisplayName("makeEmpty accepts no strings and is deterministic")
        void makeEmpty() {
            Automaton a = BasicAutomata.makeEmpty();
            assertTrue(a.isEmpty());
            assertFalse(a.run(""));
            assertFalse(a.run("a"));
            assertTrue(a.isDeterministic());
        }

        @Test
        @DisplayName("makeEmptyString accepts only the empty string")
        void makeEmptyString() {
            Automaton a = BasicAutomata.makeEmptyString();
            assertTrue(a.run(""));
            assertFalse(a.run("a"));
            assertTrue(a.isEmptyString());
        }

        @Test
        @DisplayName("makeAnyString accepts every string, including empty")
        void makeAnyString() {
            Automaton a = BasicAutomata.makeAnyString();
            assertTrue(a.run(""));
            assertTrue(a.run("a"));
            assertTrue(a.run("hello world"));
            assertTrue(a.isTotal());
        }
    }

    @Nested
    @DisplayName("Character-level factories")
    class CharFactories {

        @Test
        @DisplayName("makeAnyChar accepts exactly one arbitrary character")
        void makeAnyChar() {
            Automaton a = BasicAutomata.makeAnyChar();
            assertTrue(a.run("x"));
            assertTrue(a.run("Z"));
            assertFalse(a.run(""));
            assertFalse(a.run("ab"));
        }

        @Test
        @DisplayName("makeChar accepts exactly the given character")
        void makeChar() {
            Automaton a = BasicAutomata.makeChar('q');
            assertTrue(a.run("q"));
            assertFalse(a.run(""));
            assertFalse(a.run("Q"));
            assertFalse(a.run("qq"));
        }

        @Test
        @DisplayName("makeCharRange accepts inclusive bounds")
        void makeCharRangeInclusive() {
            Automaton a = BasicAutomata.makeCharRange('c', 'f');
            assertTrue(a.run("c"));
            assertTrue(a.run("d"));
            assertTrue(a.run("f"));
            assertFalse(a.run("b"));
            assertFalse(a.run("g"));
        }

        @Test
        @DisplayName("makeCharRange with min == max degenerates to makeChar")
        void makeCharRangeSingleton() {
            Automaton a = BasicAutomata.makeCharRange('a', 'a');
            assertTrue(a.run("a"));
            assertFalse(a.run("b"));
        }

        @Test
        @DisplayName("makeCharSet accepts every character in the set and no others")
        void makeCharSet() {
            Automaton a = BasicAutomata.makeCharSet("acx");
            assertTrue(a.run("a"));
            assertTrue(a.run("c"));
            assertTrue(a.run("x"));
            assertFalse(a.run("b"));
            assertFalse(a.run(""));
            assertFalse(a.run("ac"));
        }

        @Test
        @DisplayName("makeCharSet with a single-char set degenerates to makeChar")
        void makeCharSetSingleton() {
            Automaton a = BasicAutomata.makeCharSet("z");
            assertTrue(a.run("z"));
            assertFalse(a.run("a"));
        }
    }

    @Nested
    @DisplayName("String factories")
    class StringFactories {

        @Test
        @DisplayName("makeString accepts exactly the given string")
        void makeString() {
            Automaton a = BasicAutomata.makeString("hello");
            assertTrue(a.run("hello"));
            assertFalse(a.run(""));
            assertFalse(a.run("hell"));
            assertFalse(a.run("hellos"));
            assertFalse(a.run("Hello"));
        }

        @Test
        @DisplayName("makeString of empty string == makeEmptyString")
        void makeStringEmpty() {
            Automaton a = BasicAutomata.makeString("");
            assertTrue(a.run(""));
            assertFalse(a.run("a"));
        }

        @Test
        @DisplayName("makeStringUnion accepts every string in the set and nothing else")
        void makeStringUnion() {
            Automaton a = BasicAutomata.makeStringUnion("cat", "dog", "fish");
            assertTrue(a.run("cat"));
            assertTrue(a.run("dog"));
            assertTrue(a.run("fish"));
            assertFalse(a.run(""));
            assertFalse(a.run("bird"));
            assertFalse(a.run("cats"));
        }

        @Test
        @DisplayName("makeStringUnion of zero strings yields the empty automaton")
        void makeStringUnionEmpty() {
            Automaton a = BasicAutomata.makeStringUnion();
            assertTrue(a.isEmpty());
        }

        @Test
        @DisplayName("makeStringMatcher matches any string containing the substring")
        void makeStringMatcher() {
            Automaton a = BasicAutomata.makeStringMatcher("bc");
            assertTrue(a.run("bc"));
            assertTrue(a.run("abc"));
            assertTrue(a.run("xxbcxx"));
            assertFalse(a.run(""));
            assertFalse(a.run("ab"));
            assertFalse(a.run("b"));
        }
    }

    @Nested
    @DisplayName("Numeric factories")
    class NumericFactories {

        @Test
        @DisplayName("makeInterval variable-width accepts decimals in range")
        void makeIntervalVariableWidth() {
            Automaton a = BasicAutomata.makeInterval(5, 42, 0);
            assertTrue(a.run("5"));
            assertTrue(a.run("10"));
            assertTrue(a.run("42"));
            assertFalse(a.run("4"));
            assertFalse(a.run("43"));
            assertFalse(a.run("100"));
        }

        @Test
        @DisplayName("makeInterval fixed-width pads with zeros")
        void makeIntervalFixedWidth() {
            Automaton a = BasicAutomata.makeInterval(5, 42, 3);
            assertTrue(a.run("005"));
            assertTrue(a.run("042"));
            assertFalse(a.run("5"));
            assertFalse(a.run("42"));
            assertFalse(a.run("043"));
        }

        @Test
        @DisplayName("makeInterval throws when min > max")
        void makeIntervalMinGreaterThanMax() {
            assertThrows(IllegalArgumentException.class,
                    () -> BasicAutomata.makeInterval(10, 5, 0));
        }

        @Test
        @DisplayName("makeInterval throws when fixed digit-width is too small")
        void makeIntervalDigitsTooSmall() {
            assertThrows(IllegalArgumentException.class,
                    () -> BasicAutomata.makeInterval(1, 999, 2));
        }

        @Test
        @DisplayName("makeMaxInteger accepts values <= n")
        void makeMaxInteger() {
            Automaton a = BasicAutomata.makeMaxInteger("100");
            assertTrue(a.run("0"));
            assertTrue(a.run("50"));
            assertTrue(a.run("100"));
            assertFalse(a.run("101"));
            assertFalse(a.run("999"));
        }

        @Test
        @DisplayName("makeMinInteger accepts values >= n")
        void makeMinInteger() {
            Automaton a = BasicAutomata.makeMinInteger("100");
            assertFalse(a.run("99"));
            assertTrue(a.run("100"));
            assertTrue(a.run("101"));
            assertTrue(a.run("9999"));
        }
    }
}
