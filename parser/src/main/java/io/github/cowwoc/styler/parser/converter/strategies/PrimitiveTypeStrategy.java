package io.github.cowwoc.styler.parser.converter.strategies;

import io.github.cowwoc.styler.ast.node.PrimitiveTypeNode;
import io.github.cowwoc.styler.parser.ArenaNodeStorage;
import io.github.cowwoc.styler.parser.NodeType;
import io.github.cowwoc.styler.parser.converter.ConversionContext;

/**
 * Conversion strategy for primitive type nodes.
 *
 * @since 1.0
 */
public final class PrimitiveTypeStrategy extends BaseConversionStrategy<PrimitiveTypeNode>
{
	/**
	 * Creates a new conversion strategy for this node type.
	 */
	public PrimitiveTypeStrategy()
	{
		super(NodeType.PRIMITIVE_TYPE, PrimitiveTypeNode.class);
	}

	@Override
	public PrimitiveTypeNode convert(int nodeId, ArenaNodeStorage nodeStorage,
		ConversionContext context)
	{
		ArenaNodeStorage.NodeInfo nodeInfo = nodeStorage.getNode(nodeId);

		String typeName = context.getSourceText(
			nodeInfo.startOffset(),
			nodeInfo.endOffset()).trim();

		return new PrimitiveTypeNode(
			getSourceRange(nodeInfo, context),
			context.getLeadingComments(),
			context.getTrailingComments(),
			context.getWhitespace(),
			context.getFormattingHints(),
			context.getParentNode(),
			typeName);
	}
}
