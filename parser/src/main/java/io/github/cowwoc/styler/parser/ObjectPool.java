package io.github.cowwoc.styler.parser;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Supplier;

/**
 * Java-idiomatic object pool for reducing allocations during parsing.
 *
 * Unlike Rust's arena allocation, this works with Java's GC by reusing objects
 * rather than managing raw memory. This approach is inspired by the String
 * interning pattern and SWC's performance optimizations, adapted for Java.
 *
 * Evidence: While Rust-style arenas don't directly apply to Java, the principle
 * of reducing temporary allocations during parsing can still provide performance
 * benefits in Java applications.
 *
 * @param <T> the type of objects managed by this pool
 */
public class ObjectPool<T>
{
	private final Queue<T> pool = new ConcurrentLinkedQueue<>();
	private final Supplier<T> factory;
	private final int maxSize;

	/**
	 * Creates an object pool with default maximum size.
	 *
	 * @param factory the factory supplier for creating new objects
	 */
	public ObjectPool(Supplier<T> factory)
{
		this(factory, 1000); // Default max size
	}

	/**
	 * Creates an object pool with specified maximum size.
	 *
	 * @param factory the factory supplier for creating new objects
	 * @param maxSize the maximum number of objects to keep in the pool
	 */
	public ObjectPool(Supplier<T> factory, int maxSize)
{
		this.factory = factory;
		this.maxSize = maxSize;
	}

	/**
	 * Borrows an object from the pool, creating a new one if the pool is empty.
	 *
	 * @return an object of type {@code T}, either from the pool or newly created by the factory
	 */
	public T borrow()
{
		T object = pool.poll();
		if (object != null)
{
			return object;
		}
		return factory.get();
	}

	/**
	 * Returns an object to the pool for reuse.
	 * If the pool is at maximum capacity, the object will be discarded for GC.
	 *
	 * @param object the object to return to the pool, or {@code null} to discard
	 */
	public void returnObject(T object)
{
		if (pool.size() < maxSize)
{
			// Reset the object if it has a reset method
			if (object instanceof Poolable poolable)
{
				poolable.reset();
			}
			pool.offer(object);
		}
		// If pool is full, let the object be garbage collected
	}

	/**
	 * Gets the current pool size (for monitoring).
	 *
	 * @return the number of objects currently available in the pool
	 */
	public int size()
{
		return pool.size();
	}

	/**
	 * Interface for objects that can be pooled and reset.
	 */
	@FunctionalInterface
	public interface Poolable
{
		/**
		 * Resets this object to its initial state for reuse in the pool.
		 * Called before returning the object to the pool.
		 */
		void reset();
	}
}