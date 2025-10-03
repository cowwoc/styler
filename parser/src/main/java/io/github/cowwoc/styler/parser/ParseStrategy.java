package io.github.cowwoc.styler.parser;

/**
 * Strategy interface for parsing Java language constructs.
 * Enables pluggable parsing support for different Java versions and language features.
 *
 * This pattern allows the parser to be extended for future Java versions without
 * massive refactoring of the core parser logic.
 */
public interface ParseStrategy
{
	/**
	 * Determines if this strategy can handle parsing the given construct
	 * at the current parse context position.
	 *
	 * @param version The target Java version being parsed
	 * @param context The current parsing context
	 * @return {@code true} if this strategy can handle the current construct
	 */
	boolean canHandle(JavaVersion version, ParseContext context);

	/**
	 * Parses the language construct and returns the node ID.
	 * This method should only be called if canHandle() returns {@code true}.
	 *
	 * @param context The parsing context
	 * @return The ID of the created AST node
	 */
	int parseConstruct(ParseContext context);

	/**
	 * Gets the priority of this strategy when multiple strategies can handle the same construct.
	 * Higher priority strategies are tried first.
	 *
	 * @return The priority level (higher = more priority)
	 */
	default int getPriority()
{
		return 0;
	}

	/**
	 * Gets a description of what this strategy handles for debugging and documentation.
	 *
	 * @return A human-readable description
	 */
	String getDescription();
}