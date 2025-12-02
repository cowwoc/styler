package io.github.cowwoc.styler.formatter.linelength.internal;

import io.github.cowwoc.styler.formatter.TransformationContext;
import io.github.cowwoc.styler.formatter.linelength.LineLengthConfiguration;
import io.github.cowwoc.styler.formatter.linelength.WrapStyle;

import java.util.ArrayList;
import java.util.List;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;

/**
 * Wraps long lines using AST context-aware break point detection.
 * Uses ContextDetector to identify semantic wrapping contexts and applies
 * context-specific wrapping rules.
 * <p>
 * <b>Thread-safety</b>: This class is thread-safe.
 */
public final class LineWrapper
{
	private final ContextDetector contextDetector;
	private final LineLengthConfiguration config;

	/**
	 * Creates a line wrapper with AST context detection.
	 *
	 * @param contextDetector AST-based context detector for semantic analysis
	 * @param transformationContext the transformation context with source access
	 * @param config the line length configuration
	 * @throws NullPointerException if any parameter is null
	 */
	public LineWrapper(ContextDetector contextDetector, TransformationContext transformationContext,
		LineLengthConfiguration config)
	{
		requireThat(contextDetector, "contextDetector").isNotNull();
		requireThat(transformationContext, "transformationContext").isNotNull();
		requireThat(config, "config").isNotNull();

		this.contextDetector = contextDetector;
		this.config = config;
	}

	/**
	 * Finds potential break points in a line based on AST context.
	 *
	 * @param line the line content
	 * @param lineStartPosition the character position of line start in source
	 * @return list of potential break points (character positions)
	 */
	public List<Integer> findBreakPoints(String line, int lineStartPosition)
	{
		requireThat(line, "line").isNotNull();
		requireThat(lineStartPosition, "lineStartPosition").isGreaterThanOrEqualTo(0);

		List<Integer> breakPoints = new ArrayList<>();

		// Scan line for potential break characters
		for (int i = 0; i < line.length(); ++i)
		{
			char currentChar = line.charAt(i);
			int absolutePosition = lineStartPosition + i;

			// Detect context at this position
			WrapContext context = contextDetector.detectContext(absolutePosition);

			// Check if this character is a valid break point based on context
			if (isBreakCharacter(currentChar, context))
			{
				breakPoints.add(i);
			}
		}

		return breakPoints;
	}

	/**
	 * Checks if a character is a valid break point in the given context.
	 *
	 * @param charAtPosition the character to check
	 * @param context the wrapping context
	 * @return true if the character is a valid break point
	 */
	private boolean isBreakCharacter(char charAtPosition, WrapContext context)
	{
		return switch (context)
		{
			case METHOD_CHAIN ->
				charAtPosition == '.' && shouldBreakAtDot();
			case METHOD_ARGUMENTS ->
				charAtPosition == ',' && shouldBreakAtComma();
			case METHOD_PARAMETERS ->
				charAtPosition == ',' && shouldBreakAtComma();
			case BINARY_EXPRESSION ->
				isOperatorChar(charAtPosition) && shouldBreakAtOperator();
			case TERNARY_EXPRESSION ->
				(charAtPosition == '?' || charAtPosition == ':') && shouldBreakAtOperator();
			case ARRAY_INITIALIZER ->
				charAtPosition == ',' && shouldBreakAtComma();
			case ANNOTATION_ARGUMENTS ->
				charAtPosition == ',' && shouldBreakAtComma();
			case GENERIC_TYPE_ARGS ->
				charAtPosition == ',' && shouldBreakAtComma();
			case QUALIFIED_NAME, NOT_WRAPPABLE ->
				false;
		};
	}

	/**
	 * Checks if a character is an operator character.
	 *
	 * @param charAtPosition the character to check
	 * @return true if the character is an operator
	 */
	private boolean isOperatorChar(char charAtPosition)
	{
		return switch (charAtPosition)
		{
			case '+', '-', '*', '/', '%', '&', '|', '^', '<', '>', '=' -> true;
			default -> false;
		};
	}

	/**
	 * Determines if breaks should occur at dots based on configuration.
	 *
	 * @return true if wrapping at dots is enabled
	 */
	private boolean shouldBreakAtDot()
	{
		return config.methodChainWrap() != WrapStyle.NEVER;
	}

	/**
	 * Determines if breaks should occur at commas based on configuration.
	 *
	 * @return true if wrapping at commas is enabled
	 */
	private boolean shouldBreakAtComma()
	{
		return config.methodArgumentsWrap() != WrapStyle.NEVER;
	}

