package io.github.cowwoc.styler.parser.strategies;

import io.github.cowwoc.styler.parser.JavaVersion;
import io.github.cowwoc.styler.parser.ParseContext;
import io.github.cowwoc.styler.parser.ParseStrategy;
import io.github.cowwoc.styler.parser.ParsingPhase;
import io.github.cowwoc.styler.parser.TokenType;

/**
 * Strategy for parsing string templates introduced in Java 21.
 *
 * <p>This is a simple token-based strategy - the phase parameter is not relevant
 * since string template syntax is distinct.
 */
public class StringTemplateStrategy implements ParseStrategy
{
	@Override
	public boolean canHandle(JavaVersion version, ParsingPhase phase, ParseContext context)
{
		// Phase not relevant - string template syntax is distinct
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
		return PRIORITY_KEYWORD_BASED;
	}

	@Override
	public String getDescription()
{
		return "String templates (Java 21+)";
	}
}