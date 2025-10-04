package io.github.cowwoc.styler.parser.test.strategy;

import io.github.cowwoc.styler.parser.ParseContext;
import io.github.cowwoc.styler.parser.ParseStrategy;
import io.github.cowwoc.styler.parser.ParsingPhase;
import io.github.cowwoc.styler.parser.TokenType;
import io.github.cowwoc.styler.parser.strategies.PrimitiveTypePatternStrategy;
import org.testng.annotations.Test;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;
import static io.github.cowwoc.styler.parser.test.strategy.StrategyTestConstants.*;

/**
 * Tests for {@link PrimitiveTypePatternStrategy}.
 * Validates token-based strategy behavior for JDK 25 primitive type patterns (JEP 507).
 */
public final class PrimitiveTypePatternStrategyTest
{
	// ========== Version Filtering Tests ==========

	@Test
	public void canHandle_withJava25AndIntKeyword_returnsTrue()
	{
		PrimitiveTypePatternStrategy strategy = new PrimitiveTypePatternStrategy();
		ParseContext context = ParseContextTestFactory.createMinimalContext(TokenType.INT);

		boolean result = strategy.canHandle(JAVA_25, CLASS_BODY, context);

		requireThat(result, "canHandle").isTrue();
	}

	@Test
	public void canHandle_withJava24_returnsFalse()
	{
		PrimitiveTypePatternStrategy strategy = new PrimitiveTypePatternStrategy();
		ParseContext context = ParseContextTestFactory.createMinimalContext(TokenType.INT);

		boolean result = strategy.canHandle(JAVA_24, CLASS_BODY, context);

		requireThat(result, "canHandle").isFalse();
	}

	// ========== Primitive Type Coverage Tests ==========

	@Test
	public void canHandle_withIntKeyword_returnsTrue()
	{
		PrimitiveTypePatternStrategy strategy = new PrimitiveTypePatternStrategy();
		ParseContext context = ParseContextTestFactory.createMinimalContext(TokenType.INT);

		boolean result = strategy.canHandle(JAVA_25, CLASS_BODY, context);

		requireThat(result, "canHandle").isTrue();
	}

	@Test
	public void canHandle_withLongKeyword_returnsTrue()
	{
		PrimitiveTypePatternStrategy strategy = new PrimitiveTypePatternStrategy();
		ParseContext context = ParseContextTestFactory.createMinimalContext(TokenType.LONG);

		boolean result = strategy.canHandle(JAVA_25, CLASS_BODY, context);

		requireThat(result, "canHandle").isTrue();
	}

	@Test
	public void canHandle_withDoubleKeyword_returnsTrue()
	{
		PrimitiveTypePatternStrategy strategy = new PrimitiveTypePatternStrategy();
		ParseContext context = ParseContextTestFactory.createMinimalContext(TokenType.DOUBLE);

		boolean result = strategy.canHandle(JAVA_25, CLASS_BODY, context);

		requireThat(result, "canHandle").isTrue();
	}

	@Test
	public void canHandle_withFloatKeyword_returnsTrue()
	{
		PrimitiveTypePatternStrategy strategy = new PrimitiveTypePatternStrategy();
		ParseContext context = ParseContextTestFactory.createMinimalContext(TokenType.FLOAT);

		boolean result = strategy.canHandle(JAVA_25, CLASS_BODY, context);

		requireThat(result, "canHandle").isTrue();
	}

	@Test
	public void canHandle_withBooleanKeyword_returnsTrue()
	{
		PrimitiveTypePatternStrategy strategy = new PrimitiveTypePatternStrategy();
		ParseContext context = ParseContextTestFactory.createMinimalContext(TokenType.BOOLEAN);

		boolean result = strategy.canHandle(JAVA_25, CLASS_BODY, context);

		requireThat(result, "canHandle").isTrue();
	}

	@Test
	public void canHandle_withByteKeyword_returnsTrue()
	{
		PrimitiveTypePatternStrategy strategy = new PrimitiveTypePatternStrategy();
		ParseContext context = ParseContextTestFactory.createMinimalContext(TokenType.BYTE);

		boolean result = strategy.canHandle(JAVA_25, CLASS_BODY, context);

		requireThat(result, "canHandle").isTrue();
	}

	@Test
	public void canHandle_withShortKeyword_returnsTrue()
	{
		PrimitiveTypePatternStrategy strategy = new PrimitiveTypePatternStrategy();
		ParseContext context = ParseContextTestFactory.createMinimalContext(TokenType.SHORT);

		boolean result = strategy.canHandle(JAVA_25, CLASS_BODY, context);

		requireThat(result, "canHandle").isTrue();
	}

	@Test
	public void canHandle_withCharKeyword_returnsTrue()
	{
		PrimitiveTypePatternStrategy strategy = new PrimitiveTypePatternStrategy();
		ParseContext context = ParseContextTestFactory.createMinimalContext(TokenType.CHAR);

		boolean result = strategy.canHandle(JAVA_25, CLASS_BODY, context);

		requireThat(result, "canHandle").isTrue();
	}

