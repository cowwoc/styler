package io.github.cowwoc.styler.parser.test;

import io.github.cowwoc.styler.ast.core.NodeArena;
import io.github.cowwoc.styler.ast.core.NodeType;
import io.github.cowwoc.styler.ast.core.TypeDeclarationAttribute;
import io.github.cowwoc.styler.parser.Parser;
import org.testng.annotations.Test;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;
import static io.github.cowwoc.styler.parser.test.ParserTestUtils.parse;

/**
 * Tests for parsing contextual keywords as expression starters.
 * <p>
 * Validates that contextual keywords can be used as standalone identifiers in expressions,
 * not just after the dot operator (which already works).
 */
public final class ContextualKeywordExpressionTest
{
	/**
	 * Verifies that {@code var} can start an expression (assignment target).
	 */
	@Test
	public void varAsAssignmentTarget()
	{
		String source = """
			class Test
			{
				void test()
				{
					var = 5;
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.IDENTIFIER, 31, 34);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 37, 38);
			expected.allocateNode(NodeType.ASSIGNMENT_EXPRESSION, 31, 38);
			expected.allocateNode(NodeType.BLOCK, 27, 42);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 14, 42);
			expected.allocateClassDeclaration(0, 44, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 45);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Verifies that {@code module} can start an expression (assignment target).
	 */
	@Test
	public void moduleAsAssignmentTarget()
	{
		String source = """
			class Test
			{
				void test()
				{
					module = null;
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.IDENTIFIER, 31, 37);
			expected.allocateNode(NodeType.NULL_LITERAL, 40, 44);
			expected.allocateNode(NodeType.ASSIGNMENT_EXPRESSION, 31, 44);
			expected.allocateNode(NodeType.BLOCK, 27, 48);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 14, 48);
			expected.allocateClassDeclaration(0, 50, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 51);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Verifies that {@code with} can start a method call expression.
	 */
	@Test
	public void withAsMethodCallStart()
	{
		String source = """
			class Test
			{
				void test()
				{
					with(arg);
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.IDENTIFIER, 31, 35);
			expected.allocateNode(NodeType.IDENTIFIER, 36, 39);
			expected.allocateNode(NodeType.METHOD_INVOCATION, 31, 40);
			expected.allocateNode(NodeType.BLOCK, 27, 44);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 14, 44);
			expected.allocateClassDeclaration(0, 46, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 47);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Verifies that {@code to} can start a method call expression.
	 */
	@Test
	public void toAsMethodCallStart()
	{
		String source = """
			class Test
			{
				void test()
				{
					to(destination);
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.IDENTIFIER, 31, 33);
			expected.allocateNode(NodeType.IDENTIFIER, 34, 45);
			expected.allocateNode(NodeType.METHOD_INVOCATION, 31, 46);
			expected.allocateNode(NodeType.BLOCK, 27, 50);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 14, 50);
			expected.allocateClassDeclaration(0, 52, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 53);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Verifies that {@code requires} can start a method call expression.
	 */
	@Test
	public void requiresAsMethodCallStart()
	{
		String source = """
			class Test
			{
				void test()
				{
					requires(dependency);
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.IDENTIFIER, 31, 39);
			expected.allocateNode(NodeType.IDENTIFIER, 40, 50);
			expected.allocateNode(NodeType.METHOD_INVOCATION, 31, 51);
			expected.allocateNode(NodeType.BLOCK, 27, 55);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 14, 55);
			expected.allocateClassDeclaration(0, 57, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 58);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Verifies that {@code exports} can start a method call expression.
	 */
	@Test
	public void exportsAsMethodCallStart()
	{
		String source = """
			class Test
			{
				void test()
				{
					exports(pkg);
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.IDENTIFIER, 31, 38);
			expected.allocateNode(NodeType.IDENTIFIER, 39, 42);
			expected.allocateNode(NodeType.METHOD_INVOCATION, 31, 43);
			expected.allocateNode(NodeType.BLOCK, 27, 47);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 14, 47);
			expected.allocateClassDeclaration(0, 49, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 50);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Verifies that {@code open} can start a method call expression.
	 */
	@Test
	public void openAsMethodCallStart()
	{
		String source = """
			class Test
			{
				void test()
				{
					open(resource);
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.IDENTIFIER, 31, 35);
			expected.allocateNode(NodeType.IDENTIFIER, 36, 44);
			expected.allocateNode(NodeType.METHOD_INVOCATION, 31, 45);
			expected.allocateNode(NodeType.BLOCK, 27, 49);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 14, 49);
			expected.allocateClassDeclaration(0, 51, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 52);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Verifies that {@code opens} can start a method call expression.
	 */
	@Test
	public void opensAsMethodCallStart()
	{
		String source = """
			class Test
			{
				void test()
				{
					opens(pkg);
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.IDENTIFIER, 31, 36);
			expected.allocateNode(NodeType.IDENTIFIER, 37, 40);
			expected.allocateNode(NodeType.METHOD_INVOCATION, 31, 41);
			expected.allocateNode(NodeType.BLOCK, 27, 45);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 14, 45);
			expected.allocateClassDeclaration(0, 47, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 48);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Verifies that {@code uses} can start a method call expression.
	 */
	@Test
	public void usesAsMethodCallStart()
	{
		String source = """
			class Test
			{
				void test()
				{
					uses(service);
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.IDENTIFIER, 31, 35);
			expected.allocateNode(NodeType.IDENTIFIER, 36, 43);
			expected.allocateNode(NodeType.METHOD_INVOCATION, 31, 44);
			expected.allocateNode(NodeType.BLOCK, 27, 48);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 14, 48);
			expected.allocateClassDeclaration(0, 50, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 51);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Verifies that {@code provides} can start a method call expression.
	 */
	@Test
	public void providesAsMethodCallStart()
	{
		String source = """
			class Test
			{
				void test()
				{
					provides(impl);
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.IDENTIFIER, 31, 39);
			expected.allocateNode(NodeType.IDENTIFIER, 40, 44);
			expected.allocateNode(NodeType.METHOD_INVOCATION, 31, 45);
			expected.allocateNode(NodeType.BLOCK, 27, 49);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 14, 49);
			expected.allocateClassDeclaration(0, 51, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 52);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Verifies that {@code transitive} can start a method call expression.
	 */
	@Test
	public void transitiveAsMethodCallStart()
	{
		String source = """
			class Test
			{
				void test()
				{
					transitive(deps);
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.IDENTIFIER, 31, 41);
			expected.allocateNode(NodeType.IDENTIFIER, 42, 46);
			expected.allocateNode(NodeType.METHOD_INVOCATION, 31, 47);
			expected.allocateNode(NodeType.BLOCK, 27, 51);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 14, 51);
			expected.allocateClassDeclaration(0, 53, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 54);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Verifies that contextual keyword can be used as method call argument.
	 */
	@Test
	public void contextualKeywordAsArgument()
	{
		String source = """
			class Test
			{
				void test()
				{
					process(var);
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 31, 38);
			expected.allocateNode(NodeType.IDENTIFIER, 31, 38);
			expected.allocateNode(NodeType.IDENTIFIER, 39, 42);
			expected.allocateNode(NodeType.METHOD_INVOCATION, 31, 43);
			expected.allocateNode(NodeType.BLOCK, 27, 47);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 14, 47);
			expected.allocateClassDeclaration(0, 49, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 50);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Verifies that contextual keyword can be used on RHS of binary expression.
	 */
	@Test
	public void contextualKeywordInBinaryExpression()
	{
		String source = """
			class Test
			{
				void test()
				{
					int x = 1 + var;
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.INTEGER_LITERAL, 39, 40);
			expected.allocateNode(NodeType.IDENTIFIER, 43, 46);
			expected.allocateNode(NodeType.BINARY_EXPRESSION, 39, 46);
			expected.allocateNode(NodeType.BLOCK, 27, 50);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 14, 50);
			expected.allocateClassDeclaration(0, 52, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 53);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Verifies that contextual keyword can be used with unary increment.
	 */
	@Test
	public void contextualKeywordWithIncrement()
	{
		String source = """
			class Test
			{
				void test()
				{
					++var;
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.IDENTIFIER, 33, 36);
			expected.allocateNode(NodeType.UNARY_EXPRESSION, 31, 36);
			expected.allocateNode(NodeType.BLOCK, 27, 40);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 14, 40);
			expected.allocateClassDeclaration(0, 42, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 43);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Verifies that the contextual keyword {@code module} can be used as a lambda parameter in a method
	 * argument.
	 */
	@Test
	public void contextualKeywordAsLambdaParameterInMethodArgument()
	{
		String source = """
			class Test
			{
				void m()
				{
					modules.forEach(module -> process(module));
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 28, 43);
			expected.allocateNode(NodeType.IDENTIFIER, 28, 35);
			expected.allocateNode(NodeType.FIELD_ACCESS, 28, 43);
			expected.allocateNode(NodeType.IDENTIFIER, 54, 61);
			expected.allocateNode(NodeType.IDENTIFIER, 62, 68);
			expected.allocateNode(NodeType.METHOD_INVOCATION, 54, 69);
			expected.allocateNode(NodeType.LAMBDA_EXPRESSION, 44, 69);
			expected.allocateNode(NodeType.METHOD_INVOCATION, 28, 70);
			expected.allocateNode(NodeType.BLOCK, 24, 74);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 14, 74);
			expected.allocateClassDeclaration(0, 76, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 77);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}
}
