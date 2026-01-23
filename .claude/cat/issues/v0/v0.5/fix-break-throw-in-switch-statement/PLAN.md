# Task Plan: fix-break-throw-in-switch-statement

## Objective

Fix parsing of break and throw statements inside traditional switch cases.

## Problem Analysis

Traditional switch statements have break and throw as statements:
```java
switch (x) {
    case 1:
        doSomething();
        break;
    case 2:
        throw new Exception();
}
```

These are being parsed as expressions instead of statements.
This is likely related to the old-style switch case parsing issue.

## Affected Files

- `spring-expression/.../Tokenizer.java`
- `spring-expression/.../Operator.java`
- `spring-webmvc/.../ViewControllerBeanDefinitionParser.java`
- `spring-websocket/.../ConcurrentWebSocketSessionDecorator.java`
- `spring-web/.../RfcUriParser.java`

## Approach

Ensure switch statement parsing correctly handles statement context
for case bodies, allowing break/throw as statements.

## Execution Steps

1. Add test cases for switch with break and throw
2. Review switch statement vs expression distinction
3. Fix statement parsing in case body context
4. Verify all affected files parse correctly

## Success Criteria

- [ ] `break;` inside switch case parses correctly
- [ ] `throw new X();` inside switch case parses correctly
- [ ] All 5 Spring Framework files parse correctly
