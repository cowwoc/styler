package io.github.cowwoc.styler.formatter.api.test;

import io.github.cowwoc.styler.formatter.api.ConfigurationException;
import io.github.cowwoc.styler.formatter.api.WrapConfiguration;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for {@link WrapConfiguration}.
 */
public class WrapConfigurationTest
{
	/**
	 * Verifies the createDefault() factory method returns a configuration with standard values:
	 * 120 character line length, 8-space continuation indent, 4-space tabs, wrap before operators,
	 * wrap before dot, no URL wrapping in strings, and URL protection enabled.
	 */
	@Test
	public void createDefaultReturnsValidConfiguration()
	{
		WrapConfiguration config = WrapConfiguration.createDefault();

		assertThat(config).isNotNull();
		assertThat(config.getMaxLineLength()).isEqualTo(120);
		assertThat(config.getContinuationIndentSpaces()).isEqualTo(8);
		assertThat(config.getTabWidth()).isEqualTo(4);
		assertThat(config.isWrapBeforeOperator()).isTrue();
		assertThat(config.isWrapBeforeDot()).isTrue();
		assertThat(config.isWrapAfterUrlsInStrings()).isFalse();
		assertThat(config.isUrlProtection()).isTrue();
	}

	/**
	 * Verifies the builder can create a configuration with custom values for all parameters.
	 */
	@Test
	public void builderWithValidParametersBuildsSuccessfully() throws ConfigurationException
	{
		WrapConfiguration config = WrapConfiguration.builder().
			withMaxLineLength(100).
			withContinuationIndentSpaces(4).
			withTabWidth(2).
			withWrapBeforeOperator(false).
			withWrapBeforeDot(false).
			withWrapAfterUrlsInStrings(true).
			withUrlProtection(false).
			build();

		assertThat(config.getMaxLineLength()).isEqualTo(100);
		assertThat(config.getContinuationIndentSpaces()).isEqualTo(4);
		assertThat(config.getTabWidth()).isEqualTo(2);
		assertThat(config.isWrapBeforeOperator()).isFalse();
		assertThat(config.isWrapBeforeDot()).isFalse();
		assertThat(config.isWrapAfterUrlsInStrings()).isTrue();
		assertThat(config.isUrlProtection()).isFalse();
	}

	/**
	 * Verifies the build() method throws ConfigurationException when maximum line length
	 * is below the minimum allowed value of 40 characters.
	 */
	@Test
	public void builderWithMaxLineLengthTooSmallThrowsConfigurationException()
	{
		assertThatThrownBy(() ->
			WrapConfiguration.builder().withMaxLineLength(30).build()).
			isInstanceOf(ConfigurationException.class).
			hasMessageContaining("maxLineLength must be between 40 and 500");
	}

	/**
	 * Verifies the build() method throws ConfigurationException when maximum line length
	 * exceeds the maximum allowed value of 500 characters.
	 */
	@Test
	public void builderWithMaxLineLengthTooLargeThrowsConfigurationException()
	{
		assertThatThrownBy(() ->
			WrapConfiguration.builder().withMaxLineLength(600).build()).
			isInstanceOf(ConfigurationException.class).
			hasMessageContaining("maxLineLength must be between 40 and 500");
	}

	/**
	 * Verifies the build() method accepts the minimum allowed line length of 40 characters.
	 */
	@Test
	public void builderWithMinimumLineLengthBuildsSuccessfully() throws ConfigurationException
	{
		WrapConfiguration config = WrapConfiguration.builder().
			withMaxLineLength(40).
			build();

		assertThat(config.getMaxLineLength()).isEqualTo(40);
	}

	/**
	 * Verifies the build() method accepts the maximum allowed line length of 500 characters.
	 */
	@Test
	public void builderWithMaximumLineLengthBuildsSuccessfully() throws ConfigurationException
	{
		WrapConfiguration config = WrapConfiguration.builder().
			withMaxLineLength(500).
			build();

		assertThat(config.getMaxLineLength()).isEqualTo(500);
	}

	/**
	 * Verifies the build() method throws ConfigurationException when continuation indent
	 * is below the minimum allowed value of 2 spaces.
	 */
	@Test
	public void builderWithContinuationIndentTooSmallThrowsConfigurationException()
	{
		assertThatThrownBy(() ->
			WrapConfiguration.builder().withContinuationIndentSpaces(1).build()).
			isInstanceOf(ConfigurationException.class).
			hasMessageContaining("continuationIndentSpaces must be between 2 and 16");
	}

	/**
	 * Verifies the build() method throws ConfigurationException when continuation indent
	 * exceeds the maximum allowed value of 16 spaces.
	 */
	@Test
	public void builderWithContinuationIndentTooLargeThrowsConfigurationException()
	{
		assertThatThrownBy(() ->
			WrapConfiguration.builder().withContinuationIndentSpaces(20).build()).
			isInstanceOf(ConfigurationException.class).
			hasMessageContaining("continuationIndentSpaces must be between 2 and 16");
	}

	/**
	 * Verifies the build() method accepts the minimum allowed continuation indent of 2 spaces.
	 */
	@Test
	public void builderWithMinimumContinuationIndentBuildsSuccessfully() throws ConfigurationException
	{
		WrapConfiguration config = WrapConfiguration.builder().
			withContinuationIndentSpaces(2).
			build();

		assertThat(config.getContinuationIndentSpaces()).isEqualTo(2);
	}

