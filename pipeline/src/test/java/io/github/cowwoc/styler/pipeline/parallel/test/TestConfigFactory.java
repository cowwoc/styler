package io.github.cowwoc.styler.pipeline.parallel.test;

import io.github.cowwoc.styler.pipeline.parallel.ErrorStrategy;
import io.github.cowwoc.styler.pipeline.parallel.ParallelProcessingConfig;

/**
 * Factory for creating test configurations.
 */
public final class TestConfigFactory
{
	private TestConfigFactory()
	{
		// Prevent instantiation
	}

	/**
	 * Creates a default configuration for testing.
	 *
	 * @return a default {@code ParallelProcessingConfig}
	 */
	public static ParallelProcessingConfig createDefaultConfig()
	{
		return ParallelProcessingConfig.builder().
			maxConcurrency(ParallelProcessingConfig.calculateDefaultMaxConcurrency()).
			errorStrategy(ErrorStrategy.CONTINUE).
			build();
	}

	/**
	 * Creates a configuration with fail-fast error strategy.
	 *
	 * @return a {@code ParallelProcessingConfig} with FAIL_FAST strategy
	 */
	public static ParallelProcessingConfig createFailFastConfig()
	{
		return ParallelProcessingConfig.builder().
			maxConcurrency(ParallelProcessingConfig.calculateDefaultMaxConcurrency()).
			errorStrategy(ErrorStrategy.FAIL_FAST).
			build();
	}

	/**
	 * Creates a configuration with limited concurrency.
	 *
	 * @param maxConcurrency the maximum concurrent files
	 * @return a {@code ParallelProcessingConfig} with specified concurrency limit
	 */
	public static ParallelProcessingConfig createLimitedConcurrencyConfig(int maxConcurrency)
	{
		return ParallelProcessingConfig.builder().
			maxConcurrency(maxConcurrency).
			errorStrategy(ErrorStrategy.CONTINUE).
			build();
	}

	/**
	 * Creates a configuration with custom concurrency and error strategy.
	 *
	 * @param maxConcurrency the maximum concurrent files
	 * @param errorStrategy the error handling strategy
	 * @return a configured {@code ParallelProcessingConfig}
	 */
	public static ParallelProcessingConfig createCustomConfig(int maxConcurrency, ErrorStrategy errorStrategy)
	{
		return ParallelProcessingConfig.builder().
			maxConcurrency(maxConcurrency).
			errorStrategy(errorStrategy).
			build();
	}
}
