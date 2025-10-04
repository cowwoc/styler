package io.github.cowwoc.styler.formatter.impl;

import io.github.cowwoc.styler.formatter.api.ImportOrganizerRuleConfiguration;
import io.github.cowwoc.styler.formatter.api.ImportOrganizerRuleConfiguration.SortOrder;
import io.github.cowwoc.styler.formatter.api.ImportOrganizerRuleConfiguration.StaticImportsPosition;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;

/**
 * Groups and sorts imports according to configuration rules.
 * <p>
 * This grouper organizes imports into configurable groups based on package patterns
 * and sorts imports within each group according to the specified sort order.
 * <p>
 * Grouping rules:
 * <ul>
 *   <li><b>static</b> - matches all static imports</li>
 *   <li><b>java</b> - matches java.* and javax.* imports</li>
 *   <li><b>third-party</b> - matches imports not in java/javax or project packages</li>
 *   <li><b>project</b> - matches imports from the current project's packages</li>
 *   <li><b>custom groups</b> - user-defined regex patterns</li>
 * </ul>
 * <p>
 * <b>Thread Safety:</b> This class is stateless and thread-safe.
 *
 * @since {@code 1}.{@code 0}.{@code 0}
 * @author Import Organizer Team
 */
public final class ImportGrouper
{
	private final ImportOrganizerRuleConfiguration config;
	private final Map<String, List<Pattern>> compiledCustomGroups;

	/**
	 * Creates an import grouper with the specified configuration.
	 *
	 * @param config the configuration for grouping and sorting
	 * @throws NullPointerException if config is {@code null}
	 */
	public ImportGrouper(ImportOrganizerRuleConfiguration config)
	{
		requireThat(config, "config").isNotNull();
		this.config = config;

		// Pre-compile regex patterns for performance
		Map<String, List<Pattern>> compiled = new LinkedHashMap<>();
		for (Map.Entry<String, List<String>> entry : config.getCustomGroups().entrySet())
		{
			String groupName = entry.getKey();
			List<String> patterns = entry.getValue();
			List<Pattern> compiledPatterns = new ArrayList<>();
			for (String patternStr : patterns)
			{
				compiledPatterns.add(Pattern.compile(patternStr));
			}
			compiled.put(groupName, compiledPatterns);
		}
		this.compiledCustomGroups = Collections.unmodifiableMap(compiled);
	}

	/**
	 * Groups and sorts the provided imports according to configuration.
	 *
	 * @param imports the imports to group and sort
	 * @return list of import groups in configured order
	 * @throws NullPointerException if imports is {@code null}
	 */
	public List<ImportGroup> groupAndSort(List<ImportAnalyzer.ImportInfo> imports)
	{
		requireThat(imports, "imports").isNotNull();

		Map<String, List<ImportAnalyzer.ImportInfo>> groupMap = new LinkedHashMap<>();

		for (String groupName : config.getGroups())
		{
			groupMap.put(groupName, new ArrayList<>());
		}

		for (String customGroupName : config.getCustomGroups().keySet())
		{
			groupMap.put(customGroupName, new ArrayList<>());
		}

		for (ImportAnalyzer.ImportInfo importInfo : imports)
		{
			String groupName = determineGroup(importInfo);
			groupMap.computeIfAbsent(groupName, k -> new ArrayList<>()).add(importInfo);
		}

		for (List<ImportAnalyzer.ImportInfo> groupImports : groupMap.values())
		{
			sortImports(groupImports);
		}

		List<ImportGroup> result = new ArrayList<>();
		for (String groupName : config.getGroups())
		{
			List<ImportAnalyzer.ImportInfo> groupImports = groupMap.get(groupName);
			if (groupImports != null && !groupImports.isEmpty())
			{
				result.add(new ImportGroup(groupName, groupImports));
			}
		}

		for (String customGroupName : config.getCustomGroups().keySet())
		{
			List<ImportAnalyzer.ImportInfo> groupImports = groupMap.get(customGroupName);
			if (groupImports != null && !groupImports.isEmpty())
			{
				result.add(new ImportGroup(customGroupName, groupImports));
			}
		}

		return result;
	}

	/**
	 * Determines which group an import belongs to.
	 *
	 * @param importInfo the import to classify
	 * @return the group name
	 */
	private String determineGroup(ImportAnalyzer.ImportInfo importInfo)
	{
		// Check if this is a static import and if static imports should be in a separate group
		if (importInfo.isStatic() && config.getStaticImportsPosition() == StaticImportsPosition.SEPARATE)
		{
			return "static";
		}

		// Check custom groups first (user-defined patterns take precedence)
		for (Map.Entry<String, List<Pattern>> customGroup : compiledCustomGroups.entrySet())
		{
			String groupName = customGroup.getKey();
			List<Pattern> patterns = customGroup.getValue();

			for (Pattern pattern : patterns)
			{
				if (pattern.matcher(importInfo.getFullName()).matches())
				{
					return groupName;
				}
			}
		}

		// Check built-in groups in configured order
		for (String groupName : config.getGroups())
		{
			if (matchesBuiltInGroup(groupName, importInfo))
			{
				return groupName;
			}
		}

		// Default to third-party if no match found
		return "third-party";
	}

	/**
	 * Checks if an import matches a built-in group pattern.
	 *
	 * @param groupName the built-in group name
	 * @param importInfo the import to check
	 * @return {@code true} if the import matches this group
	 */
	private boolean matchesBuiltInGroup(String groupName, ImportAnalyzer.ImportInfo importInfo)
	{
		return switch (groupName)
		{
			case "static" -> importInfo.isStatic();
			case "java" -> importInfo.getPackageName().startsWith("java.") ||
				importInfo.getPackageName().startsWith("javax.");
			case "project" ->
			{
				// Project imports require explicit configuration via custom groups
				yield false;
			}
			case "third-party" ->
			{
				// Third-party is anything not java/javax and not project
				yield !importInfo.getPackageName().startsWith("java.") &&
					!importInfo.getPackageName().startsWith("javax.");
			}
			default -> false; // Unknown group name
		};
	}

	/**
	 * Sorts imports within a group according to configuration.
	 *
	 * @param imports the imports to sort (modified in place)
	 */
	private void sortImports(List<ImportAnalyzer.ImportInfo> imports)
	{
		Comparator<ImportAnalyzer.ImportInfo> comparator = createComparator();
		imports.sort(comparator);
	}

	/**
	 * Creates a comparator for sorting imports based on configuration.
	 *
	 * @return the comparator for import sorting
	 */
	private Comparator<ImportAnalyzer.ImportInfo> createComparator()
	{
		SortOrder sortOrder = config.getSortOrder();

		return switch (sortOrder)
		{
			case LEXICOGRAPHIC -> Comparator.comparing(ImportAnalyzer.ImportInfo::getFullName);
			case LENGTH ->
				Comparator.comparingInt((ImportAnalyzer.ImportInfo i) -> i.getFullName().length()).
					thenComparing(ImportAnalyzer.ImportInfo::getFullName);
			case NONE -> (a, b) -> 0; // Preserve original order
		};
	}
}
