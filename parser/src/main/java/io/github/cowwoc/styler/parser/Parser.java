package io.github.cowwoc.styler.parser;

import io.github.cowwoc.styler.ast.core.ImportAttribute;
import io.github.cowwoc.styler.ast.core.NodeArena;
import io.github.cowwoc.styler.ast.core.NodeIndex;
import io.github.cowwoc.styler.ast.core.NodeType;
import io.github.cowwoc.styler.ast.core.PackageAttribute;
import io.github.cowwoc.styler.ast.core.ParameterAttribute;
import io.github.cowwoc.styler.ast.core.SecurityConfig;

import java.io.IOException;
import java.io.Serial;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;

import io.github.cowwoc.styler.parser.internal.ExpressionParser;
import io.github.cowwoc.styler.parser.internal.ModuleParser;
import io.github.cowwoc.styler.parser.internal.ParserAccess;
import io.github.cowwoc.styler.parser.internal.StatementParser;
import io.github.cowwoc.styler.parser.internal.TypeParser;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;

/**
 * Recursive descent parser for Java source code.
 * Builds an Index-Overlay AST using NodeArena for memory efficiency.
 * Enforces maximum parsing depth for security.
 */
public final class Parser implements AutoCloseable
{
	/**
	 * Frequency of timeout checks during token consumption.
	 * Checked every 100 token consumptions to amortize System.currentTimeMillis() overhead (~100ns).
	 */
	private static final int TIMEOUT_CHECK_INTERVAL = 100;

	private final String sourceCode;
	private final List<Token> tokens;
	private final NodeArena arena;
	private final Instant parsingDeadline;
	private int position;
	private int depth;

	/**
	 * Counter for periodic timeout checks in consume().
	 * Reset to 0 every {@link #TIMEOUT_CHECK_INTERVAL} calls.
	 */
	private int tokenCheckCounter;

	/**
	 * Counter for pending GREATER_THAN tokens from split RIGHT_SHIFT tokens.
	 * When parsing nested generics like {@code List<Map<String, Integer>>}, the {@code >>} is
	 * tokenized as RIGHT_SHIFT. When we consume the RIGHT_SHIFT as a GREATER_THAN, we increment this counter to
	 * indicate that the next GREATER_THAN expectation should not advance the position.
	 */
	private int pendingGTCount;

	/**
	 * Helper for parsing module-info.java files (JPMS module declarations).
	 */
	private final ModuleParser moduleParser;

	/**
	 * Helper for parsing try-catch-finally statements and resources.
	 */
	private final StatementParser statementParser;

	/**
	 * Helper for parsing type declarations (class, interface, enum, record, annotation).
	 */
	private final TypeParser typeParser;

	/**
	 * Helper for expression parsing, specifically cast and lambda disambiguation.
	 */
	private final ExpressionParser expressionParser;

	/**
	 * Accessor providing controlled access to parser internals for helper classes.
	 * This allows helper classes to call private methods without exposing them publicly.
	 */
	private final ParserAccess parserAccess;

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

		this.sourceCode = source;
		Lexer lexer = new Lexer(source);
		this.tokens = lexer.tokenize();

		// SEC-007: Token count limit to prevent excessive memory consumption
		if (tokens.size() > SecurityConfig.MAX_TOKEN_COUNT)
		{
			throw new IllegalArgumentException(
				"Too many tokens: " + tokens.size() + " exceeds maximum of " + SecurityConfig.MAX_TOKEN_COUNT);
		}

		this.arena = new NodeArena();
		this.parserAccess = createParserAccess();
		this.expressionParser = new ExpressionParser(parserAccess);
		this.moduleParser = new ModuleParser(parserAccess);
		this.statementParser = new StatementParser(parserAccess);
		this.typeParser = new TypeParser(parserAccess);

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
	 * Returns the source code being parsed.
	 *
	 * @return the source code
	 */
	public String getSourceCode()
	{
		return sourceCode;
	}

	/**
	 * Returns the token list.
	 *
	 * @return the tokens
	 */
	public List<Token> getTokens()
	{
		return tokens;
	}

	/**
	 * Returns the current token position.
	 *
	 * @return the position
	 */
	public int getPosition()
	{
		return position;
	}

	/**
	 * Sets the current token position.
	 *
	 * @param position the new position
	 */
	public void setPosition(int position)
	{
		this.position = position;
	}

	/**
	 * Parses the source code and returns the result.
	 *
	 * @return the parse result containing either the root node or parse errors
	 */
	public ParseResult parse()
	{
		try
		{
			return new ParseResult.Success(parseCompilationUnit());
		}
		catch (ParserException e)
		{
			ParseError error = createError(e.getMessage(), e.getPosition());
			return new ParseResult.Failure(List.of(error));
		}
	}

