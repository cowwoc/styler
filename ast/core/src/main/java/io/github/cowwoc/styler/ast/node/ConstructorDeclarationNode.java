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
 * AST node representing a constructor declaration.
 */
public final class ConstructorDeclarationNode extends ASTNode
	{
	private final List<ASTNode> modifiers;
	private final List<ASTNode> typeParameters;
	private final String name;
	private final List<ASTNode> parameters;
	private final List<ASTNode> thrownExceptions;
	private final ASTNode body;

	/**
	 * Creates a constructor declaration node.
	 *
	 * @param range the source range
	 * @param leadingComments comments before this node
	 * @param trailingComments comments after this node
	 * @param whitespace whitespace information
	 * @param hints formatting hints
	 * @param parent the parent node
	 * @param modifiers constructor modifiers (public, private, etc.)
	 * @param typeParameters generic type parameters
	 * @param name the constructor name
	 * @param parameters constructor parameters
	 * @param thrownExceptions exceptions thrown by this constructor
	 * @param body the constructor body
	  * @throws NullPointerException if {@code typeParameters} is null
	  * @throws NullPointerException if {@code thrownExceptions} is null
	  * @throws NullPointerException if {@code parameters} is null
	  * @throws NullPointerException if {@code name} is null
	  * @throws NullPointerException if {@code modifiers} is null
	  * @throws NullPointerException if {@code body} is null
	 */
	public ConstructorDeclarationNode(SourceRange range, List<Comment> leadingComments,
		List<Comment> trailingComments, WhitespaceInfo whitespace, FormattingHints hints,
		Optional<ASTNode> parent, List<ASTNode> modifiers, List<ASTNode> typeParameters,
		String name, List<ASTNode> parameters, List<ASTNode> thrownExceptions, ASTNode body)
			{
		super(range, leadingComments, trailingComments, whitespace, hints, parent);
		requireThat(modifiers, "modifiers").isNotNull();
		requireThat(typeParameters, "typeParameters").isNotNull();
		requireThat(name, "name").isNotNull().isNotBlank();
		requireThat(parameters, "parameters").isNotNull();
		requireThat(thrownExceptions, "thrownExceptions").isNotNull();
		requireThat(body, "body").isNotNull();
		this.modifiers = List.copyOf(modifiers);
		this.typeParameters = List.copyOf(typeParameters);
		this.name = name;
		this.parameters = List.copyOf(parameters);
		this.thrownExceptions = List.copyOf(thrownExceptions);
		this.body = body;
	}

	public List<ASTNode> getModifiers()
		{
		return modifiers;
		}
	public List<ASTNode> getTypeParameters()
		{
		return typeParameters;
		}
	public String getName()
		{
		return name;
		}
	public List<ASTNode> getParameters()
		{
		return parameters;
		}
	public List<ASTNode> getThrownExceptions()
		{
		return thrownExceptions;
		}
	public ASTNode getBody()
		{
		return body;
		}

	@Override
	public <R, A> R accept(ASTVisitor<R, A> visitor, A arg)
		{
		return visitor.visitConstructorDeclaration(this, arg);
	}

	@Override
	public ASTNodeBuilder<ConstructorDeclarationNode> toBuilder()
		{
		return new Builder().setRange(getRange()).setLeadingComments(getLeadingComments()).
			setTrailingComments(getTrailingComments()).setWhitespace(getWhitespace()).
			setHints(getHints()).setParent(getParent()).setModifiers(modifiers).setTypeParameters(typeParameters).
			setName(name).setParameters(parameters).setThrownExceptions(thrownExceptions).setBody(body);
	}

	@Override
	public List<ASTNode> getChildren()
		{
		var children = new java.util.ArrayList<ASTNode>();
		children.addAll(modifiers);
		children.addAll(typeParameters);
		children.addAll(parameters);
		children.addAll(thrownExceptions);
		children.add(body);
		return List.copyOf(children);
	}

	@Override
	protected ASTNode withMetadata(SourceRange newRange, List<Comment> newLeadingComments,
		List<Comment> newTrailingComments, WhitespaceInfo newWhitespace, FormattingHints newHints,
		Optional<ASTNode> newParent)
			{
		return new ConstructorDeclarationNode(newRange, newLeadingComments, newTrailingComments,
			newWhitespace, newHints, newParent, modifiers, typeParameters, name, parameters, thrownExceptions, body);
	}

/**
 * Builder for creating {@link ConstructorDeclarationNode} instances.
 */
	public static final class Builder implements ASTNodeBuilder<ConstructorDeclarationNode>
		{
		private SourceRange range;
		private List<Comment> leadingComments = List.of();
		private List<Comment> trailingComments = List.of();
		private WhitespaceInfo whitespace = WhitespaceInfo.none();
		private FormattingHints hints = FormattingHints.defaults();
		private Optional<ASTNode> parent = Optional.empty();
		private List<ASTNode> modifiers = List.of();
		private List<ASTNode> typeParameters = List.of();
		private String name;
		private List<ASTNode> parameters = List.of();
		private List<ASTNode> thrownExceptions = List.of();
		private ASTNode body;

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
		 * Sets the constructor modifiers.
		 *
		 * @param modifiers constructor modifiers (public, private, etc.)
		 * @return this builder
		 */
		public Builder setModifiers(List<ASTNode> modifiers)
			{
			this.modifiers = List.copyOf(modifiers); return this;
			}
		/**
		 * Sets the generic type parameters.
		 *
		 * @param typeParameters generic type parameters
		 * @return this builder
		 */
		public Builder setTypeParameters(List<ASTNode> typeParameters)
			{
			this.typeParameters = List.copyOf(typeParameters); return this;
			}
		/**
		 * Sets the constructor name.
		 *
		 * @param name the constructor name
		 * @return this builder
		 */
		public Builder setName(String name)
			{
			this.name = name; return this;
			}
		/**
		 * Sets the constructor parameters.
		 *
		 * @param parameters constructor parameters
		 * @return this builder
		 */
		public Builder setParameters(List<ASTNode> parameters)
			{
			this.parameters = List.copyOf(parameters); return this;
			}
		/**
		 * Sets the exceptions thrown by this constructor.
		 *
		 * @param thrownExceptions exceptions thrown by this constructor
		 * @return this builder
		 */
		public Builder setThrownExceptions(List<ASTNode> thrownExceptions)
			{
			this.thrownExceptions = List.copyOf(thrownExceptions); return this;
			}
		/**
		 * Sets the constructor body.
		 *
		 * @param body the constructor body
		 * @return this builder
		 */
		public Builder setBody(ASTNode body)
			{
			this.body = body; return this;
			}

		@Override
		public ConstructorDeclarationNode build()
			{
			if (!isValid())
				throw new IllegalStateException("Invalid builder state: " +
					String.join(", ", getValidationErrors()));
			return new ConstructorDeclarationNode(range, leadingComments, trailingComments, whitespace,
				hints, parent, modifiers, typeParameters, name, parameters, thrownExceptions, body);
		}

		@Override public boolean isValid()
			{
			return range != null && name != null && !name.isBlank() && body != null;
			}
		@Override public List<String> getValidationErrors()
			{
			var errors = new java.util.ArrayList<String>();
			if (range == null)
				errors.add("range is required");
			if (name == null || name.isBlank())
				errors.add("name is required and cannot be blank");
			if (body == null)
				errors.add("body is required");
			return errors;
			}
	}
}
