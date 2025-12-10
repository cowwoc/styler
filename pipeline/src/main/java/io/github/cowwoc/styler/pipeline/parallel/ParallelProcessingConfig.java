package io.github.cowwoc.styler.pipeline.parallel;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;

/**
 * Immutable configuration for parallel batch file processing.
 * <p>
 * Controls concurrency limits, error handling strategy, and progress reporting. Uses memory-based
 * concurrency defaults to prevent out-of-memory conditions while allowing high parallelism.
 * <p>
 * Concurrency Model:
 * <ul>
 *     <li>Default {@code maxConcurrency} is calculated from JVM max memory</li>
 *     <li>Estimated memory per file: 5 MB (conservative for typical source files)</li>
 *     <li>Formula: {@code maxMemory / (5 * 1024 * 1024)} files concurrently</li>
 *     <li>Users can override if needed for specific hardware or file characteristics</li>
 * </ul>
 * <p>
 * <b>Thread-safety</b>: This record is immutable and thread-safe.
 *
 * @param maxConcurrency maximum number of files to process concurrently
 * @param errorStrategy how to handle errors during processing
 * @param progressCallback optional callback for progress reporting, or null for no progress updates
 */
public record ParallelProcessingConfig(
	int maxConcurrency,
	ErrorStrategy errorStrategy,
	ProgressCallback progressCallback)
{
	/**
	 * Creates configuration with validation.
	 * <p>
	 * Validates that {@code maxConcurrency} is positive.
	 *
	 * @param maxConcurrency maximum concurrent files, must be &gt; 0
	 * @param errorStrategy the error handling strategy, must not be null
	 * @param progressCallback optional callback for progress updates, may be null
	 * @throws IllegalArgumentException if {@code maxConcurrency} is not positive
	 * @throws NullPointerException if {@code errorStrategy} is null
	 */
	public ParallelProcessingConfig
	{
		requireThat(maxConcurrency, "maxConcurrency").isGreaterThan(0);
		requireThat(errorStrategy, "errorStrategy").isNotNull();
	}

	/**
	 * Calculates the default maximum concurrency based on JVM memory.
	 * <p>
	 * Uses heuristic: {@code maxMemory / (5 * 1024 * 1024)} to estimate how many files
	 * can be safely processed concurrently in memory.
	 * <p>
	 * This ensures the batch processor respects memory constraints on the target system
	 * while allowing high parallelism when abundant memory is available.
	 *
	 * @return the number of files to process concurrently, minimum 1
	 */
	public static int calculateDefaultMaxConcurrency()
	{
		long maxMemory = Runtime.getRuntime().maxMemory();
		long estimatedMemoryPerFile = 5 * 1024 * 1024; // ~5 MB per file
		return (int) Math.max(1, maxMemory / estimatedMemoryPerFile);
	}

	/**
	 * Creates a builder for fluent configuration.
	 *
	 * @return a new {@code Builder} instance
	 */
	public static Builder builder()
	{
		return new Builder();
	}

	/**
	 * Builder for creating {@code ParallelProcessingConfig} instances.
	 */
	public static final class Builder
	{
		private int maxConcurrency = calculateDefaultMaxConcurrency();
		private ErrorStrategy errorStrategy = ErrorStrategy.CONTINUE;
		private ProgressCallback progressCallback;

		/**
		 * Sets the maximum concurrent files to process.
		 *
		 * @param maxConcurrency the maximum, must be &gt; 0
		 * @return this builder for method chaining
		 * @throws IllegalArgumentException if {@code maxConcurrency} is not positive
		 */
		public Builder maxConcurrency(int maxConcurrency)
		{
			requireThat(maxConcurrency, "maxConcurrency").isGreaterThan(0);
			this.maxConcurrency = maxConcurrency;
			return this;
		}

		/**
		 * Sets the error handling strategy.
		 *
		 * @param errorStrategy the strategy, must not be null
		 * @return this builder for method chaining
		 * @throws NullPointerException if {@code errorStrategy} is null
		 */
		public Builder errorStrategy(ErrorStrategy errorStrategy)
		{
			requireThat(errorStrategy, "errorStrategy").isNotNull();
			this.errorStrategy = errorStrategy;
			return this;
		}

		/**
		 * Sets the progress callback.
		 *
		 * @param progressCallback the callback, or null for no progress updates
		 * @return this builder for method chaining
		 */
		public Builder progressCallback(ProgressCallback progressCallback)
		{
			this.progressCallback = progressCallback;
			return this;
		}

		/**
		 * Builds the {@code ParallelProcessingConfig}.
		 *
		 * @return a new immutable configuration instance
		 */
		public ParallelProcessingConfig build()
		{
			return new ParallelProcessingConfig(maxConcurrency, errorStrategy, progressCallback);
		}
	}
}
