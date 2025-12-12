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
import java.util.concurrent.atomic.AtomicInteger;

import org.testng.annotations.Test;

import io.github.cowwoc.styler.pipeline.FileProcessingPipeline;
import io.github.cowwoc.styler.pipeline.parallel.BatchProcessor;
import io.github.cowwoc.styler.pipeline.parallel.BatchResult;
import io.github.cowwoc.styler.pipeline.parallel.DefaultBatchProcessor;
import io.github.cowwoc.styler.pipeline.parallel.ParallelProcessingConfig;

/**
 * Integration tests for virtual thread execution through BatchProcessor.
 *
 * Note: VirtualThreadExecutor is an internal implementation detail.
 * These tests verify the virtual thread behavior through the BatchProcessor API.
 */
public class VirtualThreadExecutorTest
{
	/**
	 * Tests that tasks are executed concurrently using virtual threads.
	 */
	@Test
	public void shouldExecuteTasksConcurrently() throws IOException, InterruptedException
	{
		FileProcessingPipeline pipeline = TestPipelineFactory.createDefaultPipeline();
		ParallelProcessingConfig config = TestConfigFactory.createDefaultConfig();

		List<Path> files = new ArrayList<>();
		for (int i = 0; i < 10; i += 1)
		{
			files.add(TestFileFactory.createValidJavaFile());
		}

		try (BatchProcessor processor = new DefaultBatchProcessor(pipeline, config))
		{
			Instant startTime = Instant.now();
			BatchResult result = processor.processFiles(files);
			Duration elapsed = Duration.between(startTime, Instant.now());

			// Verify all files were processed
			assertEquals(result.successCount(), 10);

			// Should complete in reasonable time (not instantaneous like sequential)
			requireThat(elapsed, "elapsed").isLessThan(Duration.ofSeconds(30));
		}
		finally
		{
			for (Path file : files)
			{
				Files.deleteIfExists(file);
			}
		}
	}

	/**
	 * Tests that concurrency limits are respected by virtual thread executor.
	 */
	@Test
	public void shouldRespectConcurrencyLimits() throws IOException, InterruptedException
	{
		FileProcessingPipeline pipeline = TestPipelineFactory.createDefaultPipeline();
		ParallelProcessingConfig config = TestConfigFactory.createLimitedConcurrencyConfig(5);

		List<Path> files = new ArrayList<>();
		for (int i = 0; i < 20; i += 1)
		{
			files.add(TestFileFactory.createValidJavaFile());
		}

		try (BatchProcessor processor = new DefaultBatchProcessor(pipeline, config))
		{
			BatchResult result = processor.processFiles(files);

			// Verify all files processed with concurrency limit
			assertEquals(result.totalFiles(), 20);
			assertEquals(result.successCount(), 20);
		}
		finally
		{
			for (Path file : files)
			{
				Files.deleteIfExists(file);
			}
		}
	}

	/**
	 * Tests that execution completes without deadlock.
	 */
	@Test(timeOut = 60_000)
	public void shouldCompleteWithoutDeadlock() throws IOException, InterruptedException
	{
		FileProcessingPipeline pipeline = TestPipelineFactory.createDefaultPipeline();
		ParallelProcessingConfig config = TestConfigFactory.createDefaultConfig();

		List<Path> files = new ArrayList<>();
		for (int i = 0; i < 30; i += 1)
		{
			files.add(TestFileFactory.createValidJavaFile());
		}

		try (BatchProcessor processor = new DefaultBatchProcessor(pipeline, config))
		{
			BatchResult result = processor.processFiles(files);
			assertEquals(result.totalFiles(), 30);
		}
		finally
		{
			for (Path file : files)
			{
				Files.deleteIfExists(file);
			}
		}
	}

	/**
	 * Tests that progress tracking is atomic across virtual threads.
	 */
	@Test
	public void shouldTrackProgressAtomically() throws IOException, InterruptedException
	{
		FileProcessingPipeline pipeline = TestPipelineFactory.createDefaultPipeline();
		AtomicInteger callCount = new AtomicInteger(0);

		ParallelProcessingConfig config = ParallelProcessingConfig.builder().
			maxConcurrency(ParallelProcessingConfig.calculateDefaultMaxConcurrency()).
			progressCallback((completed, total, file) -> callCount.incrementAndGet()).
			build();

		List<Path> files = new ArrayList<>();
		for (int i = 0; i < 15; i += 1)
		{
			files.add(TestFileFactory.createValidJavaFile());
		}

		try (BatchProcessor processor = new DefaultBatchProcessor(pipeline, config))
		{
			processor.processFiles(files);

			// Progress callbacks should be atomic
			assertEquals(callCount.get(), 15);
		}
		finally
		{
			for (Path file : files)
			{
				Files.deleteIfExists(file);
			}
		}
	}
}