	/**
	 * Determines if breaks should occur at operators based on configuration.
	 *
	 * @return true if wrapping at operators is enabled
	 */
	private boolean shouldBreakAtOperator()
	{
		return config.binaryExpressionWrap() != WrapStyle.NEVER;
	}

	/**
	 * Applies wrapping to a long line based on context-aware break points.
	 *
	 * @param line the line to wrap
	 * @param lineStartPosition the character position of line start
	 * @return the wrapped line (may contain newlines)
	 */
	public String wrapLine(String line, int lineStartPosition)
	{
		requireThat(line, "line").isNotNull();
		requireThat(lineStartPosition, "lineStartPosition").isGreaterThanOrEqualTo(0);

		int visualLength = calculateVisualLength(line);
		if (visualLength <= config.maxLineLength())
			return line;

		// Find break points
		List<Integer> breakPoints = findBreakPoints(line, lineStartPosition);
		if (breakPoints.isEmpty())
			return line;  // No valid break points found

		// Select best break point (closest to max length that doesn't exceed it)
		int bestBreakPoint = selectBestBreakPoint(line, breakPoints);
		if (bestBreakPoint < 0)
			return line;  // No suitable break point

		// Determine wrap style for the break point context
		int absolutePosition = lineStartPosition + bestBreakPoint;
		WrapContext context = contextDetector.detectContext(absolutePosition);
		WrapStyle wrapStyle = getWrapStyleForContext(context);

		// Apply wrapping based on style
		return applyWrap(line, bestBreakPoint, wrapStyle, lineStartPosition);
	}

	/**
	 * Calculates the visual length of a line after tab expansion.
	 *
	 * @param line the line to measure
	 * @return the visual length
	 */
	private int calculateVisualLength(String line)
	{
		int length = 0;
		for (int i = 0; i < line.length(); ++i)
		{
			char currentChar = line.charAt(i);
			if (currentChar == '\t')
				length = length + config.tabWidth() - (length % config.tabWidth());
			else
				++length;
		}
		return length;
	}

	/**
	 * Selects the best break point from the available options.
	 * Prefers break points that result in lines not exceeding max length.
	 *
	 * @param line the line being wrapped
	 * @param breakPoints available break points
	 * @return the best break point index, or -1 if none suitable
	 */
	private int selectBestBreakPoint(String line, List<Integer> breakPoints)
	{
		int maxLength = config.maxLineLength();
		int bestBreakPoint = -1;

		// Find the rightmost break point that keeps the first part under max length
		for (int breakPoint : breakPoints)
		{
			String firstPart = line.substring(0, breakPoint + 1);
			int visualLength = calculateVisualLength(firstPart);

			if (visualLength <= maxLength)
				bestBreakPoint = breakPoint;
			else if (bestBreakPoint < 0)
				// If no break point keeps us under, use the first one
				bestBreakPoint = breakPoint;
		}

		return bestBreakPoint;
	}

	/**
	 * Gets the wrap style for a given context.
	 *
	 * @param context the wrap context
	 * @return the configured wrap style
	 */
	private WrapStyle getWrapStyleForContext(WrapContext context)
	{
		return switch (context)
		{
			case METHOD_CHAIN -> config.methodChainWrap();
			case METHOD_ARGUMENTS -> config.methodArgumentsWrap();
			case METHOD_PARAMETERS -> config.methodParametersWrap();
			case BINARY_EXPRESSION -> config.binaryExpressionWrap();
			case TERNARY_EXPRESSION -> config.ternaryExpressionWrap();
			case ARRAY_INITIALIZER -> config.arrayInitializerWrap();
			case ANNOTATION_ARGUMENTS -> config.annotationArgumentsWrap();
			case GENERIC_TYPE_ARGS -> config.genericTypeArgsWrap();
			case QUALIFIED_NAME, NOT_WRAPPABLE -> WrapStyle.NEVER;
		};
	}

