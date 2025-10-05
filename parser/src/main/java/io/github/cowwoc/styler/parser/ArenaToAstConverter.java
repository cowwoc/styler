package io.github.cowwoc.styler.parser;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;
import io.github.cowwoc.styler.ast.ASTNode;
import io.github.cowwoc.styler.ast.FormattingHints;
import io.github.cowwoc.styler.ast.SourcePosition;
import io.github.cowwoc.styler.ast.SourceRange;
import io.github.cowwoc.styler.ast.WhitespaceInfo;
import io.github.cowwoc.styler.ast.node.ClassDeclarationNode;
import io.github.cowwoc.styler.ast.node.CompilationUnitNode;
import io.github.cowwoc.styler.ast.node.IdentifierNode;
import io.github.cowwoc.styler.ast.node.ImportDeclarationNode;
import io.github.cowwoc.styler.ast.node.PackageDeclarationNode;
import io.github.cowwoc.styler.parser.ArenaNodeStorage.NodeInfo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Converts Arena-based node storage to high-level AST objects.
 * <p>
 * This converter bridges the memory-efficient Arena representation (16 bytes per node)
 * with the high-level AST objects required by formatting rules. The conversion is
 * performed once per file after parsing completes.
 * <p>
 * <strong>Implementation Strategy</strong>: This is a minimal viable implementation
 * supporting only 4 node types (CompilationUnit, Package, Import, Class). Additional
 * node types will be added incrementally as the formatter requires them.
 * <p>
 * <strong>Performance</strong>: Conversion uses O(log n) source position mapping via
 * binary search in a precomputed line boundary index. Target performance is &lt;100ms
 * for typical Java source files.
 * <p>
 * <strong>Thread Safety</strong>: This class is stateless and thread-safe. The Arena
 * storage and source text are passed as parameters to each conversion method.
 *
 * @see ArenaNodeStorage
 * @see CompilationUnitNode
 */
public final class ArenaToAstConverter
{
	/**
	 * Converts the Arena root node to a CompilationUnitNode AST.
	 * <p>
	 * This is the main entry point for Arena-to-AST conversion. The Arena storage
	 * must remain alive during the conversion (do not close it until this method returns).
	 *
	 * @param rootNodeId the Arena node ID of the compilation unit (must be &gt;= 0)
	 * @param storage the Arena storage containing all parsed nodes (must not be null)
	 * @param sourceText the original Java source code for position mapping (must not be null)
	 * @return the fully-constructed CompilationUnitNode with all children converted
	 * @throws NullPointerException if {@code storage} or {@code sourceText} is null
	 * @throws IllegalStateException if Arena data is malformed (invalid offsets, broken parent-child links)
	 * @throws UnsupportedOperationException if conversion encounters unimplemented node types
	 */
	public CompilationUnitNode convert(int rootNodeId, ArenaNodeStorage storage, String sourceText)
	{
		requireThat(storage, "storage").isNotNull();
		requireThat(sourceText, "sourceText").isNotNull();
		requireThat(rootNodeId, "rootNodeId").isGreaterThanOrEqualTo(0);

		// Build line start index for O(log n) position mapping
		SourcePositionMapper positionMapper = new SourcePositionMapper(sourceText);

		// Convert root node (must be COMPILATION_UNIT)
		NodeInfo rootInfo = storage.getNode(rootNodeId);
		if (rootInfo.nodeType() != NodeType.COMPILATION_UNIT)
		{
			throw new IllegalStateException(
				"Root node must be COMPILATION_UNIT, got: " + NodeType.getTypeName(rootInfo.nodeType()));
		}

		return convertCompilationUnit(rootInfo, storage, positionMapper);
	}

