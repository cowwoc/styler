package io.github.cowwoc.styler.parser.strategies;

import io.github.cowwoc.styler.parser.JavaVersion;
import io.github.cowwoc.styler.parser.NodeType;
import io.github.cowwoc.styler.parser.ParseContext;
import io.github.cowwoc.styler.parser.ParseStrategy;
import io.github.cowwoc.styler.parser.ParsingPhase;
import io.github.cowwoc.styler.parser.TokenType;

/**
 * Strategy for parsing JDK 25 flexible constructor bodies (JEP 513).
 *
 * <p>Flexible constructor bodies allow statements before super() or this() calls,
 * enabling field initialization and validation logic before superclass construction.
 *
 * <p><strong>This is a phase-aware strategy</strong> - it requires the {@code CONSTRUCTOR_BODY}
 * phase to correctly identify when to apply this parsing logic, since the opening brace
 * {@code LBRACE} is not unique to constructors.
 *
 * <h2>Example Code</h2>
 * <pre>{@code
 * public class Example extends Base {
 *     private final int value;
 *
 *     public Example(int input) {
 *         // Flexible constructor body - statements before super()
 *         value = input * 2;
 *         if (value < 0) {
 *             throw new IllegalArgumentException("Negative value");
 *         }
 *         super(value); // super() can come after statements
 *     }
 * }
 * }</pre>
 *
 * @since 1.0
 * @see <a href="https://openjdk.org/jeps/513">JEP 513: Flexible Constructor Bodies</a>
 */
public class FlexibleConstructorBodiesStrategy implements ParseStrategy
{
	@Override
	public boolean canHandle(JavaVersion version, ParsingPhase phase, ParseContext context)
	{
		// CRITICAL: Phase-aware detection
		// Only valid in constructor bodies, not methods or initializer blocks
		return version.isAtLeast(JavaVersion.JAVA_25) &&
			   phase == ParsingPhase.CONSTRUCTOR_BODY &&
			   context.currentTokenIs(TokenType.LBRACE);
	}

	@Override
	public int parseConstruct(ParseContext context)
	{
		int startPos = context.getCurrentPosition();

		context.expect(TokenType.LBRACE);

		// Parse statements - some may come before super()/this()
		// JEP 513: Compiler enforces that super()/this() is eventually called
		// Parser doesn't need to validate this - javac will handle it
		while (!context.currentTokenIs(TokenType.RBRACE) && !context.isAtEnd())
		{
			context.parseStatement(); // Delegate to parser's statement parsing logic
		}

		context.expect(TokenType.RBRACE);

		int endPos = context.getCurrentPosition();
		return context.getNodeStorage().allocateNode(startPos, endPos - startPos,
			NodeType.FLEXIBLE_CONSTRUCTOR_BODY, -1);
	}

	@Override
	public int getPriority()
	{
		return PRIORITY_PHASE_AWARE;
	}

	@Override
	public String getDescription()
	{
		return "Flexible constructor bodies (Java 25+, JEP 513)";
	}
}
