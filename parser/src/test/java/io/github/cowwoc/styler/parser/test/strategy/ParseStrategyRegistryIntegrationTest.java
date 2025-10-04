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
 * Integration tests for ParseStrategyRegistry lifecycle and full system behavior.
 * Validates registry creation, strategy management, version handling, and default strategy loading.
 */
public final class ParseStrategyRegistryIntegrationTest
{
	/**
	 * Verifies that ParseStrategyRegistry supports complete create-register-find workflow.
	 */
	@Test
	public void registryLifecycleCreateRegisterFindWorksEndToEnd()
	{
		// Create registry
		ParseStrategyRegistry registry = new ParseStrategyRegistry();

		// Register strategy
		FlexibleConstructorBodiesStrategy strategy = new FlexibleConstructorBodiesStrategy();
		registry.registerStrategy(JAVA_25, strategy);

		// Find strategy
		ParseContext context = ParseContextTestFactory.createMinimalContext(TokenType.LBRACE);
		ParseStrategy found = registry.findStrategy(JAVA_25, CONSTRUCTOR_BODY, context);

		// Validate
		requireThat(found, "foundStrategy").isEqualTo(strategy);
	}

	/**
	 * Verifies that default strategy registration includes FlexibleConstructorBodiesStrategy for Java 25.
	 */
	@Test
	public void defaultStrategiesAfterRegistrationContainsFlexibleConstructor()
	{
		ParseStrategyRegistry registry = new ParseStrategyRegistry();
		registry.registerDefaultStrategies();

		List<ParseStrategy> java25Strategies = registry.getStrategies(JAVA_25);

		boolean hasFlexibleConstructor = java25Strategies.stream().
			anyMatch(s -> s.getDescription().contains("JEP"));

		requireThat(hasFlexibleConstructor, "hasFlexibleConstructorStrategy").isTrue();
	}

	/**
	 * Verifies that ParseStrategyRegistry isolates strategies by Java version independently.
	 */
	@Test
	public void multiVersionRegistryDifferentStrategiesPerVersionIsolatesCorrectly()
	{
		ParseStrategyRegistry registry = new ParseStrategyRegistry();
		VersionSpecificStrategy java21Strategy = new VersionSpecificStrategy(JAVA_21, 10);
		VersionSpecificStrategy java25Strategy = new VersionSpecificStrategy(JAVA_25, 10);

		registry.registerStrategy(JAVA_21, java21Strategy);
		registry.registerStrategy(JAVA_25, java25Strategy);

		ParseContext context = ParseContextTestFactory.createMinimalContext(TokenType.LBRACE);

		ParseStrategy found21 = registry.findStrategy(JAVA_21, CONSTRUCTOR_BODY, context);
		ParseStrategy found25 = registry.findStrategy(JAVA_25, CONSTRUCTOR_BODY, context);

		requireThat(found21, "java21Strategy").isEqualTo(java21Strategy);
		requireThat(found25, "java25Strategy").isEqualTo(java25Strategy);
	}

	/**
	 * Verifies that ParseStrategyRegistry returns empty list when no strategies are registered.
	 */
	@Test
	public void getStrategiesEmptyRegistryReturnsEmptyList()
	{
		ParseStrategyRegistry registry = new ParseStrategyRegistry();

		List<ParseStrategy> strategies = registry.getStrategies(JAVA_25);

		requireThat(strategies.isEmpty(), "strategiesIsEmpty").isTrue();
	}

	/**
	 * Verifies that ParseStrategyRegistry returns all registered strategies for a version.
	 */
	@Test
	public void getStrategiesAfterMultipleRegistrationsReturnsAll()
	{
		ParseStrategyRegistry registry = new ParseStrategyRegistry();
		SimpleStrategy strategy1 = new SimpleStrategy(10);
		SimpleStrategy strategy2 = new SimpleStrategy(20);
		SimpleStrategy strategy3 = new SimpleStrategy(15);

		registry.registerStrategy(JAVA_25, strategy1);
		registry.registerStrategy(JAVA_25, strategy2);
		registry.registerStrategy(JAVA_25, strategy3);

		List<ParseStrategy> strategies = registry.getStrategies(JAVA_25);

		requireThat(strategies.size(), "strategiesSize").isEqualTo(3);
		requireThat(strategies.contains(strategy1), "containsStrategy1").isTrue();
		requireThat(strategies.contains(strategy2), "containsStrategy2").isTrue();
		requireThat(strategies.contains(strategy3), "containsStrategy3").isTrue();
	}

