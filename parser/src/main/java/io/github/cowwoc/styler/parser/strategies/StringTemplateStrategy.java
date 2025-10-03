package io.github.cowwoc.styler.parser.strategies;

import io.github.cowwoc.styler.parser.JavaVersion;
import io.github.cowwoc.styler.parser.ParseContext;
import io.github.cowwoc.styler.parser.ParseStrategy;
import io.github.cowwoc.styler.parser.TokenType;

/**
 * Strategy for parsing string templates introduced in Java 21.
 */
public class StringTemplateStrategy implements ParseStrategy
{
	@Override
	public boolean canHandle(JavaVersion version, ParseContext context)
{
		return version.isAtLeast(JavaVersion.JAVA_21) &&
			   context.currentTokenIs(TokenType.STRING_TEMPLATE_START);
	}

	@Override
	public int parseConstruct(ParseContext context)
{
		throw new UnsupportedOperationException(
			"String template parsing for Java 21+ is not yet implemented. " +
			"Current parser supports basic Java constructs.");
	}

	@Override
	public int getPriority()
{
		return 10;
	}

	@Override
	public String getDescription()
{
		return "String templates (Java 21+)";
	}
}