package io.github.cowwoc.styler.parser.test;

import io.github.cowwoc.styler.parser.test.ParserTestUtils.SemanticNode;
import org.testng.annotations.Test;

import java.util.Set;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;
import static io.github.cowwoc.styler.ast.core.NodeType.ASSERT_STATEMENT;
import static io.github.cowwoc.styler.ast.core.NodeType.ASSIGNMENT_EXPRESSION;
import static io.github.cowwoc.styler.ast.core.NodeType.BINARY_EXPRESSION;
import static io.github.cowwoc.styler.ast.core.NodeType.BLOCK;
import static io.github.cowwoc.styler.ast.core.NodeType.BOOLEAN_LITERAL;
import static io.github.cowwoc.styler.ast.core.NodeType.BREAK_STATEMENT;
import static io.github.cowwoc.styler.ast.core.NodeType.CATCH_CLAUSE;
import static io.github.cowwoc.styler.ast.core.NodeType.CLASS_DECLARATION;
import static io.github.cowwoc.styler.ast.core.NodeType.COMPILATION_UNIT;
import static io.github.cowwoc.styler.ast.core.NodeType.CONTINUE_STATEMENT;
import static io.github.cowwoc.styler.ast.core.NodeType.DO_WHILE_STATEMENT;
import static io.github.cowwoc.styler.ast.core.NodeType.ENHANCED_FOR_STATEMENT;
import static io.github.cowwoc.styler.ast.core.NodeType.FINALLY_CLAUSE;
import static io.github.cowwoc.styler.ast.core.NodeType.FOR_STATEMENT;
import static io.github.cowwoc.styler.ast.core.NodeType.IDENTIFIER;
import static io.github.cowwoc.styler.ast.core.NodeType.IF_STATEMENT;
import static io.github.cowwoc.styler.ast.core.NodeType.INTEGER_LITERAL;
import static io.github.cowwoc.styler.ast.core.NodeType.METHOD_DECLARATION;
import static io.github.cowwoc.styler.ast.core.NodeType.OBJECT_CREATION;
import static io.github.cowwoc.styler.ast.core.NodeType.QUALIFIED_NAME;
import static io.github.cowwoc.styler.ast.core.NodeType.RETURN_STATEMENT;
import static io.github.cowwoc.styler.ast.core.NodeType.SWITCH_STATEMENT;
import static io.github.cowwoc.styler.ast.core.NodeType.SYNCHRONIZED_STATEMENT;
import static io.github.cowwoc.styler.ast.core.NodeType.THROW_STATEMENT;
import static io.github.cowwoc.styler.ast.core.NodeType.TRY_STATEMENT;
import static io.github.cowwoc.styler.ast.core.NodeType.UNARY_EXPRESSION;
import static io.github.cowwoc.styler.ast.core.NodeType.WHILE_STATEMENT;
import static io.github.cowwoc.styler.parser.test.ParserTestUtils.parseSemanticAst;
import static io.github.cowwoc.styler.parser.test.ParserTestUtils.semanticNode;

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
			semanticNode(COMPILATION_UNIT, 0, 78),
			semanticNode(CLASS_DECLARATION, 7, 77, "Test"),
			semanticNode(METHOD_DECLARATION, 21, 75),
			semanticNode(BLOCK, 40, 75),
			semanticNode(IF_STATEMENT, 44, 72),
			semanticNode(BINARY_EXPRESSION, 48, 53),
			semanticNode(IDENTIFIER, 48, 49),
			semanticNode(INTEGER_LITERAL, 52, 53),
			semanticNode(BLOCK, 57, 72),
			semanticNode(ASSIGNMENT_EXPRESSION, 62, 67),
			semanticNode(QUALIFIED_NAME, 62, 63),
			semanticNode(IDENTIFIER, 62, 63),
			semanticNode(INTEGER_LITERAL, 66, 67));
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
			semanticNode(COMPILATION_UNIT, 0, 103),
			semanticNode(CLASS_DECLARATION, 7, 102, "Test"),
			semanticNode(METHOD_DECLARATION, 21, 100),
			semanticNode(BLOCK, 40, 100),
			semanticNode(IF_STATEMENT, 44, 97),
			semanticNode(BINARY_EXPRESSION, 48, 53),
			semanticNode(IDENTIFIER, 48, 49),
			semanticNode(INTEGER_LITERAL, 52, 53),
			semanticNode(BLOCK, 57, 72),
			semanticNode(QUALIFIED_NAME, 62, 63),
			semanticNode(IDENTIFIER, 62, 63),
			semanticNode(INTEGER_LITERAL, 66, 67),
			semanticNode(ASSIGNMENT_EXPRESSION, 62, 67),
			semanticNode(BLOCK, 82, 97),
			semanticNode(QUALIFIED_NAME, 87, 88),
			semanticNode(IDENTIFIER, 87, 88),
			semanticNode(INTEGER_LITERAL, 91, 92),
			semanticNode(ASSIGNMENT_EXPRESSION, 87, 92));
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
			semanticNode(COMPILATION_UNIT, 0, 96),
			semanticNode(CLASS_DECLARATION, 7, 95, "Test"),
			semanticNode(METHOD_DECLARATION, 21, 93),
			semanticNode(BLOCK, 40, 93),
			semanticNode(FOR_STATEMENT, 44, 90),
			semanticNode(INTEGER_LITERAL, 57, 58),
			semanticNode(BINARY_EXPRESSION, 60, 66),
			semanticNode(IDENTIFIER, 60, 61),
			semanticNode(INTEGER_LITERAL, 64, 66),
			semanticNode(UNARY_EXPRESSION, 68, 71),
			semanticNode(IDENTIFIER, 70, 71),
			semanticNode(BLOCK, 75, 90),
			semanticNode(QUALIFIED_NAME, 80, 81),
			semanticNode(IDENTIFIER, 80, 81),
			semanticNode(IDENTIFIER, 84, 85),
			semanticNode(ASSIGNMENT_EXPRESSION, 80, 85));
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
			semanticNode(COMPILATION_UNIT, 0, 89),
			semanticNode(CLASS_DECLARATION, 7, 88, "Test"),
			semanticNode(METHOD_DECLARATION, 21, 86),
			semanticNode(BLOCK, 40, 86),
			semanticNode(ENHANCED_FOR_STATEMENT, 44, 83),
			semanticNode(QUALIFIED_NAME, 49, 55),
			semanticNode(IDENTIFIER, 60, 64),
			semanticNode(BLOCK, 68, 83),
			semanticNode(QUALIFIED_NAME, 73, 74),
			semanticNode(IDENTIFIER, 73, 74),
			semanticNode(IDENTIFIER, 77, 78),
			semanticNode(ASSIGNMENT_EXPRESSION, 73, 78));
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
			semanticNode(COMPILATION_UNIT, 0, 79),
			semanticNode(CLASS_DECLARATION, 7, 78, "Test"),
			semanticNode(METHOD_DECLARATION, 21, 76),
			semanticNode(BLOCK, 40, 76),
			semanticNode(WHILE_STATEMENT, 44, 73),
			semanticNode(BINARY_EXPRESSION, 51, 56),
			semanticNode(IDENTIFIER, 51, 52),
			semanticNode(INTEGER_LITERAL, 55, 56),
			semanticNode(BLOCK, 60, 73),
			semanticNode(UNARY_EXPRESSION, 65, 68),
			semanticNode(IDENTIFIER, 67, 68));
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
			semanticNode(COMPILATION_UNIT, 0, 85),
			semanticNode(CLASS_DECLARATION, 7, 84, "Test"),
			semanticNode(METHOD_DECLARATION, 21, 82),
			semanticNode(BLOCK, 40, 82),
			semanticNode(DO_WHILE_STATEMENT, 44, 79),
			semanticNode(BLOCK, 49, 62),
			semanticNode(UNARY_EXPRESSION, 54, 57),
			semanticNode(IDENTIFIER, 56, 57),
			semanticNode(BINARY_EXPRESSION, 72, 77),
			semanticNode(IDENTIFIER, 72, 73),
			semanticNode(INTEGER_LITERAL, 76, 77));
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
			semanticNode(COMPILATION_UNIT, 0, 157),
			semanticNode(CLASS_DECLARATION, 7, 156, "Test"),
			semanticNode(METHOD_DECLARATION, 21, 154),
			semanticNode(BLOCK, 40, 154),
			semanticNode(SWITCH_STATEMENT, 44, 151),
			semanticNode(IDENTIFIER, 52, 53),
			semanticNode(INTEGER_LITERAL, 67, 68),
			semanticNode(QUALIFIED_NAME, 74, 75),
			semanticNode(IDENTIFIER, 74, 75),
			semanticNode(INTEGER_LITERAL, 78, 79),
			semanticNode(ASSIGNMENT_EXPRESSION, 74, 79),
			semanticNode(BREAK_STATEMENT, 85, 91),
			semanticNode(INTEGER_LITERAL, 100, 101),
			semanticNode(QUALIFIED_NAME, 107, 108),
			semanticNode(IDENTIFIER, 107, 108),
			semanticNode(INTEGER_LITERAL, 111, 112),
			semanticNode(ASSIGNMENT_EXPRESSION, 107, 112),
			semanticNode(BREAK_STATEMENT, 118, 124),
			semanticNode(QUALIFIED_NAME, 141, 142),
			semanticNode(IDENTIFIER, 141, 142),
			semanticNode(INTEGER_LITERAL, 145, 146),
			semanticNode(ASSIGNMENT_EXPRESSION, 141, 146));
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
			semanticNode(COMPILATION_UNIT, 0, 59),
			semanticNode(CLASS_DECLARATION, 7, 58, "Test"),
			semanticNode(METHOD_DECLARATION, 21, 56),
			semanticNode(BLOCK, 39, 56),
			semanticNode(RETURN_STATEMENT, 43, 53),
			semanticNode(INTEGER_LITERAL, 50, 52));
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
			semanticNode(COMPILATION_UNIT, 0, 79),
			semanticNode(CLASS_DECLARATION, 7, 78, "Test"),
			semanticNode(METHOD_DECLARATION, 21, 76),
			semanticNode(BLOCK, 40, 76),
			semanticNode(THROW_STATEMENT, 44, 73),
			semanticNode(OBJECT_CREATION, 50, 72),
			semanticNode(QUALIFIED_NAME, 54, 70));
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
			semanticNode(COMPILATION_UNIT, 0, 111),
			semanticNode(CLASS_DECLARATION, 7, 110, "Test"),
			semanticNode(METHOD_DECLARATION, 21, 108),
			semanticNode(BLOCK, 40, 108),
			semanticNode(TRY_STATEMENT, 44, 105),
			semanticNode(BLOCK, 50, 65),
			semanticNode(CATCH_CLAUSE, 68, 105),
			semanticNode(QUALIFIED_NAME, 75, 84),
			semanticNode(BLOCK, 90, 105),
			semanticNode(QUALIFIED_NAME, 55, 56),
			semanticNode(IDENTIFIER, 55, 56),
			semanticNode(INTEGER_LITERAL, 59, 60),
			semanticNode(ASSIGNMENT_EXPRESSION, 55, 60),
			semanticNode(QUALIFIED_NAME, 95, 96),
			semanticNode(IDENTIFIER, 95, 96),
			semanticNode(INTEGER_LITERAL, 99, 100),
			semanticNode(ASSIGNMENT_EXPRESSION, 95, 100));
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
			semanticNode(COMPILATION_UNIT, 0, 99),
			semanticNode(CLASS_DECLARATION, 7, 98, "Test"),
			semanticNode(METHOD_DECLARATION, 21, 96),
			semanticNode(BLOCK, 40, 96),
			semanticNode(TRY_STATEMENT, 44, 93),
			semanticNode(BLOCK, 50, 65),
			semanticNode(FINALLY_CLAUSE, 68, 93),
			semanticNode(BLOCK, 78, 93),
			semanticNode(QUALIFIED_NAME, 55, 56),
			semanticNode(IDENTIFIER, 55, 56),
			semanticNode(INTEGER_LITERAL, 59, 60),
			semanticNode(ASSIGNMENT_EXPRESSION, 55, 60),
			semanticNode(QUALIFIED_NAME, 83, 84),
			semanticNode(IDENTIFIER, 83, 84),
			semanticNode(INTEGER_LITERAL, 87, 88),
			semanticNode(ASSIGNMENT_EXPRESSION, 83, 88));
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
			semanticNode(COMPILATION_UNIT, 0, 101),
			semanticNode(CLASS_DECLARATION, 7, 100, "Test"),
			semanticNode(METHOD_DECLARATION, 21, 98),
			semanticNode(BLOCK, 40, 98),
			semanticNode(TRY_STATEMENT, 44, 95),
			semanticNode(QUALIFIED_NAME, 49, 55),
			semanticNode(OBJECT_CREATION, 60, 76),
			semanticNode(QUALIFIED_NAME, 64, 74),
			semanticNode(BLOCK, 80, 95),
			semanticNode(QUALIFIED_NAME, 85, 86),
			semanticNode(IDENTIFIER, 85, 86),
			semanticNode(INTEGER_LITERAL, 89, 90),
			semanticNode(ASSIGNMENT_EXPRESSION, 85, 90));
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
			semanticNode(COMPILATION_UNIT, 0, 87),
			semanticNode(CLASS_DECLARATION, 7, 86, "Test"),
			semanticNode(METHOD_DECLARATION, 21, 84),
			semanticNode(BLOCK, 40, 84),
			semanticNode(SYNCHRONIZED_STATEMENT, 44, 81),
			semanticNode(IDENTIFIER, 58, 62),
			semanticNode(BLOCK, 66, 81),
			semanticNode(QUALIFIED_NAME, 71, 72),
			semanticNode(IDENTIFIER, 71, 72),
			semanticNode(INTEGER_LITERAL, 75, 76),
			semanticNode(ASSIGNMENT_EXPRESSION, 71, 76));
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
			semanticNode(COMPILATION_UNIT, 0, 80),
			semanticNode(CLASS_DECLARATION, 7, 79, "Test"),
			semanticNode(METHOD_DECLARATION, 21, 77),
			semanticNode(BLOCK, 40, 77),
			semanticNode(WHILE_STATEMENT, 44, 74),
			semanticNode(BOOLEAN_LITERAL, 51, 55),
			semanticNode(BLOCK, 59, 74),
			semanticNode(BREAK_STATEMENT, 64, 70));
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
			semanticNode(COMPILATION_UNIT, 0, 83),
			semanticNode(CLASS_DECLARATION, 7, 82, "Test"),
			semanticNode(METHOD_DECLARATION, 21, 80),
			semanticNode(BLOCK, 40, 80),
			semanticNode(WHILE_STATEMENT, 44, 77),
			semanticNode(BOOLEAN_LITERAL, 51, 55),
			semanticNode(BLOCK, 59, 77),
			semanticNode(CONTINUE_STATEMENT, 64, 73));
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
			semanticNode(COMPILATION_UNIT, 0, 63),
			semanticNode(CLASS_DECLARATION, 7, 62, "Test"),
			semanticNode(METHOD_DECLARATION, 21, 60),
			semanticNode(BLOCK, 40, 60),
			semanticNode(ASSERT_STATEMENT, 44, 57),
			semanticNode(BINARY_EXPRESSION, 51, 56),
			semanticNode(IDENTIFIER, 51, 52),
			semanticNode(INTEGER_LITERAL, 55, 56));
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
			semanticNode(COMPILATION_UNIT, 0, 60),
			semanticNode(CLASS_DECLARATION, 7, 59, "Test"),
			semanticNode(METHOD_DECLARATION, 21, 57),
			semanticNode(BLOCK, 40, 57),
			semanticNode(INTEGER_LITERAL, 52, 53));
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
			semanticNode(COMPILATION_UNIT, 0, 74),
			semanticNode(CLASS_DECLARATION, 7, 73, "Test"),
			semanticNode(METHOD_DECLARATION, 21, 71),
			semanticNode(BLOCK, 40, 71),
			semanticNode(INTEGER_LITERAL, 52, 53),
			semanticNode(INTEGER_LITERAL, 59, 60),
			semanticNode(INTEGER_LITERAL, 66, 67));
		requireThat(actual, "actual").isEqualTo(expected);
	}
}
