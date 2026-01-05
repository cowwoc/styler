package io.github.cowwoc.styler.ast.core;

import java.util.List;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;

/**
 * Semantic attribute for opens directive nodes.
 * <p>
 * Contains the opened package name and optional target modules. The {@code opens} directive grants
 * deep reflective access to a package at runtime, which is required by frameworks like Spring, Hibernate,
 * and JPA that use reflection to access private members.
 * <p>
 * <b>Thread-safety</b>: This record is immutable and thread-safe.
 *
 * @param packageName   the opened package name (e.g., {@code "com.example.internal"})
 * @param targetModules list of target modules for qualified opens (e.g., {@code "spring.core"}),
 *                      or empty list for unqualified opens accessible to all modules
 */
public record OpensDirectiveAttribute(String packageName, List<String> targetModules)
	implements NodeAttribute
{
	/**
	 * Creates an opens directive attribute with the specified package name and target modules.
	 *
	 * @param packageName   the opened package name
	 * @param targetModules the target modules for qualified opens, or empty for unqualified
	 * @throws NullPointerException     if {@code packageName} or {@code targetModules} is null
	 * @throws IllegalArgumentException if {@code packageName} is empty
	 */
	public OpensDirectiveAttribute
	{
		requireThat(packageName, "packageName").isNotBlank();
		requireThat(targetModules, "targetModules").isNotNull();
		targetModules = List.copyOf(targetModules);
	}
}
