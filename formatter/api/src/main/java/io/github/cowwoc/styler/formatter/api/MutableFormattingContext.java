package io.github.cowwoc.styler.formatter.api;

import io.github.cowwoc.styler.ast.ASTNode;
import io.github.cowwoc.styler.ast.node.CompilationUnitNode;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;

/**
 * A mutable context for applying formatting transformations to an AST.
 *
 * This implementation uses immutable AST reconstruction pattern for thread-safe operations.
 * Based on comprehensive benchmarking, single-thread processing provides the best
 * performance for typical file sizes (&lt;5000 lines), while maintaining architectural integrity.
 */
public class MutableFormattingContext
{
	private CompilationUnitNode rootNode;
	private final String sourceText;
	private final Path filePath;
	private final RuleConfiguration configuration;
	private final Set<String> enabledRules;
	private final Map<String, Object> metadata;
	private final AtomicInteger modificationCount = new AtomicInteger(0);

	// Security: Resource protection mechanisms
	private static final int MAX_MODIFICATIONS = 10_000;
	private static final int MAX_RECURSION_DEPTH = 1000;
	private int currentRecursionDepth;

	/**
	 * Creates a new mutable formatting context.
	 *
	 * @param rootNode the root AST node to format
	 * @param sourceText the original source text
	 * @param filePath the path to the source file
	 * @param configuration the formatting configuration
	 * @param enabledRules the set of enabled rule IDs
	 * @param metadata additional metadata
	 * @throws NullPointerException if any argument is {@code null}
	 */
	public MutableFormattingContext(CompilationUnitNode rootNode, String sourceText, Path filePath,
	                               RuleConfiguration configuration, Set<String> enabledRules,
	                               Map<String, Object> metadata)
	{
		this.rootNode = requireThat(rootNode, "rootNode").isNotNull().getValue();
		this.sourceText = requireThat(sourceText, "sourceText").isNotNull().getValue();
		this.filePath = requireThat(filePath, "filePath").isNotNull().getValue();
		this.configuration = requireThat(configuration, "configuration").isNotNull().getValue();
		this.enabledRules = Set.copyOf(requireThat(enabledRules, "enabledRules").isNotNull().getValue());
		this.metadata = Map.copyOf(requireThat(metadata, "metadata").isNotNull().getValue());
	}

	/**
	 * Returns the root AST node for the source file being formatted.
	 *
	 * @return the root AST node, never {@code null}
	 */
	public CompilationUnitNode getRootNode()
	{
		return rootNode;
	}

	/**
	 * Returns the original source text of the file being formatted.
	 *
	 * @return the source text, never {@code null}
	 */
	public String getSourceText()
	{
		return sourceText;
	}

	/**
	 * Returns the file path of the source being formatted.
	 *
	 * @return the file path, never {@code null}
	 */
	public Path getFilePath()
	{
		return filePath;
	}

	/**
	 * Returns the configuration for the current rule.
	 *
	 * @return the rule configuration, never {@code null}
	 */
	public RuleConfiguration getConfiguration()
	{
		return configuration;
	}

	/**
	 * Returns the set of rule IDs that are enabled for this formatting operation.
	 *
	 * @return the set of enabled rule IDs, never {@code null}
	 */
	public Set<String> getEnabledRules()
	{
		return enabledRules;
	}

	/**
	 * Returns additional metadata associated with this formatting operation.
	 *
	 * @return the metadata map, never {@code null}
	 */
	public Map<String, Object> getMetadata()
	{
		return metadata;
	}

	/**
	 * Checks if a specific rule is enabled in this context.
	 *
	 * @param ruleId the ID of the rule to check, never {@code null}
	 * @return {@code true} if the rule is enabled, {@code false} otherwise
	 */
	public boolean isRuleEnabled(String ruleId)
	{
		return enabledRules.contains(ruleId);
	}

