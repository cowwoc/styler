# Task Plan: fix-lambda-parameter-parsing

## Objective

Fix parser to correctly handle multi-parameter lambda expressions in method arguments.

## Problem Analysis

**Error**: "Expected RIGHT_PARENTHESIS but found COMMA" (318 occurrences in Spring Framework)

The parser treats `(a, b)` as a parenthesized expression and fails when it sees a comma.
Lambda parameter lists like `(actual, compiled) -> { ... }` are not being recognized.

**Root cause**: In `parsePrimary()`, when the parser sees `(`, it immediately calls
`parseParenthesizedExpression()` without checking if this is a lambda parameter list.

## Example Failing Code

```java
testCompiledResult(targetClassName, generatedCode, TestBeanWithPublicField.class,
    (actual, compiled) -> {
        actual.accept(instance);
        assertThat(compiled.getSourceFile()).contains("instance.age = 123");
    });
```

## Tasks

1. [ ] Implement `isLambdaParameterList()` lookahead in `parsePrimary()`
2. [ ] Handle lambda vs cast disambiguation: `(Type) x ->` vs `(x) ->`
3. [ ] Support lambda parameter lists with types: `(String a, int b) -> ...`
4. [ ] Add tests for multi-parameter lambdas in method arguments
5. [ ] Verify against Spring Framework error cases

## Technical Approach

When seeing `(` in `parsePrimary()`:
1. Check if followed by `)` + `->` (no-param lambda)
2. Check if followed by `IDENTIFIER` + `,` or `IDENTIFIER` + `)` + `->`
3. If lambda pattern detected, call `parseLambdaExpression()`
4. Otherwise, call `parseParenthesizedExpression()` or `parseCastExpression()`

## Verification

- [ ] `(a, b) -> expr` parses correctly
- [ ] `(String a, int b) -> expr` parses correctly
- [ ] `() -> expr` still works
- [ ] `(expr)` parenthesized expressions still work
- [ ] `(Type) expr` casts still work
- [ ] Spring Framework error count reduced

