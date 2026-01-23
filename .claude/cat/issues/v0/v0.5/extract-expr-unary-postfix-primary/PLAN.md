# Plan: extract-expr-unary-postfix-primary

## Objective
Extract unary, postfix, and primary expression methods from Parser.java to ExpressionParser.java.

## Parent Task
Decomposed from: extract-expression-parser (sequence 3 of 6)

## Scope

### Methods to Extract (~500 lines)

**Unary and Postfix (lines 2909-3119):**

| Method | Lines | Purpose |
|--------|-------|---------|
| parseUnary | 2909-2931 | Unary prefix operators |
| parsePostfix | 2933-3071 | Method calls, field access, array access, method refs |
| parseDotExpression | 3079-3119 | Field access, class literal, qualified this/super/new |
| parseArrayAccessOrClassLiteral | 3129-3156 | Array access or array type class literal |

**Primary Expressions (lines 3158-3299):**

| Method | Lines | Purpose |
|--------|-------|---------|
| parsePrimary | 3158-3242 | Primary expression entry point |
| parseLiteralExpression | 3252-3267 | Literal nodes |
| parsePrimitiveClassLiteral | 3276-3299 | Primitive class literals |

### Prerequisites
- create-parser-access-interface completed (ParserAccess interface exists)
- ExpressionParser skeleton exists

**Note:** This task can run in parallel with other extract-expr-* subtasks.

### Implementation Steps

1. Copy methods to ExpressionParser.java
2. Transform field accesses
3. parsePostfix is the largest method (~140 lines) - handle carefully
4. Add delegation stubs in Parser.java
5. Verify compilation

## Verification

```bash
./mvnw compile -pl parser -q
```

## Acceptance Criteria
- [ ] 7 methods extracted to ExpressionParser
- [ ] parsePostfix handles all postfix operations
- [ ] Parser compiles successfully
