package io.github.cowwoc.styler.config.test;

import io.github.cowwoc.styler.config.Config;
import io.github.cowwoc.styler.config.ConfigBuilder;
import io.github.cowwoc.styler.config.ConfigMerger;
import io.github.cowwoc.styler.config.exception.ConfigurationException;
import org.testng.annotations.Test;

import java.util.List;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;

/**
 * Tests for {@link ConfigMerger} focusing on field-level precedence logic.
 * <p>
 * Priority 1: Business logic tests validate the core value proposition - field-level merging
 * where nearest config wins for each field independently.
 */
public final class ConfigMergerTest
{
	private final ConfigMerger merger = new ConfigMerger();

	/**
	 * RISK: Wrong precedence → project config doesn't override user config
	 * IMPACT: High - violates user's mental model of "nearest wins"
	 */
	@Test
	public void mergeBuilders_projectOverridesUser_nearestWins() throws ConfigurationException
	{
		// User config: maxLineLength = 100
		ConfigBuilder userConfig = new ConfigBuilder().maxLineLength(100);

		// Project config: maxLineLength = 120
		ConfigBuilder projectConfig = new ConfigBuilder().maxLineLength(120);

		// Project is nearest (first in list), so it should win
		Config merged = merger.merge(List.of(projectConfig, userConfig));

		requireThat(merged.maxLineLength(), "maxLineLength").isEqualTo(120);
	}

	/**
	 * RISK: Field-level merging doesn't work → wholesale replacement instead
	 * IMPACT: Critical - defeats the entire purpose of field-level precedence
	 */
	@Test
	public void mergeBuilders_fieldLevelPrecedence_eachFieldIndependent() throws ConfigurationException
	{
		// User config sets maxLineLength
		ConfigBuilder userConfig = new ConfigBuilder().maxLineLength(100);

		// Project config doesn't set maxLineLength (uses default)
		ConfigBuilder projectConfig = new ConfigBuilder();

		// Merged should use user's maxLineLength since project doesn't override it
		Config merged = merger.merge(List.of(projectConfig, userConfig));

		requireThat(merged.maxLineLength(), "maxLineLength").isEqualTo(100);
	}

	/**
	 * RISK: Empty builder list causes exception instead of returning defaults
	 * IMPACT: Medium - breaks graceful degradation when no config files exist
	 */
	@Test
	public void mergeBuilders_emptyList_returnsDefaults() throws ConfigurationException
	{
		Config merged = merger.merge(List.of());

		requireThat(merged.maxLineLength(), "maxLineLength").
			isEqualTo(Config.DEFAULT_MAX_LINE_LENGTH);
	}

	/**
	 * RISK: Single config doesn't work → assumes multiple configs required
	 * IMPACT: Medium - breaks when only one config file exists
	 */
	@Test
	public void mergeBuilders_singleConfig_returnsUnmodified() throws ConfigurationException
	{
		ConfigBuilder singleConfig = new ConfigBuilder().maxLineLength(150);

		Config merged = merger.merge(List.of(singleConfig));

		requireThat(merged.maxLineLength(), "maxLineLength").isEqualTo(150);
	}

	/**
	 * RISK: All configs use defaults → incorrectly uses first config's defaults
	 * IMPACT: Low - edge case but should use system defaults
	 */
	@Test
	public void mergeBuilders_allDefault_usesSystemDefaults() throws ConfigurationException
	{
		ConfigBuilder config1 = new ConfigBuilder();
		ConfigBuilder config2 = new ConfigBuilder();
		ConfigBuilder config3 = new ConfigBuilder();

		Config merged = merger.merge(List.of(config1, config2, config3));

		requireThat(merged.maxLineLength(), "maxLineLength").
			isEqualTo(Config.DEFAULT_MAX_LINE_LENGTH);
	}

	/**
	 * RISK: Precedence order reversed → farthest config wins instead of nearest
	 * IMPACT: Critical - completely inverts the expected behavior
	 */
	@Test
	public void mergeBuilders_threeConfigs_nearestWinsForEachField() throws ConfigurationException
	{
		// System config (farthest): maxLineLength = 80
		ConfigBuilder systemConfig = new ConfigBuilder().maxLineLength(80);

		// User config (middle): maxLineLength = 100
		ConfigBuilder userConfig = new ConfigBuilder().maxLineLength(100);

		// Project config (nearest): maxLineLength = 120
		ConfigBuilder projectConfig = new ConfigBuilder().maxLineLength(120);

		// Nearest (project) should win
		Config merged = merger.merge(List.of(projectConfig, userConfig, systemConfig));

		requireThat(merged.maxLineLength(), "maxLineLength").isEqualTo(120);
	}

	/**
	 * RISK: Precedence skips middle config → only checks first and last
	 * IMPACT: High - middle configs (like user config) get ignored
	 */
	@Test
	public void mergeBuilders_middleConfigWins_whenNearestNotSet() throws ConfigurationException
	{
		// System config (farthest): maxLineLength = 80
		ConfigBuilder systemConfig = new ConfigBuilder().maxLineLength(80);

		// User config (middle): maxLineLength = 100
		ConfigBuilder userConfig = new ConfigBuilder().maxLineLength(100);

		// Project config (nearest): doesn't set maxLineLength
		ConfigBuilder projectConfig = new ConfigBuilder();

		// User (middle) should win since project doesn't override
		Config merged = merger.merge(List.of(projectConfig, userConfig, systemConfig));

		requireThat(merged.maxLineLength(), "maxLineLength").isEqualTo(100);
	}
}
