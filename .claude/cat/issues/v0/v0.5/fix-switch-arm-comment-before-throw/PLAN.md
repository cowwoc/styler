# Plan: fix-switch-arm-comment-before-throw

## Problem
Parser fails when a comment appears between `->` and `throw` in switch expression arms. The parser
expects an expression after `->` but encounters a comment followed by `throw` (which is a statement).

## Satisfies
- None (validation gate bug)

## Reproduction Code
```java
var x = switch (value) {
    default ->
        // Should never happen...
        throw new IllegalStateException("Unexpected: " + value);
};
```

## Expected vs Actual
- **Expected:** Parses successfully, `throw` treated as expression in switch arm context
- **Actual:** `Unexpected token: THROW` error at the throw keyword

## Root Cause
After parsing `->` in switch arm context, the parser skips comments but then fails to recognize
`throw` as valid. The recent fix (bad20656) handled lambda detection after comments in method
arguments, but switch arms need similar handling for throw-as-expression.

## Fix Approach Outlines

### Conservative
Patch the switch arm parsing to explicitly skip comments before checking for throw.
- **Risk:** LOW
- **Tradeoff:** May not generalize to other statement-as-expression cases

### Balanced
Ensure comment skipping is consistently applied before expression parsing in switch arm context.
- **Risk:** MEDIUM
- **Tradeoff:** Need to identify all code paths

### Aggressive
Refactor switch arm expression parsing to use a common pattern with lambda body parsing.
- **Risk:** HIGH
- **Tradeoff:** Larger change surface, potential regressions
