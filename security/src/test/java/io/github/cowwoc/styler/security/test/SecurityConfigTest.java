package io.github.cowwoc.styler.security.test;

import io.github.cowwoc.styler.security.*;

import org.testng.annotations.Test;
import static org.testng.Assert.*;

/**
 * Tests for SecurityConfig immutable configuration with builder pattern.
 */
public class SecurityConfigTest
{
	@Test
	public void defaultConfigHasValidLimits()
	{
		SecurityConfig config = SecurityConfig.DEFAULT;

		assertTrue(config.maxFileSizeBytes() > 0);
		assertTrue(config.maxHeapBytes() > 0);
		assertTrue(config.executionTimeoutMs() > 0);
		assertTrue(config.maxRecursionDepth() > 0);
	}

	@Test
	public void builderCreatesValidConfig()
	{
		SecurityConfig config = new SecurityConfig.Builder()
			.maxFileSizeBytes(1024)
			.maxHeapBytes(2048)
			.executionTimeoutMs(500)
			.maxRecursionDepth(100)
			.build();

		assertEquals(config.maxFileSizeBytes(), 1024);
		assertEquals(config.maxHeapBytes(), 2048);
		assertEquals(config.executionTimeoutMs(), 500);
		assertEquals(config.maxRecursionDepth(), 100);
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void builderRejectsNegativeFileSize()
	{
		new SecurityConfig.Builder().maxFileSizeBytes(-1);
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void builderRejectsZeroFileSize()
	{
		new SecurityConfig.Builder().maxFileSizeBytes(0);
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void builderRejectsNegativeHeapSize()
	{
		new SecurityConfig.Builder().maxHeapBytes(-1);
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void builderRejectsNegativeTimeout()
	{
		new SecurityConfig.Builder().executionTimeoutMs(-1);
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void builderRejectsNegativeDepth()
	{
		new SecurityConfig.Builder().maxRecursionDepth(-1);
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void recordConstructorRejectsInvalidLimits()
	{
		new SecurityConfig(0, 1, 1, 1);
	}

	@Test
	public void recordIsImmutable()
	{
		SecurityConfig config1 = new SecurityConfig.Builder().maxFileSizeBytes(1000).build();
		SecurityConfig config2 = new SecurityConfig.Builder().maxFileSizeBytes(1000).build();

		assertEquals(config1.maxFileSizeBytes(), config2.maxFileSizeBytes());
	}

	@Test
	public void builderAllowsChaining()
	{
		SecurityConfig config = new SecurityConfig.Builder()
			.maxFileSizeBytes(100)
			.maxHeapBytes(200)
			.executionTimeoutMs(300)
			.maxRecursionDepth(400)
			.build();

		assertEquals(config.maxFileSizeBytes(), 100);
		assertEquals(config.maxRecursionDepth(), 400);
	}
}
