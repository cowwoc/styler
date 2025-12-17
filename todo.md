# TODO List - Styler Java Code Formatter (Main Branch)

## 🚀 READY TO WORK NOW (Multi-Instance Coordination)

**Current Status**: Phase B in progress (B6 ready), Phase C tasks C4-C6 unblocked

**COMPLETED**:
- B1a: Line Length Formatter ✅ COMPLETE
- B1b: Import Organization ✅ COMPLETE
- B2: File Processing Pipeline Infrastructure ✅ COMPLETE
- B2.5: Pipeline Stage Implementation ✅ COMPLETE (2025-12-13)
- B3: AI Violation Output ✅ COMPLETE (2025-12-05)
- B4: Error Catalog ✅ COMPLETE (2025-12-05)
- B5: CLI Integration ✅ COMPLETE (2025-12-09)
- C1: File Discovery ✅ COMPLETE (2025-12-09)
- C2: Virtual Thread Processing ✅ COMPLETE (2025-12-10)
- C3a: Brace Formatting ✅ COMPLETE (2025-12-10)
- C3b: Whitespace Formatting ✅ COMPLETE (2025-12-11)
- C3c: Indentation Formatting ✅ COMPLETE (2025-12-11)

**Phase B**: ✅ COMPLETE (8/8 tasks)
**Phase C**: In progress (3/6 tasks - C4, C5, C6 now unblocked)

**Phase A - ✅ COMPLETE (5/5 tasks)**:
- ✅ A0: styler-formatter module (defines FormattingRule interfaces)
- ✅ A1: Index-Overlay AST Parser (ast + parser modules)
- ✅ A2: TOML Configuration (config module)
- ✅ A3: CLI Arguments (cli module)
- ✅ A4: Security Framework (security module)

---

## 🔒 Task Coordination (Multi-Instance Work)

**Lock System**: Before starting ANY task, acquire lock per `docs/project/task-protocol-core.md`
- Lock file: `/workspace/locks/{task-name}.json`
- Lock contains: `session_id`, `task_name`, `state`, `created_at`
- **ONLY work on tasks where lock contains YOUR session_id**

**Task Status Indicators**:
- `[ ] READY:` - No dependencies, can start immediately
- `[ ] BLOCKED:` - Dependencies must complete first
- `[ ] IN_PROGRESS:` - Someone is working on it (check `/workspace/locks/`)

**Task Completion** (per CLAUDE.md task archival policy):
- Completed tasks are REMOVED from todo.md (entire entry deleted)
- Completed tasks are ADDED to changelog.md under completion date
- When checking dependencies: search BOTH todo.md AND changelog.md

**Before Starting a Task**:
1. ✅ Verify task status is `READY` (not BLOCKED)
2. ✅ Check `/workspace/locks/` - if lock exists with different session_id, task is taken
3. ✅ Acquire lock via task-protocol-core.md INIT state
4. ✅ Update task status to `IN_PROGRESS` in todo.md
5. ✅ Create isolated worktree: `/workspace/branches/{task-name}/code`
6. ✅ Begin work following full 7-phase task protocol

**Parallel Work Opportunities**:
- **Phase A**: INCOMPLETE - A0 never implemented, A1-A4 complete
- **Phase B**: BLOCKED - B1 tasks require A0 (styler-formatter module)
- **Phase C**: BLOCKED - Depends on Phase B completion

---

## 🎯 STREAMLINED IMPLEMENTATION STRATEGY

**Philosophy**: Build a working AI-integrated Java formatter with proven architecture patterns:
- ✅ Index-Overlay AST with Arena API for memory efficiency
- ✅ Prioritize AI agent integration (primary differentiator and use case)
- ✅ Build to 5 formatting rules for realistic concurrency benchmarking
- ✅ Earlier real-world validation (Maven plugin, parallel processing in Phase C)
- ✅ Complete what we start (no stubbing, no TODOs)

