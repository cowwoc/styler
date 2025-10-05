package io.github.cowwoc.styler.plugin.engine;

import io.github.cowwoc.styler.ast.SourcePosition;
import io.github.cowwoc.styler.ast.SourceRange;
import io.github.cowwoc.styler.formatter.api.TextEdit;
import org.testng.annotations.Test;

import java.util.List;

import static org.testng.Assert.assertEquals;

/**
 * Comprehensive tests for TextEditApplicator.
 */
public class TextEditApplicatorTest
{
	/**
	 * Verifies TextEditApplicator handles null source validation.
	 */
	@Test(expectedExceptions = NullPointerException.class)
	public void rejectsNullSource()
	{
		TextEditApplicator applicator = new TextEditApplicator();
		applicator.applyEdits(null, List.of());
	}

	/**
	 * Verifies TextEditApplicator handles null edits validation.
	 */
	@Test(expectedExceptions = NullPointerException.class)
	public void rejectsNullEdits()
	{
		TextEditApplicator applicator = new TextEditApplicator();
		applicator.applyEdits("source", null);
	}

	/**
	 * Verifies TextEditApplicator returns original source when no edits provided.
	 */
	@Test
	public void returnsOriginalSourceWithNoEdits()
	{
		TextEditApplicator applicator = new TextEditApplicator();
		String source = "class Test {}";
		String result = applicator.applyEdits(source, List.of());
		assertEquals(result, source);
	}

	/**
	 * Verifies TextEditApplicator handles empty source correctly.
	 */
	@Test
	public void handlesEmptySource()
	{
		TextEditApplicator applicator = new TextEditApplicator();
		String result = applicator.applyEdits("", List.of());
		assertEquals(result, "");
	}

	/**
	 * Verifies single edit at start of file is applied correctly.
	 */
	@Test
	public void appliesSingleEditAtStartOfFile()
	{
		TextEditApplicator applicator = new TextEditApplicator();

		String source = "class Test {}";
		TextEdit edit = TextEdit.create(
			new SourceRange(new SourcePosition(1, 1), new SourcePosition(1, 6)),
			"interface",
			"test-rule");

		String result = applicator.applyEdits(source, List.of(edit));
		assertEquals(result, "interface Test {}");
	}

	/**
	 * Verifies multiple non-overlapping edits are applied in correct order.
	 */
	@Test
	public void appliesMultipleNonOverlappingEdits()
	{
		TextEditApplicator applicator = new TextEditApplicator();

		String source = "class Test {}";
		TextEdit edit1 = TextEdit.create(
			new SourceRange(new SourcePosition(1, 1), new SourcePosition(1, 1)),
			"public ",
			"modifier-rule");
		TextEdit edit2 = TextEdit.create(
			new SourceRange(new SourcePosition(1, 6), new SourcePosition(1, 6)),
			" final",
			"final-rule");

		String result = applicator.applyEdits(source, List.of(edit1, edit2));
		assertEquals(result, "public class final Test {}");
	}

	/**
	 * Verifies overlapping edits are detected and rejected.
	 */
	@Test(expectedExceptions = IllegalArgumentException.class,
		expectedExceptionsMessageRegExp = ".*overlap.*")
	public void rejectsOverlappingEdits()
	{
		TextEditApplicator applicator = new TextEditApplicator();

		String source = "class Test {}";
		TextEdit edit1 = TextEdit.create(
			new SourceRange(new SourcePosition(1, 1), new SourcePosition(1, 6)),
			"interface",
			"rule1");
		TextEdit edit2 = TextEdit.create(
			new SourceRange(new SourcePosition(1, 3), new SourcePosition(1, 8)),
			"record",
			"rule2");

		applicator.applyEdits(source, List.of(edit1, edit2));
	}

	/**
	 * Verifies edits at end of file are handled correctly.
	 */
	@Test
	public void handlesEditsAtEndOfFile()
	{
		TextEditApplicator applicator = new TextEditApplicator();

		String source = "class Test";
		TextEdit edit = TextEdit.create(
			new SourceRange(new SourcePosition(1, 11), new SourcePosition(1, 11)),
			" {}",
			"add-braces");

		String result = applicator.applyEdits(source, List.of(edit));
		assertEquals(result, "class Test {}");
	}

	/**
	 * Verifies deletion edit (empty replacement) works correctly.
	 */
	@Test
	public void handlesDeletionEdit()
	{
		TextEditApplicator applicator = new TextEditApplicator();

		String source = "public class Test {}";
		TextEdit edit = TextEdit.create(
			new SourceRange(new SourcePosition(1, 1), new SourcePosition(1, 8)),
			"",
			"remove-modifier");

		String result = applicator.applyEdits(source, List.of(edit));
		assertEquals(result, "class Test {}");
	}

	/**
	 * Verifies insertion edit (zero-length range) works correctly.
	 */
	@Test
	public void handlesInsertionEdit()
	{
		TextEditApplicator applicator = new TextEditApplicator();

		String source = "class Test {}";
		TextEdit edit = TextEdit.create(
			new SourceRange(new SourcePosition(1, 1), new SourcePosition(1, 1)),
			"public ",
			"add-modifier");

		String result = applicator.applyEdits(source, List.of(edit));
		assertEquals(result, "public class Test {}");
	}

	/**
	 * Verifies multi-line edits are handled correctly.
	 */
	@Test
	public void handlesMultiLineEdits()
	{
		TextEditApplicator applicator = new TextEditApplicator();

		String source = "class Test\n{\n}";
		TextEdit edit = TextEdit.create(
			new SourceRange(new SourcePosition(1, 1), new SourcePosition(1, 1)),
			"public ",
			"modifier-rule");

		String result = applicator.applyEdits(source, List.of(edit));
		assertEquals(result, "public class Test\n{\n}");
	}

	/**
	 * Verifies edits are applied in reverse order (from end to start).
	 */
	@Test
	public void appliesEditsInReverseOrder()
	{
		TextEditApplicator applicator = new TextEditApplicator();

		String source = "class Test {}";
		TextEdit edit1 = TextEdit.create(
			new SourceRange(new SourcePosition(1, 1), new SourcePosition(1, 1)),
			"public ",
			"rule1");
		TextEdit edit2 = TextEdit.create(
			new SourceRange(new SourcePosition(1, 12), new SourcePosition(1, 12)),
			" extends Object",
			"rule2");

		// Apply edits - should work regardless of order provided
		String result = applicator.applyEdits(source, List.of(edit1, edit2));
		assertEquals(result, "public class Test  extends Object{}");
	}
}
