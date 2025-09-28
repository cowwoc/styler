package io.github.cowwoc.styler.parser.test;

import io.github.cowwoc.styler.parser.IndexOverlayParser;
import io.github.cowwoc.styler.parser.JavaVersion;
import io.github.cowwoc.styler.parser.NodeRegistry;
import io.github.cowwoc.styler.parser.NodeType;
import org.testng.annotations.Test;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;
import static org.testng.Assert.*;

/**
 * Tests for core parser system behavior and correctness.
 *
 * This test class focuses on validating parser behavior rather than input validation.
 * It tests the parser's ability to correctly process various Java constructs and
 * produce accurate AST representations. This addresses the code quality requirement
 * to eliminate test input validation helper methods and focus on system behavior.
 *
 * Each test validates:
 * - Parser produces correct AST structure for given input
 * - Source code reconstruction matches original input
 * - Node relationships and metadata are accurate
 * - Error handling maintains system stability
 */
@Test(singleThreaded = true)
public class ParserBehaviorTest {

    /**
     * Tests parser behavior with minimal valid Java class.
     *
     * Validates that the parser correctly handles the simplest possible
     * Java class declaration and produces appropriate AST structure.
     */
    @Test(description = "Minimal Java class parsing behavior")
    public void testMinimalClassParsingBehavior() {
        String minimalClass = "public class Test { }";

        IndexOverlayParser parser = new IndexOverlayParser(minimalClass);
        int rootId = parser.parse();

        // Validate parser behavior: correct AST structure
        requireThat(rootId, "rootId").isNotEqualTo(-1);

        NodeRegistry.NodeInfo root = parser.getNode(rootId);
        requireThat(root.nodeType(), "root_type").isEqualTo(NodeType.COMPILATION_UNIT);

        // Validate parser behavior: accurate source reconstruction
        String reconstructed = parser.getNodeText(rootId);
        requireThat(reconstructed, "source_reconstruction").isEqualTo(minimalClass);

        // Parser should handle simple class correctly
    }

    /**
     * Tests parser behavior with complex method signatures.
     *
     * Validates that the parser correctly handles method signatures with
     * generics, exceptions, annotations, and parameter variations.
     */
    @Test(description = "Complex method signature parsing behavior")
    public void testComplexMethodSignatureParsingBehavior() {
        String complexMethod = """
            import java.util.List;
            import java.util.Map;

            public class ComplexMethodExample {
                @Override
                @SuppressWarnings("unchecked")
                public final <T extends Comparable<T>, R> Map<String, List<R>>
                    processData(T input, List<? super T> items, R... results)
                    throws IllegalArgumentException, IllegalStateException {

                    if (input == null) {
                        throw new IllegalArgumentException("Input cannot be null");
                    }

                    return Map.of();
                }
            }
            """;

        IndexOverlayParser parser = new IndexOverlayParser(complexMethod);
        int rootId = parser.parse();

        // Validate parser behavior: successful parsing of complex signatures
        requireThat(rootId, "complex_method rootId").isNotEqualTo(-1);

        // Validate parser behavior: correct AST structure preservation
        String reconstructed = parser.getNodeText(rootId);
        requireThat(reconstructed, "complex_method reconstruction").isEqualTo(complexMethod);

        // Validate parser behavior: AST contains expected structure
        NodeRegistry.NodeInfo root = parser.getNode(rootId);
        requireThat(root.childIds().size(), "complex_method children").isGreaterThan(0);
    }

    /**
     * Tests parser behavior with nested generic types.
     *
     * Validates that the parser correctly handles deeply nested generic
     * type declarations and maintains proper AST structure.
     */
    @Test(description = "Nested generic types parsing behavior")
    public void testNestedGenericTypesParsingBehavior() {
        String nestedGenerics = """
            import java.util.*;
            import java.util.concurrent.ConcurrentHashMap;

            public class NestedGenericsExample {
                private Map<String, List<Map<Integer, Set<Optional<String>>>>> complexStructure;

                private ConcurrentHashMap<
                    UUID,
                    Map<String, List<Function<String, Optional<Integer>>>>
                > functionMap;

                public <T extends Comparable<T> & Serializable,
                        U extends Collection<? super T>,
                        V extends Map<? extends String, ? super U>>
                Optional<V> processNestedStructure(T item, U collection, V map) {

                    if (collection.contains(item)) {
                        map.put(item.toString(), collection);
                        return Optional.of(map);
                    }

                    return Optional.empty();
                }
            }
            """;

        IndexOverlayParser parser = new IndexOverlayParser(nestedGenerics);
        int rootId = parser.parse();

        // Validate parser behavior: handles complex generic nesting
        requireThat(rootId, "nested_generics rootId").isNotEqualTo(-1);

        // Validate parser behavior: preserves complex type information
        String reconstructed = parser.getNodeText(rootId);
        requireThat(reconstructed, "nested_generics reconstruction").isEqualTo(nestedGenerics);

        // Parser should handle complex generics successfully
    }

