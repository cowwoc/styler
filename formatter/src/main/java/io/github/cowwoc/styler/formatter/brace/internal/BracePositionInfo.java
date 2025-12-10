package io.github.cowwoc.styler.formatter.brace.internal;

import io.github.cowwoc.styler.ast.core.NodeIndex;
import io.github.cowwoc.styler.ast.core.NodeType;
import io.github.cowwoc.styler.formatter.brace.BraceStyle;

/**
 * Information about a detected brace in the source code.
 * <p>
 * <b>Thread-safety</b>: This record is immutable and thread-safe.
 *
 * @param node the AST node index for this brace
 * @param nodeType the type of AST node
 * @param bracePosition the character position of the opening brace
 * @param lineNumber the line number (1-based) of the brace
 * @param columnNumber the column number (1-based) of the brace
 * @param currentStyle the detected current brace style
 * @param expectedStyle the expected brace style based on configuration
 */
public record BracePositionInfo(NodeIndex node, NodeType nodeType, int bracePosition, int lineNumber,
	int columnNumber, BraceStyle currentStyle, BraceStyle expectedStyle)
{
}
