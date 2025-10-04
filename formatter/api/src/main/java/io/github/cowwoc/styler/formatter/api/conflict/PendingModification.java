package io.github.cowwoc.styler.formatter.api.conflict;

import io.github.cowwoc.styler.formatter.api.TextEdit;
import java.io.Serializable;
import java.util.Objects;

/**
 * Immutable record of a pending AST modification requested by a formatting rule.
 * <p>
 * Each formatting rule can request multiple modifications to the AST.
 * These modifications are queued as PendingModification instances before being
 * applied, enabling conflict detection and resolution.
 *
 * @param edit the text edit to apply, never {@code null}
 * @param ruleId identifier of the formatting rule that created this modification, never {@code null}
 * @param priority priority of the rule for conflict resolution (higher values win), must be non-negative
 * @param sequenceNumber order in which this modification was created, used as tiebreaker when priorities are
 *     equal, must be non-negative
 */
public record PendingModification(TextEdit edit, String ruleId, int priority, int sequenceNumber)
	implements Serializable
{
	/**
	 * Compact constructor with validation and defensive copying.
	 *
	 * @throws IllegalArgumentException if {@code edit} or {@code ruleId} is null, or if {@code priority} or
	 *     {@code sequenceNumber} is negative
	 */
	public PendingModification
	{
		Objects.requireNonNull(edit, "edit cannot be null");
		Objects.requireNonNull(ruleId, "ruleId cannot be null");

		if (priority < 0)
		{
			throw new IllegalArgumentException("priority cannot be negative: " + priority);
		}
		if (sequenceNumber < 0)
		{
			throw new IllegalArgumentException("sequenceNumber cannot be negative: " + sequenceNumber);
		}
	}
}
