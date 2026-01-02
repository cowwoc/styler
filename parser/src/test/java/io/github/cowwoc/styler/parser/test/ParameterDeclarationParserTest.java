package io.github.cowwoc.styler.parser.test;

import io.github.cowwoc.styler.ast.core.NodeType;
import io.github.cowwoc.styler.parser.test.ParserTestUtils.SemanticNode;
import org.testng.annotations.Test;

import java.util.Set;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;
import static io.github.cowwoc.styler.parser.test.ParserTestUtils.parseSemanticAst;
import static io.github.cowwoc.styler.parser.test.ParserTestUtils.*;
import static io.github.cowwoc.styler.parser.test.ParserTestUtils.typeDeclaration;
import static io.github.cowwoc.styler.parser.test.ParserTestUtils.parameterNode;

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
			compilationUnit( 0, 52),
			typeDeclaration(NodeType.CLASS_DECLARATION, 7, 51, "Test"),
			methodDeclaration( 21, 49),
			parameterNode(37, 42, "x"),
			block( 45, 49));
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
			compilationUnit( 0, 58),
			typeDeclaration(NodeType.CLASS_DECLARATION, 7, 57, "Test"),
			methodDeclaration( 21, 55),
			qualifiedName( 37, 43),
			parameterNode(37, 48, "name"),
			block( 51, 55));
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
			compilationUnit( 0, 66),
			typeDeclaration(NodeType.CLASS_DECLARATION, 7, 65, "Test"),
			methodDeclaration( 21, 63),
			qualifiedName( 37, 51),
			parameterNode(37, 56, "list"),
			block( 59, 63));
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
			compilationUnit( 0, 72),
			typeDeclaration(NodeType.CLASS_DECLARATION, 7, 71, "Test"),
			methodDeclaration( 21, 69),
			parameterNode(37, 42, "x"),
			qualifiedName( 44, 50),
			parameterNode(44, 52, "y"),
			parameterNode(54, 62, "z"),
			block( 65, 69));
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
			compilationUnit( 0, 64),
			typeDeclaration(NodeType.CLASS_DECLARATION, 7, 63, "Test"),
			methodDeclaration( 21, 61),
			qualifiedName( 43, 49),
			parameterNode(37, 54, "name"),
			block( 57, 61));
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
			compilationUnit( 0, 62),
			typeDeclaration(NodeType.CLASS_DECLARATION, 7, 61, "Test"),
			methodDeclaration( 21, 59),
			parameterNode(37, 52, "count"),
			block( 55, 59));
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
			compilationUnit( 0, 74),
			typeDeclaration(NodeType.CLASS_DECLARATION, 7, 73, "Test"),
			methodDeclaration( 21, 71),
			parameterNode(37, 48, "x"),
			qualifiedName( 56, 62),
			parameterNode(50, 64, "y"),
			block( 67, 71));
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
			compilationUnit( 0, 61),
			typeDeclaration(NodeType.CLASS_DECLARATION, 7, 60, "Test"),
			methodDeclaration( 21, 58),
			qualifiedName( 37, 43),
			parameterNode(37, 51, "args"),
			block( 54, 58));
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
			compilationUnit( 0, 72),
			typeDeclaration(NodeType.CLASS_DECLARATION, 7, 71, "Test"),
			methodDeclaration( 21, 69),
			parameterNode(37, 46, "first"),
			qualifiedName( 48, 54),
			parameterNode(48, 62, "rest"),
			block( 65, 69));
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
			compilationUnit( 0, 67),
			typeDeclaration(NodeType.CLASS_DECLARATION, 7, 66, "Test"),
			methodDeclaration( 21, 64),
			annotation( 37, 45),
			qualifiedName( 38, 45),
			qualifiedName( 46, 52),
			parameterNode(37, 57, "name"),
			block( 60, 64));
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
			compilationUnit( 0, 77),
			typeDeclaration(NodeType.CLASS_DECLARATION, 7, 76, "Test"),
			methodDeclaration( 21, 74),
			annotation( 37, 45),
			qualifiedName( 38, 45),
			annotation( 46, 55),
			qualifiedName( 47, 55),
			qualifiedName( 56, 62),
			parameterNode(37, 67, "name"),
			block( 70, 74));
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
			compilationUnit( 0, 65),
			typeDeclaration(NodeType.CLASS_DECLARATION, 7, 64, "Test"),
			methodDeclaration( 21, 62),
			parameterizedType( 37, 49),
			qualifiedName( 37, 41),
			qualifiedName( 42, 48),
			parameterNode(37, 55, "items"),
			block( 58, 62));
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
			compilationUnit( 0, 78),
			typeDeclaration(NodeType.CLASS_DECLARATION, 7, 77, "Test"),
			methodDeclaration( 21, 75),
			parameterizedType( 37, 63),
			parameterizedType( 49, 63),
			qualifiedName( 37, 40),
			qualifiedName( 41, 47),
			qualifiedName( 49, 53),
			qualifiedName( 49, 63),
			qualifiedName( 54, 61),
			parameterNode(37, 68, "data"),
			block( 71, 75));
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
			compilationUnit( 0, 61),
			typeDeclaration(NodeType.CLASS_DECLARATION, 7, 60, "Test"),
			methodDeclaration( 21, 58),
			qualifiedName( 37, 43),
			parameterNode(37, 51, "names"),
			block( 54, 58));
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
			compilationUnit( 0, 61),
			typeDeclaration(NodeType.CLASS_DECLARATION, 7, 60, "Test"),
			methodDeclaration( 21, 58),
			parameterNode(37, 51, "matrix"),
			block( 54, 58));
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
			compilationUnit( 0, 65),
			typeDeclaration(NodeType.CLASS_DECLARATION, 7, 64, "Test"),
			constructorDeclaration( 21, 62),
			qualifiedName( 33, 39),
			parameterNode(33, 44, "name"),
			parameterNode(46, 55, "value"),
			block( 58, 62));
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
			compilationUnit( 0, 38),
			typeDeclaration(NodeType.RECORD_DECLARATION, 7, 37, "Point"),
			parameterNode(20, 25, "x"),
			parameterNode(27, 32, "y"));
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
			compilationUnit( 0, 91),
			typeDeclaration(NodeType.CLASS_DECLARATION, 7, 90, "Test"),
			methodDeclaration( 21, 88),
			block( 40, 88),
			tryStatement( 44, 85),
			block( 50, 55),
			catchClause( 58, 85),
			qualifiedName( 65, 74),
			parameterNode(65, 76, "e"),
			block( 80, 85));
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
			compilationUnit( 0, 59),
			typeDeclaration(NodeType.CLASS_DECLARATION, 7, 58, "Test"),
			methodDeclaration( 21, 56),
			qualifiedName( 40, 44),
			parameterNode(40, 49, "this"),
			block( 52, 56));
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
			compilationUnit( 0, 60),
			typeDeclaration(NodeType.CLASS_DECLARATION, 7, 59, "Test"),
			methodDeclaration( 21, 57),
			qualifiedName( 37, 43),
			parameterNode(37, 50, "args"),
			block( 53, 57));
		requireThat(actual, "actual").isEqualTo(expected);
	}
}