	// ========== Negative Cases ==========

	@Test
	public void canHandle_withNonPrimitiveKeyword_returnsFalse()
	{
		PrimitiveTypePatternStrategy strategy = new PrimitiveTypePatternStrategy();
		ParseContext context = ParseContextTestFactory.createMinimalContext(TokenType.CLASS);

		boolean result = strategy.canHandle(JAVA_25, CLASS_BODY, context);

		requireThat(result, "canHandle").isFalse();
	}

	@Test
	public void canHandle_withIdentifier_returnsFalse()
	{
		PrimitiveTypePatternStrategy strategy = new PrimitiveTypePatternStrategy();
		ParseContext context = ParseContextTestFactory.createMinimalContext(TokenType.IDENTIFIER);

		boolean result = strategy.canHandle(JAVA_25, CLASS_BODY, context);

		requireThat(result, "canHandle").isFalse();
	}

	@Test
	public void canHandle_withVoid_returnsFalse()
	{
		PrimitiveTypePatternStrategy strategy = new PrimitiveTypePatternStrategy();
		ParseContext context = ParseContextTestFactory.createMinimalContext(TokenType.VOID);

		boolean result = strategy.canHandle(JAVA_25, CLASS_BODY, context);

		requireThat(result, "canHandle").isFalse();
	}

	// ========== Phase Independence Validation ==========

	@Test
	public void canHandle_withTopLevelPhase_returnsTrue()
	{
		PrimitiveTypePatternStrategy strategy = new PrimitiveTypePatternStrategy();
		ParseContext context = ParseContextTestFactory.createMinimalContext(TokenType.INT);

		boolean result = strategy.canHandle(JAVA_25, TOP_LEVEL, context);

		requireThat(result, "canHandle").isTrue();
	}

	@Test
	public void canHandle_withClassBodyPhase_returnsTrue()
	{
		PrimitiveTypePatternStrategy strategy = new PrimitiveTypePatternStrategy();
		ParseContext context = ParseContextTestFactory.createMinimalContext(TokenType.INT);

		boolean result = strategy.canHandle(JAVA_25, CLASS_BODY, context);

		requireThat(result, "canHandle").isTrue();
	}

	@Test
	public void canHandle_withMethodBodyPhase_returnsTrue()
	{
		PrimitiveTypePatternStrategy strategy = new PrimitiveTypePatternStrategy();
		ParseContext context = ParseContextTestFactory.createMinimalContext(TokenType.INT);

		boolean result = strategy.canHandle(JAVA_25, METHOD_BODY, context);

		requireThat(result, "canHandle").isTrue();
	}

	@Test
	public void canHandle_withConstructorBodyPhase_returnsTrue()
	{
		PrimitiveTypePatternStrategy strategy = new PrimitiveTypePatternStrategy();
		ParseContext context = ParseContextTestFactory.createMinimalContext(TokenType.INT);

		boolean result = strategy.canHandle(JAVA_25, CONSTRUCTOR_BODY, context);

		requireThat(result, "canHandle").isTrue();
	}

	@Test
	public void canHandle_phaseDoesNotAffectResult_allPhasesConsistent()
	{
		PrimitiveTypePatternStrategy strategy = new PrimitiveTypePatternStrategy();
		ParseContext context = ParseContextTestFactory.createMinimalContext(TokenType.LONG);

		ParsingPhase[] allPhases = {
			TOP_LEVEL, CLASS_BODY, METHOD_BODY, CONSTRUCTOR_BODY, INITIALIZER_BLOCK
		};

		for (ParsingPhase phase : allPhases)
		{
			boolean result = strategy.canHandle(JAVA_25, phase, context);
			requireThat(result, "result").isTrue();
		}
	}

	// ========== Priority Validation ==========

	@Test
	public void getPriority_returnsKeywordBasedPriority()
	{
		PrimitiveTypePatternStrategy strategy = new PrimitiveTypePatternStrategy();

		int priority = strategy.getPriority();

		requireThat(priority, "priority").isEqualTo(ParseStrategy.PRIORITY_KEYWORD_BASED);
	}

	// ========== Description Validation ==========

	@Test
	public void getDescription_containsJavaVersionAndJep()
	{
		PrimitiveTypePatternStrategy strategy = new PrimitiveTypePatternStrategy();

		String description = strategy.getDescription();

		requireThat(description, "description").contains("Java 25");
		requireThat(description, "description").contains("JEP 507");
	}

	// ========== Error Handling ==========

	@Test(expectedExceptions = UnsupportedOperationException.class)
	public void parseConstruct_throwsUnsupportedOperationException()
	{
		PrimitiveTypePatternStrategy strategy = new PrimitiveTypePatternStrategy();
		ParseContext context = ParseContextTestFactory.createMinimalContext(TokenType.INT);

		strategy.parseConstruct(context);
	}
}
