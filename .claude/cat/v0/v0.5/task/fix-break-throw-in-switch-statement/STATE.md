# State

- **Status:** completed
- **Progress:** 100%
- **Resolution:** duplicate
- **Duplicate Of:** pre-CAT implementation (commit a9659516, 2025-12-27)
- **Dependencies:** [fix-old-style-switch-case-label]
- **Last Updated:** 2026-01-15
- **Completed:** 2026-01-15

## Error Pattern

**4 occurrences** in Spring Framework 6.2.1

Errors:
- `Unexpected token in expression: BREAK`
- `Unexpected token in expression: THROW`

Example code:
```java
switch (ch) {
    case '+':
        pushCharToken(TokenKind.PLUS);
        break;  // <-- error here
    case '-':
        throw new IllegalStateException("...");  // <-- error here
}
```

Break and throw statements inside switch cases are incorrectly parsed.

## Resolution Notes

This functionality was already implemented before CAT initialization:

1. **Implementation:** `isColonStyleSwitch()` and `parseSwitchStatementBody()` methods
   in Parser.java (commit a9659516, 2025-12-27)

2. **Tests:** `testSwitchStatementWithBreakAfterMethodCall()` and
   `testSwitchStatementWithThrowInCase()` in StatementParserTest.java

3. **Verification:** All scenarios from this task's PLAN.md pass with existing implementation.
