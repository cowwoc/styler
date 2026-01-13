# Task State: fix-switch-default-case-parsing

## Status
status: pending
progress: 0%

## Dependencies
- fix-switch-expression-case-parsing (related - may share code)

## Error Pattern

**40 occurrences** in Spring Framework 6.2.1

Error: `Expected COLON but found SEMICOLON`

## Root Cause

Parser fails on switch expression default case with arrow and semicolon:

```java
return switch (database) {
    case DB2 -> DB2Dialect.class;
    case DERBY -> DerbyDialect.class;
    default -> null;  // <-- Parser expects : but finds ;
};
```

The arrow-style switch expressions are handled, but termination with null or
simple expressions followed by semicolon is not.

---
*Pending task - see PLAN.md*
