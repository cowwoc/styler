# Documentation Reference System

> **Version:** 1.0 | **Last Updated:** 2025-10-20
> **Audience:** All developers and documentation maintainers
> **Purpose:** Guidelines for referencing documentation sections

## Overview

This project uses an anchor-based documentation reference system that automatically maintains line number mappings. This prevents broken references when documentation is edited.

## Quick Reference

### ✅ CORRECT - Anchor-Based References

```bash
# In hooks and scripts
source .claude/hooks/lib/doc-reference-resolver.sh
REF=$(resolve_doc_ref "task-protocol-core.md#init-classified")
echo "Read detailed protocol: $REF"
# Output: Read /workspace/main/docs/project/task-protocol-core.md lines 1590-1634
```

```markdown
<!-- In documentation -->
See [Agent Coordination](main-agent-coordination.md#lock-ownership) for details.
```

### ❌ INCORRECT - Hard-Coded Line Numbers

```bash
# DON'T DO THIS
echo "Read /workspace/main/docs/project/task-protocol-core.md lines 1583-1626"
```

```markdown
<!-- DON'T DO THIS -->
Read task-protocol-core.md lines 1583-1626 for details.
```

## Adding Anchors to Documentation

When creating new sections in markdown files:

```markdown
## Section Title {#anchor-id}
```

**Anchor naming conventions:**
- Use lowercase kebab-case: `{#my-section-title}`
- Be descriptive and stable: `{#lock-ownership}` not `{#section-1}`
- Match heading semantics, not position
- Avoid dates, version numbers, or transient details

**Examples:**
```markdown
## 🚨 Lock Ownership & Task Recovery {#lock-ownership}
## Multi-Agent Implementation Workflow {#multi-agent-implementation-workflow}
## INIT → CLASSIFIED {#init-classified}
```

## Using References in Code

### Shell Scripts and Hooks

```bash
#!/bin/bash
set -euo pipefail

# Source the resolver library
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
source "${SCRIPT_DIR}/lib/doc-reference-resolver.sh"

# Resolve reference to get exact line numbers
DOC_REF=$(resolve_doc_ref "task-protocol-core.md#init-classified")

# Use in output
cat << EOF
📖 DETAILED PROTOCOL:
   $DOC_REF
   (Section: "INIT → CLASSIFIED")
EOF
```

### Markdown Documentation

Use standard markdown anchor links:

```markdown
See [Lock Ownership](main-agent-coordination.md#lock-ownership) for details.

For complete protocol, read:
- [INIT Phase](task-protocol-core.md#init-classified)
- [Agent Workflow](task-protocol-core.md#multi-agent-implementation-workflow)
```

## System Maintenance

### Automatic Index Updates

The pre-commit hook automatically regenerates `docs/.index.json` when markdown files change:

```bash
# Happens automatically on commit
git add docs/project/my-file.md
git commit -m "Update documentation"
# Hook regenerates docs/.index.json and stages it
```

### Manual Index Regeneration

```bash
./scripts/generate-doc-index.sh
```

### Testing References

```bash
# Test a reference
./scripts/resolve-doc-reference.sh "task-protocol-core.md#init-classified"
# Output: Read /workspace/main/docs/project/task-protocol-core.md lines 1590-1634

# Check if anchor exists
./scripts/resolve-doc-reference.sh "task-protocol-core.md#nonexistent"
# Output: ERROR: Reference not found in index
```

## Migration Guide

### Finding Old References

```bash
# Find hard-coded line number references
./scripts/find-hardcoded-references.sh

# Shows files with old-style references:
# .claude/hooks/my-hook.sh:45: Read task-protocol-core.md lines 1583-1626
```

### Converting to Anchor-Based

1. Find the section being referenced
2. Check if it has an anchor ID `{#anchor-id}`
3. If no anchor exists, add one following naming conventions
4. Replace hard-coded reference with `resolve_doc_ref` call

**Before:**
```bash
echo "Read /workspace/main/docs/project/task-protocol-core.md lines 1583-1626"
```

**After:**
```bash
DOC_REF=$(resolve_doc_ref "task-protocol-core.md#init-classified")
echo "$DOC_REF"
```

## Troubleshooting

### Reference Not Found

```
ERROR: Reference not found in index: task-protocol-core.md#my-anchor
```

**Solutions:**
1. Check anchor exists in the file: `grep "{#my-anchor}" docs/project/task-protocol-core.md`
2. Regenerate index: `./scripts/generate-doc-index.sh`
3. Verify anchor syntax: `{#lowercase-kebab-case}` not `{#CamelCase}`

### Index Out of Date

```
# Line numbers seem wrong
```

**Solution:**
```bash
./scripts/generate-doc-index.sh
git add docs/.index.json
git commit -m "Update documentation index"
```

### Hook Can't Find Resolver Library

```
ERROR: doc-reference-resolver.sh not found
```

**Solution:**
```bash
# In your hook script, use absolute path
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
source "${SCRIPT_DIR}/lib/doc-reference-resolver.sh"
```

## Benefits

✅ **Stable References**: Anchor names rarely change, line numbers constantly do
✅ **Auto-Maintenance**: Pre-commit hook keeps index up-to-date
✅ **Performance**: Instant index lookups vs full file scans
✅ **Readable**: `#lock-ownership` is clearer than `lines 1583-1626`
✅ **Tooling Support**: Standard markdown anchor links work everywhere
✅ **Graceful Degradation**: Falls back to section-only reference if index missing

## See Also

- [scripts/generate-doc-index.sh](../../scripts/generate-doc-index.sh) - Index generator
- [scripts/resolve-doc-reference.sh](../../scripts/resolve-doc-reference.sh) - Reference resolver
- [.claude/hooks/lib/doc-reference-resolver.sh](../../.claude/hooks/lib/doc-reference-resolver.sh) - Resolver library
- [.git/hooks/pre-commit](../../.git/hooks/pre-commit) - Auto-update hook
