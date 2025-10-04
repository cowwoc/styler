package io.github.cowwoc.styler.parser.test.strategy;

import io.github.cowwoc.styler.parser.JavaVersion;
import io.github.cowwoc.styler.parser.ParseContext;
import io.github.cowwoc.styler.parser.ParseStrategy;
import io.github.cowwoc.styler.parser.ParseStrategyRegistry;
import io.github.cowwoc.styler.parser.ParsingPhase;
import io.github.cowwoc.styler.parser.TokenInfo;
import io.github.cowwoc.styler.parser.TokenType;
import io.github.cowwoc.styler.parser.strategies.FlexibleConstructorBodiesStrategy;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;
import static io.github.cowwoc.styler.parser.test.strategy.StrategyTestConstants.*;

/**
 * Integration tests for phase-aware parsing.
 * Validates end-to-end parsing scenarios with phase transitions and strategy interactions.
 */
public final class PhaseAwareParsingIntegrationTest
{
	@Test
	public void parseConstructorBody_withFlexibleConstructorBodiesStrategy_findsStrategy()
	{
		ParseStrategyRegistry registry = new ParseStrategyRegistry();
		FlexibleConstructorBodiesStrategy strategy = new FlexibleConstructorBodiesStrategy();
		registry.registerStrategy(JAVA_25, strategy);

		ParseContext context = ParseContextTestFactory.createMinimalContext(TokenType.LBRACE);
		ParseStrategy found = registry.findStrategy(JAVA_25, CONSTRUCTOR_BODY, context);

		requireThat(found, "foundStrategy").isEqualTo(strategy);
	}

	@Test
	public void parseConstructorBody_java24_doesNotFindFlexibleConstructorBodiesStrategy()
	{
		ParseStrategyRegistry registry = new ParseStrategyRegistry();
		FlexibleConstructorBodiesStrategy strategy = new FlexibleConstructorBodiesStrategy();
		registry.registerStrategy(JAVA_24, strategy);

		ParseContext context = ParseContextTestFactory.createMinimalContext(TokenType.LBRACE);
		ParseStrategy found = registry.findStrategy(JAVA_24, CONSTRUCTOR_BODY, context);

		// Strategy exists but canHandle returns false for Java 24
		requireThat(found, "foundStrategy").isNull();
	}

	@Test
	public void parseMethodBody_withConstructorOnlyStrategy_returnsNull()
	{
		ParseStrategyRegistry registry = new ParseStrategyRegistry();
		FlexibleConstructorBodiesStrategy strategy = new FlexibleConstructorBodiesStrategy();
		registry.registerStrategy(JAVA_25, strategy);

		ParseContext context = ParseContextTestFactory.createMinimalContext(TokenType.LBRACE);
		ParseStrategy found = registry.findStrategy(JAVA_25, METHOD_BODY, context);

		requireThat(found, "foundStrategy").isNull();
	}

	@Test
	public void phaseTransition_fromClassToConstructor_selectsPhaseAwareStrategy()
	{
		ParseStrategyRegistry registry = new ParseStrategyRegistry();
		PhaseTrackingStrategy classStrategy = new PhaseTrackingStrategy(CLASS_BODY, 10);
		PhaseTrackingStrategy constructorStrategy = new PhaseTrackingStrategy(CONSTRUCTOR_BODY, 15);

		registry.registerStrategy(JAVA_25, classStrategy);
		registry.registerStrategy(JAVA_25, constructorStrategy);

		ParseContext context = ParseContextTestFactory.createMinimalContext(TokenType.LBRACE);

		// In CLASS_BODY phase, should select classStrategy
		ParseStrategy classPhase = registry.findStrategy(JAVA_25, CLASS_BODY, context);
		requireThat(classPhase, "classPhaseStrategy").isEqualTo(classStrategy);

		// In CONSTRUCTOR_BODY phase, should select constructorStrategy
		ParseStrategy constructorPhase = registry.findStrategy(JAVA_25, CONSTRUCTOR_BODY, context);
		requireThat(constructorPhase, "constructorPhaseStrategy").isEqualTo(constructorStrategy);
	}

