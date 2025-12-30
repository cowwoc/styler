package io.github.cowwoc.styler.parser.test;

import io.github.cowwoc.styler.parser.test.ParserTestUtils.SemanticNode;
import org.testng.annotations.Test;

import java.util.Set;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;
import static io.github.cowwoc.styler.ast.core.NodeType.ASSIGNMENT_EXPRESSION;
import static io.github.cowwoc.styler.ast.core.NodeType.BINARY_EXPRESSION;
import static io.github.cowwoc.styler.ast.core.NodeType.BLOCK;
import static io.github.cowwoc.styler.ast.core.NodeType.CLASS_DECLARATION;
import static io.github.cowwoc.styler.ast.core.NodeType.COMPILATION_UNIT;
import static io.github.cowwoc.styler.ast.core.NodeType.CONSTRUCTOR_DECLARATION;
import static io.github.cowwoc.styler.ast.core.NodeType.FIELD_ACCESS;
import static io.github.cowwoc.styler.ast.core.NodeType.FIELD_DECLARATION;
import static io.github.cowwoc.styler.ast.core.NodeType.IDENTIFIER;
import static io.github.cowwoc.styler.ast.core.NodeType.INTEGER_LITERAL;
import static io.github.cowwoc.styler.ast.core.NodeType.METHOD_DECLARATION;
import static io.github.cowwoc.styler.ast.core.NodeType.METHOD_INVOCATION;
import static io.github.cowwoc.styler.ast.core.NodeType.PARAMETER_DECLARATION;
import static io.github.cowwoc.styler.ast.core.NodeType.QUALIFIED_NAME;
import static io.github.cowwoc.styler.ast.core.NodeType.RETURN_STATEMENT;
import static io.github.cowwoc.styler.ast.core.NodeType.SUPER_EXPRESSION;
import static io.github.cowwoc.styler.ast.core.NodeType.THIS_EXPRESSION;
import static io.github.cowwoc.styler.parser.test.ParserTestUtils.parseSemanticAst;
import static io.github.cowwoc.styler.parser.test.ParserTestUtils.semanticNode;

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
			semanticNode(BLOCK, 48, 81),
			semanticNode(CLASS_DECLARATION, 0, 86, "Outer"),
			semanticNode(CLASS_DECLARATION, 15, 84, "Inner"),
			semanticNode(COMPILATION_UNIT, 0, 87),
			semanticNode(IDENTIFIER, 66, 71),
			semanticNode(METHOD_DECLARATION, 32, 81),
			semanticNode(QUALIFIED_NAME, 53, 59),
			semanticNode(THIS_EXPRESSION, 66, 76));
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
			semanticNode(BLOCK, 102, 132),
			semanticNode(BLOCK, 31, 35),
			semanticNode(CLASS_DECLARATION, 0, 37, "Parent"),
			semanticNode(CLASS_DECLARATION, 39, 137, "Outer"),
			semanticNode(CLASS_DECLARATION, 69, 135, "Inner"),
			semanticNode(COMPILATION_UNIT, 0, 138),
			semanticNode(FIELD_ACCESS, 107, 125),
			semanticNode(IDENTIFIER, 107, 112),
			semanticNode(METHOD_DECLARATION, 16, 35),
			semanticNode(METHOD_DECLARATION, 86, 132),
			semanticNode(METHOD_INVOCATION, 107, 127),
			semanticNode(QUALIFIED_NAME, 107, 113),
			semanticNode(QUALIFIED_NAME, 59, 65),
			semanticNode(SUPER_EXPRESSION, 107, 118));
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
			semanticNode(BLOCK, 69, 105),
			semanticNode(CLASS_DECLARATION, 0, 114, "Outer"),
			semanticNode(CLASS_DECLARATION, 15, 112, "Middle"),
			semanticNode(CLASS_DECLARATION, 33, 109, "Inner"),
			semanticNode(COMPILATION_UNIT, 0, 115),
			semanticNode(IDENTIFIER, 88, 94),
			semanticNode(METHOD_DECLARATION, 52, 105),
			semanticNode(QUALIFIED_NAME, 75, 81),
			semanticNode(THIS_EXPRESSION, 88, 99));
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
			semanticNode(ASSIGNMENT_EXPRESSION, 70, 88),
			semanticNode(BLOCK, 65, 93),
			semanticNode(CLASS_DECLARATION, 0, 98, "Outer"),
			semanticNode(CLASS_DECLARATION, 15, 96, "Inner"),
			semanticNode(COMPILATION_UNIT, 0, 99),
			semanticNode(FIELD_DECLARATION, 32, 45),
			semanticNode(IDENTIFIER, 70, 75),
			semanticNode(IDENTIFIER, 78, 83),
			semanticNode(METHOD_DECLARATION, 49, 93),
			semanticNode(QUALIFIED_NAME, 70, 75),
			semanticNode(THIS_EXPRESSION, 78, 88));
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
			semanticNode(BLOCK, 102, 132),
			semanticNode(BLOCK, 31, 35),
			semanticNode(CLASS_DECLARATION, 0, 37, "Parent"),
			semanticNode(CLASS_DECLARATION, 39, 137, "Outer"),
			semanticNode(CLASS_DECLARATION, 69, 135, "Inner"),
			semanticNode(COMPILATION_UNIT, 0, 138),
			semanticNode(FIELD_ACCESS, 107, 125),
			semanticNode(IDENTIFIER, 107, 112),
			semanticNode(METHOD_DECLARATION, 16, 35),
			semanticNode(METHOD_DECLARATION, 86, 132),
			semanticNode(METHOD_INVOCATION, 107, 127),
			semanticNode(QUALIFIED_NAME, 107, 113),
			semanticNode(QUALIFIED_NAME, 59, 65),
			semanticNode(SUPER_EXPRESSION, 107, 118));
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
			semanticNode(BLOCK, 99, 133),
			semanticNode(CLASS_DECLARATION, 0, 33, "Parent"),
			semanticNode(CLASS_DECLARATION, 35, 138, "Outer"),
			semanticNode(CLASS_DECLARATION, 65, 136, "Inner"),
			semanticNode(COMPILATION_UNIT, 0, 139),
			semanticNode(FIELD_ACCESS, 111, 128),
			semanticNode(FIELD_DECLARATION, 16, 31),
			semanticNode(IDENTIFIER, 111, 116),
			semanticNode(INTEGER_LITERAL, 28, 30),
			semanticNode(METHOD_DECLARATION, 82, 133),
			semanticNode(QUALIFIED_NAME, 55, 61),
			semanticNode(RETURN_STATEMENT, 104, 129),
			semanticNode(SUPER_EXPRESSION, 111, 122));
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
			semanticNode(BLOCK, 51, 78),
			semanticNode(CLASS_DECLARATION, 0, 83, "Outer"),
			semanticNode(CLASS_DECLARATION, 15, 81, "Inner"),
			semanticNode(COMPILATION_UNIT, 0, 84),
			semanticNode(IDENTIFIER, 63, 68),
			semanticNode(METHOD_DECLARATION, 32, 78),
			semanticNode(RETURN_STATEMENT, 56, 74),
			semanticNode(THIS_EXPRESSION, 63, 73));
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
			semanticNode(BLOCK, 40, 44),
			semanticNode(BLOCK, 80, 108),
			semanticNode(CLASS_DECLARATION, 0, 113, "Outer"),
			semanticNode(CLASS_DECLARATION, 47, 111, "Inner"),
			semanticNode(COMPILATION_UNIT, 0, 114),
			semanticNode(IDENTIFIER, 85, 91),
			semanticNode(IDENTIFIER, 92, 97),
			semanticNode(METHOD_DECLARATION, 15, 44),
			semanticNode(METHOD_DECLARATION, 64, 108),
			semanticNode(METHOD_INVOCATION, 85, 103),
			semanticNode(PARAMETER_DECLARATION, 27, 37, "obj"),
			semanticNode(QUALIFIED_NAME, 27, 33),
			semanticNode(QUALIFIED_NAME, 85, 91),
			semanticNode(THIS_EXPRESSION, 92, 102));
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
			semanticNode(BLOCK, 83, 122),
			semanticNode(CLASS_DECLARATION, 0, 16, "Parent"),
			semanticNode(CLASS_DECLARATION, 18, 127, "Outer"),
			semanticNode(CLASS_DECLARATION, 48, 125, "Inner"),
			semanticNode(COMPILATION_UNIT, 0, 128),
			semanticNode(FIELD_ACCESS, 95, 115),
			semanticNode(IDENTIFIER, 95, 100),
			semanticNode(METHOD_DECLARATION, 65, 122),
			semanticNode(METHOD_INVOCATION, 95, 117),
			semanticNode(QUALIFIED_NAME, 38, 44),
			semanticNode(RETURN_STATEMENT, 88, 118),
			semanticNode(SUPER_EXPRESSION, 95, 106));
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
			semanticNode(BLOCK, 57, 84),
			semanticNode(CLASS_DECLARATION, 0, 89, "Outer"),
			semanticNode(CLASS_DECLARATION, 18, 87, "Inner"),
			semanticNode(COMPILATION_UNIT, 0, 90),
			semanticNode(IDENTIFIER, 69, 74),
			semanticNode(METHOD_DECLARATION, 35, 84),
			semanticNode(QUALIFIED_NAME, 41, 42),
			semanticNode(RETURN_STATEMENT, 62, 80),
			semanticNode(THIS_EXPRESSION, 69, 79));
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
			semanticNode(ASSIGNMENT_EXPRESSION, 66, 87),
			semanticNode(BLOCK, 61, 92),
			semanticNode(CLASS_DECLARATION, 0, 97, "Outer"),
			semanticNode(CLASS_DECLARATION, 34, 95, "Inner"),
			semanticNode(COMPILATION_UNIT, 0, 98),
			semanticNode(CONSTRUCTOR_DECLARATION, 51, 92),
			semanticNode(FIELD_DECLARATION, 15, 31),
			semanticNode(IDENTIFIER, 66, 74),
			semanticNode(IDENTIFIER, 77, 82),
			semanticNode(QUALIFIED_NAME, 66, 74),
			semanticNode(THIS_EXPRESSION, 77, 87));
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
			semanticNode(BINARY_EXPRESSION, 75, 94),
			semanticNode(BLOCK, 63, 99),
			semanticNode(CLASS_DECLARATION, 0, 104, "Outer"),
			semanticNode(CLASS_DECLARATION, 15, 102, "Inner"),
			semanticNode(COMPILATION_UNIT, 0, 105),
			semanticNode(IDENTIFIER, 75, 80),
			semanticNode(IDENTIFIER, 89, 94),
			semanticNode(METHOD_DECLARATION, 32, 99),
			semanticNode(PARAMETER_DECLARATION, 47, 59, "other"),
			semanticNode(QUALIFIED_NAME, 47, 53),
			semanticNode(RETURN_STATEMENT, 68, 95),
			semanticNode(THIS_EXPRESSION, 75, 85));
		requireThat(actual, "actual").isEqualTo(expected);
	}
}
