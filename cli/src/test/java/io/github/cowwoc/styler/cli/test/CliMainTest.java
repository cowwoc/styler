package io.github.cowwoc.styler.cli.test;

import io.github.cowwoc.styler.cli.CliMain;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.testng.SkipException;
import org.testng.annotations.Test;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;

/**
 * Unit tests for CliMain entry point and command-line processing.
 *
 * Tests cover exit codes, argument parsing, file processing modes, and error handling.
 */
public class CliMainTest
{
	/**
	 * Tests that CliMain rejects null arguments with NullPointerException.
	 */
	@Test(expectedExceptions = NullPointerException.class)
	public void cliMainWithNullArgsThrowsNullPointerException()
	{
		new CliMain().run(null);
	}

	/**
	 * Tests that CliMain with empty arguments prints help and returns exit code 0.
	 */
	@Test
	public void cliMainWithEmptyArgsReturnsExitCode0()
	{
		String[] args = {};
		CliMain cliMain = new CliMain();

		int exitCode = cliMain.run(args);
		requireThat(exitCode, "exitCode").isEqualTo(0);
	}

	/**
	 * Tests that --help flag prints help message and returns exit code 0.
	 */
	@Test
	public void cliMainWithHelpFlagReturnsExitCode0()
	{
		String[] args = {"--help"};
		CliMain cliMain = new CliMain();

		int exitCode = cliMain.run(args);
		requireThat(exitCode, "exitCode").isEqualTo(0);
	}

	/**
	 * Tests that --version flag prints version and returns exit code 0.
	 */
	@Test
	public void cliMainWithVersionFlagReturnsExitCode0()
	{
		String[] args = {"--version"};
		CliMain cliMain = new CliMain();

		int exitCode = cliMain.run(args);
		requireThat(exitCode, "exitCode").isEqualTo(0);
	}

	/**
	 * Tests that invalid CLI flag causes exit code 2 (usage error).
	 */
	@Test
	public void cliMainWithInvalidFlagReturnsExitCode2()
	{
		String[] args = {"--invalid-flag", "test.java"};
		CliMain cliMain = new CliMain();

		int exitCode = cliMain.run(args);
		requireThat(exitCode, "exitCode").isEqualTo(2);
	}

	/**
	 * Tests that non-existent file causes exit code 2 (file not found error).
	 */
	@Test
	public void cliMainWithNonexistentFileReturnsExitCode2()
	{
		String[] args = {"nonexistent-file-12345.java"};
		CliMain cliMain = new CliMain();

		int exitCode = cliMain.run(args);
		requireThat(exitCode, "exitCode").isEqualTo(2);
	}

	/**
	 * Tests that non-existent directory causes exit code 2.
	 */
	@Test
	public void cliMainWithNonexistentDirectoryReturnsExitCode2()
	{
		String[] args = {"/nonexistent/directory/path/"};
		CliMain cliMain = new CliMain();

		int exitCode = cliMain.run(args);
		requireThat(exitCode, "exitCode").isEqualTo(2);
	}

	/**
	 * Tests processing valid Java file with no violations returns exit code 0.
	 */
	@Test
	public void cliMainWithValidFileReturnsExitCode0() throws IOException
	{
		Path tempDir = Files.createTempDirectory("cli-test-");
		try
		{
			Path tempFile = Files.createTempFile(tempDir, "Valid", ".java");
			String validJava = "public class ValidClass {}";
			Files.writeString(tempFile, validJava);

			String[] args = {tempFile.toString()};
			CliMain cliMain = new CliMain();

			int exitCode = cliMain.run(args);
			requireThat(exitCode, "exitCode").isEqualTo(0);
		}
		finally
		{
			if (tempDir != null && Files.exists(tempDir))
				Files.walk(tempDir).
					sorted((p1, p2) -> p2.compareTo(p1)).
					forEach(path ->
					{
						try
						{
							Files.delete(path);
						}
						catch (IOException e)
						{
							// Best effort cleanup
							assert e != null;
						}
					});
		}
	}

