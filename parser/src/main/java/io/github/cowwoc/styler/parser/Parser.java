package io.github.cowwoc.styler.parser;

import io.github.cowwoc.styler.ast.core.NodeArena;
import io.github.cowwoc.styler.ast.core.NodeIndex;
import io.github.cowwoc.styler.ast.core.NodeType;
import io.github.cowwoc.styler.ast.core.SecurityConfig;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.function.Supplier;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;

/**
 * Recursive descent parser for Java source code.
 * Builds an Index-Overlay AST using NodeArena for memory efficiency.
 * Enforces maximum parsing depth for security.
 */
public final class Parser implements AutoCloseable
{
	/**
	 * Frequency of memory usage checks during parsing.
	 * Checked every 100 calls to amortize Runtime.getRuntime() overhead (~500ns) to ~5ns per call.
	 */
	private static final int MEMORY_CHECK_INTERVAL = 100;

	/**
	 * Frequency of timeout checks during token consumption.
	 * Checked every 100 token consumptions to amortize System.currentTimeMillis() overhead (~100ns).
	 */
	private static final int TIMEOUT_CHECK_INTERVAL = 100;

	private final List<Token> tokens;
	private final NodeArena arena;
	private final Instant parsingDeadline;
	private int position;
	private int depth;

	/**
	 * Counter for periodic memory checks in enterDepth().
	 * Reset to 0 every {@link #MEMORY_CHECK_INTERVAL} calls.
	 * Separate from tokenCheckCounter to allow independent tuning of check frequencies
	 * based on different performance characteristics (memory checks are more expensive).
	 */
	private int depthCheckCounter;

	/**
	 * Counter for periodic timeout checks in consume().
	 * Reset to 0 every {@link #TIMEOUT_CHECK_INTERVAL} calls.
	 * Separate from depthCheckCounter to allow independent tuning of check frequencies
	 * based on different calling patterns (token consumption vs recursion depth).
	 */
	private int tokenCheckCounter;

	/**
	 * Creates a parser by reading a file with UTF-8 encoding.
	 * SEC-002: Enforces UTF-8 encoding and validates against decoding errors.
	 *
	 * @param path the path to the source file
	 * @return a new parser for the file contents
	 * @throws IOException if file cannot be read
	 * @throws IllegalArgumentException if file exceeds size limits or contains encoding errors
	 */
	public static Parser fromPath(Path path) throws IOException
	{
		requireThat(path, "path").isNotNull();

		// SEC-002: Read with explicit UTF-8 encoding
		byte[] bytes = Files.readAllBytes(path);
		String source = new String(bytes, StandardCharsets.UTF_8);

		// SEC-002: Validate for replacement characters indicating encoding errors
		if (source.indexOf('\uFFFD') != -1)
		{
			throw new IllegalArgumentException(
				"Source file contains invalid UTF-8 sequences (replacement character detected): " + path);
		}

		return new Parser(source);
	}

	/**
	 * Creates a new parser for the specified source code.
	 * For security, prefer using {@link #fromPath(Path)} which enforces UTF-8 encoding.
	 *
	 * @param source the Java source code to parse
	 * @throws NullPointerException if {@code source} is null
	 * @throws IllegalArgumentException if {@code source} exceeds maximum size limit
	 */
	public Parser(String source)
	{
		requireThat(source, "source").isNotNull();

		// SEC-001: File size limit validation to prevent DoS attacks
		int sourceBytes = source.length() * 2; // Approximate UTF-16 byte count
		if (sourceBytes > SecurityConfig.MAX_SOURCE_SIZE_BYTES)
		{
			throw new IllegalArgumentException(
				"Source file too large: " + sourceBytes + " bytes exceeds maximum of " +
				SecurityConfig.MAX_SOURCE_SIZE_BYTES + " bytes");
		}

		Lexer lexer = new Lexer(source);
		this.tokens = lexer.tokenize();

		// SEC-007: Token count limit to prevent excessive memory consumption
		if (tokens.size() > SecurityConfig.MAX_TOKEN_COUNT)
		{
			throw new IllegalArgumentException(
				"Too many tokens: " + tokens.size() + " exceeds maximum of " + SecurityConfig.MAX_TOKEN_COUNT);
		}

		this.arena = new NodeArena();

		// SEC-006: Set parsing deadline for timeout enforcement
		this.parsingDeadline = Instant.now().plusMillis(SecurityConfig.PARSING_TIMEOUT_MS);
	}

	/**
	 * Returns the NodeArena used by this parser.
	 *
	 * @return the node arena
	 */
	public NodeArena getArena()
	{
		return arena;
	}

	/**
	 * Parses the source code and returns the root compilation unit node.
	 *
	 * @return the root node index
	 * @throws ParserException if parsing fails
	 */
	public NodeIndex parse()
	{
		return parseCompilationUnit();
	}

	private NodeIndex parseCompilationUnit()
	{
		int start = currentToken().start();

		// Package declaration
		NodeIndex packageDecl = NodeIndex.NULL;
		skipComments();
		if (match(TokenType.PACKAGE))
		{
			packageDecl = parsePackageDeclaration(start);
		}

		// Import declarations
		skipComments();
		while (match(TokenType.IMPORT))
		{
			parseImportDeclaration();
			skipComments();
		}

		// Type declarations (class, interface, enum)
		while (currentToken().type() != TokenType.EOF)
		{
			skipComments();
			if (isTypeDeclarationStart())
			{
				parseTypeDeclaration();
			}
			else if (currentToken().type() == TokenType.SEMICOLON)
			{
				consume(); // Empty statement at top level
			}
			else if (currentToken().type() == TokenType.EOF)
			{
				break;
			}
			else
			{
				throw new ParserException(
					"Unexpected token at top level: " + currentToken().type() +
					" (expected type declaration, import, or package)",
					currentToken().start());
			}
		}

		int end = tokens.get(tokens.size() - 1).end();
		return arena.allocateNode(NodeType.COMPILATION_UNIT, start, end, packageDecl.index());
	}

