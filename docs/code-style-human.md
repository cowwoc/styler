# Code Style Guide - Human Understanding

**Purpose**: Comprehensive guide to code quality standards for Java code formatter development
**Audience**: Developers, code reviewers, and technical leads

## 📋 Table of Contents

- [🧠 Philosophy and Principles](#philosophy-and-principles)
- [📚 Understanding the Documentation Structure](#understanding-the-documentation-structure)  
- [🛠 Working with Different Codebase Types](#working-with-different-codebase-types)
- [⚙️ Development Workflow Integration](#development-workflow-integration)
- [🚫 Common Anti-Patterns](#common-anti-patterns-in-parser-code)
- [🔧 Build Process Integration](#integration-with-build-process)
- [📈 Continuous Improvement](#continuous-improvement)

---

## 🧠 Philosophy and Principles

### Why Code Style Matters in Code Formatter Development

This codebase implements Styler, a Java code formatter that processes source code and applies formatting transformations. Code quality is critical because:

- **Parse Accuracy**: Errors in AST parsing could corrupt source code or lose formatting information
- **Performance Requirements**: Code formatters must process large codebases efficiently with minimal latency
- **System Reliability**: Formatters must handle malformed code gracefully without data loss
- **Team Productivity**: Consistent code style reduces cognitive load and speeds development
- **Long-term Maintenance**: Parser and formatter logic must evolve with Java language changes

### Core Design Principles

1. **Fail-Fast Validation**: Detect parse errors as early as possible in AST construction
2. **Performance Over Precision**: Optimize for speed and memory efficiency in large codebases
3. **Fidelity Preservation**: All source code transformations must preserve developer intent and comments
4. **Immutability**: AST data structures should prevent accidental modification during formatting
5. **Type Safety**: Use strong typing to prevent confusion between tokens, nodes, and trivia elements

## 📚 Understanding the Documentation Structure

> **Quick Start**: New to the project? Start with [Common Practices](code-style/common-human.md) then move to your language-specific guide.

### Language-Specific Guides
- **[Common Practices](code-style/common-human.md)**: Universal principles applying to all code
- **[Java Patterns](code-style/java-human.md)**: JVM-specific patterns for AST parsing and code formatting  
- **[TypeScript Practices](code-style/typescript-human.md)**: Type-safe frontend development
- **[Testing Conventions](code-style/testing-human.md)**: Testing patterns, parallel execution, and JPMS structure

### Rule Priority Levels

**TIER 1 CRITICAL**: Build blockers and parsing accuracy issues
- These rules prevent compilation failures or parsing errors
- Violations must be fixed before code review approval
- Examples: AST fidelity, input validation, source position tracking

**TIER 2 IMPORTANT**: Maintainability and business logic patterns
- These rules affect code quality and team productivity
- Should be addressed during code review
- Examples: Error message context, proper exception types, design patterns

**TIER 3 QUALITY**: Best practices and conventions
- These rules improve readability and long-term maintainability
- Can be addressed in follow-up improvements
- Examples: Method length, parameter count, naming conventions

## 🛠 Working with Different Codebase Types

### Pure Java Projects
**Focus Areas**: AST parsing accuracy, JVM optimization, compiler patterns
**Key Concerns**: Parser performance, validation frameworks, language feature support
**Reference**: [Java Style Guide](code-style/java-human.md) for detailed Java-specific guidance

### Pure TypeScript Projects  
**Focus Areas**: Type safety, API contracts, frontend reliability
**Key Concerns**: Interface design, null safety, build-time validation
**Reference**: [TypeScript Style Guide](code-style/typescript-human.md) for TypeScript-specific guidance

### Mixed Language Projects
**Coordination Strategy**: 
- Maintain consistent business logic patterns across languages
- Use TypeScript interfaces that match Java domain objects
- Apply common formatting and style rules uniformly
- Ensure API contracts are type-safe on both ends

## ⚙️ Development Workflow Integration

### Before Writing Code
1. **Understand Domain Context**: Review Java parsing and formatting requirements
2. **Choose Appropriate Patterns**: Select patterns based on business logic complexity
3. **Plan Validation Strategy**: Determine fail-fast validation points
4. **Consider Test Strategy**: Plan how to verify parsing and formatting accuracy

### During Development
1. **Apply TIER 1 Rules**: Ensure build won't fail and calculations are accurate
2. **Follow Language Patterns**: Use established patterns from language-specific guides
3. **Write Parallel-Safe Tests**: Follow [Testing Conventions](code-style/testing-human.md) for thread-safe test design
4. **Document Business Logic**: Add context for complex parsing transformations
5. **Validate Edge Cases**: Consider boundary conditions for malformed source code

### Code Review Process
1. **Automated Checks**: Run linting and build validation first
2. **TIER 1 Verification**: Verify critical parsing accuracy rules
3. **Test Quality Review**: Ensure tests follow [parallel execution safety rules](code-style/testing-human.md#parallel-test-execution-requirements)
4. **Business Logic Review**: Check calculation logic against requirements
5. **Documentation Review**: Ensure external sources are properly referenced
6. **TIER 2/3 Assessment**: Evaluate maintainability and best practices

## 🚫 Common Anti-Patterns in Financial Code

### Precision Anti-Patterns
❌ **Using `double` for money**: `double balance = 150.75;`
✅ **Using `BigDecimal`**: `BigDecimal balance = new BigDecimal("150.75");`

### Validation Anti-Patterns  
❌ **No validation**: `public void setSourceCode(String source) { this.source = source; }`
✅ **Fail-fast validation**: `requireThat(source, "source").isNotNull().isNotBlank();`

### Documentation Anti-Patterns
❌ **Magic parsing**: `depth = maxDepth * 2;`
✅ **Documented constants**: `@JavaLanguageSpec("JLS §14.4") depth = calculateNestingDepth(MAX_BLOCK_DEPTH);`

### Error Handling Anti-Patterns
❌ **Generic exceptions**: `throw new Exception("Invalid input");`
✅ **Parser context**: `throw new ParseException("Nesting depth " + depth + " exceeds maximum " + MAX_DEPTH + " at line " + line);`

## 🔧 Integration with Build Process

### Automated Enforcement
- **Linting**: Catches formatting and basic style violations
- **Type Checking**: Ensures type safety in TypeScript code
- **Unit Tests**: Validates parsing and formatting accuracy
- **Integration Tests**: Verifies end-to-end calculation workflows

### Manual Review Focus
- **Business Logic Correctness**: Verify calculation algorithms match requirements
- **Regulatory Compliance**: Check external source documentation
- **Edge Case Handling**: Review boundary condition processing
- **Performance Considerations**: Assess scalability for large datasets

### Automated Style Fixing Tools

**Styler Format Engine**: For code formatting and style enforcement
- **Location**: `styler-core` Maven module
- **Strategy**: AST-based formatting approach with trivia preservation
- **Usage**: Command-line interface for batch formatting operations
- **Main Class**: `io.github.styler.cli.StylerCLI`
- **Test Suite**: Comprehensive test coverage validating comment preservation and format fidelity

**Consolidate-Then-Split Strategy**:
1. **Consolidate**: Multi-line constructs → single line format
2. **Evaluate**: Check if consolidated line ≤ 120 characters
3. **Split**: If too long, split at semantic boundaries (parameters, strings)
4. **Result**: Resolves UnderutilizedLines while respecting LineLength limits

## 📈 Continuous Improvement

### Annual Updates
- **Tax Rule Changes**: Update calculation logic for new tax years
- **Regulatory Updates**: Modify validation and documentation as requirements evolve
- **Pattern Evolution**: Refine patterns based on team experience and new requirements

### Code Quality Metrics
- **Violation Trend Analysis**: Track style violation rates over time
- **Financial Accuracy Testing**: Monitor calculation precision in automated tests
- **Code Review Effectiveness**: Measure defect catch rate in reviews
- **Team Productivity**: Assess impact of style consistency on development speed

---

**Remember**: Code style in code formatters isn't just about aesthetics—it's about correctness, maintainability, and language compliance. Every style rule exists to support the ultimate goal of delivering accurate, reliable source code parsing and formatting.