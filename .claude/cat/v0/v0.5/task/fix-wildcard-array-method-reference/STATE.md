# State

- **Status:** completed
- **Progress:** 100%
- **Resolution:** implemented
- **Dependencies:** []
- **Last Updated:** 2026-01-15
- **Completed:** 2026-01-15

## Error Pattern

**5 occurrences** in Spring Framework 6.2.1

Error: `Unexpected token in expression: QUESTION_MARK`

Example code:
```java
this.classes = classes.toArray(Class<?>[]::new);
return completedInterfaces.toArray(Class<?>[]::new);
```

The parser doesn't handle array creation method references with wildcard generic types.
