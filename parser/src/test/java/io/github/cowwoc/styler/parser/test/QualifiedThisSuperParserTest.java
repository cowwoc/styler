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
 * Tests for parsing qualified {@code this} and {@code super} expressions.
 */
public final class QualifiedThisSuperParserTest
{
	/**
	 * Validates that a simple qualified {@code this} expression parses correctly.
	 * Tests the form {@code Outer.this} used to access the enclosing class instance.
	 */
	@Test
	public void testSimpleQualifiedThis()
	{
		String source = """
			class Outer
			{
				class Inner
				{
					void method()
					{
						Object obj = Outer.this;
					}
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 53, 59);
			expected.allocateNode(NodeType.IDENTIFIER, 66, 71);
			expected.allocateNode(NodeType.THIS_EXPRESSION, 66, 76);
			expected.allocateNode(NodeType.BLOCK, 48, 81);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 32, 81);
			expected.allocateClassDeclaration(15, 84, new TypeDeclarationAttribute("Inner"));
			expected.allocateClassDeclaration(0, 86, new TypeDeclarationAttribute("Outer"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 87);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates that a simple qualified {@code super} expression parses correctly.
	 * Tests the form {@code Outer.super} used to access the superclass of an enclosing class.
	 */
	@Test
	public void testSimpleQualifiedSuper()
	{
		String source = """
			class Parent
			{
				void helper()
				{
				}
			}

			class Outer extends Parent
			{
				class Inner
				{
					void method()
					{
						Outer.super.helper();
					}
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.BLOCK, 31, 35);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 16, 35);
			expected.allocateClassDeclaration(0, 37, new TypeDeclarationAttribute("Parent"));
			expected.allocateNode(NodeType.QUALIFIED_NAME, 59, 65);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 107, 113);
			expected.allocateNode(NodeType.IDENTIFIER, 107, 112);
			expected.allocateNode(NodeType.SUPER_EXPRESSION, 107, 118);
			expected.allocateNode(NodeType.FIELD_ACCESS, 107, 125);
			expected.allocateNode(NodeType.METHOD_INVOCATION, 107, 127);
			expected.allocateNode(NodeType.BLOCK, 102, 132);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 86, 132);
			expected.allocateClassDeclaration(69, 135, new TypeDeclarationAttribute("Inner"));
			expected.allocateClassDeclaration(39, 137, new TypeDeclarationAttribute("Outer"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 138);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates that nested qualified {@code this} expressions parse correctly.
	 * Tests the form {@code Middle.this} used in deeply nested inner classes.
	 */
	@Test
	public void testNestedQualifiedThis()
	{
		String source = """
			class Outer
			{
				class Middle
				{
					class Inner
					{
						void method()
						{
							Object obj = Middle.this;
						}
					}
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 75, 81);
			expected.allocateNode(NodeType.IDENTIFIER, 88, 94);
			expected.allocateNode(NodeType.THIS_EXPRESSION, 88, 99);
			expected.allocateNode(NodeType.BLOCK, 69, 105);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 52, 105);
			expected.allocateClassDeclaration(33, 109, new TypeDeclarationAttribute("Inner"));
			expected.allocateClassDeclaration(15, 112, new TypeDeclarationAttribute("Middle"));
			expected.allocateClassDeclaration(0, 114, new TypeDeclarationAttribute("Outer"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 115);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates that qualified {@code this} in an assignment context parses correctly.
	 * Tests assigning the outer class instance to a variable.
	 */
	@Test
	public void testQualifiedThisInAssignment()
	{
		String source = """
			class Outer
			{
				class Inner
				{
					Object outer;

					void method()
					{
						outer = Outer.this;
					}
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.FIELD_DECLARATION, 32, 45);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 70, 75);
			expected.allocateNode(NodeType.IDENTIFIER, 70, 75);
			expected.allocateNode(NodeType.IDENTIFIER, 78, 83);
			expected.allocateNode(NodeType.THIS_EXPRESSION, 78, 88);
			expected.allocateNode(NodeType.ASSIGNMENT_EXPRESSION, 70, 88);
			expected.allocateNode(NodeType.BLOCK, 65, 93);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 49, 93);
			expected.allocateClassDeclaration(15, 96, new TypeDeclarationAttribute("Inner"));
			expected.allocateClassDeclaration(0, 98, new TypeDeclarationAttribute("Outer"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 99);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates that qualified {@code super} method call parses correctly.
	 * Tests calling a superclass method via the enclosing class: {@code Outer.super.doWork()}.
	 */
	@Test
	public void testQualifiedSuperMethodCall()
	{
		String source = """
			class Parent
			{
				void doWork()
				{
				}
			}

			class Outer extends Parent
			{
				class Inner
				{
					void method()
					{
						Outer.super.doWork();
					}
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.BLOCK, 31, 35);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 16, 35);
			expected.allocateClassDeclaration(0, 37, new TypeDeclarationAttribute("Parent"));
			expected.allocateNode(NodeType.QUALIFIED_NAME, 59, 65);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 107, 113);
			expected.allocateNode(NodeType.IDENTIFIER, 107, 112);
			expected.allocateNode(NodeType.SUPER_EXPRESSION, 107, 118);
			expected.allocateNode(NodeType.FIELD_ACCESS, 107, 125);
			expected.allocateNode(NodeType.METHOD_INVOCATION, 107, 127);
			expected.allocateNode(NodeType.BLOCK, 102, 132);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 86, 132);
			expected.allocateClassDeclaration(69, 135, new TypeDeclarationAttribute("Inner"));
			expected.allocateClassDeclaration(39, 137, new TypeDeclarationAttribute("Outer"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 138);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates that qualified {@code super} field access parses correctly.
	 * Tests accessing a superclass field via the enclosing class: {@code Outer.super.value}.
	 */
	@Test
	public void testQualifiedSuperFieldAccess()
	{
		String source = """
			class Parent
			{
				int value = 42;
			}

			class Outer extends Parent
			{
				class Inner
				{
					int getValue()
					{
						return Outer.super.value;
					}
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.INTEGER_LITERAL, 28, 30);
			expected.allocateNode(NodeType.FIELD_DECLARATION, 16, 31);
			expected.allocateClassDeclaration(0, 33, new TypeDeclarationAttribute("Parent"));
			expected.allocateNode(NodeType.QUALIFIED_NAME, 55, 61);
			expected.allocateNode(NodeType.IDENTIFIER, 111, 116);
			expected.allocateNode(NodeType.SUPER_EXPRESSION, 111, 122);
			expected.allocateNode(NodeType.FIELD_ACCESS, 111, 128);
			expected.allocateNode(NodeType.RETURN_STATEMENT, 104, 129);
			expected.allocateNode(NodeType.BLOCK, 99, 133);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 82, 133);
			expected.allocateClassDeclaration(65, 136, new TypeDeclarationAttribute("Inner"));
			expected.allocateClassDeclaration(35, 138, new TypeDeclarationAttribute("Outer"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 139);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates that qualified {@code this} in a return statement parses correctly.
	 * Tests returning the enclosing class instance from an inner class method.
	 */
	@Test
	public void testQualifiedThisInReturnStatement()
	{
		String source = """
			class Outer
			{
				class Inner
				{
					Outer getOuter()
					{
						return Outer.this;
					}
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.IDENTIFIER, 63, 68);
			expected.allocateNode(NodeType.THIS_EXPRESSION, 63, 73);
			expected.allocateNode(NodeType.RETURN_STATEMENT, 56, 74);
			expected.allocateNode(NodeType.BLOCK, 51, 78);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 32, 78);
			expected.allocateClassDeclaration(15, 81, new TypeDeclarationAttribute("Inner"));
			expected.allocateClassDeclaration(0, 83, new TypeDeclarationAttribute("Outer"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 84);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates that qualified {@code this} as a method argument parses correctly.
	 * Tests passing the enclosing class instance to a method.
	 */
	@Test
	public void testQualifiedThisAsMethodArgument()
	{
		String source = """
			class Outer
			{
				void accept(Object obj)
				{
				}

				class Inner
				{
					void method()
					{
						accept(Outer.this);
					}
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 27, 33);
			expected.allocateParameterDeclaration(27, 37, new ParameterAttribute("obj", false, false, false));
			expected.allocateNode(NodeType.BLOCK, 40, 44);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 15, 44);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 85, 91);
			expected.allocateNode(NodeType.IDENTIFIER, 85, 91);
			expected.allocateNode(NodeType.IDENTIFIER, 92, 97);
			expected.allocateNode(NodeType.THIS_EXPRESSION, 92, 102);
			expected.allocateNode(NodeType.METHOD_INVOCATION, 85, 103);
			expected.allocateNode(NodeType.BLOCK, 80, 108);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 64, 108);
			expected.allocateClassDeclaration(47, 111, new TypeDeclarationAttribute("Inner"));
			expected.allocateClassDeclaration(0, 113, new TypeDeclarationAttribute("Outer"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 114);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates that chained method call on qualified {@code super} parses correctly.
	 * Tests calling {@code toString()} on the superclass instance: {@code Outer.super.toString()}.
	 */
	@Test
	public void testChainedQualifiedSuperMethodCall()
	{
		String source = """
			class Parent
			{
			}

			class Outer extends Parent
			{
				class Inner
				{
					String method()
					{
						return Outer.super.toString();
					}
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateClassDeclaration(0, 16, new TypeDeclarationAttribute("Parent"));
			expected.allocateNode(NodeType.QUALIFIED_NAME, 38, 44);
			expected.allocateNode(NodeType.IDENTIFIER, 95, 100);
			expected.allocateNode(NodeType.SUPER_EXPRESSION, 95, 106);
			expected.allocateNode(NodeType.FIELD_ACCESS, 95, 115);
			expected.allocateNode(NodeType.METHOD_INVOCATION, 95, 117);
			expected.allocateNode(NodeType.RETURN_STATEMENT, 88, 118);
			expected.allocateNode(NodeType.BLOCK, 83, 122);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 65, 122);
			expected.allocateClassDeclaration(48, 125, new TypeDeclarationAttribute("Inner"));
			expected.allocateClassDeclaration(18, 127, new TypeDeclarationAttribute("Outer"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 128);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates that qualified {@code this} with a generic outer class parses correctly.
	 * Tests accessing the enclosing instance when the outer class has type parameters.
	 */
	@Test
	public void testGenericOuterClassQualifiedThis()
	{
		String source = """
			class Outer<T>
			{
				class Inner
				{
					Outer<T> getOuter()
					{
						return Outer.this;
					}
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 41, 42);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 41, 42);
			expected.allocateNode(NodeType.IDENTIFIER, 69, 74);
			expected.allocateNode(NodeType.THIS_EXPRESSION, 69, 79);
			expected.allocateNode(NodeType.RETURN_STATEMENT, 62, 80);
			expected.allocateNode(NodeType.BLOCK, 57, 84);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 35, 84);
			expected.allocateClassDeclaration(18, 87, new TypeDeclarationAttribute("Inner"));
			expected.allocateClassDeclaration(0, 89, new TypeDeclarationAttribute("Outer"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 90);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates that qualified {@code this} in an inner class constructor parses correctly.
	 * Tests using the enclosing instance during inner class construction.
	 */
	@Test
	public void testQualifiedThisInInnerClassConstructor()
	{
		String source = """
			class Outer
			{
				Object outerRef;

				class Inner
				{
					Inner()
					{
						outerRef = Outer.this;
					}
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.FIELD_DECLARATION, 15, 31);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 66, 74);
			expected.allocateNode(NodeType.IDENTIFIER, 66, 74);
			expected.allocateNode(NodeType.IDENTIFIER, 77, 82);
			expected.allocateNode(NodeType.THIS_EXPRESSION, 77, 87);
			expected.allocateNode(NodeType.ASSIGNMENT_EXPRESSION, 66, 87);
			expected.allocateNode(NodeType.BLOCK, 61, 92);
			expected.allocateNode(NodeType.CONSTRUCTOR_DECLARATION, 51, 92);
			expected.allocateClassDeclaration(34, 95, new TypeDeclarationAttribute("Inner"));
			expected.allocateClassDeclaration(0, 97, new TypeDeclarationAttribute("Outer"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 98);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates that qualified {@code this} works in a comparison expression.
	 * Tests using the enclosing instance in an equals comparison.
	 */
	@Test
	public void testQualifiedThisInComparison()
	{
		String source = """
			class Outer
			{
				class Inner
				{
					boolean isSame(Object other)
					{
						return Outer.this == other;
					}
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 47, 53);
			expected.allocateParameterDeclaration(47, 59, new ParameterAttribute("other", false, false, false));
			expected.allocateNode(NodeType.IDENTIFIER, 75, 80);
			expected.allocateNode(NodeType.THIS_EXPRESSION, 75, 85);
			expected.allocateNode(NodeType.IDENTIFIER, 89, 94);
			expected.allocateNode(NodeType.BINARY_EXPRESSION, 75, 94);
			expected.allocateNode(NodeType.RETURN_STATEMENT, 68, 95);
			expected.allocateNode(NodeType.BLOCK, 63, 99);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 32, 99);
			expected.allocateClassDeclaration(15, 102, new TypeDeclarationAttribute("Inner"));
			expected.allocateClassDeclaration(0, 104, new TypeDeclarationAttribute("Outer"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 105);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}
}
