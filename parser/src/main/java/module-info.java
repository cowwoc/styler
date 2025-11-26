/**
 * Styler Parser module - Java source code parser with Index-Overlay AST.
 */
module io.github.cowwoc.styler.parser
{
	requires transitive io.github.cowwoc.styler.ast.core;
	requires io.github.cowwoc.requirements12.java;

	exports io.github.cowwoc.styler.parser;
}
