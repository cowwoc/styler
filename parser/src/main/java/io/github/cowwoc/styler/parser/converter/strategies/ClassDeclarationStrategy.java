package io.github.cowwoc.styler.parser.converter.strategies;

import io.github.cowwoc.styler.ast.ASTNode;
import io.github.cowwoc.styler.ast.node.ClassDeclarationNode;
import io.github.cowwoc.styler.parser.ArenaNodeStorage;
import io.github.cowwoc.styler.parser.NodeType;
import io.github.cowwoc.styler.parser.converter.ConversionContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Conversion strategy for class declaration nodes.
 *
 * @since 1.0
 */
public final class ClassDeclarationStrategy extends BaseConversionStrategy<ClassDeclarationNode>
{
	/**
	 * Creates a new conversion strategy for this node type.
	 */
	public ClassDeclarationStrategy()
	{
		super(NodeType.CLASS_DECLARATION, ClassDeclarationNode.class);
	}

	@Override
	public ClassDeclarationNode convert(int nodeId, ArenaNodeStorage nodeStorage,
		ConversionContext context)
	{
		ArenaNodeStorage.NodeInfo nodeInfo = nodeStorage.getNode(nodeId);
		List<Integer> childIds = nodeInfo.childIds();

		List<ASTNode> modifiers = new ArrayList<>();
		String className = null;
		List<ASTNode> typeParameters = new ArrayList<>();
		Optional<ASTNode> superClass = Optional.empty();
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
					if (className == null)
					{
						className = context.getSourceText(
							childInfo.startOffset(),
							childInfo.endOffset());
					}
				}
				case NodeType.CLASS_TYPE, NodeType.PARAMETERIZED_TYPE ->
				{
					if (superClass.isEmpty())
					{
						superClass = Optional.of(context.convertNode(childId, nodeStorage));
					}
					else
					{
						interfaces.add(context.convertNode(childId, nodeStorage));
					}
				}
				case NodeType.METHOD_DECLARATION, NodeType.CONSTRUCTOR_DECLARATION,
					NodeType.FIELD_DECLARATION -> members.add(context.convertNode(childId, nodeStorage));
				default ->
				{
					// Handle other member types
				}
			}
		}

		if (className == null)
		{
			throw new AssertionError(buildMissingChildError(nodeInfo, nodeStorage,
				new String[]{"IDENTIFIER_EXPRESSION"},
				"class name"));
		}

		return new ClassDeclarationNode(
			getSourceRange(nodeInfo, context),
			context.getLeadingComments(),
			context.getTrailingComments(),
			context.getWhitespace(),
			context.getFormattingHints(),
			context.getParentNode(),
			modifiers,
			className,
			typeParameters,
			superClass,
			interfaces,
			members);
	}
}
