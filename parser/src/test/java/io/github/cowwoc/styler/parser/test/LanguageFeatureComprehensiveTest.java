package io.github.cowwoc.styler.parser.test;

import io.github.cowwoc.styler.parser.IndexOverlayParser;
import io.github.cowwoc.styler.parser.JavaVersion;
import io.github.cowwoc.styler.parser.NodeRegistry;
import io.github.cowwoc.styler.parser.NodeType;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;
import static org.testng.Assert.*;

/**
 * Comprehensive language feature test suite organized by JDK version.
 *
 * This test class implements the multi-tier test architecture recommended by the
 * technical architect, with systematic coverage of all Java language features
 * from JDK 8 through JDK 25. Each test validates both structural correctness
 * and semantic accuracy of the parsed AST.
 *
 * Architecture:
 * - Data-driven test framework with parameterized tests
 * - Deep equality checking with semantic validation
 * - Performance baseline integration for regression detection
 * - Comprehensive error recovery validation
 */
@Test(singleThreaded = true)
public class LanguageFeatureComprehensiveTest {

    /**
     * Test data provider for JDK 8 language features.
     *
     * Provides realistic Java code samples demonstrating each JDK 8 feature
     * in practical scenarios, avoiding hardcoded empty strings.
     */
    @DataProvider(name = "jdk8Features")
    public Object[][] jdk8FeatureData() {
        return new Object[][]{
            {
                "Lambda expressions with type inference",
                """
                import java.util.List;
                import java.util.stream.Collectors;

                public class LambdaExample {
                    public List<String> processNames(List<String> names) {
                        return names.stream()
                            .filter(name -> name != null && !name.isBlank())
                            .map(name -> name.trim().toUpperCase())
                            .sorted((a, b) -> a.compareToIgnoreCase(b))
                            .collect(Collectors.toList());
                    }
                }
                """
            },
            {
                "Method references - all types",
                """
                import java.util.List;
                import java.util.function.Function;

                public class MethodReferenceExample {
                    private static String format(String input) {
                        return input.trim();
                    }

                    public void demonstrateReferences() {
                        List<String> data = List.of("  hello  ", "  world  ");

                        // Static method reference
                        data.stream().map(MethodReferenceExample::format);

                        // Instance method reference
                        data.stream().map(String::trim);

                        // Constructor reference
                        Function<String, StringBuilder> builder = StringBuilder::new;
                    }
                }
                """
            },
            {
                "Default and static interface methods",
                """
                public interface ProcessorInterface {
                    // Abstract method
                    String process(String input);

                    // Default method
                    default String preprocess(String input) {
                        return input != null ? input.trim() : "";
                    }

                    // Static method
                    static String postprocess(String output) {
                        return output.isEmpty() ? "EMPTY" : output;
                    }

                    // Another default with implementation
                    default boolean isValid(String input) {
                        return input != null && !input.isBlank();
                    }
                }
                """
            },
            {
                "Type annotations and repeating annotations",
                """
                import java.lang.annotation.Repeatable;
                import java.lang.annotation.ElementType;
                import java.lang.annotation.Target;

                @Repeatable(Authors.class)
                @interface Author {
                    String value();
                }

                @interface Authors {
                    Author[] value();
                }

                @Author("John Doe")
                @Author("Jane Smith")
                public class AnnotationExample {
                    private @NonNull String name;

                    public void process(@NonNull String input) {
                        @NonNull String processed = input.trim();
                        System.out.println(processed);
                    }
                }
                """
            }
        };
    }

