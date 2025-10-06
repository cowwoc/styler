package io.github.cowwoc.styler.parser.converter.strategies;

import io.github.cowwoc.styler.ast.node.CharLiteralNode;
import io.github.cowwoc.styler.parser.ArenaNodeStorage;
import io.github.cowwoc.styler.parser.NodeType;
import io.github.cowwoc.styler.parser.converter.ConversionContext;

/**
 * Conversion strategy for char literal nodes.
 *
 * @since 1.0
 */
public final class CharLiteralStrategy extends BaseConversionStrategy<CharLiteralNode>
{
	/**
	 * Creates a new conversion strategy for this node type.
	 */
	public CharLiteralStrategy()
	{
		super(NodeType.LITERAL_EXPRESSION, CharLiteralNode.class);
	}

	@Override
	public CharLiteralNode convert(int nodeId, ArenaNodeStorage nodeStorage,
		ConversionContext context)
	{
		ArenaNodeStorage.NodeInfo nodeInfo = nodeStorage.getNode(nodeId);

		return new CharLiteralNode(
			getSourceRange(nodeInfo, context),
			context.getLeadingComments(),
			context.getTrailingComments(),
			context.getWhitespace(),
			context.getFormattingHints(),
			context.getParentNode());
	}
}
