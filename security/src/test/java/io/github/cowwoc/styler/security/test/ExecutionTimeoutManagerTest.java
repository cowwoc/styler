package io.github.cowwoc.styler.security.test;

import io.github.cowwoc.styler.security.*;

import io.github.cowwoc.styler.security.exceptions.ExecutionTimeoutException;
import org.testng.annotations.Test;

import java.nio.file.Path;
import java.time.Duration;

import static org.testng.Assert.*;

/**
 * Tests for ExecutionTimeoutManager execution time tracking.
 */
public class ExecutionTimeoutManagerTest
{
	/**
	 * Verifies that {@code checkTimeout()} passes when called before the timeout expires.
	 */
	@Test
	public void checkPassesBeforeTimeout()
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

	/**
	 * Verifies that {@code checkTimeout()} throws {@link ExecutionTimeoutException} after the timeout expires.
	 *
	 * @throws InterruptedException if the thread is interrupted while sleeping
	 */
	@Test(expectedExceptions = ExecutionTimeoutException.class)
	public void checkFailsAfterTimeout() throws InterruptedException
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

	/**
	 * Verifies that {@code checkTimeout()} throws {@link IllegalStateException} when called without
	 * {@code startTracking()}.
	 */
	@Test(expectedExceptions = IllegalStateException.class)
	public void checkWithoutStartThrowsException()
	{
		ExecutionTimeoutManager manager = new ExecutionTimeoutManager();
		SecurityConfig config = new SecurityConfig.Builder().executionTimeout(Duration.ofMillis(1000)).build();
		Path testFile = Path.of("Test.java");

		manager.checkTimeout(testFile, config);
	}

	/**
	 * Verifies that {@code getElapsedTime()} returns a non-negative duration.
	 */
	@Test
	public void getElapsedTimeReturnsNonNegativeValue()
	{
		ExecutionTimeoutManager manager = new ExecutionTimeoutManager();

		try
		{
			manager.startTracking();

			Duration elapsed = manager.getElapsedTime();
			// On some systems (especially Windows), elapsed time can be 0 if measured immediately
			assertTrue(elapsed.toNanos() >= 0);
		}
		finally
		{
			manager.stopTracking();
		}
	}

	/**
	 * Verifies that {@code getElapsedTime()} throws {@link IllegalStateException} when called without
	 * {@code startTracking()}.
	 */
	@Test(expectedExceptions = IllegalStateException.class)
	public void getElapsedWithoutStartThrowsException()
	{
		ExecutionTimeoutManager manager = new ExecutionTimeoutManager();
		manager.getElapsedTime();
	}

	/**
	 * Verifies that {@code stopTracking()} resets the manager state.
	 */
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

	/**
	 * Verifies that the manager supports multiple start/stop cycles.
	 */
	@Test
	public void multipleStartStopCyclesWork()
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

	/**
	 * Verifies that {@code checkTimeout()} throws {@link NullPointerException} for a null file.
	 */
	@Test(expectedExceptions = NullPointerException.class)
	public void nullFileThrowsException()
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

	/**
	 * Verifies that {@code checkTimeout()} throws {@link NullPointerException} for a null config.
	 */
	@Test(expectedExceptions = NullPointerException.class)
	public void nullConfigThrowsException()
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
