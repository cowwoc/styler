# Changelog
## 2026-01-06

### Fix Array Creation Expression Parsing ✅

**Task**: `fix-array-creation-expression-parsing`

**Root Cause Discovery**:
- Task name was misleading - issue was NOT about static imports (those work correctly)
- Actual root cause: `parseNewExpression()` called `parseType()` which consumed `[]` brackets before array dimension expressions could be parsed
- This caused cascading errors like "Expected DOT but found IDENTIFIER" when parsing `new int[5]` or `new int[]{1,2,3}`

**Solution Implemented**:
- Created `parseTypeWithoutArrayDimensions()` helper method that parses type names without consuming array brackets
- Modified `parseNewExpression()` to use the new helper, allowing array dimensions and initializers to be parsed correctly

**Deliverables**:
- Modified `Parser.java` with new helper method and refactored array creation parsing
- Added `ArrayCreationParserTest.java` with 20 comprehensive tests covering all array creation patterns
- All tests verify both successful parsing AND correct AST structure

**Files Modified**:
- `parser/src/main/java/.../Parser.java` - Added `parseTypeWithoutArrayDimensions()`, refactored `parseNewExpression()`
- `parser/src/test/java/.../ArrayCreationParserTest.java` - 20 tests (+568 lines)

**Quality**:
- All 771 parser tests pass (751 existing + 20 new)
- Zero Checkstyle/PMD violations
- Enables parsing of Spring Framework 6.2.1 sources (fixes ~10 previously failing files)

---

### Add Anonymous Inner Class Parser Tests ✅

**Task**: `add-anonymous-inner-class-support`

**Finding**:
- Parser already supports anonymous inner class syntax in `parseObjectCreation()` (Parser.java:3049-3070)
- Existing implementation checks for `LEFT_BRACE` after constructor arguments and parses class body members
- Task scope shifted from "implement new feature" to "add comprehensive test coverage"

**Deliverables**:
- Added `AnonymousInnerClassParserTest.java` with 20 comprehensive tests
- Tests cover: empty body, methods, fields, constructor args, generics, diamond operator, nested classes
- All patterns verified working: `new Type() { }`, `new Type(args) { }`, `new Generic<T>() { }`

**Files Added**:
- `parser/src/test/java/.../test/AnonymousInnerClassParserTest.java` - 20 tests (+925 lines)

**Quality**:
- All 20 new tests pass
- All existing parser tests pass
- Zero Checkstyle/PMD violations

---

### Fix Block Comment in Member Declaration ✅

**Task**: `fix-block-comment-in-member-declaration`

**Problem Solved**:
- Block comments between class/interface/enum member declarations caused parse errors
- Error: "Unexpected token in member declaration: BLOCK_COMMENT"
- Affected ~10 files in Spring Framework's cglib, objenesis, asm packages

**Root Cause**:
- `parseMemberDeclaration()` didn't call `parseComments()` at its start
- Some call sites (class body loop) correctly called `parseComments()` first
- Other call sites (enum body after semicolon, anonymous class bodies) did not

**Solution Implemented**:
- Added `parseComments()` call at start of `parseMemberDeclaration()` (Parser.java:1137)
- Ensures all member declaration contexts uniformly handle leading comments

**Files Modified**:
- `parser/src/main/java/.../parser/Parser.java` - Added parseComments() call (+1 line)
- `parser/src/test/java/.../BlockCommentParserTest.java` - Added test (+27 lines)

**Quality**:
- All parser tests pass (55 test classes)
- Full project build passes
- Zero Checkstyle/PMD violations

---

### Fix ClasspathScanner Per-File Overhead ✅

**Task**: `fix-classpath-scanner-per-file-overhead`

**Problem Solved**:
- `ImportOrganizerFormattingRule` created new `ClasspathScanner` for EACH file processed
- `ClasspathScanner.create()` scanned system classpath even with `TypeResolutionConfig.EMPTY`
- Added 13.6ms overhead per file (for 1691 files = ~23 seconds wasted)

