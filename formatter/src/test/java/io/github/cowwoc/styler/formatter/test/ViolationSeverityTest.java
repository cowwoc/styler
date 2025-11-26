package io.github.cowwoc.styler.formatter.test;

import io.github.cowwoc.styler.formatter.ViolationSeverity;
import org.testng.annotations.Test;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;

/**
 * Tests for ViolationSeverity enum.
 */
public class ViolationSeverityTest
{
	/**
	 * Tests that ERROR severity exists.
	 */
	@Test
	public void shouldContainErrorSeverity()
	{
		requireThat(ViolationSeverity.ERROR.name(), "ERROR.name").isEqualTo("ERROR");
	}

	/**
	 * Tests that WARNING severity exists.
	 */
	@Test
	public void shouldContainWarningSeverity()
	{
		requireThat(ViolationSeverity.WARNING.name(), "WARNING.name").isEqualTo("WARNING");
	}

	/**
	 * Tests that INFO severity exists.
	 */
	@Test
	public void shouldContainInfoSeverity()
	{
		requireThat(ViolationSeverity.INFO.name(), "INFO.name").isEqualTo("INFO");
	}

	/**
	 * Tests that exactly three severity levels exist.
	 */
	@Test
	public void shouldHaveExactlyThreeSeverityLevels()
	{
		requireThat(ViolationSeverity.values().length, "values.length").isEqualTo(3);
	}

	/**
	 * Tests that severity ordering is ERROR, WARNING, INFO.
	 */
	@Test
	public void shouldMaintainSeverityOrderingErrorHighest()
	{
		requireThat(ViolationSeverity.ERROR.ordinal(), "ERROR.ordinal").
			isLessThan(ViolationSeverity.WARNING.ordinal());
		requireThat(ViolationSeverity.WARNING.ordinal(), "WARNING.ordinal").
			isLessThan(ViolationSeverity.INFO.ordinal());
	}
}
