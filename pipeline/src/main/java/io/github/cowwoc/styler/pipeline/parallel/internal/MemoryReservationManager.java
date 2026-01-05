package io.github.cowwoc.styler.pipeline.parallel.internal;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;

import java.util.concurrent.Semaphore;

/**
 * Manages memory-based concurrency control for batch file processing.
 * <p>
 * Uses a semaphore where permits represent memory units (1 MB each). Files acquire permits
 * proportional to their estimated memory consumption. This prevents out-of-memory errors by
 * limiting concurrent file processing based on available heap memory.
 * <p>
 * Memory Estimation:
 * <ul>
 *     <li>Total permits calculated from heap size: (maxHeap * 0.7) / 1 MB</li>
 *     <li>Per-file permits: max(1, (fileSize * 5) / 1 MB)</li>
 *     <li>Files reserve permits before processing and release upon completion</li>
 * </ul>
 * <p>
 * Example:
 * <pre>
 * MemoryReservationManager manager = new MemoryReservationManager();
 * try (Reservation reservation = manager.reserve(fileSizeBytes))
 * {
 *     processFile(file);
 * }
 * </pre>
 * <p>
 * <b>Thread-safety</b>: This class is thread-safe. Multiple threads may call
 * {@code reserve()} and {@code release()} concurrently. The semaphore uses unfair
 * ordering for better throughput.
 */
public final class MemoryReservationManager
{
	/**
	 * The size of each permit unit in bytes (1 MB).
	 */
	public static final int PERMIT_UNIT_BYTES = 1024 * 1024;
	/**
	 * The fraction of heap memory available for file processing (70%).
	 */
	public static final double HEAP_USAGE_FRACTION = 0.7;
	/**
	 * The memory multiplier applied to file size to estimate processing memory.
	 * <p>
	 * Accounts for tokenization, AST nodes, formatting state, and output buffers.
	 */
	public static final int MEMORY_MULTIPLIER = 5;

	private final Semaphore semaphore;
	private final int totalPermits;

	/**
	 * Creates a manager with permits based on available heap memory.
	 * <p>
	 * Total permits = (maxHeap * 0.7) / 1 MB. Uses an unfair semaphore for better
	 * throughput under contention.
	 */
	public MemoryReservationManager()
	{
		long maxMemory = Runtime.getRuntime().maxMemory();
		long availableForProcessing = (long) (maxMemory * HEAP_USAGE_FRACTION);
		this.totalPermits = (int) (availableForProcessing / PERMIT_UNIT_BYTES);
		this.semaphore = new Semaphore(totalPermits);
	}

	/**
	 * Creates a manager with a specific number of permits.
	 * <p>
	 * This constructor is for testing purposes to enable controlled concurrency scenarios.
	 *
	 * @param totalPermits the total number of permits available
	 * @throws IllegalArgumentException if {@code totalPermits} is not positive
	 */
	public MemoryReservationManager(int totalPermits)
	{
		requireThat(totalPermits, "totalPermits").isPositive();
		this.totalPermits = totalPermits;
		this.semaphore = new Semaphore(totalPermits);
	}

	/**
	 * Reserves memory for processing a file.
	 * <p>
	 * Blocks until sufficient permits are available. The returned {@code Reservation}
	 * must be closed to release permits.
	 *
	 * @param fileSizeBytes the file size in bytes
	 * @return a reservation handle (must be closed when done)
	 * @throws InterruptedException if the thread is interrupted while waiting for permits
	 * @throws IllegalArgumentException <ul>
	 *                                    <li>if {@code fileSizeBytes} is negative</li>
	 *                                    <li>if estimated memory exceeds total available
	 *                                        (deadlock prevention)</li>
	 *                                  </ul>
	 */
	public Reservation reserve(long fileSizeBytes) throws InterruptedException
	{
		requireThat(fileSizeBytes, "fileSizeBytes").isNotNegative();

		int permits = calculatePermits(fileSizeBytes);

		// Fail-fast if file requires more permits than total available
		if (permits > totalPermits)
		{
			throw new IllegalArgumentException(
				"Insufficient heap memory to process file. " +
				"Estimated memory required: " + (long) permits * PERMIT_UNIT_BYTES / (1024 * 1024) + " MB, " +
				"available for processing: " + (long) totalPermits * PERMIT_UNIT_BYTES / (1024 * 1024) + " MB. " +
				"Increase -Xmx or skip this file.");
		}

		semaphore.acquire(permits);
		return new Reservation(permits, this);
	}

	/**
	 * Releases permits back to the pool.
	 * <p>
	 * Called by {@code Reservation.close()} to return permits when processing completes.
	 *
	 * @param permits the number of permits to release, must be positive
	 */
	public void release(int permits)
	{
		semaphore.release(permits);
	}

	/**
	 * Returns the total number of permits.
	 *
	 * @return total permits available in this manager
	 */
	public int getTotalPermits()
	{
		return totalPermits;
	}

	/**
	 * Returns currently available permits.
	 *
	 * @return available permits
	 */
	public int getAvailablePermits()
	{
		return semaphore.availablePermits();
	}

	/**
	 * Calculates permits needed for a given file size.
	 *
	 * @param fileSizeBytes the file size in bytes
	 * @return number of permits needed, minimum 1
	 */
	public int calculatePermits(long fileSizeBytes)
	{
		long estimatedBytes = fileSizeBytes * MEMORY_MULTIPLIER;
		return Math.max(1, (int) (estimatedBytes / PERMIT_UNIT_BYTES));
	}
}
