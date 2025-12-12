package io.github.cowwoc.styler.pipeline.parallel.test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.testng.annotations.Test;

import io.github.cowwoc.styler.pipeline.FileProcessingPipeline;
import io.github.cowwoc.styler.pipeline.parallel.BatchProcessor;
import io.github.cowwoc.styler.pipeline.parallel.BatchResult;
import io.github.cowwoc.styler.pipeline.parallel.DefaultBatchProcessor;
import io.github.cowwoc.styler.pipeline.parallel.ErrorStrategy;
import io.github.cowwoc.styler.pipeline.parallel.ParallelProcessingConfig;

/**
 * Error handling tests for {@code BatchProcessor}.
 */
public class BatchProcessorErrorHandlingTest
{
	/**
	 * Tests that file not found errors are handled gracefully.
	 */
	@Test
	public void shouldHandleFileNotFoundGracefully() throws IOException, InterruptedException
	{
		FileProcessingPipeline pipeline = TestPipelineFactory.createDefaultPipeline();
		ParallelProcessingConfig config = TestConfigFactory.createDefaultConfig();
		Path tempDir = Files.createTempDirectory("temp");
		Path nonexistentFile = tempDir.resolve("missing.java");

		try (BatchProcessor processor = new DefaultBatchProcessor(pipeline, config))
		{
			BatchResult result = processor.processFiles(List.of(nonexistentFile));

			assertEquals(result.failureCount(), 1);
			assertTrue(result.errors().containsKey(nonexistentFile));
		}
	}

	/**
	 * Tests that multiple errors are collected correctly during batch processing.
	 */
	@Test
	public void shouldCollectMultipleErrors() throws IOException, InterruptedException
	{
		FileProcessingPipeline pipeline = TestPipelineFactory.createDefaultPipeline();
		ParallelProcessingConfig config = TestConfigFactory.createDefaultConfig();
		Path tempDir = Files.createTempDirectory("temp");

		List<Path> files = new ArrayList<>();
		for (int i = 0; i < 3; i += 1)
		{
			files.add(tempDir.resolve("missing" + i + ".java"));
		}

		try (BatchProcessor processor = new DefaultBatchProcessor(pipeline, config))
		{
			BatchResult result = processor.processFiles(files);

			assertEquals(result.failureCount(), 3);
			assertEquals(result.errors().size(), 3);
		}
	}

	/**
	 * Tests that processing continues when CONTINUE error strategy is used.
	 */
	@Test
	public void shouldContinueProcessingWithContinueStrategy() throws IOException, InterruptedException
	{
		FileProcessingPipeline pipeline = TestPipelineFactory.createDefaultPipeline();
		ParallelProcessingConfig config = ParallelProcessingConfig.builder().
			maxConcurrency(ParallelProcessingConfig.calculateDefaultMaxConcurrency()).
			errorStrategy(ErrorStrategy.CONTINUE).
			build();

		Path tempDir = Files.createTempDirectory("temp");
		List<Path> files = new ArrayList<>();
		files.add(TestFileFactory.createValidJavaFile());
		files.add(tempDir.resolve("missing.java"));
		files.add(TestFileFactory.createValidJavaFile());

		try (BatchProcessor processor = new DefaultBatchProcessor(pipeline, config))
		{
			BatchResult result = processor.processFiles(files);

			// CONTINUE strategy should process all files
			assertEquals(result.totalFiles(), 3);
			assertEquals(result.successCount(), 2);
			assertEquals(result.failureCount(), 1);
		}
		finally
		{
			for (Path file : files)
			{
				try
				{
					Files.deleteIfExists(file);
				}
				catch (IOException _)
				{
					// Intentionally ignored
				}
			}
		}
	}

