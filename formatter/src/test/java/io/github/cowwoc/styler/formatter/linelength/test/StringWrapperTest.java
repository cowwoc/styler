package io.github.cowwoc.styler.formatter.linelength.test;

import io.github.cowwoc.styler.formatter.linelength.LineLengthConfiguration;
import io.github.cowwoc.styler.formatter.linelength.internal.StringWrapper;
import org.testng.annotations.Test;

import java.util.List;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;

/**
 * Tests for StringWrapper literal handling with URL and path protection.
 */
public class StringWrapperTest
{
	/**
	 * Tests that null string is rejected.
	 */
	@Test(expectedExceptions = NullPointerException.class)
	public void shouldRejectNullStringInShouldWrap()
	{
		LineLengthConfiguration config = LineLengthConfiguration.defaultConfig();
		StringWrapper.shouldWrap(null, config);
	}

	/**
	 * Tests that null config is rejected.
	 */
	@Test(expectedExceptions = NullPointerException.class)
	public void shouldRejectNullConfig()
	{
		StringWrapper.shouldWrap("test", null);
	}

	/**
	 * Tests that short strings are not wrapped.
	 */
	@Test
	public void shouldNotWrapShortStrings()
	{
		LineLengthConfiguration config = LineLengthConfiguration.defaultConfig();
		requireThat(StringWrapper.shouldWrap("\"short\"", config), "shouldWrap").isFalse();
	}

	/**
	 * Tests that long strings should be wrapped when enabled.
	 */
	@Test
	public void shouldWrapLongStrings()
	{
		LineLengthConfiguration config = LineLengthConfiguration.defaultConfig();
		String longString = "\"" + "a".repeat(200) + "\"";
		requireThat(StringWrapper.shouldWrap(longString, config), "shouldWrap").isTrue();
	}

	/**
	 * Tests that split returns non-null result.
	 */
	@Test
	public void shouldReturnNonNullFromSplit()
	{
		List<String> parts = StringWrapper.splitString("\"test string\"", 80, 0);
		requireThat(parts, "parts").isNotNull();
	}

	/**
	 * Tests that split handles empty content.
	 */
	@Test
	public void shouldHandleEmptyContent()
	{
		List<String> parts = StringWrapper.splitString("\"\"", 80, 0);
		requireThat(parts, "parts").isNotNull().isNotEmpty();
	}

	/**
	 * Tests that URLs are protected from splitting.
	 */
	@Test
	public void shouldProtectURLs()
	{
		List<String> parts = StringWrapper.splitString(
			"\"https://docs.oracle.com/javase/specs/\"", 20, 0);
		// URL should remain intact in one part
		requireThat(parts.size(), "parts.size()").isEqualTo(1);
	}

	/**
	 * Tests that file paths are protected from splitting.
	 */
	@Test
	public void shouldProtectFilePaths()
	{
		List<String> parts = StringWrapper.splitString(
			"\"/path/to/file.txt\"", 10, 0);
		// File path should remain intact in one part
		requireThat(parts.size(), "parts.size()").isEqualTo(1);
	}

	/**
	 * Tests null string in splitString is rejected.
	 */
	@Test(expectedExceptions = NullPointerException.class)
	public void shouldRejectNullStringInSplit()
	{
		StringWrapper.splitString(null, 80, 0);
	}

	/**
	 * Tests that invalid max length is rejected.
	 */
	@Test(expectedExceptions = IllegalArgumentException.class)
	public void shouldRejectInvalidMaxLength()
	{
		StringWrapper.splitString("\"test\"", 0, 0);
	}

	/**
	 * Tests that negative indentation is rejected.
	 */
	@Test(expectedExceptions = IllegalArgumentException.class)
	public void shouldRejectNegativeIndentation()
	{
		StringWrapper.splitString("\"test\"", 80, -1);
	}
}
