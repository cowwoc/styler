# State

- **Status:** pending
- **Progress:** 0%
- **Dependencies:** []
- **Last Updated:** 2026-01-14

## Error Pattern

**2 occurrences** in Spring Framework 6.2.1

Error: `Expected IDENTIFIER but found AT_SIGN`

Example code:
```java
enum InstrumentedMethod {
    /**
     * Documentation
     */
    @SuppressWarnings("NullAway")
    CLASS_GETFIELD(Class.class, "getField", ...),

    @Deprecated(since = "6.0.5")
    SOME_OTHER_CONSTANT(...),
```

Annotations on enum constants aren't being parsed correctly.
