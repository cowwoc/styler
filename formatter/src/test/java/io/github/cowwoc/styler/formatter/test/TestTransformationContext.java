package io.github.cowwoc.styler.formatter.test;

import io.github.cowwoc.styler.ast.core.NodeArena;
import io.github.cowwoc.styler.ast.core.NodeIndex;
import io.github.cowwoc.styler.ast.core.NodeType;
import io.github.cowwoc.styler.formatter.TransformationContext;
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

	/**
	 * Creates a test context with the given source code.
	 *
	 * @param sourceCode the source code to use
	 * @throws NullPointerException if sourceCode is null
	 */
	public TestTransformationContext(String sourceCode)
	{
		requireThat(sourceCode, "sourceCode").isNotNull();

		this.sourceCode = sourceCode;
		this.filePath = Path.of("Test.java");
		this.securityConfig = SecurityConfig.DEFAULT;
		this.arena = new NodeArena();

		// Create a minimal AST with a compilation unit covering the entire source
		this.rootNode = arena.allocateNode(NodeType.COMPILATION_UNIT, 0, sourceCode.length(), 0);
	}

	/**
	 * Creates a test context with a custom arena for testing specific AST scenarios.
	 *
	 * @param sourceCode the source code
	 * @param arena      the pre-configured arena
	 * @param rootNode   the root node index
	 * @throws NullPointerException if any parameter is null
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
}
