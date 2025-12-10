package io.github.cowwoc.styler.formatter.test.brace;

import io.github.cowwoc.styler.formatter.FormattingViolation;
import io.github.cowwoc.styler.formatter.brace.BraceFormattingConfiguration;
import io.github.cowwoc.styler.formatter.brace.BraceStyle;
import io.github.cowwoc.styler.formatter.brace.internal.BraceAnalyzer;
import io.github.cowwoc.styler.formatter.test.TestTransformationContext;
import org.testng.annotations.Test;

import java.util.List;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;

/**
 * Tests for BraceAnalyzer violation detection.
 */
public class BraceAnalyzerTest
{
	/**
	 * Tests detection of no violations for Allman style class.
	 */
	@Test
	public void shouldDetectNoViolationsForAllmanStyleClass()
	{
		String source = """
			class Test
			{
			}
			""";
		TestTransformationContext context = new TestTransformationContext(source);
		BraceFormattingConfiguration config = BraceFormattingConfiguration.defaultConfig();

		List<FormattingViolation> violations = BraceAnalyzer.analyze(context, config);

		requireThat(violations, "violations").isEmpty();
	}

	/**
	 * Tests detection of K&R violation when Allman is required.
	 */
	@Test
	public void shouldDetectKRViolationWhenAllmanRequired()
	{
		String source = "class Test {" + "\n" + "}";
		TestTransformationContext context = new TestTransformationContext(source);
		BraceFormattingConfiguration config = BraceFormattingConfiguration.defaultConfig();

		List<FormattingViolation> violations = BraceAnalyzer.analyze(context, config);

		requireThat(violations, "violations").isNotEmpty();
	}

	/**
	 * Tests detection of no violations for K&R style with appropriate config.
	 */
	@Test
	public void shouldDetectNoViolationsForKRStyleWithConfig()
	{
		String source = "class Test {" + "\n" + "}";
		TestTransformationContext context = new TestTransformationContext(source);
		BraceFormattingConfiguration config = new BraceFormattingConfiguration("brace-style",
			BraceStyle.SAME_LINE);

		List<FormattingViolation> violations = BraceAnalyzer.analyze(context, config);

		requireThat(violations, "violations").isEmpty();
	}

	/**
	 * Tests detection of Allman violation when K&R is required.
	 */
	@Test
	public void shouldDetectAllmanViolationWhenKRRequired()
	{
		String source = """
			class Test
			{
			}
			""";
		TestTransformationContext context = new TestTransformationContext(source);
		BraceFormattingConfiguration config = new BraceFormattingConfiguration("brace-style",
			BraceStyle.SAME_LINE);

		List<FormattingViolation> violations = BraceAnalyzer.analyze(context, config);

		requireThat(violations, "violations").isNotEmpty();
	}

	/**
	 * Tests detection with method declarations.
	 */
	@Test
	public void shouldDetectMethodBraceViolations()
	{
		String source = """
			class Test {
			    void method() {
			    }
			}
			""";
		TestTransformationContext context = new TestTransformationContext(source);
		BraceFormattingConfiguration config = BraceFormattingConfiguration.defaultConfig();

		List<FormattingViolation> violations = BraceAnalyzer.analyze(context, config);

		// Class has K&R style, methods have K&R style - all violations
		requireThat(violations, "violations").isNotEmpty();
	}

	/**
	 * Tests detection with multiple constructs.
	 */
	@Test
	public void shouldDetectViolationsInMultipleConstructs()
	{
		String source = """
			class Test {
			    void method() {
			        if (true) {
			        }
			    }
			}
			""";
		TestTransformationContext context = new TestTransformationContext(source);
		BraceFormattingConfiguration config = BraceFormattingConfiguration.defaultConfig();

		List<FormattingViolation> violations = BraceAnalyzer.analyze(context, config);

		// Multiple K&R style violations when Allman required
		requireThat(violations, "violations").isNotEmpty();
	}

	/**
	 * Tests that violation includes proper severity.
	 */
	@Test
	public void shouldIncludeProperSeverityInViolation()
	{
		String source = "class Test {" + "\n" + "}";
		TestTransformationContext context = new TestTransformationContext(source);
		BraceFormattingConfiguration config = BraceFormattingConfiguration.defaultConfig();

		List<FormattingViolation> violations = BraceAnalyzer.analyze(context, config);

		if (!violations.isEmpty())
			requireThat(violations.get(0).severity(), "severity").isEqualTo(
				io.github.cowwoc.styler.formatter.ViolationSeverity.WARNING);
	}

	/**
	 * Tests empty source code.
	 */
	@Test
	public void shouldHandleEmptySourceCode()
	{
		String source = "";
		TestTransformationContext context = new TestTransformationContext(source);
		BraceFormattingConfiguration config = BraceFormattingConfiguration.defaultConfig();

		List<FormattingViolation> violations = BraceAnalyzer.analyze(context, config);

		requireThat(violations, "violations").isEmpty();
	}

	/**
	 * Tests source with no braces.
	 */
	@Test
	public void shouldHandleSourceWithNoBraces()
	{
		String source = "interface Foo;";
		TestTransformationContext context = new TestTransformationContext(source);
		BraceFormattingConfiguration config = BraceFormattingConfiguration.defaultConfig();

		List<FormattingViolation> violations = BraceAnalyzer.analyze(context, config);

		requireThat(violations, "violations").isEmpty();
	}
}
