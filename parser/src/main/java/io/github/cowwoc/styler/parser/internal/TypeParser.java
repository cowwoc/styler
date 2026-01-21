package io.github.cowwoc.styler.parser.internal;

import io.github.cowwoc.styler.ast.core.NodeIndex;
import io.github.cowwoc.styler.ast.core.NodeType;
import io.github.cowwoc.styler.ast.core.ParameterAttribute;
import io.github.cowwoc.styler.ast.core.TypeDeclarationAttribute;
import io.github.cowwoc.styler.parser.Parser.ParserException;
import io.github.cowwoc.styler.parser.Token;
import io.github.cowwoc.styler.parser.TokenType;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.that;

/**
 * Helper class for parsing type declarations (class, interface, enum, record, annotation).
 * <p>
 * Extracted from Parser to reduce class size while maintaining cohesive parsing logic.
 */
public final class TypeParser
{
	private final ParserAccess parser;

	/**
	 * Creates a new type parser that delegates to the given parser.
	 *
	 * @param parser the parent parser providing token access and helper methods
	 */
	public TypeParser(ParserAccess parser)
	{
		assert that(parser, "parser").isNotNull().elseThrow();
		this.parser = parser;
	}

	/**
	 * Parses a type declaration (class, interface, enum, record, or annotation type).
	 * <p>
	 * Handles modifiers (public, private, protected, static, final, abstract, sealed, non-sealed, strictfp),
	 * annotations, and delegates to the appropriate declaration parser.
	 */
	public void parseTypeDeclaration()
	{
		// Annotations and modifiers (including sealed/non-sealed)
		while (isModifier(this.parser.currentToken().type()) ||
			this.parser.currentToken().type() == TokenType.SEALED ||
			this.parser.currentToken().type() == TokenType.NON_SEALED ||
			this.parser.currentToken().type() == TokenType.AT_SIGN)
		{
			if (this.parser.currentToken().type() == TokenType.AT_SIGN)
			{
				// Check if this is @interface (annotation type declaration) or regular annotation
				int checkpoint = this.parser.getPosition();
				this.parser.consume();
				if (this.parser.currentToken().type() == TokenType.INTERFACE)
				{
					// This is @interface, backtrack and let the normal flow handle it
					this.parser.setPosition(checkpoint);
					break;
				}
				// Regular annotation - parse it and continue
				this.parser.setPosition(checkpoint);
				this.parser.parseAnnotation();
			}
			else
				this.parser.consume();
		}

		if (this.parser.match(TokenType.CLASS))
			parseClassDeclaration();
		else if (this.parser.match(TokenType.INTERFACE))
			parseInterfaceDeclaration();
		else if (this.parser.match(TokenType.ENUM))
			parseEnumDeclaration();
		else if (this.parser.match(TokenType.RECORD))
			parseRecordDeclaration();
		else if (this.parser.match(TokenType.AT_SIGN))
		{
			this.parser.expect(TokenType.INTERFACE);
			parseAnnotationDeclaration();
		}
	}

	/**
	 * Parses an implicit class declaration (JEP 512).
	 * <p>
	 * An implicit class contains top-level members (fields and methods) without an explicit class declaration.
	 * This is used for simple "Hello World" style programs.
	 *
	 * @return a {@link NodeIndex} pointing to the allocated {@link NodeType#IMPLICIT_CLASS_DECLARATION} node
	 */
	public NodeIndex parseImplicitClassDeclaration()
	{
		int implicitStart = this.parser.currentToken().start();

		while (this.parser.currentToken().type() != TokenType.END_OF_FILE)
		{
			this.parser.parseComments();
			if (this.parser.currentToken().type() == TokenType.END_OF_FILE)
				break;
			parseMemberDeclarationInternal();
		}

		int implicitEnd = this.parser.previousToken().end();
		return this.parser.getArena().allocateImplicitClassDeclaration(implicitStart, implicitEnd);
	}

	/**
	 * Parses type parameters in angle brackets ({@code <T, U extends Comparable>}).
	 * <p>
	 * Expects the opening {@code <} to have already been consumed.
	 */
	public void parseTypeParameters()
	{
		// Handle comments before first type parameter
		this.parser.parseComments();
		parseTypeParameter();
		while (this.parser.match(TokenType.COMMA))
		{
			// Handle comments between type parameters
			this.parser.parseComments();
			parseTypeParameter();
		}
		this.parser.expectGTInGeneric();
	}

