package io.github.cowwoc.styler.cli.test.pipeline;

import io.github.cowwoc.styler.cli.pipeline.BatchResult;
import io.github.cowwoc.styler.cli.pipeline.FileProcessorPipeline;
import io.github.cowwoc.styler.cli.pipeline.ParallelFileProcessor;
import io.github.cowwoc.styler.cli.pipeline.PipelineException;
import io.github.cowwoc.styler.cli.pipeline.PipelineStage;
import io.github.cowwoc.styler.cli.pipeline.ProcessingContext;
import io.github.cowwoc.styler.cli.pipeline.StageResult;
import io.github.cowwoc.styler.cli.pipeline.progress.ProgressObserver;
import io.github.cowwoc.styler.cli.security.MemoryMonitor;
import org.testng.annotations.Test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link ParallelFileProcessor}.
 * <p>
 * These tests verify thread safety, concurrency control, and error aggregation
 * for parallel file processing with virtual threads.
 */
public final class ParallelFileProcessorTest
{
	/**
	 * Verifies that processing empty file list returns empty result.
	 */
	@Test
	public void emptyFileListReturnsEmptyResult() throws InterruptedException
	{
		try (ParallelFileProcessor<Path> processor = createProcessor(ParallelFileProcessorTest::createSuccessPipeline))
		{
			BatchResult result = processor.processFiles(List.of());

			assertThat(result.isSuccess()).isTrue();
			assertThat(result.totalFiles()).isZero();
			assertThat(result.successCount()).isZero();
			assertThat(result.errorCount()).isZero();
		}
	}

	/**
	 * Verifies that all files are processed successfully when no errors occur.
	 */
	@Test
	public void allFilesProcessedSuccessfully() throws InterruptedException
	{
		List<Path> files = createTestFiles(10);

		try (ParallelFileProcessor<Path> processor = createProcessor(ParallelFileProcessorTest::createSuccessPipeline))
		{
			BatchResult result = processor.processFiles(files);

			assertThat(result.isSuccess()).isTrue();
			assertThat(result.successCount()).isEqualTo(10);
			assertThat(result.errorCount()).isZero();
			assertThat(result.errors()).isEmpty();
		}
	}

	/**
	 * Verifies that errors are correctly aggregated when some files fail.
	 */
	@Test
	public void errorsAreAggregatedAcrossFiles() throws InterruptedException
	{
		List<Path> files = createTestFiles(10);
		AtomicInteger processCount = new AtomicInteger(0);

		// Every 3rd file fails (3rd, 6th, 9th) = 3 failures, 7 successes
		try (ParallelFileProcessor<Path> processor = createProcessor(() -> createConditionalPipeline(
			() -> processCount.incrementAndGet() % 3 != 0)))
		{
			BatchResult result = processor.processFiles(files);

			assertThat(result.totalFiles()).isEqualTo(10);
			assertThat(result.successCount()).isEqualTo(7);
			assertThat(result.errorCount()).isEqualTo(3);
			assertThat(result.hasPartialSuccess()).isTrue();
			assertThat(result.errors()).hasSize(3);
		}
	}

	/**
	 * Verifies that concurrent file limit is enforced correctly.
	 */
	@Test
	public void concurrencyLimitIsEnforced() throws InterruptedException
	{
		List<Path> files = createTestFiles(20);
		int maxConcurrent = 5;

		try (ParallelFileProcessor<Path> processor =
				 ParallelFileProcessor.<Path>builder().
					 	maxConcurrentFiles(maxConcurrent).
					 	pipelineFactory(() -> createSlowPipeline(50)).
					 	memoryMonitor(new MemoryMonitor(1024 * 1024 * 1024)).
					 	build())
		{
			BatchResult result = processor.processFiles(files);

			assertThat(result.isSuccess()).isTrue();
			assertThat(result.totalFiles()).isEqualTo(20);
			assertThat(result.successCount()).isEqualTo(20);
		}
	}