	/**
	 * Creates a parse error with line and column information calculated from the position.
	 *
	 * @param message  the error message
	 * @param position the 0-based character offset in source code
	 * @return a new parse error with line and column information
	 */
	private ParseError createError(String message, int position)
	{
		int line = 1;
		int column = 1;
		for (int i = 0; i < position && i < sourceCode.length(); ++i)
		{
			if (sourceCode.charAt(i) == '\n')
			{
				++line;
				column = 1;
			}
			else
				++column;
		}
		return new ParseError(position, line, column, message);
	}

	private NodeIndex parseCompilationUnit()
	{
		int start = currentToken().start();

		// Parse any leading comments before detection
		parseComments();

		// Check if this is a module declaration (module-info.java)
		// Module declarations start with optional annotations, optional "open", then "module"
		if (moduleParser.isModuleDeclarationStart())
		{
			return moduleParser.parseModuleCompilationUnit();
		}

		// Check for package-level annotations
		// Only consume annotations if they are followed by 'package' keyword (not @interface or class)
		int packageAnnotationsStart = -1;
		if (hasPackageLevelAnnotations())
		{
			packageAnnotationsStart = currentToken().start();
			while (currentToken().type() == TokenType.AT_SIGN && !isAnnotationTypeDeclaration())
			{
				parseAnnotation();
				parseComments();
			}
		}

		if (match(TokenType.PACKAGE))
		{
			// Package declaration start: first annotation if present, else 'package' keyword position
			int effectiveStart;
			if (packageAnnotationsStart >= 0)
				effectiveStart = packageAnnotationsStart;
			else
				effectiveStart = previousToken().start();
			parsePackageDeclaration(effectiveStart);
		}

		// Import declarations
		parseComments();
		while (currentToken().type() == TokenType.IMPORT)
		{
			parseImportDeclaration();
			parseComments();
		}

		// Type declarations (class, interface, enum) or implicit class (JEP 512)
		parseComments();
		if (isTypeDeclarationStart())
		{
			// Traditional type declarations
			while (currentToken().type() != TokenType.END_OF_FILE)
			{
				parseComments();
				if (isTypeDeclarationStart())
					parseTypeDeclaration();
				else if (currentToken().type() == TokenType.SEMICOLON)
					consume(); // Empty statement at top level
				else if (currentToken().type() == TokenType.END_OF_FILE)
					break;
				else
					throw new ParserException(
						"Unexpected token at top level: " + currentToken().type() +
						" (expected type declaration, import, or package)",
						currentToken().start());
			}
		}
		else if (isMemberDeclarationStart())
			// JEP 512: Implicit class - members without explicit class declaration
			parseImplicitClassDeclaration();
		else if (currentToken().type() != TokenType.END_OF_FILE)
			throw new ParserException(
				"Unexpected token at top level: " + currentToken().type() +
				" (expected type declaration, import, or package)",
				currentToken().start());

		int end = tokens.get(tokens.size() - 1).end();
		return arena.allocateNode(NodeType.COMPILATION_UNIT, start, end);
	}

	private boolean isTypeDeclarationStart()
	{
		// Look ahead past modifiers to find the actual declaration keyword
		int lookahead = position;
		while (lookahead < tokens.size())
		{
			TokenType type = tokens.get(lookahead).type();
			if (type == TokenType.CLASS || type == TokenType.INTERFACE ||
				type == TokenType.ENUM || type == TokenType.RECORD)
				return true;
			// @interface is an annotation type declaration
			if (type == TokenType.AT_SIGN && lookahead + 1 < tokens.size() &&
				tokens.get(lookahead + 1).type() == TokenType.INTERFACE)
				return true;
			// Skip modifiers to continue looking
			if (isModifierToken(type))
			{
				++lookahead;
				continue;
			}
			// Skip comments to continue looking
			if (isCommentToken(type))
			{
				++lookahead;
				continue;
			}
			// Skip annotations (@ followed by identifier and optional arguments)
			if (type == TokenType.AT_SIGN)
			{
				lookahead = skipAnnotationAt(lookahead);
				continue;
			}
			// Found a non-modifier, non-type-keyword token - not a type declaration
			return false;
		}
		return false;
	}

	private boolean isModifierToken(TokenType type)
	{
		return switch (type)
		{
			case PUBLIC, PRIVATE, PROTECTED, STATIC, FINAL, ABSTRACT, SEALED, NON_SEALED, STRICTFP -> true;
			default -> false;
		};
	}