	/**
	 * Converts a CompilationUnit node from Arena to AST.
	 * <p>
	 * A compilation unit consists of:
	 * <ul>
	 * <li>Optional package declaration (0 or 1)</li>
	 * <li>Import declarations (0 to N)</li>
	 * <li>Type declarations (0 to N)</li>
	 * </ul>
	 *
	 * @param nodeInfo the Arena node information
	 * @param storage the Arena storage
	 * @param positionMapper the source position mapper
	 * @return the CompilationUnitNode AST
	 */
	private CompilationUnitNode convertCompilationUnit(NodeInfo nodeInfo, ArenaNodeStorage storage,
		SourcePositionMapper positionMapper)
	{
		validateNodeData(nodeInfo, storage);

		// Extract package declaration (0 or 1 child)
		Optional<ASTNode> packageDecl = extractPackageDeclaration(nodeInfo, storage, positionMapper);

		// Extract import declarations (0 to N children)
		List<ASTNode> imports = extractImportDeclarations(nodeInfo, storage, positionMapper);

		// Extract type declarations (0 to N children)
		List<ASTNode> types = extractTypeDeclarations(nodeInfo, storage, positionMapper);

		// Build CompilationUnitNode
		SourceRange range = positionMapper.createSourceRange(nodeInfo);
		return new CompilationUnitNode(
			range,
			List.of(), // leadingComments - Arena doesn't store comments yet
			List.of(), // trailingComments
			WhitespaceInfo.none(), // whitespace - calculated from positions later
			FormattingHints.defaults(),
			Optional.empty(), // parent - CompilationUnit is root
			packageDecl,
			imports,
			types);
	}

	/**
	 * Extracts the optional package declaration from compilation unit children.
	 * <p>
	 * If the first child is a PACKAGE_DECLARATION node, it is converted and returned.
	 * Otherwise, returns Optional.empty().
	 *
	 * @param compilationUnitInfo the compilation unit node
	 * @param storage the Arena storage
	 * @param positionMapper the source position mapper
	 * @return Optional containing PackageDeclarationNode, or empty if no package
	 */
	private Optional<ASTNode> extractPackageDeclaration(NodeInfo compilationUnitInfo,
		ArenaNodeStorage storage, SourcePositionMapper positionMapper)
	{
		List<Integer> childIds = compilationUnitInfo.childIds();
		if (childIds.isEmpty())
		{
			return Optional.empty();
		}

		NodeInfo firstChild = storage.getNode(childIds.get(0));
		if (firstChild.nodeType() == NodeType.PACKAGE_DECLARATION)
		{
			return Optional.of(convertPackageDeclaration(firstChild, storage, positionMapper));
		}

		return Optional.empty();
	}

	/**
	 * Extracts import declarations from compilation unit children.
	 * <p>
	 * Scans children after the optional package declaration, collecting all
	 * IMPORT_DECLARATION nodes until a different node type is encountered.
	 *
	 * @param compilationUnitInfo the compilation unit node
	 * @param storage the Arena storage
	 * @param positionMapper the source position mapper
	 * @return list of ImportDeclarationNode objects (may be empty)
	 */
	private List<ASTNode> extractImportDeclarations(NodeInfo compilationUnitInfo,
		ArenaNodeStorage storage, SourcePositionMapper positionMapper)
	{
		List<Integer> childIds = compilationUnitInfo.childIds();
		List<ASTNode> imports = new ArrayList<>();

		// Start after package declaration (if present)
		int startIndex = 0;
		if (!childIds.isEmpty())
		{
			NodeInfo firstChild = storage.getNode(childIds.get(0));
			if (firstChild.nodeType() == NodeType.PACKAGE_DECLARATION)
			{
				startIndex = 1;
			}
		}

		// Collect consecutive IMPORT_DECLARATION nodes
		for (int i = startIndex; i < childIds.size(); ++i)
		{
			NodeInfo child = storage.getNode(childIds.get(i));
			if (child.nodeType() == NodeType.IMPORT_DECLARATION)
			{
				imports.add(convertImportDeclaration(child, storage, positionMapper));
			}
			else
			{
				break; // Stop at first non-import node
			}
		}

		return imports;
	}

