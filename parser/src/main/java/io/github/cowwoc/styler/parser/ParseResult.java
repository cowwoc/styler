package io.github.cowwoc.styler.parser;

import io.github.cowwoc.styler.ast.core.NodeIndex;

import java.nio.file.Path;
import java.util.List;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;

/**
 * Represents the result of parsing Java source code.
 * <p>
 * This sealed interface provides two possible outcomes: {@link Success} containing the root node of the
 * parsed AST, or {@link Failure} containing a list of parse errors. This design enables exhaustive pattern
 * matching and ensures callers handle both success and failure cases.
 * <p>
 * <b>Thread-safety</b>: All implementations are immutable.
 */
public sealed interface ParseResult
	permits ParseResult.Success, ParseResult.Failure
{
	/**
	 * A successful parse result containing the root node of the AST.
	 * <p>
	 * <b>Thread-safety</b>: This class is immutable.
	 *
	 * @param rootNode the root node of the parsed AST
	 */
	record Success(NodeIndex rootNode) implements ParseResult
	{
		/**
		 * Creates a new successful parse result.
		 *
		 * @param rootNode the root node of the parsed AST
		 * @throws NullPointerException if {@code rootNode} is {@code null}
		 */
		public Success
		{
			requireThat(rootNode, "rootNode").isNotNull();
		}
	}

	/**
	 * A failed parse result containing a list of errors encountered during parsing.
	 * <p>
	 * <b>Thread-safety</b>: This class is immutable.
	 *
	 * @param errors the list of parse errors encountered
	 */
	record Failure(List<ParseError> errors) implements ParseResult
	{
		/**
		 * Creates a new failed parse result.
		 *
		 * @param errors the list of parse errors encountered
		 * @throws IllegalArgumentException if {@code errors} is empty
		 */
		public Failure
		{
			requireThat(errors, "errors").isNotEmpty();
			errors = List.copyOf(errors);
		}

		/**
		 * Returns a formatted error message containing all parse errors with their locations.
		 *
		 * @return a human-readable error message in the format {@code line:column: message}
		 */
		public String getErrorMessage()
		{
			return getErrorMessage(null);
		}

		/**
		 * Returns a formatted error message containing all parse errors with their locations.
		 *
		 * @param filePath the file path to include as a prefix, or {@code null} to omit
		 * @return a human-readable error message in the format {@code [filePath:]line:column: message}
		 */
		public String getErrorMessage(Path filePath)
		{
			StringBuilder sb = new StringBuilder();
			for (ParseError error : errors)
			{
				if (!sb.isEmpty())
					sb.append('\n');
				if (filePath != null)
					sb.append(filePath).append(':');
				sb.append(error.line()).append(':').append(error.column()).append(": ").append(error.message());
			}
			return sb.toString();
		}
	}
}
