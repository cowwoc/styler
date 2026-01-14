# State

- **Status:** pending
- **Progress:** 0%
- **Dependencies:** []
- **Last Updated:** 2026-01-14

## Error Pattern

**12 occurrences** in Spring Framework 6.2.1

Error: `Expected RIGHT_BRACE but found LINE_COMMENT`

Example code:
```java
case "equals" -> (proxy == args[0]); // Only consider equal when proxies are identical.
```

The parser fails when a switch expression arm has a comment after the semicolon
but before the closing brace or next case.
