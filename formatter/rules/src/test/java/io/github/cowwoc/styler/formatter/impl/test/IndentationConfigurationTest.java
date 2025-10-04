package io.github.cowwoc.styler.formatter.impl.test;

import io.github.cowwoc.styler.formatter.api.ConfigurationException;
import io.github.cowwoc.styler.formatter.impl.IndentationConfiguration;
import io.github.cowwoc.styler.formatter.impl.IndentationMode;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link IndentationConfiguration}.
 */
public final class IndentationConfigurationTest
{
	/**
	 * Verifies that the default configuration uses spaces mode with 4-space indentation.
	 */
	@Test
	public void defaultConfigurationUsesSpacesMode()
	{
		IndentationConfiguration config = IndentationConfiguration.createDefault();

		assertThat(config.getMode()).isEqualTo(IndentationMode.SPACES);
		assertThat(config.getIndentSize()).isEqualTo(4);
		assertThat(config.getContinuationIndent()).isEqualTo(4);
		assertThat(config.getTabWidth()).isEqualTo(4);
	}

	/**
	 * Verifies that the default configuration enables alignment options.
	 */
	@Test
	public void defaultConfigurationEnablesAlignment()
	{
		IndentationConfiguration config = IndentationConfiguration.createDefault();

		assertThat(config.isAlignArrayElements()).isTrue();
		assertThat(config.isAlignParameters()).isTrue();
		assertThat(config.isPreserveCommentIndentation()).isFalse();
	}

	/**
	 * Verifies that builder can create configuration with tabs mode.
	 */
	@Test
	public void builderCanCreateTabsMode() throws ConfigurationException
	{
		IndentationConfiguration config = IndentationConfiguration.builder().
			withMode(IndentationMode.TABS).
			withIndentSize(4).
			withTabWidth(4).
			build();

		assertThat(config.getMode()).isEqualTo(IndentationMode.TABS);
	}

	/**
	 * Verifies that builder can create configuration with mixed mode.
	 */
	@Test
	public void builderCanCreateMixedMode() throws ConfigurationException
	{
		IndentationConfiguration config = IndentationConfiguration.builder().
			withMode(IndentationMode.MIXED).
			withIndentSize(4).
			withTabWidth(4).
			build();

		assertThat(config.getMode()).isEqualTo(IndentationMode.MIXED);
	}

	/**
	 * Verifies that builder can customize indentation size.
	 */
	@Test
	public void builderCanCustomizeIndentSize() throws ConfigurationException
	{
		IndentationConfiguration config = IndentationConfiguration.builder().
			withIndentSize(2).
			build();

		assertThat(config.getIndentSize()).isEqualTo(2);
	}

	/**
	 * Verifies that builder can customize continuation indent.
	 */
	@Test
	public void builderCanCustomizeContinuationIndent() throws ConfigurationException
	{
		IndentationConfiguration config = IndentationConfiguration.builder().
			withContinuationIndent(8).
			build();

		assertThat(config.getContinuationIndent()).isEqualTo(8);
	}

	/**
	 * Verifies that builder can customize tab width.
	 */
	@Test
	public void builderCanCustomizeTabWidth() throws ConfigurationException
	{
		IndentationConfiguration config = IndentationConfiguration.builder().
			withTabWidth(7).
			build();

		assertThat(config.getTabWidth()).isEqualTo(7);
	}

	/**
	 * Verifies that builder can disable array element alignment.
	 */
	@Test
	public void builderCanDisableArrayAlignment() throws ConfigurationException
	{
		IndentationConfiguration config = IndentationConfiguration.builder().
			withAlignArrayElements(false).
			build();

		assertThat(config.isAlignArrayElements()).isFalse();
	}

	/**
	 * Verifies that builder can disable parameter alignment.
	 */
	@Test
	public void builderCanDisableParameterAlignment() throws ConfigurationException
	{
		IndentationConfiguration config = IndentationConfiguration.builder().
			withAlignParameters(false).
			build();

		assertThat(config.isAlignParameters()).isFalse();
	}

	/**
	 * Verifies that builder can enable comment indentation preservation.
	 */
	@Test
	public void builderCanEnableCommentPreservation() throws ConfigurationException
	{
		IndentationConfiguration config = IndentationConfiguration.builder().
			withPreserveCommentIndentation(true).
			build();

		assertThat(config.isPreserveCommentIndentation()).isTrue();
	}

	/**
	 * Verifies that builder accepts maximum indent size.
	 */
	@Test
	public void builderAcceptsMaximumIndentSize() throws ConfigurationException
	{
		IndentationConfiguration config = IndentationConfiguration.builder().
			withIndentSize(16).
			build();

		assertThat(config.getIndentSize()).isEqualTo(16);
	}

