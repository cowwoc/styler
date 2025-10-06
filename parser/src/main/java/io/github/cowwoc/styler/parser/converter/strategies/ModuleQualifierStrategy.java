package io.github.cowwoc.styler.parser.converter.strategies;

import io.github.cowwoc.styler.ast.node.ModuleQualifierNode;
import io.github.cowwoc.styler.parser.ArenaNodeStorage;
import io.github.cowwoc.styler.parser.NodeType;
import io.github.cowwoc.styler.parser.converter.ConversionContext;

import java.util.ArrayList;
import java.util.List;

/**
 * Conversion strategy for module qualifier nodes.
 *
 * @since 1.0
 */
public final class ModuleQualifierStrategy extends BaseConversionStrategy<ModuleQualifierNode>
{
	/**
	 * Creates a new conversion strategy for this node type.
	 */
	public ModuleQualifierStrategy()
	{
		super(NodeType.MODULE_QUALIFIER, ModuleQualifierNode.class);
	}

	@Override
	public ModuleQualifierNode convert(int nodeId, ArenaNodeStorage nodeStorage,
		ConversionContext context)
	{
		ArenaNodeStorage.NodeInfo nodeInfo = nodeStorage.getNode(nodeId);
		List<Integer> childIds = nodeInfo.childIds();

		List<io.github.cowwoc.styler.ast.ASTNode> children = new ArrayList<>();
		for (int childId : childIds)
		{
			children.add(context.convertNode(childId, nodeStorage));
		}

		return new ModuleQualifierNode(
			getSourceRange(nodeInfo, context),
			context.getLeadingComments(),
			context.getTrailingComments(),
			context.getWhitespace(),
			context.getFormattingHints(),
			context.getParentNode(),
			children);
	}
}