	/**
	 * Parses a single member declaration within a class body.
	 * <p>
	 * Used for parsing anonymous class members and inner class members.
	 */
	public void parseMemberDeclaration()
	{
		parseMemberDeclarationInternal();
	}

	/**
	 * Parses type arguments in angle brackets ({@code <String, Integer>}).
	 * <p>
	 * Expects the opening {@code <} to have already been consumed.
	 */
	public void parseTypeArguments()
	{
		// Handle diamond operator: <> with no type arguments
		if (this.parser.currentToken().type() == TokenType.GREATER_THAN)
		{
			this.parser.expectGTInGeneric();
			return;
		}
		// Handle comments before first type argument
		this.parser.parseComments();
		// Parse type arguments and allocate nodes (return value is the allocated NodeIndex)
		parseTypeArgument();
		while (this.parser.match(TokenType.COMMA))
		{
			// Handle comments between type arguments
			this.parser.parseComments();
			parseTypeArgument();
		}
		this.parser.expectGTInGeneric();
	}

	/**
	 * Checks if the given token type is a modifier keyword.
	 *
	 * @param type the token type to check
	 * @return {@code true} if the token is a modifier
	 */
	public boolean isModifier(TokenType type)
	{
		return switch (type)
		{
			case PUBLIC, PRIVATE, PROTECTED, STATIC, FINAL, ABSTRACT, STRICTFP, SYNCHRONIZED, NATIVE,
				TRANSIENT, VOLATILE, DEFAULT -> true;
			default -> false;
		};
	}

	private NodeIndex parseClassDeclaration()
	{
		// CLASS keyword already consumed, capture its position
		int start = this.parser.previousToken().start();

		// Capture type name and position before consuming
		Token nameToken = this.parser.currentToken();
		this.parser.expect(TokenType.IDENTIFIER);
		String typeName = nameToken.decodedText();

		// Type parameters
		if (this.parser.match(TokenType.LESS_THAN))
			parseTypeParameters();

		// Extends clause
		if (this.parser.match(TokenType.EXTENDS))
			this.parser.parseType();

		// Skip comments between extends and implements
		while (this.parser.currentToken().type() == TokenType.LINE_COMMENT ||
			this.parser.currentToken().type() == TokenType.BLOCK_COMMENT)
			this.parser.consume();

		// Implements clause
		if (this.parser.match(TokenType.IMPLEMENTS))
		{
			this.parser.parseType();
			while (this.parser.match(TokenType.COMMA))
				this.parser.parseType();
		}

		// Permits clause (for sealed classes)
		if (this.parser.match(TokenType.PERMITS))
		{
			this.parser.parseType();
			while (this.parser.match(TokenType.COMMA))
				this.parser.parseType();
		}

		// Class body
		parseClassBody();

		int end = this.parser.previousToken().end();
		TypeDeclarationAttribute attribute = new TypeDeclarationAttribute(typeName);
		return this.parser.getArena().allocateClassDeclaration(start, end, attribute);
	}

	private NodeIndex parseInterfaceDeclaration()
	{
		// INTERFACE keyword already consumed, capture its position
		int start = this.parser.previousToken().start();

		// Capture type name and position before consuming
		Token nameToken = this.parser.currentToken();
		this.parser.expect(TokenType.IDENTIFIER);
		String typeName = nameToken.decodedText();

		if (this.parser.match(TokenType.LESS_THAN))
			parseTypeParameters();

		if (this.parser.match(TokenType.EXTENDS))
		{
			this.parser.parseType();
			while (this.parser.match(TokenType.COMMA))
				this.parser.parseType();
		}

		// Permits clause (for sealed interfaces)
		if (this.parser.match(TokenType.PERMITS))
		{
			this.parser.parseType();
			while (this.parser.match(TokenType.COMMA))
				this.parser.parseType();
		}

		parseClassBody();

		int end = this.parser.previousToken().end();
		TypeDeclarationAttribute attribute = new TypeDeclarationAttribute(typeName);
		return this.parser.getArena().allocateInterfaceDeclaration(start, end, attribute);
	}

