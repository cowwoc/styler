package io.github.styler.parser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Java-idiomatic replacement for arena allocation, focusing on Index-Overlay architecture.
 *
 * This maintains the core benefits of the study's findings:
 * - Memory efficiency through compact representation
 * - Cache-friendly access patterns
 * - Bulk operations for performance
 * - Index-based references instead of object pointers
 *
 * Evidence: The study shows "3-5x memory reduction" and "better cache locality"
 * from Index-Overlay patterns, which we achieve through primitive arrays
 * rather than Rust-style arena allocation.
 */
public class NodeRegistry {
    private static final int INITIAL_CAPACITY = 1024;
    private static final int GROWTH_FACTOR = 2;

    // Core node data (parallel arrays for cache efficiency)
    private int[] startOffsets;
    private int[] lengths;
    private byte[] nodeTypes;
    private int[] parentIds;

    // Child relationships using parallel arrays (true parallel array architecture)
    // childrenStart[i] = start index in childrenData array for node i's children
    // childrenCount[i] = number of children for node i
    private int[] childrenStart;
    private int[] childrenCount;
    private int[] childrenData;    // Flat array storing all child IDs
    private int childrenDataSize = 0;

    private int nodeCount = 0;
    private int capacity;

    public NodeRegistry() {
        this(INITIAL_CAPACITY);
    }

    public NodeRegistry(int initialCapacity) {
        this.capacity = initialCapacity;
        this.startOffsets = new int[capacity];
        this.lengths = new int[capacity];
        this.nodeTypes = new byte[capacity];
        this.parentIds = new int[capacity];
        this.childrenStart = new int[capacity];
        this.childrenCount = new int[capacity];

        // Estimate children data size (most nodes have 0-3 children)
        this.childrenData = new int[capacity * 2];

        // Initialize children arrays
        Arrays.fill(childrenStart, -1); // -1 indicates no children
        Arrays.fill(childrenCount, 0);
    }

    /**
     * Allocates a new node and returns its ID.
     */
    public int allocateNode(byte nodeType, int startOffset, int length, int parentId) {
        ensureCapacity();

        int nodeId = nodeCount++;
        startOffsets[nodeId] = startOffset;
        lengths[nodeId] = length;
        nodeTypes[nodeId] = nodeType;
        parentIds[nodeId] = parentId;

        // Add to parent's children using parallel arrays
        if (parentId >= 0 && parentId < nodeCount) {
            addChildToParent(parentId, nodeId);
        }

        return nodeId;
    }

    /**
     * Updates a node's length (useful during parsing when end position is determined).
     */
    public void updateNodeLength(int nodeId, int newLength) {
        validateNodeId(nodeId);
        lengths[nodeId] = newLength;
    }

    /**
     * Gets node information by ID.
     */
    public NodeInfo getNode(int nodeId) {
        validateNodeId(nodeId);
        return new NodeInfo(
            nodeId,
            startOffsets[nodeId],
            lengths[nodeId],
            nodeTypes[nodeId],
            parentIds[nodeId],
            getChildren(nodeId) // Now uses parallel array implementation
        );
    }

    /**
     * Gets all child nodes of the specified node.
     */
    public List<Integer> getChildren(int nodeId) {
        validateNodeId(nodeId);

        int count = childrenCount[nodeId];
        if (count == 0) {
            return List.of();
        }

        int start = childrenStart[nodeId];
        List<Integer> result = new ArrayList<>(count);
        for (int i = start; i < start + count; i++) {
            result.add(childrenData[i]);
        }

        return result;
    }

    /**
     * Gets the number of allocated nodes.
     */
    public int getNodeCount() {
        return nodeCount;
    }

    /**
     * Resets the registry for reuse (bulk deallocation equivalent).
     */
    public void reset() {
        nodeCount = 0;
        childrenDataSize = 0;
        // Reset children tracking - use old nodeCount value (0 here)
        Arrays.fill(childrenStart, -1);
        Arrays.fill(childrenCount, 0);
    }

    /**
     * Gets estimated memory usage in bytes.
     */
    public long getEstimatedMemoryUsage() {
        // Rough estimate: 4 arrays of ints/bytes + children lists
        return (capacity * 6 * Integer.BYTES) + // 6 int arrays (startOffsets, lengths, parentIds, childrenStart, childrenCount)
               (capacity * Byte.BYTES) +        // nodeTypes byte array
               (childrenData.length * Integer.BYTES); // childrenData array
    }

    private void ensureCapacity() {
        if (nodeCount >= capacity) {
            int newCapacity = capacity * GROWTH_FACTOR;

            // Resize all parallel arrays
            startOffsets = Arrays.copyOf(startOffsets, newCapacity);
            lengths = Arrays.copyOf(lengths, newCapacity);
            nodeTypes = Arrays.copyOf(nodeTypes, newCapacity);
            parentIds = Arrays.copyOf(parentIds, newCapacity);
            childrenStart = Arrays.copyOf(childrenStart, newCapacity);
            childrenCount = Arrays.copyOf(childrenCount, newCapacity);

            // Initialize new slots
            Arrays.fill(childrenStart, capacity, newCapacity, -1);
            Arrays.fill(childrenCount, capacity, newCapacity, 0);

            capacity = newCapacity;
        }
    }

    private void addChildToParent(int parentId, int childId) {
        // Ensure childrenData has space
        if (childrenDataSize >= childrenData.length) {
            childrenData = Arrays.copyOf(childrenData, childrenData.length * 2);
        }

        if (childrenCount[parentId] == 0) {
            // First child for this parent
            childrenStart[parentId] = childrenDataSize;
            childrenData[childrenDataSize] = childId;
            childrenCount[parentId] = 1;
            childrenDataSize++;
        } else {
            // Check if we can append (children are stored contiguously)
            int start = childrenStart[parentId];
            int count = childrenCount[parentId];

            if (start + count == childrenDataSize) {
                // Can append directly
                childrenData[childrenDataSize] = childId;
                childrenCount[parentId]++;
                childrenDataSize++;
            } else {
                // Need to relocate existing children to make room
                int newStart = childrenDataSize;

                // Copy existing children
                for (int i = 0; i < count; i++) {
                    childrenData[newStart + i] = childrenData[start + i];
                }

                // Add new child
                childrenData[newStart + count] = childId;

                // Update parent's info
                childrenStart[parentId] = newStart;
                childrenCount[parentId] = count + 1;
                childrenDataSize += count + 1;
            }
        }
    }

    private void validateNodeId(int nodeId) {
        if (nodeId < 0 || nodeId >= nodeCount) {
            throw new IllegalArgumentException("Invalid node ID: " + nodeId);
        }
    }

    /**
     * Node information record for efficient access.
     */
    public record NodeInfo(
        int nodeId,
        int startOffset,
        int length,
        byte nodeType,
        int parentId,
        List<Integer> childIds
    ) {
        public int endOffset() {
            return startOffset + length;
        }

        public boolean isRoot() {
            return parentId == -1;
        }

        public String getTypeName() {
            return NodeType.getTypeName(nodeType);
        }
    }
}