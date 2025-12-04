package io.github.cowwoc.styler.pipeline;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;

import java.util.Optional;

/**
 * Represents the result of a pipeline stage execution using Railway-Oriented Programming (ROP).
 * <p>
 * This sealed interface enforces explicit handling of success, failure, and skip outcomes. Unlike
 * exception-based control flow, ROP makes error handling compositional and predictable.
 * <p>
 * Implementations:
 * <ul>
 *     <li>{@link Success} - Stage completed successfully with optional data payload</li>
 *     <li>{@link Failure} - Stage failed with error details</li>
 *     <li>{@link Skipped} - Stage was skipped (not an error)</li>
 * </ul>
 * <p>
 * Example usage:
 * <pre>
 * StageResult result = stage.execute(context);
 * if (result instanceof StageResult.Success success)
 * {
 *     Object data = success.data(); // Direct accessor for record component
 *     Optional&lt;Object&gt; optData = success.optionalData(); // Wrapped in Optional
 *     // Process successful result
 * }
 * else if (result instanceof StageResult.Failure failure)
 * {
 *     String error = failure.message();
 *     Throwable cause = failure.cause();
 *     // Handle error
 * }
 * else if (result instanceof StageResult.Skipped skipped)
 * {
 *     String reason = skipped.reason();
 *     // Log skip reason
 * }
 * </pre>
 */
public sealed interface StageResult
		permits StageResult.Success, StageResult.Failure, StageResult.Skipped
{
	/**
	 * Checks if this result represents a successful outcome.
	 *
	 * @return true if this is {@code Success} or {@code Skipped}, false if {@code Failure}
	 */
	boolean isSuccess();

	/**
	 * Returns the error message if this result is a {@code Failure}.
	 *
	 * @return Optional containing error message if {@code Failure}, empty if {@code Success} or
	 *     {@code Skipped}
	 */
	Optional<String> errorMessage();

	/**
	 * Returns the data payload if this result is a {@code Success}.
	 *
	 * @return Optional containing data if {@code Success}, empty otherwise
	 */
	default Optional<Object> optionalData()
	{
		return Optional.empty();
	}

	/**
	 * Represents successful completion of a stage.
	 *
	 * @param data the result data (may be null)
	 */
	record Success(Object data) implements StageResult
	{
		/**
		 * Creates a {@code Success} result with optional data.
		 *
		 * @param data the result data (may be null)
		 */
		public Success
		{
			// data may be null
		}

		@Override
		public boolean isSuccess()
		{
			return true;
		}

		@Override
		public Optional<String> errorMessage()
		{
			return Optional.empty();
		}

		@Override
		public Optional<Object> optionalData()
		{
			return Optional.ofNullable(data);
		}
	}

	/**
	 * Represents failure of a stage.
	 *
	 * @param message the error message (required)
	 * @param cause the underlying cause exception (may be null)
	 */
	record Failure(String message, Throwable cause) implements StageResult
	{
		/**
		 * Creates a {@code Failure} result with error details.
		 *
		 * @param message the error message (required)
		 * @param cause the underlying cause exception (may be null)
		 * @throws NullPointerException if {@code message} is null
		 * @throws IllegalArgumentException if {@code message} is empty
		 */
		public Failure
		{
			requireThat(message, "message").isNotEmpty();
		}

		@Override
		public boolean isSuccess()
		{
			return false;
		}

		@Override
		public Optional<String> errorMessage()
		{
			return Optional.of(message);
		}
	}

	/**
	 * Represents skipping of a stage (not an error condition).
	 *
	 * @param reason the reason for skipping
	 */
	record Skipped(String reason) implements StageResult
	{
		/**
		 * Creates a {@code Skipped} result with reason for skipping.
		 *
		 * @param reason the reason for skipping
		 * @throws NullPointerException if {@code reason} is null
		 * @throws IllegalArgumentException if {@code reason} is empty
		 */
		public Skipped
		{
			requireThat(reason, "reason").isNotEmpty();
		}

		@Override
		public boolean isSuccess()
		{
			return true;
		}

		@Override
		public Optional<String> errorMessage()
		{
			return Optional.empty();
		}
	}
}
