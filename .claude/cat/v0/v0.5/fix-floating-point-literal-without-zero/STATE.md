# State

- **Status:** completed
- **Progress:** 100%
- **Dependencies:** []
- **Last Updated:** 2026-01-14
- **Completed:** 2026-01-14

## Error Pattern

**6 occurrences** in Spring Framework 6.2.1

Error: `Unexpected token in expression: DOT`

Example code:
```java
new Person("Johannes Brahms").setSomeDouble(.0025)
```

The parser doesn't recognize `.0025` as a valid floating-point literal.
Java allows floating-point literals without a leading zero: `.5`, `.0025`, `.99`.
