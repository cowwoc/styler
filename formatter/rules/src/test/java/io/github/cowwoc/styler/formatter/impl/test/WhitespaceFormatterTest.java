package io.github.cowwoc.styler.formatter.impl.test;

import io.github.cowwoc.styler.ast.FormattingHints;
import io.github.cowwoc.styler.ast.SourcePosition;
import io.github.cowwoc.styler.ast.SourceRange;
import io.github.cowwoc.styler.ast.WhitespaceInfo;
import io.github.cowwoc.styler.ast.node.BinaryExpressionNode;
import io.github.cowwoc.styler.ast.node.CompilationUnitNode;
import io.github.cowwoc.styler.ast.node.NumberLiteralNode;
import io.github.cowwoc.styler.formatter.api.FormattingContext;
import io.github.cowwoc.styler.formatter.api.FormattingResult;
import io.github.cowwoc.styler.formatter.api.RuleConfiguration;
import io.github.cowwoc.styler.formatter.api.ValidationResult;
import io.github.cowwoc.styler.formatter.impl.WhitespaceConfiguration;
import io.github.cowwoc.styler.formatter.impl.WhitespaceFormatter;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link WhitespaceFormatter}.
 * <p>
 * Validates rule interface implementation, validation logic, and thread safety.
 */
public final class WhitespaceFormatterTest
{
	/**
	 * Verifies that formatter returns correct rule identifier.
	 */
	@Test
	public void getRuleIdReturnsCorrectIdentifier()
	{
		WhitespaceFormatter formatter = new WhitespaceFormatter();

		String ruleId = formatter.getRuleId();

		assertThat(ruleId).isEqualTo("io.github.cowwoc.styler.rules.Whitespace");
	}

	/**
	 * Verifies that formatter executes at priority 50 before line length rule.
	 */
	@Test
	public void getPriorityReturnsPriority50()
	{
		WhitespaceFormatter formatter = new WhitespaceFormatter();

		int priority = formatter.getPriority();

		assertThat(priority).isEqualTo(50);
	}

	/**
	 * Verifies that default configuration is a valid WhitespaceConfiguration instance.
	 */
	@Test
	public void getDefaultConfigurationReturnsWhitespaceConfiguration()
	{
		WhitespaceFormatter formatter = new WhitespaceFormatter();

		RuleConfiguration config = formatter.getDefaultConfiguration();

		assertThat(config).isInstanceOf(WhitespaceConfiguration.class);
	}

	/**
	 * Verifies that validation fails when context is null.
	 */
	@Test
	public void validateWithNullContextReturnsFailure()
	{
		WhitespaceFormatter formatter = new WhitespaceFormatter();

		ValidationResult result = formatter.validate(null);

		assertThat(result.isFailure()).isTrue();
		assertThat(result.getFirstErrorMessage()).contains("cannot be null");
	}

	/**
	 * Verifies that operator spacing rule detects missing spaces around binary operators.
	 */
	@Test
	public void applyDetectsOperatorSpacingViolations()
	{
		// Create simple Java expression with no spacing: "1+2"
		String sourceText = "1+2";
		CompilationUnitNode root = createASTWithBinaryExpression(
			sourceText, 0, 1, "+", 2, 3);
		FormattingContext context = createContext(root, sourceText);

		WhitespaceFormatter formatter = new WhitespaceFormatter();
		FormattingResult result = formatter.apply(context);

		// Should generate edits to add spacing (expected: "1 + 2")
		assertThat(result.hasEdits()).isTrue();
	}

	/**
	 * Verifies that properly spaced operators do not generate edits.
	 */
	@Test
	public void applyAcceptsCorrectlySpacedOperators()
	{
		// Create expression with correct spacing: "1 + 2"
		String sourceText = "1 + 2";
		CompilationUnitNode root = createASTWithBinaryExpression(
			sourceText, 0, 1, "+", 4, 5);
		FormattingContext context = createContext(root, sourceText);

		WhitespaceFormatter formatter = new WhitespaceFormatter();
		FormattingResult result = formatter.apply(context);

		// Should not generate edits for correctly spaced code
		assertThat(result.isEmpty()).isTrue();
	}

	private static CompilationUnitNode createASTWithBinaryExpression(String sourceText,
		int leftStart, int leftEnd, String operator, int rightStart, int rightEnd)
	{
		// Create literal nodes for left and right operands
		SourcePosition leftPos1 = new SourcePosition(1, leftStart + 1);
		SourcePosition leftPos2 = new SourcePosition(1, leftEnd + 1);
		SourceRange leftRange = new SourceRange(leftPos1, leftPos2);

		NumberLiteralNode leftNode = new NumberLiteralNode(leftRange, List.of(), List.of(),
			WhitespaceInfo.none(), FormattingHints.defaults(), Optional.empty());

		SourcePosition rightPos1 = new SourcePosition(1, rightStart + 1);
		SourcePosition rightPos2 = new SourcePosition(1, rightEnd + 1);
		SourceRange rightRange = new SourceRange(rightPos1, rightPos2);

		NumberLiteralNode rightNode = new NumberLiteralNode(rightRange, List.of(), List.of(),
			WhitespaceInfo.none(), FormattingHints.defaults(), Optional.empty());

		// Create binary expression node
		SourcePosition exprStart = new SourcePosition(1, 1);
		SourcePosition exprEnd = new SourcePosition(1, sourceText.length() + 1);
		SourceRange exprRange = new SourceRange(exprStart, exprEnd);

		BinaryExpressionNode binaryExpr = new BinaryExpressionNode(exprRange, List.of(),
			List.of(), WhitespaceInfo.none(), FormattingHints.defaults(), Optional.empty(),
			leftNode, operator, rightNode);

		// Wrap in compilation unit
		return new CompilationUnitNode(exprRange, List.of(), List.of(), WhitespaceInfo.none(),
			FormattingHints.defaults(), Optional.empty(), Optional.empty(), List.of(),
			List.of(binaryExpr));
	}

	private static FormattingContext createContext(CompilationUnitNode root, String sourceText)
	{
		return new FormattingContext(root, sourceText, java.nio.file.Path.of("test.java"),
			WhitespaceConfiguration.createDefault(),
			Set.of("io.github.cowwoc.styler.rules.Whitespace"), Map.of());
	}
}