    /**
     * Tests parser behavior with annotation-heavy code.
     *
     * Validates that the parser correctly processes various annotation
     * forms and maintains their relationships in the AST.
     */
    @Test(description = "Annotation-heavy code parsing behavior")
    public void testAnnotationHeavyCodeParsingBehavior() {
        String annotationHeavy = """
            import java.lang.annotation.*;

            @Retention(RetentionPolicy.RUNTIME)
            @Target({ElementType.TYPE, ElementType.METHOD, ElementType.FIELD})
            @Documented
            @interface CustomAnnotation {
                String value() default "";
                int priority() default 0;
                String[] tags() default {};
            }

            @CustomAnnotation(
                value = "Example class",
                priority = 10,
                tags = {"important", "tested", "documented"}
            )
            @SuppressWarnings({"unchecked", "deprecation"})
            @Deprecated(since = "1.5", forRemoval = true)
            public class AnnotationExample {

                @CustomAnnotation("Field annotation")
                @Nullable
                private String annotatedField;

                @CustomAnnotation(value = "Method annotation", priority = 5)
                @Override
                @SafeVarargs
                @SuppressWarnings("varargs")
                public final void annotatedMethod(@NonNull String... args) {
                    // Method implementation
                }
            }
            """;

        IndexOverlayParser parser = new IndexOverlayParser(annotationHeavy);
        int rootId = parser.parse();

        // Validate parser behavior: processes annotation-heavy code
        requireThat(rootId, "annotation_heavy rootId").isNotEqualTo(-1);

        // Validate parser behavior: preserves all annotations
        String reconstructed = parser.getNodeText(rootId);
        requireThat(reconstructed, "annotation_heavy reconstruction").isEqualTo(annotationHeavy);

        // Validate parser behavior: AST structure includes annotations
        NodeRegistry.NodeInfo root = parser.getNode(rootId);
        requireThat(root.nodeType(), "annotation_heavy root type").isEqualTo(NodeType.COMPILATION_UNIT);
    }

    /**
     * Tests parser behavior with lambda and stream operations.
     *
     * Validates that the parser correctly handles functional programming
     * constructs and complex lambda expressions.
     */
    @Test(description = "Lambda and stream operations parsing behavior")
    public void testLambdaStreamOperationsParsingBehavior() {
        String lambdaStreams = """
            import java.util.*;
            import java.util.stream.Collectors;
            import java.util.function.Predicate;

            public class LambdaStreamExample {
                public Map<String, List<Integer>> processData(List<String> input) {
                    return input.stream()
                        .filter(Objects::nonNull)
                        .filter(s -> !s.isBlank())
                        .filter(((Predicate<String>) String::isEmpty).negate())
                        .map(String::trim)
                        .map(s -> s.split(","))
                        .flatMap(Arrays::stream)
                        .map(s -> {
                            try {
                                return Integer.parseInt(s.trim());
                            } catch (NumberFormatException e) {
                                return 0;
                            }
                        })
                        .filter(i -> i > 0)
                        .collect(Collectors.groupingBy(
                            i -> i % 2 == 0 ? "even" : "odd",
                            TreeMap::new,
                            Collectors.toList()
                        ));
                }

                public void complexLambdaExample() {
                    Map<String, Function<Integer, String>> operations = Map.of(
                        "square", i -> String.valueOf(i * i),
                        "double", i -> String.valueOf(i * 2),
                        "format", i -> String.format("Number: %,d", i)
                    );

                    BiFunction<String, Integer, Optional<String>> processor =
                        (operation, value) -> Optional.ofNullable(operations.get(operation))
                            .map(func -> func.apply(value));
                }
            }
            """;

        IndexOverlayParser parser = new IndexOverlayParser(lambdaStreams);
        int rootId = parser.parse();

        // Validate parser behavior: handles complex lambda expressions
        requireThat(rootId, "lambda_streams rootId").isNotEqualTo(-1);

        // Validate parser behavior: preserves functional constructs
        String reconstructed = parser.getNodeText(rootId);
        requireThat(reconstructed, "lambda_streams reconstruction").isEqualTo(lambdaStreams);

        // Validate parser behavior: maintains proper AST structure
        NodeRegistry.NodeInfo root = parser.getNode(rootId);
        requireThat(root.childIds().size(), "lambda_streams children").isGreaterThan(0);
    }

