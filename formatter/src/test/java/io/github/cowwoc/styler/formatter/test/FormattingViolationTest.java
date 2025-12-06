package io.github.cowwoc.styler.formatter.test;

import io.github.cowwoc.styler.formatter.DefaultFormattingViolation;
import io.github.cowwoc.styler.formatter.FormattingViolation;
import io.github.cowwoc.styler.formatter.ViolationSeverity;
import org.testng.annotations.Test;

import java.nio.file.Path;
import java.util.List;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;

/**
 * Tests for FormattingViolation immutability and validation.
 */
public class FormattingViolationTest
{
	private static final Path TEST_FILE = Path.of("Test.java");

	/**
	 * Tests that a violation can be created with all required fields.
	 */
	@Test
	public void shouldCreateViolationWithAllFields()
	{
		FormattingViolation violation = new DefaultFormattingViolation(
			"LINE_LENGTH", ViolationSeverity.ERROR, "Line exceeds 120 characters",
			TEST_FILE, 100, 150, 10, 5, List.of());

		requireThat(violation.lineNumber(), "lineNumber").isEqualTo(10);
		requireThat(violation.columnNumber(), "columnNumber").isEqualTo(5);
		requireThat(violation.severity(), "severity").isEqualTo(ViolationSeverity.ERROR);
		requireThat(violation.ruleId(), "ruleId").isEqualTo("LINE_LENGTH");
		requireThat(violation.message(), "message").isEqualTo("Line exceeds 120 characters");
		requireThat(violation.filePath(), "filePath").isEqualTo(TEST_FILE);
		requireThat(violation.startPosition(), "startPosition").isEqualTo(100);
		requireThat(violation.endPosition(), "endPosition").isEqualTo(150);
	}

	/**
	 * Tests that null severity is rejected.
	 */
	@Test(expectedExceptions = NullPointerException.class)
	public void shouldRejectNullSeverity()
	{
		new DefaultFormattingViolation("TEST", null, "test", TEST_FILE, 0, 10, 1, 1, List.of());
	}

	/**
	 * Tests that null ruleId is rejected.
	 */
	@Test(expectedExceptions = NullPointerException.class)
	public void shouldRejectNullRuleId()
	{
		new DefaultFormattingViolation(null, ViolationSeverity.ERROR, "test", TEST_FILE,
			0, 10, 1, 1, List.of());
	}

	/**
	 * Tests that null message is rejected.
	 */
	@Test(expectedExceptions = NullPointerException.class)
	public void shouldRejectNullMessage()
	{
		new DefaultFormattingViolation("TEST", ViolationSeverity.ERROR, null, TEST_FILE,
			0, 10, 1, 1, List.of());
	}

	/**
	 * Tests that empty ruleId is rejected.
	 */
	@Test(expectedExceptions = IllegalArgumentException.class)
	public void shouldRejectEmptyRuleId()
	{
		new DefaultFormattingViolation("", ViolationSeverity.ERROR, "test", TEST_FILE,
			0, 10, 1, 1, List.of());
	}

	/**
	 * Tests that empty message is rejected.
	 */
	@Test(expectedExceptions = IllegalArgumentException.class)
	public void shouldRejectEmptyMessage()
	{
		new DefaultFormattingViolation("TEST", ViolationSeverity.ERROR, "", TEST_FILE,
			0, 10, 1, 1, List.of());
	}

	/**
	 * Tests that whitespace-only ruleId is rejected.
	 */
	@Test(expectedExceptions = IllegalArgumentException.class)
	public void shouldRejectWhitespaceOnlyRuleId()
	{
		new DefaultFormattingViolation("  ", ViolationSeverity.ERROR, "test", TEST_FILE,
			0, 10, 1, 1, List.of());
	}

