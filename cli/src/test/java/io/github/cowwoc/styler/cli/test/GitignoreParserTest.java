package io.github.cowwoc.styler.cli.test;
import io.github.cowwoc.styler.cli.GitignoreParser;


import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;

/**
 * Unit tests for GitignoreParser.
 */
public class GitignoreParserTest
{
	/**
	 * Verifies that empty .gitignore file loads successfully.
	 */
	@Test
	public void parseEmptyGitignoreFile() throws IOException
	{
		Path gitignoreFile = Files.createTempFile("gitignore", "");
		try
		{
			GitignoreParser parser = GitignoreParser.parse(gitignoreFile);

			requireThat(parser.getRuleCount(), "ruleCount").isEqualTo(0);
			requireThat(parser.isIgnored(Paths.get("Example.java")), "isIgnored").isFalse();
		}
		finally
		{
			Files.deleteIfExists(gitignoreFile);
		}
	}

	/**
	 * Verifies that simple glob patterns work correctly.
	 */
	@Test
	public void parseSimpleGlobPatterns() throws IOException
	{
		Path gitignoreFile = Files.createTempFile("gitignore", "");
		Files.writeString(gitignoreFile, "*.class\n*.log\n");

		try
		{
			GitignoreParser parser = GitignoreParser.parse(gitignoreFile);

			requireThat(parser.getRuleCount(), "ruleCount").isEqualTo(2);
			requireThat(parser.isIgnored(Paths.get("Example.class")), "ignoresClass").isTrue();
			requireThat(parser.isIgnored(Paths.get("application.log")), "ignoresLog").isTrue();
			requireThat(parser.isIgnored(Paths.get("Example.java")), "ignoresJava").isFalse();
		}
		finally
		{
			Files.deleteIfExists(gitignoreFile);
		}
	}

	/**
	 * Verifies that directory patterns work correctly.
	 */
	@Test
	public void parseDirectoryPatterns() throws IOException
	{
		Path gitignoreFile = Files.createTempFile("gitignore", "");
		Files.writeString(gitignoreFile, "target/\nbuild/\n");

		try
		{
			GitignoreParser parser = GitignoreParser.parse(gitignoreFile);

			requireThat(parser.isIgnored(Paths.get("target/classes/Example.class")), "ignoresTarget").isTrue();
			requireThat(parser.isIgnored(Paths.get("build/libs/app.jar")), "ignoresBuild").isTrue();
			requireThat(parser.isIgnored(Paths.get("src/main/java/Example.java")), "ignoresSrc").isFalse();
		}
		finally
		{
			Files.deleteIfExists(gitignoreFile);
		}
	}

	/**
	 * Verifies that negation patterns work correctly.
	 */
	@Test
	public void parseNegationPatterns() throws IOException
	{
		Path gitignoreFile = Files.createTempFile("gitignore", "");
		Files.writeString(gitignoreFile, "*.log\n!important.log\n");

		try
		{
			GitignoreParser parser = GitignoreParser.parse(gitignoreFile);

			requireThat(parser.isIgnored(Paths.get("application.log")), "ignoresAppLog").isTrue();
			requireThat(parser.isIgnored(Paths.get("important.log")), "ignoresImportantLog").isFalse();
		}
		finally
		{
			Files.deleteIfExists(gitignoreFile);
		}
	}

	/**
	 * Verifies that comments and empty lines are skipped.
	 */
	@Test
	public void parseSkipsCommentsAndEmptyLines() throws IOException
	{
		Path gitignoreFile = Files.createTempFile("gitignore", "");
		Files.writeString(gitignoreFile, "# This is a comment\n\n*.class\n  \n# Another comment\n");

		try
		{
			GitignoreParser parser = GitignoreParser.parse(gitignoreFile);

			requireThat(parser.getRuleCount(), "ruleCount").isEqualTo(1);
			requireThat(parser.isIgnored(Paths.get("Example.class")), "ignoresClass").isTrue();
		}
		finally
		{
			Files.deleteIfExists(gitignoreFile);
		}
	}

