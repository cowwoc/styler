package io.github.cowwoc.styler.formatter.importorg.internal;

import io.github.cowwoc.styler.formatter.importorg.CustomImportPattern;
import io.github.cowwoc.styler.formatter.importorg.ImportGroup;
import io.github.cowwoc.styler.formatter.importorg.ImportOrganizerConfiguration;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;

/**
 * Groups and sorts imports according to configuration rules.
 * <p>
 * Separates static and regular imports, groups by pattern, and formats output
 * with appropriate blank line separators between groups.
 * <p>
 * <b>Thread-safety</b>: This class is stateless and thread-safe.
 */
public final class ImportGrouper
{
	private ImportGrouper()
	{
		// Utility class
	}

	/**
	 * Organizes and formats imports according to configuration.
	 * <p>
	 * Separates static, module, and regular imports, groups by pattern matching,
	 * sorts alphabetically within groups, and formats output with blank
	 * lines between groups.
	 * <p>
	 * Module imports (JEP 511) are placed in their own section, typically after
	 * regular imports but before static imports.
	 *
	 * @param imports list of import declarations
	 * @param config organization configuration
	 * @return formatted import section as string (without trailing newline)
	 * @throws NullPointerException if {@code imports} or {@code config} is {@code null}
	 */
	public static String organizeImports(
		List<ImportDeclaration> imports,
		ImportOrganizerConfiguration config)
	{
		requireThat(imports, "imports").isNotNull();
		requireThat(config, "config").isNotNull();

		List<ImportDeclaration> staticImports = new ArrayList<>();
		List<ImportDeclaration> moduleImports = new ArrayList<>();
		List<ImportDeclaration> regularImports = new ArrayList<>();

		for (ImportDeclaration imp : imports)
		{
			if (imp.isStatic())
			{
				staticImports.add(imp);
			}
			else if (imp.isModule())
			{
				moduleImports.add(imp);
			}
			else
			{
				regularImports.add(imp);
			}
		}

		Map<ImportGroup, List<ImportDeclaration>> regularGroups =
			groupByPattern(regularImports, config);
		Map<ImportGroup, List<ImportDeclaration>> staticGroups =
			groupByPattern(staticImports, config);

		if (config.sortImportsAlphabetically())
		{
			regularGroups.values().forEach(ImportGrouper::sortAlphabetically);
			staticGroups.values().forEach(ImportGrouper::sortAlphabetically);
			sortAlphabetically(moduleImports);
		}

		StringBuilder result = new StringBuilder();

		if (config.staticImportsFirst())
		{
			appendGroups(result, staticGroups, config.groupOrder(), true, false);
			appendModuleImportsSection(result, moduleImports, !staticImports.isEmpty());
			if ((!staticImports.isEmpty() || !moduleImports.isEmpty()) && !regularImports.isEmpty())
			{
				result.append('\n');
			}
			appendGroups(result, regularGroups, config.groupOrder(), false, false);
		}
		else
		{
			appendGroups(result, regularGroups, config.groupOrder(), false, false);
			appendModuleImportsSection(result, moduleImports, !regularImports.isEmpty());
			if ((!regularImports.isEmpty() || !moduleImports.isEmpty()) && !staticImports.isEmpty())
			{
				result.append('\n');
			}
			appendGroups(result, staticGroups, config.groupOrder(), true, false);
		}

		return result.toString();
	}

	/**
	 * Appends the module imports section to the result.
	 * <p>
	 * Module imports are formatted as {@code import module <module-name>;}.
	 *
	 * @param result        StringBuilder to append to
	 * @param moduleImports list of module import declarations
	 * @param hasPrevious   whether there are imports before this section
	 */
	private static void appendModuleImportsSection(
		StringBuilder result,
		List<ImportDeclaration> moduleImports,
		boolean hasPrevious)
	{
		if (moduleImports.isEmpty())
		{
			return;
		}

		if (hasPrevious)
		{
			result.append('\n');
		}

		for (ImportDeclaration imp : moduleImports)
		{
			result.append("import module ").append(imp.qualifiedName()).append(";\n");
		}
	}

