# Plan: extract-expr-lambda-parsing

## Objective
Extract lambda expression parsing methods from Parser.java to ExpressionParser.java.

## Parent Task
Decomposed from: extract-expression-parser (sequence 4 of 6)

## Scope

### Methods to Extract (~200 lines)

From Parser.java lines 2558-2738:

| Method | Lines | Purpose |
|--------|-------|---------|
| parseExpression | 2558-2577 | Main entry point for expression parsing |
| parseLambdaBody | 2579-2598 | Parses lambda body (block or expression) |
| parseLambdaParameters | 2606-2611 | Dispatches typed/untyped lambda parsing |
| isTypedLambdaParameters | 2618-2661 | Detects typed vs untyped parameters |
| parseTypedLambdaParameters | 2663-2671 | Parses typed lambda parameters |
| parseTypedLambdaParameter | 2673-2686 | Parses single typed lambda parameter |
| parseUntypedLambdaParameters | 2688-2696 | Parses untyped lambda parameters |
| parseParenthesizedOrLambda | 2713-2738 | Disambiguates parens/cast/lambda |

**Assignment and Conditional (lines 2740-2784):**

| Method | Lines | Purpose |
|--------|-------|---------|
| parseAssignment | 2740-2755 | Parses assignment expressions |
| isAssignmentOperator | 2757-2766 | Checks assignment operators |
| parseTernary | 2768-2784 | Parses conditional operator |

### Prerequisites
- extract-expr-unary-postfix-primary completed
- Lambda body calls parseExpression recursively

### Implementation Steps

1. Copy methods to ExpressionParser.java
2. Transform field accesses
3. parseExpression becomes the public entry point
4. Add delegation in Parser.java: parseExpression() calls expressionParser.parseExpression()
5. Verify compilation

## Verification

```bash
./mvnw compile -pl parser -q
```

## Acceptance Criteria
- [ ] 11 methods extracted to ExpressionParser
- [ ] parseExpression is the main entry point
- [ ] Parser compiles successfully
