package io.github.cowwoc.styler.pipeline.internal.test;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.util.List;
import java.util.Locale;

import org.testng.annotations.Test;

import io.github.cowwoc.styler.formatter.TypeResolutionConfig;
import io.github.cowwoc.styler.formatter.internal.ClasspathScanner;
import io.github.cowwoc.styler.pipeline.CompilationValidationResult;
import io.github.cowwoc.styler.pipeline.internal.CompilationValidator;

/**
 * Tests for compilation validation before formatting.
 * <p>
 * Validates that source files have corresponding compiled class files on the classpath, and that
 * class files are not stale (source file modified after compilation).
 */
public final class CompilationValidatorTest
{
	// ===============================
	// Core Validation Tests (Phase 1)
	// ===============================

	/**
	 * Verifies that validation passes when the class file exists and is newer than the source file.
	 * This is the normal case after a successful compilation.
	 *
	 * @throws IOException if file creation fails
	 */
	@Test
	public void shouldPassValidationWhenClassFileExistsAndNewer() throws IOException
	{
		Path tempDir = Files.createTempDirectory("compilation-test-");
		try
		{
			// Create source file
			Path sourceFile = tempDir.resolve("Foo.java");
			Files.writeString(sourceFile, """
				public class Foo
				{
				}
				""");

			// Create classpath directory and class file
			Path classesDir = tempDir.resolve("classes");
			Files.createDirectories(classesDir);
			Path classFile = classesDir.resolve("Foo.class");
			Files.writeString(classFile, "fake class file content");

			// Set timestamps: class file newer than source
			Instant sourceTime = Instant.now().minusSeconds(60);
			Instant classTime = Instant.now();
			Files.setLastModifiedTime(sourceFile, FileTime.from(sourceTime));
			Files.setLastModifiedTime(classFile, FileTime.from(classTime));

			TypeResolutionConfig config = new TypeResolutionConfig(List.of(classesDir), List.of());
			try (ClasspathScanner scanner = ClasspathScanner.create(config))
			{
				CompilationValidator validator = new CompilationValidator(scanner);
				CompilationValidationResult result = validator.validate(sourceFile, "", List.of("Foo"));

				requireThat(result, "result").isInstanceOf(CompilationValidationResult.Valid.class);
			}
		}
		finally
		{
			deleteDirectoryRecursively(tempDir);
		}
	}

	/**
	 * Verifies that validation passes when the class file has the same timestamp as the source file.
	 * This edge case can occur when compilation happens very quickly.
	 *
	 * @throws IOException if file creation fails
	 */
	@Test
	public void shouldPassValidationWhenClassFileSameTimestamp() throws IOException
	{
		Path tempDir = Files.createTempDirectory("compilation-test-");
		try
		{
			Path sourceFile = tempDir.resolve("Bar.java");
			Files.writeString(sourceFile, """
				public class Bar
				{
				}
				""");

			Path classesDir = tempDir.resolve("classes");
			Files.createDirectories(classesDir);
			Path classFile = classesDir.resolve("Bar.class");
			Files.writeString(classFile, "fake class file content");

			// Set same timestamp for both files
			Instant timestamp = Instant.now();
			Files.setLastModifiedTime(sourceFile, FileTime.from(timestamp));
			Files.setLastModifiedTime(classFile, FileTime.from(timestamp));

			TypeResolutionConfig config = new TypeResolutionConfig(List.of(classesDir), List.of());
			try (ClasspathScanner scanner = ClasspathScanner.create(config))
			{
				CompilationValidator validator = new CompilationValidator(scanner);
				CompilationValidationResult result = validator.validate(sourceFile, "", List.of("Bar"));

				requireThat(result, "result").isInstanceOf(CompilationValidationResult.Valid.class);
			}
		}
		finally
		{
			deleteDirectoryRecursively(tempDir);
		}
	}