	/**
	 * Verifies that ParseStrategyRegistry returns strategies sorted by priority in descending order.
	 */
	@Test
	public void getStrategiesReturnsSortedByPriorityDescendingOrder()
	{
		ParseStrategyRegistry registry = new ParseStrategyRegistry();
		SimpleStrategy low = new SimpleStrategy(5);
		SimpleStrategy medium = new SimpleStrategy(10);
		SimpleStrategy high = new SimpleStrategy(20);

		// Register in random order
		registry.registerStrategy(JAVA_25, medium);
		registry.registerStrategy(JAVA_25, high);
		registry.registerStrategy(JAVA_25, low);

		List<ParseStrategy> strategies = registry.getStrategies(JAVA_25);

		// Should be sorted by priority descending
		requireThat(strategies.get(0), "firstStrategy").isEqualTo(high);
		requireThat(strategies.get(1), "secondStrategy").isEqualTo(medium);
		requireThat(strategies.get(2), "thirdStrategy").isEqualTo(low);
	}

	/**
	 * Verifies that ParseStrategyRegistry falls back through older Java versions when strategy not found.
	 */
	@Test
	public void versionFallbackChainRequestNewestFallsBackThroughVersions()
	{
		ParseStrategyRegistry registry = new ParseStrategyRegistry();
		SimpleStrategy java14Strategy = new SimpleStrategy(10);

		registry.registerStrategy(JAVA_14, java14Strategy);

		ParseContext context = ParseContextTestFactory.createMinimalContext(TokenType.LBRACE);

		// Request Java 25, should fall back through 24, 21 to 14
		ParseStrategy found = registry.findStrategy(JAVA_25, CONSTRUCTOR_BODY, context);

		requireThat(found, "foundStrategy").isEqualTo(java14Strategy);
	}

	/**
	 * Verifies that ParseStrategyRegistry uses exact version match when available without fallback.
	 */
	@Test
	public void findStrategyNoVersionFallbackWhenExactVersionExists()
	{
		ParseStrategyRegistry registry = new ParseStrategyRegistry();
		SimpleStrategy java21Strategy = new SimpleStrategy(5);
		SimpleStrategy java25Strategy = new SimpleStrategy(10);

		registry.registerStrategy(JAVA_21, java21Strategy);
		registry.registerStrategy(JAVA_25, java25Strategy);

		ParseContext context = ParseContextTestFactory.createMinimalContext(TokenType.LBRACE);

		ParseStrategy found = registry.findStrategy(JAVA_25, CONSTRUCTOR_BODY, context);

		// Should use Java 25 strategy, not fall back to Java 21
		requireThat(found, "foundStrategy").isEqualTo(java25Strategy);
	}

	/**
	 * Verifies that multiple ParseStrategyRegistry instances maintain independent state.
	 */
	@Test
	public void registryIsolationTwoInstancesCompletelyIndependent()
	{
		ParseStrategyRegistry registry1 = new ParseStrategyRegistry();
		ParseStrategyRegistry registry2 = new ParseStrategyRegistry();

		SimpleStrategy strategy1 = new SimpleStrategy(10);
		registry1.registerStrategy(JAVA_25, strategy1);

		// registry2 should not have strategy1
		List<ParseStrategy> strategies2 = registry2.getStrategies(JAVA_25);
		requireThat(strategies2.isEmpty(), "registry2IsEmpty").isTrue();

		// registry1 should have strategy1
		List<ParseStrategy> strategies1 = registry1.getStrategies(JAVA_25);
		requireThat(strategies1.size(), "registry1Size").isEqualTo(1);
	}

	/**
	 * Verifies that default strategy registration populates strategies for supported Java versions.
	 */
	@Test
	public void defaultStrategiesRegisteredForAllVersions()
	{
		ParseStrategyRegistry registry = new ParseStrategyRegistry();
		registry.registerDefaultStrategies();

		// Should have strategies for at least Java 25
		List<ParseStrategy> java25Strategies = registry.getStrategies(JAVA_25);
		requireThat(java25Strategies.isEmpty(), "java25StrategiesNotEmpty").isFalse();
	}

