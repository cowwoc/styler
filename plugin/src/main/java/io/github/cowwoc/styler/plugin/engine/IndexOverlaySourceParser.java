package io.github.cowwoc.styler.plugin.engine;

import io.github.cowwoc.styler.ast.node.CompilationUnitNode;
import io.github.cowwoc.styler.parser.ArenaNodeStorage;
import io.github.cowwoc.styler.parser.ArenaToAstConverter;
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
			ArenaNodeStorage storage = parser.getNodeStorage();

			ArenaToAstConverter converter = new ArenaToAstConverter();
			return converter.convert(rootNodeId, storage, sourceText);
		}
		catch (UnsupportedOperationException e)
		{
			throw new MojoExecutionException(
				"Formatter requires unsupported node type in: " + sourcePath +
				"\nError: " + e.getMessage() +
				"\nSolution: Extend ArenaToAstConverter to support this node type", e);
		}
		catch (IllegalStateException e)
		{
			throw new MojoExecutionException(
				"Parser generated invalid Arena data for: " + sourcePath +
				"\nError: " + e.getMessage() +
				"\nThis indicates a parser bug - please report with source file", e);
		}
		catch (Exception e)
		{
			throw new MojoExecutionException(
				"Failed to parse source file: " + sourcePath + "\nError: " + e.getMessage(), e);
		}
	}
}
