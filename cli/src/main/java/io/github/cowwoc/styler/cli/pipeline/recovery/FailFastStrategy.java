package io.github.cowwoc.styler.cli.pipeline.recovery;

import io.github.cowwoc.styler.cli.pipeline.PipelineException;
import io.github.cowwoc.styler.cli.pipeline.StageResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;

/**
 * Error recovery strategy that immediately fails without attempting recovery.
 * <p>
 * This strategy is appropriate for fatal errors where recovery is not possible or
 * desirable, such as OutOfMemoryError, system failures, or configuration errors.
 * The error is logged and propagated immediately.
 * <p>
 * Example usage:
 * <pre>{@code
 * ErrorRecoveryStrategy strategy = new FailFastStrategy();
 * StageResult<Output> result = strategy.recover(fatalException, "stage", retryOp);
 * // Returns failure immediately without retry
 * }</pre>
 */
public final class FailFastStrategy implements ErrorRecoveryStrategy
{
	@SuppressWarnings("PMD.FieldNamingConventions") // Standard SLF4J logger naming convention
	private static final Logger logger = LoggerFactory.getLogger(FailFastStrategy.class);

	/**
	 * Creates a fail-fast strategy.
	 */
	public FailFastStrategy()
	{
	}

	@Override
	public <T> StageResult<T> recover(PipelineException exception, String stageName,
		RetryOperation<T> retryOperation)
	{
		requireThat(exception, "exception").isNotNull();
		requireThat(stageName, "stageName").isNotEmpty();
		requireThat(retryOperation, "retryOperation").isNotNull();

		logger.error("Fatal error in {} stage for file '{}', failing immediately: {}",
			stageName, exception.getFilePath(), exception.getMessage(), exception);

		// Immediately return failure without attempting recovery
		return StageResult.failure(exception);
	}
}
