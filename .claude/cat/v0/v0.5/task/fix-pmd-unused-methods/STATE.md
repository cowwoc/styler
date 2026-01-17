# State

- **Status:** pending
- **Progress:** 0%
- **Priority:** HIGH (blocking main branch build)
- **Dependencies:** none
- **Last Updated:** 2026-01-17

## Description

Remove unused private method stubs in Parser.java that were left behind during refactoring to helper
classes (ExpressionParser, StatementParser, TypeParser, ModuleParser).

**Impact:** 52 PMD violations blocking `./mvnw verify` on main branch.

## Acceptance Criteria

- [ ] All unused private methods removed from Parser.java
- [ ] PMD check passes: `./mvnw pmd:check -pl parser`
- [ ] Full build passes: `./mvnw verify`
- [ ] No regression in parser functionality
