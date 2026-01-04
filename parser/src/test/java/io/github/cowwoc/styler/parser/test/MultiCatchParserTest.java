package io.github.cowwoc.styler.parser.test;

import io.github.cowwoc.styler.ast.core.NodeArena;
import io.github.cowwoc.styler.ast.core.NodeType;
import io.github.cowwoc.styler.ast.core.ParameterAttribute;
import io.github.cowwoc.styler.ast.core.TypeDeclarationAttribute;
import io.github.cowwoc.styler.parser.Parser;
import org.testng.annotations.Test;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;
import static io.github.cowwoc.styler.parser.test.ParserTestUtils.parse;

/**
 * Tests for parsing multi-catch (union type) exception handling (JDK 7+).
 */
public final class MultiCatchParserTest
{
	/**
	 * Validates parsing of basic two-exception union type in catch clause.
	 * Tests the simplest multi-catch form: {@code catch (IOException | SQLException e)}.
	 */
	@Test
	public void shouldParseTwoExceptionUnionType()
	{
		String source = """
			public class Test
			{
				public void foo()
				{
					try
					{
					}
					catch (IOException | SQLException e)
					{
					}
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			// Parser allocates nodes in post-order (children before parents)
			expected.allocateNode(NodeType.BLOCK, 50, 55);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 65, 76);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 79, 91);
			expected.allocateNode(NodeType.UNION_TYPE, 65, 91);
			expected.allocateParameterDeclaration(65, 93, new ParameterAttribute("e", false, false, false));
			expected.allocateNode(NodeType.BLOCK, 97, 102);
			expected.allocateNode(NodeType.CATCH_CLAUSE, 58, 102);
			expected.allocateNode(NodeType.TRY_STATEMENT, 44, 102);
			expected.allocateNode(NodeType.BLOCK, 40, 105);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 21, 105);
			expected.allocateClassDeclaration(7, 107, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 108);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates parsing of three-exception union type in catch clause.
	 * Tests common pattern with three exception types joined by {@code |} operators.
	 */
	@Test
	public void shouldParseThreeExceptionUnionType()
	{
		String source = """
			public class Test
			{
				public void foo()
				{
					try
					{
					}
					catch (IOException | SQLException | TimeoutException e)
					{
					}
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			// Parser allocates nodes in post-order (children before parents)
			expected.allocateNode(NodeType.BLOCK, 50, 55);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 65, 76);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 79, 91);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 94, 110);
			expected.allocateNode(NodeType.UNION_TYPE, 65, 110);
			expected.allocateParameterDeclaration(65, 112, new ParameterAttribute("e", false, false, false));
			expected.allocateNode(NodeType.BLOCK, 116, 121);
			expected.allocateNode(NodeType.CATCH_CLAUSE, 58, 121);
			expected.allocateNode(NodeType.TRY_STATEMENT, 44, 121);
			expected.allocateNode(NodeType.BLOCK, 40, 124);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 21, 124);
			expected.allocateClassDeclaration(7, 126, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 127);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates that simple catch clause without union type still works correctly.
	 * Regression test ensuring multi-catch support does not break single-exception catch.
	 */
	@Test
	public void shouldContinueToParseSimpleCatch()
	{
		String source = """
			public class Test
			{
				public void foo()
				{
					try
					{
						x = 1;
					}
					catch (Exception e)
					{
						x = 0;
					}
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			// Parser allocates nodes in post-order (children before parents)
			expected.allocateNode(NodeType.QUALIFIED_NAME, 55, 56);
			expected.allocateNode(NodeType.IDENTIFIER, 55, 56);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 59, 60);
			expected.allocateNode(NodeType.ASSIGNMENT_EXPRESSION, 55, 60);
			expected.allocateNode(NodeType.BLOCK, 50, 65);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 75, 84);
			expected.allocateParameterDeclaration(75, 86, new ParameterAttribute("e", false, false, false));
			expected.allocateNode(NodeType.QUALIFIED_NAME, 95, 96);
			expected.allocateNode(NodeType.IDENTIFIER, 95, 96);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 99, 100);
			expected.allocateNode(NodeType.ASSIGNMENT_EXPRESSION, 95, 100);
			expected.allocateNode(NodeType.BLOCK, 90, 105);
			expected.allocateNode(NodeType.CATCH_CLAUSE, 68, 105);
			expected.allocateNode(NodeType.TRY_STATEMENT, 44, 105);
			expected.allocateNode(NodeType.BLOCK, 40, 108);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 21, 108);
			expected.allocateClassDeclaration(7, 110, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 111);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates parsing of multi-catch with final modifier on parameter.
	 * Tests: {@code catch (final IOException | SQLException e)}.
	 */
	@Test
	public void shouldParseFinalMultiCatchParameter()
	{
		String source = """
			public class Test
			{
				public void foo()
				{
					try
					{
					}
					catch (final IOException | SQLException e)
					{
					}
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			// Parser allocates nodes in post-order (children before parents)
			expected.allocateNode(NodeType.BLOCK, 50, 55);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 71, 82);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 85, 97);
			expected.allocateNode(NodeType.UNION_TYPE, 71, 97);
			expected.allocateParameterDeclaration(65, 99, new ParameterAttribute("e", false, true, false));
			expected.allocateNode(NodeType.BLOCK, 103, 108);
			expected.allocateNode(NodeType.CATCH_CLAUSE, 58, 108);
			expected.allocateNode(NodeType.TRY_STATEMENT, 44, 108);
			expected.allocateNode(NodeType.BLOCK, 40, 111);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 21, 111);
			expected.allocateClassDeclaration(7, 113, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 114);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates parsing of multi-catch with fully qualified exception type names.
	 * Tests: {@code catch (java.io.IOException | java.sql.SQLException e)}.
	 */
	@Test
	public void shouldParseFullyQualifiedExceptionUnion()
	{
		String source = """
			public class Test
			{
				public void foo()
				{
					try
					{
					}
					catch (java.io.IOException | java.sql.SQLException e)
					{
					}
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			// Parser allocates nodes in post-order (children before parents)
			expected.allocateNode(NodeType.BLOCK, 50, 55);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 65, 84);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 87, 108);
			expected.allocateNode(NodeType.UNION_TYPE, 65, 108);
			expected.allocateParameterDeclaration(65, 110, new ParameterAttribute("e", false, false, false));
			expected.allocateNode(NodeType.BLOCK, 114, 119);
			expected.allocateNode(NodeType.CATCH_CLAUSE, 58, 119);
			expected.allocateNode(NodeType.TRY_STATEMENT, 44, 119);
			expected.allocateNode(NodeType.BLOCK, 40, 122);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 21, 122);
			expected.allocateClassDeclaration(7, 124, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 125);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}
}
