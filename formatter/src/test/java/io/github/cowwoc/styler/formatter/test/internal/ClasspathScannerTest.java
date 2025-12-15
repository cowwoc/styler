package io.github.cowwoc.styler.formatter.test.internal;

import io.github.cowwoc.styler.formatter.TypeResolutionConfig;
import io.github.cowwoc.styler.formatter.internal.ClasspathScanner;
import org.testng.annotations.Test;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * Tests for ClasspathScanner class.
 */
public final class ClasspathScannerTest
{
	/**
	 * Tests that ClasspathScanner.classExists() returns {@code true} for a class that exists in a
	 * JAR file.
	 *
	 * @throws IOException if test JAR creation fails
	 */
	@Test
	public void classExistsForJarClass() throws IOException
	{
		Path jarPath = ClasspathTestUtils.createTestJar("com.example.Foo");
		TypeResolutionConfig config = new TypeResolutionConfig(List.of(jarPath), List.of());

		try (ClasspathScanner scanner = ClasspathScanner.create(config))
		{
			assertTrue(scanner.classExists("com.example.Foo"),
				"Should find class in JAR");
		}
		finally
		{
			Files.deleteIfExists(jarPath);
		}
	}

	/**
	 * Tests that ClasspathScanner.classExists() returns {@code false} for a class that does not
	 * exist in the classpath.
	 *
	 * @throws IOException if test JAR creation fails
	 */
	@Test
	public void classNotExistsForMissingClass() throws IOException
	{
		Path jarPath = ClasspathTestUtils.createTestJar("com.example.Foo");
		TypeResolutionConfig config = new TypeResolutionConfig(List.of(jarPath), List.of());

		try (ClasspathScanner scanner = ClasspathScanner.create(config))
		{
			assertFalse(scanner.classExists("com.example.Bar"),
				"Should not find missing class");
		}
		finally
		{
			Files.deleteIfExists(jarPath);
		}
	}

	/**
	 * Tests that ClasspathScanner.classExists() returns {@code true} for nested classes.
	 *
	 * @throws IOException if test JAR creation fails
	 */
	@Test
	public void classExistsForNestedClass() throws IOException
	{
		Path jarPath = ClasspathTestUtils.createTestJar("com.example.Outer$Inner");
		TypeResolutionConfig config = new TypeResolutionConfig(List.of(jarPath), List.of());

		try (ClasspathScanner scanner = ClasspathScanner.create(config))
		{
			assertTrue(scanner.classExists("com.example.Outer$Inner"),
				"Should find nested class");
		}
		finally
		{
			Files.deleteIfExists(jarPath);
		}
	}

	/**
	 * Tests that ClasspathScanner.classExists() works with directory classpath entries.
	 *
	 * @throws IOException if test directory creation fails
	 */
	@Test
	public void classExistsForDirectoryClass() throws IOException
	{
		Path directory = ClasspathTestUtils.createTestDirectory("com.example.Foo");
		TypeResolutionConfig config = new TypeResolutionConfig(List.of(directory), List.of());

		try (ClasspathScanner scanner = ClasspathScanner.create(config))
		{
			assertTrue(scanner.classExists("com.example.Foo"),
				"Should find class in directory");
		}
		finally
		{
			deleteRecursively(directory);
		}
	}

	/**
	 * Tests that ClasspathScanner.listPackageClasses() returns all classes in a package.
	 *
	 * @throws IOException if test JAR creation fails
	 */
	@Test
	public void listPackageClassesReturnsAllClasses() throws IOException
	{
		Path jarPath = ClasspathTestUtils.createTestJar("com.example.Foo", "com.example.Bar");
		TypeResolutionConfig config = new TypeResolutionConfig(List.of(jarPath), List.of());

		try (ClasspathScanner scanner = ClasspathScanner.create(config))
		{
			Set<String> classes = scanner.listPackageClasses("com.example");
			requireThat(classes, "classes").contains("com.example.Foo");
			requireThat(classes, "classes").contains("com.example.Bar");
		}
		finally
		{
			Files.deleteIfExists(jarPath);
		}
	}

