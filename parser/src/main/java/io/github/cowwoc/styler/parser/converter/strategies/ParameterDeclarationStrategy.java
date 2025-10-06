package io.github.cowwoc.styler.parser.converter.strategies;

import io.github.cowwoc.styler.ast.ASTNode;
import io.github.cowwoc.styler.ast.node.ParameterDeclarationNode;
import io.github.cowwoc.styler.ast.node.PrimitiveTypeNode;
import io.github.cowwoc.styler.parser.ArenaNodeStorage;
import io.github.cowwoc.styler.parser.NodeType;
import io.github.cowwoc.styler.parser.converter.ConversionContext;

import java.util.ArrayList;
import java.util.List;

/**
 * Conversion strategy for parameter declaration nodes.
 *
 * @since 1.0
 */
public final class ParameterDeclarationStrategy
	extends BaseConversionStrategy<ParameterDeclarationNode>
{
	/**
	 * Creates a new conversion strategy for this node type.
	 */
	public ParameterDeclarationStrategy()
	{
		super(NodeType.PARAMETER_DECLARATION, ParameterDeclarationNode.class);
	}

	@Override
	public ParameterDeclarationNode convert(int nodeId, ArenaNodeStorage nodeStorage,
		ConversionContext context)
	{
		ArenaNodeStorage.NodeInfo nodeInfo = nodeStorage.getNode(nodeId);

		List<ASTNode> modifiers = new ArrayList<>();
		ASTNode type = new PrimitiveTypeNode(
			getSourceRange(nodeInfo, context),
			context.getLeadingComments(),
			context.getTrailingComments(),
			context.getWhitespace(),
			context.getFormattingHints(),
			context.getParentNode(),
			"void");
		String name = "param";
		boolean isVarArgs = false;

		return new ParameterDeclarationNode(
			getSourceRange(nodeInfo, context),
			context.getLeadingComments(),
			context.getTrailingComments(),
			context.getWhitespace(),
			context.getFormattingHints(),
			context.getParentNode(),
			modifiers,
			type,
			name,
			isVarArgs);
	}
}
