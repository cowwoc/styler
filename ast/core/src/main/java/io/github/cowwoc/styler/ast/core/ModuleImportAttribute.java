package io.github.cowwoc.styler.ast.core;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;

/**
 * Semantic attribute for module import declaration nodes.
 * <p>
 * Contains the module name from a JEP 511 module import statement (e.g., {@code import module java.base;}).
 * Module imports import all public types exported by the specified module.
 * <p>
 * <b>Thread-safety</b>: This record is immutable and thread-safe.
 *
 * @param moduleName the module name (e.g., {@code "java.base"} or {@code "java.sql"})
 */
public record ModuleImportAttribute(String moduleName) implements NodeAttribute
{
	/**
	 * Creates a module import attribute with the specified module name.
	 *
	 * @param moduleName the module name
	 * @throws NullPointerException     if {@code moduleName} is null
	 * @throws IllegalArgumentException if {@code moduleName} is empty
	 */
	public ModuleImportAttribute
	{
		requireThat(moduleName, "moduleName").isNotBlank();
	}
}
