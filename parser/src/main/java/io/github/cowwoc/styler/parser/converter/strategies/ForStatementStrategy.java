package io.github.cowwoc.styler.parser.converter.strategies;

import io.github.cowwoc.styler.ast.ASTNode;
import io.github.cowwoc.styler.ast.node.ForStatementNode;
import io.github.cowwoc.styler.parser.ArenaNodeStorage;
import io.github.cowwoc.styler.parser.NodeType;
import io.github.cowwoc.styler.parser.converter.ConversionContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Conversion strategy for for statement nodes.
 *
 * @since 1.0
 */
public final class ForStatementStrategy extends BaseConversionStrategy<ForStatementNode>
{
	/**
	 * Creates a new conversion strategy for this node type.
	 */
	public ForStatementStrategy()
	{
		super(NodeType.FOR_STATEMENT, ForStatementNode.class);
	}

	@Override
	public ForStatementNode convert(int nodeId, ArenaNodeStorage nodeStorage,
		ConversionContext context)
	{
		ArenaNodeStorage.NodeInfo nodeInfo = nodeStorage.getNode(nodeId);
		List<Integer> childIds = nodeInfo.childIds();

		List<ASTNode> initializers = new ArrayList<>();
		Optional<ASTNode> condition = Optional.empty();
		List<ASTNode> updaters = new ArrayList<>();
		ASTNode body = null;

		// Parse children based on position and type
		for (int i = 0; i < childIds.size(); ++i)
		{
			ArenaNodeStorage.NodeInfo childInfo = nodeStorage.getNode(childIds.get(i));
			byte childType = childInfo.nodeType();

			if (childType == NodeType.BLOCK_STATEMENT ||
				childType == NodeType.EXPRESSION_STATEMENT)
			{
				if (body == null)
				{
					body = context.convertNode(childIds.get(i), nodeStorage);
				}
			}
			else
			{
				ASTNode child = context.convertNode(childIds.get(i), nodeStorage);
				if (i == 0 || childType == NodeType.LOCAL_VARIABLE_DECLARATION)
				{
					initializers.add(child);
				}
				else if (condition.isEmpty() && childType != NodeType.LOCAL_VARIABLE_DECLARATION)
				{
					condition = Optional.of(child);
				}
				else
				{
					updaters.add(child);
				}
			}
		}

		if (body == null)
		{
			throw new IllegalStateException(
				"For statement must have a body at node " + nodeId);
		}

		return new ForStatementNode(
			getSourceRange(nodeInfo, context),
			context.getLeadingComments(),
			context.getTrailingComments(),
			context.getWhitespace(),
			context.getFormattingHints(),
			context.getParentNode(),
			initializers,
			condition,
			updaters,
			body);
	}
}
