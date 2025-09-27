package io.github.cowwoc.styler.parser.test;

import io.github.cowwoc.styler.parser.IndexOverlayParser;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

/**
 * Unit tests comparing enum vs class parsing for the same field declaration.
 */
public class EnumVsClassComparisonTest {

    @Test(description = "Class with double field should parse successfully")
    public void testClassWithDoubleField() {
        String source = """
            public class Test {
                private final double field;
            }
            """;

        IndexOverlayParser parser = new IndexOverlayParser(source);

        try {
            int rootId = parser.parse();
            assertNotEquals(rootId, -1, "Class with double field should parse");
        } catch (Exception e) {
            fail("Class should not throw exception: " + e.getMessage());
        }
    }

    @Test(description = "Enum with identical double field should fail with DOUBLE error")
    public void testEnumWithDoubleField() {
        String source = """
            public enum Test {
                ;
                private final double field;
            }
            """;

        IndexOverlayParser parser = new IndexOverlayParser(source);

        try {
            int rootId = parser.parse();
            // If this succeeds, the bug is fixed
            assertNotEquals(rootId, -1, "Enum with double field should parse");
        } catch (Exception e) {
            // This should be the current bug
            assertTrue(e.getMessage().contains("DOUBLE"),
                "Should fail with DOUBLE token error: " + e.getMessage());
        }
    }

    @Test(description = "Enum with empty body should parse")
    public void testEmptyEnum() {
        String source = """
            public enum Test {
                ;
            }
            """;

        IndexOverlayParser parser = new IndexOverlayParser(source);

        try {
            int rootId = parser.parse();
            assertNotEquals(rootId, -1, "Empty enum should parse");
        } catch (Exception e) {
            fail("Empty enum should not throw exception: " + e.getMessage());
        }
    }
}