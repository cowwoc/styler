package io.github.cowwoc.styler.parser.test.strategy;

import io.github.cowwoc.styler.parser.JavaVersion;
import io.github.cowwoc.styler.parser.ParsingPhase;
import io.github.cowwoc.styler.parser.TokenType;

/**
 * Shared constants for strategy testing.
 * All constants are immutable and parallel-safe.
 */
public final class StrategyTestConstants
{
	// Java versions
	public static final JavaVersion JAVA_14 = JavaVersion.JAVA_14;
	public static final JavaVersion JAVA_21 = JavaVersion.JAVA_21;
	public static final JavaVersion JAVA_24 = JavaVersion.JAVA_24;
	public static final JavaVersion JAVA_25 = JavaVersion.JAVA_25;

	// Parsing phases
	public static final ParsingPhase TOP_LEVEL = ParsingPhase.TOP_LEVEL;
	public static final ParsingPhase CLASS_BODY = ParsingPhase.CLASS_BODY;
	public static final ParsingPhase CONSTRUCTOR_BODY = ParsingPhase.CONSTRUCTOR_BODY;
	public static final ParsingPhase METHOD_BODY = ParsingPhase.METHOD_BODY;
	public static final ParsingPhase INITIALIZER_BLOCK = ParsingPhase.INITIALIZER_BLOCK;

	// Common token sequences
	public static final TokenType[] CONSTRUCTOR_BODY_TOKENS = {
		TokenType.LBRACE, TokenType.IDENTIFIER, TokenType.SEMICOLON,
		TokenType.SUPER, TokenType.LPAREN, TokenType.RPAREN, TokenType.RBRACE
	};

	public static final TokenType[] SWITCH_EXPRESSION_TOKENS = {
		TokenType.SWITCH, TokenType.LPAREN, TokenType.IDENTIFIER,
		TokenType.RPAREN, TokenType.LBRACE, TokenType.RBRACE
	};

	// Realistic source code samples
	public static final String FLEXIBLE_CONSTRUCTOR_SOURCE = """
		{
			this.value = input * 2;
			if (value < 0) throw new IllegalArgumentException();
			super(value);
		}
		""";

	public static final String SWITCH_EXPRESSION_SOURCE = """
		switch (day) {
			case MONDAY -> 1;
			case TUESDAY -> 2;
			default -> 0;
		}
		""";

	private StrategyTestConstants()
	{
	}
}
