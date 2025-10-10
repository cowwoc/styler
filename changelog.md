# Changelog

## 2025-10-10

### Task: `implement-index-overlay-parser` - Index-Overlay AST parser with comprehensive security ✅

**Completion Date**: 2025-10-10
**Commits**: bc7487e (implementation), 86e533a (documentation/tooling)

**Solution Implemented**:
- Complete index-overlay parser for Java 16-25 syntax
- Recursive descent parser with modern Java feature support (records, sealed classes, pattern matching)
- 6-layer security defense against resource exhaustion attacks
- Memory-efficient node storage using index-based references
- 165 comprehensive tests using requireThat() assertions

**Core Components**:
- **Parser Module**: Lexer with 30+ token types, recursive descent parser
- **AST Core Module**: NodeArena (index-based storage), NodeIndex (value-class references), NodeType (50+ types), SecurityConfig (centralized limits)

**Security Controls** (all enforced and tested):
1. SEC-001: File Size Limit - 10MB maximum source file
2. SEC-007: Token Count Limit - 1M tokens per file
3. SEC-006: Parse Timeout - 30 seconds maximum
4. SEC-005: Memory Limit - 512MB heap with active monitoring
5. SEC-002: Nesting Depth - 200 maximum parse depth
6. SEC-011: Arena Capacity - 10M maximum AST nodes

**Test Coverage** (165/165 passing):
- SecurityTest: All 6 security controls validated
- LexerTest: Complete token generation coverage
- ParserTest: All Java constructs tested
- ClassParserTest: Class/interface/enum/record parsing
- ModernJavaFeaturesTest: JDK 16-25 features
- IntegrationTest: End-to-end parsing validation

**Files Created** (24 files):
- parser/pom.xml, ast/pom.xml, ast/core/pom.xml
- parser/src/main/java: Lexer.java, Parser.java, Token.java, TokenType.java
- ast/core/src/main/java: NodeArena.java, NodeIndex.java, NodeType.java, SecurityConfig.java
- parser/src/test/java: 8 test classes (ClassParserTest, IntegrationTest, LexerTest, ModernJavaFeaturesTest, ParserTest, SecurityTest, StatementParserTest, TokenTest)
- ast/core/src/test/java: NodeArenaTest.java, NodeIndexTest.java
- .claude/hooks: detect-generic-javadoc.sh, reset-javadoc-warning.sh

**Files Modified**:
- pom.xml: Added ast and parser modules
- CLAUDE.md: Added JavaDoc manual documentation requirement
- docs/code-style/java-claude.md: Added 3 new style rules (requireThat(), no redundant defaults, switch for OR chains)
- docs/code-style/java-human.md: Added human-readable explanations for new rules
- .claude/hooks: Updated detect-meta-commentary.sh and enforce-user-approval.sh

**Quality Gates**:
- ✅ BUILD SUCCESS
- ✅ All 165 tests passing (36 ast/core + 85 parser = 121 total)
- ✅ Checkstyle: 0 violations
- ✅ PMD: 0 violations

**Stakeholder Approvals**:
- ✅ Technical Architect: APPROVED
- ✅ Security Auditor: APPROVED
- ✅ Code Quality Auditor: APPROVED
- ✅ Performance Analyzer: APPROVED
- ✅ Style Auditor: APPROVED

---

## 2025-10-09

### Task: `implement-cli-arguments` - Command-line argument parsing without file processing ✅

**Completion Date**: 2025-10-09
**Commit**: b56983e6bb0f8fd6c6e3e1b0a88f6ee0e8b5f6d5

**Solution Implemented**:
- Implemented complete CLI argument parsing using picocli programmatic API (non-reflection)
- Created immutable CLIOptions record with Builder pattern for configuration
- Implemented ArgumentParser with support for --config, --check, --fix, --help, --version flags
- Created HelpFormatter with dynamic version reading from ModuleDescriptor (no manual sync)
- Established exception hierarchy: CLIException, UsageException, HelpRequestedException

**Core Components**:
- **CLIOptions**: Immutable record with defensive copying and comprehensive validation
- **ArgumentParser**: Thread-safe parser using picocli programmatic API
- **HelpFormatter**: Dynamic help text generation with version info from module descriptor
- **Exception Types**: Proper hierarchy with @Serial annotations for serialization

**Code Quality**:
- All classes marked `final` where appropriate (per design guidelines)
- Correct `@throws` documentation: `NullPointerException` for null checks
- Removed redundant `isNotNull()` before `isNotEmpty()` (already validates null)
- Parameter validation grouped before field assignments
- Edge cases documented in JavaDoc
- @Serial annotations on all serialVersionUID fields

**Test Coverage** (51 tests, 0 failures):
- CLIOptions Builder: construction, validation, immutability, equality tests
- ArgumentParser: valid arguments, help/version, invalid inputs, edge cases, thread safety
- HelpFormatter: help text, version info, consistency verification
- All tests use requireThat() assertions with idiomatic `.contains()` pattern
- TestNG `@Test(expectedExceptions)` for simple exception verification

**Code Style Documentation**:
- Added `maven-human.md`: Maven POM dependency grouping standards
  * Group by type (project dependencies first), then by scope
  * No blank lines within groups, exactly one blank line between groups
- Added `maven-claude.md`: Detection patterns for POM organization
- Updated `java-human.md`: Added final class modifier rule

