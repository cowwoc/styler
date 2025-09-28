package io.github.cowwoc.styler.formatter.api;

import io.github.cowwoc.styler.ast.SourceRange;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;

/**
 * Represents a formatting violation detected by a rule.
 * <p>
 * Violations indicate issues in the source code that either cannot be automatically
 * fixed or are configured to only be reported rather than automatically corrected.
 * This enables rules to provide feedback to developers without making changes.
 */
public final class FormattingViolation
{
	private final String ruleId;
	private final SourceRange location;
	private final String message;
	private final ViolationSeverity severity;
	private final String suggestedFix;

	/**
	 * Creates a new formatting violation.
	 *
	 * @param ruleId       the ID of the rule that detected the violation, never null
	 * @param location     the location of the violation in the source code, never null
	 * @param message      a human-readable description of the violation, never null
	 * @param severity     the severity level of the violation, never null
	 * @param suggestedFix an optional suggested fix for the violation, may be null
	 */
	public FormattingViolation(@Nonnull String ruleId,
	                           @Nonnull SourceRange location,
	                           @Nonnull String message,
	                           @Nonnull ViolationSeverity severity,
	                           @Nullable String suggestedFix)
	{
		this.ruleId = Objects.requireNonNull(ruleId, "Rule ID cannot be null");
		this.location = Objects.requireNonNull(location, "Location cannot be null");
		this.message = Objects.requireNonNull(message, "Message cannot be null");
		this.severity = Objects.requireNonNull(severity, "Severity cannot be null");
		this.suggestedFix = suggestedFix;
	}

	/**
	 * Creates a new formatting violation without a suggested fix.
	 *
	 * @param ruleId   the ID of the rule that detected the violation, never null
	 * @param location the location of the violation in the source code, never null
	 * @param message  a human-readable description of the violation, never null
	 * @param severity the severity level of the violation, never null
	 * @return a new formatting violation, never null
	 */
	@Nonnull
	public static FormattingViolation create(@Nonnull String ruleId,
	                                          @Nonnull SourceRange location,
	                                          @Nonnull String message,
	                                          @Nonnull ViolationSeverity severity)
	{
		return new FormattingViolation(ruleId, location, message, severity, null);
	}

	/**
	 * Creates a warning-level formatting violation.
	 *
	 * @param ruleId   the ID of the rule that detected the violation, never null
	 * @param location the location of the violation in the source code, never null
	 * @param message  a human-readable description of the violation, never null
	 * @return a new warning violation, never null
	 */
	@Nonnull
	public static FormattingViolation warning(@Nonnull String ruleId,
	                                           @Nonnull SourceRange location,
	                                           @Nonnull String message)
	{
		return create(ruleId, location, message, ViolationSeverity.WARNING);
	}

	/**
	 * Creates an error-level formatting violation.
	 *
	 * @param ruleId   the ID of the rule that detected the violation, never null
	 * @param location the location of the violation in the source code, never null
	 * @param message  a human-readable description of the violation, never null
	 * @return a new error violation, never null
	 */
	@Nonnull
	public static FormattingViolation error(@Nonnull String ruleId,
	                                         @Nonnull SourceRange location,
	                                         @Nonnull String message)
	{
		return create(ruleId, location, message, ViolationSeverity.ERROR);
	}

	/**
	 * Returns the ID of the rule that detected this violation.
	 *
	 * @return the rule ID, never null
	 */
	@Nonnull
	public String getRuleId()
	{
		return ruleId;
	}

	/**
	 * Returns the location of the violation in the source code.
	 *
	 * @return the source location, never null
	 */
	@Nonnull
	public SourceRange getLocation()
	{
		return location;
	}

	/**
	 * Returns a human-readable description of the violation.
	 *
	 * @return the violation message, never null
	 */
	@Nonnull
	public String getMessage()
	{
		return message;
	}

	/**
	 * Returns the severity level of this violation.
	 *
	 * @return the violation severity, never null
	 */
	@Nonnull
	public ViolationSeverity getSeverity()
	{
		return severity;
	}

	/**
	 * Returns a suggested fix for this violation, if available.
	 *
	 * @return the suggested fix, or null if no fix is suggested
	 */
	@Nullable
	public String getSuggestedFix()
	{
		return suggestedFix;
	}

	/**
	 * Returns whether this violation has a suggested fix.
	 *
	 * @return true if a suggested fix is available, false otherwise
	 */
	public boolean hasSuggestedFix()
	{
		return suggestedFix != null && !suggestedFix.trim().isEmpty();
	}

	/**
	 * Returns whether this violation is an error.
	 *
	 * @return true if the severity is ERROR, false otherwise
	 */
	public boolean isError()
	{
		return severity == ViolationSeverity.ERROR;
	}

	/**
	 * Returns whether this violation is a warning.
	 *
	 * @return true if the severity is WARNING, false otherwise
	 */
	public boolean isWarning()
	{
		return severity == ViolationSeverity.WARNING;
	}

	/**
	 * Returns whether this violation is informational.
	 *
	 * @return true if the severity is INFO, false otherwise
	 */
	public boolean isInfo()
	{
		return severity == ViolationSeverity.INFO;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj) return true;
		if (obj == null || getClass() != obj.getClass()) return false;
		FormattingViolation that = (FormattingViolation) obj;
		return Objects.equals(ruleId, that.ruleId) &&
			Objects.equals(location, that.location) &&
			Objects.equals(message, that.message) &&
			severity == that.severity &&
			Objects.equals(suggestedFix, that.suggestedFix);
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(ruleId, location, message, severity, suggestedFix);
	}

	@Override
	public String toString()
	{
		return severity + " [" + ruleId + "] " + message + " at " + location +
			(hasSuggestedFix() ? " (suggested fix: " + suggestedFix + ")" : "");
	}
}