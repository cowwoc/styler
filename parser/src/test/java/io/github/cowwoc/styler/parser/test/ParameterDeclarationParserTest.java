package io.github.cowwoc.styler.parser.test;

import io.github.cowwoc.styler.ast.core.NodeType;
import io.github.cowwoc.styler.parser.test.ParserTestUtils.SemanticNode;
import org.testng.annotations.Test;

import java.util.Set;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;
import static io.github.cowwoc.styler.parser.test.ParserTestUtils.parseSemanticAst;
import static io.github.cowwoc.styler.parser.test.ParserTestUtils.semanticNode;

/**
 * Tests for parsing parameter declarations in methods, constructors, catch clauses, and records.
 */
public final class ParameterDeclarationParserTest
{
	// ==================== Simple Parameter Tests ====================

	/**
	 * Validates parsing of a method with a simple primitive parameter.
	 */
	@Test
	public void shouldParseSimplePrimitiveParameter()
	{
		String source = """
			public class Test
			{
				public void foo(int x)
				{
				}
			}
			""";
		Set<SemanticNode> actual = parseSemanticAst(source);
		Set<SemanticNode> expected = Set.of(
			semanticNode(NodeType.COMPILATION_UNIT, 0, 52),
			semanticNode(NodeType.CLASS_DECLARATION, 7, 51, "Test"),
			semanticNode(NodeType.METHOD_DECLARATION, 21, 49),
			semanticNode(NodeType.PARAMETER_DECLARATION, 37, 42, "x"),
			semanticNode(NodeType.BLOCK, 45, 49));
		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Validates parsing of a method with a simple reference type parameter.
	 */
	@Test
	public void shouldParseSimpleReferenceTypeParameter()
	{
		String source = """
			public class Test
			{
				public void foo(String name)
				{
				}
			}
			""";
		Set<SemanticNode> actual = parseSemanticAst(source);
		Set<SemanticNode> expected = Set.of(
			semanticNode(NodeType.COMPILATION_UNIT, 0, 58),
			semanticNode(NodeType.CLASS_DECLARATION, 7, 57, "Test"),
			semanticNode(NodeType.METHOD_DECLARATION, 21, 55),
			semanticNode(NodeType.QUALIFIED_NAME, 37, 43),
			semanticNode(NodeType.PARAMETER_DECLARATION, 37, 48, "name"),
			semanticNode(NodeType.BLOCK, 51, 55));
		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Validates parsing of a method with a qualified type parameter.
	 */
	@Test
	public void shouldParseQualifiedTypeParameter()
	{
		String source = """
			public class Test
			{
				public void foo(java.util.List list)
				{
				}
			}
			""";
		Set<SemanticNode> actual = parseSemanticAst(source);
		Set<SemanticNode> expected = Set.of(
			semanticNode(NodeType.COMPILATION_UNIT, 0, 66),
			semanticNode(NodeType.CLASS_DECLARATION, 7, 65, "Test"),
			semanticNode(NodeType.METHOD_DECLARATION, 21, 63),
			semanticNode(NodeType.QUALIFIED_NAME, 37, 51),
			semanticNode(NodeType.PARAMETER_DECLARATION, 37, 56, "list"),
			semanticNode(NodeType.BLOCK, 59, 63));
		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Validates parsing of a method with multiple parameters.
	 */
	@Test
	public void shouldParseMultipleParameters()
	{
		String source = """
			public class Test
			{
				public void foo(int x, String y, double z)
				{
				}
			}
			""";
		Set<SemanticNode> actual = parseSemanticAst(source);
		Set<SemanticNode> expected = Set.of(
			semanticNode(NodeType.COMPILATION_UNIT, 0, 72),
			semanticNode(NodeType.CLASS_DECLARATION, 7, 71, "Test"),
			semanticNode(NodeType.METHOD_DECLARATION, 21, 69),
			semanticNode(NodeType.PARAMETER_DECLARATION, 37, 42, "x"),
			semanticNode(NodeType.QUALIFIED_NAME, 44, 50),
			semanticNode(NodeType.PARAMETER_DECLARATION, 44, 52, "y"),
			semanticNode(NodeType.PARAMETER_DECLARATION, 54, 62, "z"),
			semanticNode(NodeType.BLOCK, 65, 69));
		requireThat(actual, "actual").isEqualTo(expected);
	}

	// ==================== Modifier Tests ====================

	/**
	 * Validates parsing of a parameter with the {@code final} modifier.
	 */
	@Test
	public void shouldParseFinalParameter()
	{
		String source = """
			public class Test
			{
				public void foo(final String name)
				{
				}
			}
			""";
		Set<SemanticNode> actual = parseSemanticAst(source);
		Set<SemanticNode> expected = Set.of(
			semanticNode(NodeType.COMPILATION_UNIT, 0, 64),
			semanticNode(NodeType.CLASS_DECLARATION, 7, 63, "Test"),
			semanticNode(NodeType.METHOD_DECLARATION, 21, 61),
			semanticNode(NodeType.QUALIFIED_NAME, 43, 49),
			semanticNode(NodeType.PARAMETER_DECLARATION, 37, 54, "name"),
			semanticNode(NodeType.BLOCK, 57, 61));
		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Validates parsing of a final primitive parameter.
	 */
	@Test
	public void shouldParseFinalPrimitiveParameter()
	{
		String source = """
			public class Test
			{
				public void foo(final int count)
				{
				}
			}
			""";
		Set<SemanticNode> actual = parseSemanticAst(source);
		Set<SemanticNode> expected = Set.of(
			semanticNode(NodeType.COMPILATION_UNIT, 0, 62),
			semanticNode(NodeType.CLASS_DECLARATION, 7, 61, "Test"),
			semanticNode(NodeType.METHOD_DECLARATION, 21, 59),
			semanticNode(NodeType.PARAMETER_DECLARATION, 37, 52, "count"),
			semanticNode(NodeType.BLOCK, 55, 59));
		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Validates parsing of multiple final parameters.
	 */
	@Test
	public void shouldParseMultipleFinalParameters()
	{
		String source = """
			public class Test
			{
				public void foo(final int x, final String y)
				{
				}
			}
			""";
		Set<SemanticNode> actual = parseSemanticAst(source);
		Set<SemanticNode> expected = Set.of(
			semanticNode(NodeType.COMPILATION_UNIT, 0, 74),
			semanticNode(NodeType.CLASS_DECLARATION, 7, 73, "Test"),
			semanticNode(NodeType.METHOD_DECLARATION, 21, 71),
			semanticNode(NodeType.PARAMETER_DECLARATION, 37, 48, "x"),
			semanticNode(NodeType.QUALIFIED_NAME, 56, 62),
			semanticNode(NodeType.PARAMETER_DECLARATION, 50, 64, "y"),
			semanticNode(NodeType.BLOCK, 67, 71));
		requireThat(actual, "actual").isEqualTo(expected);
	}

	// ==================== Varargs Tests ====================

	/**
	 * Validates parsing of a varargs parameter.
	 */
	@Test
	public void shouldParseVarargsParameter()
	{
		String source = """
			public class Test
			{
				public void foo(String... args)
				{
				}
			}
			""";
		Set<SemanticNode> actual = parseSemanticAst(source);
		Set<SemanticNode> expected = Set.of(
			semanticNode(NodeType.COMPILATION_UNIT, 0, 61),
			semanticNode(NodeType.CLASS_DECLARATION, 7, 60, "Test"),
			semanticNode(NodeType.METHOD_DECLARATION, 21, 58),
			semanticNode(NodeType.QUALIFIED_NAME, 37, 43),
			semanticNode(NodeType.PARAMETER_DECLARATION, 37, 51, "args"),
			semanticNode(NodeType.BLOCK, 54, 58));
		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Validates parsing of varargs with preceding parameters.
	 */
	@Test
	public void shouldParseVarargsWithPrecedingParameters()
	{
		String source = """
			public class Test
			{
				public void foo(int first, String... rest)
				{
				}
			}
			""";
		Set<SemanticNode> actual = parseSemanticAst(source);
		Set<SemanticNode> expected = Set.of(
			semanticNode(NodeType.COMPILATION_UNIT, 0, 72),
			semanticNode(NodeType.CLASS_DECLARATION, 7, 71, "Test"),
			semanticNode(NodeType.METHOD_DECLARATION, 21, 69),
			semanticNode(NodeType.PARAMETER_DECLARATION, 37, 46, "first"),
			semanticNode(NodeType.QUALIFIED_NAME, 48, 54),
			semanticNode(NodeType.PARAMETER_DECLARATION, 48, 62, "rest"),
			semanticNode(NodeType.BLOCK, 65, 69));
		requireThat(actual, "actual").isEqualTo(expected);
	}

	// ==================== Annotated Parameter Tests ====================

	/**
	 * Validates parsing of an annotated parameter.
	 */
	@Test
	public void shouldParseAnnotatedParameter()
	{
		String source = """
			public class Test
			{
				public void foo(@NotNull String name)
				{
				}
			}
			""";
		Set<SemanticNode> actual = parseSemanticAst(source);
		Set<SemanticNode> expected = Set.of(
			semanticNode(NodeType.COMPILATION_UNIT, 0, 67),
			semanticNode(NodeType.CLASS_DECLARATION, 7, 66, "Test"),
			semanticNode(NodeType.METHOD_DECLARATION, 21, 64),
			semanticNode(NodeType.QUALIFIED_NAME, 38, 45),
			semanticNode(NodeType.QUALIFIED_NAME, 46, 52),
			semanticNode(NodeType.PARAMETER_DECLARATION, 37, 57, "name"),
			semanticNode(NodeType.BLOCK, 60, 64));
		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Validates parsing of a parameter with multiple annotations.
	 */
	@Test
	public void shouldParseMultipleAnnotationsOnParameter()
	{
		String source = """
			public class Test
			{
				public void foo(@NotNull @Nullable String name)
				{
				}
			}
			""";
		Set<SemanticNode> actual = parseSemanticAst(source);
		Set<SemanticNode> expected = Set.of(
			semanticNode(NodeType.COMPILATION_UNIT, 0, 77),
			semanticNode(NodeType.CLASS_DECLARATION, 7, 76, "Test"),
			semanticNode(NodeType.METHOD_DECLARATION, 21, 74),
			semanticNode(NodeType.QUALIFIED_NAME, 38, 45),
			semanticNode(NodeType.QUALIFIED_NAME, 47, 55),
			semanticNode(NodeType.QUALIFIED_NAME, 56, 62),
			semanticNode(NodeType.PARAMETER_DECLARATION, 37, 67, "name"),
			semanticNode(NodeType.BLOCK, 70, 74));
		requireThat(actual, "actual").isEqualTo(expected);
	}

	// ==================== Complex Type Tests ====================

	/**
	 * Validates parsing of a generic type parameter.
	 */
	@Test
	public void shouldParseGenericTypeParameter()
	{
		String source = """
			public class Test
			{
				public void foo(List<String> items)
				{
				}
			}
			""";
		Set<SemanticNode> actual = parseSemanticAst(source);
		Set<SemanticNode> expected = Set.of(
			semanticNode(NodeType.COMPILATION_UNIT, 0, 65),
			semanticNode(NodeType.CLASS_DECLARATION, 7, 64, "Test"),
			semanticNode(NodeType.METHOD_DECLARATION, 21, 62),
			semanticNode(NodeType.PARAMETERIZED_TYPE, 37, 49),
			semanticNode(NodeType.QUALIFIED_NAME, 37, 41),
			semanticNode(NodeType.QUALIFIED_NAME, 42, 48),
			semanticNode(NodeType.PARAMETER_DECLARATION, 37, 55, "items"),
			semanticNode(NodeType.BLOCK, 58, 62));
		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Validates parsing of a nested generic type parameter.
	 */
	@Test
	public void shouldParseNestedGenericTypeParameter()
	{
		String source = """
			public class Test
			{
				public void foo(Map<String, List<Integer>> data)
				{
				}
			}
			""";
		Set<SemanticNode> actual = parseSemanticAst(source);
		Set<SemanticNode> expected = Set.of(
			semanticNode(NodeType.COMPILATION_UNIT, 0, 78),
			semanticNode(NodeType.CLASS_DECLARATION, 7, 77, "Test"),
			semanticNode(NodeType.METHOD_DECLARATION, 21, 75),
			semanticNode(NodeType.PARAMETERIZED_TYPE, 37, 63),
			semanticNode(NodeType.PARAMETERIZED_TYPE, 49, 63),
			semanticNode(NodeType.QUALIFIED_NAME, 37, 40),
			semanticNode(NodeType.QUALIFIED_NAME, 41, 47),
			semanticNode(NodeType.QUALIFIED_NAME, 49, 53),
			semanticNode(NodeType.QUALIFIED_NAME, 49, 63),
			semanticNode(NodeType.QUALIFIED_NAME, 54, 61),
			semanticNode(NodeType.PARAMETER_DECLARATION, 37, 68, "data"),
			semanticNode(NodeType.BLOCK, 71, 75));
		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Validates parsing of an array type parameter.
	 */
	@Test
	public void shouldParseArrayTypeParameter()
	{
		String source = """
			public class Test
			{
				public void foo(String[] names)
				{
				}
			}
			""";
		Set<SemanticNode> actual = parseSemanticAst(source);
		Set<SemanticNode> expected = Set.of(
			semanticNode(NodeType.COMPILATION_UNIT, 0, 61),
			semanticNode(NodeType.CLASS_DECLARATION, 7, 60, "Test"),
			semanticNode(NodeType.METHOD_DECLARATION, 21, 58),
			semanticNode(NodeType.QUALIFIED_NAME, 37, 43),
			semanticNode(NodeType.PARAMETER_DECLARATION, 37, 51, "names"),
			semanticNode(NodeType.BLOCK, 54, 58));
		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Validates parsing of a multi-dimensional array type parameter.
	 */
	@Test
	public void shouldParseMultiDimensionalArrayParameter()
	{
		String source = """
			public class Test
			{
				public void foo(int[][] matrix)
				{
				}
			}
			""";
		Set<SemanticNode> actual = parseSemanticAst(source);
		Set<SemanticNode> expected = Set.of(
			semanticNode(NodeType.COMPILATION_UNIT, 0, 61),
			semanticNode(NodeType.CLASS_DECLARATION, 7, 60, "Test"),
			semanticNode(NodeType.METHOD_DECLARATION, 21, 58),
			semanticNode(NodeType.PARAMETER_DECLARATION, 37, 51, "matrix"),
			semanticNode(NodeType.BLOCK, 54, 58));
		requireThat(actual, "actual").isEqualTo(expected);
	}

	// ==================== Context Tests ====================

	/**
	 * Validates parsing of constructor parameters.
	 */
	@Test
	public void shouldParseConstructorParameters()
	{
		String source = """
			public class Test
			{
				public Test(String name, int value)
				{
				}
			}
			""";
		Set<SemanticNode> actual = parseSemanticAst(source);
		Set<SemanticNode> expected = Set.of(
			semanticNode(NodeType.COMPILATION_UNIT, 0, 65),
			semanticNode(NodeType.CLASS_DECLARATION, 7, 64, "Test"),
			semanticNode(NodeType.CONSTRUCTOR_DECLARATION, 21, 62),
			semanticNode(NodeType.QUALIFIED_NAME, 33, 39),
			semanticNode(NodeType.PARAMETER_DECLARATION, 33, 44, "name"),
			semanticNode(NodeType.PARAMETER_DECLARATION, 46, 55, "value"),
			semanticNode(NodeType.BLOCK, 58, 62));
		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Validates parsing of record components as parameters.
	 */
	@Test
	public void shouldParseRecordComponentsAsParameters()
	{
		String source = """
			public record Point(int x, int y)
			{
			}
			""";
		Set<SemanticNode> actual = parseSemanticAst(source);
		Set<SemanticNode> expected = Set.of(
			semanticNode(NodeType.COMPILATION_UNIT, 0, 38),
			semanticNode(NodeType.RECORD_DECLARATION, 7, 37, "Point"),
			semanticNode(NodeType.PARAMETER_DECLARATION, 20, 25, "x"),
			semanticNode(NodeType.PARAMETER_DECLARATION, 27, 32, "y"));
		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Validates parsing of catch clause parameter.
	 */
	@Test
	public void shouldParseCatchClauseParameter()
	{
		String source = """
			public class Test
			{
				public void foo()
				{
					try
					{
					}
					catch (Exception e)
					{
					}
				}
			}
			""";
		Set<SemanticNode> actual = parseSemanticAst(source);
		Set<SemanticNode> expected = Set.of(
			semanticNode(NodeType.COMPILATION_UNIT, 0, 91),
			semanticNode(NodeType.CLASS_DECLARATION, 7, 90, "Test"),
			semanticNode(NodeType.METHOD_DECLARATION, 21, 88),
			semanticNode(NodeType.BLOCK, 40, 88),
			semanticNode(NodeType.TRY_STATEMENT, 44, 85),
			semanticNode(NodeType.BLOCK, 50, 55),
			semanticNode(NodeType.CATCH_CLAUSE, 58, 85),
			semanticNode(NodeType.QUALIFIED_NAME, 65, 74),
			semanticNode(NodeType.PARAMETER_DECLARATION, 65, 76, "e"),
			semanticNode(NodeType.BLOCK, 80, 85));
		requireThat(actual, "actual").isEqualTo(expected);
	}

	// ==================== Edge Case Tests ====================

	/**
	 * Validates parsing of receiver parameter.
	 */
	@Test
	public void shouldParseReceiverParameter()
	{
		String source = """
			public class Test
			{
				public void method(Test this)
				{
				}
			}
			""";
		Set<SemanticNode> actual = parseSemanticAst(source);
		Set<SemanticNode> expected = Set.of(
			semanticNode(NodeType.COMPILATION_UNIT, 0, 59),
			semanticNode(NodeType.CLASS_DECLARATION, 7, 58, "Test"),
			semanticNode(NodeType.METHOD_DECLARATION, 21, 56),
			semanticNode(NodeType.QUALIFIED_NAME, 40, 44),
			semanticNode(NodeType.PARAMETER_DECLARATION, 40, 49, "this"),
			semanticNode(NodeType.BLOCK, 52, 56));
		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Validates parsing of C-style array parameter syntax.
	 */
	@Test
	public void shouldParseCStyleArrayParameter()
	{
		String source = """
			public class Test
			{
				public void foo(String args[])
				{
				}
			}
			""";
		Set<SemanticNode> actual = parseSemanticAst(source);
		Set<SemanticNode> expected = Set.of(
			semanticNode(NodeType.COMPILATION_UNIT, 0, 60),
			semanticNode(NodeType.CLASS_DECLARATION, 7, 59, "Test"),
			semanticNode(NodeType.METHOD_DECLARATION, 21, 57),
			semanticNode(NodeType.QUALIFIED_NAME, 37, 43),
			semanticNode(NodeType.PARAMETER_DECLARATION, 37, 50, "args"),
			semanticNode(NodeType.BLOCK, 53, 57));
		requireThat(actual, "actual").isEqualTo(expected);
	}
}
