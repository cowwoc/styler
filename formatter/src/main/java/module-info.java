module io.github.cowwoc.styler.formatter.api
{
	requires transitive io.github.cowwoc.styler.ast;
	requires java.logging;
	requires com.fasterxml.jackson.databind;
	requires org.slf4j;

	exports io.github.cowwoc.styler.formatter.api;
}