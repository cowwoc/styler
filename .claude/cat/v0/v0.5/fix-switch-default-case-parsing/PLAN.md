# Task Plan: fix-switch-default-case-parsing

## Objective

Fix parser to handle switch expression default case with arrow syntax and simple
expressions like `null`.

## Problem Analysis

**Error:** `Expected COLON but found SEMICOLON`
**Occurrences:** 40 in Spring Framework 6.2.1

Example from HibernateJpaVendorAdapter.java:

```java
return switch (database) {
    case DB2 -> DB2Dialect.class;
    case MYSQL -> MySQLDialect.class;
    default -> null;
};
```

The parser correctly handles arrow-style cases returning class literals but fails
when `default -> null;` terminates the switch.

## Approach

1. Review existing switch expression parsing (fix-switch-expression-case-parsing)
2. Identify how default case differs in parsing
3. Fix parser to handle `default -> expression;` pattern
4. Add comprehensive test cases

## Execution Steps

1. Examine Parser.java switch expression handling
2. Create failing test case for `default -> null;`
3. Implement fix
4. Add tests for various default case patterns
5. Verify all tests pass

## Success Criteria

- [ ] Parser handles `default -> null;` in switch expressions
- [ ] Parser handles `default -> SomeClass.class;` patterns
- [ ] All existing tests pass
- [ ] New test cases cover default case patterns
