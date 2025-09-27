package io.github.cowwoc.styler.parser.test;

import io.github.cowwoc.styler.parser.IndexOverlayParser;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

/**
 * Unit tests for record compact constructor parsing.
 * Tests the specific syntax: public Person { ... } without parameters.
 */
public class RecordCompactConstructorTest {

    @Test(description = "Record with compact constructor should parse")
    public void testCompactConstructor() {
        String source = """
            public record Person(String name) {
                public Person {
                    name = name.trim();
                }
            }
            """;

        IndexOverlayParser parser = new IndexOverlayParser(source);

        try {
            int rootId = parser.parse();
            assertNotEquals(rootId, -1, "Record with compact constructor should parse");
        } catch (Exception e) {
            fail("Should not throw exception for compact constructor: " + e.getMessage());
        }
    }

    @Test(description = "Minimal compact constructor to isolate the exact issue")
    public void testMinimalCompactConstructor() {
        String source = """
            record Test(int x) {
                Test {
                }
            }
            """;

        IndexOverlayParser parser = new IndexOverlayParser(source);

        try {
            int rootId = parser.parse();
            assertNotEquals(rootId, -1, "Minimal compact constructor should parse");
        } catch (Exception e) {
            fail("Should not throw exception for minimal compact constructor: " + e.getMessage());
        }
    }
}