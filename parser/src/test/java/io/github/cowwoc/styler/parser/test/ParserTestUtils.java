package io.github.cowwoc.styler.parser.test;

import io.github.cowwoc.styler.ast.core.ImportAttribute;
import io.github.cowwoc.styler.ast.core.NodeArena;
import io.github.cowwoc.styler.ast.core.NodeIndex;
import io.github.cowwoc.styler.ast.core.NodeType;
import io.github.cowwoc.styler.parser.ParseResult;
import io.github.cowwoc.styler.parser.Parser;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;

/**
 * Utility methods for parser tests.
 * <p>
 * Provides AST validation capabilities to verify that parsing produces the expected node structure,
 * not just that parsing succeeds without exceptions.
 */
public final class ParserTestUtils
{
	private ParserTestUtils()
	{
	}

	/**
	 * Represents an AST node with its type, source positions, and optional attribute value.
	 * <p>
	 * This is the semantic representation of an AST node, including its exact position in source code.
	 * Two ASTs with the same set of SemanticNodes are semantically identical, regardless of arena
	 * allocation order.
	 * <p>
	 * The tree structure is implicit in the positions: a node A contains node B if
	 * {@code A.start <= B.start && B.end <= A.end}.
	 * <p>
	 * Use type-specific factory methods to create instances: {@link #compilationUnit(int, int)},
	 * {@link #importNode(int, int, String, boolean)},
	 * {@link #typeDeclaration(NodeType, int, int, String)}, etc.
	 */
	public static final class SemanticNode
	{
		private final NodeType type;
		private final int start;
		private final int end;
		private final String attributeValue;
		private final Boolean isStatic;

		private SemanticNode(NodeType type, int start, int end, String attributeValue, Boolean isStatic)
		{
			requireThat(type, "type").isNotNull();
			requireThat(start, "start").isNotNegative();
			// Only COMPILATION_UNIT can have zero-width span (empty source file)
			if (type == NodeType.COMPILATION_UNIT)
				requireThat(end, "end").isGreaterThanOrEqualTo(start, "start");
			else
				requireThat(end, "end").isGreaterThan(start, "start");
			if (attributeValue != null)
				requireThat(attributeValue, "attributeValue").isNotEmpty();

			this.type = type;
			this.start = start;
			this.end = end;
			this.attributeValue = attributeValue;
			this.isStatic = isStatic;
		}

		/**
		 * @return the node type
		 */
		public NodeType type()
		{
			return type;
		}

		/**
		 * @return the start position in source code (inclusive)
		 */
		public int start()
		{
			return start;
		}

		/**
		 * @return the end position in source code (exclusive)
		 */
		public int end()
		{
			return end;
		}

		/**
		 * @return the attribute value, or {@code null} if the node has no attribute
		 */
		public String attributeValue()
		{
			return attributeValue;
		}

		/**
		 * @return {@code true} if this is a static import, {@code false} for regular imports,
		 *    {@code null} for non-import nodes
		 */
		public Boolean isStatic()
		{
			return isStatic;
		}

		@Override
		public boolean equals(Object obj)
		{
			return obj instanceof SemanticNode other &&
				type == other.type &&
				start == other.start &&
				end == other.end &&
				Objects.equals(attributeValue, other.attributeValue) &&
				Objects.equals(isStatic, other.isStatic);
		}

		@Override
		public int hashCode()
		{
			return Objects.hash(type, start, end, attributeValue, isStatic);
		}

		@Override
		public String toString()
		{
			StringBuilder sb = new StringBuilder(128);
			sb.append("SemanticNode[type=").append(type).
				append(", start=").append(start).
				append(", end=").append(end);
			if (attributeValue != null)
				sb.append(", attributeValue=").append(attributeValue);
			if (isStatic != null)
				sb.append(", isStatic=").append(isStatic);
			sb.append(']');
			return sb.toString();
		}
	}

	// ========== Factory methods for types requiring attributes ==========

	/**
	 * Creates an import declaration node.
	 *
	 * @param start         the start position in source code (inclusive)
	 * @param end           the end position in source code (exclusive)
	 * @param qualifiedName the fully qualified name of the imported type or package
	 * @param isStatic      {@code true} if this is a static import
	 * @return a new semantic import node
	 * @throws NullPointerException     if {@code qualifiedName} is null
	 * @throws IllegalArgumentException if {@code start} is negative, or if {@code end} is less than or
	 *                                  equal to {@code start}, or if {@code qualifiedName} is empty
	 */
	public static SemanticNode importNode(int start, int end, String qualifiedName, boolean isStatic)
	{
		requireThat(qualifiedName, "qualifiedName").isNotEmpty();
		return new SemanticNode(NodeType.IMPORT_DECLARATION, start, end, qualifiedName, isStatic);
	}

	/**
	 * Creates a module import declaration node.
	 *
	 * @param start      the start position in source code (inclusive)
	 * @param end        the end position in source code (exclusive)
	 * @param moduleName the name of the imported module
	 * @return a new semantic module import node
	 * @throws NullPointerException     if {@code moduleName} is null
	 * @throws IllegalArgumentException if {@code start} is negative, or if {@code end} is less than or
	 *                                  equal to {@code start}, or if {@code moduleName} is empty
	 */
	public static SemanticNode moduleImportNode(int start, int end, String moduleName)
	{
		requireThat(moduleName, "moduleName").isNotEmpty();
		return new SemanticNode(NodeType.MODULE_IMPORT_DECLARATION, start, end, moduleName, null);
	}

