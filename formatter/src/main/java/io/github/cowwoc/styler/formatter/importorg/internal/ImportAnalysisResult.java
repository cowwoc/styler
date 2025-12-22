package io.github.cowwoc.styler.formatter.importorg.internal;

import java.util.Set;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;

/**
 * Result of import analysis containing unused imports and any unresolved symbols.
 * <p>
 * When wildcard expansion is enabled and the classpath is incomplete, some symbols may not be resolvable
 * to their defining package. In this case, wildcard imports are preserved (not marked as unused) and the
 * unresolved symbols are reported for diagnostic purposes.
 * <p>
 * <b>Thread-safety</b>: This record is immutable and thread-safe.
 *
 * @param unusedImports     qualified names of imports not referenced in code
 * @param unresolvedSymbols symbols that could not be resolved to an import source (empty if resolution
 *                          was complete or wildcard expansion is disabled)
 * @throws NullPointerException if any argument is {@code null}
 */
public record ImportAnalysisResult(
	Set<String> unusedImports,
	Set<String> unresolvedSymbols)
{
	/**
	 * Creates an import analysis result.
	 *
	 * @param unusedImports     qualified names of imports not referenced in code
	 * @param unresolvedSymbols symbols that could not be resolved to an import source
	 */
	public ImportAnalysisResult
	{
		requireThat(unusedImports, "unusedImports").isNotNull();
		unusedImports = Set.copyOf(unusedImports);
		requireThat(unresolvedSymbols, "unresolvedSymbols").isNotNull();
		unresolvedSymbols = Set.copyOf(unresolvedSymbols);
	}

	/**
	 * Returns whether all symbols were successfully resolved.
	 *
	 * @return {@code true} if all symbols were resolved, {@code false} if some symbols are unresolved
	 */
	public boolean isResolutionComplete()
	{
		return unresolvedSymbols.isEmpty();
	}

	/**
	 * Creates an empty result with no unused imports and no unresolved symbols.
	 *
	 * @return empty analysis result
	 */
	public static ImportAnalysisResult empty()
	{
		return new ImportAnalysisResult(Set.of(), Set.of());
	}
}
