# Safe Code Removal Procedures

**Purpose**: Guidelines for safely removing code patterns from multiple files without accidentally destroying functional code.

**Created**: 2025-11-07 after accidentally gutting 7 hooks during timing code removal

## The Problem

When removing instrumentation, debugging code, or other patterns from multiple files, aggressive removal scripts can accidentally delete functional code, leaving only boilerplate (shebang, set commands).

**Real Example** (2025-11-07):
- **Task**: Remove timing instrumentation from 47 hooks
- **Mistake**: Removal script was too aggressive
- **Impact**: 7 hooks reduced to 3 lines (only `#!/bin/bash` and `set -euo pipefail`)
- **Hooks destroyed**: auto-learn-from-mistakes.sh, block-data-loss.sh, detect-worktree-violation.sh, enforce-requirements-phase.sh, load-todo.sh, post-tool-use-detect-assistant-giving-up.sh, verify-convergence-entry.sh
- **Recovery**: Restored from backups
- **Root cause**: Didn't validate hooks after removal, declared task complete too early

## Safe Removal Procedure

### Phase 1: Identify Removal Patterns

**❌ WRONG - Vague Pattern**:
```bash
# Dangerous: May match more than intended
sed -i '/timing/,/end/d' *.sh
```

**✅ CORRECT - Precise Pattern**:
```bash
# Identify EXACT patterns to remove
PATTERNS_TO_REMOVE=(
  "HOOK_START="
  "log_timing()"
  "trap.*timing.*exit"
)

# Test pattern on one file first
for pattern in "${PATTERNS_TO_REMOVE[@]}"; do
  echo "Pattern: $pattern"
  grep -n "$pattern" example-hook.sh || echo "  No matches"
done
```

### Phase 2: Create Backups

**MANDATORY before any removal**:

```bash
# Create timestamped backups
BACKUP_SUFFIX=".backup-$(date +%Y%m%d-%H%M%S)"

for file in /workspace/.claude/hooks/*.sh; do
  if [[ -f "$file" ]] && [[ ! "$file" =~ \.backup ]]; then
    cp "$file" "${file}${BACKUP_SUFFIX}"
  fi
done

echo "Backups created with suffix: $BACKUP_SUFFIX"
```

### Phase 3: Remove Code with Validation

**Create removal script with per-file validation**:

```bash
#!/bin/bash
# safe-pattern-removal.sh
# Removes specific patterns with per-file validation

set -euo pipefail

PATTERN="$1"  # Pattern to remove
TARGET_DIR="${2:-.}"
MIN_LINES="${3:-10}"  # Minimum lines after removal (safety check)

if [[ -z "$PATTERN" ]]; then
  echo "Usage: $0 <pattern> [target-dir] [min-lines]" >&2
  exit 1
fi

echo "Removing pattern: $PATTERN"
echo "Target directory: $TARGET_DIR"
echo "Minimum lines after removal: $MIN_LINES"
echo ""

EXIT_CODE=0

for file in "$TARGET_DIR"/*.sh; do
  if [[ ! -f "$file" ]] || [[ "$file" =~ \.backup ]]; then
    continue
  fi

  filename=$(basename "$file")
  lines_before=$(wc -l < "$file")

  # Remove pattern
  sed -i "/$PATTERN/d" "$file"

  lines_after=$(wc -l < "$file")
  lines_removed=$((lines_before - lines_after))

  # Validate syntax
  if ! bash -n "$file" 2>/dev/null; then
    echo "❌ $filename: SYNTAX ERROR after removal" >&2
    # Restore from backup
    BACKUP=$(ls -t "${file}.backup-"* 2>/dev/null | head -1)
    if [[ -n "$BACKUP" ]]; then
      cp "$BACKUP" "$file"
      echo "   Restored from $BACKUP" >&2
    fi
    EXIT_CODE=1
    continue
  fi

  # Check if file was gutted
  functional_lines=$(grep -v '^\s*#' "$file" | grep -v '^\s*$' | wc -l)
  if [[ $functional_lines -lt $MIN_LINES ]]; then
    echo "⚠️  $filename: SUSPICIOUSLY SMALL after removal ($functional_lines functional lines, removed $lines_removed)" >&2
    echo "   Review manually to ensure functional code not removed" >&2
    EXIT_CODE=1
  else
    echo "✅ $filename: Removed $lines_removed lines ($functional_lines functional lines remain)"
  fi
done

if [[ $EXIT_CODE -eq 0 ]]; then
  echo ""
  echo "✅ Pattern removal complete with validation"
else
  echo ""
  echo "❌ Some files failed validation - review manually"
fi

exit $EXIT_CODE
```

### Phase 4: Functional Testing

**BEFORE removing backups, run functional tests**:

```bash
# 1. Syntax validation
echo "Running syntax validation..."
for hook in /workspace/.claude/hooks/*.sh; do
  if [[ -f "$hook" ]] && [[ ! "$hook" =~ \.backup ]]; then
    bash -n "$hook" || echo "SYNTAX ERROR: $hook"
  fi
done

# 2. Integrity check
echo "Running integrity check..."
bash /workspace/.claude/hooks/tests/validate-hook-integrity.sh

# 3. Functional tests
echo "Running functional tests..."
bash /tmp/test-hooks-still-work.sh

# 4. Hook-specific tests (if available)
for test in /workspace/.claude/hooks/tests/test-*.sh; do
  if [[ -f "$test" ]]; then
    echo "Running $(basename "$test")..."
    bash "$test" || echo "FAILED: $test"
  fi
done
```

### Phase 5: Manual Review

**Sample files before declaring complete**:

```bash
# Check a few files to verify removal was clean
SAMPLE_HOOKS=(
  "auto-learn-from-mistakes.sh"
  "enforce-commit-squashing.sh"
  "load-todo.sh"
)

echo "Manual review of sample hooks:"
for hook in "${SAMPLE_HOOKS[@]}"; do
  echo ""
  echo "=== $hook ==="
  wc -l "/workspace/.claude/hooks/$hook"
  head -20 "/workspace/.claude/hooks/$hook"
done
```

### Phase 6: Cleanup Backups

**ONLY after validation passes**:

```bash
# Verify all tests passed
if [[ $ALL_TESTS_PASSED == "true" ]]; then
  # Remove backups
  rm -f /workspace/.claude/hooks/*.backup-*
  echo "✅ Backups removed after successful validation"
else
  echo "⚠️  Keeping backups due to validation failures"
  echo "Review issues before cleanup"
fi
```

## Validation Checklist

Before declaring code removal complete:

- [ ] **Backups created** with timestamp
- [ ] **Patterns identified** precisely (not vague)
- [ ] **Removal script** validates per-file
- [ ] **Syntax check** passes for all files
- [ ] **Integrity check** passes (no gutted files)
- [ ] **Functional tests** pass (hooks still work)
- [ ] **Manual review** of sample files
- [ ] **No errors** in validation output
- [ ] **Backups cleanup** only after all checks pass

## Anti-Patterns to Avoid

### ❌ WRONG: Bulk removal without validation

```bash
# Dangerous: No per-file validation
sed -i '/pattern/d' *.sh
echo "Done!"  # ← Declared complete without validation
```

### ❌ WRONG: Removing backups too early

```bash
# Removes backups before functional testing
rm *.backup
bash test.sh  # ← Too late if test fails
```

### ❌ WRONG: Vague patterns

```bash
# May match more than intended
sed -i '/^[[:space:]]*#/d' *.sh  # ← Removes ALL comments, including documentation
```

### ✅ CORRECT: Precise removal with validation

```bash
# Specific pattern with validation
sed -i '/^[[:space:]]*# TIMING:/d' *.sh
bash -n file.sh || { echo "Syntax error"; restore_backup; }
bash test.sh || { echo "Functional test failed"; restore_backup; }
```

## Tool-Assisted Safe Removal

**Use existing tools for common operations**:

### Option 1: grep to verify before removal

```bash
# Preview what will be removed
grep -n "PATTERN" *.sh

# User confirms, then remove
sed -i '/PATTERN/d' *.sh
```

### Option 2: Interactive removal with confirmation

```bash
for file in *.sh; do
  if grep -q "PATTERN" "$file"; then
    echo "Found pattern in $file:"
    grep -n "PATTERN" "$file"
    read -p "Remove from this file? (y/n) " -n 1 -r
    echo
    if [[ $REPLY =~ ^[Yy]$ ]]; then
      sed -i '/PATTERN/d' "$file"
      bash -n "$file" || echo "ERROR: Syntax broken"
    fi
  fi
done
```

## Recovery Procedures

If you discover files were gutted after removal:

### Step 1: Stop immediately

```bash
# Don't make it worse - stop removal process
echo "STOP: Gutted files detected"
```

### Step 2: Restore from backups

```bash
# Restore all files from most recent backup
for backup in /workspace/.claude/hooks/*.backup-*; do
  original="${backup%.backup-*}.sh"
  if [[ -f "$backup" ]]; then
    # Check if original was gutted
    functional_lines=$(grep -v '^\s*#' "$original" | grep -v '^\s*$' | wc -l)
    backup_lines=$(grep -v '^\s*#' "$backup" | grep -v '^\s*$' | wc -l)

    if [[ $functional_lines -lt 10 ]] && [[ $backup_lines -gt 10 ]]; then
      echo "Restoring $original from $backup"
      cp "$backup" "$original"
    fi
  fi
done
```

### Step 3: Validate restoration

```bash
# Verify files restored correctly
bash /workspace/.claude/hooks/tests/validate-hook-integrity.sh
bash /tmp/test-hooks-still-work.sh
```

### Step 4: Analyze what went wrong

```bash
# Invoke learn-from-mistakes skill
Skill: learn-from-mistakes

# Document the mistake for prevention
```

## Prevention via Automation

**Create pre-commit hook to catch gutted files**:

```bash
#!/bin/bash
# .git/hooks/pre-commit
# Prevents committing gutted hook files

HOOKS_DIR=".claude/hooks"
MIN_LINES=10

for hook in "$HOOKS_DIR"/*.sh; do
  if [[ -f "$hook" ]] && git diff --cached --name-only | grep -q "$(basename "$hook")"; then
    functional_lines=$(grep -v '^\s*#' "$hook" | grep -v '^\s*$' | wc -l)
    if [[ $functional_lines -lt $MIN_LINES ]]; then
      echo "❌ ERROR: Attempting to commit gutted hook: $(basename "$hook")" >&2
      echo "   Only $functional_lines functional lines (expected at least $MIN_LINES)" >&2
      echo "   File may have been accidentally damaged during code removal" >&2
      exit 1
    fi
  fi
done
```

## Summary

**Key Principles**:
1. **Precision over speed**: Use specific patterns, not vague ones
2. **Validation per file**: Check each file individually
3. **Test before cleanup**: Keep backups until ALL validation passes
4. **Multiple validation layers**: Syntax + integrity + functional tests
5. **Manual review**: Sample check before declaring complete

**Remember**: It's better to be slow and careful than fast and destructive. Functional code is irreplaceable; removal can always wait for proper validation.
