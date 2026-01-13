# Task State: fix-cast-lambda-in-method-args

## Status
status: pending
progress: 0%

## Dependencies
- fix-cast-lambda-expression (must complete first - addresses simpler case)

## Error Pattern

**344 occurrences** in Spring Framework 6.2.1

Error: `Expected RIGHT_PARENTHESIS but found COMMA`

## Root Cause

Parser fails when cast lambda appears as argument in method calls with multiple args:

```java
Arguments.of((Runnable) () -> { ... }, secondArg)
```

The existing `fix-cast-lambda-expression` handles simple cast+lambda, but not when
the result is passed as a method argument followed by more arguments.

---
*Pending task - see PLAN.md*
