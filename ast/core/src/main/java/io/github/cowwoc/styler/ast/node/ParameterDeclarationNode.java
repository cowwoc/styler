package io.github.cowwoc.styler.ast.node;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;
import io.github.cowwoc.styler.ast.*;
import io.github.cowwoc.styler.ast.visitor.ASTVisitor;
import io.github.cowwoc.styler.ast.builder.ASTNodeBuilder;

import java.util.List;
import java.util.Optional;

/**
 * AST node representing a parameter declaration.
 */
public final class ParameterDeclarationNode extends ASTNode {
	private final List<ASTNode> modifiers;
	private final ASTNode type;
	private final String name;
	private final boolean isVarArgs;

	public ParameterDeclarationNode(SourceRange range, List<Comment> leadingComments,
		List<Comment> trailingComments, WhitespaceInfo whitespace, FormattingHints hints,
		Optional<ASTNode> parent, List<ASTNode> modifiers, ASTNode type, String name, boolean isVarArgs) {
		super(range, leadingComments, trailingComments, whitespace, hints, parent);
		requireThat(modifiers, "modifiers").isNotNull();
		requireThat(type, "type").isNotNull();
		requireThat(name, "name").isNotNull().isNotBlank();
		this.modifiers = List.copyOf(modifiers);
		this.type = type;
		this.name = name;
		this.isVarArgs = isVarArgs;
	}

	public List<ASTNode> getModifiers() { return modifiers; }
	public ASTNode getType() { return type; }
	public String getName() { return name; }
	public boolean isVarArgs() { return isVarArgs; }

	@Override
	public <R, A> R accept(ASTVisitor<R, A> visitor, final A arg) {
		return visitor.visitParameterDeclaration(this, arg);
	}

	@Override
	public ASTNodeBuilder<ParameterDeclarationNode> toBuilder() {
		return new Builder().setRange(getRange()).setLeadingComments(getLeadingComments())
			.setTrailingComments(getTrailingComments()).setWhitespace(getWhitespace())
			.setHints(getHints()).setParent(getParent()).setModifiers(modifiers).setType(type).setName(name).setVarArgs(isVarArgs);
	}

	@Override
	public List<ASTNode> getChildren() {
		var children = new java.util.ArrayList<ASTNode>();
		children.addAll(modifiers);
		children.add(type);
		return List.copyOf(children);
	}

	@Override
	protected ASTNode withMetadata(SourceRange newRange, List<Comment> newLeadingComments,
		List<Comment> newTrailingComments, WhitespaceInfo newWhitespace, FormattingHints newHints,
		Optional<ASTNode> newParent) {
		return new ParameterDeclarationNode(newRange, newLeadingComments, newTrailingComments,
			newWhitespace, newHints, newParent, modifiers, type, name, isVarArgs);
	}

	public static final class Builder implements ASTNodeBuilder<ParameterDeclarationNode> {
		private SourceRange range;
		private List<Comment> leadingComments = List.of();
		private List<Comment> trailingComments = List.of();
		private WhitespaceInfo whitespace = WhitespaceInfo.none();
		private FormattingHints hints = FormattingHints.defaults();
		private Optional<ASTNode> parent = Optional.empty();
		private List<ASTNode> modifiers = List.of();
		private ASTNode type;
		private String name;
		private boolean isVarArgs = false;

		@Override public Builder setRange(SourceRange range) { this.range = range; return this; }
		@Override public Builder setLeadingComments(List<Comment> comments) { this.leadingComments = List.copyOf(comments); return this; }
		@Override public Builder setTrailingComments(List<Comment> comments) { this.trailingComments = List.copyOf(comments); return this; }
		@Override public Builder setWhitespace(WhitespaceInfo whitespace) { this.whitespace = whitespace; return this; }
		@Override public Builder setHints(FormattingHints hints) { this.hints = hints; return this; }
		@Override public Builder setParent(Optional<ASTNode> parent) { this.parent = parent; return this; }
		@Override public Builder addLeadingComment(Comment comment) { var newComments = new java.util.ArrayList<>(leadingComments); newComments.add(comment); this.leadingComments = List.copyOf(newComments); return this; }
		@Override public Builder addTrailingComment(Comment comment) { var newComments = new java.util.ArrayList<>(trailingComments); newComments.add(comment); this.trailingComments = List.copyOf(newComments); return this; }

		public Builder setModifiers(List<ASTNode> modifiers) { this.modifiers = List.copyOf(modifiers); return this; }
		public Builder setType(ASTNode type) { this.type = type; return this; }
		public Builder setName(String name) { this.name = name; return this; }
		public Builder setVarArgs(boolean isVarArgs) { this.isVarArgs = isVarArgs; return this; }

		@Override
		public ParameterDeclarationNode build() {
			if (!isValid()) throw new IllegalStateException("Invalid builder state: " + String.join(", ", getValidationErrors()));
			return new ParameterDeclarationNode(range, leadingComments, trailingComments, whitespace, hints, parent, modifiers, type, name, isVarArgs);
		}

		@Override public Builder reset() { range = null; leadingComments = List.of(); trailingComments = List.of(); whitespace = WhitespaceInfo.none(); hints = FormattingHints.defaults(); parent = Optional.empty(); modifiers = List.of(); type = null; name = null; isVarArgs = false; return this; }
		@Override public Builder copy() { return new Builder().setRange(range).setLeadingComments(leadingComments).setTrailingComments(trailingComments).setWhitespace(whitespace).setHints(hints).setParent(parent).setModifiers(modifiers).setType(type).setName(name).setVarArgs(isVarArgs); }
		@Override public boolean isValid() { return range != null && type != null && name != null && !name.isBlank(); }
		@Override public List<String> getValidationErrors() { var errors = new java.util.ArrayList<String>(); if (range == null) errors.add("range is required"); if (type == null) errors.add("type final is required"); if (name == null || name.isBlank()) errors.add("name is required and cannot final be blank"); return errors; }
	}
}