**Solution Implemented**:
- Added early return in `ClasspathScanner.create()` for empty config (avoids system classpath scan)
- Moved `ClasspathScanner` from internal package to public package for context access
- Added `TransformationContext.classpathScanner()` method for shared scanner access
- Created `ProcessingContext.classpathScanner` field for pipeline-level sharing
- Made `FileProcessingPipeline` implement `AutoCloseable` to manage scanner lifecycle
- Updated `ImportOrganizerFormattingRule` to use `context.classpathScanner()` instead of creating per-file

**Files Modified**:
- `formatter/src/main/java/.../formatter/ClasspathScanner.java` - Moved from internal, added early return
- `formatter/src/main/java/.../formatter/TransformationContext.java` - Added classpathScanner() method
- `formatter/src/main/java/.../importorg/ImportOrganizerFormattingRule.java` - Use context scanner
- `pipeline/src/main/java/.../pipeline/FileProcessingPipeline.java` - AutoCloseable, owns scanner
- `pipeline/src/main/java/.../pipeline/ProcessingContext.java` - Added classpathScanner field
- `pipeline/src/main/java/.../pipeline/internal/DefaultTransformationContext.java` - Added scanner field

**Performance Impact**:
- Per-file overhead: 13.6ms → ~0ms
- Spring Framework (1691 files): ~23 seconds saved

**Quality**:
- All tests passing (1532 tests)
- Zero Checkstyle/PMD violations

---

## 2026-01-05

### Add module-info.java Parsing Support (JPMS) ✅

**Task**: `add-module-info-parsing`

**Problem Solved**:
- Parser only handled compilation units with package/import/type declarations
- No support for parsing JPMS module declarations (module-info.java files)
- Could not parse module directives: requires, exports, opens, uses, provides

**Solution Implemented**:
- Added `ModuleParser` helper class to handle all module-related parsing
- Implemented module declaration parsing with support for open modules
- Added all module directive types with their attributes:
  - `requires` with transitive/static modifiers
  - `exports` and `opens` with qualified target modules
  - `uses` for service provider interface declarations
  - `provides` with multiple implementation classes
- Extracted module parsing from Parser to reduce class size (NcssCount compliance)
- Uses index-based lookahead for `isModuleDeclarationStart()` to avoid arena side effects

**Files Created**:
- `parser/src/main/java/.../parser/ModuleParser.java` - Module parsing helper class

**Files Modified**:
- `parser/src/main/java/.../parser/Parser.java` - Delegates to ModuleParser
- Various AST, token, and node type files for module support

**Quality**:
- All tests pass
- Zero Checkstyle/PMD violations

---

### Apply Brace Omission Style for Single-Line Control Statements ✅

**Task**: `apply-brace-omission-style`

**Problem Solved**:
- Codebase had inconsistent brace usage for single-line control statements
- No documented style rule for when to omit braces

**Solution Implemented**:
- Added brace omission rule to style documentation (java-style.md, java-claude.md, java-human.md)
- Applied style across 68 source files in the codebase
- Clarified rule: Omit braces ONLY when body fits on ONE visual line
- Multi-line statements (throws with string concatenation, etc.) require braces

**Files Modified**:
- `.claude/rules/java-style.md` - Quick reference with examples
- `docs/code-style/java-claude.md` - Detection patterns for violations
- `docs/code-style/java-human.md` - Rationale and practical examples
- 68 Java source files across all modules (brace removal for single-line bodies)
- 5 Java files (brace restoration for multi-line throw statements)

**Quality**:
- Zero Checkstyle/PMD violations
- Build successful

---

### Add Compilation Validation for Source Files ✅

**Task**: `add-compilation-check`

**Problem Solved**:
- No validation existed to ensure source files were compiled before formatting
- Users could run formatter on stale or missing class files, leading to issues
- Styler's scope.md specifies compilation is a precondition for proper formatting

