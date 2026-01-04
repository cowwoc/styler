package io.github.cowwoc.styler.parser.test;

import io.github.cowwoc.styler.ast.core.NodeArena;
import io.github.cowwoc.styler.ast.core.NodeType;
import io.github.cowwoc.styler.ast.core.TypeDeclarationAttribute;
import org.testng.annotations.Test;
import io.github.cowwoc.styler.parser.Parser;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;
import static io.github.cowwoc.styler.parser.test.ParserTestUtils.parse;

/**
 * Tests for parsing qualified class instantiation expressions ({@code outer.new Inner()}).
 */
public final class QualifiedInstantiationParserTest
{
	/**
	 * Validates that a simple qualified instantiation parses correctly.
	 * Tests the form {@code outer.new Inner()} used to create inner class instances.
	 */
	@Test
	public void testSimpleQualifiedInstantiation()
	{
		String source = """
			class Test
			{
				void m()
				{
					outer.new Inner();
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 28, 34);
			expected.allocateNode(NodeType.IDENTIFIER, 28, 33);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 38, 43);
			expected.allocateNode(NodeType.OBJECT_CREATION, 28, 45);
			expected.allocateNode(NodeType.BLOCK, 24, 49);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 14, 49);
			expected.allocateClassDeclaration(0, 51, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 52);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates that a chained method call after qualified instantiation parses correctly.
	 * Tests the form {@code outer.new Inner().method()}.
	 */
	@Test
	public void testChainedQualifiedInstantiation()
	{
		String source = """
			class Test
			{
				void m()
				{
					outer.new Inner().getValue();
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 28, 34);
			expected.allocateNode(NodeType.IDENTIFIER, 28, 33);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 38, 43);
			expected.allocateNode(NodeType.OBJECT_CREATION, 28, 45);
			expected.allocateNode(NodeType.FIELD_ACCESS, 28, 54);
			expected.allocateNode(NodeType.METHOD_INVOCATION, 28, 56);
			expected.allocateNode(NodeType.BLOCK, 24, 60);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 14, 60);
			expected.allocateClassDeclaration(0, 62, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 63);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates that qualified instantiation with an expression qualifier parses correctly.
	 * Tests the form {@code getOuter().new Inner()}.
	 */
	@Test
	public void testExpressionQualifierInstantiation()
	{
		String source = """
			class Test
			{
				void m()
				{
					getOuter().new Inner();
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 28, 36);
			expected.allocateNode(NodeType.IDENTIFIER, 28, 36);
			expected.allocateNode(NodeType.METHOD_INVOCATION, 28, 38);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 43, 48);
			expected.allocateNode(NodeType.OBJECT_CREATION, 28, 50);
			expected.allocateNode(NodeType.BLOCK, 24, 54);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 14, 54);
			expected.allocateClassDeclaration(0, 56, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 57);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates that qualified instantiation with constructor arguments parses correctly.
	 * Tests the form {@code outer.new Inner(arg1, arg2)}.
	 */
	@Test
	public void testQualifiedInstantiationWithArguments()
	{
		String source = """
			class Test
			{
				void m()
				{
					outer.new Inner(1, 2);
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 28, 34);
			expected.allocateNode(NodeType.IDENTIFIER, 28, 33);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 38, 43);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 44, 45);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 47, 48);
			expected.allocateNode(NodeType.OBJECT_CREATION, 28, 49);
			expected.allocateNode(NodeType.BLOCK, 24, 53);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 14, 53);
			expected.allocateClassDeclaration(0, 55, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 56);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates that nested qualified instantiation parses correctly.
	 * Tests creating an inner class instance using {@code Outer.this.new Inner()}.
	 */
	@Test
	public void testNestedQualifiedInstantiation()
	{
		String source = """
			class Test
			{
				void m()
				{
					Outer.this.new Inner();
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 28, 34);
			expected.allocateNode(NodeType.IDENTIFIER, 28, 33);
			expected.allocateNode(NodeType.THIS_EXPRESSION, 28, 38);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 43, 48);
			expected.allocateNode(NodeType.OBJECT_CREATION, 28, 50);
			expected.allocateNode(NodeType.BLOCK, 24, 54);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 14, 54);
			expected.allocateClassDeclaration(0, 56, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 57);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates that qualified instantiation with anonymous class body parses correctly.
	 * Tests the form {@code outer.new Inner() { ... }}.
	 */
	@Test
	public void testQualifiedInstantiationWithAnonymousClass()
	{
		String source = """
			class Test
			{
				void m()
				{
					outer.new Inner()
					{
					};
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 28, 34);
			expected.allocateNode(NodeType.IDENTIFIER, 28, 33);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 38, 43);
			expected.allocateNode(NodeType.OBJECT_CREATION, 28, 53);
			expected.allocateNode(NodeType.BLOCK, 24, 57);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 14, 57);
			expected.allocateClassDeclaration(0, 59, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 60);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}
}
