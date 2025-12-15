package io.github.cowwoc.styler.formatter;

import java.nio.file.Path;
import java.util.List;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;

/**
 * Configuration for type resolution during formatting.
 * <p>
 * Provides classpath and modulepath access for advanced import analysis such as wildcard
 * resolution and unused import detection.
 * <p>
 * <b>Thread-safety</b>: This class is immutable.
 *
 * @param classpathEntries paths to JAR files and directories on classpath
 * @param modulepathEntries paths to modules on modulepath
 */
public record TypeResolutionConfig(
	List<Path> classpathEntries,
	List<Path> modulepathEntries)
{
	/**
	 * Empty configuration with no classpath access.
	 */
	public static final TypeResolutionConfig EMPTY = new TypeResolutionConfig(List.of(), List.of());

	/**
	 * Creates a new type resolution configuration.
	 *
	 * @throws NullPointerException if any argument is {@code null}
	 */
	public TypeResolutionConfig
	{
		requireThat(classpathEntries, "classpathEntries").isNotNull();
		requireThat(modulepathEntries, "modulepathEntries").isNotNull();
		classpathEntries = List.copyOf(classpathEntries);
		modulepathEntries = List.copyOf(modulepathEntries);
	}

	/**
	 * Returns {@code true} if classpath scanning is available.
	 *
	 * @return {@code true} if at least one classpath or modulepath entry exists
	 */
	public boolean hasClasspathAccess()
	{
		return !classpathEntries.isEmpty() || !modulepathEntries.isEmpty();
	}
}
