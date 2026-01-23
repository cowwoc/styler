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
	@SuppressWarnings("PMD.UseEqualsToCompareStrings")
	@Test
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

	// ===== Binary literal tests =====

	/**
	 * Verifies that lowercase binary literals are recognized.
	 * Validates that the 0b prefix (lowercase) is correctly identified and the
	 * entire literal including all binary digits is tokenized as a single INTEGER_LITERAL.
	 */
	@Test
	public void testBinaryLiteralLowercase()
	{
		Lexer lexer = new Lexer("0b1010");
		List<Token> tokens = lexer.tokenize();

		requireThat(tokens.size(), "tokens.size()").isEqualTo(2);
		Token token = tokens.get(0);
		requireThat(token.type(), "token.type()").isEqualTo(TokenType.INTEGER_LITERAL);
		requireThat(token.text(), "token.text()").isEqualTo("0b1010");
		requireThat(tokens.get(1).type(), "tokens.get(1).type()").isEqualTo(TokenType.END_OF_FILE);
	}

	/**
	 * Verifies that uppercase binary literals are recognized.
	 * Validates that the 0B prefix (uppercase) is correctly identified and tokenized
	 * as an INTEGER_LITERAL, ensuring case-insensitive prefix handling.
	 */
	@Test
	public void testBinaryLiteralUppercase()
	{
		Lexer lexer = new Lexer("0B1111");
		List<Token> tokens = lexer.tokenize();

		requireThat(tokens.size(), "tokens.size()").isEqualTo(2);
		Token token = tokens.get(0);
		requireThat(token.type(), "token.type()").isEqualTo(TokenType.INTEGER_LITERAL);
		requireThat(token.text(), "token.text()").isEqualTo("0B1111");
		requireThat(tokens.get(1).type(), "tokens.get(1).type()").isEqualTo(TokenType.END_OF_FILE);
	}

	/**
	 * Verifies that binary literals with underscores are recognized.
	 * Validates that numeric separators (underscores) within binary literals are preserved
	 * and the entire literal including separators is tokenized as a single INTEGER_LITERAL.
	 * This tests Java 7+ feature for improved readability of large binary numbers.
	 */
	@Test
	public void testBinaryLiteralWithUnderscores()
	{
		Lexer lexer = new Lexer("0b1010_1100");
		List<Token> tokens = lexer.tokenize();

		requireThat(tokens.size(), "tokens.size()").isEqualTo(2);
		Token token = tokens.get(0);
		requireThat(token.type(), "token.type()").isEqualTo(TokenType.INTEGER_LITERAL);
		requireThat(token.text(), "token.text()").isEqualTo("0b1010_1100");
		requireThat(tokens.get(1).type(), "tokens.get(1).type()").isEqualTo(TokenType.END_OF_FILE);
	}

	/**
	 * Verifies that binary long literals with L suffix are recognized.
	 * Validates that the L suffix is correctly identified and the entire binary literal
	 * including the suffix is tokenized as a single LONG_LITERAL.
	 */
	@Test
	public void testBinaryLongLiteral()
	{
		Lexer lexer = new Lexer("0b1L");
		List<Token> tokens = lexer.tokenize();

		requireThat(tokens.size(), "tokens.size()").isEqualTo(2);
		Token token = tokens.get(0);
		requireThat(token.type(), "token.type()").isEqualTo(TokenType.LONG_LITERAL);
		requireThat(token.text(), "token.text()").isEqualTo("0b1L");
		requireThat(tokens.get(1).type(), "tokens.get(1).type()").isEqualTo(TokenType.END_OF_FILE);
	}

	/**
	 * Verifies that binary literals with multiple digit groups and underscores are recognized.
	 * Validates comprehensive support for binary literals with underscores in various positions,
	 * testing realistic usage patterns for large binary constants.
	 */
	@Test
	public void testBinaryLiteralMultipleUnderscores()
	{
		Lexer lexer = new Lexer("0b1111_0000_1010_1100");
		List<Token> tokens = lexer.tokenize();

		requireThat(tokens.size(), "tokens.size()").isEqualTo(2);
		Token token = tokens.get(0);
		requireThat(token.type(), "token.type()").isEqualTo(TokenType.INTEGER_LITERAL);
		requireThat(token.text(), "token.text()").isEqualTo("0b1111_0000_1010_1100");
		requireThat(tokens.get(1).type(), "tokens.get(1).type()").isEqualTo(TokenType.END_OF_FILE);
	}

	/**
	 * Verifies that binary long literals with underscores and L suffix are recognized.
	 * Validates combined support for numeric separators and the long suffix in binary literals.
	 */
	@Test
	public void testBinaryLongLiteralWithUnderscores()
	{
		Lexer lexer = new Lexer("0b1010_1100L");
		List<Token> tokens = lexer.tokenize();

		requireThat(tokens.size(), "tokens.size()").isEqualTo(2);
		Token token = tokens.get(0);
		requireThat(token.type(), "token.type()").isEqualTo(TokenType.LONG_LITERAL);
		requireThat(token.text(), "token.text()").isEqualTo("0b1010_1100L");
		requireThat(tokens.get(1).type(), "tokens.get(1).type()").isEqualTo(TokenType.END_OF_FILE);
	}

	/**
	 * Verifies that single-bit binary literals are recognized.
	 * Validates edge case handling for minimal binary literals containing a single bit value.
	 */
	@Test
	public void testBinaryLiteralMinimal()
	{
		Lexer lexer = new Lexer("0b0");
		List<Token> tokens = lexer.tokenize();

		requireThat(tokens.size(), "tokens.size()").isEqualTo(2);
		Token token = tokens.get(0);
		requireThat(token.type(), "token.type()").isEqualTo(TokenType.INTEGER_LITERAL);
		requireThat(token.text(), "token.text()").isEqualTo("0b0");
		requireThat(tokens.get(1).type(), "tokens.get(1).type()").isEqualTo(TokenType.END_OF_FILE);
	}

	/**
	 * Verifies that all-ones binary literals are recognized.
	 * Validates edge case handling for binary literals containing only 1 bits.
	 */
	@Test
	public void testBinaryLiteralAllOnes()
	{
		Lexer lexer = new Lexer("0b1111111");
		List<Token> tokens = lexer.tokenize();

		requireThat(tokens.size(), "tokens.size()").isEqualTo(2);
		Token token = tokens.get(0);
		requireThat(token.type(), "token.type()").isEqualTo(TokenType.INTEGER_LITERAL);
		requireThat(token.text(), "token.text()").isEqualTo("0b1111111");
		requireThat(tokens.get(1).type(), "tokens.get(1).type()").isEqualTo(TokenType.END_OF_FILE);
	}

	// ===== Hex literal tests =====

	/**
	 * Verifies that lowercase hexadecimal literals are recognized.
	 * Validates that the 0x prefix (lowercase) is correctly identified and the
	 * entire literal with lowercase hex digits is tokenized as a single INTEGER_LITERAL.
	 */
	@Test
	public void testHexLiteralLowercase()
	{
		Lexer lexer = new Lexer("0xdead");
		List<Token> tokens = lexer.tokenize();

		requireThat(tokens.size(), "tokens.size()").isEqualTo(2);
		Token token = tokens.get(0);
		requireThat(token.type(), "token.type()").isEqualTo(TokenType.INTEGER_LITERAL);
		requireThat(token.text(), "token.text()").isEqualTo("0xdead");
		requireThat(tokens.get(1).type(), "tokens.get(1).type()").isEqualTo(TokenType.END_OF_FILE);
	}

	/**
	 * Verifies that uppercase hexadecimal literals are recognized.
	 * Validates that the 0X prefix (uppercase) is correctly identified and tokenized
	 * as an INTEGER_LITERAL, ensuring case-insensitive prefix handling.
	 */
	@Test
	public void testHexLiteralUppercase()
	{
		Lexer lexer = new Lexer("0XBEEF");
		List<Token> tokens = lexer.tokenize();

		requireThat(tokens.size(), "tokens.size()").isEqualTo(2);
		Token token = tokens.get(0);
		requireThat(token.type(), "token.type()").isEqualTo(TokenType.INTEGER_LITERAL);
		requireThat(token.text(), "token.text()").isEqualTo("0XBEEF");
		requireThat(tokens.get(1).type(), "tokens.get(1).type()").isEqualTo(TokenType.END_OF_FILE);
	}

	/**
	 * Verifies that hexadecimal literals with underscores are recognized.
	 * Validates that numeric separators (underscores) within hex literals are preserved
	 * and the entire literal including separators is tokenized as a single INTEGER_LITERAL.
	 * This tests Java 7+ feature for improved readability of large hex numbers.
	 */
	@Test
	public void testHexLiteralWithUnderscores()
	{
		Lexer lexer = new Lexer("0xDEAD_BEEF");
		List<Token> tokens = lexer.tokenize();

		requireThat(tokens.size(), "tokens.size()").isEqualTo(2);
		Token token = tokens.get(0);
		requireThat(token.type(), "token.type()").isEqualTo(TokenType.INTEGER_LITERAL);
		requireThat(token.text(), "token.text()").isEqualTo("0xDEAD_BEEF");
		requireThat(tokens.get(1).type(), "tokens.get(1).type()").isEqualTo(TokenType.END_OF_FILE);
	}

	/**
	 * Verifies that hexadecimal long literals with L suffix are recognized.
	 * Validates that the L suffix is correctly identified and the entire hex literal
	 * including the suffix is tokenized as a single LONG_LITERAL.
	 */
	@Test
	public void testHexLongLiteral()
	{
		Lexer lexer = new Lexer("0xFFL");
		List<Token> tokens = lexer.tokenize();

		requireThat(tokens.size(), "tokens.size()").isEqualTo(2);
		Token token = tokens.get(0);
		requireThat(token.type(), "token.type()").isEqualTo(TokenType.LONG_LITERAL);
		requireThat(token.text(), "token.text()").isEqualTo("0xFFL");
		requireThat(tokens.get(1).type(), "tokens.get(1).type()").isEqualTo(TokenType.END_OF_FILE);
	}

	/**
	 * Verifies that hexadecimal literals with mixed case hex digits are recognized.
	 * Validates that lexer preserves the original case of hex digits (a-f)
	 * when mixed with uppercase and lowercase prefix variations.
	 */
	@Test
	public void testHexLiteralMixedCase()
	{
		Lexer lexer = new Lexer("0xAbCdEf");
		List<Token> tokens = lexer.tokenize();

		requireThat(tokens.size(), "tokens.size()").isEqualTo(2);
		Token token = tokens.get(0);
		requireThat(token.type(), "token.type()").isEqualTo(TokenType.INTEGER_LITERAL);
		requireThat(token.text(), "token.text()").isEqualTo("0xAbCdEf");
		requireThat(tokens.get(1).type(), "tokens.get(1).type()").isEqualTo(TokenType.END_OF_FILE);
	}

	/**
	 * Verifies that hexadecimal literals with multiple underscore groups are recognized.
	 * Validates comprehensive support for hex literals with underscores in various positions,
	 * testing realistic usage patterns for large hex constants.
	 */
	@Test
	public void testHexLiteralMultipleUnderscores()
	{
		Lexer lexer = new Lexer("0xFF_FF_FF_FF");
		List<Token> tokens = lexer.tokenize();

		requireThat(tokens.size(), "tokens.size()").isEqualTo(2);
		Token token = tokens.get(0);
		requireThat(token.type(), "token.type()").isEqualTo(TokenType.INTEGER_LITERAL);
		requireThat(token.text(), "token.text()").isEqualTo("0xFF_FF_FF_FF");
		requireThat(tokens.get(1).type(), "tokens.get(1).type()").isEqualTo(TokenType.END_OF_FILE);
	}

	/**
	 * Verifies that hexadecimal long literals with underscores and L suffix are recognized.
	 * Validates combined support for numeric separators and the long suffix in hex literals.
	 */
	@Test
	public void testHexLongLiteralWithUnderscores()
	{
		Lexer lexer = new Lexer("0xDEAD_BEEFL");
		List<Token> tokens = lexer.tokenize();

		requireThat(tokens.size(), "tokens.size()").isEqualTo(2);
		Token token = tokens.get(0);
		requireThat(token.type(), "token.type()").isEqualTo(TokenType.LONG_LITERAL);
		requireThat(token.text(), "token.text()").isEqualTo("0xDEAD_BEEFL");
		requireThat(tokens.get(1).type(), "tokens.get(1).type()").isEqualTo(TokenType.END_OF_FILE);
	}

	/**
	 * Verifies that single-digit hexadecimal literals are recognized.
	 * Validates edge case handling for minimal hex literals containing a single hex digit.
	 */
	@Test
	public void testHexLiteralMinimal()
	{
		Lexer lexer = new Lexer("0x0");
		List<Token> tokens = lexer.tokenize();

		requireThat(tokens.size(), "tokens.size()").isEqualTo(2);
		Token token = tokens.get(0);
		requireThat(token.type(), "token.type()").isEqualTo(TokenType.INTEGER_LITERAL);
		requireThat(token.text(), "token.text()").isEqualTo("0x0");
		requireThat(tokens.get(1).type(), "tokens.get(1).type()").isEqualTo(TokenType.END_OF_FILE);
	}

	/**
	 * Verifies that hexadecimal literals with all F digits are recognized.
	 * Validates edge case handling for hex literals containing maximum digit values.
	 */
	@Test
	public void testHexLiteralAllFs()
	{
		Lexer lexer = new Lexer("0xFFFFFFFF");
		List<Token> tokens = lexer.tokenize();

		requireThat(tokens.size(), "tokens.size()").isEqualTo(2);
		Token token = tokens.get(0);
		requireThat(token.type(), "token.type()").isEqualTo(TokenType.INTEGER_LITERAL);
		requireThat(token.text(), "token.text()").isEqualTo("0xFFFFFFFF");
		requireThat(tokens.get(1).type(), "tokens.get(1).type()").isEqualTo(TokenType.END_OF_FILE);
	}

	/**
	 * Verifies that hexadecimal literals with only decimal digits are recognized.
	 * Validates that hex digits composed of only 0-9 (without a-f) are correctly identified.
	 */
	@Test
	public void testHexLiteralDecimalDigitsOnly()
	{
		Lexer lexer = new Lexer("0x123");
		List<Token> tokens = lexer.tokenize();

		requireThat(tokens.size(), "tokens.size()").isEqualTo(2);
		Token token = tokens.get(0);
		requireThat(token.type(), "token.type()").isEqualTo(TokenType.INTEGER_LITERAL);
		requireThat(token.text(), "token.text()").isEqualTo("0x123");
		requireThat(tokens.get(1).type(), "tokens.get(1).type()").isEqualTo(TokenType.END_OF_FILE);
	}

	// ===== Hex float literal tests =====

	/**
	 * Verifies that basic hexadecimal float literals with exponent are recognized.
	 * Validates that hex float literals with the p/P exponent notation are correctly
	 * identified and tokenized as DOUBLE_LITERAL (default float type).
	 * This tests Java's hex float format: 0x{hex-digits}p{exponent}.
	 */
	@Test
	public void testHexFloatLiteral()
	{
		Lexer lexer = new Lexer("0x1p10");
		List<Token> tokens = lexer.tokenize();

		requireThat(tokens.size(), "tokens.size()").isEqualTo(2);
		Token token = tokens.get(0);
		requireThat(token.type(), "token.type()").isEqualTo(TokenType.DOUBLE_LITERAL);
		requireThat(token.text(), "token.text()").isEqualTo("0x1p10");
		requireThat(tokens.get(1).type(), "tokens.get(1).type()").isEqualTo(TokenType.END_OF_FILE);
	}

	/**
	 * Verifies that hexadecimal float literals with float suffix are recognized.
	 * Validates that the f/F suffix correctly identifies hex float literals
	 * as FLOAT_LITERAL rather than DOUBLE_LITERAL.
	 */
	@Test
	public void testHexFloatLiteralWithFloatSuffix()
	{
		Lexer lexer = new Lexer("0x1p1f");
		List<Token> tokens = lexer.tokenize();

		requireThat(tokens.size(), "tokens.size()").isEqualTo(2);
		Token token = tokens.get(0);
		requireThat(token.type(), "token.type()").isEqualTo(TokenType.FLOAT_LITERAL);
		requireThat(token.text(), "token.text()").isEqualTo("0x1p1f");
		requireThat(tokens.get(1).type(), "tokens.get(1).type()").isEqualTo(TokenType.END_OF_FILE);
	}

	/**
	 * Verifies that hexadecimal float literals with negative exponent are recognized.
	 * Validates that the p notation with minus sign in the exponent is correctly handled
	 * and tokenized as a single DOUBLE_LITERAL token.
	 */
	@Test
	public void testHexFloatLiteralNegativeExponent()
	{
		Lexer lexer = new Lexer("0x1p-10");
		List<Token> tokens = lexer.tokenize();

		requireThat(tokens.size(), "tokens.size()").isEqualTo(2);
		Token token = tokens.get(0);
		requireThat(token.type(), "token.type()").isEqualTo(TokenType.DOUBLE_LITERAL);
		requireThat(token.text(), "token.text()").isEqualTo("0x1p-10");
		requireThat(tokens.get(1).type(), "tokens.get(1).type()").isEqualTo(TokenType.END_OF_FILE);
	}

	/**
	 * Verifies that hexadecimal float literals with positive exponent are recognized.
	 * Validates that the p notation with plus sign in the exponent is correctly handled
	 * and tokenized as a single DOUBLE_LITERAL token.
	 */
	@Test
	public void testHexFloatLiteralPositiveExponent()
	{
		Lexer lexer = new Lexer("0x1p+10");
		List<Token> tokens = lexer.tokenize();

		requireThat(tokens.size(), "tokens.size()").isEqualTo(2);
		Token token = tokens.get(0);
		requireThat(token.type(), "token.type()").isEqualTo(TokenType.DOUBLE_LITERAL);
		requireThat(token.text(), "token.text()").isEqualTo("0x1p+10");
		requireThat(tokens.get(1).type(), "tokens.get(1).type()").isEqualTo(TokenType.END_OF_FILE);
	}

	/**
	 * Verifies that hexadecimal float literals with fractional part are recognized.
	 * Validates that hex floats with decimal point separating integer and fractional
	 * hex digits are correctly tokenized as DOUBLE_LITERAL.
	 */
	@Test
	public void testHexFloatLiteralWithFraction()
	{
		Lexer lexer = new Lexer("0x1.8p1");
		List<Token> tokens = lexer.tokenize();

		requireThat(tokens.size(), "tokens.size()").isEqualTo(2);
		Token token = tokens.get(0);
		requireThat(token.type(), "token.type()").isEqualTo(TokenType.DOUBLE_LITERAL);
		requireThat(token.text(), "token.text()").isEqualTo("0x1.8p1");
		requireThat(tokens.get(1).type(), "tokens.get(1).type()").isEqualTo(TokenType.END_OF_FILE);
	}

	/**
	 * Verifies that uppercase P exponent notation is recognized in hex float literals.
	 * Validates that both lowercase p and uppercase P are accepted for the exponent indicator.
	 */
	@Test
	public void testHexFloatLiteralUppercaseP()
	{
		Lexer lexer = new Lexer("0x1P10");
		List<Token> tokens = lexer.tokenize();

		requireThat(tokens.size(), "tokens.size()").isEqualTo(2);
		Token token = tokens.get(0);
		requireThat(token.type(), "token.type()").isEqualTo(TokenType.DOUBLE_LITERAL);
		requireThat(token.text(), "token.text()").isEqualTo("0x1P10");
		requireThat(tokens.get(1).type(), "tokens.get(1).type()").isEqualTo(TokenType.END_OF_FILE);
	}

	/**
	 * Verifies that hexadecimal float literals with uppercase D suffix are recognized.
	 * Validates that the D suffix explicitly marks the literal as DOUBLE_LITERAL.
	 */
	@Test
	public void testHexFloatLiteralWithDoubleSuffix()
	{
		Lexer lexer = new Lexer("0x1p1d");
		List<Token> tokens = lexer.tokenize();

		requireThat(tokens.size(), "tokens.size()").isEqualTo(2);
		Token token = tokens.get(0);
		requireThat(token.type(), "token.type()").isEqualTo(TokenType.DOUBLE_LITERAL);
		requireThat(token.text(), "token.text()").isEqualTo("0x1p1d");
		requireThat(tokens.get(1).type(), "tokens.get(1).type()").isEqualTo(TokenType.END_OF_FILE);
	}

	/**
	 * Verifies that complex hexadecimal float literals with multiple fractional digits are recognized.
	 * Validates comprehensive support for hex floats with extended fractional parts.
	 */
	@Test
	public void testHexFloatLiteralComplexFraction()
	{
		Lexer lexer = new Lexer("0xA.Bp5");
		List<Token> tokens = lexer.tokenize();

		requireThat(tokens.size(), "tokens.size()").isEqualTo(2);
		Token token = tokens.get(0);
		requireThat(token.type(), "token.type()").isEqualTo(TokenType.DOUBLE_LITERAL);
		requireThat(token.text(), "token.text()").isEqualTo("0xA.Bp5");
		requireThat(tokens.get(1).type(), "tokens.get(1).type()").isEqualTo(TokenType.END_OF_FILE);
	}

	/**
	 * Verifies that hexadecimal float literals with zero exponent are recognized.
	 * Validates edge case handling for hex floats with p0 exponent.
	 */
	@Test
	public void testHexFloatLiteralZeroExponent()
	{
		Lexer lexer = new Lexer("0x1p0");
		List<Token> tokens = lexer.tokenize();

		requireThat(tokens.size(), "tokens.size()").isEqualTo(2);
		Token token = tokens.get(0);
		requireThat(token.type(), "token.type()").isEqualTo(TokenType.DOUBLE_LITERAL);
		requireThat(token.text(), "token.text()").isEqualTo("0x1p0");
		requireThat(tokens.get(1).type(), "tokens.get(1).type()").isEqualTo(TokenType.END_OF_FILE);
	}

	/**
	 * Verifies that hexadecimal float literals with only fractional part (no integer part) are recognized.
	 * Validates edge case handling for hex floats like 0x.1p1.
	 */
	@Test
	public void testHexFloatLiteralOnlyFractional()
	{
		Lexer lexer = new Lexer("0x.1p1");
		List<Token> tokens = lexer.tokenize();

		requireThat(tokens.size(), "tokens.size()").isEqualTo(2);
		Token token = tokens.get(0);
		requireThat(token.type(), "token.type()").isEqualTo(TokenType.DOUBLE_LITERAL);
		requireThat(token.text(), "token.text()").isEqualTo("0x.1p1");
		requireThat(tokens.get(1).type(), "tokens.get(1).type()").isEqualTo(TokenType.END_OF_FILE);
	}

	/**
	 * Verifies that hexadecimal float literals with uppercase hex digits are recognized.
	 * Validates correct handling of uppercase A-F digits in hex float literals.
	 */
	@Test
	public void testHexFloatLiteralUppercaseHexDigits()
	{
		Lexer lexer = new Lexer("0xF.Cp2");
		List<Token> tokens = lexer.tokenize();

		requireThat(tokens.size(), "tokens.size()").isEqualTo(2);
		Token token = tokens.get(0);
		requireThat(token.type(), "token.type()").isEqualTo(TokenType.DOUBLE_LITERAL);
		requireThat(token.text(), "token.text()").isEqualTo("0xF.Cp2");
		requireThat(tokens.get(1).type(), "tokens.get(1).type()").isEqualTo(TokenType.END_OF_FILE);
	}

	/**
	 * Verifies that hexadecimal float literals with mixed case hex digits are recognized.
	 * Validates correct handling of mixed uppercase and lowercase hex digits in float literals.
	 */
	@Test
	public void testHexFloatLiteralMixedCase()
	{
		Lexer lexer = new Lexer("0xaB.cDp3");
		List<Token> tokens = lexer.tokenize();

		requireThat(tokens.size(), "tokens.size()").isEqualTo(2);
		Token token = tokens.get(0);
		requireThat(token.type(), "token.type()").isEqualTo(TokenType.DOUBLE_LITERAL);
		requireThat(token.text(), "token.text()").isEqualTo("0xaB.cDp3");
		requireThat(tokens.get(1).type(), "tokens.get(1).type()").isEqualTo(TokenType.END_OF_FILE);
	}

	// ===== Octal literal tests =====

	/**
	 * Verifies that octal literals are recognized.
	 * Validates that numeric values prefixed with 0 and containing only octal digits (0-7)
	 * are correctly identified and tokenized as a single INTEGER_LITERAL.
	 * This tests traditional octal literal format from Java.
	 */
	@Test
	public void testOctalLiteral()
	{
		Lexer lexer = new Lexer("0755");
		List<Token> tokens = lexer.tokenize();

		requireThat(tokens.size(), "tokens.size()").isEqualTo(2);
		Token token = tokens.get(0);
		requireThat(token.type(), "token.type()").isEqualTo(TokenType.INTEGER_LITERAL);
		requireThat(token.text(), "token.text()").isEqualTo("0755");
		requireThat(tokens.get(1).type(), "tokens.get(1).type()").isEqualTo(TokenType.END_OF_FILE);
	}

	/**
	 * Verifies that octal literals with underscores are recognized.
	 * Validates that numeric separators (underscores) within octal literals are preserved
	 * and the entire literal including separators is tokenized as a single INTEGER_LITERAL.
	 * This tests Java 7+ feature for improved readability of large octal numbers.
	 */
	@Test
	public void testOctalLiteralWithUnderscores()
	{
		Lexer lexer = new Lexer("0_77");
		List<Token> tokens = lexer.tokenize();

		requireThat(tokens.size(), "tokens.size()").isEqualTo(2);
		Token token = tokens.get(0);
		requireThat(token.type(), "token.type()").isEqualTo(TokenType.INTEGER_LITERAL);
		requireThat(token.text(), "token.text()").isEqualTo("0_77");
		requireThat(tokens.get(1).type(), "tokens.get(1).type()").isEqualTo(TokenType.END_OF_FILE);
	}

	/**
	 * Verifies that octal long literals with L suffix are recognized.
	 * Validates that the L suffix is correctly identified and the entire octal literal
	 * including the suffix is tokenized as a single LONG_LITERAL.
	 */
	@Test
	public void testOctalLongLiteral()
	{
		Lexer lexer = new Lexer("0777L");
		List<Token> tokens = lexer.tokenize();

		requireThat(tokens.size(), "tokens.size()").isEqualTo(2);
		Token token = tokens.get(0);
		requireThat(token.type(), "token.type()").isEqualTo(TokenType.LONG_LITERAL);
		requireThat(token.text(), "token.text()").isEqualTo("0777L");
		requireThat(tokens.get(1).type(), "tokens.get(1).type()").isEqualTo(TokenType.END_OF_FILE);
	}

	/**
	 * Verifies that octal literals with multiple underscore groups are recognized.
	 * Validates comprehensive support for octal literals with underscores in various positions,
	 * testing realistic usage patterns for large octal constants.
	 */
	@Test
	public void testOctalLiteralMultipleUnderscores()
	{
		Lexer lexer = new Lexer("0_777_777_777");
		List<Token> tokens = lexer.tokenize();

		requireThat(tokens.size(), "tokens.size()").isEqualTo(2);
		Token token = tokens.get(0);
		requireThat(token.type(), "token.type()").isEqualTo(TokenType.INTEGER_LITERAL);
		requireThat(token.text(), "token.text()").isEqualTo("0_777_777_777");
		requireThat(tokens.get(1).type(), "tokens.get(1).type()").isEqualTo(TokenType.END_OF_FILE);
	}

	/**
	 * Verifies that octal long literals with underscores and L suffix are recognized.
	 * Validates combined support for numeric separators and the long suffix in octal literals.
	 */
	@Test
	public void testOctalLongLiteralWithUnderscores()
	{
		Lexer lexer = new Lexer("0_777_777L");
		List<Token> tokens = lexer.tokenize();

		requireThat(tokens.size(), "tokens.size()").isEqualTo(2);
		Token token = tokens.get(0);
		requireThat(token.type(), "token.type()").isEqualTo(TokenType.LONG_LITERAL);
		requireThat(token.text(), "token.text()").isEqualTo("0_777_777L");
		requireThat(tokens.get(1).type(), "tokens.get(1).type()").isEqualTo(TokenType.END_OF_FILE);
	}

	/**
	 * Verifies that single-zero octal literals are recognized.
	 * Validates edge case handling for minimal octal literals.
	 */
	@Test
	public void testOctalLiteralZero()
	{
		Lexer lexer = new Lexer("0");
		List<Token> tokens = lexer.tokenize();

		requireThat(tokens.size(), "tokens.size()").isEqualTo(2);
		Token token = tokens.get(0);
		requireThat(token.type(), "token.type()").isEqualTo(TokenType.INTEGER_LITERAL);
		requireThat(token.text(), "token.text()").isEqualTo("0");
		requireThat(tokens.get(1).type(), "tokens.get(1).type()").isEqualTo(TokenType.END_OF_FILE);
	}

	/**
	 * Verifies that maximum octal literals with all 7 digits are recognized.
	 * Validates edge case handling for octal literals containing maximum octal digit values.
	 */
	@Test
	public void testOctalLiteralAll7s()
	{
		Lexer lexer = new Lexer("07777777");
		List<Token> tokens = lexer.tokenize();

		requireThat(tokens.size(), "tokens.size()").isEqualTo(2);
		Token token = tokens.get(0);
		requireThat(token.type(), "token.type()").isEqualTo(TokenType.INTEGER_LITERAL);
		requireThat(token.text(), "token.text()").isEqualTo("07777777");
		requireThat(tokens.get(1).type(), "tokens.get(1).type()").isEqualTo(TokenType.END_OF_FILE);
	}

	/**
	 * Verifies that octal literals with underscores after leading zero are recognized.
	 * Validates correct parsing of 0_... pattern with underscores immediately following the leading zero.
	 */
	@Test
	public void testOctalLiteralUnderscoreAfterZero()
	{
		Lexer lexer = new Lexer("0_1");
		List<Token> tokens = lexer.tokenize();

		requireThat(tokens.size(), "tokens.size()").isEqualTo(2);
		Token token = tokens.get(0);
		requireThat(token.type(), "token.type()").isEqualTo(TokenType.INTEGER_LITERAL);
		requireThat(token.text(), "token.text()").isEqualTo("0_1");
		requireThat(tokens.get(1).type(), "tokens.get(1).type()").isEqualTo(TokenType.END_OF_FILE);
	}

	/**
	 * Verifies that various octal digit combinations are recognized.
	 * Validates support for octal literals using different combinations of valid octal digits (0-7).
	 */
	@Test
	public void testOctalLiteralVariousDigits()
	{
		Lexer lexer = new Lexer("01234567");
		List<Token> tokens = lexer.tokenize();

		requireThat(tokens.size(), "tokens.size()").isEqualTo(2);
		Token token = tokens.get(0);
		requireThat(token.type(), "token.type()").isEqualTo(TokenType.INTEGER_LITERAL);
		requireThat(token.text(), "token.text()").isEqualTo("01234567");
		requireThat(tokens.get(1).type(), "tokens.get(1).type()").isEqualTo(TokenType.END_OF_FILE);
	}

	// ===== Floating point without leading zero tests =====

	/**
	 * Verifies that a simple decimal without leading zero is recognized.
	 */
	@Test
	public void testFloatingPointSimpleDecimal()
	{
		Lexer lexer = new Lexer(".5");
		List<Token> tokens = lexer.tokenize();

		requireThat(tokens.size(), "tokens.size()").isEqualTo(2);
		Token token = tokens.get(0);
		requireThat(token.type(), "token.type()").isEqualTo(TokenType.DOUBLE_LITERAL);
		requireThat(token.text(), "token.text()").isEqualTo(".5");
		requireThat(tokens.get(1).type(), "tokens.get(1).type()").isEqualTo(TokenType.END_OF_FILE);
	}

	/**
	 * Verifies that a decimal with multiple digits is recognized.
	 */
	@Test
	public void testFloatingPointMultipleDigits()
	{
		Lexer lexer = new Lexer(".0025");
		List<Token> tokens = lexer.tokenize();

		requireThat(tokens.size(), "tokens.size()").isEqualTo(2);
		Token token = tokens.get(0);
		requireThat(token.type(), "token.type()").isEqualTo(TokenType.DOUBLE_LITERAL);
		requireThat(token.text(), "token.text()").isEqualTo(".0025");
	}

	/**
	 * Verifies that a decimal with exponent is recognized.
	 */
	@Test
	public void testFloatingPointWithExponent()
	{
		Lexer lexer = new Lexer(".5e10");
		List<Token> tokens = lexer.tokenize();

		requireThat(tokens.size(), "tokens.size()").isEqualTo(2);
		Token token = tokens.get(0);
		requireThat(token.type(), "token.type()").isEqualTo(TokenType.DOUBLE_LITERAL);
		requireThat(token.text(), "token.text()").isEqualTo(".5e10");
	}

	/**
	 * Verifies that a decimal with negative exponent is recognized.
	 */
	@Test
	public void testFloatingPointWithNegativeExponent()
	{
		Lexer lexer = new Lexer(".5e-3");
		List<Token> tokens = lexer.tokenize();

		requireThat(tokens.size(), "tokens.size()").isEqualTo(2);
		Token token = tokens.get(0);
		requireThat(token.type(), "token.type()").isEqualTo(TokenType.DOUBLE_LITERAL);
		requireThat(token.text(), "token.text()").isEqualTo(".5e-3");
	}

	/**
	 * Verifies that a decimal with float suffix is recognized.
	 */
	@Test
	public void testFloatingPointWithFloatSuffix()
	{
		Lexer lexer = new Lexer(".5f");
		List<Token> tokens = lexer.tokenize();

		requireThat(tokens.size(), "tokens.size()").isEqualTo(2);
		Token token = tokens.get(0);
		requireThat(token.type(), "token.type()").isEqualTo(TokenType.FLOAT_LITERAL);
		requireThat(token.text(), "token.text()").isEqualTo(".5f");
	}

	/**
	 * Verifies that a decimal with double suffix is recognized.
	 */
	@Test
	public void testFloatingPointWithDoubleSuffix()
	{
		Lexer lexer = new Lexer(".5d");
		List<Token> tokens = lexer.tokenize();

		requireThat(tokens.size(), "tokens.size()").isEqualTo(2);
		Token token = tokens.get(0);
		requireThat(token.type(), "token.type()").isEqualTo(TokenType.DOUBLE_LITERAL);
		requireThat(token.text(), "token.text()").isEqualTo(".5d");
	}

	/**
	 * Verifies that underscores are allowed in the fractional part.
	 */
	@Test
	public void testFloatingPointWithUnderscores()
	{
		Lexer lexer = new Lexer(".123_456");
		List<Token> tokens = lexer.tokenize();

		requireThat(tokens.size(), "tokens.size()").isEqualTo(2);
		Token token = tokens.get(0);
		requireThat(token.type(), "token.type()").isEqualTo(TokenType.DOUBLE_LITERAL);
		requireThat(token.text(), "token.text()").isEqualTo(".123_456");
	}

	/**
	 * Verifies that a DOT not followed by a digit remains a DOT token.
	 */
	@Test
	public void testDotNotFollowedByDigit()
	{
		Lexer lexer = new Lexer("a.b");
		List<Token> tokens = lexer.tokenize();

		requireThat(tokens.size(), "tokens.size()").isEqualTo(4);
		requireThat(tokens.get(0).type(), "tokens.get(0).type()").isEqualTo(TokenType.IDENTIFIER);
		requireThat(tokens.get(1).type(), "tokens.get(1).type()").isEqualTo(TokenType.DOT);
		requireThat(tokens.get(2).type(), "tokens.get(2).type()").isEqualTo(TokenType.IDENTIFIER);
	}

	/**
	 * Verifies parsing in a method call context (the original error case).
	 */
	@Test
	public void testFloatingPointInMethodCallContext()
	{
		Lexer lexer = new Lexer("setSomeDouble(.0025)");
		List<Token> tokens = lexer.tokenize();

		requireThat(tokens.size(), "tokens.size()").isEqualTo(5);
		requireThat(tokens.get(0).type(), "tokens.get(0).type()").isEqualTo(TokenType.IDENTIFIER);
		requireThat(tokens.get(1).type(), "tokens.get(1).type()").isEqualTo(TokenType.LEFT_PARENTHESIS);
		requireThat(tokens.get(2).type(), "tokens.get(2).type()").isEqualTo(TokenType.DOUBLE_LITERAL);
		requireThat(tokens.get(2).text(), "tokens.get(2).text()").isEqualTo(".0025");
		requireThat(tokens.get(3).type(), "tokens.get(3).type()").isEqualTo(TokenType.RIGHT_PARENTHESIS);
	}

	// ===== Octal escape tests =====

	/**
	 * Validates lexing of a character literal with single-digit octal escape for NUL character.
	 */
	@Test
	public void testCharLiteralWithSingleDigitOctalNul()
	{
		String source = "'\\0'";
		Lexer lexer = new Lexer(source);
		List<Token> tokens = lexer.tokenize();

		requireThat(tokens.size(), "tokens.size()").isEqualTo(2);
		Token token = tokens.get(0);
		requireThat(token.type(), "token.type()").isEqualTo(TokenType.CHAR_LITERAL);
		requireThat(token.text(), "token.text()").isEqualTo("'\\0'");
		requireThat(token.start(), "token.start()").isEqualTo(0);
		requireThat(token.end(), "token.end()").isEqualTo(4);
		requireThat(tokens.get(1).type(), "tokens.get(1).type()").isEqualTo(TokenType.END_OF_FILE);
	}

	/**
	 * Validates lexing of a character literal with single-digit octal escape at max value (7).
	 */
	@Test
	public void testCharLiteralWithSingleDigitOctalMax()
	{
		String source = "'\\7'";
		Lexer lexer = new Lexer(source);
		List<Token> tokens = lexer.tokenize();

		requireThat(tokens.size(), "tokens.size()").isEqualTo(2);
		Token token = tokens.get(0);
		requireThat(token.type(), "token.type()").isEqualTo(TokenType.CHAR_LITERAL);
		requireThat(token.text(), "token.text()").isEqualTo("'\\7'");
		requireThat(token.start(), "token.start()").isEqualTo(0);
		requireThat(token.end(), "token.end()").isEqualTo(4);
		requireThat(tokens.get(1).type(), "tokens.get(1).type()").isEqualTo(TokenType.END_OF_FILE);
	}

	/**
	 * Validates lexing of a character literal with two-digit octal escape.
	 */
	@Test
	public void testCharLiteralWithTwoDigitOctal()
	{
		String source = "'\\12'";
		Lexer lexer = new Lexer(source);
		List<Token> tokens = lexer.tokenize();

		requireThat(tokens.size(), "tokens.size()").isEqualTo(2);
		Token token = tokens.get(0);
		requireThat(token.type(), "token.type()").isEqualTo(TokenType.CHAR_LITERAL);
		requireThat(token.text(), "token.text()").isEqualTo("'\\12'");
		requireThat(token.start(), "token.start()").isEqualTo(0);
		requireThat(token.end(), "token.end()").isEqualTo(5);
		requireThat(tokens.get(1).type(), "tokens.get(1).type()").isEqualTo(TokenType.END_OF_FILE);
	}

	/**
	 * Validates lexing of a character literal with three-digit octal escape starting with 0.
	 */
	@Test
	public void testCharLiteralWithThreeDigitOctal()
	{
		String source = "'\\013'";
		Lexer lexer = new Lexer(source);
		List<Token> tokens = lexer.tokenize();

		requireThat(tokens.size(), "tokens.size()").isEqualTo(2);
		Token token = tokens.get(0);
		requireThat(token.type(), "token.type()").isEqualTo(TokenType.CHAR_LITERAL);
		requireThat(token.text(), "token.text()").isEqualTo("'\\013'");
		requireThat(token.start(), "token.start()").isEqualTo(0);
		requireThat(token.end(), "token.end()").isEqualTo(6);
		requireThat(tokens.get(1).type(), "tokens.get(1).type()").isEqualTo(TokenType.END_OF_FILE);
	}

	/**
	 * Validates lexing of a character literal with maximum octal value (377 = 255 decimal).
	 */
	@Test
	public void testCharLiteralWithMaxOctalValue()
	{
		String source = "'\\377'";
		Lexer lexer = new Lexer(source);
		List<Token> tokens = lexer.tokenize();

		requireThat(tokens.size(), "tokens.size()").isEqualTo(2);
		Token token = tokens.get(0);
		requireThat(token.type(), "token.type()").isEqualTo(TokenType.CHAR_LITERAL);
		requireThat(token.text(), "token.text()").isEqualTo("'\\377'");
		requireThat(token.start(), "token.start()").isEqualTo(0);
		requireThat(token.end(), "token.end()").isEqualTo(6);
		requireThat(tokens.get(1).type(), "tokens.get(1).type()").isEqualTo(TokenType.END_OF_FILE);
	}

	/**
	 * Validates lexing of a character literal with high first digit (4-7) limiting to two digits.
	 */
	@Test
	public void testCharLiteralWithHighFirstDigitOctal()
	{
		String source = "'\\47'";
		Lexer lexer = new Lexer(source);
		List<Token> tokens = lexer.tokenize();

		requireThat(tokens.size(), "tokens.size()").isEqualTo(2);
		Token token = tokens.get(0);
		requireThat(token.type(), "token.type()").isEqualTo(TokenType.CHAR_LITERAL);
		requireThat(token.text(), "token.text()").isEqualTo("'\\47'");
		requireThat(token.start(), "token.start()").isEqualTo(0);
		requireThat(token.end(), "token.end()").isEqualTo(5);
		requireThat(tokens.get(1).type(), "tokens.get(1).type()").isEqualTo(TokenType.END_OF_FILE);
	}

	/**
	 * Validates lexing of a string literal containing an octal escape sequence.
	 */
	@Test
	public void testStringLiteralWithOctalEscape()
	{
		String source = "\"Hello\\012World\"";
		Lexer lexer = new Lexer(source);
		List<Token> tokens = lexer.tokenize();

		requireThat(tokens.size(), "tokens.size()").isEqualTo(2);
		Token token = tokens.get(0);
		requireThat(token.type(), "token.type()").isEqualTo(TokenType.STRING_LITERAL);
		requireThat(token.text(), "token.text()").isEqualTo("\"Hello\\012World\"");
		requireThat(tokens.get(1).type(), "tokens.get(1).type()").isEqualTo(TokenType.END_OF_FILE);
	}

	/**
	 * Validates lexing of a pattern found in Spring Framework source code.
	 */
	@Test
	public void testSpringFrameworkOctalPattern()
	{
		String source = "sb.append('\\013')";
		Lexer lexer = new Lexer(source);
		List<Token> tokens = lexer.tokenize();

		requireThat(tokens.size(), "tokens.size()").isEqualTo(7);
		Token charLiteral = tokens.get(4);
		requireThat(charLiteral.type(), "charLiteral.type()").isEqualTo(TokenType.CHAR_LITERAL);
		requireThat(charLiteral.text(), "charLiteral.text()").isEqualTo("'\\013'");
		requireThat(tokens.get(5).type(), "tokens.get(5).type()").isEqualTo(TokenType.RIGHT_PARENTHESIS);
	}

	/**
	 * Validates that octal escape correctly stops at non-octal digit (8 or 9).
	 */
	@Test
	public void testOctalEscapeFollowedByNonOctalDigit()
	{
		String source = "\"\\09\"";
		Lexer lexer = new Lexer(source);
		List<Token> tokens = lexer.tokenize();

		requireThat(tokens.size(), "tokens.size()").isEqualTo(2);
		Token token = tokens.get(0);
		requireThat(token.type(), "token.type()").isEqualTo(TokenType.STRING_LITERAL);
		requireThat(token.text(), "token.text()").isEqualTo("\"\\09\"");
		requireThat(tokens.get(1).type(), "tokens.get(1).type()").isEqualTo(TokenType.END_OF_FILE);
	}

	// ===== Unicode escape in literals tests =====

	/**
	 * Tests character literal with valid Unicode escape sequence.
	 * Validates that Unicode escape sequences (backslash-u plus 4 hex digits) in character literals are
	 * recognized as complete token text without interpretation.
	 */
	@Test
	public void testCharLiteralWithUnicodeEscape()
	{
		String source = "'\\u0041'";
		Lexer lexer = new Lexer(source);
		List<Token> tokens = lexer.tokenize();

		requireThat(tokens.size(), "tokens.size()").isEqualTo(2);
		Token token = tokens.get(0);
		requireThat(token.type(), "token.type()").isEqualTo(TokenType.CHAR_LITERAL);
		requireThat(token.text(), "token.text()").isEqualTo("'\\u0041'");
		requireThat(token.start(), "token.start()").isEqualTo(0);
		requireThat(token.end(), "token.end()").isEqualTo(8);
		requireThat(tokens.get(1).type(), "tokens.get(1).type()").isEqualTo(TokenType.END_OF_FILE);
	}

	/**
	 * Tests character literal with Unicode replacement character.
	 * Validates that U+FFFD (replacement character) in escape sequences is properly handled.
	 */
	@Test
	public void testCharLiteralWithReplacementCharacter()
	{
		String source = "'\\uFFFD'";
		Lexer lexer = new Lexer(source);
		List<Token> tokens = lexer.tokenize();

		requireThat(tokens.size(), "tokens.size()").isEqualTo(2);
		Token token = tokens.get(0);
		requireThat(token.type(), "token.type()").isEqualTo(TokenType.CHAR_LITERAL);
		requireThat(token.text(), "token.text()").isEqualTo("'\\uFFFD'");
		requireThat(tokens.get(1).type(), "tokens.get(1).type()").isEqualTo(TokenType.END_OF_FILE);
	}

	/**
	 * Tests string literal with single Unicode escape sequence.
	 * Validates that Unicode escapes are preserved as token text in string literals.
	 */
	@Test
	public void testStringLiteralWithSingleUnicodeEscape()
	{
		String source = "\"\\u0048ello\"";
		Lexer lexer = new Lexer(source);
		List<Token> tokens = lexer.tokenize();

		requireThat(tokens.size(), "tokens.size()").isEqualTo(2);
		Token token = tokens.get(0);
		requireThat(token.type(), "token.type()").isEqualTo(TokenType.STRING_LITERAL);
		requireThat(token.text(), "token.text()").isEqualTo("\"\\u0048ello\"");
		requireThat(tokens.get(1).type(), "tokens.get(1).type()").isEqualTo(TokenType.END_OF_FILE);
	}

	/**
	 * Tests string literal with multiple consecutive Unicode escape sequences.
	 * Validates that the lexer correctly handles strings containing multiple escapes.
	 */
	@Test
	public void testStringLiteralWithMultipleUnicodeEscapes()
	{
		String source = "\"\\u0048\\u0065\\u006C\\u006C\\u006F\"";
		Lexer lexer = new Lexer(source);
		List<Token> tokens = lexer.tokenize();

		requireThat(tokens.size(), "tokens.size()").isEqualTo(2);
		Token token = tokens.get(0);
		requireThat(token.type(), "token.type()").isEqualTo(TokenType.STRING_LITERAL);
		requireThat(token.text(), "token.text()").isEqualTo("\"\\u0048\\u0065\\u006C\\u006C\\u006F\"");
		requireThat(tokens.get(1).type(), "tokens.get(1).type()").isEqualTo(TokenType.END_OF_FILE);
	}

	/**
	 * Tests string literal with Unicode escape at the end of content.
	 * Validates that Unicode escapes at string boundaries are properly tokenized.
	 */
	@Test
	public void testStringLiteralWithUnicodeAtEnd()
	{
		String source = "\"Hello\\u0021\"";
		Lexer lexer = new Lexer(source);
		List<Token> tokens = lexer.tokenize();

		requireThat(tokens.size(), "tokens.size()").isEqualTo(2);
		Token token = tokens.get(0);
		requireThat(token.type(), "token.type()").isEqualTo(TokenType.STRING_LITERAL);
		requireThat(token.text(), "token.text()").isEqualTo("\"Hello\\u0021\"");
		requireThat(tokens.get(1).type(), "tokens.get(1).type()").isEqualTo(TokenType.END_OF_FILE);
	}

	/**
	 * Tests string literal with both standard and Unicode escape sequences.
	 * Validates that lexer correctly handles mixed escape types in the same token.
	 */
	@Test
	public void testStringLiteralWithMixedEscapes()
	{
		String source = "\"\\n\\u0041\\t\"";
		Lexer lexer = new Lexer(source);
		List<Token> tokens = lexer.tokenize();

		requireThat(tokens.size(), "tokens.size()").isEqualTo(2);
		Token token = tokens.get(0);
		requireThat(token.type(), "token.type()").isEqualTo(TokenType.STRING_LITERAL);
		requireThat(token.text(), "token.text()").isEqualTo("\"\\n\\u0041\\t\"");
		requireThat(tokens.get(1).type(), "tokens.get(1).type()").isEqualTo(TokenType.END_OF_FILE);
	}

	/**
	 * Tests text block with Unicode escape sequences.
	 * Validates that Unicode escapes in text blocks (triple-quoted strings) are preserved.
	 */
	@Test
	public void testTextBlockWithUnicodeEscape()
	{
		String source = "\"\"\"\n\\u0041\\u0042\\u0043\n\"\"\"";
		Lexer lexer = new Lexer(source);
		List<Token> tokens = lexer.tokenize();

		requireThat(tokens.size(), "tokens.size()").isEqualTo(2);
		Token token = tokens.get(0);
		requireThat(token.type(), "token.type()").isEqualTo(TokenType.STRING_LITERAL);
		requireThat(token.text(), "token.text()").isEqualTo("\"\"\"\n\\u0041\\u0042\\u0043\n\"\"\"");
		requireThat(tokens.get(1).type(), "tokens.get(1).type()").isEqualTo(TokenType.END_OF_FILE);
	}

	/**
	 * Tests text block with mixed standard and Unicode escape sequences.
	 * Validates that text blocks handle both escape types correctly.
	 */
	@Test
	public void testTextBlockWithMixedEscapes()
	{
		String source = "\"\"\"\nHello\\u0020World\\n\n\"\"\"";
		Lexer lexer = new Lexer(source);
		List<Token> tokens = lexer.tokenize();

		requireThat(tokens.size(), "tokens.size()").isEqualTo(2);
		Token token = tokens.get(0);
		requireThat(token.type(), "token.type()").isEqualTo(TokenType.STRING_LITERAL);
		requireThat(token.text(), "token.text()").contains("\\u0020");
		requireThat(token.text(), "token.text()").contains("\\n");
		requireThat(tokens.get(1).type(), "tokens.get(1).type()").isEqualTo(TokenType.END_OF_FILE);
	}

	/**
	 * Tests that standard escape sequences continue to work with Unicode escape support.
	 * Validates backward compatibility of newline escape in character literals.
	 */
	@Test
	public void testPreserveStandardEscapesUnchanged()
	{
		String source = "'\\n'";
		Lexer lexer = new Lexer(source);
		List<Token> tokens = lexer.tokenize();

		requireThat(tokens.size(), "tokens.size()").isEqualTo(2);
		Token token = tokens.get(0);
		requireThat(token.type(), "token.type()").isEqualTo(TokenType.CHAR_LITERAL);
		requireThat(token.text(), "token.text()").isEqualTo("'\\n'");
		requireThat(tokens.get(1).type(), "tokens.get(1).type()").isEqualTo(TokenType.END_OF_FILE);
	}

	/**
	 * Tests that standard escape sequences work in string literals.
	 * Validates backward compatibility of tab, carriage return, and newline escapes.
	 */
	@Test
	public void testPreserveStringStandardEscapes()
	{
		String source = "\"\\t\\r\\n\"";
		Lexer lexer = new Lexer(source);
		List<Token> tokens = lexer.tokenize();

		requireThat(tokens.size(), "tokens.size()").isEqualTo(2);
		Token token = tokens.get(0);
		requireThat(token.type(), "token.type()").isEqualTo(TokenType.STRING_LITERAL);
		requireThat(token.text(), "token.text()").isEqualTo("\"\\t\\r\\n\"");
		requireThat(tokens.get(1).type(), "tokens.get(1).type()").isEqualTo(TokenType.END_OF_FILE);
	}

	/**
	 * Tests character literal with lowercase hexadecimal in Unicode escape.
	 * Validates that both uppercase and lowercase hex digits are correctly recognized.
	 */
	@Test
	public void testCharLiteralWithLowercaseUnicodeEscape()
	{
		String source = "'\\u004a'";
		Lexer lexer = new Lexer(source);
		List<Token> tokens = lexer.tokenize();

		requireThat(tokens.size(), "tokens.size()").isEqualTo(2);
		Token token = tokens.get(0);
		requireThat(token.type(), "token.type()").isEqualTo(TokenType.CHAR_LITERAL);
		requireThat(token.text(), "token.text()").isEqualTo("'\\u004a'");
		requireThat(tokens.get(1).type(), "tokens.get(1).type()").isEqualTo(TokenType.END_OF_FILE);
	}

	/**
	 * Tests string literal with consecutive Unicode escapes without separators.
	 * Validates that back-to-back escape sequences are correctly recognized with proper token length.
	 */
	@Test
	public void testStringWithConsecutiveUnicodeEscapes()
	{
		String source = "\"\\u0041\\u0042\"";
		Lexer lexer = new Lexer(source);
		List<Token> tokens = lexer.tokenize();

		requireThat(tokens.size(), "tokens.size()").isEqualTo(2);
		Token token = tokens.get(0);
		requireThat(token.type(), "token.type()").isEqualTo(TokenType.STRING_LITERAL);
		requireThat(token.text(), "token.text()").isEqualTo("\"\\u0041\\u0042\"");
		requireThat(token.end() - token.start(), "token.length()").isEqualTo(14);
		requireThat(tokens.get(1).type(), "tokens.get(1).type()").isEqualTo(TokenType.END_OF_FILE);
	}

	// ===== Unicode escape outside literals tests =====

	/**
	 * Tests Unicode escape in identifier position.
	 * {@code \u0041} is 'A', so {@code int \u0041 = 1;} should parse as {@code int A = 1;}.
	 */
	@Test
	public void testUnicodeEscapeAsIdentifier()
	{
		String source = "int \\u0041 = 1;";
		Lexer lexer = new Lexer(source);
		List<Token> tokens = lexer.tokenize();

		requireThat(tokens.size(), "tokens.size()").isEqualTo(6);
		requireThat(tokens.get(0).type(), "tokens.get(0).type()").isEqualTo(TokenType.INT);
		requireThat(tokens.get(1).type(), "tokens.get(1).type()").isEqualTo(TokenType.IDENTIFIER);
		requireThat(tokens.get(2).type(), "tokens.get(2).type()").isEqualTo(TokenType.ASSIGN);
	}

	// ===== Unicode preprocessing tests =====

	/**
	 * Tests that a simple Unicode escape is recognized as an identifier.
	 * {@code \u0041} is 'A', so {@code int \u0041 = 1;} should produce an IDENTIFIER token.
	 */
	@Test
	public void testRecognizeSimpleUnicodeEscapeAsIdentifier()
	{
		String source = "int \\u0041 = 1;";
		Lexer lexer = new Lexer(source);
		List<Token> tokens = lexer.tokenize();

		requireThat(tokens.size(), "tokens.size()").isEqualTo(6);
		Token identifierToken = tokens.get(1);
		requireThat(identifierToken.type(), "identifierToken.type()").isEqualTo(TokenType.IDENTIFIER);
		requireThat(identifierToken.text(), "identifierToken.text()").isEqualTo("\\u0041");
		requireThat(identifierToken.decodedText(), "identifierToken.decodedText()").isEqualTo("A");
	}

	/**
	 * Tests that multiple consecutive Unicode escapes form a single identifier.
	 * {@code \u0041\u0042\u0043} decodes to "ABC" and should be a single IDENTIFIER token.
	 */
	@Test
	public void testRecognizeMultipleConsecutiveUnicodeEscapesAsIdentifier()
	{
		String source = "\\u0041\\u0042\\u0043";
		Lexer lexer = new Lexer(source);
		List<Token> tokens = lexer.tokenize();

		requireThat(tokens.size(), "tokens.size()").isEqualTo(2);
		Token token = tokens.getFirst();
		requireThat(token.type(), "token.type()").isEqualTo(TokenType.IDENTIFIER);
		requireThat(token.text(), "token.text()").isEqualTo("\\u0041\\u0042\\u0043");
		requireThat(token.decodedText(), "token.decodedText()").isEqualTo("ABC");
	}

	/**
	 * Tests that an identifier starting with a Unicode escape is recognized.
	 * {@code \u0041bc} decodes to "Abc" and should be a single IDENTIFIER token.
	 */
	@Test
	public void testRecognizeUnicodeEscapeAtIdentifierStart()
	{
		String source = "\\u0041bc";
		Lexer lexer = new Lexer(source);
		List<Token> tokens = lexer.tokenize();

		requireThat(tokens.size(), "tokens.size()").isEqualTo(2);
		Token token = tokens.getFirst();
		requireThat(token.type(), "token.type()").isEqualTo(TokenType.IDENTIFIER);
		requireThat(token.text(), "token.text()").isEqualTo("\\u0041bc");
		requireThat(token.decodedText(), "token.decodedText()").isEqualTo("Abc");
	}

	/**
	 * Tests that a Unicode escape in the middle of an identifier is recognized.
	 * {@code a\u0042c} decodes to "aBc" and should be a single IDENTIFIER token.
	 */
	@Test
	public void testRecognizeUnicodeEscapeInMiddleOfIdentifier()
	{
		String source = "a\\u0042c";
		Lexer lexer = new Lexer(source);
		List<Token> tokens = lexer.tokenize();

		requireThat(tokens.size(), "tokens.size()").isEqualTo(2);
		Token token = tokens.getFirst();
		requireThat(token.type(), "token.type()").isEqualTo(TokenType.IDENTIFIER);
		requireThat(token.text(), "token.text()").isEqualTo("a\\u0042c");
		requireThat(token.decodedText(), "token.decodedText()").isEqualTo("aBc");
	}

	/**
	 * Tests that a mixed identifier with both regular and escaped characters is recognized.
	 * {@code test\u0041var} decodes to "testAvar" and should be a single IDENTIFIER token.
	 */
	@Test
	public void testRecognizeMixedIdentifierWithUnicodeEscape()
	{
		String source = "test\\u0041var";
		Lexer lexer = new Lexer(source);
		List<Token> tokens = lexer.tokenize();

		requireThat(tokens.size(), "tokens.size()").isEqualTo(2);
		Token token = tokens.getFirst();
		requireThat(token.type(), "token.type()").isEqualTo(TokenType.IDENTIFIER);
		requireThat(token.text(), "token.text()").isEqualTo("test\\u0041var");
		requireThat(token.decodedText(), "token.decodedText()").isEqualTo("testAvar");
	}

	/**
	 * Tests that a fully escaped "public" keyword is recognized as PUBLIC.
	 * {@code \u0070\u0075\u0062\u006C\u0069\u0063} decodes to "public".
	 */
	@Test
	public void testRecognizeFullyEscapedPublicKeyword()
	{
		String source = "\\u0070\\u0075\\u0062\\u006C\\u0069\\u0063 class Test {}";
		Lexer lexer = new Lexer(source);
		List<Token> tokens = lexer.tokenize();

		Token firstToken = tokens.getFirst();
		requireThat(firstToken.type(), "firstToken.type()").isEqualTo(TokenType.PUBLIC);
		requireThat(firstToken.text(), "firstToken.text()").isEqualTo("\\u0070\\u0075\\u0062\\u006C\\u0069\\u0063");
		requireThat(firstToken.decodedText(), "firstToken.decodedText()").isEqualTo("public");
	}

	/**
	 * Tests that a partially escaped "public" keyword is recognized.
	 * {@code \u0070ublic} decodes to "public" where only 'p' is escaped.
	 */
	@Test
	public void testRecognizePartiallyEscapedPublicKeyword()
	{
		String source = "\\u0070ublic";
		Lexer lexer = new Lexer(source);
		List<Token> tokens = lexer.tokenize();

		requireThat(tokens.size(), "tokens.size()").isEqualTo(2);
		Token token = tokens.getFirst();
		requireThat(token.type(), "token.type()").isEqualTo(TokenType.PUBLIC);
		requireThat(token.text(), "token.text()").isEqualTo("\\u0070ublic");
		requireThat(token.decodedText(), "token.decodedText()").isEqualTo("public");
	}

	/**
	 * Tests that an escaped "class" keyword is recognized.
	 * {@code \u0063lass} decodes to "class" where only 'c' is escaped.
	 */
	@Test
	public void testRecognizeEscapedClassKeyword()
	{
		String source = "\\u0063lass Test {}";
		Lexer lexer = new Lexer(source);
		List<Token> tokens = lexer.tokenize();

		Token firstToken = tokens.getFirst();
		requireThat(firstToken.type(), "firstToken.type()").isEqualTo(TokenType.CLASS);
		requireThat(firstToken.text(), "firstToken.text()").isEqualTo("\\u0063lass");
		requireThat(firstToken.decodedText(), "firstToken.decodedText()").isEqualTo("class");
	}

	/**
	 * Tests that an escaped "static" keyword is recognized.
	 * {@code \u0073tatic} decodes to "static" where only 's' is escaped.
	 */
	@Test
	public void testRecognizeEscapedStaticKeyword()
	{
		String source = "\\u0073tatic int x = 1;";
		Lexer lexer = new Lexer(source);
		List<Token> tokens = lexer.tokenize();

		Token firstToken = tokens.getFirst();
		requireThat(firstToken.type(), "firstToken.type()").isEqualTo(TokenType.STATIC);
		requireThat(firstToken.text(), "firstToken.text()").isEqualTo("\\u0073tatic");
		requireThat(firstToken.decodedText(), "firstToken.decodedText()").isEqualTo("static");
	}

	/**
	 * Tests that an escaped "var" contextual keyword is recognized.
	 * {@code \u0076ar} decodes to "var" where only 'v' is escaped.
	 */
	@Test
	public void testRecognizeEscapedVarKeyword()
	{
		String source = "\\u0076ar x = 1;";
		Lexer lexer = new Lexer(source);
		List<Token> tokens = lexer.tokenize();

		Token firstToken = tokens.getFirst();
		requireThat(firstToken.type(), "firstToken.type()").isEqualTo(TokenType.VAR);
		requireThat(firstToken.text(), "firstToken.text()").isEqualTo("\\u0076ar");
		requireThat(firstToken.decodedText(), "firstToken.decodedText()").isEqualTo("var");
	}

	/**
	 * Tests that the assign operator works with Unicode-escaped identifiers.
	 * Validates proper tokenization after a Unicode-escaped identifier.
	 */
	@Test
	public void testRecognizeAssignOperatorAfterEscapedIdentifier()
	{
		String source = "\\u0041 = 1;";
		Lexer lexer = new Lexer(source);
		List<Token> tokens = lexer.tokenize();

		requireThat(tokens.get(0).type(), "tokens.get(0).type()").isEqualTo(TokenType.IDENTIFIER);
		requireThat(tokens.get(1).type(), "tokens.get(1).type()").isEqualTo(TokenType.ASSIGN);
		requireThat(tokens.get(2).type(), "tokens.get(2).type()").isEqualTo(TokenType.INTEGER_LITERAL);
		requireThat(tokens.get(3).type(), "tokens.get(3).type()").isEqualTo(TokenType.SEMICOLON);
	}

	/**
	 * Tests that the equal operator works in expressions with Unicode-escaped identifiers.
	 */
	@Test
	public void testRecognizeEqualOperatorWithEscapedIdentifiers()
	{
		String source = "\\u0041 == \\u0042";
		Lexer lexer = new Lexer(source);
		List<Token> tokens = lexer.tokenize();

		requireThat(tokens.get(0).type(), "tokens.get(0).type()").isEqualTo(TokenType.IDENTIFIER);
		requireThat(tokens.get(0).decodedText(), "tokens.get(0).decodedText()").isEqualTo("A");
		requireThat(tokens.get(1).type(), "tokens.get(1).type()").isEqualTo(TokenType.EQUAL);
		requireThat(tokens.get(2).type(), "tokens.get(2).type()").isEqualTo(TokenType.IDENTIFIER);
		requireThat(tokens.get(2).decodedText(), "tokens.get(2).decodedText()").isEqualTo("B");
	}

	/**
	 * Tests that the plus-assign operator works with Unicode-escaped identifiers.
	 */
	@Test
	public void testRecognizePlusAssignOperatorWithEscapedIdentifier()
	{
		String source = "\\u0041 += 5;";
		Lexer lexer = new Lexer(source);
		List<Token> tokens = lexer.tokenize();

		requireThat(tokens.get(0).type(), "tokens.get(0).type()").isEqualTo(TokenType.IDENTIFIER);
		requireThat(tokens.get(1).type(), "tokens.get(1).type()").isEqualTo(TokenType.PLUS_ASSIGN);
	}

	/**
	 * Tests that braces work properly with Unicode-escaped class declarations.
	 */
	@Test
	public void testRecognizeBracesWithEscapedKeyword()
	{
		String source = "\\u0063lass X {}";
		Lexer lexer = new Lexer(source);
		List<Token> tokens = lexer.tokenize();

		requireThat(tokens.get(0).type(), "tokens.get(0).type()").isEqualTo(TokenType.CLASS);
		requireThat(tokens.get(1).type(), "tokens.get(1).type()").isEqualTo(TokenType.IDENTIFIER);
		requireThat(tokens.get(2).type(), "tokens.get(2).type()").isEqualTo(TokenType.LEFT_BRACE);
		requireThat(tokens.get(3).type(), "tokens.get(3).type()").isEqualTo(TokenType.RIGHT_BRACE);
	}

	/**
	 * Tests that method declaration tokens work with Unicode-escaped modifiers.
	 */
	@Test
	public void testRecognizeMethodDeclarationWithEscapedModifier()
	{
		String source = "\\u0070ublic void test() {}";
		Lexer lexer = new Lexer(source);
		List<Token> tokens = lexer.tokenize();

		requireThat(tokens.get(0).type(), "tokens.get(0).type()").isEqualTo(TokenType.PUBLIC);
		requireThat(tokens.get(1).type(), "tokens.get(1).type()").isEqualTo(TokenType.VOID);
		requireThat(tokens.get(2).type(), "tokens.get(2).type()").isEqualTo(TokenType.IDENTIFIER);
		requireThat(tokens.get(3).type(), "tokens.get(3).type()").isEqualTo(TokenType.LEFT_PARENTHESIS);
		requireThat(tokens.get(4).type(), "tokens.get(4).type()").isEqualTo(TokenType.RIGHT_PARENTHESIS);
	}

	/**
	 * Tests that double-u Unicode escape is valid per JLS 3.3.
	 * {@code \uu0041} should decode to 'A'.
	 */
	@Test
	public void testRecognizeDoubleUEscape()
	{
		String source = "\\uu0041";
		Lexer lexer = new Lexer(source);
		List<Token> tokens = lexer.tokenize();

		requireThat(tokens.size(), "tokens.size()").isEqualTo(2);
		Token token = tokens.getFirst();
		requireThat(token.type(), "token.type()").isEqualTo(TokenType.IDENTIFIER);
		requireThat(token.text(), "token.text()").isEqualTo("\\uu0041");
		requireThat(token.decodedText(), "token.decodedText()").isEqualTo("A");
	}

	/**
	 * Tests that triple-u Unicode escape is valid per JLS 3.3.
	 * {@code \uuu0041} should decode to 'A'.
	 */
	@Test
	public void testRecognizeTripleUEscape()
	{
		String source = "\\uuu0041";
		Lexer lexer = new Lexer(source);
		List<Token> tokens = lexer.tokenize();

		requireThat(tokens.size(), "tokens.size()").isEqualTo(2);
		Token token = tokens.getFirst();
		requireThat(token.type(), "token.type()").isEqualTo(TokenType.IDENTIFIER);
		requireThat(token.text(), "token.text()").isEqualTo("\\uuu0041");
		requireThat(token.decodedText(), "token.decodedText()").isEqualTo("A");
	}

	/**
	 * Tests that many consecutive 'u' characters in Unicode escape is valid.
	 * {@code \uuuuuuuu0041} should decode to 'A'.
	 */
	@Test
	public void testRecognizeManyUEscape()
	{
		String source = "\\uuuuuuuu0041";
		Lexer lexer = new Lexer(source);
		List<Token> tokens = lexer.tokenize();

		requireThat(tokens.size(), "tokens.size()").isEqualTo(2);
		Token token = tokens.getFirst();
		requireThat(token.type(), "token.type()").isEqualTo(TokenType.IDENTIFIER);
		requireThat(token.text(), "token.text()").isEqualTo("\\uuuuuuuu0041");
		requireThat(token.decodedText(), "token.decodedText()").isEqualTo("A");
	}

	/**
	 * Tests that multiple-u escape forms a keyword when decoded.
	 * {@code \uuu0069nt} should decode to "int" and be recognized as INT keyword.
	 */
	@Test
	public void testRecognizeMultipleUEscapeFormingKeyword()
	{
		String source = "\\uuu0069nt";
		Lexer lexer = new Lexer(source);
		List<Token> tokens = lexer.tokenize();

		requireThat(tokens.size(), "tokens.size()").isEqualTo(2);
		Token token = tokens.getFirst();
		requireThat(token.type(), "token.type()").isEqualTo(TokenType.INT);
		requireThat(token.text(), "token.text()").isEqualTo("\\uuu0069nt");
		requireThat(token.decodedText(), "token.decodedText()").isEqualTo("int");
	}

	/**
	 * Tests that token text preserves the original Unicode escape.
	 * The original escape sequence should be preserved in text() for formatter output.
	 */
	@Test
	public void testPreserveOriginalEscapeInTokenText()
	{
		String source = "\\u0041";
		Lexer lexer = new Lexer(source);
		List<Token> tokens = lexer.tokenize();

		Token token = tokens.getFirst();
		requireThat(token.text(), "token.text()").isEqualTo("\\u0041");
		requireThat(token.text(), "token.text()").isNotEqualTo("A");
	}

	/**
	 * Tests that token positions correctly reference the original source.
	 * Position should reflect the 6-character escape sequence, not the decoded character.
	 */
	@Test
	public void testPreserveTokenPositionsForUnicodeEscape()
	{
		String source = "int \\u0041 = 1;";
		Lexer lexer = new Lexer(source);
		List<Token> tokens = lexer.tokenize();

		Token identifierToken = tokens.get(1);
		requireThat(identifierToken.start(), "identifierToken.start()").isEqualTo(4);
		requireThat(identifierToken.end(), "identifierToken.end()").isEqualTo(10);
		requireThat(identifierToken.length(), "identifierToken.length()").isEqualTo(6);
	}

	/**
	 * Tests that mixed identifier preserves original text including escapes.
	 */
	@Test
	public void testPreserveMixedIdentifierOriginalText()
	{
		String source = "test\\u0041var";
		Lexer lexer = new Lexer(source);
		List<Token> tokens = lexer.tokenize();

		Token token = tokens.getFirst();
		requireThat(token.text(), "token.text()").isEqualTo("test\\u0041var");
		requireThat(token.decodedText(), "token.decodedText()").isEqualTo("testAvar");
	}

	/**
	 * Tests that multiple escapes in token preserve all original escape sequences.
	 */
	@Test
	public void testPreserveMultipleEscapesInTokenText()
	{
		String source = "\\u0041\\u0042\\u0043";
		Lexer lexer = new Lexer(source);
		List<Token> tokens = lexer.tokenize();

		Token token = tokens.getFirst();
		requireThat(token.text(), "token.text()").isEqualTo("\\u0041\\u0042\\u0043");
		requireThat(token.text().contains("\\u0041"), "containsFirstEscape").isTrue();
		requireThat(token.text().contains("\\u0042"), "containsSecondEscape").isTrue();
		requireThat(token.text().contains("\\u0043"), "containsThirdEscape").isTrue();
	}

	/**
	 * Tests that an incomplete Unicode escape (too few hex digits) is handled.
	 * backslash-u-00 has only 2 hex digits instead of required 4.
	 */
	@Test
	public void testHandleIncompleteHexDigits()
	{
		String source = "\\u00";
		Lexer lexer = new Lexer(source);
		List<Token> tokens = lexer.tokenize();

		Token token = tokens.getFirst();
		requireThat(token.type(), "token.type()").isEqualTo(TokenType.ERROR);
	}

	/**
	 * Tests that an invalid hex character in Unicode escape is handled.
	 * backslash-u-00GG contains 'G' which is not a valid hex digit.
	 */
	@Test
	public void testHandleInvalidHexCharacter()
	{
		String source = "\\u00GG";
		Lexer lexer = new Lexer(source);
		List<Token> tokens = lexer.tokenize();

		Token token = tokens.getFirst();
		requireThat(token.type(), "token.type()").isEqualTo(TokenType.ERROR);
	}

	/**
	 * Tests that a backslash not followed by 'u' is not treated as Unicode escape.
	 * backslash-x-0041 is not a Unicode escape since 'x' is not 'u'.
	 */
	@Test
	public void testNotTreatBackslashXAsUnicodeEscape()
	{
		String source = "\"\\x0041\"";
		Lexer lexer = new Lexer(source);
		List<Token> tokens = lexer.tokenize();

		Token token = tokens.getFirst();
		requireThat(token.type(), "token.type()").isEqualTo(TokenType.STRING_LITERAL);
		requireThat(token.text(), "token.text()").isEqualTo("\"\\x0041\"");
	}

	/**
	 * Tests that escaped backslash followed by u in string literal is handled.
	 * {@code "\\u0041"} contains an escaped backslash, not a Unicode escape.
	 */
	@Test
	public void testHandleEscapedBackslashInStringLiteral()
	{
		String source = "\"\\\\u0041\"";
		Lexer lexer = new Lexer(source);
		List<Token> tokens = lexer.tokenize();

		Token token = tokens.getFirst();
		requireThat(token.type(), "token.type()").isEqualTo(TokenType.STRING_LITERAL);
		requireThat(token.text(), "token.text()").isEqualTo("\"\\\\u0041\"");
	}

	/**
	 * Tests that Unicode escape at very end of source is handled gracefully.
	 */
	@Test
	public void testHandleUnicodeEscapeAtEndOfSource()
	{
		String source = "test\\u0041";
		Lexer lexer = new Lexer(source);
		List<Token> tokens = lexer.tokenize();

		requireThat(tokens.size(), "tokens.size()").isEqualTo(2);
		Token token = tokens.getFirst();
		requireThat(token.type(), "token.type()").isEqualTo(TokenType.IDENTIFIER);
		requireThat(token.text(), "token.text()").isEqualTo("test\\u0041");
		requireThat(token.decodedText(), "token.decodedText()").isEqualTo("testA");
	}

	/**
	 * Tests that Unicode escapes inside string literals continue to work.
	 * Regression test for existing in-literal escape handling.
	 */
	@Test
	public void testContinueToLexUnicodeEscapeInStringLiteral()
	{
		String source = "\"\\u0048ello\"";
		Lexer lexer = new Lexer(source);
		List<Token> tokens = lexer.tokenize();

		requireThat(tokens.size(), "tokens.size()").isEqualTo(2);
		Token token = tokens.getFirst();
		requireThat(token.type(), "token.type()").isEqualTo(TokenType.STRING_LITERAL);
		requireThat(token.text(), "token.text()").isEqualTo("\"\\u0048ello\"");
	}

	/**
	 * Tests that Unicode escapes inside character literals continue to work.
	 * Regression test for existing in-literal escape handling.
	 */
	@Test
	public void testContinueToLexUnicodeEscapeInCharLiteral()
	{
		String source = "'\\u0041'";
		Lexer lexer = new Lexer(source);
		List<Token> tokens = lexer.tokenize();

		requireThat(tokens.size(), "tokens.size()").isEqualTo(2);
		Token token = tokens.getFirst();
		requireThat(token.type(), "token.type()").isEqualTo(TokenType.CHAR_LITERAL);
		requireThat(token.text(), "token.text()").isEqualTo("'\\u0041'");
	}

	/**
	 * Tests that "non-sealed" keyword works with Unicode escape for 'n'.
	 * {@code \u006eon-sealed} decodes to "non-sealed".
	 */
	@Test
	public void testRecognizeEscapedNonSealedKeyword()
	{
		String source = "\\u006eon-sealed class Test {}";
		Lexer lexer = new Lexer(source);
		List<Token> tokens = lexer.tokenize();

		Token firstToken = tokens.getFirst();
		requireThat(firstToken.type(), "firstToken.type()").isEqualTo(TokenType.NON_SEALED);
		requireThat(firstToken.decodedText(), "firstToken.decodedText()").isEqualTo("non-sealed");
	}

	/**
	 * Tests that identifiers with digits and escapes work correctly.
	 * {@code var1\u0032} decodes to "var12".
	 */
	@Test
	public void testRecognizeIdentifierWithDigitAndEscape()
	{
		String source = "var1\\u0032";
		Lexer lexer = new Lexer(source);
		List<Token> tokens = lexer.tokenize();

		requireThat(tokens.size(), "tokens.size()").isEqualTo(2);
		Token token = tokens.getFirst();
		requireThat(token.type(), "token.type()").isEqualTo(TokenType.IDENTIFIER);
		requireThat(token.text(), "token.text()").isEqualTo("var1\\u0032");
		requireThat(token.decodedText(), "token.decodedText()").isEqualTo("var12");
	}

	/**
	 * Tests that underscore character via Unicode escape works in identifiers.
	 * {@code my\u005Fvar} decodes to "my_var".
	 */
	@Test
	public void testRecognizeUnderscoreViaUnicodeEscape()
	{
		String source = "my\\u005Fvar";
		Lexer lexer = new Lexer(source);
		List<Token> tokens = lexer.tokenize();

		requireThat(tokens.size(), "tokens.size()").isEqualTo(2);
		Token token = tokens.getFirst();
		requireThat(token.type(), "token.type()").isEqualTo(TokenType.IDENTIFIER);
		requireThat(token.text(), "token.text()").isEqualTo("my\\u005Fvar");
		requireThat(token.decodedText(), "token.decodedText()").isEqualTo("my_var");
	}

	/**
	 * Tests that Unicode escape for dollar sign works in identifiers.
	 * Java allows $ in identifiers.
	 * {@code my\u0024var} decodes to "my$var".
	 */
	@Test
	public void testRecognizeDollarSignViaUnicodeEscape()
	{
		String source = "my\\u0024var";
		Lexer lexer = new Lexer(source);
		List<Token> tokens = lexer.tokenize();

		requireThat(tokens.size(), "tokens.size()").isEqualTo(2);
		Token token = tokens.getFirst();
		requireThat(token.type(), "token.type()").isEqualTo(TokenType.IDENTIFIER);
		requireThat(token.text(), "token.text()").isEqualTo("my\\u0024var");
		requireThat(token.decodedText(), "token.decodedText()").isEqualTo("my$var");
	}

	/**
	 * Tests tokens without Unicode escapes share the same String instance.
	 * This is an optimization test to verify memory efficiency.
	 */
	@SuppressWarnings("PMD.UseEqualsToCompareStrings")
	@Test
	public void testShareStringInstanceWhenNoEscapes()
	{
		String source = "regularIdentifier";
		Lexer lexer = new Lexer(source);
		List<Token> tokens = lexer.tokenize();

		Token token = tokens.getFirst();
		requireThat(token.text() == token.decodedText(), "sameInstance").isTrue();
	}

	/**
	 * Tests that a Unicode escape for a non-identifier character does not extend the identifier.
	 * {@code test\u0020end} should produce two identifiers since \u0020 is space.
	 */
	@Test
	public void testNotExtendIdentifierWithNonIdentifierEscape()
	{
		String source = "test\\u0020end";
		Lexer lexer = new Lexer(source);
		List<Token> tokens = lexer.tokenize();

		Token firstToken = tokens.getFirst();
		requireThat(firstToken.type(), "firstToken.type()").isEqualTo(TokenType.IDENTIFIER);
		requireThat(firstToken.decodedText(), "firstToken.decodedText()").isEqualTo("test");
	}
}
