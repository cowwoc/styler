package io.github.cowwoc.styler.parser.test;

import io.github.cowwoc.styler.parser.IndexOverlayParser;
import io.github.cowwoc.styler.parser.JavaLexer;
import io.github.cowwoc.styler.parser.TokenInfo;
import io.github.cowwoc.styler.parser.TokenType;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

/**
 * Unit tests for primitive type parsing bugs.
 */
public class PrimitiveTypeParsingTest {

    @Test(description = "Double keyword should be recognized as primitive type")
    public void testDoubleKeywordLexing() {
        // First verify the lexer produces DOUBLE for the keyword
        String source = "double";
        JavaLexer lexer = new JavaLexer(source);

        TokenInfo token = lexer.nextToken();

        assertEquals(token.type(), TokenType.DOUBLE, "Should tokenize 'double' as DOUBLE keyword");
        assertEquals(token.text(), "double");
    }

    @Test(description = "Simple field declaration with double should parse")
    public void testSimpleDoubleField() {
        // Minimal test case for double field declaration
        String source = """
            public class Test {
                double field;
            }
            """;

        IndexOverlayParser parser = new IndexOverlayParser(source);

        try {
            int rootId = parser.parse();
            assertNotEquals(rootId, -1, "Should parse double field successfully");
        } catch (Exception e) {
            fail("Should not throw exception for double field: " + e.getMessage());
        }
    }

    @Test(description = "Field with modifiers and double type should parse")
    public void testModifiedDoubleField() {
        String source = """
            public class Test {
                private final double field;
            }
            """;

        IndexOverlayParser parser = new IndexOverlayParser(source);

        try {
            int rootId = parser.parse();
            assertNotEquals(rootId, -1, "Should parse modified double field successfully");
        } catch (Exception e) {
            fail("Should not throw exception for modified double field: " + e.getMessage());
        }
    }
}