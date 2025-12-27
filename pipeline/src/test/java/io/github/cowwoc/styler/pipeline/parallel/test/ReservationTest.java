package io.github.cowwoc.styler.pipeline.parallel.test;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;

import org.testng.annotations.Test;

import io.github.cowwoc.styler.pipeline.parallel.internal.MemoryReservationManager;
import io.github.cowwoc.styler.pipeline.parallel.internal.Reservation;

/**
 * Tests for Reservation lifecycle and AutoCloseable pattern.
 */
public class ReservationTest
{
	/**
	 * Tests that closing a reservation releases permits.
	 */
	@Test
	public void shouldReleasePermitsOnClose() throws InterruptedException
	{
		MemoryReservationManager manager = new MemoryReservationManager();
		long fileSize = 1024;

		Reservation reservation = manager.reserve(fileSize);
		int permits = reservation.permits();
		requireThat(permits, "permits").isPositive();

		reservation.close();
		// Verify permits were released by making another reservation
		// If permits were not released, this could block or fail
		try (Reservation secondReservation = manager.reserve(fileSize))
		{
			requireThat(secondReservation.permits(), "secondReservation.permits()").isPositive();
		}
	}

	/**
	 * Tests that Reservation works with try-with-resources pattern.
	 */
	@Test
	public void shouldWorkWithTryWithResources() throws InterruptedException
	{
		MemoryReservationManager manager = new MemoryReservationManager();
		long fileSize = 1024;

		try (Reservation reservation = manager.reserve(fileSize))
		{
			int permits = reservation.permits();
			requireThat(permits, "permits").isPositive();
		}
		// Resource should be automatically closed, permits released
	}

	/**
	 * Tests that double-close is handled safely.
	 */
	@Test
	public void shouldHandleDoubleCloseSafely() throws InterruptedException
	{
		MemoryReservationManager manager = new MemoryReservationManager();
		long fileSize = 1024;

		Reservation reservation = manager.reserve(fileSize);

		reservation.close();
		reservation.close();

		// Test passes if no exception thrown
	}

	/**
	 * Tests that Reservation provides correct permit count.
	 */
	@Test
	public void shouldProvideCorrectPermitCount() throws InterruptedException
	{
		MemoryReservationManager manager = new MemoryReservationManager();
		long fileSize = 5 * 1024 * 1024;
		int expectedPermits = (int) ((fileSize * MemoryReservationManager.MEMORY_MULTIPLIER) /
			MemoryReservationManager.PERMIT_UNIT_BYTES);

		try (Reservation reservation = manager.reserve(fileSize))
		{
			requireThat(reservation.permits(), "reservation.permits()").isEqualTo(expectedPermits);
		}
	}
}
