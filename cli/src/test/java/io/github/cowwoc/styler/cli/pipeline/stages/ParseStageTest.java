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
 * Tests for {@link ParseStage}.
 */
@SuppressWarnings("PMD.MethodNamingConventions") // Test methods use descriptive_scenario_outcome pattern
public final class ParseStageTest
{
	/**
	 * Verifies that ParseStage successfully parses a valid Java source file.
	 */
	@Test
	public void validJavaFile_parseStageExecution_returnsSuccess() throws IOException, PipelineException
	{
		String sourceCode = "public class Test { }";
		Path tempFile = Files.createTempFile("test-", ".java");

		try
		{
			Files.writeString(tempFile, sourceCode);

			ParseStage stage = new ParseStage();
			ProcessingContext context = ProcessingContext.builder(tempFile).build();

			StageResult<ParsedFile> result = stage.execute(tempFile, context);

			requireThat(result.isSuccess(), "result.isSuccess").isTrue();
			ParsedFile parsed = result.output().orElseThrow();
			requireThat(parsed.sourceFile(), "parsed.sourceFile").isEqualTo(tempFile);
			requireThat(parsed.sourceText(), "parsed.sourceText").isEqualTo(sourceCode);
			requireThat(parsed.parser(), "parsed.parser").isNotNull();
			requireThat(parsed.rootNodeId(), "parsed.rootNodeId").isGreaterThanOrEqualTo(0);

			// Clean up parser
			parsed.parser().close();
		}
		finally
		{
			Files.deleteIfExists(tempFile);
		}
	}

	/**
	 * Verifies that ParseStage handles nonexistent files gracefully.
	 */
	@Test
	public void nonexistentFile_parseStageExecution_returnsFailure() throws PipelineException
	{
		Path nonexistent = Path.of("/nonexistent/file.java");
		ParseStage stage = new ParseStage();
		ProcessingContext context = ProcessingContext.builder(nonexistent).build();

		StageResult<ParsedFile> result = stage.execute(nonexistent, context);

		requireThat(result.isSuccess(), "result.isSuccess").isFalse();
		requireThat(result.exception().isPresent(), "hasException").isTrue();
	}

	/**
	 * Verifies that ParseStage returns the correct stage ID.
	 */
	@Test
	public void parseStage_getStageId_returnsParse()
	{
		ParseStage stage = new ParseStage();
		requireThat(stage.getStageId(), "stageId").isEqualTo("parse");
	}
}