	/**
	 * Extracts type declarations from compilation unit children.
	 * <p>
	 * Collects all type declaration nodes (CLASS_DECLARATION, INTERFACE_DECLARATION,
	 * etc.) from the children after package and imports.
	 *
	 * @param compilationUnitInfo the compilation unit node
	 * @param storage the Arena storage
	 * @param positionMapper the source position mapper
	 * @return list of type declaration nodes (may be empty)
	 */
	private List<ASTNode> extractTypeDeclarations(NodeInfo compilationUnitInfo,
		ArenaNodeStorage storage, SourcePositionMapper positionMapper)
	{
		List<Integer> childIds = compilationUnitInfo.childIds();
		List<ASTNode> types = new ArrayList<>();

		// Find where type declarations start (after package and imports)
		int startIndex = 0;
		for (int i = 0; i < childIds.size(); ++i)
		{
			NodeInfo child = storage.getNode(childIds.get(i));
			byte type = child.nodeType();
			if (type != NodeType.PACKAGE_DECLARATION && type != NodeType.IMPORT_DECLARATION)
			{
				startIndex = i;
				break;
			}
		}

		// Collect all type declarations
		for (int i = startIndex; i < childIds.size(); ++i)
		{
			NodeInfo child = storage.getNode(childIds.get(i));
			types.add(convertTypeDeclaration(child, storage, positionMapper));
		}

		return types;
	}

	/**
	 * Converts a PackageDeclaration node from Arena to AST.
	 * <p>
	 * Package declarations have the form: {@code package com.example;}
	 * <p>
	 * For MVP, creates a minimal PackageDeclarationNode using Builder pattern.
	 * Full parsing of package annotations and qualified name will be added in future phases.
	 *
	 * @param nodeInfo the Arena node information
	 * @param storage the Arena storage
	 * @param positionMapper the source position mapper
	 * @return the PackageDeclarationNode AST
	 */
	private PackageDeclarationNode convertPackageDeclaration(NodeInfo nodeInfo,
		ArenaNodeStorage storage, SourcePositionMapper positionMapper)
	{
		validateNodeData(nodeInfo, storage);

		SourceRange range = positionMapper.createSourceRange(nodeInfo);
		String sourceText = positionMapper.extractSourceText(nodeInfo);

		// Extract package name from source text (remove "package" keyword and semicolon)
		String packageName = sourceText.replaceFirst("^package\\s+", "").replaceFirst(";\\s*$", "").trim();

		// Create simple identifier node for package name (MVP - no qualified name parsing yet)
		SourceRange nameRange = range; // Use same range as package declaration for MVP
		IdentifierNode nameNode = new IdentifierNode.Builder().
			setRange(nameRange).
			setName(packageName).
			build();

		return new PackageDeclarationNode(
			range,
			List.of(), // leadingComments
			List.of(), // trailingComments
			WhitespaceInfo.none(),
			FormattingHints.defaults(),
			Optional.empty(), // parent
			List.of(), // annotations - MVP: empty
			nameNode);
	}

	/**
	 * Converts an ImportDeclaration node from Arena to AST.
	 * <p>
	 * Import declarations have the form: {@code import java.util.List;}
	 * or {@code import static java.util.Collections.*;}
	 * <p>
	 * For MVP, creates a minimal ImportDeclarationNode using Builder pattern.
	 * Full parsing of qualified names will be added in future phases.
	 *
	 * @param nodeInfo the Arena node information
	 * @param storage the Arena storage
	 * @param positionMapper the source position mapper
	 * @return the ImportDeclarationNode AST
	 */
	private ImportDeclarationNode convertImportDeclaration(NodeInfo nodeInfo,
		ArenaNodeStorage storage, SourcePositionMapper positionMapper)
	{
		validateNodeData(nodeInfo, storage);

		SourceRange range = positionMapper.createSourceRange(nodeInfo);
		String sourceText = positionMapper.extractSourceText(nodeInfo);

		// Extract import details from source text
		boolean isStatic = sourceText.contains("static");
		boolean isOnDemand = sourceText.contains("*");

		// Extract import name (remove "import", "static", semicolon)
		String importName = sourceText.
			replaceFirst("^import\\s+", "").
			replaceFirst("static\\s+", "").
			replaceFirst(";\\s*$", "").
			trim();

		// Create simple identifier node for import name (MVP - no qualified name parsing yet)
		SourceRange nameRange = range; // Use same range as import declaration for MVP
		IdentifierNode nameNode = new IdentifierNode.Builder().
			setRange(nameRange).
			setName(importName).
			build();

		return new ImportDeclarationNode(
			range,
			List.of(), // leadingComments
			List.of(), // trailingComments
			WhitespaceInfo.none(),
			FormattingHints.defaults(),
			Optional.empty(), // parent
			isStatic,
			nameNode,
			isOnDemand);
	}

