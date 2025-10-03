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
 * AST node representing a for statement.
 */
public final class ForStatementNode extends ASTNode
	{
	private final List<ASTNode> initializers;
	private final Optional<ASTNode> condition;
	private final List<ASTNode> updaters;
	private final ASTNode body;

	/**
	 * Creates a for statement node.
	 *
	 * @param range the source range
	 * @param leadingComments comments before this node
	 * @param trailingComments comments after this node
	 * @param whitespace whitespace information
	 * @param hints formatting hints
	 * @param parent the parent node
	 * @param initializers the loop initializers
	 * @param condition the loop condition (empty for infinite loops)
	 * @param updaters the loop updaters
	 * @param body the loop body statement
	 * @throws NullPointerException if initializers, condition, updaters, or body is {@code null}
	 */
	public ForStatementNode(SourceRange range, List<Comment> leadingComments,
		List<Comment> trailingComments, WhitespaceInfo whitespace, FormattingHints hints,
		Optional<ASTNode> parent, List<ASTNode> initializers, Optional<ASTNode> condition,
		List<ASTNode> updaters, ASTNode body)
			{
		super(range, leadingComments, trailingComments, whitespace, hints, parent);
		requireThat(initializers, "initializers").isNotNull();
		requireThat(condition, "condition").isNotNull();
		requireThat(updaters, "updaters").isNotNull();
		requireThat(body, "body").isNotNull();
		this.initializers = List.copyOf(initializers);
		this.condition = condition;
		this.updaters = List.copyOf(updaters);
		this.body = body;
	}

	public List<ASTNode> getInitializers()
		{
		return initializers;
		}
	public Optional<ASTNode> getCondition()
		{
		return condition;
		}
	public List<ASTNode> getUpdaters()
		{
		return updaters;
		}
	public ASTNode getBody()
		{
		return body;
		}

	@Override
	public <R, A> R accept(ASTVisitor<R, A> visitor, final A arg)
		{
		return visitor.visitForStatement(this, arg);
		}

	@Override
	public ASTNodeBuilder<ForStatementNode> toBuilder()
		{
		return new Builder().setRange(getRange()).setLeadingComments(getLeadingComments()).
			setTrailingComments(getTrailingComments()).setWhitespace(getWhitespace()).
			setHints(getHints()).setParent(getParent()).setInitializers(initializers).
			setCondition(condition).setUpdaters(updaters).setBody(body);
	}

	@Override
	public List<ASTNode> getChildren()
		{
		var children = new java.util.ArrayList<ASTNode>();
		children.addAll(initializers);
		condition.ifPresent(children::add);
		children.addAll(updaters);
		children.add(body);
		return List.copyOf(children);
	}

	@Override
	protected ASTNode withMetadata(SourceRange newRange, List<Comment> newLeadingComments,
		List<Comment> newTrailingComments, WhitespaceInfo newWhitespace, FormattingHints newHints,
		Optional<ASTNode> newParent)
			{
		return new ForStatementNode(newRange, newLeadingComments, newTrailingComments,
			newWhitespace, newHints, newParent, initializers, condition, updaters, body);
	}

/**
 * Builder for creating {@link ForStatementNode} instances.
 */
	public static final class Builder implements ASTNodeBuilder<ForStatementNode>
		{
		private SourceRange range;
		private List<Comment> leadingComments = List.of();
		private List<Comment> trailingComments = List.of();
		private WhitespaceInfo whitespace = WhitespaceInfo.none();
		private FormattingHints hints = FormattingHints.defaults();
		private Optional<ASTNode> parent = Optional.empty();
		private List<ASTNode> initializers = List.of();
		private Optional<ASTNode> condition = Optional.empty();
		private List<ASTNode> updaters = List.of();
		private ASTNode body;

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
		 * Sets the loop initializers.
		 *
		 * @param initializers the loop initializers
		 * @return this builder
		 */
		public Builder setInitializers(List<ASTNode> initializers)
			{
			this.initializers = List.copyOf(initializers); return this;
			}
		/**
		 * Sets the loop condition.
		 *
		 * @param condition the loop condition (empty for infinite loops)
		 * @return this builder
		 */
		public Builder setCondition(Optional<ASTNode> condition)
			{
			this.condition = condition; return this;
			}
		/**
		 * Sets the loop updaters.
		 *
		 * @param updaters the loop updaters
		 * @return this builder
		 */
		public Builder setUpdaters(List<ASTNode> updaters)
			{
			this.updaters = List.copyOf(updaters); return this;
			}
		/**
		 * Sets the loop body statement.
		 *
		 * @param body the loop body statement
		 * @return this builder
		 */
		public Builder setBody(ASTNode body)
			{
			this.body = body; return this;
			}

		@Override
		public ForStatementNode build()
			{
			if (!isValid())
				throw new IllegalStateException("Invalid builder state: " +
					String.join(", ", getValidationErrors()));
			return new ForStatementNode(range, leadingComments, trailingComments, whitespace,
				hints, parent, initializers, condition, updaters, body);
		}

		@Override public boolean isValid()
			{
			return range != null && body != null;
			}
		@Override public List<String> getValidationErrors()
			{
			var errors = new java.util.ArrayList<String>();
			if (range == null)
				errors.add("range is required");
			if (body == null)
				errors.add("body is required");
			return errors;
			}
	}
}