	/**
	 * Creates a package declaration node.
	 *
	 * @param start       the start position in source code (inclusive)
	 * @param end         the end position in source code (exclusive)
	 * @param packageName the package name
	 * @return a new semantic package node
	 * @throws NullPointerException     if {@code packageName} is null
	 * @throws IllegalArgumentException if {@code start} is negative, or if {@code end} is less than or
	 *                                  equal to {@code start}, or if {@code packageName} is empty
	 */
	public static SemanticNode packageNode(int start, int end, String packageName)
	{
		requireThat(packageName, "packageName").isNotEmpty();
		return new SemanticNode(NodeType.PACKAGE_DECLARATION, start, end, packageName, null);
	}

	/**
	 * Creates a type declaration node (class, interface, enum, record, or annotation).
	 *
	 * @param type     the node type (must be a type declaration type)
	 * @param start    the start position in source code (inclusive)
	 * @param end      the end position in source code (exclusive)
	 * @param typeName the declared type name
	 * @return a new semantic type declaration node
	 * @throws NullPointerException     if {@code type} or {@code typeName} is null
	 * @throws IllegalArgumentException if {@code type} is not a type declaration type, or if
	 *                                  {@code start} is negative, or if {@code end} is less than or equal
	 *                                  to {@code start}, or if {@code typeName} is empty
	 */
	public static SemanticNode typeDeclaration(NodeType type, int start, int end, String typeName)
	{
		requireThat(type, "type").isNotNull();
		requireThat(typeName, "typeName").isNotEmpty();
		if (!isTypeDeclaration(type))
		{
			throw new IllegalArgumentException("Expected a type declaration type but got: " + type);
		}
		return new SemanticNode(type, start, end, typeName, null);
	}

	/**
	 * Creates a parameter declaration node.
	 *
	 * @param start the start position in source code (inclusive)
	 * @param end   the end position in source code (exclusive)
	 * @param name  the parameter name
	 * @return a new semantic parameter node
	 * @throws NullPointerException     if {@code name} is null
	 * @throws IllegalArgumentException if {@code start} is negative, or if {@code end} is less than or
	 *                                  equal to {@code start}, or if {@code name} is empty
	 */
	public static SemanticNode parameterNode(int start, int end, String name)
	{
		requireThat(name, "name").isNotEmpty();
		return new SemanticNode(NodeType.PARAMETER_DECLARATION, start, end, name, null);
	}

	/**
	 * @param type the node type
	 * @return {@code true} if the node type is a type declaration
	 */
	private static boolean isTypeDeclaration(NodeType type)
	{
		return switch (type)
		{
			case CLASS_DECLARATION, INTERFACE_DECLARATION, ENUM_DECLARATION,
				RECORD_DECLARATION, ANNOTATION_DECLARATION -> true;
			default -> false;
		};
	}

	// ========== Factory methods for types without attributes ==========

	/**
	 * Creates a LINE_COMMENT node.
	 *
	 * @param start the start position in source code (inclusive)
	 * @param end   the end position in source code (exclusive)
	 * @return a new semantic node
	 */
	public static SemanticNode lineComment(int start, int end)
	{
		return new SemanticNode(NodeType.LINE_COMMENT, start, end, null, null);
	}

	/**
	 * Creates a MARKDOWN_DOC_COMMENT node.
	 *
	 * @param start the start position in source code (inclusive)
	 * @param end   the end position in source code (exclusive)
	 * @return a new semantic node
	 */
	public static SemanticNode markdownDocComment(int start, int end)
	{
		return new SemanticNode(NodeType.MARKDOWN_DOC_COMMENT, start, end, null, null);
	}

	/**
	 * Creates a BLOCK_COMMENT node.
	 *
	 * @param start the start position in source code (inclusive)
	 * @param end   the end position in source code (exclusive)
	 * @return a new semantic node
	 */
	public static SemanticNode blockComment(int start, int end)
	{
		return new SemanticNode(NodeType.BLOCK_COMMENT, start, end, null, null);
	}

	/**
	 * Creates a JAVADOC_COMMENT node.
	 *
	 * @param start the start position in source code (inclusive)
	 * @param end   the end position in source code (exclusive)
	 * @return a new semantic node
	 */
	public static SemanticNode javadocComment(int start, int end)
	{
		return new SemanticNode(NodeType.JAVADOC_COMMENT, start, end, null, null);
	}

	/**
	 * Creates an INTEGER_LITERAL node.
	 *
	 * @param start the start position in source code (inclusive)
	 * @param end   the end position in source code (exclusive)
	 * @return a new semantic node
	 */
	public static SemanticNode integerLiteral(int start, int end)
	{
		return new SemanticNode(NodeType.INTEGER_LITERAL, start, end, null, null);
	}

	/**
	 * Creates a LONG_LITERAL node.
	 *
	 * @param start the start position in source code (inclusive)
	 * @param end   the end position in source code (exclusive)
	 * @return a new semantic node
	 */
	public static SemanticNode longLiteral(int start, int end)
	{
		return new SemanticNode(NodeType.LONG_LITERAL, start, end, null, null);
	}

	/**
	 * Creates a FLOAT_LITERAL node.
	 *
	 * @param start the start position in source code (inclusive)
	 * @param end   the end position in source code (exclusive)
	 * @return a new semantic node
	 */
	public static SemanticNode floatLiteral(int start, int end)
	{
		return new SemanticNode(NodeType.FLOAT_LITERAL, start, end, null, null);
	}

