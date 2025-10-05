package io.github.cowwoc.styler.cli.test.pipeline.stages;
import io.github.cowwoc.styler.cli.pipeline.stages.FormatStage;

import io.github.cowwoc.styler.cli.pipeline.PipelineException;
import io.github.cowwoc.styler.cli.pipeline.ProcessingContext;
import io.github.cowwoc.styler.cli.pipeline.StageResult;
import io.github.cowwoc.styler.parser.IndexOverlayParser;
import org.testng.annotations.Test;

import java.nio.file.Path;
import java.nio.file.Paths;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;

/**
 * Tests for {@link FormatStage}.
 */
@SuppressWarnings("PMD.MethodNamingConventions")
// Test methods use descriptive_scenario_outcome pattern
public final class FormatStageTest
{
	/**
	 * Verifies that FormatStage returns source text unchanged (identity transformation).
	 */
	@Test
	public void parsedFile_formatStageExecution_returnsSourceText() throws PipelineException
	{
		String sourceCode = "public class Test { }";
		Path sourceFile = Paths.get("test.java");

		// Don't use try-with-resources - FormatStage closes the parser
		IndexOverlayParser parser = new IndexOverlayParser(sourceCode);
		int rootNode = parser.parse();
		ParsedFile parsed = new ParsedFile(sourceFile, parser, rootNode, sourceCode);

		FormatStage stage = new FormatStage();
		ProcessingContext context = ProcessingContext.builder(sourceFile).build();

		StageResult<String> result = stage.execute(parsed, context);

		requireThat(result.isSuccess(), "result.isSuccess").isTrue();
		String formatted = result.output().orElseThrow();
		requireThat(formatted, "formattedSource").isEqualTo(sourceCode);
	}

	/**
	 * Verifies that FormatStage handles parsed files correctly.
	 */
	@Test
	public void parsedFile_formatStageWithMultipleLines_returnsSourceText() throws PipelineException
	{
		String sourceCode = "public class Test {\n  public void method() {\n  }\n}";
		Path sourceFile = Paths.get("test.java");

		// Don't use try-with-resources - FormatStage closes the parser
		IndexOverlayParser parser = new IndexOverlayParser(sourceCode);
		int rootNode = parser.parse();
		ParsedFile parsed = new ParsedFile(sourceFile, parser, rootNode, sourceCode);

		FormatStage stage = new FormatStage();
		ProcessingContext context = ProcessingContext.builder(sourceFile).build();

		StageResult<String> result = stage.execute(parsed, context);

		requireThat(result.isSuccess(), "result.isSuccess").isTrue();
		String formatted = result.output().orElseThrow();
		requireThat(formatted, "formattedSource").isEqualTo(sourceCode);
	}

	/**
	 * Verifies that FormatStage returns the correct stage ID.
	 */
	@Test
	public void formatStage_getStageId_returnsFormat()
	{
		FormatStage stage = new FormatStage();
		requireThat(stage.getStageId(), "stageId").isEqualTo("format");
	}

	/**
	 * Verifies that FormatStage supports error recovery.
	 */
	@Test
	public void formatStage_supportsErrorRecovery_returnsTrue()
	{
		FormatStage stage = new FormatStage();
		requireThat(stage.supportsErrorRecovery(), "supportsRecovery").isTrue();
	}
}
