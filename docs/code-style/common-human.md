# Common Style Guide - Human Understanding

**Purpose**: Universal code quality principles that apply to all languages  
**Companion**: See [common-claude.md](common-claude.md) for detection patterns

## 📋 Table of Contents

- [🧠 Philosophy: Code as Communication](#philosophy-code-as-communication)
- [🚨 TIER 1 CRITICAL - Build Blockers](#tier-1-critical---build-blockers)
- [⚠️ TIER 2 IMPORTANT - Code Review](#tier-2-important---code-review)
- [💡 TIER 3 QUALITY - Best Practices](#tier-3-quality---best-practices)
- [📚 Navigation](#navigation)

---

## 🧠 Philosophy: Code as Communication

Code is written once but read many times. In a code formatter handling AST transformations, clarity and
maintainability are critical because:
- **Team collaboration**: Multiple developers work on parser logic and formatting rules
- **Language evolution**: Java language features change with each release, requiring clear, modifiable code
- **Long-term maintenance**: Formatters must support legacy and cutting-edge language features simultaneously

## 🚨 TIER 1 CRITICAL - Build Blockers



## ⚠️ TIER 2 IMPORTANT - Code Review

### Magic Strings - Hardcoded Values
**Why problematic in parsers**: Hardcoded strings often represent language constructs that change over time.
Parser error messages, token names, and language keywords need to be easily updateable.

**Maintenance burden**: Scattered strings make it difficult to update messaging when language specifications
change.

**Localization considerations**: Code formatters may need to support multiple languages for error messages and
user-facing text.

## 💡 TIER 3 QUALITY - Best Practices

### Increment/Decrement - Use Prefix Form
**Why prefer `++i` over `i += 1`**: The prefix increment/decrement operators are more idiomatic and concise.
Using `i += 1` or `i -= 1` is unnecessarily verbose when the increment/decrement operators exist.

**Consistency**: Using prefix form (`++i`) consistently throughout the codebase makes patterns easier to
recognize. Prefix form is preferred over postfix (`i++`) unless the return value matters.

### Exception Handling - Misleading Comments
**Why comments must match actions**: A comment like `// Log error but continue` that doesn't actually log
anything creates confusion during debugging and maintenance. Either perform the promised action or use an
accurate comment like `// Intentionally ignored`.

**Silent failures**: Misleading comments hide potential issues. If an exception is truly ignorable, document
why with a truthful comment.

### Unused Variables - Use Underscore Convention
**Why use `_` for unused variables**: When catching exceptions or receiving parameters that aren't used, naming
them `_` clearly signals intent. This applies to:
- Catch blocks: `catch (IOException _) { /* ignore */ }`
- Lambdas: `(unused, value) -> value` becomes `(_, value) -> value`
- Override methods with unused parameters

**Readability**: The underscore convention is widely recognized across languages (Python, Kotlin, Rust) and
immediately signals "this value is intentionally ignored."

### Code Duplication - Repeated Logic Blocks
**Why extract common functionality**: Parser operations often involve similar patterns (token validation, AST
construction, error handling). Shared utilities ensure consistent behavior and easier maintenance.

**Testing benefits**: Shared functions can be unit tested once and reused confidently throughout the code
formatting system.

**Language updates**: When Java language features or grammar rules change, having logic in one place makes
updates safer and more reliable.

### Comments - Obvious Statements
**When to comment parser code**:
- **Grammar rules**: "Apply precedence rules for binary operators per JLS §15.7"
- **Language references**: "Based on Java Language Specification §14.9"
- **Complex transformations**: "AST restructuring for method reference expressions per JLS §15.13"

**When not to comment**: Basic programming operations that are self-evident from well-named variables and
methods.

### Comments - Inline Placement
**Why comments should precede code**: Inline comments interrupt the flow of reading code and make lines
longer, reducing readability especially on smaller screens.

**Parser code readability**: AST transformation logic is already complex. Comments should enhance
understanding, not clutter the logical flow.

**Best practice**: Place explanatory comments on the line above the code they describe, allowing the eye to
read explanation first, then implementation.

### Comments - Historical References
**Why avoid historical comments**: Code should document the current state, not past decisions. Version control
systems (git) provide complete change history.

**Maintenance burden**: Historical comments become stale and misleading as code evolves. They add cognitive
overhead without functional value.

**Parser code focus**: Code formatting logic should focus on current language specifications, not previous
implementations or future possibilities.

**Parser code clarity**: Well-written parser code should read like a description of the transformation
process, with comments reserved for language specification context rather than programming mechanics.

## 📚 Navigation

### Language-Specific Implementation

These common principles apply universally, but specific implementation details vary by language:
- **[Java Style Guide](java-human.md)**: AST validation and parsing exception handling patterns
- **[TypeScript Style Guide](typescript-human.md)**: Type safety and interface design considerations
- **[Testing Conventions](testing-human.md)**: Testing patterns, parallel execution, and JPMS structure

Both language-specific guides build upon these foundational principles while addressing language-specific
concerns for code formatter development.

### Related Documentation
- **[Master Style Guide](../code-style-human.md)**: Complete overview and philosophy
- **[Claude Detection Patterns](common-claude.md)**: Automated rule detection patterns