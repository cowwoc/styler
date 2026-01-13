# Task State: fix-contextual-keywords-as-identifiers

## Status
status: completed
progress: 100%
started: 2026-01-13
completed: 2026-01-13

## Summary
Implemented support for contextual keywords as identifiers. Java contextual keywords (var, yield,
module, open, to, requires, exports, opens, uses, provides, with, transitive) can now be used as
identifiers outside their special contexts.

## Changes
- Parser.java: Added `isContextualKeyword()`, `expectIdentifierOrContextualKeyword()`, and
  `isIdentifierOrContextualKeyword()` methods
- Updated ~15 identifier parsing locations to accept contextual keywords
- Created ContextualKeywordIdentifierTest.java with 8 tests verifying AST structure

## Commit
d446349 bugfix: allow contextual keywords as identifiers
