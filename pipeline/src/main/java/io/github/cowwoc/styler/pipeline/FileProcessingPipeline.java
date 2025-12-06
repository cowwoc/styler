package io.github.cowwoc.styler.pipeline;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;

import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import io.github.cowwoc.styler.formatter.FormattingRule;
import io.github.cowwoc.styler.formatter.FormattingConfiguration;
import io.github.cowwoc.styler.security.SecurityConfig;

/**
 * Main orchestrator for the file processing pipeline.
 * <p>
 * FileProcessingPipeline coordinates the execution of pipeline stages in sequence:
 * Parse → Format → Validate → Output. It implements the Chain of Responsibility pattern
 * for flexible stage sequencing and Template Method pattern for consistent lifecycle management.
 * <p>
 * Configuration via builder pattern:
 * <pre>
 * FileProcessingPipeline pipeline = FileProcessingPipeline.builder().
 *     securityConfig(securityConfig).
 *     formattingRules(List.of(new LineLengthFormattingRule())).
 *     formattingConfig(config).
 *     validationOnly(false).
 *     build();
 *
 * try (PipelineResult result = pipeline.processFile(path))
 * {
 *     if (result.overallSuccess())
 *     {
 *         // Handle successful processing
 *     }
 * }
 * </pre>
 * <p>
 * Architecture:
 * <ul>
 *     <li><strong>Chain of Responsibility:</strong> Stages are executed in sequence</li>
 *     <li><strong>Template Method:</strong> Base class handles lifecycle, error handling, cleanup</li>
 *     <li><strong>Railway-Oriented Programming:</strong> Explicit success/failure handling</li>
 *     <li><strong>File-Level Isolation:</strong> Errors don't affect other files</li>
 * </ul>
 * <p>
 * Memory Management:
 * <ul>
 *     <li>NodeArena created during parsing and retained in PipelineResult</li>
 *     <li>Caller must use try-with-resources for automatic cleanup</li>
 *     <li>Zero GC pressure during processing (off-heap allocation)</li>
 * </ul>
 */
public final class FileProcessingPipeline
{
	private final SecurityConfig securityConfig;
	private final List<FormattingRule> formattingRules;
	private final FormattingConfiguration formattingConfig;
	private final boolean validationOnly;
	private final List<PipelineStage> stages;

	/**
	 * Creates a FileProcessingPipeline with configuration.
	 *
	 * @param securityConfig the security configuration
	 * @param formattingRules the formatting rules to apply
	 * @param formattingConfig the formatting configuration
	 * @param validationOnly true to only validate without applying fixes
	 * @param stages the pipeline stages in execution order
	 */
	private FileProcessingPipeline(
			SecurityConfig securityConfig,
			List<FormattingRule> formattingRules,
			FormattingConfiguration formattingConfig,
			boolean validationOnly,
			List<PipelineStage> stages)
	{
		this.securityConfig = securityConfig;
		this.formattingRules = formattingRules;
		this.formattingConfig = formattingConfig;
		this.validationOnly = validationOnly;
		this.stages = stages;
	}

	/**
	 * Creates a new builder for configuring the pipeline.
	 *
	 * @return a new Builder instance
	 */
	public static Builder builder()
	{
		return new Builder();
	}

	/**
	 * Processes a single file through the pipeline.
	 *
	 * @param filePath the path to the file to process
	 * @return PipelineResult with processing outcome (should be used with try-with-resources)
	 * @throws NullPointerException if {@code filePath} is {@code null}
	 */
	public PipelineResult processFile(Path filePath)
	{
		requireThat(filePath, "filePath").isNotNull();

		Instant startTime = Instant.now();
		List<StageResult> results = new ArrayList<>();

		ProcessingContext context = ProcessingContext.create(
				filePath,
				securityConfig,
				formattingConfig,
				formattingRules,
				validationOnly);

		// Execute stages in sequence
		for (PipelineStage stage : stages)
		{
			StageResult result = stage.execute(context);
			results.add(result);

			// Stop on failure (file-level isolation)
			if (!result.isSuccess())
			{
				break;
			}
		}

		// Determine overall success
		boolean overallSuccess = results.stream().allMatch(StageResult::isSuccess);

		Duration processingTime = Duration.between(startTime, Instant.now());

		return new PipelineResult(
				filePath,
				results,
				processingTime,
				overallSuccess,
				null);  // Arena will be set by parse stage
	}

	/**
	 * Processes multiple files through the pipeline.
	 *
	 * @param filePaths the paths to the files to process
	 * @return list of PipelineResult objects (each should be closed when done)
	 * @throws NullPointerException if {@code filePaths} is {@code null}
	 */
	public List<PipelineResult> processFiles(List<Path> filePaths)
	{
		requireThat(filePaths, "filePaths").isNotNull();

		List<PipelineResult> results = new ArrayList<>();
		for (Path path : filePaths)
		{
			results.add(processFile(path));
		}
		return results;
	}