	/**
	 * Creates a DOUBLE_LITERAL node.
	 *
	 * @param start the start position in source code (inclusive)
	 * @param end   the end position in source code (exclusive)
	 * @return a new semantic node
	 */
	public static SemanticNode doubleLiteral(int start, int end)
	{
		return new SemanticNode(NodeType.DOUBLE_LITERAL, start, end, null, null);
	}

	/**
	 * Creates a BOOLEAN_LITERAL node.
	 *
	 * @param start the start position in source code (inclusive)
	 * @param end   the end position in source code (exclusive)
	 * @return a new semantic node
	 */
	public static SemanticNode booleanLiteral(int start, int end)
	{
		return new SemanticNode(NodeType.BOOLEAN_LITERAL, start, end, null, null);
	}

	/**
	 * Creates a CHAR_LITERAL node.
	 *
	 * @param start the start position in source code (inclusive)
	 * @param end   the end position in source code (exclusive)
	 * @return a new semantic node
	 */
	public static SemanticNode charLiteral(int start, int end)
	{
		return new SemanticNode(NodeType.CHAR_LITERAL, start, end, null, null);
	}

	/**
	 * Creates a STRING_LITERAL node.
	 *
	 * @param start the start position in source code (inclusive)
	 * @param end   the end position in source code (exclusive)
	 * @return a new semantic node
	 */
	public static SemanticNode stringLiteral(int start, int end)
	{
		return new SemanticNode(NodeType.STRING_LITERAL, start, end, null, null);
	}

	/**
	 * Creates a NULL_LITERAL node.
	 *
	 * @param start the start position in source code (inclusive)
	 * @param end   the end position in source code (exclusive)
	 * @return a new semantic node
	 */
	public static SemanticNode nullLiteral(int start, int end)
	{
		return new SemanticNode(NodeType.NULL_LITERAL, start, end, null, null);
	}

	/**
	 * Creates an IDENTIFIER node.
	 *
	 * @param start the start position in source code (inclusive)
	 * @param end   the end position in source code (exclusive)
	 * @return a new semantic node
	 */
	public static SemanticNode identifier(int start, int end)
	{
		return new SemanticNode(NodeType.IDENTIFIER, start, end, null, null);
	}

	/**
	 * Creates a QUALIFIED_NAME node.
	 *
	 * @param start the start position in source code (inclusive)
	 * @param end   the end position in source code (exclusive)
	 * @return a new semantic node
	 */
	public static SemanticNode qualifiedName(int start, int end)
	{
		return new SemanticNode(NodeType.QUALIFIED_NAME, start, end, null, null);
	}

	/**
	 * Creates a BINARY_EXPRESSION node.
	 *
	 * @param start the start position in source code (inclusive)
	 * @param end   the end position in source code (exclusive)
	 * @return a new semantic node
	 */
	public static SemanticNode binaryExpression(int start, int end)
	{
		return new SemanticNode(NodeType.BINARY_EXPRESSION, start, end, null, null);
	}

	/**
	 * Creates a UNARY_EXPRESSION node.
	 *
	 * @param start the start position in source code (inclusive)
	 * @param end   the end position in source code (exclusive)
	 * @return a new semantic node
	 */
	public static SemanticNode unaryExpression(int start, int end)
	{
		return new SemanticNode(NodeType.UNARY_EXPRESSION, start, end, null, null);
	}

	/**
	 * Creates a POSTFIX_EXPRESSION node.
	 *
	 * @param start the start position in source code (inclusive)
	 * @param end   the end position in source code (exclusive)
	 * @return a new semantic node
	 */
	public static SemanticNode postfixExpression(int start, int end)
	{
		return new SemanticNode(NodeType.POSTFIX_EXPRESSION, start, end, null, null);
	}

	/**
	 * Creates an ASSIGNMENT_EXPRESSION node.
	 *
	 * @param start the start position in source code (inclusive)
	 * @param end   the end position in source code (exclusive)
	 * @return a new semantic node
	 */
	public static SemanticNode assignmentExpression(int start, int end)
	{
		return new SemanticNode(NodeType.ASSIGNMENT_EXPRESSION, start, end, null, null);
	}

	/**
	 * Creates a CONDITIONAL_EXPRESSION node.
	 *
	 * @param start the start position in source code (inclusive)
	 * @param end   the end position in source code (exclusive)
	 * @return a new semantic node
	 */
	public static SemanticNode conditionalExpression(int start, int end)
	{
		return new SemanticNode(NodeType.CONDITIONAL_EXPRESSION, start, end, null, null);
	}

	/**
	 * Creates an INSTANCEOF_EXPRESSION node.
	 *
	 * @param start the start position in source code (inclusive)
	 * @param end   the end position in source code (exclusive)
	 * @return a new semantic node
	 */
	public static SemanticNode instanceofExpression(int start, int end)
	{
		return new SemanticNode(NodeType.INSTANCEOF_EXPRESSION, start, end, null, null);
	}

	/**
	 * Creates a LAMBDA_EXPRESSION node.
	 *
	 * @param start the start position in source code (inclusive)
	 * @param end   the end position in source code (exclusive)
	 * @return a new semantic node
	 */
	public static SemanticNode lambdaExpression(int start, int end)
	{
		return new SemanticNode(NodeType.LAMBDA_EXPRESSION, start, end, null, null);
	}

