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
 * Tests for parsing contextual keywords used as identifiers.
 */
public class ContextualKeywordIdentifierTest
{
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

			// Positions verified against actual output
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

			// Positions verified against actual output
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

			// Positions verified against actual output
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

			// Positions verified against actual output
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

			// Positions verified against actual output
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

			// Positions verified against actual output
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

			// Positions verified against actual output
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

			// Positions verified against actual output
			expected.allocateNode(NodeType.BLOCK, 48, 52);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 22, 52);
			expected.allocateClassDeclaration(7, 54, new TypeDeclarationAttribute("Graph"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 55);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}
}
