package io.github.cowwoc.styler.parser.test;

import io.github.cowwoc.styler.ast.core.ImportAttribute;
import io.github.cowwoc.styler.ast.core.NodeArena;
import io.github.cowwoc.styler.ast.core.NodeType;
import io.github.cowwoc.styler.ast.core.ParameterAttribute;
import io.github.cowwoc.styler.ast.core.TypeDeclarationAttribute;
import io.github.cowwoc.styler.parser.Parser;
import org.testng.annotations.Test;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;
import static io.github.cowwoc.styler.parser.test.ParserTestUtils.parse;

/**
 * Tests for parsing anonymous inner class declarations.
 */
public final class AnonymousInnerClassParserTest
{
	/**
	 * Validates that an empty anonymous class parses correctly.
	 * Tests the form {@code new Object() { }}.
	 */
	@Test
	public void testEmptyAnonymousClass()
	{
		String source = """
			class Test
			{
				void m()
				{
					new Object()
					{
					};
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 32, 38);
			expected.allocateNode(NodeType.OBJECT_CREATION, 28, 48);
			expected.allocateNode(NodeType.BLOCK, 24, 52);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 14, 52);
			expected.allocateClassDeclaration(0, 54, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 55);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates that an anonymous class with a method override parses correctly.
	 * Tests overriding a method with the {@code @Override} annotation.
	 */
	@Test
	public void testAnonymousClassWithMethodOverride()
	{
		String source = """
			class Test
			{
				Runnable r = new Runnable()
				{
					@Override
					public void run()
					{
					}
				};
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 31, 39);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 48, 56);
			expected.allocateNode(NodeType.ANNOTATION, 47, 56);
			expected.allocateNode(NodeType.BLOCK, 79, 84);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 47, 84);
			expected.allocateNode(NodeType.OBJECT_CREATION, 27, 87);
			expected.allocateNode(NodeType.FIELD_DECLARATION, 14, 88);
			expected.allocateClassDeclaration(0, 90, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 91);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates that an anonymous class with a field declaration parses correctly.
	 * Tests declaring a field inside the anonymous class body.
	 */
	@Test
	public void testAnonymousClassWithField()
	{
		String source = """
			class Test
			{
				void m()
				{
					new Object()
					{
						private int value = 42;
					};
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 32, 38);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 68, 70);
			expected.allocateNode(NodeType.FIELD_DECLARATION, 48, 71);
			expected.allocateNode(NodeType.OBJECT_CREATION, 28, 75);
			expected.allocateNode(NodeType.BLOCK, 24, 79);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 14, 79);
			expected.allocateClassDeclaration(0, 81, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 82);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates that an anonymous class with multiple members parses correctly.
	 * Tests having both fields and methods in the anonymous class body.
	 */
	@Test
	public void testAnonymousClassWithMultipleMembers()
	{
		String source = """
			class Test
			{
				void m()
				{
					new Object()
					{
						private int count = 0;

						public void increment()
						{
							count++;
						}
					};
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 32, 38);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 68, 69);
			expected.allocateNode(NodeType.FIELD_DECLARATION, 48, 70);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 108, 113);
			expected.allocateNode(NodeType.IDENTIFIER, 108, 113);
			expected.allocateNode(NodeType.POSTFIX_EXPRESSION, 108, 115);
			expected.allocateNode(NodeType.BLOCK, 102, 121);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 75, 121);
			expected.allocateNode(NodeType.OBJECT_CREATION, 28, 125);
			expected.allocateNode(NodeType.BLOCK, 24, 129);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 14, 129);
			expected.allocateClassDeclaration(0, 131, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 132);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates that an anonymous class with a single constructor argument parses correctly.
	 * Tests the form {@code new Thread("name") { }}.
	 */
	@Test
	public void testAnonymousClassWithSingleConstructorArg()
	{
		String source = """
			class Test
			{
				void m()
				{
					new Thread("worker")
					{
						@Override
						public void run()
						{
						}
					};
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 32, 38);
			expected.allocateNode(NodeType.STRING_LITERAL, 39, 47);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 57, 65);
			expected.allocateNode(NodeType.ANNOTATION, 56, 65);
			expected.allocateNode(NodeType.BLOCK, 90, 96);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 56, 96);
			expected.allocateNode(NodeType.OBJECT_CREATION, 28, 100);
			expected.allocateNode(NodeType.BLOCK, 24, 104);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 14, 104);
			expected.allocateClassDeclaration(0, 106, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 107);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates that an anonymous class with multiple constructor arguments parses correctly.
	 * Tests passing multiple arguments to the superclass constructor.
	 */
	@Test
	public void testAnonymousClassWithMultipleConstructorArgs()
	{
		String source = """
			class Test
			{
				void m()
				{
					new Thread(null, null, "name", 1024)
					{
					};
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 32, 38);
			expected.allocateNode(NodeType.NULL_LITERAL, 39, 43);
			expected.allocateNode(NodeType.NULL_LITERAL, 45, 49);
			expected.allocateNode(NodeType.STRING_LITERAL, 51, 57);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 59, 63);
			expected.allocateNode(NodeType.OBJECT_CREATION, 28, 72);
			expected.allocateNode(NodeType.BLOCK, 24, 76);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 14, 76);
			expected.allocateClassDeclaration(0, 78, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 79);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates that an anonymous class with diamond operator parses correctly.
	 * Tests the form {@code new ArrayList<>() { }}.
	 */
	@Test
	public void testAnonymousClassWithDiamondOperator()
	{
		String source = """
			import java.util.ArrayList;
			import java.util.List;

			class Test
			{
				void m()
				{
					List<String> list = new ArrayList<>()
					{
					};
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateImportDeclaration(0, 27, new ImportAttribute("java.util.ArrayList", false));
			expected.allocateImportDeclaration(28, 50, new ImportAttribute("java.util.List", false));
			expected.allocateNode(NodeType.QUALIFIED_NAME, 80, 84);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 85, 91);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 85, 91);
			expected.allocateNode(NodeType.PARAMETERIZED_TYPE, 80, 92);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 104, 113);
			expected.allocateNode(NodeType.PARAMETERIZED_TYPE, 104, 115);
			expected.allocateNode(NodeType.OBJECT_CREATION, 100, 125);
			expected.allocateNode(NodeType.BLOCK, 76, 129);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 66, 129);
			expected.allocateClassDeclaration(52, 131, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 132);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates that an anonymous class with explicit type arguments parses correctly.
	 * Tests the form {@code new Comparator<String>() { }}.
	 */
	@Test
	public void testAnonymousClassWithExplicitTypeArguments()
	{
		String source = """
			import java.util.Comparator;

			class Test
			{
				void m()
				{
					Comparator<String> comp = new Comparator<String>()
					{
						@Override
						public int compare(String a, String b)
						{
							return 0;
						}
					};
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateImportDeclaration(0, 28, new ImportAttribute("java.util.Comparator", false));
			expected.allocateNode(NodeType.QUALIFIED_NAME, 58, 68);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 69, 75);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 69, 75);
			expected.allocateNode(NodeType.PARAMETERIZED_TYPE, 58, 76);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 88, 98);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 99, 105);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 99, 105);
			expected.allocateNode(NodeType.PARAMETERIZED_TYPE, 88, 106);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 117, 125);
			expected.allocateNode(NodeType.ANNOTATION, 116, 125);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 148, 154);
			expected.allocateParameterDeclaration(148, 156, new ParameterAttribute("a", false, false, false));
			expected.allocateNode(NodeType.QUALIFIED_NAME, 158, 164);
			expected.allocateParameterDeclaration(158, 166, new ParameterAttribute("b", false, false, false));
			expected.allocateNode(NodeType.INTEGER_LITERAL, 184, 185);
			expected.allocateNode(NodeType.RETURN_STATEMENT, 177, 186);
			expected.allocateNode(NodeType.BLOCK, 171, 191);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 116, 191);
			expected.allocateNode(NodeType.OBJECT_CREATION, 84, 195);
			expected.allocateNode(NodeType.BLOCK, 54, 199);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 44, 199);
			expected.allocateClassDeclaration(30, 201, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 202);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates that nested anonymous classes parse correctly.
	 * Tests an anonymous class containing another anonymous class.
	 */
	@Test
	public void testNestedAnonymousClasses()
	{
		String source = """
			class Test
			{
				void m()
				{
					new Object()
					{
						void inner()
						{
							new Object()
							{
							};
						}
					};
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 32, 38);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 74, 80);
			expected.allocateNode(NodeType.OBJECT_CREATION, 70, 94);
			expected.allocateNode(NodeType.BLOCK, 64, 100);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 48, 100);
			expected.allocateNode(NodeType.OBJECT_CREATION, 28, 104);
			expected.allocateNode(NodeType.BLOCK, 24, 108);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 14, 108);
			expected.allocateClassDeclaration(0, 110, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 111);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates that an anonymous class as a method argument parses correctly.
	 * Tests passing an anonymous class directly to a method call.
	 */
	@Test
	public void testAnonymousClassAsMethodArgument()
	{
		String source = """
			class Test
			{
				void m()
				{
					execute(new Runnable()
					{
						@Override
						public void run()
						{
						}
					});
				}

				void execute(Runnable r)
				{
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 28, 35);
			expected.allocateNode(NodeType.IDENTIFIER, 28, 35);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 40, 48);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 59, 67);
			expected.allocateNode(NodeType.ANNOTATION, 58, 67);
			expected.allocateNode(NodeType.BLOCK, 92, 98);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 58, 98);
			expected.allocateNode(NodeType.OBJECT_CREATION, 36, 102);
			expected.allocateNode(NodeType.METHOD_INVOCATION, 28, 103);
			expected.allocateNode(NodeType.BLOCK, 24, 107);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 14, 107);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 123, 131);
			expected.allocateParameterDeclaration(123, 133, new ParameterAttribute("r", false, false, false));
			expected.allocateNode(NodeType.BLOCK, 136, 140);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 110, 140);
			expected.allocateClassDeclaration(0, 142, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 143);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates that an anonymous class in a return statement parses correctly.
	 * Tests returning an anonymous class from a method.
	 */
	@Test
	public void testAnonymousClassInReturnStatement()
	{
		String source = """
			class Test
			{
				Runnable createRunner()
				{
					return new Runnable()
					{
						@Override
						public void run()
						{
						}
					};
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 54, 62);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 73, 81);
			expected.allocateNode(NodeType.ANNOTATION, 72, 81);
			expected.allocateNode(NodeType.BLOCK, 106, 112);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 72, 112);
			expected.allocateNode(NodeType.OBJECT_CREATION, 50, 116);
			expected.allocateNode(NodeType.RETURN_STATEMENT, 43, 117);
			expected.allocateNode(NodeType.BLOCK, 39, 120);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 14, 120);
			expected.allocateClassDeclaration(0, 122, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 123);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates that an anonymous class as a field initializer parses correctly.
	 * Tests assigning an anonymous class to an instance field.
	 */
	@Test
	public void testAnonymousClassAsFieldInitializer()
	{
		String source = """
			class Test
			{
				private Runnable runner = new Runnable()
				{
					@Override
					public void run()
					{
					}
				};
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 44, 52);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 61, 69);
			expected.allocateNode(NodeType.ANNOTATION, 60, 69);
			expected.allocateNode(NodeType.BLOCK, 92, 97);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 60, 97);
			expected.allocateNode(NodeType.OBJECT_CREATION, 40, 100);
			expected.allocateNode(NodeType.FIELD_DECLARATION, 14, 101);
			expected.allocateClassDeclaration(0, 103, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 104);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates that an anonymous class with a static initializer parses correctly.
	 * Tests using a static initializer block inside an anonymous class.
	 */
	@Test
	public void testAnonymousClassWithStaticInitializer()
	{
		String source = """
			class Test
			{
				void m()
				{
					new Object()
					{
						static
						{
							System.out.println("Static init");
						}
					};
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 32, 38);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 64, 82);
			expected.allocateNode(NodeType.IDENTIFIER, 64, 70);
			expected.allocateNode(NodeType.FIELD_ACCESS, 64, 74);
			expected.allocateNode(NodeType.FIELD_ACCESS, 64, 82);
			expected.allocateNode(NodeType.STRING_LITERAL, 83, 96);
			expected.allocateNode(NodeType.METHOD_INVOCATION, 64, 97);
			expected.allocateNode(NodeType.BLOCK, 58, 103);
			expected.allocateNode(NodeType.OBJECT_CREATION, 28, 107);
			expected.allocateNode(NodeType.BLOCK, 24, 111);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 14, 111);
			expected.allocateClassDeclaration(0, 113, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 114);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates that an anonymous class with an instance initializer parses correctly.
	 * Tests using an instance initializer block inside an anonymous class.
	 */
	@Test
	public void testAnonymousClassWithInstanceInitializer()
	{
		String source = """
			class Test
			{
				void m()
				{
					new Object()
					{
						{
							System.out.println("Instance init");
						}
					};
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 32, 38);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 54, 72);
			expected.allocateNode(NodeType.IDENTIFIER, 54, 60);
			expected.allocateNode(NodeType.FIELD_ACCESS, 54, 64);
			expected.allocateNode(NodeType.FIELD_ACCESS, 54, 72);
			expected.allocateNode(NodeType.STRING_LITERAL, 73, 88);
			expected.allocateNode(NodeType.METHOD_INVOCATION, 54, 89);
			expected.allocateNode(NodeType.BLOCK, 48, 95);
			expected.allocateNode(NodeType.OBJECT_CREATION, 28, 99);
			expected.allocateNode(NodeType.BLOCK, 24, 103);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 14, 103);
			expected.allocateClassDeclaration(0, 105, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 106);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates that an anonymous class containing an inner class parses correctly.
	 * Tests declaring a named inner class inside an anonymous class body.
	 */
	@Test
	public void testAnonymousClassWithInnerClass()
	{
		String source = """
			class Test
			{
				void m()
				{
					new Object()
					{
						class Inner
						{
							void doSomething()
							{
							}
						}
					};
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 32, 38);
			expected.allocateNode(NodeType.BLOCK, 92, 99);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 69, 99);
			expected.allocateClassDeclaration(48, 104, new TypeDeclarationAttribute("Inner"));
			expected.allocateNode(NodeType.OBJECT_CREATION, 28, 108);
			expected.allocateNode(NodeType.BLOCK, 24, 112);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 14, 112);
			expected.allocateClassDeclaration(0, 114, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 115);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates that an anonymous class implementing a multi-method interface parses correctly.
	 * Tests implementing multiple methods from the same interface.
	 */
	@Test
	public void testAnonymousClassImplementingMultipleMethodInterface()
	{
		String source = """
			import java.util.EventListener;

			class Test
			{
				void m()
				{
					new EventListener()
					{
						public void eventOccurred()
						{
						}

						public void eventProcessed()
						{
						}
					};
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateImportDeclaration(0, 31, new ImportAttribute("java.util.EventListener", false));
			expected.allocateNode(NodeType.QUALIFIED_NAME, 65, 78);
			expected.allocateNode(NodeType.BLOCK, 119, 125);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 88, 125);
			expected.allocateNode(NodeType.BLOCK, 162, 168);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 130, 168);
			expected.allocateNode(NodeType.OBJECT_CREATION, 61, 172);
			expected.allocateNode(NodeType.BLOCK, 57, 176);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 47, 176);
			expected.allocateClassDeclaration(33, 178, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 179);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates that an anonymous class with complex constructor arguments parses correctly.
	 * Tests passing expressions and method calls as constructor arguments.
	 */
	@Test
	public void testAnonymousClassWithComplexConstructorArgs()
	{
		String source = """
			import java.io.File;

			class Test
			{
				void m()
				{
					new File(System.getProperty("user.home") + "/test")
					{
						@Override
						public String getName()
						{
							return "custom";
						}
					};
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateImportDeclaration(0, 20, new ImportAttribute("java.io.File", false));
			expected.allocateNode(NodeType.QUALIFIED_NAME, 54, 58);
			expected.allocateNode(NodeType.IDENTIFIER, 59, 65);
			expected.allocateNode(NodeType.FIELD_ACCESS, 59, 77);
			expected.allocateNode(NodeType.STRING_LITERAL, 78, 89);
			expected.allocateNode(NodeType.METHOD_INVOCATION, 59, 90);
			expected.allocateNode(NodeType.STRING_LITERAL, 93, 100);
			expected.allocateNode(NodeType.BINARY_EXPRESSION, 59, 100);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 110, 118);
			expected.allocateNode(NodeType.ANNOTATION, 109, 118);
			expected.allocateNode(NodeType.STRING_LITERAL, 162, 170);
			expected.allocateNode(NodeType.RETURN_STATEMENT, 155, 171);
			expected.allocateNode(NodeType.BLOCK, 149, 176);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 109, 176);
			expected.allocateNode(NodeType.OBJECT_CREATION, 50, 180);
			expected.allocateNode(NodeType.BLOCK, 46, 184);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 36, 184);
			expected.allocateClassDeclaration(22, 186, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 187);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates that an anonymous class with annotated members parses correctly.
	 * Tests using annotations on methods and fields inside the anonymous class.
	 */
	@Test
	public void testAnonymousClassWithAnnotatedMembers()
	{
		String source = """
			class Test
			{
				void m()
				{
					new Object()
					{
						@SuppressWarnings("unused")
						private int field = 0;

						@Override
						public String toString()
						{
							return "custom";
						}
					};
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 32, 38);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 49, 65);
			expected.allocateNode(NodeType.STRING_LITERAL, 66, 74);
			expected.allocateNode(NodeType.ANNOTATION, 48, 75);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 99, 100);
			expected.allocateNode(NodeType.FIELD_DECLARATION, 48, 101);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 107, 115);
			expected.allocateNode(NodeType.ANNOTATION, 106, 115);
			expected.allocateNode(NodeType.STRING_LITERAL, 160, 168);
			expected.allocateNode(NodeType.RETURN_STATEMENT, 153, 169);
			expected.allocateNode(NodeType.BLOCK, 147, 174);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 106, 174);
			expected.allocateNode(NodeType.OBJECT_CREATION, 28, 178);
			expected.allocateNode(NodeType.BLOCK, 24, 182);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 14, 182);
			expected.allocateClassDeclaration(0, 184, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 185);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates that an anonymous class extending an abstract class parses correctly.
	 * Tests implementing abstract methods from a parent class.
	 */
	@Test
	public void testAnonymousClassExtendingAbstractClass()
	{
		String source = """
			import java.util.TimerTask;

			class Test
			{
				void m()
				{
					new TimerTask()
					{
						@Override
						public void run()
						{
							System.out.println("Task executed");
						}
					};
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateImportDeclaration(0, 27, new ImportAttribute("java.util.TimerTask", false));
			expected.allocateNode(NodeType.QUALIFIED_NAME, 61, 70);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 81, 89);
			expected.allocateNode(NodeType.ANNOTATION, 80, 89);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 120, 138);
			expected.allocateNode(NodeType.IDENTIFIER, 120, 126);
			expected.allocateNode(NodeType.FIELD_ACCESS, 120, 130);
			expected.allocateNode(NodeType.FIELD_ACCESS, 120, 138);
			expected.allocateNode(NodeType.STRING_LITERAL, 139, 154);
			expected.allocateNode(NodeType.METHOD_INVOCATION, 120, 155);
			expected.allocateNode(NodeType.BLOCK, 114, 161);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 80, 161);
			expected.allocateNode(NodeType.OBJECT_CREATION, 57, 165);
			expected.allocateNode(NodeType.BLOCK, 53, 169);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 43, 169);
			expected.allocateClassDeclaration(29, 171, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 172);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates that an anonymous class with a generic method parses correctly.
	 * Tests declaring a generic method inside an anonymous class.
	 */
	@Test
	public void testAnonymousClassWithGenericMethod()
	{
		String source = """
			class Test
			{
				void m()
				{
					new Object()
					{
						<T> T identity(T value)
						{
							return value;
						}
					};
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 32, 38);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 63, 64);
			expected.allocateParameterDeclaration(63, 70, new ParameterAttribute("value", false, false, false));
			expected.allocateNode(NodeType.IDENTIFIER, 88, 93);
			expected.allocateNode(NodeType.RETURN_STATEMENT, 81, 94);
			expected.allocateNode(NodeType.BLOCK, 75, 99);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 48, 99);
			expected.allocateNode(NodeType.OBJECT_CREATION, 28, 103);
			expected.allocateNode(NodeType.BLOCK, 24, 107);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 14, 107);
			expected.allocateClassDeclaration(0, 109, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 110);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}
}
