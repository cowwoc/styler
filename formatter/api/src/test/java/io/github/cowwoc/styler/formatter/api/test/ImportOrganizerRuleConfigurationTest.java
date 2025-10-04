package io.github.cowwoc.styler.formatter.api.test;

import io.github.cowwoc.styler.formatter.api.ImportOrganizerRuleConfiguration;
import io.github.cowwoc.styler.formatter.api.ImportOrganizerRuleConfiguration.SortOrder;
import io.github.cowwoc.styler.formatter.api.ImportOrganizerRuleConfiguration.StaticImportsPosition;
import io.github.cowwoc.styler.formatter.api.RuleConfiguration;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Map;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;

/**
 * Unit tests for ImportOrganizerRuleConfiguration.
 */
public final class ImportOrganizerRuleConfigurationTest
{
	/**
	 * Verifies that default configuration is created with correct values.
	 */
	@Test
	public void defaultConfigurationHasExpectedValues()
	{
		ImportOrganizerRuleConfiguration config = new ImportOrganizerRuleConfiguration();

		requireThat(config.isRemoveUnused(), "removeUnused").isTrue();
		requireThat(config.isOrganizeGroups(), "organizeGroups").isTrue();
		requireThat(config.getGroups(), "groups").
			isEqualTo(List.of("static", "java", "third-party", "project"));
		requireThat(config.getCustomGroups(), "customGroups").isEqualTo(Map.of());
		requireThat(config.getSortOrder(), "sortOrder").isEqualTo(SortOrder.LEXICOGRAPHIC);
		requireThat(config.getBlankLinesBetweenGroups(), "blankLinesBetweenGroups").isEqualTo(1);
		requireThat(config.isPreserveManualSpacing(), "preserveManualSpacing").isFalse();
		requireThat(config.getStaticImportsPosition(), "staticImportsPosition").
			isEqualTo(StaticImportsPosition.TOP);
		requireThat(config.isExpandWildcards(), "expandWildcards").isFalse();
		requireThat(config.getWildcardThreshold(), "wildcardThreshold").isEqualTo(5);
		requireThat(config.isIncludeSamePackageImports(), "includeSamePackageImports").isFalse();
	}

	/**
	 * Verifies that custom configuration values are applied correctly.
	 */
	@Test
	public void customConfigurationValuesAreApplied()
	{
		ImportOrganizerRuleConfiguration config = new ImportOrganizerRuleConfiguration(
			false, false,
			List.of("java", "third-party"),
			Map.of("javax", List.of("javax\\..*")),
			SortOrder.LENGTH,
			2, true, StaticImportsPosition.BOTTOM, true, 10, true);

		requireThat(config.isRemoveUnused(), "removeUnused").isFalse();
		requireThat(config.isOrganizeGroups(), "organizeGroups").isFalse();
		requireThat(config.getGroups(), "groups").isEqualTo(List.of("java", "third-party"));
		requireThat(config.getCustomGroups().get("javax"), "customGroups['javax']").
			isEqualTo(List.of("javax\\..*"));
		requireThat(config.getSortOrder(), "sortOrder").isEqualTo(SortOrder.LENGTH);
		requireThat(config.getBlankLinesBetweenGroups(), "blankLinesBetweenGroups").isEqualTo(2);
		requireThat(config.isPreserveManualSpacing(), "preserveManualSpacing").isTrue();
		requireThat(config.getStaticImportsPosition(), "staticImportsPosition").
			isEqualTo(StaticImportsPosition.BOTTOM);
		requireThat(config.isExpandWildcards(), "expandWildcards").isTrue();
		requireThat(config.getWildcardThreshold(), "wildcardThreshold").isEqualTo(10);
		requireThat(config.isIncludeSamePackageImports(), "includeSamePackageImports").isTrue();
	}

	/**
	 * Verifies that blankLinesBetweenGroups validation rejects invalid values.
	 */
	@Test(expectedExceptions = IllegalArgumentException.class)
	public void blankLinesBetweenGroupsRejectsBelowMinimum()
	{
		new ImportOrganizerRuleConfiguration(
			null, null, null, null, null, -1, null, null, null, null, null);
	}

	/**
	 * Verifies that wildcardThreshold validation rejects invalid values.
	 */
	@Test(expectedExceptions = IllegalArgumentException.class)
	public void wildcardThresholdRejectsBelowMinimum()
	{
		new ImportOrganizerRuleConfiguration(
			null, null, null, null, null, null, null, null, null, 1, null);
	}

