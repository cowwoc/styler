package io.github.cowwoc.styler.formatter.api.test;

import io.github.cowwoc.styler.formatter.api.*;
import org.testng.annotations.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for the ValidationResult class.
 */
public class ValidationResultTest
{
	/**
	 * Test successful validation.
	 */
	@Test
	public void successfulValidation()
	{
		ValidationResult result = ValidationResult.success();

		assertThat(result.isValid()).isTrue();
		assertThat(result.isFailure()).isFalse();
		assertThat(result.getErrorMessages()).isEmpty();
		assertThat(result.getFirstErrorMessage()).isNull();
		assertThat(result.getCombinedErrorMessage()).isNull();
	}

	/**
	 * Test failed validation with single error.
	 */
	@Test
	public void failedValidationWithSingleError()
	{
		String errorMessage = "Invalid configuration parameter";
		ValidationResult result = ValidationResult.failure(errorMessage);

		assertThat(result.isValid()).isFalse();
		assertThat(result.isFailure()).isTrue();
		assertThat(result.getErrorMessages()).containsExactly(errorMessage);
		assertThat(result.getFirstErrorMessage()).isEqualTo(errorMessage);
		assertThat(result.getCombinedErrorMessage()).isEqualTo(errorMessage);
	}

	/**
	 * Test failed validation with multiple errors.
	 */
	@Test
	public void failedValidationWithMultipleErrors()
	{
		List<String> errorMessages = List.of("Error 1", "Error 2", "Error 3");
		ValidationResult result = ValidationResult.failure(errorMessages);

		assertThat(result.isValid()).isFalse();
		assertThat(result.isFailure()).isTrue();
		assertThat(result.getErrorMessages()).containsExactlyElementsOf(errorMessages);
		assertThat(result.getFirstErrorMessage()).isEqualTo("Error 1");
		assertThat(result.getCombinedErrorMessage()).isEqualTo("Error 1; Error 2; Error 3");
	}

	/**
	 * Test failure with null error message.
	 */
	@Test
	public void failureWithNullErrorMessage()
	{
		assertThatThrownBy(() -> ValidationResult.failure((String) null)).
			isInstanceOf(NullPointerException.class).
			hasMessageContaining("Error message cannot be null");
	}

	/**
	 * Test failure with null error message list.
	 */
	@Test
	public void failureWithNullErrorMessageList()
	{
		assertThatThrownBy(() -> ValidationResult.failure((List<String>) null)).
			isInstanceOf(NullPointerException.class).
			hasMessageContaining("Error messages cannot be null");
	}

	/**
	 * Test failure with empty error message list.
	 */
	@Test
	public void failureWithEmptyErrorMessageList()
	{
		assertThatThrownBy(() -> ValidationResult.failure(List.of())).
			isInstanceOf(IllegalArgumentException.class).
			hasMessageContaining("Error messages list cannot be empty");
	}

	/**
	 * Test equals and hash code.
	 */
	@Test
	public void equalsAndHashCode()
	{
		ValidationResult success1 = ValidationResult.success();
		ValidationResult success2 = ValidationResult.success();
		ValidationResult failure1 = ValidationResult.failure("Error");
		ValidationResult failure2 = ValidationResult.failure("Error");
		ValidationResult failure3 = ValidationResult.failure("Different error");

		// Test equality
		assertThat(success1).isEqualTo(success2);
		assertThat(failure1).isEqualTo(failure2);
		assertThat(success1).isNotEqualTo(failure1);
		assertThat(failure1).isNotEqualTo(failure3);

		// Test hash code consistency
		assertThat(success1.hashCode()).isEqualTo(success2.hashCode());
		assertThat(failure1.hashCode()).isEqualTo(failure2.hashCode());
	}

	/**
	 * Verifies that toString() returns a meaningful string representation.
	 */
	@Test
	public void toStringRepresentation()
	{
		ValidationResult success = ValidationResult.success();
		ValidationResult failure = ValidationResult.failure("Test error");

		assertThat(success.toString()).contains("valid=true");
		assertThat(failure.toString()).contains("valid=false").contains("Test error");
	}
}