	@Test
	public void multiplePhaseAwareStrategies_eachHandlesDifferentPhase()
	{
		ParseStrategyRegistry registry = new ParseStrategyRegistry();
		PhaseTrackingStrategy topLevel = new PhaseTrackingStrategy(TOP_LEVEL, 10);
		PhaseTrackingStrategy classBody = new PhaseTrackingStrategy(CLASS_BODY, 10);
		PhaseTrackingStrategy methodBody = new PhaseTrackingStrategy(METHOD_BODY, 10);

		registry.registerStrategy(JAVA_25, topLevel);
		registry.registerStrategy(JAVA_25, classBody);
		registry.registerStrategy(JAVA_25, methodBody);

		ParseContext context = ParseContextTestFactory.createMinimalContext(TokenType.LBRACE);

		requireThat(registry.findStrategy(JAVA_25, TOP_LEVEL, context), "topLevelResult")
			.isEqualTo(topLevel);
		requireThat(registry.findStrategy(JAVA_25, CLASS_BODY, context), "classBodyResult")
			.isEqualTo(classBody);
		requireThat(registry.findStrategy(JAVA_25, METHOD_BODY, context), "methodBodyResult")
			.isEqualTo(methodBody);
	}

	@Test
	public void constructorWithStatements_java25_selectsFlexibleConstructorStrategy()
	{
		ParseStrategyRegistry registry = new ParseStrategyRegistry();
		registry.registerDefaultStrategies();

		// Simulate constructor body: { statement; }
		List<TokenInfo> tokens = new ArrayList<>();
		tokens.add(new TokenInfo(TokenType.LBRACE, 0, 1, "{"));
		tokens.add(new TokenInfo(TokenType.IDENTIFIER, 2, 9, "statement"));
		tokens.add(new TokenInfo(TokenType.SEMICOLON, 11, 1, ";"));
		tokens.add(new TokenInfo(TokenType.RBRACE, 13, 1, "}"));

		ParseContext context = ParseContextTestFactory.createFromTokensAndSource(tokens,
			"{ statement; }");

		ParseStrategy found = registry.findStrategy(JAVA_25, CONSTRUCTOR_BODY, context);

		requireThat(found, "foundStrategy").isNotNull();
		requireThat(found.getDescription().contains("25"), "descriptionContains25").isTrue();
	}

	@Test
	public void versionFallback_java25Request_findsJava21Strategy()
	{
		ParseStrategyRegistry registry = new ParseStrategyRegistry();
		PhaseTrackingStrategy java21Strategy = new PhaseTrackingStrategy(CONSTRUCTOR_BODY, 15);

		// Only register for Java 21
		registry.registerStrategy(JAVA_21, java21Strategy);

		ParseContext context = ParseContextTestFactory.createMinimalContext(TokenType.LBRACE);

		// Request Java 25, should fall back to Java 21
		ParseStrategy found = registry.findStrategy(JAVA_25, CONSTRUCTOR_BODY, context);

		requireThat(found, "foundStrategy").isEqualTo(java21Strategy);
	}

	@Test
	public void versionFallback_multipleEarlierVersions_selectsFirstMatch()
	{
		ParseStrategyRegistry registry = new ParseStrategyRegistry();
		PhaseTrackingStrategy java14Strategy = new PhaseTrackingStrategy(CONSTRUCTOR_BODY, 15);
		PhaseTrackingStrategy java21Strategy = new PhaseTrackingStrategy(CONSTRUCTOR_BODY, 15);

		registry.registerStrategy(JAVA_14, java14Strategy);
		registry.registerStrategy(JAVA_21, java21Strategy);

		ParseContext context = ParseContextTestFactory.createMinimalContext(TokenType.LBRACE);

		// Request Java 25, fallback iterates in declaration order (14, 21, 24)
		// Should find Java 14 first (declaration order)
		ParseStrategy found = registry.findStrategy(JAVA_25, CONSTRUCTOR_BODY, context);

		requireThat(found, "foundStrategy").isEqualTo(java14Strategy);
	}

