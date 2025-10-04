package io.github.cowwoc.styler.cli.pipeline.stages;

import io.github.cowwoc.styler.cli.pipeline.AbstractPipelineStage;
import io.github.cowwoc.styler.cli.pipeline.PipelineException;
import io.github.cowwoc.styler.cli.pipeline.ProcessingContext;

/**
 * Pipeline stage that formats parsed Java source code.
 * <p>
 * This stage takes a {@link ParsedFile} containing the parsed AST and applies formatting
 * transformations to produce formatted source text.
 * <p>
 * <b>Current Limitation:</b> Full FormattingRule integration requires an AST converter from
 * the index-overlay format (ArenaNodeStorage) to the traditional object-oriented AST
 * (CompilationUnitNode). For this initial implementation, the formatter returns the original
 * source text unchanged. Future enhancement will add proper rule-based formatting.
 * <p>
 * Example usage:
 * <pre>{@code
 * FormatStage stage = new FormatStage();
 * ProcessingContext context = ProcessingContext.builder(sourceFile).build();
 * StageResult<String> result = stage.execute(parsedFile, context);
 *
 * if (result.isSuccess()) {
 *     String formattedSource = result.output().orElseThrow();
 *     // Pass to WriteStage
 * }
 * }</pre>
 *
 * @see ParsedFile
 * @see io.github.cowwoc.styler.formatter.api.FormattingRule
 */
public final class FormatStage extends AbstractPipelineStage<ParsedFile, String>
{
	/**
	 * Creates a new format stage.
	 */
	public FormatStage()
	{
		super();
	}

	@Override
	@SuppressWarnings("PMD.UseTryWithResources") // Parser is owned by ParsedFile, manual close required
	protected String process(ParsedFile input, ProcessingContext context) throws PipelineException
	{
		try
		{
			// FUTURE ENHANCEMENT: Implement full FormattingRule integration
			// This requires creating an AST converter from index-overlay format (ArenaNodeStorage)
			// to traditional format (CompilationUnitNode) that the formatter API expects.
			//
			// Current implementation: return the original source text unchanged (identity transformation).
			// This allows the pipeline to work end-to-end while the AST converter is developed.

			return input.sourceText();
		}
		finally
		{
			// Close the parser to release Arena memory
			try
			{
				input.parser().close();
			}
			catch (Exception e)
			{
				// Log but don't fail - formatting succeeded even if cleanup had issues
				logger.debug("Failed to close parser for {}: {}", input.sourceFile(), e.getMessage());
			}
		}
	}

	@Override
	public String getStageId()
	{
		return "format";
	}

	@Override
	public boolean supportsErrorRecovery()
	{
		return true; // Formatting errors can use fallback strategy (original source)
	}
}