**Dependency-First Ordering**: Each phase builds on previous phases without creating stubs or placeholders.

---

## Phase A: Foundation (Zero External Dependencies) ✅ COMPLETE

**Goal**: Build core parsing, configuration, and security infrastructure without depending on formatters or AI
integration.

**Status**: All Phase A tasks complete (A0-A4).

---

## Phase B: AI Integration (Vertical Slice)

**Goal**: Build complete end-to-end pipeline from CLI → parse → format → AI feedback output. This phase
delivers working AI agent integration.

**Status**: ✅ COMPLETE (8/8 tasks).

**Coordination**: B1 tasks can run in parallel (2 instances). After B1 completes,
B2-B5 have sequential dependencies.

### B1. Minimal Formatting Rules (MVP) ✅ COMPLETE
- [x] **COMPLETE:** `implement-line-length-formatter` - Context-aware line wrapping with AST integration (2025-12-02)

- [x] **COMPLETE:** `implement-import-organization` - Import grouping and unused import removal (2025-12-03)

### B1.5. Classpath Infrastructure ✅ COMPLETE (2025-12-15)

- [x] **COMPLETE:** `add-classpath-support` - Enable passing project classpath/modulepath into Styler (2025-12-15)

- [ ] **READY:** `resolve-wildcard-imports` - Enhance import organization with wildcard resolution
  - **Dependencies**: `implement-import-organization` ✅ COMPLETE, `add-classpath-support` ✅ COMPLETE
  - **Blocks**: None (optional enhancement)
  - **Parallelizable With**: B2 and beyond (independent enhancement)
  - **Estimated Effort**: 2-3 days
  - **Purpose**: Resolve `import java.util.*` to determine which classes are actually used
  - **Scope**: Extend ImportAnalyzer to use classpath for wildcard import analysis
  - **Components**:
    - WildcardResolver: Scan classpath for classes matching wildcard patterns
    - ImportAnalyzer enhancement: Use classpath to detect unused wildcard imports
    - Optional: Expand wildcards to explicit imports (configurable)
  - **Features**:
    - Detect unused wildcard imports when classpath available
    - Optionally expand wildcards to explicit imports
    - Fall back to conservative mode when no classpath provided
  - **Integration**: Extends ImportAnalyzer from `implement-import-organization`
  - **Quality**: Comprehensive tests with sample classpaths, edge cases for nested classes

### B2. File Processing Pipeline Infrastructure ✅ COMPLETE
- [x] **COMPLETE:** `implement-file-processing-pipeline` - Pipeline infrastructure (2025-12-04)
  - **Delivered**: FileProcessingPipeline class, StageResult types, ProcessingContext, AbstractPipelineStage
  - **Note**: Stage implementations return `StageResult.Skipped` - see B2.5 for actual implementation

### B2.5. Pipeline Stage Implementation (Critical Path)
- [x] **COMPLETE:** `implement-pipeline-stages` - Wire parser and formatters into pipeline stages (2025-12-13)
  - **Dependencies**: A1 ✅ (parser), B1 ✅ (2 rules), C3 ✅ (3 rules), B2 ✅ (pipeline infrastructure)
  - **Blocks**: C4, C5, C6, D1 (all need functional pipeline to measure real work)
  - **Parallelizable With**: None (critical path)
  - **Estimated Effort**: 2-3 days
  - **Purpose**: Make the pipeline actually process files (currently all stages return Skipped)
  - **Scope**: Implement the 4 pipeline stages to perform real work
  - **Components**:
    - **ParseStage**: Use A1 parser to parse Java files into AST
      - Input: File path from ProcessingContext
      - Output: Parsed AST stored in ProcessingContext
      - Error handling: Return StageResult.Failure for parse errors
    - **FormatStage**: Apply all 5 formatting rules to parsed AST
      - Input: AST from ParseStage
      - Apply: LineLengthFormattingRule, ImportOrganizerFormattingRule, BraceFormattingRule,
        WhitespaceFormattingRule, IndentationFormattingRule
      - Output: Formatted source code and/or list of violations
    - **ValidationStage**: Collect and aggregate violations from formatting
      - Input: Violations from FormatStage
      - Output: Validated violation list with severity/priority
    - **OutputStage**: Write formatted output or report violations
      - Validation-only mode: Report violations via B3 AI output
      - Fix mode: Write formatted code back to file
  - **Integration**: Connects A1 parser, all formatting rules (B1 + C3), B3 violation output
  - **Verification**: Run `styler check` on a real Java file and see actual violations
  - **Quality**: Integration tests with real Java files, verify end-to-end processing

