package io.github.cowwoc.styler.formatter.impl;

import io.github.cowwoc.styler.formatter.api.TextEdit;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;

/**
 * Generates TextEdit objects to fix brace formatting violations.
 *
 * <p>This generator converts {@link BraceViolation} records into {@link TextEdit} objects that can be
 * applied to source code to correct brace placement according to the configured style.
 *
 * <p><strong>Current Limitations:</strong>
 * <ul>
 *   <li>Does not preserve comments between declaration and braces</li>
 *   <li>Uses simplified indentation calculation (may need refinement for complex cases)</li>
 *   <li>Assumes single-character brace symbols</li>
 * </ul>
 *
 * <p><strong>Thread Safety:</strong> This class is stateless and thread-safe.
 */
public final class BraceEditGenerator
{
	private static final String RULE_ID = "BraceFormatter";
	/** GNU style indentation offset for opening braces (standard GNU convention). */
	private static final int GNU_INDENT_OFFSET = 2;

	private final String sourceText;
	private final IndentationCalculator indentationCalculator;

	/**
	 * Creates a new brace edit generator.
	 *
	 * @param sourceText the source code text, never {@code null}
	 * @param indentationCalculator calculator for indentation levels, never {@code null}
	 * @throws NullPointerException if any parameter is {@code null}
	 */
	public BraceEditGenerator(String sourceText, IndentationCalculator indentationCalculator)
	{
		requireThat(sourceText, "sourceText").isNotNull();
		requireThat(indentationCalculator, "indentationCalculator").isNotNull();

		this.sourceText = sourceText;
		this.indentationCalculator = indentationCalculator;
	}

	/**
	 * Generates a TextEdit to fix a brace formatting violation.
	 *
	 * <p><strong>Note:</strong> This implementation provides basic brace repositioning.
	 * Complex cases involving comments or multi-line declarations may require manual review.
	 *
	 * @param violation the brace violation to fix, never {@code null}
	 * @return TextEdit that will fix the violation, never {@code null}
	 * @throws NullPointerException if {@code violation} is {@code null}
	 */
	public TextEdit generateEdit(BraceViolation violation)
	{
		requireThat(violation, "violation").isNotNull();

		if (violation.isOpeningBraceViolation())
		{
			return generateOpeningBraceEdit(violation);
		}
		return generateClosingBraceEdit(violation);
	}

	/**
	 * Generates edit for opening brace violation.
	 *
	 * <p>Strategy:
	 * <ul>
	 *   <li>K&R: Move brace to end of declaration line</li>
	 *   <li>Allman: Move brace to new line, align with declaration</li>
	 *   <li>GNU: Move brace to new line, indent 2 spaces from declaration</li>
	 * </ul>
	 *
	 * @param violation the violation to fix, never {@code null}
	 * @return TextEdit for opening brace correction
	 */
	private TextEdit generateOpeningBraceEdit(BraceViolation violation)
	{
		BraceContext context = violation.context();
		String styleName = violation.styleName();

		// Extract declaration line and brace line for analysis
		int declarationLine = context.node().getRange().start().line();
		String declarationLineText = SourceTextUtil.extractLine(sourceText, declarationLine);
		int declarationIndent = indentationCalculator.calculateIndentationLevel(declarationLineText);

		// Calculate new brace position based on style
		String replacement;
		if ("K&R".equals(styleName))
		{
			// K&R: Brace on same line - add space before brace
			replacement = " {";
		}
		else if ("Allman".equals(styleName))
		{
			// Allman: New line, same indentation as declaration
			String indentStr = indentationCalculator.generateIndentation(declarationIndent);
			replacement = "\n" + indentStr + "{";
		}
		else if ("GNU".equals(styleName))
		{
			// GNU: New line, indented 2 spaces from declaration
			String indentStr = indentationCalculator.generateIndentation(declarationIndent + GNU_INDENT_OFFSET);
			replacement = "\n" + indentStr + "{";
		}
		else
		{
			throw new IllegalStateException("Unknown style: " + styleName);
		}

		// Create edit that replaces the incorrectly positioned brace
		return TextEdit.create(violation.violationRange(), replacement, RULE_ID);
	}

	/**
	 * Generates edit for closing brace violation.
	 *
	 * <p>All styles align closing brace with declaration start (except K&R single-line blocks).
	 *
	 * @param violation the violation to fix, never {@code null}
	 * @return TextEdit for closing brace correction
	 */
	private TextEdit generateClosingBraceEdit(BraceViolation violation)
	{
		BraceContext context = violation.context();

		// Extract declaration line for indentation analysis
		int declarationLine = context.node().getRange().start().line();
		String declarationLineText = SourceTextUtil.extractLine(sourceText, declarationLine);
		int declarationIndent = indentationCalculator.calculateIndentationLevel(declarationLineText);

		// Closing brace should be on new line, aligned with declaration
		String indentStr = indentationCalculator.generateIndentation(declarationIndent);
		String replacement = "\n" + indentStr + "}";

		// Create edit that replaces the incorrectly positioned brace
		return TextEdit.create(violation.violationRange(), replacement, RULE_ID);
	}
}
