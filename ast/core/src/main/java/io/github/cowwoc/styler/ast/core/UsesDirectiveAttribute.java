package io.github.cowwoc.styler.ast.core;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;

/**
 * Semantic attribute for uses directive nodes.
 * <p>
 * Contains the fully qualified service interface name. The {@code uses} directive declares that this module
 * consumes implementations of the specified service interface via {@link java.util.ServiceLoader}.
 * <p>
 * <b>Thread-safety</b>: This record is immutable and thread-safe.
 *
 * @param serviceTypeName the fully qualified service interface name (e.g., {@code "java.sql.Driver"})
 */
public record UsesDirectiveAttribute(String serviceTypeName) implements NodeAttribute
{
	/**
	 * Creates a uses directive attribute with the specified service type name.
	 *
	 * @param serviceTypeName the fully qualified service interface name
	 * @throws NullPointerException     if {@code serviceTypeName} is null
	 * @throws IllegalArgumentException if {@code serviceTypeName} is empty
	 */
	public UsesDirectiveAttribute
	{
		requireThat(serviceTypeName, "serviceTypeName").isNotBlank();
	}
}
