package io.github.cowwoc.styler.parser.internal;

import io.github.cowwoc.styler.ast.core.NodeIndex;
import io.github.cowwoc.styler.ast.core.NodeType;
import io.github.cowwoc.styler.parser.Parser;
import io.github.cowwoc.styler.parser.TokenType;

/**
 * Helper class for parsing try-catch-finally statements and resources.
 * <p>
 * Extracted from {@link Parser} to reduce class size while maintaining cohesive parsing logic.
 */
public final class StatementParser
{
	private final Parser parser;

	/**
	 * Creates a new statement parser that delegates to the given parser.
	 *
	 * @param parser the parent parser providing token access and helper methods
	 */
	public StatementParser(Parser parser)
	{
		this.parser = parser;
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
