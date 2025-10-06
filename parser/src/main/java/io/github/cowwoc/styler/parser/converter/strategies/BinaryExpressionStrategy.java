package io.github.cowwoc.styler.parser.converter.strategies;

import io.github.cowwoc.styler.ast.ASTNode;
import io.github.cowwoc.styler.ast.node.BinaryExpressionNode;
import io.github.cowwoc.styler.parser.ArenaNodeStorage;
import io.github.cowwoc.styler.parser.NodeType;
import java.util.List;
import io.github.cowwoc.styler.parser.converter.ConversionContext;

/**
 * Conversion strategy for binary expression nodes.
 *
 * @since 1.0
 */
public final class BinaryExpressionStrategy extends BaseConversionStrategy<BinaryExpressionNode>
{
	/**
	 * Creates a new conversion strategy for this node type.
	 */
	public BinaryExpressionStrategy()
	{
		super(NodeType.BINARY_EXPRESSION, BinaryExpressionNode.class);
	}

	@Override
	public BinaryExpressionNode convert(int nodeId, ArenaNodeStorage nodeStorage,
		ConversionContext context)
	{
		ArenaNodeStorage.NodeInfo nodeInfo = nodeStorage.getNode(nodeId);
		List<Integer> childIds = nodeInfo.childIds();

		// Get child node positions to extract operator between them
		ArenaNodeStorage.NodeInfo leftInfo = nodeStorage.getNode(childIds.get(0));
		ArenaNodeStorage.NodeInfo rightInfo = nodeStorage.getNode(childIds.get(1));

		ASTNode left = context.convertNode(childIds.get(0), nodeStorage);
		ASTNode right = context.convertNode(childIds.get(1), nodeStorage);

		// Extract operator from text between left and right operands
		String operator = extractOperator(leftInfo, rightInfo, context);

		return new BinaryExpressionNode(
			getSourceRange(nodeInfo, context),
			context.getLeadingComments(),
			context.getTrailingComments(),
			context.getWhitespace(),
			context.getFormattingHints(),
			context.getParentNode(),
			left,
			operator,
			right);
	}

	/**
	 * Extracts the binary operator from source text between left and right operands.
	 *
	 * @param leftInfo the left operand node information
	 * @param rightInfo the right operand node information
	 * @param context the conversion context for source text access
	 * @return the operator string (e.g., "+", "==", "&&", "instanceof")
	 */
	private String extractOperator(ArenaNodeStorage.NodeInfo leftInfo,
		ArenaNodeStorage.NodeInfo rightInfo, ConversionContext context)
	{
		// Extract text between left operand end and right operand start
		String operatorRegion = context.getSourceText(
			leftInfo.endOffset(),
			rightInfo.startOffset());

		// Remove whitespace to get the operator
		return operatorRegion.trim();
	}
}
