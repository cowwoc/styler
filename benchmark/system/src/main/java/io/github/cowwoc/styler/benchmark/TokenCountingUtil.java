package io.github.cowwoc.styler.benchmark;

import io.github.cowwoc.styler.parser.JavaLexer;
import io.github.cowwoc.styler.parser.TokenInfo;
import io.github.cowwoc.styler.parser.TokenType;

/**
 * Utility for counting tokens in Java source code.
 *
 * <p>This class centralizes token counting logic to prevent code duplication across
 * benchmarks and tests. All token counting operations should use this utility to ensure
 * consistent behavior and simplify maintenance when the lexer API evolves.
 *
 * <p>Usage Context:
 * <ul>
 *   <li>Performance benchmarks measuring parser throughput
 *   <li>Memory usage benchmarks tracking heap consumption
 *   <li>Unit tests validating token counting accuracy
 * </ul>
 */
public final class TokenCountingUtil
{
	private TokenCountingUtil()
	{
		// Utility class - prevent instantiation
	}

	/**
	 * Counts the number of tokens in Java source code using the Styler lexer.
	 *
	 * @param content the Java source code
	 * @return the number of tokens parsed (excluding EOF)
	 */
	public static int countTokens(String content)
	{
		JavaLexer lexer = new JavaLexer(content);
		int count = 0;
		while (true)
		{
			TokenInfo token = lexer.nextToken();
			if (token.type() == TokenType.EOF)
			{
				break;
			}
			++count;
		}
		return count;
	}
}
