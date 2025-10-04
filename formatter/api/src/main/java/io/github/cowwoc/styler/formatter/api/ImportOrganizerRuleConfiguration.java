package io.github.cowwoc.styler.formatter.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Configuration for import organization formatting rules.
 * <p>
 * This rule configuration controls how import statements are organized,
 * including grouping, sorting, and removal of unused imports. Supports
 * configurable group definitions, custom patterns, and various import
 * handling strategies.
 * <p>
 * <b>Thread Safety:</b> This class is immutable and thread-safe.
 * <b>Security:</b> All configuration values are validated for security compliance.
 *
 * @since {@code 1}.{@code 0}.{@code 0}
 * @author Plugin Framework Team
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
	"removeUnused", "organizeGroups", "groups", "customGroups", "sortOrder",
	"blankLinesBetweenGroups", "preserveManualSpacing", "staticImportsPosition",
	"expandWildcards", "wildcardThreshold", "includeSamePackageImports"
})
public final class ImportOrganizerRuleConfiguration extends RuleConfiguration
{
	// Validation constants
	private static final int MAX_GROUP_DEFINITIONS = 10;
	private static final int MAX_CUSTOM_GROUPS = 20;
	private static final int MAX_PATTERN_LENGTH = 200;
	private static final int MIN_WILDCARD_THRESHOLD = 2;
	private static final int MAX_WILDCARD_THRESHOLD = 100;

	// Default configuration values
	private static final boolean DEFAULT_REMOVE_UNUSED = true;
	private static final boolean DEFAULT_ORGANIZE_GROUPS = true;
	private static final List<String> DEFAULT_GROUPS =
		List.of("static", "java", "third-party", "project");
	private static final Map<String, List<String>> DEFAULT_CUSTOM_GROUPS = Collections.emptyMap();
	private static final SortOrder DEFAULT_SORT_ORDER = SortOrder.LEXICOGRAPHIC;
	private static final int DEFAULT_BLANK_LINES_BETWEEN_GROUPS = 1;
	private static final boolean DEFAULT_PRESERVE_MANUAL_SPACING = false;
	private static final StaticImportsPosition DEFAULT_STATIC_IMPORTS_POSITION =
		StaticImportsPosition.TOP;
	private static final boolean DEFAULT_EXPAND_WILDCARDS = false;
	private static final int DEFAULT_WILDCARD_THRESHOLD = 5;
	private static final boolean DEFAULT_INCLUDE_SAME_PACKAGE_IMPORTS = false;

	@JsonProperty("removeUnused")
	private final boolean removeUnused;

	@JsonProperty("organizeGroups")
	private final boolean organizeGroups;

	@JsonProperty("groups")
	private final List<String> groups;

	@JsonProperty("customGroups")
	private final Map<String, List<String>> customGroups;

	@JsonProperty("sortOrder")
	private final SortOrder sortOrder;

	@JsonProperty("blankLinesBetweenGroups")
	private final int blankLinesBetweenGroups;

	@JsonProperty("preserveManualSpacing")
	private final boolean preserveManualSpacing;

	@JsonProperty("staticImportsPosition")
	private final StaticImportsPosition staticImportsPosition;

	@JsonProperty("expandWildcards")
	private final boolean expandWildcards;

	@JsonProperty("wildcardThreshold")
	private final int wildcardThreshold;

	@JsonProperty("includeSamePackageImports")
	private final boolean includeSamePackageImports;

