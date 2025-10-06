package io.github.cowwoc.styler.parser.converter.strategies;

import io.github.cowwoc.styler.ast.ASTNode;
import io.github.cowwoc.styler.ast.SourceRange;
import io.github.cowwoc.styler.parser.ArenaNodeStorage;
import io.github.cowwoc.styler.parser.converter.ConversionContext;
import io.github.cowwoc.styler.parser.converter.ConversionStrategy;
import io.github.cowwoc.styler.parser.converter.InvalidNodeStructureException;

import java.util.ArrayList;
import java.util.List;

/**
 * Base class for conversion strategies providing common utility methods.
 * <p>
 * This abstract class handles common conversion patterns like child node recursion,
 * source range mapping, and metadata extraction. Concrete strategies extend this class
 * and implement node-specific construction logic.
 * </p>
 *
 * @param <T> the specific AST node type this strategy produces
 * @since 1.0
 */
public abstract class BaseConversionStrategy<T extends ASTNode> implements ConversionStrategy<T>
{
	private final byte handledNodeType;
	private final Class<T> producedNodeClass;

	/**
	 * Creates a base conversion strategy.
	 *
	 * @param handledNodeType the node type byte constant this strategy handles
	 * @param producedNodeClass the AST node class this strategy produces
	 */
	protected BaseConversionStrategy(byte handledNodeType, Class<T> producedNodeClass)
{
		this.handledNodeType = handledNodeType;
		this.producedNodeClass = producedNodeClass;
	}

	@Override
	public byte getHandledNodeType()
{
		return handledNodeType;
	}

	@Override
	public Class<T> getProducedNodeClass()
{
		return producedNodeClass;
	}

	/**
	 * Converts child nodes recursively using the conversion context.
	 *
	 * @param childIds list of child node IDs from Arena
	 * @param nodeStorage the Arena node storage
	 * @param context the conversion context
	 * @return list of converted AST child nodes
	 */
	protected List<ASTNode> convertChildren(
		List<Integer> childIds,
		ArenaNodeStorage nodeStorage,
		ConversionContext context)
{
		List<ASTNode> children = new ArrayList<>(childIds.size());
		for (int childId : childIds)
{
			children.add(context.convertNode(childId, nodeStorage));
		}
		return children;
	}

	/**
	 * Gets the source range for a node from Arena storage.
	 *
	 * @param nodeInfo the Arena node info
	 * @param context the conversion context
	 * @return the source range
	 */
	protected SourceRange getSourceRange(
		ArenaNodeStorage.NodeInfo nodeInfo,
		ConversionContext context)
{
		return context.getSourceRange(nodeInfo.startOffset(), nodeInfo.endOffset());
	}

	/**
	 * Validates that a node has the expected number of children.
	 *
	 * @param nodeInfo the Arena node info
	 * @param expectedCount the expected child count
	 * @param context the conversion context
	 * @throws InvalidNodeStructureException if child count doesn't match
	 */
	protected void validateChildCount(
		ArenaNodeStorage.NodeInfo nodeInfo,
		int expectedCount,
		ConversionContext context)
{
		int actualCount = nodeInfo.childIds().size();
		if (actualCount != expectedCount)
{
			SourceRange range = getSourceRange(nodeInfo, context);
			throw new InvalidNodeStructureException(
				"Expected " + expectedCount + " children but found " + actualCount,
				nodeInfo.nodeId(),
				nodeInfo.nodeType(),
				range);
		}
	}

	/**
	 * Validates that a node has at least the specified number of children.
	 *
	 * @param nodeInfo the Arena node info
	 * @param minCount the minimum expected child count
	 * @param context the conversion context
	 * @throws InvalidNodeStructureException if child count is less than minimum
	 */
	protected void validateMinChildCount(
		ArenaNodeStorage.NodeInfo nodeInfo,
		int minCount,
		ConversionContext context)
{
		int actualCount = nodeInfo.childIds().size();
		if (actualCount < minCount)
{
			SourceRange range = getSourceRange(nodeInfo, context);
			throw new InvalidNodeStructureException(
				"Expected at least " + minCount + " children but found " + actualCount,
				nodeInfo.nodeId(),
				nodeInfo.nodeType(),
				range);
		}
	}
}