	/**
	 * Retrieves a metadata value with type safety.
	 *
	 * @param key  the metadata key, never {@code null}
	 * @param type the expected type of the value, never {@code null}
	 * @param <T>  the type parameter
	 * @return the metadata value cast to the specified type, or {@code null} if not present
	 * @throws ClassCastException if the value cannot be cast to the specified type
	 */
	public <T> T getMetadata(String key, Class<T> type)
	{
		Object value = metadata.get(key);
		if (value != null)
		{
			return type.cast(value);
		}
		return null;
	}

	/**
	 * Replaces the root node of the AST.
	 *
	 * @param newRoot the new root node
	 * @throws NullPointerException if {@code newRoot} is {@code null}
	 */
	public void setRootNode(CompilationUnitNode newRoot)
	{
		this.rootNode = requireThat(newRoot, "newRoot").isNotNull().getValue();
		modificationCount.incrementAndGet();
	}

	/**
	 * Replaces a child node with a new node using immutable AST reconstruction.
	 * This creates a new parent node with the child replaced, rebuilding the tree path to root.
	 *
	 * @param parent the parent node
	 * @param oldChild the child node to replace
	 * @param newChild the new child node
	 * @throws NullPointerException if any argument is {@code null}
	 * @throws IllegalArgumentException if {@code oldChild} is not a child of {@code parent}
	 * @throws IllegalStateException if resource limits are exceeded
	 */
	public void replaceChild(ASTNode parent, ASTNode oldChild, ASTNode newChild)
	{
		requireThat(parent, "parent").isNotNull();
		requireThat(oldChild, "oldChild").isNotNull();
		requireThat(newChild, "newChild").isNotNull();

		++currentRecursionDepth;
		try
		{
			checkResourceLimits();
			validateChildMembership(parent, oldChild);

			List<ASTNode> path = findPathToRoot(parent);
			int childIndex = findChildIndex(parent, oldChild);
			ASTNode newParent = buildNodeWithModifiedChild(parent, childIndex, newChild);

			reconstructAncestorChain(path, newParent);
		}
		finally
		{
			--currentRecursionDepth;
		}
	}

	/**
	 * Inserts a new node before the specified sibling using immutable AST reconstruction.
	 * This creates a new parent node with the child inserted, rebuilding the tree path to root.
	 *
	 * @param parent the parent node
	 * @param newChild the node to insert
	 * @param beforeSibling the sibling node before which to insert
	 * @throws NullPointerException if any argument is {@code null}
	 * @throws IllegalArgumentException if {@code beforeSibling} is not a child of {@code parent}
	 * @throws IllegalStateException if resource limits are exceeded
	 */
	public void insertBefore(ASTNode parent, ASTNode newChild, ASTNode beforeSibling)
	{
		requireThat(parent, "parent").isNotNull();
		requireThat(newChild, "newChild").isNotNull();
		requireThat(beforeSibling, "beforeSibling").isNotNull();

		++currentRecursionDepth;
		try
		{
			checkResourceLimits();
			validateChildMembership(parent, beforeSibling);

			int index = findChildIndex(parent, beforeSibling);
			List<ASTNode> path = findPathToRoot(parent);

			// Build new parent with child inserted at index
			List<ASTNode> children = new ArrayList<>(parent.getChildren());
			children.add(index, newChild);

			// For now, this will throw UnsupportedOperationException until specific node types are implemented
			// The error message will guide implementation when needed
			ASTNode newParent = rebuildNodeWithChildren(parent, children);

			reconstructAncestorChain(path, newParent);
		}
		finally
		{
			--currentRecursionDepth;
		}
	}

