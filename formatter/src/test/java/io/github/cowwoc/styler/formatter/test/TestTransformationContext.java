package io.github.cowwoc.styler.formatter.test;

import io.github.cowwoc.styler.ast.core.NodeArena;
import io.github.cowwoc.styler.ast.core.NodeIndex;
import io.github.cowwoc.styler.formatter.AstPositionIndex;
import io.github.cowwoc.styler.formatter.TransformationContext;
import io.github.cowwoc.styler.formatter.TypeResolutionConfig;
import io.github.cowwoc.styler.parser.ParseResult;
import io.github.cowwoc.styler.parser.Parser;
import io.github.cowwoc.styler.security.SecurityConfig;

import java.nio.file.Path;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;

/**
 * Test implementation of TransformationContext for formatter unit testing.
 * <p>
 * <b>Thread-safety</b>: This class is not thread-safe.
 */
public final class TestTransformationContext implements TransformationContext
{
	private final NodeArena arena;
	private final NodeIndex rootNode;
	private final String sourceCode;
	private final Path filePath;
	private final SecurityConfig securityConfig;
	private final AstPositionIndex positionIndex;

	/**
	 * Creates a test context with the given source code.
	 *
	 * @param sourceCode the source code to use
	 * @throws NullPointerException     if {@code sourceCode} is {@code null}
	 * @throws IllegalArgumentException if {@code sourceCode} is not valid Java
	 */
	public TestTransformationContext(String sourceCode)
	{
		requireThat(sourceCode, "sourceCode").isNotNull();

		this.sourceCode = sourceCode;
		this.filePath = Path.of("Test.java");
		this.securityConfig = SecurityConfig.DEFAULT;

		Parser parser = new Parser(sourceCode);
		switch (parser.parse())
		{
			case ParseResult.Success success ->
			{
				this.arena = parser.getArena();
				this.rootNode = success.rootNode();
				this.positionIndex = new AstPositionIndex(this.arena, sourceCode.length());
			}
			case ParseResult.Failure failure -> throw new IllegalArgumentException(
				"Failed to parse source code:\n" + failure.getErrorMessage() +
				"\n\nSource:\n" + sourceCode);
		}
	}

	/**
	 * Creates a test context with a custom arena for testing specific AST scenarios.
	 *
	 * @param sourceCode the source code
	 * @param arena      the pre-configured arena
	 * @param rootNode   the root node index
	 * @throws NullPointerException if any argument is null
	 */
	public TestTransformationContext(String sourceCode, NodeArena arena, NodeIndex rootNode)
	{
		requireThat(sourceCode, "sourceCode").isNotNull();
		requireThat(arena, "arena").isNotNull();
		requireThat(rootNode, "rootNode").isNotNull();

		this.sourceCode = sourceCode;
		this.filePath = Path.of("Test.java");
		this.securityConfig = SecurityConfig.DEFAULT;
		this.arena = arena;
		this.rootNode = rootNode;
		this.positionIndex = new AstPositionIndex(arena, sourceCode.length());
	}

	@Override
	public NodeArena arena()
	{
		return arena;
	}

	@Override
	public NodeIndex rootNode()
	{
		return rootNode;
	}

	@Override
	public String sourceCode()
	{
		return sourceCode;
	}

	@Override
	public Path filePath()
	{
		return filePath;
	}

	@Override
	public SecurityConfig securityConfig()
	{
		return securityConfig;
	}

	@Override
	public String getSourceText(NodeIndex nodeIndex)
	{
		requireThat(nodeIndex, "nodeIndex").isNotNull();
		int start = arena.getStart(nodeIndex);
		int end = arena.getEnd(nodeIndex);
		return sourceCode.substring(start, end);
	}

	@Override
	public int getLineNumber(int position)
	{
		requireThat(position, "position").isGreaterThanOrEqualTo(0).
			isLessThanOrEqualTo(sourceCode.length());

		int lineNumber = 1;
		for (int i = 0; i < position; ++i)
		{
			if (sourceCode.charAt(i) == '\n')
				++lineNumber;
		}
		return lineNumber;
	}

	@Override
	public int getColumnNumber(int position)
	{
		requireThat(position, "position").isGreaterThanOrEqualTo(0).
			isLessThanOrEqualTo(sourceCode.length());

		int column = 1;
		for (int i = position - 1; i >= 0 && sourceCode.charAt(i) != '\n'; --i)
			++column;

		return column;
	}

	@Override
	public void checkDeadline()
	{
		// No deadline enforcement in tests
	}

	@Override
	public TypeResolutionConfig typeResolutionConfig()
	{
		return TypeResolutionConfig.EMPTY;
	}

	@Override
	public AstPositionIndex positionIndex()
	{
		return positionIndex;
	}
}
