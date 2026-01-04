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
 * Tests for parsing local type declarations (classes, interfaces, enums, records)
 * inside method bodies, constructor bodies, and initializer blocks.
 */
public final class LocalTypeDeclarationParserTest
{
	// ==================== Category 1: Basic Local Type Declarations ====================

	/**
	 * Validates parsing of a local class declaration inside a method body.
	 * Local classes have been supported since Java 1.1 and can contain methods.
	 */
	@Test
	public void testLocalClassDeclaration()
	{
		String source = """
			class Test
			{
				void m()
				{
					class Helper
					{
						void help()
						{
						}
					}
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.BLOCK, 63, 69);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 48, 69);
			expected.allocateClassDeclaration(28, 73, new TypeDeclarationAttribute("Helper"));
			expected.allocateNode(NodeType.BLOCK, 24, 76);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 14, 76);
			expected.allocateClassDeclaration(0, 78, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 79);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates parsing of a local interface declaration inside a method body.
	 * Local interfaces are supported since JDK 16.
	 */
	@Test
	public void testLocalInterfaceDeclaration()
	{
		String source = """
			class Test
			{
				void m()
				{
					interface Validator
					{
						boolean validate();
					}
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.METHOD_DECLARATION, 55, 74);
			expected.allocateInterfaceDeclaration(28, 78, new TypeDeclarationAttribute("Validator"));
			expected.allocateNode(NodeType.BLOCK, 24, 81);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 14, 81);
			expected.allocateClassDeclaration(0, 83, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 84);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates parsing of a local enum declaration inside a method body.
	 * Local enums are supported since JDK 16.
	 */
	@Test
	public void testLocalEnumDeclaration()
	{
		String source = """
			class Test
			{
				void m()
				{
					enum Status
					{
						OK,
						ERROR
					}
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.ENUM_CONSTANT, 47, 49);
			expected.allocateNode(NodeType.ENUM_CONSTANT, 54, 59);
			expected.allocateEnumDeclaration(28, 63, new TypeDeclarationAttribute("Status"));
			expected.allocateNode(NodeType.BLOCK, 24, 66);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 14, 66);
			expected.allocateClassDeclaration(0, 68, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 69);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates parsing of a local record declaration inside a method body.
	 * Local records are supported since JDK 16.
	 */
	@Test
	public void testLocalRecordDeclaration()
	{
		String source = """
			class Test
			{
				void m()
				{
					record Point(int x, int y)
					{
					}
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateParameterDeclaration(41, 46, new ParameterAttribute("x", false, false, false));
			expected.allocateParameterDeclaration(48, 53, new ParameterAttribute("y", false, false, false));
			expected.allocateRecordDeclaration(28, 62, new TypeDeclarationAttribute("Point"));
			expected.allocateNode(NodeType.BLOCK, 24, 65);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 14, 65);
			expected.allocateClassDeclaration(0, 67, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 68);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	// ==================== Category 2: Local Types with Modifiers ====================

	/**
	 * Validates parsing of a local class with final modifier.
	 * Local classes can be declared final to prevent subclassing.
	 */
	@Test
	public void testLocalClassWithFinalModifier()
	{
		String source = """
			class Test
			{
				void m()
				{
					final class FinalHelper
					{
					}
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateClassDeclaration(34, 59, new TypeDeclarationAttribute("FinalHelper"));
			expected.allocateNode(NodeType.BLOCK, 24, 62);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 14, 62);
			expected.allocateClassDeclaration(0, 64, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 65);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates parsing of a local class with abstract modifier.
	 * Local abstract classes can contain abstract methods.
	 */
	@Test
	public void testLocalClassWithAbstractModifier()
	{
		String source = """
			class Test
			{
				void m()
				{
					abstract class AbstractHelper
					{
						abstract void doWork();
					}
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.METHOD_DECLARATION, 65, 88);
			expected.allocateClassDeclaration(37, 92, new TypeDeclarationAttribute("AbstractHelper"));
			expected.allocateNode(NodeType.BLOCK, 24, 95);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 14, 95);
			expected.allocateClassDeclaration(0, 97, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 98);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates parsing of a local class with annotation.
	 * Local classes can have annotations like SuppressWarnings.
	 */
	@Test
	public void testLocalClassWithAnnotation()
	{
		String source = """
			class Test
			{
				void m()
				{
					@SuppressWarnings("unchecked")
					class AnnotatedHelper
					{
					}
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 29, 45);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 29, 45);
			expected.allocateNode(NodeType.STRING_LITERAL, 46, 57);
			expected.allocateNode(NodeType.ANNOTATION, 28, 58);
			expected.allocateClassDeclaration(61, 90, new TypeDeclarationAttribute("AnnotatedHelper"));
			expected.allocateNode(NodeType.BLOCK, 24, 93);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 14, 93);
			expected.allocateClassDeclaration(0, 95, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 96);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates parsing of a local record with annotation.
	 * Local records can have annotations applied.
	 */
	@Test
	public void testLocalRecordWithAnnotation()
	{
		String source = """
			class Test
			{
				void m()
				{
					@Deprecated
					record DeprecatedPoint(int x, int y)
					{
					}
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 29, 39);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 29, 39);
			expected.allocateNode(NodeType.ANNOTATION, 28, 39);
			expected.allocateParameterDeclaration(65, 70, new ParameterAttribute("x", false, false, false));
			expected.allocateParameterDeclaration(72, 77, new ParameterAttribute("y", false, false, false));
			expected.allocateRecordDeclaration(42, 86, new TypeDeclarationAttribute("DeprecatedPoint"));
			expected.allocateNode(NodeType.BLOCK, 24, 89);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 14, 89);
			expected.allocateClassDeclaration(0, 91, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 92);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates parsing of a local interface with annotation.
	 * Local interfaces can have annotations like FunctionalInterface.
	 */
	@Test
	public void testLocalInterfaceWithAnnotation()
	{
		String source = """
			class Test
			{
				void m()
				{
					@FunctionalInterface
					interface Processor
					{
						void process();
					}
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 29, 48);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 29, 48);
			expected.allocateNode(NodeType.ANNOTATION, 28, 48);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 78, 93);
			expected.allocateInterfaceDeclaration(51, 97, new TypeDeclarationAttribute("Processor"));
			expected.allocateNode(NodeType.BLOCK, 24, 100);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 14, 100);
			expected.allocateClassDeclaration(0, 102, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 103);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	// ==================== Category 3: Nesting Contexts ====================

	/**
	 * Validates parsing of a local class inside a constructor body.
	 * Local classes can be declared in constructors, not just methods.
	 */
	@Test
	public void testLocalClassInConstructor()
	{
		String source = """
			class Test
			{
				Test()
				{
					class Helper
					{
					}
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateClassDeclaration(26, 46, new TypeDeclarationAttribute("Helper"));
			expected.allocateNode(NodeType.BLOCK, 22, 49);
			expected.allocateNode(NodeType.CONSTRUCTOR_DECLARATION, 14, 49);
			expected.allocateClassDeclaration(0, 51, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 52);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates parsing of a local class inside an instance initializer block.
	 * Local classes can be declared in instance initializer blocks.
	 */
	@Test
	public void testLocalClassInInstanceInitializer()
	{
		String source = """
			class Test
			{
				{
					class Helper
					{
					}
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateClassDeclaration(18, 38, new TypeDeclarationAttribute("Helper"));
			expected.allocateNode(NodeType.BLOCK, 14, 41);
			expected.allocateClassDeclaration(0, 43, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 44);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates parsing of a local class inside a static initializer block.
	 * Local classes can be declared in static initializer blocks.
	 */
	@Test
	public void testLocalClassInStaticInitializer()
	{
		String source = """
			class Test
			{
				static
				{
					class Helper
					{
					}
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateClassDeclaration(26, 46, new TypeDeclarationAttribute("Helper"));
			expected.allocateNode(NodeType.BLOCK, 22, 49);
			expected.allocateClassDeclaration(0, 51, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 52);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates parsing of a local record inside a lambda expression body.
	 * Local types can be declared inside lambda bodies (JDK 16+).
	 */
	@Test
	public void testLocalRecordInLambda()
	{
		String source = """
			class Test
			{
				void m()
				{
					Runnable r = () ->
					{
						record Point(int x, int y)
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
			expected.allocateNode(NodeType.QUALIFIED_NAME, 28, 36);
			expected.allocateParameterDeclaration(67, 72, new ParameterAttribute("x", false, false, false));
			expected.allocateParameterDeclaration(74, 79, new ParameterAttribute("y", false, false, false));
			expected.allocateRecordDeclaration(54, 90, new TypeDeclarationAttribute("Point"));
			expected.allocateNode(NodeType.BLOCK, 49, 94);
			expected.allocateNode(NodeType.LAMBDA_EXPRESSION, 41, 94);
			expected.allocateNode(NodeType.BLOCK, 24, 98);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 14, 98);
			expected.allocateClassDeclaration(0, 100, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 101);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates parsing of multiple local types in a single method.
	 * Multiple local type declarations can coexist in the same method body.
	 */
	@Test
	public void testMultipleLocalTypesInMethod()
	{
		String source = """
			class Test
			{
				void m()
				{
					class Helper1
					{
					}
					class Helper2
					{
					}
					record Data(int value)
					{
					}
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateClassDeclaration(28, 49, new TypeDeclarationAttribute("Helper1"));
			expected.allocateClassDeclaration(52, 73, new TypeDeclarationAttribute("Helper2"));
			expected.allocateParameterDeclaration(88, 97, new ParameterAttribute("value", false, false, false));
			expected.allocateRecordDeclaration(76, 106, new TypeDeclarationAttribute("Data"));
			expected.allocateNode(NodeType.BLOCK, 24, 109);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 14, 109);
			expected.allocateClassDeclaration(0, 111, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 112);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	// ==================== Category 4: Complex Local Types ====================

	/**
	 * Validates parsing of a local class with members (field, constructor, method).
	 * Local classes can contain their own fields, constructors, and methods.
	 */
	@Test
	public void testLocalClassWithMembers()
	{
		String source = """
			class Test
			{
				void m()
				{
					class Helper
					{
						private int value;

						Helper(int value)
						{
							this.value = value;
						}

						int getValue()
						{
							return value;
						}
					}
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.FIELD_DECLARATION, 48, 66);
			expected.allocateParameterDeclaration(78, 87, new ParameterAttribute("value", false, false, false));
			expected.allocateNode(NodeType.THIS_EXPRESSION, 98, 102);
			expected.allocateNode(NodeType.FIELD_ACCESS, 98, 108);
			expected.allocateNode(NodeType.IDENTIFIER, 111, 116);
			expected.allocateNode(NodeType.ASSIGNMENT_EXPRESSION, 98, 116);
			expected.allocateNode(NodeType.BLOCK, 92, 122);
			expected.allocateNode(NodeType.CONSTRUCTOR_DECLARATION, 71, 122);
			expected.allocateNode(NodeType.IDENTIFIER, 158, 163);
			expected.allocateNode(NodeType.RETURN_STATEMENT, 151, 164);
			expected.allocateNode(NodeType.BLOCK, 145, 169);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 127, 169);
			expected.allocateClassDeclaration(28, 173, new TypeDeclarationAttribute("Helper"));
			expected.allocateNode(NodeType.BLOCK, 24, 176);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 14, 176);
			expected.allocateClassDeclaration(0, 178, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 179);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates parsing of a local interface with a default method.
	 * Local interfaces can contain default methods.
	 */
	@Test
	public void testLocalInterfaceWithDefaultMethod()
	{
		String source = """
			class Test
			{
				void m()
				{
					interface Processor
					{
						void process();

						default void setup()
						{
						}
					}
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.METHOD_DECLARATION, 55, 70);
			expected.allocateNode(NodeType.BLOCK, 99, 105);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 75, 105);
			expected.allocateInterfaceDeclaration(28, 109, new TypeDeclarationAttribute("Processor"));
			expected.allocateNode(NodeType.BLOCK, 24, 112);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 14, 112);
			expected.allocateClassDeclaration(0, 114, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 115);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates parsing of a local enum with constructor and methods.
	 * Local enums can have fields, constructors, and methods.
	 */
	@Test
	public void testLocalEnumWithConstructorAndMethods()
	{
		String source = """
			class Test
			{
				void m()
				{
					enum Status
					{
						OK(0),
						ERROR(1);

						private final int code;

						Status(int code)
						{
							this.code = code;
						}

						int getCode()
						{
							return code;
						}
					}
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.INTEGER_LITERAL, 50, 51);
			expected.allocateNode(NodeType.ENUM_CONSTANT, 47, 52);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 63, 64);
			expected.allocateNode(NodeType.ENUM_CONSTANT, 57, 65);
			expected.allocateNode(NodeType.FIELD_DECLARATION, 71, 94);
			expected.allocateParameterDeclaration(106, 114, new ParameterAttribute("code", false, false, false));
			expected.allocateNode(NodeType.THIS_EXPRESSION, 125, 129);
			expected.allocateNode(NodeType.FIELD_ACCESS, 125, 134);
			expected.allocateNode(NodeType.IDENTIFIER, 137, 141);
			expected.allocateNode(NodeType.ASSIGNMENT_EXPRESSION, 125, 141);
			expected.allocateNode(NodeType.BLOCK, 119, 147);
			expected.allocateNode(NodeType.CONSTRUCTOR_DECLARATION, 99, 147);
			expected.allocateNode(NodeType.IDENTIFIER, 182, 186);
			expected.allocateNode(NodeType.RETURN_STATEMENT, 175, 187);
			expected.allocateNode(NodeType.BLOCK, 169, 192);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 152, 192);
			expected.allocateEnumDeclaration(28, 196, new TypeDeclarationAttribute("Status"));
			expected.allocateNode(NodeType.BLOCK, 24, 199);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 14, 199);
			expected.allocateClassDeclaration(0, 201, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 202);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates parsing of a local record with a custom method.
	 * Local records can contain additional methods beyond the canonical ones.
	 */
	@Test
	public void testLocalRecordWithCustomMethod()
	{
		String source = """
			class Test
			{
				void m()
				{
					record Point(int x, int y)
					{
						double distance()
						{
							return Math.sqrt(x * x + y * y);
						}
					}
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateParameterDeclaration(41, 46, new ParameterAttribute("x", false, false, false));
			expected.allocateParameterDeclaration(48, 53, new ParameterAttribute("y", false, false, false));
			expected.allocateNode(NodeType.IDENTIFIER, 96, 100);
			expected.allocateNode(NodeType.FIELD_ACCESS, 96, 105);
			expected.allocateNode(NodeType.IDENTIFIER, 106, 107);
			expected.allocateNode(NodeType.IDENTIFIER, 110, 111);
			expected.allocateNode(NodeType.BINARY_EXPRESSION, 106, 111);
			expected.allocateNode(NodeType.IDENTIFIER, 114, 115);
			expected.allocateNode(NodeType.IDENTIFIER, 118, 119);
			expected.allocateNode(NodeType.BINARY_EXPRESSION, 114, 119);
			expected.allocateNode(NodeType.BINARY_EXPRESSION, 106, 119);
			expected.allocateNode(NodeType.METHOD_INVOCATION, 96, 120);
			expected.allocateNode(NodeType.RETURN_STATEMENT, 89, 121);
			expected.allocateNode(NodeType.BLOCK, 83, 126);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 62, 126);
			expected.allocateRecordDeclaration(28, 130, new TypeDeclarationAttribute("Point"));
			expected.allocateNode(NodeType.BLOCK, 24, 133);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 14, 133);
			expected.allocateClassDeclaration(0, 135, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 136);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates parsing of nested local classes (local class containing member class).
	 * A local class can contain its own nested class declarations.
	 */
	@Test
	public void testNestedLocalClass()
	{
		String source = """
			class Test
			{
				void m()
				{
					class Outer
					{
						class Inner
						{
						}
					}
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateClassDeclaration(47, 68, new TypeDeclarationAttribute("Inner"));
			expected.allocateClassDeclaration(28, 72, new TypeDeclarationAttribute("Outer"));
			expected.allocateNode(NodeType.BLOCK, 24, 75);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 14, 75);
			expected.allocateClassDeclaration(0, 77, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 78);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	// ==================== Category 5: Extends/Implements Clauses ====================

	/**
	 * Validates parsing of local class with extends clause.
	 * A local class can extend another class.
	 */
	@Test
	public void testLocalClassExtends()
	{
		String source = """
			class Test
			{
				void m()
				{
					class Base
					{
					}
					class Derived extends Base
					{
					}
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateClassDeclaration(28, 46, new TypeDeclarationAttribute("Base"));
			expected.allocateNode(NodeType.QUALIFIED_NAME, 71, 75);
			expected.allocateClassDeclaration(49, 83, new TypeDeclarationAttribute("Derived"));
			expected.allocateNode(NodeType.BLOCK, 24, 86);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 14, 86);
			expected.allocateClassDeclaration(0, 88, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 89);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates parsing of local class implementing a local interface.
	 * A local class can implement an interface declared in the same scope.
	 */
	@Test
	public void testLocalClassImplements()
	{
		String source = """
			class Test
			{
				void m()
				{
					interface Runnable
					{
						void run();
					}
					class Worker implements Runnable
					{
						public void run()
						{
						}
					}
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.METHOD_DECLARATION, 54, 65);
			expected.allocateInterfaceDeclaration(28, 69, new TypeDeclarationAttribute("Runnable"));
			expected.allocateNode(NodeType.QUALIFIED_NAME, 96, 104);
			expected.allocateNode(NodeType.BLOCK, 133, 139);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 112, 139);
			expected.allocateClassDeclaration(72, 143, new TypeDeclarationAttribute("Worker"));
			expected.allocateNode(NodeType.BLOCK, 24, 146);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 14, 146);
			expected.allocateClassDeclaration(0, 148, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 149);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates parsing of local record implementing a local interface.
	 * A local record can implement an interface declared in the same scope.
	 */
	@Test
	public void testLocalRecordImplements()
	{
		String source = """
			class Test
			{
				void m()
				{
					interface Printable
					{
						String print();
					}
					record Message(String text) implements Printable
					{
						public String print()
						{
							return text;
						}
					}
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.METHOD_DECLARATION, 55, 70);
			expected.allocateInterfaceDeclaration(28, 74, new TypeDeclarationAttribute("Printable"));
			expected.allocateNode(NodeType.QUALIFIED_NAME, 92, 98);
			expected.allocateParameterDeclaration(92, 103, new ParameterAttribute("text", false, false, false));
			expected.allocateNode(NodeType.QUALIFIED_NAME, 116, 125);
			expected.allocateNode(NodeType.IDENTIFIER, 171, 175);
			expected.allocateNode(NodeType.RETURN_STATEMENT, 164, 176);
			expected.allocateNode(NodeType.BLOCK, 158, 181);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 133, 181);
			expected.allocateRecordDeclaration(77, 185, new TypeDeclarationAttribute("Message"));
			expected.allocateNode(NodeType.BLOCK, 24, 188);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 14, 188);
			expected.allocateClassDeclaration(0, 190, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 191);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates parsing of local class with both extends and implements clauses.
	 * A local class can extend a class and implement an interface simultaneously.
	 */
	@Test
	public void testLocalClassExtendsAndImplements()
	{
		String source = """
			class Test
			{
				void m()
				{
					abstract class Base
					{
					}
					interface Runnable
					{
						void run();
					}
					class Worker extends Base implements Runnable
					{
						public void run()
						{
						}
					}
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateClassDeclaration(37, 55, new TypeDeclarationAttribute("Base"));
			expected.allocateNode(NodeType.METHOD_DECLARATION, 84, 95);
			expected.allocateInterfaceDeclaration(58, 99, new TypeDeclarationAttribute("Runnable"));
			expected.allocateNode(NodeType.QUALIFIED_NAME, 123, 127);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 139, 147);
			expected.allocateNode(NodeType.BLOCK, 176, 182);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 155, 182);
			expected.allocateClassDeclaration(102, 186, new TypeDeclarationAttribute("Worker"));
			expected.allocateNode(NodeType.BLOCK, 24, 189);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 14, 189);
			expected.allocateClassDeclaration(0, 191, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 192);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates parsing of local interface extending another local interface.
	 * A local interface can extend another interface declared in the same scope.
	 */
	@Test
	public void testLocalInterfaceExtends()
	{
		String source = """
			class Test
			{
				void m()
				{
					interface Base
					{
					}
					interface Extended extends Base
					{
					}
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateInterfaceDeclaration(28, 50, new TypeDeclarationAttribute("Base"));
			expected.allocateNode(NodeType.QUALIFIED_NAME, 80, 84);
			expected.allocateInterfaceDeclaration(53, 92, new TypeDeclarationAttribute("Extended"));
			expected.allocateNode(NodeType.BLOCK, 24, 95);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 14, 95);
			expected.allocateClassDeclaration(0, 97, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 98);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}
}
