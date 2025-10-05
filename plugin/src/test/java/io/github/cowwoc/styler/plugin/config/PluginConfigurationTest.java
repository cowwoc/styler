package io.github.cowwoc.styler.plugin.config;

import org.apache.maven.project.MavenProject;
import org.testng.annotations.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.testng.Assert.*;

/**
 * Unit tests for PluginConfiguration record and Builder.
 * Validates defensive copying, null handling, and immutability.
 */
public class PluginConfigurationTest
{
	/**
	 * Verifies that Builder performs defensive copying of includes list.
	 */
	@Test
	public void testBuilderDefensiveCopyingOfIncludes()
	{
		List<String> includes = new ArrayList<>();
		includes.add("**/*.java");

		PluginConfiguration config = PluginConfiguration.builder().
			project(new MavenProject()).
			sourceDirectory(new File("src")).
			encoding("UTF-8").
			includes(includes).
			build();

		// Mutate original list
		includes.add("malicious");

		// Verify config list unchanged (defensive copy worked)
		assertEquals(config.includes().size(), 1);
		assertEquals(config.includes().get(0), "**/*.java");
	}

	/**
	 * Verifies that Builder performs defensive copying of excludes list.
	 */
	@Test
	public void testBuilderDefensiveCopyingOfExcludes()
	{
		List<String> excludes = new ArrayList<>();
		excludes.add("**/*Test.java");

		PluginConfiguration config = PluginConfiguration.builder().
			project(new MavenProject()).
			sourceDirectory(new File("src")).
			encoding("UTF-8").
			excludes(excludes).
			build();

		// Mutate original list
		excludes.add("malicious");

		// Verify config list unchanged (defensive copy worked)
		assertEquals(config.excludes().size(), 1);
		assertEquals(config.excludes().get(0), "**/*Test.java");
	}

	/**
	 * Verifies that null project parameter throws NullPointerException.
	 */
	@Test(expectedExceptions = NullPointerException.class)
	public void testNullProjectThrowsNPE()
	{
		PluginConfiguration.builder().
			project(null).
			sourceDirectory(new File("src")).
			encoding("UTF-8").
			build();
	}

	/**
	 * Verifies that null sourceDirectory parameter throws NullPointerException.
	 */
	@Test(expectedExceptions = NullPointerException.class)
	public void testNullSourceDirectoryThrowsNPE()
	{
		PluginConfiguration.builder().
			project(new MavenProject()).
			sourceDirectory(null).
			encoding("UTF-8").
			build();
	}

	/**
	 * Verifies that null encoding parameter throws NullPointerException.
	 */
	@Test(expectedExceptions = NullPointerException.class)
	public void testNullEncodingThrowsNPE()
	{
		PluginConfiguration.builder().
			project(new MavenProject()).
			sourceDirectory(new File("src")).
			encoding(null).
			build();
	}

	/**
	 * Verifies that null includes list is replaced with empty list.
	 */
	@Test
	public void testNullIncludesReplacedWithEmptyList()
	{
		PluginConfiguration config = PluginConfiguration.builder().
			project(new MavenProject()).
			sourceDirectory(new File("src")).
			encoding("UTF-8").
			includes(null).
			build();

		assertNotNull(config.includes());
		assertTrue(config.includes().isEmpty());
	}

	/**
	 * Verifies that null excludes list is replaced with empty list.
	 */
	@Test
	public void testNullExcludesReplacedWithEmptyList()
	{
		PluginConfiguration config = PluginConfiguration.builder().
			project(new MavenProject()).
			sourceDirectory(new File("src")).
			encoding("UTF-8").
			excludes(null).
			build();

		assertNotNull(config.excludes());
		assertTrue(config.excludes().isEmpty());
	}

	/**
	 * Verifies Builder default values are applied correctly.
	 */
	@Test
	public void testBuilderDefaults()
	{
		PluginConfiguration config = PluginConfiguration.builder().
			project(new MavenProject()).
			sourceDirectory(new File("src")).
			build();

		assertEquals(config.encoding(), PluginConfiguration.Builder.DEFAULT_ENCODING);
		assertEquals(config.includes(), PluginConfiguration.Builder.DEFAULT_INCLUDES);
		assertEquals(config.excludes(), PluginConfiguration.Builder.DEFAULT_EXCLUDES);
		assertFalse(config.skip());
		assertFalse(config.skipTests());
		assertTrue(config.failOnViolation());
	}

	/**
	 * Verifies that configuration record is immutable after construction.
	 */
	@Test(expectedExceptions = UnsupportedOperationException.class)
	public void testConfigurationIsImmutable()
	{
		PluginConfiguration config = PluginConfiguration.builder().
			project(new MavenProject()).
			sourceDirectory(new File("src")).
			encoding("UTF-8").
			build();

		// Attempt to modify includes list should throw
		config.includes().add("malicious");
	}
}
