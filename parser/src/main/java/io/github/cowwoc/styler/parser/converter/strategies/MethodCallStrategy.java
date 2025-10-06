package io.github.cowwoc.styler.parser.converter.strategies;

import io.github.cowwoc.styler.ast.node.MethodCallNode;
import io.github.cowwoc.styler.parser.ArenaNodeStorage;
import io.github.cowwoc.styler.parser.NodeType;
import io.github.cowwoc.styler.parser.converter.ConversionContext;

/**
 * Conversion strategy for method call nodes.
 *
 * @since 1.0
 */
public final class MethodCallStrategy extends BaseConversionStrategy<MethodCallNode>
{
	/**
	 * Creates a new conversion strategy for this node type.
	 */
	public MethodCallStrategy()
	{
		super(NodeType.METHOD_CALL_EXPRESSION, MethodCallNode.class);
	}

	@Override
	public MethodCallNode convert(int nodeId, ArenaNodeStorage nodeStorage,
		ConversionContext context)
	{
		ArenaNodeStorage.NodeInfo nodeInfo = nodeStorage.getNode(nodeId);

		return new MethodCallNode(
			getSourceRange(nodeInfo, context),
			context.getLeadingComments(),
			context.getTrailingComments(),
			context.getWhitespace(),
			context.getFormattingHints(),
			context.getParentNode());
	}
}