	/**
	 * Creates a METHOD_REFERENCE node.
	 *
	 * @param start the start position in source code (inclusive)
	 * @param end   the end position in source code (exclusive)
	 * @return a new semantic node
	 */
	public static SemanticNode methodReference(int start, int end)
	{
		return new SemanticNode(NodeType.METHOD_REFERENCE, start, end, null, null);
	}

	/**
	 * Creates a CAST_EXPRESSION node.
	 *
	 * @param start the start position in source code (inclusive)
	 * @param end   the end position in source code (exclusive)
	 * @return a new semantic node
	 */
	public static SemanticNode castExpression(int start, int end)
	{
		return new SemanticNode(NodeType.CAST_EXPRESSION, start, end, null, null);
	}

	/**
	 * Creates an ARRAY_ACCESS node.
	 *
	 * @param start the start position in source code (inclusive)
	 * @param end   the end position in source code (exclusive)
	 * @return a new semantic node
	 */
	public static SemanticNode arrayAccess(int start, int end)
	{
		return new SemanticNode(NodeType.ARRAY_ACCESS, start, end, null, null);
	}

	/**
	 * Creates a FIELD_ACCESS node.
	 *
	 * @param start the start position in source code (inclusive)
	 * @param end   the end position in source code (exclusive)
	 * @return a new semantic node
	 */
	public static SemanticNode fieldAccess(int start, int end)
	{
		return new SemanticNode(NodeType.FIELD_ACCESS, start, end, null, null);
	}

	/**
	 * Creates a METHOD_INVOCATION node.
	 *
	 * @param start the start position in source code (inclusive)
	 * @param end   the end position in source code (exclusive)
	 * @return a new semantic node
	 */
	public static SemanticNode methodInvocation(int start, int end)
	{
		return new SemanticNode(NodeType.METHOD_INVOCATION, start, end, null, null);
	}

	/**
	 * Creates a PARENTHESIZED_EXPRESSION node.
	 *
	 * @param start the start position in source code (inclusive)
	 * @param end   the end position in source code (exclusive)
	 * @return a new semantic node
	 */
	public static SemanticNode parenthesizedExpression(int start, int end)
	{
		return new SemanticNode(NodeType.PARENTHESIZED_EXPRESSION, start, end, null, null);
	}

	/**
	 * Creates an ARRAY_CREATION node.
	 *
	 * @param start the start position in source code (inclusive)
	 * @param end   the end position in source code (exclusive)
	 * @return a new semantic node
	 */
	public static SemanticNode arrayCreation(int start, int end)
	{
		return new SemanticNode(NodeType.ARRAY_CREATION, start, end, null, null);
	}

	/**
	 * Creates an OBJECT_CREATION node.
	 *
	 * @param start the start position in source code (inclusive)
	 * @param end   the end position in source code (exclusive)
	 * @return a new semantic node
	 */
	public static SemanticNode objectCreation(int start, int end)
	{
		return new SemanticNode(NodeType.OBJECT_CREATION, start, end, null, null);
	}

	/**
	 * Creates a THIS_EXPRESSION node.
	 *
	 * @param start the start position in source code (inclusive)
	 * @param end   the end position in source code (exclusive)
	 * @return a new semantic node
	 */
	public static SemanticNode thisExpression(int start, int end)
	{
		return new SemanticNode(NodeType.THIS_EXPRESSION, start, end, null, null);
	}

	/**
	 * Creates a SUPER_EXPRESSION node.
	 *
	 * @param start the start position in source code (inclusive)
	 * @param end   the end position in source code (exclusive)
	 * @return a new semantic node
	 */
	public static SemanticNode superExpression(int start, int end)
	{
		return new SemanticNode(NodeType.SUPER_EXPRESSION, start, end, null, null);
	}

	/**
	 * Creates an ARRAY_INITIALIZER node.
	 *
	 * @param start the start position in source code (inclusive)
	 * @param end   the end position in source code (exclusive)
	 * @return a new semantic node
	 */
	public static SemanticNode arrayInitializer(int start, int end)
	{
		return new SemanticNode(NodeType.ARRAY_INITIALIZER, start, end, null, null);
	}

	/**
	 * Creates a CLASS_LITERAL node.
	 *
	 * @param start the start position in source code (inclusive)
	 * @param end   the end position in source code (exclusive)
	 * @return a new semantic node
	 */
	public static SemanticNode classLiteral(int start, int end)
	{
		return new SemanticNode(NodeType.CLASS_LITERAL, start, end, null, null);
	}

	/**
	 * Creates a BLOCK node.
	 *
	 * @param start the start position in source code (inclusive)
	 * @param end   the end position in source code (exclusive)
	 * @return a new semantic node
	 */
	public static SemanticNode block(int start, int end)
	{
		return new SemanticNode(NodeType.BLOCK, start, end, null, null);
	}

	/**
	 * Creates an EXPRESSION_STATEMENT node.
	 *
	 * @param start the start position in source code (inclusive)
	 * @param end   the end position in source code (exclusive)
	 * @return a new semantic node
	 */
	public static SemanticNode expressionStatement(int start, int end)
	{
		return new SemanticNode(NodeType.EXPRESSION_STATEMENT, start, end, null, null);
	}

	/**
	 * Creates a VARIABLE_DECLARATION node.
	 *
	 * @param start the start position in source code (inclusive)
	 * @param end   the end position in source code (exclusive)
	 * @return a new semantic node
	 */
	public static SemanticNode variableDeclaration(int start, int end)
	{
		return new SemanticNode(NodeType.VARIABLE_DECLARATION, start, end, null, null);
	}

