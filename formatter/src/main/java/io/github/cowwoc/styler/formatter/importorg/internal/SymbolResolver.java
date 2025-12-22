package io.github.cowwoc.styler.formatter.importorg.internal;

import io.github.cowwoc.styler.ast.core.NodeIndex;
import io.github.cowwoc.styler.ast.core.NodeType;
import io.github.cowwoc.styler.formatter.AstPositionIndex;
import io.github.cowwoc.styler.formatter.TransformationContext;
import io.github.cowwoc.styler.formatter.internal.ClasspathScanner;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;

/**
 * Resolves used identifiers to their import sources.
 * <p>
 * This utility determines which import provides each identifier used in source code. It handles
 * both explicit imports (which directly provide their simple name) and wildcard imports (which
 * require classpath scanning to determine available classes).
 * <p>
 * <b>Resolution Algorithm</b>:
 * <ol>
 *   <li>Extract package name and locally declared types from AST</li>
 *   <li>Build a map from simple names to explicit imports (non-wildcard imports provide their
 *       simple name directly)</li>
 *   <li>For each used identifier, attempt resolution:
 *     <ol>
 *       <li>Check if matched by an explicit import</li>
 *       <li>Check if matched by a wildcard import via classpath lookup</li>
 *       <li>Check if it is a java.lang type (implicitly imported)</li>
 *       <li>Check if it is a type declared locally in the same file</li>
 *       <li>Check if it is a same-package type (via classpath lookup)</li>
 *       <li>Otherwise mark as unresolved</li>
 *     </ol>
 *   </li>
 *   <li>Return resolution result with all mappings and any unresolved symbols</li>
 * </ol>
 * <p>
 * <b>Thread-safety</b>: This class is stateless and thread-safe.
 */
public final class SymbolResolver
{
	private SymbolResolver()
	{
		// Utility class
	}

	/**
	 * Resolves used identifiers to their import sources.
	 * <p>
	 * This method attempts to determine which import provides each identifier used in the source
	 * code. If any identifier cannot be resolved, it is added to the unresolved set, indicating
	 * that the classpath may be incomplete.
	 *
	 * @param imports         all import declarations in the file
	 * @param usedIdentifiers identifiers used in source code (excluding Java keywords)
	 * @param context         the transformation context with AST access
	 * @param scanner         classpath scanner for wildcard resolution (must not be {@code null})
	 * @return resolution result containing resolved mappings and any unresolved symbols
	 * @throws NullPointerException if any argument is {@code null}
	 */
	public static SymbolResolutionResult resolve(
		List<ImportDeclaration> imports,
		Set<String> usedIdentifiers,
		TransformationContext context,
		ClasspathScanner scanner)
	{
		requireThat(imports, "imports").isNotNull();
		requireThat(usedIdentifiers, "usedIdentifiers").isNotNull();
		requireThat(context, "context").isNotNull();
		requireThat(scanner, "scanner").isNotNull();

		// Extract package name and locally declared types from AST
		String packageName = extractPackageName(context);
		Set<String> localTypes = extractLocalTypeDeclarations(context);

		// Build lookup structures
		Map<String, String> explicitImports = buildExplicitImportMap(imports);
		List<ImportDeclaration> wildcardImports = imports.stream().
			filter(ImportDeclaration::isWildcard).
			toList();

		// Cache for wildcard package contents (expensive to compute)
		Map<String, Set<String>> wildcardPackageClasses = new HashMap<>();
		for (ImportDeclaration wildcard : wildcardImports)
		{
			String wildcardPackage = wildcard.packageName();
			if (!wildcardPackageClasses.containsKey(wildcardPackage))
			{
				wildcardPackageClasses.put(wildcardPackage, scanner.listPackageClasses(wildcardPackage));
			}
		}

		// Resolve each used identifier
		Map<String, String> resolved = new HashMap<>();
		Set<String> unresolved = new HashSet<>();

		for (String identifier : usedIdentifiers)
		{
			String resolvedImport = resolveIdentifier(
				identifier, explicitImports, wildcardImports, wildcardPackageClasses);

			if (resolvedImport != null)
			{
				resolved.put(identifier, resolvedImport);
			}
			else if (requiresImport(identifier, localTypes, packageName, scanner))
			{
				unresolved.add(identifier);
			}
			// else: identifier is from java.lang, declared locally, in same package, or a primitive/keyword
		}

		return new SymbolResolutionResult(resolved, unresolved);
	}

