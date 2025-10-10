package io.github.cowwoc.styler.parser;

/**
 * Enumeration of all token types in Java source code.
 * Covers JDK 25 language features including keywords, operators, literals, and separators.
 */
public enum TokenType
{
	// Keywords
	ABSTRACT,
	ASSERT,
	BOOLEAN,
	BREAK,
	BYTE,
	CASE,
	CATCH,
	CHAR,
	CLASS,
	CONST,      // Reserved but not used
	CONTINUE,
	DEFAULT,
	DO,
	DOUBLE,
	ELSE,
	ENUM,
	EXTENDS,
	FINAL,
	FINALLY,
	FLOAT,
	FOR,
	GOTO,       // Reserved but not used
	IF,
	IMPLEMENTS,
	IMPORT,
	INSTANCEOF,
	INT,
	INTERFACE,
	LONG,
	NATIVE,
	NEW,
	NON_SEALED, // JDK 17+
	PACKAGE,
	PERMITS,    // JDK 17+
	PRIVATE,
	PROTECTED,
	PUBLIC,
	RECORD,     // JDK 16+
	RETURN,
	SEALED,     // JDK 17+
	SHORT,
	STATIC,
	STRICTFP,
	SUPER,
	SWITCH,
	SYNCHRONIZED,
	THIS,
	THROW,
	THROWS,
	TRANSIENT,
	TRY,
	VAR,        // JDK 10+
	VOID,
	VOLATILE,
	WHILE,
	YIELD,      // JDK 13+

	// Literals
	INTEGER_LITERAL,
	LONG_LITERAL,
	FLOAT_LITERAL,
	DOUBLE_LITERAL,
	BOOLEAN_LITERAL,
	CHAR_LITERAL,
	STRING_LITERAL,
	NULL_LITERAL,

	// Separators
	LPAREN,          // (
	RPAREN,          // )
	LBRACE,          // {
	RBRACE,          // }
	LBRACKET,        // [
	RBRACKET,        // ]
	SEMICOLON,       // ;
	COMMA,           // ,
	DOT,             // .
	ELLIPSIS,        // ...
	AT,              // @
	DOUBLE_COLON,    // :: (method reference)

	// Operators
	ASSIGN,          // =
	GT,              // >
	LT,              // <
	NOT,             // !
	TILDE,           // ~
	QUESTION,        // ?
	COLON,           // :
	ARROW,           // -> (lambda)
	EQ,              // ==
	LE,              // <=
	GE,              // >=
	NE,              // !=
	AND,             // &&
	OR,              // ||
	INC,             // ++
	DEC,             // --
	PLUS,            // +
	MINUS,           // -
	STAR,            // *
	DIV,             // /
	BITAND,          // &
	BITOR,           // |
	CARET,           // ^
	MOD,             // %
	LSHIFT,          // <<
	RSHIFT,          // >>
	URSHIFT,         // >>>
	PLUSASSIGN,      // +=
	MINUSASSIGN,     // -=
	STARASSIGN,      // *=
	DIVASSIGN,       // /=
	BITANDASSIGN,    // &=
	BITORASSIGN,     // |=
	CARETASSIGN,     // ^=
	MODASSIGN,       // %=
	LSHIFTASSIGN,    // <<=
	RSHIFTASSIGN,    // >>=
	URSHIFTASSIGN,   // >>>=

	// Identifiers and comments
	IDENTIFIER,
	LINE_COMMENT,
	BLOCK_COMMENT,
	JAVADOC_COMMENT,

	// Special tokens
	WHITESPACE,
	EOF,
	ERROR            // For error recovery
}
