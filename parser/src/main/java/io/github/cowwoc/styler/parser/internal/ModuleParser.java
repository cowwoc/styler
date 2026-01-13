package io.github.cowwoc.styler.parser.internal;

import io.github.cowwoc.styler.ast.core.ExportsDirectiveAttribute;
import io.github.cowwoc.styler.ast.core.ModuleDeclarationAttribute;
import io.github.cowwoc.styler.ast.core.ModuleImportAttribute;
import io.github.cowwoc.styler.ast.core.NodeIndex;
import io.github.cowwoc.styler.ast.core.OpensDirectiveAttribute;
import io.github.cowwoc.styler.ast.core.ProvidesDirectiveAttribute;
import io.github.cowwoc.styler.ast.core.RequiresDirectiveAttribute;
import io.github.cowwoc.styler.ast.core.UsesDirectiveAttribute;
import io.github.cowwoc.styler.parser.Parser;
import io.github.cowwoc.styler.parser.Token;
import io.github.cowwoc.styler.parser.TokenType;

import java.util.ArrayList;
import java.util.List;

/**
 * Helper class for parsing module-info.java files (JPMS module declarations).
 * <p>
 * Extracted from {@link Parser} to reduce class size while maintaining cohesive parsing logic.
 */
public final class ModuleParser
{
	private final Parser parser;

	/**
	 * Creates a new module parser that delegates to the given parser.
	 *
	 * @param parser the parent parser providing token access and helper methods
	 */
	public ModuleParser(Parser parser)
	{
		this.parser = parser;
	}

	/**
	 * Checks if the current position starts a module declaration.
	 * <p>
	 * Uses index-based lookahead to detect "module" or "open module" keywords, skipping any leading
	 * annotations. This avoids modifying parser state or the arena during the lookahead check.
	 *
	 * @return {@code true} if a module declaration starts at the current position
	 */
	public boolean isModuleDeclarationStart()
	{
		List<Token> tokens = parser.getTokens();
		int lookahead = parser.getPosition();

		// Skip annotations (module declarations can have annotations like @Deprecated)
		while (lookahead < tokens.size() && tokens.get(lookahead).type() == TokenType.AT_SIGN)
			lookahead = skipAnnotationAt(lookahead, tokens);

		if (lookahead >= tokens.size())
			return false;

		TokenType current = tokens.get(lookahead).type();

		// Check for "open module" or just "module"
		if (current == TokenType.OPEN)
		{
			++lookahead;
			return lookahead < tokens.size() && tokens.get(lookahead).type() == TokenType.MODULE;
		}
		return current == TokenType.MODULE;
	}

	/**
	 * Skips over an annotation starting at the given position in the token list.
	 * <p>
	 * Handles both marker annotations ({@code @Override}) and annotations with arguments
	 * ({@code @SuppressWarnings("unchecked")}).
	 *
	 * @param startPosition the token index of the {@code @} symbol
	 * @param tokens        the token list to scan
	 * @return the token index immediately after the annotation
	 */
	private int skipAnnotationAt(int startPosition, List<Token> tokens)
	{
		int lookahead = startPosition + 1;
		if (lookahead >= tokens.size() || tokens.get(lookahead).type() != TokenType.IDENTIFIER)
			return lookahead;

		++lookahead;
		if (lookahead >= tokens.size() || tokens.get(lookahead).type() != TokenType.LEFT_PARENTHESIS)
			return lookahead;

		// Skip annotation arguments
		int parenDepth = 1;
		++lookahead;
		while (lookahead < tokens.size() && parenDepth > 0)
		{
			TokenType type = tokens.get(lookahead).type();
			if (type == TokenType.LEFT_PARENTHESIS)
				++parenDepth;
			else if (type == TokenType.RIGHT_PARENTHESIS)
				--parenDepth;
			++lookahead;
		}
		return lookahead;
	}

