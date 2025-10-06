package io.github.cowwoc.styler.parser.converter.strategies;

import io.github.cowwoc.styler.ast.node.EnhancedForStatementNode;
import io.github.cowwoc.styler.parser.ArenaNodeStorage;
import io.github.cowwoc.styler.parser.NodeType;
import io.github.cowwoc.styler.parser.converter.ConversionContext;

/**
 * Conversion strategy for enhanced for statement nodes.
 *
 * @since 1.0
 */
public final class EnhancedForStatementStrategy
	extends BaseConversionStrategy<EnhancedForStatementNode>
{
	/**
	 * Creates a new conversion strategy for this node type.
	 */
	public EnhancedForStatementStrategy()
	{
		super(NodeType.ENHANCED_FOR_STATEMENT, EnhancedForStatementNode.class);
	}

	@Override
	public EnhancedForStatementNode convert(int nodeId, ArenaNodeStorage nodeStorage,
		ConversionContext context)
	{
		ArenaNodeStorage.NodeInfo nodeInfo = nodeStorage.getNode(nodeId);

		return new EnhancedForStatementNode(
			getSourceRange(nodeInfo, context),
			context.getLeadingComments(),
			context.getTrailingComments(),
			context.getWhitespace(),
			context.getFormattingHints(),
			context.getParentNode());
	}
}
