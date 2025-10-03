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
 * AST node representing an annotation.
 */
public final class AnnotationNode extends ASTNode
	{
	private final ASTNode name;
	private final List<ASTNode> elements;

	/**
	 * Creates an annotation node.
	 *
	 * @param range the source range
	 * @param leadingComments comments before this node
	 * @param trailingComments comments after this node
	 * @param whitespace whitespace information
	 * @param hints formatting hints
	 * @param parent the parent node
	 * @param name the annotation name
	 * @param elements the annotation elements
	 * @throws NullPointerException if name or elements is {@code null}
	 */
	public AnnotationNode(SourceRange range, List<Comment> leadingComments,
		List<Comment> trailingComments, WhitespaceInfo whitespace, FormattingHints hints,
		Optional<ASTNode> parent, ASTNode name, List<ASTNode> elements)
			{
		super(range, leadingComments, trailingComments, whitespace, hints, parent);
		requireThat(name, "name").isNotNull();
		requireThat(elements, "elements").isNotNull();
		this.name = name;
		this.elements = List.copyOf(elements);
	}

	public ASTNode getName()
		{
		return name;
		}
	public List<ASTNode> getElements()
		{
		return elements;
		}

	@Override
	public <R, A> R accept(ASTVisitor<R, A> visitor, final A arg)
		{
		return visitor.visitAnnotation(this, arg);
	}

	@Override
	public ASTNodeBuilder<AnnotationNode> toBuilder()
		{
		return new Builder().setRange(getRange()).setLeadingComments(getLeadingComments()).
			setTrailingComments(getTrailingComments()).setWhitespace(getWhitespace()).
			setHints(getHints()).setParent(getParent()).setName(name).setElements(elements);
	}

	@Override
	public List<ASTNode> getChildren()
		{
		var children = new java.util.ArrayList<ASTNode>();
		children.add(name);
		children.addAll(elements);
		return List.copyOf(children);
	}

	@Override
	protected ASTNode withMetadata(SourceRange newRange, List<Comment> newLeadingComments,
		List<Comment> newTrailingComments, WhitespaceInfo newWhitespace, FormattingHints newHints,
		Optional<ASTNode> newParent)
			{
		return new AnnotationNode(newRange, newLeadingComments, newTrailingComments,
			newWhitespace, newHints, newParent, name, elements);
	}

/**
 * Builder for creating {@link AnnotationNode} instances.
 */
	public static final class Builder implements ASTNodeBuilder<AnnotationNode>
		{
		private SourceRange range;
		private List<Comment> leadingComments = List.of();
		private List<Comment> trailingComments = List.of();
		private WhitespaceInfo whitespace = WhitespaceInfo.none();
		private FormattingHints hints = FormattingHints.defaults();
		private Optional<ASTNode> parent = Optional.empty();
		private ASTNode name;
		private List<ASTNode> elements = List.of();

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
		 * Sets the annotation name.
		 *
		 * @param name the annotation name
		 * @return this builder
		 */
		public Builder setName(ASTNode name)
			{
			this.name = name; return this;
			}
		/**
		 * Sets the annotation elements.
		 *
		 * @param elements the annotation elements
		 * @return this builder
		 */
		public Builder setElements(List<ASTNode> elements)
			{
			this.elements = List.copyOf(elements); return this;
			}

		@Override
		public AnnotationNode build()
			{
			if (!isValid())
				throw new IllegalStateException("Invalid builder state: " +
					String.join(", ", getValidationErrors()));
			return new AnnotationNode(range, leadingComments, trailingComments, whitespace, hints,
				parent, name, elements);
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
				errors.add("name final is required");
			return errors;
			}
	}
}
