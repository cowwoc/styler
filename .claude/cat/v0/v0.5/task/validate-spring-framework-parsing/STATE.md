# State

- **Status:** in-progress
- **Progress:** 80%
- **Resolution:** implemented
- **Dependencies:** fix-old-style-switch-fallthrough, fix-lambda-in-ternary-expression, fix-misc-expression-edge-cases
- **Last Updated:** 2026-01-17
- **Note:** Validation run completed. 17 files still failing. Need new tasks for remaining errors.

## Validation Run (2026-01-17 - Post-Dependencies)

**Result:** 99.81% success rate (8,800/8,817 files)

| Metric | Value |
|--------|-------|
| Total files | 8,817 |
| Succeeded | 8,800 |
| Failed | 17 |
| Time | 3,532ms |
| Throughput | 2,496.3 files/sec |

**Improvement from last run:** 21 → 17 errors (-4 files fixed)
**Improvement from start of v0.5:** 93.2% → 99.81% (+581 files now parse correctly)

## Remaining Errors (17 files) - New Tasks Required

| Error Type | Count | Task Status |
|------------|-------|-------------|
| Unexpected CASE in expression | 5 | fix-switch-case-in-expression-context (completed but still failing) |
| Unexpected DEFAULT in expression | 3 | fix-switch-case-in-expression-context (completed but still failing) |
| Unexpected BREAK in expression | 3 | fix-switch-case-in-expression-context (completed but still failing) |
| Unexpected THROW in expression | 2 | fix-switch-case-in-expression-context (completed but still failing) |
| Unexpected WHILE in expression | 1 | fix-switch-case-in-expression-context (completed but still failing) |
| Expected RIGHT_PARENTHESIS but found ARROW | 2 | NEW: fix-lambda-arrow-in-parenthesized-context |
| Expected SEMICOLON but found ARROW | 1 | NEW: fix-lambda-arrow-in-parenthesized-context |

### Error Categories Analysis (Investigation 2026-01-17)

**Old-Style Switch Fall-Through (14 files) - PATTERN MISMATCH DISCOVERED:**

The existing tests cover simple patterns like:
```java
switch (x) {
    case 1:
        break;  // <-- Simple case body
}
```

But the failing files have **NESTED switch statements inside case/default bodies**:
```java
default:
    switch (mode) {   // <-- Nested switch as case body
    case EQ: intOp = ...; break;
    }
```

This is a DIFFERENT parsing path than what `fix-switch-case-in-expression-context` addressed.
The nested switch after `default:` is being interpreted as an expression, not a statement.

**Root cause:** `parseOldStyleSwitchCaseBody()` may not correctly handle switch statements as the
first statement after a case label.

**Files affected:**
- CodeEmitter.java (nested switch in default)
- EmitUtils.java (nested switch in default)
- ClassReader.java (while in case)
- Tokenizer.java, Operator.java, RfcUriParser.java (break patterns in nested context)
- Msg.java, SecondMsg.java (3 copies each - protobuf generated)
- ViewControllerBeanDefinitionParser.java, ConcurrentWebSocketSessionDecorator.java (throw)

**Lambda in Parenthesized Context (3 files) - NEW PATTERN:**

The existing `fix-lambda-arrow-in-parenthesized-context` task addressed lambdas with annotated generic
type parameters. But these 3 files have a DIFFERENT pattern - simple lambdas in method arguments:
```java
Mono.usingWhen(
    getConnection(),
    this::populate,
    connection -> release(connection),  // <-- Simple lambda, not annotated
    ...
)
```

The parser sees `connection` and expects `)` for the method call, but finds `->`.

**Files affected:**
- DatabasePopulator.java
- Jackson2ObjectMapperBuilder.java
- RouterFunctionsTests.java

## Previous Run (2026-01-17 - Pre-Dependencies)

**Result:** 99.76% success rate (8,796/8,817 files)

| Metric | Value |
|--------|-------|
| Total files | 8,817 |
| Succeeded | 8,796 |
| Failed | 21 |
| Time | 5,503ms |
| Throughput | 1,602.2 files/sec |

## Previous Run (2026-01-14)

**Result:** 99.03% success rate (8,731/8,817 files)

| Metric | Value |
|--------|-------|
| Total files | 8,817 |
| Succeeded | 8,731 |
| Failed | 86 |
| Time | 4,014ms |
| Throughput | 2,196.6 files/sec |

## Previous Run (2026-01-13)

**Result:** 93.2% success rate (8,219/8,817 files)

## Conclusion

**17 errors remain after investigation.** These are NOT the same errors that previous tasks addressed.
The previous tasks were incorrectly marked as complete because their tests didn't cover the actual patterns.

**Status:** Two distinct new error patterns discovered that require new tasks:
1. **Nested switch in case/default body** (14 files) - different parsing path than tested
2. **Simple lambda in method argument** (3 files) - different from annotated generic lambda

**Recommendation:** Create targeted tasks for these specific patterns rather than re-opening completed tasks.

---
*Investigation completed 2026-01-17. Previous tasks covered related but different patterns.*