	/**
	 * Builder for configuring FileProcessingPipeline.
	 */
	public static final class Builder
	{
		private SecurityConfig securityConfig;
		private List<FormattingRule> formattingRules = List.of();
		private FormattingConfiguration formattingConfig;
		private boolean validationOnly = true;

		/**
		 * Sets the security configuration.
		 *
		 * @param config the security configuration
		 * @return this builder for chaining
		 * @throws NullPointerException if {@code config} is {@code null}
		 */
		public Builder securityConfig(SecurityConfig config)
		{
			this.securityConfig = requireThat(config, "config").isNotNull().getValue();
			return this;
		}

		/**
		 * Sets the formatting rules to apply.
		 *
		 * @param rules the formatting rules
		 * @return this builder for chaining
		 * @throws NullPointerException if {@code rules} is {@code null}
		 */
		public Builder formattingRules(List<FormattingRule> rules)
		{
			this.formattingRules = requireThat(rules, "rules").isNotNull().getValue();
			return this;
		}

		/**
		 * Sets the formatting configuration.
		 *
		 * @param config the formatting configuration
		 * @return this builder for chaining
		 * @throws NullPointerException if {@code config} is {@code null}
		 */
		public Builder formattingConfig(FormattingConfiguration config)
		{
			this.formattingConfig = requireThat(config, "config").isNotNull().getValue();
			return this;
		}

		/**
		 * Sets whether to only validate without applying fixes.
		 *
		 * @param validationOnly true for validation-only mode, false for fix mode
		 * @return this builder for chaining
		 */
		public Builder validationOnly(boolean validationOnly)
		{
			this.validationOnly = validationOnly;
			return this;
		}

		/**
		 * Builds the FileProcessingPipeline with validated configuration.
		 *
		 * @return the configured pipeline
		 * @throws NullPointerException if required configuration is missing
		 */
		public FileProcessingPipeline build()
		{
			requireThat(securityConfig, "securityConfig").isNotNull();
			requireThat(formattingConfig, "formattingConfig").isNotNull();

			// Initialize stages (package-private implementations)
			List<PipelineStage> stages = new ArrayList<>();
			stages.add(new ParseStage());
			stages.add(new FormatStage());
			stages.add(new ValidationStage());
			stages.add(new OutputStage());

			return new FileProcessingPipeline(
					securityConfig,
					formattingRules,
					formattingConfig,
					validationOnly,
					stages);
		}
	}

	// Import stage implementations (package-private)
	// These will be in io.github.cowwoc.styler.pipeline.stages package
	private static final class ParseStage extends AbstractPipelineStage
	{
		@Override
		protected void setup(ProcessingContext context) throws Exception
		{
			// Default: no setup needed
		}

		@Override
		protected StageResult executeStage(ProcessingContext context) throws Exception
		{
			return new StageResult.Skipped("Not yet implemented");
		}

		@Override
		protected void cleanup(ProcessingContext context) throws Exception
		{
			// Default: no cleanup needed
		}

		@Override
		public String getStageName()
		{
			return "parse";
		}
	}

	private static final class FormatStage extends AbstractPipelineStage
	{
		@Override
		protected void setup(ProcessingContext context) throws Exception
		{
			// Default: no setup needed
		}

		@Override
		protected StageResult executeStage(ProcessingContext context) throws Exception
		{
			return new StageResult.Skipped("Not yet implemented");
		}

		@Override
		protected void cleanup(ProcessingContext context) throws Exception
		{
			// Default: no cleanup needed
		}

		@Override
		public String getStageName()
		{
			return "format";
		}
	}

	private static final class ValidationStage extends AbstractPipelineStage
	{
		@Override
		protected void setup(ProcessingContext context) throws Exception
		{
			// Default: no setup needed
		}

		@Override
		protected StageResult executeStage(ProcessingContext context) throws Exception
		{
			return new StageResult.Skipped("Not yet implemented");
		}

		@Override
		protected void cleanup(ProcessingContext context) throws Exception
		{
			// Default: no cleanup needed
		}

		@Override
		public String getStageName()
		{
			return "validate";
		}
	}

	private static final class OutputStage extends AbstractPipelineStage
	{
		@Override
		protected void setup(ProcessingContext context) throws Exception
		{
			// Default: no setup needed
		}

		@Override
		protected StageResult executeStage(ProcessingContext context) throws Exception
		{
			return new StageResult.Skipped("Not yet implemented");
		}

		@Override
		protected void cleanup(ProcessingContext context) throws Exception
		{
			// Default: no cleanup needed
		}

		@Override
		public String getStageName()
		{
			return "output";
		}
	}
}
