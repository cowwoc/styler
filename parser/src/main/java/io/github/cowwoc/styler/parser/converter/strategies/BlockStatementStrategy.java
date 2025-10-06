package io.github.cowwoc.styler.parser.converter.strategies;

import io.github.cowwoc.styler.ast.ASTNode;
import io.github.cowwoc.styler.ast.node.BlockStatementNode;
import io.github.cowwoc.styler.parser.ArenaNodeStorage;
import io.github.cowwoc.styler.parser.NodeType;
import io.github.cowwoc.styler.parser.converter.ConversionContext;

import java.util.List;

/**
 * Conversion strategy for block statement nodes.
 *
 * @since 1.0
 */
public final class BlockStatementStrategy extends BaseConversionStrategy<BlockStatementNode>
{
	/**
	 * Creates a new conversion strategy for this node type.
	 */
	public BlockStatementStrategy()
	{
		super(NodeType.BLOCK_STATEMENT, BlockStatementNode.class);
	}

	@Override
	public BlockStatementNode convert(int nodeId, ArenaNodeStorage nodeStorage,
		ConversionContext context)
	{
		ArenaNodeStorage.NodeInfo nodeInfo = nodeStorage.getNode(nodeId);

		List<ASTNode> statements = convertChildren(nodeInfo.childIds(), nodeStorage, context);

		return new BlockStatementNode(
			getSourceRange(nodeInfo, context),
			context.getLeadingComments(),
			context.getTrailingComments(),
			context.getWhitespace(),
			context.getFormattingHints(),
			context.getParentNode(),
			statements);
	}
}
