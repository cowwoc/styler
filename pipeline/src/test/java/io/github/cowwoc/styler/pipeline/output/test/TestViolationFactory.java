package io.github.cowwoc.styler.pipeline.output.test;

import io.github.cowwoc.styler.formatter.DefaultFormattingViolation;
import io.github.cowwoc.styler.formatter.FixStrategy;
import io.github.cowwoc.styler.formatter.FormattingViolation;
import io.github.cowwoc.styler.formatter.ViolationSeverity;

import java.nio.file.Path;
import java.util.List;

/**
 * Factory for creating test violations with various configurations.
 */
public final class TestViolationFactory
{
	private static final Path DEFAULT_TEST_FILE = Path.of("Test.java");
	private static final int DEFAULT_START_POSITION = 100;
	private static final int DEFAULT_END_POSITION = 150;
	private static final int DEFAULT_LINE_NUMBER = 10;
	private static final int DEFAULT_COLUMN_NUMBER = 5;

	private TestViolationFactory()
	{
		// Utility class
	}

	/**
	 * Creates a basic violation with specified ruleId, severity, and message.
	 *
	 * @param ruleId the rule identifier
	 * @param severity the severity level
	 * @param message the violation message
	 * @return a FormattingViolation instance
	 */
	public static FormattingViolation createViolation(String ruleId, ViolationSeverity severity,
		String message)
	{
		return new DefaultFormattingViolation(
			ruleId,
			severity,
			message,
			DEFAULT_TEST_FILE,
			DEFAULT_START_POSITION,
			DEFAULT_END_POSITION,
			DEFAULT_LINE_NUMBER,
			DEFAULT_COLUMN_NUMBER,
			List.of());
	}

	/**
	 * Creates a violation with the specified file path.
	 *
	 * @param path the file path
	 * @return a FormattingViolation instance
	 */
	public static FormattingViolation createViolation(Path path)
	{
		return new DefaultFormattingViolation(
			"TEST_RULE",
			ViolationSeverity.ERROR,
			"Test message",
			path,
			DEFAULT_START_POSITION,
			DEFAULT_END_POSITION,
			DEFAULT_LINE_NUMBER,
			DEFAULT_COLUMN_NUMBER,
			List.of());
	}

	/**
	 * Creates a violation with specified message.
	 *
	 * @param message the violation message
	 * @return a FormattingViolation instance
	 */
	public static FormattingViolation createViolationWithMessage(String message)
	{
		return new DefaultFormattingViolation(
			"TEST_RULE",
			ViolationSeverity.ERROR,
			message,
			DEFAULT_TEST_FILE,
			DEFAULT_START_POSITION,
			DEFAULT_END_POSITION,
			DEFAULT_LINE_NUMBER,
			DEFAULT_COLUMN_NUMBER,
			List.of());
	}

	/**
	 * Creates a violation with a single fix strategy.
	 *
	 * @param fix the fix strategy
	 * @return a FormattingViolation instance
	 */
	public static FormattingViolation createViolationWithFix(FixStrategy fix)
	{
		return new DefaultFormattingViolation(
			"TEST_RULE",
			ViolationSeverity.ERROR,
			"Test message",
			DEFAULT_TEST_FILE,
			DEFAULT_START_POSITION,
			DEFAULT_END_POSITION,
			DEFAULT_LINE_NUMBER,
			DEFAULT_COLUMN_NUMBER,
			List.of(fix));
	}

	/**
	 * Creates a violation with multiple fix strategies.
	 *
	 * @param fixes the list of fix strategies
	 * @return a FormattingViolation instance
	 */
	public static FormattingViolation createViolationWithFixes(List<FixStrategy> fixes)
	{
		return new DefaultFormattingViolation(
			"TEST_RULE",
			ViolationSeverity.ERROR,
			"Test message",
			DEFAULT_TEST_FILE,
			DEFAULT_START_POSITION,
			DEFAULT_END_POSITION,
			DEFAULT_LINE_NUMBER,
			DEFAULT_COLUMN_NUMBER,
			fixes);
	}

	/**
	 * Creates a violation with specified severity.
	 *
	 * @param severity the severity level
	 * @return a FormattingViolation instance
	 */
	public static FormattingViolation createViolationWithSeverity(ViolationSeverity severity)
	{
		return new DefaultFormattingViolation(
			"TEST_RULE",
			severity,
			"Test message",
			DEFAULT_TEST_FILE,
			DEFAULT_START_POSITION,
			DEFAULT_END_POSITION,
			DEFAULT_LINE_NUMBER,
			DEFAULT_COLUMN_NUMBER,
			List.of());
	}

	/**
	 * Creates a violation with specified ruleId.
	 *
	 * @param ruleId the rule identifier
	 * @return a FormattingViolation instance
	 */
	public static FormattingViolation createViolationWithRuleId(String ruleId)
	{
		return new DefaultFormattingViolation(
			ruleId,
			ViolationSeverity.ERROR,
			"Test message",
			DEFAULT_TEST_FILE,
			DEFAULT_START_POSITION,
			DEFAULT_END_POSITION,
			DEFAULT_LINE_NUMBER,
			DEFAULT_COLUMN_NUMBER,
			List.of());
	}

	/**
	 * Creates a violation with specified line and column numbers.
	 *
	 * @param lineNumber the line number
	 * @param columnNumber the column number
	 * @return a FormattingViolation instance
	 */
	public static FormattingViolation createViolationAtLocation(int lineNumber, int columnNumber)
	{
		return new DefaultFormattingViolation(
			"TEST_RULE",
			ViolationSeverity.ERROR,
			"Test message",
			DEFAULT_TEST_FILE,
			DEFAULT_START_POSITION,
			DEFAULT_END_POSITION,
			lineNumber,
			columnNumber,
			List.of());
	}
}