	private boolean isTypeDeclarationStart()
	{
		return switch (currentToken().type())
		{
			case CLASS, INTERFACE, ENUM, RECORD, SEALED, NON_SEALED, AT, PUBLIC, PRIVATE, PROTECTED, STATIC,
				FINAL, ABSTRACT, STRICTFP -> true;
			default -> false;
		};
	}

	private NodeIndex parsePackageDeclaration(int start)
	{
		NodeIndex name = parseQualifiedName();
		expect(TokenType.SEMICOLON);
		return arena.allocateNode(NodeType.PACKAGE_DECLARATION, start, tokens.get(position - 1).end(), name.index());
	}

	private void parseImportDeclaration()
	{
		match(TokenType.STATIC);

		// Parse qualified name, but handle wildcard imports
		expect(TokenType.IDENTIFIER);
		while (currentToken().type() == TokenType.DOT)
		{
			consume(); // DOT
			if (match(TokenType.STAR))
			{
				// Wildcard import: import java.util.*;
				expect(TokenType.SEMICOLON);
				return;
			}
			expect(TokenType.IDENTIFIER);
		}
		expect(TokenType.SEMICOLON);
	}

	private NodeIndex parseQualifiedName()
	{
		int start = currentToken().start();
		expect(TokenType.IDENTIFIER);
		while (match(TokenType.DOT))
		{
			if (currentToken().type() == TokenType.IDENTIFIER)
			{
				consume();
			}
			else
			{
				break;
			}
		}
		int end = tokens.get(position - 1).end();
		return arena.allocateNode(NodeType.QUALIFIED_NAME, start, end, 0);
	}

	private void parseTypeDeclaration()
	{
		// Modifiers (including sealed/non-sealed)
		while (isModifier(currentToken().type()) ||
			currentToken().type() == TokenType.SEALED ||
			currentToken().type() == TokenType.NON_SEALED)
		{
			consume();
		}

		if (match(TokenType.CLASS))
		{
			parseClassDeclaration();
		}
		else if (match(TokenType.INTERFACE))
		{
			parseInterfaceDeclaration();
		}
		else if (match(TokenType.ENUM))
		{
			parseEnumDeclaration();
		}
		else if (match(TokenType.RECORD))
		{
			parseRecordDeclaration();
		}
		else if (match(TokenType.AT))
		{
			expect(TokenType.INTERFACE);
			parseAnnotationDeclaration();
		}
	}

	private boolean isModifier(TokenType type)
	{
		return switch (type)
		{
			case PUBLIC, PRIVATE, PROTECTED, STATIC, FINAL, ABSTRACT, STRICTFP, SYNCHRONIZED, NATIVE,
				TRANSIENT, VOLATILE -> true;
			default -> false;
		};
	}

	private void parseClassDeclaration()
	{
		expect(TokenType.IDENTIFIER); // Class name

		// Type parameters
		if (match(TokenType.LT))
		{
			parseTypeParameters();
		}

		// Extends clause
		if (match(TokenType.EXTENDS))
		{
			parseType();
		}

		// Implements clause
		if (match(TokenType.IMPLEMENTS))
		{
			parseType();
			while (match(TokenType.COMMA))
			{
				parseType();
			}
		}

		// Permits clause (for sealed classes)
		if (match(TokenType.PERMITS))
		{
			parseType();
			while (match(TokenType.COMMA))
			{
				parseType();
			}
		}

		// Class body
		parseClassBody();
	}

	private void parseInterfaceDeclaration()
	{
		expect(TokenType.IDENTIFIER); // Interface name

		if (match(TokenType.LT))
		{
			parseTypeParameters();
		}

		if (match(TokenType.EXTENDS))
		{
			parseType();
			while (match(TokenType.COMMA))
			{
				parseType();
			}
		}

		// Permits clause (for sealed interfaces)
		if (match(TokenType.PERMITS))
		{
			parseType();
			while (match(TokenType.COMMA))
			{
				parseType();
			}
		}

		parseClassBody();
	}

	private void parseEnumDeclaration()
	{
		expect(TokenType.IDENTIFIER); // Enum name

		if (match(TokenType.IMPLEMENTS))
		{
			parseType();
			while (match(TokenType.COMMA))
			{
				parseType();
			}
		}

		expect(TokenType.LBRACE);
		parseEnumBody();
		expect(TokenType.RBRACE);
	}

	private void parseAnnotationDeclaration()
	{
		expect(TokenType.IDENTIFIER); // Annotation name
		parseClassBody();
	}

	private void parseRecordDeclaration()
	{
		expect(TokenType.IDENTIFIER); // Record name

		// Type parameters (optional)
		if (match(TokenType.LT))
		{
			parseTypeParameters();
		}

		// Record components (mandatory)
		expect(TokenType.LPAREN);
		if (currentToken().type() != TokenType.RPAREN)
		{
			parseParameter();
			while (match(TokenType.COMMA))
			{
				parseParameter();
			}
		}
		expect(TokenType.RPAREN);

		// Implements clause (optional)
		if (match(TokenType.IMPLEMENTS))
		{
			parseType();
			while (match(TokenType.COMMA))
			{
				parseType();
			}
		}

		// Record body (optional - can be empty)
		parseClassBody();
	}