	/**
	 * Skips over an annotation starting at the given position.
	 * <p>
	 * Handles both marker annotations ({@code @Override}) and annotations with arguments
	 * ({@code @SuppressWarnings("unchecked")}).
	 *
	 * @param startPosition the token index of the {@code @} symbol
	 * @return the token index immediately after the annotation (the first token that is not part of the
	 *         annotation)
	 */
	private int skipAnnotationAt(int startPosition)
	{
		int lookahead = startPosition + 1;
		if (lookahead >= tokens.size() || tokens.get(lookahead).type() != TokenType.IDENTIFIER)
			return lookahead;
		++lookahead;
		if (lookahead >= tokens.size() || tokens.get(lookahead).type() != TokenType.LEFT_PARENTHESIS)
			return lookahead;
		// Skip annotation arguments
		int parenthesisDepth = 1;
		++lookahead;
		while (lookahead < tokens.size() && parenthesisDepth > 0)
		{
			TokenType type = tokens.get(lookahead).type();
			if (type == TokenType.LEFT_PARENTHESIS)
				++parenthesisDepth;
			else if (type == TokenType.RIGHT_PARENTHESIS)
				--parenthesisDepth;
			++lookahead;
		}
		return lookahead;
	}

	/**
	 * Checks if the current token is {@code @} and the next is {@code interface},
	 * indicating an annotation type declaration.
	 *
	 * @return {@code true} if this is the start of an annotation type declaration
	 */
	private boolean isAnnotationTypeDeclaration()
	{
		return currentToken().type() == TokenType.AT_SIGN &&
			position + 1 < tokens.size() &&
			tokens.get(position + 1).type() == TokenType.INTERFACE;
	}

	/**
	 * Uses lookahead to check if annotations at the current position are package-level annotations.
	 * <p>
	 * Package-level annotations are annotations that appear before the {@code package} keyword
	 * in package-info.java files. This method looks ahead through annotations (skipping annotation
	 * type declarations) to find if they precede a {@code package} keyword.
	 *
	 * @return {@code true} if the current position has annotations followed by a package declaration
	 */
	private boolean hasPackageLevelAnnotations()
	{
		if (currentToken().type() != TokenType.AT_SIGN)
			return false;
		// Don't treat @interface as package annotation
		if (isAnnotationTypeDeclaration())
			return false;

		// Save position for lookahead
		int checkpoint = position;

		// Skip annotations and comments until we find either PACKAGE or something else
		while (currentToken().type() == TokenType.AT_SIGN && !isAnnotationTypeDeclaration())
		{
			// Skip the annotation
			consume(); // @
			parseQualifiedName();
			if (match(TokenType.LEFT_PARENTHESIS))
				skipBalancedParens();
			// Skip any comments between annotations
			while (currentToken().type() == TokenType.LINE_COMMENT ||
				currentToken().type() == TokenType.BLOCK_COMMENT)
				consume();
		}

		boolean isPackageAnnotation = currentToken().type() == TokenType.PACKAGE;
		position = checkpoint;
		return isPackageAnnotation;
	}

	private NodeIndex parsePackageDeclaration(int start)
	{
		// Capture the package name from the qualified name tokens
		int nameStart = currentToken().start();
		parseQualifiedName();
		int nameEnd = previousToken().end();
		String packageName = sourceCode.substring(nameStart, nameEnd);

		expect(TokenType.SEMICOLON);
		PackageAttribute attribute = new PackageAttribute(packageName);
		return arena.allocatePackageDeclaration(start, previousToken().end(), attribute);
	}

	private NodeIndex parseImportDeclaration()
	{
		int start = currentToken().start();
		expect(TokenType.IMPORT);

		// JEP 511: Module import syntax: import module java.base;
		if (match(TokenType.MODULE))
			return moduleParser.parseModuleImport(start);

		boolean isStatic = match(TokenType.STATIC);

		// Build the qualified name from tokens
		StringBuilder qualifiedName = new StringBuilder();
		expectIdentifierOrContextualKeyword();
		qualifiedName.append(previousToken().decodedText());

		while (currentToken().type() == TokenType.DOT)
		{
			consume(); // DOT
			qualifiedName.append('.');
			if (match(TokenType.STAR))
			{
				// Wildcard import: import java.util.*;
				qualifiedName.append('*');
				expect(TokenType.SEMICOLON);
				int end = previousToken().end();
				ImportAttribute attribute = new ImportAttribute(qualifiedName.toString(), isStatic);
				return arena.allocateImportDeclaration(start, end, attribute);
			}
			expectIdentifierOrContextualKeyword();
			qualifiedName.append(previousToken().decodedText());
		}
		expect(TokenType.SEMICOLON);
		int end = previousToken().end();
		ImportAttribute attribute = new ImportAttribute(qualifiedName.toString(), isStatic);
		return arena.allocateImportDeclaration(start, end, attribute);
	}

	/**
	 * Parses a qualified name (e.g., {@code java.lang.String}).
	 *
	 * @return the qualified name node index
	 */
	public NodeIndex parseQualifiedName()
	{
		int start = currentToken().start();
		expectIdentifierOrContextualKeyword();
		while (match(TokenType.DOT))
		{
			if (isIdentifierOrContextualKeyword())
				consume();
			else
				break;
		}
		int end = previousToken().end();
		return arena.allocateNode(NodeType.QUALIFIED_NAME, start, end);
	}

