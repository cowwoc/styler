# State

- **Status:** completed
- **Progress:** 100%
- **Priority:** HIGH (blocking main branch build)
- **Dependencies:** none
- **Last Updated:** 2026-01-17

## Description

Remove unused private method stubs in Parser.java that were left behind during refactoring to helper
classes (ExpressionParser, StatementParser, TypeParser, ModuleParser).

**Impact:** 52 PMD violations blocking `./mvnw verify` on main branch.

## Acceptance Criteria

- [x] All unused private methods removed from Parser.java
- [x] UnusedPrivateMethod/UnusedLocalVariable PMD violations fixed (36 methods + 1 variable)
- [ ] PMD check passes: `./mvnw pmd:check -pl parser` - 15 pre-existing violations remain in other files
- [ ] Full build passes: `./mvnw verify` - blocked by above

## Notes

The 15 remaining PMD violations are pre-existing issues in different files:
- ExpressionParser: 2 UnnecessaryFullyQualifiedName, 1 NcssCount, 3 AvoidDeeplyNestedIfStmts
- LexerOctalEscapeTest: 9 CommentRequired (missing test JavaDoc)

These are separate issues that should be addressed in a new task.
