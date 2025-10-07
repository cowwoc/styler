package io.github.cowwoc.styler.parser.test;

import io.github.cowwoc.styler.parser.ArenaNodeStorage;
import io.github.cowwoc.styler.parser.IndexOverlayParser;
import io.github.cowwoc.styler.parser.NodeType;
import org.testng.annotations.Test;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;

/**
 * Tests for parsing lambda expressions in various contexts.
 * Validates JLS §15.27 (Lambda Expressions) compliance.
 *
 * NOTE: These tests use basic smoke testing (verify parsing succeeds) plus AST validation
 * to ensure lambda expressions are correctly recognized and parsed.
 */
public final class LambdaExpressionParsingTest
{
	/**
	 * Verifies that ThreadLocal.withInitial with lambda block body parses successfully.
	 * This is the original failing case from core/src/main/java/io/github/cowwoc/styler/core/util/Strings.java.
	 *
	 * Bug was: "Expected SEMICOLON but found IDENTIFIER at position 374"
	 * Root cause: Parser failed to recognize lambda expression as valid method argument.
	 */
	@Test
	public void testThreadLocalWithInitialLambdaBlockBody()
	{
		String source = """
			package test;

			import java.text.DecimalFormat;

			public class Test
			{
				private static final ThreadLocal<DecimalFormat> FORMATTER = ThreadLocal.withInitial(() ->
				{
					DecimalFormat formatter = new DecimalFormat();
					return formatter;
				});
			}
			""";

		try (IndexOverlayParser parser = new IndexOverlayParser(source))
		{
			int rootNodeId = parser.parse();
			ArenaNodeStorage storage = parser.getNodeStorage();

			// Verify parsing succeeded
			requireThat(rootNodeId, "rootNodeId").isGreaterThanOrEqualTo(0);

			// Verify AST structure
			ArenaNodeStorage.NodeInfo rootNode = storage.getNode(rootNodeId);
			requireThat(rootNode.nodeType(), "rootNodeType").isEqualTo(NodeType.COMPILATION_UNIT);
		}
	}

	/**
	 * Verifies that lambda with no parameters and block body parses successfully.
	 * Tests basic lambda syntax: () -> { statements }
	 */
	@Test
	public void testLambdaWithNoParametersAndBlockBody()
	{
		String source = """
			package test;

			public class Test
			{
				private static final Runnable task = () ->
				{
					System.out.println("Hello");
				};
			}
			""";

		try (IndexOverlayParser parser = new IndexOverlayParser(source))
		{
			int rootNodeId = parser.parse();
			requireThat(rootNodeId, "rootNodeId").isGreaterThanOrEqualTo(0);
		}
	}

	/**
	 * Verifies that lambda with single parameter parses successfully.
	 * Tests lambda syntax: x -> expression
	 */
	@Test
	public void testLambdaWithSingleParameter()
	{
		String source = """
			package test;

			import java.util.function.Function;

			public class Test
			{
				private static final Function<Integer, Integer> inc = x -> x + 1;
			}
			""";

		try (IndexOverlayParser parser = new IndexOverlayParser(source))
		{
			int rootNodeId = parser.parse();
			requireThat(rootNodeId, "rootNodeId").isGreaterThanOrEqualTo(0);
		}
	}

	/**
	 * Verifies that lambda with multiple parameters parses successfully.
	 * Tests lambda syntax: (a, b) -> expression
	 */
	@Test
	public void testLambdaWithMultipleParameters()
	{
		String source = """
			package test;

			import java.util.function.BiFunction;

			public class Test
			{
				private static final BiFunction<Integer, Integer, Integer> add = (a, b) -> a + b;
			}
			""";

		try (IndexOverlayParser parser = new IndexOverlayParser(source))
		{
			int rootNodeId = parser.parse();
			requireThat(rootNodeId, "rootNodeId").isGreaterThanOrEqualTo(0);
		}
	}

	/**
	 * Verifies that lambda in static field initializer parses successfully.
	 * Tests lambda in static context with method call argument.
	 */
	@Test
	public void testLambdaInStaticFieldInitializer()
	{
		String source = """
			package test;

			import java.util.function.Supplier;

			public class Test
			{
				private static final Supplier<String> supplier = () -> "default";
			}
			""";

		try (IndexOverlayParser parser = new IndexOverlayParser(source))
		{
			int rootNodeId = parser.parse();
			requireThat(rootNodeId, "rootNodeId").isGreaterThanOrEqualTo(0);
		}
	}

