package io.github.cowwoc.styler.cli.test.pipeline;

import io.github.cowwoc.styler.cli.pipeline.BatchResult;
import io.github.cowwoc.styler.cli.pipeline.PipelineException;
import org.testng.annotations.Test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link BatchResult}.
 */
public final class BatchResultTest
{
	/**
	 * Verifies that BatchResult correctly reports success when no errors occurred.
	 */
	@Test
	public void isSuccessReturnsTrueWhenNoErrors()
	{
		BatchResult result = new BatchResult(10, 0, List.of());

		assertThat(result.isSuccess()).isTrue();
		assertThat(result.hasErrors()).isFalse();
		assertThat(result.hasPartialSuccess()).isFalse();
	}

	/**
	 * Verifies that BatchResult correctly reports failure when errors occurred.
	 */
	@Test
	public void isSuccessReturnsFalseWhenErrorsExist()
	{
		Path file = Paths.get("test.java");
		RuntimeException cause = new RuntimeException("Test cause");
		PipelineException error = new PipelineException("Test error", file, "test-stage", cause);
		BatchResult result = new BatchResult(5, 1, List.of(error));

		assertThat(result.isSuccess()).isFalse();
		assertThat(result.hasErrors()).isTrue();
		assertThat(result.hasPartialSuccess()).isTrue();
	}

	/**
	 * Verifies that BatchResult correctly calculates total files processed.
	 */
	@Test
	public void totalFilesReturnsCorrectSum()
	{
		BatchResult result = new BatchResult(7, 3, createMockErrors(3));

		assertThat(result.totalFiles()).isEqualTo(10);
		assertThat(result.successCount()).isEqualTo(7);
		assertThat(result.errorCount()).isEqualTo(3);
	}

	/**
	 * Verifies that empty BatchResult has correct default values.
	 */
	@Test
	public void emptyBatchResultHasZeroCounters()
	{
		BatchResult result = BatchResult.empty();

		assertThat(result.successCount()).isZero();
		assertThat(result.errorCount()).isZero();
		assertThat(result.totalFiles()).isZero();
		assertThat(result.errors()).isEmpty();
		assertThat(result.isSuccess()).isTrue();
	}

	/**
	 * Verifies that errors list is immutable after creation.
	 */
	@Test(expectedExceptions = UnsupportedOperationException.class)
	public void errorsListIsImmutable()
	{
		List<PipelineException> mutableErrors = new ArrayList<>(createMockErrors(2));
		BatchResult result = new BatchResult(5, 2, mutableErrors);

		// Modify original list - should not affect BatchResult
		mutableErrors.clear();
		assertThat(result.errors()).hasSize(2);

		// Try to modify returned list - should throw
		result.errors().clear();
	}

	/**
	 * Verifies that BatchResult rejects negative success count.
	 */
	@Test(expectedExceptions = IllegalArgumentException.class)
	public void negativeSuccessCountThrowsException()
	{
		new BatchResult(-1, 0, List.of());
	}

	/**
	 * Verifies that BatchResult rejects negative error count.
	 */
	@Test(expectedExceptions = IllegalArgumentException.class)
	public void negativeErrorCountThrowsException()
	{
		new BatchResult(0, -1, List.of());
	}

	/**
	 * Verifies that BatchResult rejects null errors list.
	 */
	@Test(expectedExceptions = NullPointerException.class)
	public void nullErrorsListThrowsException()
	{
		new BatchResult(0, 0, null);
	}

	/**
	 * Verifies that BatchResult rejects mismatched error count and list size.
	 */
	@Test(expectedExceptions = IllegalArgumentException.class)
	public void mismatchedErrorCountAndListSizeThrowsException()
	{
		new BatchResult(5, 3, createMockErrors(2));
	}

	/**
	 * Verifies that hasPartialSuccess returns false when all files succeeded.
	 */
	@Test
	public void hasPartialSuccessReturnsFalseWhenAllSucceeded()
	{
		BatchResult result = new BatchResult(10, 0, List.of());

		assertThat(result.hasPartialSuccess()).isFalse();
	}

	/**
	 * Verifies that hasPartialSuccess returns false when all files failed.
	 */
	@Test
	public void hasPartialSuccessReturnsFalseWhenAllFailed()
	{
		BatchResult result = new BatchResult(0, 5, createMockErrors(5));

		assertThat(result.hasPartialSuccess()).isFalse();
	}

	/**
	 * Creates mock PipelineException instances for testing.
	 *
	 * @param count the number of exceptions to create
	 * @return list of mock exceptions
	 */
	private static List<PipelineException> createMockErrors(int count)
	{
		List<PipelineException> errors = new ArrayList<>();
		for (int i = 0; i < count; ++i)
		{
			Path file = Paths.get("file" + i + ".java");
			RuntimeException cause = new RuntimeException("Mock error " + i);
			errors.add(new PipelineException("Error " + i, file, "stage-" + i, cause));
		}
		return errors;
	}
}