**Solution Implemented**:
- Added `CompilationValidator` class for validating class file existence and timestamps
- Added `CompilationValidationResult` sealed interface with `Valid` and `Invalid` variants
- Added `validateCompilation(Collection<Path>)` method to `FileProcessingPipeline`
- Upfront validation: check all source files at start, before processing any files
- Timestamp comparison: class file must be at least as new as source file

**Files Created**:
- `pipeline/src/main/java/.../pipeline/CompilationValidationResult.java` - Result types
- `pipeline/src/main/java/.../pipeline/internal/CompilationValidator.java` - Validation logic
- `pipeline/src/test/java/.../pipeline/internal/test/CompilationValidatorTest.java` - 17 tests

**Files Modified**:
- `pipeline/src/main/java/.../pipeline/FileProcessingPipeline.java` - Added validateCompilation()
- `pipeline/src/test/java/module-info.java` - Exported test package

**API**:
```java
// One-time upfront validation before processing
CompilationValidationResult result = pipeline.validateCompilation(sourceFiles);
if (result instanceof CompilationValidationResult.Invalid invalid) {
    System.err.println(invalid.getErrorMessage());
    return;
}
// Proceed with formatting...
```

**Quality**:
- All tests passing (17 new tests)
- Zero Checkstyle/PMD violations
- Build successful

---

### Add Primitive Type Pattern Support (JEP 507) ✅

**Task**: `add-primitive-type-patterns`

**Problem Solved**:
- Parser did not support primitive type patterns in switch expressions (JEP 507)
- Syntax like `case int i ->` and `obj instanceof int i` failed to parse

**Solution Implemented**:
- Added `tryParsePrimitiveTypePattern()` method to Parser.java
- Modified `parseCaseLabelElement()` to check for primitive type patterns
- Supports all primitive types: int, long, double, float, boolean, byte, char, short
- Supports guard expressions: `case int i when i > 0 ->`
- Supports unnamed pattern variables: `case int _ ->`
- Works in both switch expressions and instanceof expressions

**Files Modified**:
- `parser/src/main/java/.../parser/Parser.java` - Added primitive type pattern parsing
- `parser/src/test/java/.../parser/test/PrimitiveTypePatternParserTest.java` - 8 comprehensive tests

**Test Coverage**:
- Basic primitive type patterns (int, long, double)
- Guard expressions with simple and complex conditions
- Instanceof expressions with primitive patterns
- Unnamed pattern variables
- All primitive types in single switch
- Complex guard expressions with multiple conditions

**Quality**:
- All 8 tests passing with proper AST comparison
- Zero Checkstyle/PMD violations
- Build successful

---

### Add Unicode Escape Preprocessing per JLS §3.3 ✅

**Task**: `add-unicode-escape-preprocessing`

**Problem Solved**:
- Lexer only processed Unicode escapes inside string/char literals
- Code like `int \u0041 = 1;` (valid Java for `int A = 1;`) failed to parse
- JLS §3.3 requires Unicode escapes to be processed before lexical analysis

**Solution Implemented**:
- Token record enhanced with `decodedText` field for semantic operations
- `text` field preserves original source for formatter output fidelity
- Lexer `tryParseUnicodeEscape()` uses checkpoint/rollback pattern
- Supports multiple-u syntax per JLS (`\uuu0041` is valid)
- Removed redundant `getText(sourceCode)` method from Token
- Parser updated to use `decodedText()` for semantic operations

**Files Modified**:
- `parser/src/main/java/.../parser/Token.java` - Added `decodedText` field, removed `getText()`
- `parser/src/main/java/.../parser/Lexer.java` - Unicode escape detection and parsing
- `parser/src/main/java/.../parser/Parser.java` - Use `decodedText()` for semantic operations
- `parser/src/test/java/.../parser/test/LexerTest.java` - Additional Unicode escape tests
- `parser/src/test/java/.../parser/test/LexerUnicodePreprocessingTest.java` - 36 comprehensive tests
- `parser/src/test/java/.../parser/test/LexerUnicodeEscapeOutsideLiteralsTest.java` - Edge case tests
- `.claude/rules/java-style.md` - Added magic numbers guideline

