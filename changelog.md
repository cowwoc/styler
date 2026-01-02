# Changelog

## 2026-01-02

### Refactor Parser Limits to be AST-Focused ✅

**Task**: `refactor-parser-depth-limiting`

**Problem Solved**:
- `MAX_PARSE_DEPTH` (30) was framed as parser recursion depth - an implementation detail
- `MAX_ARENA_CAPACITY` (10M nodes) was too generous for single-file parsing
- Limits should describe AST properties, not parser internals

**Solution Implemented**:
- Renamed `MAX_PARSE_DEPTH` → `MAX_NODE_DEPTH` (30 → 100)
  - Describes maximum nesting depth of nodes in the AST
  - Protects against stack overflow from deeply nested expressions
  - 100 provides margin for legitimate nesting while preventing stack overflow
- Lowered `MAX_ARENA_CAPACITY` from 10M → 100K nodes
  - More appropriate limit for single-file parsing (~1.6MB)
  - Typical ASTs have 1K-10K nodes; 100K provides 10x safety margin

**Files Modified**:
- `ast/core/src/main/java/.../ast/core/SecurityConfig.java` - Updated constants and JavaDoc
- `parser/src/main/java/.../parser/Parser.java` - Updated constant reference
- `parser/src/test/java/.../parser/test/ParserTest.java` - Updated constant reference
- `parser/src/test/java/.../parser/test/SecurityTest.java` - Updated constant reference

**Quality**:
- All tests passing
- Zero Checkstyle/PMD violations
- Build successful

---

### Collapse Import Node Types (AST Simplification) ✅

**Task**: `collapse-import-node-types`

**Problem Solved**:
- AST treated static imports as distinct node type (`STATIC_IMPORT_DECLARATION`) separate from regular imports
- Consumer code had to query two node types and merge results, adding complexity
- Semantically incorrect - "static" is a modifier, not a different construct

**Solution Implemented**:
- Added `boolean isStatic` component to `ImportAttribute` record
- Updated Parser to pass `isStatic` flag when creating import nodes
- Simplified `ImportExtractor` to use single `findNodesByType()` call
- Removed `STATIC_IMPORT_DECLARATION` from `NodeType` enum
- Removed `allocateStaticImportDeclaration()` from `NodeArena`

**Files Modified**:
- `ast/core/src/main/java/.../ImportAttribute.java` - Added `isStatic` component
- `ast/core/src/main/java/.../NodeType.java` - Removed `STATIC_IMPORT_DECLARATION`
- `ast/core/src/main/java/.../NodeArena.java` - Removed static import allocation method
- `parser/src/main/java/.../Parser.java` - Use single allocation for all imports
- `formatter/src/main/java/.../ImportExtractor.java` - Single query approach
- `formatter/src/main/java/.../ContextDetector.java` - Removed switch case

**Test Updates**:
- `ClassParserTest.java`, `ModuleImportParserTest.java` - Updated assertions
- `ParserImportAttributeTest.java` - Converted to full AST comparison with `isStatic` verification
- `ParserPackageAttributeTest.java` - Converted to full AST comparison
- `ParserTypeAttributeTest.java` - Converted to full AST comparison
- `PackageAnnotationParserTest.java` - Converted to full AST comparison
- `NodeArenaAttributeTest.java` - Updated for new attribute structure

**Test Infrastructure Improvements**:
- `ParserTestUtils.SemanticNode` - Refactored with compile-time safety:
  - Changed from public record to final class with private constructor
  - Removed generic `node(NodeType, int, int)` method that allowed passing attribute-requiring types
  - Added 72 type-specific factory methods for all non-attribute NodeTypes:
    - `compilationUnit()`, `methodDeclaration()`, `block()`, `returnStatement()`, etc.
    - `importNode()`, `moduleImportNode()`, `packageNode()`, `typeDeclaration()`, `parameterNode()`
  - `importNode()` requires `isStatic` parameter (prevents missing static flag)
  - Factory methods enforce compile-time correctness (cannot pass wrong NodeType)
- Updated 35 test files to use type-safe factory methods instead of generic `node()` calls

