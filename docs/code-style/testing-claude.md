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
- `@Test\s*public\s+void\s+[a-z][a-zA-Z0-9]*\(\)`
- `parseExpressionWithInvalidTokenThrowsParseException`
- `methodConditionExpectedResult` pattern

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

### Risk-Based Test Coverage Philosophy
- `// RISK:.*→` (business risk documentation)
- `// IMPACT:.*-` (impact assessment)
- `// BUSINESS RULE:` (business rule validation)
- `// USER IMPACT:` (user workflow impact)
- `// BUG RISK:` (bug probability assessment)

### Priority 1: Business Logic Scenarios
- `mergeConfigs[A-Z][a-zA-Z0-9]*\(\)` (config merge business logic)
- `parseTomlWithInvalidBusinessRuleRejectsClearly`
- Business rule validation tests
- User workflow validation

### Priority 2: Happy Path Validation
- `parseTomlWithValidConfigReturnsCorrectObject`
- Standard use case validation
- Core feature validation

### Priority 3: Edge Cases
- `parseTomlWithMaxBoundaryValue[A-Z]`
- `loadConfigWithConcurrentAccess[A-Z]`
- Boundary value tests
- Concurrency tests

### Priority 4: Error Handling
- `parseTomlWithMalformedSyntaxProvidesActionableError`
- `try\s*\{[^}]*parser\.parse\([^)]*\);[^}]*fail\(.*Expected`
- Error message validation
- Actionable error patterns

### Metric-Driven Testing Anti-Patterns
- `testGetterReturnsField\(\)` (coverage-only test)
- `testPrivateHelperMethodLogic\(\)` (implementation detail test)
- Tests without business logic validation

### Risk-Driven Testing Best Practices
- `discoverConfigWithGitBoundaryStopsAtRepositoryRoot`
- Repository boundary validation
- Critical business rule tests
- User workflow impact tests

---

## 📋 VALIDATION RULES

**Critical Requirements:**
1. JPMS Compliance - Test module descriptors required
2. Parallel Safety - No shared mutable state
3. Isolation - Instance creation per test method
4. Thread Safety - Stateless utilities only
5. Risk Documentation - Business risk, impact, and bug risk comments

**Standard Requirements:**
1. Naming Convention - `method_condition_expectedResult`
2. Test Organization - TestNG groups for categorization
3. Builder Pattern - For complex test data creation
4. Resource Management - Read-only external data
5. Test Prioritization - Business logic > Happy path > Edge cases > Error handling

**Risk-Based Testing Requirements:**
1. Business Logic First - Critical workflows validated before edge cases
2. Risk Documentation - All high-priority tests document RISK, IMPACT, BUSINESS RULE
3. Bug Prevention Focus - Tests target real user-impacting bugs, not coverage metrics
4. Regression Tests - Every discovered bug gets a dedicated regression test
5. Actionable Errors - Error handling tests validate error message usefulness

**Detection Priority:**
- 🔴 CRITICAL: Parallel test violations, JPMS structure, missing business logic tests
- 🟡 STANDARD: Naming conventions, organization patterns, risk documentation
- 🟢 OPTIONAL: Advanced patterns, optimizations, coverage-only tests

---

**Related Documentation:**
- `testing-human.md` - Human-readable testing explanations
- `java-claude.md` - Java-specific Claude patterns
- `common-claude.md` - Universal code style patterns