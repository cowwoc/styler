/**
 * Styler Pipeline - File processing pipeline orchestrator.
 *
 * This module provides the orchestration layer for processing Java source files through a configurable
 * pipeline of stages. It integrates the parser (for AST generation), security framework (for validation),
 * and formatter rules (for style checking and correction).
 *
 * Key components:
 * - FileProcessingPipeline: Main entry point and orchestrator
 * - ProcessingContext: Immutable configuration for processing
 * - PipelineResult: Aggregated results from all stages
 * - StageResult: Railway-Oriented Programming sealed interface for error handling
 * - ParseStage, FormatStage, ValidationStage, OutputStage: Pipeline stages
 *
 * Architecture:
 * - Chain of Responsibility pattern for stage sequencing
 * - Template Method pattern for stage lifecycle management
 * - Railway-Oriented Programming for explicit error handling
 * - Arena-based memory management for efficient AST processing
 */
module io.github.cowwoc.styler.pipeline
{
	requires transitive io.github.cowwoc.styler.parser;
	requires transitive io.github.cowwoc.styler.security;
	requires transitive io.github.cowwoc.styler.formatter;
	requires io.github.cowwoc.requirements12.java;
	requires java.logging;
	requires com.fasterxml.jackson.databind;

	exports io.github.cowwoc.styler.pipeline;
	exports io.github.cowwoc.styler.pipeline.output;
}