	/**
	 * Converts a type declaration node from Arena to AST.
	 * <p>
	 * Dispatches to the appropriate converter based on node type.
	 * Currently supports CLASS_DECLARATION only; other types throw UnsupportedOperationException.
	 *
	 * @param nodeInfo the Arena node information
	 * @param storage the Arena storage
	 * @param positionMapper the source position mapper
	 * @return the type declaration AST node
	 * @throws UnsupportedOperationException if node type is not yet implemented
	 */
	private ASTNode convertTypeDeclaration(NodeInfo nodeInfo, ArenaNodeStorage storage,
		SourcePositionMapper positionMapper)
	{
		return switch (nodeInfo.nodeType())
		{
			case NodeType.CLASS_DECLARATION -> convertClassDeclaration(nodeInfo, storage, positionMapper);
			default -> throw new UnsupportedOperationException(
				"Node type not yet implemented: " + NodeType.getTypeName(nodeInfo.nodeType()) +
				" at position " + nodeInfo.startOffset() + ". " +
				"Add implementation when formatter requires this type.");
		};
	}

	/**
	 * Converts a ClassDeclaration node from Arena to AST.
	 * <p>
	 * Class declarations have the form: {@code [modifiers] class Name [extends X] [implements Y] { }}
	 * <p>
	 * This is a minimal implementation for MVP - children (methods, fields) are not yet converted.
	 * Full parsing of modifiers, type parameters, extends/implements clauses will be added in future phases.
	 *
	 * @param nodeInfo the Arena node information
	 * @param storage the Arena storage
	 * @param positionMapper the source position mapper
	 * @return the ClassDeclarationNode AST
	 */
	private ClassDeclarationNode convertClassDeclaration(NodeInfo nodeInfo, ArenaNodeStorage storage,
		SourcePositionMapper positionMapper)
	{
		validateNodeData(nodeInfo, storage);

		SourceRange range = positionMapper.createSourceRange(nodeInfo);
		String sourceText = positionMapper.extractSourceText(nodeInfo);

		// Extract class name from source text (simple regex for MVP)
		String className = extractClassName(sourceText);

		return new ClassDeclarationNode(
			range,
			List.of(), // leadingComments
			List.of(), // trailingComments
			WhitespaceInfo.none(),
			FormattingHints.defaults(),
			Optional.empty(), // parent
			List.of(), // modifiers - MVP: empty
			className,
			List.of(), // typeParameters - MVP: empty
			Optional.empty(), // superClass - MVP: none
			List.of(), // interfaces - MVP: empty
			List.of()); // members - MVP: empty
	}

	/**
	 * Extracts the class name from source text.
	 * <p>
	 * This is a simple regex-based extraction for MVP. Full parsing will use proper AST parsing.
	 *
	 * @param sourceText the class declaration source text
	 * @return the extracted class name
	 */
	private String extractClassName(String sourceText)
	{
		// Simple pattern: find "class" keyword followed by identifier
		Pattern pattern = Pattern.compile("class\\s+([A-Za-z_][A-Za-z0-9_]*)");
		Matcher matcher = pattern.matcher(sourceText);
		if (matcher.find())
		{
			return matcher.group(1);
		}
		// Fallback: use "UnknownClass" if pattern doesn't match
		return "UnknownClass";
	}

