package io.github.cowwoc.styler.parser.internal;

import io.github.cowwoc.styler.ast.core.NodeIndex;
import io.github.cowwoc.styler.ast.core.NodeType;
import io.github.cowwoc.styler.parser.Parser.ParserException;
import io.github.cowwoc.styler.parser.TokenType;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.that;

/**
 * Helper class for parsing statements within method bodies.
 * <p>
 * Extracted from Parser to reduce class size while maintaining cohesive parsing logic.
 * Handles all statement types including control flow, declarations, and expressions.
 */
public final class StatementParser
{
	private final ParserAccess parser;

	/**
	 * Creates a new statement parser that delegates to the given parser.
	 *
	 * @param parser the parent parser providing token access and helper methods
	 */
	public StatementParser(ParserAccess parser)
	{
		assert that(parser, "parser").isNotNull().elseThrow();
		this.parser = parser;
	}

	/**
	 * Parses a statement within a block.
	 * <p>
	 * Handles all Java statement types: control flow, declarations, expression statements,
	 * and labeled statements.
	 */
	public void parseStatement()
	{
		// Check for labeled statement: IDENTIFIER COLON
		int checkpoint = parser.getPosition();
		if (parser.match(TokenType.IDENTIFIER) && parser.match(TokenType.COLON))
		{
			parseLabeledStatement(checkpoint);
			return;
		}
		parser.setPosition(checkpoint);

		TokenType type = parser.currentToken().type();

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
			case SEMICOLON -> parser.consume();
			case LEFT_BRACE -> parser.parseBlock();
			case CLASS, INTERFACE, ENUM -> parseLocalTypeDeclaration();
			case RECORD -> parseRecordOrVariableDeclaration();
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
	public NodeIndex parseLabeledStatement(int labelStart)
	{
		parseStatement();
		int end = parser.previousToken().end();
		return parser.getArena().allocateNode(NodeType.LABELED_STATEMENT, labelStart, end);
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
	public boolean isLocalTypeDeclarationStart()
	{
		int checkpoint = parser.getPosition();
		// Skip modifiers and annotations
		while (parser.isModifier(parser.currentToken().type()) ||
			parser.currentToken().type() == TokenType.AT_SIGN ||
			parser.currentToken().type() == TokenType.SEALED ||
			parser.currentToken().type() == TokenType.NON_SEALED)
		{
			if (parser.currentToken().type() == TokenType.AT_SIGN)
			{
				parser.consume();
				parser.parseQualifiedName();
				if (parser.match(TokenType.LEFT_PARENTHESIS))
					skipBalancedParens();
			}
			else
				parser.consume();
		}
		boolean result = switch (parser.currentToken().type())
		{
			case CLASS, INTERFACE, ENUM, RECORD -> true;
			default -> false;
		};
		parser.setPosition(checkpoint);
		return result;
	}

	/**
	 * Parses a local type declaration (class, interface, enum, or record).
	 * <p>
	 * Local types are type declarations that appear inside method bodies, constructors,
	 * or initializer blocks. This method handles modifiers (such as {@code final},
	 * {@code abstract}, {@code sealed}, and annotations) that may precede the type keyword,
	 * then delegates to {@link ParserAccess#parseNestedTypeDeclaration()}.
	 */
	public void parseLocalTypeDeclaration()
	{
		parser.skipMemberModifiers();
		parser.parseNestedTypeDeclaration();
	}

	/**
	 * Disambiguates between a record type declaration and a variable declaration using
	 * {@code record} as the type name.
	 * <p>
	 * A record type declaration has the form: {@code record Name(components...) {...}}
	 * A variable declaration has the form: {@code record name = value;} or {@code record a, b;}
	 * <p>
	 * Uses lookahead to check if the identifier following {@code record} is followed by
	 * {@code (} (record type declaration) or {@code =}, {@code ,}, or {@code ;} (variable declaration).
	 */
	public void parseRecordOrVariableDeclaration()
	{
		int checkpoint = parser.getPosition();
		parser.consume(); // RECORD keyword
		if (parser.isIdentifierOrContextualKeyword())
		{
			parser.consume(); // identifier
			TokenType next = parser.currentToken().type();
			parser.setPosition(checkpoint);
			if (next == TokenType.LEFT_PARENTHESIS)
				parseLocalTypeDeclaration();
			else
				parseExpressionOrVariableStatement();
		}
		else
		{
			// `record` not followed by identifier - could be:
			// 1. Expression using `record` as variable: record.method() or record[i]
			// 2. Malformed record declaration (will produce an error from parseExpressionOrVariableStatement)
			TokenType next = parser.currentToken().type();
			parser.setPosition(checkpoint);
			if (next == TokenType.DOT || next == TokenType.LEFT_BRACKET ||
				next == TokenType.LEFT_PARENTHESIS)
				// Treat `record` as a variable name in an expression
				parseExpressionOrVariableStatement();
			else
				parseLocalTypeDeclaration();
		}
	}

	/**
	 * Skips tokens until matching closing parenthesis is found.
	 * <p>
	 * Used for lookahead when skipping annotation arguments. Assumes the opening
	 * parenthesis has already been consumed. Counts parenthesis depth and consumes
	 * tokens until the depth returns to zero.
	 */
	public void skipBalancedParens()
	{
		int depth = 1;
		while (depth > 0 && parser.currentToken().type() != TokenType.END_OF_FILE)
		{
			if (parser.match(TokenType.LEFT_PARENTHESIS))
				++depth;
			else if (parser.match(TokenType.RIGHT_PARENTHESIS))
				--depth;
			else
				parser.consume();
		}
	}

	/**
	 * Parses a break statement.
	 *
	 * @return the break statement node index
	 */
	public NodeIndex parseBreakStatement()
	{
		int start = parser.currentToken().start();
		parser.consume();
		if (parser.currentToken().type() == TokenType.IDENTIFIER)
			parser.consume();
		parser.expect(TokenType.SEMICOLON);
		int end = parser.previousToken().end();
		return parser.getArena().allocateNode(NodeType.BREAK_STATEMENT, start, end);
	}

	/**
	 * Parses a continue statement.
	 *
	 * @return the continue statement node index
	 */
	public NodeIndex parseContinueStatement()
	{
		int start = parser.currentToken().start();
		parser.consume();
		if (parser.currentToken().type() == TokenType.IDENTIFIER)
			parser.consume();
		parser.expect(TokenType.SEMICOLON);
		int end = parser.previousToken().end();
		return parser.getArena().allocateNode(NodeType.CONTINUE_STATEMENT, start, end);
	}

	/**
	 * Parses an if statement with optional else clause.
	 *
	 * @return the if statement node index
	 */
	public NodeIndex parseIfStatement()
	{
		int start = parser.currentToken().start();
		parser.expect(TokenType.IF);
		// Handle comments after 'if' keyword
		parser.parseComments();
		parser.expect(TokenType.LEFT_PARENTHESIS);
		parser.parseExpression();
		parser.expect(TokenType.RIGHT_PARENTHESIS);
		// Handle comments after condition
		parser.parseComments();
		parseStatement();
		// Handle comments between statement and else
		parser.parseComments();
		if (parser.match(TokenType.ELSE))
		{
			// Handle comments after 'else' keyword
			parser.parseComments();
			parseStatement();
		}
		int end = parser.previousToken().end();
		return parser.getArena().allocateNode(NodeType.IF_STATEMENT, start, end);
	}

	/**
	 * Attempts to parse an enhanced for-loop header (type identifier : expression).
	 *
	 * @return {@code true} if enhanced for-loop header was successfully parsed
	 */
	public boolean tryParseEnhancedForHeader()
	{
		try
		{
			if (!looksLikeTypeStart())
				return false;
			// Consume declaration annotations (before FINAL)
			while (parser.currentToken().type() == TokenType.AT_SIGN)
				parser.parseAnnotation();
			// Consume FINAL modifier if present
			if (parser.currentToken().type() == TokenType.FINAL)
				parser.consume();
			parser.parseType();
			if (!parser.isIdentifierOrContextualKeyword())
				return false;
			parser.consume();
			return parser.match(TokenType.COLON);
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
	public boolean looksLikeTypeStart()
	{
		TokenType type = parser.currentToken().type();
		return type == TokenType.AT_SIGN || type == TokenType.FINAL || parser.isPrimitiveType(type) ||
			type == TokenType.IDENTIFIER || parser.isContextualKeyword(type);
	}

	/**
	 * Parses a for statement (traditional or enhanced).
	 *
	 * @return the for statement node index
	 */
	public NodeIndex parseForStatement()
	{
		int start = parser.currentToken().start();
		parser.expect(TokenType.FOR);
		// Handle comments after 'for' keyword
		parser.parseComments();
		parser.expect(TokenType.LEFT_PARENTHESIS);
		// Handle comments after opening parenthesis
		parser.parseComments();

		// Enhanced for or regular for
		int checkpoint = parser.getPosition();
		boolean isEnhanced = tryParseEnhancedForHeader();

		if (isEnhanced)
		{
			parser.parseExpression();
			parser.expect(TokenType.RIGHT_PARENTHESIS);
			parseStatement();
			int end = parser.previousToken().end();
			return parser.getArena().allocateNode(NodeType.ENHANCED_FOR_STATEMENT, start, end);
		}
		parser.setPosition(checkpoint);
		// Regular for
		if (!parser.match(TokenType.SEMICOLON))
			parseExpressionOrVariableStatement();
		if (!parser.match(TokenType.SEMICOLON))
		{
			parser.parseExpression();
			parser.expect(TokenType.SEMICOLON);
		}
		if (parser.currentToken().type() != TokenType.RIGHT_PARENTHESIS)
		{
			parser.parseExpression();
			while (parser.match(TokenType.COMMA))
				parser.parseExpression();
		}
		parser.expect(TokenType.RIGHT_PARENTHESIS);
		parseStatement();
		int end = parser.previousToken().end();
		return parser.getArena().allocateNode(NodeType.FOR_STATEMENT, start, end);
	}

	/**
	 * Parses a while statement.
	 *
	 * @return the while statement node index
	 */
	public NodeIndex parseWhileStatement()
	{
		int start = parser.currentToken().start();
		parser.expect(TokenType.WHILE);
		// Handle comments after 'while' keyword
		parser.parseComments();
		parser.expect(TokenType.LEFT_PARENTHESIS);
		parser.parseExpression();
		parser.expect(TokenType.RIGHT_PARENTHESIS);
		// Handle comments after condition
		parser.parseComments();
		parseStatement();
		int end = parser.previousToken().end();
		return parser.getArena().allocateNode(NodeType.WHILE_STATEMENT, start, end);
	}

	/**
	 * Parses a do-while statement.
	 *
	 * @return the do-while statement node index
	 */
	public NodeIndex parseDoWhileStatement()
	{
		int start = parser.currentToken().start();
		parser.expect(TokenType.DO);
		parseStatement();
		parser.expect(TokenType.WHILE);
		parser.expect(TokenType.LEFT_PARENTHESIS);
		parser.parseExpression();
		parser.expect(TokenType.RIGHT_PARENTHESIS);
		parser.expect(TokenType.SEMICOLON);
		int end = parser.previousToken().end();
		return parser.getArena().allocateNode(NodeType.DO_WHILE_STATEMENT, start, end);
	}

	/**
	 * Parses a switch statement.
	 *
	 * @return the switch statement node index
	 */
	public NodeIndex parseSwitchStatement()
	{
		int start = parser.currentToken().start();
		parser.expect(TokenType.SWITCH);
		parser.expect(TokenType.LEFT_PARENTHESIS);
		parser.parseExpression();
		parser.expect(TokenType.RIGHT_PARENTHESIS);
		parser.expect(TokenType.LEFT_BRACE);
		// Handle comments after opening brace
		parser.parseComments();
		while (parser.currentToken().type() == TokenType.CASE ||
			parser.currentToken().type() == TokenType.DEFAULT)
		{
			if (parser.match(TokenType.CASE))
			{
				// Parse first case label element
				parseCaseLabelElement();
				// Handle multiple case labels: case 1, 2, 3 -> or case 'L', 'l':
				while (parser.match(TokenType.COMMA))
					parseCaseLabelElement();
			}
			else
				parser.consume(); // DEFAULT

			if (parser.match(TokenType.ARROW))
			{
				// Consume comments between arrow and body
				parser.parseComments();
				// Arrow case: case 1 -> expr; or case 1 -> { ... }
				if (parser.currentToken().type() == TokenType.LEFT_BRACE)
					parser.parseBlock();
				else if (parser.currentToken().type() == TokenType.THROW)
				{
					parser.consume();
					parser.parseExpression();
					parser.expect(TokenType.SEMICOLON);
				}
				else
					parseStatement();
			}
			else
			{
				// Colon case (traditional): case 1:
				parser.expect(TokenType.COLON);
				parser.parseColonCaseBody();
			}
			// Handle comments between case/default labels
			parser.parseComments();
		}
		parser.expect(TokenType.RIGHT_BRACE);
		int end = parser.previousToken().end();
		return parser.getArena().allocateNode(NodeType.SWITCH_STATEMENT, start, end);
	}

	/**
	 * Parses a single case label element in a switch statement or expression.
	 * Handles:
	 * <ul>
	 *   <li>Constant expressions: {@code case 1}, {@code case 'L'}, {@code case FOO}</li>
	 *   <li>Type patterns: {@code case String s}, {@code case Foo.Bar bar}, {@code case Type _}</li>
	 *   <li>Primitive type patterns (JEP 507): {@code case int i}, {@code case double d}</li>
	 *   <li>{@code null} keyword: {@code case null}</li>
	 *   <li>{@code default} keyword: {@code case null, default}</li>
	 * </ul>
	 * Type patterns are distinguished from constant expressions by checking if an identifier
	 * (potentially qualified) is followed by another identifier or underscore.
	 */
	public void parseCaseLabelElement()
	{
		// Handle special keywords that can appear as case labels
		if (parser.match(TokenType.NULL_LITERAL))
			return;
		if (parser.match(TokenType.DEFAULT))
			return;

		// Try to detect primitive type pattern (JEP 507): int i, double d, etc.
		if (tryParsePrimitiveTypePattern())
			return;

		// Try to detect reference type pattern: Type identifier or Type _
		// Type patterns look like: String s, Foo.Bar bar, Integer _, etc.
		if (parser.currentToken().type() == TokenType.IDENTIFIER && tryParseTypePattern())
			return;

		// Parse as case label expression (no lambda lookahead)
		parseCaseLabelExpression();
	}

	/**
	 * Parses an expression in the context of a case label.
	 * <p>
	 * This method is similar to {@link ParserAccess#parseExpression()} but does NOT apply lambda lookahead.
	 * In case labels, the pattern {@code identifier ->} always represents a constant reference
	 * followed by the case arrow, never a lambda expression.
	 */
	public void parseCaseLabelExpression()
	{
		// Use parseLogicalOr() to avoid treating COLON as ternary operator.
		// Ternary expressions are invalid in case labels anyway (would need second COLON
		// that conflicts with the case label terminator).
		parser.parseLogicalOr();
	}

	/**
	 * Attempts to parse a primitive type pattern in a case label.
	 * Primitive type patterns have the form: {@code primitiveType patternVariable}
	 * <p>
	 * Examples:
	 * <ul>
	 *   <li>{@code case int i ->}</li>
	 *   <li>{@code case double d when d > 0 ->}</li>
	 *   <li>{@code case long _}</li>
	 * </ul>
	 *
	 * @return {@code true} if a primitive type pattern was parsed, {@code false} otherwise
	 */
	public boolean tryParsePrimitiveTypePattern()
	{
		if (!parser.isPrimitiveType(parser.currentToken().type()))
			return false;

		int checkpoint = parser.getPosition();
		parser.consume(); // primitive type keyword

		// Check if followed by identifier (pattern variable)
		if (parser.currentToken().type() != TokenType.IDENTIFIER)
		{
			// Not a type pattern, restore position
			parser.setPosition(checkpoint);
			return false;
		}

		parser.consume(); // pattern variable

		// Check for optional guard: "when" expression
		if (isContextualKeyword("when"))
			parseGuardExpression();
		return true;
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
	public boolean tryParseTypePattern()
	{
		int checkpoint = parser.getPosition();
		int typeStart = parser.currentToken().start();

		// Parse potential type (may be qualified like Foo.Bar.Baz)
		parser.consume(); // First identifier
		while (parser.match(TokenType.DOT))
		{
			if (parser.currentToken().type() != TokenType.IDENTIFIER)
			{
				// Not a qualified name, restore position
				parser.setPosition(checkpoint);
				return false;
			}
			parser.consume();
		}

		// Check if this is a record pattern: Type(components...)
		if (parser.currentToken().type() == TokenType.LEFT_PARENTHESIS)
		{
			parseRecordPattern(typeStart);
			return true;
		}

		// Check if next token is an identifier (pattern variable)
		// This includes both named variables (s, bar) and unnamed pattern (_)
		if (parser.currentToken().type() == TokenType.IDENTIFIER)
		{
			parser.consume();
			// Check for optional guard: "when" expression
			if (isContextualKeyword("when"))
				parseGuardExpression();
			return true;
		}

		// Not a type pattern, restore position
		parser.setPosition(checkpoint);
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
	public NodeIndex parseRecordPattern(int typeStart)
	{
		parser.expect(TokenType.LEFT_PARENTHESIS);
		parseRecordPatternComponents();
		parser.expect(TokenType.RIGHT_PARENTHESIS);

		// Check for optional guard: "when" expression
		if (isContextualKeyword("when"))
			parseGuardExpression();

		int end = parser.previousToken().end();
		return parser.getArena().allocateNode(NodeType.RECORD_PATTERN, typeStart, end);
	}

	/**
	 * Parses the component patterns inside a record pattern's parentheses.
	 * Handles empty component lists and comma-separated component patterns.
	 */
	public void parseRecordPatternComponents()
	{
		// Handle empty component list: Empty()
		if (parser.currentToken().type() == TokenType.RIGHT_PARENTHESIS)
			return;

		parseComponentPattern();
		while (parser.match(TokenType.COMMA))
			parseComponentPattern();
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
	public void parseComponentPattern()
	{
		// Check for unnamed pattern: _
		if (parser.currentToken().type() == TokenType.IDENTIFIER &&
			"_".equals(parser.currentToken().decodedText()))
		{
			parser.consume();
			return;
		}

		// Parse type (may be primitive, var, or qualified reference type)
		int componentTypeStart = parser.currentToken().start();
		if (parser.isPrimitiveType(parser.currentToken().type()))
			parser.consume();
		else if (parser.currentToken().type() == TokenType.VAR)
			// Type inference with 'var' keyword
			parser.consume();
		else if (parser.isIdentifierOrContextualKeyword())
		{
			parser.consume();
			while (parser.match(TokenType.DOT))
			{
				if (!parser.isIdentifierOrContextualKeyword())
				{
					throw new ParserException(
						"Expected identifier after '.' in type", parser.currentToken().start());
				}
				parser.consume();
			}
		}
		else
		{
			throw new ParserException(
				"Expected type in component pattern", parser.currentToken().start());
		}

		parser.parseArrayDimensionsWithAnnotations();

		// Determine what follows the type:
		// - LEFT_PARENTHESIS -> nested record pattern
		// - IDENTIFIER -> type pattern with variable name
		if (parser.currentToken().type() == TokenType.LEFT_PARENTHESIS)
			// Nested record pattern
			parseRecordPattern(componentTypeStart);
		else if (parser.isIdentifierOrContextualKeyword())
			// Type pattern: consume the variable name
			parser.consume();
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
		return parser.currentToken().type() == TokenType.IDENTIFIER &&
			parser.currentToken().decodedText().equals(keyword);
	}

	/**
	 * Parses a guard expression following the "when" contextual keyword in a guarded pattern.
	 * The "when" keyword must have already been detected via {@link #isContextualKeyword(String)}.
	 * <p>
	 * Example: {@code case String s when s.length() > 5 -> ...}
	 */
	public void parseGuardExpression()
	{
		// Consume the "when" contextual keyword
		parser.consume();
		// Parse the guard condition expression
		parser.parseExpression();
	}

	/**
	 * Parses a return statement.
	 *
	 * @return the return statement node index
	 */
	public NodeIndex parseReturnStatement()
	{
		int start = parser.currentToken().start();
		parser.expect(TokenType.RETURN);
		if (parser.currentToken().type() != TokenType.SEMICOLON)
			parser.parseExpression();
		parser.expect(TokenType.SEMICOLON);
		int end = parser.previousToken().end();
		return parser.getArena().allocateNode(NodeType.RETURN_STATEMENT, start, end);
	}

	/**
	 * Parses a throw statement.
	 *
	 * @return the throw statement node index
	 */
	public NodeIndex parseThrowStatement()
	{
		int start = parser.currentToken().start();
		parser.expect(TokenType.THROW);
		parser.parseExpression();
		parser.expect(TokenType.SEMICOLON);
		int end = parser.previousToken().end();
		return parser.getArena().allocateNode(NodeType.THROW_STATEMENT, start, end);
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
	public NodeIndex parseYieldStatement()
	{
		int start = parser.currentToken().start();
		parser.expect(TokenType.YIELD);
		parser.parseExpression();
		parser.expect(TokenType.SEMICOLON);
		int end = parser.previousToken().end();
		return parser.getArena().allocateNode(NodeType.YIELD_STATEMENT, start, end);
	}

	/**
	 * Parses a try statement with optional try-with-resources, catch clauses, and finally clause.
	 * <p>
	 * Grammar:
	 * <pre>
	 * TryStatement:
	 *     try Block Catches
	 *     try Block [Catches] Finally
	 *     try ResourceSpecification Block [Catches] [Finally]
	 * </pre>
	 *
	 * @return the try statement node index
	 */
	public NodeIndex parseTryStatement()
	{
		int start = parser.currentToken().start();
		parser.expect(TokenType.TRY);

		// Try-with-resources
		if (parser.match(TokenType.LEFT_PARENTHESIS))
		{
			parseResource();
			while (parser.match(TokenType.SEMICOLON))
				if (parser.currentToken().type() != TokenType.RIGHT_PARENTHESIS)
					parseResource();
			parser.expect(TokenType.RIGHT_PARENTHESIS);
		}

		parser.parseBlock();

		// Handle comments before catch clauses
		parser.parseComments();
		// Catch clauses
		while (parser.currentToken().type() == TokenType.CATCH)
		{
			parseCatchClause();
			// Handle comments between catch/finally clauses
			parser.parseComments();
		}

		// Finally clause
		if (parser.currentToken().type() == TokenType.FINALLY)
			parseFinallyClause();

		int end = parser.previousToken().end();
		return parser.getArena().allocateNode(NodeType.TRY_STATEMENT, start, end);
	}

	/**
	 * Parses a synchronized statement.
	 *
	 * @return the synchronized statement node index
	 */
	public NodeIndex parseSynchronizedStatement()
	{
		int start = parser.currentToken().start();
		parser.expect(TokenType.SYNCHRONIZED);
		parser.expect(TokenType.LEFT_PARENTHESIS);
		parser.parseExpression();
		parser.expect(TokenType.RIGHT_PARENTHESIS);
		parser.parseBlock();
		int end = parser.previousToken().end();
		return parser.getArena().allocateNode(NodeType.SYNCHRONIZED_STATEMENT, start, end);
	}

	/**
	 * Parses an assert statement.
	 *
	 * @return the assert statement node index
	 */
	public NodeIndex parseAssertStatement()
	{
		int start = parser.currentToken().start();
		parser.expect(TokenType.ASSERT);
		parser.parseExpression();
		if (parser.match(TokenType.COLON))
			parser.parseExpression();
		parser.expect(TokenType.SEMICOLON);
		int end = parser.previousToken().end();
		return parser.getArena().allocateNode(NodeType.ASSERT_STATEMENT, start, end);
	}

	/**
	 * Attempts to parse a variable declaration. Backtracks to checkpoint on failure.
	 *
	 * @param checkpoint the position to backtrack to on failure
	 * @return {@code true} if successfully parsed a variable declaration
	 */
	public boolean tryParseVariableDeclaration(int checkpoint)
	{
		try
		{
			// Consume declaration annotations (before FINAL modifier)
			while (parser.currentToken().type() == TokenType.AT_SIGN)
				parser.parseAnnotation();
			// Consume optional FINAL modifier
			if (parser.currentToken().type() == TokenType.FINAL)
				parser.consume();
			parser.parseType();
			if (!parser.isIdentifierOrContextualKeyword())
			{
				parser.setPosition(checkpoint);
				return false;
			}
			parser.consume();
			parser.parseArrayDimensionsWithAnnotations();
			if (parser.match(TokenType.ASSIGN))
				parser.parseExpression();
			parseAdditionalDeclarators();
			parser.expect(TokenType.SEMICOLON);
			return true;
		}
		catch (ParserException e)
		{
			parser.setPosition(checkpoint);
			return false;
		}
	}

	/**
	 * Parses additional variable declarators after the first one (comma-separated).
	 */
	public void parseAdditionalDeclarators()
	{
		while (parser.match(TokenType.COMMA))
		{
			parser.expectIdentifierOrContextualKeyword();
			parser.parseArrayDimensionsWithAnnotations();
			if (parser.match(TokenType.ASSIGN))
				parser.parseExpression();
		}
	}

	/**
	 * Parses either an expression statement or a variable declaration statement.
	 */
	public void parseExpressionOrVariableStatement()
	{
		int checkpoint = parser.getPosition();

		// Try to parse as variable declaration
		if ((parser.currentToken().type() == TokenType.AT_SIGN ||
			parser.currentToken().type() == TokenType.FINAL ||
			parser.currentToken().type() == TokenType.VAR ||
			parser.currentToken().type() == TokenType.RECORD ||
			parser.isPrimitiveType(parser.currentToken().type()) ||
			parser.currentToken().type() == TokenType.IDENTIFIER) &&
			tryParseVariableDeclaration(checkpoint))
			return;

		// Parse as expression statement
		parser.parseExpression();
		parser.expect(TokenType.SEMICOLON);
	}

	// ========== Try-Catch-Finally Support Methods ==========

	/**
	 * Parses a catch clause within a try statement.
	 * <p>
	 * Grammar:
	 * <pre>
	 * CatchClause:
	 *     catch ( CatchFormalParameter ) Block
	 * </pre>
	 *
	 * @return the catch clause node index
	 */
	public NodeIndex parseCatchClause()
	{
		int start = parser.currentToken().start();
		parser.expect(TokenType.CATCH);
		parser.expect(TokenType.LEFT_PARENTHESIS);
		// Handle comments before catch parameter
		parser.parseComments();
		parser.parseCatchParameter();
		// Handle comments after catch parameter
		parser.parseComments();
		parser.expect(TokenType.RIGHT_PARENTHESIS);
		parser.parseBlock();
		int end = parser.previousToken().end();
		return parser.getArena().allocateNode(NodeType.CATCH_CLAUSE, start, end);
	}

	/**
	 * Parses a finally clause within a try statement.
	 * <p>
	 * Grammar:
	 * <pre>
	 * Finally:
	 *     finally Block
	 * </pre>
	 *
	 * @return the finally clause node index
	 */
	public NodeIndex parseFinallyClause()
	{
		int start = parser.currentToken().start();
		parser.expect(TokenType.FINALLY);
		parser.parseBlock();
		int end = parser.previousToken().end();
		return parser.getArena().allocateNode(NodeType.FINALLY_CLAUSE, start, end);
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
	public void parseResource()
	{
		// Consume declaration annotations (e.g., @Cleanup)
		while (parser.currentToken().type() == TokenType.AT_SIGN)
			parser.parseAnnotation();

		if (isResourceVariableReference())
			parseResourceVariableReference();
		else
			parseResourceDeclaration();
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
	boolean isResourceVariableReference()
	{
		// Field access: this.resource or Outer.this.resource
		if (parser.currentToken().type() == TokenType.THIS)
			return true;

		// Simple identifier followed by ; or ) indicates variable reference
		// Note: qualified names like java.io.Reader would be followed by IDENTIFIER (variable name)
		if (parser.currentToken().type() == TokenType.IDENTIFIER)
		{
			int checkpoint = parser.getPosition();
			parser.consume();
			TokenType nextType = parser.currentToken().type();
			parser.setPosition(checkpoint);
			return nextType == TokenType.SEMICOLON || nextType == TokenType.RIGHT_PARENTHESIS;
		}

		return false;
	}

	/**
	 * Parses a variable reference in try-with-resources (JDK 9+).
	 * <p>
	 * Handles simple identifiers ({@code reader}) and field access expressions
	 * ({@code this.resource}, {@code Outer.this.resource}).
	 */
	void parseResourceVariableReference()
	{
		// Parse as expression - will naturally produce IDENTIFIER or FIELD_ACCESS node
		parser.parseExpression();
	}

	/**
	 * Parses a resource declaration in try-with-resources (JDK 7+ traditional syntax).
	 * <p>
	 * Syntax: {@code [final] Type variableName = expression}
	 */
	void parseResourceDeclaration()
	{
		// Optional FINAL modifier
		if (parser.currentToken().type() == TokenType.FINAL)
			parser.consume();
		parser.parseType();
		parser.expectIdentifierOrContextualKeyword();
		parser.expect(TokenType.ASSIGN);
		parser.parseExpression();
	}
}
