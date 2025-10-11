# Claude Common Style Guide - Detection Patterns

**File Scope**: All files (`.java`, `.ts`, `.mts`, `.tsx`)
**Purpose**: Language-agnostic violation detection patterns

## TIER 1 CRITICAL - Build Blockers

(None)

## TIER 2 IMPORTANT - Code Review

### Magic Strings - Hardcoded Values
**Pattern**: `"[^"]{20,}"` | **Violation**: Long strings in code without constants | **Correct**: Extract to named constants

## TIER 3 QUALITY - Best Practices

### Code Duplication - Repeated Logic Blocks
**Pattern**: Manual review - identical blocks >5 lines | **Violation**: Copy-pasted code | **Correct**: Extract to shared utilities

### Comments - Obvious Statements
**Pattern**: `//\s*(Increment|Decrement|Set|Get|Return)` | **Violation**: `counter++; // Increment counter` | **Correct**: Remove obvious comments, keep complex logic explanations

### Comments - Inline Placement
**Pattern**: `\S+.*//.*[^;]$` | **Violation**: `maximumSize(3650). // ~10 years` | **Correct**: Comment precedes code: `// Maximum cache size: ~10 years\nmaximumSize(3650).` | **Rationale**: Comments precede for readability

### Comments - Historical References
**Pattern**: `//.*\b(removed|added|changed|was|previously|used to)\b` | **Violation**: `// Note: ExpressionParser removed - will be added` | **Correct**: Remove historical comments, use git history | **Rationale**: Code documents current state, not past

## Detection Commands

```bash

# Long hardcoded strings
grep -rn '"[^"]{20,}"' --include="*.java" src/  

# Obvious Comments
grep -rn "//\s*(Increment\|Decrement\|Set\|Get\|Return)" src/
```