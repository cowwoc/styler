# Task Plan: fix-final-in-pattern-matching

## Objective

Support `final` modifier in instanceof pattern matching.

## Problem Analysis

Java 16+ allows `final` in pattern variables:
```java
if (obj instanceof final String s) { ... }
if (obj instanceof final Map<?, ?> map) { ... }
```

The `final` keyword makes the pattern variable effectively final,
preventing reassignment in the body.

Current parser expects identifier after type in pattern matching
but encounters FINAL keyword.

## Affected Files

- `spring-webmvc/.../AbstractMultiCheckedElementTag.java`

## Approach

Update instanceof pattern parsing to allow optional `final` modifier
before the type.

## Execution Steps

1. Add test case for `instanceof final Type name`
2. Locate instanceof pattern parsing in Parser.java
3. Add handling for optional FINAL before type
4. Verify affected file parses correctly

## Success Criteria

- [ ] `instanceof final Type var` parses correctly
- [ ] `instanceof final Map<?, ?> map` parses correctly
- [ ] Spring Framework file with this pattern parses