	private NodeIndex parseEnumDeclaration()
	{
		// ENUM keyword already consumed, capture its position
		int start = this.parser.previousToken().start();

		// Capture type name and position before consuming
		Token nameToken = this.parser.currentToken();
		this.parser.expect(TokenType.IDENTIFIER);
		String typeName = nameToken.decodedText();

		if (this.parser.match(TokenType.IMPLEMENTS))
		{
			this.parser.parseType();
			while (this.parser.match(TokenType.COMMA))
				this.parser.parseType();
		}

		this.parser.expect(TokenType.LEFT_BRACE);
		parseEnumBody();
		this.parser.expect(TokenType.RIGHT_BRACE);

		int end = this.parser.previousToken().end();
		TypeDeclarationAttribute attribute = new TypeDeclarationAttribute(typeName);
		return this.parser.getArena().allocateEnumDeclaration(start, end, attribute);
	}

	/**
	 * Parses an annotation type declaration ({@code @interface Name { }}).
	 * <p>
	 * Expects the caller to have already consumed the {@code @} and {@code interface} tokens.
	 *
	 * @return a {@link NodeIndex} pointing to the allocated {@link NodeType#ANNOTATION_DECLARATION} node
	 */
	private NodeIndex parseAnnotationDeclaration()
	{
		// position - 2 points to '@' because caller consumed both '@' and 'interface' tokens
		int start = this.parser.getTokens().get(this.parser.getPosition() - 2).start();

		// Capture type name and position before consuming
		Token nameToken = this.parser.currentToken();
		this.parser.expect(TokenType.IDENTIFIER);
		String typeName = nameToken.decodedText();

		parseClassBody();
		int end = this.parser.previousToken().end();
		TypeDeclarationAttribute attribute = new TypeDeclarationAttribute(typeName);
		return this.parser.getArena().allocateAnnotationTypeDeclaration(start, end, attribute);
	}

	/**
	 * Parses a record declaration ({@code record Name(components) { }}).
	 * <p>
	 * Expects the caller to have already consumed the {@code record} keyword.
	 *
	 * @return a {@link NodeIndex} pointing to the allocated {@link NodeType#RECORD_DECLARATION} node
	 */
	private NodeIndex parseRecordDeclaration()
	{
		// position - 1 points to 'record' because caller consumed that keyword
		int start = this.parser.previousToken().start();

		// Capture type name and position before consuming
		Token nameToken = this.parser.currentToken();
		this.parser.expect(TokenType.IDENTIFIER);
		String typeName = nameToken.decodedText();

		// Type parameters (optional)
		if (this.parser.match(TokenType.LESS_THAN))
			parseTypeParameters();

		// Record components (mandatory)
		this.parser.expect(TokenType.LEFT_PARENTHESIS);
		if (this.parser.currentToken().type() != TokenType.RIGHT_PARENTHESIS)
		{
			// Handle comments before first component
			this.parser.parseComments();
			parseParameter();
			while (this.parser.match(TokenType.COMMA))
			{
				// Handle comments between components
				this.parser.parseComments();
				parseParameter();
			}
			// Handle comments before closing parenthesis
			this.parser.parseComments();
		}
		this.parser.expect(TokenType.RIGHT_PARENTHESIS);

		// Implements clause (optional)
		if (this.parser.match(TokenType.IMPLEMENTS))
		{
			this.parser.parseType();
			while (this.parser.match(TokenType.COMMA))
				this.parser.parseType();
		}

		// Record body (optional - can be empty)
		parseClassBody();

		int end = this.parser.previousToken().end();
		TypeDeclarationAttribute attribute = new TypeDeclarationAttribute(typeName);
		return this.parser.getArena().allocateRecordDeclaration(start, end, attribute);
	}

	private void parseTypeParameter()
	{
		// Parse annotations before type parameter name (JSR 308: @Nullable T)
		while (this.parser.currentToken().type() == TokenType.AT_SIGN)
			this.parser.parseAnnotation();
		this.parser.expect(TokenType.IDENTIFIER);
		if (this.parser.match(TokenType.EXTENDS))
		{
			this.parser.parseType();
			while (this.parser.match(TokenType.BITWISE_AND))
				this.parser.parseType();
		}
	}

	private NodeIndex parseTypeArgument()
	{
		if (this.parser.match(TokenType.QUESTION_MARK))
		{
			Token wildcardToken = this.parser.previousToken();
			int start = wildcardToken.start();

			if (this.parser.match(TokenType.EXTENDS) || this.parser.match(TokenType.SUPER))
			{
				this.parser.parseType();
				return this.parser.getArena().allocateNode(NodeType.WILDCARD_TYPE, start,
					this.parser.previousToken().end());
			}
			// Unbounded wildcard: reuse wildcardToken for end position
			return this.parser.getArena().allocateNode(NodeType.WILDCARD_TYPE, start, wildcardToken.end());
		}

		int start = this.parser.currentToken().start();
		this.parser.parseType();
		return this.parser.getArena().allocateNode(NodeType.QUALIFIED_NAME, start,
			this.parser.previousToken().end());
	}

