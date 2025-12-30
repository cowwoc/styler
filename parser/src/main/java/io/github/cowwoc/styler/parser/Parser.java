package io.github.cowwoc.styler.parser;

import io.github.cowwoc.styler.ast.core.ImportAttribute;
import io.github.cowwoc.styler.ast.core.NodeArena;
import io.github.cowwoc.styler.ast.core.NodeIndex;
import io.github.cowwoc.styler.ast.core.NodeType;
import io.github.cowwoc.styler.ast.core.PackageAttribute;
import io.github.cowwoc.styler.ast.core.ParameterAttribute;
import io.github.cowwoc.styler.ast.core.SecurityConfig;
import io.github.cowwoc.styler.ast.core.TypeDeclarationAttribute;

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
	 * Counter for pending GT tokens from split RSHIFT tokens.
	 * When parsing nested generics like {@code List<Map<String, Integer>>}, the {@code >>} is
	 * tokenized as RSHIFT. When we consume the RSHIFT as a GT, we increment this counter to
	 * indicate that the next GT expectation should not advance the position.
	 */
	private int pendingGTCount;

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
			{
				++column;
			}
		}
		return new ParseError(position, line, column, message);
	}

	private NodeIndex parseCompilationUnit()
	{
		int start = currentToken().start();

		// Package declaration
		parseComments();
		if (match(TokenType.PACKAGE))
		{
			parsePackageDeclaration(start);
		}

		// Import declarations
		parseComments();
		while (currentToken().type() == TokenType.IMPORT)
		{
			parseImportDeclaration();
			parseComments();
		}

		// Type declarations (class, interface, enum)
		while (currentToken().type() != TokenType.EOF)
		{
			parseComments();
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
		return arena.allocateNode(NodeType.COMPILATION_UNIT, start, end);
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
		boolean isStatic = match(TokenType.STATIC);

		// Build the qualified name from tokens
		StringBuilder qualifiedName = new StringBuilder();
		expect(TokenType.IDENTIFIER);
		qualifiedName.append(previousToken().getText(sourceCode));

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
				ImportAttribute attribute = new ImportAttribute(qualifiedName.toString());
				if (isStatic)
				{
					return arena.allocateStaticImportDeclaration(start, end, attribute);
				}
				return arena.allocateImportDeclaration(start, end, attribute);
			}
			expect(TokenType.IDENTIFIER);
			qualifiedName.append(previousToken().getText(sourceCode));
		}
		expect(TokenType.SEMICOLON);
		int end = previousToken().end();
		ImportAttribute attribute = new ImportAttribute(qualifiedName.toString());
		if (isStatic)
		{
			return arena.allocateStaticImportDeclaration(start, end, attribute);
		}
		return arena.allocateImportDeclaration(start, end, attribute);
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
		int end = previousToken().end();
		return arena.allocateNode(NodeType.QUALIFIED_NAME, start, end);
	}

	private void parseTypeDeclaration()
	{
		// Annotations and modifiers (including sealed/non-sealed)
		while (isModifier(currentToken().type()) ||
			currentToken().type() == TokenType.SEALED ||
			currentToken().type() == TokenType.NON_SEALED ||
			currentToken().type() == TokenType.AT)
		{
			if (currentToken().type() == TokenType.AT)
			{
				// Check if this is @interface (annotation type declaration) or regular annotation
				int checkpoint = position;
				consume(); // @
				if (currentToken().type() == TokenType.INTERFACE)
				{
					// This is @interface, backtrack and let the normal flow handle it
					position = checkpoint;
					break;
				}
				// Regular annotation - parse it and continue
				position = checkpoint;
				parseAnnotation();
			}
			else
			{
				consume();
			}
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
				TRANSIENT, VOLATILE, DEFAULT -> true;
			default -> false;
		};
	}

	private NodeIndex parseClassDeclaration()
	{
		// CLASS keyword already consumed, capture its position
		int start = previousToken().start();

		// Capture type name and position before consuming
		Token nameToken = currentToken();
		expect(TokenType.IDENTIFIER);
		String typeName = nameToken.getText(sourceCode);

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

		int end = previousToken().end();
		TypeDeclarationAttribute attribute = new TypeDeclarationAttribute(typeName);
		return arena.allocateClassDeclaration(start, end, attribute);
	}

	private NodeIndex parseInterfaceDeclaration()
	{
		// INTERFACE keyword already consumed, capture its position
		int start = previousToken().start();

		// Capture type name and position before consuming
		Token nameToken = currentToken();
		expect(TokenType.IDENTIFIER);
		String typeName = nameToken.getText(sourceCode);

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

		int end = previousToken().end();
		TypeDeclarationAttribute attribute = new TypeDeclarationAttribute(typeName);
		return arena.allocateInterfaceDeclaration(start, end, attribute);
	}

	private NodeIndex parseEnumDeclaration()
	{
		// ENUM keyword already consumed, capture its position
		int start = previousToken().start();

		// Capture type name and position before consuming
		Token nameToken = currentToken();
		expect(TokenType.IDENTIFIER);
		String typeName = nameToken.getText(sourceCode);

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

		int end = previousToken().end();
		TypeDeclarationAttribute attribute = new TypeDeclarationAttribute(typeName);
		return arena.allocateEnumDeclaration(start, end, attribute);
	}

	/**
	 * Parses an annotation type declaration ({@code @interface Name { }}).
	 * <p>
	 * Expects the caller to have already consumed the {@code @} and {@code interface} tokens.
	 *
	 * @return a {@link NodeIndex} pointing to the allocated {@link NodeType#ANNOTATION_DECLARATION} node
	 */
	private NodeIndex parseAnnotationDeclaration()
	{
		// position - 2 points to '@' because caller consumed both '@' and 'interface' tokens
		int start = tokens.get(position - 2).start();

		// Capture type name and position before consuming
		Token nameToken = currentToken();
		expect(TokenType.IDENTIFIER);
		String typeName = nameToken.getText(sourceCode);

		parseClassBody();
		int end = previousToken().end();
		TypeDeclarationAttribute attribute = new TypeDeclarationAttribute(typeName);
		return arena.allocateAnnotationTypeDeclaration(start, end, attribute);
	}

	/**
	 * Parses a record declaration ({@code record Name(components) { }}).
	 * <p>
	 * Expects the caller to have already consumed the {@code record} keyword.
	 *
	 * @return a {@link NodeIndex} pointing to the allocated {@link NodeType#RECORD_DECLARATION} node
	 */
	private NodeIndex parseRecordDeclaration()
	{
		// position - 1 points to 'record' because caller consumed that keyword
		int start = previousToken().start();

		// Capture type name and position before consuming
		Token nameToken = currentToken();
		expect(TokenType.IDENTIFIER);
		String typeName = nameToken.getText(sourceCode);

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

		int end = previousToken().end();
		TypeDeclarationAttribute attribute = new TypeDeclarationAttribute(typeName);
		return arena.allocateRecordDeclaration(start, end, attribute);
	}

	private void parseTypeParameters()
	{
		parseTypeParameter();
		while (match(TokenType.COMMA))
		{
			parseTypeParameter();
		}
		expectGTInGeneric();
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
		// Parse type annotations (e.g., @Nullable, @NonNull)
		while (currentToken().type() == TokenType.AT)
		{
			parseAnnotation();
		}
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
			int typeStart = currentToken().start();
			parseQualifiedName();
			if (match(TokenType.LT))
			{
				parseTypeArguments();
				// Create PARAMETERIZED_TYPE node wrapping base type and type arguments
				int typeEnd = previousToken().end();
				arena.allocateNode(NodeType.PARAMETERIZED_TYPE, typeStart, typeEnd);
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

	/**
	 * Checks whether the given token type can start a unary expression excluding plus and minus.
	 * <p>
	 * Used for cast disambiguation per JLS 15.16: when we see {@code (Identifier) + x}, we cannot tell if
	 * this is a cast {@code (Type) (+x)} or a parenthesized expression {@code (var) + x}. Reference type
	 * casts are only recognized when followed by something other than {@code +} or {@code -}. Primitive type
	 * casts like {@code (int)} have no such ambiguity since primitive type names cannot be variable names.
	 *
	 * @param type the token type to check
	 * @return {@code true} if the token can start a unary expression (excluding {@code +} and {@code -})
	 */
	private boolean canStartUnaryExpressionNotPlusMinus(TokenType type)
	{
		return switch (type)
		{
			case IDENTIFIER, INTEGER_LITERAL, LONG_LITERAL, FLOAT_LITERAL, DOUBLE_LITERAL, BOOLEAN_LITERAL,
				CHAR_LITERAL, STRING_LITERAL, NULL_LITERAL, THIS, SUPER, NEW, LPAREN, NOT, TILDE, INC, DEC,
				SWITCH, BOOLEAN, BYTE, SHORT, INT, LONG, CHAR, FLOAT, DOUBLE, VOID -> true;
			default -> false;
		};
	}

	/**
	 * Checks whether the given token type can start any unary expression.
	 * <p>
	 * Used for cast disambiguation: primitive type casts {@code (int) expr} can be followed by any unary
	 * operand including {@code +} and {@code -}.
	 *
	 * @param type the token type to check
	 * @return {@code true} if the token can start any unary expression
	 */
	private boolean canStartUnaryExpression(TokenType type)
	{
		return canStartUnaryExpressionNotPlusMinus(type) || type == TokenType.PLUS || type == TokenType.MINUS;
	}

	/**
	 * Attempts to parse a cast expression after the opening parenthesis has been consumed.
	 * <p>
	 * Cast expressions have the form {@code (Type) operand}. This method handles:
	 * <ul>
	 *   <li>Primitive type casts: {@code (int) value}</li>
	 *   <li>Reference type casts: {@code (String) value}</li>
	 *   <li>Parameterized type casts: {@code (List<String>) value}</li>
	 *   <li>Array type casts: {@code (int[]) value}</li>
	 *   <li>Intersection type casts: {@code (Serializable & Comparable) value}</li>
	 * </ul>
	 * <p>
	 * Disambiguation between cast and parenthesized expression follows Java Language Specification:
	 * <ul>
	 *   <li>Primitive type casts can be followed by any unary expression operand</li>
	 *   <li>Reference type casts can only be followed by unary expressions that cannot be
	 *       confused with binary operator continuations (i.e., not {@code +} or {@code -})</li>
	 * </ul>
	 *
	 * @param start the start position of the opening parenthesis
	 * @return the {@link NodeIndex} of the cast expression if successful, or {@code null} if this
	 *         is not a cast expression (caller should continue with parenthesized/lambda parsing)
	 */
	private NodeIndex tryCastExpression(int start)
	{
		// Save checkpoint after '(' is consumed
		int checkpoint = position;

		// Determine if type starts with primitive
		boolean isPrimitive = isPrimitiveType(currentToken().type());

		// Track whether we've seen intersection types (&)
		boolean isIntersectionType = false;

		// Try to parse the type
		try
		{
			// Parse annotations before type (e.g., (@NonNull String) value)
			while (currentToken().type() == TokenType.AT)
			{
				parseAnnotation();
			}

			if (isPrimitive)
			{
				consume();
			}
			else if (currentToken().type() == TokenType.IDENTIFIER)
			{
				// Parse qualified name (e.g., java.lang.String)
				consume();
				while (match(TokenType.DOT))
				{
					if (currentToken().type() != TokenType.IDENTIFIER)
					{
						// Not a valid qualified name, restore and return null
						position = checkpoint;
						return null;
					}
					consume();
				}

				// Parse type arguments (e.g., List<String>)
				if (match(TokenType.LT))
				{
					parseTypeArguments();
				}

				// Parse intersection types (e.g., Serializable & Comparable)
				while (match(TokenType.BITAND))
				{
					isIntersectionType = true;
					// Parse annotations before intersection type component
					while (currentToken().type() == TokenType.AT)
					{
						parseAnnotation();
					}
					if (currentToken().type() != TokenType.IDENTIFIER)
					{
						// Not a valid intersection type, restore and return null
						position = checkpoint;
						return null;
					}
					consume();
					while (match(TokenType.DOT))
					{
						if (currentToken().type() != TokenType.IDENTIFIER)
						{
							position = checkpoint;
							return null;
						}
						consume();
					}
					if (match(TokenType.LT))
					{
						parseTypeArguments();
					}
				}
			}
			else
			{
				// Not a valid type start, restore and return null
				position = checkpoint;
				return null;
			}

			// Parse array dimensions (e.g., int[], String[][])
			while (match(TokenType.LBRACKET))
			{
				expect(TokenType.RBRACKET);
			}
		}
		catch (ParserException e)
		{
			// Type parsing failed, restore and return null
			position = checkpoint;
			return null;
		}

		// Check for closing parenthesis
		if (currentToken().type() != TokenType.RPAREN)
		{
			// Not a cast (could be expression like (a + b))
			position = checkpoint;
			return null;
		}
		consume(); // Consume ')'

		// Check disambiguation rules based on next token
		TokenType nextTokenType = currentToken().type();
		boolean validCast;
		if (isPrimitive && !isIntersectionType)
		{
			// Primitive type cast: can be followed by any unary operand
			validCast = canStartUnaryExpression(nextTokenType);
		}
		else
		{
			// Reference type cast (or intersection type): cannot be followed by + or -
			// to avoid ambiguity with binary expressions like (a) + b
			validCast = canStartUnaryExpressionNotPlusMinus(nextTokenType);
		}

		if (!validCast)
		{
			// This is not a cast expression, restore position
			position = checkpoint;
			return null;
		}

		// This is a valid cast - parse the operand
		NodeIndex operand = parseUnary();
		int end = arena.getEnd(operand);
		return arena.allocateNode(NodeType.CAST_EXPRESSION, start, end);
	}

	private void parseTypeArguments()
	{
		// Handle diamond operator: <> with no type arguments
		if (currentToken().type() == TokenType.GT)
		{
			expectGTInGeneric();
			return;
		}
		// Parse type arguments and allocate nodes (return value is the allocated NodeIndex)
		parseTypeArgument();
		while (match(TokenType.COMMA))
		{
			parseTypeArgument();
		}
		expectGTInGeneric();
	}

	private NodeIndex parseTypeArgument()
	{
		if (match(TokenType.QUESTION))
		{
			Token wildcardToken = previousToken();
			int start = wildcardToken.start();

			if (match(TokenType.EXTENDS) || match(TokenType.SUPER))
			{
				parseType();
				return arena.allocateNode(NodeType.WILDCARD_TYPE, start, previousToken().end());
			}
			// Unbounded wildcard: reuse wildcardToken for end position
			return arena.allocateNode(NodeType.WILDCARD_TYPE, start, wildcardToken.end());
		}

		int start = currentToken().start();
		parseType();
		return arena.allocateNode(NodeType.QUALIFIED_NAME, start, previousToken().end());
	}

	private void parseClassBody()
	{
		// Skip any comments before opening brace
		parseComments();
		expect(TokenType.LBRACE);
		while (!match(TokenType.RBRACE))
		{
			parseComments();
			if (currentToken().type() == TokenType.RBRACE)
			{
				// Let match() in while condition consume the RBRACE
				continue;
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
		// Handle comments before the first constant (or before SEMICOLON/RBRACE if no constants)
		parseComments();
		if (currentToken().type() != TokenType.SEMICOLON && currentToken().type() != TokenType.RBRACE)
		{
			parseEnumConstant();
			while (match(TokenType.COMMA))
			{
				// Handle comments after comma (e.g., trailing comma with comment before semicolon)
				parseComments();
				if (currentToken().type() == TokenType.SEMICOLON || currentToken().type() == TokenType.RBRACE)
				{
					break;
				}
				parseEnumConstant();
			}
		}
		// Handle comments after the last constant (before semicolon or rbrace)
		parseComments();

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
		parseComments();
		int start = currentToken().start();
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
		int end = previousToken().end();
		arena.allocateNode(NodeType.ENUM_CONSTANT, start, end);
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
			parseIdentifierMember(start);
		}
		else if (isPrimitiveType(currentToken().type()) || currentToken().type() == TokenType.VOID)
		{
			parsePrimitiveTypedMember(start);
		}
		else if (currentToken().type() == TokenType.LBRACE)
		{
			// Instance or static initializer (parseBlock expects the LBRACE)
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

	private void parseIdentifierMember(int memberStart)
	{
		int checkpoint = position;
		consume(); // Consume first identifier (could be type, constructor name, or field name)

		// Handle qualified type names: Outer.Inner, ValueLayout.OfInt, etc.
		while (match(TokenType.DOT))
		{
			if (currentToken().type() != TokenType.IDENTIFIER)
				break;
			consume();
		}

		if (match(TokenType.LPAREN))
		{
			// Constructor (no return type, identifier is constructor name)
			parseMethodRest(memberStart, true);
			return;
		}

		if (currentToken().type() == TokenType.LBRACE)
		{
			// Compact constructor (Java 16+): record component validation without parameter list
			// Example: public record Point(int x, int y) { public Point { validateInputs(); } }
			parseBlock();
			return;
		}

		// Handle generic type arguments: List<String>, Map<K, V>, etc.
		if (match(TokenType.LT))
		{
			parseTypeArguments();
		}

		// Handle array type: Type[]
		while (match(TokenType.LBRACKET))
		{
			expect(TokenType.RBRACKET);
		}

		if (currentToken().type() == TokenType.IDENTIFIER)
		{
			// Method with non-primitive return type: ReturnType methodName(...)
			// First identifier was return type, now consume method name
			consume();
			if (match(TokenType.LPAREN))
			{
				parseMethodRest(memberStart, false);
				return;
			}

			// This is a field declaration: Type fieldName = ...
			parseFieldRest(memberStart);
			return;
		}

		// Field with identifier type (no name found, restore and try as expression)
		position = checkpoint;
		consume(); // Re-consume type
		parseFieldRest(memberStart);
	}

	private void parsePrimitiveTypedMember(int memberStart)
	{
		consume();
		if (match(TokenType.LBRACKET))
		{
			expect(TokenType.RBRACKET);
		}
		expect(TokenType.IDENTIFIER);
		if (match(TokenType.LPAREN))
		{
			parseMethodRest(memberStart, false);
		}
		else
		{
			parseFieldRest(memberStart);
		}
	}

	private NodeIndex parseAnnotation()
	{
		int start = currentToken().start();
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
		int end = previousToken().end();
		return arena.allocateNode(NodeType.ANNOTATION, start, end);
	}

	private NodeIndex parseMethodRest(int start, boolean isConstructor)
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

		// Annotation element default value (for @interface methods)
		if (match(TokenType.DEFAULT))
		{
			parseExpression();
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

		int end = previousToken().end();
		NodeType nodeType;
		if (isConstructor)
			nodeType = NodeType.CONSTRUCTOR_DECLARATION;
		else
			nodeType = NodeType.METHOD_DECLARATION;
		return arena.allocateNode(nodeType, start, end);
	}

	private NodeIndex parseParameter()
	{
		int start = currentToken().start();
		boolean isFinal = false;

		// Modifiers (annotations and final)
		while (currentToken().type() == TokenType.FINAL || currentToken().type() == TokenType.AT)
		{
			if (currentToken().type() == TokenType.AT)
			{
				parseAnnotation();
			}
			else
			{
				consume();
				isFinal = true;
			}
		}

		parseType();
		boolean isVarargs = match(TokenType.ELLIPSIS);

		// Check for receiver parameter (ClassName this)
		boolean isReceiver = currentToken().type() == TokenType.THIS;
		String parameterName;
		if (isReceiver)
		{
			parameterName = "this";
			consume();
		}
		else
		{
			Token nameToken = currentToken();
			expect(TokenType.IDENTIFIER);
			parameterName = nameToken.getText(sourceCode);
		}

		// Handle C-style array syntax: String args[]
		while (match(TokenType.LBRACKET))
		{
			expect(TokenType.RBRACKET);
		}

		int end = previousToken().end();
		ParameterAttribute attribute = new ParameterAttribute(parameterName, isVarargs, isFinal, isReceiver);
		return arena.allocateParameterDeclaration(start, end, attribute);
	}

	/**
	 * Parses a catch clause parameter, handling union types for multi-catch (JDK 7+).
	 * <p>
	 * Handles both simple exception types ({@code catch (Exception e)}) and union types
	 * ({@code catch (IOException | SQLException e)}).
	 *
	 * @return a {@link NodeIndex} pointing to the allocated parameter node
	 */
	private NodeIndex parseCatchParameter()
	{
		int start = currentToken().start();
		boolean isFinal = false;

		// Modifiers (annotations and final)
		while (currentToken().type() == TokenType.FINAL || currentToken().type() == TokenType.AT)
		{
			if (currentToken().type() == TokenType.AT)
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
		if (currentToken().type() == TokenType.BITOR)
		{
			while (match(TokenType.BITOR))
			{
				parseType();
			}
			// Create UNION_TYPE node spanning all exception types
			int typeEnd = previousToken().end();
			arena.allocateNode(NodeType.UNION_TYPE, typeStart, typeEnd);
		}

		// Parameter name
		Token nameToken = currentToken();
		expect(TokenType.IDENTIFIER);
		String parameterName = nameToken.getText(sourceCode);

		int end = previousToken().end();
		ParameterAttribute attribute = new ParameterAttribute(parameterName, false, isFinal, false);
		return arena.allocateParameterDeclaration(start, end, attribute);
	}

	private NodeIndex parseFieldRest(int start)
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

		int end = previousToken().end();
		return arena.allocateNode(NodeType.FIELD_DECLARATION, start, end);
	}

	private NodeIndex parseBlock()
	{
		int start = currentToken().start();
		expect(TokenType.LBRACE);
		while (!match(TokenType.RBRACE))
		{
			parseComments();
			if (currentToken().type() == TokenType.RBRACE)
			{
				// Let match() in while condition consume the RBRACE
				continue;
			}
			if (currentToken().type() == TokenType.EOF)
			{
				throw new ParserException("Unexpected EOF in block", currentToken().start());
			}
			parseStatement();
		}
		int end = previousToken().end();
		return arena.allocateNode(NodeType.BLOCK, start, end);
	}

	private void parseStatement()
	{
		// Check for labeled statement: IDENTIFIER COLON
		int checkpoint = position;
		if (match(TokenType.IDENTIFIER) && match(TokenType.COLON))
		{
			parseLabeledStatement(checkpoint);
			return;
		}
		position = checkpoint;

		TokenType type = currentToken().type();

		switch (type)
		{
			case IF -> parseIfStatement();
			case FOR -> parseForStatement();
			case WHILE -> parseWhileStatement();
			case DO -> parseDoWhileStatement();
			case SWITCH -> parseSwitchStatement();
			case RETURN -> parseReturnStatement();
			case THROW -> parseThrowStatement();
			case YIELD -> parseYieldStatement();
			case TRY -> parseTryStatement();
			case SYNCHRONIZED -> parseSynchronizedStatement();
			case BREAK -> parseBreakStatement();
			case CONTINUE -> parseContinueStatement();
			case ASSERT -> parseAssertStatement();
			case SEMICOLON -> consume();
			case LBRACE -> parseBlock();
			case CLASS, INTERFACE, ENUM, RECORD -> parseLocalTypeDeclaration();
			default ->
			{
				if (isLocalTypeDeclarationStart())
					parseLocalTypeDeclaration();
				else
					parseExpressionOrVariableStatement();
			}
		}
	}

	/**
	 * Parses a labeled statement.
	 * A labeled statement has the form: {@code label: statement}
	 *
	 * @param labelStart the position where the label starts
	 * @return the node index for the labeled statement
	 */
	private NodeIndex parseLabeledStatement(int labelStart)
	{
		parseStatement();
		int end = previousToken().end();
		return arena.allocateNode(NodeType.LABELED_STATEMENT, labelStart, end);
	}

	/**
	 * Checks if the current position starts a local type declaration.
	 * <p>
	 * Uses lookahead to detect modifiers/annotations followed by type keywords
	 * ({@code class}, {@code interface}, {@code enum}, {@code record}).
	 * This method performs lookahead without permanently consuming tokens.
	 *
	 * @return {@code true} if a local type declaration starts at the current position
	 */
	private boolean isLocalTypeDeclarationStart()
	{
		int checkpoint = position;
		// Skip modifiers and annotations
		while (isModifier(currentToken().type()) ||
			currentToken().type() == TokenType.AT ||
			currentToken().type() == TokenType.SEALED ||
			currentToken().type() == TokenType.NON_SEALED)
		{
			if (currentToken().type() == TokenType.AT)
			{
				consume();
				parseQualifiedName();
				if (match(TokenType.LPAREN))
				{
					skipBalancedParens();
				}
			}
			else
			{
				consume();
			}
		}
		boolean result = switch (currentToken().type())
		{
			case CLASS, INTERFACE, ENUM, RECORD -> true;
			default -> false;
		};
		position = checkpoint;
		return result;
	}

	/**
	 * Parses a local type declaration (class, interface, enum, or record).
	 * <p>
	 * Local types are type declarations that appear inside method bodies, constructors,
	 * or initializer blocks. This method handles modifiers (such as {@code final},
	 * {@code abstract}, {@code sealed}, and annotations) that may precede the type keyword,
	 * then delegates to {@link #parseNestedTypeDeclaration()}.
	 */
	private void parseLocalTypeDeclaration()
	{
		skipMemberModifiers();
		parseNestedTypeDeclaration();
	}

	/**
	 * Skips tokens until matching closing parenthesis is found.
	 * <p>
	 * Used for lookahead when skipping annotation arguments. Assumes the opening
	 * parenthesis has already been consumed. Counts parenthesis depth and consumes
	 * tokens until the depth returns to zero.
	 */
	private void skipBalancedParens()
	{
		int depth = 1;
		while (depth > 0 && currentToken().type() != TokenType.EOF)
		{
			if (match(TokenType.LPAREN))
			{
				++depth;
			}
			else if (match(TokenType.RPAREN))
			{
				--depth;
			}
			else
			{
				consume();
			}
		}
	}

	private NodeIndex parseBreakStatement()
	{
		int start = currentToken().start();
		consume();
		if (currentToken().type() == TokenType.IDENTIFIER)
		{
			consume();
		}
		expect(TokenType.SEMICOLON);
		int end = previousToken().end();
		return arena.allocateNode(NodeType.BREAK_STATEMENT, start, end);
	}

	private NodeIndex parseContinueStatement()
	{
		int start = currentToken().start();
		consume();
		if (currentToken().type() == TokenType.IDENTIFIER)
		{
			consume();
		}
		expect(TokenType.SEMICOLON);
		int end = previousToken().end();
		return arena.allocateNode(NodeType.CONTINUE_STATEMENT, start, end);
	}

	private NodeIndex parseIfStatement()
	{
		int start = currentToken().start();
		expect(TokenType.IF);
		expect(TokenType.LPAREN);
		parseExpression();
		expect(TokenType.RPAREN);
		parseStatement();
		if (match(TokenType.ELSE))
		{
			parseStatement();
		}
		int end = previousToken().end();
		return arena.allocateNode(NodeType.IF_STATEMENT, start, end);
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
			// Consume declaration annotations (before FINAL)
			while (currentToken().type() == TokenType.AT)
			{
				parseAnnotation();
			}
			// Consume FINAL modifier if present
			if (currentToken().type() == TokenType.FINAL)
			{
				consume();
			}
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
		return type == TokenType.AT || type == TokenType.FINAL || isPrimitiveType(type) || type == TokenType.IDENTIFIER;
	}

	private NodeIndex parseForStatement()
	{
		int start = currentToken().start();
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
			int end = previousToken().end();
			return arena.allocateNode(NodeType.ENHANCED_FOR_STATEMENT, start, end);
		}
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
		int end = previousToken().end();
		return arena.allocateNode(NodeType.FOR_STATEMENT, start, end);
	}

	private NodeIndex parseWhileStatement()
	{
		int start = currentToken().start();
		expect(TokenType.WHILE);
		expect(TokenType.LPAREN);
		parseExpression();
		expect(TokenType.RPAREN);
		parseStatement();
		int end = previousToken().end();
		return arena.allocateNode(NodeType.WHILE_STATEMENT, start, end);
	}

	private NodeIndex parseDoWhileStatement()
	{
		int start = currentToken().start();
		expect(TokenType.DO);
		parseStatement();
		expect(TokenType.WHILE);
		expect(TokenType.LPAREN);
		parseExpression();
		expect(TokenType.RPAREN);
		expect(TokenType.SEMICOLON);
		int end = previousToken().end();
		return arena.allocateNode(NodeType.DO_WHILE_STATEMENT, start, end);
	}

	private NodeIndex parseSwitchStatement()
	{
		int start = currentToken().start();
		expect(TokenType.SWITCH);
		expect(TokenType.LPAREN);
		parseExpression();
		expect(TokenType.RPAREN);
		expect(TokenType.LBRACE);
		while (currentToken().type() == TokenType.CASE || currentToken().type() == TokenType.DEFAULT)
		{
			if (match(TokenType.CASE))
			{
				// Parse first case label element
				parseCaseLabelElement();
				// Handle multiple case labels: case 1, 2, 3 -> or case 'L', 'l':
				while (match(TokenType.COMMA))
				{
					parseCaseLabelElement();
				}
			}
			else
			{
				consume(); // DEFAULT
			}

			if (match(TokenType.ARROW))
			{
				// Arrow case: case 1 -> expr; or case 1 -> { ... }
				if (currentToken().type() == TokenType.LBRACE)
				{
					parseBlock();
				}
				else if (currentToken().type() == TokenType.THROW)
				{
					consume();
					parseExpression();
					expect(TokenType.SEMICOLON);
				}
				else
				{
					parseStatement();
				}
			}
			else
			{
				// Colon case (traditional): case 1:
				expect(TokenType.COLON);
				while (currentToken().type() != TokenType.CASE &&
					currentToken().type() != TokenType.DEFAULT &&
					currentToken().type() != TokenType.RBRACE)
				{
					parseStatement();
				}
			}
		}
		expect(TokenType.RBRACE);
		int end = previousToken().end();
		return arena.allocateNode(NodeType.SWITCH_STATEMENT, start, end);
	}

	private NodeIndex parseSwitchExpression(int start)
	{
		// SWITCH already consumed
		expect(TokenType.LPAREN);
		parseExpression();
		expect(TokenType.RPAREN);
		expect(TokenType.LBRACE);

		while (currentToken().type() == TokenType.CASE || currentToken().type() == TokenType.DEFAULT)
		{
			if (match(TokenType.CASE))
			{
				// Parse first case label element (may be expression or type pattern)
				parseCaseLabelElement();
				// Handle multiple case labels: case 1, 2, 3 ->
				while (match(TokenType.COMMA))
				{
					parseCaseLabelElement();
				}
			}
			else
			{
				consume(); // DEFAULT
			}

			if (match(TokenType.ARROW))
			{
				// Arrow case: case 1 -> expr;
				if (currentToken().type() == TokenType.LBRACE)
				{
					// Block body: case 1 -> { ... }
					parseBlock();
				}
				else if (currentToken().type() == TokenType.THROW)
				{
					// Throw expression: case 1 -> throw new Exception();
					consume();
					parseExpression();
					expect(TokenType.SEMICOLON);
				}
				else
				{
					// Expression body: case 1 -> value;
					parseExpression();
					expect(TokenType.SEMICOLON);
				}
			}
			else
			{
				// Colon case (traditional): case 1:
				expect(TokenType.COLON);
				while (currentToken().type() != TokenType.CASE &&
					currentToken().type() != TokenType.DEFAULT &&
					currentToken().type() != TokenType.RBRACE)
				{
					parseStatement();
				}
			}
		}

		expect(TokenType.RBRACE);
		int end = previousToken().end();
		return arena.allocateNode(NodeType.SWITCH_EXPRESSION, start, end);
	}

	/**
	 * Parses a single case label element in a switch statement or expression.
	 * Handles:
	 * <ul>
	 *   <li>Constant expressions: {@code case 1}, {@code case 'L'}, {@code case FOO}</li>
	 *   <li>Type patterns: {@code case String s}, {@code case Foo.Bar bar}, {@code case Type _}</li>
	 *   <li>{@code null} keyword: {@code case null}</li>
	 *   <li>{@code default} keyword: {@code case null, default}</li>
	 * </ul>
	 * Type patterns are distinguished from constant expressions by checking if an identifier
	 * (potentially qualified) is followed by another identifier or underscore.
	 */
	private void parseCaseLabelElement()
	{
		// Handle special keywords that can appear as case labels
		if (match(TokenType.NULL_LITERAL))
		{
			return;
		}
		if (match(TokenType.DEFAULT))
		{
			return;
		}

		// Try to detect type pattern: Type identifier or Type _
		// Type patterns look like: String s, Foo.Bar bar, Integer _, etc.
		if (currentToken().type() == TokenType.IDENTIFIER && tryParseTypePattern())
		{
			return;
		}

		// Parse as regular expression
		parseExpression();
	}

	/**
	 * Attempts to parse a type pattern or record pattern in a case label.
	 * <ul>
	 *   <li>Type pattern: {@code Type patternVariable} (e.g., {@code String s}, {@code Foo.Bar bar},
	 *       {@code Integer _})</li>
	 *   <li>Record pattern: {@code Type(componentPatterns...)} (e.g., {@code Point(int x, int y)},
	 *       {@code Box(String _)})</li>
	 * </ul>
	 * Both may be followed by a guard expression: {@code when guardExpr}
	 *
	 * @return {@code true} if a pattern was successfully parsed, {@code false} if the current
	 *         position should be treated as a regular expression
	 */
	private boolean tryParseTypePattern()
	{
		int checkpoint = position;
		int typeStart = currentToken().start();

		// Parse potential type (may be qualified like Foo.Bar.Baz)
		consume(); // First identifier
		while (match(TokenType.DOT))
		{
			if (currentToken().type() != TokenType.IDENTIFIER)
			{
				// Not a qualified name, restore position
				position = checkpoint;
				return false;
			}
			consume();
		}

		// Check if this is a record pattern: Type(components...)
		if (currentToken().type() == TokenType.LPAREN)
		{
			parseRecordPattern(typeStart);
			return true;
		}

		// Check if next token is an identifier (pattern variable)
		// This includes both named variables (s, bar) and unnamed pattern (_)
		if (currentToken().type() == TokenType.IDENTIFIER)
		{
			consume();
			// Check for optional guard: "when" expression
			if (isContextualKeyword("when"))
			{
				parseGuardExpression();
			}
			return true;
		}

		// Not a type pattern, restore position
		position = checkpoint;
		return false;
	}

	/**
	 * Parses a record pattern after the type name has been consumed.
	 * Record patterns have the form: {@code Type(componentPattern, componentPattern, ...)}
	 * <p>
	 * Examples:
	 * <ul>
	 *   <li>{@code Point(int x, int y)}</li>
	 *   <li>{@code Empty()}</li>
	 *   <li>{@code Box(Point(int x, int y))}</li>
	 *   <li>{@code Point(int x, int y) when x > 0}</li>
	 * </ul>
	 *
	 * @param typeStart the start position of the type name
	 * @return the node index of the record pattern
	 */
	private NodeIndex parseRecordPattern(int typeStart)
	{
		expect(TokenType.LPAREN);
		parseRecordPatternComponents();
		expect(TokenType.RPAREN);

		// Check for optional guard: "when" expression
		if (isContextualKeyword("when"))
		{
			parseGuardExpression();
		}

		int end = previousToken().end();
		return arena.allocateNode(NodeType.RECORD_PATTERN, typeStart, end);
	}

	/**
	 * Parses the component patterns inside a record pattern's parentheses.
	 * Handles empty component lists and comma-separated component patterns.
	 */
	private void parseRecordPatternComponents()
	{
		// Handle empty component list: Empty()
		if (currentToken().type() == TokenType.RPAREN)
		{
			return;
		}

		parseComponentPattern();
		while (match(TokenType.COMMA))
		{
			parseComponentPattern();
		}
	}

	/**
	 * Parses a single component pattern within a record pattern.
	 * Component patterns can be:
	 * <ul>
	 *   <li>Unnamed pattern: {@code _}</li>
	 *   <li>Type pattern: {@code Type variable} (e.g., {@code int x}, {@code String s},
	 *       {@code var x}, {@code String[] items})</li>
	 *   <li>Nested record pattern: {@code Type(components...)} (e.g., {@code Point(int x, int y)})</li>
	 * </ul>
	 */
	private void parseComponentPattern()
	{
		// Check for unnamed pattern: _
		if (currentToken().type() == TokenType.IDENTIFIER &&
			"_".equals(currentToken().getText(sourceCode)))
		{
			consume();
			return;
		}

		// Parse type (may be primitive, var, or qualified reference type)
		int componentTypeStart = currentToken().start();
		if (isPrimitiveType(currentToken().type()))
		{
			consume();
		}
		else if (currentToken().type() == TokenType.VAR)
		{
			// Type inference with 'var' keyword
			consume();
		}
		else if (currentToken().type() == TokenType.IDENTIFIER)
		{
			consume();
			while (match(TokenType.DOT))
			{
				if (currentToken().type() != TokenType.IDENTIFIER)
				{
					throw new ParserException(
						"Expected identifier after '.' in type", currentToken().start());
				}
				consume();
			}
		}
		else
		{
			throw new ParserException(
				"Expected type in component pattern", currentToken().start());
		}

		// Handle array dimensions (e.g., String[], int[][])
		while (match(TokenType.LBRACKET))
		{
			expect(TokenType.RBRACKET);
		}

		// Determine what follows the type:
		// - LPAREN -> nested record pattern
		// - IDENTIFIER -> type pattern with variable name
		if (currentToken().type() == TokenType.LPAREN)
		{
			// Nested record pattern
			parseRecordPattern(componentTypeStart);
		}
		else if (currentToken().type() == TokenType.IDENTIFIER)
		{
			// Type pattern: consume the variable name
			consume();
		}
		// else: just a type without variable (could happen in some edge cases)
	}

	/**
	 * Checks if the current token is an identifier that matches a contextual keyword.
	 * Contextual keywords like "when" are only treated as keywords in specific contexts,
	 * not as reserved words throughout the language.
	 *
	 * @param keyword the contextual keyword to check for
	 * @return {@code true} if the current token is an identifier matching the keyword
	 */
	private boolean isContextualKeyword(String keyword)
	{
		return currentToken().type() == TokenType.IDENTIFIER &&
			currentToken().getText(sourceCode).equals(keyword);
	}

	/**
	 * Parses a guard expression following the "when" contextual keyword in a guarded pattern.
	 * The "when" keyword must have already been detected via {@link #isContextualKeyword(String)}.
	 * <p>
	 * Example: {@code case String s when s.length() > 5 -> ...}
	 */
	private void parseGuardExpression()
	{
		// Consume the "when" contextual keyword
		consume();
		// Parse the guard condition expression
		parseExpression();
	}

	private NodeIndex parseReturnStatement()
	{
		int start = currentToken().start();
		expect(TokenType.RETURN);
		if (currentToken().type() != TokenType.SEMICOLON)
		{
			parseExpression();
		}
		expect(TokenType.SEMICOLON);
		int end = previousToken().end();
		return arena.allocateNode(NodeType.RETURN_STATEMENT, start, end);
	}

	private NodeIndex parseThrowStatement()
	{
		int start = currentToken().start();
		expect(TokenType.THROW);
		parseExpression();
		expect(TokenType.SEMICOLON);
		int end = previousToken().end();
		return arena.allocateNode(NodeType.THROW_STATEMENT, start, end);
	}

	/**
	 * Parses a yield statement within a switch expression block.
	 * <p>
	 * The yield statement (JDK 14+) returns a value from a switch expression block.
	 * It differs from return in that it yields a value to the enclosing switch expression,
	 * not from the enclosing method.
	 * <p>
	 * Syntax: {@code yield expression;}
	 *
	 * @return a {@link NodeIndex} pointing to the allocated {@link NodeType#YIELD_STATEMENT} node
	 */
	private NodeIndex parseYieldStatement()
	{
		int start = currentToken().start();
		expect(TokenType.YIELD);
		parseExpression();
		expect(TokenType.SEMICOLON);
		int end = previousToken().end();
		return arena.allocateNode(NodeType.YIELD_STATEMENT, start, end);
	}

	private NodeIndex parseTryStatement()
	{
		int start = currentToken().start();
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
		while (currentToken().type() == TokenType.CATCH)
		{
			parseCatchClause();
		}

		// Finally clause
		if (currentToken().type() == TokenType.FINALLY)
		{
			parseFinallyClause();
		}

		int end = previousToken().end();
		return arena.allocateNode(NodeType.TRY_STATEMENT, start, end);
	}

	private NodeIndex parseCatchClause()
	{
		int start = currentToken().start();
		expect(TokenType.CATCH);
		expect(TokenType.LPAREN);
		parseCatchParameter();
		expect(TokenType.RPAREN);
		parseBlock();
		int end = previousToken().end();
		return arena.allocateNode(NodeType.CATCH_CLAUSE, start, end);
	}

	private NodeIndex parseFinallyClause()
	{
		int start = currentToken().start();
		expect(TokenType.FINALLY);
		parseBlock();
		int end = previousToken().end();
		return arena.allocateNode(NodeType.FINALLY_CLAUSE, start, end);
	}

	/**
	 * Parses a resource in a try-with-resources statement.
	 * <p>
	 * Supports two forms per JLS 14.20.3:
	 * <ul>
	 *   <li><b>Resource declaration (JDK 7+)</b>: {@code [final] Type name = expression}
	 *       <br>Example: {@code final var reader = new FileReader(file)}</li>
	 *   <li><b>Variable reference (JDK 9+)</b>: An effectively-final variable or field access
	 *       <br>Examples: {@code reader}, {@code this.resource}, {@code Outer.this.resource}</li>
	 * </ul>
	 * <p>
	 * Annotations may precede either form.
	 */
	private void parseResource()
	{
		// Consume declaration annotations (e.g., @Cleanup)
		while (currentToken().type() == TokenType.AT)
		{
			parseAnnotation();
		}

		if (isResourceVariableReference())
		{
			parseResourceVariableReference();
		}
		else
		{
			parseResourceDeclaration();
		}
	}

	/**
	 * Determines if the current position represents a variable reference in try-with-resources
	 * rather than a full declaration.
	 * <p>
	 * Variable references are effectively-final variables or field accesses used directly
	 * as resources (JDK 9+ feature). They are distinguished from declarations by:
	 * <ul>
	 *   <li>Simple identifier followed by {@code ;} or {@code )}</li>
	 *   <li>Field access starting with {@code this}</li>
	 * </ul>
	 *
	 * @return {@code true} if current position is a variable reference
	 */
	private boolean isResourceVariableReference()
	{
		// Field access: this.resource or Outer.this.resource
		if (currentToken().type() == TokenType.THIS)
		{
			return true;
		}

		// Simple identifier followed by ; or ) indicates variable reference
		// Note: qualified names like java.io.Reader would be followed by IDENTIFIER (variable name)
		if (currentToken().type() == TokenType.IDENTIFIER)
		{
			int checkpoint = position;
			consume();
			TokenType nextType = currentToken().type();
			position = checkpoint;
			return nextType == TokenType.SEMICOLON || nextType == TokenType.RPAREN;
		}

		return false;
	}

	/**
	 * Parses a variable reference in try-with-resources (JDK 9+).
	 * <p>
	 * Handles simple identifiers ({@code reader}) and field access expressions
	 * ({@code this.resource}, {@code Outer.this.resource}).
	 */
	private void parseResourceVariableReference()
	{
		// Parse as expression - will naturally produce IDENTIFIER or FIELD_ACCESS node
		parseExpression();
	}

	/**
	 * Parses a resource declaration in try-with-resources (JDK 7+ traditional syntax).
	 * <p>
	 * Syntax: {@code [final] Type variableName = expression}
	 */
	private void parseResourceDeclaration()
	{
		// Optional FINAL modifier
		if (currentToken().type() == TokenType.FINAL)
		{
			consume();
		}
		parseType();
		expect(TokenType.IDENTIFIER);
		expect(TokenType.ASSIGN);
		parseExpression();
	}

	private NodeIndex parseSynchronizedStatement()
	{
		int start = currentToken().start();
		expect(TokenType.SYNCHRONIZED);
		expect(TokenType.LPAREN);
		parseExpression();
		expect(TokenType.RPAREN);
		parseBlock();
		int end = previousToken().end();
		return arena.allocateNode(NodeType.SYNCHRONIZED_STATEMENT, start, end);
	}

	private NodeIndex parseAssertStatement()
	{
		int start = currentToken().start();
		expect(TokenType.ASSERT);
		parseExpression();
		if (match(TokenType.COLON))
		{
			parseExpression();
		}
		expect(TokenType.SEMICOLON);
		int end = previousToken().end();
		return arena.allocateNode(NodeType.ASSERT_STATEMENT, start, end);
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
			// Consume declaration annotations (before FINAL modifier)
			while (currentToken().type() == TokenType.AT)
			{
				parseAnnotation();
			}
			// Consume optional FINAL modifier
			if (currentToken().type() == TokenType.FINAL)
			{
				consume();
			}
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
		if ((currentToken().type() == TokenType.AT ||
			currentToken().type() == TokenType.FINAL ||
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
		// Check for lambda expression: identifier -> expr
		if (currentToken().type() == TokenType.IDENTIFIER)
		{
			// Look ahead to see if this is a lambda
			int checkpoint = position;
			int start = currentToken().start();
			consume(); // consume identifier

			if (match(TokenType.ARROW))
			{
				// This is a lambda expression: x -> body
				return parseLambdaBody(start);
			}

			// Not a lambda, restore position
			position = checkpoint;
		}

		return parseAssignment();
	}

	private NodeIndex parseLambdaBody(int start)
	{
		int end;
		if (currentToken().type() == TokenType.LBRACE)
		{
			// Block lambda: x -> { statements }
			// parseBlock() creates the BLOCK node; we just need the end position
			parseBlock();
			end = previousToken().end();
		}
		else
		{
			// Expression lambda: x -> expr
			NodeIndex body = parseExpression();
			end = arena.getEnd(body);
		}
		return arena.allocateNode(NodeType.LAMBDA_EXPRESSION, start, end);
	}

	/**
	 * Parses parenthesized expression, cast expression, or lambda expression.
	 * <p>
	 * Handles:
	 * <ul>
	 *   <li>Empty parens lambda: {@code () -> expr}</li>
	 *   <li>Cast expression: {@code (Type) operand}</li>
	 *   <li>Parenthesized lambda: {@code (params) -> expr}</li>
	 *   <li>Parenthesized expression: {@code (expr)}</li>
	 * </ul>
	 *
	 * @param start the start position of the opening paren
	 * @return the parsed node
	 */
	private NodeIndex parseParenthesizedOrLambda(int start)
	{
		// Check for empty parens lambda: () -> expr
		if (match(TokenType.RPAREN))
		{
			expect(TokenType.ARROW);
			return parseLambdaBody(start);
		}

		// Try to parse as a cast expression
		NodeIndex castExpr = tryCastExpression(start);
		if (castExpr != null)
		{
			return castExpr;
		}

		// Parse the content inside parens
		NodeIndex expr = parseExpression();
		expect(TokenType.RPAREN);

		// Check if this is a lambda: (params) -> expr
		if (match(TokenType.ARROW))
		{
			return parseLambdaBody(start);
		}

		// Regular parenthesized expression
		return expr;
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
			return arena.allocateNode(NodeType.ASSIGNMENT_EXPRESSION, start, end);
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
			return arena.allocateNode(NodeType.CONDITIONAL_EXPRESSION, start, end);
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
			left = arena.allocateNode(NodeType.BINARY_EXPRESSION, start, end);
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
		NodeIndex left = parseShift();

		while (matchesAny(TokenType.LT, TokenType.GT, TokenType.LE, TokenType.GE))
		{
			NodeIndex right = parseShift();
			int start = arena.getStart(left);
			int end = arena.getEnd(right);
			left = arena.allocateNode(NodeType.BINARY_EXPRESSION, start, end);
		}

		// Handle instanceof specially - requires type reference, optionally followed by pattern variable
		if (match(TokenType.INSTANCEOF))
		{
			int start = arena.getStart(left);
			parseType();

			int end = previousToken().end();
			// Check for optional pattern variable (Java 16+ pattern matching)
			if (currentToken().type() == TokenType.IDENTIFIER)
			{
				consume();
				end = previousToken().end();
			}

			return arena.allocateNode(NodeType.BINARY_EXPRESSION, start, end);
		}

		return left;
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
			return arena.allocateNode(NodeType.UNARY_EXPRESSION, start, end);
		}

		return parsePostfix();
	}

	private NodeIndex parsePostfix()
	{
		NodeIndex left = parsePrimary();

		while (true)
		{
			parseComments();
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
				int end = previousToken().end();
				left = arena.allocateNode(NodeType.METHOD_INVOCATION, start, end);
			}
			else if (match(TokenType.DOT))
			{
				parseComments();
				// Field access, class literal, or method reference
				if (currentToken().type() == TokenType.IDENTIFIER)
				{
					int end = currentToken().end();
					consume();
					left = arena.allocateNode(NodeType.FIELD_ACCESS, start, end);
				}
				else if (match(TokenType.CLASS))
				{
					// Class literal: Type.class, Type[].class
					int end = previousToken().end();
					left = arena.allocateNode(NodeType.CLASS_LITERAL, start, end);
				}
				else if (match(TokenType.THIS))
				{
					// Qualified this: Outer.this
					int end = previousToken().end();
					left = arena.allocateNode(NodeType.THIS_EXPRESSION, start, end);
				}
				else if (match(TokenType.SUPER))
				{
					// Qualified super: Outer.super
					int end = previousToken().end();
					left = arena.allocateNode(NodeType.SUPER_EXPRESSION, start, end);
				}
				else
				{
					throw new ParserException(
						"Expected identifier, 'class', 'this', or 'super' after '.' but found " + currentToken().type(),
						currentToken().start());
				}
			}
			else if (match(TokenType.LBRACKET))
			{
				left = parseArrayAccessOrClassLiteral(start);
			}
			else if (match(TokenType.DOUBLE_COLON))
			{
				parseComments();
				// Method reference: Type::method or Type::new
				int end;
				if (match(TokenType.NEW))
				{
					// Constructor reference
					end = previousToken().end();
				}
				else if (currentToken().type() == TokenType.IDENTIFIER)
				{
					// Method reference
					end = currentToken().end();
					consume();
				}
				else
				{
					throw new ParserException(
						"Expected method name or 'new' after '::' but found " + currentToken().type(),
						currentToken().start());
				}
				left = arena.allocateNode(NodeType.METHOD_REFERENCE, start, end);
			}
			else if (currentToken().type() == TokenType.INC || currentToken().type() == TokenType.DEC)
			{
				// Postfix increment/decrement
				int end = currentToken().end();
				consume();
				left = arena.allocateNode(NodeType.POSTFIX_EXPRESSION, start, end);
			}
			else
			{
				break;
			}
		}

		return left;
	}

	/**
	 * Parses array access or array type class literal after LBRACKET consumed.
	 * Handles {@code array[index]} for array access and {@code Type[].class} for class literals.
	 *
	 * @param start the start position of the expression
	 * @return the parsed node (ARRAY_ACCESS or CLASS_LITERAL)
	 */
	private NodeIndex parseArrayAccessOrClassLiteral(int start)
	{
		// Check for array type class literal: Type[].class or Type[][].class
		if (match(TokenType.RBRACKET))
		{
			// Empty brackets - array type for class literal
			while (match(TokenType.LBRACKET))
			{
				expect(TokenType.RBRACKET);
			}
			expect(TokenType.DOT);
			expect(TokenType.CLASS);
			int end = previousToken().end();
			return arena.allocateNode(NodeType.CLASS_LITERAL, start, end);
		}
		// Array access with expression
		parseExpression();
		expect(TokenType.RBRACKET);
		int end = previousToken().end();
		return arena.allocateNode(NodeType.ARRAY_ACCESS, start, end);
	}

	private NodeIndex parsePrimary()
	{
		enterDepth();

		try
		{
			parseComments();
			Token token = currentToken();
			int start = token.start();
			int end = token.end();

			if (token.isLiteral())
			{
				consume();
				return parseLiteralExpression(token, start, end);
			}

			if (token.type() == TokenType.IDENTIFIER)
			{
				consume();
				return arena.allocateNode(NodeType.IDENTIFIER, start, end);
			}

			if (match(TokenType.LPAREN))
			{
				return parseParenthesizedOrLambda(start);
			}

			if (match(TokenType.NEW))
			{
				return parseNewExpression(start);
			}

			if (match(TokenType.THIS))
			{
				return arena.allocateNode(NodeType.THIS_EXPRESSION, start, end);
			}

			if (match(TokenType.SUPER))
			{
				return arena.allocateNode(NodeType.SUPER_EXPRESSION, start, end);
			}

			if (match(TokenType.LBRACE))
			{
				// Array initializer: {1, 2, 3}
				return parseArrayInitializer(start);
			}

			if (token.type() == TokenType.AT)
			{
				return parseAnnotation();
			}

			if (isPrimitiveType(token.type()))
			{
				consume();
				return parsePrimitiveClassLiteral(start);
			}

			if (match(TokenType.SWITCH))
			{
				// Switch expression: switch (x) { case 1 -> 10; default -> 0; }
				return parseSwitchExpression(start);
			}

			if (token.type() == TokenType.ARROW)
			{
				// Lambda with inferred parameter (handled by caller)
				// For now, just consume and create a placeholder
				consume();
				NodeIndex body = parseExpression();
				return arena.allocateNode(NodeType.LAMBDA_EXPRESSION, start, arena.getEnd(body));
			}

			// Handle unary operators that appear after comments (e.g., /* comment */ -5)
			TokenType type = token.type();
			boolean isUnaryOperator = switch (type)
			{
				case MINUS, PLUS, NOT, TILDE, INC, DEC -> true;
				default -> false;
			};
			if (isUnaryOperator)
			{
				consume();
				NodeIndex operand = parsePrimary();
				int operandEnd = arena.getEnd(operand);
				return arena.allocateNode(NodeType.UNARY_EXPRESSION, start, operandEnd);
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

	/**
	 * Parses a literal expression (integer, long, float, double, boolean, char, string, null).
	 *
	 * @param token the literal token
	 * @param start the start position
	 * @param end   the end position
	 * @return the node index for the literal
	 */
	private NodeIndex parseLiteralExpression(Token token, int start, int end)
	{
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
		return arena.allocateNode(nodeType, start, end);
	}

	/**
	 * Parses a primitive type class literal (e.g., {@code int.class}, {@code void.class}, {@code int[].class}).
	 *
	 * @param start the start position
	 * @return the node index for the class literal
	 */
	private NodeIndex parsePrimitiveClassLiteral(int start)
	{
		while (match(TokenType.LBRACKET))
		{
			expect(TokenType.RBRACKET);
		}
		expect(TokenType.DOT);
		expect(TokenType.CLASS);
		int classEnd = previousToken().end();
		return arena.allocateNode(NodeType.CLASS_LITERAL, start, classEnd);
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

		int arrayEnd = previousToken().end();
		return arena.allocateNode(NodeType.ARRAY_CREATION, start, arrayEnd);
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

		int objEnd = previousToken().end();
		return arena.allocateNode(NodeType.OBJECT_CREATION, start, objEnd);
	}

	private NodeIndex parseArrayInitializer(int start)
	{
		// LBRACE already consumed
		if (!match(TokenType.RBRACE))
		{
			// Handle nested array initializers or expressions
			if (currentToken().type() == TokenType.LBRACE)
			{
				int nestedStart = currentToken().start();
				consume();
				parseArrayInitializer(nestedStart);
			}
			else
			{
				parseExpression();
			}
			while (match(TokenType.COMMA))
			{
				if (currentToken().type() == TokenType.RBRACE)
				{
					break;
				}
				if (currentToken().type() == TokenType.LBRACE)
				{
					int nestedStart = currentToken().start();
					consume();
					parseArrayInitializer(nestedStart);
				}
				else
				{
					parseExpression();
				}
			}
			expect(TokenType.RBRACE);
		}
		int end = previousToken().end();
		return arena.allocateNode(NodeType.ARRAY_INITIALIZER, start, end);
	}

	// Token navigation

	/**
	 * Parses comment tokens and creates AST nodes for them.
	 * Comments are preserved in the AST to support pure AST-based position checking.
	 */
	private void parseComments()
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

	private Token currentToken()
	{
		if (position < tokens.size())
		{
			return tokens.get(position);
		}
		return tokens.get(tokens.size() - 1);
	}

	/**
	 * Returns the token that was just consumed (at position - 1).
	 *
	 * @return the previous token
	 */
	private Token previousToken()
	{
		return tokens.get(position - 1);
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
			++position;
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

	/**
	 * Expects a GT token in generic type context, handling split RSHIFT tokens.
	 * When parsing nested generics like {@code List<Map<String, Integer>>}, the {@code >>}
	 * is tokenized as RSHIFT. This method handles both GT and RSHIFT cases.
	 */
	private void expectGTInGeneric()
	{
		// Check for pending GT from previous RSHIFT split
		if (pendingGTCount > 0)
		{
			--pendingGTCount;
			return;
		}

		TokenType type = currentToken().type();
		if (type == TokenType.GT)
		{
			consume();
		}
		else if (type == TokenType.RSHIFT)
		{
			// RSHIFT (>>) represents two GT tokens
			// Consume and mark one GT as pending for next call
			consume();
			++pendingGTCount;
		}
		else if (type == TokenType.URSHIFT)
		{
			// URSHIFT (>>>) represents three GT tokens
			consume();
			pendingGTCount += 2;
		}
		else
		{
			throw new ParserException(
				"Expected GT but found " + type + " at position " + currentToken().start(),
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
