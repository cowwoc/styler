package io.github.cowwoc.styler.formatter.test.linemapping;

import io.github.cowwoc.styler.formatter.linemapping.LineMapping;
import io.github.cowwoc.styler.formatter.linemapping.internal.LineMappingBuilder;
import org.testng.annotations.Test;

import java.util.Optional;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;

/**
 * Tests for line mapping functionality.
 */
public class LineMappingTest
{
	/**
	 * Tests that identity mapping returns same line numbers.
	 */
	@Test
	public void identityMappingReturnsSameLine()
	{
		String content = """
			line 1
			line 2
			line 3
			""";

		LineMappingBuilder builder = new LineMappingBuilder();
		LineMapping mapping = builder.build(content, content);

		requireThat(mapping.originalLineCount(), "originalLineCount").isEqualTo(4);
		requireThat(mapping.formattedLineCount(), "formattedLineCount").isEqualTo(4);
		requireThat(mapping.lineDelta(), "lineDelta").isEqualTo(0);

		requireThat(mapping.toFormattedLine(1), "toFormattedLine(1)").isEqualTo(Optional.of(1));
		requireThat(mapping.toFormattedLine(2), "toFormattedLine(2)").isEqualTo(Optional.of(2));
		requireThat(mapping.toFormattedLine(3), "toFormattedLine(3)").isEqualTo(Optional.of(3));

		requireThat(mapping.toOriginalLine(1), "toOriginalLine(1)").isEqualTo(Optional.of(1));
		requireThat(mapping.toOriginalLine(2), "toOriginalLine(2)").isEqualTo(Optional.of(2));
		requireThat(mapping.toOriginalLine(3), "toOriginalLine(3)").isEqualTo(Optional.of(3));
	}

	/**
	 * Tests that static identity factory method creates correct mapping.
	 */
	@Test
	public void staticIdentityFactoryCreatesCorrectMapping()
	{
		LineMapping mapping = LineMapping.identity(5);

		requireThat(mapping.originalLineCount(), "originalLineCount").isEqualTo(5);
		requireThat(mapping.formattedLineCount(), "formattedLineCount").isEqualTo(5);
		requireThat(mapping.lineDelta(), "lineDelta").isEqualTo(0);

		for (int i = 1; i <= 5; ++i)
		{
			requireThat(mapping.toFormattedLine(i), "toFormattedLine(" + i + ")").isEqualTo(Optional.of(i));
			requireThat(mapping.toOriginalLine(i), "toOriginalLine(" + i + ")").isEqualTo(Optional.of(i));
		}
	}

	/**
	 * Tests line addition - blank line inserted.
	 */
	@Test
	public void lineAdditionInsertsBlankLine()
	{
		String original = """
			line 1
			line 2
			""";

		String formatted = """
			line 1

			line 2
			""";

		LineMappingBuilder builder = new LineMappingBuilder();
		LineMapping mapping = builder.build(original, formatted);

		requireThat(mapping.originalLineCount(), "originalLineCount").isEqualTo(3);
		requireThat(mapping.formattedLineCount(), "formattedLineCount").isEqualTo(4);
		requireThat(mapping.lineDelta(), "lineDelta").isEqualTo(1);

		// Original line 1 maps to formatted line 1
		requireThat(mapping.toFormattedLine(1), "toFormattedLine(1)").isEqualTo(Optional.of(1));
		// Original line 2 maps to formatted line 3 (after the inserted blank)
		requireThat(mapping.toFormattedLine(2), "toFormattedLine(2)").isEqualTo(Optional.of(3));

		// Formatted line 1 maps to original line 1
		requireThat(mapping.toOriginalLine(1), "toOriginalLine(1)").isEqualTo(Optional.of(1));
		// Formatted line 2 (inserted blank) has no original
		requireThat(mapping.toOriginalLine(2), "toOriginalLine(2)").isEqualTo(Optional.empty());
		// Formatted line 3 maps to original line 2
		requireThat(mapping.toOriginalLine(3), "toOriginalLine(3)").isEqualTo(Optional.of(2));
	}

	/**
	 * Tests line removal - blank line removed.
	 */
	@Test
	public void lineRemovalRemovesBlankLine()
	{
		String original = """
			line 1

			line 2
			""";

		String formatted = """
			line 1
			line 2
			""";

		LineMappingBuilder builder = new LineMappingBuilder();
		LineMapping mapping = builder.build(original, formatted);

		requireThat(mapping.originalLineCount(), "originalLineCount").isEqualTo(4);
		requireThat(mapping.formattedLineCount(), "formattedLineCount").isEqualTo(3);
		requireThat(mapping.lineDelta(), "lineDelta").isEqualTo(-1);

		// Original line 1 maps to formatted line 1
		requireThat(mapping.toFormattedLine(1), "toFormattedLine(1)").isEqualTo(Optional.of(1));
		// Original line 2 (blank) was deleted
		requireThat(mapping.toFormattedLine(2), "toFormattedLine(2)").isEqualTo(Optional.empty());
		// Original line 3 maps to formatted line 2
		requireThat(mapping.toFormattedLine(3), "toFormattedLine(3)").isEqualTo(Optional.of(2));

		// Formatted line 1 maps to original line 1
		requireThat(mapping.toOriginalLine(1), "toOriginalLine(1)").isEqualTo(Optional.of(1));
		// Formatted line 2 maps to original line 3
		requireThat(mapping.toOriginalLine(2), "toOriginalLine(2)").isEqualTo(Optional.of(3));
	}

