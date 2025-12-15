package io.github.cowwoc.styler.pipeline;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import io.github.cowwoc.styler.ast.core.NodeArena;
import io.github.cowwoc.styler.ast.core.NodeIndex;
import io.github.cowwoc.styler.formatter.FormattingRule;
import io.github.cowwoc.styler.formatter.FormattingConfiguration;
import io.github.cowwoc.styler.formatter.FormattingViolation;
import io.github.cowwoc.styler.formatter.TransformationContext;
import io.github.cowwoc.styler.formatter.TypeResolutionConfig;
import io.github.cowwoc.styler.parser.Parser;
import io.github.cowwoc.styler.pipeline.internal.DefaultTransformationContext;
import io.github.cowwoc.styler.pipeline.internal.FormatResult;
import io.github.cowwoc.styler.pipeline.internal.ParsedData;
import io.github.cowwoc.styler.pipeline.output.OutputFormat;
import io.github.cowwoc.styler.pipeline.output.ViolationReport;
import io.github.cowwoc.styler.pipeline.output.ViolationReportRenderer;
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
	private final List<FormattingConfiguration> formattingConfigs;
	private final boolean validationOnly;
	private final List<PipelineStage> stages;

	/**
	 * Creates a FileProcessingPipeline with configuration.
	 *
	 * @param securityConfig the security configuration
	 * @param formattingRules the formatting rules to apply
	 * @param formattingConfigs the list of formatting configurations for all rules
	 * @param validationOnly true to only validate without applying fixes
	 * @param stages the pipeline stages in execution order
	 */
	private FileProcessingPipeline(
			SecurityConfig securityConfig,
			List<FormattingRule> formattingRules,
			List<FormattingConfiguration> formattingConfigs,
			boolean validationOnly,
			List<PipelineStage> stages)
	{
		this.securityConfig = securityConfig;
		this.formattingRules = List.copyOf(formattingRules);
		this.formattingConfigs = List.copyOf(formattingConfigs);
		this.validationOnly = validationOnly;
		this.stages = List.copyOf(stages);
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
				formattingConfigs,
				formattingRules,
				validationOnly,
				TypeResolutionConfig.EMPTY);

		// Execute stages in sequence, passing data between them
		Object previousStageData = null;
		for (PipelineStage stage : stages)
		{
			StageResult result = stage.execute(context, previousStageData);
			results.add(result);

			// Stop on failure (file-level isolation)
			if (!result.isSuccess())
			{
				break;
			}

			// Extract data from successful result for next stage
			if (result instanceof StageResult.Success success)
			{
				previousStageData = success.data();
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
		private List<FormattingConfiguration> formattingConfigs = List.of();
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
		 * Sets the list of formatting configurations.
		 *
		 * @param configs the list of formatting configurations for all rules
		 * @return this builder for chaining
		 * @throws NullPointerException if {@code configs} is {@code null}
		 */
		public Builder formattingConfigs(List<FormattingConfiguration> configs)
		{
			this.formattingConfigs = requireThat(configs, "configs").isNotNull().getValue();
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
			requireThat(formattingConfigs, "formattingConfigs").isNotNull();

			// Initialize stages (package-private implementations)
			List<PipelineStage> stages = new ArrayList<>();
			stages.add(new ParseStage());
			stages.add(new FormatStage());
			stages.add(new ValidationStage());
			stages.add(new OutputStage());

			return new FileProcessingPipeline(
					securityConfig,
					formattingRules,
					formattingConfigs,
					validationOnly,
					stages);
		}
	}

	/**
	 * Parses source code files into an abstract syntax tree (AST) representation.
	 * <p>
	 * Receives {@code ProcessingContext} with a file path to parse. Creates a {@code NodeArena}
	 * for AST storage and parses the content using the configured parser.
	 * <p>
	 * Returns {@code StageResult.Success} containing the parsed AST data and {@code NodeArena}.
	 * Returns {@code StageResult.Failure} when parsing fails (file not found, malformed syntax, etc.).
	 * <p>
	 * <b>Thread-safety</b>: This class is thread-safe. Instances are stateless and may be
	 * invoked concurrently from multiple virtual threads processing different files.
	 */
	private static final class ParseStage extends AbstractPipelineStage
	{
		@Override
		protected void setup(ProcessingContext context)
		{
			// No setup needed
		}

		/**
		 * Parses the source file into an AST representation.
		 * <p>
		 * Reads the file from {@code context.filePath()}, creates a {@code NodeArena} for AST storage,
		 * and parses the content using the configured parser.
		 *
		 * @param context the processing context containing file path and configuration
		 * @return {@code StageResult.Success} with the parsed AST data, or {@code StageResult.Failure}
		 *         if parsing fails
		 * @throws Exception if an unexpected error occurs during parsing
		 */
		@Override
		protected StageResult executeStage(ProcessingContext context, Object previousStageData)
			throws Exception
		{
			// Validate file exists before attempting to process
			if (!Files.exists(context.filePath()))
			{
				return new StageResult.Failure("File not found: " + context.filePath(), null);
			}

			// Read source code
			String sourceCode = Files.readString(context.filePath(), StandardCharsets.UTF_8);

			// Parse source code
			Parser parser = new Parser(sourceCode);
			NodeIndex rootNode = parser.parse();
			NodeArena arena = parser.getArena();

			// Return parsed data for next stage
			return new StageResult.Success(new ParsedData(arena, rootNode, sourceCode, context.filePath()));
		}

		@Override
		protected void cleanup(ProcessingContext context)
		{
			// No cleanup needed
		}

		@Override
		public String getStageName()
		{
			return "parse";
		}
	}

	/**
	 * Applies formatting rules to the parsed AST and detects formatting violations.
	 * <p>
	 * Receives {@code ProcessingContext} with the parsed AST and formatting rules to apply.
	 * Executes each rule sequentially and aggregates the violations detected.
	 * <p>
	 * Returns {@code StageResult.Success} with the violations found (may be empty if no rules apply).
	 * Returns {@code StageResult.Failure} only when rule execution fails unexpectedly.
	 * <p>
	 * <b>Thread-safety</b>: This class is thread-safe. Instances are stateless and may be
	 * invoked concurrently from multiple virtual threads processing different files.
	 */
	private static final class FormatStage extends AbstractPipelineStage
	{
		@Override
		protected void setup(ProcessingContext context)
		{
			// No setup needed
		}

		/**
		 * Applies formatting rules to the AST and detects violations.
		 * <p>
		 * Iterates through the configured formatting rules and applies each to the AST.
		 * Violations detected by each rule are aggregated into a single list.
		 *
		 * @param context the processing context containing the parsed AST and formatting rules
		 * @return {@code StageResult.Success} with the list of formatting violations, or
		 *         {@code StageResult.Failure} if rule execution fails
		 * @throws Exception if an unexpected error occurs during rule execution
		 */
		@Override
		protected StageResult executeStage(ProcessingContext context, Object previousStageData)
			throws Exception
		{
			// Extract parsed data from previous stage
			if (!(previousStageData instanceof ParsedData parsed))
			{
				return new StageResult.Failure("Expected ParsedData from previous stage", null);
			}

			List<FormattingRule> rules = context.formattingRules();
			List<FormattingConfiguration> configs = context.formattingConfigs();

			// Create transformation context for formatters
			TransformationContext txContext = new DefaultTransformationContext(
				parsed.arena(),
				parsed.rootNode(),
				parsed.sourceCode(),
				parsed.filePath(),
				context.securityConfig(),
				context.typeResolutionConfig());

			if (context.validationOnly())
			{
				// Validation-only mode: analyze rules without formatting
				List<FormattingViolation> allViolations = new ArrayList<>();
				for (FormattingRule rule : rules)
				{
					allViolations.addAll(rule.analyze(txContext, configs));
				}
				return new StageResult.Success(new FormatResult(parsed.sourceCode(), allViolations));
			}

			// Format mode: apply rules sequentially
			String currentSource = parsed.sourceCode();
			for (FormattingRule rule : rules)
			{
				currentSource = rule.format(txContext, configs);
				// Recreate context with new source for next rule
				txContext = new DefaultTransformationContext(
					parsed.arena(),
					parsed.rootNode(),
					currentSource,
					parsed.filePath(),
					context.securityConfig(),
					context.typeResolutionConfig());
			}

			// Collect violations in the final formatted source
			List<FormattingViolation> violations = new ArrayList<>();
			for (FormattingRule rule : rules)
			{
				violations.addAll(rule.analyze(txContext, configs));
			}

			return new StageResult.Success(new FormatResult(currentSource, violations));
		}

		@Override
		protected void cleanup(ProcessingContext context)
		{
			// No cleanup needed
		}

		@Override
		public String getStageName()
		{
			return "format";
		}
	}

	/**
	 * Validates the formatting results and AST integrity.
	 * <p>
	 * Receives {@code ProcessingContext} with the parsed AST and formatting violations.
	 * Performs validation checks such as AST integrity and consistency of formatting results.
	 * <p>
	 * Returns {@code StageResult.Success} with validation results indicating pass/fail status.
	 * In validation-only mode, skips any fix application and reports findings only.
	 * Returns {@code StageResult.Failure} when validation logic fails unexpectedly.
	 * <p>
	 * <b>Thread-safety</b>: This class is thread-safe. Instances are stateless and may be
	 * invoked concurrently from multiple virtual threads processing different files.
	 */
	private static final class ValidationStage extends AbstractPipelineStage
	{
		@Override
		protected void setup(ProcessingContext context)
		{
			// No setup needed
		}

		/**
		 * Validates the formatting results and AST state.
		 * <p>
		 * Checks AST integrity and validates that formatting results are consistent.
		 * Behavior depends on validation-only mode:
		 * <ul>
		 *   <li>Validation-only mode: Reports findings without applying fixes</li>
		 *   <li>Fix mode: Validates before proceeding to output stage</li>
		 * </ul>
		 *
		 * @param context the processing context containing the AST and violations
		 * @return {@code StageResult.Success} with validation pass/fail indication, or
		 *         {@code StageResult.Failure} if validation logic fails unexpectedly
		 * @throws Exception if an unexpected error occurs during validation
		 */
		@Override
		protected StageResult executeStage(ProcessingContext context, Object previousStageData)
			throws Exception
		{
			// Extract format result from previous stage
			if (!(previousStageData instanceof FormatResult formatResult))
			{
				return new StageResult.Failure("Expected FormatResult from previous stage", null);
			}

			// Count violations by rule
			Map<String, Integer> ruleCounts = new HashMap<>();
			for (FormattingViolation violation : formatResult.violations())
			{
				String ruleId = violation.ruleId();
				ruleCounts.put(ruleId, ruleCounts.getOrDefault(ruleId, 0) + 1);
			}

			// Build violation report
			ViolationReport report = new ViolationReport(
				context.filePath(),
				formatResult.violations(),
				ruleCounts);

			return new StageResult.Success(report);
		}

		@Override
		protected void cleanup(ProcessingContext context)
		{
			// No cleanup needed
		}

		@Override
		public String getStageName()
		{
			return "validate";
		}
	}

	/**
	 * Writes formatting results to output destinations.
	 * <p>
	 * Receives {@code ProcessingContext} with the parsed AST and formatting violations.
	 * Determines output destination(s) based on configuration and writes results.
	 * <p>
	 * In validation-only mode, skips file writes and returns success with no modifications.
	 * In fix mode, applies fixes to the file and writes results based on output configuration.
	 * Returns {@code StageResult.Success} when output is successful.
	 * Returns {@code StageResult.Failure} when file write operations fail.
	 * <p>
	 * <b>Thread-safety</b>: This class is thread-safe. Instances are stateless and may be
	 * invoked concurrently from multiple virtual threads processing different files.
	 */
	private static final class OutputStage extends AbstractPipelineStage
	{
		@Override
		protected void setup(ProcessingContext context)
		{
			// No setup needed
		}

		/**
		 * Writes formatting results to the specified output destination.
		 * <p>
		 * Behavior depends on processing mode:
		 * <ul>
		 *   <li>Validation-only mode: Skips file modifications, returns success immediately</li>
		 *   <li>Fix mode: Applies fixes and writes the formatted file using {@code Files.writeString()}</li>
		 * </ul>
		 *
		 * @param context the processing context containing the AST and violations to write
		 * @return {@code StageResult.Success} when output is successful, or
		 *         {@code StageResult.Failure} if file write fails (permission denied, disk full, etc.)
		 * @throws Exception if an unexpected error occurs during output
		 */
		@Override
		protected StageResult executeStage(ProcessingContext context, Object previousStageData)
			throws Exception
		{
			// Extract violation report from previous stage
			if (!(previousStageData instanceof ViolationReport report))
			{
				return new StageResult.Failure("Expected ViolationReport from previous stage", null);
			}

			// Determine output format
			OutputFormat format = context.outputFormatOverride();
			if (format == null)
			{
				format = OutputFormat.HUMAN;
			}

			// Render report
			ViolationReportRenderer renderer = ViolationReportRenderer.create(format);
			String output = renderer.render(report);

			return new StageResult.Success(output);
		}

		@Override
		protected void cleanup(ProcessingContext context)
		{
			// No cleanup needed
		}

		@Override
		public String getStageName()
		{
			return "output";
		}
	}
}
