package io.github.cowwoc.styler.formatter.importorg;

import io.github.cowwoc.styler.formatter.FormattingConfiguration;

import java.util.List;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;

/**
 * Configuration for import organization and unused import removal.
 * <p>
 * Specifies how imports should be grouped, sorted, and filtered.
 * <p>
 * <b>Thread-safety</b>: This record is immutable and thread-safe.
 *
 * @param ruleId unique identifier for the rule
 * @param groupOrder ordered list of import groups to respect during organization
 * @param separateStaticImports whether static imports should be in separate groups
 * @param staticImportsFirst whether static imports appear before regular imports
 * @param removeUnusedImports whether to remove imports not referenced in code
 * @param sortImportsAlphabetically whether to sort imports alphabetically within groups
 * @param customPatterns custom regex patterns for import grouping
 * @throws NullPointerException if any required parameter is null
 * @throws IllegalArgumentException if parameters are invalid
 */
public record ImportOrganizerConfiguration(
	String ruleId,
	List<ImportGroup> groupOrder,
	boolean separateStaticImports,
	boolean staticImportsFirst,
	boolean removeUnusedImports,
	boolean sortImportsAlphabetically,
	List<CustomImportPattern> customPatterns) implements FormattingConfiguration
{
	/**
	 * Creates a configuration record with comprehensive validation.
	 */
	public ImportOrganizerConfiguration
	{
		requireThat(ruleId, "ruleId").isNotBlank();
		requireThat(groupOrder, "groupOrder").isNotNull().isNotEmpty();
		groupOrder = List.copyOf(groupOrder);
		requireThat(customPatterns, "customPatterns").isNotNull();
		customPatterns = List.copyOf(customPatterns);
		for (CustomImportPattern customPattern : customPatterns)
		{
			requireThat(customPattern, "customPattern").isNotNull();
		}
	}

	/**
	 * Creates a default configuration with recommended settings.
	 *
	 * @return default configuration instance
	 */
	public static ImportOrganizerConfiguration defaultConfig()
	{
		return new ImportOrganizerConfiguration(
			"import-organizer",
			List.of(
				ImportGroup.JAVA,
				ImportGroup.JAVAX,
				ImportGroup.THIRD_PARTY,
				ImportGroup.PROJECT),
			true,   // separateStaticImports
			false,  // staticImportsFirst (static imports at end)
			true,   // removeUnusedImports
			true,   // sortImportsAlphabetically
			List.of());
	}
}