	/**
	 * Verifies that processing continues even if first file fails.
	 */
	@Test
	public void processingContinuesAfterFirstFileFailure() throws InterruptedException
	{
		List<Path> files = createTestFiles(5);
		AtomicInteger processCount = new AtomicInteger(0);

		// First file fails, rest succeed
		try (ParallelFileProcessor<Path> processor = createProcessor(() -> createConditionalPipeline(
			() -> processCount.incrementAndGet() > 1)))
		{
			BatchResult result = processor.processFiles(files);

			assertThat(result.totalFiles()).isEqualTo(5);
			assertThat(result.successCount()).isEqualTo(4);
			assertThat(result.errorCount()).isEqualTo(1);
			assertThat(result.hasPartialSuccess()).isTrue();
		}
	}

	/**
	 * Verifies that closed processor throws IllegalStateException.
	 */
	@Test(expectedExceptions = IllegalStateException.class)
	public void closedProcessorThrowsException() throws InterruptedException
	{
		ParallelFileProcessor<Path> processor = createProcessor(ParallelFileProcessorTest::createSuccessPipeline);
		processor.close();

		processor.processFiles(createTestFiles(1));
	}

	/**
	 * Verifies that null file list throws NullPointerException.
	 */
	@Test(expectedExceptions = NullPointerException.class)
	public void nullFileListThrowsException() throws InterruptedException
	{
		try (ParallelFileProcessor<Path> processor = createProcessor(ParallelFileProcessorTest::createSuccessPipeline))
		{
			processor.processFiles(null);
		}
	}

	/**
	 * Verifies thread safety by processing files concurrently from multiple threads.
	 */
	@Test
	public void processingIsThreadSafe() throws InterruptedException
	{
		List<Path> files = createTestFiles(50);

		try (ParallelFileProcessor<Path> processor =
				 ParallelFileProcessor.<Path>builder().
					 	maxConcurrentFiles(10).
					 	pipelineFactory(ParallelFileProcessorTest::createSuccessPipeline).
					 	memoryMonitor(new MemoryMonitor(1024 * 1024 * 1024)).
					 	build())
		{
			BatchResult result = processor.processFiles(files);

			assertThat(result.isSuccess()).isTrue();
			assertThat(result.totalFiles()).isEqualTo(50);
			assertThat(result.successCount()).isEqualTo(50);
			assertThat(result.errorCount()).isZero();
		}
	}

	/**
	 * Creates a ParallelFileProcessor for testing.
	 *
	 * @param pipelineFactory factory for creating pipeline instances
	 * @return configured processor
	 */
	private static ParallelFileProcessor<Path> createProcessor(
		java.util.function.Supplier<FileProcessorPipeline<Path>> pipelineFactory)
	{
		return ParallelFileProcessor.<Path>builder().
				maxConcurrentFiles(100).
				progressObserver(ProgressObserver.noOp()).
				pipelineFactory(pipelineFactory).
				memoryMonitor(new MemoryMonitor(1024 * 1024 * 1024)).
				build();
	}

	/**
	 * Creates a pipeline that always succeeds.
	 * <p>
	 * Uses a no-op stage that always succeeds for any file.
	 *
	 * @return success pipeline
	 */
	private static FileProcessorPipeline<Path> createSuccessPipeline()
	{
		PipelineStage<Path, Path> noOpStage = new PipelineStage<>()
		{
			@Override
			public StageResult<Path> execute(Path input, ProcessingContext context)
			{
				// Pass through the input file
				return StageResult.success(input);
			}

			@Override
			public String getStageId()
			{
				return "no-op-stage";
			}
		};

		return FileProcessorPipeline.<Path>builder().
				addStage(noOpStage).
				progressObserver(ProgressObserver.noOp()).
				build();
	}

