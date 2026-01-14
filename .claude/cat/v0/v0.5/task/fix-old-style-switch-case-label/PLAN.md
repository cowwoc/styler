# Task Plan: fix-old-style-switch-case-label

## Objective

Fix parsing of traditional switch statements with case labels followed by blocks.

## Problem Analysis

Old-style switch statements use `case X:` labels with statement blocks:
```java
switch (tag) {
    case 18: {
        doSomething();
        break;
    }
    default: {
        handleDefault();
    }
}
```

This is being confused with switch expressions. The parser sees `case` in
expression context instead of recognizing it as a switch label.

## Affected Files

- `spring-messaging/.../Msg.java` (protobuf generated)
- `spring-messaging/.../SecondMsg.java` (protobuf generated)
- `spring-webflux/.../Msg.java`
- `spring-webflux/.../SecondMsg.java`
- `spring-web/.../Msg.java`
- `spring-web/.../SecondMsg.java`
- `spring-core/.../CodeEmitter.java`
- `spring-core/.../EmitUtils.java`

## Approach

Review switch statement parsing to ensure traditional case labels are handled
correctly, especially when followed by block statements.

## Execution Steps

1. Add test case for traditional switch with block after case label
2. Review `parseSwitchStatement()` and related methods
3. Fix case label parsing to not treat as expression
4. Verify all affected files parse correctly

## Success Criteria

- [ ] Traditional `case X: { ... }` syntax parses correctly
- [ ] `default: { ... }` syntax parses correctly
- [ ] All 8 Spring Framework files with this pattern parse
