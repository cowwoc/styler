package io.github.cowwoc.styler.parser.converter.strategies;

import io.github.cowwoc.styler.ast.node.ContinueStatementNode;
import io.github.cowwoc.styler.parser.ArenaNodeStorage;
import io.github.cowwoc.styler.parser.NodeType;
import io.github.cowwoc.styler.parser.converter.ConversionContext;


/**
 * Conversion strategy for continue statement nodes.
 *
 * @since 1.0
 */
public final class ContinueStatementStrategy extends BaseConversionStrategy<ContinueStatementNode>
{
	/**
	 * Creates a new conversion strategy for this node type.
	 */
	public ContinueStatementStrategy()
	{
		super(NodeType.CONTINUE_STATEMENT, ContinueStatementNode.class);
	}

	@Override
	public ContinueStatementNode convert(int nodeId, ArenaNodeStorage nodeStorage,
		ConversionContext context)
	{
		ArenaNodeStorage.NodeInfo nodeInfo = nodeStorage.getNode(nodeId);

		return new ContinueStatementNode(
			getSourceRange(nodeInfo, context),
			context.getLeadingComments(),
			context.getTrailingComments(),
			context.getWhitespace(),
			context.getFormattingHints(),
			context.getParentNode());
	}
}
