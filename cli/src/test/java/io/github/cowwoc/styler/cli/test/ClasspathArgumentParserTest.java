package io.github.cowwoc.styler.cli.test;

import io.github.cowwoc.styler.cli.ArgumentParser;
import io.github.cowwoc.styler.cli.CLIOptions;
import org.testng.annotations.Test;

import java.nio.file.Path;
import java.util.List;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * Tests for ClasspathArgumentParser support in ArgumentParser.
 */
public final class ClasspathArgumentParserTest
{
	private final ArgumentParser parser = new ArgumentParser();

	/**
	 * Verifies parsing of single classpath entry with --classpath flag.
	 */
	@Test
	public void parseWithSingleClasspathEntry() throws Exception
	{
		String[] args = {"--classpath", "/lib.jar", "test.java"};
		CLIOptions options = parser.parse(args);

		assertEquals(options.classpathEntries(), List.of(Path.of("/lib.jar")),
			"Should parse single classpath entry");
	}

	/**
	 * Verifies parsing of multiple classpath entries separated by path separator.
	 */
	@Test
	public void parseWithMultipleClasspathEntries() throws Exception
	{
		String pathSeparator = java.io.File.pathSeparator;
		String[] args = {"--classpath", "/a.jar" + pathSeparator + "/b.jar", "test.java"};
		CLIOptions options = parser.parse(args);

		assertEquals(options.classpathEntries(), List.of(Path.of("/a.jar"), Path.of("/b.jar")),
			"Should parse multiple classpath entries");
	}

	/**
	 * Verifies parsing of modulepath entries with --module-path flag.
	 */
	@Test
	public void parseWithModulePath() throws Exception
	{
		String[] args = {"--module-path", "/mod.jar", "test.java"};
		CLIOptions options = parser.parse(args);

		assertEquals(options.modulepathEntries(), List.of(Path.of("/mod.jar")),
			"Should parse modulepath entry");
	}

	/**
	 * Verifies parsing of both classpath and modulepath together.
	 */
	@Test
	public void parseWithBothClasspathAndModulepath() throws Exception
	{
		String[] args = {"--classpath", "/cp.jar", "--module-path", "/mp.jar", "test.java"};
		CLIOptions options = parser.parse(args);

		assertEquals(options.classpathEntries(), List.of(Path.of("/cp.jar")),
			"Should parse classpath entry");
		assertEquals(options.modulepathEntries(), List.of(Path.of("/mp.jar")),
			"Should parse modulepath entry");
	}

	/**
	 * Verifies parsing with short classpath flag -cp.
	 */
	@Test
	public void parseWithShortClasspathFlag() throws Exception
	{
		String[] args = {"-cp", "/lib.jar", "test.java"};
		CLIOptions options = parser.parse(args);

		assertEquals(options.classpathEntries(), List.of(Path.of("/lib.jar")),
			"Should parse classpath with -cp short flag");
	}

	/**
	 * Verifies parsing with short modulepath flag -p.
	 */
	@Test
	public void parseWithShortModulepathFlag() throws Exception
	{
		String[] args = {"-p", "/mod.jar", "test.java"};
		CLIOptions options = parser.parse(args);

		assertEquals(options.modulepathEntries(), List.of(Path.of("/mod.jar")),
			"Should parse modulepath with -p short flag");
	}

	/**
	 * Verifies that empty classpath value results in empty list.
	 */
	@Test
	public void parseWithEmptyClasspathValue() throws Exception
	{
		String[] args = {"--classpath", "", "test.java"};
		CLIOptions options = parser.parse(args);

		assertTrue(options.classpathEntries().isEmpty(),
			"Empty classpath value should result in empty list");
	}

	/**
	 * Verifies that relative paths are preserved.
	 */
	@Test
	public void parseWithRelativePath() throws Exception
	{
		String[] args = {"--classpath", "../lib/a.jar", "test.java"};
		CLIOptions options = parser.parse(args);

		assertEquals(options.classpathEntries(), List.of(Path.of("../lib/a.jar")),
			"Should preserve relative paths");
	}

	/**
	 * Verifies that paths with spaces are handled correctly.
	 */
	@Test
	public void parseWithSpacesInPath() throws Exception
	{
		String[] args = {"--classpath", "/path with spaces/lib.jar", "test.java"};
		CLIOptions options = parser.parse(args);

		assertEquals(options.classpathEntries(), List.of(Path.of("/path with spaces/lib.jar")),
			"Should preserve paths with spaces");
	}

	/**
	 * Verifies that omitting classpath and modulepath options results in empty lists.
	 */
	@Test
	public void parseWithNoClasspathOption() throws Exception
	{
		String[] args = {"test.java"};
		CLIOptions options = parser.parse(args);

		assertTrue(options.classpathEntries().isEmpty(),
			"Omitted classpath should result in empty list");
		assertTrue(options.modulepathEntries().isEmpty(),
			"Omitted modulepath should result in empty list");
	}
}
