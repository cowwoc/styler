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
 * AST node representing a qualified name (e.g., "java.util.List").
 */
public final class QualifiedNameNode extends ASTNode
	{
	private final List<String> parts;

	/**
	 * Creates a qualified name node.
	 *
	 * @param range the source range
	 * @param leadingComments comments before this node
	 * @param trailingComments comments after this node
	 * @param whitespace whitespace information
	 * @param hints formatting hints
	 * @param parent the parent node
	 * @param parts the name parts (e.g., ["java", "util", "List"])
	 * @throws NullPointerException if parts is {@code null} or contains {@code null} elements
	 * @throws IllegalArgumentException if parts is empty or contains blank strings
	 */
	public QualifiedNameNode(SourceRange range, List<Comment> leadingComments,
		List<Comment> trailingComments, WhitespaceInfo whitespace, FormattingHints hints,
		Optional<ASTNode> parent, List<String> parts)
			{
		super(range, leadingComments, trailingComments, whitespace, hints, parent);
		requireThat(parts, "parts").isNotNull().isNotEmpty();
		requireThat(parts, "parts").doesNotContain(null);
		requireThat(parts.stream().noneMatch(String::isBlank), "parts").isTrue();
		this.parts = List.copyOf(parts);
	}

	public List<String> getParts()
		{
		return parts;
		}
	/**
	 * Returns the dot-separated qualified name.
	 *
	 * @return the qualified name (e.g., "java.util.{@code List}")
	 */
	public String getQualifiedName()
		{
		return String.join(".", parts);
		}

	@Override
	public <R, A> R accept(ASTVisitor<R, A> visitor, A arg)
		{
		return visitor.visitQualifiedName(this, arg);
		}

	@Override
	public ASTNodeBuilder<QualifiedNameNode> toBuilder()
		{
		return new Builder().setRange(getRange()).setLeadingComments(getLeadingComments()).
			setTrailingComments(getTrailingComments()).setWhitespace(getWhitespace()).
			setHints(getHints()).setParent(getParent()).setParts(parts);
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
		return new QualifiedNameNode(newRange, newLeadingComments, newTrailingComments,
			newWhitespace, newHints, newParent, parts);
	}

/**
 * Builder for creating {@link QualifiedNameNode} instances.
 */
	public static final class Builder implements ASTNodeBuilder<QualifiedNameNode>
		{
		private SourceRange range;
		private List<Comment> leadingComments = List.of();
		private List<Comment> trailingComments = List.of();
		private WhitespaceInfo whitespace = WhitespaceInfo.none();
		private FormattingHints hints = FormattingHints.defaults();
		private Optional<ASTNode> parent = Optional.empty();
		private List<String> parts = List.of();

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
		 * Sets the name parts.
		 *
		 * @param parts the name parts (e.g., ["java", "util", "List"])
		 * @return this builder
		 */
		public Builder setParts(List<String> parts)
			{
			this.parts = List.copyOf(parts); return this;
			}

		@Override
		public QualifiedNameNode build()
			{
			if (!isValid())
				throw new IllegalStateException("Invalid builder state: " +
					String.join(", ", getValidationErrors()));
			return new QualifiedNameNode(range, leadingComments, trailingComments, whitespace, hints, parent, parts);
		}

		@Override public boolean isValid()
			{
			return range != null && !parts.isEmpty();
			}
		@Override public List<String> getValidationErrors()
			{
			var errors = new java.util.ArrayList<String>();
			if (range == null)
				errors.add("range is required");
			if (parts.isEmpty())
				errors.add("parts cannot be empty");
			return errors;
			}
	}
}
