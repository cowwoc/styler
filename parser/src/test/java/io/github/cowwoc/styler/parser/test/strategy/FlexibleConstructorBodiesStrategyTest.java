package io.github.cowwoc.styler.parser.test.strategy;

import io.github.cowwoc.styler.parser.JavaVersion;
import io.github.cowwoc.styler.parser.ParseContext;
import io.github.cowwoc.styler.parser.ParseStrategy;
import io.github.cowwoc.styler.parser.ParsingPhase;
import io.github.cowwoc.styler.parser.TokenType;
import io.github.cowwoc.styler.parser.strategies.FlexibleConstructorBodiesStrategy;
import org.testng.annotations.Test;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;
import static io.github.cowwoc.styler.parser.test.strategy.StrategyTestConstants.*;

/**
 * Tests for {@link FlexibleConstructorBodiesStrategy}.
 * Validates phase-aware strategy behavior for JDK 25 flexible constructor bodies (JEP 513).
 */
public final class FlexibleConstructorBodiesStrategyTest
{
	@Test
	public void canHandle_withConstructorPhaseAndJava25AndLbrace_returnsTrue()
	{
		FlexibleConstructorBodiesStrategy strategy = new FlexibleConstructorBodiesStrategy();
		ParseContext context = ParseContextTestFactory.createMinimalContext(TokenType.LBRACE);

		boolean result = strategy.canHandle(JAVA_25, CONSTRUCTOR_BODY, context);

		requireThat(result, "canHandle").isTrue();
	}

	@Test
	public void canHandle_withMethodBodyPhase_returnsFalse()
	{
		FlexibleConstructorBodiesStrategy strategy = new FlexibleConstructorBodiesStrategy();
		ParseContext context = ParseContextTestFactory.createMinimalContext(TokenType.LBRACE);

		boolean result = strategy.canHandle(JAVA_25, METHOD_BODY, context);

		requireThat(result, "canHandle").isFalse();
	}

	@Test
	public void canHandle_withInitializerBlockPhase_returnsFalse()
	{
		FlexibleConstructorBodiesStrategy strategy = new FlexibleConstructorBodiesStrategy();
		ParseContext context = ParseContextTestFactory.createMinimalContext(TokenType.LBRACE);

		boolean result = strategy.canHandle(JAVA_25, INITIALIZER_BLOCK, context);

		requireThat(result, "canHandle").isFalse();
	}

	@Test
	public void canHandle_withClassBodyPhase_returnsFalse()
	{
		FlexibleConstructorBodiesStrategy strategy = new FlexibleConstructorBodiesStrategy();
		ParseContext context = ParseContextTestFactory.createMinimalContext(TokenType.LBRACE);

		boolean result = strategy.canHandle(JAVA_25, CLASS_BODY, context);

		requireThat(result, "canHandle").isFalse();
	}

	@Test
	public void canHandle_withTopLevelPhase_returnsFalse()
	{
		FlexibleConstructorBodiesStrategy strategy = new FlexibleConstructorBodiesStrategy();
		ParseContext context = ParseContextTestFactory.createMinimalContext(TokenType.LBRACE);

		boolean result = strategy.canHandle(JAVA_25, TOP_LEVEL, context);

		requireThat(result, "canHandle").isFalse();
	}

	@Test
	public void canHandle_withJava24_returnsFalse()
	{
		FlexibleConstructorBodiesStrategy strategy = new FlexibleConstructorBodiesStrategy();
		ParseContext context = ParseContextTestFactory.createMinimalContext(TokenType.LBRACE);

		boolean result = strategy.canHandle(JAVA_24, CONSTRUCTOR_BODY, context);

		requireThat(result, "canHandle").isFalse();
	}

	@Test
	public void canHandle_withJava21_returnsFalse()
	{
		FlexibleConstructorBodiesStrategy strategy = new FlexibleConstructorBodiesStrategy();
		ParseContext context = ParseContextTestFactory.createMinimalContext(TokenType.LBRACE);

		boolean result = strategy.canHandle(JAVA_21, CONSTRUCTOR_BODY, context);

		requireThat(result, "canHandle").isFalse();
	}

	@Test
	public void canHandle_withRbraceToken_returnsFalse()
	{
		FlexibleConstructorBodiesStrategy strategy = new FlexibleConstructorBodiesStrategy();
		ParseContext context = ParseContextTestFactory.createMinimalContext(TokenType.RBRACE);

		boolean result = strategy.canHandle(JAVA_25, CONSTRUCTOR_BODY, context);

		requireThat(result, "canHandle").isFalse();
	}

