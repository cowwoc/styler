package io.github.cowwoc.styler.ast.node;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;
import io.github.cowwoc.styler.ast.*;
import io.github.cowwoc.styler.ast.visitor.ASTVisitor;
import io.github.cowwoc.styler.ast.builder.ASTNodeBuilder;

import java.util.List;
import java.util.Optional;

/**
 * AST node representing an if statement.
 */
public final class IfStatementNode extends ASTNode {
	private final ASTNode condition;
	private final ASTNode thenStatement;
	private final Optional<ASTNode> elseStatement;

	public IfStatementNode(SourceRange range, List<Comment> leadingComments,
		List<Comment> trailingComments, WhitespaceInfo whitespace, FormattingHints hints,
		Optional<ASTNode> parent, ASTNode condition, ASTNode thenStatement, Optional<ASTNode> elseStatement) {
		super(range, leadingComments, trailingComments, whitespace, hints, parent);
		requireThat(condition, "condition").isNotNull();
		requireThat(thenStatement, "thenStatement").isNotNull();
		requireThat(elseStatement, "elseStatement").isNotNull();
		this.condition = condition;
		this.thenStatement = thenStatement;
		this.elseStatement = elseStatement;
	}

	public ASTNode getCondition() { return condition; }
	public ASTNode getThenStatement() { return thenStatement; }
	public Optional<ASTNode> getElseStatement() { return elseStatement; }

	@Override
	public <R, A> R accept(ASTVisitor<R, A> visitor, final A arg) {
		return visitor.visitIfStatement(this, arg);
	}

	@Override
	public ASTNodeBuilder<IfStatementNode> toBuilder() {
		return new Builder().setRange(getRange()).setLeadingComments(getLeadingComments())
			.setTrailingComments(getTrailingComments()).setWhitespace(getWhitespace())
			.setHints(getHints()).setParent(getParent()).setCondition(condition).setThenStatement(thenStatement).setElseStatement(elseStatement);
	}

	@Override
	public List<ASTNode> getChildren() {
		var children = new java.util.ArrayList<ASTNode>();
		children.add(condition);
		children.add(thenStatement);
		elseStatement.ifPresent(children::add);
		return List.copyOf(children);
	}

	@Override
	protected ASTNode withMetadata(SourceRange newRange, List<Comment> newLeadingComments,
		List<Comment> newTrailingComments, WhitespaceInfo newWhitespace, FormattingHints newHints,
		Optional<ASTNode> newParent) {
		return new IfStatementNode(newRange, newLeadingComments, newTrailingComments,
			newWhitespace, newHints, newParent, condition, thenStatement, elseStatement);
	}

	public static final class Builder implements ASTNodeBuilder<IfStatementNode> {
		private SourceRange range;
		private List<Comment> leadingComments = List.of();
		private List<Comment> trailingComments = List.of();
		private WhitespaceInfo whitespace = WhitespaceInfo.none();
		private FormattingHints hints = FormattingHints.defaults();
		private Optional<ASTNode> parent = Optional.empty();
		private ASTNode condition;
		private ASTNode thenStatement;
		private Optional<ASTNode> elseStatement = Optional.empty();

		@Override public Builder setRange(SourceRange range) { this.range = range; return this; }
		@Override public Builder setLeadingComments(List<Comment> comments) { this.leadingComments = List.copyOf(comments); return this; }
		@Override public Builder setTrailingComments(List<Comment> comments) { this.trailingComments = List.copyOf(comments); return this; }
		@Override public Builder setWhitespace(WhitespaceInfo whitespace) { this.whitespace = whitespace; return this; }
		@Override public Builder setHints(FormattingHints hints) { this.hints = hints; return this; }
		@Override public Builder setParent(Optional<ASTNode> parent) { this.parent = parent; return this; }
		@Override public Builder addLeadingComment(Comment comment) { var newComments = new java.util.ArrayList<>(leadingComments); newComments.add(comment); this.leadingComments = List.copyOf(newComments); return this; }
		@Override public Builder addTrailingComment(Comment comment) { var newComments = new java.util.ArrayList<>(trailingComments); newComments.add(comment); this.trailingComments = List.copyOf(newComments); return this; }

		public Builder setCondition(ASTNode condition) { this.condition = condition; return this; }
		public Builder setThenStatement(ASTNode thenStatement) { this.thenStatement = thenStatement; return this; }
		public Builder setElseStatement(Optional<ASTNode> elseStatement) { this.elseStatement = elseStatement; return this; }

		@Override
		public IfStatementNode build() {
			if (!isValid()) throw new IllegalStateException("Invalid builder state: " + String.join(", ", getValidationErrors()));
			return new IfStatementNode(range, leadingComments, trailingComments, whitespace, hints, parent, condition, thenStatement, elseStatement);
		}

		@Override public Builder reset() { range = null; leadingComments = List.of(); trailingComments = List.of(); whitespace = WhitespaceInfo.none(); hints = FormattingHints.defaults(); parent = Optional.empty(); condition = null; thenStatement = null; elseStatement = Optional.empty(); return this; }
		@Override public Builder copy() { return new Builder().setRange(range).setLeadingComments(leadingComments).setTrailingComments(trailingComments).setWhitespace(whitespace).setHints(hints).setParent(parent).setCondition(condition).setThenStatement(thenStatement).setElseStatement(elseStatement); }
		@Override public boolean isValid() { return range != null && condition != null && thenStatement != null; }
		@Override public List<String> getValidationErrors() { var errors = new java.util.ArrayList<String>(); if (range == null) errors.add("range is required"); if (condition == null) errors.add("condition final is required"); if (thenStatement == null) errors.add("thenStatement final is required"); return errors; }
	}
}
