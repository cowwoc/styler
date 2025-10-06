package io.github.cowwoc.styler.parser.converter.strategies;

import io.github.cowwoc.styler.ast.node.GenericTypeNode;
import io.github.cowwoc.styler.parser.ArenaNodeStorage;
import io.github.cowwoc.styler.parser.NodeType;
import io.github.cowwoc.styler.parser.converter.ConversionContext;

/**
 * Conversion strategy for generic type nodes.
 *
 * @since 1.0
 */
public final class GenericTypeStrategy extends BaseConversionStrategy<GenericTypeNode>
{
	/**
	 * Creates a new conversion strategy for this node type.
	 */
	public GenericTypeStrategy()
	{
		super(NodeType.PARAMETERIZED_TYPE, GenericTypeNode.class);
	}

	@Override
	public GenericTypeNode convert(int nodeId, ArenaNodeStorage nodeStorage,
		ConversionContext context)
	{
		ArenaNodeStorage.NodeInfo nodeInfo = nodeStorage.getNode(nodeId);

		return new GenericTypeNode(
			getSourceRange(nodeInfo, context),
			context.getLeadingComments(),
			context.getTrailingComments(),
			context.getWhitespace(),
			context.getFormattingHints(),
			context.getParentNode());
	}
}
