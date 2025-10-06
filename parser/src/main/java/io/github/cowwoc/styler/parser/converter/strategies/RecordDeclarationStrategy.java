package io.github.cowwoc.styler.parser.converter.strategies;

import io.github.cowwoc.styler.ast.ASTNode;
import io.github.cowwoc.styler.ast.node.RecordDeclarationNode;
import io.github.cowwoc.styler.parser.ArenaNodeStorage;
import io.github.cowwoc.styler.parser.NodeType;
import io.github.cowwoc.styler.parser.converter.ConversionContext;

import java.util.ArrayList;
import java.util.List;

/**
 * Conversion strategy for record declaration nodes.
 *
 * @since 1.0
 */
public final class RecordDeclarationStrategy extends BaseConversionStrategy<RecordDeclarationNode>
{
	/**
	 * Creates a new conversion strategy for this node type.
	 */
	public RecordDeclarationStrategy()
	{
		super(NodeType.RECORD_DECLARATION, RecordDeclarationNode.class);
	}

	@Override
	public RecordDeclarationNode convert(int nodeId, ArenaNodeStorage nodeStorage,
		ConversionContext context)
	{
		ArenaNodeStorage.NodeInfo nodeInfo = nodeStorage.getNode(nodeId);
		List<Integer> childIds = nodeInfo.childIds();

		List<ASTNode> modifiers = new ArrayList<>();
		String recordName = null;
		List<ASTNode> typeParameters = new ArrayList<>();
		List<ASTNode> parameters = new ArrayList<>();
		List<ASTNode> interfaces = new ArrayList<>();
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
					if (recordName == null)
					{
						recordName = context.getSourceText(
							childInfo.startOffset(),
							childInfo.endOffset());
					}
				}
				case NodeType.PARAMETER_DECLARATION -> parameters.add(
					context.convertNode(childId, nodeStorage));
				case NodeType.CLASS_TYPE, NodeType.PARAMETERIZED_TYPE ->
					interfaces.add(context.convertNode(childId, nodeStorage));
				case NodeType.METHOD_DECLARATION, NodeType.CONSTRUCTOR_DECLARATION,
					NodeType.FIELD_DECLARATION -> members.add(context.convertNode(childId, nodeStorage));
				default ->
				{
					// Handle other types
				}
			}
		}

		if (recordName == null)
		{
			throw new IllegalStateException(
				"Record declaration must have a name at node " + nodeId);
		}

		return new RecordDeclarationNode(
			getSourceRange(nodeInfo, context),
			context.getLeadingComments(),
			context.getTrailingComments(),
			context.getWhitespace(),
			context.getFormattingHints(),
			context.getParentNode(),
			modifiers,
			recordName,
			typeParameters,
			parameters,
			interfaces,
			members);
	}
}
