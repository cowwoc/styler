package io.github.cowwoc.styler.pipeline.internal;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.OptionalLong;

import io.github.cowwoc.styler.formatter.internal.ClasspathScanner;
import io.github.cowwoc.styler.pipeline.CompilationValidationResult;

/**
 * Validates that source files have been compiled before formatting.
 * <p>
 * Checks that corresponding {@code .class} files exist on the classpath and are up-to-date
 * (have a timestamp newer than or equal to the source file).
 * <p>
 * This validator uses ClassGraph via {@link ClasspathScanner} to search for class files,
 * which supports both directories and JAR files on the classpath.
 * <p>
 * This validator enforces a strict policy:
 * <ul>
 *   <li>Classpath is mandatory when source files exist</li>
 *   <li>Each declared type must have a corresponding class file on the classpath</li>
 *   <li>Class files must be at least as new as the source file</li>
 * </ul>
 * <p>
 * <b>Thread-safety</b>: This class is thread-safe if the provided {@link ClasspathScanner} is thread-safe.
 */
public final class CompilationValidator
{
	private final ClasspathScanner scanner;

	/**
	 * Creates a new compilation validator.
	 *
	 * @param scanner the classpath scanner to use for finding class files
	 * @throws NullPointerException if {@code scanner} is {@code null}
	 */
	public CompilationValidator(ClasspathScanner scanner)
	{
		requireThat(scanner, "scanner").isNotNull();
		this.scanner = scanner;
	}

	/**
	 * Validates that a source file has been compiled.
	 * <p>
	 * For each type name, looks up the class file on the classpath using ClassGraph.
	 * <p>
	 * A class file is considered valid if:
	 * <ul>
	 *   <li>It exists on the classpath (in a directory or JAR)</li>
	 *   <li>Its last modified time is greater than or equal to the source file's</li>
	 * </ul>
	 *
	 * @param sourceFile the path to the {@code .java} source file
	 * @param packageName the package name from the source file (empty string for default package)
	 * @param typeNames the simple type names declared in the source file (e.g., {@code "MyClass"})
	 * @return validation result indicating success or listing stale/missing class files
	 * @throws NullPointerException if any argument is {@code null}
	 * @throws IllegalArgumentException if {@code typeNames} is empty
	 * @throws IOException if an I/O error occurs reading file timestamps
	 */
	public CompilationValidationResult validate(
			Path sourceFile,
			String packageName,
			List<String> typeNames) throws IOException
	{
		requireThat(sourceFile, "sourceFile").isNotNull();
		requireThat(packageName, "packageName").isNotNull();
		requireThat(typeNames, "typeNames").isNotEmpty();

		long sourceLastModified = Files.getLastModifiedTime(sourceFile).toMillis();

		List<String> missingClasses = new ArrayList<>();
		List<String> staleClasses = new ArrayList<>();

		for (String typeName : typeNames)
		{
			String fqn = buildFullyQualifiedName(packageName, typeName);
			OptionalLong classLastModified = scanner.getClassLastModified(fqn);

			if (classLastModified.isEmpty())
			{
				missingClasses.add(fqn);
			}
			else if (classLastModified.getAsLong() < sourceLastModified)
			{
				staleClasses.add(fqn);
			}
		}

		if (missingClasses.isEmpty() && staleClasses.isEmpty())
		{
			return new CompilationValidationResult.Valid();
		}
		return new CompilationValidationResult.Invalid(missingClasses, staleClasses, sourceFile);
	}

	/**
	 * Builds the fully-qualified class name from package and type name.
	 *
	 * @param packageName the package name (empty for default package)
	 * @param typeName the simple type name
	 * @return the fully-qualified name
	 */
	private String buildFullyQualifiedName(String packageName, String typeName)
	{
		if (packageName.isEmpty())
		{
			return typeName;
		}
		return packageName + "." + typeName;
	}
}
