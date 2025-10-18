# Style Guide for Code Formatting

> **Version:** 1.0 | **Last Updated:** 2025-10-18
> **Audience:** style-reviewer, style-updater, main agent (during VALIDATION)
> **Purpose:** Style validation requirements and JavaDoc documentation standards

## 🎯 COMPLETE STYLE VALIDATION

**MANDATORY PROCESS**: Style guide consists of THREE components (checkstyle + PMD + manual rules)

**CRITICAL ERROR**: Checking only checkstyle when PMD/manual violations exist

**Automated Guidance**: `smart-doc-prompter.sh` hook injects 3-component checklist

**Fixing Conflicts**: `checkstyle/fixers` module handles LineLength vs UnderutilizedLines conflicts

### Validation Components

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

### Validation Commands

```bash
# Full style validation (all three components)
./mvnw checkstyle:check pmd:check

# Individual components
./mvnw checkstyle:check
./mvnw pmd:check

# Review manual rules
cat docs/code-style-human.md
```

### Complete Validation Checklist

Before declaring style validation complete:

- [ ] Checkstyle passes: `./mvnw checkstyle:check`
- [ ] PMD passes: `./mvnw pmd:check`
- [ ] Manual rules reviewed from code-style-human.md
- [ ] All TIER1 violations fixed
- [ ] All TIER2 violations fixed
- [ ] All TIER3 violations fixed or documented

## 📝 JAVADOC MANUAL DOCUMENTATION REQUIREMENT

**CRITICAL POLICY**: JavaDoc comments require manual authoring with contextual understanding.

### Absolutely Prohibited

❌ Using scripts (Python, Bash, etc.) to generate JavaDoc comments
❌ Using sed/awk/grep to automate JavaDoc insertion
❌ Copy-pasting generic JavaDoc templates without customization
❌ AI-generated JavaDoc without human review and contextualization
❌ Batch processing JavaDoc across multiple files
❌ Converting method names to comments (e.g., "testValidToken" → "Tests Valid Token")

### Required Approach

✅ Read and understand the method's purpose and implementation
✅ Write JavaDoc that explains WHY the test exists, not just WHAT it tests
✅ Include context about edge cases, boundary conditions, or regression prevention
✅ Explain the significance of specific test scenarios
✅ Document relationships between related tests

### Example - Contextual Documentation

**BAD** (Generic, derived from method name):
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

### Enforcement

- Pre-commit hook detects generic JavaDoc patterns
- Code reviews check for contextual understanding in comments
- PMD.CommentRequired violations must be fixed, not suppressed

## 📝 STYLE-SPECIFIC CODE POLICIES

### Code Comments

**Update Outdated Comments**:
- Comments that contradict current implementation must be updated or removed
- Implementation history belongs in git, not code comments

**Avoid Implementation History in Comments**:
```java
// BAD: "This used to use ArrayList but was changed to LinkedList for performance"
// GOOD: "Uses LinkedList for efficient insertions at arbitrary positions"
```

### TODO Comments

**Three Options** (choose one):
1. **Implement**: Fix the TODO immediately
2. **Remove**: If TODO is no longer relevant
3. **Document**: Convert to tracked task in todo.md with full context

**PROHIBITED**: Superficial renaming without action
```java
// ❌ BAD: Renamed "TODO" to "FIXME" or "FUTURE" without addressing
// ✅ GOOD: Created todo.md task: "Optimize parse() method for large inputs"
```

### TestNG Tests

**Thread-Safe Patterns Only**:
- ❌ **Prohibited**: `@BeforeMethod` (creates shared mutable state)
- ✅ **Required**: Create fresh instances in each test method

**Example**:
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

### Exception Types

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

## References

- **Complete style rules**: [docs/code-style-human.md](../code-style-human.md) - Human-readable explanations
- **Automated patterns**: [docs/code-style/*.md](../code-style/) - Detection patterns for reviewers
- **Task protocol**: [task-protocol-agents.md](task-protocol-agents.md) - Agent coordination
- **Main agent guide**: [main-agent-coordination.md](main-agent-coordination.md) - Coordination patterns
