package io.github.cowwoc.styler.parser.test;

import io.github.cowwoc.styler.parser.test.ParserTestUtils.SemanticNode;
import org.testng.annotations.Test;

import java.util.Set;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;
import static io.github.cowwoc.styler.ast.core.NodeType.BLOCK;
import static io.github.cowwoc.styler.ast.core.NodeType.CATCH_CLAUSE;
import static io.github.cowwoc.styler.ast.core.NodeType.CLASS_DECLARATION;
import static io.github.cowwoc.styler.ast.core.NodeType.COMPILATION_UNIT;
import static io.github.cowwoc.styler.ast.core.NodeType.FIELD_ACCESS;
import static io.github.cowwoc.styler.ast.core.NodeType.FINALLY_CLAUSE;
import static io.github.cowwoc.styler.ast.core.NodeType.IDENTIFIER;
import static io.github.cowwoc.styler.ast.core.NodeType.METHOD_DECLARATION;
import static io.github.cowwoc.styler.ast.core.NodeType.METHOD_INVOCATION;
import static io.github.cowwoc.styler.ast.core.NodeType.NULL_LITERAL;
import static io.github.cowwoc.styler.ast.core.NodeType.OBJECT_CREATION;
import static io.github.cowwoc.styler.ast.core.NodeType.PARAMETER_DECLARATION;
import static io.github.cowwoc.styler.ast.core.NodeType.QUALIFIED_NAME;
import static io.github.cowwoc.styler.ast.core.NodeType.TRY_STATEMENT;
import static io.github.cowwoc.styler.parser.test.ParserTestUtils.parseSemanticAst;
import static io.github.cowwoc.styler.parser.test.ParserTestUtils.semanticNode;

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
			semanticNode(COMPILATION_UNIT, 0, 107),
			semanticNode(CLASS_DECLARATION, 7, 106, "Test"),
			semanticNode(METHOD_DECLARATION, 21, 104),
			semanticNode(PARAMETER_DECLARATION, 37, 59, "resource"),
			semanticNode(QUALIFIED_NAME, 37, 50),
			semanticNode(BLOCK, 62, 104),
			semanticNode(TRY_STATEMENT, 66, 101),
			semanticNode(IDENTIFIER, 71, 79),
			semanticNode(BLOCK, 83, 101),
			semanticNode(METHOD_INVOCATION, 88, 96),
			semanticNode(QUALIFIED_NAME, 88, 94),
			semanticNode(IDENTIFIER, 88, 94));
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
			semanticNode(COMPILATION_UNIT, 0, 137),
			semanticNode(CLASS_DECLARATION, 7, 136, "Test"),
			semanticNode(METHOD_DECLARATION, 21, 134),
			semanticNode(PARAMETER_DECLARATION, 37, 58, "stream1"),
			semanticNode(QUALIFIED_NAME, 37, 50),
			semanticNode(PARAMETER_DECLARATION, 60, 81, "stream2"),
			semanticNode(QUALIFIED_NAME, 60, 73),
			semanticNode(BLOCK, 84, 134),
			semanticNode(TRY_STATEMENT, 88, 131),
			semanticNode(IDENTIFIER, 93, 100),
			semanticNode(IDENTIFIER, 102, 109),
			semanticNode(BLOCK, 113, 131),
			semanticNode(METHOD_INVOCATION, 118, 126),
			semanticNode(QUALIFIED_NAME, 118, 124),
			semanticNode(IDENTIFIER, 118, 124));
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
			semanticNode(COMPILATION_UNIT, 0, 180),
			semanticNode(CLASS_DECLARATION, 7, 179, "Test"),
			semanticNode(METHOD_DECLARATION, 21, 177),
			semanticNode(PARAMETER_DECLARATION, 37, 65, "existing"),
			semanticNode(QUALIFIED_NAME, 37, 56),
			semanticNode(BLOCK, 68, 177),
			semanticNode(TRY_STATEMENT, 72, 174),
			semanticNode(QUALIFIED_NAME, 77, 99),
			semanticNode(OBJECT_CREATION, 105, 137),
			semanticNode(QUALIFIED_NAME, 109, 131),
			semanticNode(NULL_LITERAL, 132, 136),
			semanticNode(IDENTIFIER, 139, 147),
			semanticNode(BLOCK, 151, 174),
			semanticNode(METHOD_INVOCATION, 156, 169),
			semanticNode(FIELD_ACCESS, 156, 167),
			semanticNode(QUALIFIED_NAME, 156, 167),
			semanticNode(IDENTIFIER, 156, 158));
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
			semanticNode(COMPILATION_UNIT, 0, 180),
			semanticNode(CLASS_DECLARATION, 7, 179, "Test"),
			semanticNode(METHOD_DECLARATION, 21, 177),
			semanticNode(PARAMETER_DECLARATION, 37, 65, "existing"),
			semanticNode(QUALIFIED_NAME, 37, 56),
			semanticNode(BLOCK, 68, 177),
			semanticNode(TRY_STATEMENT, 72, 174),
			semanticNode(IDENTIFIER, 77, 85),
			semanticNode(QUALIFIED_NAME, 87, 109),
			semanticNode(OBJECT_CREATION, 115, 147),
			semanticNode(QUALIFIED_NAME, 119, 141),
			semanticNode(NULL_LITERAL, 142, 146),
			semanticNode(BLOCK, 151, 174),
			semanticNode(METHOD_INVOCATION, 156, 169),
			semanticNode(FIELD_ACCESS, 156, 167),
			semanticNode(QUALIFIED_NAME, 156, 167),
			semanticNode(IDENTIFIER, 156, 158));
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
			semanticNode(COMPILATION_UNIT, 0, 155),
			semanticNode(CLASS_DECLARATION, 7, 154, "Test"),
			semanticNode(METHOD_DECLARATION, 21, 152),
			semanticNode(PARAMETER_DECLARATION, 37, 59, "resource"),
			semanticNode(QUALIFIED_NAME, 37, 50),
			semanticNode(BLOCK, 62, 152),
			semanticNode(TRY_STATEMENT, 66, 149),
			semanticNode(IDENTIFIER, 71, 79),
			semanticNode(BLOCK, 83, 101),
			semanticNode(METHOD_INVOCATION, 88, 96),
			semanticNode(QUALIFIED_NAME, 88, 94),
			semanticNode(IDENTIFIER, 88, 94),
			semanticNode(CATCH_CLAUSE, 104, 149),
			semanticNode(PARAMETER_DECLARATION, 111, 122, "e"),
			semanticNode(QUALIFIED_NAME, 111, 120),
			semanticNode(BLOCK, 126, 149),
			semanticNode(METHOD_INVOCATION, 131, 144),
			semanticNode(QUALIFIED_NAME, 131, 142),
			semanticNode(IDENTIFIER, 131, 142));
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
			semanticNode(COMPILATION_UNIT, 0, 139),
			semanticNode(CLASS_DECLARATION, 7, 138, "Test"),
			semanticNode(METHOD_DECLARATION, 21, 136),
			semanticNode(PARAMETER_DECLARATION, 37, 59, "resource"),
			semanticNode(QUALIFIED_NAME, 37, 50),
			semanticNode(BLOCK, 62, 136),
			semanticNode(TRY_STATEMENT, 66, 133),
			semanticNode(IDENTIFIER, 71, 79),
			semanticNode(BLOCK, 83, 101),
			semanticNode(METHOD_INVOCATION, 88, 96),
			semanticNode(QUALIFIED_NAME, 88, 94),
			semanticNode(IDENTIFIER, 88, 94),
			semanticNode(FINALLY_CLAUSE, 104, 133),
			semanticNode(BLOCK, 114, 133),
			semanticNode(METHOD_INVOCATION, 119, 128),
			semanticNode(QUALIFIED_NAME, 119, 126),
			semanticNode(IDENTIFIER, 119, 126));
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
			semanticNode(COMPILATION_UNIT, 0, 118),
			semanticNode(CLASS_DECLARATION, 7, 117, "Test"),
			semanticNode(METHOD_DECLARATION, 21, 115),
			semanticNode(PARAMETER_DECLARATION, 37, 53, "r1"),
			semanticNode(QUALIFIED_NAME, 37, 50),
			semanticNode(PARAMETER_DECLARATION, 55, 71, "r2"),
			semanticNode(QUALIFIED_NAME, 55, 68),
			semanticNode(BLOCK, 74, 115),
			semanticNode(TRY_STATEMENT, 78, 112),
			semanticNode(IDENTIFIER, 83, 85),
			semanticNode(IDENTIFIER, 87, 89),
			semanticNode(BLOCK, 94, 112),
			semanticNode(METHOD_INVOCATION, 99, 107),
			semanticNode(QUALIFIED_NAME, 99, 105),
			semanticNode(IDENTIFIER, 99, 105));
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
			semanticNode(COMPILATION_UNIT, 0, 133),
			semanticNode(CLASS_DECLARATION, 7, 132, "Test"),
			semanticNode(METHOD_DECLARATION, 21, 130),
			semanticNode(PARAMETER_DECLARATION, 37, 52, "a"),
			semanticNode(QUALIFIED_NAME, 37, 50),
			semanticNode(PARAMETER_DECLARATION, 54, 69, "b"),
			semanticNode(QUALIFIED_NAME, 54, 67),
			semanticNode(PARAMETER_DECLARATION, 71, 86, "c"),
			semanticNode(QUALIFIED_NAME, 71, 84),
			semanticNode(BLOCK, 89, 130),
			semanticNode(TRY_STATEMENT, 93, 127),
			semanticNode(IDENTIFIER, 98, 99),
			semanticNode(IDENTIFIER, 101, 102),
			semanticNode(IDENTIFIER, 104, 105),
			semanticNode(BLOCK, 109, 127),
			semanticNode(METHOD_INVOCATION, 114, 122),
			semanticNode(QUALIFIED_NAME, 114, 120),
			semanticNode(IDENTIFIER, 114, 120));
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
			semanticNode(COMPILATION_UNIT, 0, 159),
			semanticNode(CLASS_DECLARATION, 7, 158, "Test"),
			semanticNode(METHOD_DECLARATION, 21, 156),
			semanticNode(PARAMETER_DECLARATION, 37, 59, "existing"),
			semanticNode(QUALIFIED_NAME, 37, 50),
			semanticNode(BLOCK, 62, 156),
			semanticNode(TRY_STATEMENT, 66, 153),
			semanticNode(QUALIFIED_NAME, 71, 85),
			semanticNode(NULL_LITERAL, 91, 95),
			semanticNode(IDENTIFIER, 97, 105),
			semanticNode(QUALIFIED_NAME, 107, 121),
			semanticNode(NULL_LITERAL, 127, 131),
			semanticNode(BLOCK, 135, 153),
			semanticNode(METHOD_INVOCATION, 140, 148),
			semanticNode(QUALIFIED_NAME, 140, 146),
			semanticNode(IDENTIFIER, 140, 146));
		requireThat(actual, "actual").isEqualTo(expected);
	}
}