	private void parseTypeParameters()
	{
		parseTypeParameter();
		while (match(TokenType.COMMA))
		{
			parseTypeParameter();
		}
		expect(TokenType.GT);
	}

	private void parseTypeParameter()
	{
		expect(TokenType.IDENTIFIER);
		if (match(TokenType.EXTENDS))
		{
			parseType();
			while (match(TokenType.BITAND))
			{
				parseType();
			}
		}
	}

	private void parseType()
	{
		if (isPrimitiveType(currentToken().type()))
		{
			consume();
		}
		else if (currentToken().type() == TokenType.VAR)
		{
			// Type inference with 'var' keyword (JDK 10+)
			consume();
		}
		else
		{
			parseQualifiedName();
			if (match(TokenType.LT))
			{
				parseTypeArguments();
			}
		}

		while (match(TokenType.LBRACKET))
		{
			expect(TokenType.RBRACKET);
		}
	}

	private boolean isPrimitiveType(TokenType type)
	{
		return switch (type)
		{
			case BOOLEAN, BYTE, SHORT, INT, LONG, CHAR, FLOAT, DOUBLE, VOID -> true;
			default -> false;
		};
	}

	private void parseTypeArguments()
	{
		parseTypeArgument();
		while (match(TokenType.COMMA))
		{
			parseTypeArgument();
		}
		expect(TokenType.GT);
	}

	private void parseTypeArgument()
	{
		if (match(TokenType.QUESTION))
		{
			if (match(TokenType.EXTENDS) || match(TokenType.SUPER))
			{
				parseType();
			}
		}
		else
		{
			parseType();
		}
	}

	private void parseClassBody()
	{
		expect(TokenType.LBRACE);
		while (!match(TokenType.RBRACE))
		{
			skipComments();
			if (currentToken().type() == TokenType.RBRACE)
			{
				break;
			}
			if (currentToken().type() == TokenType.EOF)
			{
				throw new ParserException("Unexpected EOF in class body", currentToken().start());
			}
			parseMemberDeclaration();
		}
	}

	private void parseEnumBody()
	{
		if (currentToken().type() != TokenType.SEMICOLON && currentToken().type() != TokenType.RBRACE)
		{
			parseEnumConstant();
			while (match(TokenType.COMMA))
			{
				if (currentToken().type() == TokenType.SEMICOLON || currentToken().type() == TokenType.RBRACE)
				{
					break;
				}
				parseEnumConstant();
			}
		}

		if (match(TokenType.SEMICOLON))
		{
			while (currentToken().type() != TokenType.RBRACE)
			{
				parseMemberDeclaration();
			}
		}
	}

	private void parseEnumConstant()
	{
		expect(TokenType.IDENTIFIER);
		if (match(TokenType.LPAREN) && !match(TokenType.RPAREN))
		{
			parseExpression();
			while (match(TokenType.COMMA))
			{
				parseExpression();
			}
			expect(TokenType.RPAREN);
		}
		if (match(TokenType.LBRACE))
		{
			while (!match(TokenType.RBRACE))
			{
				parseMemberDeclaration();
			}
		}
	}

	private void parseMemberDeclaration()
	{
		int start = currentToken().start();
		skipMemberModifiers();

		if (parseNestedTypeDeclaration())
		{
			return;
		}

		// Type parameters (for methods)
		if (match(TokenType.LT))
		{
			parseTypeParameters();
		}

		parseMemberBody(start);
	}

	private void skipMemberModifiers()
	{
		while (isModifier(currentToken().type()) ||
			currentToken().type() == TokenType.AT ||
			currentToken().type() == TokenType.SEALED ||
			currentToken().type() == TokenType.NON_SEALED)
		{
			if (currentToken().type() == TokenType.AT)
			{
				parseAnnotation();
			}
			else
			{
				consume();
			}
		}
	}

	private boolean parseNestedTypeDeclaration()
	{
		if (currentToken().type() == TokenType.CLASS)
		{
			consume();
			parseClassDeclaration();
			return true;
		}
		if (currentToken().type() == TokenType.INTERFACE)
		{
			consume();
			parseInterfaceDeclaration();
			return true;
		}
		if (currentToken().type() == TokenType.ENUM)
		{
			consume();
			parseEnumDeclaration();
			return true;
		}
		if (currentToken().type() == TokenType.RECORD)
		{
			consume();
			parseRecordDeclaration();
			return true;
		}
		return false;
	}

	private void parseMemberBody(int start)
	{
		if (currentToken().type() == TokenType.IDENTIFIER)
		{
			parseIdentifierMember();
		}
		else if (isPrimitiveType(currentToken().type()) || currentToken().type() == TokenType.VOID)
		{
			parsePrimitiveTypedMember();
		}
		else if (match(TokenType.LBRACE))
		{
			// Instance or static initializer
			parseBlock();
		}
		else if (match(TokenType.SEMICOLON))
		{
			// Empty declaration
		}
		else
		{
			throw new ParserException("Unexpected token in member declaration: " + currentToken().type(), start);
		}
	}

