# TypeScript Style Guide - Human Understanding

**Purpose**: TypeScript-specific practices for financial application development
**Companion**: See `claude-typescript-minimal-style.md` for detection patterns

## 📋 Table of Contents

- [🧠 Why TypeScript for Financial Systems](#why-typescript-for-financial-systems)
- [🚨 TIER 1 CRITICAL - Build Blockers](#tier-1-critical---build-blockers)
- [⚠️ TIER 2 IMPORTANT - Code Review](#tier-2-important---code-review)
- [💡 TIER 3 QUALITY - Best Practices](#tier-3-quality---best-practices)
- [📚 Integration with Build Process](#integration-with-build-process)

---

## 🧠 Why TypeScript for Financial Systems

TypeScript provides compile-time type safety that is crucial for financial applications because:
- **Calculation accuracy**: Type checking prevents unit mismatches (dollars vs percentages)
- **API contracts**: Interfaces ensure consistent data structures across tax calculation modules
- **Refactoring safety**: Large-scale changes to financial models are safer with type checking
- **Documentation**: Types serve as living documentation of financial data structures

## 🚨 TIER 1 CRITICAL - Build Blockers

### Export Organization - Scattered Throughout File
**Why consolidate exports**: In financial modules, scattered exports make it difficult to understand the public API. Tax calculation modules should clearly show what functionality they expose.

**API clarity**: Consolidated exports at the end of the file provide a clear module interface, making it easier to understand dependencies between financial calculation components.

**Maintenance benefit**: When tax rules change and modules need restructuring, consolidated exports make it easier to identify and update public interfaces.

### Import Organization - Incorrect Order
**Why order matters**: Consistent import ordering makes dependencies clear and prevents circular dependency issues common in financial calculation systems.

**Mental model**: The import order reflects dependency layers:
1. JSON data (configuration, tax tables)
2. External libraries (math, date handling)
3. Internal modules (shared financial utilities)
4. Relative imports (local calculation helpers)

**Financial system clarity**: Tax calculation modules often have complex dependencies. Clear import organization helps developers understand the data flow.

### Type Safety - Any Type Used
**Critical in finance**: The `any` type defeats TypeScript's safety guarantees. In financial calculations, losing type safety can lead to:
- Mixing currencies (CAD vs USD calculations)
- Unit confusion (annual vs monthly amounts)
- Invalid state transitions in financial workflows

**Business impact**: Type errors in production can lead to incorrect tax calculations, affecting real financial outcomes for users.

### Interface Naming - Generic Names
**Why descriptive names matter**: Financial systems have many data structures (tax brackets, contribution limits, pension calculations). Generic names create confusion and maintenance problems.

**Domain language**: Good interface names reflect financial domain concepts and make code self-documenting for business analysts who review tax calculation logic.

**Examples of good financial interface names**:
- `TaxBracketDefinition` instead of `Data`
- `ContributionLimits` instead of `Config`  
- `PensionCalculationState` instead of `State`

### JSDoc - Missing @throws Documentation
**Critical for financial functions**: Tax calculation functions can fail in many ways (invalid inputs, regulatory edge cases, calculation overflows). Proper documentation helps developers handle errors appropriately.

**Business requirement**: Financial applications often need detailed error handling for compliance and user experience. `@throws` documentation ensures proper error handling implementation.

### JSON Imports - Using Fetch for Static Data
**Why import over fetch**: Tax configuration data (rates, brackets, limits) should be bundled with the application for performance and reliability. Fetch introduces runtime dependencies that can cause calculation failures.

**Build-time validation**: Importing JSON files allows TypeScript to validate the data structure at compile time, catching configuration errors before deployment.

### Optional Chaining - Incorrect Usage
**Financial data safety**: Tax calculations often work with optional data (spouse information, business income, etc.). Proper null handling prevents runtime errors in production financial calculations.

**Decision framework**:
- Use `?.` when data might legitimately be missing
- Use `!` only when you have business logic guarantees
- Never use `!` with user input or external API data

### Enum Usage - String Values Without Explicit Assignment
**Why explicit values**: Financial systems often integrate with external APIs and databases. Explicit enum values ensure consistent serialization across system boundaries.

**Maintenance safety**: Auto-numbered enums can change values when new entries are added, breaking stored financial data or API contracts.

### Arrow Function Returns - Implicit Object Returns
**Common in financial transformations**: Tax calculation code often transforms data structures. Implicit returns with objects can create subtle syntax errors that are hard to debug.

**Calculation pipeline clarity**: Explicit returns make financial data transformations more readable and maintainable.

### Promise Error Handling - Missing Catch
**Critical in financial systems**: Unhandled promise rejections can crash financial calculation processes. Every async operation in tax calculations should have proper error handling.

**User experience**: Financial applications can't afford silent failures. Users need clear feedback when calculations fail.

## ⚠️ TIER 2 IMPORTANT - Code Review

### Interface Design - Missing Readonly Properties
**Why immutability matters**: Financial data like tax rates and contribution limits should not be accidentally modified during calculations. `readonly` properties prevent unintended mutations.

**Functional programming benefits**: Immutable financial data structures make calculations more predictable and easier to test.

**Audit trails**: Immutable data helps maintain proper audit trails for financial calculations.

### Type Guards - Missing User-Defined Type Guards
**Financial data validation**: Tax calculations often receive data from various sources (user input, APIs, databases). Custom type guards provide runtime validation with compile-time type safety.

**Error handling**: Good type guards provide meaningful error messages when financial data doesn't match expected types.

### Null Assertions - Unsafe Usage
**High risk in finance**: Non-null assertions (`!`) are dangerous in financial code because they can cause runtime crashes during critical calculations.

**Safe alternatives**: Use optional chaining, type guards, or default values instead of assertions.

### Generic Constraints - Missing Constraints
**Type safety in calculations**: Financial functions should constrain their generic types to ensure only appropriate data structures are used in calculations.

**API design**: Well-constrained generics make financial utility functions safer and more self-documenting.

## 💡 TIER 3 QUALITY - Best Practices

### Union Types - Using Any Instead of Union
**Better type modeling**: Financial data often has limited valid values (tax filing status, contribution types, etc.). Union types model these constraints better than `any`.

**IntelliSense benefits**: Union types provide better autocomplete and error detection during development.

### Function Overloads - Missing Overload Signatures
**Complex financial calculations**: Tax calculation functions often behave differently based on input parameters. Function overloads provide better type safety than union parameter types.

**API usability**: Overloaded functions provide better development experience with more specific return types.

### Destructuring - Missing for Object Parameters
**Financial function clarity**: Tax calculation functions often take many parameters. Destructuring makes function signatures clearer and reduces parameter-passing errors.

**Default values**: Destructuring enables easy default values for optional financial parameters.

### Utility Types - Not Using Built-in Utility Types
**Maintenance efficiency**: TypeScript's utility types reduce code duplication when creating variations of financial data interfaces.

**Type relationships**: Utility types express relationships between financial data types more clearly than manual interface definitions.

**Common financial patterns**:
- `Partial<TaxReturn>` for incomplete tax data during preparation
- `Required<OptionalIncomeFields>` for finalized calculations
- `Pick<PersonalInfo, 'age' | 'province'>` for calculation-specific data

## 📚 Navigation

### Related Documentation
- **[Common Practices](common-human.md)**: Universal principles applying to all languages
- **[Java Style Guide](java-human.md)**: JVM-specific patterns for financial calculations  
- **[Testing Conventions](testing-human.md)**: Testing patterns, parallel execution, and JPMS structure
- **[Master Style Guide](../code-style-human.md)**: Complete overview and philosophy

### Claude Detection Patterns
- **[TypeScript Detection Patterns](typescript-claude.md)**: Automated rule detection patterns

## 📚 Integration with Build Process

These TypeScript-specific rules integrate with the overall code quality system. The companion detection file provides specific patterns for automated checking, while this guide provides the conceptual framework for understanding why these practices matter in financial application development.