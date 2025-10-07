package io.github.cowwoc.styler.ast;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;
import io.github.cowwoc.styler.ast.visitor.ASTVisitor;
import io.github.cowwoc.styler.ast.builder.ASTNodeBuilder;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Base abstract class for all Abstract Syntax Tree nodes.
 * Provides immutable structure with comprehensive metadata preservation and visitor pattern support.
 * All AST nodes are immutable after construction and thread-safe for concurrent access.
 */
public abstract class ASTNode
	{
	private final SourceRange range;
	private final List<Comment> leadingComments;
	private final List<Comment> trailingComments;
	private final WhitespaceInfo whitespace;
	private final FormattingHints hints;
	private final Optional<ASTNode> parent;

	/**
	 * Constructs an AST node with complete metadata.
	 *
	 * @param range the source range of this node
	 *
	 * @param leadingComments comments appearing before this node
	 *
	 * @param trailingComments comments appearing after this node
	 *
	 * @param whitespace whitespace information for this node
	 *
	 * @param hints formatting hints for this node
	 *
	 * @param parent the parent node (empty for final root nodes)
	 * @throws NullPointerException if {@code range}, {@code leadingComments}, {@code trailingComments},
	 *                              {@code whitespace}, {@code hints}, or {@code parent} is null
	 */
	protected ASTNode(SourceRange range, List<Comment> leadingComments, List<Comment> trailingComments,
		WhitespaceInfo whitespace, FormattingHints hints, Optional<ASTNode> parent)
			{
		requireThat(range, "range").isNotNull();
		requireThat(leadingComments, "leadingComments").isNotNull();
		requireThat(trailingComments, "trailingComments").isNotNull();
		requireThat(whitespace, "whitespace").isNotNull();
		requireThat(hints, "hints").isNotNull();
		requireThat(parent, "parent").isNotNull();

		this.range = range;
		this.leadingComments = List.copyOf(leadingComments);
		this.trailingComments = List.copyOf(trailingComments);
		this.whitespace = whitespace;
		this.hints = hints;
		this.parent = parent;
	}

	/**
	 * Gets the source range covered by this AST node.
	 *
	 * @return the source range of this node
	 */
	public final SourceRange getRange()
		{
		return range;
	}

	/**
	 * Gets the starting position of this AST node.
	 *
	 * @return the start position
	 */
	public final SourcePosition getStartPosition()
		{
		return range.start();
	}

	/**
	 * Gets the ending position of this AST node.
	 *
	 * @return the end position
	 */
	public final SourcePosition getEndPosition()
		{
		return range.end();
	}

	/**
	 * Gets the comments that appear before this node.
	 *
	 * @return an immutable list of leading comments
	 */
	public final List<Comment> getLeadingComments()
		{
		return leadingComments;
	}

	/**
	 * Gets the comments that appear after this node.
	 *
	 * @return an immutable list of trailing comments
	 */
	public final List<Comment> getTrailingComments()
		{
		return trailingComments;
	}

	/**
	 * Gets the whitespace information for this node.
	 *
	 * @return the whitespace information
	 */
	public final WhitespaceInfo getWhitespace()
		{
		return whitespace;
	}

	/**
	 * Gets the formatting hints for this node.
	 *
	 * @return the formatting hints
	 */
	public final FormattingHints getHints()
		{
		return hints;
	}

	/**
	 * Gets the parent node of this AST node.
	 *
	 * @return the parent node, or empty if this is a root node
	 */
	public final Optional<ASTNode> getParent()
		{
		return parent;
	}

	/**
	 * Accepts a visitor for traversal using the Visitor pattern.
	 * This method implements double-dispatch to call the appropriate visit method on the visitor.
	 *
	 * @param <R> the return type of the visitor
	 *
	 * @param <A> the argument type for the visitor
	 *
	 * @param visitor the visitor to accept
	 *
	 * @param arg the argument to pass to the visitor
	 *
	 * @return the result of the visitor operation
	 */
	public abstract <R, A> R accept(ASTVisitor<R, A> visitor, A arg);

	/**
	 * Creates a builder for constructing a modified copy of this AST node.
	 * The builder enables immutable modifications by creating new instances.
	 *
	 * @return a builder initialized with this node's current values
	 */
	public abstract ASTNodeBuilder<? extends ASTNode> toBuilder();

	/**
	 * Gets all child AST nodes of this node.
	 * This method provides access to the tree structure for traversal operations.
	 *
	 * @return an immutable list of child nodes
	 */
	public abstract List<ASTNode> getChildren();

	/**
	 * Creates a copy of this node with updated metadata but same structure.
	 * This is useful for preserving AST structure while updating formatting information.
	 *
	 * @param newRange the new source range
	 *
	 * @param newLeadingComments the new leading comments
	 *
	 * @param newTrailingComments the new trailing comments
	 *
	 * @param newWhitespace the new whitespace information
	 *
	 * @param newHints the new formatting hints
	 *
	 * @param newParent the new parent node
	 *
	 * @return a new AST node with updated metadata
	 */
	protected abstract ASTNode withMetadata(SourceRange newRange, List<Comment> newLeadingComments,
		List<Comment> newTrailingComments, WhitespaceInfo newWhitespace, FormattingHints newHints,
		Optional<ASTNode> newParent);

	/**
	 * Checks if this node has any associated comments.
	 *
	 * @return {@code true} if this node has leading or trailing comments
	 */
	public final boolean hasComments()
		{
		return !leadingComments.isEmpty() || !trailingComments.isEmpty();
	}

	/**
	 * Checks if this node has any child nodes.
	 *
	 * @return {@code true} if this node has child nodes
	 */
	public final boolean hasChildren()
		{
		return !getChildren().isEmpty();
	}

	/**
	 * Checks if this node is a leaf node (no children).
	 *
	 * @return {@code true} if this node has no children
	 */
	public final boolean isLeaf()
		{
		return getChildren().isEmpty();
	}

	@Override
	public boolean equals(Object obj)
		{
		if (this == obj) return true;
		if (obj == null || getClass() != obj.getClass()) return false;

		ASTNode astNode = (ASTNode) obj;
		return Objects.equals(range, astNode.range) &&
			Objects.equals(leadingComments, astNode.leadingComments) &&
			Objects.equals(trailingComments, astNode.trailingComments) &&
			Objects.equals(whitespace, astNode.whitespace) &&
			Objects.equals(hints, astNode.hints);
	}

	@Override
	public int hashCode()
		{
		return Objects.hash(range, leadingComments, trailingComments, whitespace, hints);
	}

	@Override
	public String toString()
		{
		return getClass().getSimpleName() + "[" + range + "]";
	}
}
