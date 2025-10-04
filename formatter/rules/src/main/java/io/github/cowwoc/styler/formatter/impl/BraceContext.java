package io.github.cowwoc.styler.formatter.impl;

import io.github.cowwoc.styler.ast.ASTNode;
import io.github.cowwoc.styler.ast.SourceRange;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;

/**
 * Immutable context object containing brace position information for an AST node.
 *
 * <p>This record captures the location of opening and closing braces for Java constructs including classes,
 * methods, control structures, and lambda expressions. Used by brace formatting analysis to detect style
 * violations.
 *
 * <p><strong>Thread Safety:</strong> This class is immutable and thread-safe.
 *
 * @param node the AST node containing braces, never {@code null}
 * @param category the categorization of this node for style application, never {@code null}
 * @param openingBraceRange the source range of the opening brace character, never {@code null}
 * @param closingBraceRange the source range of the closing brace character, never {@code null}
 * @param isEmpty whether this is an empty block (no statements)
 */
public record BraceContext(
	ASTNode node,
	NodeCategory category,
	SourceRange openingBraceRange,
	SourceRange closingBraceRange,
	boolean isEmpty)
{
	/**
	 * Compact constructor with validation.
	 *
	 * @throws NullPointerException if any parameter except {@code isEmpty} is {@code null}
	 */
	public BraceContext
	{
		requireThat(node, "node").isNotNull();
		requireThat(category, "category").isNotNull();
		requireThat(openingBraceRange, "openingBraceRange").isNotNull();
		requireThat(closingBraceRange, "closingBraceRange").isNotNull();
	}

	/**
	 * Returns the line number of the opening brace.
	 *
	 * @return the opening brace line number, always positive
	 */
	public int getOpeningBraceLine()
	{
		return openingBraceRange.start().line();
	}

	/**
	 * Returns the line number of the closing brace.
	 *
	 * @return the closing brace line number, always positive
	 */
	public int getClosingBraceLine()
	{
		return closingBraceRange.start().line();
	}

	/**
	 * Returns whether opening and closing braces are on the same line.
	 *
	 * @return {@code true} if braces on same line, {@code false} otherwise
	 */
	public boolean isSingleLineBraces()
	{
		return getOpeningBraceLine() == getClosingBraceLine();
	}
}
