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
 * AST node representing a block statement.
 */
public final class BlockStatementNode extends ASTNode
	{
	private final List<ASTNode> statements;

	/**
	 * Creates a block statement node.
	 *
	 * @param range the source range
	 * @param leadingComments comments before this node
	 * @param trailingComments comments after this node
	 * @param whitespace whitespace information
	 * @param hints formatting hints
	 * @param parent the parent node
	 * @param statements the statements in this block
	 * @throws NullPointerException if statements is {@code null}
	 */
	public BlockStatementNode(SourceRange range, List<Comment> leadingComments,
		List<Comment> trailingComments, WhitespaceInfo whitespace, FormattingHints hints,
		Optional<ASTNode> parent, List<ASTNode> statements)
			{
		super(range, leadingComments, trailingComments, whitespace, hints, parent);
		requireThat(statements, "statements").isNotNull();
		this.statements = List.copyOf(statements);
	}

	public List<ASTNode> getStatements()
		{
		return statements;
		}

	@Override
	public <R, A> R accept(ASTVisitor<R, A> visitor, A arg)
		{
		return visitor.visitBlockStatement(this, arg);
	}

	@Override
	public ASTNodeBuilder<BlockStatementNode> toBuilder()
		{
		return new Builder().setRange(getRange()).setLeadingComments(getLeadingComments()).
			setTrailingComments(getTrailingComments()).setWhitespace(getWhitespace()).
			setHints(getHints()).setParent(getParent()).setStatements(statements);
	}

	@Override
	public List<ASTNode> getChildren()
		{
		return statements;
	}

	@Override
	protected ASTNode withMetadata(SourceRange newRange, List<Comment> newLeadingComments,
		List<Comment> newTrailingComments, WhitespaceInfo newWhitespace, FormattingHints newHints,
		Optional<ASTNode> newParent)
			{
		return new BlockStatementNode(newRange, newLeadingComments, newTrailingComments,
			newWhitespace, newHints, newParent, statements);
	}

/**
 * Builder for creating {@link BlockStatementNode} instances.
 */
	public static final class Builder implements ASTNodeBuilder<BlockStatementNode>
		{
		private SourceRange range;
		private List<Comment> leadingComments = List.of();
		private List<Comment> trailingComments = List.of();
		private WhitespaceInfo whitespace = WhitespaceInfo.none();
		private FormattingHints hints = FormattingHints.defaults();
		private Optional<ASTNode> parent = Optional.empty();
		private List<ASTNode> statements = List.of();

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
		 * Sets the statements in this block.
		 *
		 * @param statements the statements
		 * @return this builder
		 */
		public Builder setStatements(List<ASTNode> statements)
			{
			this.statements = List.copyOf(statements); return this;
			}

		@Override
		public BlockStatementNode build()
			{
			if (!isValid())
				throw new IllegalStateException("Invalid builder state: " +
					String.join(", ", getValidationErrors()));
			return new BlockStatementNode(range, leadingComments, trailingComments, whitespace,
				hints, parent, statements);
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
