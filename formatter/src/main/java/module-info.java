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

	exports io.github.cowwoc.styler.formatter;
}
