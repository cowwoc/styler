package io.github.cowwoc.styler.cli.pipeline.recovery;

import io.github.cowwoc.styler.cli.pipeline.PipelineException;
import io.github.cowwoc.styler.cli.pipeline.StageResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;

/**
 * Error recovery strategy that skips the failed file and continues processing.
 * <p>
 * This strategy logs the error and returns a failure result, allowing the pipeline to continue
 * processing other files in the batch. This is appropriate for parse errors or other failures
 * where individual file errors shouldn't halt the entire batch.
 * <p>
 * Example usage:
 * <pre>{@code
 * ErrorRecoveryStrategy strategy = new SkipFileStrategy();
 * StageResult<Output> result = strategy.recover(parseException, "parse", retryOp);
 * // Logs error and returns Failure, pipeline continues with next file
 * }</pre>
 */
public final class SkipFileStrategy implements ErrorRecoveryStrategy
{
	@SuppressWarnings("PMD.FieldNamingConventions") // Standard SLF4J logger naming convention
	private static final Logger logger = LoggerFactory.getLogger(SkipFileStrategy.class);

	@Override
	public <T> StageResult<T> recover(PipelineException exception, String stageName,
		RetryOperation<T> retryOperation)
	{
		requireThat(exception, "exception").isNotNull();
		requireThat(stageName, "stageName").isNotEmpty();
		requireThat(retryOperation, "retryOperation").isNotNull();

		logger.error("Skipping file '{}' due to {} stage failure: {}",
			exception.getFilePath(), stageName, exception.getMessage(), exception);

		return StageResult.failure(exception);
	}
}
