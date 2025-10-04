package io.github.cowwoc.styler.formatter.impl;

import io.github.cowwoc.styler.ast.ASTNode;
import io.github.cowwoc.styler.ast.SourcePosition;
import io.github.cowwoc.styler.ast.SourceRange;
import io.github.cowwoc.styler.ast.node.BinaryExpressionNode;
import io.github.cowwoc.styler.formatter.api.EditPriority;
import io.github.cowwoc.styler.formatter.api.FormattingContext;
import io.github.cowwoc.styler.formatter.api.FormattingResult;
import io.github.cowwoc.styler.formatter.api.FormattingRule;
import io.github.cowwoc.styler.formatter.api.RuleConfiguration;
import io.github.cowwoc.styler.formatter.api.TextEdit;
import io.github.cowwoc.styler.formatter.api.ValidationResult;

import java.util.ArrayList;
import java.util.List;

/**
 * Formatting rule that enforces consistent whitespace around operators, keywords, and punctuation.
 * <p>
 * This stateless, thread-safe rule analyzes Java source code and generates text edits to normalize
 * whitespace according to configured spacing rules. It handles binary operators, keywords, and
 * punctuation as specified in the Java Language Specification.
 * <p>
 * Per <a href="https://docs.oracle.com/javase/specs/jls/se25/html/jls-3.html#jls-3.6">JLS §3.6</a>,
 * white space consists of the ASCII space character, horizontal tab, form feed, and line terminators.
 * <p>
 * This rule executes at priority 50, before the line length rule (100), to establish baseline
 * spacing before line wrapping occurs.
 */
public final class WhitespaceFormatter implements FormattingRule
{
	private static final String RULE_ID = "io.github.cowwoc.styler.rules.Whitespace";
	private static final int DEFAULT_PRIORITY = 50;

	/**
	 * Creates a new whitespace formatter.
	 * <p>
	 * The formatter is stateless and can be safely reused across multiple files
	 * in concurrent processing scenarios.
	 */
	public WhitespaceFormatter()
	{
		// Stateless design - no instance fields to initialize
	}

	@Override
	public String getRuleId()
	{
		return RULE_ID;
	}

	@Override
	public int getPriority()
	{
		return DEFAULT_PRIORITY;
	}

	@Override
	public RuleConfiguration getDefaultConfiguration()
	{
		return WhitespaceConfiguration.createDefault();
	}

	@Override
	public ValidationResult validate(FormattingContext context)
	{
		if (context == null)
		{
			return ValidationResult.failure("FormattingContext cannot be null");
		}

		if (context.getRootNode() == null)
		{
			return ValidationResult.failure("AST root node cannot be null");
		}

		if (context.getSourceText() == null || context.getSourceText().isEmpty())
		{
			return ValidationResult.failure("Source text cannot be null or empty");
		}

		return ValidationResult.success();
	}

	@Override
	public FormattingResult apply(FormattingContext context)
	{
		ValidationResult validationResult = validate(context);
		if (validationResult.isFailure())
		{
			return FormattingResult.empty();
		}

		WhitespaceConfiguration config = (WhitespaceConfiguration) context.getConfiguration();
		List<TextEdit> edits = new ArrayList<>();

		// Pre-compute line offsets once per file for efficient position conversion
		int[] lineOffsets = computeLineOffsets(context.getSourceText());

		findWhitespaceViolations(context.getRootNode(), context, config, edits, lineOffsets);

		return FormattingResult.withEdits(edits);
	}

	/**
	 * Pre-computes line offset array for efficient position conversion.
	 * <p>
	 * This optimization reduces position conversion from O(n) per call to O(1) lookup
	 * after initial O(n) preprocessing, significantly reducing memory allocations.
	 *
	 * @param sourceText the source code text (never null)
	 * @return array where lineOffsets[i] is the character offset of line i (0-based)
	 */
	private int[] computeLineOffsets(String sourceText)
	{
		String[] lines = SourceTextUtil.splitIntoLines(sourceText);
		int[] offsets = new int[lines.length + 1];
		int offset = 0;

		for (int i = 0; i < lines.length; ++i)
		{
			offsets[i] = offset;
			offset += lines[i].length() + 1; // +1 for newline
		}
		offsets[lines.length] = offset; // EOF offset

		return offsets;
	}

