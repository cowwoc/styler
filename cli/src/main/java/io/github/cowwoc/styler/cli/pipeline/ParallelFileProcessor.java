package io.github.cowwoc.styler.cli.pipeline;

import io.github.cowwoc.styler.cli.pipeline.progress.ConcurrentProgressObserver;
import io.github.cowwoc.styler.cli.pipeline.progress.ProgressObserver;
import io.github.cowwoc.styler.cli.security.MemoryMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;
import java.util.function.Supplier;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;

/**
 * Orchestrates parallel file processing using Java 21+ virtual threads.
 * <p>
 * This processor coordinates concurrent execution of {@link FileProcessorPipeline} instances,
 * one per file. Uses {@link ExecutorService} with virtual threads for automatic lifecycle
 * management and coordinated shutdown. Virtual threads (JEP 444) enable millions of concurrent
 * tasks without traditional thread pool tuning.
 * <p>
 * <b>Thread Safety:</b> This class is thread-safe and can be called concurrently.
 * Each file is processed in isolation on a separate virtual thread.
 * <p>
 * Usage:
 * <pre>{@code
 * try (var processor = ParallelFileProcessor.builder()
 *         .maxConcurrentFiles(100)
 *         .progressObserver(observer)
 *         .pipelineFactory(() -> createPipeline())
 *         .build()) {
 *     BatchResult result = processor.processFiles(files);
 *     if (result.hasErrors()) {
 *         System.err.println("Failed files: " + result.errors().size());
 *     }
 * }
 * }</pre>
 *
 * @param <OUTPUT> the output type produced by the pipeline
 * @see <a href="https://openjdk.org/jeps/444">JEP 444: Virtual Threads</a>
 */
@SuppressWarnings("PMD.TypeParameterNamingConventions")
public final class ParallelFileProcessor<OUTPUT> implements AutoCloseable
{
	@SuppressWarnings("PMD.FieldNamingConventions")
	private static final Logger logger = LoggerFactory.getLogger(ParallelFileProcessor.class);

	/** Default maximum concurrent files for builder (balances parallelism with resource usage). */
	private static final int DEFAULT_MAX_CONCURRENT_FILES = 100;

	/** Default memory limit in bytes (512MB - appropriate for typical code formatting workloads). */
	private static final long DEFAULT_MEMORY_LIMIT_BYTES = 512L * 1024 * 1024;

	/** Delay in milliseconds when memory pressure is detected (allows GC to reclaim memory). */
	private static final long MEMORY_PRESSURE_DELAY_MS = 100;

	private final int maxConcurrentFiles;
	private final ProgressObserver progressObserver;
	private final Supplier<FileProcessorPipeline<OUTPUT>> pipelineFactory;
	private final MemoryMonitor memoryMonitor;
	private final Semaphore concurrencyLimiter;
	private final Queue<PipelineException> errors;
	private volatile boolean closed;

	private ParallelFileProcessor(int maxConcurrentFiles, ProgressObserver progressObserver,
		Supplier<FileProcessorPipeline<OUTPUT>> pipelineFactory, MemoryMonitor memoryMonitor)
	{
		this.maxConcurrentFiles = maxConcurrentFiles;
		this.progressObserver = progressObserver;
		this.pipelineFactory = pipelineFactory;
		this.memoryMonitor = memoryMonitor;
		this.concurrencyLimiter = new Semaphore(maxConcurrentFiles);
		this.errors = new ConcurrentLinkedQueue<>();
	}

	/**
	 * Processes multiple files concurrently using virtual threads.
	 * <p>
	 * This method blocks until all files are processed. Each file is processed in an
	 * independent virtual thread, with errors aggregated for batch reporting. Unlike
	 * shutdown-on-failure pattern, this continues processing all files even if some fail.
	 * <p>
	 * <b>Thread Safety:</b> This method is thread-safe and can be called concurrently.
	 *
	 * @param files the list of files to process (never {@code null}, may be empty)
	 * @return aggregated processing results (never {@code null})
	 * @throws InterruptedException  if thread execution is interrupted during processing
	 * @throws NullPointerException  if {@code files} is {@code null}
	 * @throws IllegalStateException if this processor has been closed
	 */
	public BatchResult processFiles(List<Path> files) throws InterruptedException
	{
		requireThat(files, "files").isNotNull();
		if (closed)
		{
			throw new IllegalStateException("Processor has been closed");
		}

		if (files.isEmpty())
		{
			logger.info("No files to process");
			return BatchResult.empty();
		}

		logger.info("Starting parallel processing: {} files with max {} concurrent", files.size(),
			maxConcurrentFiles);

		// Wrap progress observer with thread-safe batch tracking
		ConcurrentProgressObserver batchObserver =
			new ConcurrentProgressObserver(progressObserver, files.size());

		// Virtual thread executor (stable API since Java 21)
		try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor())
		{
			List<Future<Void>> futures = new ArrayList<>(files.size());

			for (Path file : files)
			{
				// Adaptive throttling based on memory pressure
				if (memoryMonitor.isMemoryPressureHigh())
				{
					logger.warn("Memory pressure detected, pausing task submission");
					Thread.sleep(MEMORY_PRESSURE_DELAY_MS);
				}

				// Acquire concurrency permit
				concurrencyLimiter.acquire();

				// Submit file processing task to virtual thread executor
				// Errors are caught and aggregated, not propagated
				futures.add(executor.submit(() -> processFile(file, batchObserver)));
			}

			// Wait for all tasks to complete
			for (Future<Void> future : futures)
			{
				try
				{
					future.get(); // Block until this task completes
				}
				catch (Exception e)
				{
					// Errors already logged and aggregated in processFile
					logger.debug("Task completed with exception (already handled): {}", e.getMessage());
				}
			}
		}
		finally
		{
			// Report final statistics
			batchObserver.onPipelineClosed();
		}

