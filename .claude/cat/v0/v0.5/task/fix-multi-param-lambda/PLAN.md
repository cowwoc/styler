# Task Plan: fix-multi-param-lambda

## Objective

Fix parser to handle **multi-parameter lambda expressions** `(param1, param2) -> body`.

## Problem Analysis (UPDATED)

**Error:** `Expected RIGHT_PARENTHESIS but found COMMA`
**Occurrences:** 344 in Spring Framework 6.2.1

**Root cause:** Parser does NOT support multi-parameter lambdas.

```java
// ALL FAIL - multi-param lambda anywhere
BiConsumer<A,B> c = (a, b) -> doSomething();
forEach((a, b) -> process(a, b));
testCompiledResult(arg1, arg2, (actual, compiled) -> { ... });
```

**Why it fails:** In `parseParenthesizedOrLambda()` (line ~2500):
1. `() -> expr` is handled (empty parens check)
2. Cast expressions `(Type) operand` are handled
3. For `(a, b) -> ...`: calls `parseExpression()` which parses `a`, then expects `)` but finds `,`

## Approach

Add lookahead in `parseParenthesizedOrLambda()` to detect multi-param lambda pattern:
`(IDENTIFIER COMMA ... ) ARROW`

Before calling `parseExpression()`:
1. Check if current token is IDENTIFIER
2. Look ahead for COMMA pattern suggesting multi-param lambda
3. If detected, parse lambda parameters then expect ARROW and body

## Execution Steps

1. Create failing tests for multi-param lambdas
2. Add `isMultiParamLambda()` lookahead method
3. Modify `parseParenthesizedOrLambda()` to handle multi-param case
4. Parse parameter list (identifiers separated by commas)
5. Expect ARROW and call `parseLambdaBody()`
6. Verify all tests pass
7. Run Spring Framework validation

## Success Criteria

- [ ] Parser handles `(a, b) -> expr` (inferred types)
- [ ] Parser handles `(a, b) -> { block }`
- [ ] Parser handles multi-param lambda in method args
- [ ] All existing tests pass
- [ ] New test cases cover identified patterns
