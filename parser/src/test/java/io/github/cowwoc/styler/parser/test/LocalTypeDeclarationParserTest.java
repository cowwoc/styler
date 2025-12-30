package io.github.cowwoc.styler.parser.test;

import io.github.cowwoc.styler.parser.test.ParserTestUtils.SemanticNode;
import org.testng.annotations.Test;

import java.util.Set;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;
import static io.github.cowwoc.styler.ast.core.NodeType.ANNOTATION;
import static io.github.cowwoc.styler.ast.core.NodeType.ASSIGNMENT_EXPRESSION;
import static io.github.cowwoc.styler.ast.core.NodeType.BINARY_EXPRESSION;
import static io.github.cowwoc.styler.ast.core.NodeType.BLOCK;
import static io.github.cowwoc.styler.ast.core.NodeType.CLASS_DECLARATION;
import static io.github.cowwoc.styler.ast.core.NodeType.COMPILATION_UNIT;
import static io.github.cowwoc.styler.ast.core.NodeType.CONSTRUCTOR_DECLARATION;
import static io.github.cowwoc.styler.ast.core.NodeType.ENUM_CONSTANT;
import static io.github.cowwoc.styler.ast.core.NodeType.ENUM_DECLARATION;
import static io.github.cowwoc.styler.ast.core.NodeType.FIELD_ACCESS;
import static io.github.cowwoc.styler.ast.core.NodeType.FIELD_DECLARATION;
import static io.github.cowwoc.styler.ast.core.NodeType.IDENTIFIER;
import static io.github.cowwoc.styler.ast.core.NodeType.INTEGER_LITERAL;
import static io.github.cowwoc.styler.ast.core.NodeType.INTERFACE_DECLARATION;
import static io.github.cowwoc.styler.ast.core.NodeType.LAMBDA_EXPRESSION;
import static io.github.cowwoc.styler.ast.core.NodeType.METHOD_DECLARATION;
import static io.github.cowwoc.styler.ast.core.NodeType.METHOD_INVOCATION;
import static io.github.cowwoc.styler.ast.core.NodeType.PARAMETER_DECLARATION;
import static io.github.cowwoc.styler.ast.core.NodeType.QUALIFIED_NAME;
import static io.github.cowwoc.styler.ast.core.NodeType.RECORD_DECLARATION;
import static io.github.cowwoc.styler.ast.core.NodeType.RETURN_STATEMENT;
import static io.github.cowwoc.styler.ast.core.NodeType.STRING_LITERAL;
import static io.github.cowwoc.styler.ast.core.NodeType.THIS_EXPRESSION;
import static io.github.cowwoc.styler.parser.test.ParserTestUtils.parseSemanticAst;
import static io.github.cowwoc.styler.parser.test.ParserTestUtils.semanticNode;

/**
 * Tests for parsing local type declarations (classes, interfaces, enums, records)
 * inside method bodies, constructor bodies, and initializer blocks.
 */
public final class LocalTypeDeclarationParserTest
{
	// ==================== Category 1: Basic Local Type Declarations ====================

