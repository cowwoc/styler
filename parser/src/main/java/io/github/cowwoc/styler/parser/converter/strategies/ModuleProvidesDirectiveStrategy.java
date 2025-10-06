package io.github.cowwoc.styler.parser.converter.strategies;

import io.github.cowwoc.styler.ast.node.ModuleProvidesDirectiveNode;
import io.github.cowwoc.styler.parser.ArenaNodeStorage;
import io.github.cowwoc.styler.parser.NodeType;
import io.github.cowwoc.styler.parser.converter.ConversionContext;

import java.util.ArrayList;
import java.util.List;

/**
 * Conversion strategy for module provides directive nodes.
 *
 * @since 1.0
 */
public final class ModuleProvidesDirectiveStrategy extends BaseConversionStrategy<ModuleProvidesDirectiveNode>
{
	/**
	 * Creates a new conversion strategy for this node type.
	 */
	public ModuleProvidesDirectiveStrategy()
	{
		super(NodeType.MODULE_PROVIDES_DIRECTIVE, ModuleProvidesDirectiveNode.class);
	}

	@Override
	public ModuleProvidesDirectiveNode convert(int nodeId, ArenaNodeStorage nodeStorage,
		ConversionContext context)
	{
		ArenaNodeStorage.NodeInfo nodeInfo = nodeStorage.getNode(nodeId);
		List<Integer> childIds = nodeInfo.childIds();

		List<io.github.cowwoc.styler.ast.ASTNode> children = new ArrayList<>();
		for (int childId : childIds)
		{
			children.add(context.convertNode(childId, nodeStorage));
		}

		return new ModuleProvidesDirectiveNode(
			getSourceRange(nodeInfo, context),
			context.getLeadingComments(),
			context.getTrailingComments(),
			context.getWhitespace(),
			context.getFormattingHints(),
			context.getParentNode(),
			children);
	}
}