	/**
	 * Verifies that validation fails when the class file does not exist on the classpath.
	 * This occurs when a source file has never been compiled.
	 *
	 * @throws IOException if file creation fails
	 */
	@Test
	public void shouldFailValidationWhenClassFileMissing() throws IOException
	{
		Path tempDir = Files.createTempDirectory("compilation-test-");

		try
		{
			Path sourceFile = tempDir.resolve("Missing.java");
			Files.writeString(sourceFile, """
				public class Missing
				{
				}
				""");

			// Create classpath directory but NO class file
			Path classesDir = tempDir.resolve("classes");
			Files.createDirectories(classesDir);

			TypeResolutionConfig config = new TypeResolutionConfig(List.of(classesDir), List.of());
			try (ClasspathScanner scanner = ClasspathScanner.create(config))
			{
				CompilationValidator validator = new CompilationValidator(scanner);
				CompilationValidationResult result = validator.validate(sourceFile, "", List.of("Missing"));

				requireThat(result, "result").isInstanceOf(CompilationValidationResult.Invalid.class);
				CompilationValidationResult.Invalid invalid = (CompilationValidationResult.Invalid) result;
				requireThat(invalid.missingClasses(), "missingClasses").contains("Missing");
			}
		}
		finally
		{
			deleteDirectoryRecursively(tempDir);
		}
	}

	/**
	 * Verifies that validation fails when the class file is older than the source file.
	 * This occurs when the source file was modified after the last compilation.
	 *
	 * @throws IOException if file creation fails
	 */
	@Test
	public void shouldFailValidationWhenClassFileStale() throws IOException
	{
		Path tempDir = Files.createTempDirectory("compilation-test-");

		try
		{
			Path sourceFile = tempDir.resolve("Stale.java");
			Files.writeString(sourceFile, """
				public class Stale
				{
				}
				""");

			Path classesDir = tempDir.resolve("classes");
			Files.createDirectories(classesDir);

			Path classFile = classesDir.resolve("Stale.class");
			Files.writeString(classFile, "fake class file content");

			// Set timestamps: source file newer than class file (stale class)
			Instant classTime = Instant.now().minusSeconds(60);
			Instant sourceTime = Instant.now();
			Files.setLastModifiedTime(classFile, FileTime.from(classTime));
			Files.setLastModifiedTime(sourceFile, FileTime.from(sourceTime));

			TypeResolutionConfig config = new TypeResolutionConfig(List.of(classesDir), List.of());
			try (ClasspathScanner scanner = ClasspathScanner.create(config))
			{
				CompilationValidator validator = new CompilationValidator(scanner);
				CompilationValidationResult result = validator.validate(sourceFile, "", List.of("Stale"));

				requireThat(result, "result").isInstanceOf(CompilationValidationResult.Invalid.class);
				CompilationValidationResult.Invalid invalid = (CompilationValidationResult.Invalid) result;
				requireThat(invalid.staleClasses(), "staleClasses").contains("Stale");
			}
		}
		finally
		{
			deleteDirectoryRecursively(tempDir);
		}
	}

	/**
	 * Verifies that validation fails when the class file is not found on any classpath entry.
	 * This tests the case where classpath is provided but the specific class is not on it.
	 *
	 * @throws IOException if file creation fails
	 */
	@Test
	public void shouldFailWhenClassNotFoundOnClasspath() throws IOException
	{
		Path tempDir = Files.createTempDirectory("compilation-test-");

		try
		{
			Path sourceFile = tempDir.resolve("NotOnPath.java");
			Files.writeString(sourceFile, """
				public class NotOnPath
				{
				}
				""");

			// Create classpath directory with a DIFFERENT class file
			Path classesDir = tempDir.resolve("classes");
			Files.createDirectories(classesDir);
			Path otherClassFile = classesDir.resolve("Other.class");
			Files.writeString(otherClassFile, "fake class file content");

			TypeResolutionConfig config = new TypeResolutionConfig(List.of(classesDir), List.of());
			try (ClasspathScanner scanner = ClasspathScanner.create(config))
			{
				CompilationValidator validator = new CompilationValidator(scanner);
				CompilationValidationResult result = validator.validate(sourceFile, "", List.of("NotOnPath"));

				requireThat(result, "result").isInstanceOf(CompilationValidationResult.Invalid.class);
				CompilationValidationResult.Invalid invalid = (CompilationValidationResult.Invalid) result;
				requireThat(invalid.missingClasses(), "missingClasses").contains("NotOnPath");
			}
		}
		finally
		{
			deleteDirectoryRecursively(tempDir);
		}
	}

