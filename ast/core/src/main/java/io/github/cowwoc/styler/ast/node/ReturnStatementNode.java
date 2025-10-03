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
 * AST node representing a return statement.
 */
public final class ReturnStatementNode extends ASTNode
	{
	private final Optional<ASTNode> expression;

	/**
	 * Creates a return statement node.
	 *
	 * @param range the source range
	 * @param leadingComments comments before this node
	 * @param trailingComments comments after this node
	 * @param whitespace whitespace information
	 * @param hints formatting hints
	 * @param parent the parent node
	 * @param expression the return expression (empty for void returns)
	 * @throws NullPointerException if expression is {@code null}
	 */
	public ReturnStatementNode(SourceRange range, List<Comment> leadingComments,
		List<Comment> trailingComments, WhitespaceInfo whitespace, FormattingHints hints,
		Optional<ASTNode> parent, Optional<ASTNode> expression)
			{
		super(range, leadingComments, trailingComments, whitespace, hints, parent);
		requireThat(expression, "expression").isNotNull();
		this.expression = expression;
	}

	public Optional<ASTNode> getExpression()
		{
		return expression;
		}

	@Override
	public <R, A> R accept(ASTVisitor<R, A> visitor, final A arg)
		{
		return visitor.visitReturnStatement(this, arg);
	}

	@Override
	public ASTNodeBuilder<ReturnStatementNode> toBuilder()
		{
		return new Builder().setRange(getRange()).setLeadingComments(getLeadingComments()).
			setTrailingComments(getTrailingComments()).setWhitespace(getWhitespace()).
			setHints(getHints()).setParent(getParent()).setExpression(expression);
	}

	@Override
	public List<ASTNode> getChildren()
		{
		return expression.map(List::of).orElse(List.of());
	}

	@Override
	protected ASTNode withMetadata(SourceRange newRange, List<Comment> newLeadingComments,
		List<Comment> newTrailingComments, WhitespaceInfo newWhitespace, FormattingHints newHints,
		Optional<ASTNode> newParent)
			{
		return new ReturnStatementNode(newRange, newLeadingComments, newTrailingComments,
			newWhitespace, newHints, newParent, expression);
	}

/**
 * Builder for creating {@link ReturnStatementNode} instances.
 */
	public static final class Builder implements ASTNodeBuilder<ReturnStatementNode>
		{
		private SourceRange range;
		private List<Comment> leadingComments = List.of();
		private List<Comment> trailingComments = List.of();
		private WhitespaceInfo whitespace = WhitespaceInfo.none();
		private FormattingHints hints = FormattingHints.defaults();
		private Optional<ASTNode> parent = Optional.empty();
		private Optional<ASTNode> expression = Optional.empty();

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
		 * Sets the return expression.
		 *
		 * @param expression the return expression (empty for void returns)
		 * @return this builder
		 */
		public Builder setExpression(Optional<ASTNode> expression)
			{
			this.expression = expression; return this;
			}

		@Override
		public ReturnStatementNode build()
			{
			if (!isValid())
				throw new IllegalStateException("Invalid builder state: " +
					String.join(", ", getValidationErrors()));
			return new ReturnStatementNode(range, leadingComments, trailingComments, whitespace,
				hints, parent, expression);
		}

		@Override public boolean isValid()
			{
			return range != null;
			}
		@Override public List<String> getValidationErrors()
			{
			var errors = new java.util.ArrayList<String>();
			if (range == null)
				errors.add("range is required");
			return errors;
			}
	}
}
