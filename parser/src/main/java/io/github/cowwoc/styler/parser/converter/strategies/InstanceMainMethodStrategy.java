package io.github.cowwoc.styler.parser.converter.strategies;

import io.github.cowwoc.styler.ast.node.InstanceMainMethodNode;
import io.github.cowwoc.styler.parser.ArenaNodeStorage;
import io.github.cowwoc.styler.parser.NodeType;
import io.github.cowwoc.styler.parser.converter.ConversionContext;

import java.util.ArrayList;
import java.util.List;

/**
 * Conversion strategy for instance main method nodes.
 *
 * @since 1.0
 */
public final class InstanceMainMethodStrategy extends BaseConversionStrategy<InstanceMainMethodNode>
{
	/**
	 * Creates a new conversion strategy for this node type.
	 */
	public InstanceMainMethodStrategy()
	{
		super(NodeType.INSTANCE_MAIN_METHOD, InstanceMainMethodNode.class);
	}

	@Override
	public InstanceMainMethodNode convert(int nodeId, ArenaNodeStorage nodeStorage,
		ConversionContext context)
	{
		ArenaNodeStorage.NodeInfo nodeInfo = nodeStorage.getNode(nodeId);
		List<Integer> childIds = nodeInfo.childIds();

		List<io.github.cowwoc.styler.ast.ASTNode> children = new ArrayList<>();
		for (int childId : childIds)
		{
			children.add(context.convertNode(childId, nodeStorage));
		}

		return new InstanceMainMethodNode(
			getSourceRange(nodeInfo, context),
			context.getLeadingComments(),
			context.getTrailingComments(),
			context.getWhitespace(),
			context.getFormattingHints(),
			context.getParentNode(),
			children);
	}
}