	/**
	 * Creates a new import organizer rule configuration.
	 *
	 * @param removeUnused              whether to remove unused imports
	 * @param organizeGroups            whether to organize imports into groups
	 * @param groups                    ordered list of group names
	 * @param customGroups              custom group patterns (group name → regex patterns)
	 * @param sortOrder                 sorting strategy within groups
	 * @param blankLinesBetweenGroups   number of blank lines between groups (0-3)
	 * @param preserveManualSpacing     whether to preserve manual blank lines within groups
	 * @param staticImportsPosition     position for static imports
	 * @param expandWildcards           whether to expand wildcard imports
	 * @param wildcardThreshold         minimum imports from package before collapsing to wildcard
	 * @param includeSamePackageImports whether to include imports from same package
	 */
	@JsonCreator
	public ImportOrganizerRuleConfiguration(
		@JsonProperty("removeUnused") Boolean removeUnused,
		@JsonProperty("organizeGroups") Boolean organizeGroups,
		@JsonProperty("groups") List<String> groups,
		@JsonProperty("customGroups") Map<String, List<String>> customGroups,
		@JsonProperty("sortOrder") SortOrder sortOrder,
		@JsonProperty("blankLinesBetweenGroups") Integer blankLinesBetweenGroups,
		@JsonProperty("preserveManualSpacing") Boolean preserveManualSpacing,
		@JsonProperty("staticImportsPosition") StaticImportsPosition staticImportsPosition,
		@JsonProperty("expandWildcards") Boolean expandWildcards,
		@JsonProperty("wildcardThreshold") Integer wildcardThreshold,
		@JsonProperty("includeSamePackageImports") Boolean includeSamePackageImports)
	{
		this.removeUnused = Objects.requireNonNullElse(removeUnused, DEFAULT_REMOVE_UNUSED);
		this.organizeGroups = Objects.requireNonNullElse(organizeGroups, DEFAULT_ORGANIZE_GROUPS);

		if (groups != null)
		{
			this.groups = List.copyOf(groups);
		}
		else
		{
			this.groups = DEFAULT_GROUPS;
		}

		if (customGroups != null)
		{
			Map<String, List<String>> copiedGroups = new HashMap<>();
			for (Map.Entry<String, List<String>> entry : customGroups.entrySet())
			{
				copiedGroups.put(entry.getKey(), List.copyOf(entry.getValue()));
			}
			this.customGroups = Collections.unmodifiableMap(copiedGroups);
		}
		else
		{
			this.customGroups = DEFAULT_CUSTOM_GROUPS;
		}

		this.sortOrder = Objects.requireNonNullElse(sortOrder, DEFAULT_SORT_ORDER);
		this.blankLinesBetweenGroups =
			Objects.requireNonNullElse(blankLinesBetweenGroups, DEFAULT_BLANK_LINES_BETWEEN_GROUPS);
		this.preserveManualSpacing =
			Objects.requireNonNullElse(preserveManualSpacing, DEFAULT_PRESERVE_MANUAL_SPACING);
		this.staticImportsPosition =
			Objects.requireNonNullElse(staticImportsPosition, DEFAULT_STATIC_IMPORTS_POSITION);
		this.expandWildcards = Objects.requireNonNullElse(expandWildcards, DEFAULT_EXPAND_WILDCARDS);
		this.wildcardThreshold = Objects.requireNonNullElse(wildcardThreshold, DEFAULT_WILDCARD_THRESHOLD);
		this.includeSamePackageImports =
			Objects.requireNonNullElse(includeSamePackageImports, DEFAULT_INCLUDE_SAME_PACKAGE_IMPORTS);

		try
		{
			validate();
		}
		catch (ConfigurationException e)
		{
			throw new IllegalArgumentException("Invalid import organizer configuration", e);
		}
	}

	/**
	 * Creates a default import organizer configuration.
	 */
	public ImportOrganizerRuleConfiguration()
	{
		this(null, null, null, null, null, null, null, null, null, null, null);
	}

	/**
	 * Validates this configuration.
	 *
	 * @throws ConfigurationException if the configuration is invalid
	 */
	@Override
	public void validate() throws ConfigurationException
	{
		validateNumericRange("blankLinesBetweenGroups", blankLinesBetweenGroups, 0, 3);
		validateNumericRange("wildcardThreshold", wildcardThreshold, MIN_WILDCARD_THRESHOLD,
			MAX_WILDCARD_THRESHOLD);

		// Validate groups list
		if (groups.isEmpty())
		{
			throw new ConfigurationException("Groups list cannot be empty");
		}

		// Validate maximum number of groups
		if (groups.size() > MAX_GROUP_DEFINITIONS)
		{
			throw new ConfigurationException("Maximum " + MAX_GROUP_DEFINITIONS +
				" group definitions allowed, found: " + groups.size());
		}

		// Validate custom groups
		if (customGroups.size() > MAX_CUSTOM_GROUPS)
		{
			throw new ConfigurationException("Maximum " + MAX_CUSTOM_GROUPS +
				" custom groups allowed, found: " + customGroups.size());
		}

		// Validate custom group patterns
		for (Map.Entry<String, List<String>> entry : customGroups.entrySet())
		{
			String groupName = entry.getKey();
			List<String> patterns = entry.getValue();

			validateParameter("customGroups key", groupName, String.class);

			for (String pattern : patterns)
			{
				validateParameter("customGroups pattern", pattern, String.class);

				if (pattern.length() > MAX_PATTERN_LENGTH)
				{
					throw new ConfigurationException(
						"Custom group pattern exceeds maximum length (" + MAX_PATTERN_LENGTH + "): " +
							pattern);
				}

				// Validate regex pattern compiles and check for ReDoS patterns
				try
				{
					java.util.regex.Pattern.compile(pattern);
				}
				catch (java.util.regex.PatternSyntaxException e)
				{
					throw new ConfigurationException("Invalid regex pattern in customGroups: " +
						pattern, e);
				}
			}
		}
	}

