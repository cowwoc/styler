package io.github.cowwoc.styler.parser.converter.strategies;

import io.github.cowwoc.styler.ast.node.ThrowStatementNode;
import io.github.cowwoc.styler.parser.ArenaNodeStorage;
import io.github.cowwoc.styler.parser.NodeType;
import io.github.cowwoc.styler.parser.converter.ConversionContext;

/**
 * Conversion strategy for throw statement nodes.
 *
 * @since 1.0
 */
public final class ThrowStatementStrategy extends BaseConversionStrategy<ThrowStatementNode>
{
	/**
	 * Creates a new conversion strategy for this node type.
	 */
	public ThrowStatementStrategy()
	{
		super(NodeType.THROW_STATEMENT, ThrowStatementNode.class);
	}

	@Override
	public ThrowStatementNode convert(int nodeId, ArenaNodeStorage nodeStorage,
		ConversionContext context)
	{
		ArenaNodeStorage.NodeInfo nodeInfo = nodeStorage.getNode(nodeId);

		return new ThrowStatementNode(
			getSourceRange(nodeInfo, context),
			context.getLeadingComments(),
			context.getTrailingComments(),
			context.getWhitespace(),
			context.getFormattingHints(),
			context.getParentNode());
	}
}
