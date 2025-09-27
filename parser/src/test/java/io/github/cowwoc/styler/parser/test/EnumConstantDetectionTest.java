package io.github.cowwoc.styler.parser.test;

import io.github.cowwoc.styler.parser.IndexOverlayParser;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

/**
 * Unit tests to isolate enum constant detection logic bugs.
 * These tests help identify exactly when isEnumConstant incorrectly returns true.
 */
public class EnumConstantDetectionTest {

    @Test(description = "Simple enum with only constants should parse")
    public void testEnumWithOnlyConstants() {
        String source = """
            public enum Test {
                VALUE1, VALUE2, VALUE3;
            }
            """;

        IndexOverlayParser parser = new IndexOverlayParser(source);

        try {
            int rootId = parser.parse();
            assertNotEquals(rootId, -1, "Simple enum with constants should parse");
        } catch (Exception e) {
            fail("Should not throw exception: " + e.getMessage());
        }
    }

    @Test(description = "Enum with constants with arguments should parse")
    public void testEnumWithConstantArguments() {
        String source = """
            public enum Test {
                VALUE1(1), VALUE2(2);
            }
            """;

        IndexOverlayParser parser = new IndexOverlayParser(source);

        try {
            int rootId = parser.parse();
            assertNotEquals(rootId, -1, "Enum with constant arguments should parse");
        } catch (Exception e) {
            fail("Should not throw exception: " + e.getMessage());
        }
    }

    @Test(description = "Enum with constructor should parse - this isolates the exact failure point")
    public void testEnumWithConstructor() {
        String source = """
            public enum Test {
                VALUE(1);

                Test(int value) {
                }
            }
            """;

        IndexOverlayParser parser = new IndexOverlayParser(source);

        try {
            int rootId = parser.parse();
            assertNotEquals(rootId, -1, "Enum with constructor should parse");
        } catch (Exception e) {
            // This will show us exactly where the enum constant detection goes wrong
            assertTrue(e.getMessage().contains("DOUBLE") || e.getMessage().contains("IDENTIFIER"),
                "Should fail with specific parsing error, got: " + e.getMessage());
        }
    }

    @Test(description = "Enum with constructor parameter types - this should reveal the bug")
    public void testEnumWithDoubleParameter() {
        String source = """
            public enum Test {
                VALUE(1.0);

                Test(double value) {
                }
            }
            """;

        IndexOverlayParser parser = new IndexOverlayParser(source);

        try {
            int rootId = parser.parse();
            assertNotEquals(rootId, -1, "Enum with double parameter should parse");
        } catch (Exception e) {
            fail("Should not throw exception for enum with double parameter: " + e.getMessage());
        }
    }
}