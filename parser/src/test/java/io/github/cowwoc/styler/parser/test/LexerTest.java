package io.github.cowwoc.styler.parser.test;

import io.github.cowwoc.styler.parser.Lexer;
import io.github.cowwoc.styler.parser.Token;
import io.github.cowwoc.styler.parser.TokenType;
import org.testng.annotations.Test;

import java.util.List;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;

/**
 * Thread-safe tests for Lexer.
 */
public class LexerTest
{
	/**
	 * Tests lexer behavior on empty input.
	 * Validates that empty source produces only END_OF_FILE token without errors,
	 * ensuring graceful handling of minimal input.
	 */
	@Test
	public void testEmptySource()
	{
		Lexer lexer = new Lexer("");
		List<Token> tokens = lexer.tokenize();

		requireThat(tokens.size(), "tokens.size()").isEqualTo(1);
		requireThat(tokens.getFirst().type(), "tokens.getFirst().type()").isEqualTo(TokenType.END_OF_FILE);
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

		requireThat(tokens.size(), "tokens.size()").isEqualTo(2); // class + END_OF_FILE
		requireThat(tokens.get(0).type(), "tokens.get(0).type()").isEqualTo(TokenType.CLASS);
		requireThat(tokens.get(0).text(), "tokens.get(0).text()").isEqualTo("class");
		requireThat(tokens.get(1).type(), "tokens.get(1).type()").isEqualTo(TokenType.END_OF_FILE);
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

		requireThat(tokens.size(), "tokens.size()").isEqualTo(4); // 3 keywords + END_OF_FILE
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

		requireThat(tokens.get(0).type(), "tokens.get(0).type()").isEqualTo(TokenType.LEFT_PARENTHESIS);
		requireThat(tokens.get(1).type(), "tokens.get(1).type()").isEqualTo(TokenType.RIGHT_PARENTHESIS);
		requireThat(tokens.get(2).type(), "tokens.get(2).type()").isEqualTo(TokenType.LEFT_BRACE);
		requireThat(tokens.get(3).type(), "tokens.get(3).type()").isEqualTo(TokenType.RIGHT_BRACE);
		requireThat(tokens.get(4).type(), "tokens.get(4).type()").isEqualTo(TokenType.LEFT_BRACKET);
		requireThat(tokens.get(5).type(), "tokens.get(5).type()").isEqualTo(TokenType.RIGHT_BRACKET);
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

		requireThat(tokens.get(0).type(), "tokens.get(0).type()").isEqualTo(TokenType.EQUAL);
		requireThat(tokens.get(1).type(), "tokens.get(1).type()").isEqualTo(TokenType.NOT_EQUAL);
		requireThat(tokens.get(2).type(), "tokens.get(2).type()").isEqualTo(TokenType.LESS_THAN_OR_EQUAL);
		requireThat(tokens.get(3).type(), "tokens.get(3).type()").isEqualTo(TokenType.GREATER_THAN_OR_EQUAL);
		requireThat(tokens.get(4).type(), "tokens.get(4).type()").isEqualTo(TokenType.LESS_THAN);
		requireThat(tokens.get(5).type(), "tokens.get(5).type()").isEqualTo(TokenType.GREATER_THAN);
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

		requireThat(tokens.get(0).type(), "tokens.get(0).type()").isEqualTo(TokenType.LOGICAL_AND);
		requireThat(tokens.get(1).type(), "tokens.get(1).type()").isEqualTo(TokenType.LOGICAL_OR);
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
		requireThat(tokens.get(3).type(), "tokens.get(3).type()").isEqualTo(TokenType.DIVIDE);
		requireThat(tokens.get(4).type(), "tokens.get(4).type()").isEqualTo(TokenType.MODULO);
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

		requireThat(tokens.get(0).type(), "tokens.get(0).type()").isEqualTo(TokenType.BITWISE_AND);
		requireThat(tokens.get(1).type(), "tokens.get(1).type()").isEqualTo(TokenType.BITWISE_OR);
		requireThat(tokens.get(2).type(), "tokens.get(2).type()").isEqualTo(TokenType.CARET);
		requireThat(tokens.get(3).type(), "tokens.get(3).type()").isEqualTo(TokenType.TILDE);
		requireThat(tokens.get(4).type(), "tokens.get(4).type()").isEqualTo(TokenType.LEFT_SHIFT);
		requireThat(tokens.get(5).type(), "tokens.get(5).type()").isEqualTo(TokenType.RIGHT_SHIFT);
		requireThat(tokens.get(6).type(), "tokens.get(6).type()").isEqualTo(TokenType.UNSIGNED_RIGHT_SHIFT);
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
		requireThat(tokens.get(1).type(), "tokens.get(1).type()").isEqualTo(TokenType.PLUS_ASSIGN);
		requireThat(tokens.get(2).type(), "tokens.get(2).type()").isEqualTo(TokenType.MINUS_ASSIGN);
		requireThat(tokens.get(3).type(), "tokens.get(3).type()").isEqualTo(TokenType.STAR_ASSIGN);
		requireThat(tokens.get(4).type(), "tokens.get(4).type()").isEqualTo(TokenType.DIVIDE_ASSIGN);
		requireThat(tokens.get(5).type(), "tokens.get(5).type()").isEqualTo(TokenType.MODULO_ASSIGN);
		requireThat(tokens.get(6).type(), "tokens.get(6).type()").isEqualTo(TokenType.BITWISE_AND_ASSIGN);
		requireThat(tokens.get(7).type(), "tokens.get(7).type()").isEqualTo(TokenType.BITWISE_OR_ASSIGN);
		requireThat(tokens.get(8).type(), "tokens.get(8).type()").isEqualTo(TokenType.CARET_ASSIGN);
		requireThat(tokens.get(9).type(), "tokens.get(9).type()").isEqualTo(TokenType.LEFT_SHIFT_ASSIGN);
		requireThat(tokens.get(10).type(), "tokens.get(10).type()").isEqualTo(TokenType.RIGHT_SHIFT_ASSIGN);
		requireThat(tokens.get(11).type(), "tokens.get(11).type()").isEqualTo(TokenType.UNSIGNED_RIGHT_SHIFT_ASSIGN);
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

		requireThat(tokens.get(0).type(), "tokens.get(0).type()").isEqualTo(TokenType.INCREMENT);
		requireThat(tokens.get(1).type(), "tokens.get(1).type()").isEqualTo(TokenType.DECREMENT);
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
		requireThat(tokens.get(3).type(), "tokens.get(3).type()").isEqualTo(TokenType.LEFT_BRACE);
		requireThat(tokens.get(4).type(), "tokens.get(4).type()").isEqualTo(TokenType.RIGHT_BRACE);
		requireThat(tokens.get(5).type(), "tokens.get(5).type()").isEqualTo(TokenType.END_OF_FILE);
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
	 * producing only significant tokens plus END_OF_FILE.
	 */
	@Test
	public void testWhitespaceSkipping()
	{
		Lexer lexer = new Lexer("  \t\n  public  \n  ");
		List<Token> tokens = lexer.tokenize();

		requireThat(tokens.size(), "tokens.size()").isEqualTo(2); // public + END_OF_FILE
		requireThat(tokens.get(0).type(), "tokens.get(0).type()").isEqualTo(TokenType.PUBLIC);
	}

	/**
	 * Tests that Unicode escapes in identifiers are properly decoded per JLS 3.3.
	 * The identifier uses a Unicode escape for 'A', which should be decoded
	 * for keyword matching while preserving original text.
	 */
	@Test
	public void testUnicodeEscapeInIdentifier()
	{
		// \u0041 is 'A'
		Lexer lexer = new Lexer("\\u0041bc");
		List<Token> tokens = lexer.tokenize();

		requireThat(tokens.size(), "tokens.size()").isEqualTo(2);
		Token token = tokens.getFirst();
		requireThat(token.type(), "token.type()").isEqualTo(TokenType.IDENTIFIER);
		requireThat(token.text(), "token.text()").isEqualTo("\\u0041bc");
		requireThat(token.decodedText(), "token.decodedText()").isEqualTo("Abc");
	}

	/**
	 * Tests that Unicode escapes that form a keyword are recognized as the keyword.
	 * Per JLS 3.3, Unicode escapes are processed before lexical analysis,
	 * so an escaped keyword should be recognized as that keyword.
	 */
	@Test
	public void testUnicodeEscapeFormingKeyword()
	{
		// \u0070ublic = "public"
		Lexer lexer = new Lexer("\\u0070ublic");
		List<Token> tokens = lexer.tokenize();

		requireThat(tokens.size(), "tokens.size()").isEqualTo(2);
		Token token = tokens.getFirst();
		requireThat(token.type(), "token.type()").isEqualTo(TokenType.PUBLIC);
		requireThat(token.text(), "token.text()").isEqualTo("\\u0070ublic");
		requireThat(token.decodedText(), "token.decodedText()").isEqualTo("public");
	}

	/**
	 * Tests that multiple consecutive 'u' characters in Unicode escapes are valid per JLS 3.3.
	 * The JLS allows \uuuu0041 as a valid escape for 'A'.
	 */
	@Test
	public void testMultipleUnicodeEscapePrefix()
	{
		// \uuuu0041 is valid and decodes to 'A'
		Lexer lexer = new Lexer("\\uuuu0041");
		List<Token> tokens = lexer.tokenize();

		requireThat(tokens.size(), "tokens.size()").isEqualTo(2);
		Token token = tokens.getFirst();
		requireThat(token.type(), "token.type()").isEqualTo(TokenType.IDENTIFIER);
		requireThat(token.text(), "token.text()").isEqualTo("\\uuuu0041");
		requireThat(token.decodedText(), "token.decodedText()").isEqualTo("A");
	}

	/**
	 * Tests that a fully escaped keyword is recognized properly.
	 * The word 'int' is entirely represented with Unicode escapes.
	 */
	@Test
	public void testFullyEscapedKeyword()
	{
		// \u0069\u006e\u0074 = "int"
		Lexer lexer = new Lexer("\\u0069\\u006e\\u0074");
		List<Token> tokens = lexer.tokenize();

		requireThat(tokens.size(), "tokens.size()").isEqualTo(2);
		Token token = tokens.getFirst();
		requireThat(token.type(), "token.type()").isEqualTo(TokenType.INT);
		requireThat(token.text(), "token.text()").isEqualTo("\\u0069\\u006e\\u0074");
		requireThat(token.decodedText(), "token.decodedText()").isEqualTo("int");
	}

	/**
	 * Tests that tokens without Unicode escapes have matching text and decodedText.
	 * For efficiency, both should reference the same String instance.
	 */
	@Test
	@SuppressWarnings("PMD.UseEqualsToCompareStrings")
	public void testNoUnicodeEscapeSharesTextInstance()
	{
		Lexer lexer = new Lexer("myVariable");
		List<Token> tokens = lexer.tokenize();

		Token token = tokens.getFirst();
		requireThat(token.text(), "token.text()").isEqualTo("myVariable");
		// Identity check (==) is intentional - verify same String instance for memory efficiency
		requireThat(token.text() == token.decodedText(), "sameInstance").isTrue();
	}

	/**
	 * Tests Unicode escape at the start of source code that forms an identifier start character.
	 * This validates the lexer properly handles Unicode escapes in the initial character position.
	 */
	@Test
	public void testUnicodeEscapeAtSourceStart()
	{
		// \u0078 is 'x'
		Lexer lexer = new Lexer("\\u0078");
		List<Token> tokens = lexer.tokenize();

		requireThat(tokens.size(), "tokens.size()").isEqualTo(2);
		Token token = tokens.getFirst();
		requireThat(token.type(), "token.type()").isEqualTo(TokenType.IDENTIFIER);
		requireThat(token.text(), "token.text()").isEqualTo("\\u0078");
		requireThat(token.decodedText(), "token.decodedText()").isEqualTo("x");
	}

	/**
	 * Tests Unicode escapes mixed with regular characters in an identifier.
	 * The identifier contains both escaped and non-escaped characters.
	 */
	@Test
	public void testMixedUnicodeEscapeIdentifier()
	{
		// my\u0056ar = "myVar"
		Lexer lexer = new Lexer("my\\u0056ar");
		List<Token> tokens = lexer.tokenize();

		requireThat(tokens.size(), "tokens.size()").isEqualTo(2);
		Token token = tokens.getFirst();
		requireThat(token.type(), "token.type()").isEqualTo(TokenType.IDENTIFIER);
		requireThat(token.text(), "token.text()").isEqualTo("my\\u0056ar");
		requireThat(token.decodedText(), "token.decodedText()").isEqualTo("myVar");
	}

	/**
	 * Tests a variable declaration using Unicode escapes.
	 * Validates that the lexer correctly tokenizes a statement where the identifier
	 * is written with Unicode escapes.
	 */
	@Test
	public void testUnicodeEscapeVariableDeclaration()
	{
		// int \u0041 = 1; where \u0041 is 'A'
		Lexer lexer = new Lexer("int \\u0041 = 1;");
		List<Token> tokens = lexer.tokenize();

		requireThat(tokens.size(), "tokens.size()").isEqualTo(6);
		requireThat(tokens.get(0).type(), "tokens.get(0).type()").isEqualTo(TokenType.INT);
		Token identToken = tokens.get(1);
		requireThat(identToken.type(), "identToken.type()").isEqualTo(TokenType.IDENTIFIER);
		requireThat(identToken.text(), "identToken.text()").isEqualTo("\\u0041");
		requireThat(identToken.decodedText(), "identToken.decodedText()").isEqualTo("A");
		requireThat(tokens.get(2).type(), "tokens.get(2).type()").isEqualTo(TokenType.ASSIGN);
		requireThat(tokens.get(3).type(), "tokens.get(3).type()").isEqualTo(TokenType.INTEGER_LITERAL);
		requireThat(tokens.get(4).type(), "tokens.get(4).type()").isEqualTo(TokenType.SEMICOLON);
	}

	/**
	 * Tests lowercase hex digits in Unicode escapes.
	 * Both uppercase and lowercase hex digits should be valid.
	 */
	@Test
	public void testLowercaseHexInUnicodeEscape()
	{
		// \u006d is 'm' (lowercase hex)
		Lexer lexer = new Lexer("\\u006d");
		List<Token> tokens = lexer.tokenize();

		requireThat(tokens.size(), "tokens.size()").isEqualTo(2);
		Token token = tokens.getFirst();
		requireThat(token.type(), "token.type()").isEqualTo(TokenType.IDENTIFIER);
		requireThat(token.decodedText(), "token.decodedText()").isEqualTo("m");
	}

	/**
	 * Tests uppercase hex digits in Unicode escapes.
	 * Both uppercase and lowercase hex digits should be valid.
	 */
	@Test
	public void testUppercaseHexInUnicodeEscape()
	{
		// \u004D is 'M' (uppercase hex)
		Lexer lexer = new Lexer("\\u004D");
		List<Token> tokens = lexer.tokenize();

		requireThat(tokens.size(), "tokens.size()").isEqualTo(2);
		Token token = tokens.getFirst();
		requireThat(token.type(), "token.type()").isEqualTo(TokenType.IDENTIFIER);
		requireThat(token.decodedText(), "token.decodedText()").isEqualTo("M");
	}
}
