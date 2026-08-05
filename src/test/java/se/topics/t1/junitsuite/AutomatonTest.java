package se.topics.t1.junitsuite;

import dk.brics.automaton.Automaton;
import dk.brics.automaton.BasicAutomata;
import dk.brics.automaton.State;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for {@link Automaton} — instance-level facade over the operations,
 * plus structural queries (state / transition counts), equality, cloning,
 * language finiteness, and enumeration.
 */
class AutomatonTest {

    @Nested
    @DisplayName("Structural queries")
    class Structural {

        @Test
        @DisplayName("empty automaton has 1 state and 0 transitions after singleton expansion")
        void emptyStructure() {
            Automaton a = BasicAutomata.makeEmpty();
            assertTrue(a.getNumberOfStates() >= 1);
            assertEquals(0, a.getNumberOfTransitions());
        }

        @Test
        @DisplayName("makeString exposes its singleton via getSingleton()")
        void singletonAccessor() {
            Automaton a = BasicAutomata.makeString("hello");
            assertEquals("hello", a.getSingleton());
        }

        @Test
        @DisplayName("makeChar-based union is not stored as a singleton")
        void nonSingletonAfterUnion() {
            Automaton u = BasicAutomata.makeChar('a').union(BasicAutomata.makeChar('b'));
            assertEquals(null, u.getSingleton());
        }

        @Test
        @DisplayName("getStates and getAcceptStates are non-empty for a non-empty automaton")
        void statesNonEmpty() {
            Automaton a = BasicAutomata.makeString("ab");
            Set<State> all = a.getStates();
            Set<State> acc = a.getAcceptStates();
            assertNotNull(all);
            assertFalse(all.isEmpty());
            assertNotNull(acc);
            assertFalse(acc.isEmpty());
        }

        @Test
        @DisplayName("getInitialState is non-null")
        void initialNonNull() {
            assertNotNull(BasicAutomata.makeChar('a').getInitialState());
        }
    }

    @Nested
    @DisplayName("Cloning and equality")
    class CloneAndEquals {

        @Test
        @DisplayName("clone produces an independent equal automaton")
        void cloneIsEqual() {
            Automaton a = BasicAutomata.makeString("foo");
            Automaton b = a.clone();
            assertEquals(a, b);
            assertTrue(b.run("foo"));
        }

        @Test
        @DisplayName("automata over different languages are not equal")
        void differentLanguageNotEqual() {
            Automaton x = BasicAutomata.makeString("foo");
            Automaton y = BasicAutomata.makeString("bar");
            assertNotEquals(x, y);
        }

        @Test
        @DisplayName("equal automata have equal hashCodes")
        void equalHashCodes() {
            Automaton x = BasicAutomata.makeString("foo");
            Automaton y = BasicAutomata.makeString("foo");
            assertEquals(x.hashCode(), y.hashCode());
        }

        @Test
        @DisplayName("equals(null) and equals(non-Automaton) are false")
        void equalsBadInputs() {
            Automaton x = BasicAutomata.makeChar('a');
            assertNotEquals(x, null);
            assertNotEquals(x, "not an automaton");
        }
    }

    @Nested
    @DisplayName("Instance operations (delegate to BasicOperations)")
    class InstanceOps {

        @Test
        @DisplayName("union() instance form matches BasicOperations behaviour")
        void unionInstance() {
            Automaton u = BasicAutomata.makeChar('a').union(BasicAutomata.makeChar('b'));
            assertTrue(u.run("a"));
            assertTrue(u.run("b"));
            assertFalse(u.run("c"));
        }

        @Test
        @DisplayName("intersection() instance form")
        void intersectionInstance() {
            Automaton x = BasicAutomata.makeChar('a').union(BasicAutomata.makeChar('b'));
            Automaton y = BasicAutomata.makeChar('b');
            Automaton i = x.intersection(y);
            assertTrue(i.run("b"));
            assertFalse(i.run("a"));
        }

        @Test
        @DisplayName("complement() instance form")
        void complementInstance() {
            Automaton c = BasicAutomata.makeChar('a').complement();
            assertFalse(c.run("a"));
            assertTrue(c.run("b"));
        }

