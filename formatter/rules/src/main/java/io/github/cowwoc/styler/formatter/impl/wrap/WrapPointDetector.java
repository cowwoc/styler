package io.github.cowwoc.styler.formatter.impl.wrap;

import io.github.cowwoc.styler.ast.ASTNode;
import io.github.cowwoc.styler.ast.SourcePosition;
import io.github.cowwoc.styler.ast.SourceRange;
import io.github.cowwoc.styler.ast.node.BinaryExpressionNode;
import io.github.cowwoc.styler.ast.node.MethodCallNode;
import io.github.cowwoc.styler.formatter.api.WrapConfiguration;
import io.github.cowwoc.styler.formatter.impl.SourceTextUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;

/**
 * Detects semantically meaningful wrap points in source code for line wrapping.
 * <p>
 * This class uses AST analysis to identify locations where lines can be wrapped
 * to improve readability. Wrap points are prioritized based on Java language
 * semantics, with method chains receiving highest priority, followed by parameters,
 * operators, and general whitespace.
 * <p>
 * Per <a href="https://docs.oracle.com/javase/specs/jls/se25/html/jls-15.html">JLS Chapter 15: Expressions</a>,
 * method invocations and binary expressions have well-defined precedence and
 * associativity that guides optimal wrap point selection.
 * <p>
 * This class is stateless and thread-safe when used with immutable parameters.
 */
public final class WrapPointDetector
{
	private final ASTNode rootNode;
	private final String sourceText;
	private final WrapConfiguration config;

	/**
	 * Creates a new wrap point detector.
	 *
	 * @param rootNode the root of the AST to analyze, never {@code null}
	 * @param sourceText the source code text, never {@code null}
	 * @param config the wrap configuration, never {@code null}
	 * @throws NullPointerException if any parameter is {@code null}
	 */
	public WrapPointDetector(ASTNode rootNode, String sourceText, WrapConfiguration config)
	{
		requireThat(rootNode, "rootNode").isNotNull();
		requireThat(sourceText, "sourceText").isNotNull();
		requireThat(config, "config").isNotNull();

		this.rootNode = rootNode;
		this.sourceText = sourceText;
		this.config = config;
	}

	/**
	 * Finds all potential wrap points within a source range.
	 * <p>
	 * This method analyzes the AST nodes within the specified range and identifies
	 * locations where line wraps would maintain semantic meaning. Wrap points
	 * are sorted by priority (highest first) and then by position.
	 *
	 * @param range the source range to analyze, never {@code null}
	 * @return list of wrap points sorted by priority, never {@code null}
	 * @throws NullPointerException if {@code range} is {@code null}
	 */
	public List<WrapPoint> findWrapPoints(SourceRange range)
	{
		requireThat(range, "range").isNotNull();

		List<WrapPoint> wrapPoints = new ArrayList<>();

		findWrapPointsInNode(rootNode, range, wrapPoints);

		findWhitespaceWrapPoints(range, wrapPoints);

		Collections.sort(wrapPoints);
		return wrapPoints;
	}

	/**
	 * Recursively finds wrap points in an AST node and its children.
	 *
	 * @param node the AST node to analyze, never {@code null}
	 * @param lineRange the line range we're interested in, never {@code null}
	 * @param wrapPoints the list to accumulate wrap points, never {@code null}
	 */
	private void findWrapPointsInNode(ASTNode node, SourceRange lineRange, List<WrapPoint> wrapPoints)
	{
		if (!overlaps(node.getRange(), lineRange))
		{
			return;
		}

		if (node instanceof MethodCallNode)
		{
			addMethodCallWrapPoint(node, lineRange, wrapPoints);
		}
		else if (node instanceof BinaryExpressionNode binaryNode)
		{
			addBinaryExpressionWrapPoint(binaryNode, lineRange, wrapPoints);
		}

		for (ASTNode child : node.getChildren())
		{
			findWrapPointsInNode(child, lineRange, wrapPoints);
		}
	}

	/**
	 * Adds a wrap point for a method call node.
	 * <p>
	 * Method call wrap points receive the highest priority as wrapping at
	 * method chain boundaries typically produces the most readable code.
	 * <p>
	 * The wrap position is determined by the {@link WrapConfiguration#isWrapBeforeDot()}
	 * setting.
	 *
	 * @param node the method call node, never {@code null}
	 * @param lineRange the line range, never {@code null}
	 * @param wrapPoints the list to add to, never {@code null}
	 */
	private void addMethodCallWrapPoint(ASTNode node, SourceRange lineRange, List<WrapPoint> wrapPoints)
	{
		SourcePosition position = node.getStartPosition();
		if (isWithinRange(position, lineRange) && position.column() > 1)
		{
			WrapPoint wrapPoint = new WrapPoint(position, WrapPoint.Priority.METHOD_CHAIN,
				"method call at " + position);
			wrapPoints.add(wrapPoint);
		}
	}

	/**
	 * Adds a wrap point for a binary expression node.
	 * <p>
	 * Binary expression wrap points are added based on the configured preference
	 * for wrapping before or after operators. Operators receive medium priority.
	 * <p>
	 * The wrap position is determined by the {@link WrapConfiguration#isWrapBeforeOperator()}
	 * setting.
	 *
	 * @param node the binary expression node, never {@code null}
	 * @param lineRange the line range, never {@code null}
	 * @param wrapPoints the list to add to, never {@code null}
	 */
	private void addBinaryExpressionWrapPoint(BinaryExpressionNode node, SourceRange lineRange,
		List<WrapPoint> wrapPoints)
	{
		SourcePosition wrapPosition;
		if (config.isWrapBeforeOperator())
		{
			wrapPosition = node.getRange().start();
		}
		else
		{
			wrapPosition = node.getLeft().getRange().end();
		}

		if (isWithinRange(wrapPosition, lineRange))
		{
			String operatorContext = String.format("operator '%s' at %s",
				node.getOperator(), wrapPosition);
			WrapPoint wrapPoint = new WrapPoint(wrapPosition,
				WrapPoint.Priority.OPERATOR, operatorContext);
			wrapPoints.add(wrapPoint);
		}
	}

	/**
	 * Finds whitespace-based wrap points as fallback options.
	 * <p>
	 * When no semantic wrap points are available, wrapping at whitespace
	 * boundaries is still preferable to mid-identifier wraps.
	 *
	 * @param lineRange the line range to analyze, never {@code null}
	 * @param wrapPoints the list to add to, never {@code null}
	 */
	private void findWhitespaceWrapPoints(SourceRange lineRange, List<WrapPoint> wrapPoints)
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
						WrapPoint wrapPoint = new WrapPoint(position,
							WrapPoint.Priority.WHITESPACE, "whitespace at " + position);
						wrapPoints.add(wrapPoint);
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
