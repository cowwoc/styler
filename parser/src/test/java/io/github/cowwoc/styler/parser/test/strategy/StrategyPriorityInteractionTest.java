package io.github.cowwoc.styler.parser.test.strategy;

import io.github.cowwoc.styler.parser.JavaVersion;
import io.github.cowwoc.styler.parser.ParseContext;
import io.github.cowwoc.styler.parser.ParseStrategy;
import io.github.cowwoc.styler.parser.ParseStrategyRegistry;
import io.github.cowwoc.styler.parser.ParsingPhase;
import io.github.cowwoc.styler.parser.TokenType;
import org.testng.annotations.Test;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;
import static io.github.cowwoc.styler.parser.test.strategy.StrategyTestConstants.*;

/**
 * Tests for strategy priority interactions and selection logic.
 * Validates how multiple strategies with different priorities compete and interact.
 */
public final class StrategyPriorityInteractionTest
{
	@Test
	public void findStrategy_twoStrategiesDifferentPriorities_selectsHigherPriority()
	{
		ParseStrategyRegistry registry = new ParseStrategyRegistry();
		TestStrategy lowPriority = new TestStrategy(5, true);
		TestStrategy highPriority = new TestStrategy(15, true);

		registry.registerStrategy(JAVA_25, lowPriority);
		registry.registerStrategy(JAVA_25, highPriority);

		ParseContext context = ParseContextTestFactory.createMinimalContext(TokenType.LBRACE);
		ParseStrategy selected = registry.findStrategy(JAVA_25, CONSTRUCTOR_BODY, context);

		requireThat(selected, "selectedStrategy").isEqualTo(highPriority);
	}

	@Test
	public void findStrategy_threeStrategiesDifferentPriorities_selectsHighest()
	{
		ParseStrategyRegistry registry = new ParseStrategyRegistry();
		TestStrategy low = new TestStrategy(5, true);
		TestStrategy medium = new TestStrategy(10, true);
		TestStrategy high = new TestStrategy(20, true);

		registry.registerStrategy(JAVA_25, low);
		registry.registerStrategy(JAVA_25, medium);
		registry.registerStrategy(JAVA_25, high);

		ParseContext context = ParseContextTestFactory.createMinimalContext(TokenType.LBRACE);
		ParseStrategy selected = registry.findStrategy(JAVA_25, CONSTRUCTOR_BODY, context);

		requireThat(selected, "selectedStrategy").isEqualTo(high);
	}

	@Test
	public void findStrategy_highPriorityCannotHandle_selectsLowerPriorityThatCan()
	{
		ParseStrategyRegistry registry = new ParseStrategyRegistry();
		TestStrategy highButCannot = new TestStrategy(20, false);
		TestStrategy lowButCan = new TestStrategy(10, true);

		registry.registerStrategy(JAVA_25, highButCannot);
		registry.registerStrategy(JAVA_25, lowButCan);

		ParseContext context = ParseContextTestFactory.createMinimalContext(TokenType.LBRACE);
		ParseStrategy selected = registry.findStrategy(JAVA_25, CONSTRUCTOR_BODY, context);

		requireThat(selected, "selectedStrategy").isEqualTo(lowButCan);
	}

	@Test
	public void findStrategy_equalPriority_selectsFirstRegistered()
	{
		ParseStrategyRegistry registry = new ParseStrategyRegistry();
		TestStrategy first = new TestStrategy(10, true);
		TestStrategy second = new TestStrategy(10, true);

		registry.registerStrategy(JAVA_25, first);
		registry.registerStrategy(JAVA_25, second);

		ParseContext context = ParseContextTestFactory.createMinimalContext(TokenType.LBRACE);
		ParseStrategy selected = registry.findStrategy(JAVA_25, CONSTRUCTOR_BODY, context);

		requireThat(selected, "selectedStrategy").isEqualTo(first);
	}

	@Test
	public void findStrategy_phaseAwareOverridesKeywordBased_whenHigherPriority()
	{
		ParseStrategyRegistry registry = new ParseStrategyRegistry();
		// Phase-aware priority is 15 (PRIORITY_PHASE_AWARE)
		TestStrategy keywordBased = new TestStrategy(10, true);
		TestStrategy phaseAware = new TestStrategy(ParseStrategy.PRIORITY_PHASE_AWARE, true);

		registry.registerStrategy(JAVA_25, keywordBased);
		registry.registerStrategy(JAVA_25, phaseAware);

		ParseContext context = ParseContextTestFactory.createMinimalContext(TokenType.LBRACE);
		ParseStrategy selected = registry.findStrategy(JAVA_25, CONSTRUCTOR_BODY, context);

		requireThat(selected, "selectedStrategy").isEqualTo(phaseAware);
	}

	@Test
	public void findStrategy_keywordBasedSelectedWhen_phaseAwareCannotHandle()
	{
		ParseStrategyRegistry registry = new ParseStrategyRegistry();
		TestStrategy keywordBased = new TestStrategy(10, true);
		TestStrategy phaseAwareCannotHandle = new TestStrategy(ParseStrategy.PRIORITY_PHASE_AWARE, false);

		registry.registerStrategy(JAVA_25, keywordBased);
		registry.registerStrategy(JAVA_25, phaseAwareCannotHandle);

		ParseContext context = ParseContextTestFactory.createMinimalContext(TokenType.LBRACE);
		ParseStrategy selected = registry.findStrategy(JAVA_25, CONSTRUCTOR_BODY, context);

		requireThat(selected, "selectedStrategy").isEqualTo(keywordBased);
	}

