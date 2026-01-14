# Plan: add-serial-annotations

## Current State
Java classes with `serialVersionUID` fields lack the `@Serial` annotation (introduced in Java 14).

## Target State
All `serialVersionUID` fields are annotated with `@java.io.Serial` for proper serialization semantics.

## Rationale
The `@Serial` annotation marks fields and methods that are part of Java's serialization mechanism. It provides
compile-time checking and documentation that these members exist specifically for serialization, not general use.

## Risk Assessment
- **Risk Level:** LOW
- **Breaking Changes:** None - annotation-only change
- **Mitigation:** Build verification after each file

## Execution Steps

### Step 1: Find all serialVersionUID fields
**Files:** All Java files in src/
**Action:** Grep for `serialVersionUID` to identify all files needing the annotation
**Verify:** `grep -r "serialVersionUID" --include="*.java" src/`
**Done:** List of all files containing serialVersionUID

### Step 2: Add @Serial annotation to each field
**Files:** Each file identified in Step 1
**Action:** For each serialVersionUID field, add `@Serial` annotation above it. Add `import java.io.Serial;`
if not present.
**Verify:** Build compiles without errors: `./mvnw compile`
**Done:** All fields annotated, no compilation errors

### Step 3: Verify no warnings
**Files:** All modified files
**Action:** Run full build to ensure no new warnings introduced
**Verify:** `./mvnw clean compile`
**Done:** Build succeeds with no serialization warnings

## Verification
- [ ] All serialVersionUID fields have @Serial annotation
- [ ] All files have import java.io.Serial
- [ ] Build passes without errors
- [ ] No behavior changes (annotation-only)
