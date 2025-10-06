package io.github.cowwoc.styler.parser.converter.strategies;

import io.github.cowwoc.styler.ast.node.QualifiedNameNode;
import io.github.cowwoc.styler.parser.ArenaNodeStorage;
import io.github.cowwoc.styler.parser.NodeType;
import io.github.cowwoc.styler.parser.converter.ConversionContext;

import java.util.List;

/**
 * Conversion strategy for qualified name nodes.
 *
 * @since 1.0
 */
public final class QualifiedNameStrategy extends BaseConversionStrategy<QualifiedNameNode>
{
	/**
	 * Creates a new conversion strategy for this node type.
	 */
	public QualifiedNameStrategy()
	{
		super(NodeType.FIELD_ACCESS_EXPRESSION, QualifiedNameNode.class);
	}

	@Override
	public QualifiedNameNode convert(int nodeId, ArenaNodeStorage nodeStorage,
		ConversionContext context)
	{
		ArenaNodeStorage.NodeInfo nodeInfo = nodeStorage.getNode(nodeId);

		// Split qualified name by dots
		String sourceText = context.getSourceText(nodeInfo.startOffset(), nodeInfo.endOffset());
		List<String> parts = List.of(sourceText.split("\\."));

		return new QualifiedNameNode(
			getSourceRange(nodeInfo, context),
			context.getLeadingComments(),
			context.getTrailingComments(),
			context.getWhitespace(),
			context.getFormattingHints(),
			context.getParentNode(),
			parts);
	}
}
