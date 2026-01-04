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
 * Tests for parsing explicit type arguments on method and constructor calls.
 * Explicit type arguments are specified using the {@code <Type>} syntax before the method name,
 * such as {@code Collections.<String>emptyList()} or {@code new <String>Container()}.
 */
public final class ExplicitTypeArgumentParserTest
{
	// ========================================
	// Method Invocation Type Arguments (7 tests)
	// ========================================

	/**
	 * Tests parsing of explicit type argument on a static method call.
	 * Syntax: {@code Collections.<String>emptyList()}.
	 */
	@Test
	public void staticMethodWithSingleTypeArgument()
	{
		String source = """
			class T
			{
				void m()
				{
					Collections.<String>emptyList();
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 25, 37);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 38, 44);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 38, 44);
			expected.allocateNode(NodeType.PARAMETERIZED_TYPE, 25, 45);
			expected.allocateNode(NodeType.IDENTIFIER, 25, 36);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 38, 44);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 38, 44);
			expected.allocateNode(NodeType.FIELD_ACCESS, 25, 54);
			expected.allocateNode(NodeType.METHOD_INVOCATION, 25, 56);
			expected.allocateNode(NodeType.BLOCK, 21, 60);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 11, 60);
			expected.allocateClassDeclaration(0, 62, new TypeDeclarationAttribute("T"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 63);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Tests parsing of explicit type argument with {@code this} as receiver.
	 * Syntax: {@code this.<T>genericMethod()}.
	 */
	@Test
	public void thisReceiverWithTypeArgument()
	{
		String source = """
			class T
			{
				void m()
				{
					this.<String>genericMethod();
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.THIS_EXPRESSION, 25, 29);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 31, 37);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 31, 37);
			expected.allocateNode(NodeType.FIELD_ACCESS, 25, 51);
			expected.allocateNode(NodeType.METHOD_INVOCATION, 25, 53);
			expected.allocateNode(NodeType.BLOCK, 21, 57);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 11, 57);
			expected.allocateClassDeclaration(0, 59, new TypeDeclarationAttribute("T"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 60);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Tests parsing of multiple type arguments on a method call.
	 * Syntax: {@code obj.<String, Integer>method()}.
	 */
	@Test
	public void multipleTypeArguments()
	{
		String source = """
			class T
			{
				void m()
				{
					obj.<String, Integer>method();
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 25, 29);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 30, 36);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 30, 36);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 38, 45);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 38, 45);
			expected.allocateNode(NodeType.PARAMETERIZED_TYPE, 25, 46);
			expected.allocateNode(NodeType.IDENTIFIER, 25, 28);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 30, 36);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 30, 36);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 38, 45);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 38, 45);
			expected.allocateNode(NodeType.FIELD_ACCESS, 25, 52);
			expected.allocateNode(NodeType.METHOD_INVOCATION, 25, 54);
			expected.allocateNode(NodeType.BLOCK, 21, 58);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 11, 58);
			expected.allocateClassDeclaration(0, 60, new TypeDeclarationAttribute("T"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 61);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Tests parsing of nested generic type argument.
	 * Syntax: {@code obj.<List<String>>method()}.
	 */
	@Test
	public void nestedGenericTypeArgument()
	{
		String source = """
			class T
			{
				void m()
				{
					obj.<List<String>>method();
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 25, 29);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 30, 34);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 35, 41);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 35, 41);
			expected.allocateNode(NodeType.PARAMETERIZED_TYPE, 30, 43);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 30, 43);
			expected.allocateNode(NodeType.PARAMETERIZED_TYPE, 25, 43);
			expected.allocateNode(NodeType.IDENTIFIER, 25, 28);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 30, 34);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 35, 41);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 35, 41);
			expected.allocateNode(NodeType.PARAMETERIZED_TYPE, 30, 43);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 30, 43);
			expected.allocateNode(NodeType.FIELD_ACCESS, 25, 49);
			expected.allocateNode(NodeType.METHOD_INVOCATION, 25, 51);
			expected.allocateNode(NodeType.BLOCK, 21, 55);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 11, 55);
			expected.allocateClassDeclaration(0, 57, new TypeDeclarationAttribute("T"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 58);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Tests parsing of wildcard type argument.
	 * Syntax: {@code obj.<? extends Number>method()}.
	 */
	@Test
	public void wildcardTypeArgument()
	{
		String source = """
			class T
			{
				void m()
				{
					obj.<? extends Number>method();
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 25, 29);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 40, 46);
			expected.allocateNode(NodeType.WILDCARD_TYPE, 30, 46);
			expected.allocateNode(NodeType.PARAMETERIZED_TYPE, 25, 47);
			expected.allocateNode(NodeType.IDENTIFIER, 25, 28);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 40, 46);
			expected.allocateNode(NodeType.WILDCARD_TYPE, 30, 46);
			expected.allocateNode(NodeType.FIELD_ACCESS, 25, 53);
			expected.allocateNode(NodeType.METHOD_INVOCATION, 25, 55);
			expected.allocateNode(NodeType.BLOCK, 21, 59);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 11, 59);
			expected.allocateClassDeclaration(0, 61, new TypeDeclarationAttribute("T"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 62);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Tests parsing of chained method calls each with type arguments.
	 * Syntax: {@code obj.<String>foo().<Integer>bar()}.
	 */
	@Test
	public void chainedMethodsWithTypeArgs()
	{
		String source = """
			class T
			{
				void m()
				{
					obj.<String>foo().<Integer>bar();
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 25, 29);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 30, 36);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 30, 36);
			expected.allocateNode(NodeType.PARAMETERIZED_TYPE, 25, 37);
			expected.allocateNode(NodeType.IDENTIFIER, 25, 28);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 30, 36);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 30, 36);
			expected.allocateNode(NodeType.FIELD_ACCESS, 25, 40);
			expected.allocateNode(NodeType.METHOD_INVOCATION, 25, 42);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 44, 51);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 44, 51);
			expected.allocateNode(NodeType.FIELD_ACCESS, 25, 55);
			expected.allocateNode(NodeType.METHOD_INVOCATION, 25, 57);
			expected.allocateNode(NodeType.BLOCK, 21, 61);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 11, 61);
			expected.allocateClassDeclaration(0, 63, new TypeDeclarationAttribute("T"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 64);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Tests parsing of explicit type argument with {@code super} as receiver.
	 * Syntax: {@code super.<T>method()}.
	 */
	@Test
	public void superReceiverWithTypeArgument()
	{
		String source = """
			class T
			{
				void m()
				{
					super.<String>method();
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.SUPER_EXPRESSION, 25, 30);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 32, 38);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 32, 38);
			expected.allocateNode(NodeType.FIELD_ACCESS, 25, 45);
			expected.allocateNode(NodeType.METHOD_INVOCATION, 25, 47);
			expected.allocateNode(NodeType.BLOCK, 21, 51);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 11, 51);
			expected.allocateClassDeclaration(0, 53, new TypeDeclarationAttribute("T"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 54);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	// ========================================
	// Constructor Type Arguments (4 tests)
	// ========================================

	/**
	 * Tests parsing of explicit type argument on constructor.
	 * Syntax: {@code new <String>Container()}.
	 */
	@Test
	public void constructorWithSingleTypeArgument()
	{
		String source = """
			class T
			{
				void m()
				{
					Object x = new <String>Container();
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 25, 31);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 41, 47);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 41, 47);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 48, 57);
			expected.allocateNode(NodeType.OBJECT_CREATION, 36, 59);
			expected.allocateNode(NodeType.BLOCK, 21, 63);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 11, 63);
			expected.allocateClassDeclaration(0, 65, new TypeDeclarationAttribute("T"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 66);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Tests parsing of multiple type arguments on constructor.
	 * Syntax: {@code new <String, Integer>Pair()}.
	 */
	@Test
	public void constructorWithMultipleTypeArguments()
	{
		String source = """
			class T
			{
				void m()
				{
					Object x = new <String, Integer>Pair();
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 25, 31);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 41, 47);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 41, 47);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 49, 56);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 49, 56);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 57, 61);
			expected.allocateNode(NodeType.OBJECT_CREATION, 36, 63);
			expected.allocateNode(NodeType.BLOCK, 21, 67);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 11, 67);
			expected.allocateClassDeclaration(0, 69, new TypeDeclarationAttribute("T"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 70);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Tests parsing of constructor with both constructor type args and class type args.
	 * Syntax: {@code new <String>Container<Integer>()}.
	 */
	@Test
	public void constructorTypeArgsWithParameterizedClass()
	{
		String source = """
			class T
			{
				void m()
				{
					Object x = new <String>Container<Integer>();
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 25, 31);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 41, 47);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 41, 47);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 48, 57);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 58, 65);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 58, 65);
			expected.allocateNode(NodeType.PARAMETERIZED_TYPE, 48, 66);
			expected.allocateNode(NodeType.OBJECT_CREATION, 36, 68);
			expected.allocateNode(NodeType.BLOCK, 21, 72);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 11, 72);
			expected.allocateClassDeclaration(0, 74, new TypeDeclarationAttribute("T"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 75);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Tests parsing of nested type argument on constructor.
	 * Syntax: {@code new <List<String>>Wrapper()}.
	 */
	@Test
	public void constructorWithNestedTypeArgument()
	{
		String source = """
			class T
			{
				void m()
				{
					Object x = new <List<String>>Wrapper();
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 25, 31);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 41, 45);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 46, 52);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 46, 52);
			expected.allocateNode(NodeType.PARAMETERIZED_TYPE, 41, 54);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 41, 54);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 54, 61);
			expected.allocateNode(NodeType.OBJECT_CREATION, 36, 63);
			expected.allocateNode(NodeType.BLOCK, 21, 67);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 11, 67);
			expected.allocateClassDeclaration(0, 69, new TypeDeclarationAttribute("T"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 70);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	// ========================================
	// Method Reference Type Arguments (4 tests)
	// ========================================

	/**
	 * Tests parsing of method reference with single type argument.
	 * Syntax: {@code List::<String>of}.
	 */
	@Test
	public void methodReferenceWithSingleTypeArgument()
	{
		String source = """
			class T
			{
				Object f = List::<String>of;
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.IDENTIFIER, 22, 26);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 29, 35);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 29, 35);
			expected.allocateNode(NodeType.METHOD_REFERENCE, 22, 38);
			expected.allocateNode(NodeType.FIELD_DECLARATION, 11, 39);
			expected.allocateClassDeclaration(0, 41, new TypeDeclarationAttribute("T"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 42);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Tests parsing of method reference with multiple type arguments.
	 * Syntax: {@code Util::<String, Integer>convert}.
	 */
	@Test
	public void methodReferenceWithMultipleTypeArguments()
	{
		String source = """
			class T
			{
				Object f = Util::<String, Integer>convert;
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.IDENTIFIER, 22, 26);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 29, 35);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 29, 35);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 37, 44);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 37, 44);
			expected.allocateNode(NodeType.METHOD_REFERENCE, 22, 52);
			expected.allocateNode(NodeType.FIELD_DECLARATION, 11, 53);
			expected.allocateClassDeclaration(0, 55, new TypeDeclarationAttribute("T"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 56);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Tests parsing of static method reference with type argument.
	 * Syntax: {@code Arrays::<String>sort}.
	 */
	@Test
	public void staticMethodReferenceWithTypeArgument()
	{
		String source = """
			class T
			{
				Object f = Arrays::<String>sort;
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.IDENTIFIER, 22, 28);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 31, 37);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 31, 37);
			expected.allocateNode(NodeType.METHOD_REFERENCE, 22, 42);
			expected.allocateNode(NodeType.FIELD_DECLARATION, 11, 43);
			expected.allocateClassDeclaration(0, 45, new TypeDeclarationAttribute("T"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 46);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Tests parsing of qualified method reference with type arguments.
	 * Syntax: {@code java.util.Collections::<String>emptyList}.
	 */
	@Test
	public void qualifiedMethodReferenceWithTypeArgs()
	{
		String source = """
			class T
			{
				Object f = java.util.Collections::<String>emptyList;
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.IDENTIFIER, 22, 26);
			expected.allocateNode(NodeType.FIELD_ACCESS, 22, 31);
			expected.allocateNode(NodeType.FIELD_ACCESS, 22, 43);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 46, 52);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 46, 52);
			expected.allocateNode(NodeType.METHOD_REFERENCE, 22, 62);
			expected.allocateNode(NodeType.FIELD_DECLARATION, 11, 63);
			expected.allocateClassDeclaration(0, 65, new TypeDeclarationAttribute("T"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 66);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	// ========================================
	// Constructor Reference Type Arguments (3 tests)
	// ========================================

	/**
	 * Tests parsing of constructor reference with single type argument.
	 * Syntax: {@code ArrayList::<String>new}.
	 */
	@Test
	public void constructorReferenceWithSingleTypeArgument()
	{
		String source = """
			class T
			{
				Object f = ArrayList::<String>new;
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.IDENTIFIER, 22, 31);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 34, 40);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 34, 40);
			expected.allocateNode(NodeType.METHOD_REFERENCE, 22, 44);
			expected.allocateNode(NodeType.FIELD_DECLARATION, 11, 45);
			expected.allocateClassDeclaration(0, 47, new TypeDeclarationAttribute("T"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 48);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Tests parsing of constructor reference with multiple type arguments.
	 * Syntax: {@code HashMap::<String, Integer>new}.
	 */
	@Test
	public void constructorReferenceWithMultipleTypeArguments()
	{
		String source = """
			class T
			{
				Object f = HashMap::<String, Integer>new;
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.IDENTIFIER, 22, 29);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 32, 38);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 32, 38);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 40, 47);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 40, 47);
			expected.allocateNode(NodeType.METHOD_REFERENCE, 22, 51);
			expected.allocateNode(NodeType.FIELD_DECLARATION, 11, 52);
			expected.allocateClassDeclaration(0, 54, new TypeDeclarationAttribute("T"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 55);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Tests parsing of nested class constructor reference with type arguments.
	 * Syntax: {@code Outer.Inner::<String>new}.
	 */
	@Test
	public void nestedClassConstructorReferenceWithTypeArgs()
	{
		String source = """
			class T
			{
				Object f = Outer.Inner::<String>new;
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.IDENTIFIER, 22, 27);
			expected.allocateNode(NodeType.FIELD_ACCESS, 22, 33);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 36, 42);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 36, 42);
			expected.allocateNode(NodeType.METHOD_REFERENCE, 22, 46);
			expected.allocateNode(NodeType.FIELD_DECLARATION, 11, 47);
			expected.allocateClassDeclaration(0, 49, new TypeDeclarationAttribute("T"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 50);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	// ========================================
	// Edge Cases (4 tests)
	// ========================================

	/**
	 * Tests parsing of diamond operator as distinct from explicit type arguments.
	 * The diamond operator {@code <>} allows type inference, while explicit type args {@code <T>}
	 * specify the type directly.
	 */
	@Test
	public void diamondOperatorDistinctFromExplicitTypeArgs()
	{
		String source = """
			class T
			{
				void m()
				{
					var a = new ArrayList<>();
					var b = obj.<String>method();
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 37, 46);
			expected.allocateNode(NodeType.PARAMETERIZED_TYPE, 37, 48);
			expected.allocateNode(NodeType.OBJECT_CREATION, 33, 50);
			expected.allocateNode(NodeType.IDENTIFIER, 62, 65);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 67, 73);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 67, 73);
			expected.allocateNode(NodeType.FIELD_ACCESS, 62, 80);
			expected.allocateNode(NodeType.METHOD_INVOCATION, 62, 82);
			expected.allocateNode(NodeType.BLOCK, 21, 86);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 11, 86);
			expected.allocateClassDeclaration(0, 88, new TypeDeclarationAttribute("T"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 89);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Tests parsing of deeply nested generic type arguments (3+ levels).
	 * Syntax: {@code obj.<Map<String, List<Integer>>>method()}.
	 */
	@Test
	public void deeplyNestedGenericTypeArguments()
	{
		String source = """
			class T
			{
				void m()
				{
					obj.<Map<String, List<Integer>>>method();
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 25, 29);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 30, 33);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 34, 40);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 34, 40);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 42, 46);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 47, 54);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 47, 54);
			expected.allocateNode(NodeType.PARAMETERIZED_TYPE, 42, 57);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 42, 57);
			expected.allocateNode(NodeType.PARAMETERIZED_TYPE, 30, 57);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 30, 57);
			expected.allocateNode(NodeType.PARAMETERIZED_TYPE, 25, 57);
			expected.allocateNode(NodeType.IDENTIFIER, 25, 28);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 30, 33);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 34, 40);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 34, 40);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 42, 46);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 47, 54);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 47, 54);
			expected.allocateNode(NodeType.PARAMETERIZED_TYPE, 42, 57);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 42, 57);
			expected.allocateNode(NodeType.PARAMETERIZED_TYPE, 30, 57);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 30, 57);
			expected.allocateNode(NodeType.FIELD_ACCESS, 25, 63);
			expected.allocateNode(NodeType.METHOD_INVOCATION, 25, 65);
			expected.allocateNode(NodeType.BLOCK, 21, 69);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 11, 69);
			expected.allocateClassDeclaration(0, 71, new TypeDeclarationAttribute("T"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 72);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Tests parsing of type arguments in ternary expression branches.
	 * Type arguments work correctly in both branches of a conditional expression.
	 */
	@Test
	public void typeArgsInExpressionContexts()
	{
		String source = """
			class T
			{
				void m(boolean f)
				{
					Object x = f ? obj.<String>a() : obj.<Integer>b();
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateParameterDeclaration(18, 27, new ParameterAttribute("f", false, false, false));
			expected.allocateNode(NodeType.QUALIFIED_NAME, 34, 40);
			expected.allocateNode(NodeType.IDENTIFIER, 45, 46);
			expected.allocateNode(NodeType.IDENTIFIER, 49, 52);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 54, 60);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 54, 60);
			expected.allocateNode(NodeType.FIELD_ACCESS, 49, 62);
			expected.allocateNode(NodeType.METHOD_INVOCATION, 49, 64);
			expected.allocateNode(NodeType.IDENTIFIER, 67, 70);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 72, 79);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 72, 79);
			expected.allocateNode(NodeType.FIELD_ACCESS, 67, 81);
			expected.allocateNode(NodeType.METHOD_INVOCATION, 67, 83);
			expected.allocateNode(NodeType.CONDITIONAL_EXPRESSION, 45, 83);
			expected.allocateNode(NodeType.BLOCK, 30, 87);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 11, 87);
			expected.allocateClassDeclaration(0, 89, new TypeDeclarationAttribute("T"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 90);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Tests parsing of type arguments in a return statement.
	 * Type arguments work correctly in return expressions.
	 */
	@Test
	public void typeArgsInReturnStatement()
	{
		String source = """
			class T
			{
				Object m()
				{
					return Collections.<String>emptyList();
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.IDENTIFIER, 34, 45);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 47, 53);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 47, 53);
			expected.allocateNode(NodeType.FIELD_ACCESS, 34, 63);
			expected.allocateNode(NodeType.METHOD_INVOCATION, 34, 65);
			expected.allocateNode(NodeType.RETURN_STATEMENT, 27, 66);
			expected.allocateNode(NodeType.BLOCK, 23, 69);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 11, 69);
			expected.allocateClassDeclaration(0, 71, new TypeDeclarationAttribute("T"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 72);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	// ========================================
	// Error Cases (3 tests)
	// ========================================

	/**
	 * Tests that malformed syntax with missing closing angle bracket fails.
	 */
	@Test
	public void malformedMissingClosingAngleBracket()
	{
		assertParseFails("""
			class T
			{
				void m()
				{
					obj.<Stringmethod();
				}
			}
			""");
	}

	/**
	 * Tests that type arguments after method name fails.
	 */
	@Test
	public void malformedTypeArgsAfterMethodName()
	{
		assertParseFails("""
			class T
			{
				void m()
				{
					obj.method<String>();
				}
			}
			""");
	}

	/**
	 * Tests that double angle brackets fail.
	 */
	@Test
	public void malformedDoubleAngleBrackets()
	{
		assertParseFails("""
			class T
			{
				void m()
				{
					obj.<<String>>method();
				}
			}
			""");
	}
}
