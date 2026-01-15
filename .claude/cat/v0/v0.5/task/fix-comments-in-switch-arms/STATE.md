# State

- **Status:** completed
- **Progress:** 100%
- **Resolution:** duplicate
- **Duplicate Of:** v0.5-fix-old-style-switch-case-label
- **Dependencies:** []
- **Completed:** 2026-01-15

## Duplicate Resolution

This task was identified as a duplicate of `fix-old-style-switch-case-label` (commit 2c51093).

The fix in commit 2c51093 added `parseComments()` calls to `parseSwitchExpression()` after processing
each case block. This addresses both:
1. Traditional colon-style case labels with trailing comments
2. Arrow-style switch expression arms with trailing comments

The test `testSwitchExpressionWithColonAndTrailingComments` in `SwitchExpressionParserTest.java`
verifies arrow-syntax switch expressions with trailing comments parse correctly.

## Original Error Pattern

**12 occurrences** in Spring Framework 6.2.1

Error: `Expected RIGHT_BRACE but found LINE_COMMENT`

Example code:
```java
case "equals" -> (proxy == args[0]); // Only consider equal when proxies are identical.
```
