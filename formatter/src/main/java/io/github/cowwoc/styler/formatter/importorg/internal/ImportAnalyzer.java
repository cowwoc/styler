package io.github.cowwoc.styler.formatter.importorg.internal;

import io.github.cowwoc.styler.formatter.TransformationContext;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;

/**
 * Detects unused imports by analyzing identifier usage in source code.
 * <p>
 * Uses a conservative approach: wildcard imports are assumed to be used (cannot
 * reliably determine usage without classpath information). Static imports are handled
 * by matching the imported method/field name against identifiers in the code.
 * <p>
 * <b>Thread-safety</b>: This class is stateless and thread-safe.
 */
public final class ImportAnalyzer
{
	private ImportAnalyzer()
	{
		// Utility class
	}

	/** Pattern for extracting identifiers from source code. */
	private static final Pattern IDENTIFIER_PATTERN = Pattern.compile("\\b([a-zA-Z_][a-zA-Z0-9_]*)\\b");

	/**
	 * Returns the import declarations that are not referenced in the code.
	 * <p>
	 * Algorithm:
	 * 1. Extract all identifiers used in code (post-import section)
	 * 2. For each import:
	 *    - If wildcard: keep (conservative)
	 *    - If simpleName in usedIdentifiers: used
	 *    - Otherwise: unused
	 *
	 * @param imports all import declarations in the file
	 * @param context transformation context for source access
	 * @return an empty set if no match is found
	 * @throws NullPointerException if {@code imports} or {@code context} is {@code null}
	 */
	public static Set<String> findUnusedImports(
		List<ImportDeclaration> imports,
		TransformationContext context)
	{
		requireThat(imports, "imports").isNotNull();
		requireThat(context, "context").isNotNull();

		// Extract all identifiers used in code
		Set<String> usedIdentifiers = extractUsedIdentifiers(context);

		Set<String> unused = new HashSet<>();

		for (ImportDeclaration imp : imports)
		{
			context.checkDeadline();

			// Conservative approach: always keep wildcard imports
			if (imp.isWildcard())
			{
				continue;
			}

			// Check if the simple name is used
			String simpleName = imp.simpleName();

			if (!usedIdentifiers.contains(simpleName))
			{
				unused.add(imp.qualifiedName());
			}
		}

		return unused;
	}

	/**
	 * Extracts all unique identifier tokens from source code.
	 * Looks at the code portion after the import section.
	 *
	 * @param context transformation context with source code
	 * @return set of unique identifier names
	 */
	private static Set<String> extractUsedIdentifiers(TransformationContext context)
	{
		Set<String> identifiers = new HashSet<>();
		String source = context.sourceCode();

		// Find the end of the import section
		int importSectionEnd = ImportExtractor.findImportSectionEnd(source);

		// Extract identifiers from code body (after imports)
		String codeBody = source.substring(importSectionEnd);

		Matcher matcher = IDENTIFIER_PATTERN.matcher(codeBody);

		while (matcher.find())
		{
			context.checkDeadline();
			String identifier = matcher.group(1);

			// Exclude Java keywords that could appear in code
			if (!isJavaKeyword(identifier))
			{
				identifiers.add(identifier);
			}
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
}
