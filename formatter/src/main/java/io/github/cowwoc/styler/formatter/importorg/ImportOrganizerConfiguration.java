package io.github.cowwoc.styler.formatter.importorg;

import io.github.cowwoc.styler.formatter.FormattingConfiguration;

import java.util.List;
import java.util.Objects;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;

/**
 * Configuration for import organization and unused import removal.
 * <p>
 * Specifies how imports should be grouped, sorted, and filtered.
 * <p>
 * <b>Thread-safety</b>: This class is immutable and thread-safe.
 */
public final class ImportOrganizerConfiguration implements FormattingConfiguration
{
	private final String ruleId;
	private final List<ImportGroup> groupOrder;
	private final boolean separateStaticImports;
	private final boolean staticImportsFirst;
	private final boolean removeUnusedImports;
	private final boolean sortImportsAlphabetically;
	private final boolean expandWildcardImports;
	private final List<CustomImportPattern> customPatterns;

	/**
	 * Creates a configuration with comprehensive validation.
	 *
	 * @param ruleId                   unique identifier for the rule
	 * @param groupOrder               ordered list of import groups to respect during organization
	 * @param separateStaticImports    whether static imports should be in separate groups
	 * @param staticImportsFirst       whether static imports appear before regular imports
	 * @param removeUnusedImports      whether to remove imports not referenced in code
	 * @param sortImportsAlphabetically whether to sort imports alphabetically within groups
	 * @param expandWildcardImports    whether to expand wildcard imports to explicit imports for used classes
	 * @param customPatterns           custom regex patterns for import grouping
	 * @throws NullPointerException     if any required argument is null
	 * @throws IllegalArgumentException if parameters are invalid
	 */
	private ImportOrganizerConfiguration(
		String ruleId,
		List<ImportGroup> groupOrder,
		boolean separateStaticImports,
		boolean staticImportsFirst,
		boolean removeUnusedImports,
		boolean sortImportsAlphabetically,
		boolean expandWildcardImports,
		List<CustomImportPattern> customPatterns)
	{
		requireThat(ruleId, "ruleId").isNotBlank();
		requireThat(groupOrder, "groupOrder").isNotNull().isNotEmpty();
		requireThat(customPatterns, "customPatterns").isNotNull();
		for (CustomImportPattern customPattern : customPatterns)
		{
			requireThat(customPattern, "customPattern").isNotNull();
		}
		this.ruleId = ruleId;
		this.groupOrder = List.copyOf(groupOrder);
		this.separateStaticImports = separateStaticImports;
		this.staticImportsFirst = staticImportsFirst;
		this.removeUnusedImports = removeUnusedImports;
		this.sortImportsAlphabetically = sortImportsAlphabetically;
		this.expandWildcardImports = expandWildcardImports;
		this.customPatterns = List.copyOf(customPatterns);
	}

	/**
	 * Creates a default configuration with recommended settings.
	 *
	 * @return default configuration instance
	 */
	public static ImportOrganizerConfiguration defaultConfig()
	{
		return builder().build();
	}

	/**
	 * Creates a builder for fluent configuration.
	 *
	 * @return a new {@code Builder} instance
	 */
	public static Builder builder()
	{
		return new Builder();
	}

	/**
	 * Returns the unique identifier for the rule.
	 *
	 * @return the rule ID
	 */
	@Override
	public String ruleId()
	{
		return ruleId;
	}

	/**
	 * Returns the ordered list of import groups to respect during organization.
	 *
	 * @return the group order
	 */
	public List<ImportGroup> groupOrder()
	{
		return groupOrder;
	}

	/**
	 * Returns whether static imports should be in separate groups.
	 *
	 * @return {@code true} if static imports should be separated
	 */
	public boolean separateStaticImports()
	{
		return separateStaticImports;
	}

	/**
	 * Returns whether static imports appear before regular imports.
	 *
	 * @return {@code true} if static imports come first
	 */
	public boolean staticImportsFirst()
	{
		return staticImportsFirst;
	}

	/**
	 * Returns whether to remove imports not referenced in code.
	 *
	 * @return {@code true} if unused imports should be removed
	 */
	public boolean removeUnusedImports()
	{
		return removeUnusedImports;
	}

	/**
	 * Returns whether to sort imports alphabetically within groups.
	 *
	 * @return {@code true} if imports should be sorted alphabetically
	 */
	public boolean sortImportsAlphabetically()
	{
		return sortImportsAlphabetically;
	}

	/**
	 * Returns whether to expand wildcard imports to explicit imports for used classes.
	 *
	 * @return {@code true} if wildcard imports should be expanded
	 */
	public boolean expandWildcardImports()
	{
		return expandWildcardImports;
	}

	/**
	 * Returns the custom regex patterns for import grouping.
	 *
	 * @return the custom patterns
	 */
	public List<CustomImportPattern> customPatterns()
	{
		return customPatterns;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (!(obj instanceof ImportOrganizerConfiguration other))
			return false;
		return separateStaticImports == other.separateStaticImports &&
			staticImportsFirst == other.staticImportsFirst &&
			removeUnusedImports == other.removeUnusedImports &&
			sortImportsAlphabetically == other.sortImportsAlphabetically &&
			expandWildcardImports == other.expandWildcardImports &&
			Objects.equals(ruleId, other.ruleId) &&
			Objects.equals(groupOrder, other.groupOrder) &&
			Objects.equals(customPatterns, other.customPatterns);
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(ruleId, groupOrder, separateStaticImports, staticImportsFirst,
			removeUnusedImports, sortImportsAlphabetically, expandWildcardImports, customPatterns);
	}

