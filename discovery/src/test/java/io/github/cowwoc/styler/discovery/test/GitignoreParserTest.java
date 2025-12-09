package io.github.cowwoc.styler.discovery.test;

import io.github.cowwoc.styler.discovery.GitignoreParser;
import io.github.cowwoc.styler.discovery.GitignoreRule;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static io.github.cowwoc.styler.discovery.test.TestUtils.deleteDirectoryRecursively;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * Tests for GitignoreParser class.
 */
public final class GitignoreParserTest
{
	@Test
	public void parseSimpleFilePatternExcludesMatchingFiles() throws IOException
	{
		Path tempDir = Files.createTempDirectory("test-gitignore-");
		try
		{
			Path gitignorePath = Files.createFile(tempDir.resolve(".gitignore"));
			Files.writeString(gitignorePath, "*.class");

			GitignoreParser parser = new GitignoreParser();
			List<GitignoreRule> rules = parser.parse(gitignorePath);

			assertTrue(parser.isIgnored(Path.of("Test.class"), rules), "Should exclude Test.class");
			assertFalse(parser.isIgnored(Path.of("Test.java"), rules), "Should not exclude Test.java");
		}
		finally
		{
			deleteDirectoryRecursively(tempDir);
		}
	}

	@Test
	public void parseDirectoryPatternExcludesEntireDirectory() throws IOException
	{
		Path tempDir = Files.createTempDirectory("test-gitignore-dir-");
		try
		{
			Path gitignorePath = Files.createFile(tempDir.resolve(".gitignore"));
			Files.writeString(gitignorePath, "build/");

			GitignoreParser parser = new GitignoreParser();
			List<GitignoreRule> rules = parser.parse(gitignorePath);

			assertTrue(parser.isIgnored(Path.of("build/output.class"), rules),
				"Should exclude files in build/");
			assertTrue(parser.isIgnored(Path.of("build/classes/Test.class"), rules),
				"Should exclude nested in build/");
		}
		finally
		{
			deleteDirectoryRecursively(tempDir);
		}
	}

	@Test
	public void parseNegationPatternIncludesPreviouslyExcludedFiles() throws IOException
	{
		Path tempDir = Files.createTempDirectory("test-gitignore-negation-");
		try
		{
			Path gitignorePath = Files.createFile(tempDir.resolve(".gitignore"));
			Files.writeString(gitignorePath, "*.log\n!important.log");

			GitignoreParser parser = new GitignoreParser();
			List<GitignoreRule> rules = parser.parse(gitignorePath);

			assertTrue(parser.isIgnored(Path.of("debug.log"), rules), "Should exclude debug.log");
			assertFalse(parser.isIgnored(Path.of("important.log"), rules),
				"Should not exclude negated important.log");
		}
		finally
		{
			deleteDirectoryRecursively(tempDir);
		}
	}

	@Test
	public void parseCommentLinesAreIgnored() throws IOException
	{
		Path tempDir = Files.createTempDirectory("test-gitignore-comments-");
		try
		{
			Path gitignorePath = Files.createFile(tempDir.resolve(".gitignore"));
			Files.writeString(gitignorePath, "# This is a comment\n*.class\n# Another comment");

			GitignoreParser parser = new GitignoreParser();
			List<GitignoreRule> rules = parser.parse(gitignorePath);

			assertTrue(parser.isIgnored(Path.of("Test.class"), rules), "Should exclude Test.class");
			assertFalse(parser.isIgnored(Path.of("# This is a comment"), rules),
				"Should not treat comment as pattern");
		}
		finally
		{
			deleteDirectoryRecursively(tempDir);
		}
	}

	@Test
	public void parseBlankLinesAreIgnored() throws IOException
	{
		Path tempDir = Files.createTempDirectory("test-gitignore-blank-");
		try
		{
			Path gitignorePath = Files.createFile(tempDir.resolve(".gitignore"));
			Files.writeString(gitignorePath, "*.class\n\n*.log\n\n");

			GitignoreParser parser = new GitignoreParser();
			List<GitignoreRule> rules = parser.parse(gitignorePath);

			assertTrue(parser.isIgnored(Path.of("Test.class"), rules), "Should exclude Test.class");
			assertTrue(parser.isIgnored(Path.of("debug.log"), rules), "Should exclude debug.log");
		}
		finally
		{
			deleteDirectoryRecursively(tempDir);
		}
	}

