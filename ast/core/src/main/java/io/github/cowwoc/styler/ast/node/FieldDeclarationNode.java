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
 * AST node representing a field declaration.
 */
public final class FieldDeclarationNode extends ASTNode
	{
	private final List<ASTNode> modifiers;
	private final ASTNode type;
	private final List<ASTNode> variables;

	/**
	 * Creates a field declaration node.
	 *
	 * @param range the source range
	 * @param leadingComments comments before this node
	 * @param trailingComments comments after this node
	 * @param whitespace whitespace information
	 * @param hints formatting hints
	 * @param parent the parent node
	 * @param modifiers the field modifiers
	 * @param type the field type
	 * @param variables the variable declarators
	 * @throws NullPointerException if modifiers, type, or variables is {@code null}
	 * @throws IllegalArgumentException if variables is empty
	 */
	public FieldDeclarationNode(SourceRange range, List<Comment> leadingComments,
		List<Comment> trailingComments, WhitespaceInfo whitespace, FormattingHints hints,
		Optional<ASTNode> parent, List<ASTNode> modifiers, ASTNode type, List<ASTNode> variables)
			{
		super(range, leadingComments, trailingComments, whitespace, hints, parent);
		requireThat(modifiers, "modifiers").isNotNull();
		requireThat(type, "type").isNotNull();
		requireThat(variables, "variables").isNotNull().isNotEmpty();
		this.modifiers = List.copyOf(modifiers);
		this.type = type;
		this.variables = List.copyOf(variables);
	}

	public List<ASTNode> getModifiers()
		{
		return modifiers;
		}
	public ASTNode getType()
		{
		return type;
		}
	public List<ASTNode> getVariables()
		{
		return variables;
		}

	@Override
	public <R, A> R accept(ASTVisitor<R, A> visitor, A arg)
		{
		return visitor.visitFieldDeclaration(this, arg);
	}

	@Override
	public ASTNodeBuilder<FieldDeclarationNode> toBuilder()
		{
		return new Builder().setRange(getRange()).setLeadingComments(getLeadingComments()).
			setTrailingComments(getTrailingComments()).setWhitespace(getWhitespace()).
			setHints(getHints()).setParent(getParent()).setModifiers(modifiers).setType(type).setVariables(variables);
	}

	@Override
	public List<ASTNode> getChildren()
		{
		var children = new java.util.ArrayList<ASTNode>();
		children.addAll(modifiers);
		children.add(type);
		children.addAll(variables);
		return List.copyOf(children);
	}

	@Override
	protected ASTNode withMetadata(SourceRange newRange, List<Comment> newLeadingComments,
		List<Comment> newTrailingComments, WhitespaceInfo newWhitespace, FormattingHints newHints,
		Optional<ASTNode> newParent)
			{
		return new FieldDeclarationNode(newRange, newLeadingComments, newTrailingComments,
			newWhitespace, newHints, newParent, modifiers, type, variables);
	}

/**
 * Builder for creating {@link FieldDeclarationNode} instances.
 */
	public static final class Builder implements ASTNodeBuilder<FieldDeclarationNode>
		{
		private SourceRange range;
		private List<Comment> leadingComments = List.of();
		private List<Comment> trailingComments = List.of();
		private WhitespaceInfo whitespace = WhitespaceInfo.none();
		private FormattingHints hints = FormattingHints.defaults();
		private Optional<ASTNode> parent = Optional.empty();
		private List<ASTNode> modifiers = List.of();
		private ASTNode type;
		private List<ASTNode> variables = List.of();

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
		 * Sets the field modifiers.
		 *
		 * @param modifiers the field modifiers
		 * @return this builder
		 */
		public Builder setModifiers(List<ASTNode> modifiers)
			{
			this.modifiers = List.copyOf(modifiers); return this;
			}
		/**
		 * Sets the field type.
		 *
		 * @param type the field type
		 * @return this builder
		 */
		public Builder setType(ASTNode type)
			{
			this.type = type; return this;
			}
		/**
		 * Sets the variable declarators.
		 *
		 * @param variables the variable declarators
		 * @return this builder
		 */
		public Builder setVariables(List<ASTNode> variables)
			{
			this.variables = List.copyOf(variables); return this;
			}

		@Override
		public FieldDeclarationNode build()
			{
			if (!isValid())
				throw new IllegalStateException("Invalid builder state: " +
					String.join(", ", getValidationErrors()));
			return new FieldDeclarationNode(range, leadingComments, trailingComments, whitespace,
				hints, parent, modifiers, type, variables);
		}

		@Override public boolean isValid()
			{
			return range != null && type != null && !variables.isEmpty();
			}
		@Override public List<String> getValidationErrors()
			{
			var errors = new java.util.ArrayList<String>();
			if (range == null)
				errors.add("range is required");
			if (type == null)
				errors.add("type is required");
			if (variables.isEmpty())
				errors.add("variables cannot be empty");
			return errors;
			}
	}
}
