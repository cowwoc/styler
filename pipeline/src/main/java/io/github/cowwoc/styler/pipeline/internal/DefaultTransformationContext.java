package io.github.cowwoc.styler.pipeline.internal;

import io.github.cowwoc.styler.ast.core.NodeArena;
import io.github.cowwoc.styler.ast.core.NodeIndex;
import io.github.cowwoc.styler.formatter.TransformationContext;
import io.github.cowwoc.styler.formatter.TypeResolutionConfig;
import io.github.cowwoc.styler.formatter.AstPositionIndex;
import io.github.cowwoc.styler.security.SecurityConfig;
import io.github.cowwoc.styler.security.exceptions.ExecutionTimeoutException;

import java.nio.file.Path;
import java.time.Instant;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;

/**
 * Default implementation of TransformationContext for use within the pipeline.
 * <p>
 * Provides secure access to the AST and source file metadata during formatting operations.
 * Enforces execution deadlines to prevent runaway formatters.
 */
public final class DefaultTransformationContext implements TransformationContext
{
	private final NodeArena arena;
	private final NodeIndex rootNode;
	private final String sourceCode;
	private final Path filePath;
	private final SecurityConfig securityConfig;
	private final Instant deadline;
	private final TypeResolutionConfig typeResolutionConfig;
	private final AstPositionIndex positionIndex;

	/**
	 * Creates a transformation context with the given data.
	 *
	 * @param arena the AST node arena
	 * @param rootNode the root node of the AST
	 * @param sourceCode the source code being formatted
	 * @param filePath the path to the source file
	 * @param securityConfig the security configuration for deadline enforcement
	 * @param typeResolutionConfig the type resolution configuration for classpath access
	 * @throws NullPointerException if any argument is null
	 */
	public DefaultTransformationContext(
			NodeArena arena,
			NodeIndex rootNode,
			String sourceCode,
			Path filePath,
			SecurityConfig securityConfig,
			TypeResolutionConfig typeResolutionConfig)
	{
		this.arena = requireThat(arena, "arena").isNotNull().getValue();
		this.rootNode = requireThat(rootNode, "rootNode").isNotNull().getValue();
		this.sourceCode = requireThat(sourceCode, "sourceCode").isNotNull().getValue();
		this.filePath = requireThat(filePath, "filePath").isNotNull().getValue();
		this.securityConfig = requireThat(securityConfig, "securityConfig").isNotNull().getValue();
		this.typeResolutionConfig = requireThat(typeResolutionConfig, "typeResolutionConfig").isNotNull().getValue();

		// Calculate execution deadline based on current time + timeout
		this.deadline = Instant.now().plus(securityConfig.executionTimeout());
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
		// Extract text from source code using node positions
		int startPos = arena.getStart(nodeIndex);
		int endPos = arena.getEnd(nodeIndex);
		return sourceCode.substring(startPos, endPos);
	}

	@Override
	public int getLineNumber(int position)
	{
		// Calculate line number from position in source code
		if (position < 0 || position > sourceCode.length())
		{
			throw new IllegalArgumentException("Position " + position + " out of bounds [0, " +
				sourceCode.length() + "]");
		}

		int lineNumber = 1;
		for (int i = 0; i < position; ++i)
		{
			if (sourceCode.charAt(i) == '\n')
			{
				++lineNumber;
			}
		}
		return lineNumber;
	}

	@Override
	public int getColumnNumber(int position)
	{
		// Calculate column number from position in source code
		if (position < 0 || position > sourceCode.length())
		{
			throw new IllegalArgumentException("Position " + position + " out of bounds [0, " +
				sourceCode.length() + "]");
		}

		int columnNumber = 1;
		for (int i = position - 1; i >= 0; --i)
		{
			if (sourceCode.charAt(i) == '\n')
			{
				break;
			}
			++columnNumber;
		}
		return columnNumber;
	}

	@Override
	public void checkDeadline()
	{
		if (Instant.now().isAfter(deadline))
		{
			throw new ExecutionTimeoutException(filePath, securityConfig.executionTimeout());
		}
	}

	@Override
	public TypeResolutionConfig typeResolutionConfig()
	{
		return typeResolutionConfig;
	}

	@Override
	public AstPositionIndex positionIndex()
	{
		return positionIndex;
	}
}