### B3. Structured Violation Output (AI Agent Integration) ✅ COMPLETE
- [x] **COMPLETE:** `implement-ai-violation-output` - Structured violation feedback for AI agents (2025-12-05)

### B3.5. Proactive Rules Summary Export
- [ ] **READY:** `implement-rules-summary-export` - Export formatting rules as markdown for AI pre-guidance
  - **Dependencies**: B3 ✅ COMPLETE (AI output infrastructure), B1 ✅ (formatters define rules)
  - **Blocks**: None (optional enhancement)
  - **Parallelizable With**: B4, B5
  - **Estimated Effort**: 1-2 days
  - **Purpose**: Allow AI agents to request formatting expectations before writing code
  - **Scope**: Markdown/JSON export of configured formatting rules and their settings
  - **Components**:
    - RulesSummaryExporter: Iterate FormattingRule instances, output descriptions
    - Output formats: Markdown (human-readable), JSON (machine-readable)
  - **Output Example**:
    - "Lines must be ≤120 characters"
    - "Imports grouped by: java → javax → third-party → project"
    - Rule configurations and thresholds
  - **Integration**: CLI flag `--explain-rules` or API method
  - **Quality**: Clear, actionable guidance for proactive compliance

### B4. Error Message Catalog ✅ COMPLETE
- [x] **COMPLETE:** `create-error-message-catalog` - Comprehensive error messages for AI and human users (2025-12-05)

### B5. CLI Integration ✅ COMPLETE
- [x] **COMPLETE:** `implement-cli-formatter-integration` - Wire CLI → pipeline → output (2025-12-09)
  -  **Integration**: Connects all Phase A and Phase B components (A3 CLI args, B2 pipeline, B3 output, B4
    errors)
  - **Quality**: Clear error messages, proper exit codes, progress reporting
  - **Estimated Effort**: 2-3 days

### B6. Multi-Configuration Architecture ✅ COMPLETE (2025-12-14)
- [x] **COMPLETE:** `implement-multi-config-architecture` - Enable formatting rules to receive all configurations (2025-12-14)

---

## Phase C: Scale & Real-World Testing

**Goal**: Scale to large codebases with parallel processing, build to 5 formatting rules for realistic
benchmarking, and validate with Maven plugin integration.

### C1. File Discovery ✅ COMPLETE (2025-12-09)

### C2. Virtual Thread Processing (Thread-per-File Baseline) ✅ COMPLETE (2025-12-10)

### C3. Additional Formatting Rules (Build to 5 Total Rules) ✅ COMPLETE

- [x] **COMPLETE:** `implement-indentation-formatting` - Indentation formatting (tabs/spaces/mixed) (2025-12-11)

