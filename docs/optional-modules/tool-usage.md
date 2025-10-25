# Tool Usage Best Practices

Common tool usage patterns that cause failures and how to prevent them.

## 🛠️ CRITICAL ERROR PREVENTION

This guide contains proven strategies for avoiding common tool-related errors in the Styler Java Code
Formatter project.

## File Editing with the Edit Tool

**PROBLEM**: "String to replace not found in file" errors due to whitespace character mismatches.

**ROOT CAUSE**: The Read tool displays output visually but doesn't distinguish between tabs and spaces.
Copying text from Read output may preserve visual formatting but not actual characters.

**PREVENTION STRATEGIES**:

### 1. Verify whitespace before editing

```bash
# Show exact whitespace characters (^I = tab, spaces shown as-is)
cat -A file.java | grep -A 2 "methodName"

# Show character codes (\t = tab)
od -c file.java | grep methodName

# Quick check for file's indentation style
head -20 file.java | cat -A | grep "^[[:space:]]" | head -3
```

### 2. Use smallest unique strings

```java
// ❌ BAD: Include indentation (tabs vs spaces mismatch risk)
old_string: "\tpublic void methodName()"

// ✅ GOOD: Exclude leading whitespace
old_string: "public void methodName()"
```

### 3. Alternative tools for whitespace-heavy edits

```bash
# Use sed for bulk replacements
sed -i 's/oldPattern/newPattern/g' file.java

# Use awk for complex transformations
awk '{gsub(/pattern/, "replacement")}1' file.java > temp && mv temp file.java
```

### 4. Preview before editing

```bash
# Test regex patterns before applying
grep -n "pattern" file.java  # See what matches
sed -n 's/old/new/p' file.java  # Preview replacements without modifying
```

**CHECKLIST BEFORE USING EDIT TOOL**:
- [ ] Is the old_string unique in the file? (Use grep to verify)
- [ ] Does old_string contain leading/trailing whitespace? (Minimize if possible)
- [ ] Have I verified the actual whitespace characters? (Use cat -A if uncertain)
- [ ] Would sed/awk be safer for this edit? (Consider for bulk changes)

## Bash Command Execution

**PROBLEM**: "No such file or directory" errors when running executables like `./mvnw`.

**ROOT CAUSE**: Bash tool maintains working directory between invocations. Commands may be executed from
unexpected directories.

**PREVENTION STRATEGIES**:

### 1. Always use absolute paths for executables

```bash
# ❌ BAD: Relative path depends on current directory
./mvnw verify

# ✅ GOOD: Absolute path works from anywhere
/workspace/tasks/task-name/code/mvnw verify
```

### 2. Include directory change in same command

```bash
# ✅ GOOD: Change directory and run command atomically
cd /workspace/tasks/task-name/code && ./mvnw verify

# ✅ GOOD: Multiple dependent commands
cd /workspace && ls -la && cat file.txt
```

### 3. Verify location when uncertain

```bash
# Check current directory before running command
pwd && ./mvnw verify

# Or combine verification with execution
pwd && ls -la mvnw && ./mvnw verify
```

### 4. Use full paths for files

```bash
# ❌ BAD: Relative path may fail if directory changed
cat README.md

# ✅ GOOD: Absolute path always works
cat /workspace/tasks/task-name/code/README.md
```

**CHECKLIST BEFORE RUNNING BASH COMMANDS**:
- [ ] Am I using an absolute path OR including `cd` in the command?
- [ ] If using relative paths, do I know the current working directory?
- [ ] For multi-step commands, are they chained with `&&` (not separate invocations)?
- [ ] For path-dependent operations, have I verified the path exists?

## Pattern Matching and Replacement

**PROBLEM**: Over-broad pattern matching causes unintended replacements (e.g., replacing inside helper methods
that use the same pattern).

**ROOT CAUSE**: Global search-and-replace without considering all possible match locations.

**PREVENTION STRATEGIES**:

### 1. Preview all matches before replacing

```bash
# See all lines that would be affected
grep -n "pattern" file.java

# Preview replacements without modifying file
sed -n 's/old/new/p' file.java

# Count matches to verify expected number
grep -c "pattern" file.java
```

### 2. Use line number ranges for targeted changes

```bash
# Only replace in specific line range
sed -i '100,200s/old/new/g' file.java

# Multiple specific lines
sed -i '50s/old/new/; 75s/old/new/; 120s/old/new/' file.java
```

### 3. Use context-aware patterns

```bash
# Only replace in specific context
sed -i '/visitVariableDeclaration/,/^[[:space:]]*}/ s/recordIndentation/visitAndRecordOnly/' file.java

# Only replace when pattern appears with specific context
sed -i '/public.*visitMethod/s/recordIndentation/visitAndRecordOnly/' file.java
```

### 4. Make patterns more specific

```bash
# ❌ BAD: Too broad, matches everywhere
sed -i 's/recordIndentation/visitAndRecordOnly/g' file.java

# ✅ GOOD: More specific pattern
sed -i 's/recordIndentation(node, depth);$/visitAndRecordOnly(node, depth);/g' file.java
```

### 5. Test on copy first

```bash
# Test replacement on a copy
cp file.java file.java.backup
sed -i 's/pattern/replacement/g' file.java
diff file.java.backup file.java  # Review changes
```

**CHECKLIST BEFORE PATTERN REPLACEMENT**:
- [ ] Have I previewed all matches? (`grep -n` or `sed -n ...p`)
- [ ] Is the pattern specific enough to avoid unintended matches?
- [ ] Should I use line ranges to limit scope?
- [ ] Have I considered context-aware matching?
- [ ] For critical files, should I test on a copy first?

## General Tool Usage Principles

1.  **READ FIRST, EDIT SECOND**: Always read a file before editing to understand its structure and whitespace
   style
2. **VERIFY BEFORE EXECUTING**: Use preview/dry-run options when available
3. **ABSOLUTE OVER RELATIVE**: Prefer absolute paths to eliminate directory confusion
4. **SPECIFIC OVER BROAD**: Make patterns as specific as possible to avoid unintended matches
5. **TEST BEFORE COMMIT**: For bulk operations, test on a subset or copy first
