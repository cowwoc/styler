package io.github.cowwoc.styler.parser.strategies;

import io.github.cowwoc.styler.parser.JavaVersion;
import io.github.cowwoc.styler.parser.ParseContext;
import io.github.cowwoc.styler.parser.ParseStrategy;
import io.github.cowwoc.styler.parser.ParsingPhase;
import io.github.cowwoc.styler.parser.TokenType;

/**
 * Strategy for parsing sealed class declarations introduced in Java 17.
 *
 * <p>This is a simple token-based strategy - the phase parameter is not relevant
 * since {@code sealed} and {@code non-sealed} are keywords.
 */
public class SealedClassStrategy implements ParseStrategy
{
	@Override
	public boolean canHandle(JavaVersion version, ParsingPhase phase, ParseContext context)
{
		// Phase not relevant - sealed/non-sealed are keywords
		return version.isAtLeast(JavaVersion.JAVA_17) &&
			   context.currentTokenIs(TokenType.SEALED);
	}

	@Override
	public int parseConstruct(ParseContext context)
{
		throw new UnsupportedOperationException(
			"Sealed class parsing for Java 17+ is not yet implemented. " +
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
		return "Sealed classes (Java 17+)";
	}
}