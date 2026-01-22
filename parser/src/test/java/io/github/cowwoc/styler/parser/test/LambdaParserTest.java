package io.github.cowwoc.styler.parser.test;

import io.github.cowwoc.styler.ast.core.NodeArena;
import io.github.cowwoc.styler.ast.core.NodeType;
import io.github.cowwoc.styler.ast.core.TypeDeclarationAttribute;
import io.github.cowwoc.styler.parser.Parser;
import org.testng.annotations.Test;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;
import static io.github.cowwoc.styler.parser.test.ParserTestUtils.parse;

/**
 * Tests for parsing lambda expressions.
 */
public final class LambdaParserTest
{
	// ========== Annotated Parameter Tests ==========

	/**
	 * Validates that a lambda with annotated generic type parameter containing element-value pair parses
	 * correctly.
	 */
	@Test
	public void shouldParseLambdaWithAnnotatedGenericParameter()
	{
		String source = """
			class Test
			{
				void m()
				{
					handle((List<@NonNull(when=MAYBE) String> items) -> items.size());
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 28, 34);
			expected.allocateNode(NodeType.IDENTIFIER, 28, 34);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 36, 40);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 42, 49);
			expected.allocateNode(NodeType.IDENTIFIER, 50, 54);
			expected.allocateNode(NodeType.IDENTIFIER, 55, 60);
			expected.allocateNode(NodeType.ASSIGNMENT_EXPRESSION, 50, 60);
			expected.allocateNode(NodeType.ANNOTATION, 41, 61);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 62, 68);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 41, 68);
			expected.allocateNode(NodeType.PARAMETERIZED_TYPE, 36, 69);
			expected.allocateNode(NodeType.IDENTIFIER, 80, 85);
			expected.allocateNode(NodeType.FIELD_ACCESS, 80, 90);
			expected.allocateNode(NodeType.METHOD_INVOCATION, 80, 92);
			expected.allocateNode(NodeType.LAMBDA_EXPRESSION, 35, 92);
			expected.allocateNode(NodeType.METHOD_INVOCATION, 28, 93);
			expected.allocateNode(NodeType.BLOCK, 24, 97);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 14, 97);
			expected.allocateClassDeclaration(0, 99, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 100);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates that a lambda with multiple annotated generic type parameters parses correctly.
	 */
	@Test
	public void shouldParseLambdaWithMultipleAnnotatedGenericParameters()
	{
		String source = """
			class Test
			{
				void m()
				{
					process((Map<@Key String, @Value(priority=1) Integer> map) -> map.size());
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 28, 35);
			expected.allocateNode(NodeType.IDENTIFIER, 28, 35);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 37, 40);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 42, 45);
			expected.allocateNode(NodeType.ANNOTATION, 41, 45);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 46, 52);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 41, 52);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 55, 60);
			expected.allocateNode(NodeType.IDENTIFIER, 61, 69);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 70, 71);
			expected.allocateNode(NodeType.ASSIGNMENT_EXPRESSION, 61, 71);
			expected.allocateNode(NodeType.ANNOTATION, 54, 72);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 73, 80);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 54, 80);
			expected.allocateNode(NodeType.PARAMETERIZED_TYPE, 37, 81);
			expected.allocateNode(NodeType.IDENTIFIER, 90, 93);
			expected.allocateNode(NodeType.FIELD_ACCESS, 90, 98);
			expected.allocateNode(NodeType.METHOD_INVOCATION, 90, 100);
			expected.allocateNode(NodeType.LAMBDA_EXPRESSION, 36, 100);
			expected.allocateNode(NodeType.METHOD_INVOCATION, 28, 101);
			expected.allocateNode(NodeType.BLOCK, 24, 105);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 14, 105);
			expected.allocateClassDeclaration(0, 107, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 108);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates that a lambda with nested annotated generic types parses correctly.
	 */
	@Test
	public void shouldParseLambdaWithNestedAnnotatedGenerics()
	{
		String source = """
			class Test
			{
				void m()
				{
					transform((Map<String, List<@Valid Item>> data) -> data.get("key"));
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 28, 37);
			expected.allocateNode(NodeType.IDENTIFIER, 28, 37);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 39, 42);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 43, 49);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 43, 49);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 51, 55);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 57, 62);
			expected.allocateNode(NodeType.ANNOTATION, 56, 62);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 63, 67);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 56, 67);
			expected.allocateNode(NodeType.PARAMETERIZED_TYPE, 51, 69);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 51, 69);
			expected.allocateNode(NodeType.PARAMETERIZED_TYPE, 39, 69);
			expected.allocateNode(NodeType.IDENTIFIER, 79, 83);
			expected.allocateNode(NodeType.FIELD_ACCESS, 79, 87);
			expected.allocateNode(NodeType.STRING_LITERAL, 88, 93);
			expected.allocateNode(NodeType.METHOD_INVOCATION, 79, 94);
			expected.allocateNode(NodeType.LAMBDA_EXPRESSION, 38, 94);
			expected.allocateNode(NodeType.METHOD_INVOCATION, 28, 95);
			expected.allocateNode(NodeType.BLOCK, 24, 99);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 14, 99);
			expected.allocateClassDeclaration(0, 101, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 102);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates that a lambda with annotation containing array element value parses correctly.
	 * <p>
	 * This test uses a simpler annotation without array initializer syntax since that is a separate
	 * parser limitation.
	 */
	@Test
	public void shouldParseLambdaWithMultipleAnnotationElements()
	{
		String source = """
			class Test
			{
				void m()
				{
					validate((List<@Constraint(min=0, max=100) Item> items) -> items.isEmpty());
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 28, 36);
			expected.allocateNode(NodeType.IDENTIFIER, 28, 36);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 38, 42);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 44, 54);
			expected.allocateNode(NodeType.IDENTIFIER, 55, 58);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 59, 60);
			expected.allocateNode(NodeType.ASSIGNMENT_EXPRESSION, 55, 60);
			expected.allocateNode(NodeType.IDENTIFIER, 62, 65);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 66, 69);
			expected.allocateNode(NodeType.ASSIGNMENT_EXPRESSION, 62, 69);
			expected.allocateNode(NodeType.ANNOTATION, 43, 70);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 71, 75);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 43, 75);
			expected.allocateNode(NodeType.PARAMETERIZED_TYPE, 38, 76);
			expected.allocateNode(NodeType.IDENTIFIER, 87, 92);
			expected.allocateNode(NodeType.FIELD_ACCESS, 87, 100);
			expected.allocateNode(NodeType.METHOD_INVOCATION, 87, 102);
			expected.allocateNode(NodeType.LAMBDA_EXPRESSION, 37, 102);
			expected.allocateNode(NodeType.METHOD_INVOCATION, 28, 103);
			expected.allocateNode(NodeType.BLOCK, 24, 107);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 14, 107);
			expected.allocateClassDeclaration(0, 109, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 110);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	// ========== Arrow Edge Case Tests ==========

	/**
	 * Validates that a lambda as RHS of field assignment parses correctly.
	 * This was the original failing pattern from Spring Framework.
	 */
	@Test
	public void shouldParseLambdaAsAssignmentRhs()
	{
		String source = """
			class Test
			{
				void init()
				{
					this.sessionManager = exchange -> Mono.just(session);
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.THIS_EXPRESSION, 31, 35);
			expected.allocateNode(NodeType.FIELD_ACCESS, 31, 50);
			expected.allocateNode(NodeType.IDENTIFIER, 65, 69);
			expected.allocateNode(NodeType.FIELD_ACCESS, 65, 74);
			expected.allocateNode(NodeType.IDENTIFIER, 75, 82);
			expected.allocateNode(NodeType.METHOD_INVOCATION, 65, 83);
			expected.allocateNode(NodeType.LAMBDA_EXPRESSION, 53, 83);
			expected.allocateNode(NodeType.ASSIGNMENT_EXPRESSION, 31, 83);
			expected.allocateNode(NodeType.BLOCK, 27, 87);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 14, 87);
			expected.allocateClassDeclaration(0, 89, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 90);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates that a no-param lambda as RHS of field assignment parses correctly.
	 */
	@Test
	public void shouldParseNoParamLambdaAsAssignmentRhs()
	{
		String source = """
			class Test
			{
				void init()
				{
					this.supplier = () -> "hello";
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.THIS_EXPRESSION, 31, 35);
			expected.allocateNode(NodeType.FIELD_ACCESS, 31, 44);
			expected.allocateNode(NodeType.STRING_LITERAL, 53, 60);
			expected.allocateNode(NodeType.LAMBDA_EXPRESSION, 47, 60);
			expected.allocateNode(NodeType.ASSIGNMENT_EXPRESSION, 31, 60);
			expected.allocateNode(NodeType.BLOCK, 27, 64);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 14, 64);
			expected.allocateClassDeclaration(0, 66, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 67);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates lambda in nested assignment (a = b = x -> expr).
	 */
	@Test
	public void shouldParseLambdaInNestedAssignment()
	{
		String source = """
			class Test
			{
				void init()
				{
					a = b = x -> x + 1;
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 31, 32);
			expected.allocateNode(NodeType.IDENTIFIER, 31, 32);
			expected.allocateNode(NodeType.IDENTIFIER, 35, 36);
			expected.allocateNode(NodeType.IDENTIFIER, 44, 45);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 48, 49);
			expected.allocateNode(NodeType.BINARY_EXPRESSION, 44, 49);
			expected.allocateNode(NodeType.LAMBDA_EXPRESSION, 39, 49);
			expected.allocateNode(NodeType.ASSIGNMENT_EXPRESSION, 35, 49);
			expected.allocateNode(NodeType.ASSIGNMENT_EXPRESSION, 31, 49);
			expected.allocateNode(NodeType.BLOCK, 27, 53);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 14, 53);
			expected.allocateClassDeclaration(0, 55, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 56);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates lambda after method reference with trailing comments.
	 * <p>
	 * This pattern occurs in Spring Framework's DatabasePopulator.java:
	 * {@code Mono.usingWhen(source, this::populate, connection -> release(connection))}
	 * The trailing comments and line breaks were causing "Expected RIGHT_PARENTHESIS but found ARROW".
	 * <p>
	 * <b>TDD RED:</b> This test reproduces a production bug and is disabled until the fix is implemented.
	 *
	 * @see <a href=".claude/cat/v0/v0.5/task/fix-lambda-arrow-in-parenthesized-context">Task to fix this</a>
	 */
	@Test
	public void shouldParseLambdaAfterMethodReferenceWithTrailingComments()
	{
		String source = """
			class Test
			{
				void foo()
				{
					Mono.usingWhen(getConnection(), //
						this::populate, //
						connection -> release(connection));
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 30, 44);
			expected.allocateNode(NodeType.IDENTIFIER, 30, 34);
			expected.allocateNode(NodeType.FIELD_ACCESS, 30, 44);
			expected.allocateNode(NodeType.IDENTIFIER, 45, 58);
			expected.allocateNode(NodeType.METHOD_INVOCATION, 45, 60);
			expected.allocateNode(NodeType.LINE_COMMENT, 62, 64);
			expected.allocateNode(NodeType.THIS_EXPRESSION, 68, 72);
			expected.allocateNode(NodeType.METHOD_REFERENCE, 68, 82);
			expected.allocateNode(NodeType.LINE_COMMENT, 84, 86);
			expected.allocateNode(NodeType.IDENTIFIER, 104, 111);
			expected.allocateNode(NodeType.IDENTIFIER, 112, 122);
			expected.allocateNode(NodeType.METHOD_INVOCATION, 104, 123);
			expected.allocateNode(NodeType.LAMBDA_EXPRESSION, 90, 123);
			expected.allocateNode(NodeType.METHOD_INVOCATION, 30, 124);
			expected.allocateNode(NodeType.BLOCK, 26, 128);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 14, 128);
			expected.allocateClassDeclaration(0, 130, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 131);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	// ========== Ternary Tests ==========

	/**
	 * Validates simple lambda as ternary alternative (else-branch).
	 * Pattern: {@code condition ? value : param -> body}
	 */
	@Test
	public void simpleLambdaAsAlternative()
	{
		String source = """
			class Test
			{
				void m()
				{
					Object r = flag ? null : x -> x + 1;
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 28, 34);
			expected.allocateNode(NodeType.IDENTIFIER, 39, 43);
			expected.allocateNode(NodeType.NULL_LITERAL, 46, 50);
			expected.allocateNode(NodeType.IDENTIFIER, 58, 59);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 62, 63);
			expected.allocateNode(NodeType.BINARY_EXPRESSION, 58, 63);
			expected.allocateNode(NodeType.LAMBDA_EXPRESSION, 53, 63);
			expected.allocateNode(NodeType.CONDITIONAL_EXPRESSION, 39, 63);
			expected.allocateNode(NodeType.BLOCK, 24, 67);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 14, 67);
			expected.allocateClassDeclaration(0, 69, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 70);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates lambda as ternary consequent (then-branch).
	 * Pattern: {@code condition ? param -> body : value}
	 */
	@Test
	public void lambdaAsConsequent()
	{
		String source = """
			class Test
			{
				void m()
				{
					Object r = flag ? x -> x * 2 : null;
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 28, 34);
			expected.allocateNode(NodeType.IDENTIFIER, 39, 43);
			expected.allocateNode(NodeType.IDENTIFIER, 51, 52);
			expected.allocateNode(NodeType.INTEGER_LITERAL, 55, 56);
			expected.allocateNode(NodeType.BINARY_EXPRESSION, 51, 56);
			expected.allocateNode(NodeType.LAMBDA_EXPRESSION, 46, 56);
			expected.allocateNode(NodeType.NULL_LITERAL, 59, 63);
			expected.allocateNode(NodeType.CONDITIONAL_EXPRESSION, 39, 63);
			expected.allocateNode(NodeType.BLOCK, 24, 67);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 14, 67);
			expected.allocateClassDeclaration(0, 69, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 70);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates nested ternary with lambda in outermost alternative.
	 * Pattern: {@code a ? b ? c : d : x -> x}
	 * Should parse as: {@code a ? (b ? c : d) : (x -> x)}
	 */
	@Test
	public void nestedTernaryWithLambda()
	{
		String source = """
			class Test
			{
				void m()
				{
					Object r = a ? b ? c : d : x -> x;
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 28, 34);
			expected.allocateNode(NodeType.IDENTIFIER, 39, 40);
			expected.allocateNode(NodeType.IDENTIFIER, 43, 44);
			expected.allocateNode(NodeType.IDENTIFIER, 47, 48);
			expected.allocateNode(NodeType.IDENTIFIER, 51, 52);
			expected.allocateNode(NodeType.CONDITIONAL_EXPRESSION, 43, 52);
			expected.allocateNode(NodeType.IDENTIFIER, 60, 61);
			expected.allocateNode(NodeType.LAMBDA_EXPRESSION, 55, 61);
			expected.allocateNode(NodeType.CONDITIONAL_EXPRESSION, 39, 61);
			expected.allocateNode(NodeType.BLOCK, 24, 65);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 14, 65);
			expected.allocateClassDeclaration(0, 67, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 68);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Validates multi-parameter lambda as ternary alternative.
	 * Pattern: {@code condition ? value : (a, b) -> a + b}
	 */
	@Test
	public void multiParamLambdaAsAlternative()
	{
		String source = """
			class Test
			{
				void m()
				{
					Object r = flag ? null : (a, b) -> a + b;
				}
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 28, 34);
			expected.allocateNode(NodeType.IDENTIFIER, 39, 43);
			expected.allocateNode(NodeType.NULL_LITERAL, 46, 50);
			expected.allocateNode(NodeType.IDENTIFIER, 63, 64);
			expected.allocateNode(NodeType.IDENTIFIER, 67, 68);
			expected.allocateNode(NodeType.BINARY_EXPRESSION, 63, 68);
			expected.allocateNode(NodeType.LAMBDA_EXPRESSION, 53, 68);
			expected.allocateNode(NodeType.CONDITIONAL_EXPRESSION, 39, 68);
			expected.allocateNode(NodeType.BLOCK, 24, 72);
			expected.allocateNode(NodeType.METHOD_DECLARATION, 14, 72);
			expected.allocateClassDeclaration(0, 74, new TypeDeclarationAttribute("Test"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 75);
			requireThat(actual, "actual").isEqualTo(expected);
		}
	}
}
