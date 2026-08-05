package se.topics.t1.junitsuite;

import dk.brics.automaton.Automaton;
import dk.brics.automaton.BasicAutomata;
import dk.brics.automaton.BasicOperations;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for {@link BasicOperations} — union, intersection, complement,
 * concatenation, repetition, difference, subset, and membership. These
 * are the algebraic core of the library, so we check each operation on
 * both accepting and rejecting witnesses.
 */
class BasicOperationsTest {

    private static Automaton a()    { return BasicAutomata.makeChar('a'); }
    private static Automaton b()    { return BasicAutomata.makeChar('b'); }
    private static Automaton ab()   { return BasicAutomata.makeString("ab"); }
    private static Automaton any()  { return BasicAutomata.makeAnyString(); }
    private static Automaton empty(){ return BasicAutomata.makeEmpty(); }

    @Nested
    @DisplayName("Union")
    class Union {

        @Test
        @DisplayName("union(a, b) accepts either")
        void unionPair() {
            Automaton u = BasicOperations.union(a(), b());
            assertTrue(u.run("a"));
            assertTrue(u.run("b"));
            assertFalse(u.run(""));
            assertFalse(u.run("c"));
        }

        @Test
        @DisplayName("union(a, empty) == a")
        void unionWithEmpty() {
            Automaton u = BasicOperations.union(a(), empty());
            assertTrue(u.run("a"));
            assertFalse(u.run(""));
            assertFalse(u.run("b"));
        }

        @Test
        @DisplayName("union(collection) accepts every member language")
        void unionCollection() {
            Automaton u = BasicOperations.union(Arrays.asList(a(), b(), BasicAutomata.makeChar('c')));
            assertTrue(u.run("a"));
            assertTrue(u.run("b"));
            assertTrue(u.run("c"));
            assertFalse(u.run("d"));
        }
    }

    @Nested
    @DisplayName("Intersection")
    class Intersection {

        @Test
        @DisplayName("intersection of disjoint singletons is empty")
        void disjoint() {
            Automaton i = BasicOperations.intersection(a(), b());
            assertTrue(i.isEmpty());
        }

        @Test
        @DisplayName("intersection with universal language is identity")
        void withUniversal() {
            Automaton i = BasicOperations.intersection(a(), any());
            assertTrue(i.run("a"));
            assertFalse(i.run("b"));
            assertFalse(i.run(""));
        }

        @Test
        @DisplayName("intersection of (a|b) and (b|c) accepts only b")
        void overlapping() {
            Automaton ab = BasicOperations.union(a(), b());
            Automaton bc = BasicOperations.union(b(), BasicAutomata.makeChar('c'));
            Automaton i = BasicOperations.intersection(ab, bc);
            assertTrue(i.run("b"));
            assertFalse(i.run("a"));
            assertFalse(i.run("c"));
        }
    }

    @Nested
    @DisplayName("Complement")
    class Complement {

        @Test
        @DisplayName("complement of singleton rejects that string and accepts others")
        void complementSingleton() {
            Automaton c = BasicOperations.complement(a());
            assertFalse(c.run("a"));
            assertTrue(c.run(""));
            assertTrue(c.run("b"));
            assertTrue(c.run("aa"));
        }

        @Test
        @DisplayName("complement of empty language accepts everything")
        void complementEmpty() {
            // isTotal() is a *structural* predicate that only holds for a very
            // specific canonical form (accepting initial with a single MIN..MAX
            // self-loop). Complement of makeEmpty produces the same *language*
            // but not necessarily that shape, so we check semantics via run().
            Automaton c = BasicOperations.complement(empty());
            assertTrue(c.run(""));
            assertTrue(c.run("a"));
            assertTrue(c.run("hello world"));
        }

        @Test
        @DisplayName("complement of universal language is empty")
        void complementUniversal() {
            Automaton c = BasicOperations.complement(any());
            assertTrue(c.isEmpty());
        }
    }

    @Nested
    @DisplayName("Concatenation")
    class Concatenation {

        @Test
        @DisplayName("concatenation of two singletons accepts the joined string only")
        void twoSingletons() {
            Automaton c = BasicOperations.concatenate(a(), b());
            assertTrue(c.run("ab"));
            assertFalse(c.run(""));
            assertFalse(c.run("a"));
            assertFalse(c.run("ba"));
            assertFalse(c.run("abb"));
        }

        @Test
        @DisplayName("concatenation with empty language yields empty language")
        void withEmpty() {
            Automaton c = BasicOperations.concatenate(a(), empty());
            assertTrue(c.isEmpty());
        }

        @Test
        @DisplayName("concatenation of a list works transitively")
        void listOfThree() {
            List<Automaton> xs = Arrays.asList(a(), b(), BasicAutomata.makeChar('c'));
            Automaton c = BasicOperations.concatenate(xs);
            assertTrue(c.run("abc"));
            assertFalse(c.run("ab"));
            assertFalse(c.run("abcd"));
        }

