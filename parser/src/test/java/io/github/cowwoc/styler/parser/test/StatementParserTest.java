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
		String source = """
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
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.IDENTIFIER, 48, 49);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 52, 53);
			expected.allocateNode(NodeType.BINARY_EXPRESSION, 48, 53);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 62, 63);
			expected.allocateNode(NodeType.IDENTIFIER, 62, 63);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 66, 67);
			expected.allocateNode(NodeType.ASSIGNMENT_EXPRESSION, 62, 67);
			expected.allocateNode(NodeType.BLOCK, 57, 72);
			expected.allocateNode(NodeType.IF_STATEMENT, 44, 72);
			expected.allocateNode(NodeType.BLOCK, 40, 75);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 21, 75);
			expected.allocateClassDeclaration(7, 77, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 78);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates parsing of if-else statement with both branches.
	 * Tests conditional with alternative execution path providing complete two-way branching.
	 */
	@Test
	public void testIfElseStatement()
	{
		String source = """
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
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.IDENTIFIER, 48, 49);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 52, 53);
			expected.allocateNode(NodeType.BINARY_EXPRESSION, 48, 53);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 62, 63);
			expected.allocateNode(NodeType.IDENTIFIER, 62, 63);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 66, 67);
			expected.allocateNode(NodeType.ASSIGNMENT_EXPRESSION, 62, 67);
			expected.allocateNode(NodeType.BLOCK, 57, 72);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 87, 88);
			expected.allocateNode(NodeType.IDENTIFIER, 87, 88);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 91, 92);
			expected.allocateNode(NodeType.ASSIGNMENT_EXPRESSION, 87, 92);
			expected.allocateNode(NodeType.BLOCK, 82, 97);
			expected.allocateNode(NodeType.IF_STATEMENT, 44, 97);
			expected.allocateNode(NodeType.BLOCK, 40, 100);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 21, 100);
			expected.allocateClassDeclaration(7, 102, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 103);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates parsing of traditional for loop with initialization, condition, and increment.
	 * Tests three-part for syntax used for counted iteration.
	 */
	@Test
	public void testForLoop()
	{
		String source = """
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
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.INTEGER_LITERAL, 57, 58);
			expected.allocateNode(NodeType.IDENTIFIER, 60, 61);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 64, 66);
			expected.allocateNode(NodeType.BINARY_EXPRESSION, 60, 66);
			expected.allocateNode(NodeType.IDENTIFIER, 70, 71);
			expected.allocateNode(NodeType.UNARY_EXPRESSION, 68, 71);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 80, 81);
			expected.allocateNode(NodeType.IDENTIFIER, 80, 81);
			expected.allocateNode(NodeType.IDENTIFIER, 84, 85);
			expected.allocateNode(NodeType.ASSIGNMENT_EXPRESSION, 80, 85);
			expected.allocateNode(NodeType.BLOCK, 75, 90);
			expected.allocateNode(NodeType.FOR_STATEMENT, 44, 90);
			expected.allocateNode(NodeType.BLOCK, 40, 93);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 21, 93);
			expected.allocateClassDeclaration(7, 95, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 96);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates parsing of enhanced for loop (for-each) syntax.
	 * Tests simplified iteration over collections and arrays using colon notation.
	 */
	@Test
	public void testEnhancedForLoop()
	{
		String source = """
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
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 49, 55);
			expected.allocateNode(NodeType.IDENTIFIER, 60, 64);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 73, 74);
			expected.allocateNode(NodeType.IDENTIFIER, 73, 74);
			expected.allocateNode(NodeType.IDENTIFIER, 77, 78);
			expected.allocateNode(NodeType.ASSIGNMENT_EXPRESSION, 73, 78);
			expected.allocateNode(NodeType.BLOCK, 68, 83);
			expected.allocateNode(NodeType.ENHANCED_FOR_STATEMENT, 44, 83);
			expected.allocateNode(NodeType.BLOCK, 40, 86);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 21, 86);
			expected.allocateClassDeclaration(7, 88, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 89);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates parsing of while loop with precondition check.
	 * Tests while syntax where condition is evaluated before each iteration.
	 */
	@Test
	public void testWhileLoop()
	{
		String source = """
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
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.IDENTIFIER, 51, 52);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 55, 56);
			expected.allocateNode(NodeType.BINARY_EXPRESSION, 51, 56);
			expected.allocateNode(NodeType.IDENTIFIER, 67, 68);
			expected.allocateNode(NodeType.UNARY_EXPRESSION, 65, 68);
			expected.allocateNode(NodeType.BLOCK, 60, 73);
			expected.allocateNode(NodeType.WHILE_STATEMENT, 44, 73);
			expected.allocateNode(NodeType.BLOCK, 40, 76);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 21, 76);
			expected.allocateClassDeclaration(7, 78, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 79);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates parsing of do-while loop with postcondition check.
	 * Tests do-while syntax where body executes at least once before condition is tested.
	 */
	@Test
	public void testDoWhileLoop()
	{
		String source = """
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
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.IDENTIFIER, 56, 57);
			expected.allocateNode(NodeType.UNARY_EXPRESSION, 54, 57);
			expected.allocateNode(NodeType.BLOCK, 49, 62);
			expected.allocateNode(NodeType.IDENTIFIER, 72, 73);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 76, 77);
			expected.allocateNode(NodeType.BINARY_EXPRESSION, 72, 77);
			expected.allocateNode(NodeType.DO_WHILE_STATEMENT, 44, 79);
			expected.allocateNode(NodeType.BLOCK, 40, 82);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 21, 82);
			expected.allocateClassDeclaration(7, 84, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 85);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates parsing of switch statement with multiple case labels and default.
	 * Tests multi-way branching including case values, break statements, and default.
	 */
	@Test
	public void testSwitchStatement()
	{
		String source = """
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
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.IDENTIFIER, 52, 53);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 67, 68);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 74, 75);
			expected.allocateNode(NodeType.IDENTIFIER, 74, 75);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 78, 79);
			expected.allocateNode(NodeType.ASSIGNMENT_EXPRESSION, 74, 79);
			expected.allocateNode(NodeType.BREAK_STATEMENT, 85, 91);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 100, 101);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 107, 108);
			expected.allocateNode(NodeType.IDENTIFIER, 107, 108);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 111, 112);
			expected.allocateNode(NodeType.ASSIGNMENT_EXPRESSION, 107, 112);
			expected.allocateNode(NodeType.BREAK_STATEMENT, 118, 124);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 141, 142);
			expected.allocateNode(NodeType.IDENTIFIER, 141, 142);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 145, 146);
			expected.allocateNode(NodeType.ASSIGNMENT_EXPRESSION, 141, 146);
			expected.allocateNode(NodeType.SWITCH_STATEMENT, 44, 151);
			expected.allocateNode(NodeType.BLOCK, 40, 154);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 21, 154);
			expected.allocateClassDeclaration(7, 156, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 157);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates parsing of return statement with expression.
	 * Tests method exit with value, essential for non-void methods.
	 */
	@Test
	public void testReturnStatement()
	{
		String source = """
			public class Test
			{
				public int foo()
				{
					return 42;
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.INTEGER_LITERAL, 50, 52);
			expected.allocateNode(NodeType.RETURN_STATEMENT, 43, 53);
			expected.allocateNode(NodeType.BLOCK, 39, 56);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 21, 56);
			expected.allocateClassDeclaration(7, 58, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 59);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates parsing of throw statement for exception propagation.
	 * Tests explicit exception throwing which transfers control to nearest matching catch.
	 */
	@Test
	public void testThrowStatement()
	{
		String source = """
			public class Test
			{
				public void foo()
				{
					throw new RuntimeException();
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 54, 70);
			expected.allocateNode(NodeType.OBJECT_CREATION, 50, 72);
			expected.allocateNode(NodeType.THROW_STATEMENT, 44, 73);
			expected.allocateNode(NodeType.BLOCK, 40, 76);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 21, 76);
			expected.allocateClassDeclaration(7, 78, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 79);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates parsing of try-catch block for exception handling.
	 * Tests exception handling with try body and catch clause for recovery.
	 */
	@Test
	public void testTryStatement()
	{
		String source = """
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
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 55, 56);
			expected.allocateNode(NodeType.IDENTIFIER, 55, 56);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 59, 60);
			expected.allocateNode(NodeType.ASSIGNMENT_EXPRESSION, 55, 60);
			expected.allocateNode(NodeType.BLOCK, 50, 65);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 75, 84);
			expected.allocateParameterDeclaration(75, 86, new ParameterAttribute("e", false, false, false));
			expected.allocateNode(NodeType.QUALIFIED_NAME, 95, 96);
			expected.allocateNode(NodeType.IDENTIFIER, 95, 96);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 99, 100);
			expected.allocateNode(NodeType.ASSIGNMENT_EXPRESSION, 95, 100);
			expected.allocateNode(NodeType.BLOCK, 90, 105);
			expected.allocateNode(NodeType.CATCH_CLAUSE, 68, 105);
			expected.allocateNode(NodeType.TRY_STATEMENT, 44, 105);
			expected.allocateNode(NodeType.BLOCK, 40, 108);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 21, 108);
			expected.allocateClassDeclaration(7, 110, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 111);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates parsing of try-finally block without catch clause.
	 * Tests finally block that executes regardless of exceptions for cleanup.
	 */
	@Test
	public void testTryFinally()
	{
		String source = """
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
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 55, 56);
			expected.allocateNode(NodeType.IDENTIFIER, 55, 56);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 59, 60);
			expected.allocateNode(NodeType.ASSIGNMENT_EXPRESSION, 55, 60);
			expected.allocateNode(NodeType.BLOCK, 50, 65);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 83, 84);
			expected.allocateNode(NodeType.IDENTIFIER, 83, 84);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 87, 88);
			expected.allocateNode(NodeType.ASSIGNMENT_EXPRESSION, 83, 88);
			expected.allocateNode(NodeType.BLOCK, 78, 93);
			expected.allocateNode(NodeType.FINALLY_CLAUSE, 68, 93);
			expected.allocateNode(NodeType.TRY_STATEMENT, 44, 93);
			expected.allocateNode(NodeType.BLOCK, 40, 96);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 21, 96);
			expected.allocateClassDeclaration(7, 98, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 99);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates parsing of try-with-resources for automatic resource management.
	 * Tests try statement with resource declarations ensuring automatic closure.
	 */
	@Test
	public void testTryWithResources()
	{
		String source = """
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
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 49, 55);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 64, 74);
			expected.allocateNode(NodeType.OBJECT_CREATION, 60, 76);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 85, 86);
			expected.allocateNode(NodeType.IDENTIFIER, 85, 86);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 89, 90);
			expected.allocateNode(NodeType.ASSIGNMENT_EXPRESSION, 85, 90);
			expected.allocateNode(NodeType.BLOCK, 80, 95);
			expected.allocateNode(NodeType.TRY_STATEMENT, 44, 95);
			expected.allocateNode(NodeType.BLOCK, 40, 98);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 21, 98);
			expected.allocateClassDeclaration(7, 100, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 101);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates parsing of synchronized block for thread synchronization.
	 * Tests synchronized statement with monitor object for mutual exclusion.
	 */
	@Test
	public void testSynchronizedStatement()
	{
		String source = """
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
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.IDENTIFIER, 58, 62);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 71, 72);
			expected.allocateNode(NodeType.IDENTIFIER, 71, 72);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 75, 76);
			expected.allocateNode(NodeType.ASSIGNMENT_EXPRESSION, 71, 76);
			expected.allocateNode(NodeType.BLOCK, 66, 81);
			expected.allocateNode(NodeType.SYNCHRONIZED_STATEMENT, 44, 81);
			expected.allocateNode(NodeType.BLOCK, 40, 84);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 21, 84);
			expected.allocateClassDeclaration(7, 86, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 87);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates parsing of break statement for loop termination.
	 * Tests break that immediately exits innermost loop or switch.
	 */
	@Test
	public void testBreakStatement()
	{
		String source = """
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
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.BOOLEAN_LITERAL, 51, 55);
			expected.allocateNode(NodeType.BREAK_STATEMENT, 64, 70);
			expected.allocateNode(NodeType.BLOCK, 59, 74);
			expected.allocateNode(NodeType.WHILE_STATEMENT, 44, 74);
			expected.allocateNode(NodeType.BLOCK, 40, 77);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 21, 77);
			expected.allocateClassDeclaration(7, 79, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 80);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates parsing of continue statement for loop iteration control.
	 * Tests continue that skips remaining loop body and proceeds to next iteration.
	 */
	@Test
	public void testContinueStatement()
	{
		String source = """
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
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.BOOLEAN_LITERAL, 51, 55);
			expected.allocateNode(NodeType.CONTINUE_STATEMENT, 64, 73);
			expected.allocateNode(NodeType.BLOCK, 59, 77);
			expected.allocateNode(NodeType.WHILE_STATEMENT, 44, 77);
			expected.allocateNode(NodeType.BLOCK, 40, 80);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 21, 80);
			expected.allocateClassDeclaration(7, 82, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 83);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates parsing of assert statement for runtime validation.
	 * Tests assertion with boolean condition for design-by-contract checks.
	 */
	@Test
	public void testAssertStatement()
	{
		String source = """
			public class Test
			{
				public void foo()
				{
					assert x > 0;
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.IDENTIFIER, 51, 52);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 55, 56);
			expected.allocateNode(NodeType.BINARY_EXPRESSION, 51, 56);
			expected.allocateNode(NodeType.ASSERT_STATEMENT, 44, 57);
			expected.allocateNode(NodeType.BLOCK, 40, 60);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 21, 60);
			expected.allocateClassDeclaration(7, 62, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 63);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates parsing of local variable declaration with initialization.
	 * Tests variable declaration statement with type, name, and initializer expression.
	 */
	@Test
	public void testVariableDeclaration()
	{
		String source = """
			public class Test
			{
				public void foo()
				{
					int x = 5;
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.INTEGER_LITERAL, 52, 53);
			expected.allocateNode(NodeType.BLOCK, 40, 57);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 21, 57);
			expected.allocateClassDeclaration(7, 59, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 60);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates parsing of multiple variable declarations in single statement.
	 * Tests comma-separated declarators with shared type and individual initializers.
	 */
	@Test
	public void testMultipleVariableDeclarations()
	{
		String source = """
			public class Test
			{
				public void foo()
				{
					int x = 1, y = 2, z = 3;
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.INTEGER_LITERAL, 52, 53);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 59, 60);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 66, 67);
			expected.allocateNode(NodeType.BLOCK, 40, 71);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 21, 71);
			expected.allocateClassDeclaration(7, 73, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 74);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}
}