	// ================================
	// FQN Mapping Tests (Phase 2)
	// ================================

	/**
	 * Verifies that a simple class (no package) maps to the correct class file path.
	 *
	 * @throws IOException if file creation fails
	 */
	@Test
	public void shouldMapSimpleClassToCorrectPath() throws IOException
	{
		Path tempDir = Files.createTempDirectory("compilation-test-");

		try
		{
			Path sourceFile = tempDir.resolve("Simple.java");
			Files.writeString(sourceFile, """
				public class Simple
				{
				}
				""");

			Path classesDir = tempDir.resolve("classes");
			Files.createDirectories(classesDir);

			// Class file should be directly in classpath root for default package
			Path classFile = classesDir.resolve("Simple.class");
			Files.writeString(classFile, "fake class file content");

			Instant classTime = Instant.now();
			Instant sourceTime = Instant.now().minusSeconds(10);
			Files.setLastModifiedTime(sourceFile, FileTime.from(sourceTime));
			Files.setLastModifiedTime(classFile, FileTime.from(classTime));

			TypeResolutionConfig config = new TypeResolutionConfig(List.of(classesDir), List.of());
			try (ClasspathScanner scanner = ClasspathScanner.create(config))
			{
				CompilationValidator validator = new CompilationValidator(scanner);
				CompilationValidationResult result = validator.validate(sourceFile, "", List.of("Simple"));

				requireThat(result, "result").isInstanceOf(CompilationValidationResult.Valid.class);
			}
		}
		finally
		{
			deleteDirectoryRecursively(tempDir);
		}
	}

	/**
	 * Verifies that a packaged class maps to the correct nested class file path.
	 *
	 * @throws IOException if file creation fails
	 */
	@Test
	public void shouldMapPackagedClassToCorrectPath() throws IOException
	{
		Path tempDir = Files.createTempDirectory("compilation-test-");

		try
		{
			// Create source in package structure
			Path srcDir = tempDir.resolve("src").resolve("com").resolve("example");
			Files.createDirectories(srcDir);
			Path sourceFile = srcDir.resolve("Packaged.java");
			Files.writeString(sourceFile, """
				package com.example;

				public class Packaged
				{
				}
				""");

			// Create class file in proper package path
			Path classesDir = tempDir.resolve("classes");
			Path packageDir = classesDir.resolve("com").resolve("example");
			Files.createDirectories(packageDir);

			Path classFile = packageDir.resolve("Packaged.class");
			Files.writeString(classFile, "fake class file content");

			Instant classTime = Instant.now();
			Instant sourceTime = Instant.now().minusSeconds(10);
			Files.setLastModifiedTime(sourceFile, FileTime.from(sourceTime));
			Files.setLastModifiedTime(classFile, FileTime.from(classTime));

			TypeResolutionConfig config = new TypeResolutionConfig(List.of(classesDir), List.of());
			try (ClasspathScanner scanner = ClasspathScanner.create(config))
			{
				CompilationValidator validator = new CompilationValidator(scanner);
				CompilationValidationResult result = validator.validate(
					sourceFile, "com.example", List.of("Packaged"));

				requireThat(result, "result").isInstanceOf(CompilationValidationResult.Valid.class);
			}
		}
		finally
		{
			deleteDirectoryRecursively(tempDir);
		}
	}

