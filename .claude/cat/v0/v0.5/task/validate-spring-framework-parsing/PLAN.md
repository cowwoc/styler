# Task Plan: validate-spring-framework-parsing

## Objective

Validate that the parser handles all Spring Framework 6.2.1 Java source files without errors.
This is a manual gate task - run before marking v0.5 complete.

## Problem Analysis

v0.5 tasks fix parser edge cases identified from Spring Framework parsing errors:
- fix-switch-expression-case-parsing
- fix-lambda-parameter-parsing
- fix-comment-in-member-declaration
- add-nested-annotation-type-support
- fix-contextual-keywords-as-identifiers
- fix-cast-lambda-expression
- add-array-initializer-in-annotation-support

Each task has unit tests, but we need end-to-end validation against the actual codebase.

## Tasks

1. [x] Create `scripts/validate-spring-framework.sh` script
2. [x] Create `tools/` module with `ValidateCodebase.java`
3. [ ] Run script against Spring Framework 6.2.1
4. [ ] Document results (error count should be 0 or near-zero)

## Script Requirements

The script should:
- Accept path to Spring Framework checkout as argument
- Find all `.java` files (excluding test files optionally)
- Run parser on each file
- Count and report errors by type
- Exit 0 if no errors, exit 1 if errors found

## Execution

```bash
# Clone Spring Framework (if not already present)
git clone --depth 1 --branch v6.2.1 https://github.com/spring-projects/spring-framework.git /tmp/spring-framework

# Run validation
./scripts/validate-spring-framework.sh /tmp/spring-framework
```

## Success Criteria

- [ ] Script created and documented
- [ ] Parser runs on all Spring Framework files
- [ ] Error count is 0 (or documented exceptions)
- [ ] Results recorded in STATE.md

## Note

This is a **manual validation gate**, not an automated test. Run before marking v0.5 complete.
