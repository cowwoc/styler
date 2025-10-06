package io.github.cowwoc.styler.parser.converter.strategies;

import io.github.cowwoc.styler.ast.node.TypeParameterNode;
import io.github.cowwoc.styler.parser.ArenaNodeStorage;
import io.github.cowwoc.styler.parser.NodeType;
import io.github.cowwoc.styler.parser.converter.ConversionContext;

/**
 * Conversion strategy for type parameter nodes.
 *
 * @since 1.0
 */
public final class TypeParameterStrategy extends BaseConversionStrategy<TypeParameterNode>
{
	/**
	 * Creates a new conversion strategy for this node type.
	 */
	public TypeParameterStrategy()
	{
		super(NodeType.PARAMETERIZED_TYPE, TypeParameterNode.class);
	}

	@Override
	public TypeParameterNode convert(int nodeId, ArenaNodeStorage nodeStorage,
		ConversionContext context)
	{
		ArenaNodeStorage.NodeInfo nodeInfo = nodeStorage.getNode(nodeId);

		return new TypeParameterNode(
			getSourceRange(nodeInfo, context),
			context.getLeadingComments(),
			context.getTrailingComments(),
			context.getWhitespace(),
			context.getFormattingHints(),
			context.getParentNode());
	}
}
