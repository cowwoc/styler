package io.github.cowwoc.styler.parser.converter.strategies;

import io.github.cowwoc.styler.ast.node.ModifierNode;
import io.github.cowwoc.styler.parser.ArenaNodeStorage;
import io.github.cowwoc.styler.parser.NodeType;
import io.github.cowwoc.styler.parser.converter.ConversionContext;

/**
 * Conversion strategy for modifier nodes.
 *
 * @since 1.0
 */
public final class ModifierStrategy extends BaseConversionStrategy<ModifierNode>
{
	/**
	 * Creates a new conversion strategy for this node type.
	 */
	public ModifierStrategy()
	{
		super(NodeType.MODIFIER, ModifierNode.class);
	}

	@Override
	public ModifierNode convert(int nodeId, ArenaNodeStorage nodeStorage,
		ConversionContext context)
	{
		ArenaNodeStorage.NodeInfo nodeInfo = nodeStorage.getNode(nodeId);

		String modifierText = context.getSourceText(
			nodeInfo.startOffset(),
			nodeInfo.endOffset()).trim();

		return new ModifierNode(
			getSourceRange(nodeInfo, context),
			context.getLeadingComments(),
			context.getTrailingComments(),
			context.getWhitespace(),
			context.getFormattingHints(),
			context.getParentNode(),
			modifierText);
	}
}
