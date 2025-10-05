package io.github.cowwoc.styler.parser.test.strategy;

import io.github.cowwoc.styler.parser.JavaVersion;
import io.github.cowwoc.styler.parser.ParseContext;
import io.github.cowwoc.styler.parser.ParseStrategy;
import io.github.cowwoc.styler.parser.ParseStrategyRegistry;
import io.github.cowwoc.styler.parser.ParsingPhase;
import io.github.cowwoc.styler.parser.TokenType;
import io.github.cowwoc.styler.parser.strategies.FlexibleConstructorBodiesStrategy;
import org.testng.annotations.Test;

import java.util.List;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;
import static io.github.cowwoc.styler.parser.test.strategy.StrategyTestConstants.*;

/**
 * Tests for {@link ParseStrategyRegistry}.
 * Validates strategy registration, lookup, and priority ordering.
 */
public final class ParseStrategyRegistryTest
{
	/**
	 * Verifies that ParseStrategyRegistry successfully registers a valid strategy.
	 */
	@Test
	public void registerStrategyWithValidStrategyAddsToRegistry()
	{
		ParseStrategyRegistry registry = new ParseStrategyRegistry();
		FlexibleConstructorBodiesStrategy strategy = new FlexibleConstructorBodiesStrategy();

		registry.registerStrategy(JAVA_25, strategy);

		List<ParseStrategy> strategies = registry.getStrategies(JAVA_25);
		requireThat(strategies.contains(strategy), "strategiesContainsRegistered").isTrue();
	}

	/**
	 * Verifies that ParseStrategyRegistry accumulates all registered strategies.
	 */
	@Test
	public void registerStrategyMultipleStrategiesAllAdded()
	{
		ParseStrategyRegistry registry = new ParseStrategyRegistry();
		TestParseStrategy strategy1 = new TestParseStrategy(10);
		TestParseStrategy strategy2 = new TestParseStrategy(5);

		registry.registerStrategy(JAVA_25, strategy1);
		registry.registerStrategy(JAVA_25, strategy2);

		List<ParseStrategy> strategies = registry.getStrategies(JAVA_25);
		requireThat(strategies.size(), "strategiesSize").isEqualTo(2);
	}

	/**
	 * Verifies that ParseStrategyRegistry maintains strategies in descending priority order.
	 */
	@Test
	public void registerStrategyMultiplePrioritiesSortedByPriorityDescending()
	{
		ParseStrategyRegistry registry = new ParseStrategyRegistry();
		TestParseStrategy lowPriority = new TestParseStrategy(5);
		TestParseStrategy highPriority = new TestParseStrategy(15);
		TestParseStrategy mediumPriority = new TestParseStrategy(10);

		registry.registerStrategy(JAVA_25, lowPriority);
		registry.registerStrategy(JAVA_25, highPriority);
		registry.registerStrategy(JAVA_25, mediumPriority);

		List<ParseStrategy> strategies = registry.getStrategies(JAVA_25);
		requireThat(strategies.get(0), "firstStrategy").isEqualTo(highPriority);
		requireThat(strategies.get(1), "secondStrategy").isEqualTo(mediumPriority);
		requireThat(strategies.get(2), "thirdStrategy").isEqualTo(lowPriority);
	}

	/**
	 * Verifies that ParseStrategyRegistry finds and returns a matching strategy.
	 */
	@Test
	public void findStrategyWithSingleMatchReturnsStrategy()
	{
		ParseStrategyRegistry registry = new ParseStrategyRegistry();
		TestParseStrategy strategy = new TestParseStrategy(10, true);
		registry.registerStrategy(JAVA_25, strategy);
		ParseContext context = ParseContextTestFactory.createMinimalContext(TokenType.LBRACE);

		ParseStrategy result = registry.findStrategy(JAVA_25, CONSTRUCTOR_BODY, context);

		requireThat(result, "result").isEqualTo(strategy);
	}

	/**
	 * Verifies that ParseStrategyRegistry returns null when no strategy can handle the request.
	 */
	@Test
	public void findStrategyWithNoMatchesReturnsNull()
	{
		ParseStrategyRegistry registry = new ParseStrategyRegistry();
		TestParseStrategy strategy = new TestParseStrategy(10, false); // canHandle returns false
		registry.registerStrategy(JAVA_25, strategy);
		ParseContext context = ParseContextTestFactory.createMinimalContext(TokenType.LBRACE);

		ParseStrategy result = registry.findStrategy(JAVA_25, CONSTRUCTOR_BODY, context);

		requireThat(result, "result").isNull();
	}

