# Style Guide for Code Formatting

> **Version:** 1.0 | **Last Updated:** 2025-10-18
> **Audience:** style, main agent (during VALIDATION)
> **Purpose:** Style validation requirements and JavaDoc documentation standards

## 🎯 COMPLETE STYLE VALIDATION {#complete-style-validation}

Style validation requires THREE components: checkstyle + PMD + manual rules.

Checking only checkstyle when PMD/manual violations exist is a CRITICAL ERROR.

`smart-doc-prompter.sh` hook injects 3-component checklist. `checkstyle/fixers` module handles LineLength vs UnderutilizedLines conflicts.

### Validation Components {#validation-components}

1. **Checkstyle** (automated tool):
   - Line length, indentation, naming conventions
   - JavaDoc requirements
   - Import organization
   - Whitespace rules

2. **PMD** (automated tool):
   - Code quality patterns
   - Unused code detection
   - Complexity metrics
   - Best practice enforcement

3. **Manual Rules** (documented in code-style-human.md):
   - TIER1 violations: Critical style issues
   - TIER2 violations: Important style issues
   - TIER3 violations: Minor style issues

### Validation Commands {#validation-commands}

```bash
# Full style validation (all three components)
./mvnw checkstyle:check pmd:check

# Individual components
./mvnw checkstyle:check
./mvnw pmd:check

# Review manual rules
cat docs/code-style-human.md
```

### Complete Validation Checklist {#complete-validation-checklist}

Before declaring style validation complete:

- [ ] Checkstyle passes: `./mvnw checkstyle:check`
- [ ] PMD passes: `./mvnw pmd:check`
- [ ] Manual rules reviewed from code-style-human.md
- [ ] All TIER1 violations fixed
- [ ] All TIER2 violations fixed
- [ ] All TIER3 violations fixed or documented

## 📝 JAVADOC MANUAL DOCUMENTATION REQUIREMENT {#javadoc-manual-documentation-requirement}

JavaDoc comments require manual authoring with contextual understanding.

### Absolutely Prohibited {#absolutely-prohibited}

❌ Scripts/automation (Python, Bash, sed/awk/grep) to generate JavaDoc
❌ Generic templates without customization
❌ AI-generated JavaDoc without human review and contextualization
❌ Batch processing JavaDoc across multiple files
❌ Converting method names to comments (e.g., "testValidToken" → "Tests Valid Token")

### Required Approach {#required-approach}

✅ Read and understand method's purpose and implementation
✅ Explain WHY the test exists, not just WHAT it tests
✅ Include context about edge cases, boundary conditions, or regression prevention
✅ Document relationships between related tests

### Contextual Documentation {#contextual-documentation}

**BAD** (Generic):
```java
/**
 * Tests valid token.
 */
@Test
public void testValidToken() {
    // ...
}
```

**GOOD** (Contextual, explains purpose):
```java
/**
 * Verifies that Token correctly stores all components (type, start, end, text) and
 * calculates length as the difference between end and start positions.
 *
 * This test validates the fundamental Token record semantics that other components
 * rely on for parsing and formatting operations.
 */
@Test
public void testValidToken() {
    // ...
}
```

### Enforcement {#enforcement}

Pre-commit hook detects generic JavaDoc patterns. PMD.CommentRequired violations must be fixed, not suppressed.

## 📝 STYLE-SPECIFIC CODE POLICIES {#style-specific-code-policies}

### Code Comments {#code-comments}

Comments that contradict current implementation must be updated or removed. Implementation history belongs in git, not code comments.

```java
// ❌ BAD: "This used to use ArrayList but was changed to LinkedList for performance"
// ✅ GOOD: "Uses LinkedList for efficient insertions at arbitrary positions"
```

### TODO Comments {#todo-comments}

**Three Options** (choose one):
1. **Implement**: Fix the TODO immediately
2. **Remove**: If TODO is no longer relevant
3. **Document**: Convert to tracked task in todo.md with full context

**PROHIBITED**: Superficial renaming without action
```java
// ❌ BAD: Renamed "TODO" to "FIXME" or "FUTURE" without addressing
// ✅ GOOD: Created todo.md task: "Optimize parse() method for large inputs"
```

### TestNG Tests {#testng-tests}

**Thread-Safe Patterns Only**:
- ❌ **Prohibited**: `@BeforeMethod` (creates shared mutable state)
- ✅ **Required**: Create fresh instances in each test method

```java
// ❌ BAD
@BeforeMethod
public void setup() {
    parser = new Parser();  // Shared state
}

// ✅ GOOD
@Test
public void testParse() {
    Parser parser = new Parser();  // Fresh instance per test
    // ...
}
```

### Exception Types {#exception-types}

Choose exception type based on cause:

- **AssertionError**: Valid input reaches impossible state (our bug/invariant violation)
  ```java
  if (state == State.READY && !isInitialized()) {
      throw new AssertionError("READY state but not initialized");
  }
  ```

- **IllegalStateException**: Wrong API usage (user called method at wrong time)
  ```java
  if (!isStarted()) {
      throw new IllegalStateException("Must call start() before parse()");
  }
  ```

- **IllegalArgumentException**: Invalid input parameter
  ```java
  if (input == null) {
      throw new IllegalArgumentException("Input cannot be null");
  }
  ```

## References {#references}

- **Complete style rules**: [docs/code-style-human.md](../code-style-human.md) - Human-readable explanations
- **Automated patterns**: [docs/code-style/*.md](../code-style/) - Detection patterns for reviewers
- **Task protocol**: [task-protocol-agents.md](task-protocol-agents.md) - Agent coordination
- **Main agent guide**: [main-agent-coordination.md](main-agent-coordination.md) - Coordination patterns
