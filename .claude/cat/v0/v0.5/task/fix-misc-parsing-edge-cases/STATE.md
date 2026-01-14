# State

- **Status:** pending
- **Progress:** 0%
- **Dependencies:** [fix-contextual-keywords-in-expressions, fix-old-style-switch-case-label]
- **Last Updated:** 2026-01-14

## Error Pattern

**~10 occurrences** in Spring Framework 6.2.1

Miscellaneous parsing edge cases:

| Error | Count | Example |
|-------|-------|---------|
| Block comment in type declaration | 1 | `Map/* comment */field` |
| Multiple top-level classes | 1 | Second class after package-private class |
| RIGHT_BRACE in member declaration | 1 | Enum with complex initializer |
| DOT after typed lambda | 2 | `(byte[] b) -> expr` |
| LESS_THAN in generic context | 2 | Complex nested generics |

These are less common patterns that may share root causes with other issues.