### C4. Concurrency Model Benchmark
- [ ] **READY:** `benchmark-concurrency-models` - Compare thread-per-file vs thread-per-block parallelism
  - **Dependencies**: B2.5 ✅ (functional pipeline), C2 ✅ (thread-per-file baseline), C3 ✅ (3
    rules), B1 ✅ (2 rules)
  - **Blocks**: C5 (Maven plugin should use optimal concurrency model if thread-per-block wins)
  - **Parallelizable With**: None (needs functional pipeline first)
  - **Estimated Effort**: 2-3 days
  - **Purpose**: Determine optimal concurrency strategy for styler through empirical testing
  - **Scope**: Benchmark thread-per-file (baseline from C2) vs thread-per-block concurrency
  - **Trigger Criteria**: Execute ONLY when `styler check` processes real Java files and reports actual
    violations (proving pipeline stages are functional)
  - **Prerequisites**: B2.5 complete (pipeline does real work), 5 formatting rules integrated
  - **Comparison Approaches**:
    - **Thread-per-file (baseline)**: One virtual thread per file (current C2 implementation)
    - **Thread-per-block**: Virtual threads for method-level parallelism within files
  - **Test Workload**: Real codebases (Spring Framework, Guava, Apache Commons) with all 5 formatting rules
  - **Benchmark Metrics**:
    - Total processing time (wall clock end-to-end)
    - Throughput (files/second)
    - Memory usage (heap pressure, GC frequency)
    - CPU utilization (actual vs theoretical max)
    - Thread coordination overhead
  - **Test Scenarios**:
    - File size variations (small <200 LOC, medium 200-1000 LOC, large >1000 LOC)
    - Concurrency levels (unlimited, capped at 100/500/1000)
    - Memory constraints (512MB, 1GB, 2GB heap)
  - **Decision Criteria**: Implement thread-per-block only if >20% improvement over thread-per-file baseline
  - **Output**: Benchmark report with recommendation for production concurrency model
  - **Integration**: Uses C2 (thread-per-file), all formatters (B1 + C3), real-world projects
  - **Quality**: Statistical rigor, JMH methodology, 95% confidence intervals

### C5. Maven Plugin (Early Real-World Testing)
- [ ] **READY:** `create-maven-plugin` - Maven plugin for build system integration
  - **Dependencies**: B2.5 ✅ (functional pipeline), C1 ✅ (file discovery), C2 ✅ (parallel processing), B5 ✅
    (CLI integration)
  -  **Blocks**: C6 (performance benchmarking uses Maven plugin), D1 (regression tests use Maven plugin), D2
    (CI/CD uses Maven plugin)
  - **Parallelizable With**: C4 (concurrency benchmark can run independently)
  - **Estimated Effort**: 2-3 days
  - **Purpose**: Integrate styler into Maven builds for automated formatting
  - **Scope**: Maven plugin with check/format goals, configuration integration
  - **Components**:
    - StylerCheckMojo: Validate formatting without changes
    - StylerFormatMojo: Auto-fix formatting violations
    - MavenConfigAdapter: Bridge Maven config to styler config
    - GoalConfiguration: Maven-specific settings
  - **Goals**: styler:check (validate), styler:format (fix), styler:help (usage)
  - **Integration**: Uses CLI as dependency, Maven lifecycle integration
  - **Real-World Testing**: Validate on actual Java projects (Spring, Guava, Commons)
  - **Quality**: Incremental builds, build cache support, clear error reporting

### C6. Performance Benchmarking
- [ ] **BLOCKED:** `create-jmh-benchmarks` - Validate performance claims with JMH benchmarks
  - **Dependencies**: B2.5 ✅ (functional pipeline), C5 (Maven plugin for running benchmarks), C2 ✅ (parallel
    processing), all formatters (B1 + C3) ✅
  -  **Blocks**: D1 (testing uses benchmarks for performance regression detection), D2 (CI/CD runs benchmark
    comparisons)
  - **Parallelizable With**: C4 (concurrency benchmark is separate empirical study)
  - **Estimated Effort**: 3-4 days
  - **Purpose**: Measure and validate parsing throughput, memory usage, scalability
  - **Scope**: JMH benchmark suite covering all scope.md performance targets
  - **Benchmarks**:
    - ParsingThroughputBenchmark: ≥10,000 tokens/sec
    - MemoryUsageBenchmark: ≤512MB per 1000 files
    - FormattingThroughputBenchmark: ≥100 files/sec
    - ScalabilityBenchmark: Linear scaling to 32 cores
    - VirtualThreadComparisonBenchmark: Virtual vs platform threads
    - RealWorldProjectBenchmark: Spring Framework, Guava, JUnit5
  - **Configuration**: Fork=3, proper warmup/measurement, 95% confidence intervals
  - **Integration**: Separate benchmark module, uses production code
  - **Quality**: Statistical rigor, comprehensive coverage

