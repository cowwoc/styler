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
	@Test(expectedExceptions = NullPointerException.class)
	void shouldRejectNullRuleId()
	{
		List<ImportGroup> groupOrder = List.of(ImportGroup.JAVA, ImportGroup.THIRD_PARTY,
			ImportGroup.PROJECT);
		new ImportOrganizerConfiguration(null, groupOrder, true, false, true, true, List.of());
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	void shouldRejectEmptyRuleId()
	{
		List<ImportGroup> groupOrder = List.of(ImportGroup.JAVA, ImportGroup.THIRD_PARTY,
			ImportGroup.PROJECT);
		new ImportOrganizerConfiguration("", groupOrder, true, false, true, true, List.of());
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	void shouldRejectWhitespaceOnlyRuleId()
	{
		List<ImportGroup> groupOrder = List.of(ImportGroup.JAVA, ImportGroup.THIRD_PARTY,
			ImportGroup.PROJECT);
		new ImportOrganizerConfiguration("   ", groupOrder, true, false, true, true, List.of());
	}

	@Test(expectedExceptions = NullPointerException.class)
	void shouldRejectNullGroupOrder()
	{
		new ImportOrganizerConfiguration("import-organizer", null, true, false, true, true, List.of());
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	void shouldRejectEmptyGroupOrder()
	{
		new ImportOrganizerConfiguration("import-organizer", List.of(), true, false, true, true,
			List.of());
	}

	@Test
	void shouldReturnCorrectRuleId()
	{
		List<ImportGroup> groupOrder = List.of(ImportGroup.JAVA, ImportGroup.THIRD_PARTY,
			ImportGroup.PROJECT);
		ImportOrganizerConfiguration config = new ImportOrganizerConfiguration("import-organizer",
			groupOrder, true, false, true, true, List.of());

		requireThat(config.ruleId(), "ruleId").isEqualTo("import-organizer");
	}

	@Test
	void shouldReturnDefaultGroupOrder()
	{
		ImportOrganizerConfiguration config = ImportOrganizerConfiguration.defaultConfig();

		List<ImportGroup> expected = List.of(ImportGroup.JAVA, ImportGroup.JAVAX, ImportGroup.THIRD_PARTY,
			ImportGroup.PROJECT);
		requireThat(config.groupOrder(), "groupOrder").isEqualTo(expected);
	}

	@Test
	void shouldReturnRemoveUnusedDefault()
	{
		ImportOrganizerConfiguration config = ImportOrganizerConfiguration.defaultConfig();

		requireThat(config.removeUnusedImports(), "removeUnusedImports").isTrue();
	}

	@Test
	void shouldReturnSortAlphabeticallyDefault()
	{
		ImportOrganizerConfiguration config = ImportOrganizerConfiguration.defaultConfig();

		requireThat(config.sortImportsAlphabetically(), "sortImportsAlphabetically").isTrue();
	}

	@Test
	void shouldAcceptCustomProjectPattern()
	{
		List<ImportGroup> groupOrder = List.of(ImportGroup.JAVA, ImportGroup.THIRD_PARTY,
			ImportGroup.PROJECT);
		CustomImportPattern customPattern = CustomImportPattern.of("PROJECT", "com\\.mycompany\\..*");
		ImportOrganizerConfiguration config = new ImportOrganizerConfiguration("import-organizer",
			groupOrder, true, false, true, true, List.of(customPattern));

		requireThat(config.customPatterns(), "customPatterns").size().isEqualTo(1);
		requireThat(config.customPatterns().getFirst().pattern().pattern(), "pattern").
			isEqualTo("com\\.mycompany\\..*");
	}
}
