/**
 * Styler Formatter API - Core interfaces and contracts for formatting rules.
 *
 * This module provides the foundation for the styler code formatter's plugin architecture.
 * Formatting rules implement the FormattingRule interface to analyze and transform source code.
 *
 * Key components:
 * - FormattingRule: Base interface for all formatting rules
 * - FormattingViolation: Immutable representation of style violations
 * - FixStrategy: Suggested fixes for violations
 * - TransformationContext: Secure access to AST and execution context
 */
module io.github.cowwoc.styler.formatter
{
	requires transitive io.github.cowwoc.styler.ast.core;
	requires transitive io.github.cowwoc.styler.security;
	requires io.github.cowwoc.requirements12.java;
	requires io.github.classgraph;

	exports io.github.cowwoc.styler.formatter;
	exports io.github.cowwoc.styler.formatter.linelength;
	exports io.github.cowwoc.styler.formatter.importorg;
	exports io.github.cowwoc.styler.formatter.brace;
	exports io.github.cowwoc.styler.formatter.whitespace;
	exports io.github.cowwoc.styler.formatter.indentation;

	// Allow test module and pipeline module to access internal implementation classes
	exports io.github.cowwoc.styler.formatter.internal to io.github.cowwoc.styler.formatter.test,
		io.github.cowwoc.styler.pipeline, io.github.cowwoc.styler.pipeline.test;
	exports io.github.cowwoc.styler.formatter.linelength.internal to io.github.cowwoc.styler.formatter.test;
	exports io.github.cowwoc.styler.formatter.importorg.internal to io.github.cowwoc.styler.formatter.test;
	exports io.github.cowwoc.styler.formatter.brace.internal to io.github.cowwoc.styler.formatter.test;
	exports io.github.cowwoc.styler.formatter.whitespace.internal to io.github.cowwoc.styler.formatter.test;
	exports io.github.cowwoc.styler.formatter.indentation.internal to io.github.cowwoc.styler.formatter.test;
}
