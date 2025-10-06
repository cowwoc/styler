package io.github.cowwoc.styler.parser.converter.strategies;

import io.github.cowwoc.styler.ast.ASTNode;
import io.github.cowwoc.styler.ast.node.ReturnStatementNode;
import io.github.cowwoc.styler.parser.ArenaNodeStorage;
import io.github.cowwoc.styler.parser.NodeType;
import java.util.List;
import io.github.cowwoc.styler.parser.converter.ConversionContext;

import java.util.Optional;

/**
 * Conversion strategy for return statement nodes.
 *
 * @since 1.0
 */
public final class ReturnStatementStrategy extends BaseConversionStrategy<ReturnStatementNode>
{
	/**
	 * Creates a new conversion strategy for this node type.
	 */
	public ReturnStatementStrategy()
	{
		super(NodeType.RETURN_STATEMENT, ReturnStatementNode.class);
	}

	@Override
	public ReturnStatementNode convert(int nodeId, ArenaNodeStorage nodeStorage,
		ConversionContext context)
	{
		ArenaNodeStorage.NodeInfo nodeInfo = nodeStorage.getNode(nodeId);
		List<Integer> childIds = nodeInfo.childIds();

		Optional<ASTNode> expression = Optional.empty();
		if (!childIds.isEmpty())
		{
			expression = Optional.of(context.convertNode(childIds.get(0), nodeStorage));
		}

		return new ReturnStatementNode(
			getSourceRange(nodeInfo, context),
			context.getLeadingComments(),
			context.getTrailingComments(),
			context.getWhitespace(),
			context.getFormattingHints(),
			context.getParentNode(),
			expression);
	}
}
