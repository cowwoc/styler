package io.github.cowwoc.styler.formatter.importorg;

import io.github.cowwoc.styler.formatter.DefaultFormattingViolation;
import io.github.cowwoc.styler.formatter.FormattingConfiguration;
import io.github.cowwoc.styler.formatter.FormattingRule;
import io.github.cowwoc.styler.formatter.FormattingViolation;
import io.github.cowwoc.styler.formatter.TransformationContext;
import io.github.cowwoc.styler.formatter.ViolationSeverity;
import io.github.cowwoc.styler.formatter.importorg.internal.ImportAnalysisResult;
import io.github.cowwoc.styler.formatter.importorg.internal.ImportAnalyzer;
import io.github.cowwoc.styler.formatter.importorg.internal.ImportDeclaration;
import io.github.cowwoc.styler.formatter.importorg.internal.ImportExtractor;
import io.github.cowwoc.styler.formatter.importorg.internal.ImportGrouper;
import io.github.cowwoc.styler.formatter.ClasspathScanner;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
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
		List<FormattingConfiguration> configs)
	{
		requireThat(context, "context").isNotNull();
		requireThat(configs, "configs").isNotNull();

		ImportOrganizerConfiguration importConfig = FormattingConfiguration.findConfig(
			configs, ImportOrganizerConfiguration.class, ImportOrganizerConfiguration.defaultConfig());
		List<FormattingViolation> violations = new ArrayList<>();

		List<ImportDeclaration> imports = ImportExtractor.extract(context);
		if (imports.isEmpty())
		{
			return violations;
		}
		context.checkDeadline();

		if (importConfig.removeUnusedImports())
		{
			ImportAnalysisResult analysisResult = analyzeImports(imports, context, importConfig);

			// Add violation for unresolved symbols (if any)
			if (!analysisResult.isResolutionComplete())
			{
				violations.add(createUnresolvedSymbolsViolation(analysisResult.unresolvedSymbols(), context));
			}

			// Add violations for unused imports
			for (ImportDeclaration imp : imports)
			{
				if (analysisResult.unusedImports().contains(imp.qualifiedName()))
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
		List<FormattingConfiguration> configs)
	{
		requireThat(context, "context").isNotNull();
		requireThat(configs, "configs").isNotNull();

		ImportOrganizerConfiguration importConfig = FormattingConfiguration.findConfig(
			configs, ImportOrganizerConfiguration.class, ImportOrganizerConfiguration.defaultConfig());
		List<ImportDeclaration> originalImports = ImportExtractor.extract(context);
		if (originalImports.isEmpty())
		{
			return context.sourceCode();
		}
		context.checkDeadline();

		List<ImportDeclaration> imports = originalImports;
		if (importConfig.removeUnusedImports())
		{
			ImportAnalysisResult analysisResult = analyzeImports(imports, context, importConfig);
			imports = imports.stream().
				filter(imp -> !analysisResult.unusedImports().contains(imp.qualifiedName())).
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
	 * Creates a violation for an unused import.
	 *
	 * @param imp     the unused import
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
			List.of());
	}

	/**
	 * Creates a violation for unresolved symbols during import analysis.
	 * <p>
	 * This is a file-level violation indicating that symbol resolution was incomplete,
	 * typically due to missing classpath entries.
	 *
	 * @param unresolvedSymbols the symbols that could not be resolved
	 * @param context           the transformation context
	 * @return a FormattingViolation
	 */
	private FormattingViolation createUnresolvedSymbolsViolation(
		Set<String> unresolvedSymbols,
		TransformationContext context)
	{
		String symbolList = unresolvedSymbols.stream().
			sorted().
			collect(java.util.stream.Collectors.joining(", "));
		String message = "Cannot expand wildcard imports: unresolved symbols [" + symbolList +
			"]. Configure classpath or set expandWildcardImports=false";

		return new DefaultFormattingViolation(
			RULE_ID,
			ViolationSeverity.WARNING,
			message,
			context.filePath(),
			0,
			0,
			1,
			1,
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

		// Imports are sorted by position, so first/last give section bounds
		int importStart = imports.getFirst().startPosition();
		int importEnd = imports.getLast().endPosition();
		String currentImportSection = context.sourceCode().substring(importStart, importEnd + 1).strip();

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

		// Imports are sorted by position, so first/last give section bounds
		int importStart = imports.getFirst().startPosition();
		// +1 because endPosition is inclusive
		int sectionEnd = imports.getLast().endPosition() + 1;

		StringBuilder result = new StringBuilder().
			append(source, 0, importStart).
			append(organized);
		if (sectionEnd < source.length() && source.charAt(sectionEnd) != '\n')
		{
			result.append('\n');
		}

		result.append(source.substring(sectionEnd));

		return result.toString();
	}

	/**
	 * Analyzes imports to find unused ones and resolve wildcard imports.
	 *
	 * @param imports      all import declarations in the file
	 * @param context      the transformation context
	 * @param importConfig the import organizer configuration
	 * @return analysis result containing unused imports and any unresolved symbols
	 */
	private ImportAnalysisResult analyzeImports(
		List<ImportDeclaration> imports,
		TransformationContext context,
		ImportOrganizerConfiguration importConfig)
	{
		if (!importConfig.expandWildcardImports())
		{
			// No scanner needed - wildcards preserved silently without symbol resolution
			return ImportAnalyzer.findUnusedImports(imports, context, importConfig, ClasspathScanner.empty());
		}

		// Use shared scanner from context - pipeline owns the scanner lifecycle
		return ImportAnalyzer.findUnusedImports(imports, context, importConfig, context.classpathScanner());
	}
}
