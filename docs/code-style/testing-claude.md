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

### Test Class Declaration
- ✅ `public\s+final\s+class\s+\w+Test` - Correct test class declaration
- ❌ `^class\s+\w+Test` - Missing public final (package-private)
- ❌ `public\s+class\s+\w+Test[^a-zA-Z]` - Missing final modifier

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

### Test Value Anti-Patterns (Meaningless Tests)
- `shouldHave\w+Value\(\)` (compile-time guarantee tests)
- `shouldHaveExactly\w+Values\(\)` (implementation detail tests)
- `shouldContainAllExpected\w+\(\)` (enum/collection structure tests)
- `\.values\(\)\)\.hasSize\(` (enum count assertions)
- `\.values\(\)\)\.containsExactly\(` (enum structure assertions)
- `assertThat\(\w+\.\w+\)\.isNotNull\(\)` without business logic (trivial null checks)
- `shouldReturn\w+List\(\)` testing collection type (implementation detail)
- Tests verifying Java language guarantees (record fields, enum values exist)

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
6. Test Framework - TestNG only (NOT JUnit)
7. Test Value - No compile-time guarantee or implementation detail tests

### Test Dependency Detection
- ✅ `org\.testng` - Correct test framework
- ❌ `org\.junit` - Wrong framework, use TestNG
- ✅ `io\.github\.cowwoc\.requirements12` - Correct assertion library (Requirements API)
- ❌ `org\.assertj` - Wrong assertion library, use Requirements API
- ✅ `logback-classic` - Required for test logging
- ✅ `logback-test\.xml` - Required configuration file

### Assertion Pattern Detection
- ✅ `requireThat\(` - Correct assertion pattern (Requirements API)
- ❌ `assertThat\(` - Wrong assertion pattern, use requireThat

**Standard Requirements:**
1. Test Class Declaration - `public final class *Test`
2. Naming Convention - `method_condition_expectedResult`
3. Test Organization - TestNG groups for categorization
4. Builder Pattern - For complex test data creation
5. Resource Management - Read-only external data
6. Test Prioritization - Business logic > Happy path > Edge cases > Error handling

**Risk-Based Testing Requirements:**
1. Business Logic First - Critical workflows validated before edge cases
2. Risk Documentation - All high-priority tests document RISK, IMPACT, BUSINESS RULE
3. Bug Prevention Focus - Tests target real user-impacting bugs, not coverage metrics
4. Regression Tests - Every discovered bug gets a dedicated regression test
5. Actionable Errors - Error handling tests validate error message usefulness

**Detection Priority:**
- 🔴 CRITICAL: Parallel test violations, JPMS structure, missing business logic tests, meaningless tests
- 🟡 STANDARD: Naming conventions, organization patterns, risk documentation
- 🟢 OPTIONAL: Advanced patterns, optimizations

---

**Related Documentation:**
- `testing-human.md` - Human-readable testing explanations
- `java-claude.md` - Java-specific Claude patterns
- `common-claude.md` - Universal code style patterns