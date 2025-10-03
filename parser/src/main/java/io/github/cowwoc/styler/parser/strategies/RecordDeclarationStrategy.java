package io.github.cowwoc.styler.parser.strategies;

import io.github.cowwoc.styler.parser.JavaVersion;
import io.github.cowwoc.styler.parser.ParseContext;
import io.github.cowwoc.styler.parser.ParseStrategy;
import io.github.cowwoc.styler.parser.ParsingPhase;
import io.github.cowwoc.styler.parser.TokenType;

/**
 * Strategy for parsing record declarations introduced in Java 16.
 *
 * <p>This is a simple token-based strategy - the phase parameter is not relevant
 * since the {@code record} keyword is unambiguous.
 */
public class RecordDeclarationStrategy implements ParseStrategy
{
	@Override
	public boolean canHandle(JavaVersion version, ParsingPhase phase, ParseContext context)
{
		// Phase not relevant for keyword-triggered features
		return version.isAtLeast(JavaVersion.JAVA_16) &&
			   context.currentTokenIs(TokenType.RECORD);
	}

	@Override
	public int parseConstruct(ParseContext context)
{
		throw new UnsupportedOperationException(
			"Record declaration parsing for Java 16+ is not yet implemented. " +
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
		return "Record declarations (Java 16+)";
	}
}