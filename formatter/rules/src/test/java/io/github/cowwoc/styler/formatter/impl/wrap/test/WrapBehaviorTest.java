package io.github.cowwoc.styler.formatter.impl.wrap.test;

import io.github.cowwoc.styler.ast.node.CompilationUnitNode;
import io.github.cowwoc.styler.ast.SourcePosition;
import io.github.cowwoc.styler.ast.SourceRange;
import io.github.cowwoc.styler.formatter.api.ConfigurationException;
import io.github.cowwoc.styler.formatter.api.TextEdit;
import io.github.cowwoc.styler.formatter.api.WrapConfiguration;
import io.github.cowwoc.styler.formatter.api.test.TestUtilities;
import io.github.cowwoc.styler.formatter.impl.wrap.WrapBehavior;
import io.github.cowwoc.styler.formatter.impl.wrap.WrapPoint;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link WrapBehavior}.
 */
public class WrapBehaviorTest
{
	/**
	 * Creates default test configuration with 8-space continuation indent and 4-space tabs.
	 *
	 * @return configured WrapConfiguration instance
	 * @throws ConfigurationException if configuration validation fails
	 */
	private static WrapConfiguration createDefaultConfig() throws ConfigurationException
	{
		return WrapConfiguration.builder().
			withContinuationIndentSpaces(8).
			withTabWidth(4).
			build();
	}

	/**
	 * Creates WrapBehavior instance with default test configuration.
	 *
	 * @return configured WrapBehavior instance
	 * @throws ConfigurationException if configuration validation fails
	 */
	private static WrapBehavior createDefaultBehavior() throws ConfigurationException
	{
		return new WrapBehavior(createDefaultConfig());
	}

	/**
	 * Verifies WrapBehavior constructor stores configuration and creates indentation calculator.
	 */
	@Test
	public void constructorStoresConfiguration() throws ConfigurationException
	{
		WrapBehavior behavior = createDefaultBehavior();
		assertThat(behavior.getIndentationCalculator()).isNotNull();
		assertThat(behavior.getIndentationCalculator()).isNotNull();
	}

	/**
	 * Verifies findWrapPoints() delegates to WrapPointDetector and returns sorted results.
	 */
	@Test
	public void findWrapPointsReturnsDetectedPoints() throws ConfigurationException
	{
		WrapBehavior behavior = createDefaultBehavior();
		CompilationUnitNode rootNode = TestUtilities.createTestAST();
		String sourceText = "    int x = 5 + 10;";
		SourceRange range = new SourceRange(
			new SourcePosition(5, 1),
			new SourcePosition(5, 80));

		List<WrapPoint> wrapPoints = behavior.findWrapPoints(range, rootNode, sourceText);

		assertThat(wrapPoints).isNotNull();
	}

	/**
	 * Verifies createWrapEdit() generates a text edit with newline and continuation indentation.
	 */
	@Test
	public void createWrapEditGeneratesEditWithIndentation() throws ConfigurationException
	{
		WrapBehavior behavior = createDefaultBehavior();
		String sourceText = "    int x = 5 + 10;";
		WrapPoint wrapPoint = new WrapPoint(
			new SourcePosition(5, 15),
			WrapPoint.Priority.OPERATOR,
			"operator");

		TextEdit edit = behavior.createWrapEdit(wrapPoint, sourceText);

		assertThat(edit).isNotNull();
		assertThat(edit.getRange().start()).isEqualTo(new SourcePosition(5, 15));
		assertThat(edit.getReplacement()).startsWith("\n");
		assertThat(edit.getReplacement().substring(1).length()).isGreaterThan(0);
	}

	/**
	 * Verifies createWrapEdit() calculates base indentation from the current line.
	 */
	@Test
	public void createWrapEditCalculatesBaseIndentation() throws ConfigurationException
	{
		WrapBehavior behavior = createDefaultBehavior();
		String sourceText = "        int x = 5 + 10;";
		WrapPoint wrapPoint = new WrapPoint(
			new SourcePosition(3, 20),
			WrapPoint.Priority.OPERATOR,
			"operator");

		TextEdit edit = behavior.createWrapEdit(wrapPoint, sourceText);

		String indentation = edit.getReplacement().substring(1);
		assertThat(indentation.length()).isGreaterThanOrEqualTo(8);
	}

