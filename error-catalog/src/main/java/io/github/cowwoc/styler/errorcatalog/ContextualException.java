package io.github.cowwoc.styler.errorcatalog;

/**
 * Interface for exceptions that provide rich error context.
 */
@SuppressWarnings("PMD.ImplicitFunctionalInterface")
public interface ContextualException
{
	/**
	 * Returns the error context.
	 *
	 * @return error context (never {@code null})
	 */
	ErrorContext getErrorContext();
}
