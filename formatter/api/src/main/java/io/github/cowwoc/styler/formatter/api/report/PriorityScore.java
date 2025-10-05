package io.github.cowwoc.styler.formatter.api.report;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.cowwoc.styler.formatter.api.ViolationSeverity;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;

/**
 * Priority score for ordering violations by severity and frequency.
 * <p>
 * Priority is calculated using the formula: {@code priority = severity_weight × frequency}
 * where severity weights are: ERROR=100, WARNING=10, INFO=1. This design uses exact integer
 * arithmetic to avoid floating-point errors and ensures deterministic ordering across all
 * platforms.
 * </p>
 * <p>
 * This class is immutable and thread-safe. Instances can be safely shared across multiple
 * threads without synchronization.
 * </p>
 * <h2>Priority Ordering</h2>
 * <p>
 * Natural ordering is descending (higher priority first):
 * </p>
 * <ul>
 * <li>ERROR with frequency 1 → priority 100</li>
 * <li>WARNING with frequency 10 → priority 100 (equal to single ERROR)</li>
 * <li>WARNING with frequency 1 → priority 10</li>
 * <li>INFO with frequency 10 → priority 10 (equal to single WARNING)</li>
 * <li>INFO with frequency 1 → priority 1</li>
 * </ul>
 *
 * @see ViolationSeverity
 */
public final class PriorityScore implements Comparable<PriorityScore>
{
	private static final int ERROR_WEIGHT = 100;
	private static final int WARNING_WEIGHT = 10;
	private static final int INFO_WEIGHT = 1;

	private final int score;

	/**
	 * Creates a priority score with the specified value.
	 * <p>
	 * This constructor is private. Use {@link #of(ViolationSeverity, int)} to create instances.
	 * </p>
	 *
	 * @param score the calculated priority score, must be greater than 0
	 */
	private PriorityScore(int score)
	{
		this.score = score;
	}

	/**
	 * Creates a priority score from severity and frequency.
	 * <p>
	 * Priority is calculated using exact integer arithmetic: {@code severity_weight × frequency}.
	 * Severity weights are: ERROR=100, WARNING=10, INFO=1.
	 * </p>
	 *
	 * @param severity  the violation severity, must not be {@code null}
	 * @param frequency the number of occurrences, must be ≥ 1
	 * @return the calculated priority score, never {@code null}
	 * @throws NullPointerException     if {@code severity} is {@code null}
	 * @throws IllegalArgumentException if {@code frequency} is less than 1
	 */
	public static PriorityScore of(ViolationSeverity severity, int frequency)
	{
		requireThat(severity, "severity").isNotNull();
		requireThat(frequency, "frequency").isGreaterThanOrEqualTo(1);

		int weight = calculateWeight(severity);
		return new PriorityScore(weight * frequency);
	}

	/**
	 * Creates a priority score from a numeric value for JSON deserialization.
	 * <p>
	 * This factory method is used by Jackson for deserializing JSON. It creates a priority score
	 * directly from the numeric value without requiring severity and frequency.
	 * </p>
	 *
	 * @param value the priority score value, must be ≥ 1
	 * @return the priority score with the specified value, never {@code null}
	 * @throws IllegalArgumentException if {@code value} is less than 1
	 */
	@JsonCreator
	public static PriorityScore fromValue(@JsonProperty("value") int value)
	{
		requireThat(value, "value").isGreaterThanOrEqualTo(1);
		return new PriorityScore(value);
	}

	/**
	 * Calculates the weight for a given severity level.
	 * <p>
	 * Weight values ensure ERROR violations are 10× more important than WARNING,
	 * and WARNING violations are 10× more important than INFO.
	 * </p>
	 *
	 * @param severity the violation severity, never {@code null}
	 * @return the severity weight (ERROR=100, WARNING=10, INFO=1)
	 */
	private static int calculateWeight(ViolationSeverity severity)
	{
		return switch (severity)
		{
			case ERROR -> ERROR_WEIGHT;
			case WARNING -> WARNING_WEIGHT;
			case INFO -> INFO_WEIGHT;
		};
	}

	/**
	 * Returns the numeric priority score value.
	 * <p>
	 * Higher values indicate higher priority. This value is used for sorting violations
	 * in descending order of importance.
	 * </p>
	 *
	 * @return the priority score value, always ≥ 1
	 */
	@JsonProperty("value")
	public int value()
	{
		return score;
	}

	/**
	 * Compares this priority score with another for ordering.
	 * <p>
	 * Natural ordering is <strong>descending</strong> (higher priority first).
	 * This means violations with higher scores appear first in sorted collections.
	 * </p>
	 *
	 * @param other the priority score to compare with, must not be {@code null}
	 * @return negative if this has lower priority, zero if equal, positive if this has higher priority
	 * @throws NullPointerException if {@code other} is {@code null}
	 */
	@Override
	public int compareTo(PriorityScore other)
	{
		requireThat(other, "other").isNotNull();
		// Higher priority first (descending order)
		return Integer.compare(other.score, this.score);
	}

	/**
	 * Compares this priority score with another object for equality.
	 * <p>
	 * Two priority scores are equal if they have the same numeric value.
	 * </p>
	 *
	 * @param obj the object to compare with, may be {@code null}
	 * @return {@code true} if {@code obj} is a PriorityScore with the same value, {@code false} otherwise
	 */
	@Override
	public boolean equals(Object obj)
	{
		return obj instanceof PriorityScore other && this.score == other.score;
	}

	/**
	 * Returns a hash code for this priority score.
	 *
	 * @return hash code based on the numeric score value
	 */
	@Override
	public int hashCode()
	{
		return Integer.hashCode(score);
	}

	/**
	 * Returns a string representation of this priority score.
	 * <p>
	 * Format: {@code PriorityScore[score=X]} where X is the numeric value.
	 * </p>
	 *
	 * @return string representation, never {@code null}
	 */
	@Override
	public String toString()
	{
		return "PriorityScore[score=" + score + "]";
	}
}
