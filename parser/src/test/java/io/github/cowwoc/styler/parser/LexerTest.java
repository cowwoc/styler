package io.github.cowwoc.styler.parser;

import org.testng.annotations.Test;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;

import java.util.List;

/**
 * Thread-safe tests for Lexer.
 */
public class LexerTest
{
	/**
	 * Tests lexer behavior on empty input.
	 * Validates that empty source produces only EOF token without errors,
	 * ensuring graceful handling of minimal input.
	 */
	@Test
	public void testEmptySource()
	{
		Lexer lexer = new Lexer("");
		List<Token> tokens = lexer.tokenize();

		requireThat(tokens.size(), "tokens.size()").isEqualTo(1);
		requireThat(tokens.getFirst().type(), "tokens.getFirst().type()").isEqualTo(TokenType.EOF);
	}

	/**
	 * Tests recognition of single Java keyword.
	 * Validates that lexer correctly identifies reserved words
	 * and distinguishes them from identifiers.
	 */
	@Test
	public void testSingleKeyword()
	{
		Lexer lexer = new Lexer("class");
		List<Token> tokens = lexer.tokenize();

		requireThat(tokens.size(), "tokens.size()").isEqualTo(2); // class + EOF
		requireThat(tokens.get(0).type(), "tokens.get(0).type()").isEqualTo(TokenType.CLASS);
		requireThat(tokens.get(0).text(), "tokens.get(0).text()").isEqualTo("class");
		requireThat(tokens.get(1).type(), "tokens.get(1).type()").isEqualTo(TokenType.EOF);
	}

	/**
	 * Tests lexing of multiple consecutive keywords.
	 * Validates that lexer properly tokenizes keyword sequences
	 * separated by whitespace, common in declarations.
	 */
	@Test
	public void testMultipleKeywords()
	{
		Lexer lexer = new Lexer("public static void");
		List<Token> tokens = lexer.tokenize();

		requireThat(tokens.size(), "tokens.size()").isEqualTo(4); // 3 keywords + EOF
		requireThat(tokens.get(0).type(), "tokens.get(0).type()").isEqualTo(TokenType.PUBLIC);
		requireThat(tokens.get(1).type(), "tokens.get(1).type()").isEqualTo(TokenType.STATIC);
		requireThat(tokens.get(2).type(), "tokens.get(2).type()").isEqualTo(TokenType.VOID);
	}

	/**
	 * Tests identifier lexing for variable and method names.
	 * Validates that lexer recognizes user-defined names
	 * distinct from keywords, preserving original text.
	 */
	@Test
	public void testIdentifier()
	{
		Lexer lexer = new Lexer("myVariable");
		List<Token> tokens = lexer.tokenize();

		requireThat(tokens.size(), "tokens.size()").isEqualTo(2);
		requireThat(tokens.get(0).type(), "tokens.get(0).type()").isEqualTo(TokenType.IDENTIFIER);
		requireThat(tokens.get(0).text(), "tokens.get(0).text()").isEqualTo("myVariable");
	}

	/**
	 * Tests integer literal lexing without suffix.
	 * Validates recognition of decimal integer constants,
	 * the most common numeric literal type.
	 */
	@Test
	public void testIntegerLiteral()
	{
		Lexer lexer = new Lexer("42");
		List<Token> tokens = lexer.tokenize();

		requireThat(tokens.size(), "tokens.size()").isEqualTo(2);
		requireThat(tokens.get(0).type(), "tokens.get(0).type()").isEqualTo(TokenType.INTEGER_LITERAL);
		requireThat(tokens.get(0).text(), "tokens.get(0).text()").isEqualTo("42");
	}

	/**
	 * Tests long literal lexing with L suffix.
	 * Validates that lexer distinguishes long literals from integers
	 * by recognizing the L suffix character.
	 */
	@Test
	public void testLongLiteral()
	{
		Lexer lexer = new Lexer("42L");
		List<Token> tokens = lexer.tokenize();

		requireThat(tokens.size(), "tokens.size()").isEqualTo(2);
		requireThat(tokens.get(0).type(), "tokens.get(0).type()").isEqualTo(TokenType.LONG_LITERAL);
		requireThat(tokens.get(0).text(), "tokens.get(0).text()").isEqualTo("42L");
	}

