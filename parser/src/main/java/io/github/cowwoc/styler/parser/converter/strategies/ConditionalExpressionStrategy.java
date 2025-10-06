package io.github.cowwoc.styler.parser.converter.strategies;

import io.github.cowwoc.styler.ast.node.ConditionalExpressionNode;
import io.github.cowwoc.styler.parser.ArenaNodeStorage;
import io.github.cowwoc.styler.parser.NodeType;
import io.github.cowwoc.styler.parser.converter.ConversionContext;

/**
 * Conversion strategy for conditional expression nodes.
 *
 * @since 1.0
 */
public final class ConditionalExpressionStrategy
	extends BaseConversionStrategy<ConditionalExpressionNode>
{
	/**
	 * Creates a new conversion strategy for this node type.
	 */
	public ConditionalExpressionStrategy()
	{
		super(NodeType.CONDITIONAL_EXPRESSION, ConditionalExpressionNode.class);
	}

	@Override
	public ConditionalExpressionNode convert(int nodeId, ArenaNodeStorage nodeStorage,
		ConversionContext context)
	{
		ArenaNodeStorage.NodeInfo nodeInfo = nodeStorage.getNode(nodeId);

		return new ConditionalExpressionNode(
			getSourceRange(nodeInfo, context),
			context.getLeadingComments(),
			context.getTrailingComments(),
			context.getWhitespace(),
			context.getFormattingHints(),
			context.getParentNode());
	}
}