**Benefits**:
- Single query returns all imports in position order
- Cleaner semantic model (static is a modifier)
- Simpler consumer code

**Quality**:
- All tests passing
- Zero Checkstyle/PMD violations

---

## 2026-01-01

### Verify JEP 513 Flexible Constructor Bodies Support ✅

**Task**: `add-flexible-constructor-bodies`

**Problem Solved**:
- No verification tests existed for JEP 513 (Flexible Constructor Bodies - JDK 25)
- Needed to confirm parser correctly handles statements before `super()`/`this()` calls

**Key Finding**:
- The parser ALREADY supports JEP 513 syntax without modifications
- Task reduced to adding verification tests to confirm the implementation

**Files Created**:
- `parser/src/test/java/.../parser/test/FlexibleConstructorBodyParserTest.java` - 16 tests

**Test Coverage**:
- Statements before super(): validation, logging, method calls, multiple statements
- Control flow before super(): if-else, try-catch
- Statements before this(): validation, assignment, computation
- Implicit super(): no explicit constructor invocation
- Complex scenarios: nested classes, inheritance chains, generic type parameters
- Edge cases: empty constructor, explicit constructor only

**Quality**:
- All 16 tests passing
- Zero Checkstyle/PMD violations
- Build successful

---

### Parse Module Import Declarations (JEP 511) ✅

**Task**: `add-module-import-declarations`

**Problem Solved**:
- Parser did not support JEP 511 module import declarations (JDK 25)
- Syntax like `import module java.base;` failed to parse

**Solution Implemented**:
- Added `MODULE` token type and keyword mapping in Lexer
- Added `MODULE_IMPORT_DECLARATION` to NodeType enum
- Created `ModuleImportAttribute` record for module names
- Extended `parseImportDeclaration()` to detect and parse module imports
- Updated `ImportExtractor` and `ImportGrouper` to handle module imports as reorderable

**Files Modified**:
- `parser/src/main/java/.../parser/TokenType.java` - Added MODULE token
- `parser/src/main/java/.../parser/Lexer.java` - Added "module" keyword
- `ast/core/src/main/java/.../ast/core/NodeType.java` - Added MODULE_IMPORT_DECLARATION
- `ast/core/src/main/java/.../ast/core/NodeArena.java` - Added allocation/getter methods
- `parser/src/main/java/.../parser/Parser.java` - Added module import parsing
- `formatter/src/main/java/.../importorg/internal/ImportDeclaration.java` - Added isModule field
- `formatter/src/main/java/.../importorg/internal/ImportExtractor.java` - Module import extraction
- `formatter/src/main/java/.../importorg/internal/ImportGrouper.java` - Module import grouping

**Files Created**:
- `ast/core/src/main/java/.../ast/core/ModuleImportAttribute.java` - Module name attribute
- `parser/src/test/java/.../parser/test/ModuleImportParserTest.java` - 8 parser tests

**Test Coverage**:
- Basic module import (`import module java.base;`)
- Multi-segment module names (`import module com.example.app;`)
- Multiple module imports in sequence
- Mixed with regular and static imports
- Error handling for missing semicolon/module name

**Quality**:
- All 94 tests passing
- Zero Checkstyle/PMD violations

---

## 2025-12-31

### Parse Array Dimension Annotations (JSR 308) ✅

**Task**: `add-array-dimension-annotations`

**Problem Solved**:
- Parser did not support JSR 308 type annotations on array dimensions (JDK 8+)
- Syntax like `String @NonNull []` or `int @A [] @B []` failed to parse

**Solution Implemented**:
- Added `parseArrayDimensionsWithAnnotations()` helper method to Parser
- Modified `parseType()` and `tryCastExpression()` to call the new helper
- Annotations before each `[]` are now parsed via `parseAnnotation()`

**Files Modified**:
- `parser/src/main/java/.../parser/Parser.java` - Added array dimension annotation parsing

**Files Created**:
- `parser/src/test/java/.../parser/test/ArrayDimensionAnnotationParserTest.java` - 7 tests

