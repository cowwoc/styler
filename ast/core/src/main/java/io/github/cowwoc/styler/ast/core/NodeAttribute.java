package io.github.cowwoc.styler.ast.core;

/**
 * Marker interface for semantic attributes attached to AST nodes.
 * <p>
 * Attributes provide semantic information that is extracted during parsing, eliminating the need
 * for formatters to parse source code strings at runtime. Only a small fraction of nodes
 * (declarations) have attributes, so storage is sparse using a separate map rather than inline
 * storage in {@link NodeArena}.
 * <p>
 * <b>Thread-safety</b>: All implementations are immutable and thread-safe.
 */
public sealed interface NodeAttribute
	permits ImportAttribute, PackageAttribute, TypeDeclarationAttribute
{
}
