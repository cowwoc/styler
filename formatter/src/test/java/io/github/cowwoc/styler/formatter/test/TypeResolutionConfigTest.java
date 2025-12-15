package io.github.cowwoc.styler.formatter.test;

import io.github.cowwoc.styler.formatter.TypeResolutionConfig;
import org.testng.annotations.Test;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * Tests for TypeResolutionConfig record.
 */
public final class TypeResolutionConfigTest
{
	/**
	 * Verifies that empty config has no classpath access.
	 */
	@Test
	public void emptyConfigHasNoClasspathAccess()
	{
		assertFalse(TypeResolutionConfig.EMPTY.hasClasspathAccess(),
			"Empty config should have no classpath access");
	}

	/**
	 * Verifies that config with classpath entries has access.
	 */
	@Test
	public void configWithClasspathHasAccess()
	{
		TypeResolutionConfig config = new TypeResolutionConfig(
			List.of(Path.of("/lib/test.jar")),
			List.of());

		assertTrue(config.hasClasspathAccess(),
			"Config with classpath entries should have access");
	}

	/**
	 * Verifies that config with modulepath entries has access.
	 */
	@Test
	public void configWithModulepathHasAccess()
	{
		TypeResolutionConfig config = new TypeResolutionConfig(
			List.of(),
			List.of(Path.of("/mod/test.jar")));

		assertTrue(config.hasClasspathAccess(),
			"Config with modulepath entries should have access");
	}

	/**
	 * Verifies that config defensively copies classpath entries.
	 */
	@Test
	public void configDefensivelyCopiesClasspath()
	{
		List<Path> mutableList = new ArrayList<>();
		mutableList.add(Path.of("/lib/original.jar"));

		TypeResolutionConfig config = new TypeResolutionConfig(mutableList, List.of());

		// Modify original list after construction
		mutableList.add(Path.of("/lib/added.jar"));

		// Config should not be affected
		assertTrue(config.classpathEntries().size() == 1,
			"Config should have defensive copy, not affected by external modifications");
	}

	/**
	 * Verifies that classpathEntries returns immutable list.
	 */
	@Test(expectedExceptions = UnsupportedOperationException.class)
	public void classpathEntriesReturnsImmutableList()
	{
		TypeResolutionConfig config = new TypeResolutionConfig(
			List.of(Path.of("/lib/test.jar")),
			List.of());

		config.classpathEntries().add(Path.of("/lib/extra.jar"));
	}
}
