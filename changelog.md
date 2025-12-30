# Changelog

## 2025-12-30

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

## 2025-12-28

### Try-with-Resources Variable Reference Support ✅

**Completion Date**: 2025-12-28

**Task**: `add-try-resource-variable`

**Problem Solved**:
- Parser required full variable declarations in try-with-resources (Type name = expr)
- JDK 9+ syntax allowing effectively-final variable references (`try (existingVar)`) failed to parse

**Solution Implemented**:
- Modified `parseResource()` to detect simple identifier vs full declaration
- When identifier followed by `;` or `)`, treat as variable reference (done)
- Otherwise, proceed with full declaration parsing (existing behavior)

**Files Modified**:
- `parser/src/main/java/.../parser/Parser.java` - Enhanced parseResource() with lookahead

**Files Created**:
- `parser/src/test/java/.../parser/test/TryResourceVariableTest.java` - 6 tests

**Test Coverage**:
- Single variable reference: `try (reader)`
- Multiple variable references: `try (reader; writer)`
- Mixed declaration and reference: `try (var x = new...; existingVar)`
- Qualified variable reference (field access): `try (this.resource)`

**Quality**:
- All 6 tests passing
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