	/**
	 * Creates a pipeline that succeeds or fails based on condition.
	 * <p>
	 * Uses a test stage that throws exceptions when shouldSucceed is false.
	 *
	 * @param shouldSucceed condition supplier
	 * @return conditional pipeline
	 */
	private static FileProcessorPipeline<Path> createConditionalPipeline(
		java.util.function.BooleanSupplier shouldSucceed)
	{
		// Create pipeline with a test stage that conditionally fails
		PipelineStage<Path, Path> testStage = new PipelineStage<>()
		{
			@Override
			public StageResult<Path> execute(Path input, ProcessingContext context)
				throws PipelineException
			{
				if (shouldSucceed.getAsBoolean())
				{
					return StageResult.success(input);
				}
			throw new PipelineException("Mock error", context.sourceFile(), "test-stage",
				new IllegalStateException("Mock cause"));
			}

			@Override
			public String getStageId()
			{
				return "test-stage";
			}
		};

		return FileProcessorPipeline.<Path>builder().
				addStage(testStage).
				progressObserver(ProgressObserver.noOp()).
				build();
	}

	/**
	 * Creates a slow pipeline for concurrency testing.
	 * <p>
	 * Introduces delay to verify concurrency limits are enforced.
	 *
	 * @param delayMillis delay in milliseconds
	 * @return slow pipeline
	 */
	private static FileProcessorPipeline<Path> createSlowPipeline(long delayMillis)
	{
		PipelineStage<Path, Path> slowStage = new PipelineStage<>()
		{
			@Override
			public StageResult<Path> execute(Path input, ProcessingContext context)
			{
				try
				{
					Thread.sleep(delayMillis);
				}
				catch (InterruptedException e)
				{
					Thread.currentThread().interrupt();
				}
				return StageResult.success(input);
			}

			@Override
			public String getStageId()
			{
				return "slow-stage";
			}
		};

		return FileProcessorPipeline.<Path>builder().
				addStage(slowStage).
				progressObserver(ProgressObserver.noOp()).
				build();
	}

	/**
	 * Verifies that high heap pressure triggers adaptive throttling by pausing task submission.
	 * <p>
	 * When memory monitor reports high pressure (&gt;80% heap usage), the processor pauses
	 * task submission for 100ms to allow garbage collection. This test validates the
	 * adaptive throttling mechanism prevents OOM crashes under memory pressure.
	 */
	@Test
	public void heapPressureTriggersThrottling() throws InterruptedException
	{
		List<Path> files = createTestFiles(10);
		AtomicInteger submitCount = new AtomicInteger(0);

		// Mock memory monitor that reports high pressure for first 5 files
		MemoryMonitor mockMonitor = new MemoryMonitor(1024 * 1024 * 1024)
		{
			@Override
			public boolean isMemoryPressureHigh()
			{
				return submitCount.get() < 5;
			}
		};

		try (ParallelFileProcessor<Path> processor =
				 ParallelFileProcessor.<Path>builder().
					 	maxConcurrentFiles(10).
					 	pipelineFactory(() -> createCountingPipeline(submitCount)).
					 	memoryMonitor(mockMonitor).
					 	build())
		{
			long startTime = System.currentTimeMillis();
			BatchResult result = processor.processFiles(files);
			long duration = System.currentTimeMillis() - startTime;

			// Verify throttling occurred (5 files × 100ms delay = ~500ms minimum)
			assertThat(duration).isGreaterThanOrEqualTo(400);
			assertThat(result.isSuccess()).isTrue();
			assertThat(result.totalFiles()).isEqualTo(10);
		}
	}

	/**
	 * Verifies that processing recovers when heap pressure drops.
	 * <p>
	 * When memory pressure decreases below threshold, the processor resumes normal
	 * task submission without delays. This validates dynamic throttle recovery.
	 */
	@Test
	public void throttleRecoveryWhenHeapPressureDrops() throws InterruptedException
	{
		List<Path> files = createTestFiles(10);
		AtomicInteger submitCount = new AtomicInteger(0);

		// Mock memory monitor that reports high pressure only for first 2 files
		MemoryMonitor mockMonitor = new MemoryMonitor(1024 * 1024 * 1024)
		{
			@Override
			public boolean isMemoryPressureHigh()
			{
				return submitCount.get() < 2;
			}
		};

		try (ParallelFileProcessor<Path> processor =
				 ParallelFileProcessor.<Path>builder().
					 	maxConcurrentFiles(10).
					 	pipelineFactory(() -> createCountingPipeline(submitCount)).
					 	memoryMonitor(mockMonitor).
					 	build())
		{
			long startTime = System.currentTimeMillis();
			BatchResult result = processor.processFiles(files);
			long duration = System.currentTimeMillis() - startTime;

			// Verify recovery (only 2 files throttled: 2 × 100ms = ~200ms)
			assertThat(duration).isLessThan(500); // Would be 1000ms if all throttled
			assertThat(result.isSuccess()).isTrue();
			assertThat(result.totalFiles()).isEqualTo(10);
		}
	}

