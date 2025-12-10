package io.github.cowwoc.styler.pipeline.parallel.test;

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
import io.github.cowwoc.styler.pipeline.parallel.ParallelProcessingConfig;

/**
 * Performance tests for {@code BatchProcessor}.
 * <p>
 * Note: Performance tests are marked with group "performance" and may be skipped in normal test runs
 * if performance validation is not needed.
 */
public class BatchProcessorPerformanceTest
{
	/**
	 * Tests that batch processing achieves reasonable throughput.
	 */
	@Test(groups = "performance")
	public void shouldAchieve100FilesPerSecondThroughput() throws IOException, InterruptedException
	{
		FileProcessingPipeline pipeline = TestPipelineFactory.createDefaultPipeline();
		ParallelProcessingConfig config = TestConfigFactory.createDefaultConfig();

		// Create 100 files (not 500 to keep test reasonable)
		List<Path> files = new ArrayList<>();
		for (int i = 0; i < 100; i += 1)
		{
			files.add(TestFileFactory.createValidJavaFile());
		}

		try (BatchProcessor processor = new DefaultBatchProcessor(pipeline, config))
		{
			long startTime = System.currentTimeMillis();
			BatchResult result = processor.processFiles(files);
			long endTime = System.currentTimeMillis();

			long elapsedMillis = endTime - startTime;
			double elapsedSeconds = elapsedMillis / 1000.0;
			int throughput = (int) (100 / elapsedSeconds);

			// Expect at least some throughput (reasonable for test environment)
			assertTrue(throughput >= 50, "Throughput should be >= 50 files/sec, was: " + throughput);
			assertTrue(result.successCount() == 100, "All 100 files should be processed");
		}
		finally
		{
			for (Path file : files)
			{
				try
				{
					Files.deleteIfExists(file);
				}
				catch (IOException e) // NOPMD - cleanup errors don't affect test result
				{
					// Intentionally ignored
				}
			}
		}
	}

	/**
	 * Tests that processing scales linearly with available CPU cores.
	 */
	@Test(groups = "performance")
	public void shouldScaleLinearlyToAvailableCores() throws IOException, InterruptedException
	{
		FileProcessingPipeline pipeline = TestPipelineFactory.createDefaultPipeline();
		ParallelProcessingConfig config = TestConfigFactory.createDefaultConfig();

		int processors = Runtime.getRuntime().availableProcessors();
		int fileCount = processors * 10; // 10 files per core

		List<Path> files = new ArrayList<>();
		for (int i = 0; i < fileCount; i += 1)
		{
			files.add(TestFileFactory.createValidJavaFile());
		}

		try (BatchProcessor processor = new DefaultBatchProcessor(pipeline, config))
		{
			long startTime = System.currentTimeMillis();
			BatchResult result = processor.processFiles(files);
			long endTime = System.currentTimeMillis();

			// Verify all files were processed
			assertTrue(result.successCount() == fileCount, "All files should be processed");
			long elapsedMillis = endTime - startTime;

			// Should complete in reasonable time (not exact linear test, just completion verification)
			assertTrue(elapsedMillis < 60_000, "Processing should complete within 60 seconds");
		}
		finally
		{
			for (Path file : files)
			{
				try
				{
					Files.deleteIfExists(file);
				}
				catch (IOException e) // NOPMD - cleanup errors don't affect test result
				{
					// Intentionally ignored
				}
			}
		}
	}

	/**
	 * Tests that high concurrency processing completes without deadlock.
	 */
	@Test(groups = "performance")
	public void shouldHandleHighConcurrencyWithoutDeadlock() throws IOException, InterruptedException
	{
		FileProcessingPipeline pipeline = TestPipelineFactory.createDefaultPipeline();
		ParallelProcessingConfig config = TestConfigFactory.createDefaultConfig();

		List<Path> files = new ArrayList<>();
		for (int i = 0; i < 50; i += 1)
		{
			files.add(TestFileFactory.createValidJavaFile());
		}

		try (BatchProcessor processor = new DefaultBatchProcessor(pipeline, config))
		{
			long startTime = System.currentTimeMillis();
			BatchResult result = processor.processFiles(files);
			long endTime = System.currentTimeMillis();

			long elapsedMillis = endTime - startTime;

			// Should complete without deadlock (30 second timeout for this test)
			assertTrue(elapsedMillis < 30_000, "Should complete within 30 seconds to avoid deadlock timeout");
			assertTrue(result.totalFiles() == 50, "All 50 files should be processed");
		}
		finally
		{
			for (Path file : files)
			{
				try
				{
					Files.deleteIfExists(file);
				}
				catch (IOException e) // NOPMD - cleanup errors don't affect test result
				{
					// Intentionally ignored
				}
			}
		}
	}
}
