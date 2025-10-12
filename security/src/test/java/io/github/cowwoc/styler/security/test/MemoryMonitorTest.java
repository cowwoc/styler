package io.github.cowwoc.styler.security.test;

import io.github.cowwoc.styler.security.*;

import io.github.cowwoc.styler.security.exceptions.MemoryLimitExceededException;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

/**
 * Tests for MemoryMonitor heap usage tracking.
 */
public class MemoryMonitorTest
{
	@Test
	public void checkPassesWhenMemoryUnderLimit() throws Exception
	{
		MemoryMonitor monitor = new MemoryMonitor();
		SecurityConfig config = new SecurityConfig.Builder()
			.maxHeapBytes(Long.MAX_VALUE)
			.build();

		monitor.checkMemoryUsage(config);
		// No exception = success
	}

	@Test(expectedExceptions = MemoryLimitExceededException.class)
	public void checkFailsWhenMemoryExceedsLimit() throws Exception
	{
		MemoryMonitor monitor = new MemoryMonitor();
		SecurityConfig config = new SecurityConfig.Builder()
			.maxHeapBytes(1) // 1 byte limit (always exceeded)
			.build();

		monitor.checkMemoryUsage(config);
	}

	@Test
	public void getCurrentUsageReturnsPositiveValue()
	{
		MemoryMonitor monitor = new MemoryMonitor();
		long usage = monitor.getCurrentUsage();

		assertTrue(usage > 0);
	}

	@Test(expectedExceptions = NullPointerException.class)
	public void nullConfigThrowsException() throws Exception
	{
		MemoryMonitor monitor = new MemoryMonitor();
		monitor.checkMemoryUsage(null);
	}
}
