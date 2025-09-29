# TypeScript Style Guide - Human Understanding

**Purpose**: TypeScript-specific practices for code formatter development
**Companion**: See `claude-typescript-minimal-style.md` for detection patterns

## 📋 Table of Contents

- [🧠 Why TypeScript for Code Formatters](#why-typescript-for-code-formatters)
- [🚨 TIER 1 CRITICAL - Build Blockers](#tier-1-critical---build-blockers)
- [⚠️ TIER 2 IMPORTANT - Code Review](#tier-2-important---code-review)
- [💡 TIER 3 QUALITY - Best Practices](#tier-3-quality---best-practices)
- [📚 Integration with Build Process](#integration-with-build-process)

---

## 🧠 Why TypeScript for Code Formatters

TypeScript provides compile-time type safety that is crucial for code formatters because:
- **Parse accuracy**: Type checking prevents type mismatches (tokens vs nodes)
- **API contracts**: Interfaces ensure consistent data structures across parser modules
- **Refactoring safety**: Large-scale changes to AST models are safer with type checking
- **Documentation**: Types serve as living documentation of parser data structures

## 🚨 TIER 1 CRITICAL - Build Blockers

### Export Organization - Scattered Throughout File
**Why consolidate exports**: In parser modules, scattered exports make it difficult to understand the public API. AST transformation modules should clearly show what functionality they expose.

**API clarity**: Consolidated exports at the end of the file provide a clear module interface, making it easier to understand dependencies between parser components.

**Maintenance benefit**: When language features change and modules need restructuring, consolidated exports make it easier to identify and update public interfaces.

### Import Organization - Incorrect Order
**Why order matters**: Consistent import ordering makes dependencies clear and prevents circular dependency issues common in parser systems.

**Mental model**: The import order reflects dependency layers:
1. JSON data (configuration, grammar definitions)
2. External libraries (parsing, AST utilities)
3. Internal modules (shared parser utilities)
4. Relative imports (local transformation helpers)

**Parser system clarity**: AST transformation modules often have complex dependencies. Clear import organization helps developers understand the data flow.

### Type Safety - Any Type Used
**Critical in parsers**: The `any` type defeats TypeScript's safety guarantees. In code parsing, losing type safety can lead to:
- Mixing token types (identifiers vs literals)
- Type confusion (expressions vs statements)
- Invalid state transitions in parser workflows

**Code quality impact**: Type errors in production can lead to incorrect code transformations, affecting source code integrity for users.

### Interface Naming - Generic Names
**Why descriptive names matter**: Parser systems have many data structures (AST nodes, token streams, parse contexts). Generic names create confusion and maintenance problems.

**Domain language**: Good interface names reflect parser domain concepts and make code self-documenting for developers who review transformation logic.

**Examples of good parser interface names**:
- `TokenDefinition` instead of `Data`
- `ParseOptions` instead of `Config`
- `TransformationState` instead of `State`

### JSDoc - Missing @throws Documentation
**Critical for parser functions**: AST parsing functions can fail in many ways (invalid syntax, language edge cases, parsing overflows). Proper documentation helps developers handle errors appropriately.

**Code quality requirement**: Parser applications often need detailed error handling for syntax validation and user experience. `@throws` documentation ensures proper error handling implementation.

### JSON Imports - Using Fetch for Static Data
**Why import over fetch**: Grammar configuration data (rules, precedence, operators) should be bundled with the application for performance and reliability. Fetch introduces runtime dependencies that can cause parsing failures.

**Build-time validation**: Importing JSON files allows TypeScript to validate the grammar structure at compile time, catching configuration errors before deployment.

### Optional Chaining - Incorrect Usage
**Parser data safety**: Code transformations often work with optional data (comments, metadata, etc.). Proper null handling prevents runtime errors in production parsing operations.

**Decision framework**:
- Use `?.` when data might legitimately be missing
- Use `!` only when you have business logic guarantees
- Never use `!` with user input or external API data

### Enum Usage - String Values Without Explicit Assignment
**Why explicit values**: Parser systems often integrate with external tools and databases. Explicit enum values ensure consistent serialization across system boundaries.

**Maintenance safety**: Auto-numbered enums can change values when new entries are added, breaking stored parser data or API contracts.

### Arrow Function Returns - Implicit Object Returns
**Common in parser transformations**: AST transformation code often transforms data structures. Implicit returns with objects can create subtle syntax errors that are hard to debug.

**Transformation pipeline clarity**: Explicit returns make AST data transformations more readable and maintainable.

### Promise Error Handling - Missing Catch
**Critical in parser systems**: Unhandled promise rejections can crash code transformation processes. Every async operation in parsing should have proper error handling.

**User experience**: Code formatters can't afford silent failures. Users need clear feedback when transformations fail.

## ⚠️ TIER 2 IMPORTANT - Code Review

### Interface Design - Missing Readonly Properties
**Why immutability matters**: Parser data like grammar rules and AST nodes should not be accidentally modified during transformations. `readonly` properties prevent unintended mutations.

**Functional programming benefits**: Immutable parser data structures make transformations more predictable and easier to test.

**Transformation trails**: Immutable data helps maintain proper audit trails for code transformations.

### Type Guards - Missing User-Defined Type Guards
**Parser data validation**: Code transformations often receive data from various sources (source files, APIs, databases). Custom type guards provide runtime validation with compile-time type safety.

**Error handling**: Good type guards provide meaningful error messages when parser data doesn't match expected types.

### Null Assertions - Unsafe Usage
**High risk in parsers**: Non-null assertions (`!`) are dangerous in parser code because they can cause runtime crashes during critical transformations.

**Safe alternatives**: Use optional chaining, type guards, or default values instead of assertions.

### Generic Constraints - Missing Constraints
**Type safety in transformations**: Parser functions should constrain their generic types to ensure only appropriate data structures are used in transformations.

**API design**: Well-constrained generics make parser utility functions safer and more self-documenting.

## 💡 TIER 3 QUALITY - Best Practices

### Union Types - Using Any Instead of Union
**Better type modeling**: Parser data often has limited valid values (token types, node kinds, etc.). Union types model these constraints better than `any`.

**IntelliSense benefits**: Union types provide better autocomplete and error detection during development.

### Function Overloads - Missing Overload Signatures
**Complex parser operations**: AST transformation functions often behave differently based on input parameters. Function overloads provide better type safety than union parameter types.

**API usability**: Overloaded functions provide better development experience with more specific return types.

### Destructuring - Missing for Object Parameters
**Parser function clarity**: AST transformation functions often take many parameters. Destructuring makes function signatures clearer and reduces parameter-passing errors.

**Default values**: Destructuring enables easy default values for optional parser parameters.

### Utility Types - Not Using Built-in Utility Types
**Maintenance efficiency**: TypeScript's utility types reduce code duplication when creating variations of parser data interfaces.

**Type relationships**: Utility types express relationships between parser data types more clearly than manual interface definitions.

**Common parser patterns**:
- `Partial<ParseResult>` for incomplete parsing during analysis
- `Required<OptionalTokenFields>` for finalized transformations
- `Pick<SourceInfo, 'line' | 'column'>` for error-reporting data

## 📚 Navigation

### Related Documentation
- **[Common Practices](common-human.md)**: Universal principles applying to all languages
- **[Java Style Guide](java-human.md)**: JVM-specific patterns for AST parsing and formatting  
- **[Testing Conventions](testing-human.md)**: Testing patterns, parallel execution, and JPMS structure
- **[Master Style Guide](../code-style-human.md)**: Complete overview and philosophy

### Claude Detection Patterns
- **[TypeScript Detection Patterns](typescript-claude.md)**: Automated rule detection patterns

## 📚 Integration with Build Process

These TypeScript-specific rules integrate with the overall code quality system. The companion detection file provides specific patterns for automated checking, while this guide provides the conceptual framework for understanding why these practices matter in code formatter development.