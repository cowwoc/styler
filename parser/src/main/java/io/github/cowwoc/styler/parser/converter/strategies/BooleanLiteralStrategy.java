package io.github.cowwoc.styler.parser.converter.strategies;

import io.github.cowwoc.styler.ast.node.BooleanLiteralNode;
import io.github.cowwoc.styler.parser.ArenaNodeStorage;
import io.github.cowwoc.styler.parser.NodeType;
import io.github.cowwoc.styler.parser.converter.ConversionContext;

/**
 * Conversion strategy for boolean literal nodes.
 *
 * @since 1.0
 */
public final class BooleanLiteralStrategy extends BaseConversionStrategy<BooleanLiteralNode>
{
	/**
	 * Creates a new conversion strategy for this node type.
	 */
	public BooleanLiteralStrategy()
	{
		super(NodeType.LITERAL_EXPRESSION, BooleanLiteralNode.class);
	}

	@Override
	public BooleanLiteralNode convert(int nodeId, ArenaNodeStorage nodeStorage,
		ConversionContext context)
	{
		ArenaNodeStorage.NodeInfo nodeInfo = nodeStorage.getNode(nodeId);

		return new BooleanLiteralNode(
			getSourceRange(nodeInfo, context),
			context.getLeadingComments(),
			context.getTrailingComments(),
			context.getWhitespace(),
			context.getFormattingHints(),
			context.getParentNode());
	}
}
