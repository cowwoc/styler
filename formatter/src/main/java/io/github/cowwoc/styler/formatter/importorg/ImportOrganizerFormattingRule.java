package io.github.cowwoc.styler.formatter.importorg;

import io.github.cowwoc.styler.formatter.DefaultFormattingViolation;
import io.github.cowwoc.styler.formatter.FormattingConfiguration;
import io.github.cowwoc.styler.formatter.FormattingRule;
import io.github.cowwoc.styler.formatter.FormattingViolation;
import io.github.cowwoc.styler.formatter.TransformationContext;
import io.github.cowwoc.styler.formatter.ViolationSeverity;
import io.github.cowwoc.styler.formatter.importorg.internal.ImportAnalyzer;
import io.github.cowwoc.styler.formatter.importorg.internal.ImportDeclaration;
import io.github.cowwoc.styler.formatter.importorg.internal.ImportExtractor;
import io.github.cowwoc.styler.formatter.importorg.internal.ImportGrouper;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;

/**
 * Formatting rule for organizing and cleaning up Java import statements.
 * <p>
 * This rule:
 * - Detects imports not referenced in the code (if enabled)
 * - Detects imports that are out of order or in the wrong group
 * - Can reorganize and deduplicate imports
 * <p>
 * <b>Thread-safety</b>: This class is thread-safe.
 */
public final class ImportOrganizerFormattingRule implements FormattingRule
{
	/**
	 * Unique identifier for this rule.
	 */
	public static final String RULE_ID = "import-organizer";

	/**
	 * Human-readable name for this rule.
	 */
	private static final String RULE_NAME = "Import Organizer";

	/**
	 * Description of what this rule checks.
	 */
	private static final String RULE_DESCRIPTION =
		"Organizes imports into groups and removes unused imports";

	/**
	 * Creates a new import organizer formatting rule instance.
	 */
	public ImportOrganizerFormattingRule()
	{
		// No-arg constructor for instantiation
	}

	@Override
	public String getId()
	{
		return RULE_ID;
	}

	@Override
	public String getName()
	{
		return RULE_NAME;
	}

	@Override
	public String getDescription()
	{
		return RULE_DESCRIPTION;
	}

	@Override
	public ViolationSeverity getDefaultSeverity()
	{
		return ViolationSeverity.WARNING;
	}

	@Override
	public List<FormattingViolation> analyze(
		TransformationContext context,
		FormattingConfiguration config)
	{
		requireThat(context, "context").isNotNull();

		ImportOrganizerConfiguration importConfig = getConfig(config);
		List<FormattingViolation> violations = new ArrayList<>();

		List<ImportDeclaration> imports = ImportExtractor.extract(context);
		if (imports.isEmpty())
		{
			return violations;
		}
		context.checkDeadline();

		if (importConfig.removeUnusedImports())
		{
			Set<String> unused = ImportAnalyzer.findUnusedImports(imports, context);
			for (ImportDeclaration imp : imports)
			{
				if (unused.contains(imp.qualifiedName()))
				{
					violations.add(createUnusedImportViolation(imp, context));
				}
			}
		}

		context.checkDeadline();

		Set<String> seen = new HashSet<>();
		for (ImportDeclaration imp : imports)
		{
			if (!seen.add(imp.qualifiedName()))
			{
				violations.add(createDuplicateImportViolation(imp, context));
			}
		}

		context.checkDeadline();

		String organizedImports = ImportGrouper.organizeImports(imports, importConfig);
		if (!importsAreOrganized(imports, organizedImports, context))
		{
			violations.add(createOrderingViolation(context));
		}

		return violations;
	}

	@Override
	public String format(
		TransformationContext context,
		FormattingConfiguration config)
	{
		requireThat(context, "context").isNotNull();

		ImportOrganizerConfiguration importConfig = getConfig(config);
		List<ImportDeclaration> originalImports = ImportExtractor.extract(context);
		if (originalImports.isEmpty())
		{
			return context.sourceCode();
		}
		context.checkDeadline();

		List<ImportDeclaration> imports = originalImports;
		if (importConfig.removeUnusedImports())
		{
			Set<String> unused = ImportAnalyzer.findUnusedImports(imports, context);
			imports = imports.stream().
				filter(imp -> !unused.contains(imp.qualifiedName())).
				toList();
		}
		context.checkDeadline();

		// Remove duplicates, keeping first occurrence
		Set<String> seen = new HashSet<>();
		imports = imports.stream().
			filter(imp -> seen.add(imp.qualifiedName())).
			toList();
		context.checkDeadline();

		String organizedImports = ImportGrouper.organizeImports(imports, importConfig);
		// Use original imports for section bounds since filtered list may have different positions
		return replaceImportSection(context.sourceCode(), originalImports, organizedImports);
	}

