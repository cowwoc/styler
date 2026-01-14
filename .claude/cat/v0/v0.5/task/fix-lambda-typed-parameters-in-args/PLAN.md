# Task Plan: fix-lambda-typed-parameters-in-args

## Objective

Fix parsing of lambda expressions with typed parameters inside method arguments.

## Problem Analysis

Lambda expressions with explicit parameter types fail in some contexts:
```java
// These should all parse correctly
listener((MyEvent event) -> handle(event));
Mono.using(() -> conn, conn -> use(conn), conn -> close(conn));
return (S s) -> { doSomething(s); };
```

The parser may be misinterpreting `(Type name)` as a cast expression instead
of lambda parameters in certain contexts.

## Affected Files

- `spring-context/.../ApplicationContextEventTests.java`
- `spring-r2dbc/.../DatabasePopulator.java`
- `spring-core/.../Converter.java`
- `spring-core/.../ByteArrayEncoder.java`
- `spring-jms/.../SimpleMessageListenerContainerTests.java`
- Plus 5 more files

## Approach

Review lambda detection logic to ensure typed parameters are correctly
identified in method argument context.

## Execution Steps

1. Add test cases for lambda with typed params in method args
2. Review `parseExpression()` lambda detection
3. Fix cast vs lambda disambiguation for this pattern
4. Verify all affected files parse correctly

## Success Criteria

- [ ] `method((Type param) -> expr)` parses correctly
- [ ] `method(param -> expr, (Type p) -> expr2)` parses correctly
- [ ] All ~10 Spring Framework files with this pattern parse
