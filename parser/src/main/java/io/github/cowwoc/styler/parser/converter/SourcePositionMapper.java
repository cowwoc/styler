package io.github.cowwoc.styler.parser.converter;

import io.github.cowwoc.styler.ast.SourcePosition;
import io.github.cowwoc.styler.ast.SourceRange;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Maps absolute character offsets to line/column source positions using O(log n) binary search.
 * <p>
 * This class builds a line offset index during construction (O(n) single pass) and then provides
 * O(log n) position lookups using binary search. The implementation is thread-safe and stateless
 * after construction.
 * </p>
 * <h2>Performance Characteristics</h2>
 * <ul>
 * <li><strong>Construction:</strong> O(n) - Single pass through source text to build line offsets</li>
 * <li><strong>Position Lookup:</strong> O(log n) - Binary search on line offset array</li>
 * <li><strong>Memory:</strong> 4 bytes per line (int array storage)</li>
 * </ul>
 * <h2>Line Numbering</h2>
 * Line numbers are 1-based (first line is line 1), column numbers are 1-based (first column is column 1).
 * This matches standard IDE and compiler conventions.
 * <h2>Thread Safety</h2>
 * Instances are immutable after construction and safe for concurrent access from multiple threads.
 *
 * @since 1.0
 */
public final class SourcePositionMapper
{
	private final String sourceText;
	private final int[] lineStartOffsets;

	/**
	 * Creates a SourcePositionMapper from source text.
	 * Builds the line offset index in O(n) time.
	 *
	 * @param sourceText the source text to map positions for
	 * @return a new {@link SourcePositionMapper} instance
	 * @throws IllegalArgumentException if sourceText is {@code null}
	 */
	public static SourcePositionMapper create(String sourceText)
{
		if (sourceText == null)
{
			throw new NullPointerException("Source text cannot be null");
		}

		return new SourcePositionMapper(sourceText);
	}

	/**
	 * Private constructor - use {@link #create(String)} factory method.
	 *
	 * @param sourceText the source text to build line offsets from
	 */
	private SourcePositionMapper(String sourceText)
{
		this.sourceText = sourceText;
		this.lineStartOffsets = buildLineOffsets(sourceText);
	}

	/**
	 * Gets the source position (line and column) for the given absolute offset.
	 * Uses O(log n) binary search on the line offset array.
	 *
	 * @param offset the absolute character offset in the source text
	 * @return the {@link SourcePosition} containing 1-based line and column numbers
	 * @throws IllegalArgumentException if offset is negative or exceeds source text length
	 */
	public SourcePosition getPosition(int offset)
{
		if (offset < 0)
{
			throw new IllegalArgumentException("Offset cannot be negative: " + offset);
		}

		if (offset > sourceText.length())
{
			throw new IllegalArgumentException(
				"Offset " + offset + " exceeds source text length " + sourceText.length());
		}

		// Binary search for the line containing this offset
		// Arrays.binarySearch returns: (-(insertion point) - 1) if not found
		// We want the line whose start offset is <= the target offset
		int lineIndex = Arrays.binarySearch(lineStartOffsets, offset);

		if (lineIndex < 0)
{
			// Offset is not exactly at a line start
			// Convert insertion point to the line containing this offset
			lineIndex = -lineIndex - 2;
		}

		// Ensure lineIndex is valid
		if (lineIndex < 0)
{
			lineIndex = 0;
		}

		int lineNumber = lineIndex + 1; // Convert to 1-based line number
		int lineStartOffset = lineStartOffsets[lineIndex];
		int columnNumber = offset - lineStartOffset + 1; // Convert to 1-based column

		return new SourcePosition(lineNumber, columnNumber);
	}

	/**
	 * Gets the source text range for the given start and end offsets.
	 *
	 * @param startOffset the starting absolute offset
	 * @param endOffset the ending absolute offset (exclusive)
	 * @return the {@link SourceRange} containing start and end positions
	 * @throws IllegalArgumentException if offsets are invalid or out of order
	 */
	public SourceRange getRange(int startOffset, int endOffset)
{
		if (startOffset > endOffset)
{
			throw new IllegalArgumentException(
				"Start offset " + startOffset + " cannot exceed end offset " + endOffset);
		}

		SourcePosition start = getPosition(startOffset);
		SourcePosition end = getPosition(endOffset);

		return new SourceRange(start, end);
	}

	/**
	 * Gets the total number of lines in the source text.
	 *
	 * @return the number of lines (1-based count)
	 */
	public int getTotalLines()
{
		return lineStartOffsets.length;
	}

	/**
	 * Gets the source text for which positions are being mapped.
	 *
	 * @return the source text
	 */
	public String getSourceText()
{
		return sourceText;
	}

	/**
	 * Builds the line offset array by scanning source text once.
	 * Handles all line terminator variations: LF (\n), CR (\r), CRLF (\r\n).
	 *
	 * @param text the source text to scan
	 * @return array of line start offsets (0-based)
	 */
	private static int[] buildLineOffsets(String text)
{
		List<Integer> offsets = new ArrayList<>();
		offsets.add(0); // First line always starts at offset 0

		for (int i = 0; i < text.length(); ++i)
{
			char ch = text.charAt(i);

			if (ch == '\n')
{
				// LF - start new line after this character
				offsets.add(i + 1);
			}
			else if (ch == '\r')
{
				// CR - check for CRLF
				if (i + 1 < text.length() && text.charAt(i + 1) == '\n')
{
					// CRLF - skip the LF and start new line after both
					++i;
					offsets.add(i + 1);
				}
				else
{
					// Just CR - start new line after this character
					offsets.add(i + 1);
				}
			}
		}

		// Convert List to int array
		return offsets.stream().mapToInt(Integer::intValue).toArray();
	}
}
