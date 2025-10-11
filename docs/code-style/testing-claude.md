# Testing Code Style Detection Patterns

**Detection rules optimized for Claude Code testing validation**

## 🔍 DETECTION PATTERNS

### JPMS-Compliant Test Structure
`module.*\.test\s*\{` | `src/test/java/module-info\.java` | `io\.github\.styler\..*\.test`

### Parallel Test Violations
**Prohibited**: `@Before/AfterTest|Method|Class` | Mutable fields: `private\s+(?:static\s+)?(?!final)\w+.*=` | Shared instances | Temp files without UUID
**Required**: Instance creation per test | `static final` immutable constants | UUID-isolated temp files

### Test Naming
`@Test\s*public\s+void\s+[a-z][a-zA-Z0-9]*\(\)` | Pattern: `methodConditionExpectedResult` | Example: `parseExpressionWithInvalidTokenThrowsParseException`

### Test Organization
Categories: `@Test\(.*groups\s*=\s*\{.*"(unit|integration|performance|fast|slow)".*\)` | Builder pattern: `\w+TestBuilder` with `.build()` | Stateless utilities: `public\s+static\s+void\s+assert\w+\(` | Read-only data: `loadTestCasesFromResource\(.*\.json.*\)`

### Thread Safety Indicators
`// ✅|❌` indicators | `requireThat\(.*\)\.` assertions | Instance creation within test methods

### Risk-Based Testing Patterns
**Documentation**: `// RISK:.*→` | `// IMPACT:.*-` | `// BUSINESS RULE:` | `// USER IMPACT:` | `// BUG RISK:`
**Priority 1 (Business Logic)**: `mergeConfigs[A-Z][a-zA-Z0-9]*\(\)` | Business rule validation | User workflow tests
**Priority 2 (Happy Path)**: `parseTomlWithValidConfigReturnsCorrectObject` | Standard use cases | Core features
**Priority 3 (Edge Cases)**: `parseTomlWithMaxBoundaryValue[A-Z]` | Boundary values | Concurrency: `loadConfigWithConcurrentAccess[A-Z]`
**Priority 4 (Error Handling)**: `parseTomlWithMalformedSyntaxProvidesActionableError` | Error message validation | Actionable errors

### Anti-Patterns
`testGetterReturnsField\(\)` (coverage-only) | `testPrivateHelperMethodLogic\(\)` (implementation detail) | Tests without business logic validation

## 📋 VALIDATION RULES

**Critical**: JPMS compliance (test module descriptors) | Parallel safety (no shared mutable state) | Isolation (instance per test) | Thread safety (stateless utilities) | Risk documentation (RISK, IMPACT, BUSINESS RULE comments)

**Standard**: Naming (`method_condition_expectedResult`) | TestNG groups | Builder pattern | Read-only data | Priority: Business logic > Happy path > Edge cases > Error handling

**Risk-Based Testing**: Business logic first | Risk docs for high-priority tests | Bug prevention focus (not coverage metrics) | Regression test per bug | Actionable error messages

**Detection Priority**: 🔴 CRITICAL (parallel violations, JPMS, missing business logic) | 🟡 STANDARD (naming, organization, risk docs) | 🟢 OPTIONAL (advanced patterns, optimizations)

**Related**: `testing-human.md` | `java-claude.md` | `common-claude.md`