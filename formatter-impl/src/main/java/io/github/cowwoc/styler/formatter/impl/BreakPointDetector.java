package io.github.cowwoc.styler.formatter.impl;

import io.github.cowwoc.styler.ast.ASTNode;
import io.github.cowwoc.styler.ast.SourcePosition;
import io.github.cowwoc.styler.ast.SourceRange;
import io.github.cowwoc.styler.ast.node.BinaryExpressionNode;
import io.github.cowwoc.styler.ast.node.MethodCallNode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;

/**
 * Detects semantically meaningful break points in source code for line wrapping.
 * <p>
 * This class uses AST analysis to identify locations where lines can be broken
 * to improve readability. Break points are prioritized based on Java language
 * semantics, with method chains receiving highest priority, followed by parameters,
 * operators, and general whitespace.
 * <p>
 * Per <a href="https://docs.oracle.com/javase/specs/jls/se25/html/jls-15.html">JLS Chapter 15: Expressions</a>,
 * method invocations and binary expressions have well-defined precedence and
 * associativity that guides optimal break point selection.
 */
final class BreakPointDetector
{
	private final ASTNode rootNode;
	private final String sourceText;
	private final LineLengthConfiguration config;

	/**
	 * Creates a new break point detector.
	 *
	 * @param rootNode the root of the AST to analyze, never {@code null}
	 * @param sourceText the source code text, never {@code null}
	 * @param config the line length configuration, never {@code null}
	 * @throws NullPointerException if any parameter is {@code null}
	 */
	BreakPointDetector(ASTNode rootNode, String sourceText, LineLengthConfiguration config)
	{
		requireThat(rootNode, "rootNode").isNotNull();
		requireThat(sourceText, "sourceText").isNotNull();
		requireThat(config, "config").isNotNull();

		this.rootNode = rootNode;
		this.sourceText = sourceText;
		this.config = config;
	}

	/**
	 * Finds all potential break points within a source range.
	 * <p>
	 * This method analyzes the AST nodes within the specified range and identifies
	 * locations where line breaks would maintain semantic meaning. Break points
	 * are sorted by priority (highest first) and then by position.
	 *
	 * @param range the source range to analyze, never {@code null}
	 * @return list of break points sorted by priority, never {@code null}
	 * @throws NullPointerException if {@code range} is {@code null}
	 */
	List<BreakPoint> findBreakPoints(SourceRange range)
	{
		requireThat(range, "range").isNotNull();

		List<BreakPoint> breakPoints = new ArrayList<>();

		findBreakPointsInNode(rootNode, range, breakPoints);

		findWhitespaceBreakPoints(range, breakPoints);

		Collections.sort(breakPoints);
		return breakPoints;
	}

	/**
	 * Recursively finds break points in an AST node and its children.
	 *
	 * @param node the AST node to analyze, never {@code null}
	 * @param lineRange the line range we're interested in, never {@code null}
	 * @param breakPoints the list to accumulate break points, never {@code null}
	 */
	private void findBreakPointsInNode(ASTNode node, SourceRange lineRange,
	                                    List<BreakPoint> breakPoints)
	{
		if (!overlaps(node.getRange(), lineRange))
		{
			return;
		}

		if (node instanceof MethodCallNode)
		{
			addMethodCallBreakPoint(node, lineRange, breakPoints);
		}
		else if (node instanceof BinaryExpressionNode binaryNode)
		{
			addBinaryExpressionBreakPoint(binaryNode, lineRange, breakPoints);
		}

		for (ASTNode child : node.getChildren())
		{
			findBreakPointsInNode(child, lineRange, breakPoints);
		}
	}

	/**
	 * Adds a break point for a method call node.
	 * <p>
	 * Method call break points receive the highest priority as breaking at
	 * method chain boundaries typically produces the most readable code.
	 *
	 * @param node the method call node, never {@code null}
	 * @param lineRange the line range, never {@code null}
	 * @param breakPoints the list to add to, never {@code null}
	 */
	private void addMethodCallBreakPoint(ASTNode node, SourceRange lineRange,
	                                      List<BreakPoint> breakPoints)
	{
		SourcePosition position = node.getStartPosition();
		if (isWithinRange(position, lineRange) && position.column() > 1)
		{
			BreakPoint breakPoint = new BreakPoint(position,
				BreakPoint.Priority.METHOD_CHAIN, "method call at " + position);
			breakPoints.add(breakPoint);
		}
	}

	/**
	 * Adds a break point for a binary expression node.
	 * <p>
	 * Binary expression break points are added based on the configured preference
	 * for breaking before or after operators. Operators receive medium priority.
	 *
	 * @param node the binary expression node, never {@code null}
	 * @param lineRange the line range, never {@code null}
	 * @param breakPoints the list to add to, never {@code null}
	 */
	private void addBinaryExpressionBreakPoint(BinaryExpressionNode node, SourceRange lineRange,
	                                            List<BreakPoint> breakPoints)
	{
		SourcePosition breakPosition;
		if (config.isBreakBeforeOperator())
		{
			breakPosition = node.getRange().start();
		}
		else
		{
			breakPosition = node.getLeft().getRange().end();
		}

		if (isWithinRange(breakPosition, lineRange))
		{
			String operatorContext = String.format("operator '%s' at %s",
				node.getOperator(), breakPosition);
			BreakPoint breakPoint = new BreakPoint(breakPosition,
				BreakPoint.Priority.OPERATOR, operatorContext);
			breakPoints.add(breakPoint);
		}
	}

	/**
	 * Finds whitespace-based break points as fallback options.
	 * <p>
	 * When no semantic break points are available, breaking at whitespace
	 * boundaries is still preferable to mid-identifier breaks.
	 *
	 * @param lineRange the line range to analyze, never {@code null}
	 * @param breakPoints the list to add to, never {@code null}
	 */
	private void findWhitespaceBreakPoints(SourceRange lineRange, List<BreakPoint> breakPoints)
	{
		int startLine = lineRange.start().line();
		int endLine = lineRange.end().line();

		for (int line = startLine; line <= endLine; line += 1)
		{
			String lineText = SourceTextUtil.extractLine(sourceText, line);
			if (lineText.isEmpty())
			{
				continue;
			}

			for (int col = 1; col <= lineText.length(); col += 1)
			{
				if (col > 1 && Character.isWhitespace(lineText.charAt(col - 1)))
				{
					SourcePosition position = new SourcePosition(line, col);
					if (isWithinRange(position, lineRange))
					{
						BreakPoint breakPoint = new BreakPoint(position,
							BreakPoint.Priority.WHITESPACE, "whitespace at " + position);
						breakPoints.add(breakPoint);
					}
				}
			}
		}
	}

	/**
	 * Checks if a position is within a source range.
	 *
	 * @param position the position to check, never {@code null}
	 * @param range the range to check against, never {@code null}
	 * @return {@code true} if the position is within the range
	 */
	private boolean isWithinRange(SourcePosition position, SourceRange range)
	{
		return range.contains(position);
	}

	/**
	 * Checks if two source ranges overlap.
	 *
	 * @param range1 the first range, never {@code null}
	 * @param range2 the second range, never {@code null}
	 * @return {@code true} if the ranges overlap
	 */
	private boolean overlaps(SourceRange range1, SourceRange range2)
	{
		return !(range1.end().compareTo(range2.start()) <= 0 ||
		         range2.end().compareTo(range1.start()) <= 0);
	}
}
