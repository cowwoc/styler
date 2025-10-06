package io.github.cowwoc.styler.parser.converter.strategies;

import io.github.cowwoc.styler.ast.node.NewExpressionNode;
import io.github.cowwoc.styler.parser.ArenaNodeStorage;
import io.github.cowwoc.styler.parser.NodeType;
import io.github.cowwoc.styler.parser.converter.ConversionContext;

/**
 * Conversion strategy for new expression nodes.
 *
 * @since 1.0
 */
public final class NewExpressionStrategy extends BaseConversionStrategy<NewExpressionNode>
{
	/**
	 * Creates a new conversion strategy for this node type.
	 */
	public NewExpressionStrategy()
	{
		super(NodeType.NEW_EXPRESSION, NewExpressionNode.class);
	}

	@Override
	public NewExpressionNode convert(int nodeId, ArenaNodeStorage nodeStorage,
		ConversionContext context)
	{
		ArenaNodeStorage.NodeInfo nodeInfo = nodeStorage.getNode(nodeId);

		return new NewExpressionNode(
			getSourceRange(nodeInfo, context),
			context.getLeadingComments(),
			context.getTrailingComments(),
			context.getWhitespace(),
			context.getFormattingHints(),
			context.getParentNode());
	}
}