	private void parseClassBody()
	{
		// Skip any comments before opening brace
		this.parser.parseComments();
		this.parser.expect(TokenType.LEFT_BRACE);
		while (!this.parser.match(TokenType.RIGHT_BRACE))
		{
			this.parser.parseComments();
			if (this.parser.currentToken().type() == TokenType.RIGHT_BRACE)
				// Let match() in while condition consume the RIGHT_BRACE
				continue;
			if (this.parser.currentToken().type() == TokenType.END_OF_FILE)
				throw new ParserException("Unexpected END_OF_FILE in class body",
					this.parser.currentToken().start());
			parseMemberDeclarationInternal();
		}
	}

	private void parseEnumBody()
	{
		// Handle comments before the first constant (or before SEMICOLON/RIGHT_BRACE if no constants)
		this.parser.parseComments();
		if (this.parser.currentToken().type() != TokenType.SEMICOLON &&
			this.parser.currentToken().type() != TokenType.RIGHT_BRACE)
		{
			parseEnumConstant();
			while (this.parser.match(TokenType.COMMA))
			{
				// Handle comments after comma (e.g., trailing comma with comment before semicolon)
				this.parser.parseComments();
				if (this.parser.currentToken().type() == TokenType.SEMICOLON ||
					this.parser.currentToken().type() == TokenType.RIGHT_BRACE)
					break;
				parseEnumConstant();
			}
		}
		// Handle comments after the last constant (before semicolon or rbrace)
		this.parser.parseComments();

		if (this.parser.match(TokenType.SEMICOLON))
			while (this.parser.currentToken().type() != TokenType.RIGHT_BRACE)
				parseMemberDeclarationInternal();
	}

	private void parseEnumConstant()
	{
		this.parser.parseComments();
		int start = this.parser.currentToken().start();
		// Parse annotations before the constant identifier
		while (this.parser.currentToken().type() == TokenType.AT_SIGN)
		{
			this.parser.parseAnnotation();
			this.parser.parseComments();
		}
		this.parser.expect(TokenType.IDENTIFIER);
		if (this.parser.match(TokenType.LEFT_PARENTHESIS) && !this.parser.match(TokenType.RIGHT_PARENTHESIS))
		{
			this.parser.parseExpression();
			while (this.parser.match(TokenType.COMMA))
				this.parser.parseExpression();
			this.parser.expect(TokenType.RIGHT_PARENTHESIS);
		}
		if (this.parser.match(TokenType.LEFT_BRACE))
		{
			while (!this.parser.match(TokenType.RIGHT_BRACE))
			{
				this.parser.parseComments();
				if (this.parser.currentToken().type() == TokenType.RIGHT_BRACE)
					// Let match() in while condition consume the RIGHT_BRACE
					continue;
				if (this.parser.currentToken().type() == TokenType.END_OF_FILE)
				{
					throw new ParserException("Unexpected END_OF_FILE in enum constant body",
						this.parser.currentToken().start());
				}
				parseMemberDeclarationInternal();
			}
		}
		int end = this.parser.previousToken().end();
		this.parser.getArena().allocateNode(NodeType.ENUM_CONSTANT, start, end);
	}

	private void parseMemberDeclarationInternal()
	{
		this.parser.parseComments();
		int start = this.parser.currentToken().start();
		skipMemberModifiers();

		if (parseNestedTypeDeclaration())
			return;

		// Type parameters (for methods)
		if (this.parser.match(TokenType.LESS_THAN))
			parseTypeParameters();

		parseMemberBody(start);
	}

	/**
	 * Skips member modifiers and annotations until reaching a type keyword or member start.
	 */
	public void skipMemberModifiers()
	{
		while (isModifier(this.parser.currentToken().type()) ||
			this.parser.currentToken().type() == TokenType.AT_SIGN ||
			this.parser.currentToken().type() == TokenType.SEALED ||
			this.parser.currentToken().type() == TokenType.NON_SEALED)
		{
			if (this.parser.currentToken().type() == TokenType.AT_SIGN)
			{
				// Check if this is @interface (annotation type declaration) or regular annotation
				int checkpoint = this.parser.getPosition();
				this.parser.consume();
				if (this.parser.currentToken().type() == TokenType.INTERFACE)
				{
					// This is @interface, backtrack and let parseNestedTypeDeclaration handle it
					this.parser.setPosition(checkpoint);
					break;
				}
				// Regular annotation - backtrack and parse it
				this.parser.setPosition(checkpoint);
				this.parser.parseAnnotation();
			}
			else
				this.parser.consume();
			// Handle comments between modifiers/annotations
			this.parser.parseComments();
		}
	}