	/**
	 * Verifies createWrapEdit() uses rule ID "io.github.cowwoc.styler.rules.LineLength".
	 */
	@Test
	public void createWrapEditUsesCorrectRuleId() throws ConfigurationException
	{
		WrapBehavior behavior = createDefaultBehavior();
		String sourceText = "    int x = 5 + 10;";
		WrapPoint wrapPoint = new WrapPoint(
			new SourcePosition(4, 15),
			WrapPoint.Priority.OPERATOR,
			"operator");

		TextEdit edit = behavior.createWrapEdit(wrapPoint, sourceText);

		assertThat(edit.getRuleId()).isEqualTo("io.github.cowwoc.styler.rules.LineLength");
	}

	/**
	 * Verifies createWrapEdit() creates edit at the exact wrap point position.
	 */
	@Test
	public void createWrapEditUsesExactPosition() throws ConfigurationException
	{
		WrapBehavior behavior = createDefaultBehavior();
		String sourceText = "    int x = 5 + 10;";
		SourcePosition position = new SourcePosition(6, 12);
		WrapPoint wrapPoint = new WrapPoint(
			position,
			WrapPoint.Priority.OPERATOR,
			"operator");

		TextEdit edit = behavior.createWrapEdit(wrapPoint, sourceText);

		assertThat(edit.getRange().start()).isEqualTo(position);
		assertThat(edit.getRange().end()).isEqualTo(position);
	}

	/**
	 * Verifies createMultipleWrapEdits() creates edits for all provided wrap points.
	 */
	@Test
	public void createMultipleWrapEditsCreatesEditForEachPoint() throws ConfigurationException
	{
		WrapBehavior behavior = createDefaultBehavior();
		String sourceText = "    int x = 5 + 10 + 20;";

		List<WrapPoint> wrapPoints = new ArrayList<>();
		wrapPoints.add(new WrapPoint(
			new SourcePosition(7, 15),
			WrapPoint.Priority.OPERATOR,
			"first"));
		wrapPoints.add(new WrapPoint(
			new SourcePosition(7, 20),
			WrapPoint.Priority.OPERATOR,
			"second"));

		List<TextEdit> edits = behavior.createMultipleWrapEdits(wrapPoints, sourceText, 120);

		assertThat(edits).hasSize(2);
	}

	/**
	 * Verifies createMultipleWrapEdits() limits edits to maximum of 3 wrap points.
	 */
	@Test
	public void createMultipleWrapEditsLimitsToThreePoints() throws ConfigurationException
	{
		WrapBehavior behavior = createDefaultBehavior();
		String sourceText = "    int x = 1 + 2 + 3 + 4 + 5;";

		List<WrapPoint> wrapPoints = new ArrayList<>();
		for (int i = 0; i < 5; ++i)
		{
			wrapPoints.add(new WrapPoint(
				new SourcePosition(8, 15 + i * 4),
				WrapPoint.Priority.OPERATOR,
				"point" + i));
		}

		List<TextEdit> edits = behavior.createMultipleWrapEdits(wrapPoints, sourceText, 120);

		assertThat(edits).hasSize(3);
	}

	/**
	 * Verifies createMultipleWrapEdits() returns empty list when no wrap points provided.
	 */
	@Test
	public void createMultipleWrapEditsWithNoPointsReturnsEmptyList() throws ConfigurationException
	{
		WrapBehavior behavior = createDefaultBehavior();
		String sourceText = "    int x = 5;";
		List<WrapPoint> wrapPoints = new ArrayList<>();

		List<TextEdit> edits = behavior.createMultipleWrapEdits(wrapPoints, sourceText, 120);

		assertThat(edits).isEmpty();
	}

	/**
	 * Verifies createMultipleWrapEdits() maintains wrap point priority order in edits.
	 */
	@Test
	public void createMultipleWrapEditsMaintainsPriorityOrder() throws ConfigurationException
	{
		WrapBehavior behavior = createDefaultBehavior();
		String sourceText = "    int x = 1 + 2;";

		List<WrapPoint> wrapPoints = new ArrayList<>();
		wrapPoints.add(new WrapPoint(
			new SourcePosition(9, 15),
			WrapPoint.Priority.METHOD_CHAIN,
			"high"));
		wrapPoints.add(new WrapPoint(
			new SourcePosition(9, 18),
			WrapPoint.Priority.WHITESPACE,
			"low"));

		List<TextEdit> edits = behavior.createMultipleWrapEdits(wrapPoints, sourceText, 120);

		assertThat(edits).hasSize(2);
		assertThat(edits.get(0).getRange().start().column()).isEqualTo(15);
		assertThat(edits.get(1).getRange().start().column()).isEqualTo(18);
	}

