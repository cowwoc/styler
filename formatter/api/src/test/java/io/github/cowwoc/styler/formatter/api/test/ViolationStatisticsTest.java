package io.github.cowwoc.styler.formatter.api.test;

import io.github.cowwoc.styler.formatter.api.ViolationSeverity;
import io.github.cowwoc.styler.formatter.api.report.ViolationStatistics;
import org.testng.annotations.Test;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link ViolationStatistics} record validation and immutability.
 */
public final class ViolationStatisticsTest
{
	/**
	 * Verifies record stores all fields correctly.
	 */
	@Test
	public void recordStoresAllFields()
	{
		Map<ViolationSeverity, Integer> severityCounts = Map.of(
			ViolationSeverity.ERROR, 3,
			ViolationSeverity.WARNING, 2);
		Map<String, Integer> ruleIdCounts = Map.of(
			"rule-a", 3,
			"rule-b", 2);

		ViolationStatistics stats = new ViolationStatistics(5, severityCounts, ruleIdCounts);

		assertThat(stats.totalViolations()).isEqualTo(5);
		assertThat(stats.severityCounts()).isEqualTo(severityCounts);
		assertThat(stats.ruleIdCounts()).isEqualTo(ruleIdCounts);
	}

	/**
	 * Verifies severity counts map is immutable.
	 */
	@Test(expectedExceptions = UnsupportedOperationException.class)
	public void severityCountsMapIsImmutable()
	{
		ViolationStatistics stats = new ViolationStatistics(1,
			Map.of(ViolationSeverity.ERROR, 1), Map.of("rule", 1));

		stats.severityCounts().put(ViolationSeverity.WARNING, 1);
	}

	/**
	 * Verifies rule ID counts map is immutable.
	 */
	@Test(expectedExceptions = UnsupportedOperationException.class)
	public void ruleIdCountsMapIsImmutable()
	{
		ViolationStatistics stats = new ViolationStatistics(1,
			Map.of(ViolationSeverity.ERROR, 1), Map.of("rule", 1));

		stats.ruleIdCounts().put("new-rule", 1);
	}

	/**
	 * Verifies defensive copying prevents external mutation.
	 */
	@Test
	public void defensiveCopyingPreventsExternalMutation()
	{
		Map<ViolationSeverity, Integer> severityCounts = new EnumMap<>(ViolationSeverity.class);
		severityCounts.put(ViolationSeverity.ERROR, 1);
		Map<String, Integer> ruleIdCounts = new HashMap<>();
		ruleIdCounts.put("rule", 1);

		ViolationStatistics stats = new ViolationStatistics(1, severityCounts, ruleIdCounts);

		severityCounts.put(ViolationSeverity.WARNING, 2);
		ruleIdCounts.put("new-rule", 2);

		assertThat(stats.severityCounts()).hasSize(1);
		assertThat(stats.ruleIdCounts()).hasSize(1);
	}

	/**
	 * Verifies negative total violations throws IllegalArgumentException.
	 */
	@Test(expectedExceptions = IllegalArgumentException.class)
	public void negativeTotalViolationsThrows()
	{
		new ViolationStatistics(-1, Map.of(), Map.of());
	}

	/**
	 * Verifies null severity counts throws NullPointerException.
	 */
	@Test(expectedExceptions = NullPointerException.class)
	public void nullSeverityCountsThrows()
	{
		new ViolationStatistics(0, null, Map.of());
	}

	/**
	 * Verifies null rule ID counts throws NullPointerException.
	 */
	@Test(expectedExceptions = NullPointerException.class)
	public void nullRuleIdCountsThrows()
	{
		new ViolationStatistics(0, Map.of(), null);
	}

	/**
	 * Verifies zero total violations is valid.
	 */
	@Test
	public void zeroTotalViolationsIsValid()
	{
		ViolationStatistics stats = new ViolationStatistics(0, Map.of(), Map.of());

		assertThat(stats.totalViolations()).isEqualTo(0);
	}

	/**
	 * Verifies records with same values are equal.
	 */
	@Test
	public void equalityWorksForSameValues()
	{
		Map<ViolationSeverity, Integer> severityCounts = Map.of(ViolationSeverity.ERROR, 1);
		Map<String, Integer> ruleIdCounts = Map.of("rule", 1);

		ViolationStatistics stats1 = new ViolationStatistics(1, severityCounts, ruleIdCounts);
		ViolationStatistics stats2 = new ViolationStatistics(1, severityCounts, ruleIdCounts);

		assertThat(stats1).isEqualTo(stats2);
		assertThat(stats1.hashCode()).isEqualTo(stats2.hashCode());
	}

	/**
	 * Verifies getCountBySeverity returns correct count for existing severity.
	 */
	@Test
	public void getCountBySeverityReturnsCountForExistingSeverity()
	{
		Map<ViolationSeverity, Integer> severityCounts = Map.of(ViolationSeverity.ERROR, 5);
		ViolationStatistics stats = new ViolationStatistics(5, severityCounts, Map.of());

		assertThat(stats.getCountBySeverity(ViolationSeverity.ERROR)).isEqualTo(5);
	}

	/**
	 * Verifies getCountBySeverity returns zero for missing severity.
	 */
	@Test
	public void getCountBySeverityReturnsZeroForMissingSeverity()
	{
		Map<ViolationSeverity, Integer> severityCounts = Map.of(ViolationSeverity.ERROR, 5);
		ViolationStatistics stats = new ViolationStatistics(5, severityCounts, Map.of());

		assertThat(stats.getCountBySeverity(ViolationSeverity.WARNING)).isEqualTo(0);
		assertThat(stats.getCountBySeverity(ViolationSeverity.INFO)).isEqualTo(0);
	}

	/**
	 * Verifies getCountBySeverity with null severity throws NullPointerException.
	 */
	@Test(expectedExceptions = NullPointerException.class)
	public void getCountBySeverityWithNullThrows()
	{
		ViolationStatistics stats = new ViolationStatistics(0, Map.of(), Map.of());

		stats.getCountBySeverity(null);
	}

	/**
	 * Verifies getCountByRuleId returns correct count for existing rule.
	 */
	@Test
	public void getCountByRuleIdReturnsCountForExistingRule()
	{
		Map<String, Integer> ruleIdCounts = Map.of("test-rule", 3);
		ViolationStatistics stats = new ViolationStatistics(3, Map.of(), ruleIdCounts);

		assertThat(stats.getCountByRuleId("test-rule")).isEqualTo(3);
	}

	/**
	 * Verifies getCountByRuleId returns zero for missing rule.
	 */
	@Test
	public void getCountByRuleIdReturnsZeroForMissingRule()
	{
		Map<String, Integer> ruleIdCounts = Map.of("test-rule", 3);
		ViolationStatistics stats = new ViolationStatistics(3, Map.of(), ruleIdCounts);

		assertThat(stats.getCountByRuleId("other-rule")).isEqualTo(0);
	}

	/**
	 * Verifies getCountByRuleId with null rule ID throws NullPointerException.
	 */
	@Test(expectedExceptions = NullPointerException.class)
	public void getCountByRuleIdWithNullThrows()
	{
		ViolationStatistics stats = new ViolationStatistics(0, Map.of(), Map.of());

		stats.getCountByRuleId(null);
	}
}
