# Plan: extract-expr-cast-lambda-detection

## Objective
Extract cast expression and lambda detection methods from Parser.java to ExpressionParser.java.

## Parent Task
Decomposed from: extract-expression-parser (sequence 1 of 6)

## Scope

### Methods to Extract (~270 lines)

From Parser.java lines 969-1240:

| Method | Lines | Purpose |
|--------|-------|---------|
| canStartUnaryExpressionNotPlusMinus | 969-978 | Cast disambiguation helper |
| canStartUnaryExpression | 989-992 | Cast disambiguation helper |
| tryCastExpression | 1017-1130 | Attempts cast expression parsing |
| parseCastOperand | 1143-1157 | Parses cast operand |
| lookaheadIsArrow | 1165-1168 | Lambda detection |
| isLambdaExpression | 1177-1240 | Detects lambda by scanning for `) ->` |

### Prerequisites
- create-parser-access-interface completed (ParserAccess interface exists)
- ExpressionParser skeleton exists

**Note:** This task can run in parallel with other extract-expr-* subtasks.

### Implementation Steps

1. Copy methods to ExpressionParser.java
2. Transform field accesses:
   - `arena` → `arena()`
   - `position` → `position()`
   - `position = X` → `setPosition(X)`
   - `tokens` → `tokens()`
3. Transform method calls to use `parser.` prefix for ParserAccess methods
4. Add method stubs in Parser.java that delegate to ExpressionParser
5. Verify compilation

## Verification

```bash
./mvnw compile -pl parser -q
```

## Acceptance Criteria
- [ ] 6 methods extracted to ExpressionParser
- [ ] Parser compiles successfully
- [ ] No public API changes
