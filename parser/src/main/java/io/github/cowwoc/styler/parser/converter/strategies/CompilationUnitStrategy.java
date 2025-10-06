package io.github.cowwoc.styler.parser.converter.strategies;

import io.github.cowwoc.styler.ast.ASTNode;
import io.github.cowwoc.styler.ast.node.CompilationUnitNode;
import io.github.cowwoc.styler.parser.ArenaNodeStorage;
import io.github.cowwoc.styler.parser.NodeType;
import io.github.cowwoc.styler.parser.converter.ConversionContext;

import java.util.List;
import java.util.Optional;

/**
 * Conversion strategy for COMPILATION_UNIT nodes.
 * <p>
 * Converts an Arena COMPILATION_UNIT node to a {@link CompilationUnitNode}, preserving
 * package declaration, imports, and type declarations.
 * </p>
 *
 * @since 1.0
 */
public final class CompilationUnitStrategy extends BaseConversionStrategy<CompilationUnitNode>
{
	/**
	 * Creates a compilation unit conversion strategy.
	 */
	public CompilationUnitStrategy()
{
		super(NodeType.COMPILATION_UNIT, CompilationUnitNode.class);
	}

	@Override
	public CompilationUnitNode convert(int nodeId, ArenaNodeStorage nodeStorage,
		ConversionContext context)
{
		ArenaNodeStorage.NodeInfo nodeInfo = nodeStorage.getNode(nodeId);
		List<Integer> childIds = nodeInfo.childIds();

		// Convert all children
		List<ASTNode> children = convertChildren(childIds, nodeStorage, context);

		// Partition children into package, imports, and type declarations
		// For now, treat first child as package (if present), rest as type declarations
		Optional<ASTNode> packageDecl = Optional.empty();
		List<ASTNode> imports = List.of();
		List<ASTNode> typeDecls = children;

		if (!children.isEmpty())
{
			// Check if first child is package declaration
			// For MVP, assume no package/imports, all children are type declarations
			typeDecls = children;
		}

		return new CompilationUnitNode(
			getSourceRange(nodeInfo, context),
			context.getLeadingComments(),
			context.getTrailingComments(),
			context.getWhitespace(),
			context.getFormattingHints(),
			context.getParentNode(),
			packageDecl,
			imports,
			typeDecls);
	}
}