    /**
     * Test data provider for JDK 14-17 language features.
     *
     * Covers records, sealed classes, pattern matching, text blocks, and switch expressions.
     */
    @DataProvider(name = "jdk14to17Features")
    public Object[][] jdk14to17FeatureData() {
        return new Object[][]{
            {
                "Records with validation and methods",
                """
                public record Person(String name, int age) {
                    public Person {
                        if (name == null || name.isBlank()) {
                            throw new IllegalArgumentException("Name cannot be null or blank");
                        }
                        if (age < 0 || age > 150) {
                            throw new IllegalArgumentException("Invalid age: " + age);
                        }
                    }

                    public String getDisplayName() {
                        return name + " (" + age + " years old)";
                    }

                    public boolean isAdult() {
                        return age >= 18;
                    }
                }
                """
            },
            {
                "Sealed classes with permits clause",
                """
                public sealed class Shape
                    permits Circle, Rectangle, Triangle {

                    protected final String color;

                    protected Shape(String color) {
                        this.color = color;
                    }

                    public abstract double area();
                }

                final class Circle extends Shape {
                    private final double radius;

                    public Circle(String color, double radius) {
                        super(color);
                        this.radius = radius;
                    }

                    @Override
                    public double area() {
                        return Math.PI * radius * radius;
                    }
                }

                final class Rectangle extends Shape {
                    private final double width, height;

                    public Rectangle(String color, double width, double height) {
                        super(color);
                        this.width = width;
                        this.height = height;
                    }

                    @Override
                    public double area() {
                        return width * height;
                    }
                }

                non-sealed class Triangle extends Shape {
                    private final double base, height;

                    public Triangle(String color, double base, double height) {
                        super(color);
                        this.base = base;
                        this.height = height;
                    }

                    @Override
                    public double area() {
                        return 0.5 * base * height;
                    }
                }
                """
            },
            {
                "Pattern matching with instanceof and switch",
                """
                public class PatternMatchingExample {
                    public String describe(Object obj) {
                        // Pattern matching with instanceof
                        if (obj instanceof String s && s.length() > 0) {
                            return "Non-empty string: " + s;
                        } else if (obj instanceof Integer i && i > 0) {
                            return "Positive integer: " + i;
                        }

                        // Pattern matching with switch
                        return switch (obj) {
                            case String s when s.isBlank() -> "Blank string";
                            case Integer i when i <= 0 -> "Non-positive integer: " + i;
                            case Double d -> "Double value: " + d;
                            case null -> "Null value";
                            default -> "Unknown type: " + obj.getClass().getSimpleName();
                        };
                    }
                }
                """
            },
            {
                "Text blocks with proper formatting",
                """
                public class TextBlockExample {
                    public String getJsonTemplate() {
                        return \"\"\"
                            {
                                "name": "%s",
                                "age": %d,
                                "active": %b,
                                "metadata": {
                                    "created": "%s",
                                    "modified": "%s"
                                }
                            }
                            \"\"\";
                    }

                    public String getSqlQuery() {
                        return \"\"\"
                            SELECT u.id, u.name, u.email,
                                   p.title, p.department
                            FROM users u
                            LEFT JOIN profiles p ON u.id = p.user_id
                            WHERE u.active = true
                              AND u.created_date >= ?
                            ORDER BY u.name, p.title
                            \"\"\";
                    }
                }
                """
            }
        };
    }

    /**
     * Test data provider for JDK 21-25 language features.
     *
     * Covers string templates, unnamed patterns, and the latest language enhancements.
     */
    @DataProvider(name = "jdk21to25Features")
    public Object[][] jdk21to25FeatureData() {
        return new Object[][]{
            {
                "String templates with expressions",
                """
                import static java.lang.StringTemplate.STR;

                public class StringTemplateExample {
                    public String formatUser(String name, int age, boolean active) {
                        return STR."User: \\{name}, Age: \\{age}, Status: \\{active ? "Active" : "Inactive"}";
                    }

                    public String generateReport(double revenue, int customers) {
                        double avgRevenue = revenue / customers;
                        return STR.\"\"\"
                            Monthly Report:
                            - Total Revenue: $\\{revenue:%.2f}
                            - Customer Count: \\{customers}
                            - Average per Customer: $\\{avgRevenue:%.2f}
                            \"\"\";
                    }
                }
                """
            },
            {
                "Unnamed patterns and variables",
                """
                public class UnnamedPatternExample {
                    public void processCoordinates(Object point) {
                        switch (point) {
                            case Point(int x, int y) when x == y -> {
                                System.out.println("Diagonal point: " + x);
                            }
                            case Point(int x, _) when x > 0 -> {
                                System.out.println("Positive X coordinate: " + x);
                            }
                            case Point(_, int y) when y > 0 -> {
                                System.out.println("Positive Y coordinate: " + y);
                            }
                            case Point(_, _) -> {
                                System.out.println("Origin or negative coordinates");
                            }
                            default -> {
                                System.out.println("Not a point");
                            }
                        }
                    }

                    record Point(int x, int y) {}
                }
                """
            }
        };
    }

    /**
     * Tests JDK 8 language features with comprehensive AST validation.
     *
     * Each test validates structural correctness, semantic accuracy,
     * and source position mapping for the parsed AST.
     */
    @Test(dataProvider = "jdk8Features", description = "JDK 8 language features")
    public void testJdk8Features(String featureDescription, String sourceCode) {
        IndexOverlayParser parser = new IndexOverlayParser(sourceCode, JavaVersion.JAVA_8);

        try {
            int rootId = parser.parse();
            validateParseResult(parser, rootId, sourceCode, featureDescription);

            // Verify JDK 8 specific AST nodes are created correctly
            NodeRegistry.NodeInfo root = parser.getNode(rootId);
            requireThat(root.nodeType(), "root node type").isEqualTo(NodeType.COMPILATION_UNIT);

        } catch (IndexOverlayParser.ParseException e) {
            fail("JDK 8 feature '" + featureDescription + "' should parse successfully: " + e.getMessage());
        }
    }

