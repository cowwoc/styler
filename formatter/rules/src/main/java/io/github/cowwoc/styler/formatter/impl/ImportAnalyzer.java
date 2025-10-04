package io.github.cowwoc.styler.formatter.impl;

import io.github.cowwoc.styler.ast.ASTNode;
import io.github.cowwoc.styler.ast.node.CompilationUnitNode;
import io.github.cowwoc.styler.ast.node.ImportDeclarationNode;
import io.github.cowwoc.styler.ast.node.QualifiedNameNode;

import java.util.ArrayList;
import java.util.List;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;

/**
 * Analyzes and classifies import statements from a compilation unit.
 * <p>
 * This analyzer extracts import information and categorizes imports as:
 * <ul>
 *   <li>Static imports - {@code import static ...}</li>
 *   <li>Wildcard imports - {@code import ....*}</li>
 *   <li>Standard imports - regular single-type imports</li>
 * </ul>
 * <p>
 * The analyzer also extracts package names and simple type names for grouping and sorting.
 * <p>
 * <b>Thread Safety:</b> This class is stateless and thread-safe.
 *
 * @since {@code 1}.{@code 0}.{@code 0}
 * @author Import Organizer Team
 */
public final class ImportAnalyzer
{
	/**
	 * Represents a single analyzed import with all necessary classification information.
	 */
	public static final class ImportInfo
	{
		private final ImportDeclarationNode node;
		private final String fullName;
		private final String packageName;
		private final String simpleName;
		private final boolean isStatic;
		private final boolean isWildcard;

		/**
		 * Creates import information for an import declaration.
		 *
		 * @param node the AST node for the import
		 * @param fullName the fully qualified name (e.g., "java.util.List")
		 * @param packageName the package portion (e.g., "java.util")
		 * @param simpleName the simple type name (e.g., "List")
		 * @param isStatic whether this is a static import
		 * @param isWildcard whether this is a wildcard import
		 * @throws NullPointerException if any parameter is {@code null}
		 */
		public ImportInfo(ImportDeclarationNode node, String fullName, String packageName,
			String simpleName, boolean isStatic, boolean isWildcard)
		{
			requireThat(node, "node").isNotNull();
			requireThat(fullName, "fullName").isNotNull();
			requireThat(packageName, "packageName").isNotNull();
			requireThat(simpleName, "simpleName").isNotNull();

			this.node = node;
			this.fullName = fullName;
			this.packageName = packageName;
			this.simpleName = simpleName;
			this.isStatic = isStatic;
			this.isWildcard = isWildcard;
		}

		/**
		 * Returns the AST node for this import.
		 *
		 * @return the import declaration node
		 */
		public ImportDeclarationNode getNode()
		{
			return node;
		}

		/**
		 * Returns the fully qualified name of the import.
		 *
		 * @return the full name (e.g., "java.util.List" or "java.util.*")
		 */
		public String getFullName()
		{
			return fullName;
		}

		/**
		 * Returns the package name portion of the import.
		 *
		 * @return the package name (e.g., "java.util")
		 */
		public String getPackageName()
		{
			return packageName;
		}

		/**
		 * Returns the simple name of the imported type or member.
		 *
		 * @return the simple name (e.g., "List" or "*" for wildcards)
		 */
		public String getSimpleName()
		{
			return simpleName;
		}

		/**
		 * Checks if this is a static import.
		 *
		 * @return {@code true} if this is a static import
		 */
		public boolean isStatic()
		{
			return isStatic;
		}

		/**
		 * Checks if this is a wildcard import.
		 *
		 * @return {@code true} if this is a wildcard import (ends with .*)
		 */
		public boolean isWildcard()
		{
			return isWildcard;
		}
	}

	/**
	 * Analyzes all import declarations in a compilation unit.
	 *
	 * @param compilationUnit the compilation unit to analyze
	 * @return list of analyzed import information, in original source order
	 * @throws NullPointerException if compilationUnit is {@code null}
	 */
	public List<ImportInfo> analyze(CompilationUnitNode compilationUnit)
	{
		requireThat(compilationUnit, "compilationUnit").isNotNull();

		List<ImportInfo> result = new ArrayList<>();
		List<ASTNode> imports = compilationUnit.getImports();

		for (ASTNode importNode : imports)
		{
			if (importNode instanceof ImportDeclarationNode importDecl)
			{
				ImportInfo info = analyzeImport(importDecl);
				result.add(info);
			}
		}

		return result;
	}

	/**
	 * Analyzes a single import declaration node.
	 *
	 * @param importDecl the import declaration to analyze
	 * @return import information extracted from the declaration
	 */
	private ImportInfo analyzeImport(ImportDeclarationNode importDecl)
	{
		boolean isStatic = importDecl.isStatic();
		boolean isWildcard = importDecl.isOnDemand();

		String fullName = extractFullName(importDecl.getName());

		// Split into package and simple name
		String packageName;
		String simpleName;

		int lastDot = fullName.lastIndexOf('.');
		if (lastDot > 0)
		{
			packageName = fullName.substring(0, lastDot);
			simpleName = fullName.substring(lastDot + 1);
		}
		else
		{
			// Single identifier import (rare, but possible in default package)
			packageName = "";
			simpleName = fullName;
		}

		// For wildcard imports, append the * to fullName
		if (isWildcard)
		{
			fullName = fullName.concat(".*");
			simpleName = "*";
		}

		return new ImportInfo(importDecl, fullName, packageName, simpleName, isStatic, isWildcard);
	}

	/**
	 * Extracts the fully qualified name from an import name AST node.
	 * <p>
	 * Import names are represented as QualifiedNameNode instances containing
	 * the dot-separated parts of the import path.
	 *
	 * @param nameNode the AST node representing the import name
	 * @return the fully qualified name as a string
	 * @throws IllegalArgumentException if nameNode is not a QualifiedNameNode
	 */
	private String extractFullName(ASTNode nameNode)
	{
		if (nameNode instanceof QualifiedNameNode qualifiedName)
		{
			return qualifiedName.getQualifiedName();
		}
		throw new IllegalArgumentException(
			"Expected QualifiedNameNode but got: " + nameNode.getClass().getName());
	}
}
