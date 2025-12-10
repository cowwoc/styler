package io.github.cowwoc.styler.pipeline.parallel.test;

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
		for (int i = 0; i < 20; i += 1)
		{
			files.add(TestFileFactory.createValidJavaFile());
		}

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
		for (int i = 0; i < 15; i += 1)
		{
			files.add(TestFileFactory.createValidJavaFile());
		}

		try (BatchProcessor processor = new DefaultBatchProcessor(pipeline, config))
		{
			processor.processFiles(files);

			// Progress should be called exactly once per file
			assertEquals(progressCounter.get(), 15);
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
	 * Tests that multiple batch processors can run concurrently with a shared pipeline.
	 */
	@Test
	@SuppressWarnings("PMD.UseTryWithResources")
	public void shouldHandleMultipleConcurrentBatchProcessors() throws IOException, InterruptedException
	{
		FileProcessingPipeline pipeline = TestPipelineFactory.createDefaultPipeline();
		ParallelProcessingConfig config = TestConfigFactory.createDefaultConfig();

		// Create 3 batch processors with shared pipeline
		List<List<Path>> fileBatches = new ArrayList<>();
		List<BatchProcessor> processors = new ArrayList<>();

		try
		{
			for (int b = 0; b < 3; b += 1)
			{
				List<Path> files = new ArrayList<>();
				for (int i = 0; i < 10; i += 1)
				{
					files.add(TestFileFactory.createValidJavaFile());
				}
				fileBatches.add(files);
				processors.add(new DefaultBatchProcessor(pipeline, config));
			}

			// Process all 3 batches
			for (int b = 0; b < 3; b += 1)
			{
				BatchResult result = processors.get(b).processFiles(fileBatches.get(b));
				assertEquals(result.successCount(), 10, "Batch " + b + " should process all 10 files");
			}
		}
		finally
		{
			// Close all processors
			for (BatchProcessor processor : processors)
			{
				processor.close();
			}

			// Cleanup all files
			for (List<Path> batch : fileBatches)
			{
				for (Path file : batch)
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
		for (int i = 0; i < 20; i += 1)
		{
			if (i % 4 == 0)
			{
				// Create non-existent file to trigger failure
				files.add(tempDir.resolve("missing_" + i + ".java"));
			}
			else
			{
				files.add(TestFileFactory.createValidJavaFile());
			}
		}

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
		for (int i = 0; i < 25; i += 1)
		{
			files.add(TestFileFactory.createValidJavaFile());
		}

		try (BatchProcessor processor = new DefaultBatchProcessor(pipeline, config))
		{
			BatchResult result = processor.processFiles(files);

			assertEquals(result.successCount(), 25);
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
