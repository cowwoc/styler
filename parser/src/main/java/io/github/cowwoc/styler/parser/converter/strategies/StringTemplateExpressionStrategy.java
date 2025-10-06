package io.github.cowwoc.styler.parser.converter.strategies;

import io.github.cowwoc.styler.ast.node.StringTemplateExpressionNode;
import io.github.cowwoc.styler.parser.ArenaNodeStorage;
import io.github.cowwoc.styler.parser.NodeType;
import io.github.cowwoc.styler.parser.converter.ConversionContext;

import java.util.ArrayList;
import java.util.List;

/**
 * Conversion strategy for string template expression nodes.
 *
 * @since 1.0
 */
public final class StringTemplateExpressionStrategy extends BaseConversionStrategy<StringTemplateExpressionNode>
{
	/**
	 * Creates a new conversion strategy for this node type.
	 */
	public StringTemplateExpressionStrategy()
	{
		super(NodeType.STRING_TEMPLATE_EXPRESSION, StringTemplateExpressionNode.class);
	}

	@Override
	public StringTemplateExpressionNode convert(int nodeId, ArenaNodeStorage nodeStorage,
		ConversionContext context)
	{
		ArenaNodeStorage.NodeInfo nodeInfo = nodeStorage.getNode(nodeId);
		List<Integer> childIds = nodeInfo.childIds();

		List<io.github.cowwoc.styler.ast.ASTNode> children = new ArrayList<>();
		for (int childId : childIds)
		{
			children.add(context.convertNode(childId, nodeStorage));
		}

		return new StringTemplateExpressionNode(
			getSourceRange(nodeInfo, context),
			context.getLeadingComments(),
			context.getTrailingComments(),
			context.getWhitespace(),
			context.getFormattingHints(),
			context.getParentNode(),
			children);
	}
}
