package io.github.cowwoc.styler.parser.converter.strategies;

import io.github.cowwoc.styler.ast.node.ArrayInitializerNode;
import io.github.cowwoc.styler.parser.ArenaNodeStorage;
import io.github.cowwoc.styler.parser.NodeType;
import io.github.cowwoc.styler.parser.converter.ConversionContext;

/**
 * Conversion strategy for array initializer nodes.
 *
 * @since 1.0
 */
public final class ArrayInitializerStrategy extends BaseConversionStrategy<ArrayInitializerNode>
{
	/**
	 * Creates a new conversion strategy for this node type.
	 */
	public ArrayInitializerStrategy()
	{
		super(NodeType.ARRAY_CREATION_EXPRESSION, ArrayInitializerNode.class);
	}

	@Override
	public ArrayInitializerNode convert(int nodeId, ArenaNodeStorage nodeStorage,
		ConversionContext context)
	{
		ArenaNodeStorage.NodeInfo nodeInfo = nodeStorage.getNode(nodeId);

		return new ArrayInitializerNode(
			getSourceRange(nodeInfo, context),
			context.getLeadingComments(),
			context.getTrailingComments(),
			context.getWhitespace(),
			context.getFormattingHints(),
			context.getParentNode());
	}
}