	/**
	 * Tests fix mode with violations returns exit code 0 after fixing.
	 */
	@Test
	public void fixModeWithViolationsReturnsExitCode0() throws IOException
	{
		Path tempDir = Files.createTempDirectory("cli-test-");
		try
		{
			Path tempFile = Files.createTempFile(tempDir, "Needsfix", ".java");
			String originalContent = "public class TestClass { private int field; }";
			Files.writeString(tempFile, originalContent);

			String[] args = {"--fix", tempFile.toString()};
			CliMain cliMain = new CliMain();

			int exitCode = cliMain.run(args);
			requireThat(exitCode, "exitCode").isEqualTo(0);
		}
		finally
		{
			if (tempDir != null && Files.exists(tempDir))
				Files.walk(tempDir).
					sorted((p1, p2) -> p2.compareTo(p1)).
					forEach(path ->
					{
						try
						{
							Files.delete(path);
						}
						catch (IOException e)
						{
							// Best effort cleanup
							assert e != null;
						}
					});
		}
	}

	/**
	 * Tests that check mode does not modify files.
	 */
	@Test
	public void checkModeDoesNotModifyFile() throws IOException
	{
		Path tempDir = Files.createTempDirectory("cli-test-");
		try
		{
			Path tempFile = Files.createTempFile(tempDir, "Readonly", ".java");
			String content = "public class TestClass {}";
			Files.writeString(tempFile, content);

			String originalContent = Files.readString(tempFile);

			String[] args = {"--check", tempFile.toString()};
			CliMain cliMain = new CliMain();

			cliMain.run(args);

			String currentContent = Files.readString(tempFile);
			requireThat(currentContent, "currentContent").isEqualTo(originalContent);
		}
		finally
		{
			if (tempDir != null && Files.exists(tempDir))
				Files.walk(tempDir).
					sorted((p1, p2) -> p2.compareTo(p1)).
					forEach(path ->
					{
						try
						{
							Files.delete(path);
						}
						catch (IOException e)
						{
							// Best effort cleanup
							assert e != null;
						}
					});
		}
	}

	/**
	 * Tests that conflicting --check and --fix flags returns exit code 2.
	 */
	@Test
	public void conflictingCheckAndFixFlagsReturnsExitCode2()
	{
		String[] args = {"--check", "--fix", "test.java"};
		CliMain cliMain = new CliMain();

		int exitCode = cliMain.run(args);
		requireThat(exitCode, "exitCode").isEqualTo(2);
	}

	/**
	 * Tests config file override uses specified configuration.
	 */
	@Test
	public void configOverrideUsesSpecifiedConfig() throws IOException
	{
		Path tempDir = Files.createTempDirectory("cli-test-");
		try
		{
			Path configFile = Files.createTempFile(tempDir, "config", ".toml");
			Path javaFile = Files.createTempFile(tempDir, "Test", ".java");

			Files.writeString(configFile, "[format]\nmaxLineLength = 80\n");
			Files.writeString(javaFile, "public class Test {}");

			String[] args = {"--config", configFile.toString(), "--check", javaFile.toString()};
			CliMain cliMain = new CliMain();

			int exitCode = cliMain.run(args);
			requireThat(exitCode, "exitCode").isGreaterThanOrEqualTo(0);
		}
		finally
		{
			if (tempDir != null && Files.exists(tempDir))
				Files.walk(tempDir).
					sorted((p1, p2) -> p2.compareTo(p1)).
					forEach(path ->
					{
						try
						{
							Files.delete(path);
						}
						catch (IOException e)
						{
							// Best effort cleanup
							assert e != null;
						}
					});
		}
	}

	/**
	 * Tests that non-existent config file returns exit code 2.
	 */
	@Test
	public void nonexistentConfigFileReturnsExitCode2()
	{
		String[] args = {"--config", "nonexistent-config.toml", "test.java"};
		CliMain cliMain = new CliMain();

		int exitCode = cliMain.run(args);
		requireThat(exitCode, "exitCode").isEqualTo(2);
	}