	/**
	 * Tests that ClasspathScanner.listPackageClasses() includes nested classes in results.
	 *
	 * @throws IOException if test JAR creation fails
	 */
	@Test
	public void listPackageClassesIncludesNestedClasses() throws IOException
	{
		Path jarPath = ClasspathTestUtils.createTestJar("com.example.Outer",
			"com.example.Outer$Inner");
		TypeResolutionConfig config = new TypeResolutionConfig(List.of(jarPath), List.of());

		try (ClasspathScanner scanner = ClasspathScanner.create(config))
		{
			Set<String> classes = scanner.listPackageClasses("com.example");
			requireThat(classes, "classes").contains("com.example.Outer");
			requireThat(classes, "classes").contains("com.example.Outer$Inner");
		}
		finally
		{
			Files.deleteIfExists(jarPath);
		}
	}

	/**
	 * Tests that ClasspathScanner.listPackageClasses() returns an empty set for unknown packages.
	 *
	 * @throws IOException if test JAR creation fails
	 */
	@Test
	public void listPackageClassesReturnsEmptyForUnknownPackage() throws IOException
	{
		Path jarPath = ClasspathTestUtils.createTestJar("com.example.Foo");
		TypeResolutionConfig config = new TypeResolutionConfig(List.of(jarPath), List.of());

		try (ClasspathScanner scanner = ClasspathScanner.create(config))
		{
			Set<String> classes = scanner.listPackageClasses("com.other");
			requireThat(classes, "classes").isEmpty();
		}
		finally
		{
			Files.deleteIfExists(jarPath);
		}
	}

	/**
	 * Tests that ClasspathScanner.create() throws IllegalArgumentException for non-existent
	 * classpath entries.
	 *
	 * @throws IOException if temporary path creation fails
	 */
	@Test(expectedExceptions = IllegalArgumentException.class)
	public void createWithNonExistentPathThrows() throws IOException
	{
		Path nonExistent = Path.of("/nonexistent/path/to/lib.jar");
		TypeResolutionConfig config = new TypeResolutionConfig(List.of(nonExistent), List.of());

		ClasspathScanner.create(config);
	}

	/**
	 * Tests that ClasspathScanner.create() succeeds with empty configuration.
	 */
	@Test
	public void createWithEmptyConfigSucceeds()
	{
		try (ClasspathScanner scanner = ClasspathScanner.create(TypeResolutionConfig.EMPTY))
		{
			assertFalse(scanner.classExists("com.example.Foo"),
				"Empty scanner should not find any classes");
		}
	}

	/**
	 * Tests that ClasspathScanner implements AutoCloseable correctly and can be used in
	 * try-with-resources.
	 *
	 * @throws IOException if test JAR creation fails
	 */
	@Test
	public void scannerImplementsAutoCloseable() throws IOException
	{
		Path jarPath = ClasspathTestUtils.createTestJar("com.example.Test");
		TypeResolutionConfig config = new TypeResolutionConfig(List.of(jarPath), List.of());

		try (ClasspathScanner scanner = ClasspathScanner.create(config))
		{
			assertTrue(scanner.classExists("com.example.Test"),
				"Scanner should work in try-with-resources");
		}
		finally
		{
			Files.deleteIfExists(jarPath);
		}
	}

	/**
	 * Recursively deletes a directory and all its contents.
	 *
	 * @param directory the directory to delete
	 * @throws IOException if deletion fails
	 */
	private void deleteRecursively(Path directory) throws IOException
	{
		if (Files.exists(directory))
		{
			Files.walk(directory).
				sorted((a, b) -> b.compareTo(a)).
				forEach(path ->
				{
					try
					{
						Files.delete(path);
					}
					catch (IOException e)
					{
						throw new UncheckedIOException("Failed to delete: " + path, e);
					}
				});
		}
	}
}
