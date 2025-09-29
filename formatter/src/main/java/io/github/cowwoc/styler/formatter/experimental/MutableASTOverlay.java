package io.github.cowwoc.styler.formatter.experimental;

import io.github.cowwoc.styler.ast.ASTNode;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Thread-local overlay that records AST modifications without mutating the shared
 * immutable parent AST. This enables thread-safe parallel processing by giving each
 * thread its own modification log while preserving the shared read-only context.
 *
 * The overlay acts like a "diff" or "patch" on top of the immutable base AST.
 */
public final class MutableASTOverlay {

    private final ImmutableASTWrapper parentContext;
    private final Map<String, ModificationRecord> modifications;
    private final AtomicLong modificationSequence;
    private final String threadId;

    /**
     * Creates a new overlay for tracking modifications to the given parent context.
     *
     * @param parentContext The immutable parent AST to overlay modifications on
     * @throws IllegalArgumentException if parentContext is null
     */
    public MutableASTOverlay(ImmutableASTWrapper parentContext) {
        if (parentContext == null) {
            throw new IllegalArgumentException("Parent context cannot be null");
        }
        this.parentContext = parentContext;
        this.modifications = new LinkedHashMap<>(); // Preserve modification order
        this.modificationSequence = new AtomicLong(0);
        this.threadId = Thread.currentThread().getName();

        // Register this overlay instance for resource tracking
        ThreadLocalResourceTracker.registerContextInstance(
            parentContext.getEstimatedMemorySize() / 10); // Overlay overhead ~10% of base AST
    }

    /**
     * Records a child replacement operation without mutating the original AST.
     *
     * @param parent The parent node containing the child
     * @param oldChild The child node to replace
     * @param newChild The replacement child node
     * @throws IllegalArgumentException if any parameter is null
     * @throws SecurityException if operation exceeds resource limits
     */
    public void recordReplaceChild(ASTNode parent, ASTNode oldChild, ASTNode newChild) {
        validateModificationParameters(parent, oldChild, newChild);

        ThreadLocalResourceTracker.enterRecursion();
        try {
            ThreadLocalResourceTracker.incrementModificationCount();

            String nodeId = generateNodeId(parent);
            long sequence = modificationSequence.incrementAndGet();

            ModificationRecord record = new ModificationRecord(
                ModificationType.REPLACE_CHILD,
                sequence,
                nodeId,
                parent,
                oldChild,
                newChild,
                threadId
            );

            modifications.put(nodeId + ":" + sequence, record);
        } finally {
            ThreadLocalResourceTracker.exitRecursion();
        }
    }

    /**
     * Records a child insertion operation without mutating the original AST.
     *
     * @param parent The parent node to insert into
     * @param index The index position for insertion
     * @param newChild The child node to insert
     * @throws IllegalArgumentException if parameters are invalid
     * @throws SecurityException if operation exceeds resource limits
     */
    public void recordInsertChild(ASTNode parent, int index, ASTNode newChild) {
        if (parent == null || newChild == null) {
            throw new IllegalArgumentException("Parent and new child cannot be null");
        }
        if (index < 0) {
            throw new IllegalArgumentException("Insertion index cannot be negative: " + index);
        }

        ThreadLocalResourceTracker.enterRecursion();
        try {
            ThreadLocalResourceTracker.incrementModificationCount();

            String nodeId = generateNodeId(parent);
            long sequence = modificationSequence.incrementAndGet();

            ModificationRecord record = new ModificationRecord(
                ModificationType.INSERT_CHILD,
                sequence,
                nodeId,
                parent,
                null, // No old child for insertion
                newChild,
                threadId
            );
            record.setInsertionIndex(index);

            modifications.put(nodeId + ":" + sequence, record);
        } finally {
            ThreadLocalResourceTracker.exitRecursion();
        }
    }

