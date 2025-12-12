package io.github.cowwoc.styler.pipeline;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;

import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.Optional;

import io.github.cowwoc.styler.ast.core.NodeArena;
import io.github.cowwoc.styler.formatter.FormattingViolation;
import io.github.cowwoc.styler.pipeline.internal.FormatResult;
import io.github.cowwoc.styler.pipeline.output.ViolationReport;

/**
 * Aggregated results from processing a single file through the pipeline.
 * <p>
 * This record encapsulates all output from pipeline execution, including stage results, violations,
 * formatted source, and metrics. It implements AutoCloseable to manage Arena-based memory lifecycle.
 * <p>
 * Memory Management:
 * <ul>
 *     <li>The pipeline retains the NodeArena for AST access by downstream code</li>
 *     <li>Callers MUST use try-with-resources to ensure cleanup</li>
 *     <li>Arena cleanup is automatic via close() method</li>
 * </ul>
 * <p>
 * Example:
 * <pre>
 * try (PipelineResult result = pipeline.processFile(path))
 * {
 *     if (result.overallSuccess())
 *     {
 *         for (FormattingViolation v : result.violations())
 *         {
 *             System.out.println(v.message());
 *         }
 *     }
 * } // Arena automatically closed
 * </pre>
 */
public final class PipelineResult implements AutoCloseable
{
	private final Path filePath;
	private final List<StageResult> stageResults;
	private final Duration processingTime;
	private final boolean overallSuccess;
	private final NodeArena arena;

	/**
	 * Creates a pipeline result with all output data.
	 *
	 * @param filePath the path of the processed file
	 * @param stageResults the results from all stages
	 * @param processingTime the total processing time
	 * @param overallSuccess true if all stages succeeded or were skipped
	 * @param arena the NodeArena from parsing (null if parsing failed)
	 * @throws NullPointerException if {@code filePath}, {@code stageResults}, or {@code processingTime} is
	 *     null
	 */
	public PipelineResult(
			Path filePath,
			List<StageResult> stageResults,
			Duration processingTime,
			boolean overallSuccess,
			NodeArena arena)
	{
		this.filePath = requireThat(filePath, "filePath").isNotNull().getValue();
		requireThat(stageResults, "stageResults").isNotNull();
		this.stageResults = List.copyOf(stageResults);
		this.processingTime = requireThat(processingTime, "processingTime").isNotNull().getValue();
		this.overallSuccess = overallSuccess;
		this.arena = arena;
	}

	/**
	 * Returns the path of the processed file.
	 *
	 * @return the file path
	 */
	public Path filePath()
	{
		return filePath;
	}

	/**
	 * Returns the results from all pipeline stages.
	 *
	 * @return unmodifiable list of stage results in execution order
	 */
	public List<StageResult> stageResults()
	{
		return stageResults;
	}

	/**
	 * Returns the total processing time for this file.
	 *
	 * @return the duration of processing
	 */
	public Duration processingTime()
	{
		return processingTime;
	}

	/**
	 * Checks if processing succeeded overall.
	 *
	 * @return true if all stages succeeded or were skipped, false if any stage failed
	 */
	public boolean overallSuccess()
	{
		return overallSuccess;
	}

	/**
	 * Returns all formatting violations collected during pipeline execution.
	 * <p>
	 * Note: A stage with {@code StageResult.Success} can contain violations. "Success" means
	 * the stage executed without errors - detecting violations is the intended behavior of
	 * the analysis, not a failure. The pipeline produces exactly one {@code ViolationReport}
	 * from the ValidationStage.
	 *
	 * @return unmodifiable list of violations, empty if no violations found or if
	 *     ValidationStage was not reached
	 */
	public List<FormattingViolation> violations()
	{
		// The pipeline produces exactly one ViolationReport from ValidationStage
		for (StageResult result : stageResults)
		{
			if (result instanceof StageResult.Success success &&
				success.data() instanceof ViolationReport report)
			{
				return report.violations();
			}
		}
		return List.of();
	}

	/**
	 * Returns the formatted source code if formatting was applied.
	 * <p>
	 * The pipeline produces exactly one {@code FormatResult} from the FormatStage.
	 * Returns empty if:
	 * <ul>
	 *     <li>{@code validationOnly} mode was enabled (source unchanged)</li>
	 *     <li>{@code FormatStage} failed before producing output</li>
	 * </ul>
	 *
	 * @return Optional containing formatted source, empty if not available
	 */
	public Optional<String> formattedSource()
	{
		// The pipeline produces exactly one FormatResult from FormatStage
		for (StageResult result : stageResults)
		{
			if (result instanceof StageResult.Success success &&
				success.data() instanceof FormatResult formatResult)
			{
				return Optional.of(formatResult.formattedSource());
			}
		}
		return Optional.empty();
	}

	/**
	 * Returns the NodeArena for further AST processing.
	 *
	 * @return empty if parsing failed
	 */
	public Optional<NodeArena> arena()
	{
		return Optional.ofNullable(arena);
	}

	/**
	 * Closes the NodeArena to free allocated memory.
	 * <p>
	 * Safe to call multiple times - subsequent calls are no-ops.
	 * Should be called via try-with-resources to ensure cleanup.
	 */
	@Override
	public void close()
	{
		if (arena != null)
		{
			arena.close();
		}
	}
}
