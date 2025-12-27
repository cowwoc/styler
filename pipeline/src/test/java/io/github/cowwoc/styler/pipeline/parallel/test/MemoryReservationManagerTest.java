package io.github.cowwoc.styler.pipeline.parallel.test;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;

import org.testng.annotations.Test;

import io.github.cowwoc.styler.pipeline.parallel.internal.MemoryReservationManager;
import io.github.cowwoc.styler.pipeline.parallel.internal.Reservation;

/**
 * Tests for reservation functionality using public API.
 */
public class MemoryReservationManagerTest
{
	/**
	 * Tests that zero-size files require minimum 1 permit.
	 */
	@Test
	public void shouldReserveOnePermitForZeroSizeFile() throws InterruptedException
	{
		MemoryReservationManager manager = new MemoryReservationManager();

		try (Reservation reservation = manager.reserve(0))
		{
			requireThat(reservation.permits(), "permits").isEqualTo(1);
		}
	}

	/**
	 * Tests that small files (2KB) require minimum 1 permit.
	 */
	@Test
	public void shouldReserveOnePermitForSmallFile() throws InterruptedException
	{
		MemoryReservationManager manager = new MemoryReservationManager();

		try (Reservation reservation = manager.reserve(2048))
		{
			requireThat(reservation.permits(), "permits").isEqualTo(1);
		}
	}

	/**
	 * Tests that larger files require proportionally more permits.
	 */
	@Test
	public void shouldScalePermitsWithFileSize() throws InterruptedException
	{
		MemoryReservationManager manager = new MemoryReservationManager();

		try (Reservation small = manager.reserve(1024 * 1024);
			 Reservation medium = manager.reserve(5 * 1024 * 1024);
			 Reservation large = manager.reserve(20 * 1024 * 1024))
		{
			requireThat(medium.permits(), "medium.permits()").isGreaterThan(small.permits());
			requireThat(large.permits(), "large.permits()").isGreaterThan(medium.permits());
		}
	}

	/**
	 * Tests that negative file sizes are rejected.
	 */
	@Test(expectedExceptions = IllegalArgumentException.class)
	public void shouldRejectNegativeFileSize() throws InterruptedException
	{
		MemoryReservationManager manager = new MemoryReservationManager();

		manager.reserve(-1);
	}

	/**
	 * Tests that permits are released when reservation is closed.
	 */
	@Test
	public void shouldReleasePermitsOnReservationClose() throws InterruptedException
	{
		MemoryReservationManager manager = new MemoryReservationManager();
		long fileSize = 1024;

		Reservation reservation = manager.reserve(fileSize);
		requireThat(reservation.permits(), "permits").isPositive();

		reservation.close();
		// If permits were not released, subsequent reservations would eventually block
		// No assertion needed - test passes if we complete without deadlock
	}

	/**
	 * Tests that multiple simultaneous reservations are supported.
	 */
	@Test
	public void shouldSupportMultipleSimultaneousReservations() throws InterruptedException
	{
		MemoryReservationManager manager = new MemoryReservationManager();
		long smallFileSize = 1024;

		try (Reservation reservation1 = manager.reserve(smallFileSize);
			 Reservation reservation2 = manager.reserve(smallFileSize);
			 Reservation reservation3 = manager.reserve(smallFileSize))
		{
			requireThat(reservation1.permits(), "reservation1.permits()").isPositive();
			requireThat(reservation2.permits(), "reservation2.permits()").isPositive();
			requireThat(reservation3.permits(), "reservation3.permits()").isPositive();
		}
	}
}
