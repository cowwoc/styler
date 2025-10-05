package io.github.cowwoc.styler.plugin.engine;

import io.github.cowwoc.styler.ast.node.CompilationUnitNode;
import io.github.cowwoc.styler.parser.IndexOverlayParser;
import io.github.cowwoc.styler.parser.JavaVersion;
import org.apache.maven.plugin.MojoExecutionException;

/**
 * SourceParser implementation using IndexOverlayParser.
 * Wraps the Arena-based parser with memory management.
 * Thread-safe (creates new parser instance per invocation).
 */
public final class IndexOverlaySourceParser implements SourceParser
{
	private final JavaVersion targetVersion;

	/**
	 * Creates parser with specified Java version.
	 *
	 * @param targetVersion Java version for parsing
	 */
	public IndexOverlaySourceParser(JavaVersion targetVersion)
	{
		this.targetVersion = targetVersion;
	}

	/**
	 * Creates parser with default Java 25 version.
	 */
	public IndexOverlaySourceParser()
	{
		this(JavaVersion.JAVA_25);
	}

	@Override
	public CompilationUnitNode parse(String sourceText, String sourcePath) throws MojoExecutionException
	{
		try (IndexOverlayParser parser = new IndexOverlayParser(sourceText, targetVersion))
		{
			int rootNodeId = parser.parse();

			// AST node conversion requires implementing Arena node to CompilationUnitNode converter
			throw new UnsupportedOperationException(
			"Arena-to-AST conversion not implemented. " +
			"Requires ArenaNodeConverter to convert Arena node ID " + rootNodeId +
			" to CompilationUnitNode. See todo.md: implement-arena-ast-converter");
		}
		catch (Exception e)
		{
			throw new MojoExecutionException(
				"Failed to parse source file: " + sourcePath + "\nError: " + e.getMessage(), e);
		}
	}
}