	/**
	 * Verifies that builder accepts maximum tab width.
	 */
	@Test
	public void builderAcceptsMaximumTabWidth() throws ConfigurationException
	{
		IndentationConfiguration config = IndentationConfiguration.builder().
			withTabWidth(8).
			build();

		assertThat(config.getTabWidth()).isEqualTo(8);
	}

	/**
	 * Verifies that builder accepts maximum continuation indent.
	 */
	@Test
	public void builderAcceptsMaximumContinuationIndent() throws ConfigurationException
	{
		IndentationConfiguration config = IndentationConfiguration.builder().
			withContinuationIndent(16).
			build();

		assertThat(config.getContinuationIndent()).isEqualTo(16);
	}

	/**
	 * Verifies that validation rejects indent size below minimum.
	 */
	@Test(expectedExceptions = IllegalArgumentException.class)
	public void validationRejectsIndentSizeBelowMinimum()
		throws ConfigurationException
	{
		IndentationConfiguration.builder().
			withIndentSize(0).
			build();
	}

	/**
	 * Verifies that validation rejects indent size above maximum.
	 */
	@Test(expectedExceptions = IllegalArgumentException.class)
	public void validationRejectsIndentSizeAboveMaximum()
		throws ConfigurationException
	{
		IndentationConfiguration.builder().
			withIndentSize(17).
			build();
	}

	/**
	 * Verifies that validation rejects tab width below minimum.
	 */
	@Test(expectedExceptions = IllegalArgumentException.class)
	public void validationRejectsTabWidthBelowMinimum()
		throws ConfigurationException
	{
		IndentationConfiguration.builder().
			withTabWidth(0).
			build();
	}

	/**
	 * Verifies that validation rejects tab width above maximum.
	 */
	@Test(expectedExceptions = IllegalArgumentException.class)
	public void validationRejectsTabWidthAboveMaximum()
		throws ConfigurationException
	{
		IndentationConfiguration.builder().
			withTabWidth(9).
			build();
	}

	/**
	 * Verifies that two configurations with same values are equal.
	 */
	@Test
	public void equalConfigurationsAreEqual() throws ConfigurationException
	{
		IndentationConfiguration config1 = IndentationConfiguration.builder().
			withMode(IndentationMode.SPACES).
			withIndentSize(4).
			build();

		IndentationConfiguration config2 = IndentationConfiguration.builder().
			withMode(IndentationMode.SPACES).
			withIndentSize(4).
			build();

		assertThat(config1).isEqualTo(config2);
		assertThat(config1.hashCode()).isEqualTo(config2.hashCode());
	}

	/**
	 * Verifies that two configurations with different values are not equal.
	 */
	@Test
	public void differentConfigurationsAreNotEqual() throws ConfigurationException
	{
		IndentationConfiguration config1 = IndentationConfiguration.builder().
			withMode(IndentationMode.SPACES).
			build();

		IndentationConfiguration config2 = IndentationConfiguration.builder().
			withMode(IndentationMode.TABS).
			build();

		assertThat(config1).isNotEqualTo(config2);
	}

	/**
	 * Verifies that getDescription returns complete configuration information.
	 */
	@Test
	public void getDescriptionReturnsCompleteInformation()
	{
		IndentationConfiguration config = IndentationConfiguration.createDefault();
		String description = config.getDescription();

		assertThat(description).contains("SPACES");
		assertThat(description).contains("indentSize=4");
		assertThat(description).contains("continuationIndent=4");
		assertThat(description).contains("tabWidth=4");
		assertThat(description).contains("alignArrayElements=true");
		assertThat(description).contains("alignParameters=true");
	}

	/**
	 * Verifies that merge with identical configuration returns same instance.
	 */
	@Test
	public void mergeWithIdenticalConfigurationReturnsSameInstance() throws ConfigurationException
	{
		IndentationConfiguration config = IndentationConfiguration.createDefault();
		IndentationConfiguration merged = (IndentationConfiguration) config.merge(config);

		assertThat(merged).isSameAs(config);
	}

	/**
	 * Verifies that merge with different configuration returns override.
	 */
	@Test
	public void mergeWithDifferentConfigurationReturnsOverride() throws ConfigurationException
	{
		IndentationConfiguration base = IndentationConfiguration.builder().
			withMode(IndentationMode.SPACES).
			build();

		IndentationConfiguration override = IndentationConfiguration.builder().
			withMode(IndentationMode.TABS).
			build();

		IndentationConfiguration merged = (IndentationConfiguration) base.merge(override);

		assertThat(merged).isSameAs(override);
		assertThat(merged.getMode()).isEqualTo(IndentationMode.TABS);
	}
}
