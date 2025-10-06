package io.github.cowwoc.styler.parser.converter.strategies;

import io.github.cowwoc.styler.ast.node.InstanceofExpressionNode;
import io.github.cowwoc.styler.parser.ArenaNodeStorage;
import io.github.cowwoc.styler.parser.NodeType;
import io.github.cowwoc.styler.parser.converter.ConversionContext;

/**
 * Conversion strategy for instanceof expression nodes.
 *
 * @since 1.0
 */
public final class InstanceofExpressionStrategy
	extends BaseConversionStrategy<InstanceofExpressionNode>
{
	/**
	 * Creates a new conversion strategy for this node type.
	 */
	public InstanceofExpressionStrategy()
	{
		super(NodeType.INSTANCEOF_EXPRESSION, InstanceofExpressionNode.class);
	}

	@Override
	public InstanceofExpressionNode convert(int nodeId, ArenaNodeStorage nodeStorage,
		ConversionContext context)
	{
		ArenaNodeStorage.NodeInfo nodeInfo = nodeStorage.getNode(nodeId);

		return new InstanceofExpressionNode(
			getSourceRange(nodeInfo, context),
			context.getLeadingComments(),
			context.getTrailingComments(),
			context.getWhitespace(),
			context.getFormattingHints(),
			context.getParentNode());
	}
}
