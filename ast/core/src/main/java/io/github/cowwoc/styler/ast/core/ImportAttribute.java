package io.github.cowwoc.styler.ast.core;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;

/**
 * Semantic attribute for import declaration nodes.
 * <p>
 * Contains the fully qualified name of the imported type or package. This eliminates the need
 * for formatters to parse import statement strings at runtime.
 * <p>
 * <b>Thread-safety</b>: This record is immutable and thread-safe.
 *
 * @param qualifiedName the fully qualified name (e.g., {@code "java.util.List"} or
 *                      {@code "java.util.*"} for wildcards)
 */
public record ImportAttribute(String qualifiedName) implements NodeAttribute
{
	/**
	 * Creates an import attribute with the specified qualified name.
	 *
	 * @param qualifiedName the fully qualified name
	 * @throws NullPointerException     if {@code qualifiedName} is null
	 * @throws IllegalArgumentException if {@code qualifiedName} is blank
	 */
	public ImportAttribute
	{
		requireThat(qualifiedName, "qualifiedName").isNotBlank();
	}
}
