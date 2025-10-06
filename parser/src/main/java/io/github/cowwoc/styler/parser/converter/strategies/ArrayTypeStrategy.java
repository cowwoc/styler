package io.github.cowwoc.styler.parser.converter.strategies;

import io.github.cowwoc.styler.ast.node.ArrayTypeNode;
import io.github.cowwoc.styler.parser.ArenaNodeStorage;
import io.github.cowwoc.styler.parser.NodeType;
import io.github.cowwoc.styler.parser.converter.ConversionContext;

/**
 * Conversion strategy for array type nodes.
 *
 * @since 1.0
 */
public final class ArrayTypeStrategy extends BaseConversionStrategy<ArrayTypeNode>
{
	/**
	 * Creates a new conversion strategy for this node type.
	 */
	public ArrayTypeStrategy()
	{
		super(NodeType.ARRAY_TYPE, ArrayTypeNode.class);
	}

	@Override
	public ArrayTypeNode convert(int nodeId, ArenaNodeStorage nodeStorage,
		ConversionContext context)
	{
		ArenaNodeStorage.NodeInfo nodeInfo = nodeStorage.getNode(nodeId);

		return new ArrayTypeNode(
			getSourceRange(nodeInfo, context),
			context.getLeadingComments(),
			context.getTrailingComments(),
			context.getWhitespace(),
			context.getFormattingHints(),
			context.getParentNode());
	}
}
