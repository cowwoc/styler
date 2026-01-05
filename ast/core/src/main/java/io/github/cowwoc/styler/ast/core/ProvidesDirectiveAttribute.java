package io.github.cowwoc.styler.ast.core;

import java.util.List;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;

/**
 * Semantic attribute for provides directive nodes.
 * <p>
 * Contains the service interface name and list of implementation classes. The {@code provides} directive
 * declares that this module provides implementations of the specified service interface, making them
 * available to consumers via {@link java.util.ServiceLoader}.
 * <p>
 * <b>Thread-safety</b>: This record is immutable and thread-safe.
 *
 * @param serviceTypeName the fully qualified service interface name (e.g., {@code "java.sql.Driver"})
 * @param implementations list of fully qualified implementation class names that implement the service
 *                        (e.g., {@code "com.example.MySqlDriver"})
 */
public record ProvidesDirectiveAttribute(String serviceTypeName, List<String> implementations)
	implements NodeAttribute
{
	/**
	 * Creates a provides directive attribute with the specified service type and implementations.
	 *
	 * @param serviceTypeName the fully qualified service interface name
	 * @param implementations the list of implementation class names
	 * @throws NullPointerException     if {@code serviceTypeName} or {@code implementations} is null
	 * @throws IllegalArgumentException if {@code serviceTypeName} is empty or {@code implementations}
	 *                                  is empty
	 */
	public ProvidesDirectiveAttribute
	{
		requireThat(serviceTypeName, "serviceTypeName").isNotBlank();
		requireThat(implementations, "implementations").isNotEmpty();
		implementations = List.copyOf(implementations);
	}
}
