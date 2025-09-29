package io.github.cowwoc.styler.formatter.experimental;

import io.github.cowwoc.styler.ast.ASTNode;
import io.github.cowwoc.styler.formatter.api.RuleConfiguration;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Experimental hybrid formatting context that provides thread-safe AST modification through
 * an overlay pattern. This is a proof-of-concept implementation of the transformation context API.
 *
 * This experimental implementation demonstrates the hybrid architecture concept:
 * - Shared immutable parent AST for read-only access
 * - Thread-local overlay for recording modifications
 * - Resource protection through ThreadLocalResourceTracker
 * - Block-level parallel processing support
 *
 * NOTE: This is an experimental proof-of-concept. Integration with the existing FormattingContext
 * API requires additional work to resolve inheritance and API compatibility issues.
 */
public final class MutableFormattingContext {

    private final ASTNode rootNode;
    private final String sourceText;
    private final Path filePath;
    private final RuleConfiguration configuration;
    private final Set<String> enabledRules;
    private final Map<String, Object> metadata;

    private final ImmutableASTWrapper immutableParent;
    private final MutableASTOverlay localOverlay;

    /**
     * Creates a hybrid formatting context with immutable parent and mutable overlay.
     *
     * @param rootNode The root AST node to wrap as immutable parent
     * @param sourceText Original source text
     * @param filePath Path to the source file
     * @param configuration Rule configuration
     * @param enabledRules Set of enabled rule IDs
     * @param metadata Additional metadata
     */
    public MutableFormattingContext(ASTNode rootNode,
                                  String sourceText,
                                  Path filePath,
                                  RuleConfiguration configuration,
                                  Set<String> enabledRules,
                                  Map<String, Object> metadata) {
        this.rootNode = rootNode;
        this.sourceText = sourceText;
        this.filePath = filePath;
        this.configuration = configuration;
        this.enabledRules = enabledRules;
        this.metadata = metadata;

        this.immutableParent = new ImmutableASTWrapper(rootNode);
        this.localOverlay = new MutableASTOverlay(immutableParent);
    }

    /**
     * Creates a child context for processing a specific AST block.
     * Used for block-level parallel processing.
     *
     * @param parentContext The parent context to inherit from
     * @param workingBlock The specific block this context will work on
     */
    public MutableFormattingContext(MutableFormattingContext parentContext, ASTNode workingBlock) {
        this.rootNode = parentContext.rootNode;
        this.sourceText = parentContext.sourceText;
        this.filePath = parentContext.filePath;
        this.configuration = parentContext.configuration;
        this.enabledRules = parentContext.enabledRules;
        this.metadata = parentContext.metadata;

        // Validate block is safe for mutable copy creation
        parentContext.immutableParent.validateBlockForMutableCopy(workingBlock);

        this.immutableParent = parentContext.immutableParent; // Shared read-only context
        this.localOverlay = new MutableASTOverlay(immutableParent); // Thread-owned overlay
    }

    // Basic accessor methods
    public ASTNode getRootNode() { return rootNode; }
    public String getSourceText() { return sourceText; }
    public Path getFilePath() { return filePath; }
    public RuleConfiguration getConfiguration() { return configuration; }
    public Set<String> getEnabledRules() { return enabledRules; }
    public Map<String, Object> getMetadata() { return metadata; }

    /**
     * Returns the immutable parent context for read-only access.
     * Thread-safe - multiple threads can safely read from this.
     *
     * @return The shared immutable AST wrapper
     */
    public ImmutableASTWrapper getImmutableParent() {
        return immutableParent;
    }

    /**
     * Returns the thread-local modification overlay.
     * Thread-owned - only this thread should access this overlay.
     *
     * @return The mutable overlay for recording modifications
     */
    public MutableASTOverlay getLocalOverlay() {
        return localOverlay;
    }

    /**
     * Records a child replacement in the thread-local overlay.
     * Does NOT mutate the shared immutable AST.
     *
     * @param parent the parent node
     * @param oldChild the child to replace
     * @param newChild the replacement child
     * @throws SecurityException if operation exceeds resource limits
     */
    public void replaceChild(ASTNode parent, ASTNode oldChild, ASTNode newChild) {
        localOverlay.recordReplaceChild(parent, oldChild, newChild);
    }

    /**
     * Records a child insertion in the thread-local overlay.
     * Does NOT mutate the shared immutable AST.
     *
     * @param parent the parent node to insert into
     * @param index the position for insertion
     * @param newChild the child node to insert
     * @throws SecurityException if operation exceeds resource limits
     */
    public void insertChild(ASTNode parent, int index, ASTNode newChild) {
        localOverlay.recordInsertChild(parent, index, newChild);
    }

    /**
     * Records a child removal in the thread-local overlay.
     * Does NOT mutate the shared immutable AST.
     *
     * @param parent the parent node
     * @param child the child to remove
     * @throws SecurityException if operation exceeds resource limits
     */
    public void removeChild(ASTNode parent, ASTNode child) {
        localOverlay.recordRemoveChild(parent, child);
    }

    /**
     * Gets the number of modifications recorded in the overlay.
     *
     * @return modification count from overlay
     */
    public int getModificationCount() {
        return localOverlay.getModificationCount();
    }

    /**
     * Checks if any modifications have been recorded.
     *
     * @return true if overlay contains modifications
     */
    public boolean hasModifications() {
        return localOverlay.getModificationCount() > 0;
    }

    /**
     * Returns thread-local resource usage statistics.
     *
     * @return Current thread's resource usage
     */
    public ThreadLocalResourceTracker.ResourceStatistics getResourceUsage() {
        return ThreadLocalResourceTracker.getCurrentUsage();
    }

    /**
     * Creates a child context for processing a specific block in parallel.
     * This enables block-level parallel processing with proper context sharing.
     *
     * @param workingBlock The AST block to process in the child context
     * @return New child context with shared immutable parent and isolated overlay
     */
    public MutableFormattingContext createChildContext(ASTNode workingBlock) {
        return new MutableFormattingContext(this, workingBlock);
    }

    /**
     * Returns all modifications as an ordered list for result assembly.
     *
     * @return List of modifications in application order
     */
    public List<MutableASTOverlay.ModificationRecord> getModifications() {
        return localOverlay.getModifications();
    }
}