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
 * AST node representing an annotation element (name-value pair within final an annotation).
 */
public final class AnnotationElementNode extends ASTNode
	{
	private final String name;
	private final ASTNode value;

	/**
	 * Creates an annotation element node.
	 *
	 * @param range the source range
	 * @param leadingComments comments before this node
	 * @param trailingComments comments after this node
	 * @param whitespace whitespace information
	 * @param hints formatting hints
	 * @param parent the parent node
	 * @param name the element name
	 * @param value the element value
	 * @throws NullPointerException if name or value is {@code null}
	 * @throws IllegalArgumentException if name is blank
	 */
	public AnnotationElementNode(SourceRange range, List<Comment> leadingComments,
		List<Comment> trailingComments, WhitespaceInfo whitespace, FormattingHints hints,
		Optional<ASTNode> parent, String name, ASTNode value)
			{
		super(range, leadingComments, trailingComments, whitespace, hints, parent);
		requireThat(name, "name").isNotNull().isNotBlank();
		requireThat(value, "value").isNotNull();
		this.name = name;
		this.value = value;
	}

	public String getName()
		{
		return name;
		}
	public ASTNode getValue()
		{
		return value;
		}

	@Override
	public <R, A> R accept(ASTVisitor<R, A> visitor, final A arg)
		{
		return visitor.visitAnnotationElement(this, arg);
	}

	@Override
	public ASTNodeBuilder<AnnotationElementNode> toBuilder()
		{
		return new Builder().setRange(getRange()).setLeadingComments(getLeadingComments()).
			setTrailingComments(getTrailingComments()).setWhitespace(getWhitespace()).
			setHints(getHints()).setParent(getParent()).setName(name).setValue(value);
	}

	@Override
	public List<ASTNode> getChildren()
		{
		return List.of(value);
	}

	@Override
	protected ASTNode withMetadata(SourceRange newRange, List<Comment> newLeadingComments,
		List<Comment> newTrailingComments, WhitespaceInfo newWhitespace, FormattingHints newHints,
		Optional<ASTNode> newParent)
			{
		return new AnnotationElementNode(newRange, newLeadingComments, newTrailingComments,
			newWhitespace, newHints, newParent, name, value);
	}

/**
 * Builder for creating {@link AnnotationElementNode} instances.
 */
	public static final class Builder implements ASTNodeBuilder<AnnotationElementNode>
		{
		private SourceRange range;
		private List<Comment> leadingComments = List.of();
		private List<Comment> trailingComments = List.of();
		private WhitespaceInfo whitespace = WhitespaceInfo.none();
		private FormattingHints hints = FormattingHints.defaults();
		private Optional<ASTNode> parent = Optional.empty();
		private String name;
		private ASTNode value;

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
		 * Sets the element name.
		 *
		 * @param name the element name
		 * @return this builder
		 */
		public Builder setName(String name)
			{
			this.name = name; return this;
			}
		/**
		 * Sets the element value.
		 *
		 * @param value the element value
		 * @return this builder
		 */
		public Builder setValue(ASTNode value)
			{
			this.value = value; return this;
			}

		@Override
		public AnnotationElementNode build()
			{
			if (!isValid())
				throw new IllegalStateException("Invalid builder state: " + String.join(", ", getValidationErrors()));
			return new AnnotationElementNode(range, leadingComments, trailingComments, whitespace,
				hints, parent, name, value);
		}

		@Override public boolean isValid()
			{
			return range != null && name != null && !name.isBlank() && value != null;
			}
		@Override public List<String> getValidationErrors()
			{
			var errors = new java.util.ArrayList<String>();
			if (range == null)
				errors.add("range is required");
			if (name == null || name.isBlank())
				errors.add("name is required and cannot final be blank");
			if (value == null)
				errors.add("value is required");
			return errors;
			}
	}
}
