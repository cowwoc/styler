package io.github.cowwoc.styler.parser.internal;

import io.github.cowwoc.styler.ast.core.NodeIndex;
import io.github.cowwoc.styler.ast.core.NodeType;
import io.github.cowwoc.styler.parser.Parser.ParserException;
import io.github.cowwoc.styler.parser.Token;
import io.github.cowwoc.styler.parser.TokenType;

import java.util.List;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.that;

/**
 * Helper class for expression parsing, specifically cast and lambda disambiguation.
 * <p>
 * Extracted from Parser to reduce class size while maintaining cohesive parsing logic.
 * Contains methods for detecting and parsing cast expressions and lambda expressions.
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

}
