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
 * AST node representing a method declaration.
 */
public final class MethodDeclarationNode extends ASTNode
	{
	private final List<ASTNode> modifiers;
	private final List<ASTNode> typeParameters;
	private final ASTNode returnType;
	private final String name;
	private final List<ASTNode> parameters;
	private final List<ASTNode> thrownExceptions;
	private final Optional<ASTNode> body;

	/**
	 * Creates a method declaration node.
	 *
	 * @param range the source range
	 * @param leadingComments comments before this node
	 * @param trailingComments comments after this node
	 * @param whitespace whitespace information
	 * @param hints formatting hints
	 * @param parent the parent node
	 * @param modifiers method modifiers (public, static, etc.)
	 * @param typeParameters generic type parameters
	 * @param returnType the return type
	 * @param name the method name
	 * @param parameters method parameters
	 * @param thrownExceptions exceptions thrown by this method
	 * @param body the method body (empty for abstract methods)
	 * @throws NullPointerException if modifiers, typeParameters, returnType, name, parameters,
	 *         thrownExceptions, or body is {@code null}
	 * @throws IllegalArgumentException if name is blank
	 */
	public MethodDeclarationNode(SourceRange range, List<Comment> leadingComments,
		List<Comment> trailingComments, WhitespaceInfo whitespace, FormattingHints hints,
		Optional<ASTNode> parent, List<ASTNode> modifiers, List<ASTNode> typeParameters,
		ASTNode returnType, String name, List<ASTNode> parameters, List<ASTNode> thrownExceptions,
		Optional<ASTNode> body)
			{
		super(range, leadingComments, trailingComments, whitespace, hints, parent);
		requireThat(modifiers, "modifiers").isNotNull();
		requireThat(typeParameters, "typeParameters").isNotNull();
		requireThat(returnType, "returnType").isNotNull();
		requireThat(name, "name").isNotNull().isNotBlank();
		requireThat(parameters, "parameters").isNotNull();
		requireThat(thrownExceptions, "thrownExceptions").isNotNull();
		requireThat(body, "body").isNotNull();
		this.modifiers = List.copyOf(modifiers);
		this.typeParameters = List.copyOf(typeParameters);
		this.returnType = returnType;
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
	public ASTNode getReturnType()
		{
		return returnType;
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
	public Optional<ASTNode> getBody()
		{
		return body;
		}

	@Override
	public <R, A> R accept(ASTVisitor<R, A> visitor, A arg)
		{
		return visitor.visitMethodDeclaration(this, arg);
	}

	@Override
	public ASTNodeBuilder<MethodDeclarationNode> toBuilder()
		{
		return new Builder().setRange(getRange()).setLeadingComments(getLeadingComments()).
			setTrailingComments(getTrailingComments()).setWhitespace(getWhitespace()).
			setHints(getHints()).setParent(getParent()).setModifiers(modifiers).setTypeParameters(typeParameters).
			setReturnType(returnType).setName(name).setParameters(parameters).
			setThrownExceptions(thrownExceptions).setBody(body);
	}

	@Override
	public List<ASTNode> getChildren()
		{
		var children = new java.util.ArrayList<ASTNode>();
		children.addAll(modifiers);
		children.addAll(typeParameters);
		children.add(returnType);
		children.addAll(parameters);
		children.addAll(thrownExceptions);
		body.ifPresent(children::add);
		return List.copyOf(children);
	}

	@Override
	protected ASTNode withMetadata(SourceRange newRange, List<Comment> newLeadingComments,
		List<Comment> newTrailingComments, WhitespaceInfo newWhitespace, FormattingHints newHints,
		Optional<ASTNode> newParent)
			{
		return new MethodDeclarationNode(newRange, newLeadingComments, newTrailingComments,
			newWhitespace, newHints, newParent, modifiers, typeParameters, returnType, name,
		parameters, thrownExceptions, body);
	}

/**
 * Builder for creating {@link MethodDeclarationNode} instances.
 */
	public static final class Builder implements ASTNodeBuilder<MethodDeclarationNode>
		{
		private SourceRange range;
		private List<Comment> leadingComments = List.of();
		private List<Comment> trailingComments = List.of();
		private WhitespaceInfo whitespace = WhitespaceInfo.none();
		private FormattingHints hints = FormattingHints.defaults();
		private Optional<ASTNode> parent = Optional.empty();
		private List<ASTNode> modifiers = List.of();
		private List<ASTNode> typeParameters = List.of();
		private ASTNode returnType;
		private String name;
		private List<ASTNode> parameters = List.of();
		private List<ASTNode> thrownExceptions = List.of();
		private Optional<ASTNode> body = Optional.empty();

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
		 * Sets the method modifiers.
		 *
		 * @param modifiers method modifiers (public, static, etc.)
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
		 * Sets the return type.
		 *
		 * @param returnType the return type
		 * @return this builder
		 */
		public Builder setReturnType(ASTNode returnType)
			{
			this.returnType = returnType; return this;
			}
		/**
		 * Sets the method name.
		 *
		 * @param name the method name
		 * @return this builder
		 */
		public Builder setName(String name)
			{
			this.name = name; return this;
			}
		/**
		 * Sets the method parameters.
		 *
		 * @param parameters method parameters
		 * @return this builder
		 */
		public Builder setParameters(List<ASTNode> parameters)
			{
			this.parameters = List.copyOf(parameters); return this;
			}
		/**
		 * Sets the exceptions thrown by this method.
		 *
		 * @param thrownExceptions exceptions thrown by this method
		 * @return this builder
		 */
		public Builder setThrownExceptions(List<ASTNode> thrownExceptions)
			{
			this.thrownExceptions = List.copyOf(thrownExceptions); return this;
			}
		/**
		 * Sets the method body.
		 *
		 * @param body the method body (empty for abstract methods)
		 * @return this builder
		 */
		public Builder setBody(Optional<ASTNode> body)
			{
			this.body = body; return this;
			}

		@Override
		public MethodDeclarationNode build()
			{
			if (!isValid())
				throw new IllegalStateException("Invalid builder state: " +
					String.join(", ", getValidationErrors()));
			return new MethodDeclarationNode(range, leadingComments, trailingComments, whitespace,
				hints, parent, modifiers, typeParameters, returnType, name, parameters, thrownExceptions, body);
		}

		@Override public boolean isValid()
			{
			return range != null && returnType != null && name != null && !name.isBlank();
			}
		@Override public List<String> getValidationErrors()
			{
			var errors = new java.util.ArrayList<String>();
			if (range == null)
				errors.add("range is required");
			if (returnType == null)
				errors.add("returnType final is required");
			if (name == null || name.isBlank())
				errors.add("name is required and cannot final be blank");
			return errors;
			}
	}
}