	private void parseIdentifierMember()
	{
		int checkpoint = position;
		consume(); // Consume first identifier (could be type, constructor name, or field name)

		if (match(TokenType.LPAREN))
		{
			// Constructor (no return type, identifier is constructor name)
			parseMethodRest();
			return;
		}

		if (currentToken().type() == TokenType.LBRACE)
		{
			// Compact constructor (Java 16+): record component validation without parameter list
			// Example: public record Point(int x, int y) { public Point { validateInputs(); } }
			parseBlock();
			return;
		}

		if (currentToken().type() == TokenType.IDENTIFIER)
		{
			// Method with non-primitive return type: ReturnType methodName(...)
			// First identifier was return type, now consume method name
			consume();
			if (match(TokenType.LPAREN))
			{
				parseMethodRest();
				return;
			}

			// Backtrack - this is a field declaration: Type fieldName = ...
			// Need to restore position and consume both type and field name
			position = checkpoint;
			consume(); // Re-consume type (e.g., "NodeIndex")
			consume(); // Consume field name (e.g., "NULL")
			parseFieldRest();
			return;
		}

		// Field with identifier type
		parseFieldRest();
	}

	private void parsePrimitiveTypedMember()
	{
		consume();
		if (match(TokenType.LBRACKET))
		{
			expect(TokenType.RBRACKET);
		}
		expect(TokenType.IDENTIFIER);
		if (match(TokenType.LPAREN))
		{
			parseMethodRest();
		}
		else
		{
			parseFieldRest();
		}
	}

	private void parseAnnotation()
	{
		expect(TokenType.AT);
		parseQualifiedName();
		if (match(TokenType.LPAREN) && !match(TokenType.RPAREN))
		{
			parseExpression();
			while (match(TokenType.COMMA))
			{
				parseExpression();
			}
			expect(TokenType.RPAREN);
		}
	}

	private void parseMethodRest()
	{
		// Parameters already consumed opening paren
		if (!match(TokenType.RPAREN))
		{
			parseParameter();
			while (match(TokenType.COMMA))
			{
				parseParameter();
			}
			expect(TokenType.RPAREN);
		}

		// Throws clause
		if (match(TokenType.THROWS))
		{
			parseQualifiedName();
			while (match(TokenType.COMMA))
			{
				parseQualifiedName();
			}
		}

		// Method body or semicolon
		if (match(TokenType.SEMICOLON))
		{
			// Abstract method
		}
		else
		{
			parseBlock();
		}
	}

	private void parseParameter()
	{
		// Modifiers
		while (currentToken().type() == TokenType.FINAL || currentToken().type() == TokenType.AT)
		{
			if (currentToken().type() == TokenType.AT)
			{
				parseAnnotation();
			}
			else
			{
				consume();
			}
		}

		parseType();
		if (match(TokenType.ELLIPSIS))
		{
			// Varargs
		}
		expect(TokenType.IDENTIFIER);
	}

	private void parseFieldRest()
	{
		// Array dimensions or initializer
		while (match(TokenType.LBRACKET))
		{
			expect(TokenType.RBRACKET);
		}

		if (match(TokenType.ASSIGN))
		{
			parseExpression();
		}

		while (match(TokenType.COMMA))
		{
			expect(TokenType.IDENTIFIER);
			while (match(TokenType.LBRACKET))
			{
				expect(TokenType.RBRACKET);
			}
			if (match(TokenType.ASSIGN))
			{
				parseExpression();
			}
		}

		expect(TokenType.SEMICOLON);
	}

	private void parseBlock()
	{
		expect(TokenType.LBRACE);
		while (!match(TokenType.RBRACE))
		{
			if (currentToken().type() == TokenType.EOF)
			{
				throw new ParserException("Unexpected EOF in block", currentToken().start());
			}
			parseStatement();
		}
	}

	private void parseStatement()
	{
		TokenType type = currentToken().type();

		if (type == TokenType.IF)
		{
			parseIfStatement();
		}
		else if (type == TokenType.FOR)
		{
			parseForStatement();
		}
		else if (type == TokenType.WHILE)
		{
			parseWhileStatement();
		}
		else if (type == TokenType.DO)
		{
			parseDoWhileStatement();
		}
		else if (type == TokenType.SWITCH)
		{
			parseSwitchStatement();
		}
		else if (type == TokenType.RETURN)
		{
			parseReturnStatement();
		}
		else if (type == TokenType.THROW)
		{
			parseThrowStatement();
		}
		else if (type == TokenType.TRY)
		{
			parseTryStatement();
		}
		else if (type == TokenType.SYNCHRONIZED)
		{
			parseSynchronizedStatement();
		}
		else if (type == TokenType.BREAK)
		{
			consume();
			if (currentToken().type() == TokenType.IDENTIFIER)
			{
				consume();
			}
			expect(TokenType.SEMICOLON);
		}
		else if (type == TokenType.CONTINUE)
		{
			consume();
			if (currentToken().type() == TokenType.IDENTIFIER)
			{
				consume();
			}
			expect(TokenType.SEMICOLON);
		}
		else if (type == TokenType.ASSERT)
		{
			parseAssertStatement();
		}
		else if (type == TokenType.SEMICOLON)
		{
			consume(); // Empty statement
		}
		else if (type == TokenType.LBRACE)
		{
			parseBlock();
		}
		else
		{
			// Expression statement or variable declaration
			parseExpressionOrVariableStatement();
		}
	}

	private void parseIfStatement()
	{
		expect(TokenType.IF);
		expect(TokenType.LPAREN);
		parseExpression();
		expect(TokenType.RPAREN);
		parseStatement();
		if (match(TokenType.ELSE))
		{
			parseStatement();
		}
	}

