package io.github.cowwoc.styler.parser.converter.strategies;

import io.github.cowwoc.styler.ast.node.TextBlockNode;
import io.github.cowwoc.styler.parser.ArenaNodeStorage;
import io.github.cowwoc.styler.parser.NodeType;
import io.github.cowwoc.styler.parser.converter.ConversionContext;

/**
 * Conversion strategy for text block nodes.
 *
 * @since 1.0
 */
public final class TextBlockStrategy extends BaseConversionStrategy<TextBlockNode>
{
	/**
	 * Creates a new conversion strategy for this node type.
	 */
	public TextBlockStrategy()
	{
		super(NodeType.LITERAL_EXPRESSION, TextBlockNode.class);
	}

	@Override
	public TextBlockNode convert(int nodeId, ArenaNodeStorage nodeStorage,
		ConversionContext context)
	{
		ArenaNodeStorage.NodeInfo nodeInfo = nodeStorage.getNode(nodeId);

		return new TextBlockNode(
			getSourceRange(nodeInfo, context),
			context.getLeadingComments(),
			context.getTrailingComments(),
			context.getWhitespace(),
			context.getFormattingHints(),
			context.getParentNode());
	}
}
