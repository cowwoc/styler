package io.github.cowwoc.styler.ast.node;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;
import io.github.cowwoc.styler.ast.*;
import io.github.cowwoc.styler.ast.visitor.ASTVisitor;
import io.github.cowwoc.styler.ast.builder.ASTNodeBuilder;

import java.util.List;
import java.util.Optional;

/**
 * AST node representing a binary expression.
 */
public final class BinaryExpressionNode extends ASTNode {
	private final ASTNode left;
	private final String operator;
	private final ASTNode right;

	public BinaryExpressionNode(SourceRange range, List<Comment> leadingComments,
		List<Comment> trailingComments, WhitespaceInfo whitespace, FormattingHints hints,
		Optional<ASTNode> parent, ASTNode left, String operator, ASTNode right) {
		super(range, leadingComments, trailingComments, whitespace, hints, parent);
		requireThat(left, "left").isNotNull();
		requireThat(operator, "operator").isNotNull().isNotBlank();
		requireThat(right, "right").isNotNull();
		this.left = left;
		this.operator = operator;
		this.right = right;
	}

	public ASTNode getLeft() { return left; }
	public String getOperator() { return operator; }
	public ASTNode getRight() { return right; }

	@Override
	public <R, A> R accept(ASTVisitor<R, A> visitor, final A arg) {
		return visitor.visitBinaryExpression(this, arg);
	}

	@Override
	public ASTNodeBuilder<BinaryExpressionNode> toBuilder() {
		return new Builder().setRange(getRange()).setLeadingComments(getLeadingComments())
			.setTrailingComments(getTrailingComments()).setWhitespace(getWhitespace())
			.setHints(getHints()).setParent(getParent()).setLeft(left).setOperator(operator).setRight(right);
	}

	@Override
	public List<ASTNode> getChildren() {
		return List.of(left, right);
	}

	@Override
	protected ASTNode withMetadata(SourceRange newRange, List<Comment> newLeadingComments,
		List<Comment> newTrailingComments, WhitespaceInfo newWhitespace, FormattingHints newHints,
		Optional<ASTNode> newParent) {
		return new BinaryExpressionNode(newRange, newLeadingComments, newTrailingComments,
			newWhitespace, newHints, newParent, left, operator, right);
	}

	public static final class Builder implements ASTNodeBuilder<BinaryExpressionNode> {
		private SourceRange range;
		private List<Comment> leadingComments = List.of();
		private List<Comment> trailingComments = List.of();
		private WhitespaceInfo whitespace = WhitespaceInfo.none();
		private FormattingHints hints = FormattingHints.defaults();
		private Optional<ASTNode> parent = Optional.empty();
		private ASTNode left;
		private String operator;
		private ASTNode right;

		@Override public Builder setRange(SourceRange range) { this.range = range; return this; }
		@Override public Builder setLeadingComments(List<Comment> comments) { this.leadingComments = List.copyOf(comments); return this; }
		@Override public Builder setTrailingComments(List<Comment> comments) { this.trailingComments = List.copyOf(comments); return this; }
		@Override public Builder setWhitespace(WhitespaceInfo whitespace) { this.whitespace = whitespace; return this; }
		@Override public Builder setHints(FormattingHints hints) { this.hints = hints; return this; }
		@Override public Builder setParent(Optional<ASTNode> parent) { this.parent = parent; return this; }
		@Override public Builder addLeadingComment(Comment comment) { var newComments = new java.util.ArrayList<>(leadingComments); newComments.add(comment); this.leadingComments = List.copyOf(newComments); return this; }
		@Override public Builder addTrailingComment(Comment comment) { var newComments = new java.util.ArrayList<>(trailingComments); newComments.add(comment); this.trailingComments = List.copyOf(newComments); return this; }

		public Builder setLeft(ASTNode left) { this.left = left; return this; }
		public Builder setOperator(String operator) { this.operator = operator; return this; }
		public Builder setRight(ASTNode right) { this.right = right; return this; }

		@Override
		public BinaryExpressionNode build() {
			if (!isValid()) throw new IllegalStateException("Invalid builder state: " + String.join(", ", getValidationErrors()));
			return new BinaryExpressionNode(range, leadingComments, trailingComments, whitespace, hints, parent, left, operator, right);
		}

		@Override public Builder reset() { range = null; leadingComments = List.of(); trailingComments = List.of(); whitespace = WhitespaceInfo.none(); hints = FormattingHints.defaults(); parent = Optional.empty(); left = null; operator = null; right = null; return this; }
		@Override public Builder copy() { return new Builder().setRange(range).setLeadingComments(leadingComments).setTrailingComments(trailingComments).setWhitespace(whitespace).setHints(hints).setParent(parent).setLeft(left).setOperator(operator).setRight(right); }
		@Override public boolean isValid() { return range != null && left != null && operator != null && !operator.isBlank() && right != null; }
		@Override public List<String> getValidationErrors() { var errors = new java.util.ArrayList<String>(); if (range == null) errors.add("range is required"); if (left == null) errors.add("left operand final is required"); if (operator == null || operator.isBlank()) errors.add("operator is required and cannot final be blank"); if (right == null) errors.add("right operand final is required"); return errors; }
	}
}
