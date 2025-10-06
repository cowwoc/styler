package io.github.cowwoc.styler.parser.converter.strategies;

import io.github.cowwoc.styler.ast.ASTNode;
import io.github.cowwoc.styler.ast.node.FieldDeclarationNode;
import io.github.cowwoc.styler.ast.node.PrimitiveTypeNode;
import io.github.cowwoc.styler.parser.ArenaNodeStorage;
import io.github.cowwoc.styler.parser.NodeType;
import io.github.cowwoc.styler.parser.converter.ConversionContext;

import java.util.ArrayList;
import java.util.List;

/**
 * Conversion strategy for field declaration nodes.
 *
 * @since 1.0
 */
public final class FieldDeclarationStrategy extends BaseConversionStrategy<FieldDeclarationNode>
{
	/**
	 * Creates a new conversion strategy for this node type.
	 */
	public FieldDeclarationStrategy()
	{
		super(NodeType.FIELD_DECLARATION, FieldDeclarationNode.class);
	}

	@Override
	public FieldDeclarationNode convert(int nodeId, ArenaNodeStorage nodeStorage,
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
		List<ASTNode> variables = new ArrayList<>();

		return new FieldDeclarationNode(
			getSourceRange(nodeInfo, context),
			context.getLeadingComments(),
			context.getTrailingComments(),
			context.getWhitespace(),
			context.getFormattingHints(),
			context.getParentNode(),
			modifiers,
			type,
			variables);
	}
}
