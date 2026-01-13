# Task State: fix-else-if-chain-parsing

## Status
status: completed
progress: 100%
started: 2026-01-13
completed: 2026-01-13

## Dependencies
None

## Error Pattern

**18 occurrences** in Spring Framework 6.2.1

Error: `Unexpected token in expression: ELSE`

## Root Cause

Parser failed when comments appeared between the closing brace `}` of an if-block
and the `else` keyword. The `parseIfStatement()` method checked for ELSE immediately
after parsing the statement, without first consuming any intervening comments.

## Solution

Added `parseComments()` call before checking for ELSE token in `parseIfStatement()`.

## Files Changed

- `parser/src/main/java/io/github/cowwoc/styler/parser/Parser.java` (+2 lines)
- `parser/src/test/java/io/github/cowwoc/styler/parser/test/ElseIfCommentParserTest.java` (new, +182 lines)

---
*Completed 2026-01-13*