	private void parseTypeDeclaration()
	{
		typeParser.parseTypeDeclaration();
	}

	/**
	 * Checks if the current position could start a member declaration (field or method).
	 * <p>
	 * Used to detect implicit class content (JEP 512) where top-level members appear without
	 * an explicit class declaration.
	 *
	 * @return {@code true} if the current token could begin a member declaration
	 */
	private boolean isMemberDeclarationStart()
	{
		TokenType type = currentToken().type();
		return switch (type)
		{
			// Return type or field type (could be void for methods)
			case IDENTIFIER, VOID -> true;
			// Member modifiers
			case PUBLIC, PRIVATE, PROTECTED, STATIC, FINAL, ABSTRACT, SYNCHRONIZED, NATIVE,
				TRANSIENT, VOLATILE, DEFAULT -> true;
			// Primitive types (for fields or method return types)
			case BOOLEAN, BYTE, SHORT, INT, LONG, CHAR, FLOAT, DOUBLE -> true;
			// Annotation on member (but not @interface which is a type declaration)
			case AT_SIGN -> !isAnnotationTypeDeclaration();
			default -> false;
		};
	}

	private NodeIndex parseImplicitClassDeclaration()
	{
		return typeParser.parseImplicitClassDeclaration();
	}

	/**
	 * Parses a complete type reference including array dimension brackets.
	 * <p>
	 * This method handles primitive types, reference types with optional type arguments, and array
	 * dimensions. For array creation expressions where dimension brackets contain expressions or
	 * initializers, use {@link #parseTypeWithoutArrayDimensions()} instead.
	 */
	public void parseType()
	{
		parseTypeWithoutArrayDimensions();
		parseArrayDimensionsWithAnnotations();
	}

