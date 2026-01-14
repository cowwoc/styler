# State

- **Status:** pending
- **Progress:** 0%
- **Dependencies:** [fix-old-style-switch-case-label]
- **Last Updated:** 2026-01-14

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
