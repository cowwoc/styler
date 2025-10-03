package io.github.cowwoc.styler.parser;

import java.util.List;

/**
 * Parse context for recursive descent parsing.
 * Manages current position in token stream and provides parsing utilities.
 */
public class ParseContext
{
	private final List<TokenInfo> tokens;
	private final ArenaNodeStorage nodeStorage;
	private final String sourceText;
	private int currentTokenIndex;
	private TokenInfo pendingToken; // For injected tokens (e.g., splitting >> into > >)
	private StatementParser statementParser; // Callback for parsing statements from strategies

	// Security: Recursion depth protection against stack overflow attacks
	private static final int MAX_RECURSION_DEPTH = 1000;
	private int currentRecursionDepth;

	// Parent tracking for proper AST structure
	private final int[] parentStack = new int[MAX_RECURSION_DEPTH];
	private int parentStackTop = -1;

	/**
	 * Creates a parse context for recursive descent parsing.
	 *
	 * @param tokens the list of tokens to parse
	 * @param nodeStorage the node storage for allocating AST nodes
	 * @param sourceText the original source code text
	 */
	public ParseContext(List<TokenInfo> tokens, ArenaNodeStorage nodeStorage, String sourceText)
{
		this.tokens = tokens;
		this.nodeStorage = nodeStorage;
		this.sourceText = sourceText;
	}

	/**
	 * Gets the current token without advancing.
	 *
	 * @return the current {@link TokenInfo} or EOF token if at end
	 */
	public TokenInfo getCurrentToken()
{
		if (pendingToken != null)
{
			return pendingToken;
		}
		if (currentTokenIndex >= tokens.size())
{
			return tokens.get(tokens.size() - 1); // Return EOF token
		}
		return tokens.get(currentTokenIndex);
	}

	/**
	 * Peeks ahead at a token relative to current position.
	 *
	 * @param offset the offset from current position (negative to look back, positive to look ahead)
	 * @return the token at the specified offset, or EOF token if out of bounds
	 */
	public TokenInfo peekToken(int offset)
{
		int index = currentTokenIndex + offset;
		if (index < 0 || index >= tokens.size())
{
			return tokens.get(tokens.size() - 1); // Return EOF token
		}
		return tokens.get(index);
	}

	/**
	 * Advances to the next token and returns it.
	 *
	 * @return the new current token after advancing
	 */
	public TokenInfo advance()
{
		if (pendingToken != null)
{
			pendingToken = null; // Consume the pending token
			return getCurrentToken();
		}
		if (currentTokenIndex < tokens.size() - 1)
{
			++currentTokenIndex;
		}
		return getCurrentToken();
	}

	/**
	 * Checks if the current token is of the specified type.
	 *
	 * @param type the token type to check
	 * @return {@code true} if current token matches the specified type, {@code false} otherwise
	 */
	public boolean currentTokenIs(TokenType type)
{
		return getCurrentToken().type() == type;
	}

	/**
	 * Expects a specific token type and advances if found.
	 * Throws ParseException if not found.
	 *
	 * @param expectedType the type of token expected at current position
	 * @return the matched token after advancing
	 */
	public TokenInfo expect(TokenType expectedType)
{
		TokenInfo current = getCurrentToken();
		if (current.type() != expectedType)
{
			// Usability: Provide helpful error message with source position
			throw new IndexOverlayParser.ParseException(
				String.format("Expected %s but found %s at position %d",
					expectedType, current.type(), current.startOffset()));
		}
		return advance();
	}

	/**
	 * Checks if we're at the end of the token stream.
	 *
	 * @return {@code true} if at EOF token, {@code false} otherwise
	 */
	public boolean isAtEnd()
{
		return getCurrentToken().type() == TokenType.EOF;
	}

	/**
	 * Gets the current position in the source text.
	 *
	 * @return the character offset of the current token in the source
	 */
	public int getCurrentPosition()
{
		return getCurrentToken().startOffset();
	}

	/**
	 * Gets the current token index.
	 *
	 * @return the index of the current token in the token list
	 */
	public int getCurrentTokenIndex()
{
		return currentTokenIndex;
	}

	/**
	 * Sets the current token index position (for backtracking during lookahead).
	 *
	 * @param tokenIndex the token index to set as current position
	 */
	public void setPosition(int tokenIndex)
{
		this.currentTokenIndex = tokenIndex;
		this.pendingToken = null; // Clear any pending token
	}