	/**
	 * Verifies that empty groups list is rejected.
	 */
	@Test(expectedExceptions = IllegalArgumentException.class)
	public void emptyGroupsListIsRejected()
	{
		new ImportOrganizerRuleConfiguration(
			null, null, List.of(), null, null, null, null, null, null, null, null);
	}

	/**
	 * Verifies that configuration merge works correctly.
	 */
	@Test
	public void configurationMergeWorks()
	{
		ImportOrganizerRuleConfiguration base = new ImportOrganizerRuleConfiguration(
			true, true, List.of("static", "java"), Map.of(), SortOrder.LEXICOGRAPHIC,
			1, false, StaticImportsPosition.TOP, false, 5, false);

		ImportOrganizerRuleConfiguration override = new ImportOrganizerRuleConfiguration(
			false, true, List.of("java", "third-party"),
			Map.of("test", List.of("test\\..*")),
			SortOrder.LENGTH, 2, true, StaticImportsPosition.TOP, false, 10, true);

		ImportOrganizerRuleConfiguration merged =
			(ImportOrganizerRuleConfiguration) base.merge(override);

		requireThat(merged.isRemoveUnused(), "removeUnused").isFalse();
		requireThat(merged.getGroups(), "groups").isEqualTo(List.of("java", "third-party"));
		requireThat(merged.getSortOrder(), "sortOrder").isEqualTo(SortOrder.LENGTH);
		requireThat(merged.getBlankLinesBetweenGroups(), "blankLinesBetweenGroups").isEqualTo(2);
		requireThat(merged.isPreserveManualSpacing(), "preserveManualSpacing").isTrue();
		requireThat(merged.getWildcardThreshold(), "wildcardThreshold").isEqualTo(10);
		requireThat(merged.isIncludeSamePackageImports(), "includeSamePackageImports").isTrue();
	}

	/**
	 * Verifies that configuration equals and hashCode work correctly.
	 */
	@Test
	public void equalsAndHashCodeWork()
	{
		ImportOrganizerRuleConfiguration config1 = new ImportOrganizerRuleConfiguration(
			true, true, List.of("static", "java"), Map.of("test", List.of("test\\..*")),
			SortOrder.LEXICOGRAPHIC, 1, false, StaticImportsPosition.TOP, false, 5, false);

		ImportOrganizerRuleConfiguration config2 = new ImportOrganizerRuleConfiguration(
			true, true, List.of("static", "java"), Map.of("test", List.of("test\\..*")),
			SortOrder.LEXICOGRAPHIC, 1, false, StaticImportsPosition.TOP, false, 5, false);

		ImportOrganizerRuleConfiguration config3 = new ImportOrganizerRuleConfiguration(
			false, true, List.of("static", "java"), Map.of("test", List.of("test\\..*")),
			SortOrder.LEXICOGRAPHIC, 1, false, StaticImportsPosition.TOP, false, 5, false);

		requireThat(config1, "config1").isEqualTo(config2);
		requireThat(config1.hashCode(), "config1.hashCode()").isEqualTo(config2.hashCode());
		requireThat(config1, "config1").isNotEqualTo(config3);
	}

	/**
	 * Verifies that configuration toString produces expected format.
	 */
	@Test
	public void toStringProducesExpectedFormat()
	{
		ImportOrganizerRuleConfiguration config = new ImportOrganizerRuleConfiguration();
		String result = config.toString();

		requireThat(result, "toString()").contains("ImportOrganizerRuleConfiguration{");
		requireThat(result, "toString()").contains("removeUnused=true");
		requireThat(result, "toString()").contains("organizeGroups=true");
	}

	/**
	 * Verifies that configuration description provides useful information.
	 */
	@Test
	public void descriptionProvidesUsefulInformation()
	{
		ImportOrganizerRuleConfiguration config = new ImportOrganizerRuleConfiguration();
		String description = config.getDescription();

		requireThat(description, "description").contains("Import Organizer Rule");
		requireThat(description, "description").contains("removeUnused=true");
		requireThat(description, "description").contains("organizeGroups=true");
		requireThat(description, "description").contains("sortOrder=LEXICOGRAPHIC");
	}