	/**
	 * Verifies the build() method accepts the maximum allowed continuation indent of 16 spaces.
	 */
	@Test
	public void builderWithMaximumContinuationIndentBuildsSuccessfully() throws ConfigurationException
	{
		WrapConfiguration config = WrapConfiguration.builder().
			withContinuationIndentSpaces(16).
			build();

		assertThat(config.getContinuationIndentSpaces()).isEqualTo(16);
	}

	/**
	 * Verifies the build() method throws ConfigurationException when tab width
	 * is below the minimum allowed value of 1 space.
	 */
	@Test
	public void builderWithTabWidthTooSmallThrowsConfigurationException()
	{
		assertThatThrownBy(() ->
			WrapConfiguration.builder().withTabWidth(0).build()).
			isInstanceOf(ConfigurationException.class).
			hasMessageContaining("tabWidth must be between 1 and 8");
	}

	/**
	 * Verifies the build() method throws ConfigurationException when tab width
	 * exceeds the maximum allowed value of 8 spaces.
	 */
	@Test
	public void builderWithTabWidthTooLargeThrowsConfigurationException()
	{
		assertThatThrownBy(() ->
			WrapConfiguration.builder().withTabWidth(10).build()).
			isInstanceOf(ConfigurationException.class).
			hasMessageContaining("tabWidth must be between 1 and 8");
	}

	/**
	 * Verifies the build() method accepts the minimum allowed tab width of 1 space.
	 */
	@Test
	public void builderWithMinimumTabWidthBuildsSuccessfully() throws ConfigurationException
	{
		WrapConfiguration config = WrapConfiguration.builder().
			withTabWidth(1).
			build();

		assertThat(config.getTabWidth()).isEqualTo(1);
	}

	/**
	 * Verifies the build() method accepts the maximum allowed tab width of 8 spaces.
	 */
	@Test
	public void builderWithMaximumTabWidthBuildsSuccessfully() throws ConfigurationException
	{
		WrapConfiguration config = WrapConfiguration.builder().
			withTabWidth(8).
			build();

		assertThat(config.getTabWidth()).isEqualTo(8);
	}

	/**
	 * Verifies the toString() method returns a human-readable string containing
	 * all configuration parameter values.
	 */
	@Test
	public void toStringReturnsReadableDescription() throws ConfigurationException
	{
		WrapConfiguration config = WrapConfiguration.createDefault();
		String description = config.toString();

		assertThat(description).contains("maxLineLength=120");
		assertThat(description).contains("wrapBeforeOperator=true");
		assertThat(description).contains("wrapBeforeDot=true");
		assertThat(description).contains("wrapAfterUrlsInStrings=false");
		assertThat(description).contains("urlProtection=true");
		assertThat(description).contains("continuationIndentSpaces=8");
		assertThat(description).contains("tabWidth=4");
	}

	/**
	 * Verifies two configuration instances with identical values are equal and produce
	 * the same hash code, as required by the equals/hashCode contract.
	 */
	@Test
	public void equalsWithSameValuesReturnsTrue() throws ConfigurationException
	{
		WrapConfiguration config1 = WrapConfiguration.builder().
			withMaxLineLength(100).
			withContinuationIndentSpaces(4).
			build();
		WrapConfiguration config2 = WrapConfiguration.builder().
			withMaxLineLength(100).
			withContinuationIndentSpaces(4).
			build();

		assertThat(config1).isEqualTo(config2);
		assertThat(config1.hashCode()).isEqualTo(config2.hashCode());
	}

	/**
	 * Verifies two configuration instances with different parameter values are not equal.
	 */
	@Test
	public void equalsWithDifferentValuesReturnsFalse() throws ConfigurationException
	{
		WrapConfiguration config1 = WrapConfiguration.builder().
			withMaxLineLength(100).
			build();
		WrapConfiguration config2 = WrapConfiguration.builder().
			withMaxLineLength(110).
			build();

		assertThat(config1).isNotEqualTo(config2);
	}

	/**
	 * Verifies configuration instance is not equal to null.
	 */
	@Test
	public void equalsWithNullReturnsFalse()
	{
		WrapConfiguration config = WrapConfiguration.createDefault();

		assertThat(config).isNotEqualTo(null);
	}

	/**
	 * Verifies configuration instance is equal to itself (reflexive property).
	 */
	@Test
	public void equalsWithSelfReturnsTrue()
	{
		WrapConfiguration config = WrapConfiguration.createDefault();

		assertThat(config).isEqualTo(config);
	}

	/**
	 * Verifies the validate() method does not throw when configuration is valid.
	 */
	@Test
	public void validateWithValidConfigurationDoesNotThrow() throws ConfigurationException
	{
		WrapConfiguration config = WrapConfiguration.createDefault();
		config.validate(); // Should not throw
	}

	/**
	 * Verifies builder method chaining works correctly for all parameters.
	 */
	@Test
	public void builderSupportsMethodChaining() throws ConfigurationException
	{
		WrapConfiguration.Builder builder = WrapConfiguration.builder();

		WrapConfiguration config = builder.
			withMaxLineLength(80).
			withContinuationIndentSpaces(4).
			withTabWidth(2).
			withWrapBeforeOperator(false).
			withWrapBeforeDot(false).
			withWrapAfterUrlsInStrings(true).
			withUrlProtection(false).
			build();

		assertThat(config.getMaxLineLength()).isEqualTo(80);
		assertThat(config.getContinuationIndentSpaces()).isEqualTo(4);
		assertThat(config.getTabWidth()).isEqualTo(2);
		assertThat(config.isWrapBeforeOperator()).isFalse();
		assertThat(config.isWrapBeforeDot()).isFalse();
		assertThat(config.isWrapAfterUrlsInStrings()).isTrue();
		assertThat(config.isUrlProtection()).isFalse();
	}
}
