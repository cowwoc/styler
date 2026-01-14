# State

- **Status:** pending
- **Progress:** 0%
- **Dependencies:** []
- **Last Updated:** 2026-01-14

## Error Pattern

**~10 occurrences** in Spring Framework 6.2.1

Errors:
- `Expected RIGHT_PARENTHESIS but found IDENTIFIER`
- `Expected RIGHT_PARENTHESIS but found ARROW`

Example code:
```java
context.addApplicationListener((MyEvent event) -> seenEvents.add(event));
connection -> ConnectionFactoryUtils.releaseConnection(connection, connectionFactory)
return (S s) -> { ... };
```

Lambda expressions with explicit type parameters aren't parsing correctly when
passed as method arguments.
