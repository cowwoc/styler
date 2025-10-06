package io.github.cowwoc.styler.parser.converter.strategies;

import io.github.cowwoc.styler.ast.node.ArrayAccessNode;
import io.github.cowwoc.styler.parser.ArenaNodeStorage;
import io.github.cowwoc.styler.parser.NodeType;
import io.github.cowwoc.styler.parser.converter.ConversionContext;

/**
 * Conversion strategy for array access nodes.
 *
 * @since 1.0
 */
public final class ArrayAccessStrategy extends BaseConversionStrategy<ArrayAccessNode>
{
	/**
	 * Creates a new conversion strategy for this node type.
	 */
	public ArrayAccessStrategy()
	{
		super(NodeType.ARRAY_ACCESS_EXPRESSION, ArrayAccessNode.class);
	}

	@Override
	public ArrayAccessNode convert(int nodeId, ArenaNodeStorage nodeStorage,
		ConversionContext context)
	{
		ArenaNodeStorage.NodeInfo nodeInfo = nodeStorage.getNode(nodeId);

		return new ArrayAccessNode(
			getSourceRange(nodeInfo, context),
			context.getLeadingComments(),
			context.getTrailingComments(),
			context.getWhitespace(),
			context.getFormattingHints(),
			context.getParentNode());
	}
}