	/**
	 * Builds a map from simple name to qualified import for explicit (non-wildcard) imports.
	 *
	 * @param imports all import declarations
	 * @return map from simple name to qualified name
	 */
	private static Map<String, String> buildExplicitImportMap(List<ImportDeclaration> imports)
	{
		Map<String, String> result = new HashMap<>();
		for (ImportDeclaration imp : imports)
		{
			if (!imp.isWildcard())
			{
				result.put(imp.simpleName(), imp.qualifiedName());
			}
		}
		return result;
	}

	/**
	 * Resolves a single identifier to its import source.
	 *
	 * @param identifier            the identifier to resolve
	 * @param explicitImports       map from simple name to qualified name for explicit imports
	 * @param wildcardImports       list of wildcard import declarations
	 * @param wildcardPackageClasses cache of package contents for each wildcard
	 * @return the qualified import name, or {@code null} if not resolved
	 */
	private static String resolveIdentifier(
		String identifier,
		Map<String, String> explicitImports,
		List<ImportDeclaration> wildcardImports,
		Map<String, Set<String>> wildcardPackageClasses)
	{
		// Check explicit imports first (highest priority)
		String explicit = explicitImports.get(identifier);
		if (explicit != null)
		{
			return explicit;
		}

		// Check wildcard imports
		for (ImportDeclaration wildcard : wildcardImports)
		{
			String packageName = wildcard.packageName();
			Set<String> packageClasses = wildcardPackageClasses.get(packageName);

			if (packageClasses != null)
			{
				for (String qualifiedClass : packageClasses)
				{
					String simpleName = extractSimpleName(qualifiedClass);
					if (simpleName.equals(identifier))
					{
						// Convert internal format (with $) to import format (with .)
						return qualifiedClass.replace('$', '.');
					}
				}
			}
		}

		return null;
	}

	/**
	 * Determines whether an identifier requires an import to be present.
	 * <p>
	 * Returns {@code false} for identifiers that do not need imports:
	 * <ul>
	 *   <li>Lowercase identifiers (typically variables/methods, not types)</li>
	 *   <li>java.lang classes (implicitly imported)</li>
	 *   <li>Types declared locally in the same compilation unit</li>
	 *   <li>Same-package types (accessible without import)</li>
	 * </ul>
	 *
	 * @param identifier  the identifier to check
	 * @param localTypes  types declared locally in the same source file
	 * @param packageName the package name of the source file (empty string for default package)
	 * @param scanner     classpath scanner for java.lang and same-package lookup
	 * @return {@code true} if this identifier should have an import
	 */
	private static boolean requiresImport(
		String identifier,
		Set<String> localTypes,
		String packageName,
		ClasspathScanner scanner)
	{
		// Lowercase identifiers are typically variables/methods, not types
		if (Character.isLowerCase(identifier.charAt(0)))
		{
			return false;
		}

		// Check if it is a java.lang type (implicitly imported)
		String javaLangClass = "java.lang." + identifier;
		if (scanner.classExists(javaLangClass))
		{
			return false;
		}

		if (localTypes.contains(identifier))
		{
			return false;
		}

		// Check if it is a same-package type (no import needed for types in same package)
		if (!packageName.isEmpty())
		{
			String samePackageClass = packageName + "." + identifier;
			if (scanner.classExists(samePackageClass))
			{
				return false;
			}
		}

		// Uppercase identifier that is not in java.lang, not local, and not in same package
		// requires an import
		return true;
	}

	/**
	 * Extracts the simple name from a fully qualified class name.
	 * <p>
	 * Handles both regular classes and nested classes (using $).
	 *
	 * @param qualifiedName the fully qualified class name
	 * @return the simple class name
	 */
	private static String extractSimpleName(String qualifiedName)
	{
		// Handle nested classes: java.util.Map$Entry -> Entry
		int lastDollar = qualifiedName.lastIndexOf('$');
		if (lastDollar >= 0)
		{
			return qualifiedName.substring(lastDollar + 1);
		}

		// Handle regular classes: java.util.List -> List
		int lastDot = qualifiedName.lastIndexOf('.');
		if (lastDot >= 0)
		{
			return qualifiedName.substring(lastDot + 1);
		}

		return qualifiedName;
	}

