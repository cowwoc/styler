package io.github.cowwoc.styler.parser.converter;

import io.github.cowwoc.styler.ast.SourceRange;

/**
 * Exception thrown when attempting to convert a node type that is not yet implemented.
 * <p>
 * This exception should NEVER occur in production if all 81 node types are properly registered
 * in the StrategyRegistry. Its presence indicates either:
 * <ul>
 * <li>A new node type was added to the parser but no conversion strategy exists</li>
 * <li>The StrategyRegistry validation failed to catch a missing implementation</li>
 * </ul>
 * </p>
 * <h2>Resolution</h2>
 * Create a {@link ConversionStrategy} implementation for the missing node type and register it
 * in the StrategyRegistry.
 *
 * @since 1.0
 */
public final class UnsupportedNodeTypeException extends ConversionException
{
	private static final long serialVersionUID = 1L;
	/**
	 * Creates an UnsupportedNodeTypeException.
	 *
	 * @param nodeId the node ID with unsupported type
	 * @param nodeType the unsupported node type byte constant
	 * @param sourceRange the source range of the node
	 */
	public UnsupportedNodeTypeException(int nodeId, byte nodeType,
		SourceRange sourceRange)
{
		super(
			"No conversion strategy registered for this node type. " +
			"This indicates a missing implementation in the converter.",
			nodeId,
			nodeType,
			sourceRange);
	}
}
