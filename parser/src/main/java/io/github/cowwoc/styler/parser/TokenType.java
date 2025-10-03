package io.github.cowwoc.styler.parser;

/**
 * Token types for Java lexical analysis.
 * Covers all Java language constructs from JDK 8 through JDK 25.
 */
public enum TokenType
{
	// Literals
	INTEGER_LITERAL,
	LONG_LITERAL,
	FLOAT_LITERAL,
	DOUBLE_LITERAL,
	BOOLEAN_LITERAL,
	CHARACTER_LITERAL,
	STRING_LITERAL,
	TEXT_BLOCK_LITERAL,      // JDK 15+
	NULL_LITERAL,

	// String templates (JDK 21 preview, 22+)
	STRING_TEMPLATE_START,    // STR."
	STRING_TEMPLATE_MID,      // middle part of template
	STRING_TEMPLATE_END,      // end part of template

	// Identifiers and keywords
	IDENTIFIER,

	// Keywords - Language constructs
	ABSTRACT, ASSERT, BOOLEAN, BREAK, BYTE, CASE, CATCH, CHAR, CLASS, CONST,
	CONTINUE, DEFAULT, DO, DOUBLE, ELSE, ENUM, EXTENDS, FINAL, FINALLY, FLOAT,
	FOR, GOTO, IF, IMPLEMENTS, IMPORT, INSTANCEOF, INT, INTERFACE, LONG,
	NATIVE, NEW, PACKAGE, PRIVATE, PROTECTED, PUBLIC, RETURN, SHORT, STATIC,
	STRICTFP, SUPER, SWITCH, SYNCHRONIZED, THIS, THROW, THROWS, TRANSIENT,
	TRY, VOID, VOLATILE, WHILE,

	// JDK 9+ keywords
	MODULE, REQUIRES, EXPORTS, OPENS, TO, USES, PROVIDES, WITH, TRANSITIVE,

	// JDK 10+ var keyword (contextual)
	VAR,

	// JDK 14+ switch expressions
	YIELD,

	// JDK 16+ records
	RECORD,

	// JDK 17+ sealed classes
	SEALED, NON_SEALED, PERMITS,

	// JDK 21+ unnamed patterns and classes
	UNNAMED,

	// JDK 22+ when (pattern matching guard)
	WHEN,

	// Operators
	ASSIGN,           // =
	PLUS_ASSIGN,      // +=
	MINUS_ASSIGN,     // -=
	MULT_ASSIGN,      // *=
	DIV_ASSIGN,       // /=
	MOD_ASSIGN,       // %=
	AND_ASSIGN,       // &=
	OR_ASSIGN,        // |=
	XOR_ASSIGN,       // ^=
	LSHIFT_ASSIGN,    // <<=
	RSHIFT_ASSIGN,    // >>=
	URSHIFT_ASSIGN,   // >>>=

	// Arithmetic operators
	PLUS,             // +
	MINUS,            // -
	MULT,             // *
	DIV,              // /
	MOD,              // %
	INCREMENT,        // ++
	DECREMENT,        // --

	// Bitwise operators
	BITWISE_AND,      // &
	BITWISE_OR,       // |
	BITWISE_XOR,      // ^
	BITWISE_NOT,      // ~
	LSHIFT,           // <<
	RSHIFT,           // >>
	URSHIFT,          // >>>

	// Logical operators
	LOGICAL_AND,      // &&
	LOGICAL_OR,       // ||
	LOGICAL_NOT,      // !

	// Comparison operators
	EQ,               // ==
	NE,               // !=
	LT,               // <
	LE,               // <=
	GT,               // >
	GE,               // >=

	// Delimiters
	LPAREN,           // (
	RPAREN,           // )
	LBRACE,           // {
	RBRACE,           // }
	LBRACKET,         // [
	RBRACKET,         // ]
	SEMICOLON,        // ;
	COMMA,            // ,
	DOT,              // .
	ELLIPSIS,         // ... (varargs)
	DOUBLE_COLON,     // :: (method references)
	ARROW,            // -> (lambda expressions)
	QUESTION,         // ? (conditional operator, wildcards)
	COLON,            // :
	AT,               // @ (annotations)

	// Comments
	LINE_COMMENT,
	BLOCK_COMMENT,
	JAVADOC_COMMENT,

	// Whitespace
	WHITESPACE,
	NEWLINE,

	// Special tokens
	EOF,
	ERROR,

	// JDK 25 specific tokens (if any new syntax is added)
	STAR;             // * (imports, etc.)

	/**
	 * Checks if this token type represents a keyword.
	 *
	 * @return {@code true} if this token is a Java keyword, {@code false} otherwise
	 */
	public boolean isKeyword()
{
		return ordinal() >= ABSTRACT.ordinal() && ordinal() <= WHEN.ordinal();
	}

	/**
	 * Checks if this token type represents a literal.
	 *
	 * @return {@code true} if this token is a literal value, {@code false} otherwise
	 */
	public boolean isLiteral()
{
		return ordinal() >= INTEGER_LITERAL.ordinal() && ordinal() <= NULL_LITERAL.ordinal();
	}

	/**
	 * Checks if this token type represents an operator.
	 *
	 * @return {@code true} if this token is an operator, {@code false} otherwise
	 */
	public boolean isOperator()
{
		return ordinal() >= ASSIGN.ordinal() && ordinal() <= GE.ordinal();
	}

	/**
	 * Checks if this token type represents whitespace or comments.
	 *
	 * @return {@code true} if this token is whitespace or a comment, {@code false} otherwise
	 */
	public boolean isTrivia()
{
		return this == LINE_COMMENT || this == BLOCK_COMMENT ||
			   this == JAVADOC_COMMENT || this == WHITESPACE || this == NEWLINE;
	}
}