	/**
	 * Parses a module compilation unit (module-info.java).
	 * <p>
	 * Module compilation units contain only a module declaration and cannot have package declarations
	 * or type declarations. The module declaration serves as the root node.
	 *
	 * @return the module declaration node index (serves as root for module-info.java files)
	 * @throws Parser.ParserException if unexpected tokens appear after the module declaration
	 */
	public NodeIndex parseModuleCompilationUnit()
	{
		// Parse module declaration (includes all directives)
		NodeIndex moduleDecl = parseModuleDeclaration();

		// Module compilation units cannot contain package/type declarations
		// Verify we've reached EOF after the module declaration
		parser.parseComments();
		if (parser.currentToken().type() != TokenType.END_OF_FILE)
		{
			throw new Parser.ParserException(
				"Unexpected token after module declaration: " + parser.currentToken().type() +
				" (module-info.java can only contain module declaration)",
				parser.currentToken().start());
		}

		// Return the module declaration directly as the root node
		// The module declaration IS the compilation unit for module-info.java
		return moduleDecl;
	}

	/**
	 * Parses a module declaration.
	 * <p>
	 * Grammar:
	 * <pre>
	 * ModuleDeclaration:
	 *     {Annotation} [open] module Identifier {. Identifier} { {ModuleDirective} }
	 * </pre>
	 *
	 * @return the module declaration node index
	 * @throws Parser.ParserException if module syntax is invalid
	 */
	private NodeIndex parseModuleDeclaration()
	{
		int start = parser.currentToken().start();

		// Skip annotations (they were already identified during lookahead)
		while (parser.currentToken().type() == TokenType.AT_SIGN)
		{
			parser.parseAnnotation();
			parser.parseComments();
		}

		// Parse "open" modifier (optional)
		boolean isOpen = parser.match(TokenType.OPEN);

		// Parse "module" keyword
		parser.expect(TokenType.MODULE);

		// Parse module name (qualified name: com.example.app)
		int nameStart = parser.currentToken().start();
		parser.parseQualifiedName();
		int nameEnd = parser.previousToken().end();
		String moduleName = parser.getSourceCode().substring(nameStart, nameEnd);

		// Parse module body: { directives }
		parser.expect(TokenType.LEFT_BRACE);
		parser.parseComments();

		// Parse directives until closing brace
		while (parser.currentToken().type() != TokenType.RIGHT_BRACE)
		{
			parseModuleDirective();
			parser.parseComments();
		}

		parser.expect(TokenType.RIGHT_BRACE);

		// Create module declaration node with attribute
		ModuleDeclarationAttribute attribute = new ModuleDeclarationAttribute(moduleName, isOpen);
		return parser.getArena().allocateModuleDeclaration(start, parser.previousToken().end(), attribute);
	}

	/**
	 * Parses a module directive (requires/exports/opens/uses/provides).
	 * <p>
	 * Routes to the appropriate directive parser based on the current keyword.
	 *
	 * @return the directive node index
	 * @throws Parser.ParserException if an invalid directive keyword is encountered
	 */
	private NodeIndex parseModuleDirective()
	{
		TokenType type = parser.currentToken().type();
		return switch (type)
		{
			case REQUIRES -> parseRequiresDirective();
			case EXPORTS -> parseExportsDirective();
			case OPENS -> parseOpensDirective();
			case USES -> parseUsesDirective();
			case PROVIDES -> parseProvidesDirective();
			default -> throw new Parser.ParserException(
				"Expected module directive (requires/exports/opens/uses/provides), found: " + type,
				parser.currentToken().start());
		};
	}

