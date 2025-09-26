package io.github.cowwoc.styler.ast.node;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;
import io.github.cowwoc.styler.ast.*;
import io.github.cowwoc.styler.ast.visitor.ASTVisitor;
import io.github.cowwoc.styler.ast.builder.ASTNodeBuilder;

import java.util.List;
import java.util.Optional;

/**
 * AST node representing a constructor declaration.
 */
public final class ConstructorDeclarationNode extends ASTNode {
	private final List<ASTNode> modifiers;
	private final List<ASTNode> typeParameters;
	private final String name;
	private final List<ASTNode> parameters;
	private final List<ASTNode> thrownExceptions;
	private final ASTNode body;

	public ConstructorDeclarationNode(SourceRange range, List<Comment> leadingComments,
		List<Comment> trailingComments, WhitespaceInfo whitespace, FormattingHints hints,
		Optional<ASTNode> parent, List<ASTNode> modifiers, List<ASTNode> typeParameters,
		String name, List<ASTNode> parameters, List<ASTNode> thrownExceptions, ASTNode body) {
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

	public List<ASTNode> getModifiers() { return modifiers; }
	public List<ASTNode> getTypeParameters() { return typeParameters; }
	public String getName() { return name; }
	public List<ASTNode> getParameters() { return parameters; }
	public List<ASTNode> getThrownExceptions() { return thrownExceptions; }
	public ASTNode getBody() { return body; }

	@Override
	public <R, A> R accept(ASTVisitor<R, A> visitor, final A arg) {
		return visitor.visitConstructorDeclaration(this, arg);
	}

	@Override
	public ASTNodeBuilder<ConstructorDeclarationNode> toBuilder() {
		return new Builder().setRange(getRange()).setLeadingComments(getLeadingComments())
			.setTrailingComments(getTrailingComments()).setWhitespace(getWhitespace())
			.setHints(getHints()).setParent(getParent()).setModifiers(modifiers).setTypeParameters(typeParameters)
			.setName(name).setParameters(parameters).setThrownExceptions(thrownExceptions).setBody(body);
	}

	@Override
	public List<ASTNode> getChildren() {
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
		Optional<ASTNode> newParent) {
		return new ConstructorDeclarationNode(newRange, newLeadingComments, newTrailingComments,
			newWhitespace, newHints, newParent, modifiers, typeParameters, name, parameters, thrownExceptions, body);
	}

	public static final class Builder implements ASTNodeBuilder<ConstructorDeclarationNode> {
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

		@Override public Builder setRange(SourceRange range) { this.range = range; return this; }
		@Override public Builder setLeadingComments(List<Comment> comments) { this.leadingComments = List.copyOf(comments); return this; }
		@Override public Builder setTrailingComments(List<Comment> comments) { this.trailingComments = List.copyOf(comments); return this; }
		@Override public Builder setWhitespace(WhitespaceInfo whitespace) { this.whitespace = whitespace; return this; }
		@Override public Builder setHints(FormattingHints hints) { this.hints = hints; return this; }
		@Override public Builder setParent(Optional<ASTNode> parent) { this.parent = parent; return this; }
		@Override public Builder addLeadingComment(Comment comment) { var newComments = new java.util.ArrayList<>(leadingComments); newComments.add(comment); this.leadingComments = List.copyOf(newComments); return this; }
		@Override public Builder addTrailingComment(Comment comment) { var newComments = new java.util.ArrayList<>(trailingComments); newComments.add(comment); this.trailingComments = List.copyOf(newComments); return this; }

		public Builder setModifiers(List<ASTNode> modifiers) { this.modifiers = List.copyOf(modifiers); return this; }
		public Builder setTypeParameters(List<ASTNode> typeParameters) { this.typeParameters = List.copyOf(typeParameters); return this; }
		public Builder setName(String name) { this.name = name; return this; }
		public Builder setParameters(List<ASTNode> parameters) { this.parameters = List.copyOf(parameters); return this; }
		public Builder setThrownExceptions(List<ASTNode> thrownExceptions) { this.thrownExceptions = List.copyOf(thrownExceptions); return this; }
		public Builder setBody(ASTNode body) { this.body = body; return this; }

		@Override
		public ConstructorDeclarationNode build() {
			if (!isValid()) throw new IllegalStateException("Invalid builder state: " + String.join(", ", getValidationErrors()));
			return new ConstructorDeclarationNode(range, leadingComments, trailingComments, whitespace, hints, parent, modifiers, typeParameters, name, parameters, thrownExceptions, body);
		}

		@Override public Builder reset() { range = null; leadingComments = List.of(); trailingComments = List.of(); whitespace = WhitespaceInfo.none(); hints = FormattingHints.defaults(); parent = Optional.empty(); modifiers = List.of(); typeParameters = List.of(); name = null; parameters = List.of(); thrownExceptions = List.of(); body = null; return this; }
		@Override public Builder copy() { return new Builder().setRange(range).setLeadingComments(leadingComments).setTrailingComments(trailingComments).setWhitespace(whitespace).setHints(hints).setParent(parent).setModifiers(modifiers).setTypeParameters(typeParameters).setName(name).setParameters(parameters).setThrownExceptions(thrownExceptions).setBody(body); }
		@Override public boolean isValid() { return range != null && name != null && !name.isBlank() && body != null; }
		@Override public List<String> getValidationErrors() { var errors = new java.util.ArrayList<String>(); if (range == null) errors.add("range is required"); if (name == null || name.isBlank()) errors.add("name is required and cannot final be blank"); if (body == null) errors.add("body final is required"); return errors; }
	}
}
