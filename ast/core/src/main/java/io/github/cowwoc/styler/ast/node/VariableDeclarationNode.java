package io.github.cowwoc.styler.ast.node;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;
import io.github.cowwoc.styler.ast.ASTNode;
import io.github.cowwoc.styler.ast.Comment;
import io.github.cowwoc.styler.ast.FormattingHints;
import io.github.cowwoc.styler.ast.SourceRange;
import io.github.cowwoc.styler.ast.WhitespaceInfo;
import io.github.cowwoc.styler.ast.visitor.ASTVisitor;
import io.github.cowwoc.styler.ast.builder.ASTNodeBuilder;

import java.util.List;
import java.util.Optional;

/**
 * AST node representing a variable declaration.
 */
public final class VariableDeclarationNode extends ASTNode
	{
	private final String name;
	private final Optional<ASTNode> initializer;

	/**
	 * Creates a variable declaration node.
	 *
	 * @param range the source range
	 * @param leadingComments comments before this node
	 * @param trailingComments comments after this node
	 * @param whitespace whitespace information
	 * @param hints formatting hints
	 * @param parent the parent node
	 * @param name the variable name
	 * @param initializer the variable initializer (empty if uninitialized)
	 * @throws NullPointerException if name or initializer is {@code null}
	 * @throws IllegalArgumentException if name is blank
	 */
	public VariableDeclarationNode(SourceRange range, List<Comment> leadingComments,
		List<Comment> trailingComments, WhitespaceInfo whitespace, FormattingHints hints,
		Optional<ASTNode> parent, String name, Optional<ASTNode> initializer)
			{
		super(range, leadingComments, trailingComments, whitespace, hints, parent);
		requireThat(name, "name").isNotNull().isNotBlank();
		requireThat(initializer, "initializer").isNotNull();
		this.name = name;
		this.initializer = initializer;
	}

	public String getName()
		{
		return name;
		}
	public Optional<ASTNode> getInitializer()
		{
		return initializer;
		}

	@Override
	public <R, A> R accept(ASTVisitor<R, A> visitor, final A arg)
		{
		return visitor.visitVariableDeclaration(this, arg);
	}

	@Override
	public ASTNodeBuilder<VariableDeclarationNode> toBuilder()
		{
		return new Builder().setRange(getRange()).setLeadingComments(getLeadingComments()).
			setTrailingComments(getTrailingComments()).setWhitespace(getWhitespace()).
			setHints(getHints()).setParent(getParent()).setName(name).setInitializer(initializer);
	}

	@Override
	public List<ASTNode> getChildren()
		{
		return initializer.map(List::of).orElse(List.of());
	}

	@Override
	protected ASTNode withMetadata(SourceRange newRange, List<Comment> newLeadingComments,
		List<Comment> newTrailingComments, WhitespaceInfo newWhitespace, FormattingHints newHints,
		Optional<ASTNode> newParent)
			{
		return new VariableDeclarationNode(newRange, newLeadingComments, newTrailingComments,
			newWhitespace, newHints, newParent, name, initializer);
	}

/**
 * Builder for creating {@link VariableDeclarationNode} instances.
 */
	public static final class Builder implements ASTNodeBuilder<VariableDeclarationNode>
		{
		private SourceRange range;
		private List<Comment> leadingComments = List.of();
		private List<Comment> trailingComments = List.of();
		private WhitespaceInfo whitespace = WhitespaceInfo.none();
		private FormattingHints hints = FormattingHints.defaults();
		private Optional<ASTNode> parent = Optional.empty();
		private String name;
		private Optional<ASTNode> initializer = Optional.empty();

		@Override public Builder setRange(SourceRange range)
			{
			this.range = range; return this;
			}
		@Override public Builder setLeadingComments(List<Comment> comments)
			{
			this.leadingComments = List.copyOf(comments); return this;
			}
		@Override public Builder setTrailingComments(List<Comment> comments)
			{
			this.trailingComments = List.copyOf(comments); return this;
			}
		@Override public Builder setWhitespace(WhitespaceInfo whitespace)
			{
			this.whitespace = whitespace; return this;
			}
		@Override public Builder setHints(FormattingHints hints)
			{
			this.hints = hints; return this;
			}
		@Override public Builder setParent(Optional<ASTNode> parent)
			{
			this.parent = parent; return this;
			}
		@Override public Builder addLeadingComment(Comment comment)
			{
			var newComments = new java.util.ArrayList<>(leadingComments);
			newComments.add(comment);
			this.leadingComments = List.copyOf(newComments);
			return this;
			}
		@Override public Builder addTrailingComment(Comment comment)
			{
			var newComments = new java.util.ArrayList<>(trailingComments);
			newComments.add(comment);
			this.trailingComments = List.copyOf(newComments);
			return this;
			}

		/**
		 * Sets the variable name.
		 *
		 * @param name the variable name
		 * @return this builder
		 */
		public Builder setName(String name)
			{
			this.name = name; return this;
			}
		/**
		 * Sets the variable initializer.
		 *
		 * @param initializer the variable initializer (empty if uninitialized)
		 * @return this builder
		 */
		public Builder setInitializer(Optional<ASTNode> initializer)
			{
			this.initializer = initializer; return this;
			}

		@Override
		public VariableDeclarationNode build()
			{
			if (!isValid())
				throw new IllegalStateException("Invalid builder state: " + String.join(", ", getValidationErrors()));
			return new VariableDeclarationNode(range, leadingComments, trailingComments, whitespace, hints, parent,
				name, initializer);
		}

		@Override public boolean isValid()
			{
			return range != null && name != null && !name.isBlank();
			}
		@Override public List<String> getValidationErrors()
			{
			var errors = new java.util.ArrayList<String>();
			if (range == null)
				errors.add("range is required");
			if (name == null || name.isBlank())
				errors.add("name is required and cannot be blank");
			return errors;
			}
	}
}
