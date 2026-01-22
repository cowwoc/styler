package io.github.cowwoc.styler.parser.test;

import io.github.cowwoc.styler.ast.core.NodeArena;
import io.github.cowwoc.styler.ast.core.NodeType;
import io.github.cowwoc.styler.ast.core.ParameterAttribute;
import io.github.cowwoc.styler.ast.core.TypeDeclarationAttribute;
import io.github.cowwoc.styler.parser.ParseResult;
import io.github.cowwoc.styler.parser.Parser;
import org.testng.annotations.Test;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;
import static io.github.cowwoc.styler.parser.test.ParserTestUtils.parse;

/**
 * Tests for parsing contextual keywords in various contexts.
 */
public final class ContextualKeywordParserTest
{
	// ========== Expression Tests ==========

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

	// ========== Identifier Tests ==========

	/**
	 * Verifies that {@code with} can be used as a method name.
	 * This is valid because {@code with} is only a keyword in record patterns.
	 */
	@Test
	public void withAsMethodName()
	{
		String source = """
			public class Builder
			{
				public Builder with(String value)
				{
					return this;
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 44, 50);
			expected.allocateParameterDeclaration(44, 56,
				new ParameterAttribute("value", false, false, false));
			expected.allocateNode(NodeType.THIS_EXPRESSION, 70, 74);
			expected.allocateNode(NodeType.RETURN_STATEMENT, 63, 75);
			expected.allocateNode(NodeType.BLOCK, 59, 78);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 24, 78);
			expected.allocateClassDeclaration(7, 80, new TypeDeclarationAttribute("Builder"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 81);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Verifies that {@code module} can be used as a variable name.
	 * This is valid because {@code module} is only a keyword in module-info.java.
	 */
	@Test
	public void moduleAsVariableName()
	{
		String source = """
			public class Test
			{
				void test()
				{
					String module = "test";
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 38, 44);
			expected.allocateNode(NodeType.STRING_LITERAL, 54, 60);
			expected.allocateNode(NodeType.BLOCK, 34, 64);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 21, 64);
			expected.allocateClassDeclaration(7, 66, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 67);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Verifies that {@code var} can be used as a parameter name.
	 * This is valid because {@code var} is only a keyword for local variable type inference.
	 */
	@Test
	public void varAsParameterName()
	{
		String source = """
			public class Test
			{
				void process(String var)
				{
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 34, 40);
			expected.allocateParameterDeclaration(34, 44,
				new ParameterAttribute("var", false, false, false));
			expected.allocateNode(NodeType.BLOCK, 47, 51);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 21, 51);
			expected.allocateClassDeclaration(7, 53, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 54);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Verifies that {@code to} can be used as a field name.
	 * This is valid because {@code to} is only a keyword in module exports/opens.
	 */
	@Test
	public void toAsFieldName()
	{
		String source = """
			public class Range
			{
				private int to;
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.FIELD_DECLARATION, 22, 37);
			expected.allocateClassDeclaration(7, 39, new TypeDeclarationAttribute("Range"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 40);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Verifies that {@code open} can be used as a method name.
	 * This is valid because {@code open} is only a keyword in module-info.java.
	 */
	@Test
	public void openAsMethodName()
	{
		String source = """
			public class Resource
			{
				public void open()
				{
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.BLOCK, 45, 49);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 25, 49);
			expected.allocateClassDeclaration(7, 51, new TypeDeclarationAttribute("Resource"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 52);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Verifies that {@code requires} can be used as a variable name.
	 * This is valid because {@code requires} is only a keyword in module-info.java.
	 */
	@Test
	public void requiresAsVariableName()
	{
		String source = """
			public class Test
			{
				void test()
				{
					boolean requires = true;
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.BOOLEAN_LITERAL, 57, 61);
			expected.allocateNode(NodeType.BLOCK, 34, 65);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 21, 65);
			expected.allocateClassDeclaration(7, 67, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 68);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Verifies that {@code yield} can be used as a variable name outside switch expressions.
	 * This is valid because {@code yield} is only a keyword inside switch expressions.
	 */
	@Test
	public void yieldAsVariableNameOutsideSwitch()
	{
		String source = """
			public class Test
			{
				void test()
				{
					int yield = 42;
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.INTEGER_LITERAL, 50, 52);
			expected.allocateNode(NodeType.BLOCK, 34, 56);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 21, 56);
			expected.allocateClassDeclaration(7, 58, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 59);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Verifies that {@code transitive} can be used as a method name.
	 * This is valid because {@code transitive} is only a keyword in module requires.
	 */
	@Test
	public void transitiveAsMethodName()
	{
		String source = """
			public class Graph
			{
				public void transitive()
				{
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.BLOCK, 48, 52);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 22, 52);
			expected.allocateClassDeclaration(7, 54, new TypeDeclarationAttribute("Graph"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 55);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	// ========== Method Call Tests ==========

	/**
	 * Tests parsing method call with 'with' as method name.
	 */
	@Test
	public void testWithMethodCall()
	{
		String source = """
			class Test
			{
				void method()
				{
					builder.with(arg);
				}
			}
			""";
		try (Parser parser = new Parser(source))
		{
			ParseResult result = parser.parse();
			requireThat(result, "result").isInstanceOf(ParseResult.Success.class);
		}
	}

	/**
	 * Tests parsing method call with 'to' as method name.
	 */
	@Test
	public void testToMethodCall()
	{
		String source = """
			class Test
			{
				void method()
				{
					message.to(recipients);
				}
			}
			""";
		try (Parser parser = new Parser(source))
		{
			ParseResult result = parser.parse();
			requireThat(result, "result").isInstanceOf(ParseResult.Success.class);
		}
	}

	/**
	 * Tests parsing method call with 'requires' as method name.
	 */
	@Test
	public void testRequiresMethodCall()
	{
		String source = """
			class Test
			{
				void method()
				{
					someModule.requires(dependency);
				}
			}
			""";
		try (Parser parser = new Parser(source))
		{
			ParseResult result = parser.parse();
			requireThat(result, "result").isInstanceOf(ParseResult.Success.class);
		}
	}

	/**
	 * Tests parsing chained method calls with contextual keywords.
	 */
	@Test
	public void testChainedContextualKeywordMethods()
	{
		String source = """
			class Test
			{
				void method()
				{
					TestCompiler.forSystem().with(files).to(output);
				}
			}
			""";
		try (Parser parser = new Parser(source))
		{
			ParseResult result = parser.parse();
			requireThat(result, "result").isInstanceOf(ParseResult.Success.class);
		}
	}
}
