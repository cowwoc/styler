# Changelog: v0.5

**Status:** In Progress

## Summary

Parser edge cases for real-world Java codebases.

## Tasks Completed

| Task | Type | Resolution | Description |
|------|------|------------|-------------|
| fix-floating-point-literal-without-zero | bugfix | implemented | Fix parsing of floating-point literals without leading zero (.5, .0025) |
| fix-contextual-keywords-in-expressions | bugfix | implemented | Recognize contextual keywords as expression starters (var, module, with, etc.) |

## Key Changes

- Added support for floating-point literals starting with decimal point
- Contextual keywords (var, module, with, to, etc.) now work as expression starters

## Files Changed

### Created
- `parser/src/test/.../LexerFloatingPointWithoutLeadingZeroTest.java` - Tests for decimal-first literals

### Modified
- `parser/src/main/.../Lexer.java` - Added scanFloatingPointStartingWithDot() method

## Technical Highlights

- Per JLS 3.10.2, Java allows `.5` as shorthand for `0.5`
- Lexer now checks for DOT followed by digit before operator scanning

## Quality

- 9 tests added for decimal-first floating-point literals
- 14 tests added for contextual keyword expression handling
- All 848 parser tests passing
