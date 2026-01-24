package io.github.cowwoc.styler.formatter.linemapping.internal;

import io.github.cowwoc.styler.formatter.linemapping.LineMapping;

import java.util.Optional;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;

/**
 * Array-backed implementation of line mapping with bidirectional mappings.
 * <p>
 * Arrays are indexed by line number (1-based), with value being the mapped line or -1 if deleted/new.
 * <p>
 * <b>Thread-safety</b>: This class is immutable and thread-safe.
 */
public final class DefaultLineMapping implements LineMapping
{
	/**
	 * Sentinel value indicating a line has no mapping (deleted or newly inserted).
	 */
	private static final int NO_MAPPING = -1;

	private final int[] originalToFormatted;
	private final int[] formattedToOriginal;
	private final int originalLineCount;
	private final int formattedLineCount;

	/**
	 * Creates a line mapping with the specified bidirectional mapping arrays.
	 *
	 * @param originalToFormatted mapping from original line indices to formatted line numbers (1-based),
	 *                            or -1 if the line was deleted
	 * @param formattedToOriginal mapping from formatted line indices to original line numbers (1-based),
	 *                            or -1 if the line is new
	 * @throws NullPointerException if any array is null
	 */
	public DefaultLineMapping(int[] originalToFormatted, int[] formattedToOriginal)
	{
		requireThat(originalToFormatted, "originalToFormatted").isNotNull();
		requireThat(formattedToOriginal, "formattedToOriginal").isNotNull();

		this.originalToFormatted = originalToFormatted.clone();
		this.formattedToOriginal = formattedToOriginal.clone();
		this.originalLineCount = originalToFormatted.length;
		this.formattedLineCount = formattedToOriginal.length;
	}

	@Override
	public Optional<Integer> toFormattedLine(int originalLine)
	{
		requireThat(originalLine, "originalLine").isPositive().isLessThanOrEqualTo(originalLineCount);
		int mapped = originalToFormatted[originalLine - 1];
		if (mapped == NO_MAPPING)
			return Optional.empty();
		return Optional.of(mapped);
	}

	@Override
	public Optional<Integer> toOriginalLine(int formattedLine)
	{
		requireThat(formattedLine, "formattedLine").isPositive().isLessThanOrEqualTo(formattedLineCount);
		int mapped = formattedToOriginal[formattedLine - 1];
		if (mapped == NO_MAPPING)
			return Optional.empty();
		return Optional.of(mapped);
	}

	@Override
	public int originalLineCount()
	{
		return originalLineCount;
	}

	@Override
	public int formattedLineCount()
	{
		return formattedLineCount;
	}
}