	/**
	 * Verifies that ParseStrategyRegistry selects strategies based on parse context token type.
	 */
	@Test
	public void findStrategyWithDifferentContextsSelectsAppropriately()
	{
		ParseStrategyRegistry registry = new ParseStrategyRegistry();
		TokenSpecificStrategy lbraceStrategy = new TokenSpecificStrategy(TokenType.LBRACE, 10);
		TokenSpecificStrategy semicolonStrategy = new TokenSpecificStrategy(TokenType.SEMICOLON, 10);

		registry.registerStrategy(JAVA_25, lbraceStrategy);
		registry.registerStrategy(JAVA_25, semicolonStrategy);

		ParseContext lbraceContext = ParseContextTestFactory.createMinimalContext(TokenType.LBRACE);
		ParseContext semicolonContext = ParseContextTestFactory.createMinimalContext(TokenType.SEMICOLON);

		ParseStrategy lbraceResult = registry.findStrategy(JAVA_25, CONSTRUCTOR_BODY, lbraceContext);
		ParseStrategy semicolonResult = registry.findStrategy(JAVA_25, CONSTRUCTOR_BODY,
			semicolonContext);

		requireThat(lbraceResult, "lbraceResult").isEqualTo(lbraceStrategy);
		requireThat(semicolonResult, "semicolonResult").isEqualTo(semicolonStrategy);
	}

	/**
	 * Verifies that ParseStrategyRegistry correctly handles complex scenarios with multiple versions,
	 * phases, and priorities.
	 */
	@Test
	public void complexIntegrationMultipleVersionsPhasesPrioritiesSelectsCorrectly()
	{
		ParseStrategyRegistry registry = new ParseStrategyRegistry();

		// Register complex scenario
		SimpleStrategy java21Low = new SimpleStrategy(5);
		PhaseSpecificStrategy java21Constructor = new PhaseSpecificStrategy(CONSTRUCTOR_BODY, 15);
		SimpleStrategy java25Medium = new SimpleStrategy(10);
		PhaseSpecificStrategy java25Constructor = new PhaseSpecificStrategy(CONSTRUCTOR_BODY, 20);

		registry.registerStrategy(JAVA_21, java21Low);
		registry.registerStrategy(JAVA_21, java21Constructor);
		registry.registerStrategy(JAVA_25, java25Medium);
		registry.registerStrategy(JAVA_25, java25Constructor);

		ParseContext context = ParseContextTestFactory.createMinimalContext(TokenType.LBRACE);

		// Java 25 CONSTRUCTOR_BODY -> should select java25Constructor (priority 20)
		ParseStrategy result1 = registry.findStrategy(JAVA_25, CONSTRUCTOR_BODY, context);
		requireThat(result1, "java25ConstructorResult").isEqualTo(java25Constructor);

		// Java 25 METHOD_BODY -> should select java25Medium (phase-specific doesn't handle)
		ParseStrategy result2 = registry.findStrategy(JAVA_25, METHOD_BODY, context);
		requireThat(result2, "java25MethodResult").isEqualTo(java25Medium);

		// Java 21 CONSTRUCTOR_BODY -> should select java21Constructor (priority 15)
		ParseStrategy result3 = registry.findStrategy(JAVA_21, CONSTRUCTOR_BODY, context);
		requireThat(result3, "java21ConstructorResult").isEqualTo(java21Constructor);
	}

	/**
	 * Simple strategy that always handles and has configurable priority.
	 */
	private static final class SimpleStrategy implements ParseStrategy
	{
		private final int priority;

		SimpleStrategy(int priority)
		{
			this.priority = priority;
		}

		@Override
		public boolean canHandle(JavaVersion version, ParsingPhase phase, ParseContext context)
		{
			return true;
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
			return "Simple strategy (priority=" + priority + ")";
		}
	}

	/**
	 * Version-specific strategy that only handles its target version.
	 */
	private static final class VersionSpecificStrategy implements ParseStrategy
	{
		private final JavaVersion targetVersion;
		private final int priority;

		VersionSpecificStrategy(JavaVersion targetVersion, int priority)
		{
			this.targetVersion = targetVersion;
			this.priority = priority;
		}

		@Override
		public boolean canHandle(JavaVersion version, ParsingPhase phase, ParseContext context)
		{
			return version == targetVersion;
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
			return "Version-specific strategy (version=" + targetVersion + ")";
		}
	}

	/**
	 * Phase-specific strategy that only handles its target phase.
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
			return "Phase-specific strategy";
		}
	}

	/**
	 * Token-specific strategy that only handles specific token types.
	 */
	private static final class TokenSpecificStrategy implements ParseStrategy
	{
		private final TokenType targetToken;
		private final int priority;

		TokenSpecificStrategy(TokenType targetToken, int priority)
		{
			this.targetToken = targetToken;
			this.priority = priority;
		}

		@Override
		public boolean canHandle(JavaVersion version, ParsingPhase phase, ParseContext context)
		{
			return context.currentTokenIs(targetToken);
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
			return "Token-specific strategy (token=" + targetToken + ")";
		}
	}
}
