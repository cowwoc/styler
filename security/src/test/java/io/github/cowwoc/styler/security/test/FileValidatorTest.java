package io.github.cowwoc.styler.security.test;

import io.github.cowwoc.styler.security.*;

import io.github.cowwoc.styler.security.exceptions.FileSizeLimitExceededException;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.testng.Assert.*;

/**
 * Tests for FileValidator security validation.
 * Thread-safe: each test creates its own temp directories.
 */
public class FileValidatorTest
{
	@Test
	public void validFilePassesValidation() throws Exception
	{
		FileValidator validator = new FileValidator();
		SecurityConfig config = SecurityConfig.DEFAULT;
		Path tempDir = Files.createTempDirectory("file-validator-test");

		try
		{
			Path file = tempDir.resolve("Test.java");
			Files.writeString(file, "public class Test {}");

			validator.validate(file, config);
			// No exception = success
		}
		finally
		{
			cleanupDirectory(tempDir);
		}
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void nonExistentFileFailsValidation() throws Exception
	{
		FileValidator validator = new FileValidator();
		SecurityConfig config = SecurityConfig.DEFAULT;
		Path tempDir = Files.createTempDirectory("file-validator-test");

		try
		{
			Path file = tempDir.resolve("NonExistent.java");
			validator.validate(file, config);
		}
		finally
		{
			cleanupDirectory(tempDir);
		}
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void directoryFailsValidation() throws Exception
	{
		FileValidator validator = new FileValidator();
		SecurityConfig config = SecurityConfig.DEFAULT;
		Path tempDir = Files.createTempDirectory("file-validator-test");

		try
		{
			Path dir = Files.createDirectory(tempDir.resolve("NotAFile.java"));
			validator.validate(dir, config);
		}
		finally
		{
			cleanupDirectory(tempDir);
		}
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void nonJavaFileFailsValidation() throws Exception
	{
		FileValidator validator = new FileValidator();
		SecurityConfig config = SecurityConfig.DEFAULT;
		Path tempDir = Files.createTempDirectory("file-validator-test");

		try
		{
			Path file = tempDir.resolve("Test.txt");
			Files.writeString(file, "text file");
			validator.validate(file, config);
		}
		finally
		{
			cleanupDirectory(tempDir);
		}
	}

	@Test(expectedExceptions = FileSizeLimitExceededException.class)
	public void oversizedFileFailsValidation() throws Exception
	{
		FileValidator validator = new FileValidator();
		Path tempDir = Files.createTempDirectory("file-validator-test");

		try
		{
			Path file = tempDir.resolve("Large.java");
			SecurityConfig smallConfig = new SecurityConfig.Builder().maxFileSizeBytes(10).build();

			Files.writeString(file, "This content exceeds 10 bytes");
			validator.validate(file, smallConfig);
		}
		finally
		{
			cleanupDirectory(tempDir);
		}
	}

	@Test
	public void fileAtExactLimitPasses() throws Exception
	{
		FileValidator validator = new FileValidator();
		Path tempDir = Files.createTempDirectory("file-validator-test");

		try
		{
			String content = "exact";
			Path file = tempDir.resolve("Exact.java");
			Files.writeString(file, content);

			SecurityConfig exactConfig = new SecurityConfig.Builder()
				.maxFileSizeBytes(content.length())
				.build();

			validator.validate(file, exactConfig);
		}
		finally
		{
			cleanupDirectory(tempDir);
		}
	}

	@Test(expectedExceptions = NullPointerException.class)
	public void nullFileThrowsException() throws Exception
	{
		FileValidator validator = new FileValidator();
		SecurityConfig config = SecurityConfig.DEFAULT;

		validator.validate(null, config);
	}

	@Test(expectedExceptions = NullPointerException.class)
	public void nullConfigThrowsException() throws Exception
	{
		FileValidator validator = new FileValidator();
		Path tempDir = Files.createTempDirectory("file-validator-test");

		try
		{
			Path file = tempDir.resolve("Test.java");
			Files.writeString(file, "test");
			validator.validate(file, null);
		}
		finally
		{
			cleanupDirectory(tempDir);
		}
	}

	private void cleanupDirectory(Path directory) throws IOException
	{
		if (directory != null && Files.exists(directory))
			Files.walk(directory)
				.sorted((a, b) -> -a.compareTo(b))
				.forEach(path -> {
					try
					{
						Files.deleteIfExists(path);
					}
					catch (IOException e)
					{
						// Ignore cleanup errors
					}
				});
	}
}
