package io.github.cowwoc.styler.parser.converter.strategies;

import io.github.cowwoc.styler.ast.ASTNode;
import io.github.cowwoc.styler.ast.node.AnnotationNode;
import io.github.cowwoc.styler.parser.ArenaNodeStorage;
import io.github.cowwoc.styler.parser.NodeType;
import io.github.cowwoc.styler.parser.converter.ConversionContext;

import java.util.ArrayList;
import java.util.List;

/**
 * Conversion strategy for annotation nodes.
 *
 * @since 1.0
 */
public final class AnnotationStrategy extends BaseConversionStrategy<AnnotationNode>
{
	/**
	 * Creates a new conversion strategy for this node type.
	 */
	public AnnotationStrategy()
	{
		super(NodeType.ANNOTATION, AnnotationNode.class);
	}

	@Override
	public AnnotationNode convert(int nodeId, ArenaNodeStorage nodeStorage,
		ConversionContext context)
	{
		ArenaNodeStorage.NodeInfo nodeInfo = nodeStorage.getNode(nodeId);
		List<Integer> childIds = nodeInfo.childIds();

		ASTNode name = null;
		List<ASTNode> elements = new ArrayList<>();

		for (int i = 0; i < childIds.size(); ++i)
		{
			ASTNode child = context.convertNode(childIds.get(i), nodeStorage);
			if (i == 0)
			{
				name = child;
			}
			else
			{
				elements.add(child);
			}
		}

		if (name == null)
		{
			throw new IllegalStateException(
				"Annotation must have a name at node " + nodeId);
		}

		return new AnnotationNode(
			getSourceRange(nodeInfo, context),
			context.getLeadingComments(),
			context.getTrailingComments(),
			context.getWhitespace(),
			context.getFormattingHints(),
			context.getParentNode(),
			name,
			elements);
	}
}
