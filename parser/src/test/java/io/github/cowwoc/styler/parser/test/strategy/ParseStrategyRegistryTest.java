package io.github.cowwoc.styler.parser.test.strategy;

import io.github.cowwoc.styler.parser.*;
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
	@Test
	public void registerStrategy_withValidStrategy_addsToRegistry()
	{
		ParseStrategyRegistry registry = new ParseStrategyRegistry();
		FlexibleConstructorBodiesStrategy strategy = new FlexibleConstructorBodiesStrategy();

		registry.registerStrategy(JAVA_25, strategy);

		List<ParseStrategy> strategies = registry.getStrategies(JAVA_25);
		requireThat(strategies.contains(strategy), "strategiesContainsRegistered").isTrue();
	}

	@Test
	public void registerStrategy_multipleStrategies_allAdded()
	{
		ParseStrategyRegistry registry = new ParseStrategyRegistry();
		TestParseStrategy strategy1 = new TestParseStrategy(10);
		TestParseStrategy strategy2 = new TestParseStrategy(5);

		registry.registerStrategy(JAVA_25, strategy1);
		registry.registerStrategy(JAVA_25, strategy2);

		List<ParseStrategy> strategies = registry.getStrategies(JAVA_25);
		requireThat(strategies.size(), "strategiesSize").isEqualTo(2);
	}

	@Test
	public void registerStrategy_multiplePriorities_sortedByPriorityDescending()
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

	@Test
	public void findStrategy_withSingleMatch_returnsStrategy()
	{
		ParseStrategyRegistry registry = new ParseStrategyRegistry();
		TestParseStrategy strategy = new TestParseStrategy(10, true);
		registry.registerStrategy(JAVA_25, strategy);
		ParseContext context = ParseContextTestFactory.createMinimalContext(TokenType.LBRACE);

		ParseStrategy result = registry.findStrategy(JAVA_25, CONSTRUCTOR_BODY, context);

		requireThat(result, "result").isEqualTo(strategy);
	}

	@Test
	public void findStrategy_withNoMatches_returnsNull()
	{
		ParseStrategyRegistry registry = new ParseStrategyRegistry();
		TestParseStrategy strategy = new TestParseStrategy(10, false); // canHandle returns false
		registry.registerStrategy(JAVA_25, strategy);
		ParseContext context = ParseContextTestFactory.createMinimalContext(TokenType.LBRACE);

		ParseStrategy result = registry.findStrategy(JAVA_25, CONSTRUCTOR_BODY, context);

		requireThat(result, "result").isNull();
	}

	@Test
	public void findStrategy_withMultipleMatches_returnsHighestPriority()
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

	@Test
	public void getStrategies_withUnregisteredVersion_returnsEmptyList()
	{
		ParseStrategyRegistry registry = new ParseStrategyRegistry();

		List<ParseStrategy> strategies = registry.getStrategies(JAVA_25);

		requireThat(strategies.isEmpty(), "strategiesIsEmpty").isTrue();
	}

	@Test
	public void getStrategies_afterRegistration_returnsStrategies()
	{
		ParseStrategyRegistry registry = new ParseStrategyRegistry();
		TestParseStrategy strategy = new TestParseStrategy(10);
		registry.registerStrategy(JAVA_25, strategy);

		List<ParseStrategy> strategies = registry.getStrategies(JAVA_25);

		requireThat(strategies.isEmpty(), "strategiesIsEmpty").isFalse();
		requireThat(strategies.size(), "strategiesSize").isEqualTo(1);
	}

	@Test
	public void registerDefaultStrategies_populatesRegistry()
	{
		ParseStrategyRegistry registry = new ParseStrategyRegistry();

		registry.registerDefaultStrategies();

		List<ParseStrategy> java25Strategies = registry.getStrategies(JAVA_25);
		requireThat(java25Strategies.isEmpty(), "java25StrategiesIsEmpty").isFalse();
	}

	@Test
	public void findStrategy_withVersionFallback_findsEarlierVersionStrategy()
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
