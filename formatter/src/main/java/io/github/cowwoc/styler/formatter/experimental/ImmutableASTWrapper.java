package io.github.cowwoc.styler.formatter.experimental;

import io.github.cowwoc.styler.ast.ASTNode;
import java.util.Optional;
import java.util.List;
import java.util.Collections;

/**
 * Immutable wrapper around AST that prevents direct mutation and provides
 * thread-safe read access for parallel processing. This is the shared
 * parent context that multiple threads can safely access without synchronization.
 */
public final class ImmutableASTWrapper {

    private final ASTNode rootNode;
    private final long nodeCount;
    private final long estimatedMemorySize;

    /**
     * Creates an immutable wrapper around the provided AST.
     *
     * @param rootNode The root AST node to wrap
     * @throws IllegalArgumentException if rootNode is null
     */
    public ImmutableASTWrapper(ASTNode rootNode) {
        if (rootNode == null) {
            throw new IllegalArgumentException("Root node cannot be null");
        }
        this.rootNode = rootNode;
        this.nodeCount = calculateNodeCount(rootNode);
        this.estimatedMemorySize = calculateEstimatedMemorySize(rootNode);
    }

    /**
     * Returns the root compilation unit node for read-only access.
     *
     * @return The immutable root node
     */
    public ASTNode getRootNode() {
        return rootNode;
    }

    /**
     * Finds a parent node of a specific type for a given node.
     * This is a generic method that works with any AST node type.
     *
     * @param node The node to start searching from
     * @param targetType The class type to search for
     * @return Optional containing the parent of the specified type, or empty if not found
     */
    public <T extends ASTNode> Optional<T> getParentOfType(ASTNode node, Class<T> targetType) {
        if (node == null) {
            return Optional.empty();
        }

        Optional<ASTNode> current = node.getParent();
        while (current.isPresent()) {
            ASTNode currentNode = current.get();
            if (targetType.isInstance(currentNode)) {
                return Optional.of(targetType.cast(currentNode));
            }
            current = currentNode.getParent();
        }
        return Optional.empty();
    }

    /**
     * Returns all child nodes of the specified parent as an immutable list.
     *
     * @param parent The parent node
     * @return Immutable list of child nodes, or empty list if parent is null
     */
    public List<ASTNode> getChildren(ASTNode parent) {
        if (parent == null) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(parent.getChildren());
    }

    /**
     * Calculates the total number of nodes in the AST for memory tracking.
     *
     * @param node The root node to count from
     * @return Total number of nodes in the subtree
     */
    private long calculateNodeCount(ASTNode node) {
        if (node == null) {
            return 0;
        }

        long count = 1; // Count this node
        for (ASTNode child : node.getChildren()) {
            count += calculateNodeCount(child);
        }
        return count;
    }

    /**
     * Estimates memory size of the AST for resource protection.
     * Uses heuristic of 200 bytes per node as baseline estimate.
     *
     * @param node The root node to estimate size for
     * @return Estimated memory size in bytes
     */
    private long calculateEstimatedMemorySize(ASTNode node) {
        // Heuristic: Each AST node averages ~200 bytes
        // (object overhead + fields + references)
        return nodeCount * 200L;
    }

    /**
     * Returns the total number of nodes in this AST.
     *
     * @return Node count for resource tracking
     */
    public long getNodeCount() {
        return nodeCount;
    }

    /**
     * Returns the estimated memory size of this AST.
     *
     * @return Estimated memory size in bytes
     */
    public long getEstimatedMemorySize() {
        return estimatedMemorySize;
    }

    /**
     * Validates that a block is safe for mutable copy creation.
     * This is critical for preventing memory exhaustion attacks.
     *
     * @param blockRoot The root of the block to validate
     * @throws SecurityException if block exceeds memory safety limits
     */
    public void validateBlockForMutableCopy(ASTNode blockRoot) {
        if (blockRoot == null) {
            throw new IllegalArgumentException("Block root cannot be null");
        }

        long blockNodeCount = calculateNodeCount(blockRoot);
        long blockMemorySize = blockNodeCount * 200L; // Same heuristic

        // Security limit: No single block should exceed 50MB when copied
        final long MAX_BLOCK_MEMORY_SIZE = 50L * 1024L * 1024L; // 50MB

        if (blockMemorySize > MAX_BLOCK_MEMORY_SIZE) {
            throw new SecurityException(
                String.format("Block exceeds maximum memory allocation: %d bytes (limit: %d)",
                    blockMemorySize, MAX_BLOCK_MEMORY_SIZE));
        }

        // Additional safety: No block should have more than 100,000 nodes
        final long MAX_BLOCK_NODE_COUNT = 100_000L;
        if (blockNodeCount > MAX_BLOCK_NODE_COUNT) {
            throw new SecurityException(
                String.format("Block exceeds maximum node count: %d nodes (limit: %d)",
                    blockNodeCount, MAX_BLOCK_NODE_COUNT));
        }
    }
}