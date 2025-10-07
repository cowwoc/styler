package io.github.cowwoc.styler.formatter.impl;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;

/**
 * Brace style strategy implementing Allman (BSD) formatting rules.
 *
 * <p><strong>Allman Style Rules:</strong>
 * <ul>
 *   <li><strong>Opening Brace:</strong> New line, same indentation as declaration/statement</li>
 *   <li><strong>Closing Brace:</strong> New line, same indentation as declaration/statement</li>
 * </ul>
 *
 * <p><strong>Example:</strong>
 * <pre>{@code
 * public void method()
 * {
 *     statement;
 * }
 * }</pre>
 *
 * <p><strong>Thread Safety:</strong> This class is stateless and thread-safe.
 */
public final class AllmanStrategy implements BraceStyleStrategy
{
	/** Assumed tab width when IndentationCalculator unavailable (standard Java convention). */
	private static final int ASSUMED_TAB_WIDTH = 4;

	/**
	 * Creates a new Allman strategy instance.
	 */
	public AllmanStrategy()
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

		// Allman: Opening brace must be on new line
		int declarationLine = context.node().getRange().start().line();
		int openingBraceLine = context.getOpeningBraceLine();

		if (openingBraceLine == declarationLine)
		{
			// Opening brace on same line as declaration - violates Allman style
			return false;
		}

		// Verify indentation alignment
		String declarationLineText = SourceTextUtil.extractLine(sourceText, declarationLine);
		String openingBraceLineText = SourceTextUtil.extractLine(sourceText, openingBraceLine);

		int declarationIndent = SourceTextUtil.getIndentationLevel(declarationLineText, ASSUMED_TAB_WIDTH);
		int openingBraceIndent = SourceTextUtil.getIndentationLevel(openingBraceLineText, ASSUMED_TAB_WIDTH);

		return openingBraceIndent == declarationIndent;
	}

	@Override
	public boolean isClosingBraceCorrect(BraceContext context, String sourceText,
		IndentationCalculator indentationCalculator)
	{
		requireThat(context, "context").isNotNull();
		requireThat(sourceText, "sourceText").isNotNull();
		requireThat(indentationCalculator, "indentationCalculator").isNotNull();

		// Allman: Closing brace must be on new line, aligned with declaration
		int declarationLine = context.node().getRange().start().line();
		int closingBraceLine = context.getClosingBraceLine();

		// Closing brace must not be on declaration line or opening brace line
		if (closingBraceLine == declarationLine || closingBraceLine == context.getOpeningBraceLine())
		{
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
		return "Allman";
	}

	@Override
	public String getOpeningBraceDescription()
	{
		return "new line, aligned with declaration";
	}

	@Override
	public String getClosingBraceDescription()
	{
		return "aligned with declaration";
	}
}
