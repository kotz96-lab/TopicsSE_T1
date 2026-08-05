package se.topics.t1.junitsuite;

import dk.brics.automaton.Datatypes;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for {@link Datatypes} — the pre-built automaton registry for XML,
 * XML Schema, and Unicode names.
 *
 * <p>The full class also ships a static-initialiser pipeline
 * ({@code Datatypes.main} → {@code buildAll()}) that builds every automaton
 * from scratch. That path depends on a companion resource
 * {@code src/Unicode.txt} which is not vendored with BRICS in this repo, so
 * we cannot exercise the {@code buildAll} code from a unit test without
 * shipping that file. Similarly, the {@code get(name)} call needs
 * {@code name.aut} class-path resources that we don't ship. What remains
 * reachable is:
 *
 * <ul>
 *   <li>the three name-registry predicates: {@code isXMLName},
 *       {@code isUnicodeBlockName}, {@code isUnicodeCategoryName};</li>
 *   <li>{@code exists(name)} — which will return {@code false} for every
 *       name in this repo because no {@code .aut} files are shipped.</li>
 * </ul>
 */
class DatatypesTest {

    @Nested
    @DisplayName("XML / XML Schema name registry")
    class XmlNames {

        @Test
        @DisplayName("isXMLName recognises canonical XML Schema type names")
        void recognisesKnown() {
            assertTrue(Datatypes.isXMLName("integer"));
            assertTrue(Datatypes.isXMLName("boolean"));
            assertTrue(Datatypes.isXMLName("decimal"));
            assertTrue(Datatypes.isXMLName("dateTime"));
            assertTrue(Datatypes.isXMLName("hexBinary"));
        }

        @Test
        @DisplayName("isXMLName returns false for unknown names")
        void rejectsUnknown() {
            assertFalse(Datatypes.isXMLName("NotAnXmlType"));
            assertFalse(Datatypes.isXMLName(""));
        }
    }

    @Nested
    @DisplayName("Unicode block name registry")
    class UnicodeBlocks {

        @Test
        @DisplayName("isUnicodeBlockName recognises canonical block names")
        void recognisesKnown() {
            assertTrue(Datatypes.isUnicodeBlockName("BasicLatin"));
            assertTrue(Datatypes.isUnicodeBlockName("Hebrew"));
            assertTrue(Datatypes.isUnicodeBlockName("Greek"));
            assertTrue(Datatypes.isUnicodeBlockName("Cyrillic"));
        }

        @Test
        @DisplayName("isUnicodeBlockName returns false for unknown names")
        void rejectsUnknown() {
            assertFalse(Datatypes.isUnicodeBlockName("NotABlock"));
            assertFalse(Datatypes.isUnicodeBlockName(""));
        }
    }

    @Nested
    @DisplayName("Unicode category name registry")
    class UnicodeCategories {

        @Test
        @DisplayName("isUnicodeCategoryName recognises canonical category names")
        void recognisesKnown() {
            assertTrue(Datatypes.isUnicodeCategoryName("Lu"));  // Uppercase Letter
            assertTrue(Datatypes.isUnicodeCategoryName("Ll"));  // Lowercase Letter
            assertTrue(Datatypes.isUnicodeCategoryName("Nd"));  // Decimal digit
            assertTrue(Datatypes.isUnicodeCategoryName("L"));   // All Letters
        }

        @Test
        @DisplayName("isUnicodeCategoryName returns false for unknown names")
        void rejectsUnknown() {
            assertFalse(Datatypes.isUnicodeCategoryName("XX"));
            assertFalse(Datatypes.isUnicodeCategoryName(""));
        }
    }

    @Nested
    @DisplayName("Resource lookup")
    class Existence {

        /**
         * FINDING (candidate for §12 refactoring proposal): BRICS'
         * {@code Datatypes.exists(name)} at Datatypes.java:471 does
         * {@code getClassLoader().getResource(name + ".aut").openStream().close()}
         * inside a {@code try/catch(IOException)}. When the resource is
         * missing, {@code getResource} returns {@code null} and
         * {@code .openStream()} on {@code null} throws {@link NullPointerException} —
         * which is <em>not</em> caught. So {@code exists()} throws NPE instead
         * of returning {@code false} for missing resources. We assert the
         * observed (buggy) behaviour so the test locks it in as documentation.
         */
        @Test
        @DisplayName("exists throws NPE for missing resources (BRICS bug — should return false)")
        void existsThrowsOnMissing() {
            org.junit.jupiter.api.Assertions.assertThrows(
                    NullPointerException.class,
                    () -> Datatypes.exists("NoSuchName_NoAutFileShipped"));
        }
    }
}
