package io.github.cowwoc.styler.parser.converter.strategies;

import io.github.cowwoc.styler.ast.node.ModuleRequiresDirectiveNode;
import io.github.cowwoc.styler.parser.ArenaNodeStorage;
import io.github.cowwoc.styler.parser.NodeType;
import io.github.cowwoc.styler.parser.converter.ConversionContext;

import java.util.ArrayList;
import java.util.List;

/**
 * Conversion strategy for module requires directive nodes.
 *
 * @since 1.0
 */
public final class ModuleRequiresDirectiveStrategy extends BaseConversionStrategy<ModuleRequiresDirectiveNode>
{
	/**
	 * Creates a new conversion strategy for this node type.
	 */
	public ModuleRequiresDirectiveStrategy()
	{
		super(NodeType.MODULE_REQUIRES_DIRECTIVE, ModuleRequiresDirectiveNode.class);
	}

	@Override
	public ModuleRequiresDirectiveNode convert(int nodeId, ArenaNodeStorage nodeStorage,
		ConversionContext context)
	{
		ArenaNodeStorage.NodeInfo nodeInfo = nodeStorage.getNode(nodeId);
		List<Integer> childIds = nodeInfo.childIds();

		List<io.github.cowwoc.styler.ast.ASTNode> children = new ArrayList<>();
		for (int childId : childIds)
		{
			children.add(context.convertNode(childId, nodeStorage));
		}

		return new ModuleRequiresDirectiveNode(
			getSourceRange(nodeInfo, context),
			context.getLeadingComments(),
			context.getTrailingComments(),
			context.getWhitespace(),
			context.getFormattingHints(),
			context.getParentNode(),
			children);
	}
}
