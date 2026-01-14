# State

- **Status:** pending
- **Progress:** 0%
- **Dependencies:** []
- **Last Updated:** 2026-01-14

## Error Pattern

**1 occurrence** in Spring Framework 6.2.1

Error: `Expected identifier but found FINAL`

Example code:
```java
else if (itemsObject instanceof final Map<?, ?> optionMap) {
    // use optionMap
}
```

Pattern matching with `final` modifier (Java 16+) isn't recognized.
