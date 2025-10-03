package io.github.cowwoc.styler.parser;

/**
 * Represents the current parsing phase/context in the recursive descent parser.
 *
 * <p>Parsing phases provide semantic context beyond token matching, enabling strategies
 * to differentiate between syntactically similar constructs based on their location
 * in the parse tree.
 *
 * <h2>Usage Example</h2>
 * <pre>{@code
 * // Flexible constructor bodies only allowed in CONSTRUCTOR_BODY phase
 * if (phase == ParsingPhase.CONSTRUCTOR_BODY && token == LBRACE) {
 *     parseFlexibleConstructorBody();
 * }
 * }</pre>
 *
 * @since 1.0
 * @see ParseStrategy
 * @see IndexOverlayParser
 */
public enum ParsingPhase
{
	/**
	 * Top-level file declarations (package, imports, top-level classes).
	 * <p>Valid constructs: package declaration, import statements, class/interface/enum/record declarations.
	 */
	TOP_LEVEL("Top-level declarations"),

	/**
	 * Inside a class body (between class {...}).
	 * <p>Valid constructs: fields, methods, constructors, nested types, initializer blocks.
	 */
	CLASS_BODY("Class body"),

	/**
	 * Inside an interface body (between interface {...}).
	 * <p>Valid constructs: abstract methods, default methods, static methods, constants, nested types.
	 */
	INTERFACE_BODY("Interface body"),

	/**
	 * Inside an enum body (between enum {...}).
	 * <p>Valid constructs: enum constants, fields, methods, constructors, nested types.
	 * <p><strong>Note:</strong> Enum constants come before methods, affecting parsing context.
	 */
	ENUM_BODY("Enum body"),

	/**
	 * Inside a record body (between record(...) {...}).
	 * <p>Valid constructs: compact constructors, canonical constructors, methods, nested types.
	 */
	RECORD_BODY("Record body"),

	/**
	 * Inside a constructor body (between constructor(...) {...}).
	 * <p><strong>JDK 25 Flexible Constructor Bodies (JEP 513):</strong>
	 * Allows statements before super() or this() calls in constructor body.
	 * <p>Valid constructs: local variables, statements, super()/this() calls.
	 */
	CONSTRUCTOR_BODY("Constructor body"),

	/**
	 * Inside a method body (between methodName(...) {...}).
	 * <p>Valid constructs: local variables, statements, expressions.
	 */
	METHOD_BODY("Method body"),

	/**
	 * Inside a static or instance initializer block.
	 * <p>Valid constructs: statements, expressions.
	 */
	INITIALIZER_BLOCK("Initializer block");

	private final String description;

	ParsingPhase(String description)
	{
		this.description = description;
	}

	/**
	 * Gets a human-readable description of this parsing phase.
	 *
	 * @return the phase description
	 */
	public String getDescription()
	{
		return description;
	}
}