	/**
	 * Tests float literal lexing with F suffix.
	 * Validates recognition of single-precision floating-point literals
	 * by checking for decimal point and F suffix.
	 */
	@Test
	public void testFloatLiteral()
	{
		Lexer lexer = new Lexer("3.14F");
		List<Token> tokens = lexer.tokenize();

		requireThat(tokens.size(), "tokens.size()").isEqualTo(2);
		requireThat(tokens.get(0).type(), "tokens.get(0).type()").isEqualTo(TokenType.FLOAT_LITERAL);
		requireThat(tokens.get(0).text(), "tokens.get(0).text()").isEqualTo("3.14F");
	}

	/**
	 * Tests double literal lexing without suffix.
	 * Validates that decimal numbers with fractional parts
	 * default to double type when no suffix specified.
	 */
	@Test
	public void testDoubleLiteral()
	{
		Lexer lexer = new Lexer("3.14");
		List<Token> tokens = lexer.tokenize();

		requireThat(tokens.size(), "tokens.size()").isEqualTo(2);
		requireThat(tokens.get(0).type(), "tokens.get(0).type()").isEqualTo(TokenType.DOUBLE_LITERAL);
		requireThat(tokens.get(0).text(), "tokens.get(0).text()").isEqualTo("3.14");
	}

	/**
	 * Tests recognition of boolean literal keywords.
	 * Validates that true and false are lexed as BOOLEAN_LITERAL
	 * rather than identifiers, providing type information early.
	 */
	@Test
	public void testBooleanLiterals()
	{
		Lexer lexer = new Lexer("true false");
		List<Token> tokens = lexer.tokenize();

		requireThat(tokens.size(), "tokens.size()").isEqualTo(3);
		requireThat(tokens.get(0).type(), "tokens.get(0).type()").isEqualTo(TokenType.BOOLEAN_LITERAL);
		requireThat(tokens.get(0).text(), "tokens.get(0).text()").isEqualTo("true");
		requireThat(tokens.get(1).type(), "tokens.get(1).type()").isEqualTo(TokenType.BOOLEAN_LITERAL);
		requireThat(tokens.get(1).text(), "tokens.get(1).text()").isEqualTo("false");
	}

	/**
	 * Tests null literal keyword recognition.
	 * Validates that null is lexed as NULL_LITERAL token
	 * for proper null reference handling.
	 */
	@Test
	public void testNullLiteral()
	{
		Lexer lexer = new Lexer("null");
		List<Token> tokens = lexer.tokenize();

		requireThat(tokens.size(), "tokens.size()").isEqualTo(2);
		requireThat(tokens.get(0).type(), "tokens.get(0).type()").isEqualTo(TokenType.NULL_LITERAL);
		requireThat(tokens.get(0).text(), "tokens.get(0).text()").isEqualTo("null");
	}

	/**
	 * Tests string literal lexing with quotes.
	 * Validates that lexer captures complete string literals
	 * including opening and closing quotes.
	 */
	@Test
	public void testStringLiteral()
	{
		Lexer lexer = new Lexer("\"Hello, World!\"");
		List<Token> tokens = lexer.tokenize();

		requireThat(tokens.size(), "tokens.size()").isEqualTo(2);
		requireThat(tokens.get(0).type(), "tokens.get(0).type()").isEqualTo(TokenType.STRING_LITERAL);
		requireThat(tokens.get(0).text(), "tokens.get(0).text()").isEqualTo("\"Hello, World!\"");
	}