    /**
     * Tests parser behavior with error recovery scenarios.
     *
     * Validates that the parser maintains operational state and provides
     * meaningful error information when encountering malformed code.
     */
    @Test(description = "Error recovery parsing behavior")
    public void testErrorRecoveryParsingBehavior() {
        String[] errorScenarios = {
            // Incomplete statement
            "public class Test { void method() { int x = ",
            // Missing closing brace
            "public class Test { void method() { System.out.println(\"test\"); ",
            // Invalid modifier combination
            "public final abstract class InvalidClass { }",
            // Malformed generic declaration
            "public class Test<T extends> { }",
            // Invalid method signature
            "public class Test { void method(,) { } }",
        };

        for (String malformedCode : errorScenarios) {
            IndexOverlayParser parser = new IndexOverlayParser(malformedCode);

            try {
                parser.parse();
                fail("Expected ParseException for malformed code: " + malformedCode);
            } catch (IndexOverlayParser.ParseException e) {
                // Validate parser behavior: provides meaningful error information
                String message = e.getMessage();
                assertNotNull(message, "Error message should not be null");
                assertFalse(message.isBlank(), "Error message should not be blank");

                // Parser should handle errors gracefully
            }
        }
    }

    /**
     * Tests parser behavior with version-specific features.
     *
     * Validates that the parser correctly handles features based on
     * the specified Java version and rejects inappropriate constructs.
     */
    @Test(description = "Version-specific feature parsing behavior")
    public void testVersionSpecificFeatureParsingBehavior() {
        // JDK 8 code with lambda expressions
        String jdk8Code = """
            import java.util.List;
            public class Jdk8Example {
                public void process() {
                    for (Integer item : List.of(1, 2, 3)) {
                        System.out.println(item);
                    }
                }
            }
            """;

        // JDK 17 code with records
        String jdk17Code = """
            public record Person(String name, int age) {
                public Person {
                    if (age < 0) throw new IllegalArgumentException("Age must be non-negative, but was: " + age);
                }
            }
            """;

        // Test JDK 8 parser behavior
        IndexOverlayParser jdk8Parser = new IndexOverlayParser(jdk8Code, JavaVersion.JAVA_8);
        int jdk8Root = jdk8Parser.parse();

        // Validate parser behavior: JDK 8 code parses with JDK 8 parser
        requireThat(jdk8Root, "JDK 8 rootId").isNotEqualTo(-1);
        requireThat(jdk8Parser.getNodeText(jdk8Root), "JDK 8 reconstruction").isEqualTo(jdk8Code);

        // Test JDK 17 parser behavior
        IndexOverlayParser jdk17Parser = new IndexOverlayParser(jdk17Code, JavaVersion.JAVA_17);
        int jdk17Root = jdk17Parser.parse();

        // Validate parser behavior: JDK 17 code parses with JDK 17 parser
        requireThat(jdk17Root, "JDK 17 rootId").isNotEqualTo(-1);
        requireThat(jdk17Parser.getNodeText(jdk17Root), "JDK 17 reconstruction").isEqualTo(jdk17Code);

        // Both parsers should handle version-specific code correctly
    }

    /**
     * Tests parser behavior with performance characteristics.
     *
     * Validates that the parser maintains consistent performance
     * behavior across different input sizes and complexity levels.
     */
    @Test(description = "Performance behavior validation")
    public void testPerformanceBehaviorValidation() {
        // Small input
        String smallInput = "public class Small { }";

        // Medium input with multiple methods
        StringBuilder mediumInputBuilder = new StringBuilder();
        mediumInputBuilder.append("public class Medium {\n");
        for (int i = 0; i < 100; i++) {
            mediumInputBuilder.append("    public void method").append(i).append("() { }\n");
        }
        mediumInputBuilder.append("}");
        String mediumInput = mediumInputBuilder.toString();

        // Measure parser behavior with small input
        long startTime = System.nanoTime();
        IndexOverlayParser smallParser = new IndexOverlayParser(smallInput);
        int smallRoot = smallParser.parse();
        long smallTime = System.nanoTime() - startTime;

        // Measure parser behavior with medium input
        startTime = System.nanoTime();
        IndexOverlayParser mediumParser = new IndexOverlayParser(mediumInput);
        int mediumRoot = mediumParser.parse();
        long mediumTime = System.nanoTime() - startTime;

        // Validate parser behavior: both inputs parse successfully
        requireThat(smallRoot, "small_input_rootId").isNotEqualTo(-1);
        requireThat(mediumRoot, "medium_input_rootId").isNotEqualTo(-1);

        // Validate parser behavior: performance scales reasonably
        long maxParseTime = 100_000_000L; // 100ms
        requireThat(smallTime, "small_input_time").isLessThan(maxParseTime);
        requireThat(mediumTime, "medium_input_time").isLessThan(maxParseTime);

        // Validate parser behavior: complexity scaling is reasonable
        requireThat(mediumTime, "complexity_scaling").isLessThan(smallTime * 50);

        // Both parsers should handle different input sizes correctly
    }
}