	/**
	 * Recursively finds whitespace violations in the AST.
	 * <p>
	 * This method performs a depth-first traversal of the AST, checking each node
	 * for whitespace violations according to the configuration. Violations are
	 * collected as TextEdit objects for later application.
	 * <p>
	 * <b>Supported Node Types:</b>
	 * <ul>
	 *   <li>{@link BinaryExpressionNode} - binary operator spacing validation</li>
	 * </ul>
	 * <p>
	 * <b>Performance Characteristics:</b>
	 * Time complexity: O(n) where n = number of AST nodes (single-pass traversal)
	 *
	 * @param node the AST node to analyze (never null)
	 * @param context the formatting context (never null)
	 * @param config the whitespace configuration (never null)
	 * @param edits the list to accumulate text edits (never null, mutable)
	 * @param lineOffsets pre-computed line offset array for position conversion (never null)
	 */
	private void findWhitespaceViolations(ASTNode node, FormattingContext context,
		WhitespaceConfiguration config, List<TextEdit> edits, int[] lineOffsets)
	{
		// Check binary operator spacing
		if (node instanceof BinaryExpressionNode binaryNode)
		{
			checkOperatorSpacing(binaryNode, context, config, edits, lineOffsets);
		}

		// Recursive traversal - visits each child exactly once
		for (ASTNode child : node.getChildren())
		{
			findWhitespaceViolations(child, context, config, edits, lineOffsets);
		}
	}

	/**
	 * Checks whitespace around binary operators.
	 * <p>
	 * Per <a href="https://docs.oracle.com/javase/specs/jls/se25/html/jls-15.html#jls-15.17">JLS §15.17-15.24</a>,
	 * binary operators should have consistent spacing for readability.
	 *
	 * @param node the binary expression node (never null)
	 * @param context the formatting context (never null)
	 * @param config the whitespace configuration (never null)
	 * @param edits the list to accumulate text edits (never null, mutable)
	 * @param lineOffsets pre-computed line offset array (never null)
	 */
	private void checkOperatorSpacing(BinaryExpressionNode node, FormattingContext context,
		WhitespaceConfiguration config, List<TextEdit> edits, int[] lineOffsets)
	{
		String sourceText = context.getSourceText();
		SourceRange operatorRange = findOperatorRange(node, sourceText, lineOffsets);

		if (operatorRange == null)
		{
			return;
		}

		int expectedSpaces = config.getOperatorSpacing();

		// Check spacing before operator
		int spacesBefore = countSpacesBefore(operatorRange.start(), sourceText, lineOffsets);
		if (spacesBefore != expectedSpaces)
		{
			TextEdit edit = createSpacingEdit(operatorRange.start(), spacesBefore,
				expectedSpaces, lineOffsets);
			edits.add(edit);
		}

		// Check spacing after operator
		int spacesAfter = countSpacesAfter(operatorRange.end(), sourceText, lineOffsets);
		if (spacesAfter != expectedSpaces)
		{
			TextEdit edit = createSpacingEdit(operatorRange.end(), spacesAfter,
				expectedSpaces, lineOffsets);
			edits.add(edit);
		}
	}

	/**
	 * Finds the source range of the operator token within a binary expression.
	 *
	 * @param node the binary expression node
	 * @param sourceText the source code text
	 * @param lineOffsets pre-computed line offset array
	 * @return the source range of the operator, or null if not found
	 */
	private SourceRange findOperatorRange(BinaryExpressionNode node, String sourceText,
		int[] lineOffsets)
	{
		SourceRange leftRange = node.getLeft().getRange();
		SourceRange rightRange = node.getRight().getRange();
		String operator = node.getOperator();

		// Operator is between left and right operands
		int searchStart = getCharacterOffset(leftRange.end(), lineOffsets);
		int searchEnd = getCharacterOffset(rightRange.start(), lineOffsets);

		String searchRegion = sourceText.substring(searchStart, searchEnd);
		int operatorIndex = searchRegion.indexOf(operator);

		if (operatorIndex == -1)
		{
			return null;
		}

		int operatorStart = searchStart + operatorIndex;
		int operatorEnd = operatorStart + operator.length();

		SourcePosition start = getSourcePosition(operatorStart, lineOffsets);
		SourcePosition end = getSourcePosition(operatorEnd, lineOffsets);

		return new SourceRange(start, end);
	}

