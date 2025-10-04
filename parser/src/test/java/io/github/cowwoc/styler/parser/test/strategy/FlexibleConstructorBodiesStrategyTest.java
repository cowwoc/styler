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
	/**
	 * Verifies that FlexibleConstructorBodiesStrategy handles constructor bodies in Java 25 with LBRACE token.
	 */
	@Test
	public void canHandleWithConstructorPhaseAndJava25AndLbraceReturnsTrue()
	{
		FlexibleConstructorBodiesStrategy strategy = new FlexibleConstructorBodiesStrategy();
		ParseContext context = ParseContextTestFactory.createMinimalContext(TokenType.LBRACE);

		boolean result = strategy.canHandle(JAVA_25, CONSTRUCTOR_BODY, context);

		requireThat(result, "canHandle").isTrue();
	}

	/**
	 * Verifies that FlexibleConstructorBodiesStrategy does not handle method body parsing phase.
	 */
	@Test
	public void canHandleWithMethodBodyPhaseReturnsFalse()
	{
		FlexibleConstructorBodiesStrategy strategy = new FlexibleConstructorBodiesStrategy();
		ParseContext context = ParseContextTestFactory.createMinimalContext(TokenType.LBRACE);

		boolean result = strategy.canHandle(JAVA_25, METHOD_BODY, context);

		requireThat(result, "canHandle").isFalse();
	}

	/**
	 * Verifies that FlexibleConstructorBodiesStrategy does not handle initializer block parsing phase.
	 */
	@Test
	public void canHandleWithInitializerBlockPhaseReturnsFalse()
	{
		FlexibleConstructorBodiesStrategy strategy = new FlexibleConstructorBodiesStrategy();
		ParseContext context = ParseContextTestFactory.createMinimalContext(TokenType.LBRACE);

		boolean result = strategy.canHandle(JAVA_25, INITIALIZER_BLOCK, context);

		requireThat(result, "canHandle").isFalse();
	}

	/**
	 * Verifies that FlexibleConstructorBodiesStrategy does not handle class body parsing phase.
	 */
	@Test
	public void canHandleWithClassBodyPhaseReturnsFalse()
	{
		FlexibleConstructorBodiesStrategy strategy = new FlexibleConstructorBodiesStrategy();
		ParseContext context = ParseContextTestFactory.createMinimalContext(TokenType.LBRACE);

		boolean result = strategy.canHandle(JAVA_25, CLASS_BODY, context);

		requireThat(result, "canHandle").isFalse();
	}

	/**
	 * Verifies that FlexibleConstructorBodiesStrategy does not handle top-level parsing phase.
	 */
	@Test
	public void canHandleWithTopLevelPhaseReturnsFalse()
	{
		FlexibleConstructorBodiesStrategy strategy = new FlexibleConstructorBodiesStrategy();
		ParseContext context = ParseContextTestFactory.createMinimalContext(TokenType.LBRACE);

		boolean result = strategy.canHandle(JAVA_25, TOP_LEVEL, context);

		requireThat(result, "canHandle").isFalse();
	}

	/**
	 * Verifies that FlexibleConstructorBodiesStrategy does not handle Java 24 source version.
	 */
	@Test
	public void canHandleWithJava24ReturnsFalse()
	{
		FlexibleConstructorBodiesStrategy strategy = new FlexibleConstructorBodiesStrategy();
		ParseContext context = ParseContextTestFactory.createMinimalContext(TokenType.LBRACE);

		boolean result = strategy.canHandle(JAVA_24, CONSTRUCTOR_BODY, context);

		requireThat(result, "canHandle").isFalse();
	}

	/**
	 * Verifies that FlexibleConstructorBodiesStrategy does not handle Java 21 source version.
	 */
	@Test
	public void canHandleWithJava21ReturnsFalse()
	{
		FlexibleConstructorBodiesStrategy strategy = new FlexibleConstructorBodiesStrategy();
		ParseContext context = ParseContextTestFactory.createMinimalContext(TokenType.LBRACE);

		boolean result = strategy.canHandle(JAVA_21, CONSTRUCTOR_BODY, context);

		requireThat(result, "canHandle").isFalse();
	}

	/**
	 * Verifies that FlexibleConstructorBodiesStrategy does not handle RBRACE token type.
	 */
	@Test
	public void canHandleWithRbraceTokenReturnsFalse()
	{
		FlexibleConstructorBodiesStrategy strategy = new FlexibleConstructorBodiesStrategy();
		ParseContext context = ParseContextTestFactory.createMinimalContext(TokenType.RBRACE);

		boolean result = strategy.canHandle(JAVA_25, CONSTRUCTOR_BODY, context);

		requireThat(result, "canHandle").isFalse();
	}

	/**
	 * Verifies that FlexibleConstructorBodiesStrategy does not handle SEMICOLON token type.
	 */
	@Test
	public void canHandleWithSemicolonTokenReturnsFalse()
	{
		FlexibleConstructorBodiesStrategy strategy = new FlexibleConstructorBodiesStrategy();
		ParseContext context = ParseContextTestFactory.createMinimalContext(TokenType.SEMICOLON);

		boolean result = strategy.canHandle(JAVA_25, CONSTRUCTOR_BODY, context);

		requireThat(result, "canHandle").isFalse();
	}

	/**
	 * Verifies that FlexibleConstructorBodiesStrategy does not handle IDENTIFIER token type.
	 */
	@Test
	public void canHandleWithIdentifierTokenReturnsFalse()
	{
		FlexibleConstructorBodiesStrategy strategy = new FlexibleConstructorBodiesStrategy();
		ParseContext context = ParseContextTestFactory.createMinimalContext(TokenType.IDENTIFIER);

		boolean result = strategy.canHandle(JAVA_25, CONSTRUCTOR_BODY, context);

		requireThat(result, "canHandle").isFalse();
	}

	/**
	 * Verifies that FlexibleConstructorBodiesStrategy returns phase-aware priority level.
	 */
	@Test
	public void getPriorityReturnsPhaseAwarePriority()
	{
		FlexibleConstructorBodiesStrategy strategy = new FlexibleConstructorBodiesStrategy();

		int priority = strategy.getPriority();

		requireThat(priority, "priority").isEqualTo(ParseStrategy.PRIORITY_PHASE_AWARE);
	}

	/**
	 * Verifies that FlexibleConstructorBodiesStrategy priority value is 15.
	 */
	@Test
	public void getPriorityReturns15()
	{
		FlexibleConstructorBodiesStrategy strategy = new FlexibleConstructorBodiesStrategy();

		int priority = strategy.getPriority();

		requireThat(priority, "priority").isEqualTo(15);
	}

	/**
	 * Verifies that FlexibleConstructorBodiesStrategy returns a non-null description.
	 */
	@Test
	public void getDescriptionReturnsNonNull()
	{
		FlexibleConstructorBodiesStrategy strategy = new FlexibleConstructorBodiesStrategy();

		String description = strategy.getDescription();

		requireThat(description, "description").isNotNull();
	}

	/**
	 * Verifies that FlexibleConstructorBodiesStrategy description contains Java 25 reference.
	 */
	@Test
	public void getDescriptionContainsJava25Reference()
	{
		FlexibleConstructorBodiesStrategy strategy = new FlexibleConstructorBodiesStrategy();

		String description = strategy.getDescription();

		requireThat(description.contains("25"), "descriptionContainsJava25").isTrue();
	}

	/**
	 * Verifies that FlexibleConstructorBodiesStrategy description contains JEP reference.
	 */
	@Test
	public void getDescriptionContainsJepReference()
	{
		FlexibleConstructorBodiesStrategy strategy = new FlexibleConstructorBodiesStrategy();

		String description = strategy.getDescription();

		requireThat(description.contains("JEP"), "descriptionContainsJEP").isTrue();
	}

	/**
	 * Verifies that FlexibleConstructorBodiesStrategy rejects all parsing phases except constructor body.
	 */
	@Test
	public void canHandleAllPhasesExceptConstructorBodyReturnsFalse()
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

	/**
	 * Verifies that FlexibleConstructorBodiesStrategy rejects all Java versions before Java 25.
	 */
	@Test
	public void canHandleAllVersionsBelow25ReturnsFalse()
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

	/**
	 * Verifies that FlexibleConstructorBodiesStrategy requires all three conditions to be met simultaneously.
	 */
	@Test
	public void canHandleAllConditionsMustBeTrue()
	{
		FlexibleConstructorBodiesStrategy strategy = new FlexibleConstructorBodiesStrategy();

		// All three conditions met = true
		ParseContext validContext = ParseContextTestFactory.createMinimalContext(TokenType.LBRACE);
		requireThat(strategy.canHandle(JAVA_25, CONSTRUCTOR_BODY, validContext), "allConditionsMet").
			isTrue();

		// Wrong version
		requireThat(strategy.canHandle(JAVA_24, CONSTRUCTOR_BODY, validContext), "wrongVersion").
			isFalse();

		// Wrong phase
		requireThat(strategy.canHandle(JAVA_25, METHOD_BODY, validContext), "wrongPhase").
			isFalse();

		// Wrong token
		ParseContext wrongToken = ParseContextTestFactory.createMinimalContext(TokenType.SEMICOLON);
		requireThat(strategy.canHandle(JAVA_25, CONSTRUCTOR_BODY, wrongToken), "wrongToken").
			isFalse();
	}
}
