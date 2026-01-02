package io.github.cowwoc.styler.parser.test;

import io.github.cowwoc.styler.parser.test.ParserTestUtils.SemanticNode;
import org.testng.annotations.Test;

import java.util.Set;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;
import static io.github.cowwoc.styler.parser.test.ParserTestUtils.assertParseFails;
import static io.github.cowwoc.styler.parser.test.ParserTestUtils.parseSemanticAst;
import static io.github.cowwoc.styler.ast.core.NodeType.CLASS_DECLARATION;
import static io.github.cowwoc.styler.parser.test.ParserTestUtils.*;
import static io.github.cowwoc.styler.parser.test.ParserTestUtils.typeDeclaration;
import static io.github.cowwoc.styler.parser.test.ParserTestUtils.parameterNode;

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
			compilationUnit( 0, 63),
			typeDeclaration(CLASS_DECLARATION, 0, 62, "T"),
			methodDeclaration( 11, 60),
			block( 21, 60),
			methodInvocation( 25, 56),
			fieldAccess( 25, 54),
			parameterizedType( 25, 45),
			qualifiedName( 25, 37),
			identifier( 25, 36),
			qualifiedName( 38, 44));

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
			compilationUnit( 0, 60),
			typeDeclaration(CLASS_DECLARATION, 0, 59, "T"),
			methodDeclaration( 11, 57),
			block( 21, 57),
			methodInvocation( 25, 53),
			fieldAccess( 25, 51),
			thisExpression( 25, 29),
			qualifiedName( 31, 37));

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
			compilationUnit( 0, 61),
			typeDeclaration(CLASS_DECLARATION, 0, 60, "T"),
			methodDeclaration( 11, 58),
			block( 21, 58),
			methodInvocation( 25, 54),
			fieldAccess( 25, 52),
			parameterizedType( 25, 46),
			qualifiedName( 25, 29),
			identifier( 25, 28),
			qualifiedName( 30, 36),
			qualifiedName( 38, 45));

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
			compilationUnit( 0, 58),
			typeDeclaration(CLASS_DECLARATION, 0, 57, "T"),
			methodDeclaration( 11, 55),
			block( 21, 55),
			methodInvocation( 25, 51),
			fieldAccess( 25, 49),
			parameterizedType( 25, 43),
			qualifiedName( 25, 29),
			identifier( 25, 28),
			parameterizedType( 30, 43),
			qualifiedName( 30, 43),
			qualifiedName( 30, 34),
			qualifiedName( 35, 41));

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
			compilationUnit( 0, 62),
			typeDeclaration(CLASS_DECLARATION, 0, 61, "T"),
			methodDeclaration( 11, 59),
			block( 21, 59),
			methodInvocation( 25, 55),
			fieldAccess( 25, 53),
			parameterizedType( 25, 47),
			qualifiedName( 25, 29),
			identifier( 25, 28),
			wildcardType( 30, 46),
			qualifiedName( 40, 46));

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
			compilationUnit( 0, 64),
			typeDeclaration(CLASS_DECLARATION, 0, 63, "T"),
			methodDeclaration( 11, 61),
			block( 21, 61),
			methodInvocation( 25, 57),
			fieldAccess( 25, 55),
			methodInvocation( 25, 42),
			fieldAccess( 25, 40),
			parameterizedType( 25, 37),
			qualifiedName( 25, 29),
			identifier( 25, 28),
			qualifiedName( 30, 36),
			qualifiedName( 44, 51));

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
			compilationUnit( 0, 54),
			typeDeclaration(CLASS_DECLARATION, 0, 53, "T"),
			methodDeclaration( 11, 51),
			block( 21, 51),
			methodInvocation( 25, 47),
			fieldAccess( 25, 45),
			superExpression( 25, 30),
			qualifiedName( 32, 38));

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
			compilationUnit( 0, 66),
			typeDeclaration(CLASS_DECLARATION, 0, 65, "T"),
			methodDeclaration( 11, 63),
			block( 21, 63),
			qualifiedName( 25, 31),
			objectCreation( 36, 59),
			qualifiedName( 41, 47),
			qualifiedName( 48, 57));

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
			compilationUnit( 0, 70),
			typeDeclaration(CLASS_DECLARATION, 0, 69, "T"),
			methodDeclaration( 11, 67),
			block( 21, 67),
			qualifiedName( 25, 31),
			objectCreation( 36, 63),
			qualifiedName( 41, 47),
			qualifiedName( 49, 56),
			qualifiedName( 57, 61));

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
			compilationUnit( 0, 75),
			typeDeclaration(CLASS_DECLARATION, 0, 74, "T"),
			methodDeclaration( 11, 72),
			block( 21, 72),
			qualifiedName( 25, 31),
			objectCreation( 36, 68),
			qualifiedName( 41, 47),
			parameterizedType( 48, 66),
			qualifiedName( 48, 57),
			qualifiedName( 58, 65));

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
			compilationUnit( 0, 70),
			typeDeclaration(CLASS_DECLARATION, 0, 69, "T"),
			methodDeclaration( 11, 67),
			block( 21, 67),
			qualifiedName( 25, 31),
			objectCreation( 36, 63),
			qualifiedName( 41, 54),
			parameterizedType( 41, 54),
			qualifiedName( 41, 45),
			qualifiedName( 46, 52),
			qualifiedName( 54, 61));

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
			compilationUnit( 0, 42),
			typeDeclaration(CLASS_DECLARATION, 0, 41, "T"),
			fieldDeclaration( 11, 39),
			methodReference( 22, 38),
			identifier( 22, 26),
			qualifiedName( 29, 35));

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
			compilationUnit( 0, 56),
			typeDeclaration(CLASS_DECLARATION, 0, 55, "T"),
			fieldDeclaration( 11, 53),
			methodReference( 22, 52),
			identifier( 22, 26),
			qualifiedName( 29, 35),
			qualifiedName( 37, 44));

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
			compilationUnit( 0, 46),
			typeDeclaration(CLASS_DECLARATION, 0, 45, "T"),
			fieldDeclaration( 11, 43),
			methodReference( 22, 42),
			identifier( 22, 28),
			qualifiedName( 31, 37));

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
			compilationUnit( 0, 66),
			typeDeclaration(CLASS_DECLARATION, 0, 65, "T"),
			fieldDeclaration( 11, 63),
			methodReference( 22, 62),
			fieldAccess( 22, 43),
			fieldAccess( 22, 31),
			identifier( 22, 26),
			qualifiedName( 46, 52));

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
			compilationUnit( 0, 48),
			typeDeclaration(CLASS_DECLARATION, 0, 47, "T"),
			fieldDeclaration( 11, 45),
			methodReference( 22, 44),
			identifier( 22, 31),
			qualifiedName( 34, 40));

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
			compilationUnit( 0, 55),
			typeDeclaration(CLASS_DECLARATION, 0, 54, "T"),
			fieldDeclaration( 11, 52),
			methodReference( 22, 51),
			identifier( 22, 29),
			qualifiedName( 32, 38),
			qualifiedName( 40, 47));

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
			compilationUnit( 0, 50),
			typeDeclaration(CLASS_DECLARATION, 0, 49, "T"),
			fieldDeclaration( 11, 47),
			methodReference( 22, 46),
			fieldAccess( 22, 33),
			identifier( 22, 27),
			qualifiedName( 36, 42));

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
			compilationUnit( 0, 89),
			typeDeclaration(CLASS_DECLARATION, 0, 88, "T"),
			methodDeclaration( 11, 86),
			block( 21, 86),
			objectCreation( 33, 50),
			parameterizedType( 37, 48),
			qualifiedName( 37, 46),
			methodInvocation( 62, 82),
			fieldAccess( 62, 80),
			identifier( 62, 65),
			qualifiedName( 67, 73));

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
			compilationUnit( 0, 72),
			typeDeclaration(CLASS_DECLARATION, 0, 71, "T"),
			methodDeclaration( 11, 69),
			block( 21, 69),
			methodInvocation( 25, 65),
			fieldAccess( 25, 63),
			parameterizedType( 25, 57),
			qualifiedName( 25, 29),
			identifier( 25, 28),
			parameterizedType( 30, 57),
			qualifiedName( 30, 57),
			qualifiedName( 30, 33),
			qualifiedName( 34, 40),
			parameterizedType( 42, 57),
			qualifiedName( 42, 57),
			qualifiedName( 42, 46),
			qualifiedName( 47, 54));

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
			compilationUnit( 0, 90),
			typeDeclaration(CLASS_DECLARATION, 0, 89, "T"),
			methodDeclaration( 11, 87),
			parameterNode( 18, 27, "f"),
			block( 30, 87),
			qualifiedName( 34, 40),
			conditionalExpression( 45, 83),
			identifier( 45, 46),
			methodInvocation( 49, 64),
			fieldAccess( 49, 62),
			identifier( 49, 52),
			qualifiedName( 54, 60),
			methodInvocation( 67, 83),
			fieldAccess( 67, 81),
			identifier( 67, 70),
			qualifiedName( 72, 79));

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
			compilationUnit( 0, 72),
			typeDeclaration(CLASS_DECLARATION, 0, 71, "T"),
			methodDeclaration( 11, 69),
			block( 23, 69),
			returnStatement( 27, 66),
			methodInvocation( 34, 65),
			fieldAccess( 34, 63),
			identifier( 34, 45),
			qualifiedName( 47, 53));

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
