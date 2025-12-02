package io.github.cowwoc.styler.formatter.linelength.internal;

/**
 * Wrapping context types identified from AST node types.
 * Corresponds to 9 wrapping contexts for different Java language constructs.
 * <p>
 * <b>Thread-safety</b>: This class is thread-safe.
 */
public enum WrapContext
{
	/**
	 * Method chain context (obj.method1().method2()).
	 * Corresponds to FIELD_ACCESS nodes.
	 */
	METHOD_CHAIN,

	/**
	 * Qualified name context (com.example.ClassName).
	 * Corresponds to QUALIFIED_NAME nodes.
	 */
	QUALIFIED_NAME,

	/**
	 * Method arguments context (method(arg1, arg2)).
	 * Corresponds to METHOD_INVOCATION argument lists.
	 */
	METHOD_ARGUMENTS,

	/**
	 * Method parameters context (void method(Type param1, Type param2)).
	 * Corresponds to METHOD_DECLARATION and PARAMETER_DECLARATION nodes.
	 */
	METHOD_PARAMETERS,

	/**
	 * Binary expression context (a + b, x * y).
	 * Corresponds to BINARY_EXPRESSION nodes.
	 */
	BINARY_EXPRESSION,

	/**
	 * Ternary conditional expression context (condition ? value1 : value2).
	 * Corresponds to CONDITIONAL_EXPRESSION nodes.
	 */
	TERNARY_EXPRESSION,

	/**
	 * Array initializer context (new int[]{1, 2, 3}).
	 * Corresponds to ARRAY_CREATION nodes.
	 */
	ARRAY_INITIALIZER,

	/**
	 * Annotation arguments context (@Annotation(param = value)).
	 * Corresponds to ANNOTATION nodes.
	 */
	ANNOTATION_ARGUMENTS,

	/**
	 * Generic type arguments context (List&lt;String&gt;, Map&lt;String, Integer&gt;).
	 * Corresponds to PARAMETERIZED_TYPE nodes.
	 */
	GENERIC_TYPE_ARGS,

	/**
	 * Context for positions that are not within wrappable constructs.
	 * Represents node types that genuinely cannot be wrapped (e.g., simple literals,
	 * keywords, identifiers not part of a wrappable construct).
	 */
	NOT_WRAPPABLE
}
