package io.github.cowwoc.styler.parser;

/**
 * Node type constants for the Index-Overlay parser architecture.
 *
 * This uses byte constants for memory efficiency - each node type fits in a single byte,
 * reducing memory overhead compared to enum objects or strings.
 *
 * Evidence: SWC and other high-performance parsers use integer/byte constants for node types
 * to minimize memory allocation and improve cache locality.
 */
public final class NodeType {

    // Compilation unit and top-level declarations
    public static final byte COMPILATION_UNIT = 1;
    public static final byte PACKAGE_DECLARATION = 2;
    public static final byte IMPORT_DECLARATION = 3;

    // Type declarations
    public static final byte CLASS_DECLARATION = 10;
    public static final byte INTERFACE_DECLARATION = 11;
    public static final byte ENUM_DECLARATION = 12;
    public static final byte ANNOTATION_DECLARATION = 13;
    public static final byte RECORD_DECLARATION = 14; // JDK 16+

    // Method and field declarations
    public static final byte METHOD_DECLARATION = 20;
    public static final byte CONSTRUCTOR_DECLARATION = 21;
    public static final byte FIELD_DECLARATION = 22;
    public static final byte PARAMETER_DECLARATION = 23;
    public static final byte LOCAL_VARIABLE_DECLARATION = 24;

    // Statements
    public static final byte BLOCK_STATEMENT = 30;
    public static final byte EXPRESSION_STATEMENT = 31;
    public static final byte IF_STATEMENT = 32;
    public static final byte WHILE_STATEMENT = 33;
    public static final byte FOR_STATEMENT = 34;
    public static final byte ENHANCED_FOR_STATEMENT = 35;
    public static final byte SWITCH_STATEMENT = 36;
    public static final byte TRY_STATEMENT = 37;
    public static final byte RETURN_STATEMENT = 38;
    public static final byte THROW_STATEMENT = 39;
    public static final byte BREAK_STATEMENT = 40;
    public static final byte CONTINUE_STATEMENT = 41;
    public static final byte SYNCHRONIZED_STATEMENT = 42;

    // JDK 17+ Switch expressions and patterns
    public static final byte SWITCH_EXPRESSION = 43;
    public static final byte YIELD_STATEMENT = 44;

    // Expressions
    public static final byte LITERAL_EXPRESSION = 50;
    public static final byte IDENTIFIER_EXPRESSION = 51;
    public static final byte METHOD_CALL_EXPRESSION = 52;
    public static final byte FIELD_ACCESS_EXPRESSION = 53;
    public static final byte ARRAY_ACCESS_EXPRESSION = 54;
    public static final byte ASSIGNMENT_EXPRESSION = 55;
    public static final byte BINARY_EXPRESSION = 56;
    public static final byte UNARY_EXPRESSION = 57;
    public static final byte CONDITIONAL_EXPRESSION = 58;
    public static final byte INSTANCEOF_EXPRESSION = 59;
    public static final byte CAST_EXPRESSION = 60;
    public static final byte LAMBDA_EXPRESSION = 61;
    public static final byte METHOD_REFERENCE_EXPRESSION = 62;
    public static final byte NEW_EXPRESSION = 63;
    public static final byte ARRAY_CREATION_EXPRESSION = 64;

    // JDK 21+ Pattern matching
    public static final byte PATTERN_EXPRESSION = 65;
    public static final byte TYPE_PATTERN = 66;
    public static final byte GUARD_PATTERN = 67;

    // JDK 25+ String templates
    public static final byte STRING_TEMPLATE_EXPRESSION = 68;
    public static final byte TEMPLATE_PROCESSOR_EXPRESSION = 69;

    // Types
    public static final byte PRIMITIVE_TYPE = 70;
    public static final byte CLASS_TYPE = 71;
    public static final byte ARRAY_TYPE = 72;
    public static final byte PARAMETERIZED_TYPE = 73;
    public static final byte WILDCARD_TYPE = 74;
    public static final byte UNION_TYPE = 75; // Multi-catch
    public static final byte INTERSECTION_TYPE = 76; // Type bounds
    public static final byte VAR_TYPE = 77; // JDK 10+ var

