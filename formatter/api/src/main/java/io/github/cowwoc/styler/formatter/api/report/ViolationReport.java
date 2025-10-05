package io.github.cowwoc.styler.formatter.api.report;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;

/**
 * A structured violation report containing violations, statistics, and metadata.
 * <p>
 * This is the aggregate root for violation reporting. It contains a collection of violations,
 * aggregated statistics, and generation metadata. Reports can be serialized to JSON or XML for
 * AI agent consumption and learning.
 * </p>
 * <p>
 * This class is immutable and thread-safe. All collections are defensively copied and returned
 * as unmodifiable views. Instances can be safely shared across multiple threads without
 * synchronization.
 * </p>
 * <h2>Usage Example</h2>
 * <pre>{@code
 * ViolationReport report = ViolationReport.builder()
 *     .addViolation(entry1)
 *     .addViolation(entry2)
 *     .statistics(stats)
 *     .build();
 * }</pre>
 *
 * @see ViolationEntry
 * @see ViolationStatistics
 * @see Builder
 */
public final class ViolationReport
{
	private final List<ViolationEntry> violations;
	private final ViolationStatistics statistics;
	private final long timestampMillis;

	/**
	 * Creates a violation report from a builder.
	 * <p>
	 * This constructor is private. Use {@link #builder()} to create instances.
	 * </p>
	 *
	 * @param builder the builder containing report data, never {@code null}
	 */
	private ViolationReport(Builder builder)
	{
		// Defensive copying to ensure immutability
		this.violations = List.copyOf(builder.violations);
		this.statistics = builder.statistics;
		this.timestampMillis = builder.timestampMillis;
	}

	/**
	 * Creates a violation report for JSON deserialization.
	 * <p>
	 * This constructor is used by Jackson for deserializing JSON. It is annotated with
	 * {@link JsonCreator} to enable property-based deserialization.
	 * </p>
	 *
	 * @param violations      the violations to include, never {@code null}
	 * @param statistics      the violation statistics, never {@code null}
	 * @param timestampMillis the generation timestamp in milliseconds, must be > 0
	 */
	@JsonCreator
	private ViolationReport(@JsonProperty("violations") List<ViolationEntry> violations,
		@JsonProperty("statistics") ViolationStatistics statistics,
		@JsonProperty("timestampMillis") long timestampMillis)
	{
		this.violations = List.copyOf(violations);
		this.statistics = statistics;
		this.timestampMillis = timestampMillis;
	}

	/**
	 * Returns a new builder for creating violation reports.
	 *
	 * @return a new builder instance, never {@code null}
	 */
	public static Builder builder()
	{
		return new Builder();
	}

	/**
	 * Creates an empty violation report.
	 * <p>
	 * Useful for representing formatting results with no violations.
	 * </p>
	 *
	 * @return an empty report with zero violations, never {@code null}
	 */
	public static ViolationReport empty()
	{
		ViolationStatistics emptyStats = new ViolationStatistics(0, Collections.emptyMap(), Collections.emptyMap());
		return builder().
			statistics(emptyStats).
			build();
	}

	/**
	 * Returns the list of violations in this report.
	 * <p>
	 * The returned list is unmodifiable. External modifications are not possible.
	 * </p>
	 *
	 * @return the violations, never {@code null}, may be empty
	 */
	@JsonProperty("violations")
	public List<ViolationEntry> violations()
	{
		// Already immutable from List.copyOf()
		return violations;
	}

	/**
	 * Returns the aggregated statistics for this report.
	 *
	 * @return the violation statistics, never {@code null}
	 */
	@JsonProperty("statistics")
	public ViolationStatistics statistics()
	{
		return statistics;
	}

	/**
	 * Returns the timestamp when this report was generated.
	 * <p>
	 * Timestamp is in milliseconds since the Unix epoch (January 1, 1970, 00:00:00 GMT).
	 * </p>
	 *
	 * @return the generation timestamp in milliseconds
	 */
	@JsonProperty("timestampMillis")
	public long timestampMillis()
	{
		return timestampMillis;
	}

	/**
	 * Returns the number of violations in this report.
	 * <p>
	 * Convenience method equivalent to {@code violations().size()}.
	 * </p>
	 *
	 * @return the violation count, always ≥ 0
	 */
	@JsonIgnore
	public int violationCount()
	{
		return violations.size();
	}

