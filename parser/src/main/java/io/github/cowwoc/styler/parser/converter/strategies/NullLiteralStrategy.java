package io.github.cowwoc.styler.parser.converter.strategies;

import io.github.cowwoc.styler.ast.node.NullLiteralNode;
import io.github.cowwoc.styler.parser.ArenaNodeStorage;
import io.github.cowwoc.styler.parser.NodeType;
import io.github.cowwoc.styler.parser.converter.ConversionContext;

/**
 * Conversion strategy for null literal nodes.
 *
 * @since 1.0
 */
public final class NullLiteralStrategy extends BaseConversionStrategy<NullLiteralNode>
{
	/**
	 * Creates a new conversion strategy for this node type.
	 */
	public NullLiteralStrategy()
	{
		super(NodeType.LITERAL_EXPRESSION, NullLiteralNode.class);
	}

	@Override
	public NullLiteralNode convert(int nodeId, ArenaNodeStorage nodeStorage,
		ConversionContext context)
	{
		ArenaNodeStorage.NodeInfo nodeInfo = nodeStorage.getNode(nodeId);

		return new NullLiteralNode(
			getSourceRange(nodeInfo, context),
			context.getLeadingComments(),
			context.getTrailingComments(),
			context.getWhitespace(),
			context.getFormattingHints(),
			context.getParentNode());
	}
}
