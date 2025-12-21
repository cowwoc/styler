package io.github.cowwoc.styler.ast.core;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;

/**
 * A reference to a node in the Arena, represented as an index.
 * This is an immutable value class that serves as a lightweight pointer to AST nodes.
 *
 * @param index the zero-based index of the node in the arena
 */
public record NodeIndex(int index)
{
	/**
	 * Creates a new node index.
	 *
	 * @param index the zero-based index of the node in the arena
	 * @throws IllegalArgumentException if {@code index} is negative
	 */
	public NodeIndex
	{
		requireThat(index, "index").isNotNegative();
	}
}
