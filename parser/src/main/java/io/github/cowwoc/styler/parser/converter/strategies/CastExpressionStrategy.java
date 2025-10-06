package io.github.cowwoc.styler.parser.converter.strategies;

import io.github.cowwoc.styler.ast.node.CastExpressionNode;
import io.github.cowwoc.styler.parser.ArenaNodeStorage;
import io.github.cowwoc.styler.parser.NodeType;
import io.github.cowwoc.styler.parser.converter.ConversionContext;

/**
 * Conversion strategy for cast expression nodes.
 *
 * @since 1.0
 */
public final class CastExpressionStrategy extends BaseConversionStrategy<CastExpressionNode>
{
	/**
	 * Creates a new conversion strategy for this node type.
	 */
	public CastExpressionStrategy()
	{
		super(NodeType.CAST_EXPRESSION, CastExpressionNode.class);
	}

	@Override
	public CastExpressionNode convert(int nodeId, ArenaNodeStorage nodeStorage,
		ConversionContext context)
	{
		ArenaNodeStorage.NodeInfo nodeInfo = nodeStorage.getNode(nodeId);

		return new CastExpressionNode(
			getSourceRange(nodeInfo, context),
			context.getLeadingComments(),
			context.getTrailingComments(),
			context.getWhitespace(),
			context.getFormattingHints(),
			context.getParentNode());
	}
}
