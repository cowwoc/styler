# Task Plan: fix-comments-before-implements

## Objective

Fix parsing of class declarations with comments between extends and implements clauses.

## Problem Analysis

Class declarations can have comments between extends and implements:
```java
public class Foo extends Base  // comment
        implements Interface {
```

The parser expects either LEFT_BRACE or IMPLEMENTS after extends clause
but encounters LINE_COMMENT first.

## Affected Files

- `spring-core/.../LinkedMultiValueMap.java`
- `spring-context/.../AutoProxyCreatorTests.java`

## Approach

Update class declaration parsing to skip comments between
extends clause and implements keyword.

## Execution Steps

1. Add test case for class with comment before implements
2. Locate class declaration parsing
3. Add comment handling before IMPLEMENTS keyword
4. Verify both affected files parse correctly

## Success Criteria

- [ ] Class with comment between extends and implements parses
- [ ] Both Spring Framework files parse correctly
