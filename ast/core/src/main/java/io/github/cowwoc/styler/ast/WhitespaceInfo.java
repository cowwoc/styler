package io.github.cowwoc.styler.ast;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;

/**
 * Represents whitespace information for AST nodes to preserve formatting context.
 * This immutable record captures spacing patterns that guide formatting decisions.
 *
 * @param leadingSpaces number of spaces before the node
 * @param trailingSpaces number of spaces after the node
 * @param blankLinesBefore number of blank lines preceding the node
 * @param blankLinesAfter number of blank lines following the node
 * @param preserveOriginal whether to preserve the original whitespace exactly
 */
public record WhitespaceInfo(int leadingSpaces, int trailingSpaces, int blankLinesBefore, int blankLinesAfter,
	boolean preserveOriginal) {
	public WhitespaceInfo {
		requireThat(leadingSpaces, "leadingSpaces").isNotNegative();
		requireThat(trailingSpaces, "trailingSpaces").isNotNegative();
		requireThat(blankLinesBefore, "blankLinesBefore").isNotNegative();
		requireThat(blankLinesAfter, "blankLinesAfter").isNotNegative();
	}

	/**
	 * Creates whitespace info with no spacing.
	 * @return a WhitespaceInfo with all spacing values set to zero
	 */
	public static WhitespaceInfo none() {
		return new WhitespaceInfo(0, 0, 0, 0, false);
	}

	/**
	 * Creates whitespace info with standard single space.
	 * @return a WhitespaceInfo with single trailing space
	 */
	public static WhitespaceInfo singleSpace() {
		return new WhitespaceInfo(0, 1, 0, 0, false);
	}

	/**
	 * Creates whitespace info with single blank line before.
	 * @return a WhitespaceInfo with one blank line before
	 */
	public static WhitespaceInfo blankLineBefore() {
		return new WhitespaceInfo(0, 0, 1, 0, false);
	}

	/**
	 * Creates a copy with preserve original formatting enabled.
	 * @return a new WhitespaceInfo with preserveOriginal set to true
	 */
	public WhitespaceInfo withPreserveOriginal() {
		return new WhitespaceInfo(leadingSpaces, trailingSpaces, blankLinesBefore, blankLinesAfter, true);
	}

	/**
	 * Creates a copy with modified leading spaces.
	 * @param spaces the new leading space count
	 * @return a new WhitespaceInfo with updated leading spaces
	 */
	public WhitespaceInfo withLeadingSpaces(int spaces) {
		requireThat(spaces, "spaces").isNotNegative();
		return new WhitespaceInfo(spaces, trailingSpaces, blankLinesBefore, blankLinesAfter, preserveOriginal);
	}

	/**
	 * Creates a copy with modified blank lines before.
	 * @param lines the new blank line count
	 * @return a new WhitespaceInfo with updated blank lines before
	 */
	public WhitespaceInfo withBlankLinesBefore(int lines) {
		requireThat(lines, "lines").isNotNegative();
		return new WhitespaceInfo(leadingSpaces, trailingSpaces, lines, blankLinesAfter, preserveOriginal);
	}

	/**
	 * Checks if this whitespace info represents any non-zero spacing.
	 * @return true if any spacing value is greater than zero
	 */
	public boolean hasSpacing() {
		return leadingSpaces > 0 || trailingSpaces > 0 || blankLinesBefore > 0 || blankLinesAfter > 0;
	}
}
