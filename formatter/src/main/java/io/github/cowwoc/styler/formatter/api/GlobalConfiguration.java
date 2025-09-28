package io.github.cowwoc.styler.formatter.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * Global configuration settings that apply to all formatting rules.
 * <p>
 * Global configuration includes settings like line endings, character encoding,
 * indentation preferences, and other formatting options that affect the entire
 * codebase consistently. These settings provide defaults that individual rules
 * can build upon or override as needed.
 * <p>
 * <b>Thread Safety:</b> This class is immutable and thread-safe.
 * <b>Security:</b> All configuration values are validated for security compliance.
 *
 * @since 1.0.0
 * @author Plugin Framework Team
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
	"indentationType", "indentationSize", "maxLineLength", "lineEnding",
	"charset", "insertFinalNewline", "trimTrailingWhitespace", "tabWidth"
})
public final class GlobalConfiguration
{
	@JsonProperty("indentationType")
	private final IndentationType indentationType;

	@JsonProperty("indentationSize")
	private final int indentationSize;

	@JsonProperty("maxLineLength")
	private final int maxLineLength;

	@JsonProperty("lineEnding")
	private final LineEnding lineEnding;

	@JsonProperty("charset")
	private final String charset;

	@JsonProperty("insertFinalNewline")
	private final boolean insertFinalNewline;

	@JsonProperty("trimTrailingWhitespace")
	private final boolean trimTrailingWhitespace;

	@JsonProperty("tabWidth")
	private final int tabWidth;

	/**
	 * Creates a new global configuration with specified settings.
	 *
	 * @param indentationType        the indentation type to use, never {@code null}
	 * @param indentationSize       the number of indentation units per level
	 * @param maxLineLength         the maximum allowed line length in characters
	 * @param lineEnding            the line ending style to use, never {@code null}
	 * @param charset               the character encoding name, never {@code null}
	 * @param insertFinalNewline    whether to ensure files end with a newline
	 * @param trimTrailingWhitespace whether to remove trailing whitespace
	 * @param tabWidth              the display width of tab characters
	 */
	public GlobalConfiguration(@JsonProperty("indentationType") IndentationType indentationType,
	                          @JsonProperty("indentationSize") Integer indentationSize,
	                          @JsonProperty("maxLineLength") Integer maxLineLength,
	                          @JsonProperty("lineEnding") LineEnding lineEnding,
	                          @JsonProperty("charset") String charset,
	                          @JsonProperty("insertFinalNewline") Boolean insertFinalNewline,
	                          @JsonProperty("trimTrailingWhitespace") Boolean trimTrailingWhitespace,
	                          @JsonProperty("tabWidth") Integer tabWidth)
	{
		this.indentationType = indentationType != null ? indentationType : IndentationType.SPACES;
		this.indentationSize = validateIndentationSize(indentationSize != null ? indentationSize : 4);
		this.maxLineLength = validateMaxLineLength(maxLineLength != null ? maxLineLength : 120);
		this.lineEnding = lineEnding != null ? lineEnding : LineEnding.SYSTEM;
		this.charset = validateCharset(charset != null ? charset : StandardCharsets.UTF_8.name());
		this.insertFinalNewline = insertFinalNewline != null ? insertFinalNewline : true;
		this.trimTrailingWhitespace = trimTrailingWhitespace != null ? trimTrailingWhitespace : true;
		this.tabWidth = validateTabWidth(tabWidth != null ? tabWidth : 4);
	}

	/**
	 * Creates a default global configuration with standard Java formatting conventions.
	 */
	public GlobalConfiguration()
	{
		this(
			IndentationType.SPACES,
			4,
			120,
			LineEnding.SYSTEM,
			StandardCharsets.UTF_8.name(),
			true,
			true,
			4
		);
	}

	/**
	 * Validates this global configuration.
	 *
	 * @throws ConfigurationException if the configuration is invalid
	 */
	public void validate() throws ConfigurationException
	{
		// Validation is performed in constructor, but this method allows
		// for future expansion and explicit validation calls
		if (indentationType == IndentationType.TABS && indentationSize != 1)
		{
			throw new ConfigurationException("When using tab indentation, indentation size must be 1");
		}

		if (tabWidth != indentationSize && indentationType == IndentationType.SPACES)
		{
			// This is a warning case - tab width and space indentation size usually match
			// but it's not strictly required, so we don't throw an exception
		}
	}

	/**
	 * Creates a new global configuration with modified indentation settings.
	 *
	 * @param newIndentationType the new indentation type, never {@code null}
	 * @param newIndentationSize the new indentation size
	 * @return a new global configuration with updated indentation
	 */
	public GlobalConfiguration withIndentation(IndentationType newIndentationType, int newIndentationSize)
	{
		return new GlobalConfiguration(
			newIndentationType, newIndentationSize, maxLineLength, lineEnding,
			charset, insertFinalNewline, trimTrailingWhitespace, tabWidth
		);
	}

