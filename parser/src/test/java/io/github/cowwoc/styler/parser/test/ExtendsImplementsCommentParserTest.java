package io.github.cowwoc.styler.parser.test;

import io.github.cowwoc.styler.ast.core.NodeArena;
import io.github.cowwoc.styler.ast.core.NodeType;
import io.github.cowwoc.styler.ast.core.TypeDeclarationAttribute;
import io.github.cowwoc.styler.parser.Parser;
import org.testng.annotations.Test;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;
import static io.github.cowwoc.styler.parser.test.ParserTestUtils.parse;

/**
 * Tests for parsing class declarations with comments between extends and implements clauses.
 */
public class ExtendsImplementsCommentParserTest
{
	/**
	 * Tests class with line comment between extends and implements.
	 */
	@Test
	public void lineCommentBetweenExtendsAndImplements()
	{
		String source = """
			class Foo extends Base  // comment
			        implements Interface
			{
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 18, 22);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 54, 63);
			expected.allocateClassDeclaration(0, 67, new TypeDeclarationAttribute("Foo"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 68);

			requireThat(actual, "actual").isEqualTo(expected);
		}
	}

	/**
	 * Tests class with block comment between extends and implements.
	 */
	@Test
	public void blockCommentBetweenExtendsAndImplements()
	{
		String source = """
			class Foo extends Base /* comment */ implements Interface
			{
			}
			""";
		try (Parser parser = parse(source);
			NodeArena expected = new NodeArena())
		{
			NodeArena actual = parser.getArena();
			expected.allocateNode(NodeType.QUALIFIED_NAME, 18, 22);
			expected.allocateNode(NodeType.QUALIFIED_NAME, 48, 57);
			expected.allocateClassDeclaration(0, 61, new TypeDeclarationAttribute("Foo"));
			expected.allocateNode(NodeType.COMPILATION_UNIT, 0, 62);

			requireThat(actual, "actual").isEqualTo(expected);
		}
	}
}