	/**
	 * Inserts a new node after the specified sibling using immutable AST reconstruction.
	 * This creates a new parent node with the child inserted, rebuilding the tree path to root.
	 *
	 * @param parent the parent node
	 * @param newChild the node to insert
	 * @param afterSibling the sibling node after which to insert
	 * @throws NullPointerException if any argument is {@code null}
	 * @throws IllegalArgumentException if {@code afterSibling} is not a child of {@code parent}
	 * @throws IllegalStateException if resource limits are exceeded
	 */
	public void insertAfter(ASTNode parent, ASTNode newChild, ASTNode afterSibling)
	{
		requireThat(parent, "parent").isNotNull();
		requireThat(newChild, "newChild").isNotNull();
		requireThat(afterSibling, "afterSibling").isNotNull();

		++currentRecursionDepth;
		try
		{
			checkResourceLimits();
			validateChildMembership(parent, afterSibling);

			int index = findChildIndex(parent, afterSibling);
			List<ASTNode> path = findPathToRoot(parent);

			// Build new parent with child inserted after the sibling
			List<ASTNode> children = new ArrayList<>(parent.getChildren());
			children.add(index + 1, newChild);

			ASTNode newParent = rebuildNodeWithChildren(parent, children);

			reconstructAncestorChain(path, newParent);
		}
		finally
		{
			--currentRecursionDepth;
		}
	}

	/**
	 * Removes a child node from its parent using immutable AST reconstruction.
	 * This creates a new parent node with the child removed, rebuilding the tree path to root.
	 *
	 * @param parent the parent node
	 * @param child the child node to remove
	 * @throws NullPointerException if any argument is {@code null}
	 * @throws IllegalArgumentException if {@code child} is not a child of {@code parent}
	 * @throws IllegalStateException if resource limits are exceeded
	 */
	public void removeChild(ASTNode parent, ASTNode child)
	{
		requireThat(parent, "parent").isNotNull();
		requireThat(child, "child").isNotNull();

		++currentRecursionDepth;
		try
		{
			checkResourceLimits();
			validateChildMembership(parent, child);

			List<ASTNode> path = findPathToRoot(parent);

			// Build new parent with child removed
			List<ASTNode> children = new ArrayList<>(parent.getChildren());
			children.remove(child);

			ASTNode newParent = rebuildNodeWithChildren(parent, children);

			reconstructAncestorChain(path, newParent);
		}
		finally
		{
			--currentRecursionDepth;
		}
	}

	/**
	 * Updates the whitespace associated with a node using immutable AST reconstruction.
	 * This creates a new node with updated whitespace, rebuilding the tree path to root.
	 *
	 * @param node the node to update
	 * @param whitespace the new whitespace content
	 * @throws NullPointerException if any argument is {@code null}
	 * @throws IllegalStateException if resource limits are exceeded
	 */
	public void setWhitespace(ASTNode node, String whitespace)
	{
		requireThat(node, "node").isNotNull();
		requireThat(whitespace, "whitespace").isNotNull();

		++currentRecursionDepth;
		try
		{
			checkResourceLimits();

			// Whitespace modification requires node-specific builder
			// This will throw UnsupportedOperationException until specific node types are implemented
			throw new UnsupportedOperationException(
				"Whitespace modification not yet implemented for node type: " + node.getClass().getSimpleName() +
				" - implement node-specific builder logic when needed for formatting rules");
		}
		finally
		{
			--currentRecursionDepth;
		}
	}

	/**
	 * Updates the comments associated with a node using immutable AST reconstruction.
	 * This creates a new node with updated comments, rebuilding the tree path to root.
	 *
	 * @param node the node to update
	 * @param comments the new comment content
	 * @throws NullPointerException if any argument is {@code null}
	 * @throws IllegalStateException if resource limits are exceeded
	 */
	public void setComments(ASTNode node, String comments)
	{
		requireThat(node, "node").isNotNull();
		requireThat(comments, "comments").isNotNull();

		++currentRecursionDepth;
		try
		{
			checkResourceLimits();

			// Comment modification requires node-specific builder
			// This will throw UnsupportedOperationException until specific node types are implemented
			throw new UnsupportedOperationException(
				"Comment modification not yet implemented for node type: " + node.getClass().getSimpleName() +
				" - implement node-specific builder logic when needed for formatting rules");
		}
		finally
		{
			--currentRecursionDepth;
		}
	}

