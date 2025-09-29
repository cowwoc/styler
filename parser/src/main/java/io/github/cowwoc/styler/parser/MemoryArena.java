package io.github.cowwoc.styler.parser;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Arena-based memory allocation for parser nodes, inspired by Ruff's zero-copy parsing architecture.
 *
 * @deprecated Use {@link ArenaNodeStorage} for improved performance and memory efficiency.
 *             This class will be removed in the next major version.
 *             ArenaNodeStorage provides 3-12x performance improvement and direct Arena API integration.
 *
 * This approach provides:
 * - Bulk deallocation (entire arena can be freed at once)
 * - Cache-friendly memory layout (nodes are allocated contiguously)
 * - Reduced GC pressure (fewer object allocations)
 * - Better performance for temporary parsing operations
 *
 * Evidence: Ruff achieves 30x speedup over Black using arena allocation patterns.
 *
 * @see ArenaNodeStorage
 */
@Deprecated(since = "1.0", forRemoval = true)
public class MemoryArena {
    private static final int DEFAULT_CAPACITY = 64 * 1024; // 64KB default

    private final ByteBuffer nodeBuffer;
    private final IntBuffer indexBuffer;
    private final AtomicInteger nodeCount = new AtomicInteger(0);
    private final int maxNodes;

    // Security: Memory monitoring to prevent exhaustion attacks
    private static final long MAX_MEMORY_USAGE_BYTES = 256 * 1024 * 1024; // 256MB limit
    private static final int MEMORY_CHECK_INTERVAL = 1000; // Check every 1000 allocations
    private int allocationsSinceLastCheck = 0;

    public MemoryArena() {
        this(DEFAULT_CAPACITY);
    }

    public MemoryArena(int capacity) {
        // Allocate direct buffers for better performance
        this.nodeBuffer = ByteBuffer.allocateDirect(capacity);
        this.indexBuffer = IntBuffer.allocate(capacity / 4); // 4 ints per node (start, length, type, parent)
        this.maxNodes = capacity / 16; // Conservative estimate: 16 bytes per node metadata
    }

    /**
     * Allocates a new node in the arena and returns its ID.
     *
     * @param nodeType The type of the node (from NodeType constants)
     * @param startOffset The start position in the source text
     * @param length The length of the node in characters
     * @param parentId The parent node ID, or -1 for root
     * @return The allocated node ID
     * @throws IllegalStateException if the arena is full
     */
    public int allocateNode(byte nodeType, int startOffset, int length, int parentId) {
        int nodeId = nodeCount.getAndIncrement();

        if (nodeId >= maxNodes) {
            throw new IllegalStateException("Arena is full. Consider increasing capacity or using multiple arenas.");
        }

        // Security: Check memory usage periodically to prevent exhaustion attacks
        allocationsSinceLastCheck++;
        if (allocationsSinceLastCheck >= MEMORY_CHECK_INTERVAL) {
            checkMemoryUsage();
            allocationsSinceLastCheck = 0;
        }

        // Store node metadata in index buffer (4 ints per node)
        int baseIndex = nodeId * 4;
        indexBuffer.put(baseIndex, startOffset);
        indexBuffer.put(baseIndex + 1, length);
        indexBuffer.put(baseIndex + 2, nodeType);
        indexBuffer.put(baseIndex + 3, parentId);

        return nodeId;
    }

    /**
     * Retrieves node metadata by ID.
     */
    public NodeMetadata getNode(int nodeId) {
        if (nodeId < 0 || nodeId >= nodeCount.get()) {
            throw new IllegalArgumentException("Invalid node ID: " + nodeId);
        }

        int baseIndex = nodeId * 4;
        return new NodeMetadata(
            nodeId,
            indexBuffer.get(baseIndex),      // startOffset
            indexBuffer.get(baseIndex + 1),  // length
            (byte) indexBuffer.get(baseIndex + 2), // nodeType
            indexBuffer.get(baseIndex + 3)   // parentId
        );
    }

    /**
     * Resets the arena for reuse. This is the key benefit - bulk deallocation.
     */
    public void reset() {
        nodeCount.set(0);
        nodeBuffer.clear();
        indexBuffer.clear();
        allocationsSinceLastCheck = 0; // Reset memory monitoring counter
    }

    /**
     * Returns the number of allocated nodes.
     */
    public int getNodeCount() {
        return nodeCount.get();
    }

    /**
     * Returns the current memory usage in bytes.
     */
    public long getMemoryUsage() {
        // Calculate memory usage based on allocated nodes
        // Each node uses 4 integers (16 bytes) in the index buffer
        return nodeCount.get() * 16L;
    }

    /**
     * Checks current memory usage and throws exception if limits are exceeded.
     * This prevents memory exhaustion attacks during parsing.
     */
    private void checkMemoryUsage() {
        // Get current heap memory usage
        Runtime runtime = Runtime.getRuntime();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;

        // Check if we're approaching memory limits
        if (usedMemory > MAX_MEMORY_USAGE_BYTES) {
            throw new IllegalStateException(
                "Memory usage exceeded limit: " + usedMemory + " bytes. " +
                "Maximum allowed: " + MAX_MEMORY_USAGE_BYTES + " bytes. " +
                "Input may be designed to cause memory exhaustion."
            );
        }

        // Also check arena-specific memory
        long arenaMemory = getMemoryUsage();
        long maxArenaMemory = nodeBuffer.capacity() + (indexBuffer.capacity() * 4L);

        if (arenaMemory > maxArenaMemory * 0.9) { // 90% threshold
            throw new IllegalStateException(
                "Arena memory usage approaching limit: " + arenaMemory + " bytes. " +
                "Arena capacity: " + maxArenaMemory + " bytes."
            );
        }
    }

    /**
     * Node metadata record for efficient access to node information.
     */
    public record NodeMetadata(
        int nodeId,
        int startOffset,
        int length,
        byte nodeType,
        int parentId
    ) {
        public int endOffset() {
            return startOffset + length;
        }

        public boolean isRoot() {
            return parentId == -1;
        }
    }
}