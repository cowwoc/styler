package io.github.cowwoc.styler.formatter.linelength.internal;

import io.github.cowwoc.styler.formatter.DefaultFormattingViolation;
import io.github.cowwoc.styler.formatter.FormattingViolation;
import io.github.cowwoc.styler.formatter.TransformationContext;
import io.github.cowwoc.styler.formatter.ViolationSeverity;
import io.github.cowwoc.styler.formatter.linelength.LineLengthConfiguration;

import java.util.ArrayList;
import java.util.List;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;

/**
 * Analyzes source code for line length violations.
 * Detects lines whose visual length exceeds the configured maximum, where visual length
 * accounts for tab expansion.
 * <p>
 * <b>Thread-safety</b>: This class is thread-safe.
 */
public final class LineAnalyzer
{
	/**
	 * Utility class constructor.
	 */
	private LineAnalyzer()
	{
	}

	/**
	 * Analyzes the source code for line length violations.
	 *
	 * @param context transformation context with source code access
	 * @param config line length configuration with rules
	 * @return list of detected violations
	 * @throws NullPointerException if {@code context} or {@code config} is {@code null}
	 */
	public static List<FormattingViolation> analyze(TransformationContext context,
		LineLengthConfiguration config)
	{
		requireThat(context, "context").isNotNull();
		requireThat(config, "config").isNotNull();

		List<FormattingViolation> violations = new ArrayList<>();
		String sourceCode = context.sourceCode();
		String[] lines = sourceCode.split("\n", -1);  // Keep trailing empty lines

		for (int i = 0; i < lines.length; ++i)
		{
			String line = lines[i];
			int visualLength = calculateVisualLength(line, config.tabWidth());

			if (visualLength > config.maxLineLength())
			{
				// Calculate character position of line start in source
				int lineStart = getLineStartPosition(sourceCode, i);
				int lineNumber = i + 1;  // 1-based line numbering

				FormattingViolation violation = new DefaultFormattingViolation(
					config.ruleId(),
					ViolationSeverity.WARNING,
					String.format("Line %d exceeds maximum length of %d (actual: %d)",
						lineNumber,
						config.maxLineLength(),
						visualLength),
					context.filePath(),
					lineStart,
					lineStart + line.length(),
					lineNumber,
					1,  // column number (1-based)
					List.of());  // no fixes available

				violations.add(violation);
			}
		}

		return violations;
	}

	/**
	 * Calculates the visual length of a line after tab expansion.
	 * Each tab character is expanded to tabWidth spaces.
	 *
	 * @param line the line to measure
	 * @param tabWidth the number of spaces per tab
	 * @return the visual length after tab expansion
	 */
	private static int calculateVisualLength(String line, int tabWidth)
	{
		int length = 0;
		for (int i = 0; i < line.length(); ++i)
		{
			char currentChar = line.charAt(i);
			if (currentChar == '\t')
				// Expand tab to next tab stop
				length = length + tabWidth - (length % tabWidth);
			else
				++length;
		}
		return length;
	}

	/**
	 * Calculates the character position of the start of a line in the source code.
	 *
	 * @param sourceCode the full source code
	 * @param lineIndex the zero-based line index
	 * @return the character position of the line start
	 */
	private static int getLineStartPosition(String sourceCode, int lineIndex)
	{
		int position = 0;
		int currentLine = 0;

		for (int i = 0; i < sourceCode.length(); ++i)
		{
			if (currentLine == lineIndex)
				return position;

			if (sourceCode.charAt(i) == '\n')
			{
				++currentLine;
				position = i + 1;
			}
		}

		return position;
	}
}
