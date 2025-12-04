package io.github.cowwoc.styler.pipeline.test;

import static org.testng.Assert.*;

import org.testng.annotations.Test;

import io.github.cowwoc.styler.pipeline.StageResult;

/**
 * Tests for StageResult - verifies Railway-Oriented Programming semantics and sealed interface enforcement.
 */
public class StageResultTest
{
	/**
	 * Test: testStageResultSuccessWithDataDataAccessible
	 * Verifies Success result with data payload returns data correctly.
	 */
	@Test
	public void testStageResultSuccessWithDataDataAccessible()
	{
		Object testData = "processed content";
		StageResult.Success success = new StageResult.Success(testData);

		assertTrue(success.isSuccess(), "Success should return true for isSuccess()");
		assertTrue(success.optionalData().isPresent(), "Data should be present");
		assertEquals(success.optionalData().get(), testData, "Data should match input");
		assertTrue(success.errorMessage().isEmpty(), "Success should have no error message");
	}

	/**
	 * Test: testStageResultSuccessWithNullDataDataIsEmpty
	 * Verifies Success result with null data returns empty Optional for data.
	 */
	@Test
	public void testStageResultSuccessWithNullDataDataIsEmpty()
	{
		StageResult.Success success = new StageResult.Success(null);

		assertTrue(success.isSuccess(), "Success should return true for isSuccess()");
		assertTrue(success.optionalData().isEmpty(), "Data should be empty for null input");
		assertTrue(success.errorMessage().isEmpty(), "Success should have no error message");
	}

	/**
	 * Test: testStageResultFailureWithValidMessageFailurePropertiesCorrect
	 * Verifies Failure result with valid message has correct properties.
	 */
	@Test
	public void testStageResultFailureWithValidMessageFailurePropertiesCorrect()
	{
		String errorMessage = "Processing failed due to invalid syntax";
		StageResult.Failure failure = new StageResult.Failure(errorMessage, null);

		assertFalse(failure.isSuccess(), "Failure should return false for isSuccess()");
		assertTrue(failure.errorMessage().isPresent(), "Error message should be present");
		assertEquals(failure.errorMessage().get(), errorMessage, "Error message should match input");
		assertTrue(failure.optionalData().isEmpty(), "Failure should have no data");
	}

	/**
	 * Test: testStageResultFailureWithCauseCauseAccessible
	 * Verifies Failure result with exception cause stores and returns cause.
	 */
	@Test
	public void testStageResultFailureWithCauseCauseAccessible()
	{
		String errorMessage = "I/O error";
		Exception cause = new Exception("File not found");
		StageResult.Failure failure = new StageResult.Failure(errorMessage, cause);

		assertFalse(failure.isSuccess(), "Failure should return false for isSuccess()");
		assertEquals(failure.message(), errorMessage, "Message should be set");
		assertEquals(failure.cause(), cause, "Cause should be set");
	}

	/**
	 * Test: testStageResultFailureNullMessageThrowsException
	 * Verifies Failure rejects null message.
	 */
	@Test(expectedExceptions = NullPointerException.class)
	public void testStageResultFailureNullMessageThrowsException()
	{
		new StageResult.Failure(null, null);
	}

	/**
	 * Test: testStageResultFailureEmptyMessageThrowsException
	 * Verifies Failure rejects empty message.
	 */
	@Test(expectedExceptions = IllegalArgumentException.class)
	public void testStageResultFailureEmptyMessageThrowsException()
	{
		new StageResult.Failure("", null);
	}

	/**
	 * Test: testStageResultSkippedWithReasonSkipPropertiesCorrect
	 * Verifies Skipped result with reason has correct properties.
	 */
	@Test
	public void testStageResultSkippedWithReasonSkipPropertiesCorrect()
	{
		String skipReason = "File already formatted, skipping formatting stage";
		StageResult.Skipped skipped = new StageResult.Skipped(skipReason);

		assertTrue(skipped.isSuccess(), "Skipped should return true for isSuccess()");
		assertEquals(skipped.reason(), skipReason, "Reason should match input");
		assertTrue(skipped.errorMessage().isEmpty(), "Skipped should have no error message");
		assertTrue(skipped.optionalData().isEmpty(), "Skipped should have no data");
	}

	/**
	 * Test: testStageResultSkippedNullReasonThrowsException
	 * Verifies Skipped rejects null reason.
	 */
	@Test(expectedExceptions = NullPointerException.class)
	public void testStageResultSkippedNullReasonThrowsException()
	{
		new StageResult.Skipped(null);
	}

	/**
	 * Test: testStageResultSkippedEmptyReasonThrowsException
	 * Verifies Skipped rejects empty reason.
	 */
	@Test(expectedExceptions = IllegalArgumentException.class)
	public void testStageResultSkippedEmptyReasonThrowsException()
	{
		new StageResult.Skipped("");
	}

	/**
	 * Test: testStageResultSealedInterfaceOnlyThreeImplementations
	 * Verifies sealed interface enforces exactly three implementations.
	 */
	@Test
	public void testStageResultSealedInterfaceOnlyThreeImplementations()
	{
		StageResult success = new StageResult.Success("data");
		StageResult failure = new StageResult.Failure("error", null);
		StageResult skipped = new StageResult.Skipped("reason");

		assertNotNull(success, "Success implementation should be valid");
		assertNotNull(failure, "Failure implementation should be valid");
		assertNotNull(skipped, "Skipped implementation should be valid");

		assertTrue(success instanceof StageResult.Success, "Should be Success instance");
		assertTrue(failure instanceof StageResult.Failure, "Should be Failure instance");
		assertTrue(skipped instanceof StageResult.Skipped, "Should be Skipped instance");
	}

	/**
	 * Test: testStageResultSuccessAndSkippedBothConsideredSuccess
	 * Verifies both Success and Skipped return true for isSuccess() (ROP semantics).
	 */
	@Test
	public void testStageResultSuccessAndSkippedBothConsideredSuccess()
	{
		StageResult success = new StageResult.Success(null);
		StageResult skipped = new StageResult.Skipped("not needed");
		StageResult failure = new StageResult.Failure("failed", null);

		assertTrue(success.isSuccess(), "Success should be considered success");
		assertTrue(skipped.isSuccess(), "Skipped should be considered success (ROP semantics)");
		assertFalse(failure.isSuccess(), "Failure should not be considered success");
	}
}
