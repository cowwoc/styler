# Task Plan: add-flexible-constructor-bodies

## Objective

Verify JEP 513 (Flexible Constructor Bodies - JDK 25) support.

## Tasks

1. Analyze existing parser support for statements before super()/this()
2. Add verification tests if already supported
3. Implement support if needed

## Verification

- [ ] Statements before super() parse correctly
- [ ] Control flow (if-else, try-catch) before super() works
- [ ] Statements before this() work

