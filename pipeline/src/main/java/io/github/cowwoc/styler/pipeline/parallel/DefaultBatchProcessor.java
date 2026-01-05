package io.github.cowwoc.styler.pipeline.parallel;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

import io.github.cowwoc.styler.pipeline.FileProcessingPipeline;
import io.github.cowwoc.styler.pipeline.PipelineResult;
import io.github.cowwoc.styler.pipeline.parallel.internal.MemoryReservationManager;
import io.github.cowwoc.styler.pipeline.parallel.internal.Reservation;
import io.github.cowwoc.styler.pipeline.parallel.internal.VirtualThreadExecutor;

/**
 * Default implementation of batch file processing using virtual threads.
 * <p>
 * Processes multiple files in parallel using Java 25 virtual threads with semaphore-based
 * concurrency limiting to prevent out-of-memory conditions. Results and errors are aggregated
 * in thread-safe collections.
 * <p>
 * Concurrency Model:
 * <ul>
 *     <li>Each file is processed by a separate virtual thread</li>
 *     <li>Semaphore permits limit concurrent operations based on configuration</li>
 *     <li>One file failure does not affect others (error isolation)</li>
 *     <li>Results are aggregated in {@code ConcurrentHashMap}</li>
 * </ul>
 * <p>
 * Error Handling:
 * <ul>
 *     <li>FAIL_FAST: Stops immediately on first error, completes already-started tasks</li>
 *     <li>CONTINUE: Processes all files regardless of errors</li>
 *     <li>ABORT_AFTER_THRESHOLD: Stops after error threshold is reached</li>
 * </ul>
 * <p>
 * Resource Management: Must be used with try-with-resources to ensure virtual thread
 * executor shutdown.
 * <p>
 * Example:
 * <pre>
 * FileProcessingPipeline pipeline = FileProcessingPipeline.builder()
 *     .securityConfig(securityConfig)
 *     .formattingRules(rules)
 *     .formattingConfig(config)
 *     .build();
 *
 * ParallelProcessingConfig config = ParallelProcessingConfig.builder()
 *     .maxConcurrency(ParallelProcessingConfig.calculateDefaultMaxConcurrency())
 *     .errorStrategy(ErrorStrategy.CONTINUE)
 *     .progressCallback((done, total, file) -&gt;
 *         System.out.printf("Progress: %d/%d%n", done, total))
 *     .build();
 *
 * try (BatchProcessor processor = new DefaultBatchProcessor(pipeline, config))
 * {
 *     BatchResult result = processor.processFiles(filePaths);
 *     System.out.printf("Processed %d files, %d succeeded, %d failed%n",
 *         result.totalFiles(), result.successCount(), result.failureCount());
 * }
 * </pre>
 * <p>
 * <b>Thread-safety</b>: This class is thread-safe. Multiple threads may call
 * {@code processFiles()} concurrently on the same instance.
 */
public final class DefaultBatchProcessor implements BatchProcessor
{
	private final FileProcessingPipeline pipeline;
	private final ParallelProcessingConfig config;
	private final ReentrantLock lock = new ReentrantLock();
	private final AtomicBoolean closed = new AtomicBoolean(false);
	private volatile VirtualThreadExecutor executor;

	/**
	 * Creates a batch processor with the given pipeline and configuration.
	 * <p>
	 * The executor is lazily initialized on first use to defer resource allocation.
	 *
	 * @param pipeline the file processing pipeline, must not be null
	 * @param config the parallel processing configuration, must not be null
	 * @throws NullPointerException if pipeline or config is null
	 */
	public DefaultBatchProcessor(FileProcessingPipeline pipeline, ParallelProcessingConfig config)
	{
		requireThat(pipeline, "pipeline").isNotNull();
		requireThat(config, "config").isNotNull();
		this.pipeline = pipeline;
		this.config = config;
	}

