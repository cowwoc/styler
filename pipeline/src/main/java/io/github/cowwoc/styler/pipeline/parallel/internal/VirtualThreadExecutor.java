package io.github.cowwoc.styler.pipeline.parallel.internal;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Internal wrapper for virtual thread executor with semaphore-based concurrency limiting.
 * <p>
 * Manages a virtual thread executor and enforces concurrency limits through semaphore permits.
 * This ensures memory-safe parallel file processing by preventing excessive concurrent operations.
 * <p>
 * Resource Management:
 * <ul>
 *     <li>Uses {@code Executors.newVirtualThreadPerTaskExecutor()} for lightweight virtual threads</li>
 *     <li>Semaphore permits control maximum concurrent operations</li>
 *     <li>Must be closed via try-with-resources to properly shut down executor</li>
 * </ul>
 * <p>
 * <b>Thread-safety</b>: This class is thread-safe. Multiple threads may submit tasks
 * concurrently.
 *
 * @see ExecutorService
 * @see Semaphore
 */
public final class VirtualThreadExecutor implements AutoCloseable
{
	private final ExecutorService executor;
	private final Semaphore semaphore;
	private final AtomicBoolean closed = new AtomicBoolean(false);

	/**
	 * Creates a virtual thread executor with concurrency limiting.
	 * <p>
	 * The executor manages a pool of virtual threads, each responsible for processing one file.
	 * The semaphore prevents more than {@code maxConcurrency} files from being processed
	 * simultaneously.
	 *
	 * @param maxConcurrency maximum number of concurrent tasks, must be &gt; 0
	 * @throws IllegalArgumentException if {@code maxConcurrency} is not positive
	 */
	public VirtualThreadExecutor(int maxConcurrency)
	{
		requireThat(maxConcurrency, "maxConcurrency").isGreaterThan(0);
		this.executor = Executors.newVirtualThreadPerTaskExecutor();
		this.semaphore = new Semaphore(maxConcurrency);
	}

	/**
	 * Submits a task for execution by a virtual thread with automatic permit management.
	 * <p>
	 * The task runs asynchronously after acquiring a semaphore permit. The permit is
	 * automatically released when the task completes (normally or exceptionally).
	 * Callers must manage synchronization and result collection through other means
	 * (e.g., callbacks, concurrent collections).
	 *
	 * @param task the task to execute, must not be null
	 * @throws NullPointerException if {@code task} is null
	 * @throws IllegalStateException if the executor has been closed
	 */
	public void submit(Runnable task)
	{
		requireThat(task, "task").isNotNull();
		if (closed.get())
		{
			throw new IllegalStateException("Executor has been closed");
		}
		executor.submit(() ->
		{
			try
			{
				semaphore.acquire();
				try
				{
					task.run();
				}
				finally
				{
					semaphore.release();
				}
			}
			catch (InterruptedException e)
			{
				Thread.currentThread().interrupt();
			}
		});
	}

	/**
	 * Gracefully shuts down the executor and waits for pending tasks to complete.
	 * <p>
	 * Calls {@code shutdown()} on the executor and waits up to 30 seconds for termination.
	 * If tasks don't complete within the timeout, any remaining tasks are cancelled via
	 * {@code shutdownNow()}.
	 * <p>
	 * Safe to call multiple times - subsequent calls are no-ops.
	 */
	@Override
	public void close()
	{
		if (!closed.compareAndSet(false, true))
		{
			return;
		}

		executor.shutdown();
		try
		{
			if (!executor.awaitTermination(30, TimeUnit.SECONDS))
			{
				executor.shutdownNow();
			}
		}
		catch (InterruptedException e)
		{
			executor.shutdownNow();
			Thread.currentThread().interrupt();
		}
	}

	/**
	 * Checks if the executor has been closed.
	 *
	 * @return true if closed, false otherwise
	 */
	public boolean isClosed()
	{
		return closed.get();
	}
}