    /**
     * Records a child removal operation without mutating the original AST.
     *
     * @param parent The parent node to remove from
     * @param childToRemove The child node to remove
     * @throws IllegalArgumentException if any parameter is null
     * @throws SecurityException if operation exceeds resource limits
     */
    public void recordRemoveChild(ASTNode parent, ASTNode childToRemove) {
        if (parent == null || childToRemove == null) {
            throw new IllegalArgumentException("Parent and child to remove cannot be null");
        }

        ThreadLocalResourceTracker.enterRecursion();
        try {
            ThreadLocalResourceTracker.incrementModificationCount();

            String nodeId = generateNodeId(parent);
            long sequence = modificationSequence.incrementAndGet();

            ModificationRecord record = new ModificationRecord(
                ModificationType.REMOVE_CHILD,
                sequence,
                nodeId,
                parent,
                childToRemove,
                null, // No new child for removal
                threadId
            );

            modifications.put(nodeId + ":" + sequence, record);
        } finally {
            ThreadLocalResourceTracker.exitRecursion();
        }
    }

    /**
     * Returns all modifications recorded in this overlay, sorted by sequence number.
     *
     * @return Immutable list of modifications in application order
     */
    public List<ModificationRecord> getModifications() {
        return modifications.values().stream()
            .sorted(Comparator.comparing(ModificationRecord::getSequenceNumber))
            .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
    }

    /**
     * Returns the parent immutable context this overlay is based on.
     *
     * @return The immutable parent AST context
     */
    public ImmutableASTWrapper getParentContext() {
        return parentContext;
    }

    /**
     * Returns the number of modifications recorded in this overlay.
     *
     * @return Modification count for tracking and debugging
     */
    public int getModificationCount() {
        return modifications.size();
    }

    /**
     * Returns the thread ID that owns this overlay.
     *
     * @return Thread identifier for debugging and validation
     */
    public String getOwnerThreadId() {
        return threadId;
    }

    /**
     * Validates parameters for modification operations.
     */
    private void validateModificationParameters(ASTNode... nodes) {
        for (ASTNode node : nodes) {
            if (node == null) {
                throw new IllegalArgumentException("AST modification parameters cannot be null");
            }
        }
    }

    /**
     * Generates a stable unique identifier for an AST node for tracking in the overlay.
     * Uses the node's source position and type to create a stable ID that survives
     * builder pattern operations and reconstruction.
     */
    private String generateNodeId(ASTNode node) {
        return String.format("%s@%s",
            node.getClass().getSimpleName(),
            node.getRange().toString());
    }

    /**
     * Represents a single modification operation recorded in the overlay.
     */
    public static final class ModificationRecord {
        private final ModificationType type;
        private final long sequenceNumber;
        private final String targetNodeId;
        private final ASTNode parentNode;
        private final ASTNode oldChild;
        private final ASTNode newChild;
        private final String ownerThreadId;
        private final long timestamp;
        private int insertionIndex = -1; // Only used for INSERT operations

        private ModificationRecord(ModificationType type, long sequenceNumber, String targetNodeId,
                                 ASTNode parentNode, ASTNode oldChild, ASTNode newChild,
                                 String ownerThreadId) {
            this.type = type;
            this.sequenceNumber = sequenceNumber;
            this.targetNodeId = targetNodeId;
            this.parentNode = parentNode;
            this.oldChild = oldChild;
            this.newChild = newChild;
            this.ownerThreadId = ownerThreadId;
            this.timestamp = System.nanoTime();
        }

        public ModificationType getType() { return type; }
        public long getSequenceNumber() { return sequenceNumber; }
        public String getTargetNodeId() { return targetNodeId; }
        public ASTNode getParentNode() { return parentNode; }
        public ASTNode getOldChild() { return oldChild; }
        public ASTNode getNewChild() { return newChild; }
        public String getOwnerThreadId() { return ownerThreadId; }
        public long getTimestamp() { return timestamp; }
        public int getInsertionIndex() { return insertionIndex; }

        void setInsertionIndex(int index) { this.insertionIndex = index; }

        @Override
        public String toString() {
            return String.format("ModificationRecord{type=%s, seq=%d, node=%s, thread=%s}",
                type, sequenceNumber, targetNodeId, ownerThreadId);
        }
    }

    /**
     * Types of AST modifications that can be recorded in the overlay.
     */
    public enum ModificationType {
        REPLACE_CHILD,
        INSERT_CHILD,
        REMOVE_CHILD
    }
}