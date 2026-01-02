package io.github.cowwoc.styler.formatter.importorg.internal;

import io.github.cowwoc.styler.ast.core.ImportAttribute;
import io.github.cowwoc.styler.ast.core.ModuleImportAttribute;
import io.github.cowwoc.styler.ast.core.NodeArena;
import io.github.cowwoc.styler.ast.core.NodeIndex;
import io.github.cowwoc.styler.ast.core.NodeType;
import io.github.cowwoc.styler.formatter.AstPositionIndex;
import io.github.cowwoc.styler.formatter.TransformationContext;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;

/**
 * Extracts import declarations from Java source code using AST traversal.
 * <p>
 * Uses the parser's AST to accurately identify import declarations, avoiding false positives
 * from "import" appearing in strings or comments.
 * <p>
 * <b>Thread-safety</b>: This class is stateless and thread-safe.
 */
public final class ImportExtractor
{
	private ImportExtractor()
	{
		// Utility class
	}

	/**
	 * Extracts all import declarations from the source code.
	 * <p>
	 * Handles:
	 * <ul>
	 *   <li>Regular imports: {@code import java.util.List;}</li>
	 *   <li>Static imports: {@code import static java.lang.Math.PI;}</li>
	 *   <li>Wildcard imports: {@code import java.util.*;}</li>
	 *   <li>Module imports (JEP 511): {@code import module java.base;}</li>
	 * </ul>
	 *
	 * @param context transformation context with source code and AST
	 * @return list of extracted import declarations
	 * @throws NullPointerException if {@code context} is {@code null}
	 */
	public static List<ImportDeclaration> extract(TransformationContext context)
	{
		requireThat(context, "context").isNotNull();

		List<ImportDeclaration> imports = new ArrayList<>();
		AstPositionIndex positionIndex = context.positionIndex();
		NodeArena arena = context.arena();

		// Process regular and static import nodes (both use IMPORT_DECLARATION with isStatic attribute)
		processImportNodes(context, positionIndex, arena, imports);
		processModuleImportNodes(context, positionIndex, arena, imports);

		// Sort by source position so first/last elements give import section bounds
		imports.sort(Comparator.comparingInt(ImportDeclaration::startPosition));
		return imports;
	}

	/**
	 * Processes import nodes and adds them to the imports list.
	 * <p>
	 * Both regular and static imports use the same {@code IMPORT_DECLARATION} node type,
	 * with the static flag stored in the {@code ImportAttribute}.
	 *
	 * @param context       the transformation context
	 * @param positionIndex the position index for AST queries
	 * @param arena         the node arena
	 * @param imports       the list to add declarations to
	 */
	private static void processImportNodes(TransformationContext context, AstPositionIndex positionIndex,
		NodeArena arena, List<ImportDeclaration> imports)
	{
		List<NodeIndex> importNodes = positionIndex.findNodesByType(NodeType.IMPORT_DECLARATION);

		for (NodeIndex node : importNodes)
		{
			context.checkDeadline();

			int startPosition = arena.getStart(node);
			// -1 because endPosition is inclusive, pointing to the semicolon
			int endPosition = arena.getEnd(node) - 1;

			// Get qualified name and static flag from the ImportAttribute stored in the AST node
			ImportAttribute attribute = arena.getImportAttribute(node);

			int lineNumber = context.getLineNumber(startPosition);

			ImportDeclaration importDecl = new ImportDeclaration(
				attribute.qualifiedName(),
				attribute.isStatic(),
				false,
				startPosition,
				endPosition,
				lineNumber);

			imports.add(importDecl);
		}
	}

	/**
	 * Processes module import nodes and adds them to the imports list.
	 *
	 * @param context       the transformation context
	 * @param positionIndex the position index for AST queries
	 * @param arena         the node arena
	 * @param imports       the list to add declarations to
	 */
	private static void processModuleImportNodes(TransformationContext context, AstPositionIndex positionIndex,
		NodeArena arena, List<ImportDeclaration> imports)
	{
		List<NodeIndex> importNodes = positionIndex.findNodesByType(NodeType.MODULE_IMPORT_DECLARATION);

		for (NodeIndex node : importNodes)
		{
			context.checkDeadline();

			int startPosition = arena.getStart(node);
			// -1 because endPosition is inclusive, pointing to the semicolon
			int endPosition = arena.getEnd(node) - 1;

			// Get module name from the ModuleImportAttribute stored in the AST node
			ModuleImportAttribute attribute = arena.getModuleImportAttribute(node);
			String moduleName = attribute.moduleName();

			int lineNumber = context.getLineNumber(startPosition);

			ImportDeclaration importDecl = new ImportDeclaration(
				moduleName,
				false,
				true,
				startPosition,
				endPosition,
				lineNumber);

			imports.add(importDecl);
		}
	}

	/**
	 * Finds the end of the import section in the source code.
	 * Returns the position after the last import declaration.
	 *
	 * @param context transformation context with source code and AST
	 * @return position after the import section, or 0 if no imports found
	 * @throws NullPointerException if {@code context} is {@code null}
	 */
	static int findImportSectionEnd(TransformationContext context)
	{
		requireThat(context, "context").isNotNull();

		AstPositionIndex positionIndex = context.positionIndex();
		NodeArena arena = context.arena();

		int endPosition = 0;
		endPosition = updateEndPosition(positionIndex, arena, NodeType.IMPORT_DECLARATION, endPosition);
		endPosition = updateEndPosition(positionIndex, arena, NodeType.MODULE_IMPORT_DECLARATION, endPosition);

		return endPosition;
	}

	/**
	 * Updates the end position by checking nodes of the given type.
	 *
	 * @param positionIndex the position index
	 * @param arena         the node arena
	 * @param nodeType      the node type to check
	 * @param currentEnd    the current end position
	 * @return the updated end position
	 */
	private static int updateEndPosition(AstPositionIndex positionIndex, NodeArena arena,
		NodeType nodeType, int currentEnd)
	{
		List<NodeIndex> nodes = positionIndex.findNodesByType(nodeType);
		int endPosition = currentEnd;
		for (NodeIndex node : nodes)
		{
			int nodeEnd = arena.getEnd(node);
			if (nodeEnd > endPosition)
				endPosition = nodeEnd;
		}
		return endPosition;
	}
}
