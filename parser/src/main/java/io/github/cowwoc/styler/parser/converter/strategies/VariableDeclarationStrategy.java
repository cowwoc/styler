package io.github.cowwoc.styler.parser.converter.strategies;

import io.github.cowwoc.styler.ast.ASTNode;
import io.github.cowwoc.styler.ast.node.VariableDeclarationNode;
import io.github.cowwoc.styler.parser.ArenaNodeStorage;
import io.github.cowwoc.styler.parser.NodeType;
import io.github.cowwoc.styler.parser.converter.ConversionContext;

import java.util.List;
import java.util.Optional;

/**
 * Conversion strategy for variable declaration nodes.
 *
 * @since 1.0
 */
public final class VariableDeclarationStrategy
	extends BaseConversionStrategy<VariableDeclarationNode>
{
	/**
	 * Creates a new conversion strategy for this node type.
	 */
	public VariableDeclarationStrategy()
	{
		super(NodeType.LOCAL_VARIABLE_DECLARATION, VariableDeclarationNode.class);
	}

	@Override
	public VariableDeclarationNode convert(int nodeId, ArenaNodeStorage nodeStorage,
		ConversionContext context)
	{
		ArenaNodeStorage.NodeInfo nodeInfo = nodeStorage.getNode(nodeId);
		List<Integer> childIds = nodeInfo.childIds();

		String name = "";
		Optional<ASTNode> initializer = Optional.empty();

		// Extract variable name from first identifier child
		for (Integer childId : childIds)
		{
			ArenaNodeStorage.NodeInfo childInfo = nodeStorage.getNode(childId);
			if (childInfo.nodeType() == NodeType.IDENTIFIER_EXPRESSION)
			{
				name = context.getSourceText(childInfo.startOffset(), childInfo.endOffset());
				break;
			}
		}

		return new VariableDeclarationNode(
			getSourceRange(nodeInfo, context),
			context.getLeadingComments(),
			context.getTrailingComments(),
			context.getWhitespace(),
			context.getFormattingHints(),
			context.getParentNode(),
			name,
			initializer);
	}
}
