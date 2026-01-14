# State

- **Status:** completed
- **Progress:** 100%
- **Dependencies:** [fix-lambda-parameter-parsing, fix-cast-lambda-expression]
- **Last Updated:** 2026-01-14
- **Completed:** 2026-01-14

## Error Pattern

**16 occurrences** in Spring Framework 6.2.1

Error: `Expected SEMICOLON but found ARROW`

## Root Cause

When `parseAssignment()` is called recursively for the right-hand side of an assignment,
it calls `parseTernary()` directly, skipping the lambda expression check that exists in
`parseExpression()`. This causes the parser to fail when a lambda appears as the RHS of
an assignment like `this.sessionManager = exchange -> Mono.just(session)`.

## Solution

Changed line 2624 in Parser.java from `parseAssignment()` to `parseExpression()` so the
RHS of assignments goes through the lambda lookahead check.

## Tests Added

- `shouldParseLambdaAsAssignmentRhs` - single-param lambda as field assignment RHS
- `shouldParseNoParamLambdaAsAssignmentRhs` - no-param lambda as field assignment RHS
- `shouldParseLambdaInNestedAssignment` - lambda in nested assignment (a = b = x -> expr)
