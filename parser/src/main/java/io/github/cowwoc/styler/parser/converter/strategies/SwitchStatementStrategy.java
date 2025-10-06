package io.github.cowwoc.styler.parser.converter.strategies;

import io.github.cowwoc.styler.ast.node.SwitchStatementNode;
import io.github.cowwoc.styler.parser.ArenaNodeStorage;
import io.github.cowwoc.styler.parser.NodeType;
import io.github.cowwoc.styler.parser.converter.ConversionContext;

/**
 * Conversion strategy for switch statement nodes.
 *
 * @since 1.0
 */
public final class SwitchStatementStrategy extends BaseConversionStrategy<SwitchStatementNode>
{
	/**
	 * Creates a new conversion strategy for this node type.
	 */
	public SwitchStatementStrategy()
	{
		super(NodeType.SWITCH_STATEMENT, SwitchStatementNode.class);
	}

	@Override
	public SwitchStatementNode convert(int nodeId, ArenaNodeStorage nodeStorage,
		ConversionContext context)
	{
		ArenaNodeStorage.NodeInfo nodeInfo = nodeStorage.getNode(nodeId);

		return new SwitchStatementNode(
			getSourceRange(nodeInfo, context),
			context.getLeadingComments(),
			context.getTrailingComments(),
			context.getWhitespace(),
			context.getFormattingHints(),
			context.getParentNode());
	}
}
