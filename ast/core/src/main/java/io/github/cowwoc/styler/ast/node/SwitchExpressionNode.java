package io.github.cowwoc.styler.ast.node;

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
 * AST node representing a switch expression.
 *
 * @since 1.0
 */
public final class SwitchExpressionNode extends ASTNode
{
	private final List<ASTNode> children;

	/**
	 * Creates a switch expression node.
	 *
	 * @param range the source range
	 * @param leadingComments comments before this node
	 * @param trailingComments comments after this node
	 * @param whitespace whitespace information
	 * @param hints formatting hints
	 * @param parent the parent node
	 * @param children the child nodes
	 */
	public SwitchExpressionNode(SourceRange range, List<Comment> leadingComments,
		List<Comment> trailingComments, WhitespaceInfo whitespace, FormattingHints hints,
		Optional<ASTNode> parent, List<ASTNode> children)
	{
		super(range, leadingComments, trailingComments, whitespace, hints, parent);
		this.children = List.copyOf(children);
	}

	@Override
	public <R, A> R accept(ASTVisitor<R, A> visitor, A arg)
	{
		return visitor.visitSwitchExpression(this, arg);
	}

	@Override
	public ASTNodeBuilder<SwitchExpressionNode> toBuilder()
	{
		return new Builder().
			setRange(getRange()).
			setLeadingComments(getLeadingComments()).
			setTrailingComments(getTrailingComments()).
			setWhitespace(getWhitespace()).
			setHints(getHints()).
			setParent(getParent()).
			setChildren(children);
	}

	@Override
	public List<ASTNode> getChildren()
	{
		return children;
	}

	@Override
	protected ASTNode withMetadata(SourceRange newRange, List<Comment> newLeadingComments,
		List<Comment> newTrailingComments, WhitespaceInfo newWhitespace, FormattingHints newHints,
		Optional<ASTNode> newParent)
	{
		return new SwitchExpressionNode(newRange, newLeadingComments, newTrailingComments,
			newWhitespace, newHints, newParent, children);
	}

	/**
	 * Builder for creating {@link SwitchExpressionNode} instances.
	 */
	public static final class Builder implements ASTNodeBuilder<SwitchExpressionNode>
	{
		private SourceRange range;
		private List<Comment> leadingComments = List.of();
		private List<Comment> trailingComments = List.of();
		private WhitespaceInfo whitespace = WhitespaceInfo.none();
		private FormattingHints hints = FormattingHints.defaults();
		private Optional<ASTNode> parent = Optional.empty();
		private List<ASTNode> children = List.of();

		@Override
		public Builder setRange(SourceRange range)
		{
			this.range = range;
			return this;
		}

		@Override
		public Builder setLeadingComments(List<Comment> comments)
		{
			this.leadingComments = List.copyOf(comments);
			return this;
		}

		@Override
		public Builder setTrailingComments(List<Comment> comments)
		{
			this.trailingComments = List.copyOf(comments);
			return this;
		}

		@Override
		public Builder setWhitespace(WhitespaceInfo whitespace)
		{
			this.whitespace = whitespace;
			return this;
		}

		@Override
		public Builder setHints(FormattingHints hints)
		{
			this.hints = hints;
			return this;
		}

		@Override
		public Builder setParent(Optional<ASTNode> parent)
		{
			this.parent = parent;
			return this;
		}

		@Override
		public Builder addLeadingComment(Comment comment)
		{
			var newComments = new java.util.ArrayList<>(leadingComments);
			newComments.add(comment);
			this.leadingComments = List.copyOf(newComments);
			return this;
		}

		@Override
		public Builder addTrailingComment(Comment comment)
		{
			var newComments = new java.util.ArrayList<>(trailingComments);
			newComments.add(comment);
			this.trailingComments = List.copyOf(newComments);
			return this;
		}

		/**
		 * Sets the child nodes.
		 *
		 * @param children the child nodes
		 * @return this builder
		 */
		public Builder setChildren(List<ASTNode> children)
		{
			this.children = List.copyOf(children);
			return this;
		}

		@Override
		public SwitchExpressionNode build()
		{
			if (!isValid())
			{
				throw new IllegalStateException("Invalid builder state: " +
					String.join(", ", getValidationErrors()));
			}
			return new SwitchExpressionNode(range, leadingComments, trailingComments, whitespace,
				hints, parent, children);
		}

		@Override
		public boolean isValid()
		{
			return range != null;
		}

		@Override
		public List<String> getValidationErrors()
		{
			var errors = new java.util.ArrayList<String>();
			if (range == null)
			{
				errors.add("range is required");
			}
			return errors;
		}
	}
}
