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
 * Tests for parsing JEP 513 - Flexible Constructor Bodies.
 * <p>
 * Verifies that the parser correctly handles statements that appear before {@code super()} or
 * {@code this()} calls in constructors, as allowed by
 * <a href="https://openjdk.org/jeps/513">JEP 513</a>.
 */
public class FlexibleConstructorBodyParserTest
{
	// ========================================
	// Category 1: Statements before super()
	// ========================================

	/**
	 * Tests simple validation before {@code super()} call.
	 * This is the most common use case for JEP 513: validating constructor arguments
	 * before passing them to the superclass constructor.
	 */
	@Test
	public void simpleValidationBeforeSuper()
	{
		String source = """
			public class Child extends Parent
			{
				public Child(int value)
				{
					if (value < 0)
						throw new IllegalArgumentException();
					super(value);
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 27, 33);
			expected.allocateParameterDeclaration(50, 59, new ParameterAttribute("value", false, false, false));
			expected.allocateNode(NodeType.IDENTIFIER, 70, 75);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 78, 79);
			expected.allocateNode(NodeType.BINARY_EXPRESSION, 70, 79);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 94, 118);
			expected.allocateNode(NodeType.OBJECT_CREATION, 90, 120);
			expected.allocateNode(NodeType.THROW_STATEMENT, 84, 121);
			expected.allocateNode(NodeType.IF_STATEMENT, 66, 121);
			expected.allocateNode(NodeType.SUPER_EXPRESSION, 124, 129);
			expected.allocateNode(NodeType.IDENTIFIER, 130, 135);
			expected.allocateNode(NodeType.METHOD_INVOCATION, 124, 136);
			expected.allocateNode(NodeType.BLOCK, 62, 140);
			expected.allocateNode(NodeType.CONSTRUCTOR_DECLARATION, 37, 140);
			expected.allocateClassDeclaration(7, 142, new TypeDeclarationAttribute("Child"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 143);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Tests multiple statements before {@code super()} call.
	 * Demonstrates that JEP 513 allows multiple statements to execute
	 * before invoking the superclass constructor, enabling complex argument preparation.
	 */
	@Test
	public void multipleStatementsBeforeSuper()
	{
		String source = """
			public class Point extends Base
			{
				public Point(int a, int b)
				{
					int x = a * 2;
					int y = b * 2;
					super(x, y);
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 27, 31);
			expected.allocateParameterDeclaration(48, 53, new ParameterAttribute("a", false, false, false));
			expected.allocateParameterDeclaration(55, 60, new ParameterAttribute("b", false, false, false));
			expected.allocateNode(NodeType.IDENTIFIER, 75, 76);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 79, 80);
			expected.allocateNode(NodeType.BINARY_EXPRESSION, 75, 80);
			expected.allocateNode(NodeType.IDENTIFIER, 92, 93);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 96, 97);
			expected.allocateNode(NodeType.BINARY_EXPRESSION, 92, 97);
			expected.allocateNode(NodeType.SUPER_EXPRESSION, 101, 106);
			expected.allocateNode(NodeType.IDENTIFIER, 107, 108);
			expected.allocateNode(NodeType.IDENTIFIER, 110, 111);
			expected.allocateNode(NodeType.METHOD_INVOCATION, 101, 112);
			expected.allocateNode(NodeType.BLOCK, 63, 116);
			expected.allocateNode(NodeType.CONSTRUCTOR_DECLARATION, 35, 116);
			expected.allocateClassDeclaration(7, 118, new TypeDeclarationAttribute("Point"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 119);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Tests method call before {@code super()} for argument validation.
	 * Shows that helper methods can be invoked before {@code super()},
	 * enabling validation logic to be factored into reusable methods.
	 */
	@Test
	public void methodCallBeforeSuper()
	{
		String source = """
			public class Data extends Base
			{
				public Data(String value)
				{
					validate(value);
					super(value);
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 26, 30);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 46, 52);
			expected.allocateParameterDeclaration(46, 58, new ParameterAttribute("value", false, false, false));
			expected.allocateNode(NodeType.QUALIFIED_NAME, 65, 73);
			expected.allocateNode(NodeType.IDENTIFIER, 65, 73);
			expected.allocateNode(NodeType.IDENTIFIER, 74, 79);
			expected.allocateNode(NodeType.METHOD_INVOCATION, 65, 80);
			expected.allocateNode(NodeType.SUPER_EXPRESSION, 84, 89);
			expected.allocateNode(NodeType.IDENTIFIER, 90, 95);
			expected.allocateNode(NodeType.METHOD_INVOCATION, 84, 96);
			expected.allocateNode(NodeType.BLOCK, 61, 100);
			expected.allocateNode(NodeType.CONSTRUCTOR_DECLARATION, 34, 100);
			expected.allocateClassDeclaration(7, 102, new TypeDeclarationAttribute("Data"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 103);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	// ========================================
	// Category 2: Control flow before super()
	// ========================================

	/**
	 * Tests if-else statement before {@code super()} call.
	 * Demonstrates conditional logic execution before superclass initialization,
	 * allowing different arguments to be passed based on conditions.
	 */
	@Test
	public void ifStatementBeforeSuper()
	{
		String source = """
			public class Wrapper extends Base
			{
				public Wrapper(int value)
				{
					int adjusted;
					if (value < 0)
						adjusted = 0;
					else
						adjusted = value;
					super(adjusted);
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 29, 33);
			expected.allocateParameterDeclaration(52, 61, new ParameterAttribute("value", false, false, false));
			expected.allocateNode(NodeType.IDENTIFIER, 88, 93);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 96, 97);
			expected.allocateNode(NodeType.BINARY_EXPRESSION, 88, 97);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 102, 110);
			expected.allocateNode(NodeType.IDENTIFIER, 102, 110);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 113, 114);
			expected.allocateNode(NodeType.ASSIGNMENT_EXPRESSION, 102, 114);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 126, 134);
			expected.allocateNode(NodeType.IDENTIFIER, 126, 134);
			expected.allocateNode(NodeType.IDENTIFIER, 137, 142);
			expected.allocateNode(NodeType.ASSIGNMENT_EXPRESSION, 126, 142);
			expected.allocateNode(NodeType.IF_STATEMENT, 84, 143);
			expected.allocateNode(NodeType.SUPER_EXPRESSION, 146, 151);
			expected.allocateNode(NodeType.IDENTIFIER, 152, 160);
			expected.allocateNode(NodeType.METHOD_INVOCATION, 146, 161);
			expected.allocateNode(NodeType.BLOCK, 64, 165);
			expected.allocateNode(NodeType.CONSTRUCTOR_DECLARATION, 37, 165);
			expected.allocateClassDeclaration(7, 167, new TypeDeclarationAttribute("Wrapper"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 168);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Tests try-catch before {@code super()} call.
	 * Demonstrates exception handling during argument preparation,
	 * allowing the constructor to handle errors before superclass initialization.
	 */
	@Test
	public void tryCatchBeforeSuper()
	{
		String source = """
			public class Parser extends Base
			{
				public Parser(String text)
				{
					int value;
					try
					{
						value = Integer.parseInt(text);
					}
					catch (NumberFormatException e)
					{
						value = 0;
					}
					super(value);
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 28, 32);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 50, 56);
			expected.allocateParameterDeclaration(50, 61, new ParameterAttribute("text", false, false, false));
			expected.allocateNode(NodeType.QUALIFIED_NAME, 92, 97);
			expected.allocateNode(NodeType.IDENTIFIER, 92, 97);
			expected.allocateNode(NodeType.IDENTIFIER, 100, 107);
			expected.allocateNode(NodeType.FIELD_ACCESS, 100, 116);
			expected.allocateNode(NodeType.IDENTIFIER, 117, 121);
			expected.allocateNode(NodeType.METHOD_INVOCATION, 100, 122);
			expected.allocateNode(NodeType.ASSIGNMENT_EXPRESSION, 92, 122);
			expected.allocateNode(NodeType.BLOCK, 87, 127);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 137, 158);
			expected.allocateParameterDeclaration(137, 160, new ParameterAttribute("e", false, false, false));
			expected.allocateNode(NodeType.QUALIFIED_NAME, 169, 174);
			expected.allocateNode(NodeType.IDENTIFIER, 169, 174);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 177, 178);
			expected.allocateNode(NodeType.ASSIGNMENT_EXPRESSION, 169, 178);
			expected.allocateNode(NodeType.BLOCK, 164, 183);
			expected.allocateNode(NodeType.CATCH_CLAUSE, 130, 183);
			expected.allocateNode(NodeType.TRY_STATEMENT, 81, 183);
			expected.allocateNode(NodeType.SUPER_EXPRESSION, 186, 191);
			expected.allocateNode(NodeType.IDENTIFIER, 192, 197);
			expected.allocateNode(NodeType.METHOD_INVOCATION, 186, 198);
			expected.allocateNode(NodeType.BLOCK, 64, 202);
			expected.allocateNode(NodeType.CONSTRUCTOR_DECLARATION, 36, 202);
			expected.allocateClassDeclaration(7, 204, new TypeDeclarationAttribute("Parser"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 205);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Tests switch statement before {@code super()} call.
	 * Demonstrates multi-way branching before superclass initialization,
	 * allowing value transformation based on multiple conditions.
	 */
	@Test
	public void switchBeforeSuper()
	{
		String source = """
			public class Handler extends Base
			{
				public Handler(int type)
				{
					String name;
					switch (type)
					{
						case 1 -> name = "ONE";
						case 2 -> name = "TWO";
						default -> name = "OTHER";
					}
					super(name);
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 29, 33);
			expected.allocateParameterDeclaration(52, 60, new ParameterAttribute("type", false, false, false));
			expected.allocateNode(NodeType.QUALIFIED_NAME, 67, 73);
			expected.allocateNode(NodeType.IDENTIFIER, 90, 94);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 108, 109);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 113, 117);
			expected.allocateNode(NodeType.IDENTIFIER, 113, 117);
			expected.allocateNode(NodeType.STRING_LITERAL, 120, 125);
			expected.allocateNode(NodeType.ASSIGNMENT_EXPRESSION, 113, 125);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 135, 136);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 140, 144);
			expected.allocateNode(NodeType.IDENTIFIER, 140, 144);
			expected.allocateNode(NodeType.STRING_LITERAL, 147, 152);
			expected.allocateNode(NodeType.ASSIGNMENT_EXPRESSION, 140, 152);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 168, 172);
			expected.allocateNode(NodeType.IDENTIFIER, 168, 172);
			expected.allocateNode(NodeType.STRING_LITERAL, 175, 182);
			expected.allocateNode(NodeType.ASSIGNMENT_EXPRESSION, 168, 182);
			expected.allocateNode(NodeType.SWITCH_STATEMENT, 82, 187);
			expected.allocateNode(NodeType.SUPER_EXPRESSION, 190, 195);
			expected.allocateNode(NodeType.IDENTIFIER, 196, 200);
			expected.allocateNode(NodeType.METHOD_INVOCATION, 190, 201);
			expected.allocateNode(NodeType.BLOCK, 63, 205);
			expected.allocateNode(NodeType.CONSTRUCTOR_DECLARATION, 37, 205);
			expected.allocateClassDeclaration(7, 207, new TypeDeclarationAttribute("Handler"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 208);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	// ========================================
	// Category 3: Statements before this()
	// ========================================

	/**
	 * Tests validation before {@code this()} delegation.
	 * Shows that JEP 513 also applies to delegating constructors,
	 * enabling validation before calling another constructor in the same class.
	 */
	@Test
	public void validationBeforeThis()
	{
		String source = """
			public class Range
			{
				public Range(int end)
				{
					if (end < 0)
						throw new IllegalArgumentException();
					this(0, end);
				}

				public Range(int start, int end)
				{
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateParameterDeclaration(35, 42, new ParameterAttribute("end", false, false, false));
			expected.allocateNode(NodeType.IDENTIFIER, 53, 56);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 59, 60);
			expected.allocateNode(NodeType.BINARY_EXPRESSION, 53, 60);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 75, 99);
			expected.allocateNode(NodeType.OBJECT_CREATION, 71, 101);
			expected.allocateNode(NodeType.THROW_STATEMENT, 65, 102);
			expected.allocateNode(NodeType.IF_STATEMENT, 49, 102);
			expected.allocateNode(NodeType.THIS_EXPRESSION, 105, 109);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 110, 111);
			expected.allocateNode(NodeType.IDENTIFIER, 113, 116);
			expected.allocateNode(NodeType.METHOD_INVOCATION, 105, 117);
			expected.allocateNode(NodeType.BLOCK, 45, 121);
			expected.allocateNode(NodeType.CONSTRUCTOR_DECLARATION, 22, 121);
			expected.allocateParameterDeclaration(137, 146, new ParameterAttribute("start", false, false, false));
			expected.allocateParameterDeclaration(148, 155, new ParameterAttribute("end", false, false, false));
			expected.allocateNode(NodeType.BLOCK, 158, 162);
			expected.allocateNode(NodeType.CONSTRUCTOR_DECLARATION, 124, 162);
			expected.allocateClassDeclaration(7, 164, new TypeDeclarationAttribute("Range"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 165);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Tests computation before {@code this()} call.
	 * Demonstrates argument transformation before delegating to another constructor,
	 * a common pattern when providing convenience constructors.
	 */
	@Test
	public void computationBeforeThis()
	{
		String source = """
			public class Size
			{
				public Size(int dimension)
				{
					int width = dimension;
					int height = dimension;
					this(width, height);
				}

				public Size(int width, int height)
				{
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateParameterDeclaration(33, 46, new ParameterAttribute("dimension", false, false, false));
			expected.allocateNode(NodeType.IDENTIFIER, 65, 74);
			expected.allocateNode(NodeType.IDENTIFIER, 91, 100);
			expected.allocateNode(NodeType.THIS_EXPRESSION, 104, 108);
			expected.allocateNode(NodeType.IDENTIFIER, 109, 114);
			expected.allocateNode(NodeType.IDENTIFIER, 116, 122);
			expected.allocateNode(NodeType.METHOD_INVOCATION, 104, 123);
			expected.allocateNode(NodeType.BLOCK, 49, 127);
			expected.allocateNode(NodeType.CONSTRUCTOR_DECLARATION, 21, 127);
			expected.allocateParameterDeclaration(142, 151, new ParameterAttribute("width", false, false, false));
			expected.allocateParameterDeclaration(153, 163, new ParameterAttribute("height", false, false, false));
			expected.allocateNode(NodeType.BLOCK, 166, 170);
			expected.allocateNode(NodeType.CONSTRUCTOR_DECLARATION, 130, 170);
			expected.allocateClassDeclaration(7, 172, new TypeDeclarationAttribute("Size"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 173);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	// ========================================
	// Category 4: Implicit super()
	// ========================================

	/**
	 * Tests constructor with statements but no explicit {@code super()} or {@code this()}.
	 * The compiler inserts an implicit {@code super()} call at the beginning,
	 * but these statements execute after the implicit superclass initialization.
	 */
	@Test
	public void constructorWithoutExplicitSuper()
	{
		String source = """
			public class Logger
			{
				public Logger(String name)
				{
					System.out.println(name);
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 37, 43);
			expected.allocateParameterDeclaration(37, 48, new ParameterAttribute("name", false, false, false));
			expected.allocateNode(NodeType.QUALIFIED_NAME, 55, 73);
			expected.allocateNode(NodeType.IDENTIFIER, 55, 61);
			expected.allocateNode(NodeType.FIELD_ACCESS, 55, 65);
			expected.allocateNode(NodeType.FIELD_ACCESS, 55, 73);
			expected.allocateNode(NodeType.IDENTIFIER, 74, 78);
			expected.allocateNode(NodeType.METHOD_INVOCATION, 55, 79);
			expected.allocateNode(NodeType.BLOCK, 51, 83);
			expected.allocateNode(NodeType.CONSTRUCTOR_DECLARATION, 23, 83);
			expected.allocateClassDeclaration(7, 85, new TypeDeclarationAttribute("Logger"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 86);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Tests empty constructor body.
	 * The simplest case: an empty constructor relies entirely on
	 * the implicit {@code super()} call inserted by the compiler.
	 */
	@Test
	public void emptyConstructorBody()
	{
		String source = """
			public class Empty
			{
				public Empty()
				{
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.BLOCK, 38, 42);
			expected.allocateNode(NodeType.CONSTRUCTOR_DECLARATION, 22, 42);
			expected.allocateClassDeclaration(7, 44, new TypeDeclarationAttribute("Empty"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 45);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	// ========================================
	// Category 5: Complex scenarios
	// ========================================

	/**
	 * Tests builder pattern validation before {@code super()}.
	 * Demonstrates multiple validation method calls as part of
	 * a comprehensive input validation strategy.
	 */
	@Test
	public void builderPatternValidation()
	{
		String source = """
			public class Entity extends Base
			{
				public Entity(String name, int id)
				{
					validateName(name);
					validateId(id);
					super(name, id);
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 28, 32);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 50, 56);
			expected.allocateParameterDeclaration(50, 61, new ParameterAttribute("name", false, false, false));
			expected.allocateParameterDeclaration(63, 69, new ParameterAttribute("id", false, false, false));
			expected.allocateNode(NodeType.QUALIFIED_NAME, 76, 88);
			expected.allocateNode(NodeType.IDENTIFIER, 76, 88);
			expected.allocateNode(NodeType.IDENTIFIER, 89, 93);
			expected.allocateNode(NodeType.METHOD_INVOCATION, 76, 94);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 98, 108);
			expected.allocateNode(NodeType.IDENTIFIER, 98, 108);
			expected.allocateNode(NodeType.IDENTIFIER, 109, 111);
			expected.allocateNode(NodeType.METHOD_INVOCATION, 98, 112);
			expected.allocateNode(NodeType.SUPER_EXPRESSION, 116, 121);
			expected.allocateNode(NodeType.IDENTIFIER, 122, 126);
			expected.allocateNode(NodeType.IDENTIFIER, 128, 130);
			expected.allocateNode(NodeType.METHOD_INVOCATION, 116, 131);
			expected.allocateNode(NodeType.BLOCK, 72, 135);
			expected.allocateNode(NodeType.CONSTRUCTOR_DECLARATION, 36, 135);
			expected.allocateClassDeclaration(7, 137, new TypeDeclarationAttribute("Entity"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 138);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Tests record with validation in compact constructor.
	 * Records support JEP 513 in their compact constructors,
	 * enabling validation before the implicit field assignments.
	 */
	@Test
	public void recordCompactConstructor()
	{
		String source = """
			public record Person(String name, int age)
			{
				public Person
				{
					if (age < 0)
						throw new IllegalArgumentException();
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 21, 27);
			expected.allocateParameterDeclaration(21, 32, new ParameterAttribute("name", false, false, false));
			expected.allocateParameterDeclaration(34, 41, new ParameterAttribute("age", false, false, false));
			expected.allocateNode(NodeType.IDENTIFIER, 69, 72);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 75, 76);
			expected.allocateNode(NodeType.BINARY_EXPRESSION, 69, 76);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 91, 115);
			expected.allocateNode(NodeType.OBJECT_CREATION, 87, 117);
			expected.allocateNode(NodeType.THROW_STATEMENT, 81, 118);
			expected.allocateNode(NodeType.IF_STATEMENT, 65, 118);
			expected.allocateNode(NodeType.BLOCK, 61, 121);
			expected.allocateRecordDeclaration(7, 123, new TypeDeclarationAttribute("Person"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 124);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Tests nested class constructor with statements before {@code super()}.
	 * Verifies that JEP 513 works correctly in nested class contexts,
	 * where the inner class may need access to outer class members.
	 */
	@Test
	public void nestedClassConstructor()
	{
		String source = """
			public class Outer
			{
				public class Inner extends Base
				{
					public Inner(int value)
					{
						if (value < 0)
							throw new IllegalArgumentException();
						super(value);
					}
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 49, 53);
			expected.allocateParameterDeclaration(72, 81, new ParameterAttribute("value", false, false, false));
			expected.allocateNode(NodeType.IDENTIFIER, 94, 99);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 102, 103);
			expected.allocateNode(NodeType.BINARY_EXPRESSION, 94, 103);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 119, 143);
			expected.allocateNode(NodeType.OBJECT_CREATION, 115, 145);
			expected.allocateNode(NodeType.THROW_STATEMENT, 109, 146);
			expected.allocateNode(NodeType.IF_STATEMENT, 90, 146);
			expected.allocateNode(NodeType.SUPER_EXPRESSION, 150, 155);
			expected.allocateNode(NodeType.IDENTIFIER, 156, 161);
			expected.allocateNode(NodeType.METHOD_INVOCATION, 150, 162);
			expected.allocateNode(NodeType.BLOCK, 85, 167);
			expected.allocateNode(NodeType.CONSTRUCTOR_DECLARATION, 59, 167);
			expected.allocateClassDeclaration(29, 170, new TypeDeclarationAttribute("Inner"));
			expected.allocateClassDeclaration(7, 172, new TypeDeclarationAttribute("Outer"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 173);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	// ========================================
	// Category 6: Edge cases
	// ========================================

	/**
	 * Tests {@code super()} using a previously declared local variable.
	 * Demonstrates that local variables declared before {@code super()}
	 * can be used as arguments to the superclass constructor.
	 */
	@Test
	public void superWithLocalVariable()
	{
		String source = """
			public class Derived extends Base
			{
				public Derived(int a, int b)
				{
					int sum = a + b;
					super(sum);
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 29, 33);
			expected.allocateParameterDeclaration(52, 57, new ParameterAttribute("a", false, false, false));
			expected.allocateParameterDeclaration(59, 64, new ParameterAttribute("b", false, false, false));
			expected.allocateNode(NodeType.IDENTIFIER, 81, 82);
			expected.allocateNode(NodeType.IDENTIFIER, 85, 86);
			expected.allocateNode(NodeType.BINARY_EXPRESSION, 81, 86);
			expected.allocateNode(NodeType.SUPER_EXPRESSION, 90, 95);
			expected.allocateNode(NodeType.IDENTIFIER, 96, 99);
			expected.allocateNode(NodeType.METHOD_INVOCATION, 90, 100);
			expected.allocateNode(NodeType.BLOCK, 67, 104);
			expected.allocateNode(NodeType.CONSTRUCTOR_DECLARATION, 37, 104);
			expected.allocateClassDeclaration(7, 106, new TypeDeclarationAttribute("Derived"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 107);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Tests lambda expression before {@code super()} call.
	 * Demonstrates that complex expressions including lambdas
	 * can appear in statements before the superclass constructor call.
	 */
	@Test
	public void lambdaBeforeSuper()
	{
		String source = """
			public class Processor extends Base
			{
				public Processor(int value)
				{
					Runnable r = () -> System.out.println(value);
					super(value);
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 31, 35);
			expected.allocateParameterDeclaration(56, 65, new ParameterAttribute("value", false, false, false));
			expected.allocateNode(NodeType.QUALIFIED_NAME, 72, 80);
			expected.allocateNode(NodeType.IDENTIFIER, 91, 97);
			expected.allocateNode(NodeType.FIELD_ACCESS, 91, 101);
			expected.allocateNode(NodeType.FIELD_ACCESS, 91, 109);
			expected.allocateNode(NodeType.IDENTIFIER, 110, 115);
			expected.allocateNode(NodeType.METHOD_INVOCATION, 91, 116);
			expected.allocateNode(NodeType.LAMBDA_EXPRESSION, 85, 116);
			expected.allocateNode(NodeType.SUPER_EXPRESSION, 120, 125);
			expected.allocateNode(NodeType.IDENTIFIER, 126, 131);
			expected.allocateNode(NodeType.METHOD_INVOCATION, 120, 132);
			expected.allocateNode(NodeType.BLOCK, 68, 136);
			expected.allocateNode(NodeType.CONSTRUCTOR_DECLARATION, 39, 136);
			expected.allocateClassDeclaration(7, 138, new TypeDeclarationAttribute("Processor"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 139);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Tests anonymous class instantiation before {@code super()} call.
	 * Demonstrates that object creation expressions with class bodies
	 * can appear before the superclass constructor invocation.
	 */
	@Test
	public void anonymousClassBeforeSuper()
	{
		String source = """
			public class Handler extends Base
			{
				public Handler(int value)
				{
					Runnable task = new Runnable()
					{
						public void run()
						{
						}
					};
					super(value);
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 29, 33);
			expected.allocateParameterDeclaration(52, 61, new ParameterAttribute("value", false, false, false));
			expected.allocateNode(NodeType.QUALIFIED_NAME, 68, 76);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 88, 96);
			expected.allocateNode(NodeType.BLOCK, 127, 133);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 106, 133);
			expected.allocateNode(NodeType.OBJECT_CREATION, 84, 137);
			expected.allocateNode(NodeType.SUPER_EXPRESSION, 141, 146);
			expected.allocateNode(NodeType.IDENTIFIER, 147, 152);
			expected.allocateNode(NodeType.METHOD_INVOCATION, 141, 153);
			expected.allocateNode(NodeType.BLOCK, 64, 157);
			expected.allocateNode(NodeType.CONSTRUCTOR_DECLARATION, 37, 157);
			expected.allocateClassDeclaration(7, 159, new TypeDeclarationAttribute("Handler"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 160);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}
}