---

## Phase D: Polish & Production Readiness

**Goal**: Production-ready release with testing, CI/CD, and documentation.

### D1. Comprehensive Testing
- [ ] **BLOCKED:** `add-regression-test-suite` - Real-world Java project regression tests
  - **Dependencies**: B2.5 (functional pipeline), C5 (Maven plugin for running styler), all formatters (B1 +
    C3) ✅
  - **Blocks**: D2 (CI/CD needs complete test suite)
  - **Parallelizable With**: D3 (documentation can be written in parallel)
  - **Estimated Effort**: 2-3 days (for regression suite)
  - **Purpose**: Prevent regressions by testing against real-world Java projects
  - **Scope**: Curated Java projects with before/after formatting comparisons
  - **Projects**: Spring Framework, Apache Commons, Guava, JUnit5, Mockito
  - **Coverage**: Various Java versions, coding styles, edge cases, large files
  - **Integration**: Automated regression testing in CI/CD pipeline
  - **Quality**: Golden file testing, failure analysis

- [ ] **BLOCKED:** `add-cli-integration-tests` - End-to-end CLI integration tests
  - **Dependencies**: B2.5 (functional pipeline), B5 ✅ (CLI integration), C1 ✅ (file discovery), C2 ✅
    (parallel processing)
  - **Blocks**: D2 (CI/CD needs complete test suite)
  - **Parallelizable With**: D1 (regression suite), D3 (documentation)
  - **Estimated Effort**: 1-2 days
  - **Purpose**: Validate complete CLI functionality with real Java files
  - **Scope**: Integration tests covering CLI arguments, file processing, output
  - **Scenarios**: Single files, directories, config variants, error conditions
  - **Integration**: Uses real Java files, temporary directories, process execution
  - **Quality**: Comprehensive scenario coverage, clear assertions

### D2. CI/CD Pipeline
- [ ] **BLOCKED:** `setup-github-actions-ci` - Automated testing and release pipeline
  -  **Dependencies**: D1 (all tests: regression + CLI integration), C6 (performance benchmarks), C5 (Maven
    plugin)
  - **Blocks**: Production releases
  - **Parallelizable With**: D3 (documentation)
  - **Estimated Effort**: 1-2 days
  - **Purpose**: Automated CI/CD for testing, building, releasing
  - **Scope**: GitHub Actions workflows for PR validation, releases, artifact publishing
  - **Workflows**:
    - PR validation: Build, test, checkstyle, PMD
    - Release builds: Version tagging, artifact creation
    - Performance regression: Benchmark comparison against baseline
  - **Integration**: Uses Maven build, integrates with existing infrastructure
  - **Quality**: Multi-platform testing, automated releases, security scanning

### D3. Essential Documentation
- [ ] **BLOCKED:** `create-user-documentation` - User guide and configuration reference
  - **Dependencies**: B5 (CLI working), A2 ✅ COMPLETE (config system), C5 (Maven plugin)
  - **Blocks**: Production release (documentation required for users)
  - **Parallelizable With**: D1 (testing), D2 (CI/CD)
  - **Estimated Effort**: 2-3 days (for user docs)
  - **Purpose**: Help users install, configure, and use styler
  - **Scope**: Installation guide, configuration reference, CLI usage, examples
  - **Sections**:
    - Installation: JAR download, Maven plugin setup
    - Configuration: .styler.toml syntax, formatting rules, precedence
    - CLI Usage: Common commands, flags, output formats
    - Examples: Real-world configuration examples, before/after samples
  - **Integration**: Link to scope.md and architecture.md
  - **Quality**: Clear examples, troubleshooting section

