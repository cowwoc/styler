package io.github.cowwoc.styler.cli.pipeline.stages;

import io.github.cowwoc.styler.cli.pipeline.AbstractPipelineStage;
import io.github.cowwoc.styler.cli.pipeline.PipelineException;
import io.github.cowwoc.styler.cli.pipeline.ProcessingContext;
import io.github.cowwoc.styler.parser.IndexOverlayParser;
import io.github.cowwoc.styler.parser.JavaVersion;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Pipeline stage that parses Java source files into AST representations.
 * <p>
 * This stage reads a Java source file from disk and parses it using the {@link IndexOverlayParser},
 * producing a {@link ParsedFile} containing the AST for downstream formatting stages.
 * <p>
 * The parser uses Arena API memory allocation for efficient AST storage with minimal GC pressure.
 * The parser instance remains open and is passed to the next stage via ParsedFile for AST access.
 * <p>
 * Example usage:
 * <pre>{@code
 * ParseStage stage = new ParseStage(JavaVersion.JAVA_21);
 * ProcessingContext context = ProcessingContext.builder(sourceFile).build();
 * StageResult<ParsedFile> result = stage.execute(sourceFile, context);
 *
 * if (result.isSuccess()) {
 *     ParsedFile parsed = result.output().orElseThrow();
 *     // Pass to FormatStage
 * }
 * }</pre>
 *
 * @see ParsedFile
 * @see IndexOverlayParser
 */
public final class ParseStage extends AbstractPipelineStage<Path, ParsedFile>
{
	private final JavaVersion targetVersion;

	/**
	 * Creates a parse stage targeting the latest supported Java version.
	 */
	public ParseStage()
	{
		this(JavaVersion.JAVA_25);
	}

	/**
	 * Creates a parse stage targeting a specific Java version.
	 *
	 * @param targetVersion the Java version to target for parsing (never {@code null})
	 * @throws NullPointerException if {@code targetVersion} is {@code null}
	 */
	public ParseStage(JavaVersion targetVersion)
	{
		super();
		if (targetVersion == null)
		{
			throw new NullPointerException("targetVersion must not be null");
		}
		this.targetVersion = targetVersion;
	}

	@Override
	protected ParsedFile process(Path input, ProcessingContext context) throws PipelineException
	{
		try
		{
			// Read source file
			String sourceText = Files.readString(input);

			// Create parser with Arena-based AST storage
			IndexOverlayParser parser = new IndexOverlayParser(sourceText, targetVersion);

			// Parse source into AST
			int rootNodeId = parser.parse();

			// Return parsed representation (parser remains open for FormatStage)
			return new ParsedFile(input, parser, rootNodeId, sourceText);
		}
		catch (IOException e)
		{
			throw new PipelineException(
				"Failed to read source file: " + e.getMessage(),
				input,
				getStageId(),
				e);
		}
		catch (Exception e)
		{
			throw new PipelineException(
				"Parse failed: " + e.getMessage(),
				input,
				getStageId(),
				e);
		}
	}

	@Override
	protected void validateInput(Path input, ProcessingContext context) throws PipelineException
	{
		super.validateInput(input, context);

		if (!Files.exists(input))
		{
			throw new PipelineException(
				"Source file does not exist",
				input,
				getStageId());
		}

		if (!Files.isReadable(input))
		{
			throw new PipelineException(
				"Source file is not readable",
				input,
				getStageId());
		}

		if (!Files.isRegularFile(input))
		{
			throw new PipelineException(
				"Source path is not a regular file",
				input,
				getStageId());
		}
	}

	@Override
	public String getStageId()
	{
		return "parse";
	}
}
