package io.github.cowwoc.styler.parser.test;

import io.github.cowwoc.styler.parser.IndexOverlayParser;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;

/**
 * Test stack overflow protection in recursive descent parser.
 */
public class StackOverflowProtectionTest {

    @Test
    public void testStackOverflowProtectionOnDeeplyNestedExpressions() {
        // Create a deeply nested expression that would cause stack overflow without protection
        StringBuilder deeplyNested = new StringBuilder();
        deeplyNested.append("public class Test { void test() { int x = ");

        // Create 1500 levels of nesting (exceeds MAX_RECURSION_DEPTH = 1000)
        for (int i = 0; i < 1500; i++) {
            deeplyNested.append("(");
        }
        deeplyNested.append("42");
        for (int i = 0; i < 1500; i++) {
            deeplyNested.append(")");
        }
        deeplyNested.append("; } }");

        IndexOverlayParser parser = new IndexOverlayParser(deeplyNested.toString());

        try {
            parser.parse();
            fail("Expected ParseException due to recursion depth limit");
        } catch (IndexOverlayParser.ParseException e) {
            // Verify the exception message indicates stack overflow protection
            String message = e.getMessage();
            assertEquals(true, message.contains("Maximum recursion depth exceeded"));
            assertEquals(true, message.contains("1000"));
            assertEquals(true, message.contains("stack overflow"));
        }
    }

    @Test
    public void testStackOverflowProtectionOnDeeplyNestedTernaryOperators() {
        // Create deeply nested ternary operators: a ? b ? c ? ... : 3 : 2 : 1
        StringBuilder deeplyNested = new StringBuilder();
        deeplyNested.append("public class Test { void test() { int x = ");

        // Create 1500 levels of ternary nesting
        for (int i = 0; i < 1500; i++) {
            deeplyNested.append("true ? ");
        }
        deeplyNested.append("42");
        for (int i = 0; i < 1500; i++) {
            deeplyNested.append(" : 0");
        }
        deeplyNested.append("; } }");

        IndexOverlayParser parser = new IndexOverlayParser(deeplyNested.toString());

        try {
            parser.parse();
            fail("Expected ParseException due to recursion depth limit");
        } catch (IndexOverlayParser.ParseException e) {
            // Verify the exception message indicates stack overflow protection
            String message = e.getMessage();
            assertEquals(true, message.contains("Maximum recursion depth exceeded"));
            assertEquals(true, message.contains("stack overflow"));
        }
    }

    @Test
    public void testStackOverflowProtectionOnDeeplyNestedBlocks() {
        // Create deeply nested blocks
        StringBuilder deeplyNested = new StringBuilder();
        deeplyNested.append("public class Test { void test() { ");

        // Create 1500 levels of block nesting
        for (int i = 0; i < 1500; i++) {
            deeplyNested.append("{ ");
        }
        deeplyNested.append("int x = 42; ");
        for (int i = 0; i < 1500; i++) {
            deeplyNested.append("} ");
        }
        deeplyNested.append("} }");

        IndexOverlayParser parser = new IndexOverlayParser(deeplyNested.toString());

        try {
            parser.parse();
            fail("Expected ParseException due to recursion depth limit");
        } catch (IndexOverlayParser.ParseException e) {
            // Verify the exception message indicates stack overflow protection
            String message = e.getMessage();
            assertEquals(true, message.contains("Maximum recursion depth exceeded"));
            assertEquals(true, message.contains("stack overflow"));
        }
    }

    @Test
    public void testNormalRecursionDepthWorks() {
        // Test that normal, reasonable nesting still works
        String normalCode = """
            public class Test {
                void test() {
                    int x = (((((42)))));
                    int y = true ? (false ? 1 : 2) : 3;
                    {
                        {
                            int z = 100;
                        }
                    }
                }
            }
            """;

        IndexOverlayParser parser = new IndexOverlayParser(normalCode);

        // This should not throw an exception
        int rootNodeId = parser.parse();
        assertEquals(true, rootNodeId >= 0);
    }
}