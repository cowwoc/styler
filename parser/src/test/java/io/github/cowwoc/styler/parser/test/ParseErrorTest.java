package io.github.cowwoc.styler.parser.test;

import io.github.cowwoc.styler.parser.ParseError;
import org.testng.annotations.Test;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;

/**
 * Tests for {@link ParseError} record validation and behavior.
 * <p>
 * Validates that parse error construction enforces constraints (position >= 0, line >= 1, column >= 1,
 * non-empty message) and that the record correctly stores and exposes all fields.
 */
public class ParseErrorTest
{
	/**
	 * Verifies that negative position values are rejected.
	 * Position must be >= 0 because it represents a 0-based character offset in source code.
	 */
	@Test(expectedExceptions = IllegalArgumentException.class)
	public void shouldRejectNegativePosition()
	{
		new ParseError(-1, 1, 1, "Error message");
	}

	/**
	 * Verifies that zero line number is rejected.
	 * Line numbers are 1-based for human readability and IDE integration.
	 */
	@Test(expectedExceptions = IllegalArgumentException.class)
	public void shouldRejectZeroLine()
	{
		new ParseError(0, 0, 1, "Error message");
	}

	/**
	 * Verifies that zero column number is rejected.
	 * Column numbers are 1-based for human readability and IDE integration.
	 */
	@Test(expectedExceptions = IllegalArgumentException.class)
	public void shouldRejectZeroColumn()
	{
		new ParseError(0, 1, 0, "Error message");
	}

	/**
	 * Verifies that empty message is rejected.
	 * Error messages must be non-empty to provide meaningful diagnostic information.
	 */
	@Test(expectedExceptions = IllegalArgumentException.class)
	public void shouldRejectEmptyMessage()
	{
		new ParseError(0, 1, 1, "");
	}

	/**
	 * Verifies that valid field values are accepted and stored correctly.
	 * Tests that all constructor parameters are accessible via their accessor methods.
	 */
	@Test
	public void shouldAcceptValidFields()
	{
		ParseError error = new ParseError(42, 10, 5, "Unexpected token");

		requireThat(error.position(), "position").isEqualTo(42);
		requireThat(error.line(), "line").isEqualTo(10);
		requireThat(error.column(), "column").isEqualTo(5);
		requireThat(error.message(), "message").isEqualTo("Unexpected token");
	}

	/**
	 * Verifies that two ParseError instances with identical fields are considered equal.
	 * Record equality is based on component values, enabling reliable comparison in tests
	 * and collections.
	 */
	@Test
	public void shouldSupportEquality()
	{
		ParseError error1 = new ParseError(0, 1, 1, "Syntax error");
		ParseError error2 = new ParseError(0, 1, 1, "Syntax error");

		requireThat(error1, "error1").isEqualTo(error2);
		requireThat(error1.hashCode(), "error1.hashCode()").isEqualTo(error2.hashCode());
	}
}
