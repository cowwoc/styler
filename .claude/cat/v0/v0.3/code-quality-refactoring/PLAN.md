# Task Plan: code-quality-refactoring

## Objective

Improve parser code quality: expand abbreviated names and convert if-else chains to switches.

## Tasks

### Part A: Expand TokenType Acronyms
1. Rename 37 TokenType constants (LPAREN->LEFT_PARENTHESIS, etc.)
2. Update all references in Parser, Lexer, Token
3. Update test assertions

### Part B: Refactor If-Else to Switch
1. Convert parseStatement() 16-branch chain to switch
2. Convert parseComments() 4-branch chain to switch

## Verification

- [ ] All tokens have descriptive names
- [ ] All tests still pass
- [ ] No behavior changes
- [ ] Style rule compliance

