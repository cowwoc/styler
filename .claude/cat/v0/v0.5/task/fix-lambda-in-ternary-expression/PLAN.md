# Task Plan: fix-lambda-in-ternary-expression

## Objective

Fix parser to handle lambda expressions appearing as the false branch of ternary (conditional)
expressions.

## Problem Analysis

The parser fails on patterns like:
```java
this.filter = (filter != null ? filter : className -> true);
```

The lambda `className -> true` appears as the false branch of the ternary. The parser expects a
RIGHT_PARENTHESIS after parsing what it thinks is the complete ternary, but encounters ARROW instead.

## Error Messages

- "Expected RIGHT_PARENTHESIS but found ARROW"
- "Expected SEMICOLON but found ARROW"

## Approach

The issue is likely in how the parser handles expression precedence:
1. Ternary `? :` has lower precedence than lambda `->`
2. When parsing `condition ? value : param -> body`, the parser may stop at `:` thinking
   the ternary is complete
3. Need to properly handle lambda as a valid ternary alternative

## Execution Steps

1. Write failing test cases for lambda in ternary alternative position
2. Analyze expression parsing for ternary and lambda precedence
3. Ensure ternary alternative can be a lambda expression
4. Handle edge cases: nested ternaries with lambdas, multiple parameters
5. Verify fix against all 5 affected files

## Success Criteria

- [ ] All 5 affected Spring Framework files parse successfully
- [ ] Pattern `condition ? value : param -> body` parses correctly
- [ ] Nested ternaries with lambda alternatives work
- [ ] Existing lambda and ternary tests still pass
