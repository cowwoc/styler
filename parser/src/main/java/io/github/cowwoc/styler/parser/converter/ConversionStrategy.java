package io.github.cowwoc.styler.parser.converter;

import io.github.cowwoc.styler.ast.ASTNode;
import io.github.cowwoc.styler.parser.ArenaNodeStorage;

/**
 * Strategy interface for converting Arena-allocated nodes to typed AST nodes.
 * <p>
 * Each node type has a dedicated ConversionStrategy implementation that knows how to:
 * <ul>
 * <li>Extract fields from Arena node structure</li>
 * <li>Recursively convert child nodes</li>
 * <li>Build the appropriate AST node with all required metadata</li>
 * <li>Validate node structure and throw descriptive exceptions on errors</li>
 * </ul>
 * </p>
 * <h2>Implementation Requirements</h2>
 * <ul>
 * <li><strong>Stateless:</strong> Strategies must not store conversion state (use ConversionContext parameter)</li>
 * <li><strong>Thread-safe:</strong> Strategies must be safe for concurrent use from multiple threads</li>
 * <li><strong>Validation:</strong> Validate node structure and throw
 * {@link InvalidNodeStructureException} on errors</li>
 * <li><strong>Type safety:</strong> Return the specific AST node type via generics</li>
 * </ul>
 * <h2>Strategy Pattern Benefits</h2>
 * <ul>
 * <li>Single Responsibility: Each strategy handles exactly one node type</li>
 * <li>Open/Closed Principle: New node types can be added without modifying existing strategies</li>
 * <li>Type Safety: Generic return type prevents casting errors</li>
 * <li>Testability: Each strategy can be tested in isolation</li>
 * </ul>
 *
 * @param <T> the specific AST node type this strategy produces
 * @since 1.0
 */
public interface ConversionStrategy<T extends ASTNode>
{
	/**
	 * Converts an Arena node to its corresponding AST node.
	 * <p>
	 * This method is responsible for:
	 * <ul>
	 * <li>Reading node data from ArenaNodeStorage</li>
	 * <li>Extracting children and recursively converting them via context</li>
	 * <li>Mapping source positions via context's SourcePositionMapper</li>
	 * <li>Building the typed AST node with all required fields</li>
	 * <li>Validating node structure and throwing exceptions on errors</li>
	 * </ul>
	 * </p>
	 *
	 * @param nodeId the Arena node ID to convert
	 * @param nodeStorage the ArenaNodeStorage containing node data
	 * @param context the conversion context providing helper services
	 * @return the converted AST node of type {@code T}
	 * @throws InvalidNodeStructureException if node structure is invalid for this type
	 * @throws ArenaClosedException if Arena has been closed
	 * @throws ConversionException if conversion fails for any other reason
	 */
	T convert(
		int nodeId,
		ArenaNodeStorage nodeStorage,
		ConversionContext context);

	/**
	 * Gets the node type byte constant this strategy handles.
	 * Used for strategy registry validation to ensure all node types are covered.
	 *
	 * @return the node type byte constant (from {@link io.github.cowwoc.styler.parser.NodeType})
	 */
	byte getHandledNodeType();

	/**
	 * Gets the AST node class this strategy produces.
	 * Used for type checking and diagnostic messaging.
	 *
	 * @return the AST node class
	 */
	Class<T> getProducedNodeClass();
}
