# Plan: extract-expr-creation

## Objective
Extract object/array creation and switch expression methods from Parser.java to ExpressionParser.java.

## Parent Task
Decomposed from: extract-expression-parser (sequence 5 of 6)

## Scope

### Methods to Extract (~200 lines)

**Object/Array Creation (lines 3314-3439):**

| Method | Lines | Purpose |
|--------|-------|---------|
| parseNewExpression | 3314-3332 | Dispatches array/object creation |
| parseArrayCreation | 3347-3377 | Array creation expressions |
| parseObjectCreation | 3379-3400 | Object instantiation |
| parseArrayInitializer | 3402-3439 | Array initializer syntax |

**Switch Expression (line 2070):**

| Method | Lines | Purpose |
|--------|-------|---------|
| parseSwitchExpression | 2070-2145 | Switch expressions |

### Prerequisites
- create-parser-access-interface completed (ParserAccess interface exists)
- ExpressionParser skeleton exists

**Note:** This task can run in parallel with other extract-expr-* subtasks.

### Implementation Steps

1. Copy methods to ExpressionParser.java
2. Transform field accesses
3. parseArrayInitializer is recursive for nested arrays
4. Add delegation stubs in Parser.java
5. Verify compilation

## Verification

```bash
./mvnw compile -pl parser -q
```

## Acceptance Criteria
- [ ] 5 methods extracted to ExpressionParser
- [ ] Array initializer handles nested cases
- [ ] Parser compiles successfully
