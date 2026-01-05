package io.github.cowwoc.styler.formatter.importorg.internal;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;

/**
 * Represents a single import declaration extracted from source code.
 * <p>
 * <b>Thread-safety</b>: This record is immutable and thread-safe.
 *
 * @param qualifiedName fully qualified name (e.g., {@code "java.util.List"}, {@code "java.util.*"} for
 *                      wildcards, or module name like {@code "java.base"} for module imports)
 * @param isStatic      whether this is a static import
 * @param isModule      whether this is a module import (JEP 511)
 * @param startPosition 0-based character offset from start of source file to "import" keyword
 * @param endPosition   0-based character offset from start of source file to semicolon (inclusive)
 * @param lineNumber    1-based line number where import appears
 * @throws NullPointerException     if {@code qualifiedName} is {@code null}
 * @throws IllegalArgumentException if {@code qualifiedName} is empty, positions are invalid,
 *                                  or both {@code isStatic} and {@code isModule} are true
 */
public record ImportDeclaration(
	String qualifiedName,
	boolean isStatic,
	boolean isModule,
	int startPosition,
	int endPosition,
	int lineNumber)
{
	/**
	 * Compact constructor for validation.
	 *
	 * @param qualifiedName the fully qualified name or module name
	 * @param isStatic      whether this is a static import
	 * @param isModule      whether this is a module import (JEP 511)
	 * @param startPosition the start position in source
	 * @param endPosition   the end position in source
	 * @param lineNumber    the 1-based line number
	 */
	public ImportDeclaration
	{
		requireThat(qualifiedName, "qualifiedName").isNotBlank();
		requireThat(startPosition, "startPosition").isNotNegative();
		requireThat(endPosition, "endPosition").isGreaterThanOrEqualTo(startPosition);
		requireThat(lineNumber, "lineNumber").isPositive();
		// Module imports cannot be static
		if (isStatic && isModule)
			throw new IllegalArgumentException("Import cannot be both static and module");
	}

	/**
	 * Returns whether this is a wildcard import (ends with .*).
	 *
	 * @return true if this import ends with ".*"
	 */
	public boolean isWildcard()
	{
		return qualifiedName.endsWith(".*");
	}

	/**
	 * Returns the simple name (last component) of this import.
	 * For wildcard imports, returns "*".
	 * For "java.util.List", returns "List".
	 *
	 * @return the simple name of the imported type/package
	 */
	public String simpleName()
	{
		if (isWildcard())
			return "*";

		int lastDot = qualifiedName.lastIndexOf('.');
		if (lastDot < 0)
			return qualifiedName;

		return qualifiedName.substring(lastDot + 1);
	}

	/**
	 * Returns the package portion of this import.
	 * For "java.util.List", returns "java.util".
	 * For "java", returns "" (empty).
	 *
	 * @return the package name portion
	 */
	public String packageName()
	{
		int lastDot = qualifiedName.lastIndexOf('.');
		if (lastDot < 0)
			return "";

		return qualifiedName.substring(0, lastDot);
	}
}
