package io.github.cowwoc.styler.formatter.impl;

import io.github.cowwoc.styler.ast.SourcePosition;
import io.github.cowwoc.styler.ast.SourceRange;
import io.github.cowwoc.styler.ast.node.CompilationUnitNode;
import io.github.cowwoc.styler.formatter.api.ImportOrganizerRuleConfiguration;
import io.github.cowwoc.styler.formatter.api.ImportOrganizerRuleConfiguration.StaticImportsPosition;
import io.github.cowwoc.styler.formatter.api.TextEdit;

import java.util.ArrayList;
import java.util.List;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;

/**
 * Reorganizes import statements in the AST by generating text edits.
 * <p>
 * This class takes organized import groups and generates the necessary text edits
 * to replace the existing import section with a properly formatted one.
 * <p>
 * The reorganizer handles:
 * <ul>
 *   <li>Calculating the source range covering all existing imports</li>
 *   <li>Generating formatted import text with proper grouping and spacing</li>
 *   <li>Applying blank lines between groups according to configuration</li>
 *   <li>Positioning static imports (top, bottom, or separate group)</li>
 *   <li>Preserving or removing manual spacing based on configuration</li>
 * </ul>
 * <p>
 * <b>Thread Safety:</b> This class is stateless and thread-safe.
 *
 * @since {@code 1}.{@code 0}.{@code 0}
 * @author Import Organizer Team
 */
public final class ImportReorganizer
{
	private static final String RULE_ID = "io.github.cowwoc.styler.rules.ImportOrganizer";

	/**
	 * Creates a new import reorganizer.
	 */
	public ImportReorganizer()
	{
		// Stateless - no fields to initialize
	}

	/**
	 * Generates text edits to reorganize imports in the compilation unit.
	 *
	 * @param compilationUnit the compilation unit containing imports
	 * @param groups          the organized import groups
	 * @param config          the configuration for import organization
	 * @param sourceText      the original source text (for extracting import text)
	 * @return list of text edits to apply, empty if no reorganization needed
	 * @throws NullPointerException if any parameter is {@code null}
	 */
	public List<TextEdit> reorganize(CompilationUnitNode compilationUnit, List<ImportGroup> groups,
		ImportOrganizerRuleConfiguration config, String sourceText)
	{
		requireThat(compilationUnit, "compilationUnit").isNotNull();
		requireThat(groups, "groups").isNotNull();
		requireThat(config, "config").isNotNull();
		requireThat(sourceText, "sourceText").isNotNull();

		// If there are no imports, nothing to reorganize
		if (compilationUnit.getImports().isEmpty())
		{
			return List.of();
		}

		SourceRange importsRange = calculateImportsRange(compilationUnit);
		String replacement = generateImportText(groups, config);

		List<TextEdit> edits = new ArrayList<>();
		edits.add(TextEdit.create(importsRange, replacement, RULE_ID));

		return edits;
	}

	/**
	 * Calculates the source range that covers all import declarations.
	 * <p>
	 * The range spans from the start of the first import to the end of the last import.
	 * Edge case: If there's only one import, the range covers just that import.
	 *
	 * @param compilationUnit the compilation unit
	 * @return the source range covering all imports
	 * @throws IllegalArgumentException if there are no imports
	 */
	private SourceRange calculateImportsRange(CompilationUnitNode compilationUnit)
	{
		List<io.github.cowwoc.styler.ast.ASTNode> imports = compilationUnit.getImports();

		requireThat(imports.isEmpty(), "imports.isEmpty()").isFalse();

		io.github.cowwoc.styler.ast.ASTNode firstImport = imports.get(0);
		io.github.cowwoc.styler.ast.ASTNode lastImport = imports.get(imports.size() - 1);

		SourcePosition start = firstImport.getRange().start();
		SourcePosition end = lastImport.getRange().end();

		return new SourceRange(start, end);
	}

	/**
	 * Generates the formatted import text from organized groups.
	 *
	 * @param groups     the organized import groups
	 * @param config     the configuration for formatting
	 * @return the formatted import text
	 */
	private String generateImportText(List<ImportGroup> groups, ImportOrganizerRuleConfiguration config)
	{
		StringBuilder result = new StringBuilder();
		int blankLinesBetweenGroups = config.getBlankLinesBetweenGroups();

		// Separate static and non-static groups if configured
		List<ImportGroup> staticGroups = new ArrayList<>();
		List<ImportGroup> nonStaticGroups = new ArrayList<>();

		for (ImportGroup group : groups)
		{
			boolean hasStatic = false;
			boolean hasNonStatic = false;

			for (ImportAnalyzer.ImportInfo importInfo : group.getImports())
			{
				if (importInfo.isStatic())
				{
					hasStatic = true;
				}
				else
				{
					hasNonStatic = true;
				}
			}

			if (hasStatic && config.getStaticImportsPosition() == StaticImportsPosition.SEPARATE)
			{
				staticGroups.add(group);
			}
			if (hasNonStatic)
			{
				nonStaticGroups.add(group);
			}
		}

		// Determine order based on static imports position
		List<ImportGroup> orderedGroups = new ArrayList<>();
		if (config.getStaticImportsPosition() == StaticImportsPosition.TOP)
		{
			orderedGroups.addAll(staticGroups);
			orderedGroups.addAll(nonStaticGroups);
		}
		else if (config.getStaticImportsPosition() == StaticImportsPosition.BOTTOM)
		{
			orderedGroups.addAll(nonStaticGroups);
			orderedGroups.addAll(staticGroups);
		}
		else
		{
			// SEPARATE - already separated, use original order
			orderedGroups.addAll(groups);
		}

		// Generate text for each group
		boolean firstGroup = true;
		for (ImportGroup group : orderedGroups)
		{
			if (group.isEmpty())
			{
				continue;
			}

			// Add blank lines between groups
			if (!firstGroup)
			{
				for (int i = 0; i < blankLinesBetweenGroups; ++i)
				{
					result.append('\n');
				}
			}
			firstGroup = false;

			// Add each import in the group
			for (ImportAnalyzer.ImportInfo importInfo : group.getImports())
			{
				result.append(formatImport(importInfo));
				result.append('\n');
			}
		}

		return result.toString();
	}

	/**
	 * Formats a single import statement.
	 * <p>
	 * Reconstructs the import declaration from the ImportInfo metadata.
	 * This ensures consistent formatting regardless of the original source formatting.
	 *
	 * @param importInfo the import information
	 * @return the formatted import statement
	 */
	private String formatImport(ImportAnalyzer.ImportInfo importInfo)
	{
		// Reconstruct the import statement from ImportInfo for consistent formatting
		// This approach is simpler and more reliable than extracting from source text
		StringBuilder result = new StringBuilder("import ");

		if (importInfo.isStatic())
		{
			result.append("static ");
		}

		result.append(importInfo.getFullName());
		result.append(';');

		return result.toString();
	}
}
