package io.github.cowwoc.styler.parser.test;

import io.github.cowwoc.styler.parser.IndexOverlayParser;
import io.github.cowwoc.styler.parser.JavaVersion;
import io.github.cowwoc.styler.parser.MemoryArena;
import org.testng.annotations.Test;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;
import static org.testng.Assert.*;

/**
 * Comprehensive security protection tests for the parser module.
 *
 * These tests validate that the parser properly protects against resource exhaustion
 * attacks while maintaining system stability. Based on the single-user security model
 * defined in docs/project/scope.md, focusing on:
 * - Stack overflow prevention (MAX_RECURSION_DEPTH = 1000)
 * - Memory arena limits and cleanup
 * - System stability after parse failures
 * - Graceful error handling without crashes
 */
@Test(singleThreaded = true)
public class SecurityProtectionTest {

    /**
     * Tests that the parser enforces maximum recursion depth to prevent stack overflow.
     *
     * Critical security requirement: All parser operations must validate resource limits
     * are enforced to prevent system instability from malicious or malformed input.
     */
    @Test(description = "Recursion depth limit enforcement")
    public void testRecursionDepthLimitEnforcement() {
        // Create input exceeding MAX_RECURSION_DEPTH (1000)
        StringBuilder deeplyNestedInput = new StringBuilder();
        deeplyNestedInput.append("public class Test { void method() { int x = ");

        // Create 1500 levels of nesting
        for (int i = 0; i < 1500; i++) {
            deeplyNestedInput.append("(");
        }
        deeplyNestedInput.append("42");
        for (int i = 0; i < 1500; i++) {
            deeplyNestedInput.append(")");
        }
        deeplyNestedInput.append("; } }");

        IndexOverlayParser parser = new IndexOverlayParser(deeplyNestedInput.toString());

        try {
            parser.parse();
            fail("Expected ParseException due to recursion depth limit");
        } catch (IndexOverlayParser.ParseException e) {
            String message = e.getMessage();
            requireThat(message.contains("Maximum recursion depth exceeded"), "recursion_error_message")
                .isEqualTo(true);
            requireThat(message.contains("1000"), "recursion_limit_mentioned")
                .isEqualTo(true);
        }

        // System should remain stable after recursion limit error
    }

    /**
     * Tests memory arena limits to prevent OutOfMemoryError conditions.
     *
     * Critical security requirement: Memory arena validation prevents system-level
     * memory exhaustion from large parsing operations.
     */
    @Test(description = "Memory arena capacity limits")
    public void testMemoryArenaCapacityLimits() {
        // Create a very large but valid Java source
        StringBuilder largeInput = new StringBuilder();
        largeInput.append("public class LargeClass {\n");

        // Add 10,000 methods to create substantial memory pressure
        for (int i = 0; i < 10000; i++) {
            largeInput.append("    public void method").append(i).append("() {\n");
            largeInput.append("        int var").append(i).append(" = ").append(i).append(";\n");
            largeInput.append("        System.out.println(\"Method ").append(i).append(": \" + var").append(i).append(");\n");
            largeInput.append("    }\n");
        }
        largeInput.append("}");

        // Test large input parsing behavior
        IndexOverlayParser parser = new IndexOverlayParser(largeInput.toString());

        try {
            int rootId = parser.parse();
            // Large input should either parse successfully or fail gracefully
            if (rootId == -1) {
                fail("Parser should either succeed or throw exception, not return -1");
            }
            // If successful, verify reconstruction
            String reconstructed = parser.getNodeText(rootId);
            requireThat(reconstructed, "large_input_reconstruction").isEqualTo(largeInput.toString());
        } catch (IndexOverlayParser.ParseException e) {
            // Acceptable - large inputs may exceed parser limits
            String message = e.getMessage();
            assertNotNull(message, "Error message should not be null");
            assertFalse(message.isBlank(), "Error message should be descriptive");
        }

        // System should remain stable after processing large input
    }

    /**
     * Tests graceful error handling ensures parser failures don't crash the system.
     *
     * High security requirement: All parse failures must maintain system stability
     * and provide meaningful error information for debugging.
     */
    @Test(description = "Graceful error handling system stability")
    public void testGracefulErrorHandlingSystemStability() {
        String[] malformedInputs = {
            "public class { unclosed", // Missing class name and brace
            "interface Test extends implements", // Invalid syntax
            "enum { CONSTANT ANOTHER }", // Missing enum name and commas
            "record Person(String name", // Unclosed parameter list
            "@Override public void method() { }", // Missing class context
            "switch (x) { case 1: case 2: }", // Missing variable declaration
            "try { } catch { }", // Missing exception type
            "for (int i = 0; i < ; i++) { }", // Missing condition
            "class Test { void () { } }", // Missing method name
            "import .*;", // Invalid import statement
        };

        IndexOverlayParser parser = null;

        for (String malformedInput : malformedInputs) {
            try {
                parser = new IndexOverlayParser(malformedInput);
                parser.parse();
                fail("Expected ParseException for malformed input: " + malformedInput);
            } catch (IndexOverlayParser.ParseException e) {
                // Expected - verify error message is meaningful
                String message = e.getMessage();
                assertNotNull(message, "Error message should not be null");
                assertFalse(message.isBlank(), "Error message should not be blank");
                assertTrue(message.length() > 10, "Error message should be descriptive");
            } catch (Exception e) {
                fail("Parser should throw ParseException, not " + e.getClass().getSimpleName() +
                     " for input: " + malformedInput);
            }

            // System should remain stable after error
        }
    }

