package io.github.cowwoc.styler.ast.node;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;
import io.github.cowwoc.styler.ast.*;
import io.github.cowwoc.styler.ast.visitor.ASTVisitor;
import io.github.cowwoc.styler.ast.builder.ASTNodeBuilder;

import java.util.List;
import java.util.Optional;

/**
 * AST node representing an import declaration.
 */
public final class ImportDeclarationNode extends ASTNode {
	private final boolean isStatic;
	private final ASTNode name;
	private final boolean isOnDemand;

	public ImportDeclarationNode(SourceRange range, List<Comment> leadingComments,
		List<Comment> trailingComments, WhitespaceInfo whitespace, FormattingHints hints,
		Optional<ASTNode> parent, boolean isStatic, ASTNode name, boolean isOnDemand) {
		super(range, leadingComments, trailingComments, whitespace, hints, parent);
		requireThat(name, "name").isNotNull();
		this.isStatic = isStatic;
		this.name = name;
		this.isOnDemand = isOnDemand;
	}

	public boolean isStatic() { return isStatic; }
	public ASTNode getName() { return name; }
	public boolean isOnDemand() { return isOnDemand; }

	@Override
	public <R, A> R accept(ASTVisitor<R, A> visitor, final A arg) {
		return visitor.visitImportDeclaration(this, arg);
	}

	@Override
	public ASTNodeBuilder<ImportDeclarationNode> toBuilder() {
		return new Builder().setRange(getRange()).setLeadingComments(getLeadingComments())
			.setTrailingComments(getTrailingComments()).setWhitespace(getWhitespace())
			.setHints(getHints()).setParent(getParent()).setStatic(isStatic).setName(name).setOnDemand(isOnDemand);
	}

	@Override
	public List<ASTNode> getChildren() {
		return List.of(name);
	}

	@Override
	protected ASTNode withMetadata(SourceRange newRange, List<Comment> newLeadingComments,
		List<Comment> newTrailingComments, WhitespaceInfo newWhitespace, FormattingHints newHints,
		Optional<ASTNode> newParent) {
		return new ImportDeclarationNode(newRange, newLeadingComments, newTrailingComments,
			newWhitespace, newHints, newParent, isStatic, name, isOnDemand);
	}

	public static final class Builder implements ASTNodeBuilder<ImportDeclarationNode> {
		private SourceRange range;
		private List<Comment> leadingComments = List.of();
		private List<Comment> trailingComments = List.of();
		private WhitespaceInfo whitespace = WhitespaceInfo.none();
		private FormattingHints hints = FormattingHints.defaults();
		private Optional<ASTNode> parent = Optional.empty();
		private boolean isStatic = false;
		private ASTNode name;
		private boolean isOnDemand = false;

		@Override public Builder setRange(SourceRange range) { this.range = range; return this; }
		@Override public Builder setLeadingComments(List<Comment> comments) { this.leadingComments = List.copyOf(comments); return this; }
		@Override public Builder setTrailingComments(List<Comment> comments) { this.trailingComments = List.copyOf(comments); return this; }
		@Override public Builder setWhitespace(WhitespaceInfo whitespace) { this.whitespace = whitespace; return this; }
		@Override public Builder setHints(FormattingHints hints) { this.hints = hints; return this; }
		@Override public Builder setParent(Optional<ASTNode> parent) { this.parent = parent; return this; }
		@Override public Builder addLeadingComment(Comment comment) { var newComments = new java.util.ArrayList<>(leadingComments); newComments.add(comment); this.leadingComments = List.copyOf(newComments); return this; }
		@Override public Builder addTrailingComment(Comment comment) { var newComments = new java.util.ArrayList<>(trailingComments); newComments.add(comment); this.trailingComments = List.copyOf(newComments); return this; }

		public Builder setStatic(boolean isStatic) { this.isStatic = isStatic; return this; }
		public Builder setName(ASTNode name) { this.name = name; return this; }
		public Builder setOnDemand(boolean isOnDemand) { this.isOnDemand = isOnDemand; return this; }

		@Override
		public ImportDeclarationNode build() {
			if (!isValid()) throw new IllegalStateException("Invalid builder state: " + String.join(", ", getValidationErrors()));
			return new ImportDeclarationNode(range, leadingComments, trailingComments, whitespace, hints, parent, isStatic, name, isOnDemand);
		}

		@Override public Builder reset() { range = null; leadingComments = List.of(); trailingComments = List.of(); whitespace = WhitespaceInfo.none(); hints = FormattingHints.defaults(); parent = Optional.empty(); isStatic = false; name = null; isOnDemand = false; return this; }
		@Override public Builder copy() { return new Builder().setRange(range).setLeadingComments(leadingComments).setTrailingComments(trailingComments).setWhitespace(whitespace).setHints(hints).setParent(parent).setStatic(isStatic).setName(name).setOnDemand(isOnDemand); }
		@Override public boolean isValid() { return range != null && name != null; }
		@Override public List<String> getValidationErrors() { var errors = new java.util.ArrayList<String>(); if (range == null) errors.add("range is required"); if (name == null) errors.add("name final is required"); return errors; }
	}
}
