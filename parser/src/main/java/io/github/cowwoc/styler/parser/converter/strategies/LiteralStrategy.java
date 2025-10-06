package io.github.cowwoc.styler.parser.converter.strategies;

import io.github.cowwoc.styler.ast.node.StringLiteralNode;
import io.github.cowwoc.styler.parser.ArenaNodeStorage;
import io.github.cowwoc.styler.parser.converter.ConversionContext;

/**
 * Conversion strategy for literal expression nodes.
 * <p>
 * Converts Arena literal nodes to appropriate AST literal nodes (String, Number, Boolean, etc.).
 * For MVP, creates StringLiteralNode as a placeholder.
 * </p>
 *
 * @since 1.0
 */
public final class LiteralStrategy extends BaseConversionStrategy<StringLiteralNode>
{
	/**
	 * Creates a literal conversion strategy.
	 *
	 * @param nodeType the Arena node type constant for literals
	 */
	public LiteralStrategy(byte nodeType)
{
		super(nodeType, StringLiteralNode.class);
	}

	@Override
	public StringLiteralNode convert(int nodeId, ArenaNodeStorage nodeStorage,
		ConversionContext context)
{
		ArenaNodeStorage.NodeInfo nodeInfo = nodeStorage.getNode(nodeId);

		// Extract actual literal value from source text
		String literalValue = context.getSourceText(
			nodeInfo.startOffset(),
			nodeInfo.endOffset());

		return new StringLiteralNode(
			getSourceRange(nodeInfo, context),
			context.getLeadingComments(),
			context.getTrailingComments(),
			context.getWhitespace(),
			context.getFormattingHints(),
			context.getParentNode(),
			literalValue);
	}
}
