package io.github.cowwoc.styler.parser.converter.strategies;

import io.github.cowwoc.styler.ast.node.WildcardTypeNode;
import io.github.cowwoc.styler.parser.ArenaNodeStorage;
import io.github.cowwoc.styler.parser.NodeType;
import io.github.cowwoc.styler.parser.converter.ConversionContext;

/**
 * Conversion strategy for wildcard type nodes.
 *
 * @since 1.0
 */
public final class WildcardTypeStrategy extends BaseConversionStrategy<WildcardTypeNode>
{
	/**
	 * Creates a new conversion strategy for this node type.
	 */
	public WildcardTypeStrategy()
	{
		super(NodeType.WILDCARD_TYPE, WildcardTypeNode.class);
	}

	@Override
	public WildcardTypeNode convert(int nodeId, ArenaNodeStorage nodeStorage,
		ConversionContext context)
	{
		ArenaNodeStorage.NodeInfo nodeInfo = nodeStorage.getNode(nodeId);

		return new WildcardTypeNode(
			getSourceRange(nodeInfo, context),
			context.getLeadingComments(),
			context.getTrailingComments(),
			context.getWhitespace(),
			context.getFormattingHints(),
			context.getParentNode());
	}
}
