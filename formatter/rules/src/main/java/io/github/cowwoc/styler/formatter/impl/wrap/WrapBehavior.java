package io.github.cowwoc.styler.formatter.impl.wrap;

import io.github.cowwoc.styler.ast.ASTNode;
import io.github.cowwoc.styler.ast.SourcePosition;
import io.github.cowwoc.styler.ast.SourceRange;
import io.github.cowwoc.styler.formatter.api.TextEdit;
import io.github.cowwoc.styler.formatter.api.WrapConfiguration;
import io.github.cowwoc.styler.formatter.impl.IndentationCalculator;
import io.github.cowwoc.styler.formatter.impl.SourceTextUtil;

import java.util.ArrayList;
import java.util.List;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;

/**
 * Shared line wrapping logic for formatting rules.
 * <p>
 * This class extracts common wrapping behavior from individual rules, providing
 * a centralized implementation for generating text edits based on detected wrap
 * points. It handles indentation calculation, operator positioning, and line
 * break generation according to the provided {@link WrapConfiguration}.
 * <p>
 * This class is stateless and can be safely reused across multiple formatting
 * operations.
 */
public final class WrapBehavior
{
	private static final String RULE_ID = "io.github.cowwoc.styler.rules.LineLength";
	private static final String NEWLINE = "\n";

	private final WrapConfiguration config;
	private final IndentationCalculator indentationCalculator;

	/**
	 * Creates a new wrap behavior with the specified configuration.
	 *
	 * @param config the wrap configuration, never {@code null}
	 * @throws NullPointerException if {@code null}
	 */
	public WrapBehavior(WrapConfiguration config)
	{
		requireThat(config, "config").isNotNull();

		this.config = config;
		this.indentationCalculator = new IndentationCalculator(config.getTabWidth(),
			config.getContinuationIndentSpaces());
	}

	/**
	 * Finds all potential wrap points within a source range.
	 * <p>
	 * This method analyzes the AST nodes within the specified range and identifies
	 * locations where line wraps would maintain semantic meaning. Wrap points
	 * are sorted by priority (highest first) and then by position.
	 *
	 * @param range the source range to analyze, never {@code null}
	 * @param rootNode the root AST node to analyze, never {@code null}
	 * @param sourceText the source code text, never {@code null}
	 * @return list of wrap points sorted by priority, never {@code null}
	 * @throws NullPointerException if any parameter is {@code null}
	 */
	public List<WrapPoint> findWrapPoints(SourceRange range, ASTNode rootNode, String sourceText)
	{
		requireThat(range, "range").isNotNull();
		requireThat(rootNode, "rootNode").isNotNull();
		requireThat(sourceText, "sourceText").isNotNull();

		WrapPointDetector detector = new WrapPointDetector(rootNode, sourceText, config);
		return detector.findWrapPoints(range);
	}

	/**
	 * Creates a text edit to wrap a line at the specified wrap point.
	 * <p>
	 * This method generates a {@code TextEdit} that inserts a newline and appropriate
	 * indentation at the wrap point position. The indentation level is calculated
	 * based on the current line's indentation plus continuation indent.
	 *
	 * @param wrapPoint the wrap point where wrapping should occur, never {@code null}
	 * @param sourceText the source code text, never {@code null}
	 * @return a text edit for wrapping at the wrap point, never {@code null}
	 * @throws NullPointerException if any parameter is {@code null}
	 */
	public TextEdit createWrapEdit(WrapPoint wrapPoint, String sourceText)
	{
		requireThat(wrapPoint, "wrapPoint").isNotNull();
		requireThat(sourceText, "sourceText").isNotNull();

		SourcePosition position = wrapPoint.getPosition();
		String line = SourceTextUtil.extractLine(sourceText, position.line());
		int baseIndentation = indentationCalculator.calculateIndentationLevel(line);

		String replacement = createWrapWithContinuationIndent(baseIndentation);

		SourceRange range = new SourceRange(position, position);
		return TextEdit.create(range, replacement, RULE_ID);
	}

	/**
	 * Creates a list of text edits for wrapping a line at multiple wrap points.
	 * <p>
	 * When a line exceeds the maximum length significantly, multiple wrap points
	 * may be needed. This method generates edits for each wrap point while
	 * maintaining proper indentation hierarchy.
	 *
	 * @param wrapPoints list of wrap points sorted by priority, never {@code null}
	 * @param sourceText the source code text, never {@code null}
	 * @param maxLineLength the maximum allowed line length in characters
	 * @return list of text edits for wrapping, never {@code null}
	 * @throws NullPointerException if {@code wrapPoints} or {@code sourceText} is {@code null}
	 */
	public List<TextEdit> createMultipleWrapEdits(List<WrapPoint> wrapPoints, String sourceText, int maxLineLength)
	{
		requireThat(wrapPoints, "wrapPoints").isNotNull();
		requireThat(sourceText, "sourceText").isNotNull();
		requireThat(maxLineLength, "maxLineLength").isPositive();

		List<TextEdit> edits = new ArrayList<>();

		for (WrapPoint wrapPoint : wrapPoints)
		{
			TextEdit edit = createWrapEdit(wrapPoint, sourceText);
			edits.add(edit);

			if (edits.size() >= 3)
			{
				break;
			}
		}

		return edits;
	}

	/**
	 * Creates wrapping replacement text with continuation indentation.
	 * <p>
	 * All wrap point types use the same wrapping strategy: insert a newline followed by
	 * continuation indentation. This provides consistent visual indication that code
	 * continues on the next line.
	 *
	 * @param baseIndentation the base indentation level in spaces
	 * @return replacement text with newline and indentation, never {@code null}
	 */
	private String createWrapWithContinuationIndent(int baseIndentation)
	{
		String indentation = indentationCalculator.
			generateContinuationIndentation(baseIndentation);
		return NEWLINE + indentation;
	}

	/**
	 * Returns the indentation calculator used by this behavior.
	 *
	 * @return the indentation calculator, never {@code null}
	 */
	public IndentationCalculator getIndentationCalculator()
	{
		return indentationCalculator;
	}
}