	/**
	 * Tests that whitespace-only message is rejected.
	 */
	@Test(expectedExceptions = IllegalArgumentException.class)
	public void shouldRejectWhitespaceOnlyMessage()
	{
		new DefaultFormattingViolation("TEST", ViolationSeverity.ERROR, "  ", TEST_FILE,
			0, 10, 1, 1, List.of());
	}

	/**
	 * Tests that negative start position is rejected.
	 */
	@Test(expectedExceptions = IllegalArgumentException.class)
	public void shouldRejectNegativeStartPosition()
	{
		new DefaultFormattingViolation("TEST", ViolationSeverity.ERROR, "test", TEST_FILE,
			-1, 10, 1, 1, List.of());
	}

	/**
	 * Tests that end position before start is rejected.
	 */
	@Test(expectedExceptions = IllegalArgumentException.class)
	public void shouldRejectEndPositionBeforeStart()
	{
		new DefaultFormattingViolation("TEST", ViolationSeverity.ERROR, "test", TEST_FILE,
			10, 5, 1, 1, List.of());
	}

	/**
	 * Tests that non-positive line number is rejected.
	 */
	@Test(expectedExceptions = IllegalArgumentException.class)
	public void shouldRejectNonPositiveLineNumber()
	{
		new DefaultFormattingViolation("TEST", ViolationSeverity.ERROR, "test", TEST_FILE,
			0, 10, 0, 1, List.of());
	}

	/**
	 * Tests that non-positive column number is rejected.
	 */
	@Test(expectedExceptions = IllegalArgumentException.class)
	public void shouldRejectNonPositiveColumnNumber()
	{
		new DefaultFormattingViolation("TEST", ViolationSeverity.ERROR, "test", TEST_FILE,
			0, 10, 1, 0, List.of());
	}

	/**
	 * Tests that valid positive line number is accepted.
	 */
	@Test
	public void shouldAcceptValidPositiveLineNumber()
	{
		FormattingViolation violation = new DefaultFormattingViolation(
			"TEST", ViolationSeverity.ERROR, "test", TEST_FILE,
			0, 10, 1, 1, List.of());

		requireThat(violation.lineNumber(), "lineNumber").isEqualTo(1);
	}

	/**
	 * Tests that equals is implemented correctly.
	 */
	@Test
	public void shouldImplementEqualsCorrectly()
	{
		FormattingViolation v1 = new DefaultFormattingViolation("TEST", ViolationSeverity.ERROR,
			"msg", TEST_FILE, 0, 10, 1, 1, List.of());
		FormattingViolation v2 = new DefaultFormattingViolation("TEST", ViolationSeverity.ERROR,
			"msg", TEST_FILE, 0, 10, 1, 1, List.of());

		requireThat(v1, "v1").isEqualTo(v2);
		requireThat(v1.hashCode(), "v1.hashCode").isEqualTo(v2.hashCode());
	}

	/**
	 * Tests that hashCode is implemented correctly.
	 */
	@Test
	public void shouldImplementHashCodeCorrectly()
	{
		FormattingViolation v1 = new DefaultFormattingViolation("TEST", ViolationSeverity.ERROR,
			"msg", TEST_FILE, 0, 10, 1, 1, List.of());
		FormattingViolation v2 = new DefaultFormattingViolation("TEST", ViolationSeverity.ERROR,
			"msg", TEST_FILE, 0, 10, 1, 1, List.of());

		requireThat(v1.hashCode(), "v1.hashCode").isEqualTo(v2.hashCode());
	}

	/**
	 * Tests that toString produces a descriptive string.
	 */
	@Test
	public void shouldProduceDescriptiveToString()
	{
		FormattingViolation v = new DefaultFormattingViolation(
			"LINE_LENGTH", ViolationSeverity.ERROR, "Too long", TEST_FILE,
			100, 150, 10, 5, List.of());

		String result = v.toString();
		requireThat(result, "result").contains("10");
		requireThat(result, "result").contains("5");
		requireThat(result, "result").contains("ERROR");
		requireThat(result, "result").contains("LINE_LENGTH");
	}
}
