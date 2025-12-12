package io.github.cowwoc.styler.security.test;

import io.github.cowwoc.styler.security.*;

import io.github.cowwoc.styler.security.exceptions.ExecutionTimeoutException;
import org.testng.annotations.Test;

import java.nio.file.Path;
import java.time.Duration;

import static org.testng.Assert.*;

/**
 * Tests for ExecutionTimeoutManager execution time tracking.
 * Thread-safe: each test creates its own manager instance.
 */
public class ExecutionTimeoutManagerTest
{
	@Test
	public void checkPassesBeforeTimeout() throws Exception
	{
		ExecutionTimeoutManager manager = new ExecutionTimeoutManager();
		SecurityConfig config = new SecurityConfig.Builder().executionTimeout(Duration.ofMillis(1000)).build();
		Path testFile = Path.of("Test.java");

		try
		{
			manager.startTracking();
			manager.checkTimeout(testFile, config);
			// No exception = success
		}
		finally
		{
			manager.stopTracking();
		}
	}

	@Test(expectedExceptions = ExecutionTimeoutException.class)
	public void checkFailsAfterTimeout() throws Exception
	{
		ExecutionTimeoutManager manager = new ExecutionTimeoutManager();
		SecurityConfig shortConfig = new SecurityConfig.Builder().executionTimeout(Duration.ofMillis(1)).build();
		Path testFile = Path.of("Test.java");

		try
		{
			manager.startTracking();
			Thread.sleep(2); // Ensure timeout exceeded
			manager.checkTimeout(testFile, shortConfig);
		}
		finally
		{
			manager.stopTracking();
		}
	}

	@Test(expectedExceptions = IllegalStateException.class)
	public void checkWithoutStartThrowsException() throws Exception
	{
		ExecutionTimeoutManager manager = new ExecutionTimeoutManager();
		SecurityConfig config = new SecurityConfig.Builder().executionTimeout(Duration.ofMillis(1000)).build();
		Path testFile = Path.of("Test.java");

		manager.checkTimeout(testFile, config);
	}

	@Test
	public void getElapsedTimeReturnsPositiveValue() throws Exception
	{
		ExecutionTimeoutManager manager = new ExecutionTimeoutManager();

		try
		{
			manager.startTracking();

			Duration elapsed = manager.getElapsedTime();
			assertTrue(elapsed.toNanos() > 0);
		}
		finally
		{
			manager.stopTracking();
		}
	}

	@Test(expectedExceptions = IllegalStateException.class)
	public void getElapsedWithoutStartThrowsException()
	{
		ExecutionTimeoutManager manager = new ExecutionTimeoutManager();
		manager.getElapsedTime();
	}

	@Test
	public void stopTrackingCleansUpState()
	{
		ExecutionTimeoutManager manager = new ExecutionTimeoutManager();

		manager.startTracking();
		manager.stopTracking();

		try
		{
			manager.getElapsedTime();
			fail("Expected IllegalStateException");
		}
		catch (IllegalStateException e)
		{
			// Expected - state was cleaned up
		}
	}

	@Test
	public void multipleStartStopCyclesWork() throws Exception
	{
		ExecutionTimeoutManager manager = new ExecutionTimeoutManager();
		SecurityConfig config = new SecurityConfig.Builder().executionTimeout(Duration.ofMillis(1000)).build();
		Path testFile = Path.of("Test.java");

		manager.startTracking();
		manager.checkTimeout(testFile, config);
		manager.stopTracking();

		manager.startTracking();
		manager.checkTimeout(testFile, config);
		manager.stopTracking();
	}

	@Test(expectedExceptions = NullPointerException.class)
	public void nullFileThrowsException() throws Exception
	{
		ExecutionTimeoutManager manager = new ExecutionTimeoutManager();
		SecurityConfig config = new SecurityConfig.Builder().executionTimeout(Duration.ofMillis(1000)).build();

		try
		{
			manager.startTracking();
			manager.checkTimeout(null, config);
		}
		finally
		{
			manager.stopTracking();
		}
	}

	@Test(expectedExceptions = NullPointerException.class)
	public void nullConfigThrowsException() throws Exception
	{
		ExecutionTimeoutManager manager = new ExecutionTimeoutManager();
		Path testFile = Path.of("Test.java");

		try
		{
			manager.startTracking();
			manager.checkTimeout(testFile, null);
		}
		finally
		{
			manager.stopTracking();
		}
	}
}
