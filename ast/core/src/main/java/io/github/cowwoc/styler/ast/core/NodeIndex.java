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
	 * Sentinel value representing an invalid/null node reference.
	 */
	public static final NodeIndex NULL = new NodeIndex(-1);

	/**
	 * Creates a new node index.
	 *
	 * @param index the zero-based index of the node in the arena
	 * @throws IllegalArgumentException if {@code index} is less than -1
	 */
	public NodeIndex
	{
		requireThat(index, "index").isGreaterThanOrEqualTo(-1);
	}

	/**
	 * Checks if this index represents a valid node reference.
	 *
	 * @return true if this is a valid node reference, false if it's NULL
	 */
	public boolean isValid()
	{
		return index >= 0;
	}

	@Override
	public String toString()
	{
		if (isValid())
		{
			return "NodeIndex[" + index + "]";
		}
		return "NodeIndex[NULL]";
	}
}
