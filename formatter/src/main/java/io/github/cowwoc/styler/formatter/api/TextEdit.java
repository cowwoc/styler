package io.github.cowwoc.styler.formatter.api;

import io.github.cowwoc.styler.ast.SourceRange;


import java.util.Objects;

/**
 * Represents a text edit that should be applied to source code.
 * <p>
 * This immutable value object specifies a range of text to replace and the
 * replacement text. Text edits are the primary output of formatting rules
 * and are applied by the formatting engine to produce formatted code.
 */
public final class TextEdit implements Comparable<TextEdit>
{
	private final SourceRange range;
	private final String replacement;
	private final String ruleId;
	private final EditPriority priority;

	/**
	 * Creates a new text edit.
	 *
	 * @param range       the source range to replace, never null
	 * @param replacement the replacement text, never null
	 * @param ruleId      the ID of the rule that generated this edit, never null
	 * @param priority    the priority for conflict resolution, never null
	 */
	public TextEdit( SourceRange range,
	                 String replacement,
	                 String ruleId,
	                 EditPriority priority)
	{
		this.range = Objects.requireNonNull(range, "Range cannot be null");
		this.replacement = Objects.requireNonNull(replacement, "Replacement cannot be null");
		this.ruleId = Objects.requireNonNull(ruleId, "Rule ID cannot be null");
		this.priority = Objects.requireNonNull(priority, "Priority cannot be null");
	}

	/**
	 * Creates a new text edit with normal priority.
	 *
	 * @param range       the source range to replace, never null
	 * @param replacement the replacement text, never null
	 * @param ruleId      the ID of the rule that generated this edit, never null
	 * @return a new text edit with normal priority, never null
	 */
	
	public static TextEdit create( SourceRange range,
	                               String replacement,
	                               String ruleId)
	{
		return new TextEdit(range, replacement, ruleId, EditPriority.NORMAL);
	}

	/**
	 * Returns the source range that should be replaced.
	 * <p>
	 * The range specifies the exact positions in the source text where
	 * the edit should be applied.
	 *
	 * @return the source range, never null
	 */
	
	public SourceRange getRange()
	{
		return range;
	}

	/**
	 * Returns the replacement text for this edit.
	 * <p>
	 * This text will replace the content at the specified range.
	 * Empty string indicates deletion of the range content.
	 *
	 * @return the replacement text, never null
	 */
	
	public String getReplacement()
	{
		return replacement;
	}

	/**
	 * Returns the ID of the rule that generated this edit.
	 * <p>
	 * The rule ID is used for conflict resolution and audit logging.
	 *
	 * @return the rule ID, never null
	 */
	
	public String getRuleId()
	{
		return ruleId;
	}

	/**
	 * Returns the priority of this edit for conflict resolution.
	 * <p>
	 * When multiple edits overlap, the priority determines which edit
	 * should be preferred.
	 *
	 * @return the edit priority, never null
	 */
	
	public EditPriority getPriority()
	{
		return priority;
	}

	/**
	 * Returns whether this edit represents a deletion.
	 *
	 * @return true if the replacement text is empty, false otherwise
	 */
	public boolean isDeletion()
	{
		return replacement.isEmpty();
	}

	/**
	 * Returns whether this edit represents an insertion.
	 *
	 * @return true if the range has zero length, false otherwise
	 */
	public boolean isInsertion()
	{
		return range.start().equals(range.end());
	}

	/**
	 * Returns whether this edit overlaps with another edit.
	 *
	 * @param other the other text edit to check, never null
	 * @return true if the edits overlap, false otherwise
	 */
	public boolean overlapsWith( TextEdit other)
	{
		// Check if ranges overlap: this.start <= other.end && other.start <= this.end
		return range.start().compareTo(other.range.end()) <= 0 &&
		       other.range.start().compareTo(range.end()) <= 0;
	}

	/**
	 * Compares text edits by position for ordering.
	 * <p>
	 * Edits are ordered by their starting position, then by ending position.
	 * This enables correct application of edits in reverse order.
	 *
	 * @param other the other text edit to compare with
	 * @return comparison result for sorting
	 */
	@Override
	public int compareTo( TextEdit other)
	{
		int startComparison = range.start().compareTo(other.range.start());
		if (startComparison != 0)
		{
			return startComparison;
		}
		return range.end().compareTo(other.range.end());
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj) return true;
		if (obj == null || getClass() != obj.getClass()) return false;
		TextEdit textEdit = (TextEdit) obj;
		return Objects.equals(range, textEdit.range) &&
			Objects.equals(replacement, textEdit.replacement) &&
			Objects.equals(ruleId, textEdit.ruleId) &&
			priority == textEdit.priority;
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(range, replacement, ruleId, priority);
	}

	@Override
	public String toString()
	{
		return "TextEdit{" +
			"range=" + range +
			", replacement='" + replacement + '\'' +
			", ruleId='" + ruleId + '\'' +
			", priority=" + priority +
			'}';
	}
}