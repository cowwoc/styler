package io.github.cowwoc.styler.cli.pipeline;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;

/**
 * Represents the result of a pipeline stage execution.
 * <p>
 * A stage result is either a {@link Success} containing output data, or a {@link Failure} containing
 * error information. This sealed interface enforces exhaustive pattern matching and prevents null returns.
 * <p>
 * Example usage with pattern matching:
 * <pre>{@code
 * StageResult<AST> result = parseStage.execute(sourceCode, context);
 * return switch (result) {
 *     case Success<AST> success -> processAST(success.output());
 *     case Failure<AST> failure -> handleError(failure.exception());
 * };
 * }</pre>
 *
 * @param <T> the type of output produced on success
 */
public sealed interface StageResult<T> permits StageResult.Success, StageResult.Failure
{
	/**
	 * Returns whether this result represents a successful execution.
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
	 * Applies the given function to the output if this is a {@link Success}.
	 *
	 * @param mapper the function to apply to the output (never {@code null})
	 * @param <U>    the type of the mapped result
	 * @return a new StageResult with the mapped output, or the same Failure if this is a {@link Failure}
	 * @throws NullPointerException if {@code mapper} is {@code null}
	 */
	<U> StageResult<U> map(Function<? super T, ? extends U> mapper);

	/**
	 * Executes the given action if this is a {@link Success}.
	 *
	 * @param action the action to execute with the output (never {@code null})
	 * @throws NullPointerException if {@code action} is {@code null}
	 */
	void ifSuccess(Consumer<? super T> action);

	/**
	 * Executes the given action if this is a {@link Failure}.
	 *
	 * @param action the action to execute with the exception (never {@code null})
	 * @throws NullPointerException if {@code action} is {@code null}
	 */
	void ifFailure(Consumer<PipelineException> action);

	/**
	 * Creates a successful result containing the given output.
	 *
	 * @param output the output value (never {@code null})
	 * @param <T>    the output type
	 * @return a new Success result (never {@code null})
	 * @throws NullPointerException if {@code output} is {@code null}
	 */
	static <T> StageResult<T> success(T output)
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
	static <T> StageResult<T> failure(PipelineException exception)
	{
		return new Failure<>(exception);
	}

	/**
	 * Represents a successful stage execution.
	 *
	 * @param value the output produced by the stage (never {@code null})
	 * @param <T>   the output type
	 */
	record Success<T>(T value) implements StageResult<T>
	{
		/**
		 * Compact constructor enforcing invariants.
		 *
		 * @param value the output produced by the stage
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

		@Override
		public <U> StageResult<U> map(Function<? super T, ? extends U> mapper)
		{
			requireThat(mapper, "mapper").isNotNull();
			return success(mapper.apply(value));
		}

		@Override
		public void ifSuccess(Consumer<? super T> action)
		{
			requireThat(action, "action").isNotNull();
			action.accept(value);
		}

		@Override
		public void ifFailure(Consumer<PipelineException> action)
		{
			requireThat(action, "action").isNotNull();
			// No action for success
		}
	}

	/**
	 * Represents a failed stage execution.
	 *
	 * @param error the exception describing the failure (never {@code null})
	 * @param <T>   the output type
	 */
	record Failure<T>(PipelineException error) implements StageResult<T>
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

		@Override
		@SuppressWarnings("unchecked")
		public <U> StageResult<U> map(Function<? super T, ? extends U> mapper)
		{
			requireThat(mapper, "mapper").isNotNull();
			return (StageResult<U>) this;
		}

		@Override
		public void ifSuccess(Consumer<? super T> action)
		{
			requireThat(action, "action").isNotNull();
			// No action for failure
		}

		@Override
		public void ifFailure(Consumer<PipelineException> action)
		{
			requireThat(action, "action").isNotNull();
			action.accept(error);
		}
	}
}
