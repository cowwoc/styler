package io.github.cowwoc.styler.formatter;

import io.github.cowwoc.styler.ast.core.NodeArena;
import io.github.cowwoc.styler.ast.core.NodeIndex;
import io.github.cowwoc.styler.security.SecurityConfig;
import io.github.cowwoc.styler.security.exceptions.ExecutionTimeoutException;
import java.nio.file.Path;

/**
 * Context provided to formatting rules during AST analysis and transformation.
 * Provides secure access to the AST and source file metadata.
 * <p>
 * <b>Thread-safety</b>: Implementations must be thread-safe for read operations.
 * Write operations (transformation) must be synchronized externally.
 */
public interface TransformationContext
{
	/**
	 * Returns the AST node arena for traversal.
	 *
	 * @return the node arena
	 */
	NodeArena arena();

	/**
	 * Returns the root node of the AST.
	 *
	 * @return the root node index
	 */
	NodeIndex rootNode();

	/**
	 * Returns the original source code.
	 *
	 * @return the source code
	 */
	String sourceCode();

	/**
	 * Returns the path to the source file.
	 *
	 * @return the file path
	 */
	Path filePath();

	/**
	 * Returns the security configuration for resource limits.
	 *
	 * @return the security configuration
	 */
	SecurityConfig securityConfig();

	/**
	 * Extracts text from the source code for the given node.
	 *
	 * @param nodeIndex the node to extract text for
	 * @return the source text for the node
	 * @throws NullPointerException if {@code nodeIndex} is null
	 * @throws IllegalArgumentException if {@code nodeIndex} is invalid
	 */
	String getSourceText(NodeIndex nodeIndex);

	/**
	 * Returns the line number (1-based) for a character position.
	 *
	 * @param position the character offset in source
	 * @return the line number
	 * @throws IllegalArgumentException if position is out of bounds
	 */
	int getLineNumber(int position);

	/**
	 * Returns the column number (1-based) for a character position.
	 *
	 * @param position the character offset in source
	 * @return the column number
	 * @throws IllegalArgumentException if position is out of bounds
	 */
	int getColumnNumber(int position);

	/**
	 * Checks if the execution deadline from {@link #securityConfig()} has been exceeded.
	 * Rules should call this periodically during long operations.
	 *
	 * @throws ExecutionTimeoutException if the deadline is exceeded
	 */
	void checkDeadline();
}
