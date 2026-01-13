# Task Plan: add-yield-statement-support

## Objective

Add JDK 14+ `yield value;` statement support in switch expressions.

## Tasks

1. Add YIELD_STATEMENT to NodeType enum
2. Create parseYieldStatement() method
3. Add YIELD case in parseStatement()
4. Update ContextDetector exhaustive switch

## Verification

- [ ] `yield value;` parses in switch expressions
- [ ] All yield contexts tested