        @Test
        @DisplayName("concatenation of empty list yields makeEmptyString")
        void emptyList() {
            Automaton c = BasicOperations.concatenate(java.util.Collections.emptyList());
            assertTrue(c.run(""));
            assertFalse(c.run("a"));
        }
    }

    @Nested
    @DisplayName("Repetition")
    class Repetition {

        @Test
        @DisplayName("repeat (Kleene star) accepts zero or more")
        void star() {
            Automaton r = BasicOperations.repeat(a());
            assertTrue(r.run(""));
            assertTrue(r.run("a"));
            assertTrue(r.run("aaaa"));
            assertFalse(r.run("b"));
        }

        @Test
        @DisplayName("repeat(a, 2) accepts >= 2 repetitions")
        void atLeast() {
            Automaton r = BasicOperations.repeat(a(), 2);
            assertFalse(r.run(""));
            assertFalse(r.run("a"));
            assertTrue(r.run("aa"));
            assertTrue(r.run("aaaa"));
        }

        @Test
        @DisplayName("repeat(a, 2, 4) accepts inclusive range")
        void bounded() {
            Automaton r = BasicOperations.repeat(a(), 2, 4);
            assertFalse(r.run("a"));
            assertTrue(r.run("aa"));
            assertTrue(r.run("aaa"));
            assertTrue(r.run("aaaa"));
            assertFalse(r.run("aaaaa"));
        }

        @Test
        @DisplayName("repeat with min > max yields empty language")
        void invalidBounds() {
            Automaton r = BasicOperations.repeat(a(), 5, 3);
            assertTrue(r.isEmpty());
        }
    }

    @Nested
    @DisplayName("Optional, minus, subsetOf")
    class Others {

        @Test
        @DisplayName("optional accepts empty and the original language")
        void optional() {
            Automaton o = BasicOperations.optional(a());
            assertTrue(o.run(""));
            assertTrue(o.run("a"));
            assertFalse(o.run("aa"));
        }

        @Test
        @DisplayName("minus removes strings of second language from first")
        void minus() {
            Automaton ab = BasicOperations.union(a(), b());
            Automaton m = BasicOperations.minus(ab, a());
            assertTrue(m.run("b"));
            assertFalse(m.run("a"));
        }

        @Test
        @DisplayName("subsetOf(a, a|b) is true")
        void subsetTrue() {
            assertTrue(BasicOperations.subsetOf(a(), BasicOperations.union(a(), b())));
        }

        @Test
        @DisplayName("subsetOf(a|b, a) is false")
        void subsetFalse() {
            assertFalse(BasicOperations.subsetOf(BasicOperations.union(a(), b()), a()));
        }

        @Test
        @DisplayName("subsetOf(a, a) is true (reflexive)")
        void subsetReflexive() {
            Automaton x = a();
            assertTrue(BasicOperations.subsetOf(x, x));
        }
    }

    @Nested
    @DisplayName("Predicates and utilities")
    class Predicates {

        @Test
        @DisplayName("isEmpty/isEmptyString/isTotal are exclusive on canonical automata")
        void predicatesAreConsistent() {
            assertTrue(BasicOperations.isEmpty(empty()));
            assertFalse(BasicOperations.isEmpty(BasicAutomata.makeEmptyString()));

            assertTrue(BasicOperations.isEmptyString(BasicAutomata.makeEmptyString()));
            assertFalse(BasicOperations.isEmptyString(empty()));

            assertTrue(BasicOperations.isTotal(any()));
            assertFalse(BasicOperations.isTotal(empty()));
            assertFalse(BasicOperations.isTotal(BasicAutomata.makeEmptyString()));
        }

        @Test
        @DisplayName("static run matches instance run")
        void runStaticEqualsInstance() {
            Automaton x = ab();
            assertTrue(BasicOperations.run(x, "ab"));
            assertFalse(BasicOperations.run(x, "a"));
        }

        @Test
        @DisplayName("getShortestExample of singleton returns the singleton")
        void shortestAccepted() {
            String ex = BasicOperations.getShortestExample(ab(), true);
            assertNotNull(ex);
            assertTrue(ab().run(ex));
        }

        @Test
        @DisplayName("getShortestExample of empty automaton returns null when asking for accepted")
        void shortestOfEmpty() {
            assertNull(BasicOperations.getShortestExample(empty(), true));
        }

        @Test
        @DisplayName("determinize on already-deterministic automaton is a no-op-ish")
        void determinizeDeterministic() {
            Automaton x = a();
            assertTrue(x.isDeterministic());
            BasicOperations.determinize(x);
            assertTrue(x.isDeterministic());
            assertTrue(x.run("a"));
        }
    }
}
