package io.github.cowwoc.styler.ast.core;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;

/**
 * Semantic attribute for import declaration nodes.
 * <p>
 * Contains the fully qualified name of the imported type or package and whether this is a static import.
 * This eliminates the need for formatters to parse import statement strings at runtime.
 * <p>
 * <b>Thread-safety</b>: This record is immutable and thread-safe.
 *
 * @param qualifiedName the fully qualified name (e.g., {@code "java.util.List"} or
 *                      {@code "java.util.*"} for wildcards)
 * @param isStatic      {@code true} if this is a static import, {@code false} for regular imports
 */
public record ImportAttribute(String qualifiedName, boolean isStatic) implements NodeAttribute
{
	/**
	 * Creates an import attribute with the specified qualified name and static flag.
	 *
	 * @param qualifiedName the fully qualified name
	 * @param isStatic      {@code true} if this is a static import
	 * @throws NullPointerException     if {@code qualifiedName} is null
	 * @throws IllegalArgumentException if {@code qualifiedName} is empty
	 */
	public ImportAttribute
	{
		requireThat(qualifiedName, "qualifiedName").isNotBlank();
	}
}
