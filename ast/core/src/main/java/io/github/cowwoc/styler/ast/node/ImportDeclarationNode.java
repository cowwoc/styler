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
 * AST node representing an import declaration.
 */
public final class ImportDeclarationNode extends ASTNode
	{
	private final boolean isStatic;
	private final ASTNode name;
	private final boolean isOnDemand;

	/**
	 * Creates an import declaration node.
	 *
	 * @param range the source range
	 * @param leadingComments comments before this node
	 * @param trailingComments comments after this node
	 * @param whitespace whitespace information
	 * @param hints formatting hints
	 * @param parent the parent node
	 * @param isStatic whether this is a static import
	 * @param name the imported name or wildcard
	 * @param isOnDemand whether this is an on-demand import (uses *)
	 * @throws NullPointerException if name is {@code null}
	 */
	public ImportDeclarationNode(SourceRange range, List<Comment> leadingComments,
		List<Comment> trailingComments, WhitespaceInfo whitespace, FormattingHints hints,
		Optional<ASTNode> parent, boolean isStatic, ASTNode name, boolean isOnDemand)
			{
		super(range, leadingComments, trailingComments, whitespace, hints, parent);
		requireThat(name, "name").isNotNull();
		this.isStatic = isStatic;
		this.name = name;
		this.isOnDemand = isOnDemand;
	}

	/**
	 * Returns whether this is a static import.
	 *
	 * @return {@code true} if this is a static import
	 */
	public boolean isStatic()
		{
		return isStatic;
		}
	public ASTNode getName()
		{
		return name;
		}
	/**
	 * Returns whether this is an on-demand import.
	 *
	 * @return {@code true} if this is an on-demand import (uses *)
	 */
	public boolean isOnDemand()
		{
		return isOnDemand;
		}

	@Override
	public <R, A> R accept(ASTVisitor<R, A> visitor, final A arg)
		{
		return visitor.visitImportDeclaration(this, arg);
	}

	@Override
	public ASTNodeBuilder<ImportDeclarationNode> toBuilder()
		{
		return new Builder().setRange(getRange()).setLeadingComments(getLeadingComments()).
			setTrailingComments(getTrailingComments()).setWhitespace(getWhitespace()).
			setHints(getHints()).setParent(getParent()).setStatic(isStatic).setName(name).setOnDemand(isOnDemand);
	}

	@Override
	public List<ASTNode> getChildren()
		{
		return List.of(name);
	}

	@Override
	protected ASTNode withMetadata(SourceRange newRange, List<Comment> newLeadingComments,
		List<Comment> newTrailingComments, WhitespaceInfo newWhitespace, FormattingHints newHints,
		Optional<ASTNode> newParent)
			{
		return new ImportDeclarationNode(newRange, newLeadingComments, newTrailingComments,
			newWhitespace, newHints, newParent, isStatic, name, isOnDemand);
	}

/**
 * Builder for creating {@link ImportDeclarationNode} instances.
 */
	public static final class Builder implements ASTNodeBuilder<ImportDeclarationNode>
		{
		private SourceRange range;
		private List<Comment> leadingComments = List.of();
		private List<Comment> trailingComments = List.of();
		private WhitespaceInfo whitespace = WhitespaceInfo.none();
		private FormattingHints hints = FormattingHints.defaults();
		private Optional<ASTNode> parent = Optional.empty();
		private boolean isStatic;
		private ASTNode name;
		private boolean isOnDemand;

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
		 * Sets whether this is a static import.
		 *
		 * @param isStatic whether this is a static import
		 * @return this builder
		 */
		public Builder setStatic(boolean isStatic)
			{
			this.isStatic = isStatic; return this;
			}
		/**
		 * Sets the imported name.
		 *
		 * @param name the imported name or wildcard
		 * @return this builder
		 */
		public Builder setName(ASTNode name)
			{
			this.name = name; return this;
			}
		/**
		 * Sets whether this is an on-demand import.
		 *
		 * @param isOnDemand whether this is an on-demand import (uses *)
		 * @return this builder
		 */
		public Builder setOnDemand(boolean isOnDemand)
			{
			this.isOnDemand = isOnDemand; return this;
			}

		@Override
		public ImportDeclarationNode build()
			{
			if (!isValid())
				throw new IllegalStateException("Invalid builder state: " +
					String.join(", ", getValidationErrors()));
			return new ImportDeclarationNode(range, leadingComments, trailingComments, whitespace,
				hints, parent, isStatic, name, isOnDemand);
		}

		@Override public boolean isValid()
			{
			return range != null && name != null;
			}
		@Override public List<String> getValidationErrors()
			{
			var errors = new java.util.ArrayList<String>();
			if (range == null)
				errors.add("range is required");
			if (name == null)
				errors.add("name is required");
			return errors;
			}
	}
}