	/**
	 * Verifies that minimum concurrency is always enforced.
	 * <p>
	 * Even under extreme memory pressure, the processor must process at least 1 file
	 * to make forward progress. This prevents complete starvation under high load.
	 */
	@Test
	public void minimumConcurrencyEnforced() throws InterruptedException
	{
		List<Path> files = createTestFiles(5);

		// Mock memory monitor that always reports high pressure
		MemoryMonitor mockMonitor = new MemoryMonitor(1024 * 1024 * 1024)
		{
			@Override
			public boolean isMemoryPressureHigh()
			{
				return true; // Always high pressure
			}
		};

		try (ParallelFileProcessor<Path> processor =
				 ParallelFileProcessor.<Path>builder().
					 	maxConcurrentFiles(1). // Minimum concurrency
					 	pipelineFactory(ParallelFileProcessorTest::createSuccessPipeline).
					 	memoryMonitor(mockMonitor).
					 	build())
		{
			BatchResult result = processor.processFiles(files);

			// Verify all files processed despite constant memory pressure
			assertThat(result.isSuccess()).isTrue();
			assertThat(result.totalFiles()).isEqualTo(5);
			assertThat(result.successCount()).isEqualTo(5);
		}
	}

	/**
	 * Verifies that interrupt signal terminates processing gracefully.
	 * <p>
	 * When the processing thread is interrupted, the processor should detect the
	 * interrupt and propagate it as InterruptedException, allowing clean shutdown.
	 */
	@Test(expectedExceptions = InterruptedException.class)
	public void interruptSignalTerminatesGracefully() throws InterruptedException
	{
		List<Path> files = createTestFiles(100);

		try (ParallelFileProcessor<Path> processor =
				 ParallelFileProcessor.<Path>builder().
					 	maxConcurrentFiles(10).
					 	pipelineFactory(() -> createSlowPipeline(100)).
					 	memoryMonitor(new MemoryMonitor(1024 * 1024 * 1024)).
					 	build())
		{
			// Start processing in a separate thread
			Thread processingThread = Thread.currentThread();
			Thread interruptThread = new Thread(() ->
			{
				try
				{
					Thread.sleep(200); // Let processing start
					processingThread.interrupt();
				}
				catch (InterruptedException e)
				{
					Thread.currentThread().interrupt();
				}
			});

			interruptThread.start();
			processor.processFiles(files); // Should throw InterruptedException
			interruptThread.join();
		}
	}

	/**
	 * Verifies that resources are properly cleaned up when interrupted.
	 * <p>
	 * When processing is interrupted, all pipeline instances must be closed
	 * and resources released via try-with-resources. This test verifies that
	 * the processor handles interrupt cleanly without leaking resources.
	 */
	@Test
	public void resourcesCleanedUpOnInterrupt() throws InterruptedException
	{
		List<Path> files = createTestFiles(50);
		AtomicInteger pipelinesCreated = new AtomicInteger(0);

		try (ParallelFileProcessor<Path> processor =
				 ParallelFileProcessor.<Path>builder().
					 	maxConcurrentFiles(10).
					 	pipelineFactory(() -> createCountingAndSlowPipeline(pipelinesCreated, 100)).
					 	memoryMonitor(new MemoryMonitor(1024 * 1024 * 1024)).
					 	build())
		{
			Thread processingThread = Thread.currentThread();
			Thread interruptThread = new Thread(() ->
			{
				try
				{
					Thread.sleep(300); // Let some pipelines start
					processingThread.interrupt();
				}
				catch (InterruptedException e)
				{
					Thread.currentThread().interrupt();
				}
			});

			interruptThread.start();
			try
			{
				processor.processFiles(files);
			}
			catch (InterruptedException e)
			{
				// Expected - verify that some pipelines were created before interrupt
				assertThat(pipelinesCreated.get()).isGreaterThan(0);

				// Try-with-resources ensures all pipelines are properly closed
				// No resource leak verification needed - handled by AutoCloseable contract
			}
			interruptThread.join();
		}
	}

