# Bash to Script File Converter Skill

**Purpose**: Convert bash commands with parameter expansion or complex syntax to script file format to avoid parse errors in the Bash tool.

**When to Use**:
- When bash command fails with parse error: `(eval):1: parse error near '('`
- Before executing commands with `${VAR##pattern}` or `${VAR%%pattern}` syntax
- When using complex jq pipelines with command substitution
- When bash command has multiple nested parameter expansions
- Proactively for any bash command pattern known to cause issues

## Problem

The Bash tool has limitations with certain bash syntax patterns, particularly:
- Parameter expansion with pattern removal: `${VAR##prefix}`, `${VAR%%suffix}`
- Complex command substitution: `$(jq ... | tail -1)`
- Nested parameter expansions in single command
- Heredoc with complex variable interpolation

These patterns can fail with cryptic parse errors when executed directly via the Bash tool.

## Solution

Convert the problematic command to a script file approach:
1. Write command to temporary script file using heredoc
2. Execute the script file with `bash /tmp/script.sh`
3. Script file handles all parameter expansion correctly

## Usage

**Input Required**:
- The bash command that needs conversion (as a string)
- Optional: Script filename (defaults to `/tmp/converted-script.sh`)

**Output**:
- Converted command using script file approach
- Ready to execute via Bash tool

## Conversion Instructions

When you encounter a bash command that needs conversion, follow this pattern:

```bash
# Original problematic command (example):
SESSION_ID="abc-123"
AGENT_ID=$(jq -r 'select(.toolUseResult.agentId)' /path/${SESSION_ID}.jsonl | tail -1)
echo "$AGENT_ID" > output.txt

# Convert to script file approach:
cat > /tmp/converted-script.sh << 'EOF'
#!/bin/bash
SESSION_ID="abc-123"
AGENT_ID=$(jq -r 'select(.toolUseResult.agentId)' /path/${SESSION_ID}.jsonl | tail -1)
echo "$AGENT_ID" > output.txt
EOF
bash /tmp/converted-script.sh
```

## Automatic Conversion

To invoke this skill for automatic conversion:

```
Skill: bash-to-script
Command: <paste the problematic bash command here>
```

The skill will output the converted command ready to execute.

## Pattern Recognition

**Commands that need conversion** (automatic detection patterns):

1. **Parameter Expansion with Pattern Removal**:
   ```bash
   ${VAR##prefix*}    # Remove longest matching prefix
   ${VAR%%*suffix}    # Remove longest matching suffix
   ${VAR#prefix}      # Remove shortest matching prefix
   ${VAR%suffix}      # Remove shortest matching suffix
   ```

2. **Complex Command Substitution**:
   ```bash
   VAR=$(complex command | pipeline | tail -1)
   # Especially with jq, grep, awk, sed pipelines
   ```

3. **Multiple Parameter Expansions**:
   ```bash
   echo "${VAR1}" > /path/to/${VAR2}/file-${VAR3}.txt
   # Multiple expansions in single command
   ```

4. **Nested Quotes with Variables**:
   ```bash
   jq -r 'select(.field == "'"$VAR"'")' file.json
   # Nested single/double quotes with variable interpolation
   ```

## Conversion Template

**Standard template for conversion**:

```bash
cat > /tmp/<descriptive-name>.sh << 'EOF'
#!/bin/bash
set -euo pipefail

# Your original command(s) here
# All parameter expansion works correctly in script file

EOF
bash /tmp/<descriptive-name>.sh
```

**With error handling**:

```bash
cat > /tmp/<descriptive-name>.sh << 'EOF'
#!/bin/bash
set -euo pipefail
trap 'echo "ERROR in script at line $LINENO: Command failed: $BASH_COMMAND" >&2; exit 1' ERR

# Your original command(s) here

EOF
bash /tmp/<descriptive-name>.sh
```

## Examples

### Example 1: Parameter Expansion with Pattern Removal

**❌ Fails with parse error**:
```bash
BACKUP_FILE=$(ls /workspace/CLAUDE.md.backup-* 2>/dev/null | tail -1)
VALIDATION_ID="${BACKUP_FILE##*.backup-}"
echo "$VALIDATION_ID"
```

