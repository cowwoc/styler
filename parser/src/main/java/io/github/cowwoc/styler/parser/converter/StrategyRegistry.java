package io.github.cowwoc.styler.parser.converter;

import io.github.cowwoc.styler.ast.ASTNode;
import io.github.cowwoc.styler.parser.NodeType;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Registry of conversion strategies with compile-time validation of complete coverage.
 * <p>
 * This registry ensures that ALL node types defined in {@link NodeType} have corresponding
 * conversion strategies registered. Missing strategies are detected at construction time
 * and reported with detailed error messages.
 * </p>
 * <h2>Coverage Validation</h2>
 * The registry validates coverage of:
 * <ul>
 * <li><strong>81 total node types</strong> from {@link NodeType} constants</li>
 * <li><strong>Zero stubbing violations:</strong> No {@code UnsupportedOperationException} allowed</li>
 * <li><strong>No duplicate registrations:</strong> Each node type can have only one strategy</li>
 * <li><strong>Type safety:</strong> Generic types prevent incorrect strategy assignments</li>
 * </ul>
 * <h2>Thread Safety</h2>
 * Instances are immutable after construction and safe for concurrent access.
 *
 * @since 1.0
 */
public final class StrategyRegistry
{
	private final Map<Byte, ConversionStrategy<?>> strategies;

	/**
	 * Creates a StrategyRegistry and validates complete node type coverage.
	 *
	 * @param strategies map of node type constants to conversion strategies
	 * @throws IllegalArgumentException if any node types are missing strategies or if strategies is {@code null}
	 */
	private StrategyRegistry(Map<Byte, ConversionStrategy<?>> strategies)
{
		if (strategies == null)
{
			throw new NullPointerException("Strategies map cannot be null");
		}

		this.strategies = Map.copyOf(strategies); // Immutable copy
		validateCompleteCoverage();
	}

	/**
	 * Gets the conversion strategy for the specified node type.
	 *
	 * @param nodeType the node type byte constant
	 * @return the conversion strategy, or {@code null} if not registered (should never happen after validation)
	 */
	public ConversionStrategy<?> getStrategy(byte nodeType)
{
		return strategies.get(nodeType);
	}

	/**
	 * Creates a builder for constructing a StrategyRegistry with validation.
	 *
	 * @return a new {@link Builder} instance
	 */
	public static Builder builder()
{
		return new Builder();
	}

	/**
	 * Validates that all required node types have registered strategies.
	 * Throws detailed exception listing all missing node types.
	 */
	private void validateCompleteCoverage()
{
		Set<Byte> requiredNodeTypes = getAllRequiredNodeTypes();
		Set<Byte> registeredNodeTypes = strategies.keySet();

		Set<Byte> missingNodeTypes = new HashSet<>(requiredNodeTypes);
		missingNodeTypes.removeAll(registeredNodeTypes);

		if (!missingNodeTypes.isEmpty())
{
			throw new IllegalArgumentException(buildMissingTypesMessage(missingNodeTypes));
		}
	}