	/**
	 * Creates an IF_STATEMENT node.
	 *
	 * @param start the start position in source code (inclusive)
	 * @param end   the end position in source code (exclusive)
	 * @return a new semantic node
	 */
	public static SemanticNode ifStatement(int start, int end)
	{
		return new SemanticNode(NodeType.IF_STATEMENT, start, end, null, null);
	}

	/**
	 * Creates a FOR_STATEMENT node.
	 *
	 * @param start the start position in source code (inclusive)
	 * @param end   the end position in source code (exclusive)
	 * @return a new semantic node
	 */
	public static SemanticNode forStatement(int start, int end)
	{
		return new SemanticNode(NodeType.FOR_STATEMENT, start, end, null, null);
	}

	/**
	 * Creates an ENHANCED_FOR_STATEMENT node.
	 *
	 * @param start the start position in source code (inclusive)
	 * @param end   the end position in source code (exclusive)
	 * @return a new semantic node
	 */
	public static SemanticNode enhancedForStatement(int start, int end)
	{
		return new SemanticNode(NodeType.ENHANCED_FOR_STATEMENT, start, end, null, null);
	}

	/**
	 * Creates a WHILE_STATEMENT node.
	 *
	 * @param start the start position in source code (inclusive)
	 * @param end   the end position in source code (exclusive)
	 * @return a new semantic node
	 */
	public static SemanticNode whileStatement(int start, int end)
	{
		return new SemanticNode(NodeType.WHILE_STATEMENT, start, end, null, null);
	}

	/**
	 * Creates a DO_WHILE_STATEMENT node.
	 *
	 * @param start the start position in source code (inclusive)
	 * @param end   the end position in source code (exclusive)
	 * @return a new semantic node
	 */
	public static SemanticNode doWhileStatement(int start, int end)
	{
		return new SemanticNode(NodeType.DO_WHILE_STATEMENT, start, end, null, null);
	}

	/**
	 * Creates a SWITCH_STATEMENT node.
	 *
	 * @param start the start position in source code (inclusive)
	 * @param end   the end position in source code (exclusive)
	 * @return a new semantic node
	 */
	public static SemanticNode switchStatement(int start, int end)
	{
		return new SemanticNode(NodeType.SWITCH_STATEMENT, start, end, null, null);
	}

	/**
	 * Creates a SWITCH_EXPRESSION node.
	 *
	 * @param start the start position in source code (inclusive)
	 * @param end   the end position in source code (exclusive)
	 * @return a new semantic node
	 */
	public static SemanticNode switchExpression(int start, int end)
	{
		return new SemanticNode(NodeType.SWITCH_EXPRESSION, start, end, null, null);
	}

	/**
	 * Creates a SWITCH_CASE node.
	 *
	 * @param start the start position in source code (inclusive)
	 * @param end   the end position in source code (exclusive)
	 * @return a new semantic node
	 */
	public static SemanticNode switchCase(int start, int end)
	{
		return new SemanticNode(NodeType.SWITCH_CASE, start, end, null, null);
	}

	/**
	 * Creates a RETURN_STATEMENT node.
	 *
	 * @param start the start position in source code (inclusive)
	 * @param end   the end position in source code (exclusive)
	 * @return a new semantic node
	 */
	public static SemanticNode returnStatement(int start, int end)
	{
		return new SemanticNode(NodeType.RETURN_STATEMENT, start, end, null, null);
	}

	/**
	 * Creates a THROW_STATEMENT node.
	 *
	 * @param start the start position in source code (inclusive)
	 * @param end   the end position in source code (exclusive)
	 * @return a new semantic node
	 */
	public static SemanticNode throwStatement(int start, int end)
	{
		return new SemanticNode(NodeType.THROW_STATEMENT, start, end, null, null);
	}

	/**
	 * Creates a YIELD_STATEMENT node.
	 *
	 * @param start the start position in source code (inclusive)
	 * @param end   the end position in source code (exclusive)
	 * @return a new semantic node
	 */
	public static SemanticNode yieldStatement(int start, int end)
	{
		return new SemanticNode(NodeType.YIELD_STATEMENT, start, end, null, null);
	}

	/**
	 * Creates a TRY_STATEMENT node.
	 *
	 * @param start the start position in source code (inclusive)
	 * @param end   the end position in source code (exclusive)
	 * @return a new semantic node
	 */
	public static SemanticNode tryStatement(int start, int end)
	{
		return new SemanticNode(NodeType.TRY_STATEMENT, start, end, null, null);
	}

	/**
	 * Creates a CATCH_CLAUSE node.
	 *
	 * @param start the start position in source code (inclusive)
	 * @param end   the end position in source code (exclusive)
	 * @return a new semantic node
	 */
	public static SemanticNode catchClause(int start, int end)
	{
		return new SemanticNode(NodeType.CATCH_CLAUSE, start, end, null, null);
	}

	/**
	 * Creates a FINALLY_CLAUSE node.
	 *
	 * @param start the start position in source code (inclusive)
	 * @param end   the end position in source code (exclusive)
	 * @return a new semantic node
	 */
	public static SemanticNode finallyClause(int start, int end)
	{
		return new SemanticNode(NodeType.FINALLY_CLAUSE, start, end, null, null);
	}

	/**
	 * Creates a SYNCHRONIZED_STATEMENT node.
	 *
	 * @param start the start position in source code (inclusive)
	 * @param end   the end position in source code (exclusive)
	 * @return a new semantic node
	 */
	public static SemanticNode synchronizedStatement(int start, int end)
	{
		return new SemanticNode(NodeType.SYNCHRONIZED_STATEMENT, start, end, null, null);
	}

