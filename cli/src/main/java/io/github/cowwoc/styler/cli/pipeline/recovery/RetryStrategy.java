package io.github.cowwoc.styler.cli.pipeline.recovery;

import io.github.cowwoc.styler.cli.pipeline.PipelineException;
import io.github.cowwoc.styler.cli.pipeline.StageResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;

/**
 * Error recovery strategy that retries failed operations with exponential backoff.
 * <p>
 * This strategy is appropriate for transient failures like I/O errors or temporary resource
 * unavailability. It attempts the operation multiple times with increasing delays between attempts.
 * <p>
 * Example usage:
 * <pre>{@code
 * ErrorRecoveryStrategy strategy = new RetryStrategy(3, 100);
 * StageResult<Output> result = strategy.recover(ioException, "write", retryOp);
 * // Retries up to 3 times with 100ms, 200ms, 400ms delays
 * }</pre>
 */
public final class RetryStrategy implements ErrorRecoveryStrategy
{
	@SuppressWarnings("PMD.FieldNamingConventions") // Standard SLF4J logger naming convention
	private static final Logger logger = LoggerFactory.getLogger(RetryStrategy.class);

	private final int maxAttempts;
	private final long initialDelayMillis;

	/**
	 * Creates a retry strategy with default settings (3 attempts, 100ms initial delay).
	 */
	public RetryStrategy()
	{
		this(3, 100);
	}

	/**
	 * Creates a retry strategy with custom settings.
	 *
	 * @param maxAttempts the maximum number of retry attempts (must be positive)
	 * @param initialDelayMillis the initial delay between retries in milliseconds (must be positive)
	 * @throws IllegalArgumentException if {@code maxAttempts} or {@code initialDelayMillis} is not positive
	 */
	public RetryStrategy(int maxAttempts, long initialDelayMillis)
	{
		requireThat(maxAttempts, "maxAttempts").isGreaterThan(0);
		requireThat(initialDelayMillis, "initialDelayMillis").isGreaterThan(0L);
		this.maxAttempts = maxAttempts;
		this.initialDelayMillis = initialDelayMillis;
	}

	@Override
	public <T> StageResult<T> recover(PipelineException exception, String stageName,
		RetryOperation<T> retryOperation)
	{
		requireThat(exception, "exception").isNotNull();
		requireThat(stageName, "stageName").isNotEmpty();
		requireThat(retryOperation, "retryOperation").isNotNull();

		logger.warn("Retrying {} stage for file '{}' due to: {}",
			stageName, exception.getFilePath(), exception.getMessage());

		for (int attempt = 1; attempt <= maxAttempts; ++attempt)
		{
			try
			{
				// Exponential backoff: delay = initialDelay * 2^(attempt-1)
				if (attempt > 1)
				{
					long delay = initialDelayMillis * (1L << (attempt - 2));
					logger.debug("Waiting {}ms before retry attempt {}/{}", delay, attempt, maxAttempts);
					Thread.sleep(delay);
				}

				logger.debug("Retry attempt {}/{} for {} stage", attempt, maxAttempts, stageName);
				StageResult<T> result = retryOperation.execute();

				if (result.isSuccess())
				{
					logger.info("Retry successful on attempt {}/{} for {} stage",
						attempt, maxAttempts, stageName);
					return result;
				}

				// Stage returned failure result - retry
				logger.debug("Retry attempt {}/{} returned failure, continuing", attempt, maxAttempts);
			}
			catch (InterruptedException e)
			{
				Thread.currentThread().interrupt();
				logger.error("Retry interrupted for {} stage", stageName, e);
				return StageResult.failure(exception);
			}
			catch (Exception e)
			{
				logger.debug("Retry attempt {}/{} threw exception: {}",
					attempt, maxAttempts, e.getMessage());

				// Last attempt failed
				if (attempt == maxAttempts)
				{
					logger.error("All {} retry attempts failed for {} stage", maxAttempts, stageName, e);
					return StageResult.failure(new PipelineException(
						"Retry failed after " + maxAttempts + " attempts: " + e.getMessage(),
						exception.getFilePath(),
						stageName,
						e));
				}
			}
		}

		// All retries exhausted
		logger.error("All {} retry attempts exhausted for {} stage", maxAttempts, stageName);
		return StageResult.failure(exception);
	}
}
