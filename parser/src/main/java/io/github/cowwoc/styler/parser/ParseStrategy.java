package io.github.cowwoc.styler.parser;

/**
 * Strategy interface for parsing Java language constructs.
 * Enables pluggable parsing support for different Java versions and language features.
 *
 * <p>Strategies can use both token-based detection (simple features) and phase-aware
 * detection (context-dependent features).
 *
 * <h2>Simple Token-Based Strategy Example</h2>
 * <pre>{@code
 * public class RecordDeclarationStrategy implements ParseStrategy {
 *     public boolean canHandle(JavaVersion version, ParsingPhase phase, ParseContext context) {
 *         // Phase irrelevant for keyword-triggered features
 *         return version.isAtLeast(JavaVersion.JAVA_16) &&
 *                context.currentTokenIs(TokenType.RECORD);
 *     }
 * }
 * }</pre>
 *
 * <h2>Phase-Aware Strategy Example</h2>
 * <pre>{@code
 * public class FlexibleConstructorBodiesStrategy implements ParseStrategy {
 *     public boolean canHandle(JavaVersion version, ParsingPhase phase, ParseContext context) {
 *         // CRITICAL: Only in constructor bodies
 *         return version.isAtLeast(JavaVersion.JAVA_25) &&
 *                phase == ParsingPhase.CONSTRUCTOR_BODY &&
 *                context.currentTokenIs(TokenType.LBRACE);
 *     }
 * }
 * }</pre>
 *
 * @since 1.0
 * @see ParseStrategyRegistry
 * @see ParsingPhase
 */
public interface ParseStrategy
{
	/**
	 * Default priority for strategies (lowest).
	 * Used when no specific priority is needed.
	 */
	int PRIORITY_DEFAULT = 0;

	/**
	 * Priority for keyword-based strategies.
	 * Token-triggered features like records, sealed classes, switch expressions.
	 */
	int PRIORITY_KEYWORD_BASED = 10;

	/**
	 * Priority for phase-aware strategies.
	 * Context-dependent features like flexible constructor bodies that require
	 * semantic phase information to correctly identify applicability.
	 */
	int PRIORITY_PHASE_AWARE = 15;
	/**
	 * Determines if this strategy can handle parsing the given construct
	 * at the current parse context position.
	 *
	 * <p><strong>Phase-Aware Detection:</strong> For features that depend on parsing
	 * context (e.g., flexible constructor bodies), use the {@code phase} parameter
	 * to determine applicability.
	 *
	 * <p><strong>Token-Based Detection:</strong> For features triggered by specific
	 * keywords (e.g., "record", "sealed"), the phase parameter can typically be ignored.
	 *
	 * @param version The target Java version being parsed
	 * @param phase The current parsing phase/context
	 * @param context The current parsing context (token stream, position, etc.)
	 * @return {@code true} if this strategy can handle the current construct
	 */
	boolean canHandle(JavaVersion version, ParsingPhase phase, ParseContext context);

	/**
	 * Parses the language construct and returns the node ID.
	 * This method should only be called if canHandle() returns {@code true}.
	 *
	 * @param context The parsing context
	 * @return The ID of the created AST node
	 */
	int parseConstruct(ParseContext context);

	/**
	 * Gets the priority of this strategy when multiple strategies can handle the same construct.
	 * Higher priority strategies are tried first.
	 *
	 * @return The priority level (higher = more priority)
	 */
	default int getPriority()
{
		return 0;
	}

	/**
	 * Gets a description of what this strategy handles for debugging and documentation.
	 *
	 * @return A human-readable description
	 */
	String getDescription();
}