	/**
	 * Verifies that later patterns override earlier patterns.
	 */
	@Test
	public void laterPatternsOverrideEarlier() throws IOException
	{
		Path gitignoreFile = Files.createTempFile("gitignore", "");
		Files.writeString(gitignoreFile, "*.txt\n!keep.txt\n*.txt\n");

		try
		{
			GitignoreParser parser = GitignoreParser.parse(gitignoreFile);

			// First: *.txt ignores all .txt
			// Second: !keep.txt un-ignores keep.txt
			// Third: *.txt ignores all .txt again (including keep.txt)
			requireThat(parser.isIgnored(Paths.get("keep.txt")), "ignoresKeep").isTrue();
			requireThat(parser.isIgnored(Paths.get("other.txt")), "ignoresOther").isTrue();
		}
		finally
		{
			Files.deleteIfExists(gitignoreFile);
		}
	}

	/**
	 * Verifies that non-existent .gitignore file is handled gracefully.
	 */
	@Test
	public void parseNonExistentFile() throws IOException
	{
		Path gitignoreFile = Paths.get("/tmp/nonexistent-gitignore-" + System.currentTimeMillis());

		GitignoreParser parser = GitignoreParser.parse(gitignoreFile);

		requireThat(parser.getRuleCount(), "ruleCount").isEqualTo(0);
		requireThat(parser.isIgnored(Paths.get("Example.java")), "isIgnored").isFalse();
	}

	/**
	 * Verifies that oversized .gitignore file is rejected.
	 */
	@Test(expectedExceptions = IllegalArgumentException.class)
	public void rejectsOversizedFile() throws IOException
	{
		Path gitignoreFile = Files.createTempFile("gitignore", "");
		try
		{
			// Create file larger than 1MB
			StringBuilder content = new StringBuilder();
			for (int i = 0; i < 100_000; ++i)
			{
				content.append("*.pattern").append(i).append('\n');
			}
			Files.writeString(gitignoreFile, content.toString());

			GitignoreParser.parse(gitignoreFile);
		}
		finally
		{
			Files.deleteIfExists(gitignoreFile);
		}
	}

	/**
	 * Verifies that subdirectory patterns work correctly.
	 */
	@Test
	public void parseSubdirectoryPatterns() throws IOException
	{
		Path gitignoreFile = Files.createTempFile("gitignore", "");
		Files.writeString(gitignoreFile, "node_modules\n.idea\n");

		try
		{
			GitignoreParser parser = GitignoreParser.parse(gitignoreFile);

			requireThat(parser.isIgnored(Paths.get("node_modules/package/index.js")), "ignoresNodeModules").isTrue();
			requireThat(parser.isIgnored(Paths.get(".idea/workspace.xml")), "ignoresIdea").isTrue();
			requireThat(parser.isIgnored(Paths.get("src/main/java/Example.java")), "ignoresSrc").isFalse();
		}
		finally
		{
			Files.deleteIfExists(gitignoreFile);
		}
	}

	/**
	 * Verifies that patterns with many wildcards do not cause ReDoS (Regular Expression Denial of Service).
	 *
	 * <p>This test validates the fix for the security vulnerability where malicious .gitignore patterns
	 * with excessive wildcards could cause catastrophic backtracking in regex matching.
	 */
	@Test
	public void complexWildcardPatternPerformance() throws IOException
	{
		Path gitignoreFile = Files.createTempFile("gitignore", "");
		// Create pattern with many wildcards that would cause ReDoS with regex
		String maliciousPattern = "****************file****************";
		Files.writeString(gitignoreFile, maliciousPattern + "\n");

		try
		{
			GitignoreParser parser = GitignoreParser.parse(gitignoreFile);

			// This should complete quickly without hanging (even with many wildcards)
			long startTime = System.currentTimeMillis();
			boolean ignored = parser.isIgnored(Paths.get("some/deeply/nested/path/that/does/not/match"));
			long elapsed = System.currentTimeMillis() - startTime;

			// Verify the test completes in reasonable time (< 100ms)
			requireThat(elapsed, "elapsed").isLessThan(100L);
			requireThat(ignored, "ignored").isFalse();

			// Also verify pattern matches correctly when it should
			boolean matches = parser.isIgnored(Paths.get("path/to/file/here"));
			requireThat(matches, "matches").isTrue();
		}
		finally
		{
			Files.deleteIfExists(gitignoreFile);
		}
	}
}
