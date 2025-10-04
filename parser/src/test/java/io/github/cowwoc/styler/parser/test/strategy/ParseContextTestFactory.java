package io.github.cowwoc.styler.parser.test.strategy;

import io.github.cowwoc.styler.parser.ArenaNodeStorage;
import io.github.cowwoc.styler.parser.ParseContext;
import io.github.cowwoc.styler.parser.TokenInfo;
import io.github.cowwoc.styler.parser.TokenType;

import java.util.ArrayList;
import java.util.List;

/**
 * Factory for creating ParseContext instances in tests.
 * All methods are stateless and thread-safe for parallel execution.
 */
public final class ParseContextTestFactory
{
	private ParseContextTestFactory()
	{
	}

	/**
	 * Creates a minimal ParseContext with specified token types.
	 * Suitable for testing strategy canHandle() methods.
	 *
	 * @param tokens the tokens to include in the context
	 * @return a new ParseContext with the specified tokens
	 */
	public static ParseContext createMinimalContext(TokenType... tokens)
	{
		List<TokenInfo> tokenList = new ArrayList<>();
		int position = 0;

		for (TokenType token : tokens)
		{
			String text = token.name();
			tokenList.add(new TokenInfo(token, position, text.length(), text));
			position += text.length() + 1; // Add space between tokens
		}

		// Add EOF token
		tokenList.add(new TokenInfo(TokenType.EOF, position, 0, ""));

		ArenaNodeStorage nodeStorage = ArenaNodeStorage.create(100);
		return new ParseContext(tokenList, nodeStorage, buildSourceText(tokenList));
	}

	/**
	 * Creates a ParseContext from Java source code using manual lexing.
	 * Simplified version for testing without full lexer integration.
	 *
	 * @param tokens the tokens to include
	 * @param sourceText the original source text
	 * @return a new ParseContext
	 */
	public static ParseContext createFromTokensAndSource(List<TokenInfo> tokens, String sourceText)
	{
		List<TokenInfo> tokenList = new ArrayList<>(tokens);
		if (tokenList.isEmpty() || tokenList.get(tokenList.size() - 1).type() != TokenType.EOF)
		{
			int lastPos = tokenList.isEmpty() ? 0 : tokenList.get(tokenList.size() - 1).endOffset();
			tokenList.add(new TokenInfo(TokenType.EOF, lastPos, 0, ""));
		}

		ArenaNodeStorage nodeStorage = ArenaNodeStorage.create(tokenList.size() * 2);
		return new ParseContext(tokenList, nodeStorage, sourceText);
	}

	/**
	 * Builds source text from token list for testing.
	 *
	 * @param tokens the token list
	 * @return reconstructed source text
	 */
	private static String buildSourceText(List<TokenInfo> tokens)
	{
		StringBuilder sb = new StringBuilder();
		for (TokenInfo token : tokens)
		{
			if (token.type() != TokenType.EOF)
			{
				sb.append(token.text()).append(" ");
			}
		}
		return sb.toString().trim();
	}
}