	/**
	 * Tests character literal lexing with single quotes.
	 * Validates that lexer correctly identifies single-character literals
	 * enclosed in apostrophes, distinct from string literals.
	 */
	@Test
	public void testCharLiteral()
	{
		Lexer lexer = new Lexer("'a'");
		List<Token> tokens = lexer.tokenize();

		requireThat(tokens.size(), "tokens.size()").isEqualTo(2);
		requireThat(tokens.get(0).type(), "tokens.get(0).type()").isEqualTo(TokenType.CHAR_LITERAL);
		requireThat(tokens.get(0).text(), "tokens.get(0).text()").isEqualTo("'a'");
	}

	/**
	 * Tests line comment lexing (// style).
	 * Validates that lexer recognizes single-line comments extending
	 * to end of line, preserving comment text for documentation tools.
	 */
	@Test
	public void testLineComment()
	{
		Lexer lexer = new Lexer("// This is a comment");
		List<Token> tokens = lexer.tokenize();

		requireThat(tokens.size(), "tokens.size()").isEqualTo(2);
		requireThat(tokens.get(0).type(), "tokens.get(0).type()").isEqualTo(TokenType.LINE_COMMENT);
		requireThat(tokens.get(0).text(), "tokens.get(0).text()").isEqualTo("// This is a comment");
	}

	/**
	 * Tests block comment lexing (<code>/* *&#47;</code> style).
	 * Validates recognition of multi-line comments enclosed in <code>/* *&#47;</code> delimiters,
	 * which can span multiple lines and contain arbitrary text.
	 */
	@Test
	public void testBlockComment()
	{
		Lexer lexer = new Lexer("/* block comment */");
		List<Token> tokens = lexer.tokenize();

		requireThat(tokens.size(), "tokens.size()").isEqualTo(2);
		requireThat(tokens.get(0).type(), "tokens.get(0).type()").isEqualTo(TokenType.BLOCK_COMMENT);
		requireThat(tokens.get(0).text(), "tokens.get(0).text()").isEqualTo("/* block comment */");
	}

	/**
	 * Tests JavaDoc comment lexing (<code>/** *&#47;</code> style).
	 * Validates that lexer distinguishes documentation comments from
	 * regular block comments by checking for <code>/**</code> opening.
	 */
	@Test
	public void testJavadocComment()
	{
		Lexer lexer = new Lexer("/** javadoc */");
		List<Token> tokens = lexer.tokenize();

		requireThat(tokens.size(), "tokens.size()").isEqualTo(2);
		requireThat(tokens.get(0).type(), "tokens.get(0).type()").isEqualTo(TokenType.JAVADOC_COMMENT);
		requireThat(tokens.get(0).text(), "tokens.get(0).text()").isEqualTo("/** javadoc */");
	}

	/**
	 * Tests lexing of Java separator characters.
	 * Validates recognition of structural punctuation (parentheses, braces,
	 * brackets, semicolon, comma, dot) used for syntax structure.
	 */
	@Test
	public void testSeparators()
	{
		Lexer lexer = new Lexer("(){}[];,.");
		List<Token> tokens = lexer.tokenize();

		requireThat(tokens.get(0).type(), "tokens.get(0).type()").isEqualTo(TokenType.LPAREN);
		requireThat(tokens.get(1).type(), "tokens.get(1).type()").isEqualTo(TokenType.RPAREN);
		requireThat(tokens.get(2).type(), "tokens.get(2).type()").isEqualTo(TokenType.LBRACE);
		requireThat(tokens.get(3).type(), "tokens.get(3).type()").isEqualTo(TokenType.RBRACE);
		requireThat(tokens.get(4).type(), "tokens.get(4).type()").isEqualTo(TokenType.LBRACKET);
		requireThat(tokens.get(5).type(), "tokens.get(5).type()").isEqualTo(TokenType.RBRACKET);
		requireThat(tokens.get(6).type(), "tokens.get(6).type()").isEqualTo(TokenType.SEMICOLON);
		requireThat(tokens.get(7).type(), "tokens.get(7).type()").isEqualTo(TokenType.COMMA);
		requireThat(tokens.get(8).type(), "tokens.get(8).type()").isEqualTo(TokenType.DOT);
	}

