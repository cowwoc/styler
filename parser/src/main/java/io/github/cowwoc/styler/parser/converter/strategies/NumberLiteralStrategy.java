package io.github.cowwoc.styler.parser.converter.strategies;

import io.github.cowwoc.styler.ast.node.NumberLiteralNode;
import io.github.cowwoc.styler.parser.ArenaNodeStorage;
import io.github.cowwoc.styler.parser.NodeType;
import io.github.cowwoc.styler.parser.converter.ConversionContext;

/**
 * Conversion strategy for number literal nodes.
 *
 * @since 1.0
 */
public final class NumberLiteralStrategy extends BaseConversionStrategy<NumberLiteralNode>
{
	/**
	 * Creates a new conversion strategy for this node type.
	 */
	public NumberLiteralStrategy()
	{
		super(NodeType.LITERAL_EXPRESSION, NumberLiteralNode.class);
	}

	@Override
	public NumberLiteralNode convert(int nodeId, ArenaNodeStorage nodeStorage,
		ConversionContext context)
	{
		ArenaNodeStorage.NodeInfo nodeInfo = nodeStorage.getNode(nodeId);

		return new NumberLiteralNode(
			getSourceRange(nodeInfo, context),
			context.getLeadingComments(),
			context.getTrailingComments(),
			context.getWhitespace(),
			context.getFormattingHints(),
			context.getParentNode());
	}
}