	/**
	 * Attempts to parse an enhanced for-loop header (type identifier : expression).
	 *
	 * @return {@code true} if enhanced for-loop header was successfully parsed
	 */
	private boolean tryParseEnhancedForHeader()
	{
		try
		{
			if (!looksLikeTypeStart())
				return false;
			parseType();
			if (currentToken().type() != TokenType.IDENTIFIER)
				return false;
			consume();
			return match(TokenType.COLON);
		}
		catch (ParserException e)
		{
			// Not enhanced for - parsing failed
			return false;
		}
	}

	/**
	 * Checks if the current token could be the start of a type declaration.
	 *
	 * @return {@code true} if current token is FINAL, a primitive type, or an identifier
	 */
	private boolean looksLikeTypeStart()
	{
		TokenType type = currentToken().type();
		return type == TokenType.FINAL || isPrimitiveType(type) || type == TokenType.IDENTIFIER;
	}

	private void parseForStatement()
	{
		expect(TokenType.FOR);
		expect(TokenType.LPAREN);

		// Enhanced for or regular for
		int checkpoint = position;
		boolean isEnhanced = tryParseEnhancedForHeader();

		if (isEnhanced)
		{
			parseExpression();
			expect(TokenType.RPAREN);
			parseStatement();
		}
		else
		{
			position = checkpoint;
			// Regular for
			if (!match(TokenType.SEMICOLON))
			{
				parseExpressionOrVariableStatement();
			}
			if (!match(TokenType.SEMICOLON))
			{
				parseExpression();
				expect(TokenType.SEMICOLON);
			}
			if (currentToken().type() != TokenType.RPAREN)
			{
				parseExpression();
				while (match(TokenType.COMMA))
				{
					parseExpression();
				}
			}
			expect(TokenType.RPAREN);
			parseStatement();
		}
	}

	private void parseWhileStatement()
	{
		expect(TokenType.WHILE);
		expect(TokenType.LPAREN);
		parseExpression();
		expect(TokenType.RPAREN);
		parseStatement();
	}

	private void parseDoWhileStatement()
	{
		expect(TokenType.DO);
		parseStatement();
		expect(TokenType.WHILE);
		expect(TokenType.LPAREN);
		parseExpression();
		expect(TokenType.RPAREN);
		expect(TokenType.SEMICOLON);
	}

	private void parseSwitchStatement()
	{
		expect(TokenType.SWITCH);
		expect(TokenType.LPAREN);
		parseExpression();
		expect(TokenType.RPAREN);
		expect(TokenType.LBRACE);
		while (currentToken().type() == TokenType.CASE || currentToken().type() == TokenType.DEFAULT)
		{
			if (match(TokenType.CASE))
			{
				parseExpression();
			}
			else
			{
				consume(); // DEFAULT
			}
			expect(TokenType.COLON);
			while (currentToken().type() != TokenType.CASE &&
				currentToken().type() != TokenType.DEFAULT &&
				currentToken().type() != TokenType.RBRACE)
			{
				parseStatement();
			}
		}
		expect(TokenType.RBRACE);
	}

	private void parseReturnStatement()
	{
		expect(TokenType.RETURN);
		if (currentToken().type() != TokenType.SEMICOLON)
		{
			parseExpression();
		}
		expect(TokenType.SEMICOLON);
	}

	private void parseThrowStatement()
	{
		expect(TokenType.THROW);
		parseExpression();
		expect(TokenType.SEMICOLON);
	}

	private void parseTryStatement()
	{
		expect(TokenType.TRY);

		// Try-with-resources
		if (match(TokenType.LPAREN))
		{
			parseResource();
			while (match(TokenType.SEMICOLON))
			{
				if (currentToken().type() != TokenType.RPAREN)
				{
					parseResource();
				}
			}
			expect(TokenType.RPAREN);
		}

		parseBlock();

		// Catch clauses
		while (match(TokenType.CATCH))
		{
			expect(TokenType.LPAREN);
			parseParameter();
			expect(TokenType.RPAREN);
			parseBlock();
		}

		// Finally clause
		if (match(TokenType.FINALLY))
		{
			parseBlock();
		}
	}

	private void parseResource()
	{
		if (currentToken().type() == TokenType.FINAL)
		{
			consume();
		}
		parseType();
		expect(TokenType.IDENTIFIER);
		expect(TokenType.ASSIGN);
		parseExpression();
	}

	private void parseSynchronizedStatement()
	{
		expect(TokenType.SYNCHRONIZED);
		expect(TokenType.LPAREN);
		parseExpression();
		expect(TokenType.RPAREN);
		parseBlock();
	}

	private void parseAssertStatement()
	{
		expect(TokenType.ASSERT);
		parseExpression();
		if (match(TokenType.COLON))
		{
			parseExpression();
		}
		expect(TokenType.SEMICOLON);
	}

	/**
	 * Attempts to parse a variable declaration. Backtracks to checkpoint on failure.
	 *
	 * @param checkpoint the position to backtrack to on failure
	 * @return {@code true} if successfully parsed a variable declaration
	 */
	private boolean tryParseVariableDeclaration(int checkpoint)
	{
		try
		{
			parseType();
			if (currentToken().type() != TokenType.IDENTIFIER)
			{
				position = checkpoint;
				return false;
			}
			consume();
			parseOptionalArrayBrackets();
			if (match(TokenType.ASSIGN))
				parseExpression();
			parseAdditionalDeclarators();
			expect(TokenType.SEMICOLON);
			return true;
		}
		catch (ParserException e)
		{
			position = checkpoint;
			return false;
		}
	}

