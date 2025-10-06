package io.github.cowwoc.styler.parser.converter.strategies;

import io.github.cowwoc.styler.ast.node.WhileStatementNode;
import io.github.cowwoc.styler.parser.ArenaNodeStorage;
import io.github.cowwoc.styler.parser.NodeType;
import io.github.cowwoc.styler.parser.converter.ConversionContext;

/**
 * Conversion strategy for while statement nodes.
 *
 * @since 1.0
 */
public final class WhileStatementStrategy extends BaseConversionStrategy<WhileStatementNode>
{
	/**
	 * Creates a new conversion strategy for this node type.
	 */
	public WhileStatementStrategy()
	{
		super(NodeType.WHILE_STATEMENT, WhileStatementNode.class);
	}

	@Override
	public WhileStatementNode convert(int nodeId, ArenaNodeStorage nodeStorage,
		ConversionContext context)
	{
		ArenaNodeStorage.NodeInfo nodeInfo = nodeStorage.getNode(nodeId);

		return new WhileStatementNode(
			getSourceRange(nodeInfo, context),
			context.getLeadingComments(),
			context.getTrailingComments(),
			context.getWhitespace(),
			context.getFormattingHints(),
			context.getParentNode());
	}
}