	@Test
	public void canHandle_withSemicolonToken_returnsFalse()
	{
		FlexibleConstructorBodiesStrategy strategy = new FlexibleConstructorBodiesStrategy();
		ParseContext context = ParseContextTestFactory.createMinimalContext(TokenType.SEMICOLON);

		boolean result = strategy.canHandle(JAVA_25, CONSTRUCTOR_BODY, context);

		requireThat(result, "canHandle").isFalse();
	}

	@Test
	public void canHandle_withIdentifierToken_returnsFalse()
	{
		FlexibleConstructorBodiesStrategy strategy = new FlexibleConstructorBodiesStrategy();
		ParseContext context = ParseContextTestFactory.createMinimalContext(TokenType.IDENTIFIER);

		boolean result = strategy.canHandle(JAVA_25, CONSTRUCTOR_BODY, context);

		requireThat(result, "canHandle").isFalse();
	}

	@Test
	public void getPriority_returnsPhaseAwarePriority()
	{
		FlexibleConstructorBodiesStrategy strategy = new FlexibleConstructorBodiesStrategy();

		int priority = strategy.getPriority();

		requireThat(priority, "priority").isEqualTo(ParseStrategy.PRIORITY_PHASE_AWARE);
	}

	@Test
	public void getPriority_returns15()
	{
		FlexibleConstructorBodiesStrategy strategy = new FlexibleConstructorBodiesStrategy();

		int priority = strategy.getPriority();

		requireThat(priority, "priority").isEqualTo(15);
	}

	@Test
	public void getDescription_returnsNonNull()
	{
		FlexibleConstructorBodiesStrategy strategy = new FlexibleConstructorBodiesStrategy();

		String description = strategy.getDescription();

		requireThat(description, "description").isNotNull();
	}

	@Test
	public void getDescription_containsJava25Reference()
	{
		FlexibleConstructorBodiesStrategy strategy = new FlexibleConstructorBodiesStrategy();

		String description = strategy.getDescription();

		requireThat(description.contains("25"), "descriptionContainsJava25").isTrue();
	}

	@Test
	public void getDescription_containsJepReference()
	{
		FlexibleConstructorBodiesStrategy strategy = new FlexibleConstructorBodiesStrategy();

		String description = strategy.getDescription();

		requireThat(description.contains("JEP"), "descriptionContainsJEP").isTrue();
	}

	@Test
	public void canHandle_allPhasesExceptConstructorBody_returnsFalse()
	{
		FlexibleConstructorBodiesStrategy strategy = new FlexibleConstructorBodiesStrategy();
		ParseContext context = ParseContextTestFactory.createMinimalContext(TokenType.LBRACE);

		for (ParsingPhase phase : ParsingPhase.values())
		{
			if (phase != CONSTRUCTOR_BODY)
			{
				boolean result = strategy.canHandle(JAVA_25, phase, context);
				requireThat(result, "canHandle_" + phase.name()).isFalse();
			}
		}
	}

	@Test
	public void canHandle_allVersionsBelow25_returnsFalse()
	{
		FlexibleConstructorBodiesStrategy strategy = new FlexibleConstructorBodiesStrategy();
		ParseContext context = ParseContextTestFactory.createMinimalContext(TokenType.LBRACE);

		JavaVersion[] olderVersions = {JAVA_14, JAVA_21, JAVA_24};
		for (JavaVersion version : olderVersions)
		{
			boolean result = strategy.canHandle(version, CONSTRUCTOR_BODY, context);
			requireThat(result, "canHandle_" + version.name()).isFalse();
		}
	}

	@Test
	public void canHandle_allConditionsMustBeTrue()
	{
		FlexibleConstructorBodiesStrategy strategy = new FlexibleConstructorBodiesStrategy();

		// All three conditions met = true
		ParseContext validContext = ParseContextTestFactory.createMinimalContext(TokenType.LBRACE);
		requireThat(strategy.canHandle(JAVA_25, CONSTRUCTOR_BODY, validContext), "allConditionsMet")
			.isTrue();

		// Wrong version
		requireThat(strategy.canHandle(JAVA_24, CONSTRUCTOR_BODY, validContext), "wrongVersion")
			.isFalse();

		// Wrong phase
		requireThat(strategy.canHandle(JAVA_25, METHOD_BODY, validContext), "wrongPhase")
			.isFalse();

		// Wrong token
		ParseContext wrongToken = ParseContextTestFactory.createMinimalContext(TokenType.SEMICOLON);
		requireThat(strategy.canHandle(JAVA_25, CONSTRUCTOR_BODY, wrongToken), "wrongToken")
			.isFalse();
	}
}
