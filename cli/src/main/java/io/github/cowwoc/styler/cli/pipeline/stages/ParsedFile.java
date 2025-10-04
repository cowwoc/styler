package io.github.cowwoc.styler.cli.pipeline.stages;

import io.github.cowwoc.styler.parser.ArenaNodeStorage;
import io.github.cowwoc.styler.parser.IndexOverlayParser;

import java.nio.file.Path;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;

/**
 * Represents a parsed Java source file with its AST representation.
 * <p>
 * This record encapsulates the result of parsing a Java source file, containing the
 * parser instance, root node ID, source text, and source file path. The parser instance
 * must remain open until formatting is complete to allow AST traversal.
 * <p>
 * Example usage:
 * <pre>{@code
 * String sourceCode = Files.readString(sourceFile);
 * try (IndexOverlayParser parser = new IndexOverlayParser(sourceCode)) {
 *     int rootNode = parser.parse();
 *     ParsedFile parsed = new ParsedFile(sourceFile, parser, rootNode, sourceCode);
 *     // Pass to FormatStage for transformation
 * }
 * }</pre>
 *
 * @param sourceFile the path to the source file that was parsed (never {@code null})
 * @param parser the parser instance containing the AST (never {@code null})
 * @param rootNodeId the ID of the root AST node in the arena storage
 * @param sourceText the original source code that was parsed (never {@code null})
 */
public record ParsedFile(Path sourceFile, IndexOverlayParser parser, int rootNodeId, String sourceText)
{
	/**
	 * Compact constructor with validation.
	 */
	public ParsedFile
	{
		requireThat(sourceFile, "sourceFile").isNotNull();
		requireThat(parser, "parser").isNotNull();
		requireThat(sourceText, "sourceText").isNotNull();
	}

	/**
	 * Returns the node storage containing the AST nodes.
	 *
	 * @return the arena node storage (never {@code null})
	 */
	public ArenaNodeStorage getNodeStorage()
	{
		return parser.getNodeStorage();
	}
}
