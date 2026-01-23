# Task Plan: fix-annotation-on-enum-constant

## Objective

Fix parsing of annotations on enum constants.

## Problem Analysis

Enum constants can have annotations:
```java
enum Status {
    @Deprecated
    OLD_VALUE,

    @SuppressWarnings("unchecked")
    CURRENT_VALUE(args)
}
```

The parser expects identifier after comma (or at start) but encounters AT_SIGN
for the annotation.

## Affected Files

- `spring-core-test/.../InstrumentedMethod.java`
- `spring-web/.../HttpStatus.java`

## Approach

Update enum constant parsing to handle optional annotations before
each constant identifier.

## Execution Steps

1. Add test case for annotated enum constants
2. Locate enum constant parsing in Parser.java
3. Add annotation handling before enum constant name
4. Verify both affected files parse correctly

## Success Criteria

- [ ] `@Annotation CONSTANT` parses in enum body
- [ ] Multiple annotations on constants work
- [ ] Both Spring Framework files parse correctly
