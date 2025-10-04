package io.github.cowwoc.styler.cli.pipeline.recovery;

import io.github.cowwoc.styler.cli.pipeline.PipelineException;
import io.github.cowwoc.styler.cli.pipeline.StageResult;

/**
 * Strategy for recovering from pipeline stage failures.
 * <p>
 * Recovery strategies determine how the pipeline should respond when a stage fails:
 * <ul>
 *     <li>{@code SkipFileStrategy} - Skip the failed file and continue with next file</li>
 *     <li>{@code RetryStrategy} - Retry the operation with exponential backoff</li>
 *     <li>{@code FallbackStrategy} - Use an alternative approach or default value</li>
 *     <li>{@code FailFastStrategy} - Terminate pipeline on first failure</li>
 * </ul>
 * <p>
 * Example usage:
 * <pre>{@code
 * ErrorRecoveryStrategy strategy = new RetryStrategy(3, Duration.ofSeconds(1));
 * StageResult<Output> result = strategy.recover(exception, context, () -> stage.execute(input, context));
 * }</pre>
 *
 * @see SkipFileStrategy
 */
@FunctionalInterface
public interface ErrorRecoveryStrategy
{
	/**
	 * Attempts to recover from a stage failure.
	 * <p>
	 * The recovery strategy receives the original exception and can either:
	 * <ul>
	 *     <li>Return a successful result with recovered data</li>
	 *     <li>Return a failure result with the original or transformed exception</li>
	 *     <li>Retry the operation by invoking {@code retryOperation}</li>
	 * </ul>
	 *
	 * @param exception      the exception that caused the failure (never {@code null})
	 * @param stageName      the name of the stage that failed (never {@code null} or empty)
	 * @param retryOperation a supplier that re-executes the failed operation (never {@code null})
	 * @param <T>            the output type
	 * @return a stage result representing the recovery outcome (never {@code null})
	 * @throws NullPointerException if {@code exception}, {@code stageName}, or {@code retryOperation} is {@code null}
	 */
	<T> StageResult<T> recover(PipelineException exception, String stageName,
		RetryOperation<T> retryOperation);

	/**
	 * Functional interface for retry operations.
	 *
	 * @param <T> the output type
	 */
	@FunctionalInterface
	interface RetryOperation<T>
	{
		/**
		 * Executes the operation that may fail.
		 *
		 * @return the stage result (never {@code null})
		 * @throws PipelineException if the operation fails
		 */
		StageResult<T> execute() throws PipelineException;
	}
}
