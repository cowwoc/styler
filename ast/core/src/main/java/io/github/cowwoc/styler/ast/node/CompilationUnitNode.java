package io.github.cowwoc.styler.ast.node;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;
import io.github.cowwoc.styler.ast.*;
import io.github.cowwoc.styler.ast.visitor.ASTVisitor;
import io.github.cowwoc.styler.ast.builder.ASTNodeBuilder;

import java.util.List;
import java.util.Optional;

public final class CompilationUnitNode extends ASTNode {
	private final Optional<ASTNode> packageDeclaration;
	private final List<ASTNode> imports;
	private final List<ASTNode> typeDeclarations;

	public CompilationUnitNode(SourceRange range, List<Comment> leadingComments,
		List<Comment> trailingComments, WhitespaceInfo whitespace, FormattingHints hints,
		Optional<ASTNode> parent, Optional<ASTNode> packageDeclaration, List<ASTNode> imports, List<ASTNode> typeDeclarations) {
		super(range, leadingComments, trailingComments, whitespace, hints, parent);
		requireThat(packageDeclaration, "packageDeclaration").isNotNull();
		requireThat(imports, "imports").isNotNull();
		requireThat(typeDeclarations, "typeDeclarations").isNotNull();
		this.packageDeclaration = packageDeclaration;
		this.imports = List.copyOf(imports);
		this.typeDeclarations = List.copyOf(typeDeclarations);
	}

	public Optional<ASTNode> getPackageDeclaration() { return packageDeclaration; }
	public List<ASTNode> getImports() { return imports; }
	public List<ASTNode> getTypeDeclarations() { return typeDeclarations; }

	@Override
	public <R, A> R accept(ASTVisitor<R, A> visitor, final A arg) { return visitor.visitCompilationUnit(this, arg); }

	@Override
	public ASTNodeBuilder<CompilationUnitNode> toBuilder() {
		return new Builder().setRange(getRange()).setLeadingComments(getLeadingComments())
			.setTrailingComments(getTrailingComments()).setWhitespace(getWhitespace())
			.setHints(getHints()).setParent(getParent()).setPackageDeclaration(packageDeclaration)
			.setImports(imports).setTypeDeclarations(typeDeclarations);
	}

	@Override
	public List<ASTNode> getChildren() {
		var children = new java.util.ArrayList<ASTNode>();
		packageDeclaration.ifPresent(children::add);
		children.addAll(imports);
		children.addAll(typeDeclarations);
		return List.copyOf(children);
	}

	@Override
	protected ASTNode withMetadata(SourceRange newRange, List<Comment> newLeadingComments,
		List<Comment> newTrailingComments, WhitespaceInfo newWhitespace, FormattingHints newHints,
		Optional<ASTNode> newParent) {
		return new CompilationUnitNode(newRange, newLeadingComments, newTrailingComments,
			newWhitespace, newHints, newParent, packageDeclaration, imports, typeDeclarations);
	}

	public static final class Builder implements ASTNodeBuilder<CompilationUnitNode> {
		private SourceRange range;
		private List<Comment> leadingComments = List.of();
		private List<Comment> trailingComments = List.of();
		private WhitespaceInfo whitespace = WhitespaceInfo.none();
		private FormattingHints hints = FormattingHints.defaults();
		private Optional<ASTNode> parent = Optional.empty();
		private Optional<ASTNode> packageDeclaration = Optional.empty();
		private List<ASTNode> imports = List.of();
		private List<ASTNode> typeDeclarations = List.of();

		@Override public Builder setRange(SourceRange range) { this.range = range; return this; }
		@Override public Builder setLeadingComments(List<Comment> comments) { this.leadingComments = List.copyOf(comments); return this; }
		@Override public Builder setTrailingComments(List<Comment> comments) { this.trailingComments = List.copyOf(comments); return this; }
		@Override public Builder setWhitespace(WhitespaceInfo whitespace) { this.whitespace = whitespace; return this; }
		@Override public Builder setHints(FormattingHints hints) { this.hints = hints; return this; }
		@Override public Builder setParent(Optional<ASTNode> parent) { this.parent = parent; return this; }
		@Override public Builder addLeadingComment(Comment comment) { var newComments = new java.util.ArrayList<>(leadingComments); newComments.add(comment); this.leadingComments = List.copyOf(newComments); return this; }
		@Override public Builder addTrailingComment(Comment comment) { var newComments = new java.util.ArrayList<>(trailingComments); newComments.add(comment); this.trailingComments = List.copyOf(newComments); return this; }

		public Builder setPackageDeclaration(Optional<ASTNode> packageDeclaration) { this.packageDeclaration = packageDeclaration; return this; }
		public Builder setImports(List<ASTNode> imports) { this.imports = List.copyOf(imports); return this; }
		public Builder setTypeDeclarations(List<ASTNode> typeDeclarations) { this.typeDeclarations = List.copyOf(typeDeclarations); return this; }

		@Override
		public CompilationUnitNode build() {
			if (!isValid()) throw new IllegalStateException("Invalid builder state: " + String.join(", ", getValidationErrors()));
			return new CompilationUnitNode(range, leadingComments, trailingComments, whitespace, hints, parent, packageDeclaration, imports, typeDeclarations);
		}

		@Override public Builder reset() { range = null; leadingComments = List.of(); trailingComments = List.of(); whitespace = WhitespaceInfo.none(); hints = FormattingHints.defaults(); parent = Optional.empty(); packageDeclaration = Optional.empty(); imports = List.of(); typeDeclarations = List.of(); return this; }
		@Override public Builder copy() { return new Builder().setRange(range).setLeadingComments(leadingComments).setTrailingComments(trailingComments).setWhitespace(whitespace).setHints(hints).setParent(parent).setPackageDeclaration(packageDeclaration).setImports(imports).setTypeDeclarations(typeDeclarations); }
		@Override public boolean isValid() { return range != null; }
		@Override public List<String> getValidationErrors() { var errors = new java.util.ArrayList<String>(); if (range == null) errors.add("range is required"); return errors; }
	}
}
