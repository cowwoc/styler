# Claude Common Style Guide - Detection Patterns

**File Scope**: All files (`.java`, `.ts`, `.mts`, `.tsx`)

Language-agnostic violation detection patterns

## TIER 1 CRITICAL - Build Blockers



## TIER 2 IMPORTANT - Code Review

### Magic Strings - Hardcoded Values
**Detection Pattern**: `"[^"]{20,}"` (long hardcoded strings)
**Violation**: Long strings embedded in code without constants
Extract to named constants with clear meaning

## TIER 3 QUALITY - Best Practices

### Code Duplication - Repeated Logic Blocks
**Detection Pattern**: Manual review - identical code blocks >5 lines
**Violation**: Copy-pasted code instead of shared functions
Extract common functionality to shared utilities

### Increment/Decrement - Verbose Form
**Detection Pattern**: `\b\w+\s*\+=\s*1\b` or `\b\w+\s*-=\s*1\b`
**Violation**: `i += 1` or `count -= 1`
Use `++i` or `--i` instead (prefix form preferred for consistency)

### Exception Handling - Misleading Comments
**Detection Pattern**: `catch.*\{[^}]*//.*\b(log|Log|LOG|record|report)\b[^}]*\}` with empty action
**Violation**: `catch (IOException e) { // Log error but continue }` (comment promises logging but nothing
happens)
Either actually log/record the error, or use an accurate comment like `// Intentionally ignored`

### Unused Variables - Named Parameters
**Detection Pattern**: `catch\s*\(\w+\s+[a-zA-Z]\w*\)` with variable unused in body
**Violation**: `catch (IOException e) { /* ignore */ }` (named variable `e` never used)
Use `_` for intentionally unused variables: `catch (IOException _) { /* ignore */ }`
This convention applies to any unused variable (catch blocks, lambdas, method parameters in overrides)

### Comments - Obvious Statements
**Detection Pattern**: `//\s*(Increment|Decrement|Set|Get|Return)`
**Violation**: `counter++; // Increment counter`
Remove obvious comments, keep complex logic explanations

### Comments - Inline Placement
**Detection Pattern**: `\S+.*//.*[^;]$` (code followed by comment on same line, excluding short explanations)
**Violation**: `maximumSize(3650). // ~10 years of daily lookups`
`// Maximum cache size: ~10 years of daily lookups\nmaximumSize(3650).`

### Comments - Historical References
**Detection Pattern**: `//.*\b(removed|added|changed|was|previously|used to)\b`
**Violation**: `// Note: ExpressionParser removed - will be added when feature is implemented`
Remove historical comments - use git history instead

## Detection Commands

```bash

# Long hardcoded strings
grep -rn '"[^"]{20,}"' --include="*.java" src/  

# Obvious Comments
grep -rn "//\s*(Increment\|Decrement\|Set\|Get\|Return)" src/
```