		// Count successes and failures
		int successCount = files.size() - errors.size();
		int errorCount = errors.size();

		logger.info("Parallel processing complete: {} succeeded, {} failed", successCount, errorCount);
		return new BatchResult(successCount, errorCount, List.copyOf(errors));
	}

	/**
	 * Processes a single file through the pipeline.
	 * <p>
	 * This method is called from virtual threads and must be thread-safe.
	 *
	 * @param file          the file to process (never {@code null})
	 * @param batchObserver the batch progress observer (never {@code null})
	 * @return null (required by {@link java.util.concurrent.Callable})
	 */
	private Void processFile(Path file, ConcurrentProgressObserver batchObserver)
	{
		try
		{
			// Create pipeline instance for this thread (no shared state)
			// CRITICAL: Use try-with-resources to ensure pipeline cleanup
			try (FileProcessorPipeline<OUTPUT> pipeline = pipelineFactory.get())
			{
				// Process file through pipeline with batch observer
				ProcessingContext context = ProcessingContext.builder(file).build();
				PipelineResult<OUTPUT> result = pipeline.process(file, context);

				// Track errors and notify observer
				if (result.isSuccess())
				{
					batchObserver.onProcessingCompleted(file);
				}
				else
				{
					PipelineException exception = result.exception().orElseThrow();
					errors.add(exception);
					batchObserver.onProcessingFailed(file, exception.getStageName(), exception);
				}
			}
		}
		catch (Exception e)
		{
			PipelineException pipelineException = new PipelineException("Unexpected error processing file",
				file, "parallel-processor", e);
			errors.add(pipelineException);
			batchObserver.onProcessingFailed(file, "parallel-processor", pipelineException);
			logger.error("Unexpected error processing file: {}", file, e);
		}
		finally
		{
			// Release concurrency permit
			concurrencyLimiter.release();
		}
		return null;
	}

	@Override
	public void close()
	{
		if (closed)
		{
			return;
		}
		logger.debug("Closing parallel file processor");
		closed = true;
	}

	/**
	 * Returns a new builder for constructing a parallel file processor.
	 *
	 * @param <T> the output type
	 * @return a new builder instance (never {@code null})
	 */
	public static <T> Builder<T> builder()
	{
		return new Builder<>();
	}

	/**
	 * Builder for creating {@link ParallelFileProcessor} instances.
	 *
	 * @param <OUTPUT> the output type produced by the pipeline
	 */
	@SuppressWarnings("PMD.TypeParameterNamingConventions")
	public static final class Builder<OUTPUT>
	{
		private int maxConcurrentFiles = DEFAULT_MAX_CONCURRENT_FILES;
		private ProgressObserver progressObserver = ProgressObserver.noOp();
		private Supplier<FileProcessorPipeline<OUTPUT>> pipelineFactory;
		private MemoryMonitor memoryMonitor = new MemoryMonitor(DEFAULT_MEMORY_LIMIT_BYTES);

		private Builder()
		{
		}

		/**
		 * Sets the maximum concurrent files to process.
		 *
		 * @param max the maximum concurrent files (must be positive)
		 * @return this builder for method chaining (never {@code null})
		 * @throws IllegalArgumentException if {@code max} is less than 1
		 */
		public Builder<OUTPUT> maxConcurrentFiles(int max)
		{
			requireThat(max, "maxConcurrentFiles").isGreaterThanOrEqualTo(1);
			this.maxConcurrentFiles = max;
			return this;
		}

		/**
		 * Sets the progress observer for monitoring.
		 *
		 * @param observer the progress observer (never {@code null})
		 * @return this builder for method chaining (never {@code null})
		 * @throws NullPointerException if {@code observer} is {@code null}
		 */
		public Builder<OUTPUT> progressObserver(ProgressObserver observer)
		{
			requireThat(observer, "progressObserver").isNotNull();
			this.progressObserver = observer;
			return this;
		}

		/**
		 * Sets the pipeline factory for creating pipeline instances.
		 *
		 * @param factory the pipeline factory (never {@code null})
		 * @return this builder for method chaining (never {@code null})
		 * @throws NullPointerException if {@code factory} is {@code null}
		 */
		public Builder<OUTPUT> pipelineFactory(Supplier<FileProcessorPipeline<OUTPUT>> factory)
		{
			requireThat(factory, "pipelineFactory").isNotNull();
			this.pipelineFactory = factory;
			return this;
		}

		/**
		 * Sets the memory monitor for adaptive throttling.
		 *
		 * @param monitor the memory monitor (never {@code null})
		 * @return this builder for method chaining (never {@code null})
		 * @throws NullPointerException if {@code monitor} is {@code null})
		 */
		public Builder<OUTPUT> memoryMonitor(MemoryMonitor monitor)
		{
			requireThat(monitor, "memoryMonitor").isNotNull();
			this.memoryMonitor = monitor;
			return this;
		}

		/**
		 * Builds a new parallel file processor.
		 *
		 * @return a new processor instance (never {@code null})
		 * @throws IllegalStateException if pipeline factory has not been set
		 */
		public ParallelFileProcessor<OUTPUT> build()
		{
			if (pipelineFactory == null)
			{
				throw new IllegalStateException("Pipeline factory must be set");
			}
			return new ParallelFileProcessor<>(maxConcurrentFiles, progressObserver, pipelineFactory, memoryMonitor);
		}
	}
}
