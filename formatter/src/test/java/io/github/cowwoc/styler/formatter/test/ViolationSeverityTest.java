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
	 * Tests that natural ordering reflects severity precedence: ERROR > WARNING > INFO.
	 */
	@Test
	public void shouldMaintainSeverityOrderingErrorHighest()
	{
		// Natural ordering via compareTo should have ERROR as highest
		requireThat(ViolationSeverity.ERROR.compareTo(ViolationSeverity.WARNING), "ERROR.compareTo(WARNING)").
			isPositive();
		requireThat(ViolationSeverity.WARNING.compareTo(ViolationSeverity.INFO), "WARNING.compareTo(INFO)").
			isPositive();
		requireThat(ViolationSeverity.ERROR.compareTo(ViolationSeverity.INFO), "ERROR.compareTo(INFO)").
			isPositive();
	}

	/**
	 * Tests that severity weights are correctly assigned.
	 */
	@Test
	public void shouldHaveCorrectWeights()
	{
		requireThat(ViolationSeverity.ERROR.weight(), "ERROR.weight").isEqualTo(10);
		requireThat(ViolationSeverity.WARNING.weight(), "WARNING.weight").isEqualTo(5);
		requireThat(ViolationSeverity.INFO.weight(), "INFO.weight").isEqualTo(1);
	}
}
