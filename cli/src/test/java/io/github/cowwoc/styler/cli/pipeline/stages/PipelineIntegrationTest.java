package io.github.cowwoc.styler.cli.pipeline.stages;

import io.github.cowwoc.styler.cli.pipeline.FileProcessorPipeline;
import io.github.cowwoc.styler.cli.pipeline.PipelineResult;
import io.github.cowwoc.styler.cli.pipeline.ProcessingContext;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;

/**
 * Integration tests for the complete file processing pipeline.
 * <p>
 * Tests the end-to-end workflow: Parse → Format → Write stages working together.
 */
@SuppressWarnings("PMD.MethodNamingConventions") // Test methods use descriptive_scenario_outcome pattern
public final class PipelineIntegrationTest
{
	/**
	 * Verifies that the complete pipeline processes a Java file successfully.
	 */
	@Test
	public void validJavaFile_completePipelineExecution_writesFormattedOutput()
		throws IOException, io.github.cowwoc.styler.cli.pipeline.PipelineException
	{
		String sourceCode = "public class Example { public void method() { } }";
		Path inputFile = Files.createTempFile("input-", ".java");
		Path outputFile = inputFile; // Write in-place

		try
		{
			Files.writeString(inputFile, sourceCode);

			FileProcessorPipeline<Path> pipeline = FileProcessorPipeline.<Path>builder().
				addStage(new ParseStage()).
				addStage(new FormatStage()).
				addStage(new WriteStage()).
				build();

			try (pipeline)
			{
				ProcessingContext context = ProcessingContext.builder(inputFile).build();
				PipelineResult<Path> result = pipeline.process(inputFile, context);

				requireThat(result.isSuccess(), "result.isSuccess").isTrue();
				Path writtenFile = result.output().orElseThrow();
				requireThat(writtenFile, "writtenFile").isEqualTo(outputFile);
				requireThat(Files.exists(writtenFile), "fileExists").isTrue();

				String written = Files.readString(writtenFile);
				requireThat(written, "writtenContent").isNotEmpty();
			}
		}
		finally
		{
			Files.deleteIfExists(inputFile);
		}
	}

	/**
	 * Verifies that pipeline handles parse errors gracefully.
	 */
	@Test
	public void malformedJavaFile_completePipelineExecution_returnsFailure()
		throws IOException, io.github.cowwoc.styler.cli.pipeline.PipelineException
	{
		String invalidCode = "public class { invalid syntax }";
		Path inputFile = Files.createTempFile("invalid-", ".java");

		try
		{
			Files.writeString(inputFile, invalidCode);

			FileProcessorPipeline<Path> pipeline = FileProcessorPipeline.<Path>builder().
				addStage(new ParseStage()).
				addStage(new FormatStage()).
				addStage(new WriteStage()).
				build();

			try (pipeline)
			{
				ProcessingContext context = ProcessingContext.builder(inputFile).build();
				PipelineResult<Path> result = pipeline.process(inputFile, context);

				requireThat(result.isSuccess(), "result.isSuccess").isFalse();
				requireThat(result.exception().isPresent(), "hasException").isTrue();
			}
		}
		finally
		{
			Files.deleteIfExists(inputFile);
		}
	}

	/**
	 * Verifies that an empty pipeline builder throws IllegalStateException.
	 */
	@Test(expectedExceptions = IllegalStateException.class)
	public void emptyPipeline_build_throwsIllegalStateException()
	{
		FileProcessorPipeline.builder().build();
	}
}
