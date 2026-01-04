package io.github.cowwoc.styler.parser.test;

import io.github.cowwoc.styler.ast.core.NodeArena;
import io.github.cowwoc.styler.ast.core.NodeType;
import io.github.cowwoc.styler.ast.core.ParameterAttribute;
import io.github.cowwoc.styler.ast.core.TypeDeclarationAttribute;
import org.testng.annotations.Test;
import io.github.cowwoc.styler.parser.Parser;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;
import static io.github.cowwoc.styler.parser.test.ParserTestUtils.parse;

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
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 37, 50);
			expected.allocateParameterDeclaration(37, 59, new ParameterAttribute("resource", false, false, false));
			expected.allocateNode(NodeType.IDENTIFIER, 71, 79);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 88, 94);
			expected.allocateNode(NodeType.IDENTIFIER, 88, 94);
			expected.allocateNode(NodeType.METHOD_INVOCATION, 88, 96);
			expected.allocateNode(NodeType.BLOCK, 83, 101);
			expected.allocateNode(NodeType.TRY_STATEMENT, 66, 101);
			expected.allocateNode(NodeType.BLOCK, 62, 104);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 21, 104);
			expected.allocateClassDeclaration(7, 106, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 107);
			requireThat(actual, "actual").isEqualTo(expected);
		}
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
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 37, 50);
			expected.allocateParameterDeclaration(37, 58, new ParameterAttribute("stream1", false, false, false));
			expected.allocateNode(NodeType.QUALIFIED_NAME, 60, 73);
			expected.allocateParameterDeclaration(60, 81, new ParameterAttribute("stream2", false, false, false));
			expected.allocateNode(NodeType.IDENTIFIER, 93, 100);
			expected.allocateNode(NodeType.IDENTIFIER, 102, 109);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 118, 124);
			expected.allocateNode(NodeType.IDENTIFIER, 118, 124);
			expected.allocateNode(NodeType.METHOD_INVOCATION, 118, 126);
			expected.allocateNode(NodeType.BLOCK, 113, 131);
			expected.allocateNode(NodeType.TRY_STATEMENT, 88, 131);
			expected.allocateNode(NodeType.BLOCK, 84, 134);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 21, 134);
			expected.allocateClassDeclaration(7, 136, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 137);
			requireThat(actual, "actual").isEqualTo(expected);
		}
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
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 37, 56);
			expected.allocateParameterDeclaration(37, 65, new ParameterAttribute("existing", false, false, false));
			expected.allocateNode(NodeType.QUALIFIED_NAME, 77, 99);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 109, 131);
			expected.allocateNode(NodeType.NULL_LITERAL, 132, 136);
			expected.allocateNode(NodeType.OBJECT_CREATION, 105, 137);
			expected.allocateNode(NodeType.IDENTIFIER, 139, 147);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 156, 167);
			expected.allocateNode(NodeType.IDENTIFIER, 156, 158);
			expected.allocateNode(NodeType.FIELD_ACCESS, 156, 167);
			expected.allocateNode(NodeType.METHOD_INVOCATION, 156, 169);
			expected.allocateNode(NodeType.BLOCK, 151, 174);
			expected.allocateNode(NodeType.TRY_STATEMENT, 72, 174);
			expected.allocateNode(NodeType.BLOCK, 68, 177);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 21, 177);
			expected.allocateClassDeclaration(7, 179, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 180);
			requireThat(actual, "actual").isEqualTo(expected);
		}
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
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 37, 56);
			expected.allocateParameterDeclaration(37, 65, new ParameterAttribute("existing", false, false, false));
			expected.allocateNode(NodeType.IDENTIFIER, 77, 85);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 87, 109);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 119, 141);
			expected.allocateNode(NodeType.NULL_LITERAL, 142, 146);
			expected.allocateNode(NodeType.OBJECT_CREATION, 115, 147);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 156, 167);
			expected.allocateNode(NodeType.IDENTIFIER, 156, 158);
			expected.allocateNode(NodeType.FIELD_ACCESS, 156, 167);
			expected.allocateNode(NodeType.METHOD_INVOCATION, 156, 169);
			expected.allocateNode(NodeType.BLOCK, 151, 174);
			expected.allocateNode(NodeType.TRY_STATEMENT, 72, 174);
			expected.allocateNode(NodeType.BLOCK, 68, 177);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 21, 177);
			expected.allocateClassDeclaration(7, 179, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 180);
			requireThat(actual, "actual").isEqualTo(expected);
		}
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
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 37, 50);
			expected.allocateParameterDeclaration(37, 59, new ParameterAttribute("resource", false, false, false));
			expected.allocateNode(NodeType.IDENTIFIER, 71, 79);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 88, 94);
			expected.allocateNode(NodeType.IDENTIFIER, 88, 94);
			expected.allocateNode(NodeType.METHOD_INVOCATION, 88, 96);
			expected.allocateNode(NodeType.BLOCK, 83, 101);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 111, 120);
			expected.allocateParameterDeclaration(111, 122, new ParameterAttribute("e", false, false, false));
			expected.allocateNode(NodeType.QUALIFIED_NAME, 131, 142);
			expected.allocateNode(NodeType.IDENTIFIER, 131, 142);
			expected.allocateNode(NodeType.METHOD_INVOCATION, 131, 144);
			expected.allocateNode(NodeType.BLOCK, 126, 149);
			expected.allocateNode(NodeType.CATCH_CLAUSE, 104, 149);
			expected.allocateNode(NodeType.TRY_STATEMENT, 66, 149);
			expected.allocateNode(NodeType.BLOCK, 62, 152);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 21, 152);
			expected.allocateClassDeclaration(7, 154, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 155);
			requireThat(actual, "actual").isEqualTo(expected);
		}
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
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 37, 50);
			expected.allocateParameterDeclaration(37, 59, new ParameterAttribute("resource", false, false, false));
			expected.allocateNode(NodeType.IDENTIFIER, 71, 79);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 88, 94);
			expected.allocateNode(NodeType.IDENTIFIER, 88, 94);
			expected.allocateNode(NodeType.METHOD_INVOCATION, 88, 96);
			expected.allocateNode(NodeType.BLOCK, 83, 101);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 119, 126);
			expected.allocateNode(NodeType.IDENTIFIER, 119, 126);
			expected.allocateNode(NodeType.METHOD_INVOCATION, 119, 128);
			expected.allocateNode(NodeType.BLOCK, 114, 133);
			expected.allocateNode(NodeType.FINALLY_CLAUSE, 104, 133);
			expected.allocateNode(NodeType.TRY_STATEMENT, 66, 133);
			expected.allocateNode(NodeType.BLOCK, 62, 136);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 21, 136);
			expected.allocateClassDeclaration(7, 138, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 139);
			requireThat(actual, "actual").isEqualTo(expected);
		}
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
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 37, 50);
			expected.allocateParameterDeclaration(37, 53, new ParameterAttribute("r1", false, false, false));
			expected.allocateNode(NodeType.QUALIFIED_NAME, 55, 68);
			expected.allocateParameterDeclaration(55, 71, new ParameterAttribute("r2", false, false, false));
			expected.allocateNode(NodeType.IDENTIFIER, 83, 85);
			expected.allocateNode(NodeType.IDENTIFIER, 87, 89);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 99, 105);
			expected.allocateNode(NodeType.IDENTIFIER, 99, 105);
			expected.allocateNode(NodeType.METHOD_INVOCATION, 99, 107);
			expected.allocateNode(NodeType.BLOCK, 94, 112);
			expected.allocateNode(NodeType.TRY_STATEMENT, 78, 112);
			expected.allocateNode(NodeType.BLOCK, 74, 115);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 21, 115);
			expected.allocateClassDeclaration(7, 117, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 118);
			requireThat(actual, "actual").isEqualTo(expected);
		}
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
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 37, 50);
			expected.allocateParameterDeclaration(37, 52, new ParameterAttribute("a", false, false, false));
			expected.allocateNode(NodeType.QUALIFIED_NAME, 54, 67);
			expected.allocateParameterDeclaration(54, 69, new ParameterAttribute("b", false, false, false));
			expected.allocateNode(NodeType.QUALIFIED_NAME, 71, 84);
			expected.allocateParameterDeclaration(71, 86, new ParameterAttribute("c", false, false, false));
			expected.allocateNode(NodeType.IDENTIFIER, 98, 99);
			expected.allocateNode(NodeType.IDENTIFIER, 101, 102);
			expected.allocateNode(NodeType.IDENTIFIER, 104, 105);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 114, 120);
			expected.allocateNode(NodeType.IDENTIFIER, 114, 120);
			expected.allocateNode(NodeType.METHOD_INVOCATION, 114, 122);
			expected.allocateNode(NodeType.BLOCK, 109, 127);
			expected.allocateNode(NodeType.TRY_STATEMENT, 93, 127);
			expected.allocateNode(NodeType.BLOCK, 89, 130);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 21, 130);
			expected.allocateClassDeclaration(7, 132, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 133);
			requireThat(actual, "actual").isEqualTo(expected);
		}
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
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 37, 50);
			expected.allocateParameterDeclaration(37, 59, new ParameterAttribute("existing", false, false, false));
			expected.allocateNode(NodeType.QUALIFIED_NAME, 71, 85);
			expected.allocateNode(NodeType.NULL_LITERAL, 91, 95);
			expected.allocateNode(NodeType.IDENTIFIER, 97, 105);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 107, 121);
			expected.allocateNode(NodeType.NULL_LITERAL, 127, 131);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 140, 146);
			expected.allocateNode(NodeType.IDENTIFIER, 140, 146);
			expected.allocateNode(NodeType.METHOD_INVOCATION, 140, 148);
			expected.allocateNode(NodeType.BLOCK, 135, 153);
			expected.allocateNode(NodeType.TRY_STATEMENT, 66, 153);
			expected.allocateNode(NodeType.BLOCK, 62, 156);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 21, 156);
			expected.allocateClassDeclaration(7, 158, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 159);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}
}
