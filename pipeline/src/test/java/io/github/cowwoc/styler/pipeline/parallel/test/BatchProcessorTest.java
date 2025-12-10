package io.github.cowwoc.styler.pipeline.parallel.test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.testng.annotations.Test;

import io.github.cowwoc.styler.pipeline.FileProcessingPipeline;
import io.github.cowwoc.styler.pipeline.parallel.BatchProcessor;
import io.github.cowwoc.styler.pipeline.parallel.BatchResult;
import io.github.cowwoc.styler.pipeline.parallel.DefaultBatchProcessor;
import io.github.cowwoc.styler.pipeline.parallel.ErrorStrategy;
import io.github.cowwoc.styler.pipeline.parallel.ParallelProcessingConfig;

/**
 * Integration tests for {@code BatchProcessor}.
 */
public class BatchProcessorTest
{
	/**
	 * Tests that null file list is rejected.
	 */
	@Test(expectedExceptions = NullPointerException.class)
	public void shouldRejectNullFileList() throws InterruptedException
	{
		FileProcessingPipeline pipeline = TestPipelineFactory.createDefaultPipeline();
		ParallelProcessingConfig config = TestConfigFactory.createDefaultConfig();

		try (BatchProcessor processor = new DefaultBatchProcessor(pipeline, config))
		{
			processor.processFiles(null);
		}
	}

	/**
	 * Tests that empty file list is processed correctly.
	 */
	@Test
	public void shouldProcessEmptyFileList() throws InterruptedException
	{
		FileProcessingPipeline pipeline = TestPipelineFactory.createDefaultPipeline();
		ParallelProcessingConfig config = TestConfigFactory.createDefaultConfig();

		try (BatchProcessor processor = new DefaultBatchProcessor(pipeline, config))
		{
			BatchResult result = processor.processFiles(List.of());

			assertEquals(result.totalFiles(), 0);
			assertEquals(result.successCount(), 0);
			assertEquals(result.failureCount(), 0);
			assertTrue(result.results().isEmpty());
		}
	}

	/**
	 * Tests that a single file is processed correctly.
	 */
	@Test
	public void shouldProcessSingleFile() throws IOException, InterruptedException
	{
		FileProcessingPipeline pipeline = TestPipelineFactory.createDefaultPipeline();
		ParallelProcessingConfig config = TestConfigFactory.createDefaultConfig();
		Path tempFile = TestFileFactory.createValidJavaFile();

		try (BatchProcessor processor = new DefaultBatchProcessor(pipeline, config))
		{
			BatchResult result = processor.processFiles(List.of(tempFile));

			assertEquals(result.totalFiles(), 1);
			assertEquals(result.successCount(), 1);
			assertEquals(result.failureCount(), 0);
			assertEquals(result.results().size(), 1);
		}
		finally
		{
			Files.deleteIfExists(tempFile);
		}
	}