	/**
	 * Validates parsing of a local class declaration inside a method body.
	 * Local classes have been supported since Java 1.1 and can contain methods.
	 */
	@Test
	public void testLocalClassDeclaration()
	{
		String source = """
			class Test
			{
				void m()
				{
					class Helper
					{
						void help()
						{
						}
					}
				}
			}
			""";
		Set<SemanticNode> actual = parseSemanticAst(source);

		Set<SemanticNode> expected = Set.of(
			semanticNode(COMPILATION_UNIT, 0, 79),
			semanticNode(CLASS_DECLARATION, 0, 78, "Test"),
			semanticNode(METHOD_DECLARATION, 14, 76),
			semanticNode(BLOCK, 24, 76),
			semanticNode(CLASS_DECLARATION, 28, 73, "Helper"),
			semanticNode(METHOD_DECLARATION, 48, 69),
			semanticNode(BLOCK, 63, 69));

		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Validates parsing of a local interface declaration inside a method body.
	 * Local interfaces are supported since JDK 16.
	 */
	@Test
	public void testLocalInterfaceDeclaration()
	{
		String source = """
			class Test
			{
				void m()
				{
					interface Validator
					{
						boolean validate();
					}
				}
			}
			""";
		Set<SemanticNode> actual = parseSemanticAst(source);

		Set<SemanticNode> expected = Set.of(
			semanticNode(COMPILATION_UNIT, 0, 84),
			semanticNode(CLASS_DECLARATION, 0, 83, "Test"),
			semanticNode(METHOD_DECLARATION, 14, 81),
			semanticNode(BLOCK, 24, 81),
			semanticNode(INTERFACE_DECLARATION, 28, 78, "Validator"),
			semanticNode(METHOD_DECLARATION, 55, 74));

		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Validates parsing of a local enum declaration inside a method body.
	 * Local enums are supported since JDK 16.
	 */
	@Test
	public void testLocalEnumDeclaration()
	{
		String source = """
			class Test
			{
				void m()
				{
					enum Status
					{
						OK,
						ERROR
					}
				}
			}
			""";
		Set<SemanticNode> actual = parseSemanticAst(source);

		Set<SemanticNode> expected = Set.of(
			semanticNode(COMPILATION_UNIT, 0, 69),
			semanticNode(CLASS_DECLARATION, 0, 68, "Test"),
			semanticNode(METHOD_DECLARATION, 14, 66),
			semanticNode(BLOCK, 24, 66),
			semanticNode(ENUM_DECLARATION, 28, 63, "Status"),
			semanticNode(ENUM_CONSTANT, 47, 49),
			semanticNode(ENUM_CONSTANT, 54, 59));

		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Validates parsing of a local record declaration inside a method body.
	 * Local records are supported since JDK 16.
	 */
	@Test
	public void testLocalRecordDeclaration()
	{
		String source = """
			class Test
			{
				void m()
				{
					record Point(int x, int y)
					{
					}
				}
			}
			""";
		Set<SemanticNode> actual = parseSemanticAst(source);

		Set<SemanticNode> expected = Set.of(
			semanticNode(COMPILATION_UNIT, 0, 68),
			semanticNode(CLASS_DECLARATION, 0, 67, "Test"),
			semanticNode(METHOD_DECLARATION, 14, 65),
			semanticNode(BLOCK, 24, 65),
			semanticNode(RECORD_DECLARATION, 28, 62, "Point"),
			semanticNode(PARAMETER_DECLARATION, 41, 46, "x"),
			semanticNode(PARAMETER_DECLARATION, 48, 53, "y"));

		requireThat(actual, "actual").isEqualTo(expected);
	}

	// ==================== Category 2: Local Types with Modifiers ====================

	/**
	 * Validates parsing of a local class with final modifier.
	 * Local classes can be declared final to prevent subclassing.
	 */
	@Test
	public void testLocalClassWithFinalModifier()
	{
		String source = """
			class Test
			{
				void m()
				{
					final class FinalHelper
					{
					}
				}
			}
			""";
		Set<SemanticNode> actual = parseSemanticAst(source);

		Set<SemanticNode> expected = Set.of(
			semanticNode(COMPILATION_UNIT, 0, 65),
			semanticNode(CLASS_DECLARATION, 0, 64, "Test"),
			semanticNode(METHOD_DECLARATION, 14, 62),
			semanticNode(BLOCK, 24, 62),
			semanticNode(CLASS_DECLARATION, 34, 59, "FinalHelper"));

		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Validates parsing of a local class with abstract modifier.
	 * Local abstract classes can contain abstract methods.
	 */
	@Test
	public void testLocalClassWithAbstractModifier()
	{
		String source = """
			class Test
			{
				void m()
				{
					abstract class AbstractHelper
					{
						abstract void doWork();
					}
				}
			}
			""";
		Set<SemanticNode> actual = parseSemanticAst(source);

		Set<SemanticNode> expected = Set.of(
			semanticNode(COMPILATION_UNIT, 0, 98),
			semanticNode(CLASS_DECLARATION, 0, 97, "Test"),
			semanticNode(METHOD_DECLARATION, 14, 95),
			semanticNode(BLOCK, 24, 95),
			semanticNode(CLASS_DECLARATION, 37, 92, "AbstractHelper"),
			semanticNode(METHOD_DECLARATION, 65, 88));

		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Validates parsing of a local class with annotation.
	 * Local classes can have annotations like SuppressWarnings.
	 */
	@Test
	public void testLocalClassWithAnnotation()
	{
		String source = """
			class Test
			{
				void m()
				{
					@SuppressWarnings("unchecked")
					class AnnotatedHelper
					{
					}
				}
			}
			""";
		Set<SemanticNode> actual = parseSemanticAst(source);

		Set<SemanticNode> expected = Set.of(
			semanticNode(COMPILATION_UNIT, 0, 96),
			semanticNode(CLASS_DECLARATION, 0, 95, "Test"),
			semanticNode(METHOD_DECLARATION, 14, 93),
			semanticNode(BLOCK, 24, 93),
			semanticNode(ANNOTATION, 28, 58),
			semanticNode(QUALIFIED_NAME, 29, 45),
			semanticNode(STRING_LITERAL, 46, 57),
			semanticNode(CLASS_DECLARATION, 61, 90, "AnnotatedHelper"));

		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Validates parsing of a local record with annotation.
	 * Local records can have annotations applied.
	 */
	@Test
	public void testLocalRecordWithAnnotation()
	{
		String source = """
			class Test
			{
				void m()
				{
					@Deprecated
					record DeprecatedPoint(int x, int y)
					{
					}
				}
			}
			""";
		Set<SemanticNode> actual = parseSemanticAst(source);

		Set<SemanticNode> expected = Set.of(
			semanticNode(COMPILATION_UNIT, 0, 92),
			semanticNode(CLASS_DECLARATION, 0, 91, "Test"),
			semanticNode(METHOD_DECLARATION, 14, 89),
			semanticNode(BLOCK, 24, 89),
			semanticNode(ANNOTATION, 28, 39),
			semanticNode(QUALIFIED_NAME, 29, 39),
			semanticNode(RECORD_DECLARATION, 42, 86, "DeprecatedPoint"),
			semanticNode(PARAMETER_DECLARATION, 65, 70, "x"),
			semanticNode(PARAMETER_DECLARATION, 72, 77, "y"));

		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Validates parsing of a local interface with annotation.
	 * Local interfaces can have annotations like FunctionalInterface.
	 */
	@Test
	public void testLocalInterfaceWithAnnotation()
	{
		String source = """
			class Test
			{
				void m()
				{
					@FunctionalInterface
					interface Processor
					{
						void process();
					}
				}
			}
			""";
		Set<SemanticNode> actual = parseSemanticAst(source);

		Set<SemanticNode> expected = Set.of(
			semanticNode(COMPILATION_UNIT, 0, 103),
			semanticNode(CLASS_DECLARATION, 0, 102, "Test"),
			semanticNode(METHOD_DECLARATION, 14, 100),
			semanticNode(BLOCK, 24, 100),
			semanticNode(ANNOTATION, 28, 48),
			semanticNode(QUALIFIED_NAME, 29, 48),
			semanticNode(INTERFACE_DECLARATION, 51, 97, "Processor"),
			semanticNode(METHOD_DECLARATION, 78, 93));

		requireThat(actual, "actual").isEqualTo(expected);
	}

	// ==================== Category 3: Nesting Contexts ====================

	/**
	 * Validates parsing of a local class inside a constructor body.
	 * Local classes can be declared in constructors, not just methods.
	 */
	@Test
	public void testLocalClassInConstructor()
	{
		String source = """
			class Test
			{
				Test()
				{
					class Helper
					{
					}
				}
			}
			""";
		Set<SemanticNode> actual = parseSemanticAst(source);

		Set<SemanticNode> expected = Set.of(
			semanticNode(COMPILATION_UNIT, 0, 52),
			semanticNode(CLASS_DECLARATION, 0, 51, "Test"),
			semanticNode(CONSTRUCTOR_DECLARATION, 14, 49),
			semanticNode(BLOCK, 22, 49),
			semanticNode(CLASS_DECLARATION, 26, 46, "Helper"));

		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Validates parsing of a local class inside an instance initializer block.
	 * Local classes can be declared in instance initializer blocks.
	 */
	@Test
	public void testLocalClassInInstanceInitializer()
	{
		String source = """
			class Test
			{
				{
					class Helper
					{
					}
				}
			}
			""";
		Set<SemanticNode> actual = parseSemanticAst(source);

		Set<SemanticNode> expected = Set.of(
			semanticNode(COMPILATION_UNIT, 0, 44),
			semanticNode(CLASS_DECLARATION, 0, 43, "Test"),
			semanticNode(BLOCK, 14, 41),
			semanticNode(CLASS_DECLARATION, 18, 38, "Helper"));

		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Validates parsing of a local class inside a static initializer block.
	 * Local classes can be declared in static initializer blocks.
	 */
	@Test
	public void testLocalClassInStaticInitializer()
	{
		String source = """
			class Test
			{
				static
				{
					class Helper
					{
					}
				}
			}
			""";
		Set<SemanticNode> actual = parseSemanticAst(source);

		Set<SemanticNode> expected = Set.of(
			semanticNode(COMPILATION_UNIT, 0, 52),
			semanticNode(CLASS_DECLARATION, 0, 51, "Test"),
			semanticNode(BLOCK, 22, 49),
			semanticNode(CLASS_DECLARATION, 26, 46, "Helper"));

		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Validates parsing of a local record inside a lambda expression body.
	 * Local types can be declared inside lambda bodies (JDK 16+).
	 */
	@Test
	public void testLocalRecordInLambda()
	{
		String source = """
			class Test
			{
				void m()
				{
					Runnable r = () ->
					{
						record Point(int x, int y)
						{
						}
					};
				}
			}
			""";
		Set<SemanticNode> actual = parseSemanticAst(source);

		Set<SemanticNode> expected = Set.of(
			semanticNode(COMPILATION_UNIT, 0, 101),
			semanticNode(CLASS_DECLARATION, 0, 100, "Test"),
			semanticNode(METHOD_DECLARATION, 14, 98),
			semanticNode(BLOCK, 24, 98),
			semanticNode(QUALIFIED_NAME, 28, 36),
			semanticNode(LAMBDA_EXPRESSION, 41, 94),
			semanticNode(BLOCK, 49, 94),
			semanticNode(RECORD_DECLARATION, 54, 90, "Point"),
			semanticNode(PARAMETER_DECLARATION, 67, 72, "x"),
			semanticNode(PARAMETER_DECLARATION, 74, 79, "y"));

		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Validates parsing of multiple local types in a single method.
	 * Multiple local type declarations can coexist in the same method body.
	 */
	@Test
	public void testMultipleLocalTypesInMethod()
	{
		String source = """
			class Test
			{
				void m()
				{
					class Helper1
					{
					}
					class Helper2
					{
					}
					record Data(int value)
					{
					}
				}
			}
			""";
		Set<SemanticNode> actual = parseSemanticAst(source);

		Set<SemanticNode> expected = Set.of(
			semanticNode(COMPILATION_UNIT, 0, 112),
			semanticNode(CLASS_DECLARATION, 0, 111, "Test"),
			semanticNode(METHOD_DECLARATION, 14, 109),
			semanticNode(BLOCK, 24, 109),
			semanticNode(CLASS_DECLARATION, 28, 49, "Helper1"),
			semanticNode(CLASS_DECLARATION, 52, 73, "Helper2"),
			semanticNode(RECORD_DECLARATION, 76, 106, "Data"),
			semanticNode(PARAMETER_DECLARATION, 88, 97, "value"));

		requireThat(actual, "actual").isEqualTo(expected);
	}

	// ==================== Category 4: Complex Local Types ====================

	/**
	 * Validates parsing of a local class with members (field, constructor, method).
	 * Local classes can contain their own fields, constructors, and methods.
	 */
	@Test
	public void testLocalClassWithMembers()
	{
		String source = """
			class Test
			{
				void m()
				{
					class Helper
					{
						private int value;

						Helper(int value)
						{
							this.value = value;
						}

						int getValue()
						{
							return value;
						}
					}
				}
			}
			""";
		Set<SemanticNode> actual = parseSemanticAst(source);

		Set<SemanticNode> expected = Set.of(
			semanticNode(COMPILATION_UNIT, 0, 179),
			semanticNode(CLASS_DECLARATION, 0, 178, "Test"),
			semanticNode(METHOD_DECLARATION, 14, 176),
			semanticNode(BLOCK, 24, 176),
			semanticNode(CLASS_DECLARATION, 28, 173, "Helper"),
			semanticNode(FIELD_DECLARATION, 48, 66),
			semanticNode(CONSTRUCTOR_DECLARATION, 71, 122),
			semanticNode(PARAMETER_DECLARATION, 78, 87, "value"),
			semanticNode(BLOCK, 92, 122),
			semanticNode(THIS_EXPRESSION, 98, 102),
			semanticNode(FIELD_ACCESS, 98, 108),
			semanticNode(IDENTIFIER, 111, 116),
			semanticNode(ASSIGNMENT_EXPRESSION, 98, 116),
			semanticNode(METHOD_DECLARATION, 127, 169),
			semanticNode(BLOCK, 145, 169),
			semanticNode(RETURN_STATEMENT, 151, 164),
			semanticNode(IDENTIFIER, 158, 163));

		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Validates parsing of a local interface with a default method.
	 * Local interfaces can contain default methods.
	 */
	@Test
	public void testLocalInterfaceWithDefaultMethod()
	{
		String source = """
			class Test
			{
				void m()
				{
					interface Processor
					{
						void process();

						default void setup()
						{
						}
					}
				}
			}
			""";
		Set<SemanticNode> actual = parseSemanticAst(source);

		Set<SemanticNode> expected = Set.of(
			semanticNode(COMPILATION_UNIT, 0, 115),
			semanticNode(CLASS_DECLARATION, 0, 114, "Test"),
			semanticNode(METHOD_DECLARATION, 14, 112),
			semanticNode(BLOCK, 24, 112),
			semanticNode(INTERFACE_DECLARATION, 28, 109, "Processor"),
			semanticNode(METHOD_DECLARATION, 55, 70),
			semanticNode(METHOD_DECLARATION, 75, 105),
			semanticNode(BLOCK, 99, 105));

		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Validates parsing of a local enum with constructor and methods.
	 * Local enums can have fields, constructors, and methods.
	 */
	@Test
	public void testLocalEnumWithConstructorAndMethods()
	{
		String source = """
			class Test
			{
				void m()
				{
					enum Status
					{
						OK(0),
						ERROR(1);

						private final int code;

						Status(int code)
						{
							this.code = code;
						}

						int getCode()
						{
							return code;
						}
					}
				}
			}
			""";
		Set<SemanticNode> actual = parseSemanticAst(source);

		Set<SemanticNode> expected = Set.of(
			semanticNode(COMPILATION_UNIT, 0, 202),
			semanticNode(CLASS_DECLARATION, 0, 201, "Test"),
			semanticNode(METHOD_DECLARATION, 14, 199),
			semanticNode(BLOCK, 24, 199),
			semanticNode(ENUM_DECLARATION, 28, 196, "Status"),
			semanticNode(ENUM_CONSTANT, 47, 52),
			semanticNode(INTEGER_LITERAL, 50, 51),
			semanticNode(ENUM_CONSTANT, 57, 65),
			semanticNode(INTEGER_LITERAL, 63, 64),
			semanticNode(FIELD_DECLARATION, 71, 94),
			semanticNode(CONSTRUCTOR_DECLARATION, 99, 147),
			semanticNode(PARAMETER_DECLARATION, 106, 114, "code"),
			semanticNode(BLOCK, 119, 147),
			semanticNode(THIS_EXPRESSION, 125, 129),
			semanticNode(FIELD_ACCESS, 125, 134),
			semanticNode(IDENTIFIER, 137, 141),
			semanticNode(ASSIGNMENT_EXPRESSION, 125, 141),
			semanticNode(METHOD_DECLARATION, 152, 192),
			semanticNode(BLOCK, 169, 192),
			semanticNode(RETURN_STATEMENT, 175, 187),
			semanticNode(IDENTIFIER, 182, 186));

		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Validates parsing of a local record with a custom method.
	 * Local records can contain additional methods beyond the canonical ones.
	 */
	@Test
	public void testLocalRecordWithCustomMethod()
	{
		String source = """
			class Test
			{
				void m()
				{
					record Point(int x, int y)
					{
						double distance()
						{
							return Math.sqrt(x * x + y * y);
						}
					}
				}
			}
			""";
		Set<SemanticNode> actual = parseSemanticAst(source);

		Set<SemanticNode> expected = Set.of(
			semanticNode(COMPILATION_UNIT, 0, 136),
			semanticNode(CLASS_DECLARATION, 0, 135, "Test"),
			semanticNode(METHOD_DECLARATION, 14, 133),
			semanticNode(BLOCK, 24, 133),
			semanticNode(RECORD_DECLARATION, 28, 130, "Point"),
			semanticNode(PARAMETER_DECLARATION, 41, 46, "x"),
			semanticNode(PARAMETER_DECLARATION, 48, 53, "y"),
			semanticNode(METHOD_DECLARATION, 62, 126),
			semanticNode(BLOCK, 83, 126),
			semanticNode(RETURN_STATEMENT, 89, 121),
			semanticNode(FIELD_ACCESS, 96, 105),
			semanticNode(IDENTIFIER, 96, 100),
			semanticNode(METHOD_INVOCATION, 96, 120),
			semanticNode(BINARY_EXPRESSION, 106, 119),
			semanticNode(BINARY_EXPRESSION, 106, 111),
			semanticNode(IDENTIFIER, 106, 107),
			semanticNode(IDENTIFIER, 110, 111),
			semanticNode(BINARY_EXPRESSION, 114, 119),
			semanticNode(IDENTIFIER, 114, 115),
			semanticNode(IDENTIFIER, 118, 119));

		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Validates parsing of nested local classes (local class containing member class).
	 * A local class can contain its own nested class declarations.
	 */
	@Test
	public void testNestedLocalClass()
	{
		String source = """
			class Test
			{
				void m()
				{
					class Outer
					{
						class Inner
						{
						}
					}
				}
			}
			""";
		Set<SemanticNode> actual = parseSemanticAst(source);

		Set<SemanticNode> expected = Set.of(
			semanticNode(COMPILATION_UNIT, 0, 78),
			semanticNode(CLASS_DECLARATION, 0, 77, "Test"),
			semanticNode(METHOD_DECLARATION, 14, 75),
			semanticNode(BLOCK, 24, 75),
			semanticNode(CLASS_DECLARATION, 28, 72, "Outer"),
			semanticNode(CLASS_DECLARATION, 47, 68, "Inner"));

		requireThat(actual, "actual").isEqualTo(expected);
	}

	// ==================== Category 5: Extends/Implements Clauses ====================

	/**
	 * Validates parsing of local class with extends clause.
	 * A local class can extend another class.
	 */
	@Test
	public void testLocalClassExtends()
	{
		String source = """
			class Test
			{
				void m()
				{
					class Base
					{
					}
					class Derived extends Base
					{
					}
				}
			}
			""";
		Set<SemanticNode> actual = parseSemanticAst(source);

		Set<SemanticNode> expected = Set.of(
			semanticNode(COMPILATION_UNIT, 0, 89),
			semanticNode(CLASS_DECLARATION, 0, 88, "Test"),
			semanticNode(METHOD_DECLARATION, 14, 86),
			semanticNode(BLOCK, 24, 86),
			semanticNode(CLASS_DECLARATION, 28, 46, "Base"),
			semanticNode(CLASS_DECLARATION, 49, 83, "Derived"),
			semanticNode(QUALIFIED_NAME, 71, 75));

		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Validates parsing of local class implementing a local interface.
	 * A local class can implement an interface declared in the same scope.
	 */
	@Test
	public void testLocalClassImplements()
	{
		String source = """
			class Test
			{
				void m()
				{
					interface Runnable
					{
						void run();
					}
					class Worker implements Runnable
					{
						public void run()
						{
						}
					}
				}
			}
			""";
		Set<SemanticNode> actual = parseSemanticAst(source);

		Set<SemanticNode> expected = Set.of(
			semanticNode(COMPILATION_UNIT, 0, 149),
			semanticNode(CLASS_DECLARATION, 0, 148, "Test"),
			semanticNode(METHOD_DECLARATION, 14, 146),
			semanticNode(BLOCK, 24, 146),
			semanticNode(INTERFACE_DECLARATION, 28, 69, "Runnable"),
			semanticNode(METHOD_DECLARATION, 54, 65),
			semanticNode(CLASS_DECLARATION, 72, 143, "Worker"),
			semanticNode(QUALIFIED_NAME, 96, 104),
			semanticNode(METHOD_DECLARATION, 112, 139),
			semanticNode(BLOCK, 133, 139));

		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Validates parsing of local record implementing a local interface.
	 * A local record can implement an interface declared in the same scope.
	 */
	@Test
	public void testLocalRecordImplements()
	{
		String source = """
			class Test
			{
				void m()
				{
					interface Printable
					{
						String print();
					}
					record Message(String text) implements Printable
					{
						public String print()
						{
							return text;
						}
					}
				}
			}
			""";
		Set<SemanticNode> actual = parseSemanticAst(source);

		Set<SemanticNode> expected = Set.of(
			semanticNode(COMPILATION_UNIT, 0, 191),
			semanticNode(CLASS_DECLARATION, 0, 190, "Test"),
			semanticNode(METHOD_DECLARATION, 14, 188),
			semanticNode(BLOCK, 24, 188),
			semanticNode(INTERFACE_DECLARATION, 28, 74, "Printable"),
			semanticNode(METHOD_DECLARATION, 55, 70),
			semanticNode(RECORD_DECLARATION, 77, 185, "Message"),
			semanticNode(QUALIFIED_NAME, 92, 98),
			semanticNode(PARAMETER_DECLARATION, 92, 103, "text"),
			semanticNode(QUALIFIED_NAME, 116, 125),
			semanticNode(METHOD_DECLARATION, 133, 181),
			semanticNode(BLOCK, 158, 181),
			semanticNode(RETURN_STATEMENT, 164, 176),
			semanticNode(IDENTIFIER, 171, 175));

		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Validates parsing of local class with both extends and implements clauses.
	 * A local class can extend a class and implement an interface simultaneously.
	 */
	@Test
	public void testLocalClassExtendsAndImplements()
	{
		String source = """
			class Test
			{
				void m()
				{
					abstract class Base
					{
					}
					interface Runnable
					{
						void run();
					}
					class Worker extends Base implements Runnable
					{
						public void run()
						{
						}
					}
				}
			}
			""";
		Set<SemanticNode> actual = parseSemanticAst(source);

		Set<SemanticNode> expected = Set.of(
			semanticNode(COMPILATION_UNIT, 0, 192),
			semanticNode(CLASS_DECLARATION, 0, 191, "Test"),
			semanticNode(METHOD_DECLARATION, 14, 189),
			semanticNode(BLOCK, 24, 189),
			semanticNode(CLASS_DECLARATION, 37, 55, "Base"),
			semanticNode(INTERFACE_DECLARATION, 58, 99, "Runnable"),
			semanticNode(METHOD_DECLARATION, 84, 95),
			semanticNode(CLASS_DECLARATION, 102, 186, "Worker"),
			semanticNode(QUALIFIED_NAME, 123, 127),
			semanticNode(QUALIFIED_NAME, 139, 147),
			semanticNode(METHOD_DECLARATION, 155, 182),
			semanticNode(BLOCK, 176, 182));

		requireThat(actual, "actual").isEqualTo(expected);
	}

	/**
	 * Validates parsing of local interface extending another local interface.
	 * A local interface can extend another interface declared in the same scope.
	 */
	@Test
	public void testLocalInterfaceExtends()
	{
		String source = """
			class Test
			{
				void m()
				{
					interface Base
					{
					}
					interface Extended extends Base
					{
					}
				}
			}
			""";
		Set<SemanticNode> actual = parseSemanticAst(source);

		Set<SemanticNode> expected = Set.of(
			semanticNode(COMPILATION_UNIT, 0, 98),
			semanticNode(CLASS_DECLARATION, 0, 97, "Test"),
			semanticNode(METHOD_DECLARATION, 14, 95),
			semanticNode(BLOCK, 24, 95),
			semanticNode(INTERFACE_DECLARATION, 28, 50, "Base"),
			semanticNode(INTERFACE_DECLARATION, 53, 92, "Extended"),
			semanticNode(QUALIFIED_NAME, 80, 84));

		requireThat(actual, "actual").isEqualTo(expected);
	}
}