	/**
	 * Creates a BREAK_STATEMENT node.
	 *
	 * @param start the start position in source code (inclusive)
	 * @param end   the end position in source code (exclusive)
	 * @return a new semantic node
	 */
	public static SemanticNode breakStatement(int start, int end)
	{
		return new SemanticNode(NodeType.BREAK_STATEMENT, start, end, null, null);
	}

	/**
	 * Creates a CONTINUE_STATEMENT node.
	 *
	 * @param start the start position in source code (inclusive)
	 * @param end   the end position in source code (exclusive)
	 * @return a new semantic node
	 */
	public static SemanticNode continueStatement(int start, int end)
	{
		return new SemanticNode(NodeType.CONTINUE_STATEMENT, start, end, null, null);
	}

	/**
	 * Creates an ASSERT_STATEMENT node.
	 *
	 * @param start the start position in source code (inclusive)
	 * @param end   the end position in source code (exclusive)
	 * @return a new semantic node
	 */
	public static SemanticNode assertStatement(int start, int end)
	{
		return new SemanticNode(NodeType.ASSERT_STATEMENT, start, end, null, null);
	}

	/**
	 * Creates an EMPTY_STATEMENT node.
	 *
	 * @param start the start position in source code (inclusive)
	 * @param end   the end position in source code (exclusive)
	 * @return a new semantic node
	 */
	public static SemanticNode emptyStatement(int start, int end)
	{
		return new SemanticNode(NodeType.EMPTY_STATEMENT, start, end, null, null);
	}

	/**
	 * Creates a LABELED_STATEMENT node.
	 *
	 * @param start the start position in source code (inclusive)
	 * @param end   the end position in source code (exclusive)
	 * @return a new semantic node
	 */
	public static SemanticNode labeledStatement(int start, int end)
	{
		return new SemanticNode(NodeType.LABELED_STATEMENT, start, end, null, null);
	}

	/**
	 * Creates a COMPILATION_UNIT node.
	 *
	 * @param start the start position in source code (inclusive)
	 * @param end   the end position in source code (exclusive)
	 * @return a new semantic node
	 */
	public static SemanticNode compilationUnit(int start, int end)
	{
		return new SemanticNode(NodeType.COMPILATION_UNIT, start, end, null, null);
	}

	/**
	 * Creates a METHOD_DECLARATION node.
	 *
	 * @param start the start position in source code (inclusive)
	 * @param end   the end position in source code (exclusive)
	 * @return a new semantic node
	 */
	public static SemanticNode methodDeclaration(int start, int end)
	{
		return new SemanticNode(NodeType.METHOD_DECLARATION, start, end, null, null);
	}

	/**
	 * Creates a CONSTRUCTOR_DECLARATION node.
	 *
	 * @param start the start position in source code (inclusive)
	 * @param end   the end position in source code (exclusive)
	 * @return a new semantic node
	 */
	public static SemanticNode constructorDeclaration(int start, int end)
	{
		return new SemanticNode(NodeType.CONSTRUCTOR_DECLARATION, start, end, null, null);
	}

	/**
	 * Creates a FIELD_DECLARATION node.
	 *
	 * @param start the start position in source code (inclusive)
	 * @param end   the end position in source code (exclusive)
	 * @return a new semantic node
	 */
	public static SemanticNode fieldDeclaration(int start, int end)
	{
		return new SemanticNode(NodeType.FIELD_DECLARATION, start, end, null, null);
	}

	/**
	 * Creates an ENUM_CONSTANT node.
	 *
	 * @param start the start position in source code (inclusive)
	 * @param end   the end position in source code (exclusive)
	 * @return a new semantic node
	 */
	public static SemanticNode enumConstant(int start, int end)
	{
		return new SemanticNode(NodeType.ENUM_CONSTANT, start, end, null, null);
	}

	/**
	 * Creates a TYPE_REFERENCE node.
	 *
	 * @param start the start position in source code (inclusive)
	 * @param end   the end position in source code (exclusive)
	 * @return a new semantic node
	 */
	public static SemanticNode typeReference(int start, int end)
	{
		return new SemanticNode(NodeType.TYPE_REFERENCE, start, end, null, null);
	}

	/**
	 * Creates an ARRAY_TYPE node.
	 *
	 * @param start the start position in source code (inclusive)
	 * @param end   the end position in source code (exclusive)
	 * @return a new semantic node
	 */
	public static SemanticNode arrayType(int start, int end)
	{
		return new SemanticNode(NodeType.ARRAY_TYPE, start, end, null, null);
	}

	/**
	 * Creates a PARAMETERIZED_TYPE node.
	 *
	 * @param start the start position in source code (inclusive)
	 * @param end   the end position in source code (exclusive)
	 * @return a new semantic node
	 */
	public static SemanticNode parameterizedType(int start, int end)
	{
		return new SemanticNode(NodeType.PARAMETERIZED_TYPE, start, end, null, null);
	}

	/**
	 * Creates a TYPE_PARAMETER node.
	 *
	 * @param start the start position in source code (inclusive)
	 * @param end   the end position in source code (exclusive)
	 * @return a new semantic node
	 */
	public static SemanticNode typeParameter(int start, int end)
	{
		return new SemanticNode(NodeType.TYPE_PARAMETER, start, end, null, null);
	}

