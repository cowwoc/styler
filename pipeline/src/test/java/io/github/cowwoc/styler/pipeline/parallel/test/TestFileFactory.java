package io.github.cowwoc.styler.pipeline.parallel.test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Factory for creating test Java files.
 */
public final class TestFileFactory
{
	private TestFileFactory()
	{
		// Prevent instantiation
	}

	private static final String VALID_JAVA_CONTENT = """
		package io.github.cowwoc.styler.test;

		public class TestClass {
		    private String field;

		    public void method() {
		        System.out.println("Hello");
		    }
		}
		""";

	private static final String MALFORMED_JAVA_CONTENT = """
		public class Malformed {
		    public void unclosed(
		""";

	/**
	 * Creates a valid Java file in a temporary directory.
	 *
	 * @return {@code Path} to the created file
	 * @throws IOException if file creation fails
	 */
	public static Path createValidJavaFile() throws IOException
	{
		Path tempDir = Files.createTempDirectory("test-");
		Path file = tempDir.resolve("TestClass.java");
		Files.writeString(file, VALID_JAVA_CONTENT);
		return file;
	}

	/**
	 * Creates a malformed Java file in a temporary directory.
	 *
	 * @return {@code Path} to the created file
	 * @throws IOException if file creation fails
	 */
	public static Path createMalformedJavaFile() throws IOException
	{
		Path tempDir = Files.createTempDirectory("test-");
		Path file = tempDir.resolve("Malformed.java");
		Files.writeString(file, MALFORMED_JAVA_CONTENT);
		return file;
	}

	/**
	 * Creates multiple valid Java files.
	 *
	 * @param count number of files to create
	 * @return {@code List} of paths to created files
	 * @throws IOException if file creation fails
	 */
	public static List<Path> createValidJavaFiles(int count) throws IOException
	{
		List<Path> files = new ArrayList<>();
		for (int i = 0; i < count; ++i)
		{
			files.add(createValidJavaFile());
		}
		return files;
	}

	/**
	 * Deletes test files silently, ignoring any errors.
	 *
	 * @param files the files to delete
	 */
	public static void deleteFilesQuietly(List<Path> files)
	{
		for (Path file : files)
		{
			try
			{
				Files.deleteIfExists(file);
			}
			catch (IOException _)
			{
				// Intentionally ignored during cleanup
			}
		}
	}
}