	@Test
	public void parseDoubleWildcardMatchesAnyDirectory() throws IOException
	{
		Path tempDir = Files.createTempDirectory("test-gitignore-doublestar-");
		try
		{
			Path gitignorePath = Files.createFile(tempDir.resolve(".gitignore"));
			Files.writeString(gitignorePath, "**/target/");

			GitignoreParser parser = new GitignoreParser();
			List<GitignoreRule> rules = parser.parse(gitignorePath);

			assertTrue(parser.isIgnored(Path.of("target/classes/Test.class"), rules),
				"Should exclude at root");
			assertTrue(parser.isIgnored(Path.of("module/target/output.jar"), rules),
				"Should exclude at intermediate level");
			assertTrue(parser.isIgnored(Path.of("a/b/c/target/d.txt"), rules),
				"Should exclude at deep level");
		}
		finally
		{
			deleteDirectoryRecursively(tempDir);
		}
	}

	@Test
	public void parseRootedPatternMatchesOnlyAtRoot() throws IOException
	{
		Path tempDir = Files.createTempDirectory("test-gitignore-rooted-");
		try
		{
			Path gitignorePath = Files.createFile(tempDir.resolve(".gitignore"));
			Files.writeString(gitignorePath, "/build");

			GitignoreParser parser = new GitignoreParser();
			List<GitignoreRule> rules = parser.parse(gitignorePath);

			assertTrue(parser.isIgnored(Path.of("build"), rules), "Should exclude build at root");
			assertFalse(parser.isIgnored(Path.of("src/build"), rules),
				"Should not exclude build in subdirectory");
		}
		finally
		{
			deleteDirectoryRecursively(tempDir);
		}
	}

	@Test(expectedExceptions = NoSuchFileException.class)
	public void parseNonExistentGitignoreThrowsNoSuchFileException() throws IOException
	{
		Path nonExistentPath = Path.of("/nonexistent/.gitignore");
		GitignoreParser parser = new GitignoreParser();
		parser.parse(nonExistentPath);
	}

	@Test
	public void parseMultipleGitignoreFilesCombinesPatterns() throws IOException
	{
		Path tempDir = Files.createTempDirectory("test-gitignore-multiple-");
		try
		{
			Path rootGitignore = Files.createFile(tempDir.resolve(".gitignore"));
			Files.writeString(rootGitignore, "*.log");

			Path subdir = Files.createDirectory(tempDir.resolve("subdir"));
			Path subdirGitignore = Files.createFile(subdir.resolve(".gitignore"));
			Files.writeString(subdirGitignore, "*.tmp");

			GitignoreParser parser = new GitignoreParser();
			List<GitignoreRule> rootRules = parser.parse(rootGitignore);
			List<GitignoreRule> subdirRules = parser.parse(subdirGitignore);

			// Combine rules manually (as FileDiscovery would do)
			List<GitignoreRule> combinedRules = new ArrayList<>();
			combinedRules.addAll(rootRules);
			combinedRules.addAll(subdirRules);

			assertTrue(parser.isIgnored(Path.of("debug.log"), combinedRules),
				"Should exclude from root .gitignore");
			assertTrue(parser.isIgnored(Path.of("cache.tmp"), combinedRules),
				"Should exclude from subdir .gitignore");
		}
		finally
		{
			deleteDirectoryRecursively(tempDir);
		}
	}

	@Test(expectedExceptions = NullPointerException.class)
	public void isIgnoredNullPathThrowsNullPointerException() throws IOException
	{
		Path tempDir = null;
		try
		{
			tempDir = Files.createTempDirectory("test-gitignore-null-");
			Path gitignorePath = Files.createFile(tempDir.resolve(".gitignore"));
			GitignoreParser parser = new GitignoreParser();
			List<GitignoreRule> rules = parser.parse(gitignorePath);
			parser.isIgnored(null, rules);
		}
		finally
		{
			deleteDirectoryRecursively(tempDir);
		}
	}

	@Test(expectedExceptions = NullPointerException.class)
	public void isIgnoredNullRulesThrowsNullPointerException()
	{
		GitignoreParser parser = new GitignoreParser();
		parser.isIgnored(Path.of("test.txt"), null);
	}

	@Test
	public void isIgnoredEmptyRulesReturnsNotIgnored()
	{
		GitignoreParser parser = new GitignoreParser();
		List<GitignoreRule> emptyRules = List.of();

		assertFalse(parser.isIgnored(Path.of("anything.java"), emptyRules),
			"Empty rules should not ignore anything");
	}
}
