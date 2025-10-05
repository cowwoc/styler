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
 * AST node representing an interface declaration.
 */
public final class InterfaceDeclarationNode extends ASTNode
	{
	private final List<ASTNode> modifiers;
	private final String name;
	private final List<ASTNode> typeParameters;
	private final List<ASTNode> extendedInterfaces;
	private final List<ASTNode> members;

	/**
	 * Creates an interface declaration node.
	 *
	 * @param range the source range
	 * @param leadingComments comments before this node
	 * @param trailingComments comments after this node
	 * @param whitespace whitespace information
	 * @param hints formatting hints
	 * @param parent the parent node
	 * @param modifiers interface modifiers (public, etc.)
	 * @param name the interface name
	 * @param typeParameters generic type parameters
	 * @param extendedInterfaces extended interfaces
	 * @param members interface members (methods, nested classes, constants)
	 * @throws NullPointerException if modifiers, name, typeParameters, extendedInterfaces, or members is {@code null}
	 * @throws IllegalArgumentException if name is blank
	 */
	public InterfaceDeclarationNode(SourceRange range, List<Comment> leadingComments,
		List<Comment> trailingComments, WhitespaceInfo whitespace, FormattingHints hints,
		Optional<ASTNode> parent, List<ASTNode> modifiers, String name, List<ASTNode> typeParameters,
		List<ASTNode> extendedInterfaces, List<ASTNode> members)
			{
		super(range, leadingComments, trailingComments, whitespace, hints, parent);
		requireThat(modifiers, "modifiers").isNotNull();
		requireThat(name, "name").isNotNull().isNotBlank();
		requireThat(typeParameters, "typeParameters").isNotNull();
		requireThat(extendedInterfaces, "extendedInterfaces").isNotNull();
		requireThat(members, "members").isNotNull();
		this.modifiers = List.copyOf(modifiers);
		this.name = name;
		this.typeParameters = List.copyOf(typeParameters);
		this.extendedInterfaces = List.copyOf(extendedInterfaces);
		this.members = List.copyOf(members);
	}

	public List<ASTNode> getModifiers()
		{
		return modifiers;
		}
	public String getName()
		{
		return name;
		}
	public List<ASTNode> getTypeParameters()
		{
		return typeParameters;
		}
	public List<ASTNode> getExtendedInterfaces()
		{
		return extendedInterfaces;
		}
	public List<ASTNode> getMembers()
		{
		return members;
		}

	@Override
	public <R, A> R accept(ASTVisitor<R, A> visitor, A arg)
		{
		return visitor.visitInterfaceDeclaration(this, arg);
	}

	@Override
	public ASTNodeBuilder<InterfaceDeclarationNode> toBuilder()
		{
		return new Builder().setRange(getRange()).setLeadingComments(getLeadingComments()).
			setTrailingComments(getTrailingComments()).setWhitespace(getWhitespace()).
			setHints(getHints()).setParent(getParent()).setModifiers(modifiers).setName(name).
			setTypeParameters(typeParameters).setExtendedInterfaces(extendedInterfaces).setMembers(members);
	}

	@Override
	public List<ASTNode> getChildren()
		{
		var children = new java.util.ArrayList<ASTNode>();
		children.addAll(modifiers);
		children.addAll(typeParameters);
		children.addAll(extendedInterfaces);
		children.addAll(members);
		return List.copyOf(children);
	}

	@Override
	protected ASTNode withMetadata(SourceRange newRange, List<Comment> newLeadingComments,
		List<Comment> newTrailingComments, WhitespaceInfo newWhitespace, FormattingHints newHints,
		Optional<ASTNode> newParent)
			{
		return new InterfaceDeclarationNode(newRange, newLeadingComments, newTrailingComments,
			newWhitespace, newHints, newParent, modifiers, name, typeParameters, extendedInterfaces, members);
	}

/**
 * Builder for creating {@link InterfaceDeclarationNode} instances.
 */
	public static final class Builder implements ASTNodeBuilder<InterfaceDeclarationNode>
		{
		private SourceRange range;
		private List<Comment> leadingComments = List.of();
		private List<Comment> trailingComments = List.of();
		private WhitespaceInfo whitespace = WhitespaceInfo.none();
		private FormattingHints hints = FormattingHints.defaults();
		private Optional<ASTNode> parent = Optional.empty();
		private List<ASTNode> modifiers = List.of();
		private String name;
		private List<ASTNode> typeParameters = List.of();
		private List<ASTNode> extendedInterfaces = List.of();
		private List<ASTNode> members = List.of();

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
		 * Sets the interface modifiers.
		 *
		 * @param modifiers interface modifiers (public, etc.)
		 * @return this builder
		 */
		public Builder setModifiers(List<ASTNode> modifiers)
			{
			this.modifiers = List.copyOf(modifiers); return this;
			}
		/**
		 * Sets the interface name.
		 *
		 * @param name the interface name
		 * @return this builder
		 */
		public Builder setName(String name)
			{
			this.name = name; return this;
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
		 * Sets the extended interfaces.
		 *
		 * @param extendedInterfaces extended interfaces
		 * @return this builder
		 */
		public Builder setExtendedInterfaces(List<ASTNode> extendedInterfaces)
			{
			this.extendedInterfaces = List.copyOf(extendedInterfaces); return this;
			}
		/**
		 * Sets the interface members.
		 *
		 * @param members interface members (methods, nested classes, constants)
		 * @return this builder
		 */
		public Builder setMembers(List<ASTNode> members)
			{
			this.members = List.copyOf(members); return this;
			}

		@Override
		public InterfaceDeclarationNode build()
			{
			if (!isValid())
				throw new IllegalStateException("Invalid builder state: " +
					String.join(", ", getValidationErrors()));
			return new InterfaceDeclarationNode(range, leadingComments, trailingComments, whitespace,
				hints, parent, modifiers, name, typeParameters, extendedInterfaces, members);
		}

		@Override public boolean isValid()
			{
			return range != null && name != null && !name.isBlank();
			}
		@Override public List<String> getValidationErrors()
			{
			var errors = new java.util.ArrayList<String>();
			if (range == null)
				errors.add("range is required");
			if (name == null || name.isBlank())
				errors.add("name is required and cannot final be blank");
			return errors;
			}
	}
}