**Test Coverage**:
- Single annotation on array dimension (`String @NonNull []`)
- Multiple annotations on single dimension (`String @A @B []`)
- Per-dimension annotations on multi-dimensional arrays (`int @A [] @B []`)
- Primitive arrays with annotations (`int @NonNull []`)
- Cast expressions with annotated array types
- Generic types with annotated array components
- Regression test for unannotated arrays

**Quality**:
- All tests passing
- Zero Checkstyle/PMD violations

---

### Parse Array Type Constructor References ✅

**Task**: `add-array-type-method-references`

**Problem Solved**:
- Parser failed on array type constructor references like `int[]::new` and `String[][]::new`
- `parsePrimitiveClassLiteral()` and `parseArrayAccessOrClassLiteral()` always expected `.class` after
  consuming empty brackets `[]`, not `::new` for array constructor references

**Solution Implemented**:
- Modified `parsePrimitiveClassLiteral()` to check for `DOUBLE_COLON` after consuming brackets
  - If `::` found with array dimensions, returns `ARRAY_TYPE` node for `parsePostfix()` to handle
  - If `::` found without brackets, throws error (primitives can't be instantiated directly)
- Modified `parseArrayAccessOrClassLiteral()` similarly for reference types
- Reuses existing `ARRAY_TYPE` and `METHOD_REFERENCE` node types (no new enum values)

**Files Modified**:
- `parser/src/main/java/.../parser/Parser.java` - Modified 2 methods (+30 lines)

**Files Created**:
- `parser/src/test/java/.../parser/test/ArrayTypeMethodReferenceParserTest.java` - 17 tests (+400 lines)

**Test Coverage**:
- Primitive array constructor references (int[], double[], boolean[], long[])
- Multi-dimensional primitive arrays (int[][], int[][][])
- Reference array constructor references (String[], Object[])
- Multi-dimensional reference arrays (String[][])
- Qualified type arrays (java.util.List[])
- Expression contexts (method argument, return, ternary, field initializer, lambda body)
- Error cases (primitive without array, malformed syntax)

**Quality**:
- All 549 parser tests passing
- Zero Checkstyle/PMD violations

---

## 2025-12-30

### Parse Qualified Class Instantiation (outer.new Inner()) ✅

**Task**: `add-qualified-class-instantiation`

**Problem Solved**:
- Parser did not support qualified class instantiation syntax for inner classes
- `parsePostfix()` after DOT only handled IDENTIFIER, CLASS, THIS, SUPER - not NEW

**Solution Implemented**:
- Added NEW token handling in `parseDotExpression()` for qualified instantiation
- Extracted `parseDotExpression()` helper method to reduce NCSS complexity
- Converted `parseNestedTypeDeclaration()` to switch expression per updated style rule

**Supported Syntax**:
- `outer.new Inner()` - simple qualified instantiation
- `outer.new Inner().method()` - chained method call
- `getOuter().new Inner()` - expression qualifier
- `outer.new Inner(1, 2)` - with constructor arguments
- `Outer.this.new Inner()` - qualified this
- `outer.new Inner() { }` - with anonymous class body

**Files Modified**:
- `parser/src/main/java/.../parser/Parser.java` - Added qualified instantiation parsing

**Files Created**:
- `parser/src/test/java/.../parser/test/QualifiedInstantiationParserTest.java` - 6 tests

**Quality**:
- All tests passing
- Zero Checkstyle/PMD violations
- Tests use proper `isEqualTo(expected)` AST comparison pattern

---

### Explicit Type Arguments Parsing Support ✅

**Task**: `add-explicit-type-arguments`

**Problem Solved**:
- Parser did not support explicit type arguments on method/constructor calls
- `Collections.<String>emptyList()`, `List::<String>of`, `new <T>Constructor()` failed to parse
- `parsePostfix()` did not check for `<` after DOT or DOUBLE_COLON
- `parseNewExpression()` did not check for `<` before type name

**Solution Implemented**:
- Added type argument parsing in `parsePostfix()` after DOT (handles `obj.<T>method()`)
- Added type argument parsing in `parsePostfix()` after DOUBLE_COLON (handles `Type::<T>method`)
- Added type argument parsing in `parseNewExpression()` (handles `new <T>Constructor()`)
- Reused existing `parseTypeArguments()` infrastructure (handles nested generics, wildcards, diamond)

**Files Modified**:
- `parser/src/main/java/.../parser/Parser.java` - Added 3 type argument checks

**Files Created**:
- `parser/src/test/java/.../parser/test/ExplicitTypeArgumentParserTest.java` - 25 tests

**Test Coverage**:
- Method invocation with type args: `Collections.<String>emptyList()`, `this.<T>method()`
- Multiple type arguments: `obj.<String, Integer>method()`
- Nested generics: `obj.<List<String>>method()`
- Constructor with type args: `new <String>Container()`
- Method reference with type args: `List::<String>of`, `Arrays::<String>sort`
- Constructor reference with type args: `ArrayList::<String>new`
- Chained calls, wildcard types, edge cases
- Error cases: malformed syntax rejection

**Quality**:
- All 25 tests passing
- Zero Checkstyle/PMD violations
- Build successful

---

### Parse Package Annotations in package-info.java ✅

**Task**: `add-package-annotations`

**Problem Solved**:
- Parser did not support annotations before `package` keyword in `package-info.java` files
- `parseCompilationUnit()` checked for `PACKAGE` token before handling annotations

**Solution Implemented**:
- Modified `parseCompilationUnit()` to parse annotations before `package` keyword
- Added `hasPackageLevelAnnotations()` helper with lookahead to detect package annotations
- Added `isAnnotationTypeDeclaration()` helper to distinguish `@interface` declarations
- `PACKAGE_DECLARATION` node now spans from first annotation to semicolon

**Files Modified**:
- `parser/src/main/java/.../parser/Parser.java` - Added package annotation parsing (+88 lines)

**Files Created**:
- `parser/src/test/java/.../parser/test/PackageAnnotationParserTest.java` - 7 tests (+227 lines)

**Test Coverage**:
- Single/multiple marker annotations before package
- Annotations with values (single, array, qualified names)
- Comments between annotations
- Error handling for annotations without package declaration

**Quality**:
- All 532 tests passing
- Zero Checkstyle/PMD violations

---

### Expand TokenType Acronyms to Full Names ✅

**Task**: `expand-tokentype-acronyms`

**Problem Solved**:
- TokenType enum used abbreviated names (LPAREN, LT, EOF) reducing code readability
- Developers needed to mentally expand acronyms to understand token meanings

**Solution Implemented**:
- Renamed 37 TokenType enum constants to descriptive names:
  - Separators: LPAREN→LEFT_PARENTHESIS, RPAREN→RIGHT_PARENTHESIS, LBRACE→LEFT_BRACE, etc.
  - Comparison: EQ→EQUAL, NE→NOT_EQUAL, LT→LESS_THAN, GT→GREATER_THAN, LE→LESS_THAN_OR_EQUAL, etc.
  - Logical: AND→LOGICAL_AND, OR→LOGICAL_OR
  - Bitwise: BITAND→BITWISE_AND, BITOR→BITWISE_OR
  - Shift: LSHIFT→LEFT_SHIFT, RSHIFT→RIGHT_SHIFT, URSHIFT→UNSIGNED_RIGHT_SHIFT
  - Compound assignment: PLUSASSIGN→PLUS_ASSIGN, MINUSASSIGN→MINUS_ASSIGN, etc.
  - Arithmetic: DIV→DIVIDE, MOD→MODULO, INC→INCREMENT, DEC→DECREMENT
  - Special: EOF→END_OF_FILE, AT→AT_SIGN, QUESTION→QUESTION_MARK

**Files Modified**:
- `parser/src/main/java/.../parser/TokenType.java` - Renamed all 37 enum constants
- `parser/src/main/java/.../parser/Parser.java` - Updated all token references
- `parser/src/main/java/.../parser/Lexer.java` - Updated token references
- `parser/src/main/java/.../parser/Token.java` - Updated token references
- `parser/src/test/java/.../parser/test/*.java` - Updated test assertions (8 test files)
- `docs/code-style/java-claude.md` - Updated example code

**Quality**:
- All 525 tests passing
- Zero compilation errors
- No behavior changes

---

### Refactor If-Else-If Chains to Switch Statements ✅

**Task**: `refactor-if-else-to-switch`

**Problem Solved**:
- Parser contained long if-else-if chains testing same variable against enum constants
- Code style rule: prefer switch over if-else-if chains with 3+ branches

**Solution Implemented**:
- Converted `parseStatement()` 16-branch if-else-if chain to switch with arrow syntax
- Converted `parseComments()` 4-branch if-else-if chain to switch with arrow syntax
- Combined related cases (CLASS, INTERFACE, ENUM, RECORD) using multiple case labels

**Files Modified**:
- `parser/src/main/java/.../parser/Parser.java` - Refactored 2 methods

**Quality**:
- All tests passing
- Zero Checkstyle/PMD violations
- Build successful
- No behavior changes

---

### Qualified This/Super Expression Support ✅

**Completion Date**: 2025-12-30

**Task**: `add-qualified-this-super`

**Problem Solved**:
- Parser did not support qualified `this` and `super` expressions for inner classes
- `Outer.this` and `Outer.super` expressions failed to parse
- `parsePostfix()` after DOT only handled IDENTIFIER and CLASS tokens

**Solution Implemented**:
- Added THIS and SUPER token handling in `parsePostfix()` after DOT
- Reused existing `THIS_EXPRESSION` and `SUPER_EXPRESSION` node types
- Updated error message to include 'this' and 'super' in expected tokens

**Files Modified**:
- `parser/src/main/java/.../parser/Parser.java` - Added THIS/SUPER handling in parsePostfix()

**Files Created**:
- `parser/src/test/java/.../parser/test/QualifiedThisSuperParserTest.java` - 12 tests

**Test Coverage**:
- Simple qualified this: `Outer.this`
- Simple qualified super: `Outer.super.method()`
- Nested qualified this: `Middle.this` in deeply nested classes
- Qualified this in assignment, return, method arguments
- Qualified super method calls and field access
- Chained method calls on qualified super
- Generic outer class qualified this: `Outer<T>.this`
- Qualified this in inner class constructor
- Qualified this in comparison expressions

**Quality**:
- All 12 tests passing
- Zero Checkstyle/PMD violations
- Build successful

---

### Nested Annotation Values ✅

**Completion Date**: 2025-12-30

**Task**: `add-nested-annotation-values`

**Problem Solved**:
- Parser threw "Unexpected token in expression: AT" when encountering nested annotations
- Could not parse valid Java code like `@Repeatable(@FooContainer)` or `@JsonProperty(@JsonAlias("name"))`

**Solution Implemented**:
- Modified `parseAnnotation()` to return `NodeIndex` instead of `void`
- Creates `ANNOTATION` node with proper start/end positions
- Added AT token case in `parsePrimary()` to handle annotations as expression values
- Extracted `parseLiteralExpression()` and `parsePrimitiveClassLiteral()` helper methods

**Files Modified**:
- `parser/src/main/java/.../parser/Parser.java` - parseAnnotation() returns NodeIndex, parsePrimary() handles AT
- `parser/src/test/java/.../parser/test/LocalAnnotationTest.java` - Added expected ANNOTATION nodes
- `parser/src/test/java/.../parser/test/ParameterDeclarationParserTest.java` - Added expected ANNOTATION nodes
- `parser/src/test/java/.../parser/test/TypeAnnotationBoundsParserTest.java` - Added expected ANNOTATION nodes

**Files Created**:
- `parser/src/test/java/.../parser/test/NestedAnnotationParserTest.java` - 6 tests

**Test Coverage**:
- Single nested annotation: `@Foo(@Bar)`
- Nested annotation with value: `@Foo(@Bar(1))`
- Nested annotations in array: `@Foo({@Bar, @Baz})`
- Deeply nested annotations: `@Foo(@Bar(@Baz(@Qux)))`
- Mixed array elements: `@Foo({1, @Bar, "text"})`
- Nested annotation in default value

**Quality**:
- All tests passing
- Zero Checkstyle/PMD violations
- Build successful

---

### Local Type Declaration Support ✅

**Completion Date**: 2025-12-30

**Task**: `add-local-type-declarations`

**Problem Solved**:
- Parser did not support local type declarations inside method bodies
- Local classes (Java 1.1+), interfaces, enums, and records (JDK 16+) failed to parse
- `parseStatement()` did not check for CLASS/INTERFACE/ENUM/RECORD tokens

**Solution Implemented**:
- Added `isLocalTypeDeclarationStart()` for lookahead detection of modifiers/annotations
- Added `parseLocalTypeDeclaration()` that delegates to existing parse methods
- Added `skipBalancedParens()` helper for skipping annotation arguments during lookahead
- Modified `parseStatement()` to detect and dispatch local type declarations

**Files Modified**:
- `parser/src/main/java/.../parser/Parser.java` - Added 3 new methods + statement dispatch

**Files Created**:
- `parser/src/test/java/.../parser/test/LocalTypeDeclarationParserTest.java` - 24 tests

**Test Coverage**:
- Basic local types: class, interface, enum, record
- Modifiers: final, abstract, annotations
- Nesting contexts: method, constructor, initializer, static initializer, lambda
- Complex types: members, default methods, enum constructors
- Inheritance: extends, implements clauses

**Quality**:
- All 24 tests passing (471 total parser tests)
- Zero Checkstyle/PMD violations
- Build successful

---

### Cast Expression Parsing Support

**Task**: `add-cast-expressions`

**Problem Solved**:
- Parser treated `(Type)` as parenthesized expression, not as cast operator
- Cast operand was not parsed as part of the cast expression
- Intersection casts `(Type1 & Type2) expr` were not supported

**Solution Implemented**:
- Added `tryCastExpression()` method with lookahead-based disambiguation
- Added `canStartUnaryExpression()` and `canStartUnaryExpressionNotPlusMinus()` helper methods
- Modified `parseParenthesizedOrLambda()` to detect and parse cast expressions
- Implemented JLS 15.16 disambiguation: primitive casts allow `+`/`-` operands, reference casts do not

**Files Modified**:
- `parser/src/main/java/.../parser/Parser.java` - Added cast expression parsing

**Files Created**:
- `parser/src/test/java/.../parser/test/CastExpressionParserTest.java` - 27 tests

**Test Coverage**:
- Primitive type casts (int, double)
- Reference type casts (String, qualified names, generics)
- Intersection casts (two types, three types, qualified types)
- Array casts (single/multi-dimensional, primitive arrays)
- Disambiguation tests (parenthesized vs cast, unary plus/minus)
- Chained casts, cast with method calls, cast in expressions
- Error cases (incomplete cast, malformed intersection)

**Quality**:
- All 27 tests passing
- Zero Checkstyle/PMD violations
- Build successful

---

## 2025-12-29

### Yield Statement Support ✅

**Completion Date**: 2025-12-29

**Task**: `add-yield-statement-support`

**Problem Solved**:
- Parser did not support JDK 14+ `yield value;` statements in switch expressions
- No `YIELD_STATEMENT` node type existed for AST representation

**Solution Implemented**:
- Added `YIELD_STATEMENT` to `NodeType` enum (after `THROW_STATEMENT`)
- Created `parseYieldStatement()` method following `parseThrowStatement()` pattern
- Added YIELD case in `parseStatement()` method
- Updated `ContextDetector` exhaustive switch to include `YIELD_STATEMENT`

**Files Modified**:
- `ast/core/src/main/java/.../ast/core/NodeType.java` - Added YIELD_STATEMENT
- `parser/src/main/java/.../parser/Parser.java` - Added parseYieldStatement() + case
- `formatter/src/main/java/.../linelength/internal/ContextDetector.java` - Added YIELD_STATEMENT case

**Files Created**:
- `parser/src/test/java/.../parser/test/YieldStatementParserTest.java` - 16 tests

**Test Coverage**:
- Simple yield with literals (integer, string, null)
- Yield with complex expressions (method invocation, binary, ternary, lambda)
- Yield with object creation and array access
- Nested switch expressions with yield
- Colon-style and arrow-style switch cases
- Multiple yields in single switch, default case with yield
- Comments around yield statements

**Quality**:
- All 16 tests passing
- Zero Checkstyle/PMD violations
- Build successful

---

### Annotation Element Defaults ✅

**Completion Date**: 2025-12-29

**Task**: `add-annotation-element-defaults`

**Problem Solved**:
- Parser did not handle `default value` clause in annotation type element declarations
- Annotation types like `@interface Config { String name() default "test"; }` failed to parse

**Solution Implemented**:
- Added DEFAULT token handling in `parseMethodRest()` after throws clause, before semicolon/block
- Simple 6-line addition: check for DEFAULT token, parse expression if found

**Files Modified**:
- `parser/src/main/java/.../parser/Parser.java` - Added DEFAULT handling in parseMethodRest()

**Files Created**:
- `parser/src/test/java/.../parser/test/AnnotationElementDefaultTest.java` - 8 tests

**Test Coverage**:
- Primitive defaults: String, int, boolean
- Array defaults: empty `{}`, non-empty `{"a", "b"}`
- Class literal defaults: `Object.class`
- Elements without defaults (regression)
- Mixed elements with and without defaults

**Known Limitation**:
- Nested annotation defaults (`@Foo default @Bar`) require `add-nested-annotation-values` task

**Quality**:
- All 8 tests passing
- Zero Checkstyle/PMD violations
- Build successful

---

### Multi-Catch Support ✅

**Completion Date**: 2025-12-29

**Task**: `add-multi-catch-support`

**Problem Solved**:
- Parser did not support JDK 7+ multi-catch syntax: `catch (IOException | SQLException e)`
- No `UNION_TYPE` node type existed for AST representation of union types

**Solution Implemented**:
- Added `UNION_TYPE` to `NodeType` enum (after `WILDCARD_TYPE`)
- Created dedicated `parseCatchParameter()` method in Parser for catch clause parameters
- Modified `parseCatchClause()` to call `parseCatchParameter()` instead of `parseParameter()`
- Union type parsing handles `|` operator between exception types

**Files Modified**:
- `ast/core/src/main/java/.../ast/core/NodeType.java` - Added UNION_TYPE
- `parser/src/main/java/.../parser/Parser.java` - Added parseCatchParameter() method
- `formatter/src/main/java/.../linelength/internal/ContextDetector.java` - Added UNION_TYPE case

**Files Created**:
- `parser/src/test/java/.../parser/test/MultiCatchParserTest.java` - 5 tests

**Test Coverage**:
- Two-exception union: `catch (IOException | SQLException e)`
- Three-exception union: `catch (IOException | SQLException | TimeoutException e)`
- Simple catch regression: Ensures single-exception catch still works
- Final modifier: `catch (final IOException | SQLException e)`
- Fully qualified names: `catch (java.io.IOException | java.sql.SQLException e)`

**Quality**:
- All tests passing
- Zero Checkstyle/PMD violations
- Build successful

---

### Binary, Hexadecimal, and Octal Literal Support ✅

**Completion Date**: 2025-12-29

**Task**: `add-binary-hex-literals`

**Problem Solved**:
- Lexer only handled decimal numeric literals via `Character.isDigit()`
- Binary (`0b1010`), hexadecimal (`0xFF`), and octal (`0755`) literals failed to tokenize

**Solution Implemented**:
- Enhanced `consumeNumber()` in Lexer to detect `0b`, `0B`, `0x`, `0X` prefixes
- Added `consumeBinaryNumber()` for binary literals with underscore support
- Added `consumeHexNumber()` for hexadecimal literals with underscore support
- Octal handled naturally by existing decimal path (leading zero with digits 0-7)

**Files Modified**:
- `parser/src/main/java/.../parser/Lexer.java` - Added binary/hex detection and consumption methods

**Files Created**:
- `parser/src/test/java/.../parser/test/NumericLiteralLexerTest.java` - 14 tests

**Test Coverage**:
- Binary literals: `0b1010`, `0B1111_0000`, uppercase/lowercase prefixes
- Hexadecimal literals: `0xFF`, `0XDEAD_BEEF`, mixed case digits
- Octal literals: `0755`, `0123`
- Underscores in all formats for readability
- Existing decimal literals (regression)

**Quality**:
- All 14 tests passing
- Zero Checkstyle/PMD violations
- Build successful

---

### Labeled Statement Support ✅

**Completion Date**: 2025-12-29

**Task**: `add-labeled-statement-support`

**Problem Solved**:
- Parser did not recognize labeled statements (`label: statement`)
- `parseStatement()` treated label identifiers as expression statements, causing parse failures

**Solution Implemented**:
- Added `LABELED_STATEMENT` to `NodeType` enum
- Modified `parseStatement()` to use lookahead for `IDENTIFIER COLON` pattern
- Created `parseLabeledStatement()` method that consumes label and recursively parses inner statement

**Files Modified**:
- `ast/core/src/main/java/.../ast/core/NodeType.java` - Added LABELED_STATEMENT
- `parser/src/main/java/.../parser/Parser.java` - Added lookahead and parseLabeledStatement()
- `formatter/src/main/java/.../linelength/internal/ContextDetector.java` - Added LABELED_STATEMENT case

**Files Created**:
- `parser/src/test/java/.../parser/test/LabeledStatementParserTest.java` - 10 tests

**Test Coverage**:
- Simple labeled statements with various inner statements (for, while, block)
- Nested labels (outer/inner pattern)
- Break and continue with labels
- Labels in switch statements
- Labels with all loop types

**Quality**:
- All 10 tests passing
- Zero Checkstyle/PMD violations
- Build successful

---

## 2025-12-30

### Try-with-Resources Variable Reference Support ✅

**Completion Date**: 2025-12-30

**Task**: `add-try-resource-variable`

**Problem Solved**:
- Parser required full variable declarations in try-with-resources (Type name = expr)
- JDK 9+ syntax allowing effectively-final variable references (`try (existingVar)`) failed to parse

**Solution Implemented**:
- Added `peekToken()` for single-token lookahead without consuming
- Added `isResourceVariableReference()` to detect variable reference vs declaration
- Added `parseResourceVariableReference()` and `parseResourceDeclaration()` methods
- Modified `parseResource()` to branch based on detection

**Files Modified**:
- `parser/src/main/java/.../parser/Parser.java` - Added 4 methods, modified parseResource()

**Files Created**:
- `parser/src/test/java/.../parser/test/TryResourceVariableTest.java` - 9 tests

**Test Coverage**:
- Single variable reference: `try (resource)`
- Multiple variable references: `try (stream1; stream2)`
- Mixed declaration and reference: `try (BufferedReader br = new...; existing)`
- Reference followed by declaration, declaration followed by reference
- With catch clause, with finally clause
- Trailing semicolon handling, three variable references
- Complex mixed scenario with interleaved declarations and references

**Quality**:
- All 9 tests passing
- Zero Checkstyle/PMD violations
- Build successful

---

### Maven Plugin Implementation ✅

**Completion Date**: 2025-12-28

**Task**: `create-maven-plugin`

**Problem Solved**:
- No build tool integration existed for running Styler during Maven builds
- Users had to manually invoke CLI for each project

**Solution Implemented**:
- Created `styler-maven-plugin` module with `StylerCheckMojo`
- Integrated with Maven's `verify` phase by default
- Supports configurable source directories, includes/excludes patterns
- Reports violations in Maven build output format

**Files Created**:
- `maven-plugin/pom.xml` - Plugin build configuration
- `maven-plugin/src/main/java/.../maven/StylerCheckMojo.java` - Check goal implementation
- `maven-plugin/src/test/java/.../maven/test/StylerCheckMojoTest.java` - Integration tests

**Configuration Options**:
- `sourceDirectories` - Directories to scan (default: src/main/java, src/test/java)
- `includes` - File patterns to include (default: **/*.java)
- `excludes` - File patterns to exclude
- `failOnViolation` - Whether to fail build on violations (default: true)
- `skip` - Skip plugin execution (default: false)

**Quality**:
- Plugin integrates with standard Maven lifecycle
- All tests passing
- Zero Checkstyle/PMD violations

---
