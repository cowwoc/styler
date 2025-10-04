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

	/**
	 * Verifies that PrimitiveTypePatternStrategy handles Java 25 int keyword.
	 */
	@Test
	public void canHandleWithJava25AndIntKeywordReturnsTrue()
	{
		PrimitiveTypePatternStrategy strategy = new PrimitiveTypePatternStrategy();
		ParseContext context = ParseContextTestFactory.createMinimalContext(TokenType.INT);

		boolean result = strategy.canHandle(JAVA_25, CLASS_BODY, context);

		requireThat(result, "canHandle").isTrue();
	}

	/**
	 * Verifies that PrimitiveTypePatternStrategy rejects Java 24 source version.
	 */
	@Test
	public void canHandleWithJava24ReturnsFalse()
	{
		PrimitiveTypePatternStrategy strategy = new PrimitiveTypePatternStrategy();
		ParseContext context = ParseContextTestFactory.createMinimalContext(TokenType.INT);

		boolean result = strategy.canHandle(JAVA_24, CLASS_BODY, context);

		requireThat(result, "canHandle").isFalse();
	}

	// ========== Primitive Type Coverage Tests ==========

	/**
	 * Verifies that PrimitiveTypePatternStrategy handles int primitive type keyword.
	 */
	@Test
	public void canHandleWithIntKeywordReturnsTrue()
	{
		PrimitiveTypePatternStrategy strategy = new PrimitiveTypePatternStrategy();
		ParseContext context = ParseContextTestFactory.createMinimalContext(TokenType.INT);

		boolean result = strategy.canHandle(JAVA_25, CLASS_BODY, context);

		requireThat(result, "canHandle").isTrue();
	}

	/**
	 * Verifies that PrimitiveTypePatternStrategy handles long primitive type keyword.
	 */
	@Test
	public void canHandleWithLongKeywordReturnsTrue()
	{
		PrimitiveTypePatternStrategy strategy = new PrimitiveTypePatternStrategy();
		ParseContext context = ParseContextTestFactory.createMinimalContext(TokenType.LONG);

		boolean result = strategy.canHandle(JAVA_25, CLASS_BODY, context);

		requireThat(result, "canHandle").isTrue();
	}

	/**
	 * Verifies that PrimitiveTypePatternStrategy handles double primitive type keyword.
	 */
	@Test
	public void canHandleWithDoubleKeywordReturnsTrue()
	{
		PrimitiveTypePatternStrategy strategy = new PrimitiveTypePatternStrategy();
		ParseContext context = ParseContextTestFactory.createMinimalContext(TokenType.DOUBLE);

		boolean result = strategy.canHandle(JAVA_25, CLASS_BODY, context);

		requireThat(result, "canHandle").isTrue();
	}

	/**
	 * Verifies that PrimitiveTypePatternStrategy handles float primitive type keyword.
	 */
	@Test
	public void canHandleWithFloatKeywordReturnsTrue()
	{
		PrimitiveTypePatternStrategy strategy = new PrimitiveTypePatternStrategy();
		ParseContext context = ParseContextTestFactory.createMinimalContext(TokenType.FLOAT);

		boolean result = strategy.canHandle(JAVA_25, CLASS_BODY, context);

		requireThat(result, "canHandle").isTrue();
	}

	/**
	 * Verifies that PrimitiveTypePatternStrategy handles boolean primitive type keyword.
	 */
	@Test
	public void canHandleWithBooleanKeywordReturnsTrue()
	{
		PrimitiveTypePatternStrategy strategy = new PrimitiveTypePatternStrategy();
		ParseContext context = ParseContextTestFactory.createMinimalContext(TokenType.BOOLEAN);

		boolean result = strategy.canHandle(JAVA_25, CLASS_BODY, context);

		requireThat(result, "canHandle").isTrue();
	}

	/**
	 * Verifies that PrimitiveTypePatternStrategy handles byte primitive type keyword.
	 */
	@Test
	public void canHandleWithByteKeywordReturnsTrue()
	{
		PrimitiveTypePatternStrategy strategy = new PrimitiveTypePatternStrategy();
		ParseContext context = ParseContextTestFactory.createMinimalContext(TokenType.BYTE);

		boolean result = strategy.canHandle(JAVA_25, CLASS_BODY, context);

		requireThat(result, "canHandle").isTrue();
	}

	/**
	 * Verifies that PrimitiveTypePatternStrategy handles short primitive type keyword.
	 */
	@Test
	public void canHandleWithShortKeywordReturnsTrue()
	{
		PrimitiveTypePatternStrategy strategy = new PrimitiveTypePatternStrategy();
		ParseContext context = ParseContextTestFactory.createMinimalContext(TokenType.SHORT);

		boolean result = strategy.canHandle(JAVA_25, CLASS_BODY, context);

		requireThat(result, "canHandle").isTrue();
	}

	/**
	 * Verifies that PrimitiveTypePatternStrategy handles char primitive type keyword.
	 */
	@Test
	public void canHandleWithCharKeywordReturnsTrue()
	{
		PrimitiveTypePatternStrategy strategy = new PrimitiveTypePatternStrategy();
		ParseContext context = ParseContextTestFactory.createMinimalContext(TokenType.CHAR);

		boolean result = strategy.canHandle(JAVA_25, CLASS_BODY, context);

		requireThat(result, "canHandle").isTrue();
	}

	// ========== Negative Cases ==========

	/**
	 * Verifies that PrimitiveTypePatternStrategy rejects non-primitive keywords.
	 */
	@Test
	public void canHandleWithNonPrimitiveKeywordReturnsFalse()
	{
		PrimitiveTypePatternStrategy strategy = new PrimitiveTypePatternStrategy();
		ParseContext context = ParseContextTestFactory.createMinimalContext(TokenType.CLASS);

		boolean result = strategy.canHandle(JAVA_25, CLASS_BODY, context);

		requireThat(result, "canHandle").isFalse();
	}

	/**
	 * Verifies that PrimitiveTypePatternStrategy rejects identifier tokens.
	 */
	@Test
	public void canHandleWithIdentifierReturnsFalse()
	{
		PrimitiveTypePatternStrategy strategy = new PrimitiveTypePatternStrategy();
		ParseContext context = ParseContextTestFactory.createMinimalContext(TokenType.IDENTIFIER);

		boolean result = strategy.canHandle(JAVA_25, CLASS_BODY, context);

		requireThat(result, "canHandle").isFalse();
	}

	/**
	 * Verifies that PrimitiveTypePatternStrategy rejects void keyword.
	 */
	@Test
	public void canHandleWithVoidReturnsFalse()
	{
		PrimitiveTypePatternStrategy strategy = new PrimitiveTypePatternStrategy();
		ParseContext context = ParseContextTestFactory.createMinimalContext(TokenType.VOID);

		boolean result = strategy.canHandle(JAVA_25, CLASS_BODY, context);

		requireThat(result, "canHandle").isFalse();
	}

	// ========== Phase Independence Validation ==========

	/**
	 * Verifies that PrimitiveTypePatternStrategy handles top-level parsing phase.
	 */
	@Test
	public void canHandleWithTopLevelPhaseReturnsTrue()
	{
		PrimitiveTypePatternStrategy strategy = new PrimitiveTypePatternStrategy();
		ParseContext context = ParseContextTestFactory.createMinimalContext(TokenType.INT);

		boolean result = strategy.canHandle(JAVA_25, TOP_LEVEL, context);

		requireThat(result, "canHandle").isTrue();
	}

	/**
	 * Verifies that PrimitiveTypePatternStrategy handles class body parsing phase.
	 */
	@Test
	public void canHandleWithClassBodyPhaseReturnsTrue()
	{
		PrimitiveTypePatternStrategy strategy = new PrimitiveTypePatternStrategy();
		ParseContext context = ParseContextTestFactory.createMinimalContext(TokenType.INT);

		boolean result = strategy.canHandle(JAVA_25, CLASS_BODY, context);

		requireThat(result, "canHandle").isTrue();
	}

	/**
	 * Verifies that PrimitiveTypePatternStrategy handles method body parsing phase.
	 */
	@Test
	public void canHandleWithMethodBodyPhaseReturnsTrue()
	{
		PrimitiveTypePatternStrategy strategy = new PrimitiveTypePatternStrategy();
		ParseContext context = ParseContextTestFactory.createMinimalContext(TokenType.INT);

		boolean result = strategy.canHandle(JAVA_25, METHOD_BODY, context);

		requireThat(result, "canHandle").isTrue();
	}

	/**
	 * Verifies that PrimitiveTypePatternStrategy handles constructor body parsing phase.
	 */
	@Test
	public void canHandleWithConstructorBodyPhaseReturnsTrue()
	{
		PrimitiveTypePatternStrategy strategy = new PrimitiveTypePatternStrategy();
		ParseContext context = ParseContextTestFactory.createMinimalContext(TokenType.INT);

		boolean result = strategy.canHandle(JAVA_25, CONSTRUCTOR_BODY, context);

		requireThat(result, "canHandle").isTrue();
	}

	/**
	 * Verifies that PrimitiveTypePatternStrategy behaves consistently across all parsing phases.
	 */
	@Test
	public void canHandlePhaseDoesNotAffectResultAllPhasesConsistent()
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

	/**
	 * Verifies that PrimitiveTypePatternStrategy returns keyword-based priority level.
	 */
	@Test
	public void getPriorityReturnsKeywordBasedPriority()
	{
		PrimitiveTypePatternStrategy strategy = new PrimitiveTypePatternStrategy();

		int priority = strategy.getPriority();

		requireThat(priority, "priority").isEqualTo(ParseStrategy.PRIORITY_KEYWORD_BASED);
	}

	// ========== Description Validation ==========

	/**
	 * Verifies that PrimitiveTypePatternStrategy description includes Java version and JEP reference.
	 */
	@Test
	public void getDescriptionContainsJavaVersionAndJep()
	{
		PrimitiveTypePatternStrategy strategy = new PrimitiveTypePatternStrategy();

		String description = strategy.getDescription();

		requireThat(description, "description").contains("Java 25");
		requireThat(description, "description").contains("JEP 507");
	}

	// ========== Error Handling ==========

	/**
	 * Verifies that PrimitiveTypePatternStrategy throws UnsupportedOperationException for parseConstruct.
	 */
	@Test(expectedExceptions = UnsupportedOperationException.class)
	public void parseConstructThrowsUnsupportedOperationException()
	{
		PrimitiveTypePatternStrategy strategy = new PrimitiveTypePatternStrategy();
		ParseContext context = ParseContextTestFactory.createMinimalContext(TokenType.INT);

		strategy.parseConstruct(context);
	}
}
