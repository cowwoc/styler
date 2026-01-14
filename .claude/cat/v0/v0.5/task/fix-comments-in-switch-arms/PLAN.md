# Task Plan: fix-comments-in-switch-arms

## Objective

Fix parsing of comments after switch expression arms.

## Problem Analysis

Switch expression arms can have trailing comments:
```java
switch (name) {
    case "equals" -> (proxy == args[0]); // comment here
    case "hashCode" -> System.identityHashCode(proxy);
    default -> -1; // error comment
}
```

The parser expects RIGHT_BRACE after the expression but encounters LINE_COMMENT.
Comments should be allowed between switch arms.

## Affected Files

- `spring-orm/.../LocalSessionFactoryBuilder.java`
- `spring-orm/.../HibernateTemplate.java`
- `spring-core/.../TypeUtils.java`
- Plus 9 more files

## Approach

Update switch expression parsing to skip comments between arms and before
the closing brace.

## Execution Steps

1. Add test case for switch expression with trailing comments
2. Locate switch expression parsing in Parser.java
3. Add comment handling between switch arms
4. Verify all 12 affected files parse correctly

## Success Criteria

- [ ] Switch expressions with trailing comments parse correctly
- [ ] All 12 Spring Framework files with this pattern parse
