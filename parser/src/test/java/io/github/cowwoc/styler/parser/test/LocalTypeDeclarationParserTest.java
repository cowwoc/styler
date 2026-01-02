package io.github.cowwoc.styler.parser.test;

import io.github.cowwoc.styler.parser.test.ParserTestUtils.SemanticNode;
import org.testng.annotations.Test;

import java.util.Set;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;
import static io.github.cowwoc.styler.parser.test.ParserTestUtils.parseSemanticAst;
import static io.github.cowwoc.styler.ast.core.NodeType.CLASS_DECLARATION;
import static io.github.cowwoc.styler.ast.core.NodeType.ENUM_DECLARATION;
import static io.github.cowwoc.styler.ast.core.NodeType.INTERFACE_DECLARATION;
import static io.github.cowwoc.styler.ast.core.NodeType.RECORD_DECLARATION;
import static io.github.cowwoc.styler.parser.test.ParserTestUtils.*;
import static io.github.cowwoc.styler.parser.test.ParserTestUtils.typeDeclaration;
import static io.github.cowwoc.styler.parser.test.ParserTestUtils.parameterNode;

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
			compilationUnit( 0, 79),
			typeDeclaration(CLASS_DECLARATION, 0, 78, "Test"),
			methodDeclaration( 14, 76),
			block( 24, 76),
			typeDeclaration(CLASS_DECLARATION, 28, 73, "Helper"),
			methodDeclaration( 48, 69),
			block( 63, 69));

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
			compilationUnit( 0, 84),
			typeDeclaration(CLASS_DECLARATION, 0, 83, "Test"),
			methodDeclaration( 14, 81),
			block( 24, 81),
			typeDeclaration(INTERFACE_DECLARATION, 28, 78, "Validator"),
			methodDeclaration( 55, 74));

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
			compilationUnit( 0, 69),
			typeDeclaration(CLASS_DECLARATION, 0, 68, "Test"),
			methodDeclaration( 14, 66),
			block( 24, 66),
			typeDeclaration(ENUM_DECLARATION, 28, 63, "Status"),
			enumConstant( 47, 49),
			enumConstant( 54, 59));

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
			compilationUnit( 0, 68),
			typeDeclaration(CLASS_DECLARATION, 0, 67, "Test"),
			methodDeclaration( 14, 65),
			block( 24, 65),
			typeDeclaration(RECORD_DECLARATION, 28, 62, "Point"),
			parameterNode( 41, 46, "x"),
			parameterNode( 48, 53, "y"));

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
			compilationUnit( 0, 65),
			typeDeclaration(CLASS_DECLARATION, 0, 64, "Test"),
			methodDeclaration( 14, 62),
			block( 24, 62),
			typeDeclaration(CLASS_DECLARATION, 34, 59, "FinalHelper"));

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
			compilationUnit( 0, 98),
			typeDeclaration(CLASS_DECLARATION, 0, 97, "Test"),
			methodDeclaration( 14, 95),
			block( 24, 95),
			typeDeclaration(CLASS_DECLARATION, 37, 92, "AbstractHelper"),
			methodDeclaration( 65, 88));

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
			compilationUnit( 0, 96),
			typeDeclaration(CLASS_DECLARATION, 0, 95, "Test"),
			methodDeclaration( 14, 93),
			block( 24, 93),
			annotation( 28, 58),
			qualifiedName( 29, 45),
			stringLiteral( 46, 57),
			typeDeclaration(CLASS_DECLARATION, 61, 90, "AnnotatedHelper"));

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
			compilationUnit( 0, 92),
			typeDeclaration(CLASS_DECLARATION, 0, 91, "Test"),
			methodDeclaration( 14, 89),
			block( 24, 89),
			annotation( 28, 39),
			qualifiedName( 29, 39),
			typeDeclaration(RECORD_DECLARATION, 42, 86, "DeprecatedPoint"),
			parameterNode( 65, 70, "x"),
			parameterNode( 72, 77, "y"));

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
			compilationUnit( 0, 103),
			typeDeclaration(CLASS_DECLARATION, 0, 102, "Test"),
			methodDeclaration( 14, 100),
			block( 24, 100),
			annotation( 28, 48),
			qualifiedName( 29, 48),
			typeDeclaration(INTERFACE_DECLARATION, 51, 97, "Processor"),
			methodDeclaration( 78, 93));

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
			compilationUnit( 0, 52),
			typeDeclaration(CLASS_DECLARATION, 0, 51, "Test"),
			constructorDeclaration( 14, 49),
			block( 22, 49),
			typeDeclaration(CLASS_DECLARATION, 26, 46, "Helper"));

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
			compilationUnit( 0, 44),
			typeDeclaration(CLASS_DECLARATION, 0, 43, "Test"),
			block( 14, 41),
			typeDeclaration(CLASS_DECLARATION, 18, 38, "Helper"));

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
			compilationUnit( 0, 52),
			typeDeclaration(CLASS_DECLARATION, 0, 51, "Test"),
			block( 22, 49),
			typeDeclaration(CLASS_DECLARATION, 26, 46, "Helper"));

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
			compilationUnit( 0, 101),
			typeDeclaration(CLASS_DECLARATION, 0, 100, "Test"),
			methodDeclaration( 14, 98),
			block( 24, 98),
			qualifiedName( 28, 36),
			lambdaExpression( 41, 94),
			block( 49, 94),
			typeDeclaration(RECORD_DECLARATION, 54, 90, "Point"),
			parameterNode( 67, 72, "x"),
			parameterNode( 74, 79, "y"));

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
			compilationUnit( 0, 112),
			typeDeclaration(CLASS_DECLARATION, 0, 111, "Test"),
			methodDeclaration( 14, 109),
			block( 24, 109),
			typeDeclaration(CLASS_DECLARATION, 28, 49, "Helper1"),
			typeDeclaration(CLASS_DECLARATION, 52, 73, "Helper2"),
			typeDeclaration(RECORD_DECLARATION, 76, 106, "Data"),
			parameterNode( 88, 97, "value"));

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
			compilationUnit( 0, 179),
			typeDeclaration(CLASS_DECLARATION, 0, 178, "Test"),
			methodDeclaration( 14, 176),
			block( 24, 176),
			typeDeclaration(CLASS_DECLARATION, 28, 173, "Helper"),
			fieldDeclaration( 48, 66),
			constructorDeclaration( 71, 122),
			parameterNode( 78, 87, "value"),
			block( 92, 122),
			thisExpression( 98, 102),
			fieldAccess( 98, 108),
			identifier( 111, 116),
			assignmentExpression( 98, 116),
			methodDeclaration( 127, 169),
			block( 145, 169),
			returnStatement( 151, 164),
			identifier( 158, 163));

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
			compilationUnit( 0, 115),
			typeDeclaration(CLASS_DECLARATION, 0, 114, "Test"),
			methodDeclaration( 14, 112),
			block( 24, 112),
			typeDeclaration(INTERFACE_DECLARATION, 28, 109, "Processor"),
			methodDeclaration( 55, 70),
			methodDeclaration( 75, 105),
			block( 99, 105));

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
			compilationUnit( 0, 202),
			typeDeclaration(CLASS_DECLARATION, 0, 201, "Test"),
			methodDeclaration( 14, 199),
			block( 24, 199),
			typeDeclaration(ENUM_DECLARATION, 28, 196, "Status"),
			enumConstant( 47, 52),
			integerLiteral( 50, 51),
			enumConstant( 57, 65),
			integerLiteral( 63, 64),
			fieldDeclaration( 71, 94),
			constructorDeclaration( 99, 147),
			parameterNode( 106, 114, "code"),
			block( 119, 147),
			thisExpression( 125, 129),
			fieldAccess( 125, 134),
			identifier( 137, 141),
			assignmentExpression( 125, 141),
			methodDeclaration( 152, 192),
			block( 169, 192),
			returnStatement( 175, 187),
			identifier( 182, 186));

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
			compilationUnit( 0, 136),
			typeDeclaration(CLASS_DECLARATION, 0, 135, "Test"),
			methodDeclaration( 14, 133),
			block( 24, 133),
			typeDeclaration(RECORD_DECLARATION, 28, 130, "Point"),
			parameterNode( 41, 46, "x"),
			parameterNode( 48, 53, "y"),
			methodDeclaration( 62, 126),
			block( 83, 126),
			returnStatement( 89, 121),
			fieldAccess( 96, 105),
			identifier( 96, 100),
			methodInvocation( 96, 120),
			binaryExpression( 106, 119),
			binaryExpression( 106, 111),
			identifier( 106, 107),
			identifier( 110, 111),
			binaryExpression( 114, 119),
			identifier( 114, 115),
			identifier( 118, 119));

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
			compilationUnit( 0, 78),
			typeDeclaration(CLASS_DECLARATION, 0, 77, "Test"),
			methodDeclaration( 14, 75),
			block( 24, 75),
			typeDeclaration(CLASS_DECLARATION, 28, 72, "Outer"),
			typeDeclaration(CLASS_DECLARATION, 47, 68, "Inner"));

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
			compilationUnit( 0, 89),
			typeDeclaration(CLASS_DECLARATION, 0, 88, "Test"),
			methodDeclaration( 14, 86),
			block( 24, 86),
			typeDeclaration(CLASS_DECLARATION, 28, 46, "Base"),
			typeDeclaration(CLASS_DECLARATION, 49, 83, "Derived"),
			qualifiedName( 71, 75));

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
			compilationUnit( 0, 149),
			typeDeclaration(CLASS_DECLARATION, 0, 148, "Test"),
			methodDeclaration( 14, 146),
			block( 24, 146),
			typeDeclaration(INTERFACE_DECLARATION, 28, 69, "Runnable"),
			methodDeclaration( 54, 65),
			typeDeclaration(CLASS_DECLARATION, 72, 143, "Worker"),
			qualifiedName( 96, 104),
			methodDeclaration( 112, 139),
			block( 133, 139));

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
			compilationUnit( 0, 191),
			typeDeclaration(CLASS_DECLARATION, 0, 190, "Test"),
			methodDeclaration( 14, 188),
			block( 24, 188),
			typeDeclaration(INTERFACE_DECLARATION, 28, 74, "Printable"),
			methodDeclaration( 55, 70),
			typeDeclaration(RECORD_DECLARATION, 77, 185, "Message"),
			qualifiedName( 92, 98),
			parameterNode( 92, 103, "text"),
			qualifiedName( 116, 125),
			methodDeclaration( 133, 181),
			block( 158, 181),
			returnStatement( 164, 176),
			identifier( 171, 175));

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
			compilationUnit( 0, 192),
			typeDeclaration(CLASS_DECLARATION, 0, 191, "Test"),
			methodDeclaration( 14, 189),
			block( 24, 189),
			typeDeclaration(CLASS_DECLARATION, 37, 55, "Base"),
			typeDeclaration(INTERFACE_DECLARATION, 58, 99, "Runnable"),
			methodDeclaration( 84, 95),
			typeDeclaration(CLASS_DECLARATION, 102, 186, "Worker"),
			qualifiedName( 123, 127),
			qualifiedName( 139, 147),
			methodDeclaration( 155, 182),
			block( 176, 182));

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
			compilationUnit( 0, 98),
			typeDeclaration(CLASS_DECLARATION, 0, 97, "Test"),
			methodDeclaration( 14, 95),
			block( 24, 95),
			typeDeclaration(INTERFACE_DECLARATION, 28, 50, "Base"),
			typeDeclaration(INTERFACE_DECLARATION, 53, 92, "Extended"),
			qualifiedName( 80, 84));

		requireThat(actual, "actual").isEqualTo(expected);
	}
}
