package io.github.cowwoc.styler.parser.converter.strategies;

import io.github.cowwoc.styler.ast.ASTNode;
import io.github.cowwoc.styler.ast.node.BlockStatementNode;
import io.github.cowwoc.styler.ast.node.ConstructorDeclarationNode;
import io.github.cowwoc.styler.parser.ArenaNodeStorage;
import io.github.cowwoc.styler.parser.NodeType;
import io.github.cowwoc.styler.parser.converter.ConversionContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Conversion strategy for constructor declaration nodes.
 *
 * @since 1.0
 */
public final class ConstructorDeclarationStrategy
	extends BaseConversionStrategy<ConstructorDeclarationNode>
{
	/**
	 * Creates a new conversion strategy for this node type.
	 */
	public ConstructorDeclarationStrategy()
	{
		super(NodeType.CONSTRUCTOR_DECLARATION, ConstructorDeclarationNode.class);
	}

	@Override
	@SuppressWarnings("PMD.AssignmentInOperand")
	public ConstructorDeclarationNode convert(int nodeId, ArenaNodeStorage nodeStorage,
		ConversionContext context)
	{
		ArenaNodeStorage.NodeInfo nodeInfo = nodeStorage.getNode(nodeId);
		List<Integer> childIds = nodeInfo.childIds();

		List<ASTNode> modifiers = new ArrayList<>();
		List<ASTNode> typeParameters = new ArrayList<>();
		String constructorName = null;
		List<ASTNode> parameters = new ArrayList<>();
		List<ASTNode> thrownExceptions = new ArrayList<>();
		Optional<ASTNode> body = Optional.empty();

		for (int childId : childIds)
		{
			ArenaNodeStorage.NodeInfo childInfo = nodeStorage.getNode(childId);
			byte childType = childInfo.nodeType();

			switch (childType)
			{
				case NodeType.MODIFIER -> modifiers.add(context.convertNode(childId, nodeStorage));
				case NodeType.PARAMETERIZED_TYPE -> typeParameters.add(
					context.convertNode(childId, nodeStorage));
				case NodeType.IDENTIFIER_EXPRESSION ->
				{
					if (constructorName == null)
					{
						constructorName = context.getSourceText(
							childInfo.startOffset(),
							childInfo.endOffset());
					}
				}
				case NodeType.PARAMETER_DECLARATION -> parameters.add(
					context.convertNode(childId, nodeStorage));
				case NodeType.BLOCK_STATEMENT ->
					body = Optional.of(context.convertNode(childId, nodeStorage));
				default ->
				{
					// Handle other types
				}
			}
		}

		if (constructorName == null)
		{
			throw new IllegalStateException(
				"Constructor declaration requires a name at node " + nodeId);
		}

		ASTNode bodyNode = body.orElse(new BlockStatementNode(
			getSourceRange(nodeInfo, context),
			context.getLeadingComments(),
			context.getTrailingComments(),
			context.getWhitespace(),
			context.getFormattingHints(),
			context.getParentNode(),
			new ArrayList<>()));

		return new ConstructorDeclarationNode(
			getSourceRange(nodeInfo, context),
			context.getLeadingComments(),
			context.getTrailingComments(),
			context.getWhitespace(),
			context.getFormattingHints(),
			context.getParentNode(),
			modifiers,
			typeParameters,
			constructorName,
			parameters,
			thrownExceptions,
			bodyNode);
	}
}
