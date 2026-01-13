# Task Plan: add-explicit-type-arguments

## Objective

Add explicit type arguments on method/constructor calls.

## Tasks

1. Add type argument parsing in parsePostfix() after DOT
2. Add type argument parsing after DOUBLE_COLON
3. Add type argument parsing in parseNewExpression()

## Verification

- [ ] `Collections.<String>emptyList()` parses
- [ ] `List::<String>of` parses
- [ ] `new <T>Constructor()` parses

