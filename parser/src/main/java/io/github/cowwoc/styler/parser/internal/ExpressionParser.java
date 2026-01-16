package io.github.cowwoc.styler.parser.internal;

import io.github.cowwoc.styler.ast.core.NodeArena;
import io.github.cowwoc.styler.ast.core.NodeIndex;
import io.github.cowwoc.styler.ast.core.NodeType;
import io.github.cowwoc.styler.parser.Parser.ParserException;
import io.github.cowwoc.styler.parser.Token;
import io.github.cowwoc.styler.parser.TokenType;

import java.util.List;
import java.util.function.Supplier;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.that;

/**
 * Helper class for expression parsing.
 * <p>
 * Extracted from Parser to reduce class size while maintaining cohesive parsing logic.
 * Contains methods for:
 * <ul>
 *   <li>Binary expression parsing with operator precedence</li>
 *   <li>Cast and lambda expression disambiguation</li>
 * </ul>
 */
public final class ExpressionParser
{
	private final ParserAccess parser;

	/**
	 * Creates a new expression parser that delegates to the given parser.
	 *
	 * @param parser the parent parser providing token access and helper methods
	 */
	public ExpressionParser(ParserAccess parser)
	{
		assert that(parser, "parser").isNotNull().elseThrow();
		this.parser = parser;
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
	public boolean canStartUnaryExpressionNotPlusMinus(TokenType type)
	{
		return switch (type)
		{
			case IDENTIFIER, INTEGER_LITERAL, LONG_LITERAL, FLOAT_LITERAL, DOUBLE_LITERAL, BOOLEAN_LITERAL,
				CHAR_LITERAL, STRING_LITERAL, NULL_LITERAL, THIS, SUPER, NEW, LEFT_PARENTHESIS, NOT, TILDE,
				INCREMENT, DECREMENT, SWITCH, BOOLEAN, BYTE, SHORT, INT, LONG, CHAR, FLOAT, DOUBLE, VOID -> true;
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
	public boolean canStartUnaryExpression(TokenType type)
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
	 * @param start                  the start position of the opening parenthesis
	 * @param parseUnaryFunction     function to parse unary expressions
	 * @param parseCastOperandHelper function to parse lambda body when cast operand is a lambda
	 * @return the {@link NodeIndex} of the cast expression if successful, or {@code null} if this
	 *         is not a cast expression (caller should continue with parenthesized/lambda parsing)
	 */
	public NodeIndex tryCastExpression(int start, java.util.function.Supplier<NodeIndex> parseUnaryFunction,
		java.util.function.Function<Integer, NodeIndex> parseCastOperandHelper)
	{
		// Save checkpoint after '(' is consumed
		int checkpoint = parser.getPosition();

		// Determine if type starts with primitive
		boolean isPrimitive = parser.isPrimitiveType(parser.currentToken().type());

		// Track whether we've seen intersection types (&)
		boolean isIntersectionType = false;

		// Try to parse the type
		try
		{
			// Parse annotations before type (e.g., (@NonNull String) value)
			while (parser.currentToken().type() == TokenType.AT_SIGN)
				parser.parseAnnotation();

			if (isPrimitive)
				parser.consume();
			else if (parser.isIdentifierOrContextualKeyword())
			{
				// Parse qualified name (e.g., java.lang.String)
				parser.consume();
				while (parser.match(TokenType.DOT))
				{
					if (!parser.isIdentifierOrContextualKeyword())
					{
						// Not a valid qualified name, restore and return null
						parser.setPosition(checkpoint);
						return null;
					}
					parser.consume();
				}

				// Parse type arguments (e.g., List<String>)
				if (parser.match(TokenType.LESS_THAN))
					parser.parseTypeArguments();

				// Parse intersection types (e.g., Serializable & Comparable)
				while (parser.match(TokenType.BITWISE_AND))
				{
					isIntersectionType = true;
					// Parse annotations before intersection type component
					while (parser.currentToken().type() == TokenType.AT_SIGN)
						parser.parseAnnotation();
					if (!parser.isIdentifierOrContextualKeyword())
					{
						// Not a valid intersection type, restore and return null
						parser.setPosition(checkpoint);
						return null;
					}
					parser.consume();
					while (parser.match(TokenType.DOT))
					{
						if (!parser.isIdentifierOrContextualKeyword())
						{
							parser.setPosition(checkpoint);
							return null;
						}
						parser.consume();
					}
					if (parser.match(TokenType.LESS_THAN))
						parser.parseTypeArguments();
				}
			}
			else
			{
				// Not a valid type start, restore and return null
				parser.setPosition(checkpoint);
				return null;
			}

			parser.parseArrayDimensionsWithAnnotations();
		}
		catch (ParserException e)
		{
			// Type parsing failed, restore and return null
			parser.setPosition(checkpoint);
			return null;
		}

		// Check for closing parenthesis
		if (parser.currentToken().type() != TokenType.RIGHT_PARENTHESIS)
		{
			// Not a cast (could be expression like (a + b))
			parser.setPosition(checkpoint);
			return null;
		}
		parser.consume(); // Consume ')'

		// Check disambiguation rules based on next token
		TokenType nextTokenType = parser.currentToken().type();
		boolean validCast;
		if (isPrimitive && !isIntersectionType)
			// Primitive type cast: can be followed by any unary operand
			validCast = canStartUnaryExpression(nextTokenType);
		else
			// Reference type cast (or intersection type): cannot be followed by + or -
			// to avoid ambiguity with binary expressions like (a) + b
			validCast = canStartUnaryExpressionNotPlusMinus(nextTokenType);

		if (!validCast)
		{
			// This is not a cast expression, restore position
			parser.setPosition(checkpoint);
			return null;
		}

		// This is a valid cast - parse the operand
		NodeIndex operand = parseCastOperand(nextTokenType, parseUnaryFunction, parseCastOperandHelper);
		int end = parser.getArena().getEnd(operand);
		return parser.getArena().allocateNode(NodeType.CAST_EXPRESSION, start, end);
	}

	/**
	 * Parses the operand of a cast expression.
	 * <p>
	 * Handles the special case where the operand is a single-parameter lambda expression
	 * ({@code identifier -> body}). Without this handling, the parser would fail on expressions like
	 * {@code (FunctionalInterface) x -> body} because it would parse {@code x} as the operand and then
	 * fail when encountering the arrow.
	 *
	 * @param nextTokenType          the type of the token following the cast's closing parenthesis
	 * @param parseUnaryFunction     function to parse unary expressions
	 * @param parseCastOperandHelper function to parse lambda body when cast operand is a lambda
	 * @return the parsed operand node
	 */
	public NodeIndex parseCastOperand(TokenType nextTokenType,
		java.util.function.Supplier<NodeIndex> parseUnaryFunction,
		java.util.function.Function<Integer, NodeIndex> parseCastOperandHelper)
	{
		// Check for lambda expression: identifier -> body
		// When a cast is followed by identifier + arrow, the operand is a lambda expression
		if (nextTokenType == TokenType.IDENTIFIER && lookaheadIsArrow())
		{
			int lambdaStart = parser.currentToken().start();
			parser.consume();
			parser.expect(TokenType.ARROW);
			return parseCastOperandHelper.apply(lambdaStart);
		}
		// Regular cast operand: not an identifier, or identifier not followed by arrow
		// Examples: (String) obj, (int) getValue(), (Type) x + y
		return parseUnaryFunction.get();
	}

	/**
	 * Checks if the token after the current position is an arrow ({@code ->}).
	 * Used for disambiguating lambda expressions from other expressions.
	 *
	 * @return {@code true} if the next token is {@code ->}
	 */
	public boolean lookaheadIsArrow()
	{
		List<Token> tokens = parser.getTokens();
		int position = parser.getPosition();
		return position + 1 < tokens.size() && tokens.get(position + 1).type() == TokenType.ARROW;
	}

	/**
	 * Checks if the current position starts a lambda expression by scanning for the {@code ) ->} pattern.
	 * Handles nested parentheses, generic type arguments, and array types.
	 * Called after the opening {@code (} has been consumed.
	 *
	 * @return {@code true} if a {@code ) ->} pattern is found with balanced parentheses and generics
	 */
	public boolean isLambdaExpression()
	{
		List<Token> tokens = parser.getTokens();
		int savedPosition = parser.getPosition();
		try
		{
			int parenthesisDepth = 1;
			int angleBracketDepth = 0;

			while (parser.getPosition() < tokens.size() && parenthesisDepth > 0)
			{
				TokenType type = parser.currentToken().type();

				switch (type)
				{
					case LEFT_PARENTHESIS -> ++parenthesisDepth;
					case RIGHT_PARENTHESIS ->
					{
						if (angleBracketDepth == 0)
							--parenthesisDepth;
					}
					case LESS_THAN -> ++angleBracketDepth;
					case GREATER_THAN ->
					{
						if (angleBracketDepth > 0)
							--angleBracketDepth;
					}
					case RIGHT_SHIFT ->
					{
						if (angleBracketDepth >= 2)
							angleBracketDepth -= 2;
						else if (angleBracketDepth == 1)
							angleBracketDepth = 0;
					}
					case UNSIGNED_RIGHT_SHIFT ->
					{
						if (angleBracketDepth >= 3)
							angleBracketDepth -= 3;
						else
							angleBracketDepth = 0;
					}
					case SEMICOLON, LEFT_BRACE, RIGHT_BRACE ->
					{
						return false;
					}
					default ->
					{
						// Continue scanning
					}
				}

				if (parenthesisDepth > 0)
					parser.consume();
			}

			if (parenthesisDepth == 0 && parser.getPosition() + 1 < tokens.size())
				return tokens.get(parser.getPosition() + 1).type() == TokenType.ARROW;

			return false;
		}
		finally
		{
			parser.setPosition(savedPosition);
		}
	}

	/**
	 * Parses an expression.
	 *
	 * @return the expression node index
	 */
	public NodeIndex parseExpression()
	{
		// Check for lambda expression: identifier -> expr
		if (parser.currentToken().type() == TokenType.IDENTIFIER)
		{
			// Look ahead to see if this is a lambda
			int checkpoint = parser.getPosition();
			int start = parser.currentToken().start();
			parser.consume();

			if (parser.match(TokenType.ARROW))
				// This is a lambda expression: x -> body
				return parseLambdaBody(start);

			// Not a lambda, restore position
			parser.setPosition(checkpoint);
		}

		return parseAssignment();
	}

	/**
	 * Parses the body of a lambda expression after the arrow has been consumed.
	 *
	 * @param start the start position of the lambda expression
	 * @return the lambda expression node index
	 */
	public NodeIndex parseLambdaBody(int start)
	{
		// Handle comments between arrow and body
		parser.parseComments();
		int end;
		if (parser.currentToken().type() == TokenType.LEFT_BRACE)
		{
			// Block lambda: x -> { statements }
			// parseBlock() creates the BLOCK node; we just need the end position
			parser.parseBlock();
			end = parser.previousToken().end();
		}
		else
		{
			// Expression lambda: x -> expr
			NodeIndex body = parseExpression();
			end = parser.getArena().getEnd(body);
		}
		return parser.getArena().allocateNode(NodeType.LAMBDA_EXPRESSION, start, end);
	}

	/**
	 * Parses a lambda expression's parameter list after lookahead confirmed {@code ) ->} pattern.
	 *
	 * @param start the start position (opening parenthesis position)
	 * @return the lambda expression node
	 */
	NodeIndex parseLambdaParameters(int start)
	{
		if (isTypedLambdaParameters())
			return parseTypedLambdaParameters(start);
		return parseUntypedLambdaParameters(start);
	}

	/**
	 * Checks if the lambda parameters are typed (have explicit type declarations).
	 *
	 * @return {@code true} if the parameters have explicit types
	 */
	private boolean isTypedLambdaParameters()
	{
		int savedPosition = parser.getPosition();
		try
		{
			while (parser.currentToken().type() == TokenType.FINAL ||
				parser.currentToken().type() == TokenType.AT_SIGN)
			{
				if (parser.currentToken().type() == TokenType.AT_SIGN)
				{
					parser.consume();
					if (parser.isIdentifierOrContextualKeyword())
						parser.consume();
					while (parser.currentToken().type() == TokenType.DOT)
					{
						parser.consume();
						if (parser.isIdentifierOrContextualKeyword())
							parser.consume();
					}
					if (parser.currentToken().type() == TokenType.LEFT_PARENTHESIS)
						parser.skipBalancedParentheses();
				}
				else
					parser.consume();
			}

			if (parser.isPrimitiveType(parser.currentToken().type()))
				return true;

			if (!parser.isIdentifierOrContextualKeyword())
				return false;

			parser.consume();

			return switch (parser.currentToken().type())
			{
				case LESS_THAN, LEFT_BRACKET, DOT -> true;
				default -> parser.isIdentifierOrContextualKeyword();
			};
		}
		finally
		{
			parser.setPosition(savedPosition);
		}
	}

	private NodeIndex parseTypedLambdaParameters(int start)
	{
		parseTypedLambdaParameter();
		while (parser.match(TokenType.COMMA))
			parseTypedLambdaParameter();
		parser.expect(TokenType.RIGHT_PARENTHESIS);
		parser.expect(TokenType.ARROW);
		return parseLambdaBody(start);
	}

	private void parseTypedLambdaParameter()
	{
		while (parser.currentToken().type() == TokenType.FINAL ||
			parser.currentToken().type() == TokenType.AT_SIGN)
		{
			if (parser.currentToken().type() == TokenType.AT_SIGN)
				parser.parseAnnotation();
			else
				parser.consume();
		}
		parser.parseType();
		parser.match(TokenType.ELLIPSIS);
		parser.expectIdentifierOrContextualKeyword();
		parser.parseArrayDimensionsWithAnnotations();
	}

	private NodeIndex parseUntypedLambdaParameters(int start)
	{
		parser.expectIdentifierOrContextualKeyword();
		while (parser.match(TokenType.COMMA))
			parser.expectIdentifierOrContextualKeyword();
		parser.expect(TokenType.RIGHT_PARENTHESIS);
		parser.expect(TokenType.ARROW);
		return parseLambdaBody(start);
	}

	/**
	 * Parses parenthesized expression, cast expression, or lambda expression.
	 * <p>
	 * Handles:
	 * <ul>
	 *   <li>Empty parens lambda: {@code () -> expr}</li>
	 *   <li>Cast expression: {@code (Type) operand}</li>
	 *   <li>Multi-parameter lambda: {@code (a, b) -> expr}</li>
	 *   <li>Parenthesized lambda: {@code (params) -> expr}</li>
	 *   <li>Parenthesized expression: {@code (expr)}</li>
	 * </ul>
	 *
	 * @param start the start position of the opening paren
	 * @return the parsed node
	 */
	public NodeIndex parseParenthesizedOrLambda(int start)
	{
		// Check for empty parens lambda: () -> expr
		if (parser.match(TokenType.RIGHT_PARENTHESIS))
		{
			parser.expect(TokenType.ARROW);
			return parseLambdaBody(start);
		}

		// Detect ALL lambda forms by scanning for ) -> pattern BEFORE trying cast
		if (isLambdaExpression())
			return parseLambdaParameters(start);

		// Try cast expression with proper delegation
		NodeIndex castExpression = tryCastExpression(start, parser::parseUnary, this::parseLambdaBody);
		if (castExpression != null)
			return castExpression;

		NodeIndex expression = parseExpression();
		parser.expect(TokenType.RIGHT_PARENTHESIS);

		if (parser.match(TokenType.ARROW))
			return parseLambdaBody(start);

		// Regular parenthesized expression
		return expression;
	}

	/**
	 * Parses an assignment expression.
	 *
	 * @return the expression node index
	 */
	public NodeIndex parseAssignment()
	{
		NodeIndex left = parseTernary();

		if (isAssignmentOperator(parser.currentToken().type()))
		{
			parser.consume();
			// Right associative - must check for lambda
			NodeIndex right = parseExpression();

			int start = parser.getArena().getStart(left);
			int end = parser.getArena().getEnd(right);
			return parser.getArena().allocateNode(NodeType.ASSIGNMENT_EXPRESSION, start, end);
		}

		return left;
	}

	private boolean isAssignmentOperator(TokenType type)
	{
		return switch (type)
		{
			case ASSIGN, PLUS_ASSIGN, MINUS_ASSIGN, STAR_ASSIGN, DIVIDE_ASSIGN, MODULO_ASSIGN,
				BITWISE_AND_ASSIGN, BITWISE_OR_ASSIGN, CARET_ASSIGN, LEFT_SHIFT_ASSIGN,
				RIGHT_SHIFT_ASSIGN, UNSIGNED_RIGHT_SHIFT_ASSIGN -> true;
			default -> false;
		};
	}

	/**
	 * Parses a ternary (conditional) expression.
	 *
	 * @return the expression node index
	 */
	public NodeIndex parseTernary()
	{
		// Pending: Replace with parseLogicalOr() after binary-operators task
		NodeIndex condition = parseLogicalOr();

		if (parser.match(TokenType.QUESTION_MARK))
		{
			parseExpression();
			parser.expect(TokenType.COLON);
			// Right associative
			NodeIndex elseExpression = parseTernary();

			int start = parser.getArena().getStart(condition);
			int end = parser.getArena().getEnd(elseExpression);
			return parser.getArena().allocateNode(NodeType.CONDITIONAL_EXPRESSION, start, end);
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
			int start = parser.getArena().getStart(left);
			int end = parser.getArena().getEnd(right);
			left = parser.getArena().allocateNode(NodeType.BINARY_EXPRESSION, start, end);
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
		TokenType current = parser.currentToken().type();
		for (TokenType type : types)
			if (current == type)
			{
				parser.consume();
				return true;
			}
		return false;
	}

	/**
	 * Parses a logical OR expression ({@code ||}).
	 *
	 * @return the expression node index
	 */
	private NodeIndex parseLogicalOr()
	{
		return parseBinaryExpression(this::parseLogicalAnd, TokenType.LOGICAL_OR);
	}

	/**
	 * Parses a logical AND expression ({@code &&}).
	 *
	 * @return the expression node index
	 */
	private NodeIndex parseLogicalAnd()
	{
		return parseBinaryExpression(this::parseBitwiseOr, TokenType.LOGICAL_AND);
	}

	/**
	 * Parses a bitwise OR expression ({@code |}).
	 *
	 * @return the expression node index
	 */
	private NodeIndex parseBitwiseOr()
	{
		return parseBinaryExpression(this::parseBitwiseXor, TokenType.BITWISE_OR);
	}

	/**
	 * Parses a bitwise XOR expression ({@code ^}).
	 *
	 * @return the expression node index
	 */
	private NodeIndex parseBitwiseXor()
	{
		return parseBinaryExpression(this::parseBitwiseAnd, TokenType.CARET);
	}

	/**
	 * Parses a bitwise AND expression ({@code &}).
	 *
	 * @return the expression node index
	 */
	private NodeIndex parseBitwiseAnd()
	{
		return parseBinaryExpression(this::parseEquality, TokenType.BITWISE_AND);
	}

	/**
	 * Parses an equality expression ({@code ==} or {@code !=}).
	 *
	 * @return the expression node index
	 */
	private NodeIndex parseEquality()
	{
		return parseBinaryExpression(this::parseRelational, TokenType.EQUAL, TokenType.NOT_EQUAL);
	}

	/**
	 * Parses a relational expression ({@code <}, {@code >}, {@code <=}, {@code >=}, {@code instanceof}).
	 *
	 * @return the expression node index
	 */
	private NodeIndex parseRelational()
	{
		NodeIndex left = parseShift();

		while (matchesAny(TokenType.LESS_THAN, TokenType.GREATER_THAN, TokenType.LESS_THAN_OR_EQUAL,
			TokenType.GREATER_THAN_OR_EQUAL))
		{
			NodeIndex right = parseShift();
			int start = parser.getArena().getStart(left);
			int end = parser.getArena().getEnd(right);
			left = parser.getArena().allocateNode(NodeType.BINARY_EXPRESSION, start, end);
		}

		// Handle instanceof specially - requires type reference, optionally followed by pattern variable
		if (parser.match(TokenType.INSTANCEOF))
		{
			int start = parser.getArena().getStart(left);
			// Consume optional FINAL modifier (Java 16+ pattern matching with final)
			if (parser.currentToken().type() == TokenType.FINAL)
				parser.consume();
			parser.parseType();

			int end = parser.previousToken().end();
			// Check for optional pattern variable (Java 16+ pattern matching)
			if (parser.currentToken().type() == TokenType.IDENTIFIER)
			{
				parser.consume();
				end = parser.previousToken().end();
			}

			return parser.getArena().allocateNode(NodeType.BINARY_EXPRESSION, start, end);
		}

		return left;
	}

	/**
	 * Parses a shift expression ({@code <<}, {@code >>}, {@code >>>}).
	 *
	 * @return the expression node index
	 */
	private NodeIndex parseShift()
	{
		return parseBinaryExpression(this::parseAdditive,
			TokenType.LEFT_SHIFT, TokenType.RIGHT_SHIFT, TokenType.UNSIGNED_RIGHT_SHIFT);
	}

	/**
	 * Parses an additive expression ({@code +} or {@code -}).
	 *
	 * @return the expression node index
	 */
	private NodeIndex parseAdditive()
	{
		return parseBinaryExpression(this::parseMultiplicative, TokenType.PLUS, TokenType.MINUS);
	}

	/**
	 * Parses a multiplicative expression ({@code *}, {@code /}, or {@code %}).
	 *
	 * @return the expression node index
	 */
	private NodeIndex parseMultiplicative()
	{
		return parseBinaryExpression(this::parseUnary, TokenType.STAR, TokenType.DIVIDE, TokenType.MODULO);
	}

	/**
	 * Parses a unary expression (prefix operators).
	 *
	 * @return the expression node index
	 */
	public NodeIndex parseUnary()
	{
		int start = parser.currentToken().start();

		TokenType type = parser.currentToken().type();
		boolean isUnaryOperator = switch (type)
		{
			case MINUS, PLUS, NOT, TILDE, INCREMENT, DECREMENT -> true;
			default -> false;
		};

		if (isUnaryOperator)
		{
			parser.consume();
			parser.enterDepth();
			NodeIndex operand = parseUnary();
			parser.exitDepth();
			int end = parser.getArena().getEnd(operand);
			return parser.getArena().allocateNode(NodeType.UNARY_EXPRESSION, start, end);
		}

		return parsePostfix();
	}

	private NodeIndex parsePostfix()
	{
		NodeIndex left = parsePrimary();

		while (true)
		{
			parser.parseComments();
			int start = parser.getArena().getStart(left);

			if (parser.match(TokenType.LEFT_PARENTHESIS))
			{
				// Method call
				while (!parser.match(TokenType.RIGHT_PARENTHESIS))
				{
					parser.parseExpression();
					if (!parser.match(TokenType.COMMA))
					{
						parser.expect(TokenType.RIGHT_PARENTHESIS);
						break;
					}
				}
				int end = parser.previousToken().end();
				left = parser.getArena().allocateNode(NodeType.METHOD_INVOCATION, start, end);
			}
			else if (parser.match(TokenType.DOT))
				left = parseDotExpression(start);
			else if (parser.match(TokenType.LEFT_BRACKET))
				left = parseArrayAccessOrClassLiteral(start);
			else if (parser.match(TokenType.DOUBLE_COLON))
			{
				parser.parseComments();
				// Explicit type arguments: Type::<String>method
				if (parser.match(TokenType.LESS_THAN))
					parser.parseTypeArguments();
				// Method reference: Type::method or Type::new
				int end;
				if (parser.match(TokenType.NEW))
					// Constructor reference
					end = parser.previousToken().end();
				else if (parser.isIdentifierOrContextualKeyword())
				{
					// Method reference
					end = parser.currentToken().end();
					parser.consume();
				}
				else
				{
					throw new ParserException(
						"Expected method name or 'new' after '::' but found " + parser.currentToken().type(),
						parser.currentToken().start());
				}
				left = parser.getArena().allocateNode(NodeType.METHOD_REFERENCE, start, end);
			}
			else if (parser.currentToken().type() == TokenType.LESS_THAN)
			{
				// Attempt to parse type arguments followed by method reference
				// Pattern: Type<Args>::method or Type<Args>::new
				int checkpoint = parser.getPosition();
				try
				{
					parser.consume();
					parser.parseTypeArguments();

					// Parse optional array dimensions: Type<Args>[]::new
					boolean hasArrayDimensions = parser.parseArrayDimensionsWithAnnotations();

					// Check if followed by ::
					if (parser.currentToken().type() == TokenType.DOUBLE_COLON)
					{
						// This is a parameterized type method reference
						// Wrap in ARRAY_TYPE if array dimensions were parsed
						if (hasArrayDimensions)
						{
							int arrayTypeEnd = parser.previousToken().end();
							left = parser.getArena().allocateNode(NodeType.ARRAY_TYPE, start, arrayTypeEnd);
						}
						parser.consume();
						parser.parseComments();

						// Explicit type arguments after :: : Type<A>::<B>method
						if (parser.match(TokenType.LESS_THAN))
							parser.parseTypeArguments();

						// Method reference: Type<Args>::method or Type<Args>::new
						int end;
						if (parser.match(TokenType.NEW))
							// Constructor reference
							end = parser.previousToken().end();
						else if (parser.isIdentifierOrContextualKeyword())
						{
							// Method reference
							end = parser.currentToken().end();
							parser.consume();
						}
						else
						{
							throw new ParserException(
								"Expected method name or 'new' after '::' but found " + parser.currentToken().type(),
								parser.currentToken().start());
						}
						left = parser.getArena().allocateNode(NodeType.METHOD_REFERENCE, start, end);
					}
					else
					{
						// Not a method reference, backtrack
						parser.setPosition(checkpoint);
						break;
					}
				}
				catch (ParserException e)
				{
					// Type argument parsing failed, backtrack and exit loop
					parser.setPosition(checkpoint);
					break;
				}
			}
			else if (parser.currentToken().type() == TokenType.INCREMENT ||
				parser.currentToken().type() == TokenType.DECREMENT)
			{
				// Postfix increment/decrement
				int end = parser.currentToken().end();
				parser.consume();
				left = parser.getArena().allocateNode(NodeType.POSTFIX_EXPRESSION, start, end);
			}
			else
				break;
		}

		return left;
	}

	/**
	 * Parses expression after DOT token (field access, class literal, qualified this/super, qualified new).
	 *
	 * @param start the start position of the expression
	 * @return the parsed node
	 */
	private NodeIndex parseDotExpression(int start)
	{
		parser.parseComments();
		// Explicit type arguments: obj.<String>method()
		if (parser.match(TokenType.LESS_THAN))
			parser.parseTypeArguments();
		if (parser.isIdentifierOrContextualKeyword())
		{
			// Field access: obj.field
			int end = parser.currentToken().end();
			parser.consume();
			return parser.getArena().allocateNode(NodeType.FIELD_ACCESS, start, end);
		}
		if (parser.match(TokenType.CLASS))
		{
			// Class literal: Type.class, Type[].class
			int end = parser.previousToken().end();
			return parser.getArena().allocateNode(NodeType.CLASS_LITERAL, start, end);
		}
		if (parser.match(TokenType.THIS))
		{
			// Qualified this: Outer.this
			int end = parser.previousToken().end();
			return parser.getArena().allocateNode(NodeType.THIS_EXPRESSION, start, end);
		}
		if (parser.match(TokenType.SUPER))
		{
			// Qualified super: Outer.super
			int end = parser.previousToken().end();
			return parser.getArena().allocateNode(NodeType.SUPER_EXPRESSION, start, end);
		}
		if (parser.match(TokenType.NEW))
			// Qualified class instantiation: outer.new Inner()
			return parser.parseNewExpression(start);
		throw new ParserException(
			"Expected identifier, 'class', 'this', 'super', or 'new' after '.' but found " +
				parser.currentToken().type(),
			parser.currentToken().start());
	}

	/**
	 * Parses array access, array type class literal, or array constructor reference after LEFT_BRACKET consumed.
	 * Handles {@code array[index]} for array access, {@code Type[].class} for class literals,
	 * and {@code Type[]::new} for array constructor references.
	 *
	 * @param start the start position of the expression
	 * @return the parsed node (ARRAY_ACCESS, CLASS_LITERAL, or ARRAY_TYPE for method reference)
	 */
	private NodeIndex parseArrayAccessOrClassLiteral(int start)
	{
		// Check for array type: Type[].class or Type[]::new or Type[][].class
		if (parser.match(TokenType.RIGHT_BRACKET))
		{
			// Parse additional dimensions with JSR 308 annotations
			parser.parseArrayDimensionsWithAnnotations();

			// Check for array constructor reference: Type[]::new
			if (parser.currentToken().type() == TokenType.DOUBLE_COLON)
			{
				// Return ARRAY_TYPE node; parsePostfix will handle ::new
				int end = parser.previousToken().end();
				return parser.getArena().allocateNode(NodeType.ARRAY_TYPE, start, end);
			}

			// Class literal: Type[].class
			parser.expect(TokenType.DOT);
			parser.expect(TokenType.CLASS);
			int end = parser.previousToken().end();
			return parser.getArena().allocateNode(NodeType.CLASS_LITERAL, start, end);
		}
		// Array access with expression
		parser.parseExpression();
		parser.expect(TokenType.RIGHT_BRACKET);
		int end = parser.previousToken().end();
		return parser.getArena().allocateNode(NodeType.ARRAY_ACCESS, start, end);
	}

	private NodeIndex parsePrimary()
	{
		parser.enterDepth();

		try
		{
			parser.parseComments();
			Token token = parser.currentToken();
			int start = token.start();
			int end = token.end();

			if (token.isLiteral())
			{
				parser.consume();
				return parseLiteralExpression(token, start, end);
			}

			if (parser.isIdentifierOrContextualKeyword())
			{
				parser.consume();
				return parser.getArena().allocateNode(NodeType.IDENTIFIER, start, end);
			}

			if (parser.match(TokenType.LEFT_PARENTHESIS))
				return parseParenthesizedOrLambda(start);

			if (parser.match(TokenType.NEW))
				return parser.parseNewExpression(start);

			if (parser.match(TokenType.THIS))
				return parser.getArena().allocateNode(NodeType.THIS_EXPRESSION, start, end);

			if (parser.match(TokenType.SUPER))
				return parser.getArena().allocateNode(NodeType.SUPER_EXPRESSION, start, end);

			if (parser.match(TokenType.LEFT_BRACE))
				// Array initializer: {1, 2, 3}
				return parser.parseArrayInitializer(start);

			if (token.type() == TokenType.AT_SIGN)
				return parser.parseAnnotation();

			if (parser.isPrimitiveType(token.type()))
			{
				parser.consume();
				return parsePrimitiveClassLiteral(start);
			}

			if (parser.match(TokenType.SWITCH))
				// Switch expression: switch (x) { case 1 -> 10; default -> 0; }
				return parser.parseSwitchExpression(start);

			if (token.type() == TokenType.ARROW)
			{
				// Lambda with inferred parameter (handled by caller)
				// For now, just consume and create a placeholder
				parser.consume();
				NodeIndex body = parseExpression();
				NodeArena arena = parser.getArena();
				return arena.allocateNode(NodeType.LAMBDA_EXPRESSION, start, arena.getEnd(body));
			}

			// Handle unary operators that appear after comments (e.g., /* comment */ -5)
			TokenType type = token.type();
			boolean isUnaryOperator = switch (type)
			{
				case MINUS, PLUS, NOT, TILDE, INCREMENT, DECREMENT -> true;
				default -> false;
			};
			if (isUnaryOperator)
			{
				parser.consume();
				NodeIndex operand = parsePrimary();
				int operandEnd = parser.getArena().getEnd(operand);
				return parser.getArena().allocateNode(NodeType.UNARY_EXPRESSION, start, operandEnd);
			}

			throw new ParserException(
				"Unexpected token in expression: " + token.type() + " at position " + start,
				start);
		}
		finally
		{
			parser.exitDepth();
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
		return parser.getArena().allocateNode(nodeType, start, end);
	}

	/**
	 * Parses a primitive type class literal or array constructor reference.
	 * Handles {@code int.class}, {@code void.class}, {@code int[].class}, and {@code int[]::new}.
	 *
	 * @param start the start position
	 * @return the node index for the class literal or array type (for method reference)
	 */
	private NodeIndex parsePrimitiveClassLiteral(int start)
	{
		boolean hasArrayDimensions = parser.parseArrayDimensionsWithAnnotations();

		// Check for array constructor reference: int[]::new
		if (parser.currentToken().type() == TokenType.DOUBLE_COLON)
		{
			if (!hasArrayDimensions)
			{
				throw new ParserException(
					"Primitive type constructor reference requires array dimensions (e.g., int[]::new)",
					parser.currentToken().start());
			}
			// Return ARRAY_TYPE node; parsePostfix will handle ::new
			int end = parser.previousToken().end();
			return parser.getArena().allocateNode(NodeType.ARRAY_TYPE, start, end);
		}

		// Class literal: int.class, int[].class
		parser.expect(TokenType.DOT);
		parser.expect(TokenType.CLASS);
		int classEnd = parser.previousToken().end();
		return parser.getArena().allocateNode(NodeType.CLASS_LITERAL, start, classEnd);
	}

	/**
	 * Parses a switch expression.
	 * <p>
	 * Handles both arrow-style ({@code case 1 -> value;}) and colon-style ({@code case 1: ...}) cases.
	 *
	 * @param start the start position of the {@code switch} keyword
	 * @return the node index for the switch expression
	 */
	public NodeIndex parseSwitchExpression(int start)
	{
		// SWITCH already consumed
		parser.expect(TokenType.LEFT_PARENTHESIS);
		parseExpression();
		parser.expect(TokenType.RIGHT_PARENTHESIS);
		parser.expect(TokenType.LEFT_BRACE);
		// Handle comments after opening brace
		parser.parseComments();

		while (parser.currentToken().type() == TokenType.CASE || parser.currentToken().type() == TokenType.DEFAULT)
		{
			if (parser.match(TokenType.CASE))
			{
				// Parse first case label element (may be expression or type pattern)
				parser.parseCaseLabelElement();
				// Handle multiple case labels: case 1, 2, 3 ->
				while (parser.match(TokenType.COMMA))
					parser.parseCaseLabelElement();
			}
			else
				parser.consume(); // DEFAULT

			if (parser.match(TokenType.ARROW))
			{
				// Arrow case: case 1 -> expr;
				if (parser.currentToken().type() == TokenType.LEFT_BRACE)
					// Block body: case 1 -> { ... }
					parser.parseBlock();
				else if (parser.currentToken().type() == TokenType.THROW)
				{
					// Throw expression: case 1 -> throw new Exception();
					parser.consume();
					parseExpression();
					parser.expect(TokenType.SEMICOLON);
				}
				else
				{
					// Expression body: case 1 -> value;
					parseExpression();
					parser.expect(TokenType.SEMICOLON);
				}
			}
			else
			{
				// Colon case (traditional): case 1:
				parser.expect(TokenType.COLON);
				// Handle comments after colon
				parser.parseComments();
				while (parser.currentToken().type() != TokenType.CASE &&
					parser.currentToken().type() != TokenType.DEFAULT &&
					parser.currentToken().type() != TokenType.RIGHT_BRACE)
					parser.parseStatement();
			}
			// Handle comments between case/default labels
			parser.parseComments();
		}

		parser.expect(TokenType.RIGHT_BRACE);
		int end = parser.previousToken().end();
		return parser.getArena().allocateNode(NodeType.SWITCH_EXPRESSION, start, end);
	}

	/**
	 * Parses a new expression for object instantiation or array creation.
	 * <p>
	 * Handles both object creation ({@code new Type()}) and array creation with dimension
	 * expressions ({@code new int[5]}) or initializers ({@code new int[]{1, 2, 3}}).
	 * <p>
	 * Array creation uses {@link ParserAccess#parseTypeWithoutArrayDimensions()} because the array brackets
	 * must be parsed separately to capture dimension expressions or initializers, rather than
	 * being consumed as part of the type.
	 *
	 * @param start the start position of the {@code new} keyword
	 * @return the node index for the parsed expression
	 */
	public NodeIndex parseNewExpression(int start)
	{
		// Explicit type arguments: new <String>Constructor()
		if (parser.match(TokenType.LESS_THAN))
			parser.parseTypeArguments();

		// Parse type without array brackets - brackets must be handled separately
		// to capture dimension expressions (new int[5]) or initializers (new int[]{1, 2})
		parser.parseTypeWithoutArrayDimensions();

		if (parser.match(TokenType.LEFT_BRACKET))
			return parseArrayCreation(start);
		if (parser.match(TokenType.LEFT_PARENTHESIS))
			return parseObjectCreation(start);

		throw new ParserException(
			"Expected '(' or '[' after 'new' but found " + parser.currentToken().type(),
			parser.currentToken().start());
	}

	/**
	 * Parses array creation after the opening bracket has been consumed.
	 * <p>
	 * Handles three forms of array creation:
	 * <ul>
	 *   <li>Dimension expressions: {@code new int[5]} or {@code new int[2][3]}</li>
	 *   <li>Partially specified dimensions: {@code new int[2][]} (expression followed by empty)</li>
	 *   <li>Array initializers: {@code new int[]{1, 2, 3}}</li>
	 * </ul>
	 *
	 * @param start the start position of the {@code new} keyword
	 * @return the node index for the {@link NodeType#ARRAY_CREATION} node
	 */
	public NodeIndex parseArrayCreation(int start)
	{
		// Parse dimension expression if present (e.g., new int[5])
		if (parser.currentToken().type() != TokenType.RIGHT_BRACKET)
			parseExpression();
		parser.expect(TokenType.RIGHT_BRACKET);

		// Handle multi-dimensional arrays: new int[2][3] or mixed new int[2][]
		while (parser.match(TokenType.LEFT_BRACKET))
		{
			if (parser.currentToken().type() != TokenType.RIGHT_BRACKET)
				parseExpression();
			parser.expect(TokenType.RIGHT_BRACKET);
		}

		// Array initializer: new int[]{1, 2, 3}
		if (parser.match(TokenType.LEFT_BRACE) && !parser.match(TokenType.RIGHT_BRACE))
		{
			parseExpression();
			while (parser.match(TokenType.COMMA))
			{
				if (parser.currentToken().type() == TokenType.RIGHT_BRACE)
					break;
				parseExpression();
			}
			parser.expect(TokenType.RIGHT_BRACE);
		}

		int arrayEnd = parser.previousToken().end();
		return parser.getArena().allocateNode(NodeType.ARRAY_CREATION, start, arrayEnd);
	}

	/**
	 * Parses object creation after the opening parenthesis has been consumed.
	 * <p>
	 * Handles constructor arguments and optional anonymous class body.
	 *
	 * @param start the start position of the {@code new} keyword
	 * @return the node index for the {@link NodeType#OBJECT_CREATION} node
	 */
	public NodeIndex parseObjectCreation(int start)
	{
		// Constructor call arguments
		if (!parser.match(TokenType.RIGHT_PARENTHESIS))
		{
			parseExpression();
			while (parser.match(TokenType.COMMA))
				parseExpression();
			parser.expect(TokenType.RIGHT_PARENTHESIS);
		}

		// Anonymous class body
		if (parser.match(TokenType.LEFT_BRACE))
		{
			while (parser.currentToken().type() != TokenType.RIGHT_BRACE &&
				parser.currentToken().type() != TokenType.END_OF_FILE)
				parser.parseMemberDeclaration();
			parser.expect(TokenType.RIGHT_BRACE);
		}

		int objEnd = parser.previousToken().end();
		return parser.getArena().allocateNode(NodeType.OBJECT_CREATION, start, objEnd);
	}

	/**
	 * Parses an array initializer after the opening brace has been consumed.
	 * <p>
	 * Handles nested array initializers and trailing commas.
	 *
	 * @param start the start position of the opening brace
	 * @return the node index for the {@link NodeType#ARRAY_INITIALIZER} node
	 */
	public NodeIndex parseArrayInitializer(int start)
	{
		// LEFT_BRACE already consumed
		// Handle comments after opening brace
		parser.parseComments();
		if (!parser.match(TokenType.RIGHT_BRACE))
		{
			// Handle nested array initializers or expressions
			if (parser.currentToken().type() == TokenType.LEFT_BRACE)
			{
				int nestedStart = parser.currentToken().start();
				parser.consume();
				parseArrayInitializer(nestedStart);
			}
			else
				parseExpression();
			while (parser.match(TokenType.COMMA))
			{
				// Handle comments after comma
				parser.parseComments();
				if (parser.currentToken().type() == TokenType.RIGHT_BRACE)
					break;
				if (parser.currentToken().type() == TokenType.LEFT_BRACE)
				{
					int nestedStart = parser.currentToken().start();
					parser.consume();
					parseArrayInitializer(nestedStart);
				}
				else
					parseExpression();
			}
			// Handle comments before closing brace
			parser.parseComments();
			parser.expect(TokenType.RIGHT_BRACE);
		}
		int end = parser.previousToken().end();
		return parser.getArena().allocateNode(NodeType.ARRAY_INITIALIZER, start, end);
	}
}