	/**
	 * Verifies that ParseStrategyRegistry selects highest priority strategy when multiple match.
	 */
	@Test
	public void findStrategyWithMultipleMatchesReturnsHighestPriority()
	{
		ParseStrategyRegistry registry = new ParseStrategyRegistry();
		TestParseStrategy lowPriority = new TestParseStrategy(5, true);
		TestParseStrategy highPriority = new TestParseStrategy(15, true);
		registry.registerStrategy(JAVA_25, lowPriority);
		registry.registerStrategy(JAVA_25, highPriority);
		ParseContext context = ParseContextTestFactory.createMinimalContext(TokenType.LBRACE);

		ParseStrategy result = registry.findStrategy(JAVA_25, CONSTRUCTOR_BODY, context);

		requireThat(result, "result").isEqualTo(highPriority);
	}

	/**
	 * Verifies that ParseStrategyRegistry returns empty list for unregistered Java version.
	 */
	@Test
	public void getStrategiesWithUnregisteredVersionReturnsEmptyList()
	{
		ParseStrategyRegistry registry = new ParseStrategyRegistry();

		List<ParseStrategy> strategies = registry.getStrategies(JAVA_25);

		requireThat(strategies.isEmpty(), "strategiesIsEmpty").isTrue();
	}

	/**
	 * Verifies that ParseStrategyRegistry retrieves registered strategies for a version.
	 */
	@Test
	public void getStrategiesAfterRegistrationReturnsStrategies()
	{
		ParseStrategyRegistry registry = new ParseStrategyRegistry();
		TestParseStrategy strategy = new TestParseStrategy(10);
		registry.registerStrategy(JAVA_25, strategy);

		List<ParseStrategy> strategies = registry.getStrategies(JAVA_25);

		requireThat(strategies.isEmpty(), "strategiesIsEmpty").isFalse();
		requireThat(strategies.size(), "strategiesSize").isEqualTo(1);
	}

	/**
	 * Verifies that ParseStrategyRegistry populates default strategies when requested.
	 */
	@Test
	public void registerDefaultStrategiesPopulatesRegistry()
	{
		ParseStrategyRegistry registry = new ParseStrategyRegistry();

		registry.registerDefaultStrategies();

		List<ParseStrategy> java25Strategies = registry.getStrategies(JAVA_25);
		requireThat(java25Strategies.isEmpty(), "java25StrategiesIsEmpty").isFalse();
	}

	/**
	 * Verifies that ParseStrategyRegistry falls back to earlier Java version strategies when needed.
	 */
	@Test
	public void findStrategyWithVersionFallbackFindsEarlierVersionStrategy()
	{
		ParseStrategyRegistry registry = new ParseStrategyRegistry();
		TestParseStrategy java21Strategy = new TestParseStrategy(10, true);
		registry.registerStrategy(JAVA_21, java21Strategy);
		ParseContext context = ParseContextTestFactory.createMinimalContext(TokenType.LBRACE);

		// Request Java 25 but only Java 21 strategy registered
		ParseStrategy result = registry.findStrategy(JAVA_25, CONSTRUCTOR_BODY, context);

		requireThat(result, "result").isEqualTo(java21Strategy);
	}

	/**
	 * Test implementation of ParseStrategy for registry testing.
	 * Thread-safe and parallel execution compatible.
	 */
	private static final class TestParseStrategy implements ParseStrategy
	{
		private final int priority;
		private final boolean canHandleResult;

		TestParseStrategy(int priority)
		{
			this(priority, false);
		}

		TestParseStrategy(int priority, boolean canHandleResult)
		{
			this.priority = priority;
			this.canHandleResult = canHandleResult;
		}

		@Override
		public boolean canHandle(JavaVersion version, ParsingPhase phase, ParseContext context)
		{
			return canHandleResult;
		}

		@Override
		public int parseConstruct(ParseContext context)
		{
			return -1;
		}

		@Override
		public int getPriority()
		{
			return priority;
		}

		@Override
		public String getDescription()
		{
			return "Test strategy (priority=" + priority + ")";
		}
	}
}
