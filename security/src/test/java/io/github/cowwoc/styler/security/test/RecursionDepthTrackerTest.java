package io.github.cowwoc.styler.security.test;

import io.github.cowwoc.styler.security.*;

import io.github.cowwoc.styler.security.exceptions.RecursionDepthExceededException;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

/**
 * Tests for RecursionDepthTracker stack overflow protection.
 * Thread-safe: each test creates its own tracker instance.
 */
public class RecursionDepthTrackerTest
{
	@Test
	public void enterIncrementsDepth() throws Exception
	{
		RecursionDepthTracker tracker = new RecursionDepthTracker();
		SecurityConfig config = new SecurityConfig.Builder().maxRecursionDepth(10).build();

		try
		{
			tracker.enter(config);
			assertEquals(tracker.getCurrentDepth(), 1);

			tracker.enter(config);
			assertEquals(tracker.getCurrentDepth(), 2);
		}
		finally
		{
			tracker.reset();
		}
	}

	@Test
	public void exitDecrementsDepth() throws Exception
	{
		RecursionDepthTracker tracker = new RecursionDepthTracker();
		SecurityConfig config = new SecurityConfig.Builder().maxRecursionDepth(10).build();

		try
		{
			tracker.enter(config);
			tracker.enter(config);

			tracker.exit();
			assertEquals(tracker.getCurrentDepth(), 1);

			tracker.exit();
			assertEquals(tracker.getCurrentDepth(), 0);
		}
		finally
		{
			tracker.reset();
		}
	}

	@Test(expectedExceptions = RecursionDepthExceededException.class)
	public void enterFailsWhenLimitExceeded() throws Exception
	{
		RecursionDepthTracker tracker = new RecursionDepthTracker();
		SecurityConfig limitConfig = new SecurityConfig.Builder().maxRecursionDepth(2).build();

		try
		{
			tracker.enter(limitConfig);
			tracker.enter(limitConfig);
			tracker.enter(limitConfig); // Should fail
		}
		finally
		{
			tracker.reset();
		}
	}

	@Test(expectedExceptions = IllegalStateException.class)
	public void exitFailsWhenDepthIsZero()
	{
		RecursionDepthTracker tracker = new RecursionDepthTracker();
		tracker.exit();
	}

	@Test
	public void resetClearsDepth() throws Exception
	{
		RecursionDepthTracker tracker = new RecursionDepthTracker();
		SecurityConfig config = new SecurityConfig.Builder().maxRecursionDepth(10).build();

		try
		{
			tracker.enter(config);
			tracker.enter(config);
			tracker.reset();

			assertEquals(tracker.getCurrentDepth(), 0);
		}
		finally
		{
			tracker.reset();
		}
	}

	@Test
	public void matchedEnterExitMaintainsZeroDepth() throws Exception
	{
		RecursionDepthTracker tracker = new RecursionDepthTracker();
		SecurityConfig config = new SecurityConfig.Builder().maxRecursionDepth(10).build();

		try
		{
			tracker.enter(config);
			tracker.exit();

			assertEquals(tracker.getCurrentDepth(), 0);
		}
		finally
		{
			tracker.reset();
		}
	}

	@Test
	public void deepRecursionWithinLimitSucceeds() throws Exception
	{
		RecursionDepthTracker tracker = new RecursionDepthTracker();
		SecurityConfig deepConfig = new SecurityConfig.Builder().maxRecursionDepth(100).build();

		try
		{
			for (int i = 0; i < 100; i++)
			{
				tracker.enter(deepConfig);
			}

			assertEquals(tracker.getCurrentDepth(), 100);

			for (int i = 0; i < 100; i++)
			{
				tracker.exit();
			}

			assertEquals(tracker.getCurrentDepth(), 0);
		}
		finally
		{
			tracker.reset();
		}
	}

	@Test
	public void initialDepthIsZero()
	{
		RecursionDepthTracker tracker = new RecursionDepthTracker();
		assertEquals(tracker.getCurrentDepth(), 0);
	}

	@Test(expectedExceptions = NullPointerException.class)
	public void nullConfigThrowsException() throws Exception
	{
		RecursionDepthTracker tracker = new RecursionDepthTracker();

		try
		{
			tracker.enter(null);
		}
		finally
		{
			tracker.reset();
		}
	}
}
