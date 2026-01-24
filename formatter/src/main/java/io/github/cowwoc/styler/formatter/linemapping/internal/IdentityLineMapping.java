package io.github.cowwoc.styler.formatter.linemapping.internal;

import io.github.cowwoc.styler.formatter.linemapping.LineMapping;

import java.util.Optional;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;

/**
 * An identity line mapping where all lines map to themselves.
 * <p>
 * <b>Thread-safety</b>: This class is immutable and thread-safe.
 */
public final class IdentityLineMapping implements LineMapping
{
	private final int lineCount;

	/**
	 * Creates an identity mapping for the specified line count.
	 *
	 * @param lineCount the number of lines in both original and formatted content
	 * @throws IllegalArgumentException if {@code lineCount} is negative
	 */
	public IdentityLineMapping(int lineCount)
	{
		requireThat(lineCount, "lineCount").isNotNegative();
		this.lineCount = lineCount;
	}

	@Override
	public Optional<Integer> toFormattedLine(int originalLine)
	{
		requireThat(originalLine, "originalLine").isPositive().isLessThanOrEqualTo(lineCount);
		return Optional.of(originalLine);
	}

	@Override
	public Optional<Integer> toOriginalLine(int formattedLine)
	{
		requireThat(formattedLine, "formattedLine").isPositive().isLessThanOrEqualTo(lineCount);
		return Optional.of(formattedLine);
	}

	@Override
	public int originalLineCount()
	{
		return lineCount;
	}

	@Override
	public int formattedLineCount()
	{
		return lineCount;
	}
}