	/**
	 * Applies the wrap at the specified break point.
	 *
	 * @param line the line to wrap
	 * @param breakPoint the character index of the break point
	 * @param wrapStyle the wrap style to apply
	 * @param lineStartPosition the original line start position
	 * @return the wrapped line
	 */
	private String applyWrap(String line, int breakPoint, WrapStyle wrapStyle,
		int lineStartPosition)
	{
		if (wrapStyle == WrapStyle.NEVER)
			return line;

		String indentation = createIndentation();
		String firstPart;
		String remainingContent;
		int remainingStartPosition;

		if (wrapStyle == WrapStyle.BEFORE)
		{
			// Break before the character
			firstPart = line.substring(0, breakPoint).stripTrailing();
			remainingContent = line.substring(breakPoint).stripLeading();
			remainingStartPosition = lineStartPosition + breakPoint;
		}
		else
		{
			// AFTER or ONE_PER_LINE - break after the character
			firstPart = line.substring(0, breakPoint + 1).stripTrailing();
			remainingContent = line.substring(breakPoint + 1).stripLeading();
			remainingStartPosition = lineStartPosition + breakPoint + 1;
		}

		// Skip whitespace in position tracking
		int substringStart;
		if (wrapStyle == WrapStyle.BEFORE)
			substringStart = breakPoint;
		else
			substringStart = breakPoint + 1;
		int whitespaceSkipped = line.substring(substringStart).length() - remainingContent.length();
		remainingStartPosition += whitespaceSkipped;

		// Recursively wrap remaining content (without indentation for position tracking)
		String wrappedRemaining = wrapLineInternal(remainingContent, remainingStartPosition, indentation);

		return firstPart + "\n" + indentation + wrappedRemaining;
	}

	/**
	 * Internal wrapping that tracks original source positions.
	 *
	 * @param content the content to wrap (without leading indentation)
	 * @param contentStartPosition position in original source
	 * @param indentation indentation to add to continuation lines
	 * @return the wrapped content
	 */
	private String wrapLineInternal(String content, int contentStartPosition, String indentation)
	{
		int indentLength = calculateVisualLength(indentation);
		int effectiveMaxLength = config.maxLineLength() - indentLength;

		int visualLength = calculateVisualLength(content);
		if (visualLength <= effectiveMaxLength)
			return content;

		// Find break points based on original source positions
		List<Integer> breakPoints = findBreakPoints(content, contentStartPosition);
		if (breakPoints.isEmpty())
			return content;

		// Select best break point considering indentation
		int bestBreakPoint = selectBestBreakPointWithIndent(content, breakPoints, indentLength);
		if (bestBreakPoint < 0)
			return content;

		// Determine wrap style
		int absolutePosition = contentStartPosition + bestBreakPoint;
		WrapContext context = contextDetector.detectContext(absolutePosition);
		WrapStyle wrapStyle = getWrapStyleForContext(context);

		if (wrapStyle == WrapStyle.NEVER)
			return content;

		String firstPart;
		String remainingContent;
		int remainingStartPosition;

		if (wrapStyle == WrapStyle.BEFORE)
		{
			firstPart = content.substring(0, bestBreakPoint).stripTrailing();
			remainingContent = content.substring(bestBreakPoint).stripLeading();
			remainingStartPosition = contentStartPosition + bestBreakPoint;
		}
		else
		{
			firstPart = content.substring(0, bestBreakPoint + 1).stripTrailing();
			remainingContent = content.substring(bestBreakPoint + 1).stripLeading();
			remainingStartPosition = contentStartPosition + bestBreakPoint + 1;
		}

		// Skip whitespace in position tracking
		int contentSubstringStart;
		if (wrapStyle == WrapStyle.BEFORE)
			contentSubstringStart = bestBreakPoint;
		else
			contentSubstringStart = bestBreakPoint + 1;
		int whitespaceSkipped = content.substring(contentSubstringStart).length() - remainingContent.length();
		remainingStartPosition += whitespaceSkipped;

		// Recursively wrap remaining
		String wrappedRemaining = wrapLineInternal(remainingContent, remainingStartPosition, indentation);

		return firstPart + "\n" + indentation + wrappedRemaining;
	}

	/**
	 * Selects best break point accounting for indentation on continuation lines.
	 *
	 * @param content the content being wrapped
	 * @param breakPoints available break points
	 * @param indentLength visual length of indentation
	 * @return the best break point index, or -1 if none suitable
	 */
	private int selectBestBreakPointWithIndent(String content, List<Integer> breakPoints, int indentLength)
	{
		int effectiveMaxLength = config.maxLineLength() - indentLength;
		int bestBreakPoint = -1;

		for (int breakPoint : breakPoints)
		{
			String firstPart = content.substring(0, breakPoint + 1);
			int visualLength = calculateVisualLength(firstPart);

			if (visualLength <= effectiveMaxLength)
				bestBreakPoint = breakPoint;
			else if (bestBreakPoint < 0)
				bestBreakPoint = breakPoint;
		}

		return bestBreakPoint;
	}

	/**
	 * Creates the indentation string for continuation lines.
	 *
	 * @return the indentation string
	 */
	private String createIndentation()
	{
		return "\t".repeat(config.indentContinuationLines());
	}
}
