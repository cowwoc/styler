# Claude Common Style Guide - Detection Patterns

**File Scope**: All files (`.java`, `.ts`, `.mts`, `.tsx`)
**Purpose**: Language-agnostic violation detection patterns

## TIER 1 CRITICAL - Build Blockers



## TIER 2 IMPORTANT - Code Review

### Magic Strings - Hardcoded Values
**Detection Pattern**: `"[^"]{20,}"` (long hardcoded strings)
**Violation**: Long strings embedded in code without constants
**Correct**: Extract to named constants with clear meaning

## TIER 3 QUALITY - Best Practices

### Code Duplication - Repeated Logic Blocks
**Detection Pattern**: Manual review - identical code blocks >5 lines
**Violation**: Copy-pasted code instead of shared functions
**Correct**: Extract common functionality to shared utilities

### Comments - Obvious Statements
**Detection Pattern**: `//\s*(Increment|Decrement|Set|Get|Return)`
**Violation**: `counter++; // Increment counter`
**Correct**: Remove obvious comments, keep complex logic explanations

### Comments - Inline Placement
**Detection Pattern**: `\S+.*//.*[^;]$` (code followed by comment on same line, excluding short explanations)
**Violation**: `maximumSize(3650). // ~10 years of daily lookups`
**Correct**: `// Maximum cache size: ~10 years of daily lookups\nmaximumSize(3650).`
**Rationale**: Comments should precede the code they document for better readability

### Comments - Historical References
**Detection Pattern**: `//.*\b(removed|added|changed|was|previously|used to)\b`
**Violation**: `// Note: ExpressionParser removed - will be added when feature is implemented`
**Correct**: Remove historical comments - use git history instead
**Rationale**: Code should document current state, not past actions

## Detection Commands

```bash

# Long hardcoded strings
grep -rn '"[^"]{20,}"' --include="*.java" src/  

# Obvious Comments
grep -rn "//\s*(Increment\|Decrement\|Set\|Get\|Return)" src/
```