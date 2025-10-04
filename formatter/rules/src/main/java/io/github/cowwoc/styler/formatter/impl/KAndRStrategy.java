package io.github.cowwoc.styler.formatter.impl;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;

/**
 * Brace style strategy implementing K&amp;R (Kernighan &amp; Ritchie) formatting rules.
 *
 * <p><strong>K&amp;R Style Rules:</strong>
 * <ul>
 *   <li><strong>Opening Brace:</strong> Same line as declaration/statement</li>
 *   <li><strong>Closing Brace:</strong> New line, aligned with declaration/statement start</li>
 * </ul>
 *
 * <p><strong>Example:</strong>
 * <pre>{@code
 * public void method() {
 *     statement;
 * }
 * }</pre>
 *
 * <p><strong>Thread Safety:</strong> This class is stateless and thread-safe.
 */
public final class KAndRStrategy implements BraceStyleStrategy
{
	/** Assumed tab width when IndentationCalculator unavailable (standard Java convention). */
	private static final int ASSUMED_TAB_WIDTH = 4;

	/**
	 * Creates a new K&amp;R strategy instance.
	 */
	public KAndRStrategy()
	{
		// Stateless - no initialization needed
	}

	@Override
	public boolean isOpeningBraceCorrect(BraceContext context, String sourceText,
		IndentationCalculator indentationCalculator)
	{
		requireThat(context, "context").isNotNull();
		requireThat(sourceText, "sourceText").isNotNull();
		requireThat(indentationCalculator, "indentationCalculator").isNotNull();

		// K&R: Opening brace must be on same line as declaration
		int declarationLine = context.node().getRange().start().line();
		int openingBraceLine = context.getOpeningBraceLine();

		return openingBraceLine == declarationLine;
	}

	@Override
	public boolean isClosingBraceCorrect(BraceContext context, String sourceText,
		IndentationCalculator indentationCalculator)
	{
		requireThat(context, "context").isNotNull();
		requireThat(sourceText, "sourceText").isNotNull();
		requireThat(indentationCalculator, "indentationCalculator").isNotNull();

		// K&R: Closing brace must be on new line, aligned with declaration
		int declarationLine = context.node().getRange().start().line();
		int closingBraceLine = context.getClosingBraceLine();

		// Closing brace must not be on same line as opening (unless single-line block)
		if (closingBraceLine == context.getOpeningBraceLine())
		{
			// Single-line blocks are allowed in K&R style
			return true;
		}

		// Closing brace must be on its own line
		if (closingBraceLine == declarationLine)
		{
			// Malformed: closing brace on declaration line
			return false;
		}

		// Verify indentation alignment
		String declarationLineText = SourceTextUtil.extractLine(sourceText, declarationLine);
		String closingBraceLineText = SourceTextUtil.extractLine(sourceText, closingBraceLine);

		int declarationIndent = SourceTextUtil.getIndentationLevel(declarationLineText, ASSUMED_TAB_WIDTH);
		int closingBraceIndent = SourceTextUtil.getIndentationLevel(closingBraceLineText, ASSUMED_TAB_WIDTH);

		return closingBraceIndent == declarationIndent;
	}

	@Override
	public String getStyleName()
	{
		return "K&R";
	}
}
