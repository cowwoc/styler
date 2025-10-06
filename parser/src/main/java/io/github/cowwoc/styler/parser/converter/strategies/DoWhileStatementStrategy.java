package io.github.cowwoc.styler.parser.converter.strategies;

import io.github.cowwoc.styler.ast.node.DoWhileStatementNode;
import io.github.cowwoc.styler.parser.ArenaNodeStorage;
import io.github.cowwoc.styler.parser.NodeType;
import io.github.cowwoc.styler.parser.converter.ConversionContext;

/**
 * Conversion strategy for do-while statement nodes.
 *
 * @since 1.0
 */
public final class DoWhileStatementStrategy extends BaseConversionStrategy<DoWhileStatementNode>
{
	/**
	 * Creates a new conversion strategy for this node type.
	 */
	public DoWhileStatementStrategy()
	{
		super(NodeType.DO_WHILE_STATEMENT, DoWhileStatementNode.class);
	}

	@Override
	public DoWhileStatementNode convert(int nodeId, ArenaNodeStorage nodeStorage,
		ConversionContext context)
	{
		ArenaNodeStorage.NodeInfo nodeInfo = nodeStorage.getNode(nodeId);

		return new DoWhileStatementNode(
			getSourceRange(nodeInfo, context),
			context.getLeadingComments(),
			context.getTrailingComments(),
			context.getWhitespace(),
			context.getFormattingHints(),
			context.getParentNode());
	}
}
