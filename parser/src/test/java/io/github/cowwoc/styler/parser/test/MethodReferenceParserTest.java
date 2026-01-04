package io.github.cowwoc.styler.parser.test;

import io.github.cowwoc.styler.ast.core.NodeArena;
import io.github.cowwoc.styler.ast.core.NodeType;
import io.github.cowwoc.styler.ast.core.ParameterAttribute;
import io.github.cowwoc.styler.ast.core.TypeDeclarationAttribute;
import org.testng.annotations.Test;
import io.github.cowwoc.styler.parser.Parser;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;
import static io.github.cowwoc.styler.parser.test.ParserTestUtils.assertParseFails;
import static io.github.cowwoc.styler.parser.test.ParserTestUtils.parse;

/**
 * Tests for method reference parsing support.
 * Method references are a compact form of lambda expressions that reference existing methods.
 * They use the {@code ::} syntax introduced in Java 8.
 */
public class MethodReferenceParserTest
{
	// ========================================
	// Core Variants (11 tests)
	// ========================================

	/**
	 * Tests static method reference parsing.
	 * Static method references have the form {@code ClassName::methodName} and reference
	 * static methods that can be used in functional contexts.
	 */
	@Test
	public void testStaticMethodReference()
	{
		String source = """
			class Test
			{
				Object f = String::valueOf;
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.IDENTIFIER, 25, 31);
			expected.allocateNode(NodeType.METHOD_REFERENCE, 25, 40);
			expected.allocateNode(NodeType.FIELD_DECLARATION, 14, 41);
			expected.allocateClassDeclaration(0, 43, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 44);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Tests instance method reference on a variable.
	 * Instance method references on variables have the form {@code variable::methodName}
	 * and bind to a specific object instance.
	 */
	@Test
	public void testInstanceMethodReferenceOnVariable()
	{
		String source = """
			class Test
			{
				void method()
				{
					String str = "test";
					Object c = str::length;
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 33, 39);
			expected.allocateNode(NodeType.STRING_LITERAL, 46, 52);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 56, 62);
			expected.allocateNode(NodeType.IDENTIFIER, 67, 70);
			expected.allocateNode(NodeType.METHOD_REFERENCE, 67, 78);
			expected.allocateNode(NodeType.BLOCK, 29, 82);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 14, 82);
			expected.allocateClassDeclaration(0, 84, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 85);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Tests instance method reference using {@code this} keyword.
	 * The {@code this::methodName} form references an instance method on the current object.
	 */
	@Test
	public void testInstanceMethodReferenceOnThis()
	{
		String source = """
			class Test
			{
				void helper()
				{
				}

				void method()
				{
					Runnable r = this::helper;
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.BLOCK, 29, 33);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 14, 33);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 55, 63);
			expected.allocateNode(NodeType.THIS_EXPRESSION, 68, 72);
			expected.allocateNode(NodeType.METHOD_REFERENCE, 68, 80);
			expected.allocateNode(NodeType.BLOCK, 51, 84);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 36, 84);
			expected.allocateClassDeclaration(0, 86, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 87);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Tests instance method reference using {@code super} keyword.
	 * The {@code super::methodName} form references an inherited method from the superclass.
	 */
	@Test
	public void testInstanceMethodReferenceOnSuper()
	{
		String source = """
			class Parent
			{
				void helper()
				{
				}
			}

			class Test extends Parent
			{
				void method()
				{
					Runnable r = super::helper;
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
			expected.allocateNode(NodeType.QUALIFIED_NAME, 58, 64);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 87, 95);
			expected.allocateNode(NodeType.SUPER_EXPRESSION, 100, 105);
			expected.allocateNode(NodeType.METHOD_REFERENCE, 100, 113);
			expected.allocateNode(NodeType.BLOCK, 83, 117);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 68, 117);
			expected.allocateClassDeclaration(39, 119, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 120);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Tests unbound instance method reference.
	 * Unbound references have the form {@code ClassName::instanceMethodName} where
	 * the first parameter of the functional interface becomes the receiver.
	 */
	@Test
	public void testUnboundInstanceMethodReference()
	{
		String source = """
			class Test
			{
				Object f = String::toLowerCase;
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.IDENTIFIER, 25, 31);
			expected.allocateNode(NodeType.METHOD_REFERENCE, 25, 44);
			expected.allocateNode(NodeType.FIELD_DECLARATION, 14, 45);
			expected.allocateClassDeclaration(0, 47, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 48);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Tests constructor reference for a regular class.
	 * Constructor references have the form {@code ClassName::new} and create new instances.
	 */
	@Test
	public void testConstructorReference()
	{
		String source = """
			class Test
			{
				Object s = String::new;
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.IDENTIFIER, 25, 31);
			expected.allocateNode(NodeType.METHOD_REFERENCE, 25, 36);
			expected.allocateNode(NodeType.FIELD_DECLARATION, 14, 37);
			expected.allocateClassDeclaration(0, 39, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 40);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Tests constructor reference with explicit generic type parameters.
	 * Generic constructor references specify type arguments before {@code ::new}.
	 */
	@Test
	public void testGenericConstructorReference()
	{
		String source = """
			class Container<T>
			{
			}

			class Test
			{
				Object s = Container::new;
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateClassDeclaration(0, 22, new TypeDeclarationAttribute("Container"));
			expected.allocateNode(NodeType.IDENTIFIER, 49, 58);
			expected.allocateNode(NodeType.METHOD_REFERENCE, 49, 63);
			expected.allocateNode(NodeType.FIELD_DECLARATION, 38, 64);
			expected.allocateClassDeclaration(24, 66, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 67);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Tests constructor reference for a nested class.
	 * Nested class constructor references use qualified names like {@code Outer.Inner::new}.
	 */
	@Test
	public void testNestedClassConstructorReference()
	{
		String source = """
			class Outer
			{
				class Inner
				{
				}

				Object f = Inner::new;
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateClassDeclaration(15, 32, new TypeDeclarationAttribute("Inner"));
			expected.allocateNode(NodeType.IDENTIFIER, 46, 51);
			expected.allocateNode(NodeType.METHOD_REFERENCE, 46, 56);
			expected.allocateNode(NodeType.FIELD_DECLARATION, 35, 57);
			expected.allocateClassDeclaration(0, 59, new TypeDeclarationAttribute("Outer"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 60);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Tests method reference with a parameterized type receiver.
	 * Method references can be applied to generic class methods.
	 */
	@Test
	public void testMethodReferenceWithGenericReceiver()
	{
		String source = """
			class Container<T>
			{
				T get()
				{
					return null;
				}
			}

			class Test
			{
				void method(Container<String> container)
				{
					Object f = container::get;
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.NULL_LITERAL, 42, 46);
			expected.allocateNode(NodeType.RETURN_STATEMENT, 35, 47);
			expected.allocateNode(NodeType.BLOCK, 31, 50);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 22, 50);
			expected.allocateClassDeclaration(0, 52, new TypeDeclarationAttribute("Container"));
			expected.allocateNode(NodeType.QUALIFIED_NAME, 80, 89);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 90, 96);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 90, 96);
			expected.allocateNode(NodeType.PARAMETERIZED_TYPE, 80, 97);
			expected.allocateParameterDeclaration(80, 107, new ParameterAttribute("container", false, false, false));
			expected.allocateNode(NodeType.QUALIFIED_NAME, 114, 120);
			expected.allocateNode(NodeType.IDENTIFIER, 125, 134);
			expected.allocateNode(NodeType.METHOD_REFERENCE, 125, 139);
			expected.allocateNode(NodeType.BLOCK, 110, 143);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 68, 143);
			expected.allocateClassDeclaration(54, 145, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 146);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Tests method reference targeting an overloaded method.
	 * When multiple overloads exist, the compiler selects the correct one based on context.
	 */
	@Test
	public void testMethodReferenceToOverloadedMethod()
	{
		String source = """
			class Test
			{
				void accept(Object o)
				{
				}

				void accept(String s)
				{
				}

				void method()
				{
					Object f = this::accept;
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 26, 32);
			expected.allocateParameterDeclaration(26, 34, new ParameterAttribute("o", false, false, false));
			expected.allocateNode(NodeType.BLOCK, 37, 41);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 14, 41);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 56, 62);
			expected.allocateParameterDeclaration(56, 64, new ParameterAttribute("s", false, false, false));
			expected.allocateNode(NodeType.BLOCK, 67, 71);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 44, 71);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 93, 99);
			expected.allocateNode(NodeType.THIS_EXPRESSION, 104, 108);
			expected.allocateNode(NodeType.METHOD_REFERENCE, 104, 116);
			expected.allocateNode(NodeType.BLOCK, 89, 120);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 74, 120);
			expected.allocateClassDeclaration(0, 122, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 123);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Tests method reference with multiple chained method calls before the reference.
	 * Method references can follow complex expression chains.
	 */
	@Test
	public void testMethodReferenceOnChainedCalls()
	{
		String source = """
			class Builder
			{
				Builder append(String s)
				{
					return this;
				}

				String build()
				{
					return null;
				}
			}

			class Test
			{
				void method(Builder b)
				{
					Object f = b.append("a").append("b")::build;
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 32, 38);
			expected.allocateParameterDeclaration(32, 40, new ParameterAttribute("s", false, false, false));
			expected.allocateNode(NodeType.THIS_EXPRESSION, 54, 58);
			expected.allocateNode(NodeType.RETURN_STATEMENT, 47, 59);
			expected.allocateNode(NodeType.BLOCK, 43, 62);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 17, 62);
			expected.allocateNode(NodeType.NULL_LITERAL, 92, 96);
			expected.allocateNode(NodeType.RETURN_STATEMENT, 85, 97);
			expected.allocateNode(NodeType.BLOCK, 81, 100);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 65, 100);
			expected.allocateClassDeclaration(0, 102, new TypeDeclarationAttribute("Builder"));
			expected.allocateNode(NodeType.QUALIFIED_NAME, 130, 137);
			expected.allocateParameterDeclaration(130, 139, new ParameterAttribute("b", false, false, false));
			expected.allocateNode(NodeType.QUALIFIED_NAME, 146, 152);
			expected.allocateNode(NodeType.IDENTIFIER, 157, 158);
			expected.allocateNode(NodeType.FIELD_ACCESS, 157, 165);
			expected.allocateNode(NodeType.STRING_LITERAL, 166, 169);
			expected.allocateNode(NodeType.METHOD_INVOCATION, 157, 170);
			expected.allocateNode(NodeType.FIELD_ACCESS, 157, 177);
			expected.allocateNode(NodeType.STRING_LITERAL, 178, 181);
			expected.allocateNode(NodeType.METHOD_INVOCATION, 157, 182);
			expected.allocateNode(NodeType.METHOD_REFERENCE, 157, 189);
			expected.allocateNode(NodeType.BLOCK, 142, 193);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 118, 193);
			expected.allocateClassDeclaration(104, 195, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 196);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	// ========================================
	// Expression Contexts (6 tests)
	// ========================================

	/**
	 * Tests method reference as an argument to a method call.
	 * Method references can be passed directly as arguments to methods expecting functional interfaces.
	 */
	@Test
	public void testMethodReferenceAsArgument()
	{
		String source = """
			class Test
			{
				void accept(Object o)
				{
				}

				void method()
				{
					accept(String::valueOf);
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 26, 32);
			expected.allocateParameterDeclaration(26, 34, new ParameterAttribute("o", false, false, false));
			expected.allocateNode(NodeType.BLOCK, 37, 41);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 14, 41);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 63, 69);
			expected.allocateNode(NodeType.IDENTIFIER, 63, 69);
			expected.allocateNode(NodeType.IDENTIFIER, 70, 76);
			expected.allocateNode(NodeType.METHOD_REFERENCE, 70, 85);
			expected.allocateNode(NodeType.METHOD_INVOCATION, 63, 86);
			expected.allocateNode(NodeType.BLOCK, 59, 90);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 44, 90);
			expected.allocateClassDeclaration(0, 92, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 93);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Tests method reference in a stream map operation.
	 * Method references are commonly used in stream pipelines for transformations.
	 */
	@Test
	public void testMethodReferenceInStreamMap()
	{
		String source = """
			class Stream
			{
				Stream map(Object mapper)
				{
					return this;
				}
			}

			class Test
			{
				void method(Stream stream)
				{
					stream.map(String::toLowerCase);
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 27, 33);
			expected.allocateParameterDeclaration(27, 40, new ParameterAttribute("mapper", false, false, false));
			expected.allocateNode(NodeType.THIS_EXPRESSION, 54, 58);
			expected.allocateNode(NodeType.RETURN_STATEMENT, 47, 59);
			expected.allocateNode(NodeType.BLOCK, 43, 62);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 16, 62);
			expected.allocateClassDeclaration(0, 64, new TypeDeclarationAttribute("Stream"));
			expected.allocateNode(NodeType.QUALIFIED_NAME, 92, 98);
			expected.allocateParameterDeclaration(92, 105, new ParameterAttribute("stream", false, false, false));
			expected.allocateNode(NodeType.QUALIFIED_NAME, 112, 122);
			expected.allocateNode(NodeType.IDENTIFIER, 112, 118);
			expected.allocateNode(NodeType.FIELD_ACCESS, 112, 122);
			expected.allocateNode(NodeType.IDENTIFIER, 123, 129);
			expected.allocateNode(NodeType.METHOD_REFERENCE, 123, 142);
			expected.allocateNode(NodeType.METHOD_INVOCATION, 112, 143);
			expected.allocateNode(NodeType.BLOCK, 108, 147);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 80, 147);
			expected.allocateClassDeclaration(66, 149, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 150);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Tests method reference in a stream filter operation.
	 * Method references returning boolean can be used as predicates in filter operations.
	 */
	@Test
	public void testMethodReferenceInStreamFilter()
	{
		String source = """
			class Stream
			{
				Stream filter(Object predicate)
				{
					return this;
				}
			}

			class Test
			{
				void method(Stream stream)
				{
					stream.filter(String::isEmpty);
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 30, 36);
			expected.allocateParameterDeclaration(30, 46, new ParameterAttribute("predicate", false, false, false));
			expected.allocateNode(NodeType.THIS_EXPRESSION, 60, 64);
			expected.allocateNode(NodeType.RETURN_STATEMENT, 53, 65);
			expected.allocateNode(NodeType.BLOCK, 49, 68);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 16, 68);
			expected.allocateClassDeclaration(0, 70, new TypeDeclarationAttribute("Stream"));
			expected.allocateNode(NodeType.QUALIFIED_NAME, 98, 104);
			expected.allocateParameterDeclaration(98, 111, new ParameterAttribute("stream", false, false, false));
			expected.allocateNode(NodeType.QUALIFIED_NAME, 118, 131);
			expected.allocateNode(NodeType.IDENTIFIER, 118, 124);
			expected.allocateNode(NodeType.FIELD_ACCESS, 118, 131);
			expected.allocateNode(NodeType.IDENTIFIER, 132, 138);
			expected.allocateNode(NodeType.METHOD_REFERENCE, 132, 147);
			expected.allocateNode(NodeType.METHOD_INVOCATION, 118, 148);
			expected.allocateNode(NodeType.BLOCK, 114, 152);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 86, 152);
			expected.allocateClassDeclaration(72, 154, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 155);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Tests method reference on a chained expression.
	 * Method references can be applied to the result of method chains.
	 */
	@Test
	public void testMethodReferenceChainedExpression()
	{
		String source = """
			class Container
			{
				String getValue()
				{
					return null;
				}
			}

			class Test
			{
				void method(Container obj)
				{
					Object c = obj.getValue()::length;
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.NULL_LITERAL, 49, 53);
			expected.allocateNode(NodeType.RETURN_STATEMENT, 42, 54);
			expected.allocateNode(NodeType.BLOCK, 38, 57);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 19, 57);
			expected.allocateClassDeclaration(0, 59, new TypeDeclarationAttribute("Container"));
			expected.allocateNode(NodeType.QUALIFIED_NAME, 87, 96);
			expected.allocateParameterDeclaration(87, 100, new ParameterAttribute("obj", false, false, false));
			expected.allocateNode(NodeType.QUALIFIED_NAME, 107, 113);
			expected.allocateNode(NodeType.IDENTIFIER, 118, 121);
			expected.allocateNode(NodeType.FIELD_ACCESS, 118, 130);
			expected.allocateNode(NodeType.METHOD_INVOCATION, 118, 132);
			expected.allocateNode(NodeType.METHOD_REFERENCE, 118, 140);
			expected.allocateNode(NodeType.BLOCK, 103, 144);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 75, 144);
			expected.allocateClassDeclaration(61, 146, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 147);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Tests method reference as part of a ternary expression.
	 * Method references can appear in the branches of conditional expressions.
	 */
	@Test
	public void testMethodReferenceInTernary()
	{
		String source = """
			class Test
			{
				void method(boolean flag)
				{
					Object f = flag ? String::length : String::hashCode;
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateParameterDeclaration(26, 38, new ParameterAttribute("flag", false, false, false));
			expected.allocateNode(NodeType.QUALIFIED_NAME, 45, 51);
			expected.allocateNode(NodeType.IDENTIFIER, 56, 60);
			expected.allocateNode(NodeType.IDENTIFIER, 63, 69);
			expected.allocateNode(NodeType.METHOD_REFERENCE, 63, 77);
			expected.allocateNode(NodeType.IDENTIFIER, 80, 86);
			expected.allocateNode(NodeType.METHOD_REFERENCE, 80, 96);
			expected.allocateNode(NodeType.CONDITIONAL_EXPRESSION, 56, 96);
			expected.allocateNode(NodeType.BLOCK, 41, 100);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 14, 100);
			expected.allocateClassDeclaration(0, 102, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 103);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Tests method reference in a return statement.
	 * Method references can be returned directly from methods with functional interface return types.
	 */
	@Test
	public void testMethodReferenceInReturnStatement()
	{
		String source = """
			class Test
			{
				Object method()
				{
					return Integer::parseInt;
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.IDENTIFIER, 42, 49);
			expected.allocateNode(NodeType.METHOD_REFERENCE, 42, 59);
			expected.allocateNode(NodeType.RETURN_STATEMENT, 35, 60);
			expected.allocateNode(NodeType.BLOCK, 31, 63);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 14, 63);
			expected.allocateClassDeclaration(0, 65, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 66);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	// ========================================
	// Error Cases (3 tests)
	// ========================================

	/**
	 * Tests that method reference with missing method name fails to parse.
	 * The syntax {@code String::} is malformed because no method name follows the double colon.
	 */
	@Test
	public void testMalformedMethodReferenceNoMethod()
	{
		assertParseFails("class Test { Object x = String::; }");
	}

	/**
	 * Tests that method reference with extra colon fails to parse.
	 * The syntax {@code String:::length} has too many colons and is invalid.
	 */
	@Test
	public void testMalformedMethodReferenceExtraColon()
	{
		assertParseFails("class Test { Object x = String:::length; }");
	}

	/**
	 * Tests that single colon is not confused with method reference.
	 * The syntax {@code String:length} uses single colon which is not valid for method references.
	 */
	@Test
	public void testMalformedMethodReferenceSingleColon()
	{
		assertParseFails("class Test { Object x = String:length; }");
	}

	// ========================================
	// Real-World Integration (4 tests)
	// ========================================

	/**
	 * Tests a complete stream pipeline with multiple method references.
	 * Real-world code often chains multiple stream operations using method references.
	 */
	@Test
	public void testRealWorldStreamPipeline()
	{
		String source = """
			class Stream
			{
				Stream filter(Object p)
				{
					return this;
				}

				Stream map(Object m)
				{
					return this;
				}

				void forEach(Object c)
				{
				}
			}

			class Test
			{
				void method(Stream names)
				{
					names
						.filter(String::isEmpty)
						.map(String::toLowerCase)
						.forEach(Object::hashCode);
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 30, 36);
			expected.allocateParameterDeclaration(30, 38, new ParameterAttribute("p", false, false, false));
			expected.allocateNode(NodeType.THIS_EXPRESSION, 52, 56);
			expected.allocateNode(NodeType.RETURN_STATEMENT, 45, 57);
			expected.allocateNode(NodeType.BLOCK, 41, 60);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 16, 60);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 74, 80);
			expected.allocateParameterDeclaration(74, 82, new ParameterAttribute("m", false, false, false));
			expected.allocateNode(NodeType.THIS_EXPRESSION, 96, 100);
			expected.allocateNode(NodeType.RETURN_STATEMENT, 89, 101);
			expected.allocateNode(NodeType.BLOCK, 85, 104);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 63, 104);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 120, 126);
			expected.allocateParameterDeclaration(120, 128, new ParameterAttribute("c", false, false, false));
			expected.allocateNode(NodeType.BLOCK, 131, 135);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 107, 135);
			expected.allocateClassDeclaration(0, 137, new TypeDeclarationAttribute("Stream"));
			expected.allocateNode(NodeType.QUALIFIED_NAME, 165, 171);
			expected.allocateParameterDeclaration(165, 177, new ParameterAttribute("names", false, false, false));
			expected.allocateNode(NodeType.QUALIFIED_NAME, 184, 200);
			expected.allocateNode(NodeType.IDENTIFIER, 184, 189);
			expected.allocateNode(NodeType.FIELD_ACCESS, 184, 200);
			expected.allocateNode(NodeType.IDENTIFIER, 201, 207);
			expected.allocateNode(NodeType.METHOD_REFERENCE, 201, 216);
			expected.allocateNode(NodeType.METHOD_INVOCATION, 184, 217);
			expected.allocateNode(NodeType.FIELD_ACCESS, 184, 225);
			expected.allocateNode(NodeType.IDENTIFIER, 226, 232);
			expected.allocateNode(NodeType.METHOD_REFERENCE, 226, 245);
			expected.allocateNode(NodeType.METHOD_INVOCATION, 184, 246);
			expected.allocateNode(NodeType.FIELD_ACCESS, 184, 258);
			expected.allocateNode(NodeType.IDENTIFIER, 259, 265);
			expected.allocateNode(NodeType.METHOD_REFERENCE, 259, 275);
			expected.allocateNode(NodeType.METHOD_INVOCATION, 184, 276);
			expected.allocateNode(NodeType.BLOCK, 180, 280);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 153, 280);
			expected.allocateClassDeclaration(139, 282, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 283);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Tests comparator creation using method references.
	 * The {@code Comparator.comparing} method commonly takes method references for key extraction.
	 */
	@Test
	public void testRealWorldComparatorCreation()
	{
		String source = """
			class Person
			{
				String getName()
				{
					return null;
				}
			}

			class Comparator
			{
				static Object comparing(Object keyExtractor)
				{
					return null;
				}
			}

			class Test
			{
				void method()
				{
					Object c = Comparator.comparing(Person::getName);
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.NULL_LITERAL, 45, 49);
			expected.allocateNode(NodeType.RETURN_STATEMENT, 38, 50);
			expected.allocateNode(NodeType.BLOCK, 34, 53);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 16, 53);
			expected.allocateClassDeclaration(0, 55, new TypeDeclarationAttribute("Person"));
			expected.allocateNode(NodeType.QUALIFIED_NAME, 101, 107);
			expected.allocateParameterDeclaration(101, 120,
				new ParameterAttribute("keyExtractor", false, false, false));
			expected.allocateNode(NodeType.NULL_LITERAL, 134, 138);
			expected.allocateNode(NodeType.RETURN_STATEMENT, 127, 139);
			expected.allocateNode(NodeType.BLOCK, 123, 142);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 77, 142);
			expected.allocateClassDeclaration(57, 144, new TypeDeclarationAttribute("Comparator"));
			expected.allocateNode(NodeType.QUALIFIED_NAME, 179, 185);
			expected.allocateNode(NodeType.IDENTIFIER, 190, 200);
			expected.allocateNode(NodeType.FIELD_ACCESS, 190, 210);
			expected.allocateNode(NodeType.IDENTIFIER, 211, 217);
			expected.allocateNode(NodeType.METHOD_REFERENCE, 211, 226);
			expected.allocateNode(NodeType.METHOD_INVOCATION, 190, 227);
			expected.allocateNode(NodeType.BLOCK, 175, 231);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 160, 231);
			expected.allocateClassDeclaration(146, 233, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 234);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Tests collector with method references for grouping.
	 * Complex collection operations often use method references with collectors.
	 */
	@Test
	public void testRealWorldCollectorWithMethodReferences()
	{
		String source = """
			class Person
			{
				String getName()
				{
					return null;
				}
			}

			class Stream
			{
				Object collect(Object collector)
				{
					return null;
				}
			}

			class Collectors
			{
				static Object groupingBy(Object classifier)
				{
					return null;
				}
			}

			class Test
			{
				void method(Stream people)
				{
					Object grouped = people.collect(Collectors.groupingBy(Person::getName));
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.NULL_LITERAL, 45, 49);
			expected.allocateNode(NodeType.RETURN_STATEMENT, 38, 50);
			expected.allocateNode(NodeType.BLOCK, 34, 53);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 16, 53);
			expected.allocateClassDeclaration(0, 55, new TypeDeclarationAttribute("Person"));
			expected.allocateNode(NodeType.QUALIFIED_NAME, 88, 94);
			expected.allocateParameterDeclaration(88, 104, new ParameterAttribute("collector", false, false, false));
			expected.allocateNode(NodeType.NULL_LITERAL, 118, 122);
			expected.allocateNode(NodeType.RETURN_STATEMENT, 111, 123);
			expected.allocateNode(NodeType.BLOCK, 107, 126);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 73, 126);
			expected.allocateClassDeclaration(57, 128, new TypeDeclarationAttribute("Stream"));
			expected.allocateNode(NodeType.QUALIFIED_NAME, 175, 181);
			expected.allocateParameterDeclaration(175, 192, new ParameterAttribute("classifier", false, false, false));
			expected.allocateNode(NodeType.NULL_LITERAL, 206, 210);
			expected.allocateNode(NodeType.RETURN_STATEMENT, 199, 211);
			expected.allocateNode(NodeType.BLOCK, 195, 214);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 150, 214);
			expected.allocateClassDeclaration(130, 216, new TypeDeclarationAttribute("Collectors"));
			expected.allocateNode(NodeType.QUALIFIED_NAME, 244, 250);
			expected.allocateParameterDeclaration(244, 257, new ParameterAttribute("people", false, false, false));
			expected.allocateNode(NodeType.QUALIFIED_NAME, 264, 270);
			expected.allocateNode(NodeType.IDENTIFIER, 281, 287);
			expected.allocateNode(NodeType.FIELD_ACCESS, 281, 295);
			expected.allocateNode(NodeType.IDENTIFIER, 296, 306);
			expected.allocateNode(NodeType.FIELD_ACCESS, 296, 317);
			expected.allocateNode(NodeType.IDENTIFIER, 318, 324);
			expected.allocateNode(NodeType.METHOD_REFERENCE, 318, 333);
			expected.allocateNode(NodeType.METHOD_INVOCATION, 296, 334);
			expected.allocateNode(NodeType.METHOD_INVOCATION, 281, 335);
			expected.allocateNode(NodeType.BLOCK, 260, 339);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 232, 339);
			expected.allocateClassDeclaration(218, 341, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 342);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Tests Optional operations with method references.
	 * Method references work naturally with Optional's functional methods.
	 */
	@Test
	public void testRealWorldOptionalWithMethodReferences()
	{
		String source = """
			class Optional
			{
				Optional map(Object mapper)
				{
					return this;
				}

				Optional filter(Object predicate)
				{
					return this;
				}

				void ifPresent(Object consumer)
				{
				}

				static Optional of(Object value)
				{
					return null;
				}
			}

			class Test
			{
				void method()
				{
					Optional opt = Optional.of("test");
					opt.map(String::toUpperCase)
						.filter(String::isEmpty)
						.ifPresent(Object::hashCode);
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 31, 37);
			expected.allocateParameterDeclaration(31, 44, new ParameterAttribute("mapper", false, false, false));
			expected.allocateNode(NodeType.THIS_EXPRESSION, 58, 62);
			expected.allocateNode(NodeType.RETURN_STATEMENT, 51, 63);
			expected.allocateNode(NodeType.BLOCK, 47, 66);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 18, 66);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 85, 91);
			expected.allocateParameterDeclaration(85, 101, new ParameterAttribute("predicate", false, false, false));
			expected.allocateNode(NodeType.THIS_EXPRESSION, 115, 119);
			expected.allocateNode(NodeType.RETURN_STATEMENT, 108, 120);
			expected.allocateNode(NodeType.BLOCK, 104, 123);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 69, 123);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 141, 147);
			expected.allocateParameterDeclaration(141, 156, new ParameterAttribute("consumer", false, false, false));
			expected.allocateNode(NodeType.BLOCK, 159, 163);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 126, 163);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 185, 191);
			expected.allocateParameterDeclaration(185, 197, new ParameterAttribute("value", false, false, false));
			expected.allocateNode(NodeType.NULL_LITERAL, 211, 215);
			expected.allocateNode(NodeType.RETURN_STATEMENT, 204, 216);
			expected.allocateNode(NodeType.BLOCK, 200, 219);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 166, 219);
			expected.allocateClassDeclaration(0, 221, new TypeDeclarationAttribute("Optional"));
			expected.allocateNode(NodeType.QUALIFIED_NAME, 256, 264);
			expected.allocateNode(NodeType.IDENTIFIER, 271, 279);
			expected.allocateNode(NodeType.FIELD_ACCESS, 271, 282);
			expected.allocateNode(NodeType.STRING_LITERAL, 283, 289);
			expected.allocateNode(NodeType.METHOD_INVOCATION, 271, 290);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 294, 301);
			expected.allocateNode(NodeType.IDENTIFIER, 294, 297);
			expected.allocateNode(NodeType.FIELD_ACCESS, 294, 301);
			expected.allocateNode(NodeType.IDENTIFIER, 302, 308);
			expected.allocateNode(NodeType.METHOD_REFERENCE, 302, 321);
			expected.allocateNode(NodeType.METHOD_INVOCATION, 294, 322);
			expected.allocateNode(NodeType.FIELD_ACCESS, 294, 333);
			expected.allocateNode(NodeType.IDENTIFIER, 334, 340);
			expected.allocateNode(NodeType.METHOD_REFERENCE, 334, 349);
			expected.allocateNode(NodeType.METHOD_INVOCATION, 294, 350);
			expected.allocateNode(NodeType.FIELD_ACCESS, 294, 364);
			expected.allocateNode(NodeType.IDENTIFIER, 365, 371);
			expected.allocateNode(NodeType.METHOD_REFERENCE, 365, 381);
			expected.allocateNode(NodeType.METHOD_INVOCATION, 294, 382);
			expected.allocateNode(NodeType.BLOCK, 252, 386);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 237, 386);
			expected.allocateClassDeclaration(223, 388, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 389);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}
}
