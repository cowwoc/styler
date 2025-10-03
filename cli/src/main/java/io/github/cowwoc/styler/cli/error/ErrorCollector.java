package io.github.cowwoc.styler.cli.error;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/**
 * Batch error collection and processing for efficient error handling across multiple files.
 * <p>
 * ErrorCollector provides optimized batch processing for scenarios involving multiple
 * files with potentially many errors. It supports both synchronous and asynchronous
 * error collection with configurable batch sizes and processing strategies.
 */
@SuppressWarnings("PMD.SystemPrintln") // CLI utility: System.out/err required for user output
public final class ErrorCollector implements AutoCloseable
{
	private final Queue<ErrorContext> pendingErrors;
	private final List<ErrorContext> allCollectedErrors;
	private final AtomicInteger errorCount;
	private final int batchSize;
	private final ExecutorService processingExecutor;
	private final Consumer<List<ErrorContext>> batchProcessor;
	private final boolean synchronousProcessing;
	private volatile boolean closed;

	/**
	 * Creates an error collector with the specified batch processing configuration.
	 *
	 * @param batchSize the number of errors to collect before triggering batch processing
	 * @param batchProcessor the function to process error batches, never {@code null}
	 * @param synchronousProcessing whether to process batches synchronously ({@code true}) or
	 *        asynchronously ({@code false})
	 * @throws IllegalArgumentException if {@code batchSize} is not positive
	 * @throws NullPointerException if {@code batchProcessor} is {@code null}
	 */
	private ErrorCollector(int batchSize, Consumer<List<ErrorContext>> batchProcessor, boolean synchronousProcessing)
	{
		if (batchSize <= 0)
		{
			throw new IllegalArgumentException("Batch size must be positive: " + batchSize);
		}
		if (batchProcessor == null)
		{
			throw new NullPointerException("Batch processor cannot be null");
		}

		this.pendingErrors = new ConcurrentLinkedQueue<>();
		this.allCollectedErrors = new CopyOnWriteArrayList<>();
		this.errorCount = new AtomicInteger(0);
		this.batchSize = batchSize;
		this.batchProcessor = batchProcessor;
		this.synchronousProcessing = synchronousProcessing;
		if (synchronousProcessing)
		{
			this.processingExecutor = null;
		}
		else
		{
			this.processingExecutor = Executors.newSingleThreadExecutor(r ->
			{
				Thread t = new Thread(r, "ErrorCollector-Processor");
				t.setDaemon(true);
				return t;
			});
		}
		this.closed = false;
	}

	/**
	 * Creates an error collector with the specified batch processing configuration (asynchronous).
	 *
	 * @param batchSize the number of errors to collect before triggering batch processing
	 * @param batchProcessor the function to process error batches, never {@code null}
	 * @throws IllegalArgumentException if {@code batchSize} is not positive or {@code batchProcessor} is {@code null}
	 */
	public ErrorCollector(int batchSize, Consumer<List<ErrorContext>> batchProcessor)
	{
		this(batchSize, batchProcessor, false);
	}

	/**
	 * Creates an error collector with default batch size of 50.
	 *
	 * @param batchProcessor the function to process error batches, never {@code null}
	 * @throws IllegalArgumentException if {@code batchProcessor} is {@code null}
	 */
	public ErrorCollector(Consumer<List<ErrorContext>> batchProcessor)
	{
		this(50, batchProcessor);
	}

	/**
	 * Adds an error to the collection for batch processing.
	 *
	 * @param error the error to add, never {@code null}
	 * @throws NullPointerException if {@code error} is {@code null}
	 * @throws IllegalStateException if the collector has been closed
	 */
	public void addError(ErrorContext error)
	{
		if (error == null)
		{
			throw new NullPointerException("Error cannot be null");
		}
		if (closed)
		{
			throw new IllegalStateException("ErrorCollector has been closed");
		}

		pendingErrors.offer(error);
		allCollectedErrors.add(error);
		int currentCount = errorCount.incrementAndGet();

		// Trigger batch processing if batch size reached
		if (currentCount % batchSize == 0)
		{
			if (synchronousProcessing)
			{
				processBatchSync();
			}
			else
			{
				processBatch();
			}
		}
	}

	/**
	 * Adds multiple errors to the collection efficiently.
	 *
	 * @param errors the errors to add, never {@code null}
	 * @throws NullPointerException if {@code errors} is {@code null} or contains {@code null} elements
	 * @throws IllegalStateException if the collector has been closed
	 */
	public void addErrors(List<ErrorContext> errors)
	{
		if (errors == null)
		{
			throw new NullPointerException("Errors list cannot be null");
		}
		if (closed)
		{
			throw new IllegalStateException("ErrorCollector has been closed");
		}

		for (ErrorContext error : errors)
		{
			if (error == null)
			{
				throw new NullPointerException("Error list cannot contain null elements");
			}
			pendingErrors.offer(error);
			allCollectedErrors.add(error);
		}

		int newCount = errorCount.addAndGet(errors.size());

		// Process multiple batches if necessary
		int batchesToProcess = newCount / batchSize - (newCount - errors.size()) / batchSize;
		for (int i = 0; i < batchesToProcess; ++i)
		{
			if (synchronousProcessing)
			{
				processBatchSync();
			}
			else
			{
				processBatch();
			}
		}
	}

