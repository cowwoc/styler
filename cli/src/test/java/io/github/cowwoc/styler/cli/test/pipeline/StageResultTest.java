package io.github.cowwoc.styler.cli.test.pipeline;
import io.github.cowwoc.styler.cli.pipeline.PipelineException;
import io.github.cowwoc.styler.cli.pipeline.StageResult;

import org.testng.annotations.Test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.atomic.AtomicBoolean;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;

/**
 * Tests for {@link StageResult}.
 */
public final class StageResultTest
{
	/**
	 * Verifies that success result contains output.
	 */
	@Test
	public void successResultOutputIsPresent()
	{
		String output = "test output";
		StageResult<String> result = StageResult.success(output);

		requireThat(result.isSuccess(), "isSuccess").isTrue();
		requireThat(result.output().isPresent(), "output.isPresent").isTrue();
		requireThat(result.output().orElseThrow(), "output.value").isEqualTo(output);
		requireThat(result.exception().isPresent(), "exception.isPresent").isFalse();
	}

	/**
	 * Verifies that failure result contains exception.
	 */
	@Test
	public void failureResultExceptionIsPresent()
	{
		Path file = Paths.get("test.java");
		PipelineException exception = new PipelineException("test error", file, "test-stage");
		StageResult<String> result = StageResult.failure(exception);

		requireThat(result.isSuccess(), "isSuccess").isFalse();
		requireThat(result.exception().isPresent(), "exception.isPresent").isTrue();
		requireThat(result.exception().orElseThrow(), "exception.value").isEqualTo(exception);
		requireThat(result.output().isPresent(), "output.isPresent").isFalse();
	}

	/**
	 * Verifies that map transforms success values.
	 */
	@Test
	public void successResultMapTransformsValue()
	{
		StageResult<Integer> result = StageResult.success(42);

		StageResult<String> mapped = result.map(Object::toString);

		requireThat(mapped.isSuccess(), "isSuccess").isTrue();
		requireThat(mapped.output().orElseThrow(), "mappedValue").isEqualTo("42");
	}

	/**
	 * Verifies that map on failure returns failure.
	 */
	@Test
	public void failureResultMapReturnsFailure()
	{
		Path file = Paths.get("test.java");
		PipelineException exception = new PipelineException("test error", file, "test-stage");
		StageResult<Integer> result = StageResult.failure(exception);

		StageResult<String> mapped = result.map(Object::toString);

		requireThat(mapped.isSuccess(), "isSuccess").isFalse();
		requireThat(mapped.exception().orElseThrow(), "exception").isEqualTo(exception);
	}

	/**
	 * Verifies that ifSuccess executes action on success.
	 */
	@Test
	public void successResultIfSuccessExecutesAction()
	{
		AtomicBoolean executed = new AtomicBoolean(false);
		StageResult<String> result = StageResult.success("test");

		result.ifSuccess(value -> executed.set(true));

		requireThat(executed.get(), "actionExecuted").isTrue();
	}

	/**
	 * Verifies that ifSuccess does not execute on failure.
	 */
	@Test
	public void failureResultIfSuccessDoesNotExecuteAction()
	{
		AtomicBoolean executed = new AtomicBoolean(false);
		Path file = Paths.get("test.java");
		PipelineException exception = new PipelineException("test error", file, "test-stage");
		StageResult<String> result = StageResult.failure(exception);

		result.ifSuccess(value -> executed.set(true));

		requireThat(executed.get(), "actionExecuted").isFalse();
	}

	/**
	 * Verifies that ifFailure executes action on failure.
	 */
	@Test
	public void failureResultIfFailureExecutesAction()
	{
		AtomicBoolean executed = new AtomicBoolean(false);
		Path file = Paths.get("test.java");
		PipelineException exception = new PipelineException("test error", file, "test-stage");
		StageResult<String> result = StageResult.failure(exception);

		result.ifFailure(ex -> executed.set(true));

		requireThat(executed.get(), "actionExecuted").isTrue();
	}

	/**
	 * Verifies that ifFailure does not execute on success.
	 */
	@Test
	public void successResultIfFailureDoesNotExecuteAction()
	{
		AtomicBoolean executed = new AtomicBoolean(false);
		StageResult<String> result = StageResult.success("test");

		result.ifFailure(ex -> executed.set(true));

		requireThat(executed.get(), "actionExecuted").isFalse();
	}

	/**
	 * Verifies that success with null value throws exception.
	 */
	@Test(expectedExceptions = NullPointerException.class)
	public void successResultNullValueThrowsNullPointerException()
	{
		StageResult.success(null);
	}

	/**
	 * Verifies that failure with null exception throws exception.
	 */
	@Test(expectedExceptions = NullPointerException.class)
	public void failureResultNullExceptionThrowsNullPointerException()
	{
		StageResult.failure(null);
	}
}
