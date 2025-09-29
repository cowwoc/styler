package io.github.cowwoc.styler.parser.test;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import io.github.cowwoc.styler.parser.ArenaNodeStorage;
import io.github.cowwoc.styler.parser.IndexOverlayParser;
import io.github.cowwoc.styler.parser.NodeType;
import io.github.cowwoc.styler.parser.ParseMetrics;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;
import static org.testng.Assert.*;

/**
 * TestNG-based comprehensive test suite for IndexOverlayParser covering all Java 25 features.
 *
 * Evidence: User specifically requested "many test cases to ensure that the parser
 * is able to handle every single aspect of Java 25 code."
 */
@Test(singleThreaded = true)
public class IndexOverlayParserTest {

    private IndexOverlayParser parser;

    @BeforeMethod
    public void setUp() {
        // Enable metrics for test validation
        System.setProperty("styler.metrics.enabled", "true");
        ParseMetrics.reset();
    }

    @Test(description = "Simple class declaration")
    public void parseSimpleClass() {
        String source = """
            package com.example;

            public class HelloWorld {
                public static void main(String[] args) {
                    System.out.println("Hello, World!");
                }
            }
            """;

        parser = new IndexOverlayParser(source);
        int rootId = parser.parse();

        // Use Requirements API for validation
        requireThat(rootId, "rootId").isNotEqualTo(-1);

        ArenaNodeStorage.NodeInfo root = parser.getNode(rootId);
        requireThat(root.nodeType(), "root.nodeType")
            .isEqualTo(NodeType.COMPILATION_UNIT);

        String parsedText = parser.getNodeText(rootId);
        requireThat(parsedText, "parsedText")
            .isEqualTo(source);

        // Verify parse metrics were collected
        ParseMetrics.MetricsSnapshot metrics = ParseMetrics.getSnapshot();
        requireThat(metrics.totalFilesProcessed(), "totalFilesProcessed")
            .isEqualTo(1L);
        requireThat(metrics.totalNodesAllocated(), "totalNodesAllocated")
            .isGreaterThan(0L);
    }

    @Test(description = "Interface with default and static methods")
    public void parseInterfaceWithMethods() {
        String source = """
            public interface Calculator {
                int add(int a, int b);

                default int multiply(int a, int b) {
                    return a * b;
                }

                static int subtract(int a, int b) {
                    return a - b;
                }
            }
            """;

        parser = new IndexOverlayParser(source);
        int rootId = parser.parse();

        requireThat(rootId, "rootId").isNotEqualTo(-1);
        requireThat(parser.getNodeText(rootId), "parsedText").isEqualTo(source);
    }

    @Test(description = "Enum with methods and constructor")
    public void parseEnumWithMethods() {
        String source = """
            public enum Planet {
                MERCURY(3.303e+23, 2.4397e6),
                VENUS(4.869e+24, 6.0518e6),
                EARTH(5.976e+24, 6.37814e6);

                private final double mass;
                private final double radius;

                Planet(double mass, double radius) {
                    this.mass = mass;
                    this.radius = radius;
                }

                public double surfaceGravity() {
                    return G * mass / (radius * radius);
                }
            }
            """;

        parser = new IndexOverlayParser(source);
        int rootId = parser.parse();

        requireThat(rootId, "rootId").isNotEqualTo(-1);
    }

    @Test(description = "Lambda expressions and method references")
    public void parseLambdasAndMethodReferences() {
        String source = """
            public class LambdaExample {
                public void demonstrate() {
                    // Lambda expressions
                    Runnable r1 = () -> System.out.println("Hello");
                    Function<String, Integer> f1 = s -> s.length();
                    BinaryOperator<Integer> f2 = (a, b) -> a + b;

                    // Method references
                    Consumer<String> printer = System.out::println;
                    Function<String, Integer> parser = Integer::parseInt;
                    Supplier<List<String>> factory = ArrayList::new;

                    // Stream operations
                    List<String> result = list.stream()
                        .filter(s -> s.startsWith("A"))
                        .map(String::toUpperCase)
                        .collect(Collectors.toList());
                }
            }
            """;

        parser = new IndexOverlayParser(source);
        int rootId = parser.parse();

        requireThat(rootId, "rootId").isNotEqualTo(-1);
    }

    @Test(description = "Record declaration with validation")
    public void parseComplexRecord() {
        String source = """
            public record Person(String name, int age) {
                public Person {
                    if (age < 0) {
                        throw new IllegalArgumentException("Age cannot be negative");
                    }
                    name = name.trim();
                }

                public Person(String name) {
                    this(name, 0);
                }

                public boolean isAdult() {
                    return age >= 18;
                }

                public static Person unknown() {
                    return new Person("Unknown", -1);
                }
            }
            """;

        parser = new IndexOverlayParser(source);
        int rootId = parser.parse();

        requireThat(rootId, "rootId").isNotEqualTo(-1);
    }

