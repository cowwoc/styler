---
name: safe-remove-code
description: Safely remove code patterns from multiple files with validation and rollback
allowed-tools: Bash, Read, Edit, Grep
---

# Safe Code Removal Skill

**Purpose**: Safely remove code patterns (instrumentation, debugging code, etc.) from multiple files with strict validation to prevent accidentally gutting files.

**Performance**: Prevents catastrophic file damage, ensures code remains functional

## When to Use This Skill

### ✅ Use safe-remove-code When:

- Removing instrumentation code from multiple files
- Cleaning up debugging statements across codebase
- Removing deprecated patterns systematically
- Need validation that files remain functional after removal

### ❌ Do NOT Use When:

- Removing code from single file (use Edit tool directly)
- Changes are complex refactoring (not simple removal)
- Pattern varies significantly across files
- Need to preserve some instances of pattern

## What This Skill Does

### 1. Creates Backup

```bash
# Timestamped backup branch before any removal
git branch backup-before-removal-$(date +%Y%m%d-%H%M%S)
```

### 2. Identifies Target Files

```bash
# Find all files containing pattern
grep -l "pattern" **/*.java
```

### 3. Per-File Validation

For EACH file:
```bash
# 1. Read original file
# 2. Preview removal (show before/after context)
# 3. Apply removal
# 4. Validate file size reasonable (not gutted)
# 5. Validate syntax if applicable
# 6. Stage if valid
```

### 4. Functional Testing

```bash
# Run build and tests after all removals
mvn clean test
```

### 5. Cleanup or Rollback

```bash
# If all validations pass:
git branch -D backup-before-removal-*

# If any validation fails:
git reset --hard backup-before-removal-*
```

## Usage

### Basic Pattern Removal

```bash
# Remove specific code pattern from multiple files
PATTERN='System\.out\.println.*instrumentation'
FILES=$(grep -l "$PATTERN" src/**/*.java)

/workspace/main/.claude/scripts/safe-remove-code.sh \
  --pattern "$PATTERN" \
  --files "$FILES" \
  --test-command "mvn test"
```

### With File Size Validation

```bash
# Ensure files don't shrink more than 20%
/workspace/main/.claude/scripts/safe-remove-code.sh \
  --pattern "DEBUG_LOG\(.*\)" \
  --files "src/**/*.java" \
  --max-shrink-percent 20 \
  --test-command "mvn test"
```

## Safety Features

### Precondition Validation

- ✅ Verifies clean working directory
- ✅ Creates timestamped backup branch
- ✅ Validates pattern format (regex)
- ✅ Confirms files exist

### Per-File Validation

- ✅ File size check (prevents gutting)
- ✅ Syntax validation (if language supports)
- ✅ Preview before/after (manual verification)
- ✅ Rollback on first failure

### Post-Removal Validation

- ✅ Build succeeds
- ✅ Tests pass
- ✅ No regressions introduced

### Error Handling

On any error:
- Stops immediately
- Rolls back to backup branch
- Reports which file/validation failed
- Leaves repository in clean state

## Validation Thresholds

### File Size Changes

```bash
# Default thresholds:
- Max shrink: 30% (prevents gutting)
- Min shrink: 1% (ensures something removed)
- Warn if no change (pattern not found)
```

### Syntax Validation

```bash
# Language-specific validation:
- Java: javac -Xlint:all
- Python: python -m py_compile
- TypeScript: tsc --noEmit
```

## Workflow Integration

### Complete Safe Removal Workflow

```markdown
1. ✅ Identify pattern to remove
2. ✅ Find all affected files
3. ✅ Invoke safe-remove-code skill
4. ✅ Skill creates backup
5. ✅ Skill removes pattern with validation
6. ✅ Skill runs tests
7. ✅ Skill reports success/failure
8. ✅ Cleanup backup if successful
```

## Output Format

Script returns JSON:

```json
{
  "status": "success",
  "message": "Pattern removed safely from all files",
  "pattern": "System\\.out\\.println.*instrumentation",
  "files_processed": 15,
  "files_modified": 12,
  "files_skipped": 3,
  "backup_branch": "backup-before-removal-20251111-123456",
  "validation_results": {
    "file_size_checks": "passed",
    "syntax_checks": "passed",
    "build_test": "passed"
  },
  "timestamp": "2025-11-11T12:34:56-05:00"
}
```

## Related

- **Edit tool**: For single-file modifications
- **Grep tool**: For finding pattern occurrences
- **git-workflow.md § Backup-Verify-Cleanup**: Git safety pattern
- **docs/optional-modules/safe-code-removal.md**: Complete procedures

## Troubleshooting

### Error: "File gutted (>30% reduction)"

```bash
# File shrank too much, likely removed too much code
# Options:
1. Refine pattern to be more specific
2. Increase --max-shrink-percent if intentional
3. Review file manually to see what was removed
```

### Error: "Build failed after removal"

```bash
# Removal broke compilation
# Rollback applied automatically
# Fix:
1. Check which file caused failure
2. Refine pattern to preserve necessary code
3. Retry with updated pattern
```

### Error: "Pattern not found in file"

```bash
# File listed but pattern doesn't match
# Possible causes:
1. Pattern already removed
2. Regex incorrect
3. Case sensitivity issue
# Skip these files and continue
```