- [ ] **BLOCKED:** `create-api-documentation` - Javadoc for public APIs
  - **Dependencies**: B1 (FormattingRule API), A2 ✅ COMPLETE (config APIs), C5 (Maven plugin APIs)
  - **Blocks**: Production release (API docs required for integration)
  - **Parallelizable With**: D1 (testing), D2 (CI/CD), other D3 task (user docs)
  - **Estimated Effort**: 1-2 days
  - **Purpose**: Document public APIs for library and plugin integration
  - **Scope**: Comprehensive Javadoc for public classes, interfaces, methods
  - **Coverage**: FormattingRule interface, configuration APIs, plugin extension points
  - **Integration**: Generated during Maven build, published with releases
  - **Quality**: Complete coverage, code examples, clear descriptions

---

## Phase E: Architecture Refinement

**Goal**: Improve formatter architecture for better maintainability, accuracy, and extensibility.

### E1. Parser Error Handling Enhancement ✅ COMPLETE (2025-12-16)
- [x] **COMPLETE:** `add-parser-error-record` - Add ParseError record and update Parser to return errors in result (2025-12-16)

### E1.5. AST Extension for Formatter Support ✅ COMPLETE (2025-12-17)
- [x] **COMPLETE:** `extend-ast-support` - Extend AST parser to support all Java constructs needed by formatters (2025-12-17)
  - **Dependencies**: E1 ✅ (parser error handling)
  - **Blocks**: E2 (migrate-formatters-to-ast)
  - **Purpose**: Add missing AST node types and parsing support required for AST-based formatting
  - **Components**:
    - Add ENUM_CONSTANT node creation in Parser.parseEnumConstant()
    - Add SWITCH_EXPRESSION to indent-producing node types
    - Fix duplicate BLOCK node allocation in lambda parsing
    - Add missing TokenType entries for Java 21+ constructs
  - **Quality**: All 357 formatter tests passing

### E2. AST-Based Formatter Migration
- [ ] **BLOCKED:** `migrate-formatters-to-ast` - Migrate all formatting rules to AST-based processing
  - **Dependencies**: E1.5 ✅ (extend-ast-support), B2.5 ✅ (pipeline stages), all formatters (B1 + C3) ✅, C4 (concurrency benchmark -
    understand performance baseline before architectural changes)
  - **Blocks**: None (architectural improvement, can be done incrementally)
  - **Parallelizable With**: D1, D2, D3 (independent of polish tasks)
  - **Estimated Effort**: 5-8 days
  - **Purpose**: Replace string/regex-based formatting logic with AST-aware transformations for higher
    accuracy and maintainability
  - **Scope**: Refactor all 5 formatting rules to operate on AST nodes rather than raw text
  - **Current State**: Formatters may use string manipulation or regex patterns that don't understand
    Java syntax context (e.g., strings, comments, nested structures)
  - **Target State**: All formatters traverse AST nodes and apply transformations based on node types,
    ensuring context-aware formatting that respects language semantics
  - **Components**:
    - **AST Visitor Framework**: Base visitor pattern for formatting rules to traverse AST
    - **LineLengthFormattingRule**: Use AST to identify safe break points (after operators, between
      arguments, after annotations) rather than arbitrary character positions
    - **ImportOrganizerFormattingRule**: Already somewhat AST-based; enhance to use AST import nodes
      directly rather than text parsing
    - **BraceFormattingRule**: Use AST to identify brace contexts (class, method, control structure,
      lambda, initializer) for context-specific formatting
    - **WhitespaceFormattingRule**: Use AST to understand operator context, method calls, type parameters
      to apply appropriate spacing rules
    - **IndentationFormattingRule**: Use AST depth and node types to calculate correct indentation levels
  - **Benefits**:
    - Eliminates false positives from formatting inside strings/comments
    - Enables context-sensitive rules (different formatting for lambdas vs methods)
    - Simplifies complex formatting logic by leveraging existing AST structure
    - Improves accuracy for edge cases (nested generics, method references, text blocks)
  - **Migration Strategy**:
    - Migrate one formatter at a time with comprehensive before/after testing
    - Maintain backward compatibility (same formatting output for common cases)
    - Add AST context to violation reports for better AI agent guidance
  - **Quality**: Regression tests comparing pre/post migration output, performance benchmarks to ensure
    no degradation, comprehensive edge case coverage

