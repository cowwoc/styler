# Task Plan: add-qualified-class-instantiation

## Objective

Add qualified class instantiation for inner classes: `outer.new Inner()`.

## Tasks

1. Add NEW token handling in parseDotExpression()
2. Extract parseDotExpression() helper method

## Verification

- [ ] `outer.new Inner()` parses
- [ ] Chained calls and anonymous classes work

