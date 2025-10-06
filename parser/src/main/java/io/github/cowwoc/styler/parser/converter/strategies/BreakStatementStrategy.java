package io.github.cowwoc.styler.parser.converter.strategies;

import io.github.cowwoc.styler.ast.node.BreakStatementNode;
import io.github.cowwoc.styler.parser.ArenaNodeStorage;
import io.github.cowwoc.styler.parser.NodeType;
import io.github.cowwoc.styler.parser.converter.ConversionContext;


/**
 * Conversion strategy for break statement nodes.
 *
 * @since 1.0
 */
public final class BreakStatementStrategy extends BaseConversionStrategy<BreakStatementNode>
{
	/**
	 * Creates a new conversion strategy for this node type.
	 */
	public BreakStatementStrategy()
	{
		super(NodeType.BREAK_STATEMENT, BreakStatementNode.class);
	}

	@Override
	public BreakStatementNode convert(int nodeId, ArenaNodeStorage nodeStorage,
		ConversionContext context)
	{
		ArenaNodeStorage.NodeInfo nodeInfo = nodeStorage.getNode(nodeId);

		return new BreakStatementNode(
			getSourceRange(nodeInfo, context),
			context.getLeadingComments(),
			context.getTrailingComments(),
			context.getWhitespace(),
			context.getFormattingHints(),
			context.getParentNode());
	}
}
