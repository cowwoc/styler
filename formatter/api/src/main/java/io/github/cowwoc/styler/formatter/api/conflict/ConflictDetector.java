package io.github.cowwoc.styler.formatter.api.conflict;

import java.util.List;

/**
 * Detects conflicts between pending AST modifications from multiple formatting rules.
 * <p>
 * A conflict occurs when two or more pending modifications attempt to modify overlapping
 * regions of the source code. Conflict detection runs before modifications are applied
 * to the AST, enabling resolution strategies to determine which modifications should proceed.
 * <p>
 * Implementations must be thread-safe as detection may occur concurrently across
 * multiple files during parallel formatting operations.
 */
public interface ConflictDetector
{
	/**
	 * Detects conflicts between pending modifications.
	 * <p>
	 * Analyzes all pending modifications to identify overlapping source ranges that indicate
	 * conflicts. Returns a list of all detected conflicts with severity classification.
	 *
	 * @param modifications the modifications to analyze for conflicts, never {@code null}
	 * @return list of detected conflicts, never {@code null}, may be empty if no conflicts found
	 * @throws IllegalArgumentException if {@code modifications} is null or contains null elements
	 */
	List<Conflict> detectConflicts(List<PendingModification> modifications);

	/**
	 * Computes the severity of a detected conflict.
	 * <p>
	 * Severity classification helps prioritize resolution strategies and error reporting:
	 * <ul>
	 * <li>MINOR: Modifications affect different aspects (e.g., whitespace vs comments)</li>
	 * <li>MODERATE: Modifications affect related aspects and may be mergeable</li>
	 * <li>SEVERE: Direct incompatible modifications requiring explicit resolution</li>
	 * </ul>
	 *
	 * @param conflict the conflict to analyze, never {@code null}
	 * @return severity classification, never {@code null}
	 * @throws IllegalArgumentException if {@code conflict} is null
	 */
	ConflictSeverity computeSeverity(Conflict conflict);
}
