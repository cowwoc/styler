package io.github.cowwoc.styler.formatter.impl.test;

import io.github.cowwoc.styler.formatter.api.ConfigurationException;
import io.github.cowwoc.styler.formatter.impl.WhitespaceConfiguration;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link WhitespaceConfiguration}.
 * <p>
 * Validates configuration construction, validation, merging, and builder pattern behavior.
 */
public final class WhitespaceConfigurationTest
{
	/**
	 * Verifies that default configuration is created with standard Java formatting conventions.
	 */
	@Test
	public void createDefaultReturnsValidConfiguration()
		throws ConfigurationException
	{
		WhitespaceConfiguration config = WhitespaceConfiguration.createDefault();

		assertThat(config.getOperatorSpacing()).isEqualTo(1);
		assertThat(config.getKeywordSpacing()).isEqualTo(1);
		assertThat(config.isSpaceAfterComma()).isTrue();
		assertThat(config.isSpaceBeforeBrace()).isTrue();
	}

	/**
	 * Verifies that builder constructs configuration with custom spacing values.
	 */
	@Test
	public void builderCreatesConfigurationWithCustomSpacing()
		throws ConfigurationException
	{
		WhitespaceConfiguration config = WhitespaceConfiguration.builder().
			withOperatorSpacing(2).
			withKeywordSpacing(1).
			withSpaceAfterComma(false).
			withSpaceBeforeBrace(true).
			build();

		assertThat(config.getOperatorSpacing()).isEqualTo(2);
		assertThat(config.getKeywordSpacing()).isEqualTo(1);
		assertThat(config.isSpaceAfterComma()).isFalse();
		assertThat(config.isSpaceBeforeBrace()).isTrue();
	}

	/**
	 * Verifies that configuration validation rejects spacing values outside valid range.
	 */
	@Test(expectedExceptions = ConfigurationException.class)
	public void builderThrowsExceptionWhenOperatorSpacingTooLarge()
		throws ConfigurationException
	{
		WhitespaceConfiguration.builder().
			withOperatorSpacing(10).
			build();
	}

	/**
	 * Verifies that configuration validation rejects negative spacing values.
	 */
	@Test(expectedExceptions = ConfigurationException.class)
	public void builderThrowsExceptionWhenKeywordSpacingNegative()
		throws ConfigurationException
	{
		WhitespaceConfiguration.builder().
			withKeywordSpacing(-1).
			build();
	}

	/**
	 * Verifies that merge combines override configuration with base configuration correctly.
	 */
	@Test
	public void mergeReturnsCorrectMergedConfiguration()
		throws ConfigurationException
	{
		WhitespaceConfiguration base = WhitespaceConfiguration.builder().
			withOperatorSpacing(1).
			withKeywordSpacing(1).
			build();

		WhitespaceConfiguration override = WhitespaceConfiguration.builder().
			withOperatorSpacing(2).
			withKeywordSpacing(2).
			build();

		WhitespaceConfiguration merged = (WhitespaceConfiguration) base.merge(override);

		assertThat(merged.getOperatorSpacing()).isEqualTo(2);
		assertThat(merged.getKeywordSpacing()).isEqualTo(2);
	}

	/**
	 * Verifies that configuration descriptions include all parameters for debugging.
	 */
	@Test
	public void getDescriptionIncludesAllParameters()
		throws ConfigurationException
	{
		WhitespaceConfiguration config = WhitespaceConfiguration.createDefault();

		String description = config.getDescription();

		assertThat(description).contains("operatorSpacing=1");
		assertThat(description).contains("keywordSpacing=1");
		assertThat(description).contains("spaceAfterComma=true");
		assertThat(description).contains("spaceBeforeBrace=true");
	}

	/**
	 * Verifies that two configurations with identical parameters are considered equal.
	 */
	@Test
	public void equalsReturnsTrueForIdenticalConfigurations()
		throws ConfigurationException
	{
		WhitespaceConfiguration config1 = WhitespaceConfiguration.builder().
			withOperatorSpacing(1).
			withKeywordSpacing(1).
			build();

		WhitespaceConfiguration config2 = WhitespaceConfiguration.builder().
			withOperatorSpacing(1).
			withKeywordSpacing(1).
			build();

		assertThat(config1).isEqualTo(config2);
		assertThat(config1.hashCode()).isEqualTo(config2.hashCode());
	}
}