	/**
	 * Creates a WILDCARD_TYPE node.
	 *
	 * @param start the start position in source code (inclusive)
	 * @param end   the end position in source code (exclusive)
	 * @return a new semantic node
	 */
	public static SemanticNode wildcardType(int start, int end)
	{
		return new SemanticNode(NodeType.WILDCARD_TYPE, start, end, null, null);
	}

	/**
	 * Creates a UNION_TYPE node.
	 *
	 * @param start the start position in source code (inclusive)
	 * @param end   the end position in source code (exclusive)
	 * @return a new semantic node
	 */
	public static SemanticNode unionType(int start, int end)
	{
		return new SemanticNode(NodeType.UNION_TYPE, start, end, null, null);
	}

	/**
	 * Creates a PRIMITIVE_TYPE node.
	 *
	 * @param start the start position in source code (inclusive)
	 * @param end   the end position in source code (exclusive)
	 * @return a new semantic node
	 */
	public static SemanticNode primitiveType(int start, int end)
	{
		return new SemanticNode(NodeType.PRIMITIVE_TYPE, start, end, null, null);
	}

	/**
	 * Creates an ANNOTATION node.
	 *
	 * @param start the start position in source code (inclusive)
	 * @param end   the end position in source code (exclusive)
	 * @return a new semantic node
	 */
	public static SemanticNode annotation(int start, int end)
	{
		return new SemanticNode(NodeType.ANNOTATION, start, end, null, null);
	}

	/**
	 * Creates an ANNOTATION_ELEMENT node.
	 *
	 * @param start the start position in source code (inclusive)
	 * @param end   the end position in source code (exclusive)
	 * @return a new semantic node
	 */
	public static SemanticNode annotationElement(int start, int end)
	{
		return new SemanticNode(NodeType.ANNOTATION_ELEMENT, start, end, null, null);
	}

	/**
	 * Creates a MODIFIERS node.
	 *
	 * @param start the start position in source code (inclusive)
	 * @param end   the end position in source code (exclusive)
	 * @return a new semantic node
	 */
	public static SemanticNode modifiers(int start, int end)
	{
		return new SemanticNode(NodeType.MODIFIERS, start, end, null, null);
	}

	/**
	 * Creates a RECORD_PATTERN node.
	 *
	 * @param start the start position in source code (inclusive)
	 * @param end   the end position in source code (exclusive)
	 * @return a new semantic node
	 */
	public static SemanticNode recordPattern(int start, int end)
	{
		return new SemanticNode(NodeType.RECORD_PATTERN, start, end, null, null);
	}

	// ========== Parsing utilities ==========

	/**
	 * Parses source code and returns a set of all AST nodes with their positions.
	 * <p>
	 * The returned set contains every node created during parsing, with its type, source positions,
	 * and optional attribute value. This enables semantic comparison of ASTs independent of
	 * arena allocation order.
	 * <p>
	 * The tree structure is implicit in the positions: containment relationships can be derived
	 * from comparing position ranges.
	 *
	 * @param source the Java source code to parse
	 * @return set of semantic AST nodes
	 * @throws AssertionError if parsing fails
	 */
	public static Set<SemanticNode> parseSemanticAst(String source)
	{
		requireThat(source, "source").isNotNull();

		try (Parser parser = new Parser(source))
		{
			switch (parser.parse())
			{
				case ParseResult.Success _ ->
				{
					NodeArena arena = parser.getArena();
					int nodeCount = arena.getNodeCount();
					Set<SemanticNode> nodes = new HashSet<>(nodeCount);

					for (int i = 0; i < nodeCount; ++i)
					{
						NodeIndex index = new NodeIndex(i);
						NodeType type = arena.getType(index);
						int start = arena.getStart(index);
						int end = arena.getEnd(index);
						SemanticNode semanticNode = createSemanticNode(arena, index, type, start, end);
						nodes.add(semanticNode);
					}
					return nodes;
				}
				case ParseResult.Failure failure ->
					throw new AssertionError("Parsing failed: " + failure);
			}
		}
	}