	@Test
	public void mixedStrategies_phaseAwareAndGeneral_selectsPhaseAwareWhenApplicable()
	{
		ParseStrategyRegistry registry = new ParseStrategyRegistry();
		GeneralStrategy generalStrategy = new GeneralStrategy(10);
		PhaseTrackingStrategy phaseAwareStrategy = new PhaseTrackingStrategy(CONSTRUCTOR_BODY,
			ParseStrategy.PRIORITY_PHASE_AWARE);

		registry.registerStrategy(JAVA_25, generalStrategy);
		registry.registerStrategy(JAVA_25, phaseAwareStrategy);

		ParseContext context = ParseContextTestFactory.createMinimalContext(TokenType.LBRACE);

		// In CONSTRUCTOR_BODY, phase-aware should win (higher priority)
		ParseStrategy constructorResult = registry.findStrategy(JAVA_25, CONSTRUCTOR_BODY, context);
		requireThat(constructorResult, "constructorResult").isEqualTo(phaseAwareStrategy);

		// In METHOD_BODY, general should win (phase-aware doesn't handle)
		ParseStrategy methodResult = registry.findStrategy(JAVA_25, METHOD_BODY, context);
		requireThat(methodResult, "methodResult").isEqualTo(generalStrategy);
	}

	@Test
	public void emptyRegistry_returnsNull_forAllPhases()
	{
		ParseStrategyRegistry registry = new ParseStrategyRegistry();
		ParseContext context = ParseContextTestFactory.createMinimalContext(TokenType.LBRACE);

		for (ParsingPhase phase : ParsingPhase.values())
		{
			ParseStrategy found = registry.findStrategy(JAVA_25, phase, context);
			requireThat(found, "strategy_" + phase.name()).isNull();
		}
	}

	@Test
	public void defaultStrategies_coverAllSupportedPhases()
	{
		ParseStrategyRegistry registry = new ParseStrategyRegistry();
		registry.registerDefaultStrategies();

		ParseContext context = ParseContextTestFactory.createMinimalContext(TokenType.LBRACE);

		// At minimum, CONSTRUCTOR_BODY should have a strategy for Java 25
		ParseStrategy constructorStrategy = registry.findStrategy(JAVA_25, CONSTRUCTOR_BODY, context);
		requireThat(constructorStrategy, "constructorStrategy").isNotNull();
	}

	@Test
	public void strategyPriority_phaseAwareValue_isCorrect()
	{
		requireThat(ParseStrategy.PRIORITY_PHASE_AWARE, "phaseAwarePriority").isEqualTo(15);
	}

	@Test
	public void registryIsolation_multipleRegistries_independentState()
	{
		ParseStrategyRegistry registry1 = new ParseStrategyRegistry();
		ParseStrategyRegistry registry2 = new ParseStrategyRegistry();

		PhaseTrackingStrategy strategy1 = new PhaseTrackingStrategy(CONSTRUCTOR_BODY, 10);
		PhaseTrackingStrategy strategy2 = new PhaseTrackingStrategy(CONSTRUCTOR_BODY, 20);

		registry1.registerStrategy(JAVA_25, strategy1);
		registry2.registerStrategy(JAVA_25, strategy2);

		ParseContext context = ParseContextTestFactory.createMinimalContext(TokenType.LBRACE);

		ParseStrategy found1 = registry1.findStrategy(JAVA_25, CONSTRUCTOR_BODY, context);
		ParseStrategy found2 = registry2.findStrategy(JAVA_25, CONSTRUCTOR_BODY, context);

		requireThat(found1, "registry1Strategy").isEqualTo(strategy1);
		requireThat(found2, "registry2Strategy").isEqualTo(strategy2);
		requireThat(found1, "registriesIndependent").isNotEqualTo(found2);
	}

	/**
	 * Phase-specific tracking strategy for testing phase transitions.
	 */
	private static final class PhaseTrackingStrategy implements ParseStrategy
	{
		private final ParsingPhase targetPhase;
		private final int priority;

		PhaseTrackingStrategy(ParsingPhase targetPhase, int priority)
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
			return "Phase tracking strategy (phase=" + targetPhase + ")";
		}
	}

	/**
	 * General strategy that handles all phases (for fallback testing).
	 */
	private static final class GeneralStrategy implements ParseStrategy
	{
		private final int priority;

		GeneralStrategy(int priority)
		{
			this.priority = priority;
		}

		@Override
		public boolean canHandle(JavaVersion version, ParsingPhase phase, ParseContext context)
		{
			return true; // Handles all phases
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
			return "General strategy (priority=" + priority + ")";
		}
	}
}