	/**
	 * Returns whether this report has no violations.
	 * <p>
	 * Convenience method equivalent to {@code violations().isEmpty()}.
	 * </p>
	 *
	 * @return {@code true} if there are no violations, {@code false} otherwise
	 */
	@JsonIgnore
	public boolean isEmpty()
	{
		return violations.isEmpty();
	}

	/**
	 * Builder for creating {@link ViolationReport} instances.
	 * <p>
	 * Provides a fluent API for constructing violation reports. The builder validates all
	 * required fields in the {@link #build()} method and ensures immutability of the created
	 * report.
	 * </p>
	 * <p>
	 * This builder is not thread-safe. Each thread should use its own builder instance.
	 * </p>
	 * <h2>Usage Example</h2>
	 * <pre>{@code
	 * ViolationReport report = ViolationReport.builder()
	 *     .violations(violationList)
	 *     .statistics(stats)
	 *     .timestampMillis(System.currentTimeMillis())
	 *     .build();
	 * }</pre>
	 */
	public static final class Builder
	{
		private List<ViolationEntry> violations = new ArrayList<>();
		private ViolationStatistics statistics;
		private long timestampMillis = System.currentTimeMillis();

		/**
		 * Creates a new builder instance.
		 * <p>
		 * This constructor is private. Use {@link ViolationReport#builder()} to create builders.
		 * </p>
		 */
		private Builder()
		{
		}

		/**
		 * Sets the violations for this report.
		 * <p>
		 * Creates a defensive copy of the provided list. External modifications to the original
		 * list will not affect this builder or the built report.
		 * </p>
		 *
		 * @param violations the violations to include, must not be {@code null}
		 * @return this builder for method chaining
		 * @throws NullPointerException if {@code violations} is {@code null}
		 */
		public Builder violations(List<ViolationEntry> violations)
		{
			requireThat(violations, "violations").isNotNull();
			this.violations = new ArrayList<>(violations);
			return this;
		}

		/**
		 * Adds a single violation to this report.
		 * <p>
		 * Violations can be added incrementally. This method can be called multiple times.
		 * </p>
		 *
		 * @param violation the violation to add, must not be {@code null}
		 * @return this builder for method chaining
		 * @throws NullPointerException if {@code violation} is {@code null}
		 */
		public Builder addViolation(ViolationEntry violation)
		{
			requireThat(violation, "violation").isNotNull();
			this.violations.add(violation);
			return this;
		}

		/**
		 * Sets the statistics for this report.
		 *
		 * @param statistics the violation statistics, must not be {@code null}
		 * @return this builder for method chaining
		 * @throws NullPointerException if {@code statistics} is {@code null}
		 */
		public Builder statistics(ViolationStatistics statistics)
		{
			requireThat(statistics, "statistics").isNotNull();
			this.statistics = statistics;
			return this;
		}

		/**
		 * Sets the generation timestamp for this report.
		 * <p>
		 * If not explicitly set, the timestamp defaults to the time when the builder was created.
		 * </p>
		 *
		 * @param timestampMillis the timestamp in milliseconds since Unix epoch, must be > 0
		 * @return this builder for method chaining
		 * @throws IllegalArgumentException if {@code timestampMillis} is ≤ 0
		 */
		public Builder timestampMillis(long timestampMillis)
		{
			requireThat(timestampMillis, "timestampMillis").isGreaterThan(0);
			this.timestampMillis = timestampMillis;
			return this;
		}

		/**
		 * Builds an immutable violation report from this builder's state.
		 * <p>
		 * Validates that all required fields are set. The {@code statistics} field is required;
		 * all other fields have sensible defaults.
		 * </p>
		 *
		 * @return the built violation report, never {@code null}
		 * @throws IllegalStateException if {@code statistics} is not set
		 */
		public ViolationReport build()
		{
			if (statistics == null)
			{
				throw new IllegalStateException("statistics must be set before calling build()");
			}
			return new ViolationReport(this);
		}
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj) return true;
		if (!(obj instanceof ViolationReport other)) return false;
		return timestampMillis == other.timestampMillis &&
			violations.equals(other.violations) &&
			statistics.equals(other.statistics);
	}

	@Override
	public int hashCode()
	{
		int result = violations.hashCode();
		result = 31 * result + statistics.hashCode();
		result = 31 * result + Long.hashCode(timestampMillis);
		return result;
	}

	@Override
	public String toString()
	{
		return "ViolationReport[" +
			"violations=" + violations.size() +
			", statistics=" + statistics +
			", timestamp=" + timestampMillis +
			"]";
	}
}