	/**
	 * Extracts the package name from the AST.
	 *
	 * @param context the transformation context with AST access
	 * @return the package name, or empty string if no package declaration (default package)
	 */
	private static String extractPackageName(TransformationContext context)
	{
		AstPositionIndex positionIndex = context.positionIndex();
		List<NodeIndex> packageNodes = positionIndex.findNodesByType(NodeType.PACKAGE_DECLARATION);

		if (packageNodes.isEmpty())
		{
			return "";
		}

		// Get the package declaration text and extract the qualified name
		String packageText = context.getSourceText(packageNodes.get(0));
		// Format: "package foo.bar.baz;" - extract the qualified name
		String result = packageText.strip();
		if (result.startsWith("package "))
		{
			result = result.substring("package ".length());
		}
		result = result.replace(";", "").strip();
		return result;
	}

	/**
	 * Extracts all type declarations from the AST.
	 * <p>
	 * Finds all class, interface, enum, and record declarations in the file, including nested types.
	 * These identifiers do not need imports as they are defined locally.
	 *
	 * @param context the transformation context with AST access
	 * @return set of simple type names declared in the source file
	 */
	private static Set<String> extractLocalTypeDeclarations(TransformationContext context)
	{
		Set<String> types = new HashSet<>();
		AstPositionIndex positionIndex = context.positionIndex();

		// Find all type declaration nodes and extract their names
		for (NodeType typeNodeType : List.of(NodeType.CLASS_DECLARATION,
			NodeType.INTERFACE_DECLARATION, NodeType.ENUM_DECLARATION, NodeType.RECORD_DECLARATION))
		{
			for (NodeIndex node : positionIndex.findNodesByType(typeNodeType))
			{
				String typeName = extractTypeNameFromDeclaration(context, node, typeNodeType);
				if (typeName != null)
				{
					types.add(typeName);
				}
			}
		}

		return types;
	}

	/**
	 * Extracts the type name from a type declaration node.
	 * <p>
	 * Parses the declaration text to find the type name after the keyword.
	 *
	 * @param context  the transformation context
	 * @param node     the type declaration node
	 * @param nodeType the type of declaration (class, interface, enum, record)
	 * @return the type name, or {@code null} if not found
	 */
	private static String extractTypeNameFromDeclaration(TransformationContext context,
		NodeIndex node, NodeType nodeType)
	{
		String declarationText = context.getSourceText(node);

		String keyword = switch (nodeType)
		{
			case CLASS_DECLARATION -> "class";
			case INTERFACE_DECLARATION -> "interface";
			case ENUM_DECLARATION -> "enum";
			case RECORD_DECLARATION -> "record";
			default -> null;
		};

		if (keyword == null)
		{
			return null;
		}

		return extractTypeNameAfterKeyword(declarationText, keyword);
	}

	/**
	 * Extracts the type name following a specific keyword in declaration text.
	 *
	 * @param declarationText the full declaration text
	 * @param keyword         the keyword to search for (class, record, interface, enum)
	 * @return the type name, or {@code null} if keyword not found
	 */
	private static String extractTypeNameAfterKeyword(String declarationText, String keyword)
	{
		// Find the keyword followed by whitespace
		int keywordIndex = declarationText.indexOf(keyword + " ");
		if (keywordIndex < 0)
		{
			keywordIndex = declarationText.indexOf(keyword + "\t");
			if (keywordIndex < 0)
			{
				keywordIndex = declarationText.indexOf(keyword + "\n");
			}
		}

		if (keywordIndex < 0)
		{
			return null;
		}

		// Extract the type name (first identifier after the keyword)
		int nameStart = keywordIndex + keyword.length();
		// Skip whitespace
		while (nameStart < declarationText.length() &&
			Character.isWhitespace(declarationText.charAt(nameStart)))
		{
			++nameStart;
		}

		// Read the identifier
		int nameEnd = nameStart;
		while (nameEnd < declarationText.length() &&
			(Character.isJavaIdentifierPart(declarationText.charAt(nameEnd))))
		{
			++nameEnd;
		}

		if (nameEnd > nameStart)
		{
			return declarationText.substring(nameStart, nameEnd);
		}

		return null;
	}
}