	/**
	 * Tests ellipsis operator lexing (...).
	 * Validates recognition of varargs syntax as single token
	 * rather than three separate dots.
	 */
	@Test
	public void testEllipsis()
	{
		Lexer lexer = new Lexer("...");
		List<Token> tokens = lexer.tokenize();

		requireThat(tokens.size(), "tokens.size()").isEqualTo(2);
		requireThat(tokens.get(0).type(), "tokens.get(0).type()").isEqualTo(TokenType.ELLIPSIS);
		requireThat(tokens.get(0).text(), "tokens.get(0).text()").isEqualTo("...");
	}

	/**
	 * Tests double colon operator lexing (::).
	 * Validates recognition of method reference operator introduced in Java 8,
	 * ensuring it's not split into two colons.
	 */
	@Test
	public void testDoubleColon()
	{
		Lexer lexer = new Lexer("::");
		List<Token> tokens = lexer.tokenize();

		requireThat(tokens.size(), "tokens.size()").isEqualTo(2);
		requireThat(tokens.get(0).type(), "tokens.get(0).type()").isEqualTo(TokenType.DOUBLE_COLON);
		requireThat(tokens.get(0).text(), "tokens.get(0).text()").isEqualTo("::");
	}

	/**
	 * Tests arrow operator lexing (->).
	 * Validates recognition of lambda arrow as single token
	 * rather than minus followed by greater-than.
	 */
	@Test
	public void testArrow()
	{
		Lexer lexer = new Lexer("->");
		List<Token> tokens = lexer.tokenize();

		requireThat(tokens.size(), "tokens.size()").isEqualTo(2);
		requireThat(tokens.get(0).type(), "tokens.get(0).type()").isEqualTo(TokenType.ARROW);
		requireThat(tokens.get(0).text(), "tokens.get(0).text()").isEqualTo("->");
	}

	/**
	 * Tests lexing of comparison operators.
	 * Validates recognition of equality (==), inequality (!=),
	 * and relational operators (<=, >=, <, >).
	 */
	@Test
	public void testComparisonOperators()
	{
		Lexer lexer = new Lexer("== != <= >= < >");
		List<Token> tokens = lexer.tokenize();

		requireThat(tokens.get(0).type(), "tokens.get(0).type()").isEqualTo(TokenType.EQ);
		requireThat(tokens.get(1).type(), "tokens.get(1).type()").isEqualTo(TokenType.NE);
		requireThat(tokens.get(2).type(), "tokens.get(2).type()").isEqualTo(TokenType.LE);
		requireThat(tokens.get(3).type(), "tokens.get(3).type()").isEqualTo(TokenType.GE);
		requireThat(tokens.get(4).type(), "tokens.get(4).type()").isEqualTo(TokenType.LT);
		requireThat(tokens.get(5).type(), "tokens.get(5).type()").isEqualTo(TokenType.GT);
	}

	/**
	 * Tests lexing of logical operators.
	 * Validates recognition of AND (&&), OR (||), and NOT (!)
	 * operators for boolean expressions.
	 */
	@Test
	public void testLogicalOperators()
	{
		Lexer lexer = new Lexer("&& || !");
		List<Token> tokens = lexer.tokenize();

		requireThat(tokens.get(0).type(), "tokens.get(0).type()").isEqualTo(TokenType.AND);
		requireThat(tokens.get(1).type(), "tokens.get(1).type()").isEqualTo(TokenType.OR);
		requireThat(tokens.get(2).type(), "tokens.get(2).type()").isEqualTo(TokenType.NOT);
	}

	/**
	 * Tests lexing of arithmetic operators.
	 * Validates recognition of basic mathematical operators (+, -, *, /, %)
	 * for numeric expressions.
	 */
	@Test
	public void testArithmeticOperators()
	{
		Lexer lexer = new Lexer("+ - * / %");
		List<Token> tokens = lexer.tokenize();

		requireThat(tokens.get(0).type(), "tokens.get(0).type()").isEqualTo(TokenType.PLUS);
		requireThat(tokens.get(1).type(), "tokens.get(1).type()").isEqualTo(TokenType.MINUS);
		requireThat(tokens.get(2).type(), "tokens.get(2).type()").isEqualTo(TokenType.STAR);
		requireThat(tokens.get(3).type(), "tokens.get(3).type()").isEqualTo(TokenType.DIV);
		requireThat(tokens.get(4).type(), "tokens.get(4).type()").isEqualTo(TokenType.MOD);
	}

