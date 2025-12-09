module io.github.cowwoc.styler.discovery
{
	requires transitive io.github.cowwoc.styler.security;
	requires io.github.cowwoc.requirements12.java;

	exports io.github.cowwoc.styler.discovery;
	exports io.github.cowwoc.styler.discovery.internal to io.github.cowwoc.styler.discovery.test;
}
