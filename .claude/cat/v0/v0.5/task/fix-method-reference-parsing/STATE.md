# State

- **Status:** completed
- **Progress:** 100%
- **Resolution:** implemented
- **Dependencies:** []
- **Completed:** 2026-01-14

## Error Pattern (UPDATED 2026-01-14)

Investigation confirmed real parsing bugs exist:

**Bug 1: Generic constructor references**
```java
Supplier<List<String>> s = ArrayList<String>::new;
// Error: "Unexpected token in expression: DOUBLE_COLON"
```

**Bug 2: Method references on parameterized types**
```java
Function<List<String>, Integer> f = List<String>::size;
// Error: "Unexpected token in expression: COMMA"
```

## Root Cause

Parser does not handle method references (`::`) when the target type has type arguments.
Works: `String::length`, `String[]::new`
Fails: `ArrayList<String>::new`, `List<String>::size`
