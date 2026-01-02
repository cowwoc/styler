package io.github.cowwoc.styler.parser.test;

import io.github.cowwoc.styler.parser.test.ParserTestUtils.SemanticNode;
import org.testng.annotations.Test;

import java.util.Set;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;
import static io.github.cowwoc.styler.parser.test.ParserTestUtils.parseSemanticAst;
import static io.github.cowwoc.styler.ast.core.NodeType.CLASS_DECLARATION;
import static io.github.cowwoc.styler.parser.test.ParserTestUtils.*;
import static io.github.cowwoc.styler.parser.test.ParserTestUtils.typeDeclaration;
import static io.github.cowwoc.styler.parser.test.ParserTestUtils.parameterNode;

/**
 * Tests for parsing qualified {@code this} and {@code super} expressions.
 */
public final class QualifiedThisSuperParserTest
{
	/**
	 * Validates that a simple qualified {@code this} expression parses correctly.
	 * Tests the form {@code Outer.this} used to access the enclosing class instance.
	 */
	@Test
	public void testSimpleQualifiedThis()
	{
		Set<SemanticNode> actual = parseSemanticAst("""
			class Outer
			{
				class Inner
				{
					void method()
					{
						Object obj = Outer.this;
					}
				}
			}
			""");

		Set<SemanticNode> expected = Set.of(
			block( 48, 81),
			typeDeclaration(CLASS_DECLARATION, 0, 86, "Outer"),
			typeDeclaration(CLASS_DECLARATION, 15, 84, "Inner"),
			compilationUnit( 0, 87),
			identifier( 66, 71),
			methodDeclaration( 32, 81),
			qualifiedName( 53, 59),
			thisExpression( 66, 76));
		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Validates that a simple qualified {@code super} expression parses correctly.
	 * Tests the form {@code Outer.super} used to access the superclass of an enclosing class.
	 */
	@Test
	public void testSimpleQualifiedSuper()
	{
		Set<SemanticNode> actual = parseSemanticAst("""
			class Parent
			{
				void helper()
				{
				}
			}

			class Outer extends Parent
			{
				class Inner
				{
					void method()
					{
						Outer.super.helper();
					}
				}
			}
			""");

		Set<SemanticNode> expected = Set.of(
			block( 102, 132),
			block( 31, 35),
			typeDeclaration(CLASS_DECLARATION, 0, 37, "Parent"),
			typeDeclaration(CLASS_DECLARATION, 39, 137, "Outer"),
			typeDeclaration(CLASS_DECLARATION, 69, 135, "Inner"),
			compilationUnit( 0, 138),
			fieldAccess( 107, 125),
			identifier( 107, 112),
			methodDeclaration( 16, 35),
			methodDeclaration( 86, 132),
			methodInvocation( 107, 127),
			qualifiedName( 107, 113),
			qualifiedName( 59, 65),
			superExpression( 107, 118));
		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Validates that nested qualified {@code this} expressions parse correctly.
	 * Tests the form {@code Middle.this} used in deeply nested inner classes.
	 */
	@Test
	public void testNestedQualifiedThis()
	{
		Set<SemanticNode> actual = parseSemanticAst("""
			class Outer
			{
				class Middle
				{
					class Inner
					{
						void method()
						{
							Object obj = Middle.this;
						}
					}
				}
			}
			""");

		Set<SemanticNode> expected = Set.of(
			block( 69, 105),
			typeDeclaration(CLASS_DECLARATION, 0, 114, "Outer"),
			typeDeclaration(CLASS_DECLARATION, 15, 112, "Middle"),
			typeDeclaration(CLASS_DECLARATION, 33, 109, "Inner"),
			compilationUnit( 0, 115),
			identifier( 88, 94),
			methodDeclaration( 52, 105),
			qualifiedName( 75, 81),
			thisExpression( 88, 99));
		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Validates that qualified {@code this} in an assignment context parses correctly.
	 * Tests assigning the outer class instance to a variable.
	 */
	@Test
	public void testQualifiedThisInAssignment()
	{
		Set<SemanticNode> actual = parseSemanticAst("""
			class Outer
			{
				class Inner
				{
					Object outer;

					void method()
					{
						outer = Outer.this;
					}
				}
			}
			""");

		Set<SemanticNode> expected = Set.of(
			assignmentExpression( 70, 88),
			block( 65, 93),
			typeDeclaration(CLASS_DECLARATION, 0, 98, "Outer"),
			typeDeclaration(CLASS_DECLARATION, 15, 96, "Inner"),
			compilationUnit( 0, 99),
			fieldDeclaration( 32, 45),
			identifier( 70, 75),
			identifier( 78, 83),
			methodDeclaration( 49, 93),
			qualifiedName( 70, 75),
			thisExpression( 78, 88));
		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Validates that qualified {@code super} method call parses correctly.
	 * Tests calling a superclass method via the enclosing class: {@code Outer.super.doWork()}.
	 */
	@Test
	public void testQualifiedSuperMethodCall()
	{
		Set<SemanticNode> actual = parseSemanticAst("""
			class Parent
			{
				void doWork()
				{
				}
			}

			class Outer extends Parent
			{
				class Inner
				{
					void method()
					{
						Outer.super.doWork();
					}
				}
			}
			""");

		Set<SemanticNode> expected = Set.of(
			block( 102, 132),
			block( 31, 35),
			typeDeclaration(CLASS_DECLARATION, 0, 37, "Parent"),
			typeDeclaration(CLASS_DECLARATION, 39, 137, "Outer"),
			typeDeclaration(CLASS_DECLARATION, 69, 135, "Inner"),
			compilationUnit( 0, 138),
			fieldAccess( 107, 125),
			identifier( 107, 112),
			methodDeclaration( 16, 35),
			methodDeclaration( 86, 132),
			methodInvocation( 107, 127),
			qualifiedName( 107, 113),
			qualifiedName( 59, 65),
			superExpression( 107, 118));
		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Validates that qualified {@code super} field access parses correctly.
	 * Tests accessing a superclass field via the enclosing class: {@code Outer.super.value}.
	 */
	@Test
	public void testQualifiedSuperFieldAccess()
	{
		Set<SemanticNode> actual = parseSemanticAst("""
			class Parent
			{
				int value = 42;
			}

			class Outer extends Parent
			{
				class Inner
				{
					int getValue()
					{
						return Outer.super.value;
					}
				}
			}
			""");

		Set<SemanticNode> expected = Set.of(
			block( 99, 133),
			typeDeclaration(CLASS_DECLARATION, 0, 33, "Parent"),
			typeDeclaration(CLASS_DECLARATION, 35, 138, "Outer"),
			typeDeclaration(CLASS_DECLARATION, 65, 136, "Inner"),
			compilationUnit( 0, 139),
			fieldAccess( 111, 128),
			fieldDeclaration( 16, 31),
			identifier( 111, 116),
			integerLiteral( 28, 30),
			methodDeclaration( 82, 133),
			qualifiedName( 55, 61),
			returnStatement( 104, 129),
			superExpression( 111, 122));
		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Validates that qualified {@code this} in a return statement parses correctly.
	 * Tests returning the enclosing class instance from an inner class method.
	 */
	@Test
	public void testQualifiedThisInReturnStatement()
	{
		Set<SemanticNode> actual = parseSemanticAst("""
			class Outer
			{
				class Inner
				{
					Outer getOuter()
					{
						return Outer.this;
					}
				}
			}
			""");

		Set<SemanticNode> expected = Set.of(
			block( 51, 78),
			typeDeclaration(CLASS_DECLARATION, 0, 83, "Outer"),
			typeDeclaration(CLASS_DECLARATION, 15, 81, "Inner"),
			compilationUnit( 0, 84),
			identifier( 63, 68),
			methodDeclaration( 32, 78),
			returnStatement( 56, 74),
			thisExpression( 63, 73));
		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Validates that qualified {@code this} as a method argument parses correctly.
	 * Tests passing the enclosing class instance to a method.
	 */
	@Test
	public void testQualifiedThisAsMethodArgument()
	{
		Set<SemanticNode> actual = parseSemanticAst("""
			class Outer
			{
				void accept(Object obj)
				{
				}

				class Inner
				{
					void method()
					{
						accept(Outer.this);
					}
				}
			}
			""");

		Set<SemanticNode> expected = Set.of(
			block( 40, 44),
			block( 80, 108),
			typeDeclaration(CLASS_DECLARATION, 0, 113, "Outer"),
			typeDeclaration(CLASS_DECLARATION, 47, 111, "Inner"),
			compilationUnit( 0, 114),
			identifier( 85, 91),
			identifier( 92, 97),
			methodDeclaration( 15, 44),
			methodDeclaration( 64, 108),
			methodInvocation( 85, 103),
			parameterNode( 27, 37, "obj"),
			qualifiedName( 27, 33),
			qualifiedName( 85, 91),
			thisExpression( 92, 102));
		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Validates that chained method call on qualified {@code super} parses correctly.
	 * Tests calling {@code toString()} on the superclass instance: {@code Outer.super.toString()}.
	 */
	@Test
	public void testChainedQualifiedSuperMethodCall()
	{
		Set<SemanticNode> actual = parseSemanticAst("""
			class Parent
			{
			}

			class Outer extends Parent
			{
				class Inner
				{
					String method()
					{
						return Outer.super.toString();
					}
				}
			}
			""");

		Set<SemanticNode> expected = Set.of(
			block( 83, 122),
			typeDeclaration(CLASS_DECLARATION, 0, 16, "Parent"),
			typeDeclaration(CLASS_DECLARATION, 18, 127, "Outer"),
			typeDeclaration(CLASS_DECLARATION, 48, 125, "Inner"),
			compilationUnit( 0, 128),
			fieldAccess( 95, 115),
			identifier( 95, 100),
			methodDeclaration( 65, 122),
			methodInvocation( 95, 117),
			qualifiedName( 38, 44),
			returnStatement( 88, 118),
			superExpression( 95, 106));
		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Validates that qualified {@code this} with a generic outer class parses correctly.
	 * Tests accessing the enclosing instance when the outer class has type parameters.
	 */
	@Test
	public void testGenericOuterClassQualifiedThis()
	{
		Set<SemanticNode> actual = parseSemanticAst("""
			class Outer<T>
			{
				class Inner
				{
					Outer<T> getOuter()
					{
						return Outer.this;
					}
				}
			}
			""");

		Set<SemanticNode> expected = Set.of(
			block( 57, 84),
			typeDeclaration(CLASS_DECLARATION, 0, 89, "Outer"),
			typeDeclaration(CLASS_DECLARATION, 18, 87, "Inner"),
			compilationUnit( 0, 90),
			identifier( 69, 74),
			methodDeclaration( 35, 84),
			qualifiedName( 41, 42),
			returnStatement( 62, 80),
			thisExpression( 69, 79));
		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Validates that qualified {@code this} in an inner class constructor parses correctly.
	 * Tests using the enclosing instance during inner class construction.
	 */
	@Test
	public void testQualifiedThisInInnerClassConstructor()
	{
		Set<SemanticNode> actual = parseSemanticAst("""
			class Outer
			{
				Object outerRef;

				class Inner
				{
					Inner()
					{
						outerRef = Outer.this;
					}
				}
			}
			""");

		Set<SemanticNode> expected = Set.of(
			assignmentExpression( 66, 87),
			block( 61, 92),
			typeDeclaration(CLASS_DECLARATION, 0, 97, "Outer"),
			typeDeclaration(CLASS_DECLARATION, 34, 95, "Inner"),
			compilationUnit( 0, 98),
			constructorDeclaration( 51, 92),
			fieldDeclaration( 15, 31),
			identifier( 66, 74),
			identifier( 77, 82),
			qualifiedName( 66, 74),
			thisExpression( 77, 87));
		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Validates that qualified {@code this} works in a comparison expression.
	 * Tests using the enclosing instance in an equals comparison.
	 */
	@Test
	public void testQualifiedThisInComparison()
	{
		Set<SemanticNode> actual = parseSemanticAst("""
			class Outer
			{
				class Inner
				{
					boolean isSame(Object other)
					{
						return Outer.this == other;
					}
				}
			}
			""");

		Set<SemanticNode> expected = Set.of(
			binaryExpression( 75, 94),
			block( 63, 99),
			typeDeclaration(CLASS_DECLARATION, 0, 104, "Outer"),
			typeDeclaration(CLASS_DECLARATION, 15, 102, "Inner"),
			compilationUnit( 0, 105),
			identifier( 75, 80),
			identifier( 89, 94),
			methodDeclaration( 32, 99),
			parameterNode( 47, 59, "other"),
			qualifiedName( 47, 53),
			returnStatement( 68, 95),
			thisExpression( 75, 85));
		requireThat(actual, "actual").isEqualTo(expected);
	}
}
