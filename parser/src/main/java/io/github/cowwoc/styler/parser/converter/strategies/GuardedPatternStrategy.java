package io.github.cowwoc.styler.parser.converter.strategies;

import io.github.cowwoc.styler.ast.node.GuardedPatternNode;
import io.github.cowwoc.styler.parser.ArenaNodeStorage;
import io.github.cowwoc.styler.parser.NodeType;
import io.github.cowwoc.styler.parser.converter.ConversionContext;

/**
 * Conversion strategy for guarded pattern nodes.
 *
 * @since 1.0
 */
public final class GuardedPatternStrategy extends BaseConversionStrategy<GuardedPatternNode>
{
	/**
	 * Creates a new conversion strategy for this node type.
	 */
	public GuardedPatternStrategy()
	{
		super(NodeType.GUARD_PATTERN, GuardedPatternNode.class);
	}

	@Override
	public GuardedPatternNode convert(int nodeId, ArenaNodeStorage nodeStorage,
		ConversionContext context)
	{
		ArenaNodeStorage.NodeInfo nodeInfo = nodeStorage.getNode(nodeId);

		return new GuardedPatternNode(
			getSourceRange(nodeInfo, context),
			context.getLeadingComments(),
			context.getTrailingComments(),
			context.getWhitespace(),
			context.getFormattingHints(),
			context.getParentNode());
	}
}