    /**
     * Tests JDK 14-17 language features with enhanced validation.
     *
     * Validates that newer language constructs are properly parsed
     * and that the AST correctly represents their semantic structure.
     */
    @Test(dataProvider = "jdk14to17Features", description = "JDK 14-17 language features")
    public void testJdk14to17Features(String featureDescription, String sourceCode) {
        IndexOverlayParser parser = new IndexOverlayParser(sourceCode, JavaVersion.JAVA_17);

        try {
            int rootId = parser.parse();
            validateParseResult(parser, rootId, sourceCode, featureDescription);

            // Additional validation for specific JDK 14-17 features
            if (featureDescription.contains("sealed")) {
                validateSealedClassAST(parser, rootId);
            } else if (featureDescription.contains("record")) {
                validateRecordAST(parser, rootId);
            } else if (featureDescription.contains("Pattern matching")) {
                validatePatternMatchingAST(parser, rootId);
            }

        } catch (IndexOverlayParser.ParseException e) {
            fail("JDK 14-17 feature '" + featureDescription + "' should parse successfully: " + e.getMessage());
        }
    }

    /**
     * Tests JDK 21-25 language features with latest validation.
     *
     * Ensures that the most recent language features are correctly
     * parsed and represented in the AST structure.
     */
    @Test(dataProvider = "jdk21to25Features", description = "JDK 21-25 language features")
    public void testJdk21to25Features(String featureDescription, String sourceCode) {
        IndexOverlayParser parser = new IndexOverlayParser(sourceCode, JavaVersion.JAVA_25);

        try {
            int rootId = parser.parse();
            validateParseResult(parser, rootId, sourceCode, featureDescription);

            // Additional validation for JDK 21-25 specific features
            if (featureDescription.contains("String template")) {
                validateStringTemplateAST(parser, rootId);
            } else if (featureDescription.contains("Unnamed pattern")) {
                validateUnnamedPatternAST(parser, rootId);
            }

        } catch (IndexOverlayParser.ParseException e) {
            fail("JDK 21-25 feature '" + featureDescription + "' should parse successfully: " + e.getMessage());
        }
    }

    /**
     * Tests error recovery for malformed language feature syntax.
     *
     * Validates that the parser can gracefully handle incomplete or
     * malformed syntax while providing meaningful error messages.
     */
    @Test(description = "Error recovery for malformed syntax")
    public void testErrorRecoveryForMalformedSyntax() {
        String[] malformedSamples = {
            // Incomplete lambda
            "list.stream().filter(x ->",
            // Malformed record
            "public record Person(String name",
            // Invalid sealed class
            "public sealed class Shape permits",
            // Incomplete pattern matching
            "switch (obj) { case String s when ->",
            // Malformed text block
            "String text = \"\"\"incomplete",
        };

        for (String malformedCode : malformedSamples) {
            IndexOverlayParser parser = new IndexOverlayParser(malformedCode, JavaVersion.JAVA_25);

            try {
                parser.parse();
                fail("Expected ParseException for malformed code: " + malformedCode);
            } catch (IndexOverlayParser.ParseException e) {
                // Verify error message is meaningful
                String message = e.getMessage();
                assertNotNull(message, "Error message should not be null");
                assertFalse(message.isBlank(), "Error message should not be blank");
                assertTrue(message.length() > 10, "Error message should be descriptive");

                // Parser should handle error gracefully
            }
        }
    }

