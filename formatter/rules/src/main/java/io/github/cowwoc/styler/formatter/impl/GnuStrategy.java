package io.github.cowwoc.styler.formatter.impl;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;

/**
 * Brace style strategy implementing GNU formatting rules.
 *
 * <p><strong>GNU Style Rules:</strong>
 * <ul>
 *   <li><strong>Opening Brace:</strong> New line, indented one level from declaration/statement</li>
 *   <li><strong>Closing Brace:</strong> New line, aligned with declaration/statement start</li>
 * </ul>
 *
 * <p><strong>Example:</strong>
 * <pre>{@code
 * public void method()
 *   {
 *     statement;
 *   }
 * }</pre>
 *
 * <p><strong>Thread Safety:</strong> This class is stateless and thread-safe.
 */
public final class GnuStrategy implements BraceStyleStrategy
{
	/** Assumed tab width when IndentationCalculator unavailable (standard Java convention). */
	private static final int ASSUMED_TAB_WIDTH = 4;
	/** GNU style indentation offset for opening braces (standard GNU convention). */
	private static final int GNU_INDENT_OFFSET = 2;

	/**
	 * Creates a new GNU strategy instance.
	 */
	public GnuStrategy()
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

		// GNU: Opening brace must be on new line, indented one level from declaration
		int declarationLine = context.node().getRange().start().line();
		int openingBraceLine = context.getOpeningBraceLine();

		if (openingBraceLine == declarationLine)
		{
			// Opening brace on same line as declaration - violates GNU style
			return false;
		}

		// Verify indentation: opening brace should be indented one level from declaration
		String declarationLineText = SourceTextUtil.extractLine(sourceText, declarationLine);
		String openingBraceLineText = SourceTextUtil.extractLine(sourceText, openingBraceLine);

		int declarationIndent = SourceTextUtil.getIndentationLevel(declarationLineText, ASSUMED_TAB_WIDTH);
		int openingBraceIndent = SourceTextUtil.getIndentationLevel(openingBraceLineText, ASSUMED_TAB_WIDTH);

		// Opening brace should be indented by GNU standard offset (2 spaces)
		int expectedIndent = declarationIndent + GNU_INDENT_OFFSET;

		return openingBraceIndent == expectedIndent;
	}

	@Override
	public boolean isClosingBraceCorrect(BraceContext context, String sourceText,
		IndentationCalculator indentationCalculator)
	{
		requireThat(context, "context").isNotNull();
		requireThat(sourceText, "sourceText").isNotNull();
		requireThat(indentationCalculator, "indentationCalculator").isNotNull();

		// GNU: Closing brace must be on new line, aligned with declaration
		int declarationLine = context.node().getRange().start().line();
		int closingBraceLine = context.getClosingBraceLine();

		// Closing brace must not be on declaration line or opening brace line
		if (closingBraceLine == declarationLine || closingBraceLine == context.getOpeningBraceLine())
		{
			return false;
		}

		// Verify indentation alignment with declaration (NOT with opening brace)
		String declarationLineText = SourceTextUtil.extractLine(sourceText, declarationLine);
		String closingBraceLineText = SourceTextUtil.extractLine(sourceText, closingBraceLine);

		int declarationIndent = SourceTextUtil.getIndentationLevel(declarationLineText, ASSUMED_TAB_WIDTH);
		int closingBraceIndent = SourceTextUtil.getIndentationLevel(closingBraceLineText, ASSUMED_TAB_WIDTH);

		return closingBraceIndent == declarationIndent;
	}

	@Override
	public String getStyleName()
	{
		return "GNU";
	}

	@Override
	public String getOpeningBraceDescription()
	{
		return "new line, indented from declaration";
	}

	@Override
	public String getClosingBraceDescription()
	{
		return "aligned with declaration";
	}
}