	@Test
	public void findStrategy_multiplePhaseAwareStrategies_selectsHighestPriority()
	{
		ParseStrategyRegistry registry = new ParseStrategyRegistry();
		TestStrategy phaseAware1 = new TestStrategy(ParseStrategy.PRIORITY_PHASE_AWARE, true);
		TestStrategy phaseAware2 = new TestStrategy(ParseStrategy.PRIORITY_PHASE_AWARE + 5, true);

		registry.registerStrategy(JAVA_25, phaseAware1);
		registry.registerStrategy(JAVA_25, phaseAware2);

		ParseContext context = ParseContextTestFactory.createMinimalContext(TokenType.LBRACE);
		ParseStrategy selected = registry.findStrategy(JAVA_25, CONSTRUCTOR_BODY, context);

		requireThat(selected, "selectedStrategy").isEqualTo(phaseAware2);
	}

	@Test
	public void findStrategy_priorityOverridesRegistrationOrder()
	{
		ParseStrategyRegistry registry = new ParseStrategyRegistry();
		TestStrategy registeredFirst = new TestStrategy(5, true);
		TestStrategy registeredLast = new TestStrategy(20, true);

		registry.registerStrategy(JAVA_25, registeredFirst);
		registry.registerStrategy(JAVA_25, registeredLast);

		ParseContext context = ParseContextTestFactory.createMinimalContext(TokenType.LBRACE);
		ParseStrategy selected = registry.findStrategy(JAVA_25, CONSTRUCTOR_BODY, context);

		requireThat(selected, "selectedStrategy").isEqualTo(registeredLast);
	}

	@Test
	public void findStrategy_allStrategiesCannotHandle_returnsNull()
	{
		ParseStrategyRegistry registry = new ParseStrategyRegistry();
		TestStrategy strategy1 = new TestStrategy(10, false);
		TestStrategy strategy2 = new TestStrategy(20, false);

		registry.registerStrategy(JAVA_25, strategy1);
		registry.registerStrategy(JAVA_25, strategy2);

		ParseContext context = ParseContextTestFactory.createMinimalContext(TokenType.LBRACE);
		ParseStrategy selected = registry.findStrategy(JAVA_25, CONSTRUCTOR_BODY, context);

		requireThat(selected, "selectedStrategy").isNull();
	}

	@Test
	public void findStrategy_versionFallback_usesHighestPriorityFromEarlierVersion()
	{
		ParseStrategyRegistry registry = new ParseStrategyRegistry();
		TestStrategy java21Low = new TestStrategy(5, true);
		TestStrategy java21High = new TestStrategy(15, true);

		registry.registerStrategy(JAVA_21, java21Low);
		registry.registerStrategy(JAVA_21, java21High);

		ParseContext context = ParseContextTestFactory.createMinimalContext(TokenType.LBRACE);
		// Request Java 25 but only Java 21 strategies exist
		ParseStrategy selected = registry.findStrategy(JAVA_25, CONSTRUCTOR_BODY, context);

		requireThat(selected, "selectedStrategy").isEqualTo(java21High);
	}

	@Test
	public void findStrategy_complexScenario_multipleVersionsAndPriorities()
	{
		ParseStrategyRegistry registry = new ParseStrategyRegistry();
		TestStrategy java21Medium = new TestStrategy(10, true);
		TestStrategy java24High = new TestStrategy(20, true);
		TestStrategy java25Low = new TestStrategy(5, true);

		registry.registerStrategy(JAVA_21, java21Medium);
		registry.registerStrategy(JAVA_24, java24High);
		registry.registerStrategy(JAVA_25, java25Low);

		ParseContext context = ParseContextTestFactory.createMinimalContext(TokenType.LBRACE);
		ParseStrategy selected = registry.findStrategy(JAVA_25, CONSTRUCTOR_BODY, context);

		// Should find java25Low (priority 5) since it's registered for JAVA_25
		// even though java24High has higher priority, it's for a different version
		requireThat(selected, "selectedStrategy").isEqualTo(java25Low);
	}

	@Test
	public void findStrategy_phaseSpecificStrategy_onlyHandlesTargetPhase()
	{
		ParseStrategyRegistry registry = new ParseStrategyRegistry();
		PhaseSpecificStrategy constructorOnly = new PhaseSpecificStrategy(CONSTRUCTOR_BODY, 15);

		registry.registerStrategy(JAVA_25, constructorOnly);

		ParseContext context = ParseContextTestFactory.createMinimalContext(TokenType.LBRACE);

		// Should handle CONSTRUCTOR_BODY
		ParseStrategy constructorResult = registry.findStrategy(JAVA_25, CONSTRUCTOR_BODY, context);
		requireThat(constructorResult, "constructorResult").isEqualTo(constructorOnly);

		// Should NOT handle METHOD_BODY
		ParseStrategy methodResult = registry.findStrategy(JAVA_25, METHOD_BODY, context);
		requireThat(methodResult, "methodResult").isNull();
	}

	/**
	 * Test strategy implementation for priority interaction tests.
	 */
	private static final class TestStrategy implements ParseStrategy
	{
		private final int priority;
		private final boolean canHandleResult;

		TestStrategy(int priority, boolean canHandleResult)
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

	/**
	 * Phase-specific test strategy that only handles a specific parsing phase.
	 */
	private static final class PhaseSpecificStrategy implements ParseStrategy
	{
		private final ParsingPhase targetPhase;
		private final int priority;

		PhaseSpecificStrategy(ParsingPhase targetPhase, int priority)
		{
			this.targetPhase = targetPhase;
			this.priority = priority;
		}

		@Override
		public boolean canHandle(JavaVersion version, ParsingPhase phase, ParseContext context)
		{
			return phase == targetPhase;
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
			return "Phase-specific strategy (phase=" + targetPhase + ", priority=" + priority + ")";
		}
	}
}
