package io.github.cowwoc.styler.pipeline.internal;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.that;

import io.github.cowwoc.styler.ast.core.NodeArena;
import io.github.cowwoc.styler.ast.core.NodeIndex;
import java.nio.file.Path;

/**
 * Data produced by the ParseStage.
 * <p>
 * Contains the parsed AST and source code for downstream processing.
 *
 * @param arena the AST node arena
 * @param rootNode the root node of the AST
 * @param sourceCode the source code as string
 * @param filePath the path to the source file
 */
public record ParsedData(NodeArena arena, NodeIndex rootNode, String sourceCode, Path filePath)
{
	/**
	 * Creates parsed data.
	 *
	 * @param arena the AST node arena
	 * @param rootNode the root node of the AST
	 * @param sourceCode the source code as string
	 * @param filePath the path to the source file
	 * @throws AssertionError if any argument is null
	 */
	public ParsedData
	{
		assert that(arena, "arena").isNotNull().elseThrow();
		assert that(rootNode, "rootNode").isNotNull().elseThrow();
		assert that(sourceCode, "sourceCode").isNotNull().elseThrow();
		assert that(filePath, "filePath").isNotNull().elseThrow();
	}
}