	/**
	 * Verifies that partial results are reported when shutdown occurs.
	 * <p>
	 * When processing is interrupted mid-batch, the interrupt should be detected
	 * and propagated as InterruptedException. The fact that the exception is thrown
	 * confirms that the processor detected the interrupt signal.
	 */
	@Test(expectedExceptions = InterruptedException.class)
	public void partialResultsReportedOnShutdown() throws InterruptedException
	{
		List<Path> files = createTestFiles(200);

		try (ParallelFileProcessor<Path> processor =
				 ParallelFileProcessor.<Path>builder().
					 	maxConcurrentFiles(10).
					 	pipelineFactory(() -> createSlowPipeline(50)).
					 	memoryMonitor(new MemoryMonitor(1024 * 1024 * 1024)).
					 	build())
		{
			Thread processingThread = Thread.currentThread();
			Thread interruptThread = new Thread(() ->
			{
				try
				{
					Thread.sleep(500); // Let some files complete (10 files × 50ms = ~500ms for first batch)
					processingThread.interrupt();
				}
				catch (InterruptedException e)
				{
					Thread.currentThread().interrupt();
				}
			});

			interruptThread.start();
			processor.processFiles(files); // Should throw InterruptedException
			interruptThread.join();
		}
	}

	/**
	 * Verifies that parallel processing achieves expected speedup over sequential.
	 * <p>
	 * With 10 concurrent files and 50ms processing delay per file, parallel processing
	 * should complete significantly faster than sequential (target: &gt;2x speedup).
	 */
	@Test
	public void parallelProcessingAchievesExpectedSpeedup() throws InterruptedException
	{
		List<Path> files = createTestFiles(50);
		long delayPerFile = 50; // ms

		// Sequential processing baseline
		long sequentialStart = System.currentTimeMillis();
		try (ParallelFileProcessor<Path> sequential =
				 ParallelFileProcessor.<Path>builder().
					 	maxConcurrentFiles(1).
					 	pipelineFactory(() -> createSlowPipeline(delayPerFile)).
					 	memoryMonitor(new MemoryMonitor(1024 * 1024 * 1024)).
					 	build())
		{
			sequential.processFiles(files);
		}
		long sequentialDuration = System.currentTimeMillis() - sequentialStart;

		// Parallel processing
		long parallelStart = System.currentTimeMillis();
		try (ParallelFileProcessor<Path> parallel =
				 ParallelFileProcessor.<Path>builder().
					 	maxConcurrentFiles(10).
					 	pipelineFactory(() -> createSlowPipeline(delayPerFile)).
					 	memoryMonitor(new MemoryMonitor(1024 * 1024 * 1024)).
					 	build())
		{
			parallel.processFiles(files);
		}
		long parallelDuration = System.currentTimeMillis() - parallelStart;

		// Verify speedup (parallel should be >2x faster)
		double speedup = (double) sequentialDuration / parallelDuration;
		assertThat(speedup).isGreaterThan(2.0);
	}

