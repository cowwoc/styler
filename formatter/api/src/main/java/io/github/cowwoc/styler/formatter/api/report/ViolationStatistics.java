package io.github.cowwoc.styler.formatter.api.report;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.cowwoc.styler.formatter.api.ViolationSeverity;

import java.util.Map;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;

/**
 * Statistics about violations in a formatting report.
 * <p>
 * Provides aggregated counts by severity level and rule ID. All collections are immutable
 * to ensure thread safety. Statistics are calculated once during report construction and
 * cannot be modified afterward.
 * </p>
 * <p>
 * This record is immutable and thread-safe. Instances can be safely shared across multiple
 * threads without synchronization.
 * </p>
 * <h2>Usage Example</h2>
 * <pre>{@code
 * Map<ViolationSeverity, Integer> severityCounts = Map.of(
 *     ViolationSeverity.ERROR, 5,
 *     ViolationSeverity.WARNING, 12
 * );
 * Map<String, Integer> ruleIdCounts = Map.of(
 *     "IndentationRule", 10,
 *     "LineLengthRule", 7
 * );
 * ViolationStatistics stats = new ViolationStatistics(17, severityCounts, ruleIdCounts);
 * }</pre>
 *
 * @param totalViolations total count of violations, must be ≥ 0
 * @param severityCounts  violations grouped by severity, never {@code null}
 * @param ruleIdCounts    violations grouped by rule ID, never {@code null}
 */
public record ViolationStatistics(@JsonProperty("totalViolations") int totalViolations,
	@JsonProperty("severityCounts") Map<ViolationSeverity, Integer> severityCounts,
	@JsonProperty("ruleIdCounts") Map<String, Integer> ruleIdCounts)
{
	/**
	 * Compact constructor for validation and defensive copying.
	 * <p>
	 * Validates that totalViolations is non-negative and creates defensive copies
	 * of the map parameters to ensure immutability. External modifications to the
	 * original maps will not affect this statistics object.
	 * </p>
	 *
	 * @throws IllegalArgumentException if {@code totalViolations} is negative
	 * @throws NullPointerException     if {@code severityCounts} or {@code ruleIdCounts} is {@code null}
	 */
	@JsonCreator
	public ViolationStatistics
	{
		requireThat(totalViolations, "totalViolations").isGreaterThanOrEqualTo(0);
		requireThat(severityCounts, "severityCounts").isNotNull();
		requireThat(ruleIdCounts, "ruleIdCounts").isNotNull();

		// Defensive copying to ensure immutability
		severityCounts = Map.copyOf(severityCounts);
		ruleIdCounts = Map.copyOf(ruleIdCounts);
	}

	/**
	 * Returns the count of violations for a specific severity level.
	 * <p>
	 * If the severity level has no violations, returns 0.
	 * </p>
	 *
	 * @param severity the severity level to query, must not be {@code null}
	 * @return the violation count for this severity, or 0 if none
	 * @throws NullPointerException if {@code severity} is {@code null}
	 */
	public int getCountBySeverity(ViolationSeverity severity)
	{
		requireThat(severity, "severity").isNotNull();
		return severityCounts.getOrDefault(severity, 0);
	}

	/**
	 * Returns the count of violations for a specific rule ID.
	 * <p>
	 * If the rule ID has no violations, returns 0.
	 * </p>
	 *
	 * @param ruleId the rule identifier to query, must not be {@code null}
	 * @return the violation count for this rule, or 0 if none
	 * @throws NullPointerException if {@code ruleId} is {@code null}
	 */
	public int getCountByRuleId(String ruleId)
	{
		requireThat(ruleId, "ruleId").isNotBlank();
		return ruleIdCounts.getOrDefault(ruleId, 0);
	}
}
