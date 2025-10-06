package io.github.cowwoc.styler.parser.converter.strategies;

import io.github.cowwoc.styler.ast.node.UnaryExpressionNode;
import io.github.cowwoc.styler.parser.ArenaNodeStorage;
import io.github.cowwoc.styler.parser.NodeType;
import io.github.cowwoc.styler.parser.converter.ConversionContext;

/**
 * Conversion strategy for unary expression nodes.
 *
 * @since 1.0
 */
public final class UnaryExpressionStrategy extends BaseConversionStrategy<UnaryExpressionNode>
{
	/**
	 * Creates a new conversion strategy for this node type.
	 */
	public UnaryExpressionStrategy()
	{
		super(NodeType.UNARY_EXPRESSION, UnaryExpressionNode.class);
	}

	@Override
	public UnaryExpressionNode convert(int nodeId, ArenaNodeStorage nodeStorage,
		ConversionContext context)
	{
		ArenaNodeStorage.NodeInfo nodeInfo = nodeStorage.getNode(nodeId);

		return new UnaryExpressionNode(
			getSourceRange(nodeInfo, context),
			context.getLeadingComments(),
			context.getTrailingComments(),
			context.getWhitespace(),
			context.getFormattingHints(),
			context.getParentNode());
	}
}
