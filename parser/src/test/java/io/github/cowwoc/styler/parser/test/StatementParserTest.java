package io.github.cowwoc.styler.parser.test;

import org.testng.annotations.Test;

import static io.github.cowwoc.styler.parser.test.ParserTestUtils.assertParseSucceeds;

/**
 * Thread-safe tests for Parser statement parsing.
 */
public class StatementParserTest
{
	/**
	 * Tests basic if statement with condition and true block.
	 * Validates parsing of conditional execution without else clause,
	 * the simplest form of branching logic.
	 */
	@Test
	public void testIfStatement()
	{
		assertParseSucceeds("""
			public class Test {
				public void foo() {
					if (x > 0) {
						x = 1;
					}
				}
			}
			""");
	}

	/**
	 * Tests if-else statement with both branches.
	 * Validates parsing of conditional with alternative execution path,
	 * providing complete two-way branching.
	 */
	@Test
	public void testIfElseStatement()
	{
		assertParseSucceeds("""
			public class Test {
				public void foo() {
					if (x > 0) {
						x = 1;
					} else {
						x = 0;
					}
				}
			}
			""");
	}

	/**
	 * Tests traditional for loop with initialization, condition, and increment.
	 * Validates parsing of three-part for syntax used for counted iteration,
	 * including loop variable declaration and update expression.
	 */
	@Test
	public void testForLoop()
	{
		assertParseSucceeds("""
			public class Test {
				public void foo() {
					for (int i = 0; i < 10; i = i + 1) {
						x = i;
					}
				}
			}
			""");
	}

	/**
	 * Tests enhanced for loop (for-each) syntax.
	 * Validates parsing of simplified iteration over collections and arrays
	 * using the colon notation introduced in Java 5.
	 */
	@Test
	public void testEnhancedForLoop()
	{
		assertParseSucceeds("""
			public class Test {
				public void foo() {
					for (String s : list) {
						x = s;
					}
				}
			}
			""");
	}

	/**
	 * Tests while loop with precondition check.
	 * Validates parsing of while syntax where condition is evaluated before
	 * each iteration, allowing zero executions if initially false.
	 */
	@Test
	public void testWhileLoop()
	{
		assertParseSucceeds("""
			public class Test {
				public void foo() {
					while (x > 0) {
						x = x - 1;
					}
				}
			}
			""");
	}

	/**
	 * Tests do-while loop with postcondition check.
	 * Validates parsing of do-while syntax where body executes at least once
	 * before condition is tested, with semicolon after while clause.
	 */
	@Test
	public void testDoWhileLoop()
	{
		assertParseSucceeds("""
			public class Test {
				public void foo() {
					do {
						x = x - 1;
					} while (x > 0);
				}
			}
			""");
	}

	/**
	 * Tests switch statement with multiple case labels and default.
	 * Validates parsing of multi-way branching including case values,
	 * break statements, and default fall-through case.
	 */
	@Test
	public void testSwitchStatement()
	{
		assertParseSucceeds("""
			public class Test {
				public void foo() {
					switch (x) {
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
	}

	/**
	 * Tests return statement with expression.
	 * Validates parsing of method exit with value, essential for
	 * non-void methods to return results to callers.
	 */
	@Test
	public void testReturnStatement()
	{
		assertParseSucceeds("""
			public class Test {
				public int foo() {
					return 42;
				}
			}
			""");
	}

	/**
	 * Tests throw statement for exception propagation.
	 * Validates parsing of explicit exception throwing, which transfers
	 * control to nearest matching catch block or method caller.
	 */
	@Test
	public void testThrowStatement()
	{
		assertParseSucceeds("""
			public class Test {
				public void foo() {
					throw new RuntimeException();
				}
			}
			""");
	}

	/**
	 * Tests try-catch block for exception handling.
	 * Validates parsing of exception handling with try body and catch clause,
	 * allowing recovery from exceptional conditions.
	 */
	@Test
	public void testTryStatement()
	{
		assertParseSucceeds("""
			public class Test {
				public void foo() {
					try {
						x = 1;
					} catch (Exception e) {
						x = 0;
					}
				}
			}
			""");
	}

	/**
	 * Tests try-finally block without catch clause.
	 * Validates parsing of finally block that executes regardless of exceptions,
	 * ensuring cleanup code runs even when errors occur.
	 */
	@Test
	public void testTryFinally()
	{
		assertParseSucceeds("""
			public class Test {
				public void foo() {
					try {
						x = 1;
					} finally {
						y = 0;
					}
				}
			}
			""");
	}

	/**
	 * Tests try-with-resources for automatic resource management.
	 * Validates parsing of try statement with resource declarations in parentheses,
	 * ensuring automatic closure of AutoCloseable resources.
	 */
	@Test
	public void testTryWithResources()
	{
		assertParseSucceeds("""
			public class Test {
				public void foo() {
					try (Reader r = new FileReader()) {
						x = 1;
					}
				}
			}
			""");
	}

	/**
	 * Tests synchronized block for thread synchronization.
	 * Validates parsing of synchronized statement with monitor object,
	 * ensuring mutual exclusion for critical sections in concurrent code.
	 */
	@Test
	public void testSynchronizedStatement()
	{
		assertParseSucceeds("""
			public class Test {
				public void foo() {
					synchronized (lock) {
						x = 1;
					}
				}
			}
			""");
	}

	/**
	 * Tests break statement for loop termination.
	 * Validates parsing of break that immediately exits innermost loop or switch,
	 * providing early exit from iteration.
	 */
	@Test
	public void testBreakStatement()
	{
		assertParseSucceeds("""
			public class Test {
				public void foo() {
					while (true) {
						break;
					}
				}
			}
			""");
	}

	/**
	 * Tests continue statement for loop iteration control.
	 * Validates parsing of continue that skips remaining loop body
	 * and proceeds to next iteration.
	 */
	@Test
	public void testContinueStatement()
	{
		assertParseSucceeds("""
			public class Test {
				public void foo() {
					while (true) {
						continue;
					}
				}
			}
			""");
	}

	/**
	 * Tests assert statement for runtime validation.
	 * Validates parsing of assertion with boolean condition,
	 * enabling design-by-contract and debugging checks.
	 */
	@Test
	public void testAssertStatement()
	{
		assertParseSucceeds("""
			public class Test {
				public void foo() {
					assert x > 0;
				}
			}
			""");
	}

	/**
	 * Tests local variable declaration with initialization.
	 * Validates parsing of variable declaration statement with type, name,
	 * and initializer expression.
	 */
	@Test
	public void testVariableDeclaration()
	{
		assertParseSucceeds("""
			public class Test {
				public void foo() {
					int x = 5;
				}
			}
			""");
	}

	/**
	 * Tests multiple variable declarations in single statement.
	 * Validates parsing of comma-separated declarators with shared type,
	 * allowing concise declaration of related variables.
	 */
	@Test
	public void testMultipleVariableDeclarations()
	{
		assertParseSucceeds("""
			public class Test {
				public void foo() {
					int x = 1, y = 2, z = 3;
				}
			}
			""");
	}
}
