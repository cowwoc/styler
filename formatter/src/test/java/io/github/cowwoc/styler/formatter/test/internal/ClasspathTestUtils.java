package io.github.cowwoc.styler.formatter.test.internal;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

/**
 * Utility methods for creating test JARs containing class files for ClasspathScanner testing.
 */
public final class ClasspathTestUtils
{
	/**
	 * Prevent instantiation.
	 */
	private ClasspathTestUtils()
	{
	}

	/**
	 * Creates a temporary JAR file containing the specified class entries.
	 * <p>
	 * This method creates a minimal JAR file with empty class file entries. The JAR structure
	 * follows standard package naming conventions (e.g., {@code "com.example.Foo"} becomes
	 * {@code "com/example/Foo.class"}).
	 *
	 * @param fullyQualifiedClassNames the fully-qualified class names to include (e.g.,
	 *                                 {@code "com.example.Foo"})
	 * @return path to the created temporary JAR file
	 * @throws IOException          if JAR creation fails
	 * @throws NullPointerException if {@code fullyQualifiedClassNames} is {@code null}
	 */
	public static Path createTestJar(String... fullyQualifiedClassNames) throws IOException
	{
		Path jarPath = Files.createTempFile("test-classpath-", ".jar");
		try (OutputStream fileOut = Files.newOutputStream(jarPath);
		     JarOutputStream jarOut = new JarOutputStream(fileOut))
		{
			for (String className : fullyQualifiedClassNames)
			{
				String entryPath = className.replace('.', '/') + ".class";
				JarEntry entry = new JarEntry(entryPath);
				jarOut.putNextEntry(entry);
				jarOut.closeEntry();
			}
		}
		return jarPath;
	}

	/**
	 * Creates a temporary directory containing class files in package structure.
	 * <p>
	 * This method creates empty {@code .class} files in the appropriate package directory
	 * structure. For example, {@code "com.example.Foo"} creates
	 * {@code "com/example/Foo.class"}.
	 *
	 * @param fullyQualifiedClassNames the fully-qualified class names to include
	 * @return path to the created temporary directory
	 * @throws IOException          if directory or file creation fails
	 * @throws NullPointerException if {@code fullyQualifiedClassNames} is {@code null}
	 */
	public static Path createTestDirectory(String... fullyQualifiedClassNames) throws IOException
	{
		Path directory = Files.createTempDirectory("test-classpath-dir-");
		for (String className : fullyQualifiedClassNames)
		{
			String relativePath = className.replace('.', '/') + ".class";
			Path classFile = directory.resolve(relativePath);
			Files.createDirectories(classFile.getParent());
			Files.createFile(classFile);
		}
		return directory;
	}
}
