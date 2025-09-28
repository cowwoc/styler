package io.github.cowwoc.styler.formatter.api;

import org.testng.annotations.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for the ValidationResult class.
 */
public class ValidationResultTest
{
	@Test
	public void testSuccessfulValidation()
	{
		ValidationResult result = ValidationResult.success();

		assertThat(result.isValid()).isTrue();
		assertThat(result.isFailure()).isFalse();
		assertThat(result.getErrorMessages()).isEmpty();
		assertThat(result.getFirstErrorMessage()).isNull();
		assertThat(result.getCombinedErrorMessage()).isNull();
	}

	@Test
	public void testFailedValidationWithSingleError()
	{
		String errorMessage = "Invalid configuration parameter";
		ValidationResult result = ValidationResult.failure(errorMessage);

		assertThat(result.isValid()).isFalse();
		assertThat(result.isFailure()).isTrue();
		assertThat(result.getErrorMessages()).containsExactly(errorMessage);
		assertThat(result.getFirstErrorMessage()).isEqualTo(errorMessage);
		assertThat(result.getCombinedErrorMessage()).isEqualTo(errorMessage);
	}

	@Test
	public void testFailedValidationWithMultipleErrors()
	{
		List<String> errorMessages = List.of("Error 1", "Error 2", "Error 3");
		ValidationResult result = ValidationResult.failure(errorMessages);

		assertThat(result.isValid()).isFalse();
		assertThat(result.isFailure()).isTrue();
		assertThat(result.getErrorMessages()).containsExactlyElementsOf(errorMessages);
		assertThat(result.getFirstErrorMessage()).isEqualTo("Error 1");
		assertThat(result.getCombinedErrorMessage()).isEqualTo("Error 1; Error 2; Error 3");
	}

	@Test
	public void testFailureWithNullErrorMessage()
	{
		assertThatThrownBy(() -> ValidationResult.failure((String) null))
			.isInstanceOf(NullPointerException.class)
			.hasMessageContaining("Error message cannot be null");
	}

	@Test
	public void testFailureWithNullErrorMessageList()
	{
		assertThatThrownBy(() -> ValidationResult.failure((List<String>) null))
			.isInstanceOf(NullPointerException.class)
			.hasMessageContaining("Error messages cannot be null");
	}

	@Test
	public void testFailureWithEmptyErrorMessageList()
	{
		assertThatThrownBy(() -> ValidationResult.failure(List.of()))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("Error messages list cannot be empty");
	}

	@Test
	public void testEqualsAndHashCode()
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

	@Test
	public void testToString()
	{
		ValidationResult success = ValidationResult.success();
		ValidationResult failure = ValidationResult.failure("Test error");

		assertThat(success.toString()).contains("valid=true");
		assertThat(failure.toString()).contains("valid=false").contains("Test error");
	}
}