	@Override
	public String toString()
	{
		return "ImportOrganizerConfiguration[" +
			"ruleId=" + ruleId +
			", groupOrder=" + groupOrder +
			", separateStaticImports=" + separateStaticImports +
			", staticImportsFirst=" + staticImportsFirst +
			", removeUnusedImports=" + removeUnusedImports +
			", sortImportsAlphabetically=" + sortImportsAlphabetically +
			", expandWildcardImports=" + expandWildcardImports +
			", customPatterns=" + customPatterns +
			']';
	}

	/**
	 * Builder for creating {@code ImportOrganizerConfiguration} instances.
	 * <p>
	 * This is the preferred way to create configuration instances, as it provides clear naming for each
	 * parameter and sensible defaults. All fields are initialized to default values matching
	 * {@link #defaultConfig()}.
	 */
	public static final class Builder
	{
		private String ruleId = "import-organizer";
		private List<ImportGroup> groupOrder = List.of(
			ImportGroup.JAVA,
			ImportGroup.JAVAX,
			ImportGroup.THIRD_PARTY,
			ImportGroup.PROJECT);
		private boolean separateStaticImports = true;
		private boolean staticImportsFirst;
		private boolean removeUnusedImports = true;
		private boolean sortImportsAlphabetically = true;
		private boolean expandWildcardImports = true;
		private List<CustomImportPattern> customPatterns = List.of();

		/**
		 * Sets the unique identifier for this rule.
		 *
		 * @param ruleId the rule ID, must not be blank
		 * @return this builder for method chaining
		 * @throws NullPointerException     if {@code ruleId} is null
		 * @throws IllegalArgumentException if {@code ruleId} is blank
		 */
		public Builder ruleId(String ruleId)
		{
			requireThat(ruleId, "ruleId").isNotBlank();
			this.ruleId = ruleId;
			return this;
		}

		/**
		 * Sets the ordered list of import groups to respect during organization.
		 *
		 * @param groupOrder the import group ordering, must not be null or empty
		 * @return this builder for method chaining
		 * @throws NullPointerException     if {@code groupOrder} is null
		 * @throws IllegalArgumentException if {@code groupOrder} is empty
		 */
		public Builder groupOrder(List<ImportGroup> groupOrder)
		{
			requireThat(groupOrder, "groupOrder").isNotNull().isNotEmpty();
			this.groupOrder = List.copyOf(groupOrder);
			return this;
		}

		/**
		 * Sets whether static imports should be in separate groups.
		 *
		 * @param separateStaticImports {@code true} to separate static imports
		 * @return this builder for method chaining
		 */
		public Builder separateStaticImports(boolean separateStaticImports)
		{
			this.separateStaticImports = separateStaticImports;
			return this;
		}

		/**
		 * Sets whether static imports appear before regular imports.
		 *
		 * @param staticImportsFirst {@code true} to place static imports first
		 * @return this builder for method chaining
		 */
		public Builder staticImportsFirst(boolean staticImportsFirst)
		{
			this.staticImportsFirst = staticImportsFirst;
			return this;
		}

		/**
		 * Sets whether to remove imports not referenced in code.
		 *
		 * @param removeUnusedImports {@code true} to remove unused imports
		 * @return this builder for method chaining
		 */
		public Builder removeUnusedImports(boolean removeUnusedImports)
		{
			this.removeUnusedImports = removeUnusedImports;
			return this;
		}

		/**
		 * Sets whether to sort imports alphabetically within groups.
		 *
		 * @param sortImportsAlphabetically {@code true} to sort alphabetically
		 * @return this builder for method chaining
		 */
		public Builder sortImportsAlphabetically(boolean sortImportsAlphabetically)
		{
			this.sortImportsAlphabetically = sortImportsAlphabetically;
			return this;
		}

		/**
		 * Sets whether to expand wildcard imports to explicit imports for used classes.
		 *
		 * @param expandWildcardImports {@code true} to expand wildcard imports
		 * @return this builder for method chaining
		 */
		public Builder expandWildcardImports(boolean expandWildcardImports)
		{
			this.expandWildcardImports = expandWildcardImports;
			return this;
		}

		/**
		 * Sets the custom regex patterns for import grouping.
		 *
		 * @param customPatterns the custom patterns, must not be null
		 * @return this builder for method chaining
		 * @throws NullPointerException if {@code customPatterns} is null
		 */
		public Builder customPatterns(List<CustomImportPattern> customPatterns)
		{
			requireThat(customPatterns, "customPatterns").isNotNull();
			this.customPatterns = List.copyOf(customPatterns);
			return this;
		}

		/**
		 * Builds the {@code ImportOrganizerConfiguration}.
		 *
		 * @return a new immutable configuration instance
		 */
		public ImportOrganizerConfiguration build()
		{
			return new ImportOrganizerConfiguration(ruleId, groupOrder, separateStaticImports,
				staticImportsFirst, removeUnusedImports, sortImportsAlphabetically,
				expandWildcardImports, customPatterns);
		}
	}
}
