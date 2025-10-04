package io.github.cowwoc.styler.cli.pipeline.stages;

import io.github.cowwoc.styler.cli.pipeline.PipelineException;
import io.github.cowwoc.styler.cli.pipeline.ProcessingContext;
import io.github.cowwoc.styler.cli.pipeline.StageResult;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;

/**
 * Tests for {@link WriteStage}.
 */
@SuppressWarnings("PMD.MethodNamingConventions") // Test methods use descriptive_scenario_outcome pattern
public final class WriteStageTest
{
	/**
	 * Verifies that WriteStage writes content to file.
	 */
	@Test
	public void validContent_writeStageExecution_writesFile() throws IOException, PipelineException
	{
		String content = "public class Test { }";
		Path outputFile = Files.createTempFile("output-", ".java");

		try
		{
			WriteStage stage = new WriteStage();
			ProcessingContext context = ProcessingContext.builder(outputFile).build();

			StageResult<Path> result = stage.execute(content, context);

			requireThat(result.isSuccess(), "result.isSuccess").isTrue();
			Path writtenFile = result.output().orElseThrow();
			requireThat(writtenFile, "writtenFile").isEqualTo(outputFile);
			requireThat(Files.exists(writtenFile), "fileExists").isTrue();

			String written = Files.readString(writtenFile);
			requireThat(written, "writtenContent").isEqualTo(content);
		}
		finally
		{
			Files.deleteIfExists(outputFile);
		}
	}

	/**
	 * Verifies that WriteStage handles empty content gracefully.
	 */
	@Test
	public void emptyContent_writeStageExecution_returnsFailure() throws PipelineException
	{
		Path outputFile = Path.of("/tmp/output.java");
		WriteStage stage = new WriteStage();
		ProcessingContext context = ProcessingContext.builder(outputFile).build();

		StageResult<Path> result = stage.execute("", context);

		requireThat(result.isSuccess(), "result.isSuccess").isFalse();
	}

	/**
	 * Verifies that WriteStage returns the correct stage ID.
	 */
	@Test
	public void writeStage_getStageId_returnsWrite()
	{
		WriteStage stage = new WriteStage();
		requireThat(stage.getStageId(), "stageId").isEqualTo("write");
	}

	/**
	 * Verifies that WriteStage supports error recovery.
	 */
	@Test
	public void writeStage_supportsErrorRecovery_returnsTrue()
	{
		WriteStage stage = new WriteStage();
		requireThat(stage.supportsErrorRecovery(), "supportsRecovery").isTrue();
	}

	/**
	 * Verifies that WriteStage creates parent directories if needed.
	 */
	@Test
	public void nonexistentParentDirectory_writeStageExecution_createsDirectories()
		throws IOException, PipelineException
	{
		Path tempDir = Files.createTempDirectory("write-test-");
		Path parentDir = tempDir.resolve("parent");
		Path outputFile = parentDir.resolve("output.java");

		try
		{
			String content = "public class Test { }";
			WriteStage stage = new WriteStage();
			ProcessingContext context = ProcessingContext.builder(outputFile).build();

			StageResult<Path> result = stage.execute(content, context);

			requireThat(result.isSuccess(), "result.isSuccess").isTrue();
			requireThat(Files.exists(parentDir), "parentDirExists").isTrue();
			requireThat(Files.exists(outputFile), "fileExists").isTrue();
		}
		finally
		{
			Files.deleteIfExists(outputFile);
			Files.deleteIfExists(parentDir);
			Files.deleteIfExists(tempDir);
		}
	}
}
