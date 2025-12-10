package io.github.cowwoc.styler.pipeline.parallel.test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.testng.annotations.Test;

import io.github.cowwoc.styler.pipeline.PipelineResult;
import io.github.cowwoc.styler.pipeline.parallel.BatchResult;

/**
 * Unit tests for {@code BatchResult}.
 */
public class BatchResultTest
{
	/**
	 * Tests that success count is validated correctly.
	 */
	@Test
	public void shouldValidateSuccessCount()
	{
		List<PipelineResult> results = List.of();
		Map<Path, String> errors = Map.of();

		BatchResult batchResult = new BatchResult(10, 5, 5, results, errors, Duration.ofSeconds(1), 10.0);

		assertEquals(batchResult.totalFiles(), 10);
		assertEquals(batchResult.successCount(), 5);
		assertEquals(batchResult.failureCount(), 5);
	}

	/**
	 * Tests that sum of success and failure counts equals total files.
	 */
	@Test
	public void shouldValidateSumOfSuccessAndFailure()
	{
		List<PipelineResult> results = List.of();
		Map<Path, String> errors = Map.of();

		BatchResult batchResult = new BatchResult(10, 6, 4, results, errors, Duration.ofSeconds(1), 10.0);

		assertEquals(batchResult.successCount() + batchResult.failureCount(), 10);
	}

	/**
	 * Tests that mismatched counts are rejected.
	 */
	@Test(expectedExceptions = IllegalArgumentException.class)
	public void shouldRejectMismatchedCounts()
	{
		List<PipelineResult> results = List.of();
		Map<Path, String> errors = Map.of();

		new BatchResult(10, 5, 4, results, errors, Duration.ofSeconds(1), 10.0);
	}

	/**
	 * Tests that null results list is rejected.
	 */
	@Test(expectedExceptions = NullPointerException.class)
	public void shouldRejectNullResults()
	{
		Map<Path, String> errors = Map.of();

		new BatchResult(0, 0, 0, null, errors, Duration.ofSeconds(1), 10.0);
	}

	/**
	 * Tests that null errors map is rejected.
	 */
	@Test(expectedExceptions = NullPointerException.class)
	public void shouldRejectNullErrors()
	{
		List<PipelineResult> results = List.of();

		new BatchResult(0, 0, 0, results, null, Duration.ofSeconds(1), 10.0);
	}

	/**
	 * Tests that null duration is rejected.
	 */
	@Test(expectedExceptions = NullPointerException.class)
	public void shouldRejectNullDuration()
	{
		List<PipelineResult> results = List.of();
		Map<Path, String> errors = Map.of();

		new BatchResult(0, 0, 0, results, errors, null, 10.0);
	}

	/**
	 * Tests that negative throughput is rejected.
	 */
	@Test(expectedExceptions = IllegalArgumentException.class)
	public void shouldRejectNegativeThroughput()
	{
		List<PipelineResult> results = List.of();
		Map<Path, String> errors = Map.of();

		new BatchResult(10, 5, 5, results, errors, Duration.ofSeconds(1), -1.0);
	}

	/**
	 * Tests that allSucceeded returns true when all files succeeded.
	 */
	@Test
	public void allSucceededShouldReturnTrue()
	{
		List<PipelineResult> results = List.of();
		Map<Path, String> errors = Map.of();

		BatchResult batchResult = new BatchResult(5, 5, 0, results, errors, Duration.ofSeconds(1), 5.0);

		assertTrue(batchResult.allSucceeded());
	}

	/**
	 * Tests that allSucceeded returns false when some files failed.
	 */
	@Test
	public void allSucceededShouldReturnFalse()
	{
		List<PipelineResult> results = List.of();
		Map<Path, String> errors = Map.of();

		BatchResult batchResult = new BatchResult(5, 3, 2, results, errors, Duration.ofSeconds(1), 3.0);

		assertFalse(batchResult.allSucceeded());
	}

	/**
	 * Tests that hasFailed returns true when some files failed.
	 */
	@Test
	public void hasFailedShouldReturnTrue()
	{
		List<PipelineResult> results = List.of();
		Map<Path, String> errors = Map.of();

		BatchResult batchResult = new BatchResult(5, 3, 2, results, errors, Duration.ofSeconds(1), 3.0);

		assertTrue(batchResult.hasFailed());
	}

	/**
	 * Tests that hasFailed returns false when all files succeeded.
	 */
	@Test
	public void hasFailedShouldReturnFalse()
	{
		List<PipelineResult> results = List.of();
		Map<Path, String> errors = Map.of();

		BatchResult batchResult = new BatchResult(5, 5, 0, results, errors, Duration.ofSeconds(1), 5.0);

		assertFalse(batchResult.hasFailed());
	}

	/**
	 * Tests that success rate is calculated correctly.
	 */
	@Test
	public void successRateShouldCalculateCorrectly()
	{
		List<PipelineResult> results = List.of();
		Map<Path, String> errors = Map.of();

		BatchResult batchResult = new BatchResult(10, 7, 3, results, errors, Duration.ofSeconds(1), 7.0);

		assertEquals(batchResult.successRate(), 70.0, 0.01);
	}

	/**
	 * Tests that success rate with zero files returns zero.
	 */
	@Test
	public void successRateWithZeroFilesShouldReturnZero()
	{
		List<PipelineResult> results = List.of();
		Map<Path, String> errors = Map.of();

		BatchResult batchResult = new BatchResult(0, 0, 0, results, errors, Duration.ofSeconds(1), 0.0);

		assertEquals(batchResult.successRate(), 0.0, 0.01);
	}

	/**
	 * Tests that results collection is immutable.
	 */
	@Test(expectedExceptions = UnsupportedOperationException.class)
	public void shouldMakeResultsImmutable()
	{
		List<PipelineResult> results = new java.util.ArrayList<>();
		Map<Path, String> errors = new HashMap<>();

		BatchResult batchResult = new BatchResult(0, 0, 0, results, errors, Duration.ofSeconds(1), 0.0);

		batchResult.results().add(null);
	}

	/**
	 * Tests that errors map is immutable.
	 */
	@Test(expectedExceptions = UnsupportedOperationException.class)
	public void shouldMakeErrorsImmutable()
	{
		List<PipelineResult> results = List.of();
		Map<Path, String> errors = new HashMap<>();
		errors.put(Paths.get("test.java"), "error");

		BatchResult batchResult = new BatchResult(1, 0, 1, results, errors, Duration.ofSeconds(1), 0.0);

		batchResult.errors().put(Paths.get("test2.java"), "error2");
	}
}
