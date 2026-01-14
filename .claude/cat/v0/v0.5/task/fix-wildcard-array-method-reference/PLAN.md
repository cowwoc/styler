# Task Plan: fix-wildcard-array-method-reference

## Objective

Fix parsing of method references for array creation with wildcard generic types.

## Problem Analysis

Method references can create arrays using `::new` syntax:
```java
String[]::new           // works
Class<?>[]::new         // fails - wildcard in generic
List<String>[]::new     // may fail
```

The parser handles `Class::new` and `Type[]::new` but fails when
the type has wildcard generics: `Class<?>[]::new`.

## Affected Files

- `spring-context/.../ReflectiveProcessorBeanFactoryInitializationAotProcessor.java`
- `spring-context/.../ReflectiveProcessorAotContributionBuilder.java`
- `spring-aop/.../AopProxyUtils.java`
- `spring-web/.../RestTemplate.java`
- Plus 1 more file

## Approach

Update method reference parsing to handle generic types with wildcards
in array creation expressions.

## Execution Steps

1. Add test case for `Class<?>[]::new`
2. Review method reference parsing for array types
3. Add wildcard generic handling in this context
4. Verify all 5 affected files parse correctly

## Success Criteria

- [ ] `Class<?>[]::new` parses correctly
- [ ] `List<? extends T>[]::new` parses correctly
- [ ] All 5 Spring Framework files with this pattern parse
