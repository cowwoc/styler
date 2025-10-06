package io.github.cowwoc.styler.parser.converter;

import io.github.cowwoc.styler.ast.SourceRange;

/**
 * Exception thrown when a node's structure doesn't match the expected pattern for its type.
 * <p>
 * Examples of invalid node structure:
 * <ul>
 * <li>METHOD_DECLARATION with wrong number of children</li>
 * <li>IF_STATEMENT missing required condition expression</li>
 * <li>BINARY_EXPRESSION with non-expression children</li>
 * <li>CLASS_DECLARATION with invalid modifier combinations</li>
 * </ul>
 * </p>
 * <h2>Possible Causes</h2>
 * <ul>
 * <li>Parser bug producing malformed AST structure</li>
 * <li>Arena memory corruption</li>
 * <li>Incorrect conversion strategy logic</li>
 * </ul>
 *
 * @since 1.0
 */
public final class InvalidNodeStructureException extends ConversionException
{
	private static final long serialVersionUID = 1L;
	/**
	 * Creates an InvalidNodeStructureException with detailed validation failure message.
	 *
	 * @param message description of the structural violation
	 * @param nodeId the node ID with invalid structure
	 * @param nodeType the node type byte constant
	 * @param sourceRange the source range of the problematic node
	 */
	public InvalidNodeStructureException(String message, int nodeId, byte nodeType,
		SourceRange sourceRange)
{
		super("Invalid node structure: " + message, nodeId, nodeType, sourceRange);
	}

	/**
	 * Creates an InvalidNodeStructureException with cause.
	 *
	 * @param message description of the structural violation
	 * @param nodeId the node ID with invalid structure
	 * @param nodeType the node type byte constant
	 * @param sourceRange the source range of the problematic node
	 * @param cause the underlying cause
	 */
	public InvalidNodeStructureException(String message, int nodeId, byte nodeType,
		SourceRange sourceRange, Throwable cause)
{
		super("Invalid node structure: " + message, nodeId, nodeType, sourceRange, cause);
	}
}
