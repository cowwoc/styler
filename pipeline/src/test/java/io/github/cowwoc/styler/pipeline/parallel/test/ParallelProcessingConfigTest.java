package io.github.cowwoc.styler.pipeline.parallel.test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.Test;

import io.github.cowwoc.styler.pipeline.parallel.ErrorStrategy;
import io.github.cowwoc.styler.pipeline.parallel.ParallelProcessingConfig;
import io.github.cowwoc.styler.pipeline.parallel.ProgressCallback;

/**
 * Unit tests for {@code ParallelProcessingConfig}.
 */
public class ParallelProcessingConfigTest
{
	/**
	 * Tests that positive max concurrency is validated correctly.
	 */
	@Test
	public void shouldValidatePositiveMaxConcurrency()
	{
		int maxConcurrency = 10;
		ErrorStrategy strategy = ErrorStrategy.CONTINUE;

		ParallelProcessingConfig config = new ParallelProcessingConfig(maxConcurrency, strategy, null);

		assertEquals(config.maxConcurrency(), 10);
	}

	/**
	 * Tests that zero max concurrency is rejected.
	 */
	@Test(expectedExceptions = IllegalArgumentException.class)
	public void shouldRejectZeroMaxConcurrency()
	{
		new ParallelProcessingConfig(0, ErrorStrategy.CONTINUE, null);
	}

	/**
	 * Tests that negative max concurrency is rejected.
	 */
	@Test(expectedExceptions = IllegalArgumentException.class)
	public void shouldRejectNegativeMaxConcurrency()
	{
		new ParallelProcessingConfig(-5, ErrorStrategy.CONTINUE, null);
	}

	/**
	 * Tests that null error strategy is rejected.
	 */
	@Test(expectedExceptions = NullPointerException.class)
	public void shouldRejectNullErrorStrategy()
	{
		new ParallelProcessingConfig(10, null, null);
	}

	/**
	 * Tests that null progress callback is allowed.
	 */
	@Test
	public void shouldAllowNullProgressCallback()
	{
		ParallelProcessingConfig config = new ParallelProcessingConfig(10, ErrorStrategy.CONTINUE, null);

		assertNull(config.progressCallback());
	}

	/**
	 * Tests that progress callback is stored correctly.
	 */
	@Test
	public void shouldStoreProgressCallback()
	{
		ProgressCallback callback = (completed, total, file) ->
		{
		};
		ParallelProcessingConfig config = new ParallelProcessingConfig(10, ErrorStrategy.CONTINUE, callback);

		assertEquals(config.progressCallback(), callback);
	}

	/**
	 * Tests that default max concurrency is calculated as a positive value.
	 */
	@Test
	public void shouldCalculateDefaultMaxConcurrency()
	{
		int defaultConcurrency = ParallelProcessingConfig.calculateDefaultMaxConcurrency();

		assertTrue(defaultConcurrency > 0);
	}

	/**
	 * Tests that default max concurrency is based on available memory.
	 */
	@Test
	public void defaultMaxConcurrencyShouldBeBasedOnMemory()
	{
		int defaultConcurrency = ParallelProcessingConfig.calculateDefaultMaxConcurrency();
		long maxMemory = Runtime.getRuntime().maxMemory();
		long estimatedMemoryPerFile = 5 * 1024 * 1024;
		int expected = (int) Math.max(1, maxMemory / estimatedMemoryPerFile);

		assertEquals(defaultConcurrency, expected);
	}

	/**
	 * Tests that builder uses default max concurrency.
	 */
	@Test
	public void builderShouldUseDefaultMaxConcurrency()
	{
		ParallelProcessingConfig config = ParallelProcessingConfig.builder().build();

		assertEquals(config.maxConcurrency(), ParallelProcessingConfig.calculateDefaultMaxConcurrency());
	}

	/**
	 * Tests that builder uses default error strategy.
	 */
	@Test
	public void builderShouldUseDefaultErrorStrategy()
	{
		ParallelProcessingConfig config = ParallelProcessingConfig.builder().build();

		assertEquals(config.errorStrategy(), ErrorStrategy.CONTINUE);
	}

	/**
	 * Tests that builder sets max concurrency.
	 */
	@Test
	public void builderShouldSetMaxConcurrency()
	{
		ParallelProcessingConfig config = ParallelProcessingConfig.builder().maxConcurrency(20).build();

		assertEquals(config.maxConcurrency(), 20);
	}

	/**
	 * Tests that builder sets error strategy.
	 */
	@Test
	public void builderShouldSetErrorStrategy()
	{
		ParallelProcessingConfig config = ParallelProcessingConfig.builder().
			errorStrategy(ErrorStrategy.FAIL_FAST).
			build();

		assertEquals(config.errorStrategy(), ErrorStrategy.FAIL_FAST);
	}

	/**
	 * Tests that builder sets progress callback.
	 */
	@Test
	public void builderShouldSetProgressCallback()
	{
		ProgressCallback callback = (completed, total, file) ->
		{
		};
		ParallelProcessingConfig config = ParallelProcessingConfig.builder().
			progressCallback(callback).
			build();

		assertEquals(config.progressCallback(), callback);
	}

	/**
	 * Tests that builder methods can be chained.
	 */
	@Test
	public void builderShouldChainMethods()
	{
		ProgressCallback callback = (completed, total, file) ->
		{
		};
		ParallelProcessingConfig config = ParallelProcessingConfig.builder().
			maxConcurrency(15).
			errorStrategy(ErrorStrategy.ABORT_AFTER_THRESHOLD).
			progressCallback(callback).
			build();

		assertEquals(config.maxConcurrency(), 15);
		assertEquals(config.errorStrategy(), ErrorStrategy.ABORT_AFTER_THRESHOLD);
		assertEquals(config.progressCallback(), callback);
	}

	/**
	 * Tests that builder rejects zero max concurrency.
	 */
	@Test(expectedExceptions = IllegalArgumentException.class)
	public void builderShouldRejectZeroMaxConcurrency()
	{
		ParallelProcessingConfig.builder().maxConcurrency(0).build();
	}

	/**
	 * Tests that builder rejects null error strategy.
	 */
	@Test(expectedExceptions = NullPointerException.class)
	public void builderShouldRejectNullErrorStrategy()
	{
		ParallelProcessingConfig.builder().errorStrategy(null).build();
	}

	/**
	 * Tests that builder creates new independent instances.
	 */
	@Test
	public void builderShouldCreateNewInstance()
	{
		ParallelProcessingConfig config1 = ParallelProcessingConfig.builder().maxConcurrency(10).build();
		ParallelProcessingConfig config2 = ParallelProcessingConfig.builder().maxConcurrency(20).build();

		assertEquals(config1.maxConcurrency(), 10);
		assertEquals(config2.maxConcurrency(), 20);
	}
}