**✅ Correct (converted)**:
```bash
cat > /tmp/extract-validation-id.sh << 'EOF'
#!/bin/bash
BACKUP_FILE=$(ls /workspace/CLAUDE.md.backup-* 2>/dev/null | tail -1)
VALIDATION_ID="${BACKUP_FILE##*.backup-}"
echo "$VALIDATION_ID"
EOF
bash /tmp/extract-validation-id.sh
```

### Example 2: Complex jq Pipeline

**❌ Fails with parse error**:
```bash
SESSION_ID="7f08a8f6-b90c-47ac-bc1b-31943c7b995a"
AGENT_ID=$(jq -r 'select(.toolUseResult.agentId) | .toolUseResult.agentId' /home/node/.config/projects/-workspace/${SESSION_ID}.jsonl 2>/dev/null | tail -1)
echo "$AGENT_ID" > /tmp/agent-id.txt
```

**✅ Correct (converted)**:
```bash
cat > /tmp/extract-agent-id.sh << 'EOF'
#!/bin/bash
SESSION_ID="7f08a8f6-b90c-47ac-bc1b-31943c7b995a"
AGENT_ID=$(jq -r 'select(.toolUseResult.agentId) | .toolUseResult.agentId' /home/node/.config/projects/-workspace/${SESSION_ID}.jsonl 2>/dev/null | tail -1)
echo "$AGENT_ID" > /tmp/agent-id.txt
EOF
bash /tmp/extract-agent-id.sh
```

### Example 3: Multiple Parameter Expansions

**❌ Fails with parse error**:
```bash
TASK="implement-api"
AGENT="architect"
WORKTREE="/workspace/tasks/${TASK}/agents/${AGENT}/code"
mkdir -p "$WORKTREE"
cd "$WORKTREE" && git status
```

**✅ Correct (converted)**:
```bash
cat > /tmp/setup-worktree.sh << 'EOF'
#!/bin/bash
TASK="implement-api"
AGENT="architect"
WORKTREE="/workspace/tasks/${TASK}/agents/${AGENT}/code"
mkdir -p "$WORKTREE"
cd "$WORKTREE" && git status
EOF
bash /tmp/setup-worktree.sh
```

## When NOT to Use Script File

Some bash commands work fine directly in Bash tool:

**✅ Simple commands (no conversion needed)**:
```bash
# Simple variable substitution
NAME="test"
echo "Hello $NAME"

# Basic command substitution
FILES=$(ls *.txt)
echo "$FILES"

# Simple paths
cd /workspace/main && pwd

# Git commands
git status
git log --oneline -5
```

## Error Messages to Watch For

If you see these errors, use this skill:

- `(eval):1: parse error near '('`
- `(eval):1: command not found: (eval)`
- `syntax error near unexpected token`
- `bad substitution`

## Integration with Existing Workflows

**optimize-doc command**: Already documented with script file requirement for validation ID extraction

**Other skills/commands**: Use this skill proactively when commands match the problematic patterns above

## Best Practices

1. **Name script files descriptively**: Use `/tmp/<descriptive-name>.sh` instead of generic names
2. **Add error handling**: Include `set -euo pipefail` and trap for better debugging
3. **Clean up**: Remove script files after use if they're not needed for debugging
4. **Document**: Add comment in script explaining what it does
5. **Test**: Verify script works before using in production workflows

## Quick Reference Card

**Need script file if command has**:
- ✓ `${VAR##pattern}` or `${VAR%%pattern}`
- ✓ Complex `$(...)` with pipes and filters
- ✓ Multiple `${VAR}` expansions in one line
- ✓ Nested quotes with variables
- ✓ Parse error mentioning `(eval):1`

**Don't need script file if**:
- ✗ Simple `$VAR` or `${VAR}`
- ✗ Basic `$(command)`
- ✗ No error reported
- ✗ Simple single-line commands

## Output Format

When skill is invoked, it outputs:

```bash
# Converted command (ready to execute):
cat > /tmp/<generated-name>.sh << 'EOF'
#!/bin/bash
set -euo pipefail

<your original command converted>

EOF
bash /tmp/<generated-name>.sh
```

Simply copy and paste this into the Bash tool to execute.

## Related Documentation

- CLAUDE.md § Tool Usage Best Practices § Bash Tool - Path Handling § Parameter Expansion Limitation
- optimize-doc command (documents script file requirement)

## Maintenance

This skill should be updated when:
- New problematic bash patterns are discovered
- Bash tool behavior changes
- Better conversion approaches are found
- Additional error patterns are identified