	/**
	 * Updates a node's length (used when end position is determined later).
	 *
	 * @param nodeId the ID of the node to update
	 * @param newLength the new length value
	 */
	public void updateNodeLength(int nodeId, int newLength)
{
		nodeStorage.updateNodeLength(nodeId, newLength);
	}

	/**
	 * Gets the source text for debugging/error messages.
	 *
	 * @return the complete source text being parsed
	 */
	public String getSourceText()
{
		return sourceText;
	}

	/**
	 * Peeks at the next token without advancing.
	 *
	 * @return the next token in the stream
	 */
	public TokenInfo peekNextToken()
{
		return peekToken(1);
	}


	/**
	 * Gets the token at a specific index.
	 *
	 * @param index the index of the token to retrieve
	 * @return the token at the specified index, or EOF token if out of bounds
	 */
	public TokenInfo getToken(int index)
{
		if (index < 0 || index >= tokens.size())
{
			return tokens.get(tokens.size() - 1); // Return EOF token
		}
		return tokens.get(index);
	}

	/**
	 * Injects a virtual token to be returned by the next getCurrentToken() call.
	 * Used for splitting compound tokens like >> into > >.
	 *
	 * @param token the token to inject as the next current token
	 */
	public void injectToken(TokenInfo token)
{
		this.pendingToken = token;
	}

	/**
	 * Enters a recursive parsing method. Increments depth and checks for stack overflow.
	 *
	 * @throws ParseException if recursion depth exceeds maximum allowed
	 */
	public void enterRecursion()
{
		++currentRecursionDepth;
		if (currentRecursionDepth > MAX_RECURSION_DEPTH)
{
			throw new IndexOverlayParser.ParseException(
				"Maximum recursion depth exceeded (" + MAX_RECURSION_DEPTH + "). " +
				"Input may contain excessively nested expressions that could cause stack overflow.");
		}
	}

	/**
	 * Exits a recursive parsing method. Decrements depth.
	 */
	public void exitRecursion()
{
		if (currentRecursionDepth > 0)
{
			--currentRecursionDepth;
		}
	}

	/**
	 * Gets the current recursion depth for monitoring purposes.
	 *
	 * @return the current recursion depth level
	 */
	public int getCurrentRecursionDepth()
{
		return currentRecursionDepth;
	}

	/**
	 * Gets the node storage for creating AST nodes.
	 *
	 * @return the {@link ArenaNodeStorage} instance
	 */
	public ArenaNodeStorage getNodeStorage()
{
		return nodeStorage;
	}

	/**
	 * Gets the current parent node ID for creating child nodes.
	 * Returns -{@code 1} if no parent (root level).
	 *
	 * @return the current parent node ID, or {@code -1} for root level
	 */
	public int getCurrentParent()
{
		if (parentStackTop >= 0)
		{
			return parentStack[parentStackTop];
		}
		return -1;
	}

	/**
	 * Pushes a parent node ID onto the parent stack.
	 * This should be called when entering a new AST scope.
	 *
	 * @param parentId the parent node ID to push onto the stack
	 */
	public void pushParent(int parentId)
{
		if (parentStackTop + 1 >= MAX_RECURSION_DEPTH)
{
			throw new IllegalStateException("Parent stack overflow: too many nested nodes");
		}
		++parentStackTop;
		parentStack[parentStackTop] = parentId;
	}

	/**
	 * Pops the current parent from the parent stack.
	 * This should be called when exiting an AST scope.
	 */
	public void popParent()
{
		if (parentStackTop < 0)
{
			throw new IllegalStateException("Parent stack underflow: no parent to pop");
		}
		--parentStackTop;
	}

	/**
	 * Sets the statement parser callback for strategies that need to parse statements.
	 *
	 * @param statementParser the statement parser callback to use
	 */
	public void setStatementParser(StatementParser statementParser)
{
		this.statementParser = statementParser;
	}

	/**
	 * Parses a statement using the registered statement parser callback.
	 * This allows strategies to delegate statement parsing back to the main parser.
	 *
	 * @throws IllegalStateException if no statement parser has been registered
	 */
	public void parseStatement()
{
		if (statementParser == null)
{
			throw new IllegalStateException(
				"No statement parser registered. Call setStatementParser() before using parseStatement()");
		}
		statementParser.parseStatement(this);
	}

	/**
	 * Callback interface for parsing statements.
	 * Allows strategies to delegate statement parsing back to the main parser.
	 */
	@FunctionalInterface
	public interface StatementParser
	{
		/**
		 * Parses a single statement at the current position in the context.
		 *
		 * @param context the parse context
		 */
		void parseStatement(ParseContext context);
	}
}