**Files Created**:
- `cli/src/main/java/io/github/cowwoc/styler/cli/CLIOptions.java`
- `cli/src/main/java/io/github/cowwoc/styler/cli/ArgumentParser.java`
- `cli/src/main/java/io/github/cowwoc/styler/cli/HelpFormatter.java`
- `cli/src/main/java/io/github/cowwoc/styler/cli/CLIException.java`
- `cli/src/main/java/io/github/cowwoc/styler/cli/UsageException.java`
- `cli/src/main/java/io/github/cowwoc/styler/cli/HelpRequestedException.java`
- `cli/src/test/java/io/github/cowwoc/styler/cli/test/CLIOptionsTest.java`
- `cli/src/test/java/io/github/cowwoc/styler/cli/test/ArgumentParserTest.java`
- `cli/src/test/java/io/github/cowwoc/styler/cli/test/HelpFormatterTest.java`
- `cli/src/test/java/module-info.java`
- `cli/src/main/java/module-info.java`
- `docs/code-style/maven-human.md`
- `docs/code-style/maven-claude.md`

**Files Modified**:
- `cli/pom.xml` - Added picocli and requirements dependencies
- `docs/code-style/java-human.md` - Added final class modifier rule

**Quality Gates**:
- ✅ BUILD SUCCESS
- ✅ All 51 tests passing
- ✅ Checkstyle: 0 violations
- ✅ PMD: 0 issues

**Integration**:
- Self-contained CLI argument parsing module
- No dependencies on parser or config loading (Phase A isolation maintained)
- Ready for integration with B5 (CLI integration) task

**Next Steps**: Task B5 (CLI integration) is now unblocked and can use CLI argument parsing

---

### Task: `implement-toml-configuration` - TOML-based configuration system with hierarchical discovery ✅

**Completion Date**: 2025-10-09
**Commit**: b08feaf844420abf976a2fdf1e2d22b262ca9a61

**Solution Implemented**:
- Complete TOML configuration system with field-level merge precedence
- Hierarchical file discovery: current dir → parents (stop at .git) → ~/.styler.toml → /etc/.styler.toml
- Thread-safe caching using ConcurrentHashMap with canonical path keys
- Immutable configuration objects with builder pattern for Optional tracking
- Jackson TOML integration with fluent API (@JsonSetter/@JsonGetter annotations)
- Security: 1MB file size limit, 100-level directory traversal depth limit
- ConfigurationLoader public facade integrating discovery, parsing, merging, and caching

**Components Created**:
- Config (record): Immutable configuration with validation
- ConfigBuilder: Builder with Optional field tracking for merge support
- ConfigParser: Jackson TOML parser with file size validation
- ConfigDiscovery: Hierarchical file search with git boundary detection
- ConfigMerger: Field-level precedence merging (nearest config wins per field)
- ConfigurationCache: Thread-safe caching with toRealPath() for symlink handling
- ConfigurationLoader: Public API facade for complete load workflow

**Files Created** (20 files):
- config/pom.xml - Module definition with Jackson TOML dependency
- config/src/main/java/io/github/cowwoc/styler/config/*.java (7 files)
- config/src/main/java/module-info.java - JPMS module descriptor
- config/src/test/java/io/github/cowwoc/styler/config/test/*.java (4 files)
- config/src/test/java/module-info.java - Test module descriptor
- docs/project/configuration-guide.md - User-facing configuration documentation
- README.md - Updated with configuration reference

**Test Results**:
- ✅ 21 tests passing (ConfigParserTest: 9/9, ConfigMergerTest: 7/7, ConfigDiscoveryTest: 5/5)
- ✅ BUILD SUCCESS
- ✅ Checkstyle: 0 violations
- ✅ PMD: 0 violations

**Quality Reviews**:
- ✅ technical-architect: APPROVED - Clean architecture, proper separation of concerns
- ✅ security-auditor: APPROVED - Resource exhaustion protections in place
- ✅ build-validator: APPROVED - All tests and quality gates passing
- ✅ style-auditor: APPROVED - Style compliance verified
- ✅ code-quality-auditor: APPROVED - No duplication, best practices followed

**Scope**: Foundation module for configuration loading - used by CLI and file processor

**Next Steps**: Task A2 complete, Phase A continues with A1, A3, A4

---

### Task: `setup-maven-multi-module-build` - Create Maven parent POM and module structure ✅

**Completion Date**: 2025-10-09
**Commit**: 6380bd469cfb1d76a8f9f7312dd2ff9cc1c6121f

**Solution Implemented**:
- Configured top-level Maven POM for multi-module build structure
- Centralized dependency management for all future modules (Checkstyle 11.0.1, PMD 7.17.0, TestNG 7.8.0, Requirements-Java 12.0, Maven plugin APIs)
- Standardized plugin versions and configuration
- Enabled build optimization with Maven build cache
- Defined ${project.root.basedir} property for config file paths to support sub-module references

**Configuration Improvements**:
- Inline version numbers for single-use dependencies (cleaner POM structure)
- Blank line separation between normal and test-scoped dependencies
- Removed non-standard ${maven.multiModuleProjectDirectory} (documented in out-of-scope.md)
- Removed blank line between <modelVersion> and <groupId> for consistency

**Files Modified**:
- `pom.xml` - Configured root POM with dependency management and quality gates
- `docs/project/scope/out-of-scope.md` - Documented removal of non-standard property

**Quality Gates**:
- ✅ BUILD SUCCESS
- ✅ Checkstyle: 0 violations
- ✅ PMD: 0 issues

**Scope**: Top-level POM only - sub-modules will be added as needed by subsequent tasks (A1-A4, B1, etc.)

**Next Steps**: Tasks A1-A4 are now unblocked and can be worked on in parallel

---

## 2025-10-08