	/**
	 * Verifies that a class in the default package (no package declaration) maps correctly.
	 *
	 * @throws IOException if file creation fails
	 */
	@Test
	public void shouldMapDefaultPackageClassToCorrectPath() throws IOException
	{
		Path tempDir = Files.createTempDirectory("compilation-test-");

		try
		{
			Path sourceFile = tempDir.resolve("DefaultPkg.java");
			Files.writeString(sourceFile, """
				public class DefaultPkg
				{
					public void doSomething()
					{
					}
				}
				""");

			Path classesDir = tempDir.resolve("classes");
			Files.createDirectories(classesDir);

			// No package = class file at root of classpath
			Path classFile = classesDir.resolve("DefaultPkg.class");
			Files.writeString(classFile, "fake class file content");

			Instant classTime = Instant.now();
			Instant sourceTime = Instant.now().minusSeconds(10);
			Files.setLastModifiedTime(sourceFile, FileTime.from(sourceTime));
			Files.setLastModifiedTime(classFile, FileTime.from(classTime));

			TypeResolutionConfig config = new TypeResolutionConfig(List.of(classesDir), List.of());
			try (ClasspathScanner scanner = ClasspathScanner.create(config))
			{
				CompilationValidator validator = new CompilationValidator(scanner);
				// Empty string for packageName indicates default package
				CompilationValidationResult result = validator.validate(sourceFile, "", List.of("DefaultPkg"));

				requireThat(result, "result").isInstanceOf(CompilationValidationResult.Valid.class);
			}
		}
		finally
		{
			deleteDirectoryRecursively(tempDir);
		}
	}

	// ================================
	// Error Reporting Tests (Phase 3)
	// ================================

	/**
	 * Verifies that the error message for a missing class file includes the source file path.
	 *
	 * @throws IOException if file creation fails
	 */
	@Test
	public void shouldReportMissingClassFileWithSourcePath() throws IOException
	{
		Path tempDir = Files.createTempDirectory("compilation-test-");

		try
		{
			Path sourceFile = tempDir.resolve("MissingReport.java");
			Files.writeString(sourceFile, """
				public class MissingReport
				{
				}
				""");

			Path classesDir = tempDir.resolve("classes");
			Files.createDirectories(classesDir);

			TypeResolutionConfig config = new TypeResolutionConfig(List.of(classesDir), List.of());
			try (ClasspathScanner scanner = ClasspathScanner.create(config))
			{
				CompilationValidator validator = new CompilationValidator(scanner);
				CompilationValidationResult result = validator.validate(
					sourceFile, "", List.of("MissingReport"));

				requireThat(result, "result").isInstanceOf(CompilationValidationResult.Invalid.class);
				CompilationValidationResult.Invalid invalid = (CompilationValidationResult.Invalid) result;
				String errorMessage = invalid.getErrorMessage();

				requireThat(errorMessage, "errorMessage").contains(sourceFile.toString());
			}
		}
		finally
		{
			deleteDirectoryRecursively(tempDir);
		}
	}

	/**
	 * Verifies that the error message for a stale class file includes the source file path.
	 *
	 * @throws IOException if file creation fails
	 */
	@Test
	public void shouldReportStaleClassFileWithSourcePath() throws IOException
	{
		Path tempDir = Files.createTempDirectory("compilation-test-");

		try
		{
			Path sourceFile = tempDir.resolve("StaleReport.java");
			Files.writeString(sourceFile, """
				public class StaleReport
				{
				}
				""");

			Path classesDir = tempDir.resolve("classes");
			Files.createDirectories(classesDir);

			Path classFile = classesDir.resolve("StaleReport.class");
			Files.writeString(classFile, "fake class file content");

			// Make source file newer
			Instant classTime = Instant.now().minusSeconds(60);
			Instant sourceTime = Instant.now();
			Files.setLastModifiedTime(classFile, FileTime.from(classTime));
			Files.setLastModifiedTime(sourceFile, FileTime.from(sourceTime));

			TypeResolutionConfig config = new TypeResolutionConfig(List.of(classesDir), List.of());
			try (ClasspathScanner scanner = ClasspathScanner.create(config))
			{
				CompilationValidator validator = new CompilationValidator(scanner);
				CompilationValidationResult result = validator.validate(
					sourceFile, "", List.of("StaleReport"));

				requireThat(result, "result").isInstanceOf(CompilationValidationResult.Invalid.class);
				CompilationValidationResult.Invalid invalid = (CompilationValidationResult.Invalid) result;
				String errorMessage = invalid.getErrorMessage();

				requireThat(errorMessage, "errorMessage").contains(sourceFile.toString());
			}
		}
		finally
		{
			deleteDirectoryRecursively(tempDir);
		}
	}

