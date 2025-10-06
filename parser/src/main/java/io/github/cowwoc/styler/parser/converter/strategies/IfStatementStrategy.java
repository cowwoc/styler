package io.github.cowwoc.styler.parser.converter.strategies;

import io.github.cowwoc.styler.ast.ASTNode;
import io.github.cowwoc.styler.ast.node.IfStatementNode;
import io.github.cowwoc.styler.parser.ArenaNodeStorage;
import io.github.cowwoc.styler.parser.NodeType;
import java.util.List;
import io.github.cowwoc.styler.parser.converter.ConversionContext;

import java.util.Optional;

/**
 * Conversion strategy for if statement nodes.
 *
 * @since 1.0
 */
public final class IfStatementStrategy extends BaseConversionStrategy<IfStatementNode>
{
	/**
	 * Creates a new conversion strategy for this node type.
	 */
	public IfStatementStrategy()
	{
		super(NodeType.IF_STATEMENT, IfStatementNode.class);
	}

	@Override
	public IfStatementNode convert(int nodeId, ArenaNodeStorage nodeStorage,
		ConversionContext context)
	{
		ArenaNodeStorage.NodeInfo nodeInfo = nodeStorage.getNode(nodeId);
		List<Integer> childIds = nodeInfo.childIds();

		// Structure: condition, thenStatement, [elseStatement]
		ASTNode condition = context.convertNode(childIds.get(0), nodeStorage);
		ASTNode thenStatement = context.convertNode(childIds.get(1), nodeStorage);
		Optional<ASTNode> elseStatement = Optional.empty();
		if (childIds.size() > 2)
		{
			elseStatement = Optional.of(context.convertNode(childIds.get(2), nodeStorage));
		}

		return new IfStatementNode(
			getSourceRange(nodeInfo, context),
			context.getLeadingComments(),
			context.getTrailingComments(),
			context.getWhitespace(),
			context.getFormattingHints(),
			context.getParentNode(),
			condition,
			thenStatement,
			elseStatement);
	}
}