	/**
	 * Validates Arena node data integrity.
	 * <p>
	 * Performs defensive checks to ensure Arena data is well-formed:
	 * <ul>
	 * <li>Source position bounds</li>
	 * <li>Source range validity</li>
	 * <li>Parent-child relationship integrity</li>
	 * <li>Child ID bounds</li>
	 * </ul>
	 *
	 * @param nodeInfo the node to validate
	 * @param storage the Arena storage
	 * @throws IllegalStateException if validation fails
	 */
	private void validateNodeData(NodeInfo nodeInfo, ArenaNodeStorage storage)
	{
		// Check 1: Node ID bounds
		if (nodeInfo.nodeId() < 0 || nodeInfo.nodeId() >= storage.getNodeCount())
		{
			throw new IllegalStateException(
				"Invalid node ID: " + nodeInfo.nodeId() +
				" (valid range: 0-" + (storage.getNodeCount() - 1) + ")");
		}

		// Check 2: Source position bounds (startOffset must be non-negative)
		if (nodeInfo.startOffset() < 0)
		{
			throw new IllegalStateException(
				"Invalid node start offset: " + nodeInfo.startOffset() +
				" for " + NodeType.getTypeName(nodeInfo.nodeType()));
		}

		// Check 3: Length must be non-negative
		if (nodeInfo.length() < 0)
		{
			throw new IllegalStateException(
				"Invalid node length: " + nodeInfo.length() +
				" for " + NodeType.getTypeName(nodeInfo.nodeType()) +
				" at offset " + nodeInfo.startOffset());
		}

		// Check 4: Parent ID validity (if not root)
		if (nodeInfo.parentId() >= 0 && nodeInfo.parentId() >= storage.getNodeCount())
		{
			throw new IllegalStateException(
				"Invalid parent ID " + nodeInfo.parentId() +
				" for node " + nodeInfo.nodeId() +
				" (max nodeId=" + (storage.getNodeCount() - 1) + ")");
		}

		// Check 5: Child ID bounds
		for (int childId : nodeInfo.childIds())
		{
			if (childId < 0 || childId >= storage.getNodeCount())
			{
				throw new IllegalStateException(
					"Invalid child ID " + childId + " for node " + nodeInfo.nodeId() +
					" (valid range: 0-" + (storage.getNodeCount() - 1) + ")");
			}
		}
	}

	/**
	 * Helper class for efficient source position mapping.
	 * <p>
	 * Converts Arena's offset-based positions (character index) to AST's
	 * line/column positions (1-based line and column numbers).
	 * <p>
	 * <strong>Performance</strong>: Uses precomputed line boundaries with
	 * binary search for O(log n) position lookup.
	 */
	private static final class SourcePositionMapper
	{
		private final String sourceText;
		private final int[] lineStartOffsets;

		/**
		 * Creates a position mapper for the given source text.
		 * <p>
		 * Preprocesses the source text once to build a line boundary index.
		 *
		 * @param sourceText the Java source code
		 */
		SourcePositionMapper(String sourceText)
		{
			this.sourceText = sourceText;
			this.lineStartOffsets = buildLineStartIndex(sourceText);
		}

		/**
		 * Builds an index of line start offsets for binary search.
		 * <p>
		 * Line 1 starts at offset 0. Each subsequent line starts at the
		 * character position immediately after a newline character.
		 *
		 * @param sourceText the source text to index
		 * @return array of line start offsets
		 */
		private static int[] buildLineStartIndex(String sourceText)
		{
			List<Integer> starts = new ArrayList<>();
			starts.add(0); // Line 1 starts at offset 0

			for (int i = 0; i < sourceText.length(); ++i)
			{
				if (sourceText.charAt(i) == '\n')
				{
					starts.add(i + 1); // Next line starts after \n
				}
			}

			return starts.stream().mapToInt(Integer::intValue).toArray();
		}

		/**
		 * Converts an offset to a SourcePosition using binary search.
		 * <p>
		 * Line and column numbers are 1-based as required by SourcePosition.
		 *
		 * @param offset the character offset in source text
		 * @return the SourcePosition (1-based line and column)
		 */
		SourcePosition offsetToPosition(int offset)
		{
			// Binary search for line number
			int line = Arrays.binarySearch(lineStartOffsets, offset);

			if (line < 0)
			{
				line = -line - 2; // Convert insertion point to line number
			}

			int lineStart = lineStartOffsets[line];
			int column = offset - lineStart + 1; // 1-based column

			return new SourcePosition(line + 1, column); // 1-based line
		}

		/**
		 * Creates a SourceRange from Arena node position information.
		 *
		 * @param nodeInfo the Arena node
		 * @return the SourceRange covering the node's source span
		 */
		SourceRange createSourceRange(NodeInfo nodeInfo)
		{
			SourcePosition start = offsetToPosition(nodeInfo.startOffset());
			SourcePosition end = offsetToPosition(nodeInfo.endOffset());
			return new SourceRange(start, end);
		}

		/**
		 * Extracts the source text for a given Arena node.
		 *
		 * @param nodeInfo the Arena node
		 * @return the source text substring covering this node
		 */
		String extractSourceText(NodeInfo nodeInfo)
		{
			return sourceText.substring(nodeInfo.startOffset(), nodeInfo.endOffset());
		}
	}
}
