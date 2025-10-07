package io.github.cowwoc.styler.parser.converter.strategies;

import io.github.cowwoc.styler.ast.ASTNode;
import io.github.cowwoc.styler.ast.node.MethodDeclarationNode;
import io.github.cowwoc.styler.parser.ArenaNodeStorage;
import io.github.cowwoc.styler.parser.NodeType;
import io.github.cowwoc.styler.parser.converter.ConversionContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Conversion strategy for method declaration nodes.
 *
 * @since 1.0
 */
public final class MethodDeclarationStrategy extends BaseConversionStrategy<MethodDeclarationNode>
{
	/**
	 * Creates a new conversion strategy for this node type.
	 */
	public MethodDeclarationStrategy()
	{
		super(NodeType.METHOD_DECLARATION, MethodDeclarationNode.class);
	}

	@Override
	@SuppressWarnings("PMD.AssignmentInOperand")
	public MethodDeclarationNode convert(int nodeId, ArenaNodeStorage nodeStorage,
		ConversionContext context)
	{
		ArenaNodeStorage.NodeInfo nodeInfo = nodeStorage.getNode(nodeId);
		List<Integer> childIds = nodeInfo.childIds();

		List<ASTNode> modifiers = new ArrayList<>();
		List<ASTNode> typeParameters = new ArrayList<>();
		ASTNode returnType = null;
		String methodName = null;
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
				case NodeType.PRIMITIVE_TYPE, NodeType.CLASS_TYPE, NodeType.ARRAY_TYPE ->
				{
					if (returnType == null)
					{
						returnType = context.convertNode(childId, nodeStorage);
					}
				}
				case NodeType.IDENTIFIER_EXPRESSION ->
				{
					if (methodName == null)
					{
						methodName = context.getSourceText(
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

		if (returnType == null || methodName == null)
		{
			throw new AssertionError(buildMissingChildError(nodeInfo, nodeStorage,
				new String[]{"IDENTIFIER_EXPRESSION"},
				"return type and method name"));
		}

		return new MethodDeclarationNode(
			getSourceRange(nodeInfo, context),
			context.getLeadingComments(),
			context.getTrailingComments(),
			context.getWhitespace(),
			context.getFormattingHints(),
			context.getParentNode(),
			modifiers,
			typeParameters,
			returnType,
			methodName,
			parameters,
			thrownExceptions,
			body);
	}
}