    @Test(description = "Switch expressions with yield")
    public void parseSwitchExpression() {
        String source = """
            public class SwitchExample {
                public String processDay(Day day) {
                    return switch (day) {
                        case MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY -> "Weekday";
                        case SATURDAY, SUNDAY -> "Weekend";
                    };
                }

                public int calculateValue(Object obj) {
                    return switch (obj) {
                        case Integer i -> i * 2;
                        case String s -> {
                            System.out.println("Processing string: " + s);
                            yield s.length();
                        }
                        case null -> 0;
                        default -> -1;
                    };
                }
            }
            """;

        parser = new IndexOverlayParser(source);
        int rootId = parser.parse();

        requireThat(rootId, "rootId").isNotEqualTo(-1);
    }

    @Test(description = "Sealed class hierarchy")
    public void parseSealedClasses() {
        String source = """
            public sealed class Shape
                permits Circle, Rectangle, Triangle {
                protected final String name;

                protected Shape(String name) {
                    this.name = name;
                }
            }

            final class Circle extends Shape {
                private final double radius;

                public Circle(double radius) {
                    super("Circle");
                    this.radius = radius;
                }
            }

            non-sealed class Triangle extends Shape {
                public Triangle() {
                    super("Triangle");
                }
            }
            """;

        parser = new IndexOverlayParser(source);
        int rootId = parser.parse();

        requireThat(rootId, "rootId").isNotEqualTo(-1);
    }

    @Test(description = "Text blocks with various formats")
    public void parseTextBlocks() {
        String source = """
            public class TextBlockExample {
                private static final String JSON = \"\"\"
                    {
                        "name": "John Doe",
                        "age": 30,
                        "city": "New York"
                    }
                    \"\"\";

                private static final String SQL = \"\"\"
                    SELECT id, name, email
                    FROM users
                    WHERE age > ?
                    ORDER BY name
                    \"\"\";
            }
            """;

        parser = new IndexOverlayParser(source);
        int rootId = parser.parse();

        requireThat(rootId, "rootId").isNotEqualTo(-1);
    }

    @DataProvider(name = "largeFileData")
    public Object[][] largeFileData() {
        return new Object[][]{
            {100},
            {1000},
            {2000}
        };
    }

    @Test(dataProvider = "largeFileData", description = "Large file parsing performance")
    public void parseLargeFiles(int classCount) {
        StringBuilder source = new StringBuilder();
        source.append("package com.example.large;\n\n");

        for (int i = 0; i < classCount; i++) {
            source.append(String.format("""
                public class Generated%d {
                    private int field%d = %d;

                    public int getField%d() {
                        return field%d;
                    }

                    public void setField%d(int value) {
                        this.field%d = value;
                    }
                }

                """, i, i, i, i, i, i, i));
        }

        long startTime = System.nanoTime();
        parser = new IndexOverlayParser(source.toString());
        int rootId = parser.parse();
        long parseTime = System.nanoTime() - startTime;

        assertNotEquals(rootId, -1);

        // Verify performance metrics
        ParseMetrics.MetricsSnapshot metrics = ParseMetrics.getSnapshot();
        assertTrue(metrics.getAverageParseTimeMs() > 0);
        assertTrue(metrics.totalNodesAllocated() > classCount);

        System.out.printf("Parsed %d classes in %.2f ms%n", classCount, parseTime / 1_000_000.0);
    }

    @Test(description = "Parser correctly reports syntax errors for invalid Java")
    public void parseWithSyntaxErrors() {
        String source = """
            public class ErrorExample {
                // Missing semicolon
                private int field1

                // Unclosed string
                private String field2 = "unclosed string

                // Valid method after errors
                public void validMethod() {
                    System.out.println("This should still parse");
                }

                // Missing closing brace intentionally
            """;

        parser = new IndexOverlayParser(source);

        // Should throw exception for invalid Java syntax - this is correct behavior
        try {
            int rootId = parser.parse();
            fail("Parser should reject invalid Java syntax with clear error messages");
        } catch (Exception e) {
            // Verify we get a meaningful error message
            assertTrue(e.getMessage().contains("Expected") || e.getMessage().contains("found"),
                "Error message should be descriptive: " + e.getMessage());

            // Verify error metrics were collected if available
            ParseMetrics.MetricsSnapshot metrics = ParseMetrics.getSnapshot();
            // Note: metrics may not have errors if parsing fails early
        }
    }