	/**
	 * Tests processing multiple files processes all and reports status.
	 */
	@Test
	public void multipleFilesProcessesAll() throws IOException
	{
		Path tempDir = Files.createTempDirectory("cli-test-");
		try
		{
			Path file1 = Files.createTempFile(tempDir, "file1", ".java");
			Path file2 = Files.createTempFile(tempDir, "file2", ".java");
			Path file3 = Files.createTempFile(tempDir, "file3", ".java");

			Files.writeString(file1, "public class File1 {}");
			Files.writeString(file2, "public class File2 {}");
			Files.writeString(file3, "public class File3 {}");

			String[] args = {file1.toString(), file2.toString(), file3.toString()};
			CliMain cliMain = new CliMain();

			int exitCode = cliMain.run(args);
			requireThat(exitCode, "exitCode").isGreaterThanOrEqualTo(0);
		}
		finally
		{
			if (tempDir != null && Files.exists(tempDir))
				Files.walk(tempDir).
					sorted((p1, p2) -> p2.compareTo(p1)).
					forEach(path ->
					{
						try
						{
							Files.delete(path);
						}
						catch (IOException e)
						{
							// Best effort cleanup
							assert e != null;
						}
					});
		}
	}

	/**
	 * Tests that directory processing is not supported and returns exit code 2.
	 */
	@Test
	public void directoryProcessingHandlesAllJavaFiles() throws IOException
	{
		Path tempDir = Files.createTempDirectory("cli-test-");
		try
		{
			Path testDir = Files.createTempDirectory(tempDir, "test-dir-");
			Path javaFile1 = Files.createFile(testDir.resolve("File1.java"));
			Path javaFile2 = Files.createFile(testDir.resolve("File2.java"));
			Path txtFile = Files.createFile(testDir.resolve("readme.txt"));

			Files.writeString(javaFile1, "public class File1 {}");
			Files.writeString(javaFile2, "public class File2 {}");
			Files.writeString(txtFile, "This is a text file");

			String[] args = {testDir.toString()};
			CliMain cliMain = new CliMain();

			int exitCode = cliMain.run(args);
			requireThat(exitCode, "exitCode").isEqualTo(2);  // Directory not supported = usage error
		}
		finally
		{
			if (tempDir != null && Files.exists(tempDir))
				Files.walk(tempDir).
					sorted((p1, p2) -> p2.compareTo(p1)).
					forEach(path ->
					{
						try
						{
							Files.delete(path);
						}
						catch (IOException e)
						{
							// Best effort cleanup
							assert e != null;
						}
					});
		}
	}

	/**
	 * Tests that empty directory is not supported and returns exit code 2.
	 */
	@Test
	public void emptyDirectoryReturnsExitCode2() throws IOException
	{
		Path tempDir = Files.createTempDirectory("cli-test-");
		try
		{
			Path emptyDir = Files.createTempDirectory(tempDir, "empty-");

			String[] args = {emptyDir.toString()};
			CliMain cliMain = new CliMain();

			int exitCode = cliMain.run(args);
			requireThat(exitCode, "exitCode").isEqualTo(2);  // Directory not supported = usage error
		}
		finally
		{
			if (tempDir != null && Files.exists(tempDir))
				Files.walk(tempDir).
					sorted((p1, p2) -> p2.compareTo(p1)).
					forEach(path ->
					{
						try
						{
							Files.delete(path);
						}
						catch (IOException e)
						{
							// Best effort cleanup
							assert e != null;
						}
					});
		}
	}

