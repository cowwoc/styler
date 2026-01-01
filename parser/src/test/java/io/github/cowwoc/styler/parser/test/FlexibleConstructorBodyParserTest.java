package io.github.cowwoc.styler.parser.test;

import io.github.cowwoc.styler.parser.test.ParserTestUtils.SemanticNode;
import org.testng.annotations.Test;

import java.util.Set;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;
import static io.github.cowwoc.styler.ast.core.NodeType.ASSIGNMENT_EXPRESSION;
import static io.github.cowwoc.styler.ast.core.NodeType.BINARY_EXPRESSION;
import static io.github.cowwoc.styler.ast.core.NodeType.BLOCK;
import static io.github.cowwoc.styler.ast.core.NodeType.CATCH_CLAUSE;
import static io.github.cowwoc.styler.ast.core.NodeType.CLASS_DECLARATION;
import static io.github.cowwoc.styler.ast.core.NodeType.COMPILATION_UNIT;
import static io.github.cowwoc.styler.ast.core.NodeType.CONSTRUCTOR_DECLARATION;
import static io.github.cowwoc.styler.ast.core.NodeType.FIELD_ACCESS;
import static io.github.cowwoc.styler.ast.core.NodeType.IDENTIFIER;
import static io.github.cowwoc.styler.ast.core.NodeType.IF_STATEMENT;
import static io.github.cowwoc.styler.ast.core.NodeType.INTEGER_LITERAL;
import static io.github.cowwoc.styler.ast.core.NodeType.LAMBDA_EXPRESSION;
import static io.github.cowwoc.styler.ast.core.NodeType.METHOD_DECLARATION;
import static io.github.cowwoc.styler.ast.core.NodeType.METHOD_INVOCATION;
import static io.github.cowwoc.styler.ast.core.NodeType.OBJECT_CREATION;
import static io.github.cowwoc.styler.ast.core.NodeType.PARAMETER_DECLARATION;
import static io.github.cowwoc.styler.ast.core.NodeType.QUALIFIED_NAME;
import static io.github.cowwoc.styler.ast.core.NodeType.RECORD_DECLARATION;
import static io.github.cowwoc.styler.ast.core.NodeType.STRING_LITERAL;
import static io.github.cowwoc.styler.ast.core.NodeType.SUPER_EXPRESSION;
import static io.github.cowwoc.styler.ast.core.NodeType.SWITCH_STATEMENT;
import static io.github.cowwoc.styler.ast.core.NodeType.THIS_EXPRESSION;
import static io.github.cowwoc.styler.ast.core.NodeType.THROW_STATEMENT;
import static io.github.cowwoc.styler.ast.core.NodeType.TRY_STATEMENT;
import static io.github.cowwoc.styler.parser.test.ParserTestUtils.parseSemanticAst;
import static io.github.cowwoc.styler.parser.test.ParserTestUtils.semanticNode;

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
		Set<SemanticNode> actual = parseSemanticAst(source);

		Set<SemanticNode> expected = Set.of(
			semanticNode(COMPILATION_UNIT, 0, 143),
			semanticNode(CLASS_DECLARATION, 7, 142, "Child"),
			semanticNode(QUALIFIED_NAME, 27, 33),
			semanticNode(CONSTRUCTOR_DECLARATION, 37, 140),
			semanticNode(PARAMETER_DECLARATION, 50, 59, "value"),
			semanticNode(BLOCK, 62, 140),
			semanticNode(IF_STATEMENT, 66, 121),
			semanticNode(BINARY_EXPRESSION, 70, 79),
			semanticNode(IDENTIFIER, 70, 75),
			semanticNode(INTEGER_LITERAL, 78, 79),
			semanticNode(THROW_STATEMENT, 84, 121),
			semanticNode(OBJECT_CREATION, 90, 120),
			semanticNode(QUALIFIED_NAME, 94, 118),
			semanticNode(METHOD_INVOCATION, 124, 136),
			semanticNode(SUPER_EXPRESSION, 124, 129),
			semanticNode(IDENTIFIER, 130, 135));

		requireThat(actual, "actual").isEqualTo(expected);
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
		Set<SemanticNode> actual = parseSemanticAst(source);

		Set<SemanticNode> expected = Set.of(
			semanticNode(COMPILATION_UNIT, 0, 119),
			semanticNode(CLASS_DECLARATION, 7, 118, "Point"),
			semanticNode(QUALIFIED_NAME, 27, 31),
			semanticNode(CONSTRUCTOR_DECLARATION, 35, 116),
			semanticNode(PARAMETER_DECLARATION, 48, 53, "a"),
			semanticNode(PARAMETER_DECLARATION, 55, 60, "b"),
			semanticNode(BLOCK, 63, 116),
			semanticNode(BINARY_EXPRESSION, 75, 80),
			semanticNode(IDENTIFIER, 75, 76),
			semanticNode(INTEGER_LITERAL, 79, 80),
			semanticNode(BINARY_EXPRESSION, 92, 97),
			semanticNode(IDENTIFIER, 92, 93),
			semanticNode(INTEGER_LITERAL, 96, 97),
			semanticNode(METHOD_INVOCATION, 101, 112),
			semanticNode(SUPER_EXPRESSION, 101, 106),
			semanticNode(IDENTIFIER, 107, 108),
			semanticNode(IDENTIFIER, 110, 111));

		requireThat(actual, "actual").isEqualTo(expected);
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
		Set<SemanticNode> actual = parseSemanticAst(source);

		Set<SemanticNode> expected = Set.of(
			semanticNode(COMPILATION_UNIT, 0, 103),
			semanticNode(CLASS_DECLARATION, 7, 102, "Data"),
			semanticNode(QUALIFIED_NAME, 26, 30),
			semanticNode(CONSTRUCTOR_DECLARATION, 34, 100),
			semanticNode(PARAMETER_DECLARATION, 46, 58, "value"),
			semanticNode(QUALIFIED_NAME, 46, 52),
			semanticNode(BLOCK, 61, 100),
			semanticNode(METHOD_INVOCATION, 65, 80),
			semanticNode(QUALIFIED_NAME, 65, 73),
			semanticNode(IDENTIFIER, 65, 73),
			semanticNode(IDENTIFIER, 74, 79),
			semanticNode(METHOD_INVOCATION, 84, 96),
			semanticNode(SUPER_EXPRESSION, 84, 89),
			semanticNode(IDENTIFIER, 90, 95));

		requireThat(actual, "actual").isEqualTo(expected);
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
		Set<SemanticNode> actual = parseSemanticAst(source);

		Set<SemanticNode> expected = Set.of(
			semanticNode(COMPILATION_UNIT, 0, 168),
			semanticNode(CLASS_DECLARATION, 7, 167, "Wrapper"),
			semanticNode(QUALIFIED_NAME, 29, 33),
			semanticNode(CONSTRUCTOR_DECLARATION, 37, 165),
			semanticNode(PARAMETER_DECLARATION, 52, 61, "value"),
			semanticNode(BLOCK, 64, 165),
			semanticNode(IF_STATEMENT, 84, 143),
			semanticNode(BINARY_EXPRESSION, 88, 97),
			semanticNode(IDENTIFIER, 88, 93),
			semanticNode(INTEGER_LITERAL, 96, 97),
			semanticNode(ASSIGNMENT_EXPRESSION, 102, 114),
			semanticNode(QUALIFIED_NAME, 102, 110),
			semanticNode(IDENTIFIER, 102, 110),
			semanticNode(INTEGER_LITERAL, 113, 114),
			semanticNode(ASSIGNMENT_EXPRESSION, 126, 142),
			semanticNode(QUALIFIED_NAME, 126, 134),
			semanticNode(IDENTIFIER, 126, 134),
			semanticNode(IDENTIFIER, 137, 142),
			semanticNode(METHOD_INVOCATION, 146, 161),
			semanticNode(SUPER_EXPRESSION, 146, 151),
			semanticNode(IDENTIFIER, 152, 160));

		requireThat(actual, "actual").isEqualTo(expected);
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
		Set<SemanticNode> actual = parseSemanticAst(source);

		Set<SemanticNode> expected = Set.of(
			semanticNode(COMPILATION_UNIT, 0, 205),
			semanticNode(CLASS_DECLARATION, 7, 204, "Parser"),
			semanticNode(QUALIFIED_NAME, 28, 32),
			semanticNode(CONSTRUCTOR_DECLARATION, 36, 202),
			semanticNode(PARAMETER_DECLARATION, 50, 61, "text"),
			semanticNode(QUALIFIED_NAME, 50, 56),
			semanticNode(BLOCK, 64, 202),
			semanticNode(TRY_STATEMENT, 81, 183),
			semanticNode(BLOCK, 87, 127),
			semanticNode(ASSIGNMENT_EXPRESSION, 92, 122),
			semanticNode(QUALIFIED_NAME, 92, 97),
			semanticNode(IDENTIFIER, 92, 97),
			semanticNode(METHOD_INVOCATION, 100, 122),
			semanticNode(FIELD_ACCESS, 100, 116),
			semanticNode(IDENTIFIER, 100, 107),
			semanticNode(IDENTIFIER, 117, 121),
			semanticNode(CATCH_CLAUSE, 130, 183),
			semanticNode(PARAMETER_DECLARATION, 137, 160, "e"),
			semanticNode(QUALIFIED_NAME, 137, 158),
			semanticNode(BLOCK, 164, 183),
			semanticNode(ASSIGNMENT_EXPRESSION, 169, 178),
			semanticNode(IDENTIFIER, 169, 174),
			semanticNode(QUALIFIED_NAME, 169, 174),
			semanticNode(INTEGER_LITERAL, 177, 178),
			semanticNode(METHOD_INVOCATION, 186, 198),
			semanticNode(SUPER_EXPRESSION, 186, 191),
			semanticNode(IDENTIFIER, 192, 197));

		requireThat(actual, "actual").isEqualTo(expected);
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
		Set<SemanticNode> actual = parseSemanticAst(source);

		Set<SemanticNode> expected = Set.of(
			semanticNode(COMPILATION_UNIT, 0, 208),
			semanticNode(CLASS_DECLARATION, 7, 207, "Handler"),
			semanticNode(QUALIFIED_NAME, 29, 33),
			semanticNode(CONSTRUCTOR_DECLARATION, 37, 205),
			semanticNode(PARAMETER_DECLARATION, 52, 60, "type"),
			semanticNode(BLOCK, 63, 205),
			semanticNode(QUALIFIED_NAME, 67, 73),
			semanticNode(SWITCH_STATEMENT, 82, 187),
			semanticNode(IDENTIFIER, 90, 94),
			semanticNode(INTEGER_LITERAL, 108, 109),
			semanticNode(ASSIGNMENT_EXPRESSION, 113, 125),
			semanticNode(QUALIFIED_NAME, 113, 117),
			semanticNode(IDENTIFIER, 113, 117),
			semanticNode(STRING_LITERAL, 120, 125),
			semanticNode(INTEGER_LITERAL, 135, 136),
			semanticNode(ASSIGNMENT_EXPRESSION, 140, 152),
			semanticNode(IDENTIFIER, 140, 144),
			semanticNode(QUALIFIED_NAME, 140, 144),
			semanticNode(STRING_LITERAL, 147, 152),
			semanticNode(ASSIGNMENT_EXPRESSION, 168, 182),
			semanticNode(IDENTIFIER, 168, 172),
			semanticNode(QUALIFIED_NAME, 168, 172),
			semanticNode(STRING_LITERAL, 175, 182),
			semanticNode(METHOD_INVOCATION, 190, 201),
			semanticNode(SUPER_EXPRESSION, 190, 195),
			semanticNode(IDENTIFIER, 196, 200));

		requireThat(actual, "actual").isEqualTo(expected);
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
		Set<SemanticNode> actual = parseSemanticAst(source);

		Set<SemanticNode> expected = Set.of(
			semanticNode(COMPILATION_UNIT, 0, 165),
			semanticNode(CLASS_DECLARATION, 7, 164, "Range"),
			semanticNode(CONSTRUCTOR_DECLARATION, 22, 121),
			semanticNode(PARAMETER_DECLARATION, 35, 42, "end"),
			semanticNode(BLOCK, 45, 121),
			semanticNode(IF_STATEMENT, 49, 102),
			semanticNode(BINARY_EXPRESSION, 53, 60),
			semanticNode(IDENTIFIER, 53, 56),
			semanticNode(INTEGER_LITERAL, 59, 60),
			semanticNode(THROW_STATEMENT, 65, 102),
			semanticNode(OBJECT_CREATION, 71, 101),
			semanticNode(QUALIFIED_NAME, 75, 99),
			semanticNode(METHOD_INVOCATION, 105, 117),
			semanticNode(THIS_EXPRESSION, 105, 109),
			semanticNode(INTEGER_LITERAL, 110, 111),
			semanticNode(IDENTIFIER, 113, 116),
			semanticNode(CONSTRUCTOR_DECLARATION, 124, 162),
			semanticNode(PARAMETER_DECLARATION, 137, 146, "start"),
			semanticNode(PARAMETER_DECLARATION, 148, 155, "end"),
			semanticNode(BLOCK, 158, 162));

		requireThat(actual, "actual").isEqualTo(expected);
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
		Set<SemanticNode> actual = parseSemanticAst(source);

		Set<SemanticNode> expected = Set.of(
			semanticNode(COMPILATION_UNIT, 0, 173),
			semanticNode(CLASS_DECLARATION, 7, 172, "Size"),
			semanticNode(CONSTRUCTOR_DECLARATION, 21, 127),
			semanticNode(PARAMETER_DECLARATION, 33, 46, "dimension"),
			semanticNode(BLOCK, 49, 127),
			semanticNode(IDENTIFIER, 65, 74),
			semanticNode(IDENTIFIER, 91, 100),
			semanticNode(METHOD_INVOCATION, 104, 123),
			semanticNode(THIS_EXPRESSION, 104, 108),
			semanticNode(IDENTIFIER, 109, 114),
			semanticNode(IDENTIFIER, 116, 122),
			semanticNode(CONSTRUCTOR_DECLARATION, 130, 170),
			semanticNode(PARAMETER_DECLARATION, 142, 151, "width"),
			semanticNode(PARAMETER_DECLARATION, 153, 163, "height"),
			semanticNode(BLOCK, 166, 170));

		requireThat(actual, "actual").isEqualTo(expected);
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
		Set<SemanticNode> actual = parseSemanticAst(source);

		Set<SemanticNode> expected = Set.of(
			semanticNode(COMPILATION_UNIT, 0, 86),
			semanticNode(CLASS_DECLARATION, 7, 85, "Logger"),
			semanticNode(CONSTRUCTOR_DECLARATION, 23, 83),
			semanticNode(PARAMETER_DECLARATION, 37, 48, "name"),
			semanticNode(QUALIFIED_NAME, 37, 43),
			semanticNode(BLOCK, 51, 83),
			semanticNode(METHOD_INVOCATION, 55, 79),
			semanticNode(QUALIFIED_NAME, 55, 73),
			semanticNode(FIELD_ACCESS, 55, 73),
			semanticNode(FIELD_ACCESS, 55, 65),
			semanticNode(IDENTIFIER, 55, 61),
			semanticNode(IDENTIFIER, 74, 78));

		requireThat(actual, "actual").isEqualTo(expected);
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
		Set<SemanticNode> actual = parseSemanticAst(source);

		Set<SemanticNode> expected = Set.of(
			semanticNode(COMPILATION_UNIT, 0, 45),
			semanticNode(CLASS_DECLARATION, 7, 44, "Empty"),
			semanticNode(CONSTRUCTOR_DECLARATION, 22, 42),
			semanticNode(BLOCK, 38, 42));

		requireThat(actual, "actual").isEqualTo(expected);
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
		Set<SemanticNode> actual = parseSemanticAst(source);

		Set<SemanticNode> expected = Set.of(
			semanticNode(COMPILATION_UNIT, 0, 138),
			semanticNode(CLASS_DECLARATION, 7, 137, "Entity"),
			semanticNode(QUALIFIED_NAME, 28, 32),
			semanticNode(CONSTRUCTOR_DECLARATION, 36, 135),
			semanticNode(PARAMETER_DECLARATION, 50, 61, "name"),
			semanticNode(QUALIFIED_NAME, 50, 56),
			semanticNode(PARAMETER_DECLARATION, 63, 69, "id"),
			semanticNode(BLOCK, 72, 135),
			semanticNode(METHOD_INVOCATION, 76, 94),
			semanticNode(QUALIFIED_NAME, 76, 88),
			semanticNode(IDENTIFIER, 76, 88),
			semanticNode(IDENTIFIER, 89, 93),
			semanticNode(METHOD_INVOCATION, 98, 112),
			semanticNode(QUALIFIED_NAME, 98, 108),
			semanticNode(IDENTIFIER, 98, 108),
			semanticNode(IDENTIFIER, 109, 111),
			semanticNode(METHOD_INVOCATION, 116, 131),
			semanticNode(SUPER_EXPRESSION, 116, 121),
			semanticNode(IDENTIFIER, 122, 126),
			semanticNode(IDENTIFIER, 128, 130));

		requireThat(actual, "actual").isEqualTo(expected);
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
		Set<SemanticNode> actual = parseSemanticAst(source);

		Set<SemanticNode> expected = Set.of(
			semanticNode(COMPILATION_UNIT, 0, 124),
			semanticNode(RECORD_DECLARATION, 7, 123, "Person"),
			semanticNode(PARAMETER_DECLARATION, 21, 32, "name"),
			semanticNode(QUALIFIED_NAME, 21, 27),
			semanticNode(PARAMETER_DECLARATION, 34, 41, "age"),
			semanticNode(BLOCK, 61, 121),
			semanticNode(IF_STATEMENT, 65, 118),
			semanticNode(BINARY_EXPRESSION, 69, 76),
			semanticNode(IDENTIFIER, 69, 72),
			semanticNode(INTEGER_LITERAL, 75, 76),
			semanticNode(THROW_STATEMENT, 81, 118),
			semanticNode(OBJECT_CREATION, 87, 117),
			semanticNode(QUALIFIED_NAME, 91, 115));

		requireThat(actual, "actual").isEqualTo(expected);
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
		Set<SemanticNode> actual = parseSemanticAst(source);

		Set<SemanticNode> expected = Set.of(
			semanticNode(COMPILATION_UNIT, 0, 173),
			semanticNode(CLASS_DECLARATION, 7, 172, "Outer"),
			semanticNode(CLASS_DECLARATION, 29, 170, "Inner"),
			semanticNode(QUALIFIED_NAME, 49, 53),
			semanticNode(CONSTRUCTOR_DECLARATION, 59, 167),
			semanticNode(PARAMETER_DECLARATION, 72, 81, "value"),
			semanticNode(BLOCK, 85, 167),
			semanticNode(IF_STATEMENT, 90, 146),
			semanticNode(BINARY_EXPRESSION, 94, 103),
			semanticNode(IDENTIFIER, 94, 99),
			semanticNode(INTEGER_LITERAL, 102, 103),
			semanticNode(THROW_STATEMENT, 109, 146),
			semanticNode(OBJECT_CREATION, 115, 145),
			semanticNode(QUALIFIED_NAME, 119, 143),
			semanticNode(METHOD_INVOCATION, 150, 162),
			semanticNode(SUPER_EXPRESSION, 150, 155),
			semanticNode(IDENTIFIER, 156, 161));

		requireThat(actual, "actual").isEqualTo(expected);
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
		Set<SemanticNode> actual = parseSemanticAst(source);

		Set<SemanticNode> expected = Set.of(
			semanticNode(COMPILATION_UNIT, 0, 107),
			semanticNode(CLASS_DECLARATION, 7, 106, "Derived"),
			semanticNode(QUALIFIED_NAME, 29, 33),
			semanticNode(CONSTRUCTOR_DECLARATION, 37, 104),
			semanticNode(PARAMETER_DECLARATION, 52, 57, "a"),
			semanticNode(PARAMETER_DECLARATION, 59, 64, "b"),
			semanticNode(BLOCK, 67, 104),
			semanticNode(BINARY_EXPRESSION, 81, 86),
			semanticNode(IDENTIFIER, 81, 82),
			semanticNode(IDENTIFIER, 85, 86),
			semanticNode(METHOD_INVOCATION, 90, 100),
			semanticNode(SUPER_EXPRESSION, 90, 95),
			semanticNode(IDENTIFIER, 96, 99));

		requireThat(actual, "actual").isEqualTo(expected);
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
		Set<SemanticNode> actual = parseSemanticAst(source);

		Set<SemanticNode> expected = Set.of(
			semanticNode(COMPILATION_UNIT, 0, 139),
			semanticNode(CLASS_DECLARATION, 7, 138, "Processor"),
			semanticNode(QUALIFIED_NAME, 31, 35),
			semanticNode(CONSTRUCTOR_DECLARATION, 39, 136),
			semanticNode(PARAMETER_DECLARATION, 56, 65, "value"),
			semanticNode(BLOCK, 68, 136),
			semanticNode(QUALIFIED_NAME, 72, 80),
			semanticNode(LAMBDA_EXPRESSION, 85, 116),
			semanticNode(METHOD_INVOCATION, 91, 116),
			semanticNode(FIELD_ACCESS, 91, 109),
			semanticNode(FIELD_ACCESS, 91, 101),
			semanticNode(IDENTIFIER, 91, 97),
			semanticNode(IDENTIFIER, 110, 115),
			semanticNode(METHOD_INVOCATION, 120, 132),
			semanticNode(SUPER_EXPRESSION, 120, 125),
			semanticNode(IDENTIFIER, 126, 131));

		requireThat(actual, "actual").isEqualTo(expected);
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
		Set<SemanticNode> actual = parseSemanticAst(source);

		Set<SemanticNode> expected = Set.of(
			semanticNode(COMPILATION_UNIT, 0, 160),
			semanticNode(CLASS_DECLARATION, 7, 159, "Handler"),
			semanticNode(QUALIFIED_NAME, 29, 33),
			semanticNode(CONSTRUCTOR_DECLARATION, 37, 157),
			semanticNode(PARAMETER_DECLARATION, 52, 61, "value"),
			semanticNode(BLOCK, 64, 157),
			semanticNode(QUALIFIED_NAME, 68, 76),
			semanticNode(OBJECT_CREATION, 84, 137),
			semanticNode(QUALIFIED_NAME, 88, 96),
			semanticNode(METHOD_DECLARATION, 106, 133),
			semanticNode(BLOCK, 127, 133),
			semanticNode(METHOD_INVOCATION, 141, 153),
			semanticNode(SUPER_EXPRESSION, 141, 146),
			semanticNode(IDENTIFIER, 147, 152));

		requireThat(actual, "actual").isEqualTo(expected);
	}
}