	@Override
	public RuleConfiguration merge(RuleConfiguration override)
	{
		if (!(override instanceof ImportOrganizerRuleConfiguration other))
		{
			throw new IllegalArgumentException("Cannot merge ImportOrganizerRuleConfiguration with " +
				override.getClass().getSimpleName());
		}

		// Merge logic: use override's value if it differs from default, otherwise use base's value
		boolean mergedRemoveUnused;
		if (other.removeUnused == DEFAULT_REMOVE_UNUSED)
		{
			mergedRemoveUnused = this.removeUnused;
		}
		else
		{
			mergedRemoveUnused = other.removeUnused;
		}

		boolean mergedOrganizeGroups;
		if (other.organizeGroups == DEFAULT_ORGANIZE_GROUPS)
		{
			mergedOrganizeGroups = this.organizeGroups;
		}
		else
		{
			mergedOrganizeGroups = other.organizeGroups;
		}

		List<String> mergedGroups;
		if (other.groups.equals(DEFAULT_GROUPS))
		{
			mergedGroups = this.groups;
		}
		else
		{
			mergedGroups = other.groups;
		}

		Map<String, List<String>> mergedCustomGroups;
		if (other.customGroups.equals(DEFAULT_CUSTOM_GROUPS))
		{
			mergedCustomGroups = this.customGroups;
		}
		else
		{
			mergedCustomGroups = other.customGroups;
		}

		SortOrder mergedSortOrder;
		if (other.sortOrder == DEFAULT_SORT_ORDER)
		{
			mergedSortOrder = this.sortOrder;
		}
		else
		{
			mergedSortOrder = other.sortOrder;
		}

		int mergedBlankLinesBetweenGroups;
		if (other.blankLinesBetweenGroups == DEFAULT_BLANK_LINES_BETWEEN_GROUPS)
		{
			mergedBlankLinesBetweenGroups = this.blankLinesBetweenGroups;
		}
		else
		{
			mergedBlankLinesBetweenGroups = other.blankLinesBetweenGroups;
		}

		boolean mergedPreserveManualSpacing;
		if (other.preserveManualSpacing == DEFAULT_PRESERVE_MANUAL_SPACING)
		{
			mergedPreserveManualSpacing = this.preserveManualSpacing;
		}
		else
		{
			mergedPreserveManualSpacing = other.preserveManualSpacing;
		}

		StaticImportsPosition mergedStaticImportsPosition;
		if (other.staticImportsPosition == DEFAULT_STATIC_IMPORTS_POSITION)
		{
			mergedStaticImportsPosition = this.staticImportsPosition;
		}
		else
		{
			mergedStaticImportsPosition = other.staticImportsPosition;
		}

		boolean mergedExpandWildcards;
		if (other.expandWildcards == DEFAULT_EXPAND_WILDCARDS)
		{
			mergedExpandWildcards = this.expandWildcards;
		}
		else
		{
			mergedExpandWildcards = other.expandWildcards;
		}

		int mergedWildcardThreshold;
		if (other.wildcardThreshold == DEFAULT_WILDCARD_THRESHOLD)
		{
			mergedWildcardThreshold = this.wildcardThreshold;
		}
		else
		{
			mergedWildcardThreshold = other.wildcardThreshold;
		}

		boolean mergedIncludeSamePackageImports;
		if (other.includeSamePackageImports == DEFAULT_INCLUDE_SAME_PACKAGE_IMPORTS)
		{
			mergedIncludeSamePackageImports = this.includeSamePackageImports;
		}
		else
		{
			mergedIncludeSamePackageImports = other.includeSamePackageImports;
		}

		return new ImportOrganizerRuleConfiguration(
			mergedRemoveUnused,
			mergedOrganizeGroups,
			mergedGroups,
			mergedCustomGroups,
			mergedSortOrder,
			mergedBlankLinesBetweenGroups,
			mergedPreserveManualSpacing,
			mergedStaticImportsPosition,
			mergedExpandWildcards,
			mergedWildcardThreshold,
			mergedIncludeSamePackageImports);
	}

