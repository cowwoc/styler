package io.github.cowwoc.styler.parser.strategies;

import io.github.cowwoc.styler.parser.JavaVersion;
import io.github.cowwoc.styler.parser.ParseContext;
import io.github.cowwoc.styler.parser.ParseStrategy;
import io.github.cowwoc.styler.parser.ParsingPhase;
import io.github.cowwoc.styler.parser.TokenType;

/**
 * Strategy for parsing primitive type patterns introduced in Java 25.
 *
 * <p>Primitive type patterns allow pattern matching with primitive types,
 * enabling constructs like {@code if (obj instanceof int i)} for improved
 * type safety and performance.
 *
 * <p>This is a token-based strategy - the {@code phase} parameter is not relevant
 * since primitive type keywords ({@code int}, {@code long}, etc.) are unambiguous
 * in pattern contexts.
 *
 * <p><strong>Example:</strong>
 * <pre>{@code
 * // Primitive type pattern (Java 25+)
 * if (obj instanceof int value) {
 *     // value is directly an int, no boxing
 * }
 * }</pre>
 *
 * @since 1.0
 * @see <a href="https://openjdk.org/jeps/507">JEP 507: Primitive Type Patterns</a>
 */
public class PrimitiveTypePatternStrategy implements ParseStrategy
{
	@Override
	public boolean canHandle(JavaVersion version, ParsingPhase phase, ParseContext context)
	{
		// Phase not relevant for keyword-triggered features
		return version.isAtLeast(JavaVersion.JAVA_25) &&
			   isPrimitiveTypeKeyword(context);
	}

	/**
	 * Checks if the current token is a primitive type keyword.
	 *
	 * @param context the current parsing context
	 * @return {@code true} if current token is {@code int}, {@code long}, {@code double},
	 *         {@code float}, {@code boolean}, {@code byte}, {@code short}, or {@code char}
	 */
	private boolean isPrimitiveTypeKeyword(ParseContext context)
	{
		return context.currentTokenIs(TokenType.INT) ||
			   context.currentTokenIs(TokenType.LONG) ||
			   context.currentTokenIs(TokenType.DOUBLE) ||
			   context.currentTokenIs(TokenType.FLOAT) ||
			   context.currentTokenIs(TokenType.BOOLEAN) ||
			   context.currentTokenIs(TokenType.BYTE) ||
			   context.currentTokenIs(TokenType.SHORT) ||
			   context.currentTokenIs(TokenType.CHAR);
	}

	@Override
	public int parseConstruct(ParseContext context)
	{
		throw new UnsupportedOperationException(
			"Primitive type pattern parsing for Java 25+ is not yet implemented. " +
			"Current parser supports basic Java constructs.");
	}

	@Override
	public int getPriority()
	{
		return PRIORITY_KEYWORD_BASED;
	}

	@Override
	public String getDescription()
	{
		return "Primitive type patterns (Java 25+, JEP 507)";
	}
}