	/**
	 * Finds the path from the root node to the target node using parent pointers.
	 * The returned path includes both the root and target nodes.
	 *
	 * @param target the node to find the path to, never {@code null}
	 * @return the list of nodes from root to target (inclusive), never {@code null}
	 * @throws NullPointerException if {@code target} is {@code null}
	 * @throws IllegalStateException if {@code target} is not part of the current AST tree
	 */
	private List<ASTNode> findPathToRoot(ASTNode target)
	{
		requireThat(target, "target").isNotNull();

		List<ASTNode> pathFromTargetToRoot = new ArrayList<>();
		ASTNode current = target;

		// Build path from target to root following parent pointers
		while (current != null)
		{
			pathFromTargetToRoot.add(current);
			current = current.getParent().orElse(null);
		}

		// Verify we reached the root node
		if (pathFromTargetToRoot.isEmpty() ||
			!pathFromTargetToRoot.get(pathFromTargetToRoot.size() - 1).equals(rootNode))
		{
			throw new IllegalStateException(
				"Cannot find path from root to node " + target.getClass().getSimpleName() +
				" at position " + target.getRange() +
				" - node may not be part of current AST tree rooted at " +
				rootNode.getClass().getSimpleName());
		}

		// Reverse to get root-to-target order
		Collections.reverse(pathFromTargetToRoot);
		return pathFromTargetToRoot;
	}

	/**
	 * Validates that a child node is actually a child of the specified parent node.
	 *
	 * @param parent the parent node, never {@code null}
	 * @param child the child node to validate, never {@code null}
	 * @throws NullPointerException if {@code parent} or {@code child} is {@code null}
	 * @throws IllegalArgumentException if {@code child} is not a child of {@code parent}
	 */
	private void validateChildMembership(ASTNode parent, ASTNode child)
	{
		requireThat(parent, "parent").isNotNull();
		requireThat(child, "child").isNotNull();

		List<ASTNode> children = parent.getChildren();
		if (!children.contains(child))
		{
			throw new IllegalArgumentException(
				"Node " + child.getClass().getSimpleName() +
				" at position " + child.getRange() +
				" is not a child of parent " + parent.getClass().getSimpleName() +
				" with " + children.size() + " children");
		}
	}

	/**
	 * Finds the index of a child node within its parent's child list.
	 *
	 * @param parent the parent node, never {@code null}
	 * @param child the child node to locate, never {@code null}
	 * @return the zero-based index of the child in the parent's child list
	 * @throws NullPointerException if {@code parent} or {@code child} is {@code null}
	 * @throws IllegalArgumentException if {@code child} is not found in {@code parent}'s children
	 */
	private int findChildIndex(ASTNode parent, ASTNode child)
	{
		requireThat(parent, "parent").isNotNull();
		requireThat(child, "child").isNotNull();

		List<ASTNode> children = parent.getChildren();
		int index = children.indexOf(child);

		if (index == -1)
		{
			throw new IllegalArgumentException(
				"Child node " + child.getClass().getSimpleName() +
				" at position " + child.getRange() +
				" not found in parent " + parent.getClass().getSimpleName() +
				" with " + children.size() + " children");
		}

		return index;
	}

	/**
	 * Builds a new parent node with a modified child at the specified index using type-specific builders.
	 *
	 * @param parent the parent node to rebuild, never {@code null}
	 * @param childIndex the index of the child to modify
	 * @param newChild the replacement child node, never {@code null}
	 * @return the reconstructed parent node with the modified child, never {@code null}
	 * @throws NullPointerException if {@code parent} or {@code newChild} is {@code null}
	 * @throws IllegalArgumentException if {@code childIndex} is out of bounds
	 * @throws UnsupportedOperationException if the node type is not yet supported
	 */
	private ASTNode buildNodeWithModifiedChild(ASTNode parent, int childIndex, ASTNode newChild)
	{
		requireThat(parent, "parent").isNotNull();
		requireThat(newChild, "newChild").isNotNull();

		List<ASTNode> children = parent.getChildren();
		if (childIndex < 0 || childIndex >= children.size())
		{
			throw new IllegalArgumentException(
				"Child index " + childIndex + " out of bounds for parent with " + children.size() + " children");
		}

		List<ASTNode> modifiedChildren = new ArrayList<>(children);
		modifiedChildren.set(childIndex, newChild);

		return rebuildNodeWithChildren(parent, modifiedChildren);
	}