	/**
	 * Parses a requires directive.
	 * <p>
	 * Grammar:
	 * <pre>
	 * RequiresDirective:
	 *     requires {RequiresModifier} ModuleName ;
	 * RequiresModifier:
	 *     transitive | static
	 * </pre>
	 *
	 * @return the requires directive node index
	 */
	private NodeIndex parseRequiresDirective()
	{
		int start = parser.currentToken().start();
		parser.expect(TokenType.REQUIRES);

		// Parse modifiers (transitive and/or static, order doesn't matter per JLS)
		boolean isTransitive = false;
		boolean isStatic = false;

		while (parser.currentToken().type() == TokenType.TRANSITIVE ||
			parser.currentToken().type() == TokenType.STATIC)
		{
			if (parser.match(TokenType.TRANSITIVE))
				isTransitive = true;
			else if (parser.match(TokenType.STATIC))
				isStatic = true;
		}

		// Parse module name
		int nameStart = parser.currentToken().start();
		parser.parseQualifiedName();
		int nameEnd = parser.previousToken().end();
		String moduleName = parser.getSourceCode().substring(nameStart, nameEnd);

		parser.expect(TokenType.SEMICOLON);

		RequiresDirectiveAttribute attribute = new RequiresDirectiveAttribute(moduleName, isTransitive, isStatic);
		return parser.getArena().allocateRequiresDirective(start, parser.previousToken().end(), attribute);
	}

	/**
	 * Parses an exports directive.
	 * <p>
	 * Grammar:
	 * <pre>
	 * ExportsDirective:
	 *     exports PackageName [to ModuleName {, ModuleName}] ;
	 * </pre>
	 *
	 * @return the exports directive node index
	 */
	private NodeIndex parseExportsDirective()
	{
		int start = parser.currentToken().start();
		parser.expect(TokenType.EXPORTS);

		// Parse package name
		int pkgStart = parser.currentToken().start();
		parser.parseQualifiedName();
		int pkgEnd = parser.previousToken().end();
		String packageName = parser.getSourceCode().substring(pkgStart, pkgEnd);

		// Parse optional "to" clause (qualified exports)
		List<String> targetModules = List.of();
		if (parser.match(TokenType.TO))
			targetModules = parseModuleNameList();

		parser.expect(TokenType.SEMICOLON);

		ExportsDirectiveAttribute attribute = new ExportsDirectiveAttribute(packageName, targetModules);
		return parser.getArena().allocateExportsDirective(start, parser.previousToken().end(), attribute);
	}

	/**
	 * Parses an opens directive.
	 * <p>
	 * Grammar:
	 * <pre>
	 * OpensDirective:
	 *     opens PackageName [to ModuleName {, ModuleName}] ;
	 * </pre>
	 *
	 * @return the opens directive node index
	 */
	private NodeIndex parseOpensDirective()
	{
		int start = parser.currentToken().start();
		parser.expect(TokenType.OPENS);

		// Parse package name
		int pkgStart = parser.currentToken().start();
		parser.parseQualifiedName();
		int pkgEnd = parser.previousToken().end();
		String packageName = parser.getSourceCode().substring(pkgStart, pkgEnd);

		// Parse optional "to" clause (qualified opens)
		List<String> targetModules = List.of();
		if (parser.match(TokenType.TO))
			targetModules = parseModuleNameList();

		parser.expect(TokenType.SEMICOLON);

		OpensDirectiveAttribute attribute = new OpensDirectiveAttribute(packageName, targetModules);
		return parser.getArena().allocateOpensDirective(start, parser.previousToken().end(), attribute);
	}

	/**
	 * Parses a uses directive.
	 * <p>
	 * Grammar:
	 * <pre>
	 * UsesDirective:
	 *     uses TypeName ;
	 * </pre>
	 *
	 * @return the uses directive node index
	 */
	private NodeIndex parseUsesDirective()
	{
		int start = parser.currentToken().start();
		parser.expect(TokenType.USES);

		// Parse service type name (fully qualified type name)
		int typeStart = parser.currentToken().start();
		parser.parseQualifiedName();
		int typeEnd = parser.previousToken().end();
		String serviceTypeName = parser.getSourceCode().substring(typeStart, typeEnd);

		parser.expect(TokenType.SEMICOLON);

		UsesDirectiveAttribute attribute = new UsesDirectiveAttribute(serviceTypeName);
		return parser.getArena().allocateUsesDirective(start, parser.previousToken().end(), attribute);
	}