---

## Deferred Features (Out of MVP Scope)

**Rationale**: These features are not essential for the MVP AI-integrated formatter. Implement only when
demand is proven.

### Advanced Features (Implement When Demand Exists)
- **Gradle Plugin** - Build after Maven plugin proves valuable
- **Incremental Parsing** - May be YAGNI for CLI tool (primarily benefits interactive editors)
- **LSP Integration** - IDE integration deferred until CLI proves valuable
- **Configuration Profiles** - Pre-defined styles (Google, Oracle) deferred until demand proven

### Infrastructure (Not Needed for MVP)
- **Docker Images** - No evidence AI agents need containerization
- **Maven Central Publishing** - Not required for initial AI agent integration
- **Enterprise Security** - JAR signing not needed for CLI tool
- **Plugin Development Ecosystem** - No evidence of third-party plugin demand

---

## Success Criteria

**MVP Success** (End of Phase B):
- ✅ Parse JDK 25 Java files into AST
- ✅ Apply 2 formatting rules (line length, import organization)
- ✅ Generate structured AI violation output
- ✅ CLI tool works end-to-end for single files
- ✅ All tests passing, zero violations

**Production Ready** (End of Phase D):
- ✅ 5 formatting rules implemented and tested (line length, imports, braces, whitespace, indentation)
- ✅ Concurrency model validated (thread-per-file vs thread-per-block benchmark complete)
- ✅ Parallel processing for large codebases (100+ files/sec)
- ✅ Maven plugin for build integration
- ✅ Performance benchmarks validate all claims
- ✅ Comprehensive test suite (unit + integration + regression)
- ✅ CI/CD pipeline for automated releases
- ✅ User documentation and API docs

---

## Implementation Notes

**Architecture Patterns:**

1. **Proven Patterns to Apply**:
   - Index-Overlay AST with Arena API memory management
   - Immutable config objects with builder pattern
   - Security framework with comprehensive validation
   - Chain of Responsibility pipeline pattern
   - Railway-Oriented Programming for error handling
   - Thread-safe stateless formatting rules

2. **Strategic Priorities**:
   - Build to 5 formatting rules for realistic concurrency benchmarking
   - Prioritize AI integration early (Phase B)
   - Maven plugin earlier for real-world validation (Phase C)
   - Empirical concurrency model selection via benchmarking (Phase C)

3. **Quality Standards**:
   - No stubbing, no TODO comments in production code
   - Unanimous stakeholder approval before merge
   - Comprehensive test coverage (unit + integration)
   - Zero checkstyle/PMD violations
   - Complete what we start or mark explicitly unsupported

4. **YAGNI Discipline**:
   - Defer features until demand proven
   - Don't build extensible frameworks for hypothetical needs
   - Focus on core value (AI agent integration)
   - Each phase delivers working end-to-end functionality

**Estimated Total Effort:**
- Phase A (Foundation): 11-16 days (includes A0 build setup)
- Phase B (AI Integration): 14-20 days (includes B4 error catalog)
- Phase C (Scale & Benchmarking): 20-30 days (includes 3 additional rules + concurrency benchmark)
- Phase D (Polish): 6-10 days
- **Total: 51-75 days**
