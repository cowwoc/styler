module io.github.cowwoc.styler.parser
{
	requires io.github.cowwoc.requirements12.java;
	requires transitive io.github.cowwoc.styler.ast.core;

	exports io.github.cowwoc.styler.parser;
	exports io.github.cowwoc.styler.parser.strategies to io.github.cowwoc.styler.parser.test;
}