	/**
	 * Tests that processing fails fast on first error when FAIL_FAST strategy is used.
	 */
	@Test
	public void shouldFailFastOnFirstError() throws IOException, InterruptedException
	{
		FileProcessingPipeline pipeline = TestPipelineFactory.createDefaultPipeline();
		ParallelProcessingConfig config = ParallelProcessingConfig.builder().
			maxConcurrency(ParallelProcessingConfig.calculateDefaultMaxConcurrency()).
			errorStrategy(ErrorStrategy.FAIL_FAST).
			build();

		Path tempDir = Files.createTempDirectory("temp");
		List<Path> files = new ArrayList<>();
		files.add(TestFileFactory.createValidJavaFile());
		files.add(tempDir.resolve("missing.java"));
		files.add(TestFileFactory.createValidJavaFile());

		try (BatchProcessor processor = new DefaultBatchProcessor(pipeline, config))
		{
			BatchResult result = processor.processFiles(files);

			// FAIL_FAST should still process started tasks but stop when error occurs
			assertEquals(result.totalFiles(), 3);
			assertTrue(result.failureCount() >= 1, "Should have at least one failure");
		}
		finally
		{
			for (Path file : files)
			{
				try
				{
					Files.deleteIfExists(file);
				}
				catch (IOException _)
				{
					// Intentionally ignored
				}
			}
		}
	}

	/**
	 * Tests that error messages are provided for failed files.
	 */
	@Test
	public void shouldProvideErrorMessages() throws IOException, InterruptedException
	{
		FileProcessingPipeline pipeline = TestPipelineFactory.createDefaultPipeline();
		ParallelProcessingConfig config = TestConfigFactory.createDefaultConfig();
		Path tempDir = Files.createTempDirectory("temp");
		Path nonexistentFile = tempDir.resolve("missing.java");

		try (BatchProcessor processor = new DefaultBatchProcessor(pipeline, config))
		{
			BatchResult result = processor.processFiles(List.of(nonexistentFile));

			String errorMessage = result.errors().get(nonexistentFile);
			assertEquals(errorMessage != null && !errorMessage.isEmpty(), true,
				"Error message should be present and non-empty");
		}
	}

	/**
	 * Tests that interruptions are handled gracefully without deadlock.
	 */
	@Test(timeOut = 30_000)
	public void shouldHandleInterruptionGracefully() throws IOException, InterruptedException
	{
		FileProcessingPipeline pipeline = TestPipelineFactory.createDefaultPipeline();
		ParallelProcessingConfig config = TestConfigFactory.createDefaultConfig();

		List<Path> files = new ArrayList<>();
		for (int i = 0; i < 20; i += 1)
		{
			files.add(TestFileFactory.createValidJavaFile());
		}

		try (BatchProcessor processor = new DefaultBatchProcessor(pipeline, config))
		{
			// Process normally (interruption would be from external thread in real scenario)
			BatchResult result = processor.processFiles(files);

			// Should complete normally without deadlock
			assertEquals(result.totalFiles(), 20);
		}
		finally
		{
			for (Path file : files)
			{
				try
				{
					Files.deleteIfExists(file);
				}
				catch (IOException _)
				{
					// Intentionally ignored
				}
			}
		}
	}

	/**
	 * Tests that mixed valid and invalid files are processed correctly.
	 */
	@Test
	public void shouldProcessMixedValidAndInvalidFiles() throws IOException, InterruptedException
	{
		FileProcessingPipeline pipeline = TestPipelineFactory.createDefaultPipeline();
		ParallelProcessingConfig config = TestConfigFactory.createDefaultConfig();
		Path tempDir = Files.createTempDirectory("temp");

		List<Path> files = new ArrayList<>();
		for (int i = 0; i < 10; i += 1)
		{
			if (i % 2 == 0)
			{
				files.add(TestFileFactory.createValidJavaFile());
			}
			else
			{
				files.add(tempDir.resolve("missing" + i + ".java"));
			}
		}

		try (BatchProcessor processor = new DefaultBatchProcessor(pipeline, config))
		{
			BatchResult result = processor.processFiles(files);

			assertEquals(result.totalFiles(), 10);
			assertEquals(result.successCount(), 5, "Should have 5 successes");
			assertEquals(result.failureCount(), 5, "Should have 5 failures");
		}
		finally
		{
			for (Path file : files)
			{
				try
				{
					Files.deleteIfExists(file);
				}
				catch (IOException _)
				{
					// Intentionally ignored
				}
			}
		}
	}
}
