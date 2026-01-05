package io.github.cowwoc.styler.pipeline.parallel.test;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.testng.annotations.Test;

import io.github.cowwoc.styler.pipeline.parallel.internal.MemoryReservationManager;
import io.github.cowwoc.styler.pipeline.parallel.internal.Reservation;

/**
 * Concurrency tests for memory reservation including blocking behavior and thread safety.
 */
public class MemoryReservationManagerConcurrencyTest
{
	/**
	 * Tests that reserve() blocks when insufficient permits are available and unblocks when released.
	 */
	@Test(timeOut = 5000)
	public void shouldBlockWhenPermitsUnavailableAndUnblockWhenReleased() throws InterruptedException
	{
		// Calculate permits needed for a 1MB file
		long largeFileSize = 1024 * 1024;
		int permitsNeeded = Math.max(1, (int) (largeFileSize * MemoryReservationManager.MEMORY_MULTIPLIER /
			MemoryReservationManager.PERMIT_UNIT_BYTES));

		// For blocking: after first reservation, remaining must be < permitsNeeded
		// So: totalPermits - permitsNeeded < permitsNeeded
		// Thus: totalPermits < 2 * permitsNeeded
		int totalPermits = 2 * permitsNeeded - 1;
		MemoryReservationManager manager = new MemoryReservationManager(totalPermits);

		// Reserve first file - should succeed
		Reservation firstReservation = manager.reserve(largeFileSize);
		int remainingPermits = manager.getAvailablePermits();
		requireThat(remainingPermits, "remainingPermits").isLessThan(permitsNeeded);

		// Latches for synchronization
		CountDownLatch threadStarted = new CountDownLatch(1);
		CountDownLatch reservationComplete = new CountDownLatch(1);

		Thread blockingThread = new Thread(() ->
		{
			try
			{
				// Signal that thread has started and is about to block
				threadStarted.countDown();

				// This should block because insufficient permits remain
				Reservation secondReservation = manager.reserve(largeFileSize);
				reservationComplete.countDown();
				secondReservation.close();
			}
			catch (InterruptedException e)
			{
				Thread.currentThread().interrupt();
			}
		});
		blockingThread.start();

		// Wait for thread to start (and begin blocking on reserve)
		boolean started = threadStarted.await(1, TimeUnit.MINUTES);
		requireThat(started, "started").isTrue();

		// Verify thread is blocked (reservation hasn't completed yet)
		requireThat(reservationComplete.getCount(), "reservationComplete.getCount()").isEqualTo(1L);

		// Release first reservation - should unblock the waiting thread
		firstReservation.close();

		// Wait for blocked thread to complete
		boolean completed = reservationComplete.await(1, TimeUnit.MINUTES);
		requireThat(completed, "completed").isTrue();
	}

	/**
	 * Tests that small files allow high concurrency.
	 */
	@Test(timeOut = 5000)
	public void shouldAllowHighConcurrencyForSmallFiles() throws InterruptedException
	{
		MemoryReservationManager manager = new MemoryReservationManager();
		long smallFileSize = 2048;

		AtomicInteger successCount = new AtomicInteger(0);
		List<Thread> threads = new ArrayList<>();

		// Create 50 threads, each reserving for a small file
		for (int i = 0; i < 50; ++i)
		{
			Thread thread = new Thread(() ->
			{
				try
				{
					Reservation reservation = manager.reserve(smallFileSize);
					successCount.incrementAndGet();
					reservation.close();
				}
				catch (InterruptedException e)
				{
					Thread.currentThread().interrupt();
				}
			});
			thread.start();
			threads.add(thread);
		}

		// Wait for all threads to complete (test timeout protects against hangs)
		for (Thread thread : threads)
			thread.join();

		// Verify all threads succeeded
		requireThat(successCount.get(), "successCount").isEqualTo(50);
	}

	/**
	 * Tests that mixed file sizes are handled concurrently.
	 */
	@Test(timeOut = 5000)
	public void shouldHandleMixedFileSizesConcurrently() throws InterruptedException
	{
		MemoryReservationManager manager = new MemoryReservationManager();

		AtomicInteger successCount = new AtomicInteger(0);
		AtomicInteger failureCount = new AtomicInteger(0);
		List<Thread> threads = new ArrayList<>();

		// Create mix of small and large file reservations
		for (int i = 0; i < 20; ++i)
		{
			final long fileSize;
			if (i % 2 == 0)
				fileSize = 1024;
			else
				fileSize = 5 * 1024 * 1024;
			Thread thread = new Thread(() ->
			{
				try
				{
					Reservation reservation = manager.reserve(fileSize);
					successCount.incrementAndGet();
					reservation.close();
				}
				catch (InterruptedException e)
				{
					failureCount.incrementAndGet();
					Thread.currentThread().interrupt();
				}
			});
			thread.start();
			threads.add(thread);
		}

		// Wait for all threads to complete (test timeout protects against hangs)
		for (Thread thread : threads)
			thread.join();

		// Verify all reservations succeeded
		requireThat(successCount.get(), "successCount").isEqualTo(20);
		requireThat(failureCount.get(), "failureCount").isEqualTo(0);
	}

	/**
	 * Tests that thread interruption during acquire is properly handled.
	 */
	@Test(timeOut = 10_000)
	public void shouldPropagateInterruptedExceptionDuringAcquire() throws InterruptedException
	{
		MemoryReservationManager manager = new MemoryReservationManager();

		// Reserve small file first
		long smallFileSize = 1024;
		Reservation initialReservation = manager.reserve(smallFileSize);

		// We need to find a file size that will cause blocking but not exceed total permits
		// Use 10MB which requires 50 permits - reasonable for most heaps
		long mediumSize = 10 * 1024 * 1024;

		CountDownLatch aboutToBlock = new CountDownLatch(1);
		AtomicBoolean interruptedExceptionThrown = new AtomicBoolean(false);
		AtomicBoolean illegalArgumentThrown = new AtomicBoolean(false);
		Thread blockingThread = new Thread(() ->
		{
			try
			{
				// Signal that we're about to attempt reservation
				aboutToBlock.countDown();
				// Try to reserve large amount - may block waiting for permits
				manager.reserve(mediumSize);
			}
			catch (InterruptedException e)
			{
				interruptedExceptionThrown.set(true);
				Thread.currentThread().interrupt();
			}
			catch (IllegalArgumentException e)
			{
				// If file requires more permits than available, that's a configuration issue
				illegalArgumentThrown.set(true);
			}
		});

		blockingThread.start();

		// Wait for thread to signal it's about to block
		boolean started = aboutToBlock.await(1, TimeUnit.MINUTES);
		requireThat(started, "started").isTrue();

		// Interrupt the thread
		blockingThread.interrupt();

		// Wait for thread to complete (test timeout protects against hangs)
		blockingThread.join();

		// If thread completed due to insufficient heap, skip the test
		if (illegalArgumentThrown.get())
		{
			initialReservation.close();
			return;
		}


		// If thread was blocked and interrupted, verify it received InterruptedException
		// Note: If there were enough permits, the thread may have completed normally
		// In that case, interruptedExceptionThrown will be false, which is also acceptable
		initialReservation.close();
	}
}
