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
 * AST node representing a package declaration.
 */
public final class PackageDeclarationNode extends ASTNode
	{
	private final List<ASTNode> annotations;
	private final ASTNode name;

	/**
	 * Creates a package declaration node.
	 *
	 * @param range the source range
	 * @param leadingComments comments before this node
	 * @param trailingComments comments after this node
	 * @param whitespace whitespace information
	 * @param hints formatting hints
	 * @param parent the parent node
	 * @param annotations package annotations
	 * @param name the package name
	 * @throws NullPointerException if annotations or name is {@code null}
	 */
	public PackageDeclarationNode(SourceRange range, List<Comment> leadingComments,
		List<Comment> trailingComments, WhitespaceInfo whitespace, FormattingHints hints,
		Optional<ASTNode> parent, List<ASTNode> annotations, ASTNode name)
			{
		super(range, leadingComments, trailingComments, whitespace, hints, parent);
		requireThat(annotations, "annotations").isNotNull();
		requireThat(name, "name").isNotNull();
		this.annotations = List.copyOf(annotations);
		this.name = name;
	}

	public List<ASTNode> getAnnotations()
		{
		return annotations;
		}
	public ASTNode getName()
		{
		return name;
		}

	@Override
	public <R, A> R accept(ASTVisitor<R, A> visitor, final A arg)
		{
		return visitor.visitPackageDeclaration(this, arg);
	}

	@Override
	public ASTNodeBuilder<PackageDeclarationNode> toBuilder()
		{
		return new Builder().setRange(getRange()).setLeadingComments(getLeadingComments()).
			setTrailingComments(getTrailingComments()).setWhitespace(getWhitespace()).
			setHints(getHints()).setParent(getParent()).setAnnotations(annotations).setName(name);
	}

	@Override
	public List<ASTNode> getChildren()
		{
		var children = new java.util.ArrayList<ASTNode>();
		children.addAll(annotations);
		children.add(name);
		return List.copyOf(children);
	}

	@Override
	protected ASTNode withMetadata(SourceRange newRange, List<Comment> newLeadingComments,
		List<Comment> newTrailingComments, WhitespaceInfo newWhitespace, FormattingHints newHints,
		Optional<ASTNode> newParent)
			{
		return new PackageDeclarationNode(newRange, newLeadingComments, newTrailingComments,
			newWhitespace, newHints, newParent, annotations, name);
	}

/**
 * Builder for creating {@link PackageDeclarationNode} instances.
 */
	public static final class Builder implements ASTNodeBuilder<PackageDeclarationNode>
		{
		private SourceRange range;
		private List<Comment> leadingComments = List.of();
		private List<Comment> trailingComments = List.of();
		private WhitespaceInfo whitespace = WhitespaceInfo.none();
		private FormattingHints hints = FormattingHints.defaults();
		private Optional<ASTNode> parent = Optional.empty();
		private List<ASTNode> annotations = List.of();
		private ASTNode name;

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
		 * Sets the package annotations.
		 *
		 * @param annotations the package annotations
		 * @return this builder
		 */
		public Builder setAnnotations(List<ASTNode> annotations)
			{
			this.annotations = List.copyOf(annotations); return this;
			}
		/**
		 * Sets the package name.
		 *
		 * @param name the package name
		 * @return this builder
		 */
		public Builder setName(ASTNode name)
			{
			this.name = name; return this;
			}

		@Override
		public PackageDeclarationNode build()
			{
			if (!isValid())
				throw new IllegalStateException("Invalid builder state: " +
					String.join(", ", getValidationErrors()));
			return new PackageDeclarationNode(range, leadingComments, trailingComments, whitespace,
				hints, parent, annotations, name);
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
