package io.github.cowwoc.styler.formatter.importorg.internal;

import io.github.cowwoc.styler.formatter.AstPositionIndex;
import io.github.cowwoc.styler.formatter.TransformationContext;
import io.github.cowwoc.styler.formatter.importorg.ImportOrganizerConfiguration;
import io.github.cowwoc.styler.formatter.internal.ClasspathScanner;

import java.util.BitSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;

/**
 * Detects unused imports by analyzing identifier usage in source code.
 * <p>
 * <b>Safety-First Wildcard Handling</b>:
 * <ul>
 *   <li>If {@code expandWildcardImports} is disabled: wildcards are conservatively kept (no violations)</li>
 *   <li>If enabled with complete symbol resolution: wildcards expanded to explicit imports</li>
 *   <li>If enabled but ANY symbol is unresolved: ALL wildcards preserved, single violation reported</li>
 * </ul>
 * <p>
 * This approach prevents silent code breakage when the classpath is misconfigured. If the user has
 * missing classpath entries, symbols from those entries would appear unresolved, causing wildcards
 * to be preserved rather than incorrectly removed.
 * <p>
 * Static imports are handled by matching the imported method/field name against identifiers in the code.
 * <p>
 * <b>Thread-safety</b>: This class is stateless and thread-safe.
 */
public final class ImportAnalyzer
{
	/**
	 * Pattern for extracting identifier-like tokens from source code.
	 * Used with AST filtering to ensure we don't match inside strings/comments.
	 */
	private static final Pattern IDENTIFIER_PATTERN = Pattern.compile("\\b([a-zA-Z_][a-zA-Z0-9_]*)\\b");

	private ImportAnalyzer()
	{
		// Utility class
	}

	/**
	 * Analyzes import declarations to find unused imports and detect violations.
	 * <p>
	 * <b>Algorithm</b>:
	 * <ol>
	 *   <li>Extract all identifiers used in code (post-import section)</li>
	 *   <li>If wildcard expansion is disabled, use conservative approach (explicit imports only)</li>
	 *   <li>If wildcard expansion is enabled:
	 *     <ol>
	 *       <li>Attempt full symbol resolution via {@link SymbolResolver}</li>
	 *       <li>If ANY symbol is unresolved: preserve ALL wildcards, report single violation</li>
	 *       <li>If ALL symbols resolved: safe to expand wildcards to explicit imports</li>
	 *     </ol>
	 *   </li>
	 * </ol>
	 *
	 * @param imports all import declarations in the file
	 * @param context transformation context for source access
	 * @param config  import organizer configuration
	 * @param scanner classpath scanner for symbol resolution (use {@link ClasspathScanner#empty()}
	 *                when {@code expandWildcardImports} is disabled)
	 * @return analysis result containing unused imports and violations
	 * @throws NullPointerException if any argument is {@code null}
	 */
	public static ImportAnalysisResult findUnusedImports(
		List<ImportDeclaration> imports,
		TransformationContext context,
		ImportOrganizerConfiguration config,
		ClasspathScanner scanner)
	{
		requireThat(imports, "imports").isNotNull();
		requireThat(context, "context").isNotNull();
		requireThat(config, "config").isNotNull();
		requireThat(scanner, "scanner").isNotNull();

		// Extract all identifiers used in code
		Set<String> usedIdentifiers = extractUsedIdentifiers(context);
		context.checkDeadline();

		if (!config.expandWildcardImports())
			// User opted out of wildcard expansion - use conservative approach
			return analyzeWithoutWildcardExpansion(imports, usedIdentifiers, context);

		// Attempt full symbol resolution
		SymbolResolutionResult resolution = SymbolResolver.resolve(imports, usedIdentifiers,
			context, scanner);
		context.checkDeadline();

		if (!resolution.isComplete())
			// Cannot safely expand wildcards - report violation and preserve all wildcards
			return createResolutionFailureResult(imports, usedIdentifiers, resolution, context);

		// All symbols resolved - safe to expand wildcards
		return analyzeWithFullResolution(imports, usedIdentifiers, resolution, context);
	}

	/**
	 * Extracts all unique identifier tokens from source code.
	 * <p>
	 * Uses regex to find identifiers in the code body (after imports), then filters using
	 * AST position data to exclude matches inside strings and comments.
	 *
	 * @param context transformation context with source code and AST access
	 * @return set of unique identifier names
	 */
	private static Set<String> extractUsedIdentifiers(TransformationContext context)
	{
		Set<String> identifiers = new HashSet<>();
		String source = context.sourceCode();
		AstPositionIndex positionIndex = context.positionIndex();

		// Find the end of the import section - we only want identifiers after imports
		int importSectionEnd = ImportExtractor.findImportSectionEnd(context);

		// Get positions inside text and comments to exclude (from AST)
		BitSet textAndCommentPositions = positionIndex.getTextAndCommentPositions();

		// Use regex to find identifier-like tokens, but filter using AST data
		Matcher matcher = IDENTIFIER_PATTERN.matcher(source);

		while (matcher.find())
		{
			context.checkDeadline();

			int start = matcher.start(1);

			// Skip identifiers before or at the import section end
			if (start <= importSectionEnd)
				continue;

			// Skip identifiers inside strings or comments (AST-based filtering)
			if (textAndCommentPositions.get(start))
				continue;

			String identifier = matcher.group(1);

			// Exclude Java keywords that could appear as identifiers
			if (!isJavaKeyword(identifier))
				identifiers.add(identifier);
		}

		return identifiers;
	}

