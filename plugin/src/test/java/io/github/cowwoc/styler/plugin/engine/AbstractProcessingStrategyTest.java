package io.github.cowwoc.styler.plugin.engine;

import io.github.cowwoc.styler.formatter.api.FormattingRule;
import io.github.cowwoc.styler.plugin.config.PluginConfiguration;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.testng.annotations.Test;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

/**
 * Tests for AbstractProcessingStrategy Template Method pattern.
 */
public class AbstractProcessingStrategyTest
{
	/**
	 * Verifies Template Method pattern executes hook methods in correct order.
	 */
	@Test
	public void templateMethodExecutesHooksInOrder() throws MojoExecutionException
	{
		TestStrategy strategy = new TestStrategy(
			createTestConfig(),
			createMockParser(),
			new FormattingContextBuilder(),
			createMockRuleLoader());

		Path sourcePath = Paths.get("Test.java");
		strategy.process(sourcePath, "class Test {}");

		assertEquals(strategy.executionLog.size(), 2);
		assertEquals(strategy.executionLog.get(0), "createResultCollector");
		assertEquals(strategy.executionLog.get(1), "processResults");
	}

	/**
	 * Verifies null sourcePath parameter is rejected.
	 */
	@Test(expectedExceptions = NullPointerException.class, expectedExceptionsMessageRegExp = ".*sourcePath.*")
	public void rejectsNullSourcePath() throws MojoExecutionException
	{
		TestStrategy strategy = new TestStrategy(
			createTestConfig(),
			createMockParser(),
			new FormattingContextBuilder(),
			createMockRuleLoader());

		strategy.process(null, "class Test {}");
	}

	/**
	 * Verifies null sourceText parameter is rejected.
	 */
	@Test(expectedExceptions = NullPointerException.class, expectedExceptionsMessageRegExp = ".*sourceText.*")
	public void rejectsNullSourceText() throws MojoExecutionException
	{
		TestStrategy strategy = new TestStrategy(
			createTestConfig(),
			createMockParser(),
			new FormattingContextBuilder(),
			createMockRuleLoader());

		strategy.process(Paths.get("Test.java"), null);
	}

	/**
	 * Verifies parse errors are wrapped in MojoExecutionException.
	 */
	@Test
	public void wrapsParseErrorsInMojoException()
	{
		SourceParser failingParser = (sourceText, sourcePath) ->
		{
			throw new IllegalArgumentException("parse error");
		};

		TestStrategy strategy = new TestStrategy(
			createTestConfig(),
			failingParser,
			new FormattingContextBuilder(),
			createMockRuleLoader());

		try
		{
			strategy.process(Paths.get("Test.java"), "invalid");
			fail("Expected MojoExecutionException");
		}
		catch (MojoExecutionException e)
		{
			assertTrue(e.getMessage().contains("Failed to process file"));
			assertTrue(e.getMessage().contains("parse error"));
		}
	}

	/**
	 * Verifies AbstractProcessingStrategy can be constructed with valid parameters.
	 */
	@Test
	public void constructsWithValidParameters()
	{
		TestStrategy strategy = new TestStrategy(
			createTestConfig(),
			createMockParser(),
			new FormattingContextBuilder(),
			createMockRuleLoader());

		assertNotNull(strategy.config);
		assertNotNull(strategy.parser);
		assertNotNull(strategy.contextBuilder);
		assertNotNull(strategy.ruleLoader);
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

	private static SourceParser createMockParser()
	{
		return (sourceText, sourcePath) -> null;
	}

	private static FormattingRuleLoader createMockRuleLoader()
	{
		return new FormattingRuleLoader()
		{
			@Override
			public List<FormattingRule> loadRules()
			{
				return List.of();
			}
		};
	}

	/**
	 * Test implementation of AbstractProcessingStrategy for testing Template Method pattern.
	 */
	private static final class TestStrategy extends AbstractProcessingStrategy
	{
		final List<String> executionLog = new java.util.ArrayList<>();

		TestStrategy(PluginConfiguration config, SourceParser parser,
			FormattingContextBuilder contextBuilder, FormattingRuleLoader ruleLoader)
		{
			super(config, parser, contextBuilder, ruleLoader);
		}

		@Override
		protected ResultCollector createResultCollector()
		{
			executionLog.add("createResultCollector");
			return (rule, result) ->
			{
			};
		}

		@Override
		protected ProcessingResult processResults(ResultCollector collector, Path sourcePath,
			String sourceText)
		{
			executionLog.add("processResults");
			return ProcessingResult.clean();
		}

		@Override
		public String getDescription()
		{
			return "Test strategy";
		}
	}
}
