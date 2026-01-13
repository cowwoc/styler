# Task Plan: implement-whitespace-formatter

## Objective

Build whitespace checking for operators, keywords, and punctuation.

## Context

Consistent whitespace improves readability. Check spacing around operators,
after keywords, and inside parentheses.

## Tasks

1. Create WhitespaceFormattingRule
2. Implement operator whitespace checking
3. Add keyword spacing rules
4. Handle parenthesis padding options

## Verification

- [ ] Detects missing/extra spaces around operators
- [ ] Keyword spacing correct (if, for, while)
- [ ] Configurable parenthesis padding
- [ ] Auto-fix adjusts whitespace

## Files

- `formatter/src/main/java/.../formatter/whitespace/WhitespaceFormattingRule.java`

