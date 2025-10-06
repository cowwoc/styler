package io.github.cowwoc.styler.parser.converter.strategies;

import io.github.cowwoc.styler.ast.ASTNode;
import io.github.cowwoc.styler.ast.node.ExpressionStatementNode;
import io.github.cowwoc.styler.parser.ArenaNodeStorage;
import io.github.cowwoc.styler.parser.NodeType;
import java.util.List;
import io.github.cowwoc.styler.parser.converter.ConversionContext;

/**
 * Conversion strategy for expression statement nodes.
 *
 * @since 1.0
 */
public final class ExpressionStatementStrategy
	extends BaseConversionStrategy<ExpressionStatementNode>
{
	/**
	 * Creates a new conversion strategy for this node type.
	 */
	public ExpressionStatementStrategy()
	{
		super(NodeType.EXPRESSION_STATEMENT, ExpressionStatementNode.class);
	}

	@Override
	public ExpressionStatementNode convert(int nodeId, ArenaNodeStorage nodeStorage,
		ConversionContext context)
	{
		ArenaNodeStorage.NodeInfo nodeInfo = nodeStorage.getNode(nodeId);
		List<Integer> childIds = nodeInfo.childIds();

		ASTNode expression = context.convertNode(childIds.get(0), nodeStorage);

		return new ExpressionStatementNode(
			getSourceRange(nodeInfo, context),
			context.getLeadingComments(),
			context.getTrailingComments(),
			context.getWhitespace(),
			context.getFormattingHints(),
			context.getParentNode(),
			expression);
	}
}
