package io.github.cowwoc.styler.parser;

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
 */
public class ObjectPool<T> {
    private final ConcurrentLinkedQueue<T> pool = new ConcurrentLinkedQueue<>();
    private final Supplier<T> factory;
    private final int maxSize;

    public ObjectPool(Supplier<T> factory) {
        this(factory, 1000); // Default max size
    }

    public ObjectPool(Supplier<T> factory, int maxSize) {
        this.factory = factory;
        this.maxSize = maxSize;
    }

    /**
     * Borrows an object from the pool, creating a new one if the pool is empty.
     */
    public T borrow() {
        T object = pool.poll();
        return object != null ? object : factory.get();
    }

    /**
     * Returns an object to the pool for reuse.
     * If the pool is at maximum capacity, the object will be discarded for GC.
     */
    public void return_object(T object) {
        if (pool.size() < maxSize) {
            // Reset the object if it has a reset method
            if (object instanceof Poolable poolable) {
                poolable.reset();
            }
            pool.offer(object);
        }
        // If pool is full, let the object be garbage collected
    }

    /**
     * Gets the current pool size (for monitoring).
     */
    public int size() {
        return pool.size();
    }

    /**
     * Interface for objects that can be pooled and reset.
     */
    public interface Poolable {
        void reset();
    }
}