	/**
	 * Returns the configuration, using defaults if null.
	 *
	 * @param config the configuration (may be null)
	 * @return the configuration to use
	 */
	private ImportOrganizerConfiguration getConfig(FormattingConfiguration config)
	{
		if (config == null)
		{
			return ImportOrganizerConfiguration.defaultConfig();
		}

		requireThat(config, "config").isInstanceOf(ImportOrganizerConfiguration.class);
		return (ImportOrganizerConfiguration) config;
	}

	/**
	 * Creates a violation for an unused import.
	 *
	 * @param imp the unused import
	 * @param context the transformation context
	 * @return a FormattingViolation
	 */
	private FormattingViolation createUnusedImportViolation(
		ImportDeclaration imp,
		TransformationContext context)
	{
		String message = "Import '" + imp.qualifiedName() + "' is not used";
		int lineNumber = context.getLineNumber(imp.startPosition());
		int columnNumber = context.getColumnNumber(imp.startPosition());

		return new DefaultFormattingViolation(
			RULE_ID,
			ViolationSeverity.WARNING,
			message,
			context.filePath(),
			imp.startPosition(),
			imp.endPosition(),
			lineNumber,
			columnNumber,
			Optional.empty(),
			List.of());
	}

	/**
	 * Creates a violation for a duplicate import.
	 *
	 * @param imp the duplicate import
	 * @param context the transformation context
	 * @return a FormattingViolation
	 */
	private FormattingViolation createDuplicateImportViolation(
		ImportDeclaration imp,
		TransformationContext context)
	{
		String message = "Duplicate import '" + imp.qualifiedName() + "'";
		int lineNumber = context.getLineNumber(imp.startPosition());
		int columnNumber = context.getColumnNumber(imp.startPosition());

		return new DefaultFormattingViolation(
			RULE_ID,
			ViolationSeverity.WARNING,
			message,
			context.filePath(),
			imp.startPosition(),
			imp.endPosition(),
			lineNumber,
			columnNumber,
			Optional.empty(),
			List.of());
	}

	/**
	 * Creates a violation for imports that are out of order.
	 *
	 * @param context the transformation context
	 * @return a FormattingViolation
	 */
	private FormattingViolation createOrderingViolation(TransformationContext context)
	{
		return new DefaultFormattingViolation(
			RULE_ID,
			ViolationSeverity.WARNING,
			"Imports are not properly organized",
			context.filePath(),
			0,
			0,
			1,  // Line number for first import
			1,  // Column number
			Optional.empty(),
			List.of());
	}

	/**
	 * Checks if imports are properly organized.
	 *
	 * @param imports the original imports
	 * @param organized the organized import section
	 * @param context  the transformation context
	 * @return true if imports are already organized
	 */
	private boolean importsAreOrganized(
		List<ImportDeclaration> imports,
		String organized,
		TransformationContext context)
	{
		if (imports.isEmpty())
		{
			return true;
		}

		int importStart = imports.get(0).startPosition();
		int importEnd = imports.get(imports.size() - 1).endPosition() + 1;
		String currentImportSection = context.sourceCode().substring(importStart, importEnd).strip();

		return currentImportSection.equals(organized.strip());
	}

	/**
	 * Replaces the import section in the source code.
	 * <p>
	 * Finds the start and end of the import section and replaces it with
	 * the newly organized imports.
	 *
	 * @param source the original source code
	 * @param imports the original imports (for finding positions)
	 * @param organized the newly organized imports
	 * @return the source code with imports replaced
	 */
	private String replaceImportSection(
		String source,
		List<ImportDeclaration> imports,
		String organized)
	{
		if (imports.isEmpty())
		{
			return source;
		}

		int importStart = imports.get(0).startPosition();
		int importEnd = imports.get(imports.size() - 1).endPosition() + 1;

		StringBuilder result = new StringBuilder().
			append(source, 0, importStart).
			append(organized);
		if (importEnd < source.length() && source.charAt(importEnd) != '\n')
		{
			result.append('\n');
		}

		result.append(source.substring(importEnd));

		return result.toString();
	}
}