	/**
	 * Creates a new global configuration with modified line length.
	 *
	 * @param newMaxLineLength the new maximum line length
	 * @return a new global configuration with updated line length
	 */
	public GlobalConfiguration withMaxLineLength(int newMaxLineLength)
	{
		return new GlobalConfiguration(
			indentationType, indentationSize, newMaxLineLength, lineEnding,
			charset, insertFinalNewline, trimTrailingWhitespace, tabWidth
		);
	}

	// Getters
	public IndentationType getIndentationType() { return indentationType; }
	public int getIndentationSize() { return indentationSize; }
	public int getMaxLineLength() { return maxLineLength; }
	public LineEnding getLineEnding() { return lineEnding; }
	public String getCharset() { return charset; }
	public boolean isInsertFinalNewline() { return insertFinalNewline; }
	public boolean isTrimTrailingWhitespace() { return trimTrailingWhitespace; }
	public int getTabWidth() { return tabWidth; }

	/**
	 * Returns the character encoding as a Charset object.
	 *
	 * @return the charset, never {@code null}
	 * @throws ConfigurationException if the charset name is invalid
	 */
	public Charset getCharsetObject() throws ConfigurationException
	{
		try
		{
			return Charset.forName(charset);
		}
		catch (IllegalArgumentException e)
		{
			throw new ConfigurationException("Invalid charset: " + charset, e);
		}
	}

	private static int validateIndentationSize(int size)
	{
		if (size < 1 || size > 16)
		{
			throw new IllegalArgumentException("Indentation size must be between 1 and 16, but was " + size);
		}
		return size;
	}

	private static int validateMaxLineLength(int length)
	{
		if (length < 40 || length > 1000)
		{
			throw new IllegalArgumentException("Maximum line length must be between 40 and 1000, but was " + length);
		}
		return length;
	}

	private static String validateCharset(String charsetName)
	{
		requireThat(charsetName, "charset").isNotNull();
		requireThat(charsetName.trim(), "charset").isNotEmpty();

		try
		{
			Charset.forName(charsetName);
		}
		catch (IllegalArgumentException e)
		{
			throw new IllegalArgumentException("Invalid charset name: " + charsetName, e);
		}

		return charsetName.trim();
	}

	private static int validateTabWidth(int width)
	{
		if (width < 1 || width > 16)
		{
			throw new IllegalArgumentException("Tab width must be between 1 and 16, but was " + width);
		}
		return width;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj) return true;
		if (obj == null || getClass() != obj.getClass()) return false;

		GlobalConfiguration that = (GlobalConfiguration) obj;
		return indentationSize == that.indentationSize &&
		       maxLineLength == that.maxLineLength &&
		       insertFinalNewline == that.insertFinalNewline &&
		       trimTrailingWhitespace == that.trimTrailingWhitespace &&
		       tabWidth == that.tabWidth &&
		       indentationType == that.indentationType &&
		       lineEnding == that.lineEnding &&
		       Objects.equals(charset, that.charset);
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(indentationType, indentationSize, maxLineLength, lineEnding,
		                   charset, insertFinalNewline, trimTrailingWhitespace, tabWidth);
	}

	@Override
	public String toString()
	{
		return "GlobalConfiguration{" +
		       "indentationType=" + indentationType +
		       ", indentationSize=" + indentationSize +
		       ", maxLineLength=" + maxLineLength +
		       ", lineEnding=" + lineEnding +
		       ", charset='" + charset + '\'' +
		       ", insertFinalNewline=" + insertFinalNewline +
		       ", trimTrailingWhitespace=" + trimTrailingWhitespace +
		       ", tabWidth=" + tabWidth +
		       '}';
	}

	/**
	 * Enumeration of indentation types.
	 */
	public enum IndentationType
	{
		/** Use space characters for indentation */
		SPACES,

		/** Use tab characters for indentation */
		TABS,

		/** Mixed indentation (tabs for major levels, spaces for alignment) */
		MIXED
	}

	/**
	 * Enumeration of line ending styles.
	 */
	public enum LineEnding
	{
		/** Unix-style line endings (LF) */
		LF,

		/** Windows-style line endings (CRLF) */
		CRLF,

		/** Classic Mac-style line endings (CR) */
		CR,

		/** Use the system's default line ending */
		SYSTEM;

		/**
		 * Returns the actual line ending string for this type.
		 *
		 * @return the line ending string
		 */
		public String getLineEndingString()
		{
			return switch (this)
			{
				case LF -> "\n";
				case CRLF -> "\r\n";
				case CR -> "\r";
				case SYSTEM -> System.lineSeparator();
			};
		}
	}
}