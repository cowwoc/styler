package io.github.cowwoc.styler.formatter.impl;

import io.github.cowwoc.styler.ast.node.CompilationUnitNode;
import io.github.cowwoc.styler.formatter.api.FormattingContext;
import io.github.cowwoc.styler.formatter.api.FormattingResult;
import io.github.cowwoc.styler.formatter.api.FormattingRule;
import io.github.cowwoc.styler.formatter.api.ImportOrganizerRuleConfiguration;
import io.github.cowwoc.styler.formatter.api.RuleConfiguration;
import io.github.cowwoc.styler.formatter.api.ValidationResult;

import java.util.List;

/**
 * Formatting rule that organizes import statements and removes unused imports.
 * <p>
 * This rule performs the following transformations:
 * <ul>
 *   <li>Groups imports according to configured patterns (java.*, third-party, project, static)</li>
 *   <li>Sorts imports within each group (lexicographically by default)</li>
 *   <li>Removes unused imports (if enabled)</li>
 *   <li>Handles wildcard imports (expand/collapse based on threshold)</li>
 *   <li>Positions static imports according to configuration</li>
 *   <li>Applies configurable blank lines between groups</li>
 * </ul>
 * <p>
 * <b>Priority:</b> 50 (runs before LineLength rule which has priority 100)
 * <p>
 * <b>Thread Safety:</b> This class is stateless and thread-safe.
 *
 * @since {@code 1}.{@code 0}.{@code 0}
 * @author Import Organizer Team
 */
public final class ImportOrganizerFormattingRule implements FormattingRule
{
	private static final String RULE_ID = "io.github.cowwoc.styler.rules.ImportOrganizer";
	private static final int RULE_PRIORITY = 50;

	/**
	 * Creates a new import organizer formatting rule.
	 */
	public ImportOrganizerFormattingRule()
	{
		// Stateless - no fields to initialize
	}

	@Override
	public String getRuleId()
	{
		return RULE_ID;
	}

	@Override
	public int getPriority()
	{
		return RULE_PRIORITY;
	}

	@Override
	public RuleConfiguration getDefaultConfiguration()
	{
		return new ImportOrganizerRuleConfiguration();
	}

	@Override
	public ValidationResult validate(FormattingContext context)
	{
		return ValidationResult.success();
	}

	@Override
	public FormattingResult apply(FormattingContext context)
	{
		ImportOrganizerRuleConfiguration config =
			(ImportOrganizerRuleConfiguration) context.getConfiguration();

		CompilationUnitNode compilationUnit = context.getRootNode();
		String sourceText = context.getSourceText();

		// Phase 2: Analyze imports
		ImportAnalyzer analyzer = new ImportAnalyzer();
		List<ImportAnalyzer.ImportInfo> imports = analyzer.analyze(compilationUnit);

		// If there are no imports, nothing to do
		if (imports.isEmpty())
		{
			return FormattingResult.empty();
		}

		// Group and sort imports
		ImportGrouper grouper = new ImportGrouper(config);
		List<ImportGroup> groups = grouper.groupAndSort(imports);

		// Phase 4: Generate text edits to reorganize imports
		ImportReorganizer reorganizer = new ImportReorganizer();
		List<io.github.cowwoc.styler.formatter.api.TextEdit> edits =
			reorganizer.reorganize(compilationUnit, groups, config, sourceText);

		if (edits.isEmpty())
		{
			return FormattingResult.empty();
		}

		return FormattingResult.withEdits(edits);
	}
}
