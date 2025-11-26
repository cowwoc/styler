---
description: >
  Guide for adding anchor-based documentation cross-references that won't break when files are
  edited
tags:
  - documentation
  - references
  - anchors
---

# Add Documentation Reference Skill

Use this skill when adding cross-references between documentation files or from hooks/scripts to
documentation.

## Why Anchor-Based References?

**Problem**: Hard-coded line numbers break when documentation is edited

**Solution**: Use section anchors that resolve to current line numbers

## For Hook Scripts and Code

### Correct Pattern - Anchor-Based

```bash
# In hooks and scripts
source .claude/hooks/lib/doc-reference-resolver.sh
DOC_REF=$(resolve_doc_ref "task-protocol-core.md#init-classified")
echo "📖 Read: $DOC_REF"
# Output: Read /workspace/main/docs/project/task-protocol-core.md lines 1590-1634
```

**Benefits**:
- Resolves to current line numbers automatically
- Survives documentation edits
- Index regenerates on commit (pre-commit hook)

### Incorrect Pattern - Hard-Coded Lines

```bash
# NEVER do this - breaks when documentation changes
echo "Read /workspace/main/docs/project/task-protocol-core.md lines 1583-1626"
```

**Why it breaks**:
- Line numbers shift when docs are edited
- No automatic update mechanism
- Creates stale/broken references

## For Markdown Documentation

### Adding Anchors to Headings

**Syntax**:
```markdown
## Section Title {#anchor-id}
```

**Anchor Naming Convention**:
- Lowercase kebab-case
- Match heading semantics
- Descriptive and stable

**Examples**:
```markdown
## Lock Ownership {#lock-ownership}
## INIT → CLASSIFIED Transition {#init-classified}
## Post-Implementation Issue Handling {#post-implementation-issue-handling-decision-tree}
```

### Referencing Sections

**Correct - Specific Section Reference**:
```markdown
See [main-agent-coordination.md § Post-Implementation Issue Handling](docs/project/main-agent-coordination.md#post-implementation-issue-handling-decision-tree)
```

**Format**: `[filename § Section Name](path#anchor-id)`

**Incorrect - Vague File-Level Reference**:
```markdown
❌ "Refer to CLAUDE.md for state-based fix permissions" (too vague, no specific section)
❌ "See main-agent-coordination.md" (file-level only, no section specified)
```

**Why specificity matters**:
- Readers can jump directly to relevant section
- Clear what information to look for
- Anchor provides stable reference point

## Workflow for Adding References

### From Hook Scripts

1. **Identify the documentation section** you want to reference
2. **Find or add anchor** to that section (e.g., `{#task-validation}`)
3. **Use doc-reference-resolver.sh**:
   ```bash
   source .claude/hooks/lib/doc-reference-resolver.sh
   DOC_REF=$(resolve_doc_ref "task-protocol-core.md#task-validation")
   echo "📖 See: $DOC_REF"
   ```
4. **Test the reference**: Run script, verify line numbers are correct

### From Markdown Docs

1. **Identify the target section** in another document
2. **Check if anchor exists**: Look for `{#anchor-id}` in heading
3. **If no anchor, add one**:
   ```markdown
   ## Target Section {#target-section}
   ```
4. **Create reference** with § separator:
   ```markdown
   See [protocol-doc.md § Target Section](docs/project/protocol-doc.md#target-section)
   ```
5. **Verify link works**: Click in GitHub/IDE preview

## Anchor Naming Guidelines

**Good Anchors** (stable, semantic):
- `{#state-machine-workflow}`
- `{#validation-fix-boundaries}`
- `{#backup-verify-cleanup}`
- `{#hook-registration-checklist}`

**Bad Anchors** (fragile, meaningless):
- `{#section-1}` (meaningless)
- `{#git-operations-updated-20251115}` (includes date, will change)
- `{#FIXME}` (temporary marker, not semantic)

**Principles**:
- Use heading content as basis
- Remove special characters
- Lowercase with hyphens
- Keep under 50 characters if possible
- Avoid dates, version numbers, or temporary markers

## System Maintenance

**Automatic Index Regeneration**:
- Pre-commit hook runs `.claude/scripts/generate-doc-index.sh`
- Updates anchor → line number mappings
- Ensures `doc-reference-resolver.sh` has current data

**Manual Regeneration** (if needed):
```bash
./.claude/scripts/generate-doc-index.sh
```

**Index Location**: `.claude/doc-index.json`

## Common Mistakes

❌ **Hard-coding line numbers**: `Read file.md lines 100-150`
❌ **Vague file references**: "See CLAUDE.md" (which section?)
❌ **Missing anchors**: Referencing section without anchor ID
❌ **Brittle anchor names**: `{#temp-fix-123}` (not semantic)
❌ **Forgetting § separator**: `[file.md Section](path)` (should use §)

## Examples

### Example 1: Hook Error Message with Doc Reference

```bash
#!/bin/bash
source .claude/hooks/lib/doc-reference-resolver.sh

DOC_REF=$(resolve_doc_ref "main-agent-coordination.md#validation-state-fix-boundaries")

echo "❌ ERROR: Style violations must be fixed by formatter agent" >&2
echo "📖 See: $DOC_REF" >&2
exit 2
```

**Output**:
```
❌ ERROR: Style violations must be fixed by formatter agent
📖 See: /workspace/main/docs/project/main-agent-coordination.md lines 354-366
```

### Example 2: Markdown Cross-Reference

**In task-protocol-core.md**:
```markdown
## VALIDATION State {#validation-state}

After implementation, the VALIDATION state runs build and tests.

For guidance on what main agent can fix vs what requires re-invoking agents, see
[main-agent-coordination.md § Validation State Fix Boundaries](../main-agent-coordination.md#validation-state-fix-boundaries).
```

### Example 3: Adding New Anchor

**Before** (no anchor):
```markdown
## Documentation System

The documentation system uses markdown files...
```

**After** (with anchor):
```markdown
## Documentation System {#documentation-system}

The documentation system uses markdown files...
```

Now can reference: `docs/project/overview.md#documentation-system`

## Complete Guide

For full documentation reference system details, see:
[docs/project/documentation-references.md](docs/project/documentation-references.md)