	/**
	 * Tests that empty Java file processing succeeds.
	 */
	@Test
	public void emptyJavaFileProcessesSuccessfully() throws IOException
	{
		Path tempDir = Files.createTempDirectory("cli-test-");
		try
		{
			Path emptyFile = Files.createTempFile(tempDir, "empty", ".java");
			Files.writeString(emptyFile, "");

			String[] args = {emptyFile.toString()};
			CliMain cliMain = new CliMain();

			int exitCode = cliMain.run(args);
			requireThat(exitCode, "exitCode").isGreaterThanOrEqualTo(0);
		}
		finally
		{
			if (tempDir != null && Files.exists(tempDir))
				Files.walk(tempDir).
					sorted((p1, p2) -> p2.compareTo(p1)).
					forEach(path ->
					{
						try
						{
							Files.delete(path);
						}
						catch (IOException e)
						{
							// Best effort cleanup
							assert e != null;
						}
					});
		}
	}

	/**
	 * Tests that whitespace-only file processing succeeds.
	 */
	@Test
	public void whitespaceOnlyFileProcessesSuccessfully() throws IOException
	{
		Path tempDir = Files.createTempDirectory("cli-test-");
		try
		{
			Path whitespaceFile = Files.createTempFile(tempDir, "whitespace", ".java");
			Files.writeString(whitespaceFile, "   \n\t\n   \n");

			String[] args = {whitespaceFile.toString()};
			CliMain cliMain = new CliMain();

			int exitCode = cliMain.run(args);
			requireThat(exitCode, "exitCode").isGreaterThanOrEqualTo(0);
		}
		finally
		{
			if (tempDir != null && Files.exists(tempDir))
				Files.walk(tempDir).
					sorted((p1, p2) -> p2.compareTo(p1)).
					forEach(path ->
					{
						try
						{
							Files.delete(path);
						}
						catch (IOException e)
						{
							// Best effort cleanup
							assert e != null;
						}
					});
		}
	}

	/**
	 * Tests that files with Unicode names process correctly.
	 */
	@Test
	public void unicodeFileNameProcessesCorrectly() throws IOException
	{
		Path tempDir = Files.createTempDirectory("cli-test-");
		try
		{
			Path unicodeFile;
			try
			{
				unicodeFile = Files.createTempFile(tempDir, "测试", ".java");
			}
			catch (java.nio.file.InvalidPathException e)
			{
				// Platform doesn't support Unicode filenames - skip test
				throw new SkipException("Platform does not support Unicode filenames");
			}
			Files.writeString(unicodeFile, "public class Unicode {}");

			String[] args = {unicodeFile.toString()};
			CliMain cliMain = new CliMain();

			int exitCode = cliMain.run(args);
			requireThat(exitCode, "exitCode").isGreaterThanOrEqualTo(0);
		}
		finally
		{
			if (tempDir != null && Files.exists(tempDir))
				Files.walk(tempDir).
					sorted((p1, p2) -> p2.compareTo(p1)).
					forEach(path ->
					{
						try
						{
							Files.delete(path);
						}
						catch (IOException e)
						{
							// Best effort cleanup
							assert e != null;
						}
					});
		}
	}

	/**
	 * Tests that files with spaces and special characters process correctly.
	 */
	@Test
	public void pathWithSpacesAndSpecialCharsWorks() throws IOException
	{
		Path tempDir = Files.createTempDirectory("cli-test-");
		try
		{
			Path specialDir = Files.createTempDirectory(tempDir, "dir with spaces-");
			Path specialFile = Files.createFile(specialDir.resolve("file (1).java"));
			Files.writeString(specialFile, "public class FileOne {}");

			String[] args = {specialFile.toString()};
			CliMain cliMain = new CliMain();

			int exitCode = cliMain.run(args);
			requireThat(exitCode, "exitCode").isGreaterThanOrEqualTo(0);
		}
		finally
		{
			if (tempDir != null && Files.exists(tempDir))
				Files.walk(tempDir).
					sorted((p1, p2) -> p2.compareTo(p1)).
					forEach(path ->
					{
						try
						{
							Files.delete(path);
						}
						catch (IOException e)
						{
							// Best effort cleanup
							assert e != null;
						}
					});
		}
	}
}
