package io.github.cowwoc.styler.parser.test;

import io.github.cowwoc.styler.parser.test.ParserTestUtils.SemanticNode;
import org.testng.annotations.Test;

import java.util.Set;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;
import static io.github.cowwoc.styler.ast.core.NodeType.BLOCK;
import static io.github.cowwoc.styler.ast.core.NodeType.CLASS_DECLARATION;
import static io.github.cowwoc.styler.ast.core.NodeType.COMPILATION_UNIT;
import static io.github.cowwoc.styler.ast.core.NodeType.CONDITIONAL_EXPRESSION;
import static io.github.cowwoc.styler.ast.core.NodeType.FIELD_ACCESS;
import static io.github.cowwoc.styler.ast.core.NodeType.FIELD_DECLARATION;
import static io.github.cowwoc.styler.ast.core.NodeType.IDENTIFIER;
import static io.github.cowwoc.styler.ast.core.NodeType.METHOD_DECLARATION;
import static io.github.cowwoc.styler.ast.core.NodeType.METHOD_INVOCATION;
import static io.github.cowwoc.styler.ast.core.NodeType.METHOD_REFERENCE;
import static io.github.cowwoc.styler.ast.core.NodeType.OBJECT_CREATION;
import static io.github.cowwoc.styler.ast.core.NodeType.PARAMETER_DECLARATION;
import static io.github.cowwoc.styler.ast.core.NodeType.PARAMETERIZED_TYPE;
import static io.github.cowwoc.styler.ast.core.NodeType.QUALIFIED_NAME;
import static io.github.cowwoc.styler.ast.core.NodeType.RETURN_STATEMENT;
import static io.github.cowwoc.styler.ast.core.NodeType.SUPER_EXPRESSION;
import static io.github.cowwoc.styler.ast.core.NodeType.THIS_EXPRESSION;
import static io.github.cowwoc.styler.ast.core.NodeType.WILDCARD_TYPE;
import static io.github.cowwoc.styler.parser.test.ParserTestUtils.assertParseFails;
import static io.github.cowwoc.styler.parser.test.ParserTestUtils.parseSemanticAst;
import static io.github.cowwoc.styler.parser.test.ParserTestUtils.semanticNode;

/**
 * Tests for parsing explicit type arguments on method and constructor calls.
 * Explicit type arguments are specified using the {@code <Type>} syntax before the method name,
 * such as {@code Collections.<String>emptyList()} or {@code new <String>Container()}.
 */
public final class ExplicitTypeArgumentParserTest
{
	// ========================================
	// Method Invocation Type Arguments (7 tests)
	// ========================================

