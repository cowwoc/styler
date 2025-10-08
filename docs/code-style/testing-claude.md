# Testing Code Style Detection Patterns

**Claude Code optimization patterns for testing validation and detection**

This document contains testing patterns and detection rules optimized for Claude Code analysis and validation.

---

## 🔍 DETECTION PATTERNS

### JPMS-Compliant Test Structure
- `module.*\.test\s*\{`
- `src/test/java/module-info\.java`
- `io\.github\.styler\..*\.test`

### Parallel Test Execution Requirements
- `@BeforeTest|@BeforeMethod|@BeforeClass` (violations)
- `@AfterTest|@AfterMethod|@AfterClass` (violations)
- `private\s+(?:static\s+)?(?!final)\w+.*=` (mutable fields)
- `private\s+\w+.*calculator.*=` (shared instances)
- `private\s+(?:ConfigSearchPath|ErrorReporter|ConfigDiscovery)\s+\w+;` (shared test fixtures)
- `Files\.createTempDirectory\("(?!.*UUID\.randomUUID)` (temp files without UUID isolation)

### Parallel Test Safety Rules
- `@Test.*public\s+void.*\{[^}]*new\s+\w+Calculator\(\)`
- `static\s+final\s+` (immutable constants)
- Test method creating instances locally

### Test Naming and Structure
- `@Test\s*public\s+void\s+\w+_\w+_\w+\(\)`
- `parseExpression_withInvalidToken_throwsParseException`
- `method_condition_expectedResult` pattern

### Test Categories and Organization
- `@Test\s*\(.*groups\s*=\s*\{.*"(unit|integration|performance|fast|slow)".*\}`
- Test categorization patterns

### Parallel-Safe Test Data Creation
- `public\s+class\s+\w+TestBuilder\s*\{`
- `public\s+\w+TestBuilder\s+with\w+\(`
- `.build\(\)` method calls

### Parallel-Safe Test Utilities and Helpers
- `public\s+static\s+void\s+assert\w+\(`
- `TestUtils\.assert\w+\(`
- Stateless utility methods

### Parallel-Safe Test Data Management
- `loadTestCasesFromResource\(.*\.json.*\)`
- Read-only test data patterns

### Thread Safety Checklist
- `// ✅|❌` indicators
- `requireThat\(.*\)\.` assertions
- Instance creation within test methods

---

## 📋 VALIDATION RULES

**Critical Requirements:**
1. JPMS Compliance - Test module descriptors required
2. Parallel Safety - No shared mutable state
3. Isolation - Instance creation per test method
4. Thread Safety - Stateless utilities only

**Standard Requirements:**  
1. Naming Convention - `method_condition_expectedResult`
2. Test Organization - TestNG groups for categorization
3. Builder Pattern - For complex test data creation
4. Resource Management - Read-only external data

**Detection Priority:**
- 🔴 CRITICAL: Parallel test violations, JPMS structure
- 🟡 STANDARD: Naming conventions, organization patterns
- 🟢 OPTIONAL: Advanced patterns, optimizations

---

**Related Documentation:**
- `testing-human.md` - Human-readable testing explanations
- `java-claude.md` - Java-specific Claude patterns
- `common-claude.md` - Universal code style patterns