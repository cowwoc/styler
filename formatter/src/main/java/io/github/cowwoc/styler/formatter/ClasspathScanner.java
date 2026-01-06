package io.github.cowwoc.styler.formatter;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.Resource;
import io.github.classgraph.ScanResult;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.OptionalLong;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;

/**
 * Scans classpath and modulepath for available classes.
 * <p>
 * Uses ClassGraph for efficient classpath scanning. Does NOT load or execute classes
 * (security boundary - only queries class existence via resource scanning).
 * <p>
 * <b>Thread-safety</b>: This class is thread-safe. The underlying {@code ScanResult} is immutable
 * after construction.
 */
public final class ClasspathScanner implements AutoCloseable
{
	private final ScanResult scanResult;
	private final AtomicBoolean closed = new AtomicBoolean();

	/**
	 * Creates an empty scanner that contains no classes.
	 * <p>
	 * Use this when symbol resolution is not needed (e.g., when wildcard expansion is disabled).
	 * This avoids the overhead of scanning the classpath.
	 *
	 * @return an empty scanner
	 */
	public static ClasspathScanner empty()
	{
		// Disable all scanning to create a minimal empty result
		ClassGraph classGraph = new ClassGraph();
		classGraph.disableNestedJarScanning();
		classGraph.disableModuleScanning();
		classGraph.disableDirScanning();
		classGraph.disableJarScanning();
		ScanResult result = classGraph.scan();
		return new ClasspathScanner(result);
	}

	/**
	 * Creates a scanner for the given type resolution configuration.
	 * <p>
	 * When the configuration has no classpath or modulepath entries, this method returns an
	 * {@link #empty()} scanner to avoid the overhead of scanning the system classpath.
	 *
	 * @param config the type resolution configuration
	 * @return a new scanner
	 * @throws NullPointerException     if {@code config} is {@code null}
	 * @throws IllegalArgumentException if any classpath entry does not exist
	 */
	public static ClasspathScanner create(TypeResolutionConfig config)
	{
		requireThat(config, "config").isNotNull();

		// Short-circuit for empty config - avoid scanning system classpath
		if (config.classpathEntries().isEmpty() && config.modulepathEntries().isEmpty())
			return empty();

		// Validate that all paths exist
		for (Path path : config.classpathEntries())
		{
			if (Files.notExists(path))
			{
				throw new IllegalArgumentException("Classpath entry does not exist: " + path);
			}
		}

		for (Path path : config.modulepathEntries())
		{
			if (Files.notExists(path))
			{
				throw new IllegalArgumentException("Modulepath entry does not exist: " + path);
			}
		}

		// Combine classpath and modulepath entries for scanning
		Set<String> pathStrings = new HashSet<>();
		for (Path path : config.classpathEntries())
		{
			pathStrings.add(path.toString());
		}
		for (Path path : config.modulepathEntries())
		{
			pathStrings.add(path.toString());
		}

		// Build ClassGraph scanner - use resource scanning instead of class info parsing
		// This allows us to detect .class files without needing valid bytecode
		ClassGraph classGraph = new ClassGraph();

		// Only override classpath if we have entries; empty config uses system classloader
		if (!pathStrings.isEmpty())
		{
			classGraph.overrideClasspath(pathStrings);
		}

		ScanResult result = classGraph.scan();
		return new ClasspathScanner(result);
	}

	/**
	 * Internal constructor.
	 *
	 * @param scanResult the ClassGraph scan result
	 */
	private ClasspathScanner(ScanResult scanResult)
	{
		this.scanResult = scanResult;
	}

	/**
	 * Checks if a fully-qualified class name exists on the classpath/modulepath.
	 *
	 * @param qualifiedName fully-qualified class name (e.g., {@code "java.util.List"})
	 * @return {@code true} if the class is accessible
	 * @throws NullPointerException  if {@code qualifiedName} is {@code null}
	 * @throws IllegalStateException if the scanner has been closed
	 */
	public boolean classExists(String qualifiedName)
	{
		requireThat(qualifiedName, "qualifiedName").isNotNull();

		if (closed.get())
		{
			throw new IllegalStateException("Scanner has been closed");
		}

		// Convert class name to resource path
		String resourcePath = qualifiedName.replace('.', '/') + ".class";

		// Check if resource exists - this doesn't require valid bytecode
		return !scanResult.getResourcesWithPath(resourcePath).isEmpty();
	}

	/**
	 * Returns the last modified time of a class file on the classpath/modulepath.
	 *
	 * @param qualifiedName fully-qualified class name (e.g., {@code "java.util.List"})
	 * @return the last modified time in milliseconds since epoch, or empty if class not found
	 * @throws NullPointerException  if {@code qualifiedName} is {@code null}
	 * @throws IllegalStateException if the scanner has been closed
	 */
	public OptionalLong getClassLastModified(String qualifiedName)
	{
		requireThat(qualifiedName, "qualifiedName").isNotNull();

		if (closed.get())
		{
			throw new IllegalStateException("Scanner has been closed");
		}

		// Convert class name to resource path
		String resourcePath = qualifiedName.replace('.', '/') + ".class";

		List<Resource> resources = scanResult.getResourcesWithPath(resourcePath);
		if (resources.isEmpty())
		{
			return OptionalLong.empty();
		}

		// Return the first match's last modified time
		Resource resource = resources.get(0);
		return OptionalLong.of(resource.getLastModified());
	}

	/**
	 * Lists all classes in a package (for wildcard import resolution).
	 *
	 * @param packageName package name (e.g., {@code "java.util"})
	 * @return set of fully-qualified class names in the package
	 * @throws NullPointerException  if {@code packageName} is {@code null}
	 * @throws IllegalStateException if the scanner has been closed
	 */
	public Set<String> listPackageClasses(String packageName)
	{
		requireThat(packageName, "packageName").isNotNull();

		if (closed.get())
		{
			throw new IllegalStateException("Scanner has been closed");
		}

		// Convert package name to path prefix
		String packagePath = packageName.replace('.', '/') + "/";

		// Get all resources matching the package path (direct children with .class extension)
		Pattern pattern = Pattern.compile(Pattern.quote(packagePath) + "[^/]+\\.class$");

		return scanResult.getResourcesMatchingPattern(pattern).stream().
			map(resource ->
			{
				String path = resource.getPath();
				// Remove .class suffix and convert path to class name
				String withoutExtension = path.substring(0, path.length() - 6);
				return withoutExtension.replace('/', '.');
			}).
			collect(Collectors.toSet());
	}

	/**
	 * Closes the scanner and releases resources.
	 */
	@Override
	public void close()
	{
		if (closed.compareAndSet(false, true))
		{
			scanResult.close();
		}
	}
}
