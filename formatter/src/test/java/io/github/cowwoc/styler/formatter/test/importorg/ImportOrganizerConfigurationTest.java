package io.github.cowwoc.styler.formatter.test.importorg;

import io.github.cowwoc.styler.formatter.importorg.CustomImportPattern;
import io.github.cowwoc.styler.formatter.importorg.ImportGroup;
import io.github.cowwoc.styler.formatter.importorg.ImportOrganizerConfiguration;
import org.testng.annotations.Test;

import java.util.List;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;

/**
 * Tests for ImportOrganizerConfiguration validation and defaults.
 */
public class ImportOrganizerConfigurationTest
{
	/**
	 * Tests that null ruleId is rejected.
	 */
	@Test(expectedExceptions = NullPointerException.class)
	void shouldRejectNullRuleId()
	{
		ImportOrganizerConfiguration.builder().
			ruleId(null).
			build();
	}

	/**
	 * Tests that empty ruleId is rejected.
	 */
	@Test(expectedExceptions = IllegalArgumentException.class)
	void shouldRejectEmptyRuleId()
	{
		ImportOrganizerConfiguration.builder().
			ruleId("").
			build();
	}

	/**
	 * Tests that whitespace-only ruleId is rejected.
	 */
	@Test(expectedExceptions = IllegalArgumentException.class)
	void shouldRejectWhitespaceOnlyRuleId()
	{
		ImportOrganizerConfiguration.builder().
			ruleId("   ").
			build();
	}

	/**
	 * Tests that null groupOrder is rejected.
	 */
	@Test(expectedExceptions = NullPointerException.class)
	void shouldRejectNullGroupOrder()
	{
		ImportOrganizerConfiguration.builder().
			groupOrder(null).
			build();
	}

	/**
	 * Tests that empty groupOrder is rejected.
	 */
	@Test(expectedExceptions = IllegalArgumentException.class)
	void shouldRejectEmptyGroupOrder()
	{
		ImportOrganizerConfiguration.builder().
			groupOrder(List.of()).
			build();
	}

	/**
	 * Tests that ruleId is returned correctly.
	 */
	@Test
	void shouldReturnCorrectRuleId()
	{
		ImportOrganizerConfiguration config = ImportOrganizerConfiguration.builder().
			ruleId("custom-rule").
			build();

		requireThat(config.ruleId(), "ruleId").isEqualTo("custom-rule");
	}

	/**
	 * Tests that default configuration returns expected group order.
	 */
	@Test
	void shouldReturnDefaultGroupOrder()
	{
		ImportOrganizerConfiguration config = ImportOrganizerConfiguration.defaultConfig();

		List<ImportGroup> expected = List.of(ImportGroup.JAVA, ImportGroup.JAVAX, ImportGroup.THIRD_PARTY,
			ImportGroup.PROJECT);
		requireThat(config.groupOrder(), "groupOrder").isEqualTo(expected);
	}

	/**
	 * Tests that default configuration has removeUnusedImports enabled.
	 */
	@Test
	void shouldReturnRemoveUnusedDefault()
	{
		ImportOrganizerConfiguration config = ImportOrganizerConfiguration.defaultConfig();

		requireThat(config.removeUnusedImports(), "removeUnusedImports").isTrue();
	}

	/**
	 * Tests that default configuration has sortImportsAlphabetically enabled.
	 */
	@Test
	void shouldReturnSortAlphabeticallyDefault()
	{
		ImportOrganizerConfiguration config = ImportOrganizerConfiguration.defaultConfig();

		requireThat(config.sortImportsAlphabetically(), "sortImportsAlphabetically").isTrue();
	}

	/**
	 * Tests that custom project pattern is accepted.
	 */
	@Test
	void shouldAcceptCustomProjectPattern()
	{
		CustomImportPattern customPattern = CustomImportPattern.of("PROJECT", "com\\.mycompany\\..*");
		ImportOrganizerConfiguration config = ImportOrganizerConfiguration.builder().
			customPatterns(List.of(customPattern)).
			build();

		requireThat(config.customPatterns(), "customPatterns").size().isEqualTo(1);
		requireThat(config.customPatterns().getFirst().pattern().pattern(), "pattern").
			isEqualTo("com\\.mycompany\\..*");
	}
}