	/**
	 * Tests that multiple files are processed correctly.
	 */
	@Test
	public void shouldProcessMultipleFiles() throws IOException, InterruptedException
	{
		FileProcessingPipeline pipeline = TestPipelineFactory.createDefaultPipeline();
		ParallelProcessingConfig config = TestConfigFactory.createDefaultConfig();
		List<Path> files = new ArrayList<>();

		for (int i = 0; i < 5; ++i)
		{
			files.add(TestFileFactory.createValidJavaFile());
		}

		try (BatchProcessor processor = new DefaultBatchProcessor(pipeline, config))
		{
			BatchResult result = processor.processFiles(files);

			assertEquals(result.totalFiles(), 5);
			assertEquals(result.successCount(), 5);
			assertEquals(result.failureCount(), 0);
			assertEquals(result.results().size(), 5);
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
	 * Tests that file failures are isolated and don't affect other files.
	 */
	@Test
	public void shouldIsolateFileFailures() throws IOException, InterruptedException
	{
		FileProcessingPipeline pipeline = TestPipelineFactory.createDefaultPipeline();
		ParallelProcessingConfig config = TestConfigFactory.createDefaultConfig();

		List<Path> files = new ArrayList<>();
		files.add(TestFileFactory.createValidJavaFile());
		files.add(TestFileFactory.createValidJavaFile());
		files.add(Paths.get(Files.createTempDirectory("temp").toString(), "nonexistent.java"));
		files.add(TestFileFactory.createValidJavaFile());
		files.add(TestFileFactory.createValidJavaFile());

		try (BatchProcessor processor = new DefaultBatchProcessor(pipeline, config))
		{
			BatchResult result = processor.processFiles(files);

			assertEquals(result.totalFiles(), 5);
			assertEquals(result.successCount(), 4, "4 files should succeed");
			assertEquals(result.failureCount(), 1, "1 file should fail");
			// All results should be present (no cascading failure)
			assertEquals(result.results().size() + result.errors().size(), 5);
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
	 * Tests that errors are reported in the result.
	 */
	@Test
	public void shouldReportErrorsInResult() throws IOException, InterruptedException
	{
		FileProcessingPipeline pipeline = TestPipelineFactory.createDefaultPipeline();
		ParallelProcessingConfig config = TestConfigFactory.createDefaultConfig();
		Path nonexistentFile = Paths.get(Files.createTempDirectory("temp").toString(), "missing.java");

		try (BatchProcessor processor = new DefaultBatchProcessor(pipeline, config))
		{
			BatchResult result = processor.processFiles(List.of(nonexistentFile));

			assertEquals(result.failureCount(), 1);
			assertEquals(result.errors().size(), 1);
			assertTrue(result.errors().containsKey(nonexistentFile));
			assertNotNull(result.errors().get(nonexistentFile));
		}
	}

	/**
	 * Tests that null pipeline is rejected.
	 */
	@Test(expectedExceptions = NullPointerException.class)
	public void shouldRejectNullPipeline()
	{
		ParallelProcessingConfig config = TestConfigFactory.createDefaultConfig();

		new DefaultBatchProcessor(null, config);
	}

	/**
	 * Tests that null config is rejected.
	 */
	@Test(expectedExceptions = NullPointerException.class)
	public void shouldRejectNullConfig()
	{
		FileProcessingPipeline pipeline = TestPipelineFactory.createDefaultPipeline();

		new DefaultBatchProcessor(pipeline, null);
	}

	/**
	 * Tests that the processor closes gracefully.
	 */
	@SuppressWarnings({"try", "PMD.UnusedLocalVariable"})
	@Test
	public void shouldCloseGracefully() throws IOException, InterruptedException
	{
		FileProcessingPipeline pipeline = TestPipelineFactory.createDefaultPipeline();
		ParallelProcessingConfig config = TestConfigFactory.createDefaultConfig();

		try (BatchProcessor processor = new DefaultBatchProcessor(pipeline, config))
		{
			// Just close it - testing that close() works without exception
		}
		// If we get here without exception, close was successful
	}

	/**
	 * Tests that close is idempotent and can be called multiple times.
	 */
	@Test
	public void shouldBeIdempotentOnClose() throws IOException, InterruptedException
	{
		FileProcessingPipeline pipeline = TestPipelineFactory.createDefaultPipeline();
		ParallelProcessingConfig config = TestConfigFactory.createDefaultConfig();

		BatchProcessor processor = new DefaultBatchProcessor(pipeline, config);
		processor.close();
		processor.close(); // Should not throw
	}

	/**
	 * Tests that throughput metrics are reported.
	 */
	@Test
	public void shouldReportThroughput() throws IOException, InterruptedException
	{
		FileProcessingPipeline pipeline = TestPipelineFactory.createDefaultPipeline();
		ParallelProcessingConfig config = TestConfigFactory.createDefaultConfig();

		List<Path> files = new ArrayList<>();
		for (int i = 0; i < 10; ++i)
		{
			files.add(TestFileFactory.createValidJavaFile());
		}

		try (BatchProcessor processor = new DefaultBatchProcessor(pipeline, config))
		{
			BatchResult result = processor.processFiles(files);

			assertTrue(result.throughputFilesPerSecond() >= 0.0);
			assertNotNull(result.totalDuration());
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
	 * Tests that error strategy is respected.
	 */
	@Test
	public void shouldRespectErrorStrategy() throws IOException, InterruptedException
	{
		FileProcessingPipeline pipeline = TestPipelineFactory.createDefaultPipeline();
		ParallelProcessingConfig config = ParallelProcessingConfig.builder().
			maxConcurrency(ParallelProcessingConfig.calculateDefaultMaxConcurrency()).
			errorStrategy(ErrorStrategy.CONTINUE).
			build();

		List<Path> files = new ArrayList<>();
		files.add(TestFileFactory.createValidJavaFile());
		files.add(Paths.get(Files.createTempDirectory("temp").toString(), "missing.java"));
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
				catch (IOException e) // NOPMD - cleanup errors don't affect test result
				{
					// Intentionally ignored
				}
			}
		}
	}

	/**
	 * Tests that progress callback is invoked correctly.
	 */
	@Test
	public void shouldTrackProgressWithCallback() throws IOException, InterruptedException
	{
		FileProcessingPipeline pipeline = TestPipelineFactory.createDefaultPipeline();
		AtomicInteger progressCallCount = new AtomicInteger(0);

		ParallelProcessingConfig config = ParallelProcessingConfig.builder().
			maxConcurrency(ParallelProcessingConfig.calculateDefaultMaxConcurrency()).
			errorStrategy(ErrorStrategy.CONTINUE).
			progressCallback((completed, total, file) -> progressCallCount.incrementAndGet()).
			build();

		List<Path> files = new ArrayList<>();
		for (int i = 0; i < 5; ++i)
		{
			files.add(TestFileFactory.createValidJavaFile());
		}

		try (BatchProcessor processor = new DefaultBatchProcessor(pipeline, config))
		{
			processor.processFiles(files);

			assertEquals(progressCallCount.get(), 5);
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
	 * Tests that files are processed with custom concurrency settings.
	 */
	@Test
	public void shouldProcessFilesWithCustomConcurrency() throws IOException, InterruptedException
	{
		FileProcessingPipeline pipeline = TestPipelineFactory.createDefaultPipeline();
		ParallelProcessingConfig config = ParallelProcessingConfig.builder().
			maxConcurrency(2).
			errorStrategy(ErrorStrategy.CONTINUE).
			build();

		List<Path> files = new ArrayList<>();
		for (int i = 0; i < 10; ++i)
		{
			files.add(TestFileFactory.createValidJavaFile());
		}

		try (BatchProcessor processor = new DefaultBatchProcessor(pipeline, config))
		{
			BatchResult result = processor.processFiles(files);

			assertEquals(result.totalFiles(), 10);
			assertEquals(result.successCount(), 10);
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
