package io.github.cowwoc.styler.pipeline.parallel.test;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;

import org.testng.annotations.Test;

import io.github.cowwoc.styler.pipeline.parallel.internal.MemoryReservationManager;
import io.github.cowwoc.styler.pipeline.parallel.internal.Reservation;

/**
 * Edge case tests for memory reservation including boundary conditions.
 */
public class MemoryReservationManagerEdgeCaseTest
{
	/**
	 * Tests permit calculation at 24MB boundary.
	 */
	@Test
	public void shouldReserveCorrectPermitsForTwentyFourMegabyteFile() throws InterruptedException
	{
		MemoryReservationManager manager = new MemoryReservationManager();

		// 24MB file × MEMORY_MULTIPLIER = 120MB estimated = 120 permits
		long fileSize24MB = 24 * 1024 * 1024;
		int expectedPermits = (int) ((fileSize24MB * MemoryReservationManager.MEMORY_MULTIPLIER) /
			MemoryReservationManager.PERMIT_UNIT_BYTES);
		try (Reservation reservation = manager.reserve(fileSize24MB))
		{
			requireThat(reservation.permits(), "permits").isEqualTo(expectedPermits);
		}
	}

	/**
	 * Tests that manager can reserve permits for large files.
	 */
	@Test
	public void shouldReservePermitsForLargeFile() throws InterruptedException
	{
		MemoryReservationManager manager = new MemoryReservationManager();
		long fileSize = 50 * 1024 * 1024;
		int expectedPermits = (int) ((fileSize * MemoryReservationManager.MEMORY_MULTIPLIER) /
			MemoryReservationManager.PERMIT_UNIT_BYTES);

		try (Reservation reservation = manager.reserve(fileSize))
		{
			requireThat(reservation.permits(), "permits").isEqualTo(expectedPermits);
		}
	}
}
