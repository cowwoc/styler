package io.github.cowwoc.styler.ast.core;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;

/**
 * Semantic attribute for requires directive nodes.
 * <p>
 * Contains the required module name and modifiers. The {@code transitive} modifier causes dependent modules
 * to implicitly require this module. The {@code static} modifier indicates a compile-time-only dependency.
 * <p>
 * <b>Thread-safety</b>: This record is immutable and thread-safe.
 *
 * @param moduleName   the required module name (e.g., {@code "java.sql"})
 * @param isTransitive {@code true} if the dependency is transitive ({@code requires transitive}),
 *                     meaning dependent modules also implicitly require this module
 * @param isStatic     {@code true} if the dependency is static ({@code requires static}),
 *                     meaning it is only required at compile time
 */
public record RequiresDirectiveAttribute(String moduleName, boolean isTransitive, boolean isStatic)
	implements NodeAttribute
{
	/**
	 * Creates a requires directive attribute with the specified module name and modifiers.
	 *
	 * @param moduleName   the required module name
	 * @param isTransitive {@code true} if the dependency is transitive
	 * @param isStatic     {@code true} if the dependency is static
	 * @throws NullPointerException     if {@code moduleName} is null
	 * @throws IllegalArgumentException if {@code moduleName} is empty
	 */
	public RequiresDirectiveAttribute
	{
		requireThat(moduleName, "moduleName").isNotBlank();
	}
}