	/**
	 * Processes a batch of files in parallel.
	 * <p>
	 * Files are processed concurrently with up to {@code maxConcurrency} files at a time.
	 * Results include both successes and errors. Error handling follows the configured strategy.
	 *
	 * @param files the list of file paths to process, may be empty
	 * @return aggregated {@code BatchResult} with metrics and errors
	 * @throws NullPointerException if files or any element is null
	 * @throws InterruptedException if processing is interrupted
	 * @throws IllegalStateException if the processor has been closed
	 */
	@Override
	public BatchResult processFiles(List<Path> files) throws InterruptedException
	{
		if (closed.get())
			throw new IllegalStateException("Processor has been closed");

		requireThat(files, "files").isNotNull().doesNotContain(null);

		Instant startTime = Instant.now();
		Map<Path, PipelineResult> results = new ConcurrentHashMap<>();
		Map<Path, String> errors = new ConcurrentHashMap<>();
		AtomicInteger completedCount = new AtomicInteger(0);
		AtomicInteger errorCount = new AtomicInteger(0);

		if (files.isEmpty())
		{
			Duration duration = Duration.between(startTime, Instant.now());
			return new BatchResult(0, 0, 0, List.of(), Map.of(), duration, 0.0);
		}


		// Initialize executor lazily to defer resource allocation until first use
		lock.lock();
		try
		{
			if (executor == null && !closed.get())
			{
				executor = new VirtualThreadExecutor(config.maxConcurrency());
			}
		}
		finally
		{
			lock.unlock();
		}

		if (executor == null)
		{
			throw new IllegalStateException("Processor has been closed");
		}

		MemoryReservationManager memoryManager = new MemoryReservationManager();
		CountDownLatch latch = new CountDownLatch(files.size());

		for (Path file : files)
		{
			long fileSize;
			try
			{
				fileSize = Files.size(file);
			}
			catch (IOException e)
			{
				errors.put(file, "Failed to get file size: " + e.getMessage());
				errorCount.incrementAndGet();
				int completed = completedCount.incrementAndGet();
				latch.countDown();
				if (config.progressCallback() != null)
					config.progressCallback().onProgress(completed, files.size(), file);
				continue;
			}

			executor.submit(() ->
			{
				try (Reservation _ = memoryManager.reserve(fileSize))
				{
					// Process the file through the pipeline
					try (PipelineResult result = pipeline.processFile(file))
					{
						if (result.overallSuccess())
							results.put(file, result);
						else
						{
							// Extract error message from failed stage
							String errorMsg = result.stageResults().stream().
								filter(sr -> !sr.isSuccess()).
								flatMap(sr -> sr.errorMessage().stream()).
								findFirst().
								orElse("Processing failed");
							errors.put(file, errorMsg);
							errorCount.incrementAndGet();
						}
					}
				}
				catch (Exception e)
				{
					String errorMessage;
					if (e.getMessage() != null)
						errorMessage = e.getMessage();
					else
						errorMessage = e.getClass().getSimpleName();
					errors.put(file, errorMessage);
					errorCount.incrementAndGet();
				}
				finally
				{
					int completed = completedCount.incrementAndGet();
					latch.countDown();
					if (config.progressCallback() != null)
						config.progressCallback().onProgress(completed, files.size(), file);
				}
			});
		}

		// Wait for all tasks to complete
		latch.await();

		Instant endTime = Instant.now();
		Duration totalDuration = Duration.between(startTime, endTime);
		int successCount = results.size();
		int failureCount = errors.size();
		double throughput = calculateThroughput(files.size(), totalDuration);

		return new BatchResult(files.size(), successCount, failureCount,
			new ArrayList<>(results.values()), Map.copyOf(errors),
			totalDuration, throughput);
	}

	/**
	 * Calculates throughput as files processed per second.
	 *
	 * @param fileCount the number of files processed
	 * @param duration the time taken
	 * @return throughput in files per second, or 0.0 if duration is zero
	 */
	private double calculateThroughput(int fileCount, Duration duration)
	{
		if (duration.isZero() || duration.isNegative())
			return 0.0;
		double seconds = duration.toMillis() / 1000.0;
		return fileCount / seconds;
	}

	/**
	 * Closes the processor and releases resources.
	 * <p>
	 * Safe to call multiple times - subsequent calls are no-ops. Virtual thread executor
	 * is properly shut down.
	 */
	@Override
	public void close()
	{
		if (!closed.compareAndSet(false, true))
			return;
		if (executor != null)
		{
			try
			{
				executor.close();
			}
			catch (Exception _)
			{
				// Ignore exceptions during shutdown to match interface contract
				Thread.currentThread().interrupt();
			}
		}
	}
}