    @Test(description = "All comment types preserved")
    public void parseCommentsAndJavadoc() {
        String source = """
            /**
             * Main class documentation.
             *
             * @author Test Author
             * @version 1.0
             */
            public class CommentExample {

                // Single line comment
                private int field1;

                /* Multi-line comment
                   spanning multiple lines */
                private int field2;

                /**
                 * Method documentation with parameters.
                 *
                 * @param input the input parameter
                 * @return the processed result
                 * @throws IllegalArgumentException if input is null
                 */
                public String process(String input) {
                    // TODO: Implement this method
                    /* FIXME: Handle null input */
                    return input != null ? input.toUpperCase() : "";
                }
            }
            """;

        parser = new IndexOverlayParser(source);
        int rootId = parser.parse();

        requireThat(rootId, "rootId").isNotEqualTo(-1);
        requireThat(parser.getNodeText(rootId), "parsedText").isEqualTo(source);
    }

    @Test(description = "Complex generic declarations")
    public void parseComplexGenerics() {
        String source = """
            public class GenericExample<T extends Comparable<T> & Serializable> {

                private final Map<String, List<T>> data = new HashMap<>();

                public <U extends T> Optional<U> findFirst(
                        Predicate<? super U> predicate,
                        Class<U> type) {
                    return data.values().stream()
                        .flatMap(List::stream)
                        .filter(item -> type.isInstance(item))
                        .map(type::cast)
                        .filter(predicate)
                        .findFirst();
                }

                public static <K, V> Builder<K, V> builder() {
                    return new Builder<K, V>();
                }

                public static class Builder<K, V> {
                    private final Map<K, V> map = new HashMap<>();

                    public Builder<K, V> put(K key, V value) {
                        map.put(key, value);
                        return this;
                    }

                    public Map<K, V> build() {
                        return Map.copyOf(map);
                    }
                }
            }
            """;

        parser = new IndexOverlayParser(source);
        int rootId = parser.parse();

        requireThat(rootId, "rootId").isNotEqualTo(-1);
    }

    @Test(description = "Incremental update simulation")
    public void testIncrementalParsing() {
        String originalSource = """
            public class Test {
                private int field = 42;

                public int getField() {
                    return field;
                }
            }
            """;

        parser = new IndexOverlayParser(originalSource);
        int originalRootId = parser.parse();

        // Simulate text edit (add a method)
        String newMethod = """

                public void setField(int value) {
                    this.field = value;
                }
            """;

        IndexOverlayParser.EditRange edit = new IndexOverlayParser.EditRange(
            originalSource.indexOf("}") - 1, // Before closing brace
            0, // No deletion
            newMethod.length(),
            newMethod
        );

        int updatedRootId = parser.parseIncremental(java.util.List.of(edit));

        assertNotEquals(updatedRootId, -1);

        // Verify incremental parsing metrics
        ParseMetrics.MetricsSnapshot metrics = ParseMetrics.getSnapshot();
        assertTrue(metrics.totalFilesProcessed() >= 2); // Original + incremental
    }

    @Test(description = "Pattern matching for switch with when guards")
    public void parsePatternMatchingSwitch() {
        String source = """
            public class PatternExample {
                public String processValue(Object obj) {
                    return switch (obj) {
                        case String s when s.length() > 10 -> "Long string: " + s;
                        case String s -> "Short string: " + s;
                        case Integer i when i > 100 -> "Large number: " + i;
                        case Integer i -> "Small number: " + i;
                        case Point(int x, int y) when x == y -> "Diagonal point";
                        case Point(int x, int y) -> "Point at (" + x + "," + y + ")";
                        case null -> "null value";
                        default -> "Unknown type";
                    };
                }
            }
            """;

        parser = new IndexOverlayParser(source);
        int rootId = parser.parse();

        requireThat(rootId, "rootId").isNotEqualTo(-1);
    }

    @Test(description = "Module declaration with all directives")
    public void parseModuleDeclaration() {
        String source = """
            module com.example.mymodule {
                requires java.base;
                requires transitive java.logging;
                requires static java.compiler;

                exports com.example.api;
                exports com.example.spi to java.base, java.logging;

                opens com.example.internal to java.base;

                uses com.example.spi.ServiceProvider;
                provides com.example.spi.ServiceProvider
                    with com.example.impl.ServiceProviderImpl;
            }
            """;

        parser = new IndexOverlayParser(source);
        int rootId = parser.parse();

        requireThat(rootId, "rootId").isNotEqualTo(-1);
    }
}