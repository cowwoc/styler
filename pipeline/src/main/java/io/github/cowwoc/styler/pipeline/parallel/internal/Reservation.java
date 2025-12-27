package io.github.cowwoc.styler.pipeline.parallel.internal;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;

/**
 * A memory reservation handle for use with try-with-resources.
 * <p>
 * Automatically releases reserved permits when closed. This record encapsulates the number
 * of permits reserved and the manager responsible for their allocation.
 * <p>
 * <b>Thread-safety</b>: This class is thread-safe. The underlying semaphore ensures
 * thread-safe permit operations.
 *
 * @param permits the number of permits reserved, must be positive
 * @param manager the memory reservation manager that issued this reservation
 */
public record Reservation(int permits, MemoryReservationManager manager) implements AutoCloseable
{
	/**
	 * Creates a reservation with validation.
	 *
	 * @param permits the number of permits, must be positive
	 * @param manager the manager, must not be null
	 * @throws IllegalArgumentException if {@code permits} is not positive
	 * @throws NullPointerException if {@code manager} is null
	 */
	public Reservation
	{
		requireThat(permits, "permits").isGreaterThan(0);
		requireThat(manager, "manager").isNotNull();
	}

	/**
	 * Releases the reserved permits back to the manager.
	 * <p>
	 * Safe to call multiple times; underlying semaphore release is idempotent.
	 */
	@Override
	public void close()
	{
		manager.release(permits);
	}
}