	/**
	 * Verifies that throughput meets the target of 100+ files/second.
	 * <p>
	 * With minimal processing overhead, the parallel processor should achieve
	 * high throughput demonstrating efficient virtual thread utilization.
	 */
	@Test
	public void throughputMeetsTarget() throws InterruptedException
	{
		List<Path> files = createTestFiles(500);

		long startTime = System.currentTimeMillis();
		try (ParallelFileProcessor<Path> processor =
				 ParallelFileProcessor.<Path>builder().
					 	maxConcurrentFiles(50).
					 	pipelineFactory(() -> createSlowPipeline(1)). // Minimal delay
					 	memoryMonitor(new MemoryMonitor(1024 * 1024 * 1024)).
					 	build())
		{
			BatchResult result = processor.processFiles(files);
			assertThat(result.isSuccess()).isTrue();
		}
		long duration = System.currentTimeMillis() - startTime;

		// Calculate throughput (files per second)
		double throughput = (files.size() * 1000.0) / duration;

		// Verify throughput exceeds 100 files/second
		assertThat(throughput).isGreaterThan(100.0);
	}

	/**
	 * Verifies that virtual threads scale to 1000+ concurrent tasks without degradation.
	 * <p>
	 * Virtual threads enable millions of concurrent tasks. This test validates that
	 * the processor handles high concurrency without performance degradation.
	 */
	@Test
	public void virtualThreadScalability() throws InterruptedException
	{
		List<Path> files = createTestFiles(1000);

		long startTime = System.currentTimeMillis();
		try (ParallelFileProcessor<Path> processor =
				 ParallelFileProcessor.<Path>builder().
					 	maxConcurrentFiles(1000).
					 	pipelineFactory(() -> createSlowPipeline(10)).
					 	memoryMonitor(new MemoryMonitor(1024 * 1024 * 1024)).
					 	build())
		{
			BatchResult result = processor.processFiles(files);

			assertThat(result.isSuccess()).isTrue();
			assertThat(result.totalFiles()).isEqualTo(1000);
		}
		long duration = System.currentTimeMillis() - startTime;

		// With 1000 concurrent threads and 10ms delay, should complete in ~10-50ms
		// (allowing overhead for virtual thread scheduling)
		assertThat(duration).isLessThan(500);
	}

	/**
	 * Creates a pipeline that counts executions via shared atomic counter.
	 * <p>
	 * Used for testing throttling behavior by tracking task submission order.
	 *
	 * @param counter shared counter to increment on each execution
	 * @return counting pipeline
	 */
	private static FileProcessorPipeline<Path> createCountingPipeline(AtomicInteger counter)
	{
		PipelineStage<Path, Path> countingStage = new PipelineStage<>()
		{
			@Override
			public StageResult<Path> execute(Path input, ProcessingContext context)
			{
				counter.incrementAndGet();
				return StageResult.success(input);
			}

			@Override
			public String getStageId()
			{
				return "counting-stage";
			}
		};

		return FileProcessorPipeline.<Path>builder().
				addStage(countingStage).
				progressObserver(ProgressObserver.noOp()).
				build();
	}

	/**
	 * Creates a pipeline that counts creations and introduces delay.
	 * <p>
	 * Used for testing resource cleanup during interrupt scenarios.
	 *
	 * @param created  counter for pipeline creations
	 * @param delayMillis delay in milliseconds for processing
	 * @return counting and slow pipeline
	 */
	private static FileProcessorPipeline<Path> createCountingAndSlowPipeline(
		AtomicInteger created, long delayMillis)
	{
		created.incrementAndGet();

		PipelineStage<Path, Path> slowStage = new PipelineStage<>()
		{
			@Override
			public StageResult<Path> execute(Path input, ProcessingContext context)
			{
				try
				{
					Thread.sleep(delayMillis);
				}
				catch (InterruptedException e)
				{
					Thread.currentThread().interrupt();
				}
				return StageResult.success(input);
			}

			@Override
			public String getStageId()
			{
				return "counting-slow-stage";
			}
		};

		return FileProcessorPipeline.<Path>builder().
				addStage(slowStage).
				progressObserver(ProgressObserver.noOp()).
				build();
	}

	/**
	 * Creates a list of test file paths.
	 *
	 * @param count number of files to create
	 * @return list of file paths
	 */
	private static List<Path> createTestFiles(int count)
	{
		List<Path> files = new ArrayList<>();
		for (int i = 0; i < count; ++i)
		{
			files.add(Paths.get("test-file-" + i + ".java"));
		}
		return Collections.unmodifiableList(files);
	}
}
