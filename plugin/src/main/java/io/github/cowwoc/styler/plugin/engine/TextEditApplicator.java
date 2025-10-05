package io.github.cowwoc.styler.plugin.engine;

import io.github.cowwoc.styler.ast.SourcePosition;
import io.github.cowwoc.styler.formatter.api.TextEdit;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * Applies text edits to source code strings.
 * Handles overlapping edits and maintains correct offsets during application.
 * Thread-safe and stateless for Maven parallel builds.
 */
public final class TextEditApplicator
{
	/**
	 * Applies a list of text edits to source text.
	 * Edits are sorted by position and applied in reverse order to maintain correct offsets.
	 *
	 * @param sourceText original source text
	 * @param edits list of text edits to apply
	 * @return modified source text with all edits applied
	 * @throws IllegalArgumentException if edits overlap or are invalid
	 * @throws NullPointerException if sourceText or edits are null
	 */
	public String applyEdits(String sourceText, List<TextEdit> edits)
	{
		Objects.requireNonNull(sourceText, "sourceText cannot be null");
		Objects.requireNonNull(edits, "edits cannot be null");

		if (edits.isEmpty())
		{
			return sourceText;
		}

		// Sort edits by start position in descending order
		// This allows us to apply edits from end to beginning, maintaining correct offsets
		List<TextEdit> sortedEdits = edits.stream().
			sorted(Comparator.comparing((TextEdit e) -> e.getRange().start()).reversed()).
			toList();

		// Validate that edits don't overlap
		validateNoOverlaps(sortedEdits);

		// Apply edits in reverse order (from end to beginning)
		StringBuilder result = new StringBuilder(sourceText);

		for (TextEdit edit : sortedEdits)
		{
			int startOffset = positionToOffset(sourceText, edit.getRange().start());
			int endOffset = positionToOffset(sourceText, edit.getRange().end());
			String replacement = edit.getReplacement();

			// Validate offsets
			if (startOffset < 0 || endOffset > result.length() || startOffset > endOffset)
			{
				throw new IllegalArgumentException(
					String.format("Invalid edit range: [%d, %d) for text length %d at %s",
						startOffset, endOffset, result.length(), edit.getRange()));
			}

			// Apply the edit
			result.replace(startOffset, endOffset, replacement);
		}

		return result.toString();
	}

	/**
	 * Converts a SourcePosition (line/column) to an absolute character offset in the source text.
	 *
	 * @param sourceText source code text
	 * @param position line/column position
	 * @return absolute character offset
	 */
	private int positionToOffset(String sourceText, SourcePosition position)
	{
		int line = 1;
		int column = 1;

		for (int i = 0; i < sourceText.length(); ++i)
		{
			if (line == position.line() && column == position.column())
			{
				return i;
			}

			char ch = sourceText.charAt(i);
			if (ch == '\n')
			{
				++line;
				column = 1;
			}
			else
			{
				++column;
			}
		}

		// Handle end of file
		if (line == position.line() && column == position.column())
		{
			return sourceText.length();
		}

		throw new IllegalArgumentException(
			String.format("Position %s is beyond end of source text (length=%d)",
				position, sourceText.length()));
	}

	/**
	 * Validates that no edits overlap with each other.
	 * Edits must be sorted in descending order by start position.
	 *
	 * @param sortedEdits edits sorted by start position (descending)
	 * @throws IllegalArgumentException if any edits overlap
	 */
	private void validateNoOverlaps(List<TextEdit> sortedEdits)
	{
		for (int i = 0; i < sortedEdits.size() - 1; ++i)
		{
			TextEdit current = sortedEdits.get(i);
			TextEdit next = sortedEdits.get(i + 1);

			// Check if edits overlap using the built-in method
			if (current.overlapsWith(next))
			{
				throw new IllegalArgumentException(
					String.format("Overlapping edits detected: %s overlaps with %s",
						current.getRange(), next.getRange()));
			}
		}
	}
}
