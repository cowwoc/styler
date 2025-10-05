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
 * AST node representing a primitive type.
 */
public final class PrimitiveTypeNode extends ASTNode
	{
	private final String type;

	/**
	 * Creates a primitive type node.
	 *
	 * @param range the source range
	 * @param leadingComments comments before this node
	 * @param trailingComments comments after this node
	 * @param whitespace whitespace information
	 * @param hints formatting hints
	 * @param parent the parent node
	 * @param type the primitive type keyword
	 * @throws NullPointerException if type is {@code null}
	 * @throws IllegalArgumentException if type is blank
	 */
	public PrimitiveTypeNode(SourceRange range, List<Comment> leadingComments,
		List<Comment> trailingComments, WhitespaceInfo whitespace, FormattingHints hints,
		Optional<ASTNode> parent, String type)
			{
		super(range, leadingComments, trailingComments, whitespace, hints, parent);
		requireThat(type, "type").isNotNull().isNotBlank();
		this.type = type;
	}

	public String getType()
		{
		return type;
		}

	@Override
	public <R, A> R accept(ASTVisitor<R, A> visitor, A arg)
		{
		return visitor.visitPrimitiveType(this, arg);
	}

	@Override
	public ASTNodeBuilder<PrimitiveTypeNode> toBuilder()
		{
		return new Builder().setRange(getRange()).setLeadingComments(getLeadingComments()).
			setTrailingComments(getTrailingComments()).setWhitespace(getWhitespace()).
			setHints(getHints()).setParent(getParent()).setType(type);
	}

	@Override
	public List<ASTNode> getChildren()
		{
		return List.of();
	}

	@Override
	protected ASTNode withMetadata(SourceRange newRange, List<Comment> newLeadingComments,
		List<Comment> newTrailingComments, WhitespaceInfo newWhitespace, FormattingHints newHints,
		Optional<ASTNode> newParent)
			{
		return new PrimitiveTypeNode(newRange, newLeadingComments, newTrailingComments,
			newWhitespace, newHints, newParent, type);
	}

/**
 * Builder for creating {@link PrimitiveTypeNode} instances.
 */
	public static final class Builder implements ASTNodeBuilder<PrimitiveTypeNode>
		{
		private SourceRange range;
		private List<Comment> leadingComments = List.of();
		private List<Comment> trailingComments = List.of();
		private WhitespaceInfo whitespace = WhitespaceInfo.none();
		private FormattingHints hints = FormattingHints.defaults();
		private Optional<ASTNode> parent = Optional.empty();
		private String type;

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
		 * Sets the primitive type keyword.
		 *
		 * @param type the primitive type keyword
		 * @return this builder
		 */
		public Builder setType(String type)
			{
			this.type = type; return this;
			}

		@Override
		public PrimitiveTypeNode build()
			{
			if (!isValid())
				throw new IllegalStateException("Invalid builder state: " +
					String.join(", ", getValidationErrors()));
			return new PrimitiveTypeNode(range, leadingComments, trailingComments, whitespace, hints, parent, type);
		}

		@Override public boolean isValid()
			{
			return range != null && type != null && !type.isBlank();
			}
		@Override public List<String> getValidationErrors()
			{
			var errors = new java.util.ArrayList<String>();
			if (range == null)
				errors.add("range is required");
			if (type == null || type.isBlank())
				errors.add("type is required and cannot be blank");
			return errors;
			}
	}
}
