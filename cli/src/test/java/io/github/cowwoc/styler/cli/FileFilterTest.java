package io.github.cowwoc.styler.cli;

import org.testng.annotations.Test;

import java.nio.file.Paths;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;

/**
 * Unit tests for FileFilter pattern matching.
 */
public class FileFilterTest
{
	/**
	 * Verifies that empty filter accepts all files.
	 */
	@Test
	public void emptyFilterAcceptsAllFiles()
	{
		FileFilter filter = FileFilter.builder().build();

		requireThat(filter.matches(Paths.get("Example.java")), "matchesExample").isTrue();
		requireThat(filter.matches(Paths.get("src/main/java/Test.java")), "matchesTest").isTrue();
	}

	/**
	 * Verifies that include pattern filters files correctly.
	 */
	@Test
	public void includePatternFiltersFiles()
	{
		FileFilter filter = FileFilter.builder().
			includePattern("*.java").
			includePattern("**/*.java").
			build();

		requireThat(filter.matches(Paths.get("Example.java")), "matchesExample").isTrue();
		requireThat(filter.matches(Paths.get("src/main/java/Test.java")), "matchesTest").isTrue();
		requireThat(filter.matches(Paths.get("readme.txt")), "matchesReadme").isFalse();
	}

	/**
	 * Verifies that exclude pattern filters files correctly.
	 */
	@Test
	public void excludePatternFiltersFiles()
	{
		FileFilter filter = FileFilter.builder().
			excludePattern("**/test/**").
			build();

		requireThat(filter.matches(Paths.get("src/main/java/Example.java")), "matchesMain").isTrue();
		requireThat(filter.matches(Paths.get("src/test/java/ExampleTest.java")), "matchesTest").isFalse();
	}

	/**
	 * Verifies that exclude overrides include (precedence).
	 */
	@Test
	public void excludeOverridesInclude()
	{
		FileFilter filter = FileFilter.builder().
			includePattern("**/*.java").
			excludePattern("**/generated/*.java").
			build();

		requireThat(filter.matches(Paths.get("src/main/java/Example.java")), "matchesExample").isTrue();
		requireThat(filter.matches(Paths.get("target/generated/Example.java")), "matchesGenerated").isFalse();
	}

	/**
	 * Verifies that multiple include patterns work correctly.
	 */
	@Test
	public void multipleIncludePatterns()
	{
		FileFilter filter = FileFilter.builder().
			includePattern("*.java").
			includePattern("*.xml").
			build();

		requireThat(filter.matches(Paths.get("Example.java")), "matchesJava").isTrue();
		requireThat(filter.matches(Paths.get("pom.xml")), "matchesXml").isTrue();
		requireThat(filter.matches(Paths.get("readme.txt")), "matchesTxt").isFalse();
	}

	/**
	 * Verifies that multiple exclude patterns work correctly.
	 */
	@Test
	public void multipleExcludePatterns()
	{
		FileFilter filter = FileFilter.builder().
			excludePattern("**/test/*.java").
			excludePattern("target/**").
			build();

		requireThat(filter.matches(Paths.get("src/main/java/Example.java")), "matchesMain").isTrue();
		requireThat(filter.matches(Paths.get("src/test/Test.java")), "matchesTest").isFalse();
		requireThat(filter.matches(Paths.get("target/classes/Example.class")), "matchesTarget").isFalse();
	}

	/**
	 * Verifies that wildcard patterns work correctly.
	 */
	@Test
	public void wildcardPatterns()
	{
		FileFilter filter = FileFilter.builder().
			includePattern("Test*.java").
			build();

		requireThat(filter.matches(Paths.get("TestExample.java")), "matchesTestExample").isTrue();
		requireThat(filter.matches(Paths.get("TestCase.java")), "matchesTestCase").isTrue();
		requireThat(filter.matches(Paths.get("Example.java")), "matchesExample").isFalse();
	}

	/**
	 * Verifies that single character wildcard (?) works correctly.
	 */
	@Test
	public void singleCharWildcard()
	{
		FileFilter filter = FileFilter.builder().
			includePattern("Test?.java").
			build();

		requireThat(filter.matches(Paths.get("Test1.java")), "matchesTest1").isTrue();
		requireThat(filter.matches(Paths.get("TestA.java")), "matchesTestA").isTrue();
		requireThat(filter.matches(Paths.get("Test10.java")), "matchesTest10").isFalse();
	}

	/**
	 * Verifies that directory exclusion works for early pruning.
	 */
	@Test
	public void shouldExcludeDirectory()
	{
		FileFilter filter = FileFilter.builder().
			excludePattern("target").
			excludePattern("target/**").
			build();

		requireThat(filter.shouldExcludeDirectory(Paths.get("target")), "excludesTarget").isTrue();
		requireThat(filter.shouldExcludeDirectory(Paths.get("src/main/java")), "excludesMain").isFalse();
	}

	/**
	 * Verifies that pattern validation rejects blank patterns.
	 */
	@Test(expectedExceptions = IllegalArgumentException.class)
	public void rejectsBlankPattern()
	{
		FileFilter.builder().includePattern("");
	}

	/**
	 * Verifies that pattern validation rejects excessively long patterns.
	 */
	@Test(expectedExceptions = IllegalArgumentException.class)
	public void rejectsLongPattern()
	{
		String longPattern = "*".repeat(600);
		FileFilter.builder().includePattern(longPattern);
	}

	/**
	 * Verifies that pattern validation rejects patterns with too many wildcards.
	 */
	@Test(expectedExceptions = IllegalArgumentException.class)
	public void rejectsTooManyWildcards()
	{
		String pattern = "*".repeat(60);
		FileFilter.builder().includePattern(pattern);
	}
}
