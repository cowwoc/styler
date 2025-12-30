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
	LEFT_PARENTHESIS,   // (
	RIGHT_PARENTHESIS,  // )
	LEFT_BRACE,         // {
	RIGHT_BRACE,        // }
	LEFT_BRACKET,       // [
	RIGHT_BRACKET,      // ]
	SEMICOLON,          // ;
	COMMA,              // ,
	DOT,                // .
	ELLIPSIS,           // ...
	AT_SIGN,            // @
	DOUBLE_COLON,       // :: (method reference)

	// Operators
	ASSIGN,                       // =
	GREATER_THAN,                 // >
	LESS_THAN,                    // <
	NOT,                          // !
	TILDE,                        // ~
	QUESTION_MARK,                // ?
	COLON,                        // :
	ARROW,                        // -> (lambda)
	EQUAL,                        // ==
	LESS_THAN_OR_EQUAL,           // <=
	GREATER_THAN_OR_EQUAL,        // >=
	NOT_EQUAL,                    // !=
	LOGICAL_AND,                  // &&
	LOGICAL_OR,                   // ||
	INCREMENT,                    // ++
	DECREMENT,                    // --
	PLUS,                         // +
	MINUS,                        // -
	STAR,                         // *
	DIVIDE,                       // /
	BITWISE_AND,                  // &
	BITWISE_OR,                   // |
	CARET,                        // ^
	MODULO,                       // %
	LEFT_SHIFT,                   // <<
	RIGHT_SHIFT,                  // >>
	UNSIGNED_RIGHT_SHIFT,         // >>>
	PLUS_ASSIGN,                  // +=
	MINUS_ASSIGN,                 // -=
	STAR_ASSIGN,                  // *=
	DIVIDE_ASSIGN,                // /=
	BITWISE_AND_ASSIGN,           // &=
	BITWISE_OR_ASSIGN,            // |=
	CARET_ASSIGN,                 // ^=
	MODULO_ASSIGN,                // %=
	LEFT_SHIFT_ASSIGN,            // <<=
	RIGHT_SHIFT_ASSIGN,           // >>=
	UNSIGNED_RIGHT_SHIFT_ASSIGN,  // >>>=

	// Identifiers and comments
	IDENTIFIER,
	LINE_COMMENT,
	MARKDOWN_DOC_COMMENT,  // JDK 23+ (JEP 467) - /// style documentation
	BLOCK_COMMENT,
	JAVADOC_COMMENT,

	// Special tokens
	WHITESPACE,
	END_OF_FILE,
	ERROR            // For error recovery
}
