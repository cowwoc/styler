package io.github.cowwoc.styler.formatter.impl;

import io.github.cowwoc.styler.ast.SourcePosition;
import io.github.cowwoc.styler.ast.SourceRange;
import io.github.cowwoc.styler.formatter.api.TextEdit;

import java.util.ArrayList;
import java.util.List;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;

/**
 * Generates {@link TextEdit} instances to correct indentation violations.
 * <p>
 * This class takes violations detected by {@link IndentationAnalyzer} and produces
 * text edits that replace incorrect indentation with properly formatted indentation
 * according to the configured mode (spaces, tabs, or mixed).
 * <p>
 * The corrector uses {@link IndentationCalculator} to generate indentation strings
 * in the appropriate format and creates {@link SourceRange} instances that precisely
 * target the leading whitespace of each violating line.
 */
public final class IndentationCorrector
{
	private static final String RULE_ID = "io.github.cowwoc.styler.rules.Indentation";

	private final IndentationConfiguration config;
	private final IndentationCalculator calculator;

	/**
	 * Creates a new indentation corrector.
	 *
	 * @param config the indentation configuration to apply, never {@code null}
	 * @throws NullPointerException if {@code config} is {@code null}
	 */
	public IndentationCorrector(IndentationConfiguration config)
	{
		requireThat(config, "config").isNotNull();

		this.config = config;
		this.calculator = new IndentationCalculator(config.getTabWidth(),
			config.getContinuationIndent());
	}

	/**
	 * Generates text edits to correct the provided violations.
	 * <p>
	 * Each violation is converted into a {@link TextEdit} that replaces the
	 * incorrect leading whitespace with correctly formatted indentation. The
	 * generated edits are ordered by position and ready to be applied by the
	 * formatting engine.
	 *
	 * @param violations the list of violations to correct, never {@code null}
	 * @return a list of text edits, never {@code null} but may be empty
	 * @throws NullPointerException if {@code violations} is {@code null}
	 */
	public List<TextEdit> correct(List<IndentationViolation> violations)
	{
		requireThat(violations, "violations").isNotNull();

		List<TextEdit> edits = new ArrayList<>();

		for (IndentationViolation violation : violations)
		{
			TextEdit edit = createEditForViolation(violation);
			edits.add(edit);
		}

		return edits;
	}

	/**
	 * Creates a text edit for a single indentation violation.
	 * <p>
	 * This method calculates the source range covering the incorrect indentation,
	 * generates the correct indentation string, and creates a {@link TextEdit}
	 * that replaces the old whitespace with the new.
	 *
	 * @param violation the violation to correct, never {@code null}
	 * @return a text edit that fixes the violation, never {@code null}
	 */
	private TextEdit createEditForViolation(IndentationViolation violation)
	{
		// Calculate the range of leading whitespace to replace
		SourceRange range = calculateWhitespaceRange(violation);

		// Generate the correct indentation string
		String correctIndentation = calculator.generateIndentation(
			violation.expectedIndentation(),
			config.getMode(),
			config.getIndentSize());

		// Create and return the text edit
		return TextEdit.create(range, correctIndentation, RULE_ID);
	}

	/**
	 * Calculates the source range covering the leading whitespace of a line.
	 * <p>
	 * The range starts at column 1 of the line and extends to the column where
	 * the actual code content begins (after all whitespace characters).
	 *
	 * @param violation the violation containing line and indentation information
	 * @return a source range covering the leading whitespace, never {@code null}
	 */
	private SourceRange calculateWhitespaceRange(IndentationViolation violation)
	{
		int lineNumber = violation.getLineNumber();
		String line = violation.lineContent();

		// Find the end of leading whitespace
		int whitespaceEnd = findWhitespaceEnd(line);

		// Create source positions for the range
		SourcePosition start = new SourcePosition(lineNumber, 1);
		SourcePosition end = new SourcePosition(lineNumber, whitespaceEnd + 1);

		return new SourceRange(start, end);
	}

	/**
	 * Finds the index where leading whitespace ends in a line.
	 * <p>
	 * This method scans from the beginning of the line until it encounters
	 * a non-whitespace character or reaches the end of the line.
	 *
	 * @param line the line to analyze, never {@code null}
	 * @return the 0-based index of the first non-whitespace character,
	 *         or the line length if the line is all whitespace
	 */
	private int findWhitespaceEnd(String line)
	{
		for (int i = 0; i < line.length(); ++i)
		{
			char c = line.charAt(i);
			if (c != ' ' && c != '\t')
			{
				return i;
			}
		}
		return line.length();
	}
}