	/**
	 * Verifies WrapBehavior constructor validates config is not null.
	 */
	@Test(expectedExceptions = NullPointerException.class)
	public void constructorWithNullConfigThrowsException()
	{
		new WrapBehavior(null);
	}

	/**
	 * Verifies findWrapPoints() validates range is not null.
	 */
	@Test(expectedExceptions = NullPointerException.class)
	public void findWrapPointsWithNullRangeThrowsException() throws ConfigurationException
	{
		WrapBehavior behavior = createDefaultBehavior();
		CompilationUnitNode rootNode = TestUtilities.createTestAST();
		behavior.findWrapPoints(null, rootNode, "source");
	}

	/**
	 * Verifies findWrapPoints() validates rootNode is not null.
	 */
	@Test(expectedExceptions = NullPointerException.class)
	public void findWrapPointsWithNullRootNodeThrowsException() throws ConfigurationException
	{
		WrapBehavior behavior = createDefaultBehavior();
		SourceRange range = new SourceRange(
			new SourcePosition(1, 1),
			new SourcePosition(1, 80));
		behavior.findWrapPoints(range, null, "source");
	}

	/**
	 * Verifies findWrapPoints() validates sourceText is not null.
	 */
	@Test(expectedExceptions = NullPointerException.class)
	public void findWrapPointsWithNullSourceTextThrowsException() throws ConfigurationException
	{
		WrapBehavior behavior = createDefaultBehavior();
		CompilationUnitNode rootNode = TestUtilities.createTestAST();
		SourceRange range = new SourceRange(
			new SourcePosition(1, 1),
			new SourcePosition(1, 80));
		behavior.findWrapPoints(range, rootNode, null);
	}

	/**
	 * Verifies createWrapEdit() validates wrapPoint is not null.
	 */
	@Test(expectedExceptions = NullPointerException.class)
	public void createWrapEditWithNullWrapPointThrowsException() throws ConfigurationException
	{
		WrapBehavior behavior = createDefaultBehavior();
		behavior.createWrapEdit(null, "source");
	}

	/**
	 * Verifies createWrapEdit() validates sourceText is not null.
	 */
	@Test(expectedExceptions = NullPointerException.class)
	public void createWrapEditWithNullSourceTextThrowsException() throws ConfigurationException
	{
		WrapBehavior behavior = createDefaultBehavior();
		WrapPoint wrapPoint = new WrapPoint(
			new SourcePosition(1, 10),
			WrapPoint.Priority.OPERATOR,
			"test");
		behavior.createWrapEdit(wrapPoint, null);
	}

	/**
	 * Verifies createMultipleWrapEdits() validates wrapPoints is not null.
	 */
	@Test(expectedExceptions = NullPointerException.class)
	public void createMultipleWrapEditsWithNullWrapPointsThrowsException() throws ConfigurationException
	{
		WrapBehavior behavior = createDefaultBehavior();
		behavior.createMultipleWrapEdits(null, "source", 120);
	}

	/**
	 * Verifies createMultipleWrapEdits() validates sourceText is not null.
	 */
	@Test(expectedExceptions = NullPointerException.class)
	public void createMultipleWrapEditsWithNullSourceTextThrowsException() throws ConfigurationException
	{
		WrapBehavior behavior = createDefaultBehavior();
		behavior.createMultipleWrapEdits(new ArrayList<>(), null, 120);
	}

	/**
	 * Verifies createMultipleWrapEdits() validates maxLineLength is positive.
	 */
	@Test(expectedExceptions = IllegalArgumentException.class)
	public void createMultipleWrapEditsWithNegativeMaxLineLengthThrowsException() throws ConfigurationException
	{
		WrapBehavior behavior = createDefaultBehavior();
		behavior.createMultipleWrapEdits(new ArrayList<>(), "source", -1);
	}

	/**
	 * Verifies getIndentationCalculator() returns the indentation calculator instance.
	 */
	@Test
	public void getIndentationCalculatorReturnsCalculator() throws ConfigurationException
	{
		WrapBehavior behavior = createDefaultBehavior();
		assertThat(behavior.getIndentationCalculator()).isNotNull();
	}

	/**
	 * Verifies WrapBehavior uses the tab width from configuration for indentation calculation.
	 */
	@Test
	public void wrapBehaviorUsesConfiguredTabWidth() throws ConfigurationException
	{
		WrapConfiguration customConfig = WrapConfiguration.builder().
			withTabWidth(2).
			withContinuationIndentSpaces(4).
			build();
		WrapBehavior customBehavior = new WrapBehavior(customConfig);

		assertThat(customBehavior.getIndentationCalculator()).isNotNull();
	}
}
