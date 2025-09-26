package io.github.cowwoc.styler.ast.node;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;
import io.github.cowwoc.styler.ast.*;
import io.github.cowwoc.styler.ast.visitor.ASTVisitor;
import io.github.cowwoc.styler.ast.builder.ASTNodeBuilder;

import java.util.List;
import java.util.Optional;

/**
 * AST node representing an enum declaration.
 */
public final class EnumDeclarationNode extends ASTNode {
	private final List<ASTNode> modifiers;
	private final String name;
	private final List<ASTNode> interfaces;
	private final List<ASTNode> constants;
	private final List<ASTNode> members;

	public EnumDeclarationNode(SourceRange range, List<Comment> leadingComments,
		List<Comment> trailingComments, WhitespaceInfo whitespace, FormattingHints hints,
		Optional<ASTNode> parent, List<ASTNode> modifiers, String name, List<ASTNode> interfaces,
		List<ASTNode> constants, List<ASTNode> members) {
		super(range, leadingComments, trailingComments, whitespace, hints, parent);
		requireThat(modifiers, "modifiers").isNotNull();
		requireThat(name, "name").isNotNull().isNotBlank();
		requireThat(interfaces, "interfaces").isNotNull();
		requireThat(constants, "constants").isNotNull();
		requireThat(members, "members").isNotNull();
		this.modifiers = List.copyOf(modifiers);
		this.name = name;
		this.interfaces = List.copyOf(interfaces);
		this.constants = List.copyOf(constants);
		this.members = List.copyOf(members);
	}

	public List<ASTNode> getModifiers() { return modifiers; }
	public String getName() { return name; }
	public List<ASTNode> getInterfaces() { return interfaces; }
	public List<ASTNode> getConstants() { return constants; }
	public List<ASTNode> getMembers() { return members; }

	@Override
	public <R, A> R accept(ASTVisitor<R, A> visitor, final A arg) {
		return visitor.visitEnumDeclaration(this, arg);
	}

	@Override
	public ASTNodeBuilder<EnumDeclarationNode> toBuilder() {
		return new Builder().setRange(getRange()).setLeadingComments(getLeadingComments())
			.setTrailingComments(getTrailingComments()).setWhitespace(getWhitespace())
			.setHints(getHints()).setParent(getParent()).setModifiers(modifiers).setName(name)
			.setInterfaces(interfaces).setConstants(constants).setMembers(members);
	}

	@Override
	public List<ASTNode> getChildren() {
		var children = new java.util.ArrayList<ASTNode>();
		children.addAll(modifiers);
		children.addAll(interfaces);
		children.addAll(constants);
		children.addAll(members);
		return List.copyOf(children);
	}

	@Override
	protected ASTNode withMetadata(SourceRange newRange, List<Comment> newLeadingComments,
		List<Comment> newTrailingComments, WhitespaceInfo newWhitespace, FormattingHints newHints,
		Optional<ASTNode> newParent) {
		return new EnumDeclarationNode(newRange, newLeadingComments, newTrailingComments,
			newWhitespace, newHints, newParent, modifiers, name, interfaces, constants, members);
	}

	public static final class Builder implements ASTNodeBuilder<EnumDeclarationNode> {
		private SourceRange range;
		private List<Comment> leadingComments = List.of();
		private List<Comment> trailingComments = List.of();
		private WhitespaceInfo whitespace = WhitespaceInfo.none();
		private FormattingHints hints = FormattingHints.defaults();
		private Optional<ASTNode> parent = Optional.empty();
		private List<ASTNode> modifiers = List.of();
		private String name;
		private List<ASTNode> interfaces = List.of();
		private List<ASTNode> constants = List.of();
		private List<ASTNode> members = List.of();

		@Override public Builder setRange(SourceRange range) { this.range = range; return this; }
		@Override public Builder setLeadingComments(List<Comment> comments) { this.leadingComments = List.copyOf(comments); return this; }
		@Override public Builder setTrailingComments(List<Comment> comments) { this.trailingComments = List.copyOf(comments); return this; }
		@Override public Builder setWhitespace(WhitespaceInfo whitespace) { this.whitespace = whitespace; return this; }
		@Override public Builder setHints(FormattingHints hints) { this.hints = hints; return this; }
		@Override public Builder setParent(Optional<ASTNode> parent) { this.parent = parent; return this; }
		@Override public Builder addLeadingComment(Comment comment) { var newComments = new java.util.ArrayList<>(leadingComments); newComments.add(comment); this.leadingComments = List.copyOf(newComments); return this; }
		@Override public Builder addTrailingComment(Comment comment) { var newComments = new java.util.ArrayList<>(trailingComments); newComments.add(comment); this.trailingComments = List.copyOf(newComments); return this; }

		public Builder setModifiers(List<ASTNode> modifiers) { this.modifiers = List.copyOf(modifiers); return this; }
		public Builder setName(String name) { this.name = name; return this; }
		public Builder setInterfaces(List<ASTNode> interfaces) { this.interfaces = List.copyOf(interfaces); return this; }
		public Builder setConstants(List<ASTNode> constants) { this.constants = List.copyOf(constants); return this; }
		public Builder setMembers(List<ASTNode> members) { this.members = List.copyOf(members); return this; }

		@Override
		public EnumDeclarationNode build() {
			if (!isValid()) throw new IllegalStateException("Invalid builder state: " + String.join(", ", getValidationErrors()));
			return new EnumDeclarationNode(range, leadingComments, trailingComments, whitespace, hints, parent, modifiers, name, interfaces, constants, members);
		}

		@Override public Builder reset() { range = null; leadingComments = List.of(); trailingComments = List.of(); whitespace = WhitespaceInfo.none(); hints = FormattingHints.defaults(); parent = Optional.empty(); modifiers = List.of(); name = null; interfaces = List.of(); constants = List.of(); members = List.of(); return this; }
		@Override public Builder copy() { return new Builder().setRange(range).setLeadingComments(leadingComments).setTrailingComments(trailingComments).setWhitespace(whitespace).setHints(hints).setParent(parent).setModifiers(modifiers).setName(name).setInterfaces(interfaces).setConstants(constants).setMembers(members); }
		@Override public boolean isValid() { return range != null && name != null && !name.isBlank(); }
		@Override public List<String> getValidationErrors() { var errors = new java.util.ArrayList<String>(); if (range == null) errors.add("range is required"); if (name == null || name.isBlank()) errors.add("name is required and cannot final be blank"); return errors; }
	}
}
