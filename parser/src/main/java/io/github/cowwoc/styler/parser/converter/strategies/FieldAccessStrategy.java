package io.github.cowwoc.styler.parser.converter.strategies;

import io.github.cowwoc.styler.ast.node.FieldAccessNode;
import io.github.cowwoc.styler.parser.ArenaNodeStorage;
import io.github.cowwoc.styler.parser.NodeType;
import io.github.cowwoc.styler.parser.converter.ConversionContext;

/**
 * Conversion strategy for field access nodes.
 *
 * @since 1.0
 */
public final class FieldAccessStrategy extends BaseConversionStrategy<FieldAccessNode>
{
	/**
	 * Creates a new conversion strategy for this node type.
	 */
	public FieldAccessStrategy()
	{
		super(NodeType.FIELD_ACCESS_EXPRESSION, FieldAccessNode.class);
	}

	@Override
	public FieldAccessNode convert(int nodeId, ArenaNodeStorage nodeStorage,
		ConversionContext context)
	{
		ArenaNodeStorage.NodeInfo nodeInfo = nodeStorage.getNode(nodeId);

		return new FieldAccessNode(
			getSourceRange(nodeInfo, context),
			context.getLeadingComments(),
			context.getTrailingComments(),
			context.getWhitespace(),
			context.getFormattingHints(),
			context.getParentNode());
	}
}
