package io.github.cowwoc.styler.plugin.engine;

import io.github.cowwoc.styler.ast.node.CompilationUnitNode;
import org.apache.maven.plugin.MojoExecutionException;

/**
 * Wrapper interface for parsing Java source code to AST.
 * Abstracts the underlying parser implementation details.
 * Thread-safe and stateless for Maven parallel builds.
 */
@FunctionalInterface
public interface SourceParser
{
	/**
	 * Parses Java source code to AST.
	 *
	 * @param sourceText source code to parse
	 * @param sourcePath file path for error reporting
	 * @return root AST node (CompilationUnit)
	 * @throws MojoExecutionException if parsing fails
	 */
	CompilationUnitNode parse(String sourceText, String sourcePath) throws MojoExecutionException;
}
