package io.github.cowwoc.styler.cli.pipeline;

import java.util.Optional;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;

/**
 * Represents the final result of pipeline execution.
 * <p>
 * Similar to {@link StageResult}, but represents the outcome of the entire pipeline rather than
 * a single stage. This sealed interface enforces exhaustive pattern matching.
 *
 * @param <T> the type of output produced on success
 */
public sealed interface PipelineResult<T> permits PipelineResult.Success, PipelineResult.Failure
{
	/**
	 * Returns whether this result represents a successful pipeline execution.
	 *
	 * @return {@code true} if this is a {@link Success}, {@code false} if this is a {@link Failure}
	 */
	boolean isSuccess();

	/**
	 * Returns the output value if this is a {@link Success}.
	 *
	 * @return an Optional containing the output, or empty if this is a {@link Failure}
	 */
	Optional<T> output();

	/**
	 * Returns the exception if this is a {@link Failure}.
	 *
	 * @return an Optional containing the exception, or empty if this is a {@link Success}
	 */
	Optional<PipelineException> exception();

	/**
	 * Creates a successful result containing the given output.
	 *
	 * @param output the output value (never {@code null})
	 * @param <T>    the output type
	 * @return a new Success result (never {@code null})
	 * @throws NullPointerException if {@code output} is {@code null}
	 */
	static <T> PipelineResult<T> success(T output)
	{
		return new Success<>(output);
	}

	/**
	 * Creates a failure result containing the given exception.
	 *
	 * @param exception the exception describing the failure (never {@code null})
	 * @param <T>       the output type
	 * @return a new Failure result (never {@code null})
	 * @throws NullPointerException if {@code exception} is {@code null}
	 */
	static <T> PipelineResult<T> failure(PipelineException exception)
	{
		return new Failure<>(exception);
	}

	/**
	 * Represents a successful pipeline execution.
	 *
	 * @param value the output produced by the pipeline (never {@code null})
	 * @param <T>   the output type
	 */
	record Success<T>(T value) implements PipelineResult<T>
	{
		/**
		 * Compact constructor enforcing invariants.
		 *
		 * @param value the output produced by the pipeline
		 * @throws NullPointerException if {@code value} is {@code null}
		 */
		public Success
		{
			requireThat(value, "value").isNotNull();
		}

		@Override
		public boolean isSuccess()
		{
			return true;
		}

		@Override
		public Optional<T> output()
		{
			return Optional.of(value);
		}

		@Override
		public Optional<PipelineException> exception()
		{
			return Optional.empty();
		}
	}

	/**
	 * Represents a failed pipeline execution.
	 *
	 * @param error the exception describing the failure (never {@code null})
	 * @param <T>   the output type
	 */
	record Failure<T>(PipelineException error) implements PipelineResult<T>
	{
		/**
		 * Compact constructor enforcing invariants.
		 *
		 * @param error the exception describing the failure
		 * @throws NullPointerException if {@code error} is {@code null}
		 */
		public Failure
		{
			requireThat(error, "error").isNotNull();
		}

		@Override
		public boolean isSuccess()
		{
			return false;
		}

		@Override
		public Optional<T> output()
		{
			return Optional.empty();
		}

		@Override
		public Optional<PipelineException> exception()
		{
			return Optional.of(error);
		}
	}
}
