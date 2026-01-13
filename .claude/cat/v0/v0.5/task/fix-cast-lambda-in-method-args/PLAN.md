# Task Plan: fix-cast-lambda-in-method-args

## Objective

Fix parser to handle cast expressions followed by lambda expressions when used as
method arguments with additional arguments following.

## Problem Analysis

**Error:** `Expected RIGHT_PARENTHESIS but found COMMA`
**Occurrences:** 344 in Spring Framework 6.2.1

The parser currently handles `(Type) () -> body` but fails when this construct appears
within method arguments:

```java
// FAILS: Cast lambda as first argument, more args follow
Arguments.of((Runnable) () -> doSomething(), MethodReference.of(Class.class, "method"))

// FAILS: Cast lambda with block body
Arguments.of((Runnable) () -> {
    try { Class.forName("java.lang.String"); }
    catch (Exception e) { throw new RuntimeException(e); }
}, MethodReference.of(Class.class, "forName"))
```

## Approach

1. Analyze existing `fix-cast-lambda-expression` implementation
2. Identify where argument list parsing conflicts with cast+lambda
3. Ensure lambda body parsing terminates correctly when followed by comma
4. Add test cases for various scenarios

## Execution Steps

1. Research existing cast+lambda handling in Parser.java
2. Create test cases that reproduce the error
3. Fix the parser logic for argument context
4. Verify all tests pass
5. Re-run Spring Framework validation to measure improvement

## Success Criteria

- [ ] Parser handles `(Type) () -> expr, nextArg` in method calls
- [ ] Parser handles `(Type) () -> { block }, nextArg` in method calls
- [ ] All existing tests pass
- [ ] New test cases cover identified patterns
