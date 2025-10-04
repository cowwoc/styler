package io.github.cowwoc.styler.formatter.api.conflict;

import java.io.Serializable;
import java.util.Objects;

/**
 * Immutable representation of a conflict between two pending modifications.
 * <p>
 * A conflict occurs when two formatting rules attempt to modify overlapping
 * regions of the source code in incompatible ways.
 *
 * @param first the first pending modification in the conflict, never {@code null}
 * @param second the second pending modification in the conflict, never {@code null}
 * @param severity the severity classification of this conflict, never {@code null}
 * @param description human-readable description of the conflict, never {@code null}
 */
public record Conflict(
	PendingModification first,
	PendingModification second,
	ConflictSeverity severity,
	String description)
	implements Serializable
{
	/**
	 * Compact constructor with validation.
	 *
	 * @throws IllegalArgumentException if any parameter is null
	 */
	public Conflict
	{
		Objects.requireNonNull(first, "first cannot be null");
		Objects.requireNonNull(second, "second cannot be null");
		Objects.requireNonNull(severity, "severity cannot be null");
		Objects.requireNonNull(description, "description cannot be null");
	}
}