	/**
	 * Creates a semantic node from arena data.
	 *
	 * @param arena the node arena
	 * @param index the node index
	 * @param type  the node type
	 * @param start the start position
	 * @param end   the end position
	 * @return a new semantic node
	 */
	private static SemanticNode createSemanticNode(NodeArena arena, NodeIndex index, NodeType type,
		int start, int end)
	{
		return switch (type)
		{
			// Types requiring attributes
			case IMPORT_DECLARATION ->
			{
				ImportAttribute attr = arena.getImportAttribute(index);
				yield importNode(start, end, attr.qualifiedName(), attr.isStatic());
			}
			case MODULE_IMPORT_DECLARATION ->
				moduleImportNode(start, end, arena.getModuleImportAttribute(index).moduleName());
			case PACKAGE_DECLARATION ->
				packageNode(start, end, arena.getPackageAttribute(index).packageName());
			case CLASS_DECLARATION, INTERFACE_DECLARATION, ENUM_DECLARATION,
				RECORD_DECLARATION, ANNOTATION_DECLARATION ->
				typeDeclaration(type, start, end, arena.getTypeDeclarationAttribute(index).typeName());
			case PARAMETER_DECLARATION ->
				parameterNode(start, end, arena.getParameterAttribute(index).name());

			// Comments
			case LINE_COMMENT -> lineComment(start, end);
			case MARKDOWN_DOC_COMMENT -> markdownDocComment(start, end);
			case BLOCK_COMMENT -> blockComment(start, end);
			case JAVADOC_COMMENT -> javadocComment(start, end);

			// Literals
			case INTEGER_LITERAL -> integerLiteral(start, end);
			case LONG_LITERAL -> longLiteral(start, end);
			case FLOAT_LITERAL -> floatLiteral(start, end);
			case DOUBLE_LITERAL -> doubleLiteral(start, end);
			case BOOLEAN_LITERAL -> booleanLiteral(start, end);
			case CHAR_LITERAL -> charLiteral(start, end);
			case STRING_LITERAL -> stringLiteral(start, end);
			case NULL_LITERAL -> nullLiteral(start, end);

			// Names
			case IDENTIFIER -> identifier(start, end);
			case QUALIFIED_NAME -> qualifiedName(start, end);

			// Expressions
			case BINARY_EXPRESSION -> binaryExpression(start, end);
			case UNARY_EXPRESSION -> unaryExpression(start, end);
			case POSTFIX_EXPRESSION -> postfixExpression(start, end);
			case ASSIGNMENT_EXPRESSION -> assignmentExpression(start, end);
			case CONDITIONAL_EXPRESSION -> conditionalExpression(start, end);
			case INSTANCEOF_EXPRESSION -> instanceofExpression(start, end);
			case LAMBDA_EXPRESSION -> lambdaExpression(start, end);
			case METHOD_REFERENCE -> methodReference(start, end);
			case CAST_EXPRESSION -> castExpression(start, end);
			case ARRAY_ACCESS -> arrayAccess(start, end);
			case FIELD_ACCESS -> fieldAccess(start, end);
			case METHOD_INVOCATION -> methodInvocation(start, end);
			case PARENTHESIZED_EXPRESSION -> parenthesizedExpression(start, end);
			case ARRAY_CREATION -> arrayCreation(start, end);
			case OBJECT_CREATION -> objectCreation(start, end);
			case THIS_EXPRESSION -> thisExpression(start, end);
			case SUPER_EXPRESSION -> superExpression(start, end);
			case ARRAY_INITIALIZER -> arrayInitializer(start, end);
			case CLASS_LITERAL -> classLiteral(start, end);

			// Statements
			case BLOCK -> block(start, end);
			case EXPRESSION_STATEMENT -> expressionStatement(start, end);
			case VARIABLE_DECLARATION -> variableDeclaration(start, end);
			case IF_STATEMENT -> ifStatement(start, end);
			case FOR_STATEMENT -> forStatement(start, end);
			case ENHANCED_FOR_STATEMENT -> enhancedForStatement(start, end);
			case WHILE_STATEMENT -> whileStatement(start, end);
			case DO_WHILE_STATEMENT -> doWhileStatement(start, end);
			case SWITCH_STATEMENT -> switchStatement(start, end);
			case SWITCH_EXPRESSION -> switchExpression(start, end);
			case SWITCH_CASE -> switchCase(start, end);
			case RETURN_STATEMENT -> returnStatement(start, end);
			case THROW_STATEMENT -> throwStatement(start, end);
			case YIELD_STATEMENT -> yieldStatement(start, end);
			case TRY_STATEMENT -> tryStatement(start, end);
			case CATCH_CLAUSE -> catchClause(start, end);
			case FINALLY_CLAUSE -> finallyClause(start, end);
			case SYNCHRONIZED_STATEMENT -> synchronizedStatement(start, end);
			case BREAK_STATEMENT -> breakStatement(start, end);
			case CONTINUE_STATEMENT -> continueStatement(start, end);
			case ASSERT_STATEMENT -> assertStatement(start, end);
			case EMPTY_STATEMENT -> emptyStatement(start, end);
			case LABELED_STATEMENT -> labeledStatement(start, end);

			// Compilation unit
			case COMPILATION_UNIT -> compilationUnit(start, end);

			// Declarations (without attributes)
			case METHOD_DECLARATION -> methodDeclaration(start, end);
			case CONSTRUCTOR_DECLARATION -> constructorDeclaration(start, end);
			case FIELD_DECLARATION -> fieldDeclaration(start, end);
			case ENUM_CONSTANT -> enumConstant(start, end);

			// Types
			case TYPE_REFERENCE -> typeReference(start, end);
			case ARRAY_TYPE -> arrayType(start, end);
			case PARAMETERIZED_TYPE -> parameterizedType(start, end);
			case TYPE_PARAMETER -> typeParameter(start, end);
			case WILDCARD_TYPE -> wildcardType(start, end);
			case UNION_TYPE -> unionType(start, end);
			case PRIMITIVE_TYPE -> primitiveType(start, end);

			// Annotations
			case ANNOTATION -> annotation(start, end);
			case ANNOTATION_ELEMENT -> annotationElement(start, end);

			// Other
			case MODIFIERS -> modifiers(start, end);
			case RECORD_PATTERN -> recordPattern(start, end);
		};
	}

	/**
	 * Asserts that the given source code fails to parse.
	 * Used to verify that malformed syntax is correctly rejected by the parser.
	 *
	 * @param source the source code to parse
	 * @throws AssertionError if parsing succeeds when it should have failed
	 */
	public static void assertParseFails(String source)
	{
		try (Parser parser = new Parser(source))
		{
			switch (parser.parse())
			{
				case ParseResult.Success success ->
					throw new AssertionError("Expected Failure but got: " + success);
				case ParseResult.Failure _ ->
				{
					// Expected - parsing should fail for malformed input
				}
			}
		}
	}
}