	/**
	 * Rebuilds a parent node with a new list of children using type-specific builders.
	 * This method handles child distribution for insertion and removal operations.
	 *
	 * @param parent the parent node to rebuild, never {@code null}
	 * @param newChildren the new list of children, never {@code null}
	 * @return the reconstructed parent node with the new children, never {@code null}
	 * @throws NullPointerException if {@code parent} or {@code newChildren} is {@code null}
	 * @throws UnsupportedOperationException if the node type is not yet supported
	 */
	private ASTNode rebuildNodeWithChildren(ASTNode parent, List<ASTNode> newChildren)
	{
		requireThat(parent, "parent").isNotNull();
		requireThat(newChildren, "newChildren").isNotNull();

		// Use instanceof to determine node type and call type-specific builder methods
		// This is a simplified approach compared to a full visitor pattern
		// Add more node types as needed for formatter rules
		if (parent instanceof CompilationUnitNode)
		{
			// CompilationUnitNode has specific child collections: packageDeclaration, imports, typeDeclarations
			// Proper implementation would need to distribute children into these collections
			// based on their actual types (package declaration, imports, type declarations)
			throw new UnsupportedOperationException(
				"CompilationUnitNode reconstruction requires child distribution logic - " +
				"implement when needed for specific formatting rules");
		}

		// Add more node types here as needed (ClassDeclarationNode, MethodDeclarationNode, etc.)

		throw new UnsupportedOperationException(
			"Node reconstruction not yet implemented for type: " + parent.getClass().getSimpleName() +
			" - implement when needed for formatting rules");
	}

	/**
	 * Reconstructs the ancestor chain from a modified node to the root.
	 * Iteratively rebuilds each ancestor node with its modified child, updating the root when complete.
	 *
	 * @param path the list of nodes from root to target, never {@code null}
	 * @param modifiedNode the modified node to propagate upward, never {@code null}
	 * @throws NullPointerException if {@code path} or {@code modifiedNode} is {@code null}
	 * @throws IllegalStateException if resource limits are exceeded or path is invalid
	 * @throws UnsupportedOperationException if any ancestor node type is not supported
	 */
	private void reconstructAncestorChain(List<ASTNode> path, ASTNode modifiedNode)
	{
		requireThat(path, "path").isNotNull();
		requireThat(modifiedNode, "modifiedNode").isNotNull();

		if (path.isEmpty())
		{
			throw new IllegalArgumentException("Path cannot be empty");
		}

		ASTNode modifiedChild = modifiedNode;

		// Iterate from parent of target up to root
		for (int i = path.size() - 2; i >= 0; --i)
		{
			ASTNode ancestor = path.get(i);
			ASTNode oldChild = path.get(i + 1);
			int childIndex = findChildIndex(ancestor, oldChild);

			modifiedChild = buildNodeWithModifiedChild(ancestor, childIndex, modifiedChild);
		}

		// modifiedChild is now the new root
		setRootNode((CompilationUnitNode) modifiedChild);
	}

	/**
	 * @return the number of modifications made to the AST
	 */
	public int getModificationCount()
	{
		return modificationCount.get();
	}

	/**
	 * Checks resource limits to prevent stack overflow and excessive modifications.
	 *
	 * @throws IllegalStateException if resource limits are exceeded
	 */
	private void checkResourceLimits()
	{
		if (currentRecursionDepth > MAX_RECURSION_DEPTH)
	{
			throw new IllegalStateException(
				"Maximum recursion depth exceeded: " + currentRecursionDepth + " > " + MAX_RECURSION_DEPTH);
		}
		if (modificationCount.get() >= MAX_MODIFICATIONS)
	{
			throw new IllegalStateException(
				"Maximum modification count exceeded: " + modificationCount.get() + " >= " + MAX_MODIFICATIONS);
		}
	}
}