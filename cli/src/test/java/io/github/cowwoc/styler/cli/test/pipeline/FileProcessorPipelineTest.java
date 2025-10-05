package io.github.cowwoc.styler.cli.test.pipeline;
import io.github.cowwoc.styler.cli.pipeline.PipelineStage;
import io.github.cowwoc.styler.cli.pipeline.PipelineResult;
import io.github.cowwoc.styler.cli.pipeline.FileProcessorPipeline;
import io.github.cowwoc.styler.cli.pipeline.PipelineException;
import io.github.cowwoc.styler.cli.pipeline.ProcessingContext;

import org.testng.annotations.Test;

import java.nio.file.Path;
import java.nio.file.Paths;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;

/**
 * Tests for {@link FileProcessorPipeline}.
 * <p>
 * Verifies the pipeline coordinator correctly executes stages in sequence and handles success/failure cases.
 */
@SuppressWarnings("PMD.MethodNamingConventions") // Test methods use descriptive_scenario_outcome pattern
public final class FileProcessorPipelineTest
{
	/**
	 * Verifies that a pipeline with a single identity stage successfully processes input.
	 */
	@Test
	public void singleStage_successfulExecution_returnsOutput()
	{
		Path testFile = Paths.get("test.java");
		ProcessingContext context = ProcessingContext.builder(testFile).build();

		FileProcessorPipeline<Path> pipeline = FileProcessorPipeline.<Path>builder().
			addStage(new IdentityStage<>()).
			build();

		try (pipeline)
		{
			PipelineResult<Path> result = pipeline.process(testFile, context);

			requireThat(result.isSuccess(), "result.isSuccess").isTrue();
			requireThat(result.output().orElse(null), "result.output").isEqualTo(testFile);
		}
	}

	/**
	 * Verifies that a pipeline with multiple stages executes them in sequence.
	 */
	@Test
	public void multipleStages_successfulExecution_executeInOrder()
	{
		Path testFile = Paths.get("test.java");
		ProcessingContext context = ProcessingContext.builder(testFile).build();

		FileProcessorPipeline<Path> pipeline = FileProcessorPipeline.<Path>builder().
			addStage(new IdentityStage<>("stage1")).
			addStage(new IdentityStage<>("stage2")).
			addStage(new IdentityStage<>("stage3")).
			build();

		try (pipeline)
		{
			PipelineResult<Path> result = pipeline.process(testFile, context);

			requireThat(result.isSuccess(), "result.isSuccess").isTrue();
			requireThat(result.output().orElse(null), "result.output").isEqualTo(testFile);
		}
	}

	/**
	 * Verifies that pipeline execution fails when a stage throws an exception.
	 */
	@Test
	public void stageThrowsException_pipelineExecution_returnsFailure()
	{
		Path testFile = Paths.get("test.java");
		ProcessingContext context = ProcessingContext.builder(testFile).build();

		PipelineStage<Path, Path> failingStage = (input, ctx) ->
		{
			throw new PipelineException("Intentional test failure", input, "failing-stage");
		};

		FileProcessorPipeline<Path> pipeline = FileProcessorPipeline.<Path>builder().
			addStage(new IdentityStage<>("stage1")).
			addStage(failingStage).
			addStage(new IdentityStage<>("stage3")).
			build();

		try (pipeline)
		{
			PipelineResult<Path> result = pipeline.process(testFile, context);

			requireThat(result.isSuccess(), "result.isSuccess").isFalse();
			requireThat(result.exception().isPresent(), "result.exception.isPresent").isTrue();
		}
	}

	/**
	 * Verifies that closing a pipeline is idempotent.
	 */
	@Test
	public void closePipeline_calledMultipleTimes_isIdempotent()
	{
		FileProcessorPipeline<String> pipeline = FileProcessorPipeline.<String>builder().
			addStage(new IdentityStage<>()).
			build();

		pipeline.close();
		pipeline.close(); // Second close should not throw
		pipeline.close(); // Third close should not throw
	}

	/**
	 * Verifies that using a closed pipeline throws IllegalStateException.
	 */
	@Test(expectedExceptions = IllegalStateException.class)
	public void closedPipeline_processFile_throwsIllegalStateException()
	{
		Path testFile = Paths.get("test.java");
		ProcessingContext context = ProcessingContext.builder(testFile).build();

		FileProcessorPipeline<Path> pipeline = FileProcessorPipeline.<Path>builder().
			addStage(new IdentityStage<>()).
			build();

		pipeline.close();
		pipeline.process(testFile, context); // Should throw
	}

	/**
	 * Verifies that building a pipeline without stages throws IllegalStateException.
	 */
	@Test(expectedExceptions = IllegalStateException.class)
	public void buildPipeline_withNoStages_throwsIllegalStateException()
	{
		FileProcessorPipeline.builder().build(); // Should throw
	}
}
