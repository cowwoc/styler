package io.github.cowwoc.styler.parser.converter.strategies;

import io.github.cowwoc.styler.ast.ASTNode;
import io.github.cowwoc.styler.ast.node.InterfaceDeclarationNode;
import io.github.cowwoc.styler.parser.ArenaNodeStorage;
import io.github.cowwoc.styler.parser.NodeType;
import io.github.cowwoc.styler.parser.converter.ConversionContext;

import java.util.ArrayList;
import java.util.List;

/**
 * Conversion strategy for interface declaration nodes.
 *
 * @since 1.0
 */
public final class InterfaceDeclarationStrategy
	extends BaseConversionStrategy<InterfaceDeclarationNode>
{
	/**
	 * Creates a new conversion strategy for this node type.
	 */
	public InterfaceDeclarationStrategy()
	{
		super(NodeType.INTERFACE_DECLARATION, InterfaceDeclarationNode.class);
	}

	@Override
	public InterfaceDeclarationNode convert(int nodeId, ArenaNodeStorage nodeStorage,
		ConversionContext context)
	{
		ArenaNodeStorage.NodeInfo nodeInfo = nodeStorage.getNode(nodeId);
		List<Integer> childIds = nodeInfo.childIds();

		List<ASTNode> modifiers = new ArrayList<>();
		String interfaceName = null;
		List<ASTNode> typeParameters = new ArrayList<>();
		List<ASTNode> extendsList = new ArrayList<>();
		List<ASTNode> members = new ArrayList<>();

		for (int childId : childIds)
		{
			ArenaNodeStorage.NodeInfo childInfo = nodeStorage.getNode(childId);
			byte childType = childInfo.nodeType();

			switch (childType)
			{
				case NodeType.MODIFIER -> modifiers.add(context.convertNode(childId, nodeStorage));
				case NodeType.IDENTIFIER_EXPRESSION ->
				{
					if (interfaceName == null)
					{
						interfaceName = context.getSourceText(
							childInfo.startOffset(),
							childInfo.endOffset());
					}
				}
				case NodeType.CLASS_TYPE, NodeType.PARAMETERIZED_TYPE ->
					extendsList.add(context.convertNode(childId, nodeStorage));
				case NodeType.METHOD_DECLARATION, NodeType.FIELD_DECLARATION ->
					members.add(context.convertNode(childId, nodeStorage));
				default ->
				{
					// Handle other member types
				}
			}
		}

		if (interfaceName == null)
		{
			throw new IllegalStateException(
				"Interface declaration must have a name at node " + nodeId);
		}

		return new InterfaceDeclarationNode(
			getSourceRange(nodeInfo, context),
			context.getLeadingComments(),
			context.getTrailingComments(),
			context.getWhitespace(),
			context.getFormattingHints(),
			context.getParentNode(),
			modifiers,
			interfaceName,
			typeParameters,
			extendsList,
			members);
	}
}
