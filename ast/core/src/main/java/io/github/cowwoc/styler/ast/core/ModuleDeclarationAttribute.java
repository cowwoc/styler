package io.github.cowwoc.styler.ast.core;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;

/**
 * Semantic attribute for module declaration nodes.
 * <p>
 * Contains the module name and whether this is an open module declaration. Open modules grant reflective
 * access to all packages at runtime, even those not explicitly exported or opened.
 * <p>
 * <b>Thread-safety</b>: This record is immutable and thread-safe.
 *
 * @param moduleName the qualified module name (e.g., {@code "com.example.app"})
 * @param isOpen     {@code true} if this is an open module declaration ({@code open module}),
 *                   {@code false} for regular modules
 */
public record ModuleDeclarationAttribute(String moduleName, boolean isOpen) implements NodeAttribute
{
	/**
	 * Creates a module declaration attribute with the specified module name and open flag.
	 *
	 * @param moduleName the qualified module name
	 * @param isOpen     {@code true} if this is an open module
	 * @throws NullPointerException     if {@code moduleName} is null
	 * @throws IllegalArgumentException if {@code moduleName} is empty
	 */
	public ModuleDeclarationAttribute
	{
		requireThat(moduleName, "moduleName").isNotBlank();
	}
}