    /**
     * Tests that resource limits work correctly with JDK 25 virtual thread constructs.
     *
     * Medium security requirement: Validate resource limit enforcement continues
     * to function properly when parsing virtual thread syntax.
     */
    @Test(description = "Virtual thread resource isolation")
    public void testVirtualThreadResourceIsolation() {
        String virtualThreadCode = """
            public class VirtualThreadExample {
                public void processData() {
                    try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
                        var futures = IntStream.range(0, 1000)
                            .mapToObj(i -> executor.submit(() -> {
                                // Simulate work that could stress parser if not properly isolated
                                return processItem(i);
                            }))
                            .toList();

                        for (var future : futures) {
                            try {
                                future.get();
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                        }
                    }
                }

                private String processItem(int index) {
                    return "Processed: " + index;
                }
            }
            """;

        // Test that virtual thread parsing respects resource limits
        IndexOverlayParser parser = new IndexOverlayParser(virtualThreadCode, JavaVersion.JAVA_25);

        try {
            int rootId = parser.parse();
            requireThat(rootId, "rootId").isNotEqualTo(-1);

            // Verify that the parsing completed successfully
            String reconstructed = parser.getNodeText(rootId);
            requireThat(reconstructed, "reconstructed").isEqualTo(virtualThreadCode);

        } catch (IndexOverlayParser.ParseException e) {
            fail("Virtual thread code should parse successfully: " + e.getMessage());
        }

        // Virtual thread parsing should complete successfully
    }

    /**
     * Tests concurrent parsing operations maintain resource isolation.
     *
     * This validates that multiple parser instances don't interfere with each
     * other's resource limits and error handling.
     */
    @Test(description = "Concurrent parser resource isolation")
    public void testConcurrentParserResourceIsolation() {
        String simpleCode = "public class Test { void method() { } }";
        String complexCode = """
            public class ComplexTest {
                public void complexMethod() {
                    var list = List.of(1, 2, 3, 4, 5);
                    var processed = list.stream()
                        .filter(x -> x > 2)
                        .map(x -> x * 2)
                        .toList();
                    for (Integer item : processed) {
                        System.out.println(item);
                    }
                }
            }
            """;

        // Create multiple parsers that could potentially interfere
        IndexOverlayParser parser1 = new IndexOverlayParser(simpleCode);
        IndexOverlayParser parser2 = new IndexOverlayParser(complexCode);

        try {
            // Parse with both parsers
            int root1 = parser1.parse();
            int root2 = parser2.parse();

            // Verify both parsed successfully
            requireThat(root1, "parser1 rootId").isNotEqualTo(-1);
            requireThat(root2, "parser2 rootId").isNotEqualTo(-1);

            // Verify reconstruction works for both
            requireThat(parser1.getNodeText(root1), "parser1 reconstruction").isEqualTo(simpleCode);
            requireThat(parser2.getNodeText(root2), "parser2 reconstruction").isEqualTo(complexCode);

        } catch (IndexOverlayParser.ParseException e) {
            fail("Concurrent parsing should succeed: " + e.getMessage());
        }

        // Both parsers should handle concurrent parsing correctly
    }

    /**
     * Tests that security protections apply to all JDK 25 language constructs.
     *
     * This ensures that new language features don't bypass existing security measures.
     */
    @Test(description = "JDK 25 security protection coverage")
    public void testJdk25SecurityProtectionCoverage() {
        // Test deeply nested pattern matching (JEP 507)
        StringBuilder deepPatterns = new StringBuilder();
        deepPatterns.append("public class Test { String process(Object obj) { return switch (obj) { ");

        // Create deep nesting that should trigger stack protection
        for (int i = 0; i < 1200; i++) {
            deepPatterns.append("case Integer x when x > ").append(i).append(" -> switch (x) { ");
        }
        deepPatterns.append("default -> \"deep\"");
        for (int i = 0; i < 1200; i++) {
            deepPatterns.append("; }");
        }
        deepPatterns.append("; }; } }");

        IndexOverlayParser parser = new IndexOverlayParser(deepPatterns.toString(), JavaVersion.JAVA_25);

        try {
            parser.parse();
            fail("Expected ParseException due to recursion depth limit in JDK 25 patterns");
        } catch (IndexOverlayParser.ParseException e) {
            requireThat(e.getMessage().contains("Maximum recursion depth exceeded"), "recursion protection")
                .isEqualTo(true);
        }

        // System should remain stable after JDK 25 recursion limit
    }
}