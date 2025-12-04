/**
 * Styler Pipeline - File processing pipeline orchestration.
 * <p>
 * This package provides the main file processing pipeline that orchestrates the complete workflow from
 * parsing Java source files through formatting, validation, and output generation. The pipeline applies
 * the Chain of Responsibility pattern for stage sequencing and Template Method pattern for consistent
 * stage lifecycle management.
 * <p>
 * <b>Main API</b>:
 * <p>
 * {@link FileProcessingPipeline} - The primary entry point for processing files. Configured via builder
 * pattern:
 * <pre>
 * FileProcessingPipeline pipeline = FileProcessingPipeline.builder().
 *     securityConfig(securityConfig).
 *     formattingRules(rules).
 *     formattingConfig(config).
 *     build();
 *
 * try (PipelineResult result = pipeline.processFile(path))
 * {
 *     if (result.overallSuccess())
 *     {
 *         List&lt;FormattingViolation&gt; violations = result.violations();
 *         // Handle violations
 *     }
 * }
 * </pre>
 * <p>
 * {@link ProcessingContext} - Immutable configuration passed through all pipeline stages. Validates inputs
 * in compact constructor using {@code requireThat()}:
 * <pre>
 * ProcessingContext context = new ProcessingContext(
 *     path,
 *     securityConfig,
 *     formattingConfig,
 *     rules,
 *     false  // validationOnly
 * );
 * </pre>
 * <p>
 * {@link PipelineResult} - Aggregated results from processing. Implements {@code AutoCloseable} for
 * {@code Arena} memory management:
 * <pre>
 * try (PipelineResult result = pipeline.processFile(path))
 * {
 *     List&lt;FormattingViolation&gt; violations = result.violations();
 *     Optional&lt;String&gt; formatted = result.formattedSource();
 * } // Arena automatically cleaned up via close()
 * </pre>
 * <p>
 * {@link StageResult} - Railway-Oriented Programming sealed interface for explicit error handling. Permits
 * three implementations:
 * <ul>
 *     <li>Success - Stage completed successfully with data payload</li>
 *     <li>Failure - Stage failed with error message and optional cause</li>
 *     <li>Skipped - Stage was skipped with reason</li>
 * </ul>
 * <p>
 * <b>Pipeline Stages (internal)</b>:
 * <p>
 * The pipeline executes stages in sequence: Parse &rarr; Format &rarr; Validate &rarr; Output
 * <ul>
 *     <li><strong>ParseStage:</strong> Parses Java source file using {@code IndexOverlayParser}, manages
 *         {@code Arena} lifecycle</li>
 *     <li><strong>FormatStage:</strong> Applies formatting rules, collects violations</li>
 *     <li><strong>ValidationStage:</strong> Security validation, correctness checking</li>
 *     <li><strong>OutputStage:</strong> Generates structured violation reports</li>
 * </ul>
 * <p>
 * <b>Error Handling Strategy</b>:
 * <p>
 * The pipeline implements Railway-Oriented Programming (ROP) for explicit error handling:
 * <ul>
 *     <li><strong>Expected Errors:</strong> Represented as {@code StageResult.Failure} (file not found,
 *         parsing errors, validation failures)</li>
 *     <li><strong>File-Level Isolation:</strong> Errors in one file don't affect others; each file gets
 *         independent {@code PipelineResult}</li>
 *     <li><strong>Stage Isolation:</strong> Failure in stage N stops downstream stages for that file;
 *         earlier results preserved</li>
 *     <li><strong>Fail-Fast Validation:</strong> {@code ProcessingContext} validates all inputs in
 *         constructor</li>
 * </ul>
 * <p>
 * <b>Memory Management</b>:
 * <p>
 * The pipeline efficiently manages {@code Arena}-based memory for AST nodes:
 * <ul>
 *     <li>16 bytes per AST node (contiguous off-heap allocation)</li>
 *     <li>Zero GC pressure during parsing</li>
 *     <li>Bulk deallocation via {@code Arena.close()} (instant cleanup)</li>
 *     <li>Target efficiency: 16MB per 1000 files</li>
 * </ul>
 * <p>
 * <b>Example Usage</b>:
 * <pre>
 * // Create pipeline with formatting rules
 * FileProcessingPipeline pipeline = FileProcessingPipeline.builder().
 *     securityConfig(securityConfig).
 *     formattingRules(List.of(
 *         new LineLengthFormattingRule(),
 *         new ImportOrganizerFormattingRule())).
 *     formattingConfig(formattingConfig).
 *     validationOnly(false).  // Fix mode instead of check mode
 *     build();
 *
 * // Process files
 * Path sourceFile = Paths.get("src/Main.java");
 * try (PipelineResult result = pipeline.processFile(sourceFile))
 * {
 *     if (result.overallSuccess())
 *     {
 *         System.out.println("File processed successfully");
 *         for (FormattingViolation v : result.violations())
 *         {
 *             System.out.println("Line " + v.lineNumber() + ": " + v.message());
 *         }
 *     }
 *     else
 *     {
 *         System.err.println("Pipeline failed: " + result.stageResults());
 *     }
 * }
 * catch (IOException e)
 * {
 *     System.err.println("File processing error: " + e.getMessage());
 * }
 * </pre>
 *
 * @see FileProcessingPipeline
 * @see ProcessingContext
 * @see PipelineResult
 * @see StageResult
 */
package io.github.cowwoc.styler.pipeline;
