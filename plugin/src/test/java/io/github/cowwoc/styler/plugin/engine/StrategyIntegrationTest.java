package io.github.cowwoc.styler.plugin.engine;

import io.github.cowwoc.styler.plugin.config.PluginConfiguration;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.testng.annotations.Test;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;

/**
 * Integration tests for ValidationStrategy and FormattingStrategy.
 * Validates strategy-specific behavior beyond AbstractProcessingStrategy tests.
 */
public class StrategyIntegrationTest
{
	/**
	 * Verifies ValidationStrategy can be instantiated and returns correct description.
	 */
	@Test
	public void validationStrategyInstantiationAndDescription()
	{
		PluginConfiguration config = createTestConfig();
		ValidationStrategy strategy = new ValidationStrategy(
			config,
			(text, path) -> null,
			new FormattingContextBuilder(),
			createEmptyRuleLoader());

		assertNotNull(strategy);
		assertEquals(strategy.getDescription(), "Check for formatting violations only (no modifications)");
	}

	/**
	 * Verifies FormattingStrategy can be instantiated and returns correct description.
	 */
	@Test
	public void formattingStrategyInstantiationAndDescription()
	{
		PluginConfiguration config = createTestConfig();
		FormattingStrategy strategy = new FormattingStrategy(
			config,
			(text, path) -> null,
			new FormattingContextBuilder(),
			createEmptyRuleLoader(),
			new TextEditApplicator());

		assertNotNull(strategy);
		assertEquals(strategy.getDescription(), "Apply formatting rules and write changes to files");
	}

	/**
	 * Verifies ValidationStrategy rejects null parameters in constructor.
	 */
	@Test(expectedExceptions = NullPointerException.class)
	public void validationStrategyRejectsNullConfig()
	{
		new ValidationStrategy(
			null,
			(text, path) -> null,
			new FormattingContextBuilder(),
			createEmptyRuleLoader());
	}

	/**
	 * Verifies FormattingStrategy rejects null parameters in constructor.
	 */
	@Test(expectedExceptions = NullPointerException.class)
	public void formattingStrategyRejectsNullEditApplicator()
	{
		new FormattingStrategy(
			createTestConfig(),
			(text, path) -> null,
			new FormattingContextBuilder(),
			createEmptyRuleLoader(),
			null);
	}

	/**
	 * Verifies ValidationStrategy returns clean result when no rules are loaded.
	 */
	@Test
	public void validationStrategyReturnsCleanWithNoRules() throws MojoExecutionException
	{
		ValidationStrategy strategy = new ValidationStrategy(
			createTestConfig(),
			(text, path) -> null,
			new FormattingContextBuilder(),
			createEmptyRuleLoader());

		Path testFile = Paths.get("Test.java");
		FileProcessingStrategy.ProcessingResult result = strategy.process(testFile, "class Test {}");

		assertFalse(result.modified());
		assertEquals(result.violationCount(), 0);
	}

	/**
	 * Verifies FormattingStrategy returns clean result when no rules are loaded.
	 */
	@Test
	public void formattingStrategyReturnsCleanWithNoRules() throws MojoExecutionException
	{
		FormattingStrategy strategy = new FormattingStrategy(
			createTestConfig(),
			(text, path) -> null,
			new FormattingContextBuilder(),
			createEmptyRuleLoader(),
			new TextEditApplicator());

		Path testFile = Paths.get("Test.java");
		FileProcessingStrategy.ProcessingResult result = strategy.process(testFile, "class Test {}");

		assertFalse(result.modified());
		assertEquals(result.editCount(), 0);
	}

	/**
	 * Verifies TextEditApplicator handles empty edit list.
	 */
	@Test
	public void textEditApplicatorHandlesEmptyEdits()
	{
		TextEditApplicator applicator = new TextEditApplicator();
		String source = "class Test {}";
		String result = applicator.applyEdits(source, List.of());
		assertEquals(result, source);
	}

	/**
	 * Verifies TextEditApplicator rejects null source.
	 */
	@Test(expectedExceptions = NullPointerException.class)
	public void textEditApplicatorRejectsNullSource()
	{
		TextEditApplicator applicator = new TextEditApplicator();
		applicator.applyEdits(null, List.of());
	}

	/**
	 * Verifies TextEditApplicator rejects null edits list.
	 */
	@Test(expectedExceptions = NullPointerException.class)
	public void textEditApplicatorRejectsNullEdits()
	{
		TextEditApplicator applicator = new TextEditApplicator();
		applicator.applyEdits("class Test {}", null);
	}

	/**
	 * Verifies TextEditApplicator handles empty source.
	 */
	@Test
	public void textEditApplicatorHandlesEmptySource()
	{
		TextEditApplicator applicator = new TextEditApplicator();
		String result = applicator.applyEdits("", List.of());
		assertEquals(result, "");
	}

	private static PluginConfiguration createTestConfig()
	{
		return new PluginConfiguration(
			new MavenProject(),
			new File("src/main/java"),
			new File("target/classes"),
			new File("."),
			"UTF-8",
			List.of("**/*.java"),
			List.of("**/generated/**"),
			true,
			false,
			false);
	}

	private static FormattingRuleLoader createEmptyRuleLoader()
	{
		return new FormattingRuleLoader()
		{
			@Override
			public List<io.github.cowwoc.styler.formatter.api.FormattingRule> loadRules()
			{
				return List.of();
			}
		};
	}
}
