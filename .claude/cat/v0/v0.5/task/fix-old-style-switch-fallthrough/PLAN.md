# Task Plan: fix-old-style-switch-fallthrough

## Objective

Fix parser to handle old-style switch statements with fall-through patterns that don't use braces.

## Problem Analysis

The parser fails on patterns like:
```java
switch (mode) {
case GE: jumpmode = LT; break;
case LE: jumpmode = GT; break;
}
```

And nested switches with fall-through:
```java
switch (mode) {
case EQ: intOp = Constants.IF_ICMPEQ; break;
case GE: swap(); /* fall through */
case LT: intOp = Constants.IF_ICMPLT; break;
}
```

The parser sees `case` or `break` as unexpected tokens in expression context because it's not
properly recognizing the switch statement block structure.

## Error Messages

- "Unexpected token in expression: CASE"
- "Unexpected token in expression: DEFAULT"
- "Unexpected token in expression: BREAK"
- "Unexpected token in expression: THROW"
- "Unexpected token in expression: WHILE"

## Approach

Investigate how the parser handles switch statement blocks and case labels. The issue may be:
1. Case labels without braces being parsed as expressions
2. Fall-through comments confusing the parser
3. Nested switch statements within case arms

## Execution Steps

1. Write failing test cases for the problematic patterns
2. Analyze parser behavior on switch statement blocks
3. Identify where case labels are being incorrectly parsed
4. Fix switch statement parsing to handle braceless case arms
5. Verify fix against all 14 affected files

## Success Criteria

- [ ] All 14 affected Spring Framework files parse successfully
- [ ] Old-style switch patterns with fall-through parse correctly
- [ ] Nested switch statements in case arms parse correctly
- [ ] Existing switch expression tests still pass
