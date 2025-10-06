package io.github.cowwoc.styler.parser.converter.strategies;

import io.github.cowwoc.styler.ast.node.RecordPatternNode;
import io.github.cowwoc.styler.parser.ArenaNodeStorage;
import io.github.cowwoc.styler.parser.NodeType;
import io.github.cowwoc.styler.parser.converter.ConversionContext;

/**
 * Conversion strategy for record pattern nodes.
 *
 * @since 1.0
 */
public final class RecordPatternStrategy extends BaseConversionStrategy<RecordPatternNode>
{
	/**
	 * Creates a new conversion strategy for this node type.
	 */
	public RecordPatternStrategy()
	{
		super(NodeType.TYPE_PATTERN, RecordPatternNode.class);
	}

	@Override
	public RecordPatternNode convert(int nodeId, ArenaNodeStorage nodeStorage,
		ConversionContext context)
	{
		ArenaNodeStorage.NodeInfo nodeInfo = nodeStorage.getNode(nodeId);

		return new RecordPatternNode(
			getSourceRange(nodeInfo, context),
			context.getLeadingComments(),
			context.getTrailingComments(),
			context.getWhitespace(),
			context.getFormattingHints(),
			context.getParentNode());
	}
}
