package io.github.cowwoc.styler.ast.core;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;

/**
 * Semantic attribute for package declaration nodes.
 * <p>
 * Contains the package name extracted during parsing. This eliminates the need for formatters
 * to parse package statement strings at runtime.
 * <p>
 * <b>Thread-safety</b>: This record is immutable and thread-safe.
 *
 * @param packageName the package name (e.g., {@code "io.github.cowwoc.styler.ast.core"})
 */
public record PackageAttribute(String packageName) implements NodeAttribute
{
	/**
	 * Creates a package attribute with the specified package name.
	 *
	 * @param packageName the package name
	 * @throws NullPointerException     if {@code packageName} is null
	 * @throws IllegalArgumentException if {@code packageName} is blank
	 */
	public PackageAttribute
	{
		requireThat(packageName, "packageName").isNotBlank();
	}
}
