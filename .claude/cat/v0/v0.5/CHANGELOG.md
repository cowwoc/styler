# Changelog: v0.5

**Status:** In Progress

## Summary

Parser edge cases for real-world Java codebases.

## Tasks Completed

| Task | Type | Resolution | Description |
|------|------|------------|-------------|
| fix-floating-point-literal-without-zero | bugfix | implemented | Fix parsing of floating-point literals without leading zero (.5, .0025) |
| fix-contextual-keywords-in-expressions | bugfix | implemented | Recognize contextual keywords as expression starters (var, module, with, etc.) |
| fix-final-in-pattern-matching | bugfix | implemented | Support final modifier in instanceof pattern matching (Java 16+) |
| fix-lambda-typed-parameters-in-args | bugfix | implemented | Parse typed lambda parameters like (Type param) -> body |
| fix-octal-escape-in-char-literal | bugfix | implemented | Handle JLS 3.10.6 octal escape sequences in character/string literals |
| add-serial-annotations | config | implemented | Add @Serial annotation to all serialVersionUID fields |
| create-parser-access-interface | refactor | implemented | Create ParserAccess interface for parser decomposition |
| extend-statement-parser | refactor | implemented | Move 30 statement parsing methods to StatementParser |
| fix-misc-parsing-edge-cases | bugfix | implemented | Fix comment handling in type declarations and enum bodies |
| extract-expr-cast-lambda-detection | refactor | implemented | Extract cast/lambda detection methods to ExpressionParser |

## Key Changes

- Added support for floating-point literals starting with decimal point
- Contextual keywords (var, module, with, to, etc.) now work as expression starters
- `instanceof final Type var` pattern matching now supported
- Typed lambda parameters `(Type param) -> body` now parsed correctly (was misinterpreted as cast)

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
- 9 tests added for typed lambda parameter parsing
- 9 tests added for octal escape sequences in character/string literals
- All parser tests passing

## Gates

### Entry
- Previous version (v0.4) complete

### Exit
- All tasks complete