	/**
	 * Parses a nested type declaration if the current token starts one.
	 *
	 * @return {@code true} if a type declaration was parsed
	 */
	public boolean parseNestedTypeDeclaration()
	{
		return switch (this.parser.currentToken().type())
		{
			case CLASS ->
			{
				this.parser.consume();
				parseClassDeclaration();
				yield true;
			}
			case INTERFACE ->
			{
				this.parser.consume();
				parseInterfaceDeclaration();
				yield true;
			}
			case ENUM ->
			{
				this.parser.consume();
				parseEnumDeclaration();
				yield true;
			}
			case RECORD ->
			{
				this.parser.consume();
				parseRecordDeclaration();
				yield true;
			}
			case AT_SIGN ->
			{
				this.parser.consume();
				this.parser.expect(TokenType.INTERFACE);
				parseAnnotationDeclaration();
				yield true;
			}
			default -> false;
		};
	}

	private void parseMemberBody(int start)
	{
		if (this.parser.isIdentifierOrContextualKeyword())
			parseIdentifierMember(start);
		else if (this.parser.isPrimitiveType(this.parser.currentToken().type()) ||
			this.parser.currentToken().type() == TokenType.VOID)
			parsePrimitiveTypedMember(start);
		else if (this.parser.currentToken().type() == TokenType.LEFT_BRACE)
			// Instance or static initializer (parseBlock expects the LEFT_BRACE)
			this.parser.parseBlock();
		else if (this.parser.match(TokenType.SEMICOLON))
		{
			// Empty declaration
		}
		else if (this.parser.currentToken().type() == TokenType.AT_SIGN)
			// Type-use annotation on return type: @Nullable String getValue()
			parseAnnotatedTypeMember(start);
		else
		{
			throw new ParserException("Unexpected token in member declaration: " +
				this.parser.currentToken().type(), start);
		}
	}

	private void parseIdentifierMember(int memberStart)
	{
		int checkpoint = this.parser.getPosition();
		this.parser.consume(); // Consume first identifier (could be type, constructor name, or field name)

		// Handle qualified type names: Outer.Inner, ValueLayout.OfInt, etc.
		while (this.parser.match(TokenType.DOT))
		{
			if (!this.parser.isIdentifierOrContextualKeyword())
				break;
			this.parser.consume();
		}

		if (this.parser.match(TokenType.LEFT_PARENTHESIS))
		{
			// Constructor (no return type, identifier is constructor name)
			parseMethodRest(memberStart, true);
			return;
		}

		if (this.parser.currentToken().type() == TokenType.LEFT_BRACE)
		{
			// Compact constructor (Java 16+): record component validation without parameter list
			// Example: public record Point(int x, int y) { public Point { validateInputs(); } }
			this.parser.parseBlock();
			return;
		}

		// Handle generic type arguments: List<String>, Map<K, V>, etc.
		this.parser.parseComments();
		if (this.parser.match(TokenType.LESS_THAN))
			parseTypeArguments();

		this.parser.parseArrayDimensionsWithAnnotations();
		this.parser.parseComments();

		if (this.parser.isIdentifierOrContextualKeyword())
		{
			// Method with non-primitive return type: ReturnType methodName(...)
			// First identifier was return type, now consume method name
			this.parser.consume();
			if (this.parser.match(TokenType.LEFT_PARENTHESIS))
			{
				parseMethodRest(memberStart, false);
				return;
			}

			// This is a field declaration: Type fieldName = ...
			parseFieldRest(memberStart);
			return;
		}

		// Field with identifier type (no name found, restore and try as expression)
		this.parser.setPosition(checkpoint);
		this.parser.consume(); // Re-consume type
		parseFieldRest(memberStart);
	}

	private void parsePrimitiveTypedMember(int memberStart)
	{
		this.parser.consume();
		this.parser.parseArrayDimensionsWithAnnotations();
		this.parser.expectIdentifierOrContextualKeyword();
		if (this.parser.match(TokenType.LEFT_PARENTHESIS))
			parseMethodRest(memberStart, false);
		else
			parseFieldRest(memberStart);
	}

