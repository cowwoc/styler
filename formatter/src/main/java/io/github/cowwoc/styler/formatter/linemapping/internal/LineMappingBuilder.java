package io.github.cowwoc.styler.formatter.linemapping.internal;

import io.github.cowwoc.styler.formatter.linemapping.LineMapping;

import java.util.Arrays;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;

/**
 * Builder that computes line mapping using LCS (Longest Common Subsequence) algorithm.
 * <p>
 * The algorithm works as follows:
 * <ol>
 *   <li>Split both original and formatted content into lines</li>
 *   <li>If identical, return identity mapping</li>
 *   <li>Compute LCS to find matching lines</li>
 *   <li>Backtrack to establish bidirectional mappings</li>
 * </ol>
 * <p>
 * <b>Thread-safety</b>: This class is not thread-safe. Each thread should use its own instance.
 */
public final class LineMappingBuilder
{
	/**
	 * Sentinel value indicating a line has no mapping.
	 */
	private static final int NO_MAPPING = -1;

	/**
	 * Creates a new LineMappingBuilder.
	 */
	public LineMappingBuilder()
	{
	}

	/**
	 * Builds a line mapping from original to formatted content.
	 *
	 * @param original  the original source code
	 * @param formatted the formatted source code
	 * @return the computed line mapping
	 * @throws NullPointerException if any argument is null
	 */
	public LineMapping build(String original, String formatted)
	{
		requireThat(original, "original").isNotNull();
		requireThat(formatted, "formatted").isNotNull();

		String[] originalLines = splitLines(original);
		String[] formattedLines = splitLines(formatted);

		if (Arrays.equals(originalLines, formattedLines))
			return LineMapping.identity(originalLines.length);

		return computeMapping(originalLines, formattedLines);
	}

	/**
	 * Splits content into lines, preserving empty trailing line if content ends with newline.
	 *
	 * @param content the content to split
	 * @return array of lines
	 */
	private String[] splitLines(String content)
	{
		if (content.isEmpty())
			return new String[0];

		// Split by line separator, keeping trailing empty strings
		// -1 limit means no limit and trailing empty strings are included
		return content.split("\r?\n", -1);
	}

	/**
	 * Computes the line mapping using LCS algorithm.
	 *
	 * @param originalLines  lines from original content
	 * @param formattedLines lines from formatted content
	 * @return the computed line mapping
	 */
	private LineMapping computeMapping(String[] originalLines, String[] formattedLines)
	{
		int originalCount = originalLines.length;
		int formattedCount = formattedLines.length;

		// Compute LCS table
		int[][] lcs = computeLcsTable(originalLines, formattedLines);

		// Backtrack to establish mappings
		int[] originalToFormatted = new int[originalCount];
		int[] formattedToOriginal = new int[formattedCount];
		Arrays.fill(originalToFormatted, NO_MAPPING);
		Arrays.fill(formattedToOriginal, NO_MAPPING);

		backtrackLcs(lcs, originalLines, formattedLines, originalToFormatted, formattedToOriginal);

		return new DefaultLineMapping(originalToFormatted, formattedToOriginal);
	}

	/**
	 * Computes the LCS (Longest Common Subsequence) table.
	 *
	 * @param originalLines  lines from original content
	 * @param formattedLines lines from formatted content
	 * @return the LCS table where {@code lcs[i][j]} is the LCS length of
	 *         {@code originalLines[0..i-1]} and {@code formattedLines[0..j-1]}
	 */
	private int[][] computeLcsTable(String[] originalLines, String[] formattedLines)
	{
		int originalCount = originalLines.length;
		int formattedCount = formattedLines.length;

		int[][] lcs = new int[originalCount + 1][formattedCount + 1];

		for (int i = 1; i <= originalCount; ++i)
		{
			for (int j = 1; j <= formattedCount; ++j)
			{
				if (originalLines[i - 1].equals(formattedLines[j - 1]))
					lcs[i][j] = lcs[i - 1][j - 1] + 1;
				else
					lcs[i][j] = Math.max(lcs[i - 1][j], lcs[i][j - 1]);
			}
		}

		return lcs;
	}

	/**
	 * Backtracks through the LCS table to establish bidirectional line mappings.
	 *
	 * @param lcs                 the LCS table
	 * @param originalLines       lines from original content
	 * @param formattedLines      lines from formatted content
	 * @param originalToFormatted output array for original-to-formatted mapping (1-based line numbers)
	 * @param formattedToOriginal output array for formatted-to-original mapping (1-based line numbers)
	 */
	private void backtrackLcs(int[][] lcs, String[] originalLines, String[] formattedLines,
		int[] originalToFormatted, int[] formattedToOriginal)
	{
		int i = originalLines.length;
		int j = formattedLines.length;

		while (i > 0 && j > 0)
		{
			if (originalLines[i - 1].equals(formattedLines[j - 1]))
			{
				// Lines match - establish bidirectional mapping
				// Arrays are 0-indexed, but line numbers are 1-based
				originalToFormatted[i - 1] = j;
				formattedToOriginal[j - 1] = i;
				--i;
				--j;
			}
			else if (lcs[i - 1][j] > lcs[i][j - 1])
			{
				// Original line was deleted (no match in formatted)
				--i;
			}
			else
			{
				// Formatted line is new (no match in original)
				--j;
			}
		}
	}
}