	/**
	 * Parses a type reference without consuming array dimension brackets.
	 * <p>
	 * This method is used specifically in array creation expressions ({@code new Type[size]}) where the array
	 * brackets must be parsed separately to capture dimension expressions or initializers.
	 * <p>
	 * For general type parsing including array brackets, use {@link #parseType()}.
	 */
	private void parseTypeWithoutArrayDimensions()
	{
		parseComments();
		// Parse type annotations (e.g., @Nullable, @NonNull)
		while (currentToken().type() == TokenType.AT_SIGN)
			parseAnnotation();
		if (isPrimitiveType(currentToken().type()))
			consume();
		else if (currentToken().type() == TokenType.VAR)
			// Type inference with 'var' keyword (JDK 10+)
			consume();
		else
		{
			int typeStart = currentToken().start();
			parseQualifiedName();
			if (match(TokenType.LESS_THAN))
			{
				parseTypeArguments();
				// Create PARAMETERIZED_TYPE node wrapping base type and type arguments
				int typeEnd = previousToken().end();
				arena.allocateNode(NodeType.PARAMETERIZED_TYPE, typeStart, typeEnd);
			}
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

	/**
	 * Checks if the given token type is a contextual keyword that can be used as an identifier.
	 * <p>
	 * Contextual keywords are reserved only in specific syntactic contexts but are valid
	 * identifiers elsewhere. For example, {@code var} is a keyword only for local variable
	 * type inference, and {@code module} is a keyword only in module-info.java.
	 *
	 * @param type the token type to check
	 * @return {@code true} if the token is a contextual keyword usable as an identifier
	 */
	private boolean isContextualKeyword(TokenType type)
	{
		return switch (type)
		{
			case VAR, YIELD, RECORD, MODULE, OPEN, TO, REQUIRES, EXPORTS, OPENS, USES, PROVIDES, WITH,
				TRANSITIVE -> true;
			default -> false;
		};
	}

	/**
	 * Checks if the given token type is a comment token.
	 *
	 * @param type the token type to check
	 * @return {@code true} if the token is any type of comment
	 */
	private boolean isCommentToken(TokenType type)
	{
		return switch (type)
		{
			case LINE_COMMENT, BLOCK_COMMENT, JAVADOC_COMMENT, MARKDOWN_DOC_COMMENT -> true;
			default -> false;
		};
	}

	/**
	 * Expects an identifier or contextual keyword token and consumes it.
	 * <p>
	 * Contextual keywords like {@code var}, {@code module}, and {@code with} are valid
	 * identifiers outside their special syntactic contexts.
	 *
	 * @throws ParserException if current token is neither an identifier nor a contextual keyword
	 */
	public void expectIdentifierOrContextualKeyword()
	{
		TokenType type = currentToken().type();
		if (type == TokenType.IDENTIFIER || isContextualKeyword(type))
			consume();
		else
		{
			throw new ParserException(
				"Expected identifier but found " + type + " at position " + currentToken().start(),
				currentToken().start());
		}
	}

	/**
	 * Checks if the current token is an identifier or a contextual keyword.
	 *
	 * @return {@code true} if current token can be used as an identifier
	 */
	private boolean isIdentifierOrContextualKeyword()
	{
		TokenType type = currentToken().type();
		return type == TokenType.IDENTIFIER || isContextualKeyword(type);
	}

	/**
	 * Parses optional array dimensions with JSR 308 type annotations.
	 * <p>
	 * Handles syntax like {@code @NonNull []} or {@code @A [] @B []}.
	 * Consumes zero or more annotated array dimension sequences.
	 *
	 * @return {@code true} if at least one array dimension was parsed
	 */
	private boolean parseArrayDimensionsWithAnnotations()
	{
		boolean hasArrayDimensions = false;
		while (true)
		{
			while (currentToken().type() == TokenType.AT_SIGN)
				parseAnnotation();
			if (!match(TokenType.LEFT_BRACKET))
				break;
			expect(TokenType.RIGHT_BRACKET);
			hasArrayDimensions = true;
		}
		return hasArrayDimensions;
	}

	private boolean canStartUnaryExpressionNotPlusMinus(TokenType type)
	{
		return expressionParser.canStartUnaryExpressionNotPlusMinus(type);
	}

	private boolean canStartUnaryExpression(TokenType type)
	{
		return expressionParser.canStartUnaryExpression(type);
	}

	private NodeIndex tryCastExpression(int start)
	{
		return expressionParser.tryCastExpression(start, expressionParser::parseUnary, this::parseLambdaBody);
	}

	private NodeIndex parseCastOperand(TokenType nextTokenType)
	{
		return expressionParser.parseCastOperand(nextTokenType, expressionParser::parseUnary, this::parseLambdaBody);
	}

	private boolean lookaheadIsArrow()
	{
		return expressionParser.lookaheadIsArrow();
	}

	private boolean isLambdaExpression()
	{
		return expressionParser.isLambdaExpression();
	}

	private void parseTypeArguments()
	{
		typeParser.parseTypeArguments();
	}

	/**
	 * Parses an annotation.
	 *
	 * @return the annotation node index
	 */
	public NodeIndex parseAnnotation()
	{
		int start = currentToken().start();
		expect(TokenType.AT_SIGN);
		parseQualifiedName();
		if (match(TokenType.LEFT_PARENTHESIS) && !match(TokenType.RIGHT_PARENTHESIS))
		{
			parseExpression();
			while (match(TokenType.COMMA))
				parseExpression();
			expect(TokenType.RIGHT_PARENTHESIS);
		}
		int end = previousToken().end();
		return arena.allocateNode(NodeType.ANNOTATION, start, end);
	}

	/**
	 * Parses a catch clause parameter, handling union types for multi-catch (JDK 7+).
	 * <p>
	 * Handles both simple exception types ({@code catch (Exception e)}) and union types
	 * ({@code catch (IOException | SQLException e)}).
	 *
	 * @return a {@link NodeIndex} pointing to the allocated parameter node
	 */
	public NodeIndex parseCatchParameter()
	{
		int start = currentToken().start();
		boolean isFinal = false;

		// Modifiers (annotations and final)
		while (currentToken().type() == TokenType.FINAL || currentToken().type() == TokenType.AT_SIGN)
		{
			if (currentToken().type() == TokenType.AT_SIGN)
			{
				parseAnnotation();
			}
			else
			{
				consume();
				isFinal = true;
			}
		}

		// Parse first exception type
		int typeStart = currentToken().start();
		parseType();

		// Check for union type (multi-catch): Type1 | Type2 | ...
		if (currentToken().type() == TokenType.BITWISE_OR)
		{
			while (match(TokenType.BITWISE_OR))
				parseType();
			// Create UNION_TYPE node spanning all exception types
			int typeEnd = previousToken().end();
			arena.allocateNode(NodeType.UNION_TYPE, typeStart, typeEnd);
		}

		// Parameter name
		Token nameToken = currentToken();
		expectIdentifierOrContextualKeyword();
		String parameterName = nameToken.decodedText();

		int end = previousToken().end();
		ParameterAttribute attribute = new ParameterAttribute(parameterName, false, isFinal, false);
		return arena.allocateParameterDeclaration(start, end, attribute);
	}

	/**
	 * Parses a block statement.
	 *
	 * @return the block node index
	 */
	public NodeIndex parseBlock()
	{
		int start = currentToken().start();
		expect(TokenType.LEFT_BRACE);
		while (!match(TokenType.RIGHT_BRACE))
		{
			parseComments();
			if (currentToken().type() == TokenType.RIGHT_BRACE)
				// Let match() in while condition consume the RIGHT_BRACE
				continue;
			if (currentToken().type() == TokenType.END_OF_FILE)
				throw new ParserException("Unexpected END_OF_FILE in block", currentToken().start());
			parseStatement();
		}
		int end = previousToken().end();
		return arena.allocateNode(NodeType.BLOCK, start, end);
	}

	private void parseStatement()
	{
		statementParser.parseStatement();
	}

	private NodeIndex parseLabeledStatement(int labelStart)
	{
		return statementParser.parseLabeledStatement(labelStart);
	}

	private boolean isLocalTypeDeclarationStart()
	{
		return statementParser.isLocalTypeDeclarationStart();
	}

	private void parseLocalTypeDeclaration()
	{
		statementParser.parseLocalTypeDeclaration();
	}

	private boolean isModifier(TokenType type)
	{
		return typeParser.isModifier(type);
	}

	private void skipMemberModifiers()
	{
		typeParser.skipMemberModifiers();
	}

	private boolean parseNestedTypeDeclaration()
	{
		return typeParser.parseNestedTypeDeclaration();
	}

	private void skipBalancedParens()
	{
		statementParser.skipBalancedParens();
	}

	private NodeIndex parseBreakStatement()
	{
		return statementParser.parseBreakStatement();
	}

	private NodeIndex parseContinueStatement()
	{
		return statementParser.parseContinueStatement();
	}

	private NodeIndex parseIfStatement()
	{
		return statementParser.parseIfStatement();
	}

	private boolean tryParseEnhancedForHeader()
	{
		return statementParser.tryParseEnhancedForHeader();
	}

	private boolean looksLikeTypeStart()
	{
		return statementParser.looksLikeTypeStart();
	}

	private NodeIndex parseForStatement()
	{
		return statementParser.parseForStatement();
	}

	private NodeIndex parseWhileStatement()
	{
		return statementParser.parseWhileStatement();
	}

	private NodeIndex parseDoWhileStatement()
	{
		return statementParser.parseDoWhileStatement();
	}

	private NodeIndex parseSwitchStatement()
	{
		return statementParser.parseSwitchStatement();
	}

	private NodeIndex parseSwitchExpression(int start)
	{
		return expressionParser.parseSwitchExpression(start);
	}

	private void parseCaseLabelElement()
	{
		statementParser.parseCaseLabelElement();
	}

	private void parseCaseLabelExpression()
	{
		statementParser.parseCaseLabelExpression();
	}

	private boolean tryParsePrimitiveTypePattern()
	{
		return statementParser.tryParsePrimitiveTypePattern();
	}

	private boolean tryParseTypePattern()
	{
		return statementParser.tryParseTypePattern();
	}

	private NodeIndex parseRecordPattern(int typeStart)
	{
		return statementParser.parseRecordPattern(typeStart);
	}

	private void parseRecordPatternComponents()
	{
		statementParser.parseRecordPatternComponents();
	}

	private void parseComponentPattern()
	{
		statementParser.parseComponentPattern();
	}

	private void parseGuardExpression()
	{
		statementParser.parseGuardExpression();
	}

	private NodeIndex parseReturnStatement()
	{
		return statementParser.parseReturnStatement();
	}

	private NodeIndex parseThrowStatement()
	{
		return statementParser.parseThrowStatement();
	}

	private NodeIndex parseYieldStatement()
	{
		return statementParser.parseYieldStatement();
	}

	private NodeIndex parseTryStatement()
	{
		return statementParser.parseTryStatement();
	}

	private NodeIndex parseSynchronizedStatement()
	{
		return statementParser.parseSynchronizedStatement();
	}

	private NodeIndex parseAssertStatement()
	{
		return statementParser.parseAssertStatement();
	}

	private boolean tryParseVariableDeclaration(int checkpoint)
	{
		return statementParser.tryParseVariableDeclaration(checkpoint);
	}

	private void parseAdditionalDeclarators()
	{
		statementParser.parseAdditionalDeclarators();
	}

	private void parseExpressionOrVariableStatement()
	{
		statementParser.parseExpressionOrVariableStatement();
	}

	private NodeIndex parseExpression()
	{
		return expressionParser.parseExpression();
	}

	private NodeIndex parseLambdaBody(int start)
	{
		return expressionParser.parseLambdaBody(start);
	}

	private NodeIndex parseParenthesizedOrLambda(int start)
	{
		return expressionParser.parseParenthesizedOrLambda(start);
	}

	private NodeIndex parseNewExpression(int start)
	{
		return expressionParser.parseNewExpression(start);
	}

	private NodeIndex parseArrayCreation(int start)
	{
		return expressionParser.parseArrayCreation(start);
	}

	private NodeIndex parseObjectCreation(int start)
	{
		return expressionParser.parseObjectCreation(start);
	}

	private NodeIndex parseArrayInitializer(int start)
	{
		return expressionParser.parseArrayInitializer(start);
	}

	// Token navigation

	/**
	 * Parses comment tokens and creates AST nodes for them.
	 * Comments are preserved in the AST to support pure AST-based position checking.
	 */
	public void parseComments()
	{
		while (true)
		{
			Token token = currentToken();
			int start = token.start();
			int end = token.end();

			switch (token.type())
			{
				case JAVADOC_COMMENT ->
				{
					consume();
					arena.allocateNode(NodeType.JAVADOC_COMMENT, start, end);
				}
				case BLOCK_COMMENT ->
				{
					consume();
					arena.allocateNode(NodeType.BLOCK_COMMENT, start, end);
				}
				case MARKDOWN_DOC_COMMENT ->
				{
					consume();
					arena.allocateNode(NodeType.MARKDOWN_DOC_COMMENT, start, end);
				}
				case LINE_COMMENT ->
				{
					consume();
					arena.allocateNode(NodeType.LINE_COMMENT, start, end);
				}
				default ->
				{
					return;
				}
			}
		}
	}

	/**
	 * Returns the current token at the current position.
	 *
	 * @return the current token
	 */
	public Token currentToken()
	{
		if (position < tokens.size())
			return tokens.get(position);
		return tokens.get(tokens.size() - 1);
	}

	/**
	 * Returns the token that was just consumed (at position - 1).
	 *
	 * @return the previous token
	 */
	public Token previousToken()
	{
		return tokens.get(position - 1);
	}

	/**
	 * Consumes the current token and advances the position.
	 *
	 * @return the consumed token
	 */
	public Token consume()
	{
		// SEC-006: Periodic timeout checking to detect hung parsers
		++tokenCheckCounter;
		if (tokenCheckCounter >= TIMEOUT_CHECK_INTERVAL)
		{
			tokenCheckCounter = 0;
			if (Instant.now().isAfter(parsingDeadline))
				throw new ParserException(
					"Parsing timeout exceeded (" + SecurityConfig.PARSING_TIMEOUT_MS + "ms) at position " +
					currentToken().start(),
					currentToken().start());
		}

		Token current = currentToken();
		if (position < tokens.size() - 1)
			++position;
		return current;
	}

	/**
	 * Matches and consumes a token if it has the given type.
	 *
	 * @param type the expected token type
	 * @return {@code true} if the token matched and was consumed
	 */
	public boolean match(TokenType type)
	{
		if (currentToken().type() == type)
		{
			consume();
			return true;
		}
		return false;
	}

	/**
	 * Expects and consumes a token of the given type.
	 *
	 * @param type the expected token type
	 * @throws ParserException if the current token is not of the expected type
	 */
	public void expect(TokenType type)
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

	/**
	 * Expects a GREATER_THAN token in generic type context, handling split RIGHT_SHIFT tokens.
	 * When parsing nested generics like {@code List<Map<String, Integer>>}, the {@code >>}
	 * is tokenized as RIGHT_SHIFT. This method handles both GREATER_THAN and RIGHT_SHIFT cases.
	 */
	private void expectGTInGeneric()
	{
		// Check for pending GREATER_THAN from previous RIGHT_SHIFT split
		if (pendingGTCount > 0)
		{
			--pendingGTCount;
			return;
		}

		TokenType type = currentToken().type();
		if (type == TokenType.GREATER_THAN)
			consume();
		else if (type == TokenType.RIGHT_SHIFT)
		{
			// RIGHT_SHIFT (>>) represents two GREATER_THAN tokens
			// Consume and mark one GREATER_THAN as pending for next call
			consume();
			++pendingGTCount;
		}
		else if (type == TokenType.UNSIGNED_RIGHT_SHIFT)
		{
			// UNSIGNED_RIGHT_SHIFT (>>>) represents three GREATER_THAN tokens
			consume();
			pendingGTCount += 2;
		}
		else
		{
			throw new ParserException(
				"Expected GREATER_THAN but found " + type + " at position " + currentToken().start(),
				currentToken().start());
		}
	}

	// Depth checking

	/**
	 * Enters a new recursion depth level with 2-tier security monitoring.
	 * <p>
	 * <strong>Security Strategy:</strong> Multi-layered defense combining timeout detection
	 * and stack depth limiting to prevent resource exhaustion attacks.
	 * <p>
	 * <strong>Tier 1 - Timeout Protection (SEC-006):</strong> Checks parsing deadline on EVERY
	 * call to detect hung parsers. Overhead: ~100ns per call (System.currentTimeMillis()).
	 * <p>
	 * <strong>Tier 2 - Stack Protection:</strong> Checks recursion depth on EVERY call to
	 * prevent stack overflow. Overhead: negligible (integer comparison).
	 * <p>
	 * <strong>Call Frequency:</strong> This method is called from parseUnary() and parsePrimary()
	 * during expression parsing, typically 10-1000 times per file depending on expression complexity.
	 *
	 * @throws ParserException if timeout exceeded or max depth exceeded
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

		++depth;
		if (depth > SecurityConfig.MAX_NODE_DEPTH)
		{
			throw new ParserException(
				"Maximum node depth exceeded (" + SecurityConfig.MAX_NODE_DEPTH + ") at position " +
				currentToken().start(),
				currentToken().start());
		}
	}

	private void exitDepth()
	{
		--depth;
	}

	/**
	 * Creates a ParserAccess implementation that delegates to this parser's methods.
	 * This allows helper classes to access parser internals without exposing them publicly.
	 *
	 * @return the parser accessor
	 */
	private ParserAccess createParserAccess()
	{
		return new ParserAccess()
		{
			@Override
			public List<Token> getTokens()
			{
				return tokens;
			}

			@Override
			public int getPosition()
			{
				return position;
			}

			@Override
			public void setPosition(int newPosition)
			{
				position = newPosition;
			}

			@Override
			public Token currentToken()
			{
				return Parser.this.currentToken();
			}

			@Override
			public Token previousToken()
			{
				return Parser.this.previousToken();
			}

			@Override
			public Token consume()
			{
				return Parser.this.consume();
			}

			@Override
			public boolean match(TokenType type)
			{
				return Parser.this.match(type);
			}

			@Override
			public void expect(TokenType type)
			{
				Parser.this.expect(type);
			}

			@Override
			public void expectIdentifierOrContextualKeyword()
			{
				Parser.this.expectIdentifierOrContextualKeyword();
			}

			@Override
			public void expectGTInGeneric()
			{
				Parser.this.expectGTInGeneric();
			}

			@Override
			public NodeArena getArena()
			{
				return arena;
			}

			@Override
			public void enterDepth()
			{
				Parser.this.enterDepth();
			}

			@Override
			public void exitDepth()
			{
				Parser.this.exitDepth();
			}

			@Override
			public void parseComments()
			{
				Parser.this.parseComments();
			}

			@Override
			public void parseType()
			{
				Parser.this.parseType();
			}

			@Override
			public void parseTypeWithoutArrayDimensions()
			{
				Parser.this.parseTypeWithoutArrayDimensions();
			}

			@Override
			public boolean parseArrayDimensionsWithAnnotations()
			{
				return Parser.this.parseArrayDimensionsWithAnnotations();
			}

			@Override
			public boolean isPrimitiveType(TokenType type)
			{
				return Parser.this.isPrimitiveType(type);
			}

			@Override
			public boolean isIdentifierOrContextualKeyword()
			{
				return Parser.this.isIdentifierOrContextualKeyword();
			}

			@Override
			public boolean isContextualKeyword(TokenType type)
			{
				return Parser.this.isContextualKeyword(type);
			}

			@Override
			public NodeIndex parseAnnotation()
			{
				return Parser.this.parseAnnotation();
			}

			@Override
			public NodeIndex parseQualifiedName()
			{
				return Parser.this.parseQualifiedName();
			}

			@Override
			public void parseTypeArguments()
			{
				Parser.this.parseTypeArguments();
			}

			@Override
			public NodeIndex parseBlock()
			{
				return Parser.this.parseBlock();
			}

			@Override
			public void parseStatement()
			{
				statementParser.parseStatement();
			}

			@Override
			public void parseMemberDeclaration()
			{
				typeParser.parseMemberDeclaration();
			}

			@Override
			public void parseCaseLabelElement()
			{
				statementParser.parseCaseLabelElement();
			}

			@Override
			public NodeIndex parseExpression()
			{
				return expressionParser.parseExpression();
			}

			@Override
			public NodeIndex parseCatchParameter()
			{
				return Parser.this.parseCatchParameter();
			}

			@Override
			public String getSourceCode()
			{
				return sourceCode;
			}

			@Override
			public boolean isModifier(TokenType type)
			{
				return Parser.this.isModifier(type);
			}

			@Override
			public void skipMemberModifiers()
			{
				Parser.this.skipMemberModifiers();
			}

			@Override
			public boolean parseNestedTypeDeclaration()
			{
				return Parser.this.parseNestedTypeDeclaration();
			}

			@Override
			public NodeIndex parseAssignment()
			{
				return expressionParser.parseAssignment();
			}

			@Override
			public NodeIndex parseLogicalOr()
			{
				return expressionParser.parseLogicalOr();
			}

			@Override
			public void skipBalancedParentheses()
			{
				Parser.this.skipBalancedParens();
			}

			@Override
			public NodeIndex parseUnary()
			{
				return expressionParser.parseUnary();
			}

			@Override
			public boolean isLambdaExpression()
			{
				return Parser.this.isLambdaExpression();
			}

			@Override
			public NodeIndex tryCastExpression(int start)
			{
				return Parser.this.tryCastExpression(start);
			}

			@Override
			public NodeIndex parseNewExpression(int start)
			{
				return Parser.this.parseNewExpression(start);
			}

			@Override
			public NodeIndex parseSwitchExpression(int start)
			{
				return Parser.this.parseSwitchExpression(start);
			}

			@Override
			public NodeIndex parseArrayInitializer(int start)
			{
				return Parser.this.parseArrayInitializer(start);
			}
		};
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
		@Serial
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