	/**
	 * Checks if a string is a Java keyword that should not be treated as an identifier.
	 *
	 * @param word the word to check
	 * @return true if the word is a Java keyword
	 */
	private static boolean isJavaKeyword(String word)
	{
		// Commonly used keywords that appear as identifiers but should be ignored
		return switch (word)
		{
			case "public", "private", "protected", "static", "final", "class", "interface",
				 "extends", "implements", "new", "if", "else", "for", "while", "do", "switch",
				 "case", "default", "break", "continue", "return", "throw", "try", "catch",
				 "finally", "synchronized", "volatile", "transient", "native", "abstract",
				 "strictfp", "enum", "package", "import", "void", "int", "long", "float",
				 "double", "boolean", "byte", "short", "char", "const", "goto", "this",
				 "super", "null", "true", "false", "instanceof" -> true;
			default -> false;
		};
	}

	/**
	 * Analyzes imports without attempting wildcard expansion.
	 * <p>
	 * This mode is used when the user has disabled wildcard expansion. Wildcards are preserved
	 * silently (no unresolved symbols reported), and only explicit imports are checked for usage.
	 *
	 * @param imports         all import declarations
	 * @param usedIdentifiers identifiers used in code
	 * @param context         transformation context for deadline checking
	 * @return analysis result with unused explicit imports (wildcards always kept)
	 */
	private static ImportAnalysisResult analyzeWithoutWildcardExpansion(
		List<ImportDeclaration> imports,
		Set<String> usedIdentifiers,
		TransformationContext context)
	{
		Set<String> unused = new HashSet<>();

		for (ImportDeclaration imp : imports)
		{
			context.checkDeadline();

			// Skip wildcards - they are preserved silently when expansion is disabled
			if (imp.isWildcard())
				continue;

			// Check if the simple name is used
			if (!usedIdentifiers.contains(imp.simpleName()))
				unused.add(imp.qualifiedName());
		}

		return new ImportAnalysisResult(unused, Set.of());
	}

	/**
	 * Creates a result for when symbol resolution is incomplete.
	 * <p>
	 * When ANY symbol cannot be resolved, we cannot safely expand wildcards because the
	 * unresolved symbol may come from a wildcard import. Removing that wildcard would break
	 * the code silently.
	 * <p>
	 * In this case, we:
	 * <ol>
	 *   <li>Preserve ALL wildcard imports (do not mark as unused)</li>
	 *   <li>Report the unresolved symbols for diagnostic purposes</li>
	 *   <li>Still check explicit imports for usage</li>
	 * </ol>
	 *
	 * @param imports         all import declarations
	 * @param usedIdentifiers identifiers used in code
	 * @param resolution      the incomplete resolution result
	 * @param context         transformation context for deadline checking
	 * @return analysis result preserving wildcards with unresolved symbols reported
	 */
	private static ImportAnalysisResult createResolutionFailureResult(
		List<ImportDeclaration> imports,
		Set<String> usedIdentifiers,
		SymbolResolutionResult resolution,
		TransformationContext context)
	{
		Set<String> unused = new HashSet<>();

		// Analyze explicit imports only (wildcards preserved)
		for (ImportDeclaration imp : imports)
		{
			context.checkDeadline();

			// Skip wildcards - they are preserved when resolution is incomplete
			if (imp.isWildcard())
				continue;

			// Check if the simple name is used
			if (!usedIdentifiers.contains(imp.simpleName()))
				unused.add(imp.qualifiedName());
		}

		return new ImportAnalysisResult(unused, resolution.unresolvedSymbols());
	}

	/**
	 * Analyzes imports with complete symbol resolution.
	 * <p>
	 * When all symbols are resolved, we know exactly which classes from wildcard imports
	 * are used, so we can safely expand wildcards to explicit imports.
	 * <p>
	 * A wildcard import is marked as unused if NONE of the classes from its package
	 * are used in the code.
	 *
	 * @param imports         all import declarations
	 * @param usedIdentifiers identifiers used in code
	 * @param resolution      the complete resolution result
	 * @param context         transformation context for deadline checking
	 * @return analysis result with unused imports identified
	 */
	private static ImportAnalysisResult analyzeWithFullResolution(
		List<ImportDeclaration> imports,
		Set<String> usedIdentifiers,
		SymbolResolutionResult resolution,
		TransformationContext context)
	{
		Set<String> unused = new HashSet<>();

		// Build set of resolved qualified names for quick lookup
		Set<String> resolvedQualifiedNames = new HashSet<>(resolution.resolvedSymbols().values());

		for (ImportDeclaration imp : imports)
		{
			context.checkDeadline();

			if (imp.isWildcard())
			{
				// Check if any resolved symbol comes from this wildcard's package
				String packagePrefix = imp.packageName() + ".";
				boolean hasResolvedSymbol = resolvedQualifiedNames.stream().
					anyMatch(qn -> qn.startsWith(packagePrefix));

				if (!hasResolvedSymbol)
					// No classes from this wildcard are used
					unused.add(imp.qualifiedName());
			}
			else
			{
				// Explicit import - check if the simple name is used
				if (!usedIdentifiers.contains(imp.simpleName()))
					unused.add(imp.qualifiedName());
			}
		}

		return new ImportAnalysisResult(unused, Set.of());
	}
}