	/**
	 * Parses optional array brackets after a variable name.
	 */
	private void parseOptionalArrayBrackets()
	{
		while (match(TokenType.LBRACKET))
			expect(TokenType.RBRACKET);
	}

	/**
	 * Parses additional variable declarators after the first one (comma-separated).
	 */
	private void parseAdditionalDeclarators()
	{
		while (match(TokenType.COMMA))
		{
			expect(TokenType.IDENTIFIER);
			parseOptionalArrayBrackets();
			if (match(TokenType.ASSIGN))
				parseExpression();
		}
	}

	private void parseExpressionOrVariableStatement()
	{
		int checkpoint = position;

		// Try to parse as variable declaration
		if ((currentToken().type() == TokenType.FINAL ||
			currentToken().type() == TokenType.VAR ||
			isPrimitiveType(currentToken().type()) ||
			currentToken().type() == TokenType.IDENTIFIER) &&
			tryParseVariableDeclaration(checkpoint))
		{
			return;
		}

		// Parse as expression statement
		parseExpression();
		expect(TokenType.SEMICOLON);
	}

	// Expression parsing with operator precedence

	private NodeIndex parseExpression()
	{
		return parseAssignment();
	}

	private NodeIndex parseAssignment()
	{
		NodeIndex left = parseTernary();

		if (isAssignmentOperator(currentToken().type()))
		{
			consume();
			NodeIndex right = parseAssignment(); // Right associative

			int start = arena.getStart(left);
			int end = arena.getEnd(right);
			return arena.allocateNode(NodeType.ASSIGNMENT_EXPRESSION, start, end, left.index());
		}

		return left;
	}

	private boolean isAssignmentOperator(TokenType type)
	{
		return switch (type)
		{
			case ASSIGN, PLUSASSIGN, MINUSASSIGN, STARASSIGN, DIVASSIGN, MODASSIGN, BITANDASSIGN, BITORASSIGN,
				CARETASSIGN, LSHIFTASSIGN, RSHIFTASSIGN, URSHIFTASSIGN -> true;
			default -> false;
		};
	}

	private NodeIndex parseTernary()
	{
		NodeIndex condition = parseLogicalOr();

		if (match(TokenType.QUESTION))
		{
			parseExpression();
			expect(TokenType.COLON);
			NodeIndex elseExpr = parseTernary(); // Right associative

			int start = arena.getStart(condition);
			int end = arena.getEnd(elseExpr);
			return arena.allocateNode(NodeType.CONDITIONAL_EXPRESSION, start, end, condition.index());
		}

		return condition;
	}

	/**
	 * Generic helper method for parsing binary expressions with operator precedence.
	 * Eliminates duplication across all binary operator parsing methods.
	 *
	 * @param nextLevel supplier for next precedence level parser
	 * @param operators variable number of operator token types to match
	 * @return the parsed binary expression node or next level node if no operators match
	 */
	private NodeIndex parseBinaryExpression(Supplier<NodeIndex> nextLevel, TokenType... operators)
	{
		NodeIndex left = nextLevel.get();

		while (matchesAny(operators))
		{
			NodeIndex right = nextLevel.get();
			int start = arena.getStart(left);
			int end = arena.getEnd(right);
			left = arena.allocateNode(NodeType.BINARY_EXPRESSION, start, end, left.index());
		}

		return left;
	}

	/**
	 * Checks if current token matches any of the provided token types and consumes if found.
	 *
	 * @param types variable number of token types to check
	 * @return true if current token matches any type (and was consumed), false otherwise
	 */
	private boolean matchesAny(TokenType... types)
	{
		TokenType current = currentToken().type();
		for (TokenType type : types)
		{
			if (current == type)
			{
				consume();
				return true;
			}
		}
		return false;
	}

	private NodeIndex parseLogicalOr()
	{
		return parseBinaryExpression(this::parseLogicalAnd, TokenType.OR);
	}

	private NodeIndex parseLogicalAnd()
	{
		return parseBinaryExpression(this::parseBitwiseOr, TokenType.AND);
	}

	private NodeIndex parseBitwiseOr()
	{
		return parseBinaryExpression(this::parseBitwiseXor, TokenType.BITOR);
	}

	private NodeIndex parseBitwiseXor()
	{
		return parseBinaryExpression(this::parseBitwiseAnd, TokenType.CARET);
	}

	private NodeIndex parseBitwiseAnd()
	{
		return parseBinaryExpression(this::parseEquality, TokenType.BITAND);
	}

	private NodeIndex parseEquality()
	{
		return parseBinaryExpression(this::parseRelational, TokenType.EQ, TokenType.NE);
	}

	private NodeIndex parseRelational()
	{
		return parseBinaryExpression(this::parseShift,
			TokenType.LT, TokenType.GT, TokenType.LE, TokenType.GE, TokenType.INSTANCEOF);
	}

	private NodeIndex parseShift()
	{
		return parseBinaryExpression(this::parseAdditive,
			TokenType.LSHIFT, TokenType.RSHIFT, TokenType.URSHIFT);
	}

	private NodeIndex parseAdditive()
	{
		return parseBinaryExpression(this::parseMultiplicative, TokenType.PLUS, TokenType.MINUS);
	}

	private NodeIndex parseMultiplicative()
	{
		return parseBinaryExpression(this::parseUnary, TokenType.STAR, TokenType.DIV, TokenType.MOD);
	}

