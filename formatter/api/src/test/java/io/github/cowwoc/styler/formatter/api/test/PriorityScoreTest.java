package io.github.cowwoc.styler.formatter.api.test;

import io.github.cowwoc.styler.formatter.api.ViolationSeverity;
import io.github.cowwoc.styler.formatter.api.report.PriorityScore;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link PriorityScore} priority calculation and ordering.
 */
public final class PriorityScoreTest
{
	/**
	 * Verifies ERROR severity with frequency 1 produces priority score 100.
	 */
	@Test
	public void errorWithFrequency1HasScore100()
	{
		PriorityScore score = PriorityScore.of(ViolationSeverity.ERROR, 1);
		assertThat(score.value()).isEqualTo(100);
	}

	/**
	 * Verifies WARNING severity with frequency 1 produces priority score 10.
	 */
	@Test
	public void warningWithFrequency1HasScore10()
	{
		PriorityScore score = PriorityScore.of(ViolationSeverity.WARNING, 1);
		assertThat(score.value()).isEqualTo(10);
	}

	/**
	 * Verifies INFO severity with frequency 1 produces priority score 1.
	 */
	@Test
	public void infoWithFrequency1HasScore1()
	{
		PriorityScore score = PriorityScore.of(ViolationSeverity.INFO, 1);
		assertThat(score.value()).isEqualTo(1);
	}

	/**
	 * Verifies ERROR severity multiplies frequency correctly (ERROR weight = 100).
	 */
	@Test
	public void errorWithFrequency5HasScore500()
	{
		PriorityScore score = PriorityScore.of(ViolationSeverity.ERROR, 5);
		assertThat(score.value()).isEqualTo(500);
	}

	/**
	 * Verifies natural ordering is descending (higher priority first).
	 */
	@Test
	public void compareToPutsHigherPriorityFirst()
	{
		PriorityScore low = PriorityScore.of(ViolationSeverity.INFO, 1);
		PriorityScore high = PriorityScore.of(ViolationSeverity.ERROR, 1);

		assertThat(high.compareTo(low)).isLessThan(0);
		assertThat(low.compareTo(high)).isGreaterThan(0);
	}

	/**
	 * Verifies sorting places higher priority scores first.
	 */
	@Test
	public void sortingIsDescendingByPriority()
	{
		PriorityScore info1 = PriorityScore.of(ViolationSeverity.INFO, 1);
		PriorityScore warning1 = PriorityScore.of(ViolationSeverity.WARNING, 1);
		PriorityScore error1 = PriorityScore.of(ViolationSeverity.ERROR, 1);

		List<PriorityScore> scores = new ArrayList<>();
		scores.add(info1);
		scores.add(warning1);
		scores.add(error1);
		Collections.shuffle(scores);
		Collections.sort(scores);

		assertThat(scores).containsExactly(error1, warning1, info1);
	}

	/**
	 * Verifies null severity is rejected with NullPointerException.
	 */
	@Test(expectedExceptions = NullPointerException.class)
	public void nullSeverityThrows()
	{
		PriorityScore.of(null, 1);
	}

	/**
	 * Verifies frequency of 0 is rejected with IllegalArgumentException.
	 */
	@Test(expectedExceptions = IllegalArgumentException.class)
	public void frequency0Throws()
	{
		PriorityScore.of(ViolationSeverity.ERROR, 0);
	}

	/**
	 * Verifies thread-safe concurrent score creation.
	 */
	@Test
	public void concurrentScoreCreationIsThreadSafe() throws Exception
	{
		ExecutorService executor = Executors.newFixedThreadPool(10);
		try
		{
			List<Future<PriorityScore>> futures = new ArrayList<>();

			for (int i = 0; i < 100; ++i)
			{
				futures.add(executor.submit(() -> PriorityScore.of(ViolationSeverity.ERROR, 5)));
			}

			for (Future<PriorityScore> future : futures)
			{
				PriorityScore score = future.get();
				assertThat(score.value()).isEqualTo(500);
			}
		}
		finally
		{
			executor.shutdown();
		}
	}
}