**Quality**:
- All tests passing (36 new tests)
- Zero Checkstyle/PMD violations
- Build successful

---

## 2026-01-03

### JEP 512 Implicit Classes and Instance Main Methods ✅

**Task**: `add-compact-source-files`

**Problem Solved**:
- Parser did not support JEP 512 implicit classes (JDK 25 feature)
- Files without explicit class declarations failed to parse
- Instance main methods (`void main()` without static) were not recognized

**Solution Implemented**:
- Added `IMPLICIT_CLASS_DECLARATION` to `NodeType` enum
- Added `allocateImplicitClassDeclaration()` to `NodeArena`
- Modified `parseCompilationUnit()` to detect implicit class scenarios
- Added `isTypeDeclarationStart()` with lookahead past modifiers to distinguish type declarations from members
- Added `isMemberDeclarationStart()` for detecting implicit class content
- Added `parseImplicitClassDeclaration()` to wrap top-level members
- Updated `ContextDetector` exhaustive switch for new node type

**Files Modified**:
- `ast/core/src/main/java/.../ast/core/NodeType.java` - Added IMPLICIT_CLASS_DECLARATION
- `ast/core/src/main/java/.../ast/core/NodeArena.java` - Added allocation method, updated isTypeDeclaration()
- `parser/src/main/java/.../parser/Parser.java` - Added implicit class detection and parsing
- `formatter/src/main/java/.../linelength/internal/ContextDetector.java` - Added switch case

**Files Created**:
- `parser/src/test/java/.../parser/test/ImplicitClassParserTest.java` - 25 tests

**Test Coverage**:
- Basic implicit class with void main()
- Instance main with String[] args
- Static members in implicit classes (static void main(), static fields)
- Mixed fields and methods
- With package declaration and imports
- Annotations and JavaDoc comments
- Multiple methods and complex scenarios

**Quality**:
- All tests passing
- Zero Checkstyle/PMD violations
- Build successful

### Migrate Parser Tests to NodeArena ✅

**Task**: `migrate-parser-tests-to-nodearena`

**Problem Solved**:
- Parser tests used `SemanticNode` wrapper class with Set-based comparison
- This abstraction added 1300+ lines of boilerplate code
- Set semantics lost ordering information important for NodeArena equality

**Solution Implemented**:
- Migrated all 38 test files from `parseSemanticAst()` + `Set<SemanticNode>` to `parse()` + `NodeArena`
- Removed SemanticNode class and all factory methods (1305 lines removed)
- ParserTestUtils reduced from 1341 lines to 56 lines
- Tests now use direct NodeArena comparison with try-with-resources

**Files Modified**:
- `parser/src/test/java/.../parser/test/ParserTestUtils.java` - Removed SemanticNode, kept only parse() and assertParseFails()
- 38 test files migrated to NodeArena pattern

**Quality**:
- All 633 parser tests passing

### Fix Remaining Comment Gaps ✅

**Task**: `fix-remaining-comment-gaps`

**Problem Solved**:
- Parser only handled comments in 18 locations via `parseComments()` calls
- Many valid Java comment locations were unsupported (control flow, array initializers, lambdas)
- Real-world codebases with comments in these locations would fail to parse correctly

**Solution Implemented**:
- Added ~36 `parseComments()` calls across 15 parser methods
- Covered: array initializers, lambda expressions, control flow (if/for/while/switch), blocks
- Note: Method/record parameters and type parameters deferred (parser architecture limitation)

**Files Modified**:
- `parser/src/main/java/.../parser/Parser.java` - Added parseComments() calls
- `parser/src/test/java/.../parser/test/ArrayInitializerCommentParserTest.java` - New tests
- `parser/src/test/java/.../parser/test/BlockCommentParserTest.java` - New tests
- `parser/src/test/java/.../parser/test/ControlFlowCommentParserTest.java` - New tests
- `parser/src/test/java/.../parser/test/LambdaCommentParserTest.java` - New tests

**Quality**:
- All 624 parser tests passing
- Zero Checkstyle/PMD violations
- Build successful

---

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