	/**
	 * Parses a provides directive.
	 * <p>
	 * Grammar:
	 * <pre>
	 * ProvidesDirective:
	 *     provides TypeName with TypeName {, TypeName} ;
	 * </pre>
	 *
	 * @return the provides directive node index
	 */
	private NodeIndex parseProvidesDirective()
	{
		int start = parser.currentToken().start();
		parser.expect(TokenType.PROVIDES);

		// Parse service type name
		int serviceStart = parser.currentToken().start();
		parser.parseQualifiedName();
		int serviceEnd = parser.previousToken().end();
		String serviceTypeName = parser.getSourceCode().substring(serviceStart, serviceEnd);

		// Parse "with" implementations
		parser.expect(TokenType.WITH);
		List<String> implementations = parseTypeNameList();

		parser.expect(TokenType.SEMICOLON);

		ProvidesDirectiveAttribute attribute = new ProvidesDirectiveAttribute(serviceTypeName, implementations);
		return parser.getArena().allocateProvidesDirective(start, parser.previousToken().end(), attribute);
	}

	/**
	 * Parses a comma-separated list of module names.
	 * <p>
	 * Used for qualified exports/opens {@code to module1, module2} clauses.
	 *
	 * @return immutable list of module names (never empty)
	 */
	private List<String> parseModuleNameList()
	{
		List<String> modules = new ArrayList<>();

		// Parse first module name
		int nameStart = parser.currentToken().start();
		parser.parseQualifiedName();
		int nameEnd = parser.previousToken().end();
		modules.add(parser.getSourceCode().substring(nameStart, nameEnd));

		// Parse additional module names after commas
		while (parser.match(TokenType.COMMA))
		{
			nameStart = parser.currentToken().start();
			parser.parseQualifiedName();
			nameEnd = parser.previousToken().end();
			modules.add(parser.getSourceCode().substring(nameStart, nameEnd));
		}

		return List.copyOf(modules);
	}

	/**
	 * Parses a comma-separated list of type names.
	 * <p>
	 * Used for {@code provides Service with Impl1, Impl2} clauses.
	 *
	 * @return immutable list of type names (never empty)
	 */
	private List<String> parseTypeNameList()
	{
		List<String> typeNames = new ArrayList<>();

		// Parse first type name
		int nameStart = parser.currentToken().start();
		parser.parseQualifiedName();
		int nameEnd = parser.previousToken().end();
		typeNames.add(parser.getSourceCode().substring(nameStart, nameEnd));

		// Parse additional type names after commas
		while (parser.match(TokenType.COMMA))
		{
			nameStart = parser.currentToken().start();
			parser.parseQualifiedName();
			nameEnd = parser.previousToken().end();
			typeNames.add(parser.getSourceCode().substring(nameStart, nameEnd));
		}

		return List.copyOf(typeNames);
	}

	/**
	 * Parses a module import declaration (JEP 511).
	 * <p>
	 * Syntax: {@code import module <module-name>;}
	 *
	 * @param start the start position of the import keyword
	 * @return the node index of the created module import declaration
	 */
	public NodeIndex parseModuleImport(int start)
	{
		// Build the module name from tokens
		StringBuilder moduleName = new StringBuilder();
		parser.expect(TokenType.IDENTIFIER);
		moduleName.append(parser.previousToken().decodedText());

		while (parser.currentToken().type() == TokenType.DOT)
		{
			parser.consume(); // DOT
			moduleName.append('.');
			parser.expect(TokenType.IDENTIFIER);
			moduleName.append(parser.previousToken().decodedText());
		}

		parser.expect(TokenType.SEMICOLON);
		int end = parser.previousToken().end();
		ModuleImportAttribute attribute = new ModuleImportAttribute(moduleName.toString());
		return parser.getArena().allocateModuleImportDeclaration(start, end, attribute);
	}
}
