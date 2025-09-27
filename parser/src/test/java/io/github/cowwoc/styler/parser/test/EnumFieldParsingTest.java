package io.github.cowwoc.styler.parser.test;

import io.github.cowwoc.styler.parser.IndexOverlayParser;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

/**
 * Unit tests for enum field declaration parsing bugs.
 */
public class EnumFieldParsingTest {

    @Test(description = "Enum with double field should parse without DOUBLE keyword errors")
    public void testEnumWithDoubleField() {
        // Minimal test case that reproduces the exact bug
        String source = """
            public enum Test {
                VALUE(1.0);

                private final double field;

                Test(double param) {
                    this.field = param;
                }
            }
            """;

        IndexOverlayParser parser = new IndexOverlayParser(source);

        // This should not throw "Unexpected token in expression: DOUBLE"
        try {
            int rootId = parser.parse();
            assertNotEquals(rootId, -1, "Parser should successfully parse enum with double field");
        } catch (Exception e) {
            fail("Parser should not throw exception for enum with double field: " + e.getMessage());
        }
    }

    @Test(description = "Simple enum constant with argument should parse")
    public void testSimpleEnumConstant() {
        String source = """
            public enum Test {
                VALUE(1.0);
            }
            """;

        IndexOverlayParser parser = new IndexOverlayParser(source);

        try {
            int rootId = parser.parse();
            assertNotEquals(rootId, -1);
        } catch (Exception e) {
            fail("Parser should not throw exception: " + e.getMessage());
        }
    }
}