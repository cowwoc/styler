package io.github.cowwoc.styler.pipeline.parallel.test;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.testng.annotations.Test;

import io.github.cowwoc.styler.pipeline.FileProcessingPipeline;
import io.github.cowwoc.styler.pipeline.parallel.BatchProcessor;
import io.github.cowwoc.styler.pipeline.parallel.BatchResult;
import io.github.cowwoc.styler.pipeline.parallel.DefaultBatchProcessor;
import io.github.cowwoc.styler.pipeline.parallel.ParallelProcessingConfig;

/**
 * Integration tests for BatchProcessor with memory reservation.
 */
public class BatchProcessorMemoryReservationIntegrationTest
{
	/**
	 * Tests that batch processor handles a mix of valid and invalid files.
	 */
	@Test
	public void shouldHandleMixOfValidAndInvalidFiles() throws IOException, InterruptedException
	{
		FileProcessingPipeline pipeline = TestPipelineFactory.createDefaultPipeline();
		ParallelProcessingConfig config = TestConfigFactory.createDefaultConfig();
		List<Path> files = new ArrayList<>();

		try
		{
			// Create valid files
			for (int i = 0; i < 3; ++i)
				files.add(TestFileFactory.createValidJavaFile());

			// Add a nonexistent file
			files.add(Path.of("/nonexistent/file.java"));

			try (BatchProcessor processor = new DefaultBatchProcessor(pipeline, config))
			{
				BatchResult result = processor.processFiles(files);

				// Some files should succeed
				requireThat(result.successCount(), "result.successCount()").isPositive();
				// Some files should fail
				requireThat(result.failureCount(), "result.failureCount()").isPositive();
			}
		}
		finally
		{
			TestFileFactory.deleteFilesQuietly(files);
		}
	}

	/**
	 * Tests that all files are eventually processed when using memory reservation.
	 * <p>
	 * Files may temporarily block waiting for permits, but all should complete.
	 */
	@Test
	public void shouldProcessAllFilesEventually() throws IOException, InterruptedException
	{
		FileProcessingPipeline pipeline = TestPipelineFactory.createDefaultPipeline();
		ParallelProcessingConfig config = TestConfigFactory.createDefaultConfig();
		List<Path> files = new ArrayList<>();

		try
		{
			// Create multiple files
			for (int i = 0; i < 10; ++i)
				files.add(TestFileFactory.createValidJavaFile());

			try (BatchProcessor processor = new DefaultBatchProcessor(pipeline, config))
			{
				BatchResult result = processor.processFiles(files);

				// All files should eventually complete
				requireThat(result.totalFiles(), "result.totalFiles()").isEqualTo(10);
				int totalProcessed = result.successCount() + result.failureCount();
				requireThat(totalProcessed, "totalProcessed").isEqualTo(10);
			}
		}
		finally
		{
			TestFileFactory.deleteFilesQuietly(files);
		}
	}
}
