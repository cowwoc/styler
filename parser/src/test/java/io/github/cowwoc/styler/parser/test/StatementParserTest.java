package io.github.cowwoc.styler.parser.test;

import io.github.cowwoc.styler.parser.test.ParserTestUtils.SemanticNode;
import org.testng.annotations.Test;

import java.util.Set;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;
import static io.github.cowwoc.styler.parser.test.ParserTestUtils.parseSemanticAst;
import static io.github.cowwoc.styler.ast.core.NodeType.CLASS_DECLARATION;
import static io.github.cowwoc.styler.parser.test.ParserTestUtils.*;
import static io.github.cowwoc.styler.parser.test.ParserTestUtils.typeDeclaration;
import static io.github.cowwoc.styler.parser.test.ParserTestUtils.parameterNode;

/**
 * Tests for parsing statements: control flow, loops, try-catch, and declarations.
 */
public class StatementParserTest
{
	/**
	 * Validates parsing of basic if statement with condition and true block.
	 * Tests the simplest form of branching logic without else clause.
	 */
	@Test
	public void testIfStatement()
	{
		Set<SemanticNode> actual = parseSemanticAst("""
			public class Test
			{
				public void foo()
				{
					if (x > 0)
					{
						x = 1;
					}
				}
			}
			""");

		Set<SemanticNode> expected = Set.of(
			compilationUnit( 0, 78),
			typeDeclaration(CLASS_DECLARATION, 7, 77, "Test"),
			methodDeclaration( 21, 75),
			block( 40, 75),
			ifStatement( 44, 72),
			binaryExpression( 48, 53),
			identifier( 48, 49),
			integerLiteral( 52, 53),
			block( 57, 72),
			assignmentExpression( 62, 67),
			qualifiedName( 62, 63),
			identifier( 62, 63),
			integerLiteral( 66, 67));
		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Validates parsing of if-else statement with both branches.
	 * Tests conditional with alternative execution path providing complete two-way branching.
	 */
	@Test
	public void testIfElseStatement()
	{
		Set<SemanticNode> actual = parseSemanticAst("""
			public class Test
			{
				public void foo()
				{
					if (x > 0)
					{
						x = 1;
					}
					else
					{
						x = 0;
					}
				}
			}
			""");

		Set<SemanticNode> expected = Set.of(
			compilationUnit( 0, 103),
			typeDeclaration(CLASS_DECLARATION, 7, 102, "Test"),
			methodDeclaration( 21, 100),
			block( 40, 100),
			ifStatement( 44, 97),
			binaryExpression( 48, 53),
			identifier( 48, 49),
			integerLiteral( 52, 53),
			block( 57, 72),
			qualifiedName( 62, 63),
			identifier( 62, 63),
			integerLiteral( 66, 67),
			assignmentExpression( 62, 67),
			block( 82, 97),
			qualifiedName( 87, 88),
			identifier( 87, 88),
			integerLiteral( 91, 92),
			assignmentExpression( 87, 92));
		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Validates parsing of traditional for loop with initialization, condition, and increment.
	 * Tests three-part for syntax used for counted iteration.
	 */
	@Test
	public void testForLoop()
	{
		Set<SemanticNode> actual = parseSemanticAst("""
			public class Test
			{
				public void foo()
				{
					for (int i = 0; i < 10; ++i)
					{
						x = i;
					}
				}
			}
			""");

		Set<SemanticNode> expected = Set.of(
			compilationUnit( 0, 96),
			typeDeclaration(CLASS_DECLARATION, 7, 95, "Test"),
			methodDeclaration( 21, 93),
			block( 40, 93),
			forStatement( 44, 90),
			integerLiteral( 57, 58),
			binaryExpression( 60, 66),
			identifier( 60, 61),
			integerLiteral( 64, 66),
			unaryExpression( 68, 71),
			identifier( 70, 71),
			block( 75, 90),
			qualifiedName( 80, 81),
			identifier( 80, 81),
			identifier( 84, 85),
			assignmentExpression( 80, 85));
		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Validates parsing of enhanced for loop (for-each) syntax.
	 * Tests simplified iteration over collections and arrays using colon notation.
	 */
	@Test
	public void testEnhancedForLoop()
	{
		Set<SemanticNode> actual = parseSemanticAst("""
			public class Test
			{
				public void foo()
				{
					for (String s : list)
					{
						x = s;
					}
				}
			}
			""");

		Set<SemanticNode> expected = Set.of(
			compilationUnit( 0, 89),
			typeDeclaration(CLASS_DECLARATION, 7, 88, "Test"),
			methodDeclaration( 21, 86),
			block( 40, 86),
			enhancedForStatement( 44, 83),
			qualifiedName( 49, 55),
			identifier( 60, 64),
			block( 68, 83),
			qualifiedName( 73, 74),
			identifier( 73, 74),
			identifier( 77, 78),
			assignmentExpression( 73, 78));
		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Validates parsing of while loop with precondition check.
	 * Tests while syntax where condition is evaluated before each iteration.
	 */
	@Test
	public void testWhileLoop()
	{
		Set<SemanticNode> actual = parseSemanticAst("""
			public class Test
			{
				public void foo()
				{
					while (x > 0)
					{
						--x;
					}
				}
			}
			""");

		Set<SemanticNode> expected = Set.of(
			compilationUnit( 0, 79),
			typeDeclaration(CLASS_DECLARATION, 7, 78, "Test"),
			methodDeclaration( 21, 76),
			block( 40, 76),
			whileStatement( 44, 73),
			binaryExpression( 51, 56),
			identifier( 51, 52),
			integerLiteral( 55, 56),
			block( 60, 73),
			unaryExpression( 65, 68),
			identifier( 67, 68));
		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Validates parsing of do-while loop with postcondition check.
	 * Tests do-while syntax where body executes at least once before condition is tested.
	 */
	@Test
	public void testDoWhileLoop()
	{
		Set<SemanticNode> actual = parseSemanticAst("""
			public class Test
			{
				public void foo()
				{
					do
					{
						--x;
					}
					while (x > 0);
				}
			}
			""");

		Set<SemanticNode> expected = Set.of(
			compilationUnit( 0, 85),
			typeDeclaration(CLASS_DECLARATION, 7, 84, "Test"),
			methodDeclaration( 21, 82),
			block( 40, 82),
			doWhileStatement( 44, 79),
			block( 49, 62),
			unaryExpression( 54, 57),
			identifier( 56, 57),
			binaryExpression( 72, 77),
			identifier( 72, 73),
			integerLiteral( 76, 77));
		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Validates parsing of switch statement with multiple case labels and default.
	 * Tests multi-way branching including case values, break statements, and default.
	 */
	@Test
	public void testSwitchStatement()
	{
		Set<SemanticNode> actual = parseSemanticAst("""
			public class Test
			{
				public void foo()
				{
					switch (x)
					{
						case 1:
							y = 1;
							break;
						case 2:
							y = 2;
							break;
						default:
							y = 0;
					}
				}
			}
			""");

		Set<SemanticNode> expected = Set.of(
			compilationUnit( 0, 157),
			typeDeclaration(CLASS_DECLARATION, 7, 156, "Test"),
			methodDeclaration( 21, 154),
			block( 40, 154),
			switchStatement( 44, 151),
			identifier( 52, 53),
			integerLiteral( 67, 68),
			qualifiedName( 74, 75),
			identifier( 74, 75),
			integerLiteral( 78, 79),
			assignmentExpression( 74, 79),
			breakStatement( 85, 91),
			integerLiteral( 100, 101),
			qualifiedName( 107, 108),
			identifier( 107, 108),
			integerLiteral( 111, 112),
			assignmentExpression( 107, 112),
			breakStatement( 118, 124),
			qualifiedName( 141, 142),
			identifier( 141, 142),
			integerLiteral( 145, 146),
			assignmentExpression( 141, 146));
		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Validates parsing of return statement with expression.
	 * Tests method exit with value, essential for non-void methods.
	 */
	@Test
	public void testReturnStatement()
	{
		Set<SemanticNode> actual = parseSemanticAst("""
			public class Test
			{
				public int foo()
				{
					return 42;
				}
			}
			""");

		Set<SemanticNode> expected = Set.of(
			compilationUnit( 0, 59),
			typeDeclaration(CLASS_DECLARATION, 7, 58, "Test"),
			methodDeclaration( 21, 56),
			block( 39, 56),
			returnStatement( 43, 53),
			integerLiteral( 50, 52));
		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Validates parsing of throw statement for exception propagation.
	 * Tests explicit exception throwing which transfers control to nearest matching catch.
	 */
	@Test
	public void testThrowStatement()
	{
		Set<SemanticNode> actual = parseSemanticAst("""
			public class Test
			{
				public void foo()
				{
					throw new RuntimeException();
				}
			}
			""");

		Set<SemanticNode> expected = Set.of(
			compilationUnit( 0, 79),
			typeDeclaration(CLASS_DECLARATION, 7, 78, "Test"),
			methodDeclaration( 21, 76),
			block( 40, 76),
			throwStatement( 44, 73),
			objectCreation( 50, 72),
			qualifiedName( 54, 70));
		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Validates parsing of try-catch block for exception handling.
	 * Tests exception handling with try body and catch clause for recovery.
	 */
	@Test
	public void testTryStatement()
	{
		Set<SemanticNode> actual = parseSemanticAst("""
			public class Test
			{
				public void foo()
				{
					try
					{
						x = 1;
					}
					catch (Exception e)
					{
						x = 0;
					}
				}
			}
			""");

		Set<SemanticNode> expected = Set.of(
			compilationUnit( 0, 111),
			typeDeclaration(CLASS_DECLARATION, 7, 110, "Test"),
			methodDeclaration( 21, 108),
			block( 40, 108),
			tryStatement( 44, 105),
			block( 50, 65),
			catchClause( 68, 105),
			qualifiedName( 75, 84),
			parameterNode( 75, 86, "e"),
			block( 90, 105),
			qualifiedName( 55, 56),
			identifier( 55, 56),
			integerLiteral( 59, 60),
			assignmentExpression( 55, 60),
			qualifiedName( 95, 96),
			identifier( 95, 96),
			integerLiteral( 99, 100),
			assignmentExpression( 95, 100));
		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Validates parsing of try-finally block without catch clause.
	 * Tests finally block that executes regardless of exceptions for cleanup.
	 */
	@Test
	public void testTryFinally()
	{
		Set<SemanticNode> actual = parseSemanticAst("""
			public class Test
			{
				public void foo()
				{
					try
					{
						x = 1;
					}
					finally
					{
						y = 0;
					}
				}
			}
			""");

		Set<SemanticNode> expected = Set.of(
			compilationUnit( 0, 99),
			typeDeclaration(CLASS_DECLARATION, 7, 98, "Test"),
			methodDeclaration( 21, 96),
			block( 40, 96),
			tryStatement( 44, 93),
			block( 50, 65),
			finallyClause( 68, 93),
			block( 78, 93),
			qualifiedName( 55, 56),
			identifier( 55, 56),
			integerLiteral( 59, 60),
			assignmentExpression( 55, 60),
			qualifiedName( 83, 84),
			identifier( 83, 84),
			integerLiteral( 87, 88),
			assignmentExpression( 83, 88));
		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Validates parsing of try-with-resources for automatic resource management.
	 * Tests try statement with resource declarations ensuring automatic closure.
	 */
	@Test
	public void testTryWithResources()
	{
		Set<SemanticNode> actual = parseSemanticAst("""
			public class Test
			{
				public void foo()
				{
					try (Reader r = new FileReader())
					{
						x = 1;
					}
				}
			}
			""");

		Set<SemanticNode> expected = Set.of(
			compilationUnit( 0, 101),
			typeDeclaration(CLASS_DECLARATION, 7, 100, "Test"),
			methodDeclaration( 21, 98),
			block( 40, 98),
			tryStatement( 44, 95),
			qualifiedName( 49, 55),
			objectCreation( 60, 76),
			qualifiedName( 64, 74),
			block( 80, 95),
			qualifiedName( 85, 86),
			identifier( 85, 86),
			integerLiteral( 89, 90),
			assignmentExpression( 85, 90));
		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Validates parsing of synchronized block for thread synchronization.
	 * Tests synchronized statement with monitor object for mutual exclusion.
	 */
	@Test
	public void testSynchronizedStatement()
	{
		Set<SemanticNode> actual = parseSemanticAst("""
			public class Test
			{
				public void foo()
				{
					synchronized (lock)
					{
						x = 1;
					}
				}
			}
			""");

		Set<SemanticNode> expected = Set.of(
			compilationUnit( 0, 87),
			typeDeclaration(CLASS_DECLARATION, 7, 86, "Test"),
			methodDeclaration( 21, 84),
			block( 40, 84),
			synchronizedStatement( 44, 81),
			identifier( 58, 62),
			block( 66, 81),
			qualifiedName( 71, 72),
			identifier( 71, 72),
			integerLiteral( 75, 76),
			assignmentExpression( 71, 76));
		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Validates parsing of break statement for loop termination.
	 * Tests break that immediately exits innermost loop or switch.
	 */
	@Test
	public void testBreakStatement()
	{
		Set<SemanticNode> actual = parseSemanticAst("""
			public class Test
			{
				public void foo()
				{
					while (true)
					{
						break;
					}
				}
			}
			""");

		Set<SemanticNode> expected = Set.of(
			compilationUnit( 0, 80),
			typeDeclaration(CLASS_DECLARATION, 7, 79, "Test"),
			methodDeclaration( 21, 77),
			block( 40, 77),
			whileStatement( 44, 74),
			booleanLiteral( 51, 55),
			block( 59, 74),
			breakStatement( 64, 70));
		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Validates parsing of continue statement for loop iteration control.
	 * Tests continue that skips remaining loop body and proceeds to next iteration.
	 */
	@Test
	public void testContinueStatement()
	{
		Set<SemanticNode> actual = parseSemanticAst("""
			public class Test
			{
				public void foo()
				{
					while (true)
					{
						continue;
					}
				}
			}
			""");

		Set<SemanticNode> expected = Set.of(
			compilationUnit( 0, 83),
			typeDeclaration(CLASS_DECLARATION, 7, 82, "Test"),
			methodDeclaration( 21, 80),
			block( 40, 80),
			whileStatement( 44, 77),
			booleanLiteral( 51, 55),
			block( 59, 77),
			continueStatement( 64, 73));
		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Validates parsing of assert statement for runtime validation.
	 * Tests assertion with boolean condition for design-by-contract checks.
	 */
	@Test
	public void testAssertStatement()
	{
		Set<SemanticNode> actual = parseSemanticAst("""
			public class Test
			{
				public void foo()
				{
					assert x > 0;
				}
			}
			""");

		Set<SemanticNode> expected = Set.of(
			compilationUnit( 0, 63),
			typeDeclaration(CLASS_DECLARATION, 7, 62, "Test"),
			methodDeclaration( 21, 60),
			block( 40, 60),
			assertStatement( 44, 57),
			binaryExpression( 51, 56),
			identifier( 51, 52),
			integerLiteral( 55, 56));
		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Validates parsing of local variable declaration with initialization.
	 * Tests variable declaration statement with type, name, and initializer expression.
	 */
	@Test
	public void testVariableDeclaration()
	{
		Set<SemanticNode> actual = parseSemanticAst("""
			public class Test
			{
				public void foo()
				{
					int x = 5;
				}
			}
			""");

		Set<SemanticNode> expected = Set.of(
			compilationUnit( 0, 60),
			typeDeclaration(CLASS_DECLARATION, 7, 59, "Test"),
			methodDeclaration( 21, 57),
			block( 40, 57),
			integerLiteral( 52, 53));
		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Validates parsing of multiple variable declarations in single statement.
	 * Tests comma-separated declarators with shared type and individual initializers.
	 */
	@Test
	public void testMultipleVariableDeclarations()
	{
		Set<SemanticNode> actual = parseSemanticAst("""
			public class Test
			{
				public void foo()
				{
					int x = 1, y = 2, z = 3;
				}
			}
			""");

		Set<SemanticNode> expected = Set.of(
			compilationUnit( 0, 74),
			typeDeclaration(CLASS_DECLARATION, 7, 73, "Test"),
			methodDeclaration( 21, 71),
			block( 40, 71),
			integerLiteral( 52, 53),
			integerLiteral( 59, 60),
			integerLiteral( 66, 67));
		requireThat(actual, "actual").isEqualTo(expected);
	}
}
