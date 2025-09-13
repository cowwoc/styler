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

Code is written once but read many times. In a code formatter handling AST transformations, clarity and maintainability are critical because:
- **Team collaboration**: Multiple developers work on parser logic and formatting rules
- **Language evolution**: Java language features change with each release, requiring clear, modifiable code
- **Long-term maintenance**: Formatters must support legacy and cutting-edge language features simultaneously

## 🚨 TIER 1 CRITICAL - Build Blockers



## ⚠️ TIER 2 IMPORTANT - Code Review

### Magic Strings - Hardcoded Values
**Why problematic in parsers**: Hardcoded strings often represent language constructs that change over time. Parser error messages, token names, and language keywords need to be easily updateable.

**Maintenance burden**: Scattered strings make it difficult to update messaging when language specifications change.

**Localization considerations**: Code formatters may need to support multiple languages for error messages and user-facing text.

## 💡 TIER 3 QUALITY - Best Practices

### Code Duplication - Repeated Logic Blocks
**Why extract common functionality**: Financial calculations often involve similar patterns (rate applications, threshold checks, scaling operations). Shared utilities ensure consistent behavior and easier maintenance.

**Testing benefits**: Shared functions can be unit tested once and reused confidently throughout the financial calculation system.

**Regulatory updates**: When tax rates or calculation methods change, having logic in one place makes updates safer and more reliable.

### Comments - Obvious Statements
**When to comment financial code**: 
- **Business rules**: "Apply additional 2% surtax for Ontario residents with income > $220,000"
- **Regulatory references**: "Based on CRA T1 General form, line 15000"
- **Complex calculations**: "Graduated rate benefits calculation per Income Tax Act 117.91"

**When not to comment**: Basic programming operations that are self-evident from well-named variables and methods.

### Comments - Inline Placement
**Why comments should precede code**: Inline comments interrupt the flow of reading code and make lines longer, reducing readability especially on smaller screens.

**Financial code readability**: Tax calculation logic is already complex. Comments should enhance understanding, not clutter the logical flow.

**Best practice**: Place explanatory comments on the line above the code they describe, allowing the eye to read explanation first, then implementation.

### Comments - Historical References
**Why avoid historical comments**: Code should document the current state, not past decisions. Version control systems (git) provide complete change history.

**Maintenance burden**: Historical comments become stale and misleading as code evolves. They add cognitive overhead without functional value.

**Financial code focus**: Tax calculation code should focus on current regulatory requirements, not previous implementations or future possibilities.

**Financial code clarity**: Well-written financial code should read like a description of the tax calculation process, with comments reserved for business context rather than programming mechanics.

## 📚 Navigation

### Language-Specific Implementation

These common principles apply universally, but specific implementation details vary by language:
- **[Java Style Guide](java-human.md)**: Additional validation and exception handling patterns
- **[TypeScript Style Guide](typescript-human.md)**: Type safety and interface design considerations
- **[Testing Conventions](testing-human.md)**: Testing patterns, parallel execution, and JPMS structure

Both language-specific guides build upon these foundational principles while addressing language-specific concerns for financial software development.

### Related Documentation
- **[Master Style Guide](../code-style-human.md)**: Complete overview and philosophy
- **[Claude Detection Patterns](common-claude.md)**: Automated rule detection patterns