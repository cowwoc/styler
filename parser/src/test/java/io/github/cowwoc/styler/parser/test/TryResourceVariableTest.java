package io.github.cowwoc.styler.parser.test;

import io.github.cowwoc.styler.parser.test.ParserTestUtils.SemanticNode;
import org.testng.annotations.Test;

import java.util.Set;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;
import static io.github.cowwoc.styler.parser.test.ParserTestUtils.parseSemanticAst;
import static io.github.cowwoc.styler.ast.core.NodeType.CLASS_DECLARATION;
import static io.github.cowwoc.styler.parser.test.ParserTestUtils.*;
import static io.github.cowwoc.styler.parser.test.ParserTestUtils.typeDeclaration;
import static io.github.cowwoc.styler.parser.test.ParserTestUtils.parameterNode;

/**
 * Tests for parsing try-with-resources variable references (JDK 9+).
 */
public class TryResourceVariableTest
{
	/**
	 * Validates parsing of single variable reference in try-with-resources.
	 * JDK 9+ allows using effectively-final variable references directly.
	 */
	@Test
	public void shouldParseSingleVariableReference()
	{
		String source = """
			public class Test
			{
				public void foo(AutoCloseable resource)
				{
					try (resource)
					{
						doWork();
					}
				}
			}
			""";

		Set<SemanticNode> actual = parseSemanticAst(source);

		Set<SemanticNode> expected = Set.of(
			compilationUnit( 0, 107),
			typeDeclaration(CLASS_DECLARATION, 7, 106, "Test"),
			methodDeclaration( 21, 104),
			parameterNode( 37, 59, "resource"),
			qualifiedName( 37, 50),
			block( 62, 104),
			tryStatement( 66, 101),
			identifier( 71, 79),
			block( 83, 101),
			methodInvocation( 88, 96),
			qualifiedName( 88, 94),
			identifier( 88, 94));
		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Validates parsing of multiple variable references separated by semicolons.
	 * Each resource is an effectively-final variable reference.
	 */
	@Test
	public void shouldParseMultipleVariableReferences()
	{
		String source = """
			public class Test
			{
				public void foo(AutoCloseable stream1, AutoCloseable stream2)
				{
					try (stream1; stream2)
					{
						doWork();
					}
				}
			}
			""";

		Set<SemanticNode> actual = parseSemanticAst(source);

		Set<SemanticNode> expected = Set.of(
			compilationUnit( 0, 137),
			typeDeclaration(CLASS_DECLARATION, 7, 136, "Test"),
			methodDeclaration( 21, 134),
			parameterNode( 37, 58, "stream1"),
			qualifiedName( 37, 50),
			parameterNode( 60, 81, "stream2"),
			qualifiedName( 60, 73),
			block( 84, 134),
			tryStatement( 88, 131),
			identifier( 93, 100),
			identifier( 102, 109),
			block( 113, 131),
			methodInvocation( 118, 126),
			qualifiedName( 118, 124),
			identifier( 118, 124));
		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Validates parsing of full declaration followed by variable reference.
	 * Demonstrates mixing traditional resource declarations with JDK 9+ references.
	 */
	@Test
	public void shouldParseMixedDeclarationAndReference()
	{
		String source = """
			public class Test
			{
				public void foo(java.io.InputStream existing)
				{
					try (java.io.BufferedReader br = new java.io.BufferedReader(null); existing)
					{
						br.readLine();
					}
				}
			}
			""";

		Set<SemanticNode> actual = parseSemanticAst(source);

		Set<SemanticNode> expected = Set.of(
			compilationUnit( 0, 180),
			typeDeclaration(CLASS_DECLARATION, 7, 179, "Test"),
			methodDeclaration( 21, 177),
			parameterNode( 37, 65, "existing"),
			qualifiedName( 37, 56),
			block( 68, 177),
			tryStatement( 72, 174),
			qualifiedName( 77, 99),
			objectCreation( 105, 137),
			qualifiedName( 109, 131),
			nullLiteral( 132, 136),
			identifier( 139, 147),
			block( 151, 174),
			methodInvocation( 156, 169),
			fieldAccess( 156, 167),
			qualifiedName( 156, 167),
			identifier( 156, 158));
		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Validates parsing of variable reference followed by full declaration.
	 * Order of resources should be preserved in AST structure.
	 */
	@Test
	public void shouldParseReferenceFollowedByDeclaration()
	{
		String source = """
			public class Test
			{
				public void foo(java.io.InputStream existing)
				{
					try (existing; java.io.BufferedReader br = new java.io.BufferedReader(null))
					{
						br.readLine();
					}
				}
			}
			""";

		Set<SemanticNode> actual = parseSemanticAst(source);

		Set<SemanticNode> expected = Set.of(
			compilationUnit( 0, 180),
			typeDeclaration(CLASS_DECLARATION, 7, 179, "Test"),
			methodDeclaration( 21, 177),
			parameterNode( 37, 65, "existing"),
			qualifiedName( 37, 56),
			block( 68, 177),
			tryStatement( 72, 174),
			identifier( 77, 85),
			qualifiedName( 87, 109),
			objectCreation( 115, 147),
			qualifiedName( 119, 141),
			nullLiteral( 142, 146),
			block( 151, 174),
			methodInvocation( 156, 169),
			fieldAccess( 156, 167),
			qualifiedName( 156, 167),
			identifier( 156, 158));
		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Validates variable reference works with catch clause.
	 * Ensures complete try-catch structure is parsed correctly.
	 */
	@Test
	public void shouldParseVariableReferenceWithCatchClause()
	{
		String source = """
			public class Test
			{
				public void foo(AutoCloseable resource)
				{
					try (resource)
					{
						doWork();
					}
					catch (Exception e)
					{
						handleError();
					}
				}
			}
			""";

		Set<SemanticNode> actual = parseSemanticAst(source);

		Set<SemanticNode> expected = Set.of(
			compilationUnit( 0, 155),
			typeDeclaration(CLASS_DECLARATION, 7, 154, "Test"),
			methodDeclaration( 21, 152),
			parameterNode( 37, 59, "resource"),
			qualifiedName( 37, 50),
			block( 62, 152),
			tryStatement( 66, 149),
			identifier( 71, 79),
			block( 83, 101),
			methodInvocation( 88, 96),
			qualifiedName( 88, 94),
			identifier( 88, 94),
			catchClause( 104, 149),
			parameterNode( 111, 122, "e"),
			qualifiedName( 111, 120),
			block( 126, 149),
			methodInvocation( 131, 144),
			qualifiedName( 131, 142),
			identifier( 131, 142));
		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Validates variable reference works with finally clause.
	 * Ensures complete try-finally structure is parsed correctly.
	 */
	@Test
	public void shouldParseVariableReferenceWithFinallyClause()
	{
		String source = """
			public class Test
			{
				public void foo(AutoCloseable resource)
				{
					try (resource)
					{
						doWork();
					}
					finally
					{
						cleanup();
					}
				}
			}
			""";

		Set<SemanticNode> actual = parseSemanticAst(source);

		Set<SemanticNode> expected = Set.of(
			compilationUnit( 0, 139),
			typeDeclaration(CLASS_DECLARATION, 7, 138, "Test"),
			methodDeclaration( 21, 136),
			parameterNode( 37, 59, "resource"),
			qualifiedName( 37, 50),
			block( 62, 136),
			tryStatement( 66, 133),
			identifier( 71, 79),
			block( 83, 101),
			methodInvocation( 88, 96),
			qualifiedName( 88, 94),
			identifier( 88, 94),
			finallyClause( 104, 133),
			block( 114, 133),
			methodInvocation( 119, 128),
			qualifiedName( 119, 126),
			identifier( 119, 126));
		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Validates trailing semicolon before RPAREN is handled correctly.
	 * The trailing semicolon should not cause parsing issues.
	 */
	@Test
	public void shouldParseMultipleVariableReferencesWithTrailingSemicolon()
	{
		String source = """
			public class Test
			{
				public void foo(AutoCloseable r1, AutoCloseable r2)
				{
					try (r1; r2;)
					{
						doWork();
					}
				}
			}
			""";

		Set<SemanticNode> actual = parseSemanticAst(source);

		Set<SemanticNode> expected = Set.of(
			compilationUnit( 0, 118),
			typeDeclaration(CLASS_DECLARATION, 7, 117, "Test"),
			methodDeclaration( 21, 115),
			parameterNode( 37, 53, "r1"),
			qualifiedName( 37, 50),
			parameterNode( 55, 71, "r2"),
			qualifiedName( 55, 68),
			block( 74, 115),
			tryStatement( 78, 112),
			identifier( 83, 85),
			identifier( 87, 89),
			block( 94, 112),
			methodInvocation( 99, 107),
			qualifiedName( 99, 105),
			identifier( 99, 105));
		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Validates parsing of three variable references.
	 * Ensures the parser handles more than two resources correctly.
	 */
	@Test
	public void shouldParseThreeVariableReferences()
	{
		String source = """
			public class Test
			{
				public void foo(AutoCloseable a, AutoCloseable b, AutoCloseable c)
				{
					try (a; b; c)
					{
						doWork();
					}
				}
			}
			""";

		Set<SemanticNode> actual = parseSemanticAst(source);

		Set<SemanticNode> expected = Set.of(
			compilationUnit( 0, 133),
			typeDeclaration(CLASS_DECLARATION, 7, 132, "Test"),
			methodDeclaration( 21, 130),
			parameterNode( 37, 52, "a"),
			qualifiedName( 37, 50),
			parameterNode( 54, 69, "b"),
			qualifiedName( 54, 67),
			parameterNode( 71, 86, "c"),
			qualifiedName( 71, 84),
			block( 89, 130),
			tryStatement( 93, 127),
			identifier( 98, 99),
			identifier( 101, 102),
			identifier( 104, 105),
			block( 109, 127),
			methodInvocation( 114, 122),
			qualifiedName( 114, 120),
			identifier( 114, 120));
		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Validates complex mixed scenario with multiple declarations and references.
	 * Tests interleaved full declarations and variable references.
	 */
	@Test
	public void shouldParseMixedWithMultipleDeclarations()
	{
		String source = """
			public class Test
			{
				public void foo(AutoCloseable existing)
				{
					try (java.io.Reader r1 = null; existing; java.io.Writer w1 = null)
					{
						doWork();
					}
				}
			}
			""";

		Set<SemanticNode> actual = parseSemanticAst(source);

		Set<SemanticNode> expected = Set.of(
			compilationUnit( 0, 159),
			typeDeclaration(CLASS_DECLARATION, 7, 158, "Test"),
			methodDeclaration( 21, 156),
			parameterNode( 37, 59, "existing"),
			qualifiedName( 37, 50),
			block( 62, 156),
			tryStatement( 66, 153),
			qualifiedName( 71, 85),
			nullLiteral( 91, 95),
			identifier( 97, 105),
			qualifiedName( 107, 121),
			nullLiteral( 127, 131),
			block( 135, 153),
			methodInvocation( 140, 148),
			qualifiedName( 140, 146),
			identifier( 140, 146));
		requireThat(actual, "actual").isEqualTo(expected);
	}
}