	/**
	 * Verifies that the error message includes actionable guidance for the user.
	 *
	 * @throws IOException if file creation fails
	 */
	@Test
	public void shouldReportActionableGuidanceInErrorMessage() throws IOException
	{
		Path tempDir = Files.createTempDirectory("compilation-test-");

		try
		{
			Path sourceFile = tempDir.resolve("Actionable.java");
			Files.writeString(sourceFile, """
				public class Actionable
				{
				}
				""");

			Path classesDir = tempDir.resolve("classes");
			Files.createDirectories(classesDir);

			TypeResolutionConfig config = new TypeResolutionConfig(List.of(classesDir), List.of());
			try (ClasspathScanner scanner = ClasspathScanner.create(config))
			{
				CompilationValidator validator = new CompilationValidator(scanner);
				CompilationValidationResult result = validator.validate(sourceFile, "", List.of("Actionable"));

				requireThat(result, "result").isInstanceOf(CompilationValidationResult.Invalid.class);
				CompilationValidationResult.Invalid invalid = (CompilationValidationResult.Invalid) result;
				String errorMessage = invalid.getErrorMessage();

				// Error message should include guidance like "Run 'mvn compile'" or similar
				requireThat(errorMessage.toLowerCase(Locale.ROOT), "errorMessage").contains("compile");
			}
		}
		finally
		{
			deleteDirectoryRecursively(tempDir);
		}
	}

	/**
	 * Verifies that multiple validation failures are aggregated in a single result.
	 *
	 * @throws IOException if file creation fails
	 */
	@Test
	public void shouldAggregateMultipleFailuresInResult() throws IOException
	{
		Path tempDir = Files.createTempDirectory("compilation-test-");

		try
		{
			Path sourceFile = tempDir.resolve("MultiClass.java");
			// File with multiple classes (one missing, one stale)
			Files.writeString(sourceFile, """
				public class MultiClass
				{
				}

				class Helper
				{
				}
				""");

			Path classesDir = tempDir.resolve("classes");
			Files.createDirectories(classesDir);

			// Only create one class file (stale)
			Path classFile = classesDir.resolve("MultiClass.class");
			Files.writeString(classFile, "fake class file content");

			// Make class file stale
			Instant classTime = Instant.now().minusSeconds(60);
			Instant sourceTime = Instant.now();
			Files.setLastModifiedTime(classFile, FileTime.from(classTime));
			Files.setLastModifiedTime(sourceFile, FileTime.from(sourceTime));

			// Helper.class is missing, MultiClass.class is stale
			TypeResolutionConfig config = new TypeResolutionConfig(List.of(classesDir), List.of());
			try (ClasspathScanner scanner = ClasspathScanner.create(config))
			{
				CompilationValidator validator = new CompilationValidator(scanner);
				CompilationValidationResult result = validator.validate(
					sourceFile, "", List.of("MultiClass", "Helper"));

				requireThat(result, "result").isInstanceOf(CompilationValidationResult.Invalid.class);
				CompilationValidationResult.Invalid invalid = (CompilationValidationResult.Invalid) result;

				// Should report both issues
				requireThat(invalid.missingClasses(), "missingClasses").contains("Helper");
				requireThat(invalid.staleClasses(), "staleClasses").contains("MultiClass");
			}
		}
		finally
		{
			deleteDirectoryRecursively(tempDir);
		}
	}

	// ================================
	// Edge Case Tests (Phase 4)
	// ================================

