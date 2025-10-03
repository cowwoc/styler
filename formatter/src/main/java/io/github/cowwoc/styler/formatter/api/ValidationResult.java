package io.github.cowwoc.styler.formatter.api;



import java.util.List;
import java.util.Objects;

/**
 * Represents the result of validating a formatting context against a rule.
 * <p>
 * This immutable value object indicates whether a rule can be applied to a given
 * context and provides detailed error messages if validation fails.
 */
public final class ValidationResult
{
	private static final ValidationResult SUCCESS = new ValidationResult(true, List.of());

	private final boolean valid;
	private final List<String> errorMessages;

	/**
	 * Creates a new validation result.
	 *
	 * @param valid         whether the validation succeeded
	 * @param errorMessages list of error messages for failed validation, never {@code null}
	 */
	private ValidationResult(boolean valid,  List<String> errorMessages)
	{
		this.valid = valid;
		this.errorMessages = List.copyOf(errorMessages);
	}

	/**
	 * Creates a successful validation result.
	 *
	 * @return a validation result indicating success, never {@code null}
	 */

	public static ValidationResult success()
	{
		return SUCCESS;
	}

	/**
	 * Creates a failed validation result with a single error message.
	 *
	 * @param errorMessage the error message describing why validation failed, never {@code null}
	 * @return a validation result indicating failure, never {@code null}
	 */

	public static ValidationResult failure( String errorMessage)
	{
		Objects.requireNonNull(errorMessage, "Error message cannot be null");
		return new ValidationResult(false, List.of(errorMessage));
	}

	/**
	 * Creates a failed validation result with multiple error messages.
	 *
	 * @param errorMessages the list of error messages, never {@code null} or empty
	 * @return a validation result indicating failure, never {@code null}
	 */

	public static ValidationResult failure( List<String> errorMessages)
	{
		Objects.requireNonNull(errorMessages, "Error messages cannot be null");
		if (errorMessages.isEmpty())
		{
			throw new IllegalArgumentException("Error messages list cannot be empty for failure result");
		}
		return new ValidationResult(false, errorMessages);
	}

	/**
	 * Returns whether the validation was successful.
	 *
	 * @return {@code true} if validation succeeded, {@code false} if it failed
	 */
	public boolean isValid()
	{
		return valid;
	}

	/**
	 * Returns whether the validation failed.
	 *
	 * @return {@code true} if validation failed, {@code false} if it succeeded
	 */
	public boolean isFailure()
	{
		return !valid;
	}

	/**
	 * Returns the list of error messages for failed validation.
	 * <p>
	 * For successful validation, this list is empty.
	 *
	 * @return the list of error messages, never {@code null} but may be empty
	 */

	public List<String> getErrorMessages()
	{
		return errorMessages;
	}

	/**
	 * Returns the first error message, if any.
	 *
	 * @return the first error message, or {@code null} if validation succeeded
	 */

	public String getFirstErrorMessage()
	{
		if (errorMessages.isEmpty())
		{
			return null;
		}
		return errorMessages.get(0);
	}

	/**
	 * Returns a combined error message containing all validation failures.
	 *
	 * @return a combined error message, or {@code null} if validation succeeded
	 */

	public String getCombinedErrorMessage()
	{
		if (errorMessages.isEmpty())
		{
			return null;
		}
		if (errorMessages.size() == 1)
		{
			return errorMessages.get(0);
		}
		return String.join("; ", errorMessages);
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj) return true;
		if (obj == null || getClass() != obj.getClass()) return false;
		ValidationResult that = (ValidationResult) obj;
		return valid == that.valid && Objects.equals(errorMessages, that.errorMessages);
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(valid, errorMessages);
	}

	@Override
	public String toString()
	{
		if (valid)
		{
			return "ValidationResult{valid=true}";
		}
		return "ValidationResult{valid=false, errors=" + errorMessages + "}";
	}
}