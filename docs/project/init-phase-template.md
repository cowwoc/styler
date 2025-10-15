# INIT Phase - Parallel Resource Loading Template

**For Java Implementation Tasks**:

```bash
# Copy-paste this pattern into first message:

Read docs/project/task-protocol-core.md + \
Read docs/project/task-protocol-operations.md + \
Read todo.md + \
Read pom.xml + \
Read checkstyle.xml + \
Read pmd.xml + \
Glob "src/main/java/**/*{KeywordFromTask}*.java" + \
Glob "src/test/java/**/*{KeywordFromTask}*Test.java" + \
Read docs/project/architecture.md + \
Glob "docs/code-style/*-claude.md"
```

**Result**: All resources loaded in 1 round-trip (vs 8-10 sequential)
**Savings**: 7-9 messages, 30-45 seconds, ~5k tokens

## Usage Instructions

1. **Identify Task Keywords**: Extract primary keywords from task name
   - Example: `implement-line-length-formatter` → Keywords: `LineLength`, `Formatter`
   - Example: `refactor-parser-architecture` → Keywords: `Parser`, `Architecture`

2. **Substitute Keywords**: Replace `{KeywordFromTask}` with identified keywords
   - Use wildcards to match related files
   - Include both implementation and test patterns

3. **Adjust for Task Type**:
   - **New Features**: Include all patterns above
   - **Refactoring**: Focus on existing implementation files
   - **Bug Fixes**: Include specific failing component patterns
   - **Documentation**: Reduce to protocol + task files only

## Example: Line Length Formatter Task

```bash
# Task: implement-line-length-formatter

Read docs/project/task-protocol-core.md + \
Read docs/project/task-protocol-operations.md + \
Read todo.md + \
Read pom.xml + \
Read checkstyle.xml + \
Read pmd.xml + \
Glob "src/main/java/**/*LineLength*.java" + \
Glob "src/main/java/**/*Formatter*.java" + \
Glob "src/test/java/**/*LineLength*Test.java" + \
Glob "src/test/java/**/*Formatter*Test.java" + \
Read docs/project/architecture.md + \
Glob "docs/code-style/*-claude.md"
```

## Example: Parser Refactoring Task

```bash
# Task: refactor-parser-architecture

Read docs/project/task-protocol-core.md + \
Read docs/project/task-protocol-operations.md + \
Read todo.md + \
Read pom.xml + \
Glob "src/main/java/**/*Parser*.java" + \
Glob "src/main/java/**/*Lexer*.java" + \
Glob "src/test/java/**/*Parser*Test.java" + \
Read docs/project/architecture.md + \
Read docs/project/scope.md
```

## Example: Documentation Update Task

```bash
# Task: update-api-documentation

Read docs/project/task-protocol-core.md + \
Read docs/project/task-protocol-operations.md + \
Read todo.md + \
Glob "docs/api/**/*.md" + \
Read docs/project/architecture.md
```

## Verification

After executing parallel read:
- Verify all expected files loaded (check for "file not found" errors)
- Confirm no additional reads needed before SYNTHESIS
- Check message count: Should be 1 message for all resources

## Common Patterns by Module

**Formatter Module**:
```
Glob "formatter-api/src/**/*.java" + \
Glob "formatter-impl/src/**/*.java" + \
Glob "formatter-impl/src/test/**/*Test.java"
```

**Parser Module**:
```
Glob "parser/src/**/*Parser*.java" + \
Glob "parser/src/**/*Lexer*.java" + \
Glob "parser/src/test/**/*Test.java"
```

**Checkstyle Integration**:
```
Glob "checkstyle/src/**/*.java" + \
Read checkstyle.xml + \
Read pmd.xml
```