	/**
	 * Verifies that validation handles multiple classes in the same source file.
	 * Both public and package-private classes should be validated.
	 *
	 * @throws IOException if file creation fails
	 */
	@Test
	public void shouldHandleMultipleClassesInSameSourceFile() throws IOException
	{
		Path tempDir = Files.createTempDirectory("compilation-test-");

		try
		{
			Path sourceFile = tempDir.resolve("Primary.java");
			Files.writeString(sourceFile, """
				public class Primary
				{
				}

				class Secondary
				{
				}
				""");

			Path classesDir = tempDir.resolve("classes");
			Files.createDirectories(classesDir);

			// Create both class files
			Path primaryClass = classesDir.resolve("Primary.class");
			Path secondaryClass = classesDir.resolve("Secondary.class");
			Files.writeString(primaryClass, "fake class file content");
			Files.writeString(secondaryClass, "fake class file content");

			Instant classTime = Instant.now();
			Instant sourceTime = Instant.now().minusSeconds(10);
			Files.setLastModifiedTime(sourceFile, FileTime.from(sourceTime));
			Files.setLastModifiedTime(primaryClass, FileTime.from(classTime));
			Files.setLastModifiedTime(secondaryClass, FileTime.from(classTime));

			TypeResolutionConfig config = new TypeResolutionConfig(List.of(classesDir), List.of());
			try (ClasspathScanner scanner = ClasspathScanner.create(config))
			{
				CompilationValidator validator = new CompilationValidator(scanner);
				CompilationValidationResult result = validator.validate(
					sourceFile, "", List.of("Primary", "Secondary"));

				requireThat(result, "result").isInstanceOf(CompilationValidationResult.Valid.class);
			}
		}
		finally
		{
			deleteDirectoryRecursively(tempDir);
		}
	}

	/**
	 * Verifies that null source file path is rejected with an exception.
	 *
	 * @throws IOException if an I/O error occurs (not expected since null check happens first)
	 */
	@Test(expectedExceptions = NullPointerException.class)
	public void shouldRejectNullSourceFilePath() throws IOException
	{
		Path tempDir;
		try
		{
			tempDir = Files.createTempDirectory("compilation-test-");
		}
		catch (IOException e)
		{
			throw new UncheckedIOException("Failed to create temp directory", e);
		}

		try
		{
			TypeResolutionConfig config = new TypeResolutionConfig(List.of(tempDir), List.of());
			try (ClasspathScanner scanner = ClasspathScanner.create(config))
			{
				CompilationValidator validator = new CompilationValidator(scanner);
				validator.validate(null, "", List.of("Foo"));
			}
		}
		finally
		{
			deleteDirectoryRecursively(tempDir);
		}
	}

	/**
	 * Verifies that ClasspathScanner rejects non-existent classpath entries during construction.
	 * <p>
	 * This differs from the previous behavior where validation would fail gracefully at runtime.
	 * With ClassGraph, we validate classpath entries upfront.
	 */
	@Test(expectedExceptions = IllegalArgumentException.class)
	public void shouldRejectNonExistentClasspathEntry()
	{
		Path tempDir;
		try
		{
			tempDir = Files.createTempDirectory("compilation-test-");
		}
		catch (IOException e)
		{
			throw new UncheckedIOException("Failed to create temp directory", e);
		}

		try
		{
			// Use a non-existent classpath directory
			Path nonExistentDir = tempDir.resolve("does-not-exist");

			TypeResolutionConfig config = new TypeResolutionConfig(List.of(nonExistentDir), List.of());
			// Should throw IllegalArgumentException because classpath entry doesn't exist
			ClasspathScanner.create(config);
		}
		finally
		{
			deleteDirectoryRecursively(tempDir);
		}
	}

	// ================================
	// Helper Methods
	// ================================

	/**
	 * Recursively deletes a directory and all its contents.
	 *
	 * @param directory the directory to delete
	 */
	private void deleteDirectoryRecursively(Path directory)
	{
		try
		{
			if (Files.exists(directory))
				Files.walk(directory).
					sorted((a, b) -> b.compareTo(a)).
					forEach(path ->
					{
						try
						{
							Files.deleteIfExists(path);
						}
						catch (IOException _)
						{
							// Intentionally ignored during cleanup
						}
					});
		}
		catch (IOException _)
		{
			// Intentionally ignored during cleanup
		}
	}
}