	@Override
	public String getDescription()
	{
		return String.format(
			"Import Organizer Rule: removeUnused=%s, organizeGroups=%s, groups=%s, sortOrder=%s",
			removeUnused, organizeGroups, groups, sortOrder);
	}

	public boolean isRemoveUnused()
	{
		return removeUnused;
	}

	public boolean isOrganizeGroups()
	{
		return organizeGroups;
	}

	public List<String> getGroups()
	{
		return groups;
	}

	public Map<String, List<String>> getCustomGroups()
	{
		return customGroups;
	}

	public SortOrder getSortOrder()
	{
		return sortOrder;
	}

	public int getBlankLinesBetweenGroups()
	{
		return blankLinesBetweenGroups;
	}

	public boolean isPreserveManualSpacing()
	{
		return preserveManualSpacing;
	}

	public StaticImportsPosition getStaticImportsPosition()
	{
		return staticImportsPosition;
	}

	public boolean isExpandWildcards()
	{
		return expandWildcards;
	}

	public int getWildcardThreshold()
	{
		return wildcardThreshold;
	}

	public boolean isIncludeSamePackageImports()
	{
		return includeSamePackageImports;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
		{
			return true;
		}
		if (obj == null || getClass() != obj.getClass())
		{
			return false;
		}

		ImportOrganizerRuleConfiguration that = (ImportOrganizerRuleConfiguration) obj;
		return removeUnused == that.removeUnused &&
			organizeGroups == that.organizeGroups &&
			blankLinesBetweenGroups == that.blankLinesBetweenGroups &&
			preserveManualSpacing == that.preserveManualSpacing &&
			expandWildcards == that.expandWildcards &&
			wildcardThreshold == that.wildcardThreshold &&
			includeSamePackageImports == that.includeSamePackageImports &&
			Objects.equals(groups, that.groups) &&
			Objects.equals(customGroups, that.customGroups) &&
			sortOrder == that.sortOrder &&
			staticImportsPosition == that.staticImportsPosition;
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(removeUnused, organizeGroups, groups, customGroups, sortOrder,
			blankLinesBetweenGroups, preserveManualSpacing, staticImportsPosition,
			expandWildcards, wildcardThreshold, includeSamePackageImports);
	}

	@Override
	public String toString()
	{
		return "ImportOrganizerRuleConfiguration{" +
			"removeUnused=" + removeUnused +
			", organizeGroups=" + organizeGroups +
			", groups=" + groups +
			", customGroups=" + customGroups +
			", sortOrder=" + sortOrder +
			", blankLinesBetweenGroups=" + blankLinesBetweenGroups +
			", preserveManualSpacing=" + preserveManualSpacing +
			", staticImportsPosition=" + staticImportsPosition +
			", expandWildcards=" + expandWildcards +
			", wildcardThreshold=" + wildcardThreshold +
			", includeSamePackageImports=" + includeSamePackageImports +
			'}';
	}

	/**
	 * Enumeration of import sorting strategies.
	 */
	public enum SortOrder
	{
		/** Sort imports lexicographically (alphabetically). */
		LEXICOGRAPHIC,

		/** Sort imports by length (shortest first). */
		LENGTH,

		/** Do not sort imports (preserve existing order). */
		NONE
	}

	/**
	 * Enumeration of static import positioning strategies.
	 */
	public enum StaticImportsPosition
	{
		/** Place static imports at the top (before all other imports). */
		TOP,

		/** Place static imports at the bottom (after all other imports). */
		BOTTOM,

		/** Place static imports in their own configured group. */
		SEPARATE
	}
}