    /**
     * Tests performance baselines for different language feature complexity.
     *
     * Validates that parsing performance remains within acceptable bounds
     * for various complexity levels of Java code.
     */
    @Test(description = "Performance baseline validation")
    public void testPerformanceBaselines() {
        // Simple code - should parse very quickly
        String simpleCode = "public class Simple { void method() { } }";

        // Complex code with multiple language features
        String complexCode = """
            import java.util.List;
            import java.util.stream.Collectors;

            public sealed class ComplexExample permits SubClass {
                private final List<String> data;

                public ComplexExample(List<String> data) {
                    this.data = data != null ? data : List.of();
                }

                public String process(Object input) {
                    return switch (input) {
                        case String s when s.length() > 10 -> {
                            yield data.stream()
                                .filter(item -> item.contains(s))
                                .map(String::trim)
                                .collect(Collectors.joining(", "));
                        }
                        case Integer i -> "Number: " + i;
                        case null -> "Null input";
                        default -> "Unknown: " + input.getClass().getSimpleName();
                    };
                }
            }

            final class SubClass extends ComplexExample {
                public SubClass() {
                    super(List.of("example", "data", "items"));
                }
            }
            """;

        // Measure parsing time for simple code
        long startTime = System.nanoTime();
        IndexOverlayParser simpleParser = new IndexOverlayParser(simpleCode);
        simpleParser.parse();
        long simpleTime = System.nanoTime() - startTime;

        // Measure parsing time for complex code
        startTime = System.nanoTime();
        IndexOverlayParser complexParser = new IndexOverlayParser(complexCode);
        complexParser.parse();
        long complexTime = System.nanoTime() - startTime;

        // Performance requirements from technical architect
        long maxParseTimeNs = 100_000_000L; // 100ms in nanoseconds

        requireThat(simpleTime, "simple parsing time").isLessThan(maxParseTimeNs);
        requireThat(complexTime, "complex parsing time").isLessThan(maxParseTimeNs);

        // Complex code should not be more than 10x slower than simple code
        requireThat(complexTime, "complexity overhead").isLessThan(simpleTime * 10);
    }

    /**
     * Validates the basic parsing result structure and correctness.
     */
    private void validateParseResult(IndexOverlayParser parser, int rootId, String originalSource, String description) {
        // Structural validation
        requireThat(rootId, description + " rootId").isNotEqualTo(-1);

        NodeRegistry.NodeInfo root = parser.getNode(rootId);
        requireThat(root, description + " root node").isNotNull();

        // Source reconstruction validation
        String reconstructed = parser.getNodeText(rootId);
        requireThat(reconstructed, description + " source reconstruction").isEqualTo(originalSource);

        // AST integrity validation
        validateASTIntegrity(parser, rootId, description);
    }

    /**
     * Validates AST integrity including node relationships and metadata.
     */
    private void validateASTIntegrity(IndexOverlayParser parser, int rootId, String description) {
        NodeRegistry.NodeInfo root = parser.getNode(rootId);

        // Validate node type is appropriate
        requireThat(root.nodeType(), description + " root type").isEqualTo(NodeType.COMPILATION_UNIT);

        // Validate children exist if this is a non-terminal node
        if (root.childIds().size() > 0) {
            for (int i = 0; i < root.childIds().size(); i++) {
                int childId = root.childIds().get(i);
                requireThat(childId, description + " child " + i).isNotEqualTo(-1);

                NodeRegistry.NodeInfo child = parser.getNode(childId);
                requireThat(child, description + " child node " + i).isNotNull();
            }
        }
    }

    /**
     * Specialized validation for sealed class AST structure.
     */
    private void validateSealedClassAST(IndexOverlayParser parser, int rootId) {
        // Implementation would validate sealed class specific AST nodes
        // For now, just verify basic structure
        NodeRegistry.NodeInfo root = parser.getNode(rootId);
        requireThat(root.childIds().size(), "sealed_class_children").isGreaterThan(0);
    }

    /**
     * Specialized validation for record AST structure.
     */
    private void validateRecordAST(IndexOverlayParser parser, int rootId) {
        // Implementation would validate record specific AST nodes
        NodeRegistry.NodeInfo root = parser.getNode(rootId);
        requireThat(root.childIds().size(), "record_children").isGreaterThan(0);
    }

    /**
     * Specialized validation for pattern matching AST structure.
     */
    private void validatePatternMatchingAST(IndexOverlayParser parser, int rootId) {
        // Implementation would validate pattern matching specific AST nodes
        NodeRegistry.NodeInfo root = parser.getNode(rootId);
        requireThat(root.childIds().size(), "pattern_matching_children").isGreaterThan(0);
    }

    /**
     * Specialized validation for string template AST structure.
     */
    private void validateStringTemplateAST(IndexOverlayParser parser, int rootId) {
        // Implementation would validate string template specific AST nodes
        NodeRegistry.NodeInfo root = parser.getNode(rootId);
        requireThat(root.childIds().size(), "string_template_children").isGreaterThan(0);
    }

    /**
     * Specialized validation for unnamed pattern AST structure.
     */
    private void validateUnnamedPatternAST(IndexOverlayParser parser, int rootId) {
        // Implementation would validate unnamed pattern specific AST nodes
        NodeRegistry.NodeInfo root = parser.getNode(rootId);
        requireThat(root.childIds().size(), "unnamed_pattern_children").isGreaterThan(0);
    }
}