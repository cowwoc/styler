package io.github.cowwoc.styler.parser.converter.strategies;

import io.github.cowwoc.styler.ast.node.PatternMatchNode;
import io.github.cowwoc.styler.parser.ArenaNodeStorage;
import io.github.cowwoc.styler.parser.NodeType;
import io.github.cowwoc.styler.parser.converter.ConversionContext;

/**
 * Conversion strategy for pattern match nodes.
 *
 * @since 1.0
 */
public final class PatternMatchStrategy extends BaseConversionStrategy<PatternMatchNode>
{
	/**
	 * Creates a new conversion strategy for this node type.
	 */
	public PatternMatchStrategy()
	{
		super(NodeType.PATTERN_EXPRESSION, PatternMatchNode.class);
	}

	@Override
	public PatternMatchNode convert(int nodeId, ArenaNodeStorage nodeStorage,
		ConversionContext context)
	{
		ArenaNodeStorage.NodeInfo nodeInfo = nodeStorage.getNode(nodeId);

		return new PatternMatchNode(
			getSourceRange(nodeInfo, context),
			context.getLeadingComments(),
			context.getTrailingComments(),
			context.getWhitespace(),
			context.getFormattingHints(),
			context.getParentNode());
	}
}
