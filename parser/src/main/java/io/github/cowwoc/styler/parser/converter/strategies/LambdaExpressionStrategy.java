package io.github.cowwoc.styler.parser.converter.strategies;

import io.github.cowwoc.styler.ast.node.LambdaExpressionNode;
import io.github.cowwoc.styler.parser.ArenaNodeStorage;
import io.github.cowwoc.styler.parser.NodeType;
import io.github.cowwoc.styler.parser.converter.ConversionContext;

/**
 * Conversion strategy for lambda expression nodes.
 *
 * @since 1.0
 */
public final class LambdaExpressionStrategy extends BaseConversionStrategy<LambdaExpressionNode>
{
	/**
	 * Creates a new conversion strategy for this node type.
	 */
	public LambdaExpressionStrategy()
	{
		super(NodeType.LAMBDA_EXPRESSION, LambdaExpressionNode.class);
	}

	@Override
	public LambdaExpressionNode convert(int nodeId, ArenaNodeStorage nodeStorage,
		ConversionContext context)
	{
		ArenaNodeStorage.NodeInfo nodeInfo = nodeStorage.getNode(nodeId);

		return new LambdaExpressionNode(
			getSourceRange(nodeInfo, context),
			context.getLeadingComments(),
			context.getTrailingComments(),
			context.getWhitespace(),
			context.getFormattingHints(),
			context.getParentNode());
	}
}
