package io.github.cowwoc.styler.ast.core;

import java.util.List;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;

/**
 * Semantic attribute for exports directive nodes.
 * <p>
 * Contains the exported package name and optional target modules. Unqualified exports make the package
 * accessible to all modules, while qualified exports (with {@code to} clause) restrict access to specific
 * modules.
 * <p>
 * <b>Thread-safety</b>: This record is immutable and thread-safe.
 *
 * @param packageName   the exported package name (e.g., {@code "com.example.api"})
 * @param targetModules list of target modules for qualified exports (e.g., {@code "com.example.test"}),
 *                      or empty list for unqualified exports accessible to all modules
 */
public record ExportsDirectiveAttribute(String packageName, List<String> targetModules)
	implements NodeAttribute
{
	/**
	 * Creates an exports directive attribute with the specified package name and target modules.
	 *
	 * @param packageName   the exported package name
	 * @param targetModules the target modules for qualified exports, or empty for unqualified
	 * @throws NullPointerException     if {@code packageName} or {@code targetModules} is null
	 * @throws IllegalArgumentException if {@code packageName} is empty
	 */
	public ExportsDirectiveAttribute
	{
		requireThat(packageName, "packageName").isNotBlank();
		requireThat(targetModules, "targetModules").isNotNull();
		targetModules = List.copyOf(targetModules);
	}
}
