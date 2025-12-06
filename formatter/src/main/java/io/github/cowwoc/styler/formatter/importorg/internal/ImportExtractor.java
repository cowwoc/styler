package io.github.cowwoc.styler.formatter.importorg.internal;

import io.github.cowwoc.styler.formatter.TransformationContext;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;

/**
 * Extracts import declarations from Java source code using text-based parsing.
 * <p>
 * <b>Thread-safety</b>: This class is stateless and thread-safe.
 */
public final class ImportExtractor
{
	private ImportExtractor()
	{
		// Utility class
	}

	/** Regex pattern for matching import statements. */
	private static final Pattern IMPORT_PATTERN =
		Pattern.compile(
			"^\\s*(import\\s+)(static\\s+)?" +           // import [static]
				"([\\w.]+(?:\\.\\*)?)" +                   // qualified name or wildcard
				"\\s*;",                                   // semicolon
			Pattern.MULTILINE);

	/**
	 * Extracts all import declarations from the source code.
	 * <p>
	 * Handles:
	 * - Regular imports: import java.util.List;
	 * - Static imports: import static java.lang.Math.PI;
	 * - Wildcard imports: import java.util.*;
	 *
	 * @param context transformation context with source code
	 * @return list of extracted import declarations
	 * @throws NullPointerException if {@code context} is {@code null}
	 */
	public static List<ImportDeclaration> extract(TransformationContext context)
	{
		requireThat(context, "context").isNotNull();

		List<ImportDeclaration> imports = new ArrayList<>();
		String source = context.sourceCode();

		Matcher matcher = IMPORT_PATTERN.matcher(source);

		while (matcher.find())
		{
			context.checkDeadline();

			int startPosition = matcher.start();
			int endPosition = matcher.end() - 1; // -1 to exclude the newline if present

			boolean isStatic = matcher.group(2) != null;
			String qualifiedName = matcher.group(3);

			// Get the line number from the context
			int lineNumber = context.getLineNumber(startPosition);

			ImportDeclaration importDecl = new ImportDeclaration(
				qualifiedName,
				isStatic,
				startPosition,
				endPosition,
				lineNumber);

			imports.add(importDecl);
		}

		return imports;
	}

	/**
	 * Finds the end of the import section in the source code.
	 * Returns the position after the last import declaration.
	 *
	 * @param source the source code
	 * @return position after the import section, or 0 if no imports found
	 */
	static int findImportSectionEnd(String source)
	{
		requireThat(source, "source").isNotNull();

		Matcher matcher = IMPORT_PATTERN.matcher(source);
		int endPosition = 0;

		while (matcher.find())
		{
			endPosition = matcher.end();
		}

		return endPosition;
	}
}
