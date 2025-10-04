package io.github.cowwoc.styler.formatter.impl.test;

import io.github.cowwoc.styler.formatter.api.ConfigurationException;
import io.github.cowwoc.styler.formatter.impl.LineLengthConfiguration;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for {@link LineLengthConfiguration}.
 */
public class LineLengthConfigurationTest
{
	/**
	 * Verifies the createDefault() factory method returns a configuration with standard values:
	 * 120 character line length, 4-space tabs, and all wrapping options enabled.
	 */
	@Test
	public void createDefaultReturnsValidConfiguration()
	{
		LineLengthConfiguration config = LineLengthConfiguration.createDefault();

		assertThat(config).isNotNull();
		assertThat(config.getMaxLineLength()).isEqualTo(120);
		assertThat(config.getTabWidth()).isEqualTo(4);
		assertThat(config.isWrapMethodChains()).isTrue();
		assertThat(config.isWrapParameters()).isTrue();
		assertThat(config.isBreakBeforeOperator()).isTrue();
	}

	/**
	 * Verifies the builder can create a configuration with custom values for all parameters.
	 */
	@Test
	public void builderWithValidParametersBuildsSuccessfully() throws ConfigurationException
	{
		LineLengthConfiguration config = LineLengthConfiguration.builder().
			withMaxLineLength(100).withTabWidth(2).withWrapMethodChains(false).
			withWrapParameters(false).withBreakBeforeOperator(false).build();

		assertThat(config.getMaxLineLength()).isEqualTo(100);
		assertThat(config.getTabWidth()).isEqualTo(2);
		assertThat(config.isWrapMethodChains()).isFalse();
		assertThat(config.isWrapParameters()).isFalse();
		assertThat(config.isBreakBeforeOperator()).isFalse();
	}

	/**
	 * Verifies the build() method throws ConfigurationException when maximum line length
	 * is below the minimum allowed value of 40 characters.
	 */
	@Test
	public void builderWithMaxLineLengthTooSmallThrowsConfigurationException()
	{
		assertThatThrownBy(() ->
			LineLengthConfiguration.builder().withMaxLineLength(30).build()).
			isInstanceOf(ConfigurationException.class);
	}

	/**
	 * Verifies the build() method throws ConfigurationException when maximum line length
	 * exceeds the maximum allowed value of 500 characters.
	 */
	@Test
	public void builderWithMaxLineLengthTooLargeThrowsConfigurationException()
	{
		assertThatThrownBy(() ->
			LineLengthConfiguration.builder().withMaxLineLength(600).build()).
			isInstanceOf(ConfigurationException.class);
	}

	/**
	 * Verifies the build() method throws ConfigurationException when tab width
	 * is below the minimum allowed value of 1 space.
	 */
	@Test
	public void builderWithTabWidthTooSmallThrowsConfigurationException()
	{
		assertThatThrownBy(() ->
			LineLengthConfiguration.builder().withTabWidth(0).build()).
			isInstanceOf(ConfigurationException.class);
	}

	/**
	 * Verifies the build() method throws ConfigurationException when tab width
	 * exceeds the maximum allowed value of 8 spaces.
	 */
	@Test
	public void builderWithTabWidthTooLargeThrowsConfigurationException()
	{
		assertThatThrownBy(() ->
			LineLengthConfiguration.builder().withTabWidth(10).build()).
			isInstanceOf(ConfigurationException.class);
	}

	/**
	 * Verifies the getDescription() method returns a human-readable string containing
	 * all configuration parameter values.
	 */
	@Test
	public void getDescriptionReturnsReadableDescription() throws ConfigurationException
	{
		LineLengthConfiguration config = LineLengthConfiguration.createDefault();
		String description = config.getDescription();

		assertThat(description).contains("maxLineLength=120");
		assertThat(description).contains("tabWidth=4");
		assertThat(description).contains("wrapMethodChains=true");
	}

	/**
	 * Verifies two configuration instances with identical values are equal and produce
	 * the same hash code, as required by the equals/hashCode contract.
	 */
	@Test
	public void equalsWithSameValuesReturnsTrue() throws ConfigurationException
	{
		LineLengthConfiguration config1 = LineLengthConfiguration.builder().
			withMaxLineLength(100).build();
		LineLengthConfiguration config2 = LineLengthConfiguration.builder().
			withMaxLineLength(100).build();

		assertThat(config1).isEqualTo(config2);
		assertThat(config1.hashCode()).isEqualTo(config2.hashCode());
	}

	/**
	 * Verifies two configuration instances with different parameter values are not equal.
	 */
	@Test
	public void equalsWithDifferentValuesReturnsFalse() throws ConfigurationException
	{
		LineLengthConfiguration config1 = LineLengthConfiguration.builder().
			withMaxLineLength(100).build();
		LineLengthConfiguration config2 = LineLengthConfiguration.builder().
			withMaxLineLength(110).build();

		assertThat(config1).isNotEqualTo(config2);
	}
}