	/**
	 * Tests parsing of explicit type argument on a static method call.
	 * Syntax: {@code Collections.<String>emptyList()}.
	 */
	@Test
	public void staticMethodWithSingleTypeArgument()
	{
		String source = """
			class T
			{
				void m()
				{
					Collections.<String>emptyList();
				}
			}
			""";
		Set<SemanticNode> actual = parseSemanticAst(source);

		Set<SemanticNode> expected = Set.of(
			semanticNode(COMPILATION_UNIT, 0, 63),
			semanticNode(CLASS_DECLARATION, 0, 62, "T"),
			semanticNode(METHOD_DECLARATION, 11, 60),
			semanticNode(BLOCK, 21, 60),
			semanticNode(METHOD_INVOCATION, 25, 56),
			semanticNode(FIELD_ACCESS, 25, 54),
			semanticNode(PARAMETERIZED_TYPE, 25, 45),
			semanticNode(QUALIFIED_NAME, 25, 37),
			semanticNode(IDENTIFIER, 25, 36),
			semanticNode(QUALIFIED_NAME, 38, 44));

		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Tests parsing of explicit type argument with {@code this} as receiver.
	 * Syntax: {@code this.<T>genericMethod()}.
	 */
	@Test
	public void thisReceiverWithTypeArgument()
	{
		String source = """
			class T
			{
				void m()
				{
					this.<String>genericMethod();
				}
			}
			""";
		Set<SemanticNode> actual = parseSemanticAst(source);

		Set<SemanticNode> expected = Set.of(
			semanticNode(COMPILATION_UNIT, 0, 60),
			semanticNode(CLASS_DECLARATION, 0, 59, "T"),
			semanticNode(METHOD_DECLARATION, 11, 57),
			semanticNode(BLOCK, 21, 57),
			semanticNode(METHOD_INVOCATION, 25, 53),
			semanticNode(FIELD_ACCESS, 25, 51),
			semanticNode(THIS_EXPRESSION, 25, 29),
			semanticNode(QUALIFIED_NAME, 31, 37));

		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Tests parsing of multiple type arguments on a method call.
	 * Syntax: {@code obj.<String, Integer>method()}.
	 */
	@Test
	public void multipleTypeArguments()
	{
		String source = """
			class T
			{
				void m()
				{
					obj.<String, Integer>method();
				}
			}
			""";
		Set<SemanticNode> actual = parseSemanticAst(source);

		Set<SemanticNode> expected = Set.of(
			semanticNode(COMPILATION_UNIT, 0, 61),
			semanticNode(CLASS_DECLARATION, 0, 60, "T"),
			semanticNode(METHOD_DECLARATION, 11, 58),
			semanticNode(BLOCK, 21, 58),
			semanticNode(METHOD_INVOCATION, 25, 54),
			semanticNode(FIELD_ACCESS, 25, 52),
			semanticNode(PARAMETERIZED_TYPE, 25, 46),
			semanticNode(QUALIFIED_NAME, 25, 29),
			semanticNode(IDENTIFIER, 25, 28),
			semanticNode(QUALIFIED_NAME, 30, 36),
			semanticNode(QUALIFIED_NAME, 38, 45));

		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Tests parsing of nested generic type argument.
	 * Syntax: {@code obj.<List<String>>method()}.
	 */
	@Test
	public void nestedGenericTypeArgument()
	{
		String source = """
			class T
			{
				void m()
				{
					obj.<List<String>>method();
				}
			}
			""";
		Set<SemanticNode> actual = parseSemanticAst(source);

		Set<SemanticNode> expected = Set.of(
			semanticNode(COMPILATION_UNIT, 0, 58),
			semanticNode(CLASS_DECLARATION, 0, 57, "T"),
			semanticNode(METHOD_DECLARATION, 11, 55),
			semanticNode(BLOCK, 21, 55),
			semanticNode(METHOD_INVOCATION, 25, 51),
			semanticNode(FIELD_ACCESS, 25, 49),
			semanticNode(PARAMETERIZED_TYPE, 25, 43),
			semanticNode(QUALIFIED_NAME, 25, 29),
			semanticNode(IDENTIFIER, 25, 28),
			semanticNode(PARAMETERIZED_TYPE, 30, 43),
			semanticNode(QUALIFIED_NAME, 30, 43),
			semanticNode(QUALIFIED_NAME, 30, 34),
			semanticNode(QUALIFIED_NAME, 35, 41));

		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Tests parsing of wildcard type argument.
	 * Syntax: {@code obj.<? extends Number>method()}.
	 */
	@Test
	public void wildcardTypeArgument()
	{
		String source = """
			class T
			{
				void m()
				{
					obj.<? extends Number>method();
				}
			}
			""";
		Set<SemanticNode> actual = parseSemanticAst(source);

		Set<SemanticNode> expected = Set.of(
			semanticNode(COMPILATION_UNIT, 0, 62),
			semanticNode(CLASS_DECLARATION, 0, 61, "T"),
			semanticNode(METHOD_DECLARATION, 11, 59),
			semanticNode(BLOCK, 21, 59),
			semanticNode(METHOD_INVOCATION, 25, 55),
			semanticNode(FIELD_ACCESS, 25, 53),
			semanticNode(PARAMETERIZED_TYPE, 25, 47),
			semanticNode(QUALIFIED_NAME, 25, 29),
			semanticNode(IDENTIFIER, 25, 28),
			semanticNode(WILDCARD_TYPE, 30, 46),
			semanticNode(QUALIFIED_NAME, 40, 46));

		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Tests parsing of chained method calls each with type arguments.
	 * Syntax: {@code obj.<String>foo().<Integer>bar()}.
	 */
	@Test
	public void chainedMethodsWithTypeArgs()
	{
		String source = """
			class T
			{
				void m()
				{
					obj.<String>foo().<Integer>bar();
				}
			}
			""";
		Set<SemanticNode> actual = parseSemanticAst(source);

		Set<SemanticNode> expected = Set.of(
			semanticNode(COMPILATION_UNIT, 0, 64),
			semanticNode(CLASS_DECLARATION, 0, 63, "T"),
			semanticNode(METHOD_DECLARATION, 11, 61),
			semanticNode(BLOCK, 21, 61),
			semanticNode(METHOD_INVOCATION, 25, 57),
			semanticNode(FIELD_ACCESS, 25, 55),
			semanticNode(METHOD_INVOCATION, 25, 42),
			semanticNode(FIELD_ACCESS, 25, 40),
			semanticNode(PARAMETERIZED_TYPE, 25, 37),
			semanticNode(QUALIFIED_NAME, 25, 29),
			semanticNode(IDENTIFIER, 25, 28),
			semanticNode(QUALIFIED_NAME, 30, 36),
			semanticNode(QUALIFIED_NAME, 44, 51));

		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Tests parsing of explicit type argument with {@code super} as receiver.
	 * Syntax: {@code super.<T>method()}.
	 */
	@Test
	public void superReceiverWithTypeArgument()
	{
		String source = """
			class T
			{
				void m()
				{
					super.<String>method();
				}
			}
			""";
		Set<SemanticNode> actual = parseSemanticAst(source);

		Set<SemanticNode> expected = Set.of(
			semanticNode(COMPILATION_UNIT, 0, 54),
			semanticNode(CLASS_DECLARATION, 0, 53, "T"),
			semanticNode(METHOD_DECLARATION, 11, 51),
			semanticNode(BLOCK, 21, 51),
			semanticNode(METHOD_INVOCATION, 25, 47),
			semanticNode(FIELD_ACCESS, 25, 45),
			semanticNode(SUPER_EXPRESSION, 25, 30),
			semanticNode(QUALIFIED_NAME, 32, 38));

		requireThat(actual, "actual").isEqualTo(expected);
	}

	// ========================================
	// Constructor Type Arguments (4 tests)
	// ========================================

	/**
	 * Tests parsing of explicit type argument on constructor.
	 * Syntax: {@code new <String>Container()}.
	 */
	@Test
	public void constructorWithSingleTypeArgument()
	{
		String source = """
			class T
			{
				void m()
				{
					Object x = new <String>Container();
				}
			}
			""";
		Set<SemanticNode> actual = parseSemanticAst(source);

		Set<SemanticNode> expected = Set.of(
			semanticNode(COMPILATION_UNIT, 0, 66),
			semanticNode(CLASS_DECLARATION, 0, 65, "T"),
			semanticNode(METHOD_DECLARATION, 11, 63),
			semanticNode(BLOCK, 21, 63),
			semanticNode(QUALIFIED_NAME, 25, 31),
			semanticNode(OBJECT_CREATION, 36, 59),
			semanticNode(QUALIFIED_NAME, 41, 47),
			semanticNode(QUALIFIED_NAME, 48, 57));

		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Tests parsing of multiple type arguments on constructor.
	 * Syntax: {@code new <String, Integer>Pair()}.
	 */
	@Test
	public void constructorWithMultipleTypeArguments()
	{
		String source = """
			class T
			{
				void m()
				{
					Object x = new <String, Integer>Pair();
				}
			}
			""";
		Set<SemanticNode> actual = parseSemanticAst(source);

		Set<SemanticNode> expected = Set.of(
			semanticNode(COMPILATION_UNIT, 0, 70),
			semanticNode(CLASS_DECLARATION, 0, 69, "T"),
			semanticNode(METHOD_DECLARATION, 11, 67),
			semanticNode(BLOCK, 21, 67),
			semanticNode(QUALIFIED_NAME, 25, 31),
			semanticNode(OBJECT_CREATION, 36, 63),
			semanticNode(QUALIFIED_NAME, 41, 47),
			semanticNode(QUALIFIED_NAME, 49, 56),
			semanticNode(QUALIFIED_NAME, 57, 61));

		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Tests parsing of constructor with both constructor type args and class type args.
	 * Syntax: {@code new <String>Container<Integer>()}.
	 */
	@Test
	public void constructorTypeArgsWithParameterizedClass()
	{
		String source = """
			class T
			{
				void m()
				{
					Object x = new <String>Container<Integer>();
				}
			}
			""";
		Set<SemanticNode> actual = parseSemanticAst(source);

		Set<SemanticNode> expected = Set.of(
			semanticNode(COMPILATION_UNIT, 0, 75),
			semanticNode(CLASS_DECLARATION, 0, 74, "T"),
			semanticNode(METHOD_DECLARATION, 11, 72),
			semanticNode(BLOCK, 21, 72),
			semanticNode(QUALIFIED_NAME, 25, 31),
			semanticNode(OBJECT_CREATION, 36, 68),
			semanticNode(QUALIFIED_NAME, 41, 47),
			semanticNode(PARAMETERIZED_TYPE, 48, 66),
			semanticNode(QUALIFIED_NAME, 48, 57),
			semanticNode(QUALIFIED_NAME, 58, 65));

		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Tests parsing of nested type argument on constructor.
	 * Syntax: {@code new <List<String>>Wrapper()}.
	 */
	@Test
	public void constructorWithNestedTypeArgument()
	{
		String source = """
			class T
			{
				void m()
				{
					Object x = new <List<String>>Wrapper();
				}
			}
			""";
		Set<SemanticNode> actual = parseSemanticAst(source);

		Set<SemanticNode> expected = Set.of(
			semanticNode(COMPILATION_UNIT, 0, 70),
			semanticNode(CLASS_DECLARATION, 0, 69, "T"),
			semanticNode(METHOD_DECLARATION, 11, 67),
			semanticNode(BLOCK, 21, 67),
			semanticNode(QUALIFIED_NAME, 25, 31),
			semanticNode(OBJECT_CREATION, 36, 63),
			semanticNode(QUALIFIED_NAME, 41, 54),
			semanticNode(PARAMETERIZED_TYPE, 41, 54),
			semanticNode(QUALIFIED_NAME, 41, 45),
			semanticNode(QUALIFIED_NAME, 46, 52),
			semanticNode(QUALIFIED_NAME, 54, 61));

		requireThat(actual, "actual").isEqualTo(expected);
	}

	// ========================================
	// Method Reference Type Arguments (4 tests)
	// ========================================

	/**
	 * Tests parsing of method reference with single type argument.
	 * Syntax: {@code List::<String>of}.
	 */
	@Test
	public void methodReferenceWithSingleTypeArgument()
	{
		String source = """
			class T
			{
				Object f = List::<String>of;
			}
			""";
		Set<SemanticNode> actual = parseSemanticAst(source);

		Set<SemanticNode> expected = Set.of(
			semanticNode(COMPILATION_UNIT, 0, 42),
			semanticNode(CLASS_DECLARATION, 0, 41, "T"),
			semanticNode(FIELD_DECLARATION, 11, 39),
			semanticNode(METHOD_REFERENCE, 22, 38),
			semanticNode(IDENTIFIER, 22, 26),
			semanticNode(QUALIFIED_NAME, 29, 35));

		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Tests parsing of method reference with multiple type arguments.
	 * Syntax: {@code Util::<String, Integer>convert}.
	 */
	@Test
	public void methodReferenceWithMultipleTypeArguments()
	{
		String source = """
			class T
			{
				Object f = Util::<String, Integer>convert;
			}
			""";
		Set<SemanticNode> actual = parseSemanticAst(source);

		Set<SemanticNode> expected = Set.of(
			semanticNode(COMPILATION_UNIT, 0, 56),
			semanticNode(CLASS_DECLARATION, 0, 55, "T"),
			semanticNode(FIELD_DECLARATION, 11, 53),
			semanticNode(METHOD_REFERENCE, 22, 52),
			semanticNode(IDENTIFIER, 22, 26),
			semanticNode(QUALIFIED_NAME, 29, 35),
			semanticNode(QUALIFIED_NAME, 37, 44));

		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Tests parsing of static method reference with type argument.
	 * Syntax: {@code Arrays::<String>sort}.
	 */
	@Test
	public void staticMethodReferenceWithTypeArgument()
	{
		String source = """
			class T
			{
				Object f = Arrays::<String>sort;
			}
			""";
		Set<SemanticNode> actual = parseSemanticAst(source);

		Set<SemanticNode> expected = Set.of(
			semanticNode(COMPILATION_UNIT, 0, 46),
			semanticNode(CLASS_DECLARATION, 0, 45, "T"),
			semanticNode(FIELD_DECLARATION, 11, 43),
			semanticNode(METHOD_REFERENCE, 22, 42),
			semanticNode(IDENTIFIER, 22, 28),
			semanticNode(QUALIFIED_NAME, 31, 37));

		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Tests parsing of qualified method reference with type arguments.
	 * Syntax: {@code java.util.Collections::<String>emptyList}.
	 */
	@Test
	public void qualifiedMethodReferenceWithTypeArgs()
	{
		String source = """
			class T
			{
				Object f = java.util.Collections::<String>emptyList;
			}
			""";
		Set<SemanticNode> actual = parseSemanticAst(source);

		Set<SemanticNode> expected = Set.of(
			semanticNode(COMPILATION_UNIT, 0, 66),
			semanticNode(CLASS_DECLARATION, 0, 65, "T"),
			semanticNode(FIELD_DECLARATION, 11, 63),
			semanticNode(METHOD_REFERENCE, 22, 62),
			semanticNode(FIELD_ACCESS, 22, 43),
			semanticNode(FIELD_ACCESS, 22, 31),
			semanticNode(IDENTIFIER, 22, 26),
			semanticNode(QUALIFIED_NAME, 46, 52));

		requireThat(actual, "actual").isEqualTo(expected);
	}

	// ========================================
	// Constructor Reference Type Arguments (3 tests)
	// ========================================

	/**
	 * Tests parsing of constructor reference with single type argument.
	 * Syntax: {@code ArrayList::<String>new}.
	 */
	@Test
	public void constructorReferenceWithSingleTypeArgument()
	{
		String source = """
			class T
			{
				Object f = ArrayList::<String>new;
			}
			""";
		Set<SemanticNode> actual = parseSemanticAst(source);

		Set<SemanticNode> expected = Set.of(
			semanticNode(COMPILATION_UNIT, 0, 48),
			semanticNode(CLASS_DECLARATION, 0, 47, "T"),
			semanticNode(FIELD_DECLARATION, 11, 45),
			semanticNode(METHOD_REFERENCE, 22, 44),
			semanticNode(IDENTIFIER, 22, 31),
			semanticNode(QUALIFIED_NAME, 34, 40));

		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Tests parsing of constructor reference with multiple type arguments.
	 * Syntax: {@code HashMap::<String, Integer>new}.
	 */
	@Test
	public void constructorReferenceWithMultipleTypeArguments()
	{
		String source = """
			class T
			{
				Object f = HashMap::<String, Integer>new;
			}
			""";
		Set<SemanticNode> actual = parseSemanticAst(source);

		Set<SemanticNode> expected = Set.of(
			semanticNode(COMPILATION_UNIT, 0, 55),
			semanticNode(CLASS_DECLARATION, 0, 54, "T"),
			semanticNode(FIELD_DECLARATION, 11, 52),
			semanticNode(METHOD_REFERENCE, 22, 51),
			semanticNode(IDENTIFIER, 22, 29),
			semanticNode(QUALIFIED_NAME, 32, 38),
			semanticNode(QUALIFIED_NAME, 40, 47));

		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Tests parsing of nested class constructor reference with type arguments.
	 * Syntax: {@code Outer.Inner::<String>new}.
	 */
	@Test
	public void nestedClassConstructorReferenceWithTypeArgs()
	{
		String source = """
			class T
			{
				Object f = Outer.Inner::<String>new;
			}
			""";
		Set<SemanticNode> actual = parseSemanticAst(source);

		Set<SemanticNode> expected = Set.of(
			semanticNode(COMPILATION_UNIT, 0, 50),
			semanticNode(CLASS_DECLARATION, 0, 49, "T"),
			semanticNode(FIELD_DECLARATION, 11, 47),
			semanticNode(METHOD_REFERENCE, 22, 46),
			semanticNode(FIELD_ACCESS, 22, 33),
			semanticNode(IDENTIFIER, 22, 27),
			semanticNode(QUALIFIED_NAME, 36, 42));

		requireThat(actual, "actual").isEqualTo(expected);
	}

	// ========================================
	// Edge Cases (4 tests)
	// ========================================

	/**
	 * Tests parsing of diamond operator as distinct from explicit type arguments.
	 * The diamond operator {@code <>} allows type inference, while explicit type args {@code <T>}
	 * specify the type directly.
	 */
	@Test
	public void diamondOperatorDistinctFromExplicitTypeArgs()
	{
		String source = """
			class T
			{
				void m()
				{
					var a = new ArrayList<>();
					var b = obj.<String>method();
				}
			}
			""";
		Set<SemanticNode> actual = parseSemanticAst(source);

		Set<SemanticNode> expected = Set.of(
			semanticNode(COMPILATION_UNIT, 0, 89),
			semanticNode(CLASS_DECLARATION, 0, 88, "T"),
			semanticNode(METHOD_DECLARATION, 11, 86),
			semanticNode(BLOCK, 21, 86),
			semanticNode(OBJECT_CREATION, 33, 50),
			semanticNode(PARAMETERIZED_TYPE, 37, 48),
			semanticNode(QUALIFIED_NAME, 37, 46),
			semanticNode(METHOD_INVOCATION, 62, 82),
			semanticNode(FIELD_ACCESS, 62, 80),
			semanticNode(IDENTIFIER, 62, 65),
			semanticNode(QUALIFIED_NAME, 67, 73));

		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Tests parsing of deeply nested generic type arguments (3+ levels).
	 * Syntax: {@code obj.<Map<String, List<Integer>>>method()}.
	 */
	@Test
	public void deeplyNestedGenericTypeArguments()
	{
		String source = """
			class T
			{
				void m()
				{
					obj.<Map<String, List<Integer>>>method();
				}
			}
			""";
		Set<SemanticNode> actual = parseSemanticAst(source);

		Set<SemanticNode> expected = Set.of(
			semanticNode(COMPILATION_UNIT, 0, 72),
			semanticNode(CLASS_DECLARATION, 0, 71, "T"),
			semanticNode(METHOD_DECLARATION, 11, 69),
			semanticNode(BLOCK, 21, 69),
			semanticNode(METHOD_INVOCATION, 25, 65),
			semanticNode(FIELD_ACCESS, 25, 63),
			semanticNode(PARAMETERIZED_TYPE, 25, 57),
			semanticNode(QUALIFIED_NAME, 25, 29),
			semanticNode(IDENTIFIER, 25, 28),
			semanticNode(PARAMETERIZED_TYPE, 30, 57),
			semanticNode(QUALIFIED_NAME, 30, 57),
			semanticNode(QUALIFIED_NAME, 30, 33),
			semanticNode(QUALIFIED_NAME, 34, 40),
			semanticNode(PARAMETERIZED_TYPE, 42, 57),
			semanticNode(QUALIFIED_NAME, 42, 57),
			semanticNode(QUALIFIED_NAME, 42, 46),
			semanticNode(QUALIFIED_NAME, 47, 54));

		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Tests parsing of type arguments in ternary expression branches.
	 * Type arguments work correctly in both branches of a conditional expression.
	 */
	@Test
	public void typeArgsInExpressionContexts()
	{
		String source = """
			class T
			{
				void m(boolean f)
				{
					Object x = f ? obj.<String>a() : obj.<Integer>b();
				}
			}
			""";
		Set<SemanticNode> actual = parseSemanticAst(source);

		Set<SemanticNode> expected = Set.of(
			semanticNode(COMPILATION_UNIT, 0, 90),
			semanticNode(CLASS_DECLARATION, 0, 89, "T"),
			semanticNode(METHOD_DECLARATION, 11, 87),
			semanticNode(PARAMETER_DECLARATION, 18, 27, "f"),
			semanticNode(BLOCK, 30, 87),
			semanticNode(QUALIFIED_NAME, 34, 40),
			semanticNode(CONDITIONAL_EXPRESSION, 45, 83),
			semanticNode(IDENTIFIER, 45, 46),
			semanticNode(METHOD_INVOCATION, 49, 64),
			semanticNode(FIELD_ACCESS, 49, 62),
			semanticNode(IDENTIFIER, 49, 52),
			semanticNode(QUALIFIED_NAME, 54, 60),
			semanticNode(METHOD_INVOCATION, 67, 83),
			semanticNode(FIELD_ACCESS, 67, 81),
			semanticNode(IDENTIFIER, 67, 70),
			semanticNode(QUALIFIED_NAME, 72, 79));

		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Tests parsing of type arguments in a return statement.
	 * Type arguments work correctly in return expressions.
	 */
	@Test
	public void typeArgsInReturnStatement()
	{
		String source = """
			class T
			{
				Object m()
				{
					return Collections.<String>emptyList();
				}
			}
			""";
		Set<SemanticNode> actual = parseSemanticAst(source);

		Set<SemanticNode> expected = Set.of(
			semanticNode(COMPILATION_UNIT, 0, 72),
			semanticNode(CLASS_DECLARATION, 0, 71, "T"),
			semanticNode(METHOD_DECLARATION, 11, 69),
			semanticNode(BLOCK, 23, 69),
			semanticNode(RETURN_STATEMENT, 27, 66),
			semanticNode(METHOD_INVOCATION, 34, 65),
			semanticNode(FIELD_ACCESS, 34, 63),
			semanticNode(IDENTIFIER, 34, 45),
			semanticNode(QUALIFIED_NAME, 47, 53));

		requireThat(actual, "actual").isEqualTo(expected);
	}

	// ========================================
	// Error Cases (3 tests)
	// ========================================

	/**
	 * Tests that malformed syntax with missing closing angle bracket fails.
	 */
	@Test
	public void malformedMissingClosingAngleBracket()
	{
		String source = """
			class T
			{
				void m()
				{
					obj.<Stringmethod();
				}
			}
			""";
		assertParseFails(source);
	}

	/**
	 * Tests that type arguments after method name fails.
	 */
	@Test
	public void malformedTypeArgsAfterMethodName()
	{
		String source = """
			class T
			{
				void m()
				{
					obj.method<String>();
				}
			}
			""";
		assertParseFails(source);
	}

	/**
	 * Tests that double angle brackets fail.
	 */
	@Test
	public void malformedDoubleAngleBrackets()
	{
		String source = """
			class T
			{
				void m()
				{
					obj.<<String>>method();
				}
			}
			""";
		assertParseFails(source);
	}
}