    // Modifiers and annotations
    public static final byte MODIFIER = 80;
    public static final byte ANNOTATION = 81;
    public static final byte ANNOTATION_ELEMENT = 82;

    // Comments and whitespace (preserved for formatting)
    public static final byte LINE_COMMENT = 90;
    public static final byte BLOCK_COMMENT = 91;
    public static final byte JAVADOC_COMMENT = 92;
    public static final byte WHITESPACE = 93;

    // Special nodes
    public static final byte ERROR_NODE = 100; // For error recovery
    public static final byte EOF_NODE = 101;

    // JDK 25 specific features
    public static final byte UNNAMED_CLASS = 110; // JDK 21 preview, 22+ permanent
    public static final byte UNNAMED_VARIABLE = 111; // JDK 22+
    public static final byte MODULE_IMPORT_DECLARATION = 112; // JDK 25 module imports (JEP 511)
    public static final byte FLEXIBLE_CONSTRUCTOR_BODY = 113; // JDK 25 flexible constructors (JEP 513)
    public static final byte PRIMITIVE_PATTERN = 114; // JDK 25 primitive patterns (JEP 507)
    public static final byte COMPACT_MAIN_METHOD = 115; // JDK 25 compact source files (JEP 512)
    public static final byte INSTANCE_MAIN_METHOD = 116; // JDK 25 instance main methods (JEP 512)

