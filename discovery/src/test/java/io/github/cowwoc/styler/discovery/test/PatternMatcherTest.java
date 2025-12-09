package io.github.cowwoc.styler.discovery.test;

import io.github.cowwoc.styler.discovery.internal.GlobPatternMatcher;
import org.testng.annotations.Test;

import java.nio.file.Path;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * Tests for GlobPatternMatcher class.
 */
public final class PatternMatcherTest
{
	@Test
	public void matchesSimpleWildcardMatchesAnyJavaFile()
	{
		GlobPatternMatcher matcher = new GlobPatternMatcher("*.java");

		assertTrue(matcher.matches(Path.of("Test.java")), "Should match Test.java");
		assertTrue(matcher.matches(Path.of("AnotherFile.java")), "Should match AnotherFile.java");
		assertFalse(matcher.matches(Path.of("Test.txt")), "Should not match Test.txt");
		assertFalse(matcher.matches(Path.of("Test.java.bak")), "Should not match Test.java.bak");
	}

	@Test
	public void matchesDoubleWildcardMatchesAcrossDirectories()
	{
		GlobPatternMatcher matcher = new GlobPatternMatcher("**/Test.java");

		assertTrue(matcher.matches(Path.of("Test.java")), "Should match Test.java at root");
		assertTrue(matcher.matches(Path.of("src/Test.java")), "Should match src/Test.java");
		assertTrue(matcher.matches(Path.of("src/main/java/Test.java")), "Should match deeply nested");
		assertFalse(matcher.matches(Path.of("src/Other.java")), "Should not match different filename");
	}

	@Test
	public void matchesQuestionMarkWildcardMatchesSingleCharacter()
	{
		GlobPatternMatcher matcher = new GlobPatternMatcher("Test?.java");

		assertTrue(matcher.matches(Path.of("Test1.java")), "Should match Test1.java");
		assertTrue(matcher.matches(Path.of("TestA.java")), "Should match TestA.java");
		assertFalse(matcher.matches(Path.of("Test.java")), "Should not match without character");
		assertFalse(matcher.matches(Path.of("Test12.java")), "Should not match with two characters");
	}

	@Test
	public void matchesCharacterClassMatchesSpecifiedCharacters()
	{
		GlobPatternMatcher matcher = new GlobPatternMatcher("Test[ABC].java");

		assertTrue(matcher.matches(Path.of("TestA.java")), "Should match TestA.java");
		assertTrue(matcher.matches(Path.of("TestB.java")), "Should match TestB.java");
		assertTrue(matcher.matches(Path.of("TestC.java")), "Should match TestC.java");
		assertFalse(matcher.matches(Path.of("TestD.java")), "Should not match TestD.java");
	}

	@Test
	public void matchesNegatedCharacterClassExcludesSpecifiedCharacters()
	{
		GlobPatternMatcher matcher = new GlobPatternMatcher("Test[!0-9].java");

		assertTrue(matcher.matches(Path.of("TestA.java")), "Should match TestA.java");
		assertFalse(matcher.matches(Path.of("Test1.java")), "Should not match Test1.java");
		assertFalse(matcher.matches(Path.of("Test9.java")), "Should not match Test9.java");
	}

	@Test
	public void matchesDirectoryPatternMatchesSpecificDirectory()
	{
		GlobPatternMatcher matcher = new GlobPatternMatcher("src/main/**/*.java");

		assertTrue(matcher.matches(Path.of("src/main/Test.java")),
			"Should match src/main/Test.java");
		assertTrue(matcher.matches(Path.of("src/main/java/Test.java")),
			"Should match src/main/java/Test.java");
		assertFalse(matcher.matches(Path.of("src/test/Test.java")),
			"Should not match src/test/Test.java");
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void matchesEmptyPatternThrowsIllegalArgumentException()
	{
		new GlobPatternMatcher("");
	}

	@Test(expectedExceptions = NullPointerException.class)
	public void matchesNullPatternThrowsNullPointerException()
	{
		new GlobPatternMatcher(null);
	}
}
