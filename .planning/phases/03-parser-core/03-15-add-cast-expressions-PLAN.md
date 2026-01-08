# Plan: add-cast-expressions

## Objective
Parse cast expressions with JLS 15.16 disambiguation.

## Tasks
1. Add tryCastExpression() with lookahead
2. Add canStartUnaryExpression() helpers
3. Implement intersection casts

## Verification
- [ ] `(int) expr` and `(String) expr` parse
- [ ] `(T1 & T2) expr` intersection casts work
- [ ] Disambiguation from parenthesized expressions correct