    /**
     * Returns a human-readable name for the given node type.
     * Useful for debugging and error messages.
     */
    public static String getTypeName(byte nodeType) {
        return switch (nodeType) {
            case COMPILATION_UNIT -> "CompilationUnit";
            case PACKAGE_DECLARATION -> "PackageDeclaration";
            case IMPORT_DECLARATION -> "ImportDeclaration";
            case CLASS_DECLARATION -> "ClassDeclaration";
            case INTERFACE_DECLARATION -> "InterfaceDeclaration";
            case ENUM_DECLARATION -> "EnumDeclaration";
            case ANNOTATION_DECLARATION -> "AnnotationDeclaration";
            case RECORD_DECLARATION -> "RecordDeclaration";
            case METHOD_DECLARATION -> "MethodDeclaration";
            case CONSTRUCTOR_DECLARATION -> "ConstructorDeclaration";
            case FIELD_DECLARATION -> "FieldDeclaration";
            case PARAMETER_DECLARATION -> "ParameterDeclaration";
            case LOCAL_VARIABLE_DECLARATION -> "LocalVariableDeclaration";
            case BLOCK_STATEMENT -> "BlockStatement";
            case EXPRESSION_STATEMENT -> "ExpressionStatement";
            case IF_STATEMENT -> "IfStatement";
            case WHILE_STATEMENT -> "WhileStatement";
            case FOR_STATEMENT -> "ForStatement";
            case ENHANCED_FOR_STATEMENT -> "EnhancedForStatement";
            case SWITCH_STATEMENT -> "SwitchStatement";
            case SWITCH_EXPRESSION -> "SwitchExpression";
            case TRY_STATEMENT -> "TryStatement";
            case RETURN_STATEMENT -> "ReturnStatement";
            case THROW_STATEMENT -> "ThrowStatement";
            case BREAK_STATEMENT -> "BreakStatement";
            case CONTINUE_STATEMENT -> "ContinueStatement";
            case SYNCHRONIZED_STATEMENT -> "SynchronizedStatement";
            case YIELD_STATEMENT -> "YieldStatement";
            case LITERAL_EXPRESSION -> "LiteralExpression";
            case IDENTIFIER_EXPRESSION -> "IdentifierExpression";
            case METHOD_CALL_EXPRESSION -> "MethodCallExpression";
            case FIELD_ACCESS_EXPRESSION -> "FieldAccessExpression";
            case ARRAY_ACCESS_EXPRESSION -> "ArrayAccessExpression";
            case ASSIGNMENT_EXPRESSION -> "AssignmentExpression";
            case BINARY_EXPRESSION -> "BinaryExpression";
            case UNARY_EXPRESSION -> "UnaryExpression";
            case CONDITIONAL_EXPRESSION -> "ConditionalExpression";
            case INSTANCEOF_EXPRESSION -> "InstanceofExpression";
            case CAST_EXPRESSION -> "CastExpression";
            case LAMBDA_EXPRESSION -> "LambdaExpression";
            case METHOD_REFERENCE_EXPRESSION -> "MethodReferenceExpression";
            case NEW_EXPRESSION -> "NewExpression";
            case ARRAY_CREATION_EXPRESSION -> "ArrayCreationExpression";
            case PATTERN_EXPRESSION -> "PatternExpression";
            case TYPE_PATTERN -> "TypePattern";
            case GUARD_PATTERN -> "GuardPattern";
            case STRING_TEMPLATE_EXPRESSION -> "StringTemplateExpression";
            case TEMPLATE_PROCESSOR_EXPRESSION -> "TemplateProcessorExpression";
            case PRIMITIVE_TYPE -> "PrimitiveType";
            case CLASS_TYPE -> "ClassType";
            case ARRAY_TYPE -> "ArrayType";
            case PARAMETERIZED_TYPE -> "ParameterizedType";
            case WILDCARD_TYPE -> "WildcardType";
            case UNION_TYPE -> "UnionType";
            case INTERSECTION_TYPE -> "IntersectionType";
            case VAR_TYPE -> "VarType";
            case MODIFIER -> "Modifier";
            case ANNOTATION -> "Annotation";
            case ANNOTATION_ELEMENT -> "AnnotationElement";
            case LINE_COMMENT -> "LineComment";
            case BLOCK_COMMENT -> "BlockComment";
            case JAVADOC_COMMENT -> "JavadocComment";
            case WHITESPACE -> "Whitespace";
            case ERROR_NODE -> "ErrorNode";
            case EOF_NODE -> "EofNode";
            case UNNAMED_CLASS -> "UnnamedClass";
            case UNNAMED_VARIABLE -> "UnnamedVariable";
            case MODULE_IMPORT_DECLARATION -> "ModuleImportDeclaration";
            case FLEXIBLE_CONSTRUCTOR_BODY -> "FlexibleConstructorBody";
            case PRIMITIVE_PATTERN -> "PrimitivePattern";
            case COMPACT_MAIN_METHOD -> "CompactMainMethod";
            case INSTANCE_MAIN_METHOD -> "InstanceMainMethod";
            default -> "Unknown(" + nodeType + ")";
        };
    }

    /**
     * Checks if the node type represents a declaration.
     */
    public static boolean isDeclaration(byte nodeType) {
        return (nodeType >= CLASS_DECLARATION && nodeType <= RECORD_DECLARATION) ||
               (nodeType >= METHOD_DECLARATION && nodeType <= LOCAL_VARIABLE_DECLARATION);
    }

    /**
     * Checks if the node type represents a statement.
     */
    public static boolean isStatement(byte nodeType) {
        return nodeType >= BLOCK_STATEMENT && nodeType <= YIELD_STATEMENT;
    }

    /**
     * Checks if the node type represents an expression.
     */
    public static boolean isExpression(byte nodeType) {
        return nodeType >= LITERAL_EXPRESSION && nodeType <= TEMPLATE_PROCESSOR_EXPRESSION;
    }

    /**
     * Checks if the node type represents a type.
     */
    public static boolean isType(byte nodeType) {
        return nodeType >= PRIMITIVE_TYPE && nodeType <= VAR_TYPE;
    }

    /**
     * Checks if the node type represents a comment or whitespace.
     */
    public static boolean isTrivia(byte nodeType) {
        return nodeType >= LINE_COMMENT && nodeType <= WHITESPACE;
    }

    // Prevent instantiation
    private NodeType() {}
}