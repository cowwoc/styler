package io.github.cowwoc.styler.formatter.importorg.internal;

import java.util.Map;
import java.util.Set;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;

/**
 * Result of symbol resolution containing resolved mappings and any unresolved symbols.
 * <p>
 * This record captures which identifiers were successfully resolved to import sources and which
 * could not be resolved due to missing classpath entries.
 * <p>
 * <b>Thread-safety</b>: This record is immutable and thread-safe.
 *
 * @param resolvedSymbols   mapping from identifier name to fully-qualified import name
 * @param unresolvedSymbols identifiers that could not be resolved to any import
 * @throws NullPointerException if any argument is {@code null}
 */
public record SymbolResolutionResult(
	Map<String, String> resolvedSymbols,
	Set<String> unresolvedSymbols)
{
	/**
	 * Creates a symbol resolution result.
	 */
	public SymbolResolutionResult
	{
		requireThat(resolvedSymbols, "resolvedSymbols").isNotNull();
		resolvedSymbols = Map.copyOf(resolvedSymbols);
		requireThat(unresolvedSymbols, "unresolvedSymbols").isNotNull();
		unresolvedSymbols = Set.copyOf(unresolvedSymbols);
	}

	/**
	 * Returns whether all symbols were successfully resolved.
	 * <p>
	 * When this returns {@code true}, it is safe to expand wildcards to explicit imports.
	 * When {@code false}, the classpath is incomplete and wildcards should be preserved.
	 *
	 * @return {@code true} if {@code unresolvedSymbols} is empty
	 */
	public boolean isComplete()
	{
		return unresolvedSymbols.isEmpty();
	}

	/**
	 * Creates an empty result with no resolved or unresolved symbols.
	 *
	 * @return empty resolution result
	 */
	public static SymbolResolutionResult empty()
	{
		return new SymbolResolutionResult(Map.of(), Set.of());
	}
}
