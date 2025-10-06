package io.github.cowwoc.styler.parser.converter.strategies;

import io.github.cowwoc.styler.ast.node.MethodReferenceNode;
import io.github.cowwoc.styler.parser.ArenaNodeStorage;
import io.github.cowwoc.styler.parser.NodeType;
import io.github.cowwoc.styler.parser.converter.ConversionContext;

/**
 * Conversion strategy for method reference nodes.
 *
 * @since 1.0
 */
public final class MethodReferenceStrategy extends BaseConversionStrategy<MethodReferenceNode>
{
	/**
	 * Creates a new conversion strategy for this node type.
	 */
	public MethodReferenceStrategy()
	{
		super(NodeType.METHOD_REFERENCE_EXPRESSION, MethodReferenceNode.class);
	}

	@Override
	public MethodReferenceNode convert(int nodeId, ArenaNodeStorage nodeStorage,
		ConversionContext context)
	{
		ArenaNodeStorage.NodeInfo nodeInfo = nodeStorage.getNode(nodeId);

		return new MethodReferenceNode(
			getSourceRange(nodeInfo, context),
			context.getLeadingComments(),
			context.getTrailingComments(),
			context.getWhitespace(),
			context.getFormattingHints(),
			context.getParentNode());
	}
}