	/**
	 * Verifies that lambda in instance field initializer parses successfully.
	 * Tests lambda in instance context.
	 */
	@Test
	public void testLambdaInInstanceFieldInitializer()
	{
		String source = """
			package test;

			import java.util.function.Supplier;

			public class Test
			{
				private final Supplier<Integer> supplier = () -> 42;
			}
			""";

		try (IndexOverlayParser parser = new IndexOverlayParser(source))
		{
			int rootNodeId = parser.parse();
			requireThat(rootNodeId, "rootNodeId").isGreaterThanOrEqualTo(0);
		}
	}

	/**
	 * Verifies that nested lambda expressions parse successfully.
	 * Tests lambda returning lambda: () -> () -> value
	 */
	@Test
	public void testNestedLambdaExpressions()
	{
		String source = """
			package test;

			import java.util.function.Supplier;

			public class Test
			{
				private static final Supplier<Supplier<Integer>> nested = () -> () -> 42;
			}
			""";

		try (IndexOverlayParser parser = new IndexOverlayParser(source))
		{
			int rootNodeId = parser.parse();
			requireThat(rootNodeId, "rootNodeId").isGreaterThanOrEqualTo(0);
		}
	}

	/**
	 * Verifies that method reference as static method argument parses successfully.
	 * Tests method reference (related to lambda): ClassName::methodName
	 */
	@Test
	public void testMethodReferenceAsArgument()
	{
		String source = """
			package test;

			import java.text.DecimalFormat;

			public class Test
			{
				private static final ThreadLocal<DecimalFormat> FORMATTER = ThreadLocal.withInitial(DecimalFormat::new);
			}
			""";

		try (IndexOverlayParser parser = new IndexOverlayParser(source))
		{
			int rootNodeId = parser.parse();
			requireThat(rootNodeId, "rootNodeId").isGreaterThanOrEqualTo(0);
		}
	}

	/**
	 * Verifies that constructor reference as argument parses successfully.
	 * Tests constructor reference: ClassName::new
	 */
	@Test
	public void testConstructorReferenceAsArgument()
	{
		String source = """
			package test;

			import java.util.ArrayList;
			import java.util.function.Supplier;

			public class Test
			{
				private static final Supplier<ArrayList<String>> listSupplier = ArrayList::new;
			}
			""";

		try (IndexOverlayParser parser = new IndexOverlayParser(source))
		{
			int rootNodeId = parser.parse();
			requireThat(rootNodeId, "rootNodeId").isGreaterThanOrEqualTo(0);
		}
	}

	/**
	 * Verifies that empty lambda body parses successfully.
	 * Tests lambda with no statements: () -> {}
	 */
	@Test
	public void testEmptyLambdaBody()
	{
		String source = """
			package test;

			public class Test
			{
				private static final Runnable task = () -> {};
			}
			""";

		try (IndexOverlayParser parser = new IndexOverlayParser(source))
		{
			int rootNodeId = parser.parse();
			requireThat(rootNodeId, "rootNodeId").isGreaterThanOrEqualTo(0);
		}
	}

	/**
	 * Verifies that lambda with explicit parameter types parses successfully.
	 * Tests typed lambda parameters: (Type1 a, Type2 b) -> expression
	 */
	@Test
	public void testExplicitParameterTypes()
	{
		String source = """
			package test;

			import java.util.function.BiFunction;

			public class Test
			{
				private static final BiFunction<String, Integer, String> concat = (String s, Integer i) -> s + i;
			}
			""";

		try (IndexOverlayParser parser = new IndexOverlayParser(source))
		{
			int rootNodeId = parser.parse();
			requireThat(rootNodeId, "rootNodeId").isGreaterThanOrEqualTo(0);
		}
	}

	/**
	 * Verifies that lambda with expression body parses successfully.
	 * Tests lambda without braces: () -> expression
	 */
	@Test
	public void testLambdaWithExpressionBody()
	{
		String source = """
			package test;

			import java.util.function.Supplier;

			public class Test
			{
				private static final Supplier<Integer> answer = () -> 42;
			}
			""";

		try (IndexOverlayParser parser = new IndexOverlayParser(source))
		{
			int rootNodeId = parser.parse();
			requireThat(rootNodeId, "rootNodeId").isGreaterThanOrEqualTo(0);
		}
	}
}