	/**
	 * Tests line modification - content change without line count change.
	 */
	@Test
	public void lineModificationPreservesLineCount()
	{
		String original = """
			line 1
			original content
			line 3
			""";

		String formatted = """
			line 1
			modified content
			line 3
			""";

		LineMappingBuilder builder = new LineMappingBuilder();
		LineMapping mapping = builder.build(original, formatted);

		requireThat(mapping.originalLineCount(), "originalLineCount").isEqualTo(4);
		requireThat(mapping.formattedLineCount(), "formattedLineCount").isEqualTo(4);
		requireThat(mapping.lineDelta(), "lineDelta").isEqualTo(0);

		// Lines 1 and 3 are unchanged and should map directly
		requireThat(mapping.toFormattedLine(1), "toFormattedLine(1)").isEqualTo(Optional.of(1));
		requireThat(mapping.toFormattedLine(3), "toFormattedLine(3)").isEqualTo(Optional.of(3));

		// Line 2 was modified - LCS won't match it
		requireThat(mapping.toFormattedLine(2), "toFormattedLine(2)").isEqualTo(Optional.empty());
		requireThat(mapping.toOriginalLine(2), "toOriginalLine(2)").isEqualTo(Optional.empty());
	}

	/**
	 * Tests boundary validation - error on invalid original line number.
	 */
	@Test(expectedExceptions = IllegalArgumentException.class)
	public void shouldRejectZeroOriginalLine()
	{
		LineMapping mapping = LineMapping.identity(3);
		mapping.toFormattedLine(0);
	}

	/**
	 * Tests boundary validation - error on original line exceeding count.
	 */
	@Test(expectedExceptions = IllegalArgumentException.class)
	public void shouldRejectOriginalLineExceedingCount()
	{
		LineMapping mapping = LineMapping.identity(3);
		mapping.toFormattedLine(4);
	}

	/**
	 * Tests boundary validation - error on invalid formatted line number.
	 */
	@Test(expectedExceptions = IllegalArgumentException.class)
	public void shouldRejectZeroFormattedLine()
	{
		LineMapping mapping = LineMapping.identity(3);
		mapping.toOriginalLine(0);
	}

	/**
	 * Tests boundary validation - error on formatted line exceeding count.
	 */
	@Test(expectedExceptions = IllegalArgumentException.class)
	public void shouldRejectFormattedLineExceedingCount()
	{
		LineMapping mapping = LineMapping.identity(3);
		mapping.toOriginalLine(4);
	}

	/**
	 * Tests empty content edge case.
	 */
	@Test
	public void emptyContentCreatesZeroLineMapping()
	{
		LineMappingBuilder builder = new LineMappingBuilder();
		LineMapping mapping = builder.build("", "");

		requireThat(mapping.originalLineCount(), "originalLineCount").isEqualTo(0);
		requireThat(mapping.formattedLineCount(), "formattedLineCount").isEqualTo(0);
		requireThat(mapping.lineDelta(), "lineDelta").isEqualTo(0);
	}

	/**
	 * Tests negative line count is rejected for identity mapping.
	 */
	@Test(expectedExceptions = IllegalArgumentException.class)
	public void shouldRejectNegativeLineCountForIdentity()
	{
		LineMapping.identity(-1);
	}

	/**
	 * Tests that null original content is rejected.
	 */
	@Test(expectedExceptions = NullPointerException.class)
	public void shouldRejectNullOriginalContent()
	{
		LineMappingBuilder builder = new LineMappingBuilder();
		builder.build(null, "content");
	}

	/**
	 * Tests that null formatted content is rejected.
	 */
	@Test(expectedExceptions = NullPointerException.class)
	public void shouldRejectNullFormattedContent()
	{
		LineMappingBuilder builder = new LineMappingBuilder();
		builder.build("content", null);
	}

	/**
	 * Tests complex multi-line change scenario.
	 */
	@Test
	public void complexMultiLineChange()
	{
		String original = """
			public class Test
			{
			    void method()
			    {
			    }
			}
			""";

		String formatted = """
			public class Test
			{
			    void method()
			    {
			        // Added comment
			    }
			}
			""";

		LineMappingBuilder builder = new LineMappingBuilder();
		LineMapping mapping = builder.build(original, formatted);

		requireThat(mapping.originalLineCount(), "originalLineCount").isEqualTo(7);
		requireThat(mapping.formattedLineCount(), "formattedLineCount").isEqualTo(8);
		requireThat(mapping.lineDelta(), "lineDelta").isEqualTo(1);

		// First 4 lines should match
		requireThat(mapping.toFormattedLine(1), "toFormattedLine(1)").isEqualTo(Optional.of(1));
		requireThat(mapping.toFormattedLine(2), "toFormattedLine(2)").isEqualTo(Optional.of(2));
		requireThat(mapping.toFormattedLine(3), "toFormattedLine(3)").isEqualTo(Optional.of(3));
		requireThat(mapping.toFormattedLine(4), "toFormattedLine(4)").isEqualTo(Optional.of(4));

		// Line 5 "    }" in original maps to line 6 "    }" in formatted
		requireThat(mapping.toFormattedLine(5), "toFormattedLine(5)").isEqualTo(Optional.of(6));
		// Line 6 "}" in original maps to line 7 "}" in formatted
		requireThat(mapping.toFormattedLine(6), "toFormattedLine(6)").isEqualTo(Optional.of(7));

		// Line 5 in formatted (the comment) is new
		requireThat(mapping.toOriginalLine(5), "toOriginalLine(5)").isEqualTo(Optional.empty());
	}
}
