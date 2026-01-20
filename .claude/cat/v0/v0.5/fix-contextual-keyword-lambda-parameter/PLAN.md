# Plan: fix-contextual-keyword-lambda-parameter

## Problem
Parser fails when contextual keywords (`module`, `sealed`, `permits`, etc.) are used as lambda
parameter names in method call argument contexts. Simple lambdas work but lambdas inside method
arguments fail.

## Satisfies
- None (validation gate bug)

## Reproduction Code
```java
// This fails
modules.forEach(module -> registerModule(module));

// This works
modules.forEach(x -> registerModule(x));
```

## Expected vs Actual
- **Expected:** Parses successfully, `module` recognized as valid identifier for lambda parameter
- **Actual:** `Expected ) but found ARROW` error - parser doesn't see `module` as valid parameter name

## Root Cause
Contextual keywords are valid identifiers in most contexts per JLS. However, in method argument
position, the parser may be checking for strict identifier rather than allowing contextual keywords.
The lambda detection logic needs to recognize contextual keywords as valid parameter names.

## Fix Approach Outlines

### Conservative
Add explicit check for contextual keywords in lambda parameter detection within method arguments.
- **Risk:** LOW
- **Tradeoff:** May need to add checks in multiple places

### Balanced
Update the identifier validation used during lambda detection to consistently allow contextual keywords.
- **Risk:** MEDIUM
- **Tradeoff:** Need to ensure it doesn't over-match

### Aggressive
Refactor contextual keyword handling to have a single "is valid identifier" check used everywhere.
- **Risk:** HIGH
- **Tradeoff:** Large refactor, many touch points
