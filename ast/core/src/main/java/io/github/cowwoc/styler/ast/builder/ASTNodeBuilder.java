package io.github.cowwoc.styler.ast.builder;

import io.github.cowwoc.styler.ast.ASTNode;
import io.github.cowwoc.styler.ast.Comment;
import io.github.cowwoc.styler.ast.FormattingHints;
import io.github.cowwoc.styler.ast.SourceRange;
import io.github.cowwoc.styler.ast.WhitespaceInfo;

import java.util.List;
import java.util.Optional;

/**
 * Base interface for AST node builders implementing the Builder pattern.
 * Enables immutable construction and modification of AST nodes with fluent interface.
 * All builders validate their state before constructing nodes to ensure AST integrity.
 *
 * @param <T> the type of AST node this builder constructs
 */
public interface ASTNodeBuilder<T extends ASTNode>
	{
	/**
	 * Sets the source range for the AST node being built.
	 *
	 * @param range the source range
	 *
	 * @return this builder for method chaining
	 */
	ASTNodeBuilder<T> setRange(SourceRange range);

	/**
	 * Sets the leading comments for the AST node being built.
	 *
	 * @param comments the leading comments
	 *
	 * @return this builder for method chaining
	 */
	ASTNodeBuilder<T> setLeadingComments(List<Comment> comments);

	/**
	 * Sets the trailing comments for the AST node being built.
	 *
	 * @param comments the trailing comments
	 *
	 * @return this builder for method chaining
	 */
	ASTNodeBuilder<T> setTrailingComments(List<Comment> comments);

	/**
	 * Sets the whitespace information for the AST node being built.
	 *
	 * @param whitespace the whitespace information
	 *
	 * @return this builder for method chaining
	 */
	ASTNodeBuilder<T> setWhitespace(WhitespaceInfo whitespace);

	/**
	 * Sets the formatting hints for the AST node being built.
	 *
	 * @param hints the formatting hints
	 *
	 * @return this builder for method chaining
	 */
	ASTNodeBuilder<T> setHints(FormattingHints hints);

	/**
	 * Sets the parent node for the AST node being built.
	 *
	 * @param parent the parent node
	 *
	 * @return this builder for method chaining
	 */
	ASTNodeBuilder<T> setParent(Optional<ASTNode> parent);

	/**
	 * Adds a leading comment to the AST node being built.
	 *
	 * @param comment the comment to add
	 *
	 * @return this builder for method chaining
	 */
	ASTNodeBuilder<T> addLeadingComment(Comment comment);

	/**
	 * Adds a trailing comment to the AST node being built.
	 *
	 * @param comment the comment to add
	 *
	 * @return this builder for method chaining
	 */
	ASTNodeBuilder<T> addTrailingComment(Comment comment);

	/**
	 * Validates the current builder state and constructs the AST node.
	 * All required fields must be set before calling this method.
	 *
	 * @return a new immutable AST node
	 *
	 * @throws IllegalStateException if the builder state is invalid
	 */
	T build();

	/**
	 * Validates the current builder state without constructing a node.
	 *
	 * @return {@code true} if the builder state is valid for construction
	 */
	boolean isValid();

	/**
	 * Gets a description of any validation errors in the current builder state.
	 *
	 * @return a list of validation error messages, empty if valid
	 */
	List<String> getValidationErrors();
}
