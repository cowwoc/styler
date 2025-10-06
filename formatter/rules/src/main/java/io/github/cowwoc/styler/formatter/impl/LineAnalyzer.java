package io.github.cowwoc.styler.formatter.impl;

import io.github.cowwoc.styler.ast.SourcePosition;
import io.github.cowwoc.styler.ast.SourceRange;
import io.github.cowwoc.styler.formatter.api.LineLengthRuleConfiguration;
import io.github.cowwoc.styler.formatter.api.WrapConfiguration;

import java.util.ArrayList;
import java.util.List;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;

/**
 * Analyzes source code to identify lines that exceed the configured maximum length.
 * <p>
 * This class performs text-based analysis of source code lines, calculating effective
 * line lengths by expanding tabs to spaces and identifying lines that violate the
 * maximum length constraint. It produces source ranges for each violating line.
 * <p>
 * Per <a href="https://docs.oracle.com/javase/specs/jls/se25/html/jls-3.html#jls-3.4">JLS §3.4</a>,
 * line terminators are defined as CR, LF, or CRLF sequences.
 */
final class LineAnalyzer
{
	private static final char TAB = '\t';
	private static final int DEFAULT_TAB_WIDTH = 4;

	private final String sourceText;
	private final LineLengthRuleConfiguration config;
	private final int tabWidth;

	/**
	 * Creates a new line analyzer for the specified source code.
	 * <p>
	 * The tab width for line length calculation is derived from the wrap configuration
	 * when available. If no wrap configuration is provided, the default tab width of
	 * {@value DEFAULT_TAB_WIDTH} spaces is used.
	 *
	 * @param sourceText the source code to analyze, never {@code null}
	 * @param config the line length rule configuration, never {@code null}
	 * @param wrapConfig the wrap configuration for tab width derivation, may be {@code null}
	 * @throws NullPointerException if {@code sourceText} or {@code config} is {@code null}
	 */
	LineAnalyzer(String sourceText, LineLengthRuleConfiguration config, WrapConfiguration wrapConfig)
	{
		requireThat(sourceText, "sourceText").isNotNull();
		requireThat(config, "config").isNotNull();
		// wrapConfig CAN be null

		this.sourceText = sourceText;
		this.config = config;
		if (wrapConfig != null)
		{
			this.tabWidth = wrapConfig.getTabWidth();
		}
		else
		{
			this.tabWidth = DEFAULT_TAB_WIDTH;
		}
	}

	/**
	 * Identifies all lines that exceed the maximum configured length.
	 * <p>
	 * This method analyzes the source code line by line, calculating the effective
	 * length of each line (with tab expansion) and identifying lines that exceed
	 * the maximum length. Empty lines and lines containing only whitespace are
	 * not flagged as violations.
	 *
	 * @return list of source ranges for lines exceeding max length, never {@code null}
	 */
	List<SourceRange> findViolatingLines()
	{
		List<SourceRange> violations = new ArrayList<>();
		String[] lines = SourceTextUtil.splitIntoLines(sourceText);

		for (int lineIndex = 0; lineIndex < lines.length; lineIndex += 1)
		{
			String line = lines[lineIndex];
			int effectiveLength = calculateEffectiveLength(line);

			if (effectiveLength > config.getMaxLineLength() && !line.isBlank())
			{
				int lineNumber = lineIndex + 1;
				SourcePosition start = new SourcePosition(lineNumber, 1);
				SourcePosition end = new SourcePosition(lineNumber, line.length() + 1);
				violations.add(new SourceRange(start, end));
			}
		}

		return violations;
	}

	/**
	 * Calculates the effective length of a line by expanding tabs to spaces.
	 * <p>
	 * Tab characters are expanded based on the configured tab width. This method
	 * simulates the visual width of the line as it would appear in an editor with
	 * the specified tab width setting.
	 *
	 * @param line the line to measure, never {@code null}
	 * @return the effective length in character columns, always non-negative
	 * @throws NullPointerException if {@code line} is {@code null}
	 */
	int calculateEffectiveLength(String line)
	{
		requireThat(line, "line").isNotNull();

		int effectiveLength = 0;
		for (int i = 0; i < line.length(); i += 1)
		{
			char c = line.charAt(i);
			if (c == TAB)
			{
				int spacesToNextTabStop = tabWidth - (effectiveLength % tabWidth);
				effectiveLength += spacesToNextTabStop;
			}
			else
			{
				effectiveLength += 1;
			}
		}
		return effectiveLength;
	}

	/**
	 * Gets the line at the specified line number (1-based).
	 *
	 * @param lineNumber the line number, must be positive and within bounds
	 * @return the line text, never {@code null}
	 * @throws IllegalArgumentException if {@code lineNumber} is invalid
	 */
	String getLine(int lineNumber)
	{
		requireThat(lineNumber, "lineNumber").isPositive();

		String[] lines = SourceTextUtil.splitIntoLines(sourceText);
		int lineIndex = lineNumber - 1;

		requireThat(lineIndex, "lineIndex").isLessThan(lines.length,
			"lineIndex must be less than the number of lines");

		return lines[lineIndex];
	}
}
