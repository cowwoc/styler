package io.github.cowwoc.styler.formatter.importorg.internal;

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

		// Process both regular and static import nodes
		processImportNodes(context, positionIndex, arena, NodeType.IMPORT_DECLARATION, false, imports);
		processImportNodes(context, positionIndex, arena, NodeType.STATIC_IMPORT_DECLARATION, true, imports);

		// Sort by source position so first/last elements give import section bounds
		imports.sort(Comparator.comparingInt(ImportDeclaration::startPosition));
		return imports;
	}

	/**
	 * Processes import nodes of a specific type and adds them to the imports list.
	 *
	 * @param context       the transformation context
	 * @param positionIndex the position index for AST queries
	 * @param arena         the node arena
	 * @param nodeType      the node type to process
	 * @param isStatic      whether these are static imports
	 * @param imports       the list to add declarations to
	 */
	private static void processImportNodes(TransformationContext context, AstPositionIndex positionIndex,
		NodeArena arena, NodeType nodeType, boolean isStatic, List<ImportDeclaration> imports)
	{
		List<NodeIndex> importNodes = positionIndex.findNodesByType(nodeType);

		for (NodeIndex node : importNodes)
		{
			context.checkDeadline();

			int startPosition = arena.getStart(node);
			// -1 because endPosition is inclusive, pointing to the semicolon
			int endPosition = arena.getEnd(node) - 1;

			// Get qualified name from the ImportAttribute stored in the AST node
			String qualifiedName = arena.getImportAttribute(node).qualifiedName();

			int lineNumber = context.getLineNumber(startPosition);

			ImportDeclaration importDecl = new ImportDeclaration(
				qualifiedName,
				isStatic,
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
		endPosition = updateEndPosition(positionIndex, arena, NodeType.STATIC_IMPORT_DECLARATION, endPosition);

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
