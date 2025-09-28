package io.github.cowwoc.styler.parser.test;

import io.github.cowwoc.styler.parser.IndexOverlayParser;
import io.github.cowwoc.styler.parser.JavaVersion;
import org.testng.annotations.Test;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;
import static org.testng.Assert.*;

/**
 * Comprehensive test suite for JDK 25 specific language features.
 *
 * This test class covers the four critical JDK 25 features identified as missing:
 * - JEP 511: Module Import Declaration
 * - JEP 512: Compact Source Files
 * - JEP 513: Flexible Constructor Bodies
 * - JEP 507: Primitive Patterns
 *
 * Each test validates parser behavior for realistic Java code samples that demonstrate
 * these language features in real-world scenarios.
 */
@Test(singleThreaded = true)
public class Jdk25FeaturesTest {

    /**
     * Tests parsing of JEP 511: Module Import Declaration.
     *
     * This feature allows simplified import statements in module-info.java files
     * to import entire modules rather than individual packages.
     */
    @Test(description = "JEP 511: Module Import Declaration")
    public void testModuleImportDeclaration() {
        String moduleInfo = """
            module com.example.myapp {
                requires java.base;
                requires java.logging;

                // JEP 511: Module Import Declaration
                imports java.net.http;
                imports java.security.jgss;

                exports com.example.myapp.api;
                exports com.example.myapp.impl to java.base;
            }
            """;

        IndexOverlayParser parser = new IndexOverlayParser(moduleInfo, JavaVersion.JAVA_25);

        try {
            int rootId = parser.parse();
            requireThat(rootId, "rootId").isNotEqualTo(-1);

            String reconstructed = parser.getNodeText(rootId);
            requireThat(reconstructed, "reconstructed").isEqualTo(moduleInfo);

        } catch (IndexOverlayParser.ParseException e) {
            fail("JEP 511 Module Import Declaration should parse successfully: " + e.getMessage());
        }
    }

    /**
     * Tests parsing of JEP 512: Compact Source Files.
     *
     * This feature allows omitting explicit class declarations for simple programs,
     * making Java source files more concise for scripting and educational purposes.
     */
    @Test(description = "JEP 512: Compact Source Files")
    public void testCompactSourceFiles() {
        String compactSource = """
            // JEP 512: Compact Source Files - implicit main class
            import java.util.List;

            void main() {
                System.out.println("Hello from compact source!");
                var numbers = List.of(1, 2, 3, 4, 5);
                for (Integer number : numbers) {
                    System.out.println(number);
                }
            }

            String formatMessage(String name) {
                return "Hello, " + name + "!";
            }
            """;

        IndexOverlayParser parser = new IndexOverlayParser(compactSource, JavaVersion.JAVA_25);

        try {
            int rootId = parser.parse();
            requireThat(rootId, "rootId").isNotEqualTo(-1);

            String reconstructed = parser.getNodeText(rootId);
            requireThat(reconstructed, "reconstructed").isEqualTo(compactSource);

        } catch (IndexOverlayParser.ParseException e) {
            fail("JEP 512 Compact Source Files should parse successfully: " + e.getMessage());
        }
    }

    /**
     * Tests parsing of JEP 513: Flexible Constructor Bodies.
     *
     * This feature allows statements to appear before explicit constructor invocation
     * (this() or super()) in constructor bodies, enabling validation and preprocessing.
     */
    @Test(description = "JEP 513: Flexible Constructor Bodies")
    public void testFlexibleConstructorBodies() {
        String flexibleConstructor = """
            public class ValidationExample {
                private final String name;
                private final int age;

                public ValidationExample(String name, int age) {
                    // JEP 513: Statements before super()/this() call
                    if (name == null || name.isBlank()) {
                        throw new IllegalArgumentException("Name cannot be null or blank");
                    }
                    if (age < 0 || age > 150) {
                        throw new IllegalArgumentException("Age must be between 0 and 150");
                    }

                    // Preprocessing
                    String processedName = name.trim().toLowerCase();
                    int validatedAge = Math.max(0, age);

                    // Now call the actual constructor logic
                    this(processedName, validatedAge, true);
                }

                private ValidationExample(String processedName, int validatedAge, boolean validated) {
                    this.name = processedName;
                    this.age = validatedAge;
                }
            }
            """;

        IndexOverlayParser parser = new IndexOverlayParser(flexibleConstructor, JavaVersion.JAVA_25);

        try {
            int rootId = parser.parse();
            requireThat(rootId, "rootId").isNotEqualTo(-1);

            String reconstructed = parser.getNodeText(rootId);
            requireThat(reconstructed, "reconstructed").isEqualTo(flexibleConstructor);

        } catch (IndexOverlayParser.ParseException e) {
            fail("JEP 513 Flexible Constructor Bodies should parse successfully: " + e.getMessage());
        }
    }

