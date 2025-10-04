package io.github.cowwoc.styler.formatter.impl;

import io.github.cowwoc.styler.ast.SourcePosition;
import io.github.cowwoc.styler.ast.SourceRange;
import io.github.cowwoc.styler.formatter.api.TextEdit;

import java.util.ArrayList;
import java.util.List;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;

/**
 * Generates text edits for line wrapping based on detected break points.
 * <p>
 * This class implements wrapping strategies for different code elements including
 * method chains, parameter lists, and binary expressions. It ensures proper
 * indentation is maintained and comments are preserved during wrapping operations.
 * <p>
 * Per <a href="https://docs.oracle.com/javase/specs/jls/se25/html/jls-3.html#jls-3.10">JLS §3.10</a>,
 * line wrapping must not split string literals, character literals, or other lexical tokens.
 */
final class WrapStrategy
{
	private static final String RULE_ID = "io.github.cowwoc.styler.rules.LineLength";
	private static final String NEWLINE = "\n";

	private final IndentationCalculator indentationCalculator;

	/**
	 * Creates a new wrap strategy with the specified configuration.
	 *
	 * @param config the line length configuration, never {@code null}
	 * @throws NullPointerException if {@code config} is {@code null}
	 */
	WrapStrategy(LineLengthConfiguration config)
	{
		requireThat(config, "config").isNotNull();

		this.indentationCalculator = new IndentationCalculator(config.getTabWidth());
	}

	/**
	 * Creates a text edit to wrap a line at the specified break point.
	 * <p>
	 * This method generates a {@code TextEdit} that inserts a newline and appropriate
	 * indentation at the break point position. The indentation level is calculated
	 * based on the current line's indentation plus continuation indent.
	 *
	 * @param breakPoint the break point where wrapping should occur, never {@code null}
	 * @param sourceText the source code text, never {@code null}
	 * @return a text edit for wrapping at the break point, never {@code null}
	 * @throws NullPointerException if any parameter is {@code null}
	 */
	TextEdit createWrapEdit(BreakPoint breakPoint, String sourceText)
	{
		requireThat(breakPoint, "breakPoint").isNotNull();
		requireThat(sourceText, "sourceText").isNotNull();

		SourcePosition position = breakPoint.getPosition();
		String line = SourceTextUtil.extractLine(sourceText, position.line());
		int baseIndentation = indentationCalculator.calculateIndentationLevel(line);

		String replacement = createWrapWithContinuationIndent(baseIndentation);

		SourceRange range = new SourceRange(position, position);
		return TextEdit.create(range, replacement, RULE_ID);
	}

	/**
	 * Creates a list of text edits for wrapping a line at multiple break points.
	 * <p>
	 * When a line exceeds the maximum length significantly, multiple break points
	 * may be needed. This method generates edits for each break point while
	 * maintaining proper indentation hierarchy.
	 *
	 * @param breakPoints list of break points sorted by priority, never {@code null}
	 * @param sourceText the source code text, never {@code null}
	 * @param maxLineLength the maximum allowed line length in characters
	 * @return list of text edits for wrapping, never {@code null}
	 * @throws NullPointerException if {@code breakPoints} or {@code sourceText} is {@code null}
	 */
	List<TextEdit> createMultipleWrapEdits(List<BreakPoint> breakPoints, String sourceText,
	                                        int maxLineLength)
	{
		requireThat(breakPoints, "breakPoints").isNotNull();
		requireThat(sourceText, "sourceText").isNotNull();
		requireThat(maxLineLength, "maxLineLength").isPositive();

		List<TextEdit> edits = new ArrayList<>();

		for (BreakPoint breakPoint : breakPoints)
		{
			TextEdit edit = createWrapEdit(breakPoint, sourceText);
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
	 * All break point types use the same wrapping strategy: insert a newline followed by
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
	 * Returns the indentation calculator used by this strategy.
	 *
	 * @return the indentation calculator, never {@code null}
	 */
	IndentationCalculator getIndentationCalculator()
	{
		return indentationCalculator;
	}
}
