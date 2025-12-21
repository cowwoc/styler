package io.github.cowwoc.styler.ast.core;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;

/**
 * Semantic attribute for type declaration nodes (class, interface, enum, record, annotation).
 * <p>
 * Contains the simple type name for direct access without token traversal. The name's position
 * can be derived from the node's identifier token when needed.
 * <p>
 * <b>Thread-safety</b>: This record is immutable and thread-safe.
 *
 * @param typeName the simple type name (e.g., {@code "NodeArena"})
 */
public record TypeDeclarationAttribute(String typeName) implements NodeAttribute
{
	/**
	 * Creates a type declaration attribute.
	 *
	 * @param typeName the simple type name
	 * @throws NullPointerException     if {@code typeName} is null
	 * @throws IllegalArgumentException if {@code typeName} is blank
	 */
	public TypeDeclarationAttribute
	{
		requireThat(typeName, "typeName").isNotBlank();
	}
}