	/**
	 * Verifies that maximum groups count validation rejects excessive configurations.
	 */
	@Test(expectedExceptions = IllegalArgumentException.class)
	public void groupsListRejectsExceedingMaximum()
	{
		List<String> tooManyGroups = new java.util.ArrayList<>();
		for (int i = 0; i < 11; ++i)
		{
			tooManyGroups.add("group" + i);
		}
		new ImportOrganizerRuleConfiguration(
			null, null, tooManyGroups, null, null, null, null, null, null, null, null);
	}

	/**
	 * Verifies that maximum custom groups validation rejects excessive configurations.
	 */
	@Test(expectedExceptions = IllegalArgumentException.class)
	public void customGroupsRejectsExceedingMaximum()
	{
		Map<String, List<String>> tooManyCustomGroups = new java.util.HashMap<>();
		for (int i = 0; i < 21; ++i)
		{
			tooManyCustomGroups.put("group" + i, List.of("pattern.*"));
		}
		new ImportOrganizerRuleConfiguration(
			null, null, null, tooManyCustomGroups, null, null, null, null, null, null, null);
	}

	/**
	 * Verifies that invalid regex patterns are rejected during validation.
	 */
	@Test(expectedExceptions = IllegalArgumentException.class)
	public void customGroupsRejectsInvalidRegexPattern()
	{
		Map<String, List<String>> invalidPatternGroups = Map.of(
			"test", List.of("[unclosed bracket"));
		new ImportOrganizerRuleConfiguration(
			null, null, null, invalidPatternGroups, null, null, null, null, null, null, null);
	}

	/**
	 * Verifies that excessively long regex patterns are rejected.
	 */
	@Test(expectedExceptions = IllegalArgumentException.class)
	public void customGroupsRejectsExcessivelyLongPattern()
	{
		String tooLongPattern = "a".repeat(201);
		Map<String, List<String>> invalidPatternGroups = Map.of(
			"test", List.of(tooLongPattern));
		new ImportOrganizerRuleConfiguration(
			null, null, null, invalidPatternGroups, null, null, null, null, null, null, null);
	}

	/**
	 * Verifies that blankLinesBetweenGroups rejects values above maximum.
	 */
	@Test(expectedExceptions = IllegalArgumentException.class)
	public void blankLinesBetweenGroupsRejectsAboveMaximum()
	{
		new ImportOrganizerRuleConfiguration(
			null, null, null, null, null, 4, null, null, null, null, null);
	}

	/**
	 * Verifies that wildcardThreshold rejects values above maximum.
	 */
	@Test(expectedExceptions = IllegalArgumentException.class)
	public void wildcardThresholdRejectsAboveMaximum()
	{
		new ImportOrganizerRuleConfiguration(
			null, null, null, null, null, null, null, null, null, 101, null);
	}

	/**
	 * Verifies that SortOrder.NONE preserves original import order.
	 */
	@Test
	public void sortOrderNoneIsSupported()
	{
		ImportOrganizerRuleConfiguration config = new ImportOrganizerRuleConfiguration(
			null, null, null, null, SortOrder.NONE, null, null, null, null, null, null);
		requireThat(config.getSortOrder(), "sortOrder").isEqualTo(SortOrder.NONE);
	}

	/**
	 * Verifies that groups list is immutable and defensively copied.
	 */
	@Test
	public void groupsListIsDefensivelyCopied()
	{
		List<String> mutableGroups = new java.util.ArrayList<>(List.of("static", "java"));
		ImportOrganizerRuleConfiguration config = new ImportOrganizerRuleConfiguration(
			null, null, mutableGroups, null, null, null, null, null, null, null, null);

		mutableGroups.add("modified");

		requireThat(config.getGroups(), "groups").isEqualTo(List.of("static", "java"));
	}

	/**
	 * Verifies that merge rejects incompatible configuration types.
	 */
	@Test(expectedExceptions = IllegalArgumentException.class)
	public void mergeRejectsIncompatibleConfigurationType()
	{
		ImportOrganizerRuleConfiguration config = new ImportOrganizerRuleConfiguration();
		RuleConfiguration incompatible = new RuleConfiguration()
		{
			@Override
			public void validate()
			{
			}

			@Override
			public RuleConfiguration merge(RuleConfiguration override)
			{
				return this;
			}

			@Override
			public String getDescription()
			{
				return "incompatible";
			}

			@Override
			public boolean equals(Object obj)
			{
				return this == obj;
			}

			@Override
			public int hashCode()
			{
				return System.identityHashCode(this);
			}
		};
		config.merge(incompatible);
	}
}