        @Test
        @DisplayName("concatenate() instance form")
        void concatenateInstance() {
            Automaton c = BasicAutomata.makeChar('a').concatenate(BasicAutomata.makeChar('b'));
            assertTrue(c.run("ab"));
            assertFalse(c.run("ba"));
        }

        @Test
        @DisplayName("repeat() instance form")
        void repeatInstance() {
            Automaton r = BasicAutomata.makeChar('a').repeat();
            assertTrue(r.run(""));
            assertTrue(r.run("aaaa"));
        }

        @Test
        @DisplayName("optional() instance form")
        void optionalInstance() {
            Automaton o = BasicAutomata.makeChar('a').optional();
            assertTrue(o.run(""));
            assertTrue(o.run("a"));
            assertFalse(o.run("aa"));
        }

        @Test
        @DisplayName("minus() instance form")
        void minusInstance() {
            Automaton x = BasicAutomata.makeChar('a').union(BasicAutomata.makeChar('b'));
            Automaton m = x.minus(BasicAutomata.makeChar('a'));
            assertTrue(m.run("b"));
            assertFalse(m.run("a"));
        }

        @Test
        @DisplayName("subsetOf() instance form")
        void subsetOfInstance() {
            Automaton x = BasicAutomata.makeChar('a');
            Automaton y = BasicAutomata.makeChar('a').union(BasicAutomata.makeChar('b'));
            assertTrue(x.subsetOf(y));
            assertFalse(y.subsetOf(x));
        }
    }

    @Nested
    @DisplayName("Finiteness and enumeration")
    class Finiteness {

        @Test
        @DisplayName("finite language reports isFinite() true")
        void finiteYes() {
            assertTrue(BasicAutomata.makeString("abc").isFinite());
        }

        @Test
        @DisplayName("infinite language reports isFinite() false")
        void finiteNo() {
            assertFalse(BasicAutomata.makeChar('a').repeat().isFinite());
        }

        @Test
        @DisplayName("getFiniteStrings on singleton returns exactly that string")
        void finiteStringsSingleton() {
            Set<String> s = BasicAutomata.makeString("xyz").getFiniteStrings();
            assertEquals(Set.of("xyz"), s);
        }

        @Test
        @DisplayName("getStrings(length) returns strings of that length only")
        void stringsOfLength() {
            Automaton a = BasicAutomata.makeChar('a').union(BasicAutomata.makeChar('b')).repeat();
            Set<String> two = a.getStrings(2);
            assertEquals(4, two.size());
            assertTrue(two.contains("aa"));
            assertTrue(two.contains("ab"));
            assertTrue(two.contains("ba"));
            assertTrue(two.contains("bb"));
        }
    }

    @Nested
    @DisplayName("Prefix and shortest example")
    class PrefixAndExample {

        @Test
        @DisplayName("getCommonPrefix returns the common prefix of the language")
        void commonPrefix() {
            // Build the union via a single RegExp so BRICS returns a minimized DFA
            // with a genuine linear prefix. Using .union() on two singletons produces
            // an NFA whose initial state branches immediately, giving a "" prefix.
            Automaton a = new dk.brics.automaton.RegExp("prefix-(a|b)").toAutomaton();
            assertEquals("prefix-", a.getCommonPrefix());
        }

        @Test
        @DisplayName("getShortestExample(accepted=true) yields an accepted string")
        void shortestAccepted() {
            Automaton a = BasicAutomata.makeString("ab");
            String ex = a.getShortestExample(true);
            assertNotNull(ex);
            assertTrue(a.run(ex));
        }
    }

    @Nested
    @DisplayName("Minimization + toString")
    class MinAndString {

        @Test
        @DisplayName("Automaton.minimize preserves language")
        void minimizePreserves() {
            Automaton a = BasicAutomata.makeChar('a').union(BasicAutomata.makeChar('a'));
            Automaton min = Automaton.minimize(a);
            assertTrue(min.run("a"));
            assertFalse(min.run("b"));
        }

        @Test
        @DisplayName("toString() is non-null")
        void toStringNonNull() {
            assertNotNull(BasicAutomata.makeChar('a').toString());
        }

        @Test
        @DisplayName("toDot() produces a Graphviz string starting with 'digraph'")
        void toDot() {
            String dot = BasicAutomata.makeChar('a').toDot();
            assertNotNull(dot);
            assertTrue(dot.startsWith("digraph"));
        }
    }
}
