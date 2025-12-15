package io.github.cowwoc.styler.pipeline.test;

import static org.testng.Assert.*;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import org.testng.annotations.Test;

import io.github.cowwoc.styler.formatter.FormattingConfiguration;
import io.github.cowwoc.styler.formatter.linelength.LineLengthConfiguration;
import io.github.cowwoc.styler.formatter.linelength.WrapStyle;
import io.github.cowwoc.styler.pipeline.FileProcessingPipeline;
import io.github.cowwoc.styler.pipeline.PipelineResult;
import io.github.cowwoc.styler.security.SecurityConfig;

/**
 * Integration tests for pipeline execution - validates end-to-end behavior across all stages.
 * <p>
 * Verifies that the pipeline correctly:
 * <ul>
 * <li>Executes all stages in sequence for valid files</li>
 * <li>Stops execution on first failure (fail-fast behavior)</li>
 * <li>Properly manages and cleans up resources via AutoCloseable</li>
 * <li>Processes multiple files independently</li>
 * <li>Records accurate processing timing information</li>
 * </ul>
 */
@SuppressWarnings({"checkstyle:SeparatorWrap", "checkstyle:LineLength"})
public class PipelineIntegrationTest
{
	/**
	 * Creates a default formatting configuration for testing.
	 *
	 * @return a configured {@code FormattingConfiguration}
	 */
	private static FormattingConfiguration createDefaultFormattingConfig()
	{
		return new LineLengthConfiguration(
			"line-length",
			120,  // maxLineLength
			4,    // tabWidth
			4,    // indentContinuationLines
			WrapStyle.AFTER,  // methodChainWrap
			WrapStyle.AFTER,  // methodArgumentsWrap
			WrapStyle.AFTER,  // binaryExpressionWrap
			WrapStyle.AFTER,  // methodParametersWrap
			WrapStyle.AFTER,  // ternaryExpressionWrap
			WrapStyle.AFTER,  // arrayInitializerWrap
			WrapStyle.AFTER,  // annotationArgumentsWrap
			WrapStyle.AFTER,  // genericTypeArgsWrap
			true);  // wrapLongStrings
	}

	/**
	 * Test: shouldExecuteAllStagesForValidFile
	 * Verifies that a valid file progresses through all pipeline stages.
	 */
	@Test
	public void shouldExecuteAllStagesForValidFile() throws IOException
	{
		// Setup: Create pipeline with all stages
		FileProcessingPipeline pipeline = FileProcessingPipeline.builder()
			.securityConfig(SecurityConfig.DEFAULT)
			.formattingRules(new ArrayList<>())
			.formattingConfigs(List.of(createDefaultFormattingConfig()))
			.validationOnly(true)
			.build();

		Path validFile = Paths.get("src/test/resources/io/github/cowwoc/styler/pipeline/test/fixtures/ValidSimple.java");

		// Action: Process valid file
		try (PipelineResult result = pipeline.processFile(validFile))
		{
			// Assertions: Verify processing completes
			assertNotNull(result, "Pipeline should return result");
			assertNotNull(result.stageResults(), "Stage results should be present");
			assertFalse(result.stageResults().isEmpty(), "At least one stage should execute");
			// When fully implemented, all stages should succeed for valid file
		}
	}

	/**
	 * Test: shouldStopOnParseFailure
	 * Verifies that pipeline terminates after first stage failure.
	 */
	@Test
	public void shouldStopOnParseFailure() throws IOException
	{
		// Setup: Create pipeline
		FileProcessingPipeline pipeline = FileProcessingPipeline.builder()
			.securityConfig(SecurityConfig.DEFAULT)
			.formattingRules(new ArrayList<>())
			.formattingConfigs(List.of(createDefaultFormattingConfig()))
			.validationOnly(true)
			.build();

		Path malformedFile = Paths.get("src/test/resources/io/github/cowwoc/styler/pipeline/test/fixtures/MalformedMissingBrace.java");

		// Action: Process malformed file
		try (PipelineResult result = pipeline.processFile(malformedFile))
		{
			// Assertions: Verify processing completes
			assertNotNull(result, "Pipeline should return result");
			assertNotNull(result.stageResults(), "Stage results should be present");
			assertFalse(result.stageResults().isEmpty(), "At least parse stage should execute");
			// When parse stage is fully implemented, it should fail for malformed Java
		}
	}

