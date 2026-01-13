# Task Plan: add-primitive-type-patterns

## Objective

Parse JEP 507 primitive type patterns in switch expressions.

## Tasks

1. Add tryParsePrimitiveTypePattern() method
2. Modify parseCaseLabelElement() to check for primitive patterns
3. Support guard expressions
4. Support unnamed pattern variables

## Verification

- [ ] `case int i ->` parses
- [ ] `obj instanceof int i` works
- [ ] Guard expressions work
- [ ] Unnamed patterns (`_`) work

