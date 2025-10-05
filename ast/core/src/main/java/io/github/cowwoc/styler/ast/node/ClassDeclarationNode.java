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
 * AST node representing a class declaration.
 */
public final class ClassDeclarationNode extends ASTNode
	{
	private final List<ASTNode> modifiers;
	private final String name;
	private final List<ASTNode> typeParameters;
	private final Optional<ASTNode> superClass;
	private final List<ASTNode> interfaces;
	private final List<ASTNode> members;

	/**
	 * Creates a class declaration node.
	 *
	 * @param range the source range
	 * @param leadingComments comments before this node
	 * @param trailingComments comments after this node
	 * @param whitespace whitespace information
	 * @param hints formatting hints
	 * @param parent the parent node
	 * @param modifiers class modifiers (public, final, etc.)
	 * @param name the class name
	 * @param typeParameters generic type parameters
	 * @param superClass the superclass (empty for Object)
	 * @param interfaces implemented interfaces
	 * @param members class members (fields, methods, nested classes)
	 * @throws NullPointerException if modifiers, name, typeParameters, superClass, interfaces, or members
	 *         is {@code null}
	 * @throws IllegalArgumentException if name is blank
	 */
	public ClassDeclarationNode(SourceRange range, List<Comment> leadingComments,
		List<Comment> trailingComments, WhitespaceInfo whitespace, FormattingHints hints,
		Optional<ASTNode> parent, List<ASTNode> modifiers, String name, List<ASTNode> typeParameters,
		Optional<ASTNode> superClass, List<ASTNode> interfaces, List<ASTNode> members)
			{
		super(range, leadingComments, trailingComments, whitespace, hints, parent);
		requireThat(modifiers, "modifiers").isNotNull();
		requireThat(name, "name").isNotNull().isNotBlank();
		requireThat(typeParameters, "typeParameters").isNotNull();
		requireThat(superClass, "superClass").isNotNull();
		requireThat(interfaces, "interfaces").isNotNull();
		requireThat(members, "members").isNotNull();
		this.modifiers = List.copyOf(modifiers);
		this.name = name;
		this.typeParameters = List.copyOf(typeParameters);
		this.superClass = superClass;
		this.interfaces = List.copyOf(interfaces);
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
	public Optional<ASTNode> getSuperClass()
		{
		return superClass;
		}
	public List<ASTNode> getInterfaces()
		{
		return interfaces;
		}
	public List<ASTNode> getMembers()
		{
		return members;
		}

	@Override
	public <R, A> R accept(ASTVisitor<R, A> visitor, A arg)
		{
		return visitor.visitClassDeclaration(this, arg);
	}

	@Override
	public ASTNodeBuilder<ClassDeclarationNode> toBuilder()
		{
		return new Builder().setRange(getRange()).setLeadingComments(getLeadingComments()).
			setTrailingComments(getTrailingComments()).setWhitespace(getWhitespace()).
			setHints(getHints()).setParent(getParent()).setModifiers(modifiers).setName(name).
			setTypeParameters(typeParameters).setSuperClass(superClass).setInterfaces(interfaces).setMembers(members);
	}

	@Override
	public List<ASTNode> getChildren()
		{
		var children = new java.util.ArrayList<ASTNode>();
		children.addAll(modifiers);
		children.addAll(typeParameters);
		superClass.ifPresent(children::add);
		children.addAll(interfaces);
		children.addAll(members);
		return List.copyOf(children);
	}

	@Override
	protected ASTNode withMetadata(SourceRange newRange, List<Comment> newLeadingComments,
		List<Comment> newTrailingComments, WhitespaceInfo newWhitespace, FormattingHints newHints,
		Optional<ASTNode> newParent)
			{
		return new ClassDeclarationNode(newRange, newLeadingComments, newTrailingComments,
			newWhitespace, newHints, newParent, modifiers, name, typeParameters, superClass, interfaces, members);
	}

/**
 * Builder for creating {@link ClassDeclarationNode} instances.
 */
	public static final class Builder implements ASTNodeBuilder<ClassDeclarationNode>
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
		private Optional<ASTNode> superClass = Optional.empty();
		private List<ASTNode> interfaces = List.of();
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
		 * Sets the class modifiers.
		 *
		 * @param modifiers class modifiers (public, final, etc.)
		 * @return this builder
		 */
		public Builder setModifiers(List<ASTNode> modifiers)
			{
			this.modifiers = List.copyOf(modifiers); return this;
			}
		/**
		 * Sets the class name.
		 *
		 * @param name the class name
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
		 * Sets the superclass.
		 *
		 * @param superClass the superclass (empty for Object)
		 * @return this builder
		 */
		public Builder setSuperClass(Optional<ASTNode> superClass)
			{
			this.superClass = superClass; return this;
			}
		/**
		 * Sets the implemented interfaces.
		 *
		 * @param interfaces implemented interfaces
		 * @return this builder
		 */
		public Builder setInterfaces(List<ASTNode> interfaces)
			{
			this.interfaces = List.copyOf(interfaces); return this;
			}
		/**
		 * Sets the class members.
		 *
		 * @param members class members (fields, methods, nested classes)
		 * @return this builder
		 */
		public Builder setMembers(List<ASTNode> members)
			{
			this.members = List.copyOf(members); return this;
			}

		@Override
		public ClassDeclarationNode build()
			{
			if (!isValid())
				throw new IllegalStateException("Invalid builder state: " +
					String.join(", ", getValidationErrors()));
			return new ClassDeclarationNode(range, leadingComments, trailingComments, whitespace,
				hints, parent, modifiers, name, typeParameters, superClass, interfaces, members);
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