	/**
	 * Groups imports by matching against configured patterns.
	 * <p>
	 * Matches imports against group patterns in order. First match wins.
	 * Unmatched imports go to THIRD_PARTY group (catch-all).
	 *
	 * @param imports list of imports to group
	 * @param config organization configuration
	 * @return map from ImportGroup to list of imports in that group
	 */
	private static Map<ImportGroup, List<ImportDeclaration>> groupByPattern(
		List<ImportDeclaration> imports,
		ImportOrganizerConfiguration config)
	{
		Map<ImportGroup, List<ImportDeclaration>> groups = new EnumMap<>(ImportGroup.class);
		for (ImportGroup group : config.groupOrder())
		{
			groups.put(group, new ArrayList<>());
		}
		groups.putIfAbsent(ImportGroup.THIRD_PARTY, new ArrayList<>());

		for (ImportDeclaration imp : imports)
		{
			ImportGroup group = classifyImport(imp, config);
			groups.get(group).add(imp);
		}

		return groups;
	}

	/**
	 * Classifies an import into a group based on pattern matching.
	 * <p>
	 * Checks patterns in order:
	 * 1. Standard groups (JAVA, JAVAX)
	 * 2. Custom patterns
	 * 3. Falls back to THIRD_PARTY
	 *
	 * @param imp the import to classify
	 * @param config organization configuration
	 * @return the ImportGroup for this import
	 */
	private static ImportGroup classifyImport(
		ImportDeclaration imp,
		ImportOrganizerConfiguration config)
	{
		String qualifiedName = imp.qualifiedName();

		for (ImportGroup group : config.groupOrder())
		{
			if (group == ImportGroup.PROJECT || group == ImportGroup.THIRD_PARTY)
				continue;

			String pattern = group.pattern();
			if (pattern != null && qualifiedName.matches(pattern))
				return group;
		}

		for (CustomImportPattern customPattern : config.customPatterns())
		{
			if (customPattern.matches(qualifiedName))
			{
				for (ImportGroup group : config.groupOrder())
				{
					if (group.name().equalsIgnoreCase(customPattern.groupName()))
						return group;
				}
			}
		}

		return ImportGroup.THIRD_PARTY;
	}

	/**
	 * Sorts a list of imports alphabetically by qualified name.
	 *
	 * @param imports the list to sort (modified in place)
	 */
	private static void sortAlphabetically(List<ImportDeclaration> imports)
	{
		imports.sort(Comparator.comparing(ImportDeclaration::qualifiedName));
	}

	/**
	 * Appends formatted import groups to the result string.
	 * <p>
	 * Outputs imports in the specified group order, with blank lines between
	 * non-empty groups.
	 *
	 * @param result   StringBuilder to append to
	 * @param groups   map from ImportGroup to list of imports
	 * @param order    desired group order
	 * @param isStatic whether these are static imports
	 * @param isModule whether these are module imports
	 */
	private static void appendGroups(
		StringBuilder result,
		Map<ImportGroup, List<ImportDeclaration>> groups,
		List<ImportGroup> order,
		boolean isStatic,
		boolean isModule)
	{
		boolean needsBlankLine = false;

		for (ImportGroup group : order)
		{
			List<ImportDeclaration> groupImports = groups.get(group);
			if (groupImports == null || groupImports.isEmpty())
			{
				continue;
			}

			if (needsBlankLine)
			{
				result.append('\n');
			}

			for (ImportDeclaration imp : groupImports)
			{
				result.append("import ");
				if (isStatic)
				{
					result.append("static ");
				}
				else if (isModule)
				{
					result.append("module ");
				}
				result.append(imp.qualifiedName()).append(";\n");
			}

			needsBlankLine = true;
		}
	}
}
