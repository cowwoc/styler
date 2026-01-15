package io.github.cowwoc.styler.parser.internal;

import io.github.cowwoc.styler.ast.core.NodeArena;
import io.github.cowwoc.styler.ast.core.NodeIndex;
import io.github.cowwoc.styler.parser.Token;
import io.github.cowwoc.styler.parser.TokenType;

import java.util.List;

/**
 * Interface exposing Parser's internal methods to helper classes without making them public API.
 * <p>
 * This "accessor interface" pattern allows:
 * <ul>
 *   <li>Helper classes (ModuleParser, StatementParser) to access Parser internals</li>
 *   <li>Parser methods to remain non-public on the Parser class itself</li>
 *   <li>Clear API boundary between public Parser interface and internal implementation</li>
 * </ul>
 * <p>
 * Helper classes receive {@code ParserAccess} in their constructor instead of {@code Parser}.
 */
public interface ParserAccess
{
	// ========== Token Navigation ==========

	/**
	 * Returns the list of tokens being parsed.
	 *
	 * @return the immutable token list
	 */
	List<Token> getTokens();

	/**
	 * Returns the current token position.
	 *
	 * @return the position index in the token list
	 */
	int getPosition();

	/**
	 * Sets the current token position.
	 *
	 * @param position the new position
	 */
	void setPosition(int position);

	/**
	 * Returns the current token without advancing the position.
	 *
	 * @return the current token
	 */
	Token currentToken();

	/**
	 * Returns the previous token (the one most recently consumed).
	 *
	 * @return the previous token
	 */
	Token previousToken();

	// ========== Token Consumption ==========

	/**
	 * Consumes the current token and advances to the next.
	 *
	 * @return the consumed token
	 */
	Token consume();

	/**
	 * Consumes the current token if it matches the given type.
	 *
	 * @param type the expected token type
	 * @return {@code true} if the token was consumed, {@code false} otherwise
	 */
	boolean match(TokenType type);

	/**
	 * Expects the current token to be of the given type and consumes it.
	 *
	 * @param type the expected token type
	 * @throws io.github.cowwoc.styler.parser.Parser.ParserException if the token does not match
	 */
	void expect(TokenType type);

	/**
	 * Expects an identifier or contextual keyword token and consumes it.
	 *
	 * @throws io.github.cowwoc.styler.parser.Parser.ParserException if token is neither
	 */
	void expectIdentifierOrContextualKeyword();

	/**
	 * Expects a GREATER_THAN token in generic context, handling split RIGHT_SHIFT tokens.
	 *
	 * @throws io.github.cowwoc.styler.parser.Parser.ParserException if no GREATER_THAN available
	 */
	void expectGTInGeneric();

	// ========== AST Construction ==========

	/**
	 * Returns the NodeArena used for allocating AST nodes.
	 *
	 * @return the node arena
	 */
	NodeArena getArena();

	// ========== Depth Tracking (Security) ==========

	/**
	 * Enters a new recursion depth level with security monitoring.
	 *
	 * @throws io.github.cowwoc.styler.parser.Parser.ParserException if timeout or max depth exceeded
	 */
	void enterDepth();

	/**
	 * Exits the current recursion depth level.
	 */
	void exitDepth();

	// ========== Comment Handling ==========

	/**
	 * Parses and records any comments at the current position.
	 */
	void parseComments();

	// ========== Type Parsing ==========

	/**
	 * Parses a type reference including array dimensions.
	 */
	void parseType();

	/**
	 * Parses a type reference without consuming array dimension brackets.
	 */
	void parseTypeWithoutArrayDimensions();

	/**
	 * Parses optional array dimensions with JSR 308 type annotations.
	 *
	 * @return {@code true} if at least one array dimension was parsed
	 */
	boolean parseArrayDimensionsWithAnnotations();

	// ========== Helper Methods ==========

	/**
	 * Checks if the given token type is a primitive type.
	 *
	 * @param type the token type to check
	 * @return {@code true} if the token is a primitive type
	 */
	boolean isPrimitiveType(TokenType type);

	/**
	 * Checks if the current token is an identifier or contextual keyword.
	 *
	 * @return {@code true} if current token can be used as an identifier
	 */
	boolean isIdentifierOrContextualKeyword();

	/**
	 * Checks if the given token type is a contextual keyword usable as an identifier.
	 *
	 * @param type the token type to check
	 * @return {@code true} if the token is a contextual keyword
	 */
	boolean isContextualKeyword(TokenType type);

	// ========== Annotation & Qualified Names ==========

	/**
	 * Parses an annotation.
	 *
	 * @return the annotation node index
	 */
	NodeIndex parseAnnotation();

	/**
	 * Parses a qualified name (e.g., {@code java.lang.String}).
	 *
	 * @return the qualified name node index
	 */
	NodeIndex parseQualifiedName();

	// ========== Block & Expression ==========

	/**
	 * Parses a block statement.
	 *
	 * @return the block node index
	 */
	NodeIndex parseBlock();

	/**
	 * Parses an expression.
	 *
	 * @return the expression node index
	 */
	NodeIndex parseExpression();

	// ========== Catch Parameter ==========

	/**
	 * Parses a catch clause parameter (potentially multi-catch).
	 *
	 * @return the parameter node index
	 */
	NodeIndex parseCatchParameter();

	// ========== Source Code ==========

	/**
	 * Returns the source code being parsed.
	 *
	 * @return the source code string
	 */
	String getSourceCode();
}
