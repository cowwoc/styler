package io.github.cowwoc.styler.cli.pipeline.recovery;

import io.github.cowwoc.styler.cli.pipeline.PipelineException;
import io.github.cowwoc.styler.cli.pipeline.StageResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;

/**
 * Error recovery strategy that uses a fallback value when the primary operation fails.
 * <p>
 * This strategy is appropriate for non-critical failures where a safe default can be used.
 * For example, formatting failures can fall back to the original source text.
 * <p>
 * Example usage:
 * <pre>{@code
 * String originalSource = "public class Test { }";
 * ErrorRecoveryStrategy strategy = new FallbackStrategy<>(originalSource);
 * StageResult<String> result = strategy.recover(formatException, "format", retryOp);
 * // Returns success with original source text
 * }</pre>
 *
 * @param <T> the type of the fallback value
 */
public final class FallbackStrategy<T> implements ErrorRecoveryStrategy
{
	// Standard SLF4J logger naming convention
	@SuppressWarnings("PMD.FieldNamingConventions")
	private static final Logger logger = LoggerFactory.getLogger(FallbackStrategy.class);

	private final T fallbackValue;

	/**
	 * Creates a fallback strategy with the specified fallback value.
	 *
	 * @param fallbackValue the value to return when recovery is needed (never {@code null})
	 * @throws NullPointerException if {@code fallbackValue} is {@code null}
	 */
	public FallbackStrategy(T fallbackValue)
	{
		requireThat(fallbackValue, "fallbackValue").isNotNull();
		this.fallbackValue = fallbackValue;
	}

	@Override
	@SuppressWarnings("unchecked")
	public <U> StageResult<U> recover(PipelineException exception, String stageName,
		RetryOperation<U> retryOperation)
	{
		requireThat(exception, "exception").isNotNull();
		requireThat(stageName, "stageName").isNotEmpty();
		requireThat(retryOperation, "retryOperation").isNotNull();

		logger.warn("Using fallback value for {} stage due to failure on file '{}': {}",
			stageName, exception.getFilePath(), exception.getMessage());

		try
		{
			// Cast is safe because the strategy is parameterized with the correct type
			return StageResult.success((U) fallbackValue);
		}
		catch (ClassCastException e)
		{
			logger.error("Fallback value type mismatch for {} stage", stageName, e);
			return StageResult.failure(new PipelineException(
				"Fallback value type mismatch: " + e.getMessage(),
				exception.getFilePath(),
				stageName,
				e));
		}
	}
}
