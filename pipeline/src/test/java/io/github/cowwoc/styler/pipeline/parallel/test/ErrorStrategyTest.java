package io.github.cowwoc.styler.pipeline.parallel.test;

import static org.testng.Assert.assertEquals;

import org.testng.annotations.Test;

import io.github.cowwoc.styler.pipeline.parallel.ErrorStrategy;

/**
 * Unit tests for {@code ErrorStrategy}.
 */
public class ErrorStrategyTest
{
	/**
	 * Tests that FAIL_FAST strategy exists.
	 */
	@Test
	public void shouldHaveFailFastStrategy()
	{
		assertEquals(ErrorStrategy.FAIL_FAST.name(), "FAIL_FAST");
	}

	/**
	 * Tests that CONTINUE strategy exists.
	 */
	@Test
	public void shouldHaveContinueStrategy()
	{
		assertEquals(ErrorStrategy.CONTINUE.name(), "CONTINUE");
	}

	/**
	 * Tests that ABORT_AFTER_THRESHOLD strategy exists.
	 */
	@Test
	public void shouldHaveAbortAfterThresholdStrategy()
	{
		assertEquals(ErrorStrategy.ABORT_AFTER_THRESHOLD.name(), "ABORT_AFTER_THRESHOLD");
	}

	/**
	 * Tests that exactly three error strategies are defined.
	 */
	@Test
	public void shouldHaveThreeStrategies()
	{
		ErrorStrategy[] strategies = ErrorStrategy.values();

		assertEquals(strategies.length, 3);
	}

	/**
	 * Tests that FAIL_FAST can be parsed from string.
	 */
	@Test
	public void shouldParseFailFastFromString()
	{
		ErrorStrategy strategy = ErrorStrategy.valueOf("FAIL_FAST");

		assertEquals(strategy, ErrorStrategy.FAIL_FAST);
	}

	/**
	 * Tests that CONTINUE can be parsed from string.
	 */
	@Test
	public void shouldParseContinueFromString()
	{
		ErrorStrategy strategy = ErrorStrategy.valueOf("CONTINUE");

		assertEquals(strategy, ErrorStrategy.CONTINUE);
	}

	/**
	 * Tests that ABORT_AFTER_THRESHOLD can be parsed from string.
	 */
	@Test
	public void shouldParseAbortAfterThresholdFromString()
	{
		ErrorStrategy strategy = ErrorStrategy.valueOf("ABORT_AFTER_THRESHOLD");

		assertEquals(strategy, ErrorStrategy.ABORT_AFTER_THRESHOLD);
	}
}
