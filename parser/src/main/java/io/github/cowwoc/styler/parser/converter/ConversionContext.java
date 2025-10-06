package io.github.cowwoc.styler.parser.converter;

import io.github.cowwoc.styler.ast.ASTNode;
import io.github.cowwoc.styler.ast.Comment;
import io.github.cowwoc.styler.ast.FormattingHints;
import io.github.cowwoc.styler.ast.SourcePosition;
import io.github.cowwoc.styler.ast.SourceRange;
import io.github.cowwoc.styler.ast.WhitespaceInfo;
import io.github.cowwoc.styler.parser.ArenaNodeStorage;

import java.util.List;
import java.util.Optional;

/**
 * Conversion context providing helper services for Arena-to-AST conversion strategies.
 * <p>
 * This class provides stateless helper methods that strategies use during conversion:
 * <ul>
 * <li><strong>Position Mapping:</strong> Convert Arena offsets to AST source positions/ranges</li>
 * <li><strong>Child Conversion:</strong> Recursively convert child Arena nodes to AST nodes</li>
 * <li><strong>Metadata Creation:</strong> Build AST metadata (comments, whitespace, hints)</li>
 * <li><strong>Validation:</strong> Check Arena lifecycle state</li>
 * </ul>
 * </p>
 * <h2>Thread Safety</h2>
 * This class is stateless and thread-safe. All state is passed as method parameters,
 * enabling concurrent conversion from multiple threads.
 * <h2>Design Pattern</h2>
 * This is a <strong>Service Object</strong> pattern providing helper methods to strategies,
 * promoting code reuse and maintaining separation of concerns.
 *
 * @since 1.0
 */
public final class ConversionContext
{
	private final SourcePositionMapper positionMapper;
	private final StrategyRegistry strategyRegistry;

	/**
	 * Creates a ConversionContext with position mapping and strategy registry.
	 *
	 * @param positionMapper the source position mapper for this source file
	 * @param strategyRegistry the registry of conversion strategies
	 * @throws IllegalArgumentException if positionMapper or strategyRegistry is {@code null}
	 */
	public ConversionContext(SourcePositionMapper positionMapper,
		StrategyRegistry strategyRegistry)
{
		if (positionMapper == null)
{
			throw new NullPointerException("Position mapper cannot be null");
		}

		if (strategyRegistry == null)
{
			throw new NullPointerException("Strategy registry cannot be null");
		}

		this.positionMapper = positionMapper;
		this.strategyRegistry = strategyRegistry;
	}

	/**
	 * Converts an Arena node to an AST node using the appropriate strategy.
	 * This is the main entry point for recursive child conversion.
	 *
	 * @param nodeId the Arena node ID to convert
	 * @param nodeStorage the Arena node storage
	 * @return the converted AST node
	 * @throws UnsupportedNodeTypeException if no strategy exists for this node type
	 * @throws InvalidNodeStructureException if node structure is invalid
	 * @throws ArenaClosedException if Arena is closed
	 */
	public ASTNode convertNode(int nodeId, ArenaNodeStorage nodeStorage)
{
		validateArenaAlive(nodeStorage);

		ArenaNodeStorage.NodeInfo nodeInfo = nodeStorage.getNode(nodeId);
		byte nodeType = nodeInfo.nodeType();

		ConversionStrategy<?> strategy = strategyRegistry.getStrategy(nodeType);
		if (strategy == null)
{
			SourceRange range = getSourceRange(nodeInfo.startOffset(), nodeInfo.endOffset());
			throw new UnsupportedNodeTypeException(nodeId, nodeType, range);
		}

		return strategy.convert(nodeId, nodeStorage, this);
	}

	/**
	 * Gets the source range for the given Arena node offsets.
	 *
	 * @param startOffset the starting offset in source text
	 * @param endOffset the ending offset in source text
	 * @return the {@link SourceRange} for this offset range
	 */
	public SourceRange getSourceRange(int startOffset, int endOffset)
{
		return positionMapper.getRange(startOffset, endOffset);
	}

	/**
	 * Gets the source position for the given Arena node offset.
	 *
	 * @param offset the offset in source text
	 * @return the {@link SourcePosition} for this offset
	 */
	public SourcePosition getSourcePosition(int offset)
{
		return positionMapper.getPosition(offset);
	}

	/**
	 * Extracts source text substring for a node using its start and end offsets.
	 *
	 * @param startOffset the starting offset in source text
	 * @param endOffset the ending offset in source text (exclusive)
	 * @return the source text substring
	 */
	public String getSourceText(int startOffset, int endOffset)
{
		return positionMapper.getSourceText().substring(startOffset, endOffset);
	}

	/**
	 * Creates empty leading comments list (placeholder for future comment extraction).
	 * <p>
	 * Currently returns empty list. Future enhancement will extract actual comments
	 * from Arena trivia nodes (LINE_COMMENT, BLOCK_COMMENT, JAVADOC_COMMENT).
	 * </p>
	 *
	 * @return empty list of comments
	 */
	public List<Comment> getLeadingComments()
{
		return List.of();
	}

	/**
	 * Creates empty trailing comments list (placeholder for future comment extraction).
	 * <p>
	 * Currently returns empty list. Future enhancement will extract actual comments
	 * from Arena trivia nodes (LINE_COMMENT, BLOCK_COMMENT, JAVADOC_COMMENT).
	 * </p>
	 *
	 * @return empty list of comments
	 */
	public List<Comment> getTrailingComments()
{
		return List.of();
	}

	/**
	 * Creates default whitespace info (placeholder for future whitespace extraction).
	 * <p>
	 * Currently returns default/empty whitespace. Future enhancement will extract
	 * actual whitespace from Arena trivia nodes (WHITESPACE).
	 * </p>
	 *
	 * @return default {@link WhitespaceInfo}
	 */
	public WhitespaceInfo getWhitespace()
{
		return WhitespaceInfo.none();
	}

	/**
	 * Creates default formatting hints (placeholder for future hint extraction).
	 * <p>
	 * Currently returns default hints. Future enhancement may infer formatting hints
	 * from source structure or parser metadata.
	 * </p>
	 *
	 * @return default {@link FormattingHints}
	 */
	public FormattingHints getFormattingHints()
{
		return FormattingHints.defaults();
	}

	/**
	 * Gets the parent node reference (empty for MVP).
	 * <p>
	 * Currently returns empty Optional. Future enhancement will track parent
	 * relationships during conversion to enable parent back-references.
	 * </p>
	 *
	 * @return empty Optional (no parent tracking in MVP)
	 */
	public Optional<ASTNode> getParentNode()
{
		return Optional.empty();
	}

	/**
	 * Validates that the Arena is still alive and usable.
	 *
	 * @param nodeStorage the Arena node storage to check
	 * @throws ArenaClosedException if Arena has been closed
	 */
	private void validateArenaAlive(ArenaNodeStorage nodeStorage)
{
		if (!nodeStorage.isAlive())
{
			throw new ArenaClosedException(
				"Cannot convert nodes after Arena has been closed. " +
				"Ensure Arena lifecycle extends through entire conversion process.");
		}
	}
}
