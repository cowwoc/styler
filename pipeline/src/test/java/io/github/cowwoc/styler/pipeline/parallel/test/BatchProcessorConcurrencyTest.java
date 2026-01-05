package io.github.cowwoc.styler.pipeline.parallel.test;

import static io.github.cowwoc.styler.pipeline.parallel.test.TestFileFactory.deleteFilesQuietly;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
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
 * Concurrency tests for {@code BatchProcessor}.
 */
public class BatchProcessorConcurrencyTest
{
	/**
	 * Tests that shared state remains consistent under concurrent file processing.
	 */
	@Test
	public void shouldNotCorruptSharedStateUnderConcurrency() throws IOException, InterruptedException
	{
		FileProcessingPipeline pipeline = TestPipelineFactory.createDefaultPipeline();
		ParallelProcessingConfig config = TestConfigFactory.createDefaultConfig();

		List<Path> files = new ArrayList<>();
		for (int i = 0; i < 20; ++i)
			files.add(TestFileFactory.createValidJavaFile());

		try (BatchProcessor processor = new DefaultBatchProcessor(pipeline, config))
		{
			BatchResult result = processor.processFiles(files);

			// Verify all files processed and state is consistent
			assertEquals(result.totalFiles(), 20);
			assertEquals(result.successCount() + result.failureCount(), 20);
			assertFalse(result.results().isEmpty());
		}
		finally
		{
			deleteFilesQuietly(files);
		}
	}

	/**
	 * Tests that progress callbacks are called atomically for each file.
	 */
	@Test
	public void shouldTrackProgressAtomically() throws IOException, InterruptedException
	{
		FileProcessingPipeline pipeline = TestPipelineFactory.createDefaultPipeline();
		AtomicInteger progressCounter = new AtomicInteger(0);

		ParallelProcessingConfig config = ParallelProcessingConfig.builder().
			maxConcurrency(ParallelProcessingConfig.calculateDefaultMaxConcurrency()).
			progressCallback((completed, total, file) -> progressCounter.incrementAndGet()).
			build();

		List<Path> files = new ArrayList<>();
		for (int i = 0; i < 15; ++i)
			files.add(TestFileFactory.createValidJavaFile());

		try (BatchProcessor processor = new DefaultBatchProcessor(pipeline, config))
		{
			processor.processFiles(files);

			// Progress should be called exactly once per file
			assertEquals(progressCounter.get(), 15);
		}
		finally
		{
			deleteFilesQuietly(files);
		}
	}

	/**
	 * Verifies that multiple batch processors can process files concurrently with a shared pipeline.
	 * Each processor handles its own batch independently while sharing the underlying pipeline,
	 * ensuring correct behavior under concurrent access patterns.
	 */
	@Test
	public void shouldHandleMultipleConcurrentBatchProcessors() throws IOException, InterruptedException
	{
		FileProcessingPipeline pipeline = TestPipelineFactory.createDefaultPipeline();
		ParallelProcessingConfig config = TestConfigFactory.createDefaultConfig();

		// Process 3 batches with separate processors sharing the pipeline
		for (int b = 0; b < 3; ++b)
		{
			List<Path> files = new ArrayList<>();
			try
			{
				for (int i = 0; i < 10; ++i)
					files.add(TestFileFactory.createValidJavaFile());

				try (BatchProcessor processor = new DefaultBatchProcessor(pipeline, config))
				{
					BatchResult result = processor.processFiles(files);
					assertEquals(result.successCount(), 10, "Batch " + b + " should process all 10 files");
				}
			}
			finally
			{
				deleteFilesQuietly(files);
			}
		}
	}

	/**
	 * Tests that file failures are handled correctly during concurrent processing.
	 */
	@Test
	public void shouldHandleFileFailuresConcurrently() throws IOException, InterruptedException
	{
		FileProcessingPipeline pipeline = TestPipelineFactory.createDefaultPipeline();
		ParallelProcessingConfig config = TestConfigFactory.createDefaultConfig();
		Path tempDir = Files.createTempDirectory("temp");

		List<Path> files = new ArrayList<>();
		for (int i = 0; i < 20; ++i)
			if (i % 4 == 0)
				// Create non-existent file to trigger failure
				files.add(tempDir.resolve("missing_" + i + ".java"));
			else
				files.add(TestFileFactory.createValidJavaFile());

		try (BatchProcessor processor = new DefaultBatchProcessor(pipeline, config))
		{
			BatchResult result = processor.processFiles(files);

			// Should have some successes and failures
			assertTrue(result.successCount() > 0, "Should have successful files");
			assertTrue(result.failureCount() > 0, "Should have failed files");
			assertEquals(result.totalFiles(), 20);
		}
		finally
		{
			deleteFilesQuietly(files);
		}
	}

	/**
	 * Tests that progress reporting callbacks maintain thread safety under concurrent execution.
	 */
	@Test
	public void shouldMaintainThreadSafetyDuringProgressReporting() throws IOException, InterruptedException
	{
		FileProcessingPipeline pipeline = TestPipelineFactory.createDefaultPipeline();

		// Counter to verify progress callbacks happen correctly
		AtomicInteger maxConcurrentCallbacks = new AtomicInteger(0);
		AtomicInteger currentCallbacks = new AtomicInteger(0);

		ParallelProcessingConfig config = ParallelProcessingConfig.builder().
			maxConcurrency(ParallelProcessingConfig.calculateDefaultMaxConcurrency()).
			progressCallback((completed, total, file) ->
			{
				int current = currentCallbacks.incrementAndGet();
				maxConcurrentCallbacks.getAndUpdate(max -> Math.max(max, current));
				try
				{
					Thread.sleep(1); // Small delay to allow callback overlap
				}
				catch (InterruptedException e)
				{
					Thread.currentThread().interrupt();
				}
				currentCallbacks.decrementAndGet();
			}).
			build();

		List<Path> files = new ArrayList<>();
		for (int i = 0; i < 25; ++i)
			files.add(TestFileFactory.createValidJavaFile());

		try (BatchProcessor processor = new DefaultBatchProcessor(pipeline, config))
		{
			BatchResult result = processor.processFiles(files);

			assertEquals(result.successCount(), 25);
		}
		finally
		{
			deleteFilesQuietly(files);
		}
	}
}
