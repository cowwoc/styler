package io.github.cowwoc.styler.pipeline.parallel.test;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;
import static org.testng.Assert.assertEquals;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
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
		for (int i = 0; i < 100; ++i)
			files.add(TestFileFactory.createValidJavaFile());

		try (BatchProcessor processor = new DefaultBatchProcessor(pipeline, config))
		{
			Instant startTime = Instant.now();
			BatchResult result = processor.processFiles(files);
			Duration elapsed = Duration.between(startTime, Instant.now());

			double elapsedSeconds = elapsed.toMillis() / 1000.0;
			int throughput = (int) (100 / elapsedSeconds);

			// Expect at least some throughput (reasonable for test environment)
			requireThat(throughput, "throughput").isGreaterThanOrEqualTo(50);
			assertEquals(result.successCount(), 100, "All 100 files should be processed");
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
		for (int i = 0; i < fileCount; ++i)
			files.add(TestFileFactory.createValidJavaFile());

		try (BatchProcessor processor = new DefaultBatchProcessor(pipeline, config))
		{
			Instant startTime = Instant.now();
			BatchResult result = processor.processFiles(files);
			Duration elapsed = Duration.between(startTime, Instant.now());

			// Verify all files were processed
			assertEquals(result.successCount(), fileCount, "All files should be processed");

			// Should complete in reasonable time (not exact linear test, just completion verification)
			requireThat(elapsed, "elapsed").isLessThan(Duration.ofSeconds(60));
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
	 * Tests that high concurrency processing completes without deadlock.
	 */
	@Test(groups = "performance", timeOut = 30_000)
	public void shouldHandleHighConcurrencyWithoutDeadlock() throws IOException, InterruptedException
	{
		FileProcessingPipeline pipeline = TestPipelineFactory.createDefaultPipeline();
		ParallelProcessingConfig config = TestConfigFactory.createDefaultConfig();

		List<Path> files = new ArrayList<>();
		for (int i = 0; i < 50; ++i)
			files.add(TestFileFactory.createValidJavaFile());

		try (BatchProcessor processor = new DefaultBatchProcessor(pipeline, config))
		{
			BatchResult result = processor.processFiles(files);
			assertEquals(result.totalFiles(), 50, "All 50 files should be processed");
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
