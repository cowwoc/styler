package io.github.cowwoc.styler.formatter.experimental;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Thread-local resource tracking to enforce per-thread cumulative limits
 * and prevent resource exhaustion attacks through multiple context instances.
 *
 * This addresses the security vulnerability where malicious input could create
 * multiple MutableFormattingContext instances per thread to bypass intended
 * resource protection.
 */
public final class ThreadLocalResourceTracker {

    // Per-thread resource limits (cumulative across all contexts in the thread)
    private static final int MAX_PER_THREAD_MODIFICATIONS = 50_000;
    private static final int MAX_PER_THREAD_RECURSION_DEPTH = 2_000;
    private static final long MAX_PER_THREAD_MEMORY_ALLOCATION = 200L * 1024L * 1024L; // 200MB

    // Thread-local storage for resource usage tracking
    private static final ThreadLocal<ResourceUsage> threadResources =
        ThreadLocal.withInitial(ResourceUsage::new);

    /**
     * Resource usage tracking for a single thread.
     */
    private static final class ResourceUsage {
        private int totalModifications = 0;
        private int currentRecursionDepth = 0;
        private int maxRecursionDepthReached = 0;
        private long totalMemoryAllocated = 0L;
        private final AtomicLong contextInstanceCount = new AtomicLong(0);

        public void reset() {
            totalModifications = 0;
            currentRecursionDepth = 0;
            maxRecursionDepthReached = 0;
            totalMemoryAllocated = 0L;
            contextInstanceCount.set(0);
        }
    }

    /**
     * Initializes resource tracking for the current thread.
     * Must be called at the start of thread processing.
     */
    public static void initialize() {
        ResourceUsage usage = threadResources.get();
        usage.reset();
    }

    /**
     * Cleans up resource tracking for the current thread.
     * Must be called when thread processing is complete.
     */
    public static void cleanup() {
        threadResources.remove();
    }

    /**
     * Registers a new context instance for resource tracking.
     *
     * @param estimatedMemorySize Estimated memory size of the context
     * @throws SecurityException if creating this context would exceed per-thread limits
     */
    public static void registerContextInstance(long estimatedMemorySize) {
        ResourceUsage usage = threadResources.get();

        // Track memory allocation
        long newTotalMemory = usage.totalMemoryAllocated + estimatedMemorySize;
        if (newTotalMemory > MAX_PER_THREAD_MEMORY_ALLOCATION) {
            throw new SecurityException(
                String.format("Per-thread memory allocation limit exceeded: %d bytes (limit: %d)",
                    newTotalMemory, MAX_PER_THREAD_MEMORY_ALLOCATION));
        }

        usage.totalMemoryAllocated = newTotalMemory;
        usage.contextInstanceCount.incrementAndGet();
    }

    /**
     * Enforces per-thread limits for modification and recursion operations.
     *
     * @param modificationDelta Number of modifications being added
     * @param recursionDelta Change in recursion depth (positive for entering, negative for exiting)
     * @throws SecurityException if operation would exceed per-thread limits
     */
    public static void enforcePerThreadLimits(int modificationDelta, int recursionDelta) {
        ResourceUsage usage = threadResources.get();

        // Check modification count limit
        int newModificationCount = usage.totalModifications + modificationDelta;
        if (newModificationCount > MAX_PER_THREAD_MODIFICATIONS) {
            throw new SecurityException(
                String.format("Per-thread modification limit exceeded: %d modifications (limit: %d)",
                    newModificationCount, MAX_PER_THREAD_MODIFICATIONS));
        }

        // Check recursion depth limit
        int newRecursionDepth = usage.currentRecursionDepth + recursionDelta;
        if (newRecursionDepth > MAX_PER_THREAD_RECURSION_DEPTH) {
            throw new SecurityException(
                String.format("Per-thread recursion depth limit exceeded: %d depth (limit: %d)",
                    newRecursionDepth, MAX_PER_THREAD_RECURSION_DEPTH));
        }

        // Update usage tracking
        usage.totalModifications = newModificationCount;
        usage.currentRecursionDepth = newRecursionDepth;
        usage.maxRecursionDepthReached = Math.max(usage.maxRecursionDepthReached, newRecursionDepth);
    }

    /**
     * Increments the modification count for the current thread.
     *
     * @throws SecurityException if increment would exceed per-thread modification limit
     */
    public static void incrementModificationCount() {
        enforcePerThreadLimits(1, 0);
    }

    /**
     * Enters a recursion level (increments recursion depth).
     *
     * @throws SecurityException if entering recursion would exceed per-thread depth limit
     */
    public static void enterRecursion() {
        enforcePerThreadLimits(0, 1);
    }

    /**
     * Exits a recursion level (decrements recursion depth).
     */
    public static void exitRecursion() {
        ResourceUsage usage = threadResources.get();
        usage.currentRecursionDepth = Math.max(0, usage.currentRecursionDepth - 1);
    }

    /**
     * Returns current resource usage statistics for monitoring and debugging.
     *
     * @return Resource usage information for the current thread
     */
    public static ResourceStatistics getCurrentUsage() {
        ResourceUsage usage = threadResources.get();
        return new ResourceStatistics(
            usage.totalModifications,
            usage.currentRecursionDepth,
            usage.maxRecursionDepthReached,
            usage.totalMemoryAllocated,
            usage.contextInstanceCount.get()
        );
    }

    /**
     * Resource usage statistics for monitoring and debugging.
     */
    public static final class ResourceStatistics {
        private final int totalModifications;
        private final int currentRecursionDepth;
        private final int maxRecursionDepthReached;
        private final long totalMemoryAllocated;
        private final long contextInstanceCount;

        private ResourceStatistics(int totalModifications, int currentRecursionDepth,
                                 int maxRecursionDepthReached, long totalMemoryAllocated,
                                 long contextInstanceCount) {
            this.totalModifications = totalModifications;
            this.currentRecursionDepth = currentRecursionDepth;
            this.maxRecursionDepthReached = maxRecursionDepthReached;
            this.totalMemoryAllocated = totalMemoryAllocated;
            this.contextInstanceCount = contextInstanceCount;
        }

        public int getTotalModifications() { return totalModifications; }
        public int getCurrentRecursionDepth() { return currentRecursionDepth; }
        public int getMaxRecursionDepthReached() { return maxRecursionDepthReached; }
        public long getTotalMemoryAllocated() { return totalMemoryAllocated; }
        public long getContextInstanceCount() { return contextInstanceCount; }

        @Override
        public String toString() {
            return String.format(
                "ResourceStatistics{modifications=%d, recursionDepth=%d, maxRecursion=%d, " +
                "memory=%d bytes, instances=%d}",
                totalModifications, currentRecursionDepth, maxRecursionDepthReached,
                totalMemoryAllocated, contextInstanceCount);
        }
    }
}