	private NodeIndex parseUnary()
	{
		int start = currentToken().start();

		TokenType type = currentToken().type();
		boolean isUnaryOperator = switch (type)
		{
			case MINUS, PLUS, NOT, TILDE, INC, DEC -> true;
			default -> false;
		};

		if (isUnaryOperator)
		{
			consume();
			enterDepth();
			NodeIndex operand = parseUnary();
			exitDepth();
			int end = arena.getEnd(operand);
			return arena.allocateNode(NodeType.UNARY_EXPRESSION, start, end, operand.index());
		}

		return parsePostfix();
	}

	private NodeIndex parsePostfix()
	{
		NodeIndex left = parsePrimary();

		while (true)
		{
			int start = arena.getStart(left);

			if (match(TokenType.LPAREN))
			{
				// Method call
				while (!match(TokenType.RPAREN))
				{
					parseExpression();
					if (!match(TokenType.COMMA))
					{
						expect(TokenType.RPAREN);
						break;
					}
				}
				int end = tokens.get(position - 1).end();
				left = arena.allocateNode(NodeType.METHOD_INVOCATION, start, end, left.index());
			}
			else if (match(TokenType.DOT))
			{
				// Field access or method reference
				if (currentToken().type() == TokenType.IDENTIFIER)
				{
					int end = currentToken().end();
					consume();
					left = arena.allocateNode(NodeType.FIELD_ACCESS, start, end, left.index());
				}
				else
				{
					throw new ParserException(
						"Expected identifier after '.' but found " + currentToken().type(),
						currentToken().start());
				}
			}
			else if (match(TokenType.LBRACKET))
			{
				// Array access
				parseExpression();
				expect(TokenType.RBRACKET);
				int end = tokens.get(position - 1).end();
				left = arena.allocateNode(NodeType.ARRAY_ACCESS, start, end, left.index());
			}
			else if (currentToken().type() == TokenType.INC || currentToken().type() == TokenType.DEC)
			{
				// Postfix increment/decrement
				int end = currentToken().end();
				consume();
				left = arena.allocateNode(NodeType.POSTFIX_EXPRESSION, start, end, left.index());
			}
			else
			{
				break;
			}
		}

		return left;
	}

	private NodeIndex parsePrimary()
	{
		enterDepth();

		try
		{
			Token token = currentToken();
			int start = token.start();
			int end = token.end();

			if (token.isLiteral())
			{
				consume();
				NodeType nodeType = switch (token.type())
				{
					case INTEGER_LITERAL -> NodeType.INTEGER_LITERAL;
					case LONG_LITERAL -> NodeType.LONG_LITERAL;
					case FLOAT_LITERAL -> NodeType.FLOAT_LITERAL;
					case DOUBLE_LITERAL -> NodeType.DOUBLE_LITERAL;
					case BOOLEAN_LITERAL -> NodeType.BOOLEAN_LITERAL;
					case CHAR_LITERAL -> NodeType.CHAR_LITERAL;
					case STRING_LITERAL -> NodeType.STRING_LITERAL;
					case NULL_LITERAL -> NodeType.NULL_LITERAL;
					default -> throw new ParserException("Unexpected literal type: " + token.type(), start);
				};
				return arena.allocateNode(nodeType, start, end, 0);
			}

			if (token.type() == TokenType.IDENTIFIER)
			{
				consume();
				return arena.allocateNode(NodeType.IDENTIFIER, start, end, 0);
			}

			if (match(TokenType.LPAREN))
			{
				NodeIndex expr = parseExpression();
				expect(TokenType.RPAREN);
				return expr;
			}

			if (match(TokenType.NEW))
			{
				return parseNewExpression(start);
			}

			if (token.type() == TokenType.ARROW)
			{
				// Lambda with inferred parameter (handled by caller)
				// For now, just consume and create a placeholder
				consume();
				NodeIndex body = parseExpression();
				return arena.allocateNode(NodeType.LAMBDA_EXPRESSION, start, arena.getEnd(body), body.index());
			}

			throw new ParserException(
				"Unexpected token in expression: " + token.type() + " at position " + start,
				start);
		}
		finally
		{
			exitDepth();
		}
	}

	private NodeIndex parseNewExpression(int start)
	{
		// Object creation: new Type() or new Type[]
		parseType();

		if (match(TokenType.LBRACKET))
		{
			return parseArrayCreation(start);
		}
		if (match(TokenType.LPAREN))
		{
			return parseObjectCreation(start);
		}
		throw new ParserException(
			"Expected '(' or '[' after 'new' but found " + currentToken().type(),
			currentToken().start());
	}

	private NodeIndex parseArrayCreation(int start)
	{
		// Array creation with dimensions
		if (currentToken().type() != TokenType.RBRACKET)
		{
			parseExpression();
		}
		expect(TokenType.RBRACKET);

		while (match(TokenType.LBRACKET))
		{
			if (currentToken().type() != TokenType.RBRACKET)
			{
				parseExpression();
			}
			expect(TokenType.RBRACKET);
		}

		// Array initializer
		if (match(TokenType.LBRACE) && !match(TokenType.RBRACE))
		{
			parseExpression();
			while (match(TokenType.COMMA))
			{
				if (currentToken().type() == TokenType.RBRACE)
				{
					break;
				}
				parseExpression();
			}
			expect(TokenType.RBRACE);
		}

		int arrayEnd = tokens.get(position - 1).end();
		return arena.allocateNode(NodeType.ARRAY_CREATION, start, arrayEnd, 0);
	}

