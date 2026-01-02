package io.github.cowwoc.styler.parser.test;

import io.github.cowwoc.styler.parser.test.ParserTestUtils.SemanticNode;
import org.testng.annotations.Test;

import java.util.Set;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;
import static io.github.cowwoc.styler.parser.test.ParserTestUtils.parseSemanticAst;
import static io.github.cowwoc.styler.ast.core.NodeType.CLASS_DECLARATION;
import static io.github.cowwoc.styler.ast.core.NodeType.RECORD_DECLARATION;
import static io.github.cowwoc.styler.parser.test.ParserTestUtils.*;
import static io.github.cowwoc.styler.parser.test.ParserTestUtils.typeDeclaration;
import static io.github.cowwoc.styler.parser.test.ParserTestUtils.parameterNode;

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
			compilationUnit( 0, 143),
			typeDeclaration(CLASS_DECLARATION, 7, 142, "Child"),
			qualifiedName( 27, 33),
			constructorDeclaration( 37, 140),
			parameterNode( 50, 59, "value"),
			block( 62, 140),
			ifStatement( 66, 121),
			binaryExpression( 70, 79),
			identifier( 70, 75),
			integerLiteral( 78, 79),
			throwStatement( 84, 121),
			objectCreation( 90, 120),
			qualifiedName( 94, 118),
			methodInvocation( 124, 136),
			superExpression( 124, 129),
			identifier( 130, 135));

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
			compilationUnit( 0, 119),
			typeDeclaration(CLASS_DECLARATION, 7, 118, "Point"),
			qualifiedName( 27, 31),
			constructorDeclaration( 35, 116),
			parameterNode( 48, 53, "a"),
			parameterNode( 55, 60, "b"),
			block( 63, 116),
			binaryExpression( 75, 80),
			identifier( 75, 76),
			integerLiteral( 79, 80),
			binaryExpression( 92, 97),
			identifier( 92, 93),
			integerLiteral( 96, 97),
			methodInvocation( 101, 112),
			superExpression( 101, 106),
			identifier( 107, 108),
			identifier( 110, 111));

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
			compilationUnit( 0, 103),
			typeDeclaration(CLASS_DECLARATION, 7, 102, "Data"),
			qualifiedName( 26, 30),
			constructorDeclaration( 34, 100),
			parameterNode( 46, 58, "value"),
			qualifiedName( 46, 52),
			block( 61, 100),
			methodInvocation( 65, 80),
			qualifiedName( 65, 73),
			identifier( 65, 73),
			identifier( 74, 79),
			methodInvocation( 84, 96),
			superExpression( 84, 89),
			identifier( 90, 95));

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
			compilationUnit( 0, 168),
			typeDeclaration(CLASS_DECLARATION, 7, 167, "Wrapper"),
			qualifiedName( 29, 33),
			constructorDeclaration( 37, 165),
			parameterNode( 52, 61, "value"),
			block( 64, 165),
			ifStatement( 84, 143),
			binaryExpression( 88, 97),
			identifier( 88, 93),
			integerLiteral( 96, 97),
			assignmentExpression( 102, 114),
			qualifiedName( 102, 110),
			identifier( 102, 110),
			integerLiteral( 113, 114),
			assignmentExpression( 126, 142),
			qualifiedName( 126, 134),
			identifier( 126, 134),
			identifier( 137, 142),
			methodInvocation( 146, 161),
			superExpression( 146, 151),
			identifier( 152, 160));

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
			compilationUnit( 0, 205),
			typeDeclaration(CLASS_DECLARATION, 7, 204, "Parser"),
			qualifiedName( 28, 32),
			constructorDeclaration( 36, 202),
			parameterNode( 50, 61, "text"),
			qualifiedName( 50, 56),
			block( 64, 202),
			tryStatement( 81, 183),
			block( 87, 127),
			assignmentExpression( 92, 122),
			qualifiedName( 92, 97),
			identifier( 92, 97),
			methodInvocation( 100, 122),
			fieldAccess( 100, 116),
			identifier( 100, 107),
			identifier( 117, 121),
			catchClause( 130, 183),
			parameterNode( 137, 160, "e"),
			qualifiedName( 137, 158),
			block( 164, 183),
			assignmentExpression( 169, 178),
			identifier( 169, 174),
			qualifiedName( 169, 174),
			integerLiteral( 177, 178),
			methodInvocation( 186, 198),
			superExpression( 186, 191),
			identifier( 192, 197));

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
			compilationUnit( 0, 208),
			typeDeclaration(CLASS_DECLARATION, 7, 207, "Handler"),
			qualifiedName( 29, 33),
			constructorDeclaration( 37, 205),
			parameterNode( 52, 60, "type"),
			block( 63, 205),
			qualifiedName( 67, 73),
			switchStatement( 82, 187),
			identifier( 90, 94),
			integerLiteral( 108, 109),
			assignmentExpression( 113, 125),
			qualifiedName( 113, 117),
			identifier( 113, 117),
			stringLiteral( 120, 125),
			integerLiteral( 135, 136),
			assignmentExpression( 140, 152),
			identifier( 140, 144),
			qualifiedName( 140, 144),
			stringLiteral( 147, 152),
			assignmentExpression( 168, 182),
			identifier( 168, 172),
			qualifiedName( 168, 172),
			stringLiteral( 175, 182),
			methodInvocation( 190, 201),
			superExpression( 190, 195),
			identifier( 196, 200));

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
			compilationUnit( 0, 165),
			typeDeclaration(CLASS_DECLARATION, 7, 164, "Range"),
			constructorDeclaration( 22, 121),
			parameterNode( 35, 42, "end"),
			block( 45, 121),
			ifStatement( 49, 102),
			binaryExpression( 53, 60),
			identifier( 53, 56),
			integerLiteral( 59, 60),
			throwStatement( 65, 102),
			objectCreation( 71, 101),
			qualifiedName( 75, 99),
			methodInvocation( 105, 117),
			thisExpression( 105, 109),
			integerLiteral( 110, 111),
			identifier( 113, 116),
			constructorDeclaration( 124, 162),
			parameterNode( 137, 146, "start"),
			parameterNode( 148, 155, "end"),
			block( 158, 162));

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
			compilationUnit( 0, 173),
			typeDeclaration(CLASS_DECLARATION, 7, 172, "Size"),
			constructorDeclaration( 21, 127),
			parameterNode( 33, 46, "dimension"),
			block( 49, 127),
			identifier( 65, 74),
			identifier( 91, 100),
			methodInvocation( 104, 123),
			thisExpression( 104, 108),
			identifier( 109, 114),
			identifier( 116, 122),
			constructorDeclaration( 130, 170),
			parameterNode( 142, 151, "width"),
			parameterNode( 153, 163, "height"),
			block( 166, 170));

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
			compilationUnit( 0, 86),
			typeDeclaration(CLASS_DECLARATION, 7, 85, "Logger"),
			constructorDeclaration( 23, 83),
			parameterNode( 37, 48, "name"),
			qualifiedName( 37, 43),
			block( 51, 83),
			methodInvocation( 55, 79),
			qualifiedName( 55, 73),
			fieldAccess( 55, 73),
			fieldAccess( 55, 65),
			identifier( 55, 61),
			identifier( 74, 78));

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
			compilationUnit( 0, 45),
			typeDeclaration(CLASS_DECLARATION, 7, 44, "Empty"),
			constructorDeclaration( 22, 42),
			block( 38, 42));

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
			compilationUnit( 0, 138),
			typeDeclaration(CLASS_DECLARATION, 7, 137, "Entity"),
			qualifiedName( 28, 32),
			constructorDeclaration( 36, 135),
			parameterNode( 50, 61, "name"),
			qualifiedName( 50, 56),
			parameterNode( 63, 69, "id"),
			block( 72, 135),
			methodInvocation( 76, 94),
			qualifiedName( 76, 88),
			identifier( 76, 88),
			identifier( 89, 93),
			methodInvocation( 98, 112),
			qualifiedName( 98, 108),
			identifier( 98, 108),
			identifier( 109, 111),
			methodInvocation( 116, 131),
			superExpression( 116, 121),
			identifier( 122, 126),
			identifier( 128, 130));

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
			compilationUnit( 0, 124),
			typeDeclaration(RECORD_DECLARATION, 7, 123, "Person"),
			parameterNode( 21, 32, "name"),
			qualifiedName( 21, 27),
			parameterNode( 34, 41, "age"),
			block( 61, 121),
			ifStatement( 65, 118),
			binaryExpression( 69, 76),
			identifier( 69, 72),
			integerLiteral( 75, 76),
			throwStatement( 81, 118),
			objectCreation( 87, 117),
			qualifiedName( 91, 115));

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
			compilationUnit( 0, 173),
			typeDeclaration(CLASS_DECLARATION, 7, 172, "Outer"),
			typeDeclaration(CLASS_DECLARATION, 29, 170, "Inner"),
			qualifiedName( 49, 53),
			constructorDeclaration( 59, 167),
			parameterNode( 72, 81, "value"),
			block( 85, 167),
			ifStatement( 90, 146),
			binaryExpression( 94, 103),
			identifier( 94, 99),
			integerLiteral( 102, 103),
			throwStatement( 109, 146),
			objectCreation( 115, 145),
			qualifiedName( 119, 143),
			methodInvocation( 150, 162),
			superExpression( 150, 155),
			identifier( 156, 161));

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
			compilationUnit( 0, 107),
			typeDeclaration(CLASS_DECLARATION, 7, 106, "Derived"),
			qualifiedName( 29, 33),
			constructorDeclaration( 37, 104),
			parameterNode( 52, 57, "a"),
			parameterNode( 59, 64, "b"),
			block( 67, 104),
			binaryExpression( 81, 86),
			identifier( 81, 82),
			identifier( 85, 86),
			methodInvocation( 90, 100),
			superExpression( 90, 95),
			identifier( 96, 99));

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
			compilationUnit( 0, 139),
			typeDeclaration(CLASS_DECLARATION, 7, 138, "Processor"),
			qualifiedName( 31, 35),
			constructorDeclaration( 39, 136),
			parameterNode( 56, 65, "value"),
			block( 68, 136),
			qualifiedName( 72, 80),
			lambdaExpression( 85, 116),
			methodInvocation( 91, 116),
			fieldAccess( 91, 109),
			fieldAccess( 91, 101),
			identifier( 91, 97),
			identifier( 110, 115),
			methodInvocation( 120, 132),
			superExpression( 120, 125),
			identifier( 126, 131));

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
			compilationUnit( 0, 160),
			typeDeclaration(CLASS_DECLARATION, 7, 159, "Handler"),
			qualifiedName( 29, 33),
			constructorDeclaration( 37, 157),
			parameterNode( 52, 61, "value"),
			block( 64, 157),
			qualifiedName( 68, 76),
			objectCreation( 84, 137),
			qualifiedName( 88, 96),
			methodDeclaration( 106, 133),
			block( 127, 133),
			methodInvocation( 141, 153),
			superExpression( 141, 146),
			identifier( 147, 152));

		requireThat(actual, "actual").isEqualTo(expected);
	}
}
