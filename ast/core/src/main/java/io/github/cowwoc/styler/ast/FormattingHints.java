package io.github.cowwoc.styler.ast;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;

import java.util.Map;
import java.util.Optional;

/**
 * Represents formatting hints and preferences for AST nodes.
 * These hints guide formatter behavior and preserve developer formatting intent.
 *
 * @param noFormat whether formatting should be disabled for this node (@formatter:off regions)
 *
 * @param preferredLineLength the preferred line length for this node
 *
 * @param indentStyle the preferred indentation style
 *
 * @param customHints additional custom formatting hints as key-value pairs
 * @throws NullPointerException if {@code preferredLineLength} is null
 * @throws NullPointerException if {@code indentStyle} is null
 * @throws NullPointerException if {@code customHints} is null
 * @throws IllegalArgumentException if {@code preferredLineLength.value} is invalid
 */
public record FormattingHints(boolean noFormat, Optional<Integer> preferredLineLength,
	Optional<IndentationStyle> indentStyle, Map<String, String> customHints)
		{
	/**
	 * Enumeration of indentation styles.
	 */
	public enum IndentationStyle
		{
		/** Use spaces for indentation. */
		SPACES,
		/** Use tabs for indentation. */
		TABS,
		/** Mixed indentation (tabs for major levels, spaces for alignment). */
		MIXED
	}

	/**
	 * Compact constructor validating formatting hint parameters.
	 *
	 * @throws NullPointerException if {@code preferredLineLength}, {@code indentStyle},
	 *         or {@code customHints} is null
	 * @throws IllegalArgumentException if {@code preferredLineLength.value} is invalid
	 */
	public FormattingHints
		{
		requireThat(preferredLineLength, "preferredLineLength").isNotNull();
		requireThat(indentStyle, "indentStyle").isNotNull();
		requireThat(customHints, "customHints").isNotNull();

		// Validate preferred line length if present
		preferredLineLength.ifPresent(length ->
			requireThat(length, "preferredLineLength.value").isGreaterThan(20));
	}

	/**
	 * Creates default formatting hints with no special formatting preferences.
	 *
	 * @return a {@code FormattingHints} with default values
	 */
	public static FormattingHints defaults()
		{
		return new FormattingHints(false, Optional.empty(), Optional.empty(), Map.of());
	}

	/**
	 * Creates formatting hints with formatting disabled.
	 *
	 * @return a {@code FormattingHints} with {@code noFormat} set to {@code true}
	 */
	public static FormattingHints noFormatting()
		{
		return new FormattingHints(true, Optional.empty(), Optional.empty(), Map.of());
	}

	/**
	 * Creates formatting hints with a preferred line length.
	 *
	 * @param lineLength the preferred line length
	 * @return a {@code FormattingHints} with specified line length preference
	 * @throws IllegalArgumentException if {@code lineLength} is invalid
	 */
	public static FormattingHints withLineLength(int lineLength)
		{
		requireThat(lineLength, "lineLength").isGreaterThan(20);
		return new FormattingHints(false, Optional.of(lineLength), Optional.empty(), Map.of());
	}

	/**
	 * Creates formatting hints with a preferred indentation style.
	 *
	 * @param style the preferred indentation style
	 * @return a {@code FormattingHints} with specified indentation style
	 * @throws NullPointerException if {@code style} is null
	 */
	public static FormattingHints withIndentStyle(IndentationStyle style)
		{
		requireThat(style, "style").isNotNull();
		return new FormattingHints(false, Optional.empty(), Optional.of(style), Map.of());
	}

	/**
	 * Creates a copy with formatting disabled.
	 *
	 * @return a new {@code FormattingHints} with {@code noFormat} set to {@code true}
	 */
	public FormattingHints withNoFormat()
		{
		return new FormattingHints(true, preferredLineLength, indentStyle, customHints);
	}

	/**
	 * Creates a copy with an additional custom hint.
	 *
	 * @param key the hint key
	 * @param value the hint value
	 * @return a new {@code FormattingHints} with the additional custom hint
	 * @throws NullPointerException if {@code key} is null
	 * @throws NullPointerException if {@code value} is null
	 * @throws IllegalArgumentException if {@code key} is invalid
	 */
	public FormattingHints withCustomHint(String key, String value)
		{
		requireThat(key, "key").isNotNull().isNotBlank();
		requireThat(value, "value").isNotNull();

		Map<String, String> newHints = Map.copyOf(Map.of(key, value));
		return new FormattingHints(noFormat, preferredLineLength, indentStyle, newHints);
	}

	/**
	 * Gets a custom hint value by key.
	 *
	 * @param key the hint key
	 * @return the hint value if present
	 * @throws NullPointerException if {@code key} is null
	 */
	public Optional<String> getCustomHint(String key)
		{
		requireThat(key, "key").isNotNull();
		return Optional.ofNullable(customHints.get(key));
	}

	/**
	 * Checks if any formatting preferences are specified.
	 *
	 * @return {@code true} if any formatting preferences are set
	 */
	public boolean hasPreferences()
		{
		return noFormat || preferredLineLength.isPresent() || indentStyle.isPresent() || !customHints.isEmpty();
	}
}
