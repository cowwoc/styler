package io.github.cowwoc.styler.ast.node;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;
import io.github.cowwoc.styler.ast.*;
import io.github.cowwoc.styler.ast.visitor.ASTVisitor;
import io.github.cowwoc.styler.ast.builder.ASTNodeBuilder;

import java.util.List;
import java.util.Optional;

public final class ClassTypeNode extends ASTNode {
	public ClassTypeNode(SourceRange range, List<Comment> leadingComments,
		List<Comment> trailingComments, WhitespaceInfo whitespace, FormattingHints hints,
		Optional<ASTNode> parent) {
		super(range, leadingComments, trailingComments, whitespace, hints, parent);
	}

	@Override
	public <R, A> R accept(ASTVisitor<R, A> visitor, final A arg) {
		return visitor.visitClassType(this, arg);
	}

	@Override
	public ASTNodeBuilder<ClassTypeNode> toBuilder() {
		return new Builder().setRange(getRange()).setLeadingComments(getLeadingComments())
			.setTrailingComments(getTrailingComments()).setWhitespace(getWhitespace())
			.setHints(getHints()).setParent(getParent());
	}

	@Override
	public List<ASTNode> getChildren() {
		return List.of();
	}

	@Override
	protected ASTNode withMetadata(SourceRange newRange, List<Comment> newLeadingComments,
		List<Comment> newTrailingComments, WhitespaceInfo newWhitespace, FormattingHints newHints,
		Optional<ASTNode> newParent) {
		return new ClassTypeNode(newRange, newLeadingComments, newTrailingComments,
			newWhitespace, newHints, newParent);
	}

	public static final class Builder implements ASTNodeBuilder<ClassTypeNode> {
		private SourceRange range;
		private List<Comment> leadingComments = List.of();
		private List<Comment> trailingComments = List.of();
		private WhitespaceInfo whitespace = WhitespaceInfo.none();
		private FormattingHints hints = FormattingHints.defaults();
		private Optional<ASTNode> parent = Optional.empty();

		@Override public Builder setRange(SourceRange range) { this.range = range; return this; }
		@Override public Builder setLeadingComments(List<Comment> comments) { this.leadingComments = List.copyOf(comments); return this; }
		@Override public Builder setTrailingComments(List<Comment> comments) { this.trailingComments = List.copyOf(comments); return this; }
		@Override public Builder setWhitespace(WhitespaceInfo whitespace) { this.whitespace = whitespace; return this; }
		@Override public Builder setHints(FormattingHints hints) { this.hints = hints; return this; }
		@Override public Builder setParent(Optional<ASTNode> parent) { this.parent = parent; return this; }
		@Override public Builder addLeadingComment(Comment comment) { var newComments = new java.util.ArrayList<>(leadingComments); newComments.add(comment); this.leadingComments = List.copyOf(newComments); return this; }
		@Override public Builder addTrailingComment(Comment comment) { var newComments = new java.util.ArrayList<>(trailingComments); newComments.add(comment); this.trailingComments = List.copyOf(newComments); return this; }

		@Override
		public ClassTypeNode build() {
			if (!isValid()) throw new IllegalStateException("Invalid builder state: " + String.join(", ", getValidationErrors()));
			return new ClassTypeNode(range, leadingComments, trailingComments, whitespace, hints, parent);
		}

		@Override public Builder reset() { range = null; leadingComments = List.of(); trailingComments = List.of(); whitespace = WhitespaceInfo.none(); hints = FormattingHints.defaults(); parent = Optional.empty(); return this; }
		@Override public Builder copy() { return new Builder().setRange(range).setLeadingComments(leadingComments).setTrailingComments(trailingComments).setWhitespace(whitespace).setHints(hints).setParent(parent); }
		@Override public boolean isValid() { return range != null; }
		@Override public List<String> getValidationErrors() { var errors = new java.util.ArrayList<String>(); if (range == null) errors.add("range is required"); return errors; }
	}
}