    /**
     * Tests parsing of JEP 507: Primitive Patterns.
     *
     * This feature extends pattern matching to primitive types, enabling
     * more concise and type-safe code when working with primitives.
     */
    @Test(description = "JEP 507: Primitive Patterns")
    public void testPrimitivePatterns() {
        String primitivePatterns = """
            public class PrimitivePatternExample {

                public String processValue(Object value) {
                    return switch (value) {
                        // JEP 507: Primitive Patterns
                        case int i when i > 0 -> "Positive integer: " + i;
                        case int i when i < 0 -> "Negative integer: " + i;
                        case int i -> "Zero";

                        case double d when d > 0.0 -> "Positive double: " + d;
                        case double d when d < 0.0 -> "Negative double: " + d;
                        case double d -> "Zero double";

                        case float f -> "Float value: " + f;
                        case long l -> "Long value: " + l;
                        case boolean b -> "Boolean: " + b;
                        case char c -> "Character: " + c;

                        case null -> "Null value";
                        default -> "Unknown type: " + value.getClass().getSimpleName();
                    };
                }

                public int calculateScore(Object input) {
                    return switch (input) {
                        case int score when score >= 90 -> score + 10; // Bonus
                        case int score when score >= 80 -> score + 5;
                        case int score -> score;
                        case double percentage -> (int) (percentage * 100);
                        default -> 0;
                    };
                }
            }
            """;

        IndexOverlayParser parser = new IndexOverlayParser(primitivePatterns, JavaVersion.JAVA_25);

        try {
            int rootId = parser.parse();
            requireThat(rootId, "rootId").isNotEqualTo(-1);

            String reconstructed = parser.getNodeText(rootId);
            requireThat(reconstructed, "reconstructed").isEqualTo(primitivePatterns);

        } catch (IndexOverlayParser.ParseException e) {
            fail("JEP 507 Primitive Patterns should parse successfully: " + e.getMessage());
        }
    }

    /**
     * Tests combined usage of multiple JDK 25 features in a single source file.
     *
     * This validates that the parser can handle complex interactions between
     * different language features without conflicts.
     */
    @Test(description = "Combined JDK 25 Features")
    public void testCombinedJdk25Features() {
        String combinedFeatures = """
            // JEP 512: Compact Source File with multiple JDK 25 features
            import java.util.List;
            import java.util.Optional;

            void main() {
                var processor = new DataProcessor("example", 42);
                var result = processor.process(List.of(1, 2.5, "test", true));
                System.out.println("Result: " + result);
            }

            class DataProcessor {
                private final String name;
                private final int threshold;

                public DataProcessor(String name, int threshold) {
                    // JEP 513: Flexible Constructor Bodies
                    if (name == null) {
                        name = "default";
                    }
                    if (threshold < 0) {
                        threshold = 0;
                    }

                    this.name = name.trim();
                    this.threshold = Math.max(0, threshold);
                }

                public String process(List<Object> items) {
                    return items.stream()
                        .map(this::processItem)
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .reduce("", (a, b) -> a + " " + b)
                        .trim();
                }

                private Optional<String> processItem(Object item) {
                    // JEP 507: Primitive Patterns
                    return switch (item) {
                        case int i when i > threshold -> Optional.of("INT:" + i);
                        case double d when d > threshold -> Optional.of("DOUBLE:" + d);
                        case boolean b -> Optional.of("BOOL:" + b);
                        case String s when !s.isBlank() -> Optional.of("STR:" + s);
                        case null -> Optional.empty();
                        default -> Optional.of("OTHER:" + item);
                    };
                }
            }
            """;

        IndexOverlayParser parser = new IndexOverlayParser(combinedFeatures, JavaVersion.JAVA_25);

        try {
            int rootId = parser.parse();
            requireThat(rootId, "rootId").isNotEqualTo(-1);

            String reconstructed = parser.getNodeText(rootId);
            requireThat(reconstructed, "reconstructed").isEqualTo(combinedFeatures);

        } catch (IndexOverlayParser.ParseException e) {
            fail("Combined JDK 25 features should parse successfully: " + e.getMessage());
        }
    }

    /**
     * Tests resource exhaustion protection with JDK 25 features.
     *
     * This test validates that deeply nested JDK 25 constructs are properly
     * protected against stack overflow, meeting security requirements.
     */
    @Test(description = "JDK 25 Features Stack Overflow Protection")
    public void testJdk25FeaturesStackOverflowProtection() {
        StringBuilder deeplyNestedPatterns = new StringBuilder();
        deeplyNestedPatterns.append("public class Test { Object process(Object value) { return switch (value) { ");

        // Create 1500 levels of nested primitive patterns (exceeds MAX_RECURSION_DEPTH = 1000)
        for (int i = 0; i < 1500; i++) {
            deeplyNestedPatterns.append("case int x").append(i).append(" when x > ").append(i).append(" -> switch (x) { ");
        }

        deeplyNestedPatterns.append("default -> 42");

        for (int i = 0; i < 1500; i++) {
            deeplyNestedPatterns.append("; }");
        }

        deeplyNestedPatterns.append("; }; } }");

        IndexOverlayParser parser = new IndexOverlayParser(deeplyNestedPatterns.toString(), JavaVersion.JAVA_25);

        try {
            parser.parse();
            fail("Expected ParseException due to recursion depth limit with JDK 25 features");
        } catch (IndexOverlayParser.ParseException e) {
            String message = e.getMessage();
            requireThat(message.contains("Maximum recursion depth exceeded"), "stack overflow message")
                .isEqualTo(true);
            requireThat(message.contains("1000"), "recursion limit")
                .isEqualTo(true);
        }
    }
}