package io.github.cowwoc.styler.parser.converter.strategies;

import io.github.cowwoc.styler.ast.ASTNode;
import io.github.cowwoc.styler.ast.node.AnnotationElementNode;
import io.github.cowwoc.styler.ast.node.PrimitiveTypeNode;
import io.github.cowwoc.styler.parser.ArenaNodeStorage;
import io.github.cowwoc.styler.parser.NodeType;
import io.github.cowwoc.styler.parser.converter.ConversionContext;

/**
 * Conversion strategy for annotation element nodes.
 *
 * @since 1.0
 */
public final class AnnotationElementStrategy extends BaseConversionStrategy<AnnotationElementNode>
{
	/**
	 * Creates a new conversion strategy for this node type.
	 */
	public AnnotationElementStrategy()
	{
		super(NodeType.ANNOTATION_ELEMENT, AnnotationElementNode.class);
	}

	@Override
	public AnnotationElementNode convert(int nodeId, ArenaNodeStorage nodeStorage,
		ConversionContext context)
	{
		ArenaNodeStorage.NodeInfo nodeInfo = nodeStorage.getNode(nodeId);

		String name = "element";
		ASTNode value = new PrimitiveTypeNode(
			getSourceRange(nodeInfo, context),
			context.getLeadingComments(),
			context.getTrailingComments(),
			context.getWhitespace(),
			context.getFormattingHints(),
			context.getParentNode(),
			"void");

		return new AnnotationElementNode(
			getSourceRange(nodeInfo, context),
			context.getLeadingComments(),
			context.getTrailingComments(),
			context.getWhitespace(),
			context.getFormattingHints(),
			context.getParentNode(),
			name,
			value);
	}
}
