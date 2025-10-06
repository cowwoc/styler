package io.github.cowwoc.styler.parser.converter.strategies;

import io.github.cowwoc.styler.ast.node.ClassTypeNode;
import io.github.cowwoc.styler.parser.ArenaNodeStorage;
import io.github.cowwoc.styler.parser.NodeType;
import io.github.cowwoc.styler.parser.converter.ConversionContext;

/**
 * Conversion strategy for class type nodes.
 *
 * @since 1.0
 */
public final class ClassTypeStrategy extends BaseConversionStrategy<ClassTypeNode>
{
	/**
	 * Creates a new conversion strategy for this node type.
	 */
	public ClassTypeStrategy()
	{
		super(NodeType.CLASS_TYPE, ClassTypeNode.class);
	}

	@Override
	public ClassTypeNode convert(int nodeId, ArenaNodeStorage nodeStorage,
		ConversionContext context)
	{
		ArenaNodeStorage.NodeInfo nodeInfo = nodeStorage.getNode(nodeId);

		return new ClassTypeNode(
			getSourceRange(nodeInfo, context),
			context.getLeadingComments(),
			context.getTrailingComments(),
			context.getWhitespace(),
			context.getFormattingHints(),
			context.getParentNode());
	}
}
