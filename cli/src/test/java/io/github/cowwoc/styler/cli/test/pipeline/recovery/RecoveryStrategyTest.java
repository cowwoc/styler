package io.github.cowwoc.styler.cli.test.pipeline.recovery;
import io.github.cowwoc.styler.cli.pipeline.recovery.FailFastStrategy;

import io.github.cowwoc.styler.cli.pipeline.PipelineException;
import io.github.cowwoc.styler.cli.pipeline.StageResult;
import org.testng.annotations.Test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.atomic.AtomicInteger;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;

/**
 * Tests for error recovery strategies.
 */
@SuppressWarnings({"PMD.MethodNamingConventions", "PMD.AvoidThrowingRawExceptionTypes"})
// Test methods use descriptive_scenario_outcome pattern
// Tests intentionally throw RuntimeException to simulate failures
public final class RecoveryStrategyTest
{
	// SkipFileStrategy tests

	/**
	 * Verifies that SkipFileStrategy returns failure.
	 */
	@Test
	public void skipFileStrategy_recover_returnsFailure()
	{
		Path file = Paths.get("test.java");
		PipelineException exception = new PipelineException("test error", file, "test-stage");
		SkipFileStrategy strategy = new SkipFileStrategy();

		StageResult<String> result = strategy.recover(exception, "test", () -> StageResult.success("retry"));

		requireThat(result.isSuccess(), "isSuccess").isFalse();
		requireThat(result.exception().orElseThrow(), "exception").isEqualTo(exception);
	}

	// RetryStrategy tests

	/**
	 * Verifies that RetryStrategy succeeds on first retry.
	 */
	@Test
	public void retryStrategy_firstAttemptSucceeds_returnsSuccess()
	{
		Path file = Paths.get("test.java");
		PipelineException exception = new PipelineException("test error", file, "test-stage");
		RetryStrategy strategy = new RetryStrategy(3, 10);

		AtomicInteger attempts = new AtomicInteger(0);
		ErrorRecoveryStrategy.RetryOperation<String> operation = () ->
		{
			attempts.incrementAndGet();
			return StageResult.success("success");
		};

		StageResult<String> result = strategy.recover(exception, "test", operation);

		requireThat(result.isSuccess(), "isSuccess").isTrue();
		requireThat(result.output().orElseThrow(), "output").isEqualTo("success");
		requireThat(attempts.get(), "attempts").isEqualTo(1);
	}

	/**
	 * Verifies that RetryStrategy retries on failure.
	 */
	@Test
	public void retryStrategy_succeedsOnSecondAttempt_returnsSuccess()
	{
		Path file = Paths.get("test.java");
		PipelineException exception = new PipelineException("test error", file, "test-stage");
		RetryStrategy strategy = new RetryStrategy(3, 10);

		AtomicInteger attempts = new AtomicInteger(0);
		ErrorRecoveryStrategy.RetryOperation<String> operation = () ->
		{
			int attempt = attempts.incrementAndGet();
			if (attempt == 1)
			{
				throw new RuntimeException("First attempt fails");
			}
			return StageResult.success("success");
		};

		StageResult<String> result = strategy.recover(exception, "test", operation);

		requireThat(result.isSuccess(), "isSuccess").isTrue();
		requireThat(attempts.get(), "attempts").isEqualTo(2);
	}

	/**
	 * Verifies that RetryStrategy fails after max attempts.
	 */
	@Test
	public void retryStrategy_allAttemptsExhausted_returnsFailure()
	{
		Path file = Paths.get("test.java");
		PipelineException exception = new PipelineException("test error", file, "test-stage");
		RetryStrategy strategy = new RetryStrategy(2, 10);

		AtomicInteger attempts = new AtomicInteger(0);
		ErrorRecoveryStrategy.RetryOperation<String> operation = () ->
		{
			attempts.incrementAndGet();
			throw new RuntimeException("Always fails");
		};

		StageResult<String> result = strategy.recover(exception, "test", operation);

		requireThat(result.isSuccess(), "isSuccess").isFalse();
		requireThat(attempts.get(), "attempts").isEqualTo(2);
	}

	// FallbackStrategy tests

	/**
	 * Verifies that FallbackStrategy returns fallback value.
	 */
	@Test
	public void fallbackStrategy_recover_returnsFallbackValue()
	{
		Path file = Paths.get("test.java");
		PipelineException exception = new PipelineException("test error", file, "test-stage");
		String fallbackValue = "fallback";
		FallbackStrategy<String> strategy = new FallbackStrategy<>(fallbackValue);

		StageResult<String> result = strategy.recover(exception, "test",
			() -> StageResult.success("should not be used"));

		requireThat(result.isSuccess(), "isSuccess").isTrue();
		requireThat(result.output().orElseThrow(), "output").isEqualTo(fallbackValue);
	}

	/**
	 * Verifies that FallbackStrategy with null throws exception.
	 */
	@Test(expectedExceptions = NullPointerException.class)
	public void fallbackStrategy_nullFallback_throwsNullPointerException()
	{
		new FallbackStrategy<>(null);
	}

	// FailFastStrategy tests

	/**
	 * Verifies that FailFastStrategy immediately returns failure.
	 */
	@Test
	public void failFastStrategy_recover_returnsFailureImmediately()
	{
		Path file = Paths.get("test.java");
		PipelineException exception = new PipelineException("test error", file, "test-stage");
		FailFastStrategy strategy = new FailFastStrategy();

		AtomicInteger attempts = new AtomicInteger(0);
		ErrorRecoveryStrategy.RetryOperation<String> operation = () ->
		{
			attempts.incrementAndGet();
			return StageResult.success("should not be called");
		};

		StageResult<String> result = strategy.recover(exception, "test", operation);

		requireThat(result.isSuccess(), "isSuccess").isFalse();
		requireThat(result.exception().orElseThrow(), "exception").isEqualTo(exception);
		requireThat(attempts.get(), "retryNotCalled").isEqualTo(0);
	}
}
