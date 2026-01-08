# Plan: expand-tokentype-acronyms

## Objective
Rename abbreviated TokenType enum constants to full descriptive names.

## Tasks
1. Rename 37 TokenType constants (LPAREN→LEFT_PARENTHESIS, etc.)
2. Update all references in Parser, Lexer, Token
3. Update test assertions

## Verification
- [ ] All tokens have descriptive names
- [ ] All tests still pass