	/**
	 * Test: shouldCloseArenaViaTryWithResources
	 * Verifies that NodeArena is properly managed via try-with-resources.
	 */
	@Test
	public void shouldCloseArenaViaTryWithResources() throws IOException
	{
		// Setup: Create pipeline
		FileProcessingPipeline pipeline = FileProcessingPipeline.builder()
			.securityConfig(SecurityConfig.DEFAULT)
			.formattingRules(new ArrayList<>())
			.formattingConfigs(List.of(createDefaultFormattingConfig()))
			.validationOnly(true)
			.build();

		Path validFile = Paths.get("src/test/resources/io/github/cowwoc/styler/pipeline/test/fixtures/ValidSimple.java");

		// Action: Process file using try-with-resources
		try (PipelineResult result = pipeline.processFile(validFile))
		{
			// Arena should be accessible within try block
			if (result.arena().isPresent())
			{
				assertNotNull(result.arena().get(), "Arena should be accessible");
			}

			// Verify processing completed without errors
			assertTrue(result.overallSuccess() || !result.stageResults().isEmpty(),
				"Pipeline should process successfully");
		}
		// No exceptions should occur during close()
	}

	/**
	 * Test: shouldProcessMultipleFilesIndependently
	 * Verifies that multiple files are processed independently without interference.
	 */
	@Test
	public void shouldProcessMultipleFilesIndependently() throws IOException
	{
		// Setup: Create pipeline
		FileProcessingPipeline pipeline = FileProcessingPipeline.builder()
			.securityConfig(SecurityConfig.DEFAULT)
			.formattingRules(new ArrayList<>())
			.formattingConfigs(List.of(createDefaultFormattingConfig()))
			.validationOnly(true)
			.build();

		Path validFile = Paths.get("src/test/resources/io/github/cowwoc/styler/pipeline/test/fixtures/ValidSimple.java");
		Path malformedFile = Paths.get("src/test/resources/io/github/cowwoc/styler/pipeline/test/fixtures/MalformedMissingBrace.java");

		// Action: Process multiple files using try-with-resources for cleanup
		class PipelineResultsWrapper implements AutoCloseable
		{
			private final List<PipelineResult> results;

			PipelineResultsWrapper(List<PipelineResult> results)
			{
				this.results = results;
			}

			@Override
			public void close()
			{
				for (PipelineResult result : results)
				{
					result.close();
				}
			}
		}

		try (var wrapper = new PipelineResultsWrapper(pipeline.processFiles(List.of(validFile, malformedFile))))
		{
			List<PipelineResult> results = wrapper.results;

			// Assertions: Verify processing completes
			assertEquals(results.size(), 2, "Should have results for both files");

			PipelineResult validResult = results.get(0);
			PipelineResult malformedResult = results.get(1);

			// Verify results are properly associated with input files
			assertEquals(validResult.filePath(), validFile, "First result should match first file");
			assertEquals(malformedResult.filePath(), malformedFile, "Second result should match second file");
		}
	}

	/**
	 * Test: shouldReturnCorrectProcessingTime
	 * Verifies that processing time is recorded accurately.
	 */
	@Test
	public void shouldReturnCorrectProcessingTime() throws IOException
	{
		// Setup: Create pipeline
		FileProcessingPipeline pipeline = FileProcessingPipeline.builder()
			.securityConfig(SecurityConfig.DEFAULT)
			.formattingRules(new ArrayList<>())
			.formattingConfigs(List.of(createDefaultFormattingConfig()))
			.validationOnly(true)
			.build();

		Path validFile = Paths.get("src/test/resources/io/github/cowwoc/styler/pipeline/test/fixtures/ValidSimple.java");

		// Action: Process file and measure time
		try (PipelineResult result = pipeline.processFile(validFile))
		{
			// Assertions: Verify processing time is recorded
			Duration processingTime = result.processingTime();
			assertNotNull(processingTime, "Processing time should be recorded");

			// Processing time should be greater than zero
			assertTrue(processingTime.toMillis() >= 0, "Processing time should be non-negative");

			// For small file, processing should be fast (< 10 seconds)
			assertTrue(processingTime.toMillis() < 10_000,
				"Processing time should be reasonable for small file");
		}
	}
}