	/**
	 * Tests lexing of bitwise operators.
	 * Validates recognition of bit manipulation operators (&, |, ^, ~)
	 * and shift operators (<<, >>, >>>).
	 */
	@Test
	public void testBitwiseOperators()
	{
		Lexer lexer = new Lexer("& | ^ ~ << >> >>>");
		List<Token> tokens = lexer.tokenize();

		requireThat(tokens.get(0).type(), "tokens.get(0).type()").isEqualTo(TokenType.BITAND);
		requireThat(tokens.get(1).type(), "tokens.get(1).type()").isEqualTo(TokenType.BITOR);
		requireThat(tokens.get(2).type(), "tokens.get(2).type()").isEqualTo(TokenType.CARET);
		requireThat(tokens.get(3).type(), "tokens.get(3).type()").isEqualTo(TokenType.TILDE);
		requireThat(tokens.get(4).type(), "tokens.get(4).type()").isEqualTo(TokenType.LSHIFT);
		requireThat(tokens.get(5).type(), "tokens.get(5).type()").isEqualTo(TokenType.RSHIFT);
		requireThat(tokens.get(6).type(), "tokens.get(6).type()").isEqualTo(TokenType.URSHIFT);
	}

	/**
	 * Tests lexing of compound assignment operators.
	 * Validates recognition of combined operation-and-assignment operators
	 * (+=, -=, *=, /=, etc.) as single tokens.
	 */
	@Test
	public void testAssignmentOperators()
	{
		Lexer lexer = new Lexer("= += -= *= /= %= &= |= ^= <<= >>= >>>=");
		List<Token> tokens = lexer.tokenize();

		requireThat(tokens.get(0).type(), "tokens.get(0).type()").isEqualTo(TokenType.ASSIGN);
		requireThat(tokens.get(1).type(), "tokens.get(1).type()").isEqualTo(TokenType.PLUSASSIGN);
		requireThat(tokens.get(2).type(), "tokens.get(2).type()").isEqualTo(TokenType.MINUSASSIGN);
		requireThat(tokens.get(3).type(), "tokens.get(3).type()").isEqualTo(TokenType.STARASSIGN);
		requireThat(tokens.get(4).type(), "tokens.get(4).type()").isEqualTo(TokenType.DIVASSIGN);
		requireThat(tokens.get(5).type(), "tokens.get(5).type()").isEqualTo(TokenType.MODASSIGN);
		requireThat(tokens.get(6).type(), "tokens.get(6).type()").isEqualTo(TokenType.BITANDASSIGN);
		requireThat(tokens.get(7).type(), "tokens.get(7).type()").isEqualTo(TokenType.BITORASSIGN);
		requireThat(tokens.get(8).type(), "tokens.get(8).type()").isEqualTo(TokenType.CARETASSIGN);
		requireThat(tokens.get(9).type(), "tokens.get(9).type()").isEqualTo(TokenType.LSHIFTASSIGN);
		requireThat(tokens.get(10).type(), "tokens.get(10).type()").isEqualTo(TokenType.RSHIFTASSIGN);
		requireThat(tokens.get(11).type(), "tokens.get(11).type()").isEqualTo(TokenType.URSHIFTASSIGN);
	}

	/**
	 * Tests lexing of increment and decrement operators.
	 * Validates recognition of ++ and -- as single tokens
	 * for pre/post increment/decrement operations.
	 */
	@Test
	public void testIncrementDecrement()
	{
		Lexer lexer = new Lexer("++ --");
		List<Token> tokens = lexer.tokenize();

		requireThat(tokens.get(0).type(), "tokens.get(0).type()").isEqualTo(TokenType.INC);
		requireThat(tokens.get(1).type(), "tokens.get(1).type()").isEqualTo(TokenType.DEC);
	}

