package io.github.cowwoc.styler.parser.converter;

import io.github.cowwoc.styler.ast.SourceRange;
import io.github.cowwoc.styler.parser.NodeType;

/**
 * Base exception for Arena-to-AST conversion failures.
 * <p>
 * Provides rich error context including node ID, node type, source range, and conversion phase.
 * All conversion exceptions include complete diagnostic information to aid debugging.
 * </p>
 * <h2>Exception Hierarchy</h2>
 * <ul>
 * <li>{@link UnsupportedNodeTypeException} - Node type not yet implemented</li>
 * <li>{@link InvalidNodeStructureException} - Node structure doesn't match expected pattern</li>
 * <li>{@link ArenaClosedException} - Attempted conversion after Arena closed</li>
 * </ul>
 *
 * @since 1.0
 */
public class ConversionException extends RuntimeException
{
	private static final long serialVersionUID = 1L;

	private final int nodeId;
	private final byte nodeType;
	private final transient SourceRange sourceRange;

	/**
	 * Creates a ConversionException with full context.
	 *
	 * @param message detailed error message
	 * @param nodeId the node ID being converted
	 * @param nodeType the node type byte constant
	 * @param sourceRange the source range of the problematic node
	 * @param cause the underlying cause, or {@code null}
	 */
	public ConversionException(String message, int nodeId, byte nodeType,
		SourceRange sourceRange, Throwable cause)
{
		super(buildMessage(message, nodeId, nodeType, sourceRange), cause);
		this.nodeId = nodeId;
		this.nodeType = nodeType;
		this.sourceRange = sourceRange;
	}

	/**
	 * Creates a ConversionException without underlying cause.
	 *
	 * @param message detailed error message
	 * @param nodeId the node ID being converted
	 * @param nodeType the node type byte constant
	 * @param sourceRange the source range of the problematic node
	 */
	public ConversionException(String message, int nodeId, byte nodeType,
		SourceRange sourceRange)
{
		this(message, nodeId, nodeType, sourceRange, null);
	}

	/**
	 * Gets the node ID that failed conversion.
	 *
	 * @return the node ID
	 */
	public int getNodeId()
{
		return nodeId;
	}

	/**
	 * Gets the node type that failed conversion.
	 *
	 * @return the node type byte constant
	 */
	public byte getNodeType()
{
		return nodeType;
	}

	/**
	 * Gets the source range where the error occurred.
	 *
	 * @return the source range
	 */
	public SourceRange getSourceRange()
{
		return sourceRange;
	}

	/**
	 * Builds comprehensive error message with node context.
	 *
	 * @param message the base error message
	 * @param nodeId the node ID
	 * @param nodeType the node type
	 * @param sourceRange the source range
	 * @return formatted error message
	 */
	@SuppressWarnings({"PMD.InsufficientStringBufferDeclaration", "PMD.ConsecutiveAppendsShouldReuse",
		"PMD.AppendCharacterWithChar"})
	private static String buildMessage(String message, int nodeId, byte nodeType,
		SourceRange sourceRange)
{
		StringBuilder sb = new StringBuilder();
		sb.append(message);
		sb.append("\n");
		sb.append("  Node ID: ").append(nodeId).append("\n");
		sb.append("  Node Type: ").append(NodeType.getTypeName(nodeType)).append(" (").append(nodeType).append(")\n");
		sb.append("  Source Range: ").append(sourceRange);

		return sb.toString();
	}
}
