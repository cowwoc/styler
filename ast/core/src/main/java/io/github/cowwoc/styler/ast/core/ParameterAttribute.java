package io.github.cowwoc.styler.ast.core;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;

/**
 * Semantic attribute for parameter declaration nodes.
 * <p>
 * Contains the parameter name, whether it is a varargs parameter, whether it has the {@code final}
 * modifier, and whether it is a receiver parameter. This eliminates the need for formatters to parse
 * parameter declaration strings at runtime.
 * <p>
 * <b>Thread-safety</b>: This record is immutable and thread-safe.
 *
 * @param name       the parameter name (e.g., {@code "args"}, {@code "input"}, or {@code "this"}
 *                   for receiver parameters)
 * @param isVarargs  {@code true} if this is a varargs parameter ({@code Type... name})
 * @param isFinal    {@code true} if this parameter has the {@code final} modifier
 * @param isReceiver {@code true} if this is a receiver parameter ({@code ClassName this})
 */
public record ParameterAttribute(String name, boolean isVarargs, boolean isFinal, boolean isReceiver)
	implements NodeAttribute
{
	/**
	 * Creates a parameter attribute.
	 *
	 * @param name       the parameter name
	 * @param isVarargs  whether this is a varargs parameter
	 * @param isFinal    whether this parameter is final
	 * @param isReceiver whether this is a receiver parameter
	 * @throws NullPointerException     if {@code name} is {@code null}
	 * @throws IllegalArgumentException if {@code name} is empty
	 */
	public ParameterAttribute
	{
		requireThat(name, "name").isNotBlank();
	}
}