	/**
	 * Tests lexing of complete class declaration.
	 * Validates that lexer correctly tokenizes a simple class declaration
	 * with keywords, identifiers, and separators in proper sequence.
	 */
	@Test
	public void testSimpleClassDeclaration()
	{
		String source = "public class HelloWorld { }";
		Lexer lexer = new Lexer(source);
		List<Token> tokens = lexer.tokenize();

		requireThat(tokens.get(0).type(), "tokens.get(0).type()").isEqualTo(TokenType.PUBLIC);
		requireThat(tokens.get(1).type(), "tokens.get(1).type()").isEqualTo(TokenType.CLASS);
		requireThat(tokens.get(2).type(), "tokens.get(2).type()").isEqualTo(TokenType.IDENTIFIER);
		requireThat(tokens.get(2).text(), "tokens.get(2).text()").isEqualTo("HelloWorld");
		requireThat(tokens.get(3).type(), "tokens.get(3).type()").isEqualTo(TokenType.LBRACE);
		requireThat(tokens.get(4).type(), "tokens.get(4).type()").isEqualTo(TokenType.RBRACE);
		requireThat(tokens.get(5).type(), "tokens.get(5).type()").isEqualTo(TokenType.EOF);
	}

	/**
	 * Tests Token.isKeyword() classification method.
	 * Validates that keyword tokens return true while
	 * identifier tokens return false.
	 */
	@Test
	public void testTokenIsKeyword()
	{
		Token keyword = new Token(TokenType.PUBLIC, 0, 6, "public");
		Token identifier = new Token(TokenType.IDENTIFIER, 7, 12, "myVar");

		requireThat(keyword.isKeyword(), "keyword.isKeyword()").isTrue();
		requireThat(identifier.isKeyword(), "identifier.isKeyword()").isFalse();
	}

	/**
	 * Tests Token.isLiteral() classification method.
	 * Validates that literal tokens return true while
	 * keyword tokens return false.
	 */
	@Test
	public void testTokenIsLiteral()
	{
		Token literal = new Token(TokenType.INTEGER_LITERAL, 0, 2, "42");
		Token keyword = new Token(TokenType.PUBLIC, 0, 6, "public");

		requireThat(literal.isLiteral(), "literal.isLiteral()").isTrue();
		requireThat(keyword.isLiteral(), "keyword.isLiteral()").isFalse();
	}

	/**
	 * Tests Token.isOperator() classification method.
	 * Validates that operator tokens return true while
	 * keyword tokens return false.
	 */
	@Test
	public void testTokenIsOperator()
	{
		Token operator = new Token(TokenType.PLUS, 0, 1, "+");
		Token keyword = new Token(TokenType.PUBLIC, 0, 6, "public");

		requireThat(operator.isOperator(), "operator.isOperator()").isTrue();
		requireThat(keyword.isOperator(), "keyword.isOperator()").isFalse();
	}

	/**
	 * Tests Token.length() calculation.
	 * Validates that token length is correctly computed as difference
	 * between end and start positions.
	 */
	@Test
	public void testTokenLength()
	{
		Token token = new Token(TokenType.IDENTIFIER, 10, 17, "myValue");

		requireThat(token.length(), "token.length()").isEqualTo(7);
	}

	/**
	 * Tests that lexer skips whitespace between tokens.
	 * Validates that spaces, tabs, and newlines are ignored,
	 * producing only significant tokens plus EOF.
	 */
	@Test
	public void testWhitespaceSkipping()
	{
		Lexer lexer = new Lexer("  \t\n  public  \n  ");
		List<Token> tokens = lexer.tokenize();

		requireThat(tokens.size(), "tokens.size()").isEqualTo(2); // public + EOF
		requireThat(tokens.get(0).type(), "tokens.get(0).type()").isEqualTo(TokenType.PUBLIC);
	}
}