	private NodeIndex parseObjectCreation(int start)
	{
		// Constructor call arguments
		if (!match(TokenType.RPAREN))
		{
			parseExpression();
			while (match(TokenType.COMMA))
			{
				parseExpression();
			}
			expect(TokenType.RPAREN);
		}

		// Anonymous class body
		if (match(TokenType.LBRACE))
		{
			while (currentToken().type() != TokenType.RBRACE && currentToken().type() != TokenType.EOF)
			{
				parseMemberDeclaration();
			}
			expect(TokenType.RBRACE);
		}

		int objEnd = tokens.get(position - 1).end();
		return arena.allocateNode(NodeType.OBJECT_CREATION, start, objEnd, 0);
	}

	// Token navigation

	private void skipComments()
	{
		while (currentToken().type() == TokenType.JAVADOC_COMMENT ||
			currentToken().type() == TokenType.BLOCK_COMMENT ||
			currentToken().type() == TokenType.LINE_COMMENT)
		{
			consume();
		}
	}

	private Token currentToken()
	{
		if (position < tokens.size())
		{
			return tokens.get(position);
		}
		return tokens.get(tokens.size() - 1);
	}

	private Token consume()
	{
		// SEC-006: Periodic timeout checking to detect hung parsers
		++tokenCheckCounter;
		if (tokenCheckCounter >= TIMEOUT_CHECK_INTERVAL)
		{
			tokenCheckCounter = 0;
			if (Instant.now().isAfter(parsingDeadline))
			{
				throw new ParserException(
					"Parsing timeout exceeded (" + SecurityConfig.PARSING_TIMEOUT_MS + "ms) at position " +
					currentToken().start(),
					currentToken().start());
			}
		}

		Token current = currentToken();
		if (position < tokens.size() - 1)
		{
			position += 1;
		}
		return current;
	}

	private boolean match(TokenType type)
	{
		if (currentToken().type() == type)
		{
			consume();
			return true;
		}
		return false;
	}

	private void expect(TokenType type)
	{
		if (currentToken().type() != type)
		{
			throw new ParserException(
				"Expected " + type + " but found " + currentToken().type() +
				" at position " + currentToken().start(),
				currentToken().start());
		}
		consume();
	}

	// Depth checking

	/**
	 * Enters a new recursion depth level with 3-tier security monitoring.
	 * <p>
	 * <strong>Security Strategy:</strong> Multi-layered defense combining timeout detection,
	 * memory monitoring, and stack depth limiting to prevent resource exhaustion attacks.
	 * <p>
	 * <strong>Tier 1 - Timeout Protection (SEC-006):</strong> Checks parsing deadline on EVERY
	 * call to detect hung parsers. Overhead: ~100ns per call (System.currentTimeMillis()).
	 * <p>
	 * <strong>Tier 2 - Memory Protection (SEC-005):</strong> Checks heap usage every 100 calls
	 * to detect memory exhaustion. Periodic checking amortizes ~500ns cost to ~5ns per call.
	 * <p>
	 * <strong>Tier 3 - Stack Protection:</strong> Checks recursion depth on EVERY call to
	 * prevent stack overflow. Overhead: negligible (integer comparison).
	 * <p>
	 * <strong>Performance Tradeoff:</strong> Memory checks occur every 100 calls rather than
	 * every call to balance security (detect runaway memory within ~100 operations) against
	 * performance (avoid expensive Runtime.getRuntime() calls in tight loops).
	 * <p>
	 * <strong>Call Frequency:</strong> This method is called from parseUnary() and parsePrimary()
	 * during expression parsing, typically 10-1000 times per file depending on expression complexity.
	 *
	 * @throws ParserException if timeout exceeded, memory limit reached, or max depth exceeded
	 */
	private void enterDepth()
	{
		// SEC-006: Check parsing timeout
		if (Instant.now().isAfter(parsingDeadline))
		{
			throw new ParserException(
				"Parsing timeout exceeded (" + SecurityConfig.PARSING_TIMEOUT_MS + "ms) at position " +
				currentToken().start(),
				currentToken().start());
		}

		// SEC-005: Check memory usage periodically to reduce overhead
		++depthCheckCounter;
		if (depthCheckCounter >= MEMORY_CHECK_INTERVAL)
		{
			depthCheckCounter = 0;
			Runtime runtime = Runtime.getRuntime();
			long usedMemory = runtime.totalMemory() - runtime.freeMemory();
			if (usedMemory > SecurityConfig.MAX_HEAP_USAGE_BYTES)
			{
				throw new ParserException(
					"Memory limit exceeded: " + usedMemory + " bytes exceeds maximum of " +
					SecurityConfig.MAX_HEAP_USAGE_BYTES + " bytes at position " + currentToken().start(),
					currentToken().start());
			}
		}

		++depth;
		if (depth > SecurityConfig.MAX_PARSE_DEPTH)
		{
			throw new ParserException(
				"Maximum parsing depth exceeded (" + SecurityConfig.MAX_PARSE_DEPTH + ") at position " +
				currentToken().start(),
				currentToken().start());
		}
	}

	private void exitDepth()
	{
		--depth;
	}

	@Override
	public void close()
	{
		arena.close();
	}

	/**
	 * Exception thrown when parsing fails.
	 */
	public static final class ParserException extends RuntimeException
	{
		private static final long serialVersionUID = 1L;
		private final int position;

		/**
		 * Creates a new parser exception.
		 *
		 * @param message  the error message
		 * @param position the position in source code where error occurred
		 */
		public ParserException(String message, int position)
		{
			super(message);
			this.position = position;
		}

		/**
		 * Returns the position in source code where the error occurred.
		 *
		 * @return the position
		 */
		public int getPosition()
		{
			return position;
		}
	}
}