	private void parseAnnotatedTypeMember(int memberStart)
	{
		// Parse type annotations (consumes @Nullable, @NonNull, etc.)
		while (this.parser.currentToken().type() == TokenType.AT_SIGN)
			this.parser.parseAnnotation();

		// After annotations, we have the actual type
		if (this.parser.isPrimitiveType(this.parser.currentToken().type()) ||
			this.parser.currentToken().type() == TokenType.VOID)
		{
			// Annotated primitive: @Positive int getValue()
			parsePrimitiveTypedMember(memberStart);
		}
		else if (this.parser.isIdentifierOrContextualKeyword())
		{
			// Annotated reference type: @Nullable String getValue()
			parseIdentifierMember(memberStart);
		}
		else
		{
			throw new ParserException("Expected type after type-use annotation but found " +
				this.parser.currentToken().type(), this.parser.currentToken().start());
		}
	}

	private NodeIndex parseMethodRest(int start, boolean isConstructor)
	{
		// Parameters already consumed opening paren
		if (!this.parser.match(TokenType.RIGHT_PARENTHESIS))
		{
			// Handle comments before first parameter
			this.parser.parseComments();
			parseParameter();
			while (this.parser.match(TokenType.COMMA))
			{
				// Handle comments between parameters
				this.parser.parseComments();
				parseParameter();
			}
			// Handle comments before closing parenthesis
			this.parser.parseComments();
			this.parser.expect(TokenType.RIGHT_PARENTHESIS);
		}

		// Throws clause
		if (this.parser.match(TokenType.THROWS))
		{
			this.parser.parseQualifiedName();
			while (this.parser.match(TokenType.COMMA))
				this.parser.parseQualifiedName();
		}

		// Annotation element default value (for @interface methods)
		if (this.parser.match(TokenType.DEFAULT))
			this.parser.parseExpression();

		// Method body or semicolon
		if (this.parser.match(TokenType.SEMICOLON))
		{
			// Abstract method
		}
		else
			this.parser.parseBlock();

		int end = this.parser.previousToken().end();
		NodeType nodeType;
		if (isConstructor)
			nodeType = NodeType.CONSTRUCTOR_DECLARATION;
		else
			nodeType = NodeType.METHOD_DECLARATION;
		return this.parser.getArena().allocateNode(nodeType, start, end);
	}

	private NodeIndex parseParameter()
	{
		int start = this.parser.currentToken().start();
		boolean isFinal = false;

		// Modifiers (annotations and final)
		while (this.parser.currentToken().type() == TokenType.FINAL ||
			this.parser.currentToken().type() == TokenType.AT_SIGN)
		{
			if (this.parser.currentToken().type() == TokenType.AT_SIGN)
				this.parser.parseAnnotation();
			else
			{
				this.parser.consume();
				isFinal = true;
			}
		}

		this.parser.parseType();
		boolean isVarargs = this.parser.match(TokenType.ELLIPSIS);

		// Check for receiver parameter (ClassName this)
		boolean isReceiver = this.parser.currentToken().type() == TokenType.THIS;
		String parameterName;
		if (isReceiver)
		{
			parameterName = "this";
			this.parser.consume();
		}
		else
		{
			Token nameToken = this.parser.currentToken();
			this.parser.expectIdentifierOrContextualKeyword();
			parameterName = nameToken.decodedText();
		}

		// Handle C-style array syntax: String args[]
		this.parser.parseArrayDimensionsWithAnnotations();

		int end = this.parser.previousToken().end();
		ParameterAttribute attribute = new ParameterAttribute(parameterName, isVarargs, isFinal, isReceiver);
		return this.parser.getArena().allocateParameterDeclaration(start, end, attribute);
	}

	private NodeIndex parseFieldRest(int start)
	{
		// Array dimensions or initializer
		this.parser.parseArrayDimensionsWithAnnotations();

		if (this.parser.match(TokenType.ASSIGN))
			this.parser.parseExpression();

		while (this.parser.match(TokenType.COMMA))
		{
			this.parser.expectIdentifierOrContextualKeyword();
			this.parser.parseArrayDimensionsWithAnnotations();
			if (this.parser.match(TokenType.ASSIGN))
				this.parser.parseExpression();
		}

		this.parser.expect(TokenType.SEMICOLON);

		int end = this.parser.previousToken().end();
		return this.parser.getArena().allocateNode(NodeType.FIELD_DECLARATION, start, end);
	}
}
