package io.github.cowwoc.styler.parser.converter.strategies;

import io.github.cowwoc.styler.ast.node.IdentifierNode;
import io.github.cowwoc.styler.parser.ArenaNodeStorage;
import io.github.cowwoc.styler.parser.converter.ConversionContext;

/**
 * Conversion strategy for identifier nodes.
 * <p>
 * Converts Arena identifier nodes to {@link IdentifierNode}, extracting the identifier
 * name from the source text using source position information.
 * </p>
 *
 * @since 1.0
 */
public final class IdentifierStrategy extends BaseConversionStrategy<IdentifierNode>
{
	/**
	 * Creates an identifier conversion strategy.
	 *
	 * @param nodeType the Arena node type constant for identifiers
	 */
	public IdentifierStrategy(byte nodeType)
{
		super(nodeType, IdentifierNode.class);
	}

	@Override
	public IdentifierNode convert(int nodeId, ArenaNodeStorage nodeStorage,
		ConversionContext context)
{
		ArenaNodeStorage.NodeInfo nodeInfo = nodeStorage.getNode(nodeId);

		// Extract actual identifier name from source text
		String identifierName = context.getSourceText(
			nodeInfo.startOffset(),
			nodeInfo.endOffset());

		return new IdentifierNode(
			getSourceRange(nodeInfo, context),
			context.getLeadingComments(),
			context.getTrailingComments(),
			context.getWhitespace(),
			context.getFormattingHints(),
			context.getParentNode(),
			identifierName);
	}
}