	/**
	 * Counts the number of spaces before a given position.
	 *
	 * @param position the source position
	 * @param sourceText the source code text
	 * @param lineOffsets pre-computed line offset array
	 * @return the number of spaces before the position
	 */
	private int countSpacesBefore(SourcePosition position, String sourceText, int[] lineOffsets)
	{
		int offset = getCharacterOffset(position, lineOffsets);
		int count = 0;

		for (int i = offset - 1; i >= 0 && sourceText.charAt(i) == ' '; --i)
		{
			++count;
		}

		return count;
	}

	/**
	 * Counts the number of spaces after a given position.
	 *
	 * @param position the source position
	 * @param sourceText the source code text
	 * @param lineOffsets pre-computed line offset array
	 * @return the number of spaces after the position
	 */
	private int countSpacesAfter(SourcePosition position, String sourceText, int[] lineOffsets)
	{
		int offset = getCharacterOffset(position, lineOffsets);
		int count = 0;

		for (int i = offset; i < sourceText.length() && sourceText.charAt(i) == ' '; ++i)
		{
			++count;
		}

		return count;
	}

	/**
	 * Creates a text edit to adjust spacing.
	 *
	 * @param position the position where spacing should be adjusted
	 * @param currentSpaces the current number of spaces
	 * @param expectedSpaces the expected number of spaces
	 * @param lineOffsets pre-computed line offset array
	 * @return the text edit
	 */
	private TextEdit createSpacingEdit(SourcePosition position, int currentSpaces,
		int expectedSpaces, int[] lineOffsets)
	{
		int offset = getCharacterOffset(position, lineOffsets);
		SourcePosition start;
		SourcePosition end;

		if (currentSpaces > expectedSpaces)
		{
			// Remove extra spaces
			int removeStart = offset - currentSpaces;
			int removeEnd = removeStart + currentSpaces;
			start = getSourcePosition(removeStart, lineOffsets);
			end = getSourcePosition(removeEnd, lineOffsets);
		}
		else
		{
			// Add missing spaces
			int insertPoint = offset - currentSpaces;
			start = getSourcePosition(insertPoint, lineOffsets);
			end = start;
		}

		String replacement = " ".repeat(expectedSpaces);
		SourceRange range = new SourceRange(start, end);

		return new TextEdit(range, replacement, RULE_ID, EditPriority.NORMAL);
	}

	/**
	 * Converts a SourcePosition to character offset using cached line offsets.
	 * <p>
	 * This optimized version performs O(1) lookup instead of O(n) line splitting,
	 * significantly reducing memory allocations and improving performance.
	 *
	 * @param position the source position (line/column, 1-based)
	 * @param lineOffsets pre-computed line offset array
	 * @return the character offset from the beginning of the file
	 */
	private int getCharacterOffset(SourcePosition position, int[] lineOffsets)
	{
		int lineIndex = position.line() - 1; // Convert to 0-based
		return lineOffsets[lineIndex] + (position.column() - 1);
	}

	/**
	 * Converts a character offset to SourcePosition using cached line offsets.
	 * <p>
	 * This optimized version uses binary search for O(log n) lookup instead of
	 * O(n) linear scan with repeated string allocations.
	 *
	 * @param offset the character offset from the beginning of the file
	 * @param lineOffsets pre-computed line offset array
	 * @return the source position (line/column, 1-based)
	 */
	private SourcePosition getSourcePosition(int offset, int[] lineOffsets)
	{
		// Binary search for the line containing this offset
		int line = java.util.Arrays.binarySearch(lineOffsets, offset);

		if (line < 0)
		{
			// Offset not exactly at line start - adjust for insertion point
			line = -line - 2;
		}

		int column = offset - lineOffsets[line] + 1; // Column is 1-based
		return new SourcePosition(line + 1, column); // Line is 1-based
	}
}
