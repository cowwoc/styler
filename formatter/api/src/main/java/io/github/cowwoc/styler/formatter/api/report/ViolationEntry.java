package io.github.cowwoc.styler.formatter.api.report;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.cowwoc.styler.ast.SourceRange;
import io.github.cowwoc.styler.formatter.api.ViolationSeverity;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;

/**
 * A single violation entry in a structured report.
 * <p>
 * Contains all metadata for a formatting violation including rule ID, severity, message,
 * source location, priority score, and optional fix suggestion for AI agent learning.
 * </p>
 * <p>
 * This record is immutable and thread-safe. Instances can be safely shared across multiple
 * threads without synchronization.
 * </p>
 * <h2>Usage Example</h2>
 * <pre>{@code
 * PriorityScore priority = PriorityScore.of(ViolationSeverity.ERROR, 1);
 * SourceRange range = ...;
 *
 * ViolationEntry entry = new ViolationEntry(
 *     "IndentationRule",
 *     ViolationSeverity.ERROR,
 *     "Incorrect indentation: expected 4 spaces, found 2",
 *     range,
 *     priority,
 *     "Add 2 spaces at line start"
 * );
 * }</pre>
 *
 * @param ruleId        the ID of the rule that detected the violation, must not be blank
 * @param severity      the severity level of the violation, must not be {@code null}
 * @param message       a human-readable description of the violation, must not be blank
 * @param sourceRange   the location of the violation in the source code, must not be {@code null}
 * @param priorityScore the calculated priority for ordering, must not be {@code null}
 * @param fixSuggestion an optional AI learning hint for fixing the violation, may be {@code null}
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ViolationEntry(@JsonProperty("ruleId") String ruleId,
	@JsonProperty("severity") ViolationSeverity severity, @JsonProperty("message") String message,
	@JsonProperty("sourceRange") SourceRange sourceRange,
	@JsonProperty("priorityScore") PriorityScore priorityScore,
	@JsonProperty("fixSuggestion") String fixSuggestion)
{
	/**
	 * Compact constructor for validation.
	 * <p>
	 * Validates that all required fields are non-null and non-blank. The {@code fixSuggestion}
	 * parameter may be {@code null} if no fix suggestion is available.
	 * </p>
	 *
	 * @throws IllegalArgumentException if {@code ruleId} or {@code message} is blank
	 * @throws NullPointerException     if any required parameter is {@code null}
	 */
	@JsonCreator
	public ViolationEntry
	{
		requireThat(ruleId, "ruleId").isNotBlank();
		requireThat(severity, "severity").isNotNull();
		requireThat(message, "message").isNotBlank();
		requireThat(sourceRange, "sourceRange").isNotNull();
		requireThat(priorityScore, "priorityScore").isNotNull();
		// fixSuggestion may be null - no validation needed
	}

	/**
	 * Returns whether this violation has a fix suggestion.
	 * <p>
	 * A fix suggestion is present if the {@code fixSuggestion} field is non-null and non-blank.
	 * </p>
	 *
	 * @return {@code true} if a fix suggestion is available, {@code false} otherwise
	 */
	@JsonIgnore
	public boolean hasFixSuggestion()
	{
		return fixSuggestion != null && !fixSuggestion.isBlank();
	}

	/**
	 * Returns whether this violation is an error.
	 * <p>
	 * Convenience method for checking if severity is {@link ViolationSeverity#ERROR}.
	 * </p>
	 *
	 * @return {@code true} if the severity is ERROR, {@code false} otherwise
	 */
	@JsonIgnore
	public boolean isError()
	{
		return severity == ViolationSeverity.ERROR;
	}

	/**
	 * Returns whether this violation is a warning.
	 * <p>
	 * Convenience method for checking if severity is {@link ViolationSeverity#WARNING}.
	 * </p>
	 *
	 * @return {@code true} if the severity is WARNING, {@code false} otherwise
	 */
	@JsonIgnore
	public boolean isWarning()
	{
		return severity == ViolationSeverity.WARNING;
	}

	/**
	 * Returns whether this violation is informational.
	 * <p>
	 * Convenience method for checking if severity is {@link ViolationSeverity#INFO}.
	 * </p>
	 *
	 * @return {@code true} if the severity is INFO, {@code false} otherwise
	 */
	@JsonIgnore
	public boolean isInfo()
	{
		return severity == ViolationSeverity.INFO;
	}
}