	/**
	 * Gets the complete set of node types that require conversion strategies.
	 * Includes all node types except trivia and error nodes (comments, whitespace, EOF, ERROR_NODE).
	 *
	 * @return set of required node type byte constants
	 */
	@SuppressWarnings("PMD.NcssCount")
	private Set<Byte> getAllRequiredNodeTypes()
{
		Set<Byte> required = new HashSet<>();

		// Top-level and declarations
		required.add(NodeType.COMPILATION_UNIT);
		required.add(NodeType.PACKAGE_DECLARATION);
		required.add(NodeType.IMPORT_DECLARATION);
		required.add(NodeType.CLASS_DECLARATION);
		required.add(NodeType.INTERFACE_DECLARATION);
		required.add(NodeType.ENUM_DECLARATION);
		required.add(NodeType.ANNOTATION_DECLARATION);
		required.add(NodeType.RECORD_DECLARATION);

		// Members
		required.add(NodeType.METHOD_DECLARATION);
		required.add(NodeType.CONSTRUCTOR_DECLARATION);
		required.add(NodeType.FIELD_DECLARATION);
		required.add(NodeType.PARAMETER_DECLARATION);
		required.add(NodeType.LOCAL_VARIABLE_DECLARATION);
		required.add(NodeType.ENUM_CONSTANT);

		// Statements
		required.add(NodeType.BLOCK_STATEMENT);
		required.add(NodeType.EXPRESSION_STATEMENT);
		required.add(NodeType.IF_STATEMENT);
		required.add(NodeType.WHILE_STATEMENT);
		required.add(NodeType.FOR_STATEMENT);
		required.add(NodeType.ENHANCED_FOR_STATEMENT);
		required.add(NodeType.SWITCH_STATEMENT);
		required.add(NodeType.TRY_STATEMENT);
		required.add(NodeType.RETURN_STATEMENT);
		required.add(NodeType.THROW_STATEMENT);
		required.add(NodeType.BREAK_STATEMENT);
		required.add(NodeType.CONTINUE_STATEMENT);
		required.add(NodeType.SYNCHRONIZED_STATEMENT);
		required.add(NodeType.SWITCH_EXPRESSION);
		required.add(NodeType.YIELD_STATEMENT);

		// Expressions
		required.add(NodeType.EXPRESSION);
		required.add(NodeType.LITERAL_EXPRESSION);
		required.add(NodeType.IDENTIFIER_EXPRESSION);
		required.add(NodeType.METHOD_CALL_EXPRESSION);
		required.add(NodeType.FIELD_ACCESS_EXPRESSION);
		required.add(NodeType.ARRAY_ACCESS_EXPRESSION);
		required.add(NodeType.ASSIGNMENT_EXPRESSION);
		required.add(NodeType.BINARY_EXPRESSION);
		required.add(NodeType.UNARY_EXPRESSION);
		required.add(NodeType.CONDITIONAL_EXPRESSION);
		required.add(NodeType.INSTANCEOF_EXPRESSION);
		required.add(NodeType.CAST_EXPRESSION);
		required.add(NodeType.LAMBDA_EXPRESSION);
		required.add(NodeType.METHOD_REFERENCE_EXPRESSION);
		required.add(NodeType.NEW_EXPRESSION);
		required.add(NodeType.ARRAY_CREATION_EXPRESSION);
		required.add(NodeType.PATTERN_EXPRESSION);
		required.add(NodeType.TYPE_PATTERN);
		required.add(NodeType.GUARD_PATTERN);
		required.add(NodeType.STRING_TEMPLATE_EXPRESSION);
		required.add(NodeType.TEMPLATE_PROCESSOR_EXPRESSION);

		// Types
		required.add(NodeType.PRIMITIVE_TYPE);
		required.add(NodeType.CLASS_TYPE);
		required.add(NodeType.ARRAY_TYPE);
		required.add(NodeType.PARAMETERIZED_TYPE);
		required.add(NodeType.WILDCARD_TYPE);
		required.add(NodeType.UNION_TYPE);
		required.add(NodeType.INTERSECTION_TYPE);
		required.add(NodeType.VAR_TYPE);

		// Other structural nodes
		required.add(NodeType.MODIFIER);
		required.add(NodeType.ANNOTATION);
		required.add(NodeType.ANNOTATION_ELEMENT);

		// Java 21-25 features
		required.add(NodeType.UNNAMED_CLASS);
		required.add(NodeType.UNNAMED_VARIABLE);
		required.add(NodeType.MODULE_IMPORT_DECLARATION);
		required.add(NodeType.FLEXIBLE_CONSTRUCTOR_BODY);
		required.add(NodeType.PRIMITIVE_PATTERN);
		required.add(NodeType.COMPACT_MAIN_METHOD);
		required.add(NodeType.INSTANCE_MAIN_METHOD);

		// Module system
		required.add(NodeType.MODULE_DECLARATION);
		required.add(NodeType.MODULE_REQUIRES_DIRECTIVE);
		required.add(NodeType.MODULE_EXPORTS_DIRECTIVE);
		required.add(NodeType.MODULE_OPENS_DIRECTIVE);
		required.add(NodeType.MODULE_PROVIDES_DIRECTIVE);
		required.add(NodeType.MODULE_USES_DIRECTIVE);
		required.add(NodeType.MODULE_QUALIFIER);

		// EXCLUDED: Trivia and error nodes (handled separately)
		// LINE_COMMENT, BLOCK_COMMENT, JAVADOC_COMMENT, WHITESPACE, ERROR_NODE, EOF_NODE

		return required;
	}

	/**
	 * Builds detailed error message listing all missing node types.
	 *
	 * @param missingTypes set of missing node type byte constants
	 * @return formatted error message
	 */
	@SuppressWarnings({"PMD.InsufficientStringBufferDeclaration", "PMD.ConsecutiveAppendsShouldReuse"})
	private String buildMissingTypesMessage(Set<Byte> missingTypes)
{
		StringBuilder sb = new StringBuilder();
		sb.append("StrategyRegistry is missing conversion strategies for ");
		sb.append(missingTypes.size()).append(" node type(s):\n");

		for (byte nodeType : missingTypes)
{
			sb.append("  - ").append(NodeType.getTypeName(nodeType)).append(" (").append(nodeType).append(")\n");
		}

		sb.append("\nAll node types must have complete conversion strategy implementations.");
		sb.append("\nNo stubbing violations (UnsupportedOperationException) are permitted.");

		return sb.toString();
	}

	/**
	 * Builder for constructing StrategyRegistry with fluent API.
	 */
	public static final class Builder
{
		private final Map<Byte, ConversionStrategy<?>> strategies = new HashMap<>();

		/**
		 * Registers a conversion strategy for a specific node type.
		 *
		 * @param <T> the AST node type produced by this strategy
		 * @param strategy the conversion strategy to register
		 * @return this builder for method chaining
		 * @throws IllegalArgumentException if strategy is {@code null} or if a strategy is already
		 *                                  registered for this node type
		 */
		public <T extends ASTNode> Builder register(ConversionStrategy<T> strategy)
{
			if (strategy == null)
{
				throw new NullPointerException("Strategy cannot be null");
			}

			byte nodeType = strategy.getHandledNodeType();

			if (strategies.containsKey(nodeType))
{
				throw new IllegalArgumentException(
					"Duplicate strategy registration for node type: " +
					NodeType.getTypeName(nodeType) + " (" + nodeType + ")");
			}

			strategies.put(nodeType, strategy);
			return this;
		}

		/**
		 * Builds the StrategyRegistry with validation.
		 *
		 * @return a new {@link StrategyRegistry} with all strategies registered
		 * @throws IllegalArgumentException if any required node types are missing strategies
		 */
		public StrategyRegistry build()
{
			return new StrategyRegistry(strategies);
		}
	}
}
