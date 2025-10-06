package io.github.cowwoc.styler.parser.converter.strategies;

import io.github.cowwoc.styler.ast.ASTNode;
import io.github.cowwoc.styler.ast.node.EnumDeclarationNode;
import io.github.cowwoc.styler.parser.ArenaNodeStorage;
import io.github.cowwoc.styler.parser.NodeType;
import io.github.cowwoc.styler.parser.converter.ConversionContext;

import java.util.ArrayList;
import java.util.List;

/**
 * Conversion strategy for enum declaration nodes.
 *
 * @since 1.0
 */
public final class EnumDeclarationStrategy extends BaseConversionStrategy<EnumDeclarationNode>
{
	/**
	 * Creates a new conversion strategy for this node type.
	 */
	public EnumDeclarationStrategy()
	{
		super(NodeType.ENUM_DECLARATION, EnumDeclarationNode.class);
	}

	@Override
	public EnumDeclarationNode convert(int nodeId, ArenaNodeStorage nodeStorage,
		ConversionContext context)
	{
		ArenaNodeStorage.NodeInfo nodeInfo = nodeStorage.getNode(nodeId);
		List<Integer> childIds = nodeInfo.childIds();

		List<ASTNode> modifiers = new ArrayList<>();
		String enumName = null;
		List<ASTNode> interfaces = new ArrayList<>();
		List<ASTNode> constants = new ArrayList<>();
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
					if (enumName == null)
					{
						enumName = context.getSourceText(
							childInfo.startOffset(),
							childInfo.endOffset());
					}
				}
				case NodeType.CLASS_TYPE, NodeType.PARAMETERIZED_TYPE ->
					interfaces.add(context.convertNode(childId, nodeStorage));
				case NodeType.ENUM_CONSTANT -> constants.add(context.convertNode(childId, nodeStorage));
				case NodeType.METHOD_DECLARATION, NodeType.CONSTRUCTOR_DECLARATION,
					NodeType.FIELD_DECLARATION -> members.add(context.convertNode(childId, nodeStorage));
				default ->
				{
					// Handle other types
				}
			}
		}

		if (enumName == null)
		{
			throw new IllegalStateException(
				"Enum declaration must have a name at node " + nodeId);
		}

		return new EnumDeclarationNode(
			getSourceRange(nodeInfo, context),
			context.getLeadingComments(),
			context.getTrailingComments(),
			context.getWhitespace(),
			context.getFormattingHints(),
			context.getParentNode(),
			modifiers,
			enumName,
			interfaces,
			constants,
			members);
	}
}
