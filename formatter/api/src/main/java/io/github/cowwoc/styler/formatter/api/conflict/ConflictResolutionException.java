package io.github.cowwoc.styler.formatter.api.conflict;

import java.util.Objects;

/**
 * Checked exception thrown when conflicts between formatting rules cannot be automatically resolved.
 * <p>
 * This exception indicates that the configured resolution strategy is unable to determine
 * which modifications should be applied. The exception includes a detailed {@link ConflictReport}
 * to assist users in understanding and resolving the conflicts manually.
 * <p>
 * Common scenarios that trigger this exception:
 * <ul>
 * <li>FailFastResolutionStrategy encounters any conflict (strict mode)</li>
 * <li>Modifications have equal priority and cannot be merged (no tiebreaker)</li>
 * <li>Circular dependencies between modifications</li>
 * <li>Resource limits exceeded during resolution</li>
 * </ul>
 */
public class ConflictResolutionException extends Exception
{
	private static final long serialVersionUID = 1L;

	private final ConflictReport report;

	/**
	 * Constructs a new conflict resolution exception with the specified detail message and conflict report.
	 *
	 * @param message detailed message explaining why conflicts could not be resolved, never {@code null}
	 * @param report comprehensive conflict information for user analysis, never {@code null}
	 * @throws IllegalArgumentException if {@code message} or {@code report} is null
	 */
	public ConflictResolutionException(String message, ConflictReport report)
	{
		super(message);
		this.report = Objects.requireNonNull(report, "report cannot be null");
	}

	/**
	 * Constructs a new conflict resolution exception with the specified detail message, conflict report,
	 * and underlying cause.
	 *
	 * @param message detailed message explaining why conflicts could not be resolved, never {@code null}
	 * @param report comprehensive conflict information for user analysis, never {@code null}
	 * @param cause the underlying exception that caused the resolution failure, may be {@code null}
	 * @throws IllegalArgumentException if {@code message} or {@code report} is null
	 */
	public ConflictResolutionException(String message, ConflictReport report, Throwable cause)
	{
		super(message, cause);
		this.report = Objects.requireNonNull(report, "report cannot be null");
	}

	/**
	 * Gets the detailed conflict report associated with this exception.
	 *
	 * @return conflict report containing all detected conflicts, never {@code null}
	 */
	public ConflictReport getReport()
	{
		return report;
	}
}
