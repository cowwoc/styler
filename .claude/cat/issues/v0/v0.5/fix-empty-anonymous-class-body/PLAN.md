# Plan: fix-empty-anonymous-class-body

## Problem

Parser fails with "Unexpected token in member declaration: RIGHT_BRACE" when parsing an anonymous
class that has only a comment (no actual members) in its body.

## Reproduction Code

```java
return new WebMvcConfigurer() {
    // ...
};
```

Error: `ParseError[line=30, column=3, position=1116, message="Unexpected token in member declaration: RIGHT_BRACE"]`

## Expected vs Actual

- **Expected:** Empty anonymous class body with comment parses successfully
- **Actual:** Parser fails with "Unexpected token in member declaration: RIGHT_BRACE"

## Root Cause

The class body parser expects at least one member declaration or immediately sees a closing brace.
When the body contains only a comment, the comment is consumed but then the parser finds `}` where
it expects a member declaration start (modifier, type, identifier, etc.).

## Fix Approach Outlines

### Conservative

Add explicit check for RIGHT_BRACE after consuming comments in class body parsing loop.
- **Risk:** LOW
- **Tradeoff:** Targeted fix with minimal code changes

### Balanced

Refactor class body parsing to properly handle empty bodies with or without comments.
- **Risk:** MEDIUM
- **Tradeoff:** Cleaner solution but more code changes

### Aggressive

Generalize comment handling across all body parsing (class, enum, interface, record).
- **Risk:** HIGH
- **Tradeoff:** May affect other edge cases

## Selected Approach

Conservative

## Detailed Fix

### Risk Assessment

- **Risk Level:** LOW
- **Regression Risk:** LOW - targeted fix in class body parsing
- **Mitigation:** Add specific test cases for empty anonymous class with comment

### Execution Steps

1. **Write failing test:**
   - Create test for `new Interface() { // comment }` pattern
   - Verify: Test fails with current parser

2. **Locate class body parsing:**
   - Find where member declarations are parsed in class body
   - Identify the loop that expects members or closing brace

3. **Add comment-aware check:**
   - After consuming any leading comments in body loop
   - Check for RIGHT_BRACE before trying to parse member declaration
   - If RIGHT_BRACE found, break the loop cleanly

4. **Run tests:**
   - Verify: New test passes
   - Verify: All existing tests still pass
   - Verify: Spring Boot validation passes with 0 failures