	/**
	 * Processes any remaining errors that haven't reached the batch size threshold.
	 * This method should be called when no more errors will be added.
	 */
	public void flush()
	{
		if (!pendingErrors.isEmpty())
		{
			if (synchronousProcessing)
			{
				processBatchSync();
			}
			else
			{
				processBatch();
			}
		}
	}

	/**
	 * Processes any remaining errors and returns all collected errors.
	 * This method blocks until all processing is complete.
	 *
	 * @return all errors that have been collected, never {@code null}
	 */
	public List<ErrorContext> getAllErrors()
	{
		return new ArrayList<>(allCollectedErrors);
	}

	/**
	 * Returns the current number of errors collected.
	 *
	 * @return the error count
	 */
	public int getErrorCount()
	{
		return errorCount.get();
	}

	/**
	 * Returns whether any errors have been collected.
	 *
	 * @return {@code true} if errors are present, {@code false} otherwise
	 */
	public boolean hasErrors()
	{
		return errorCount.get() > 0;
	}

	/**
	 * Clears all collected errors and resets the count.
	 */
	public void clear()
	{
		pendingErrors.clear();
		allCollectedErrors.clear();
		errorCount.set(0);
	}

	/**
	 * Returns whether this collector has been closed.
	 *
	 * @return {@code true} if closed, {@code false} if still accepting errors
	 */
	public boolean isClosed()
	{
		return closed;
	}

	/**
	 * Processes a batch of errors asynchronously.
	 */
	private void processBatch()
	{
		if (pendingErrors.isEmpty())
		{
			return;
		}

		// Extract a batch of errors
		List<ErrorContext> batch = new ArrayList<>();
		for (int i = 0; i < batchSize && !pendingErrors.isEmpty(); ++i)
		{
			ErrorContext error = pendingErrors.poll();
			if (error != null)
			{
				batch.add(error);
			}
		}

		if (!batch.isEmpty())
		{
			// Process batch asynchronously
			CompletableFuture.runAsync(() -> batchProcessor.accept(batch), processingExecutor).
				exceptionally(throwable ->
				{
					// Log processing error but continue
					System.err.println("Error processing batch: " + throwable.getMessage());
					return null;
				});
		}
	}

	/**
	 * Processes a batch of errors synchronously for immediate completion.
	 */
	private void processBatchSync()
	{
		if (pendingErrors.isEmpty())
		{
			return;
		}

		// Extract a batch of errors
		List<ErrorContext> batch = new ArrayList<>();
		for (int i = 0; i < batchSize && !pendingErrors.isEmpty(); ++i)
		{
			ErrorContext error = pendingErrors.poll();
			if (error != null)
			{
				batch.add(error);
			}
		}

		if (!batch.isEmpty())
		{
			// Process batch synchronously
			try
			{
				batchProcessor.accept(batch);
			}
			catch (Exception e)
			{
				// Log processing error but continue
				System.err.println("Error processing batch: " + e.getMessage());
			}
		}
	}

	/**
	 * Closes the error collector and shuts down background processing.
	 * Any remaining errors will be processed before shutdown.
	 */
	@Override
	public void close()
	{
		if (closed)
		{
			return;
		}

		closed = true;
		flush();

		// Shutdown executor gracefully (only if using async processing)
		if (processingExecutor != null)
		{
			processingExecutor.shutdown();
			try
			{
				if (!processingExecutor.awaitTermination(5, java.util.concurrent.TimeUnit.SECONDS))
				{
					processingExecutor.shutdownNow();
				}
			}
			catch (InterruptedException e)
			{
				processingExecutor.shutdownNow();
				Thread.currentThread().interrupt();
			}
		}
	}

	/**
	 * Creates an error collector that integrates with an ErrorReporter for streaming processing.
	 *
	 * @param errorReporter the error reporter to send batches to, never {@code null}
	 * @param batchSize the batch size for processing
	 * @throws NullPointerException if {@code errorReporter} is {@code null}
	 * @throws IllegalArgumentException if {@code batchSize} is not positive
	 * @return a new ErrorCollector that forwards batches to the reporter, never {@code null}
	 */
	public static ErrorCollector forReporter(ErrorReporter errorReporter, int batchSize)
	{
		if (errorReporter == null)
		{
			throw new NullPointerException("Error reporter cannot be null");
		}
		if (batchSize <= 0)
		{
			throw new IllegalArgumentException("Batch size must be positive: " + batchSize);
		}

		return new ErrorCollector(batchSize, batch ->
		{
			// Forward each error in the batch to the reporter
			for (ErrorContext error : batch)
			{
				errorReporter.reportError(error);
			}
		}, true); // Use synchronous processing for immediate reporter integration
	}

	/**
	 * Creates a simple error collector that accumulates all errors without batch processing.
	 *
	 * @return a new ErrorCollector for simple accumulation, never {@code null}
	 */
	public static ErrorCollector createSimple()
	{
		return new ErrorCollector(Integer.MAX_VALUE, batch ->
		{
			// No-op processor for simple accumulation
		});
	}
}