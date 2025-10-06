package io.github.cowwoc.styler.parser.converter.strategies;

import io.github.cowwoc.styler.ast.node.SynchronizedStatementNode;
import io.github.cowwoc.styler.parser.ArenaNodeStorage;
import io.github.cowwoc.styler.parser.NodeType;
import io.github.cowwoc.styler.parser.converter.ConversionContext;

/**
 * Conversion strategy for synchronized statement nodes.
 *
 * @since 1.0
 */
public final class SynchronizedStatementStrategy
	extends BaseConversionStrategy<SynchronizedStatementNode>
{
	/**
	 * Creates a new conversion strategy for this node type.
	 */
	public SynchronizedStatementStrategy()
	{
		super(NodeType.SYNCHRONIZED_STATEMENT, SynchronizedStatementNode.class);
	}

	@Override
	public SynchronizedStatementNode convert(int nodeId, ArenaNodeStorage nodeStorage,
		ConversionContext context)
	{
		ArenaNodeStorage.NodeInfo nodeInfo = nodeStorage.getNode(nodeId);

		return new SynchronizedStatementNode(
			getSourceRange(nodeInfo, context),
			context.getLeadingComments(),
			context.getTrailingComments(),
			context.getWhitespace(),
			context.getFormattingHints(),
			context.getParentNode());
	}
}
