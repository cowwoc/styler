# TODO List - Styler Java Code Formatter (Main Branch)

## 🚀 READY TO WORK NOW (Multi-Instance Coordination)

**Current Status**: A0 complete! Phase A tasks now available

**READY TO START** (4 parallel instances possible):
- A1: AST Parser Foundation ← 5-7 days, implements Index-Overlay parser
- A2: TOML Configuration ← 2-3 days, implements config system
- A3: CLI Arguments ← 1-2 days, implements argument parsing
- A4: Security Framework ← 2-3 days, implements security controls

**Phase B/C/D**: Blocked until all Phase A tasks (A1-A4) complete

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
- **Phase A** (after A0): 4 tasks can run in parallel (A1, A2, A3, A4)
- **Phase B**: Limited parallelism (B1 must complete first, then B2-B5 have dependencies)
- **Phase C**: Some parallelism (C1 independent, C3 independent of C1/C2, C4 depends on all)

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

## Phase A: Foundation (Zero External Dependencies)

**Goal**: Build core parsing, configuration, and security infrastructure without depending on formatters or AI integration.

**Coordination**: A0 must complete first. After A0, tasks A1-A4 can run in parallel (4 instances possible).

### A1. AST Parser Foundation
- [ ] **READY:** `implement-index-overlay-parser` - Index-Overlay AST parser for JDK 25
  - **Dependencies**: A0 ✅ COMPLETE (build system - need styler-parser, styler-ast-core modules)
  - **Blocks**: B2 (pipeline), all formatters (B1, C3), C4 (concurrency benchmark)
  - **Parallelizable With**: A2, A3, A4 (after A0 completes)
  - **Estimated Effort**: 5-7 days
  - **Purpose**: Parse Java source files into immutable AST representation with memory-efficient index-based storage
  - **Scope**: Complete parser supporting JDK 25 features (pattern matching, records, sealed classes, string templates)
  - **Architecture**: Index-Overlay pattern with Arena API memory management
  - **Components**:
    - Lexer: Tokenize Java source with all JDK 25 tokens
    - Parser: Build AST from token stream using recursive descent
    - ArenaNodeStorage: Index-based node storage with capacity management
    - AST Node Types: Complete node hierarchy for all Java constructs
  - **Memory Management Strategy**:
    - Arena Lifecycle: One arena per file processing (thread-local)
    - Automatic Cleanup: Try-with-resources pattern ensures arena release
    - Memory Limits: MemoryMonitor enforces 512MB heap limit across all arenas
    - Thread Safety: Thread-local arenas eliminate synchronization overhead
    - Error Recovery: Finally blocks guarantee arena release on exceptions
  - **Error Handling**: Graceful error recovery with descriptive messages (no code execution)
  - **Performance Targets**: ≥10,000 tokens/sec, ≤512MB per 1000 files
  - **Integration**: Self-contained, no dependencies on config or formatters
  - **Quality**: 100% JDK 25 feature coverage, comprehensive test suite
  - **Estimated Effort**: 5-7 days

### A4. Security Framework
- [ ] **READY:** `implement-security-controls` - Essential security for CLI tool
  - **Dependencies**: A0 ✅ COMPLETE (build system - need styler-security module)
  - **Blocks**: B2 (pipeline), C1 (file discovery)
  - **Parallelizable With**: A1, A2, A3 (after A0 completes)
  - **Estimated Effort**: 2-3 days
  - **Purpose**: Protect against malicious inputs, resource exhaustion, path traversal
  - **Scope**: Input validation, file size limits, memory monitoring, execution timeouts
  - **Components**:
    - SecurityConfig: Immutable config with builder (file size, memory, timeout limits)
    - FileValidator: File size, type, existence validation
    - PathSanitizer: Path normalization and traversal protection
    - MemoryMonitor: JVM heap usage tracking (512MB limit)
    - ExecutionTimeoutManager: Thread-based timeout enforcement (30s per file)
    - RecursionDepthTracker: Stack overflow protection (1000 max depth)
  - **Limits**: 10MB max file size, 512MB heap, 30s timeout, .java files only
  - **Security Model**: Single-user scenario (scope.md) - focus on resource exhaustion, not data exfiltration
  - **Integration**: Used by CLI and file processor before any file operations
  - **Quality**: Comprehensive exception hierarchy, actionable error messages
  - **Estimated Effort**: 2-3 days

---

## Phase B: AI Integration (Vertical Slice)

**Goal**: Build complete end-to-end pipeline from CLI → parse → format → AI feedback output. This phase delivers working AI agent integration.

**Coordination**: B1 tasks can run in parallel (2 instances). After B1 completes, B2-B5 have sequential dependencies.

### B1. Minimal Formatting Rules (MVP)
- [ ] **BLOCKED:** `implement-line-length-formatter` - Line length violations and auto-fixing
  - **Dependencies**: A0 (styler-formatter-api module), A1 (parser for AST)
  - **Blocks**: B2 (pipeline needs formatters)
  - **Parallelizable With**: `implement-import-organization` (other B1 task)
  - **Estimated Effort**: 2-3 days
  - **Purpose**: Detect and optionally fix lines exceeding configured length
  - **Scope**: Line length validation with configurable limit (default 120 chars)
  - **Components**:
    - LineLengthFormattingRule: FormattingRule implementation
    - LineLengthConfiguration: Immutable config with limit setting
    - LineWrapper: Smart line wrapping with semantic awareness
  - **Features**: Detect violations, suggest wrapping points, optional auto-fix
  - **Integration**: Plugs into format stage, uses AST for semantic wrapping
  - **Quality**: Comprehensive tests, respects code semantics
  - **Estimated Effort**: 2-3 days

- [ ] **BLOCKED:** `implement-import-organization` - Import grouping and unused import removal
  - **Dependencies**: A0 (styler-formatter-api module), A1 (parser for AST)
  - **Blocks**: B2 (pipeline needs formatters)
  - **Parallelizable With**: `implement-line-length-formatter` (other B1 task)
  - **Estimated Effort**: 2-3 days
  - **Purpose**: Organize imports and remove unused ones
  - **Scope**: Import grouping (java/javax, third-party, project, static) with configurable patterns
  - **Components**:
    - ImportOrganizerFormattingRule: FormattingRule implementation
    - ImportOrganizerConfiguration: Group patterns, ordering rules
    - ImportAnalyzer: Detect unused imports
    - ImportGrouper: Group and sort imports
  - **Features**: Configurable group ordering, unused import detection, auto-removal
  - **Integration**: Uses AST import nodes, transformation context API
  - **Quality**: Comprehensive tests, security constraints (ReDoS prevention)
  - **Estimated Effort**: 2-3 days

### B2. File Processing Pipeline
- [ ] **BLOCKED:** `implement-file-processing-pipeline` - Orchestrate parse → format → output
  - **Dependencies**: A1 (parser), A4 (security), B1 (both formatters: line length + imports)
  - **Blocks**: B3 (AI output), B4 (error catalog), B5 (CLI integration), all of Phase C
  - **Parallelizable With**: None (depends on B1, blocks B3/B4/B5)
  - **Estimated Effort**: 3-4 days
  - **Purpose**: Coordinate complete file processing workflow with error recovery
  - **Scope**: Pipeline coordinator handling parse → format → validate → output stages
  - **Architecture**:
    - FileProcessorPipeline: Chain of Responsibility orchestrator
    - AbstractPipelineStage: Template Method pattern for stage lifecycle
    - ProcessingContext: Immutable context with builder pattern
    - StageResult/PipelineResult: Sealed interfaces for Railway-Oriented Programming
  - **Stages**:
    - ParseStage: IndexOverlayParser integration with Arena memory management
    - FormatStage: Apply formatting rules from B1 (line length, import organization)
    - ValidationStage: Security validation, build verification
    - OutputStage: Generate structured violation reports
  - **Error Recovery**: File-level isolation, retry strategies, fail-fast for fatal errors
  - **Integration**: Uses parser (A1), security (A4), formatters (B1), outputs to AI agents
  - **Quality**: Comprehensive test coverage, progress tracking, clear error boundaries
  - **Estimated Effort**: 3-4 days

### B3. Structured Violation Output (AI Agent Integration)
- [ ] **BLOCKED:** `implement-ai-violation-output` - Structured violation feedback for AI agents
  - **Dependencies**: B2 (pipeline - violation collection), B1 (formatters - violation types)
  - **Blocks**: B5 (CLI integration needs output formatter)
  - **Parallelizable With**: B4 (error catalog - independent concerns)
  - **Estimated Effort**: 3-4 days
  - **Purpose**: Generate machine-readable violation reports with actionable fix strategies
  - **Scope**: JSON/XML output with rule IDs, severity, fix strategies, priority scores
  - **Architecture**:
    - ViolationCollector: Track violations during formatting
    - ViolationReport: Immutable violation representation
    - AIOutputFormatter: Generate structured output (JSON/XML)
    - PriorityCalculator: Severity × frequency scoring
  - **Output Format**:
    - Rule ID, file location, severity, description
    - Context-specific fix strategies with code examples
    - Priority score (severity × frequency)
    - Grouped by rule type for pattern recognition
  - **Context Detection**: Automatic AI vs human detection (no --ai-mode flag)
  - **Integration**: Embedded in output stage, uses formatting rule violations
  - **Quality**: Well-structured output, comprehensive fix guidance
  - **Estimated Effort**: 3-4 days

### B4. Error Message Catalog
- [ ] **BLOCKED:** `create-error-message-catalog` - Comprehensive error messages for AI and human users
  - **Dependencies**: B2 (pipeline - error types), A1 (parser - parse errors)
  - **Blocks**: B5 (CLI integration needs error formatting)
  - **Parallelizable With**: B3 (AI output - independent concerns)
  - **Estimated Effort**: 2 days
  - **Purpose**: Provide clear, actionable error messages for all failure scenarios
  - **Scope**: Error code catalog, context-specific messages, fix suggestions
  - **Components**:
    - ErrorCatalog: Central registry of error codes and messages
    - ContextualErrorFormatter: Generate AI vs human-appropriate error output
    - ParseErrorMessages: Parser-specific error messages with source locations
    - FormattingErrorMessages: Formatter-specific error guidance
  - **Dual-Audience Design**:
    - AI Format: Structured error codes with programmatic fix strategies
    - Human Format: Narrative descriptions with examples
  - **Integration**: Used by all error handling in pipeline (B2)
  - **Quality**: Comprehensive coverage, clear fix guidance, internationalization-ready
  - **Estimated Effort**: 2 days

### B5. CLI Integration
- [ ] **BLOCKED:** `implement-cli-formatter-integration` - Wire CLI → pipeline → output
  - **Dependencies**: A2 (config), A3 (CLI args), B2 (pipeline), B3 (AI output), B4 (errors)
  - **Blocks**: All of Phase C (C1-C6 need working CLI)
  - **Parallelizable With**: None (depends on all other Phase B tasks)
  - **Estimated Effort**: 2-3 days
  - **Purpose**: Complete end-to-end CLI workflow for single files
  - **Scope**: CLI invokes pipeline, handles output, reports errors
  - **Components**:
    - CLIMain: Entry point, argument processing
    - PipelineOrchestrator: Execute pipeline for input files
    - OutputHandler: Format and display results
    - ErrorReporter: Clear error messages with file locations (uses B4 error catalog)
  - **Flow**: CLI args → config loading → pipeline execution → structured output
  - **Integration**: Connects all Phase A and Phase B components (A3 CLI args, B2 pipeline, B3 output, B4 errors)
  - **Quality**: Clear error messages, proper exit codes, progress reporting
  - **Estimated Effort**: 2-3 days

---

## Phase C: Scale & Real-World Testing

**Goal**: Scale to large codebases with parallel processing, build to 5 formatting rules for realistic benchmarking, and validate with Maven plugin integration.

### C1. File Discovery
- [ ] **BLOCKED:** `implement-file-discovery` - Recursive Java file discovery with filtering
  - **Dependencies**: B5 (CLI integration complete), A4 (security for file validation)
  - **Blocks**: C2 (parallel processing needs file list), C5 (Maven plugin needs discovery)
  - **Parallelizable With**: C3 (formatting rules development)
  - **Estimated Effort**: 1-2 days
  - **Purpose**: Find Java source files in directories with include/exclude patterns
  - **Scope**: Recursive directory traversal, glob patterns, .gitignore integration
  - **Components**:
    - FileDiscovery: Directory walker with filtering
    - PatternMatcher: Glob pattern matching for includes/excludes
    - GitignoreParser: Respect .gitignore rules
  - **Features**: Include/exclude patterns, symlink handling, large directory support
  - **Integration**: Provides file list to parallel processor
  - **Quality**: Efficient traversal, security validation

### C2. Virtual Thread Processing (Thread-per-File Baseline)
- [ ] **BLOCKED:** `implement-virtual-thread-processing` - Multi-threaded file processing with virtual threads
  - **Dependencies**: B5 (CLI integration), C1 (file discovery for file list), B2 (pipeline to wrap)
  - **Blocks**: C4 (concurrency benchmark needs baseline), C5 (Maven plugin needs parallel processing)
  - **Parallelizable With**: C3 (formatting rules development)
  - **Estimated Effort**: 3-4 days
  - **Purpose**: Process large codebases efficiently using Java 25 virtual threads
  - **Scope**: Virtual thread pool for file-level parallelism with unlimited concurrency
  - **Architecture**:
    - VirtualThreadExecutor: Virtual thread-based executor service
    - BatchProcessor: Distribute files across virtual threads
    - ResultAggregator: Collect and merge processing results
  - **Strategy**: File-level parallelism (one virtual thread per file, JVM manages scheduling)
  - **Error Handling**: File-level isolation, partial failure support
  - **Performance Targets**: 100+ files/sec, linear scalability to 32 cores
  - **Integration**: Wraps file processing pipeline from B2, uses file list from C1
  - **Quality**: Thread-safe design, comprehensive concurrency tests

### C3. Additional Formatting Rules (Build to 5 Total Rules)
- [ ] **BLOCKED:** `implement-brace-formatting` - Brace style formatting (K&R, Allman, GNU)
  - **Dependencies**: B1 (formatter infrastructure), A1 (AST nodes)
  - **Blocks**: C4 (concurrency benchmark needs 5 rules total)
  - **Parallelizable With**: C1 (file discovery), C2 (parallel processing), other C3 tasks (whitespace, indentation)
  - **Estimated Effort**: 2-3 days
  - **Purpose**: Enforce consistent brace placement across Java constructs
  - **Scope**: Configurable brace styles (K&R, Allman, GNU) with construct-specific overrides
  - **Components**:
    - BraceFormattingRule: FormattingRule implementation
    - BraceConfiguration: Style settings, empty block handling, overrides
    - BraceAnalyzer: Detect brace violations
    - BraceFormatter: Apply configured brace style
  - **Features**: Multiple brace styles, control structure overrides, empty block handling
  - **Integration**: Uses AST structure nodes, transformation context API
  - **Quality**: Comprehensive tests covering all Java constructs

- [ ] **BLOCKED:** `implement-whitespace-formatting` - Whitespace around operators and keywords
  - **Dependencies**: B1 (formatter infrastructure), A1 (AST nodes)
  - **Blocks**: C4 (concurrency benchmark needs 5 rules total)
  - **Parallelizable With**: C1 (file discovery), C2 (parallel processing), other C3 tasks (brace, indentation)
  - **Estimated Effort**: 2-3 days
  - **Purpose**: Ensure consistent spacing around operators, keywords, punctuation
  - **Scope**: Configurable whitespace rules for operators, keywords, commas, semicolons
  - **Components**:
    - WhitespaceFormattingRule: FormattingRule implementation
    - WhitespaceConfiguration: Operator/keyword spacing settings
    - WhitespaceAnalyzer: Detect spacing violations
    - WhitespaceFormatter: Apply whitespace corrections
  - **Features**: Operator spacing, keyword spacing, comma/semicolon handling
  - **Integration**: Uses AST expression nodes, transformation context API
  - **Quality**: Comprehensive tests, performance optimizations

- [ ] **BLOCKED:** `implement-indentation-formatting` - Indentation formatting (tabs/spaces/mixed)
  - **Dependencies**: B1 (formatter infrastructure), A1 (AST nodes)
  - **Blocks**: C4 (concurrency benchmark needs 5 rules total)
  - **Parallelizable With**: C1 (file discovery), C2 (parallel processing), other C3 tasks (brace, whitespace)
  - **Estimated Effort**: 2-3 days
  - **Purpose**: Enforce consistent indentation across all code constructs
  - **Scope**: Configurable indentation (tabs, spaces, mixed) with continuation indent
  - **Components**:
    - IndentationFormattingRule: FormattingRule implementation
    - IndentationConfiguration: Tab/space settings, depth, continuation indent
    - IndentationAnalyzer: Detect indentation violations
    - IndentationFormatter: Apply indentation corrections
  - **Features**: Tab/space/mixed modes, configurable depth, continuation lines
  - **Integration**: Uses AST block structure, transformation context API
  - **Quality**: Comprehensive tests covering nested structures

### C4. Concurrency Model Benchmark
- [ ] **BLOCKED:** `benchmark-concurrency-models` - Compare thread-per-file vs thread-per-block parallelism
  - **Dependencies**: C2 (thread-per-file baseline), all C3 tasks (brace + whitespace + indentation = 3 rules), B1 (line length + imports = 2 rules), total 5 rules
  - **Blocks**: C5 (Maven plugin should use optimal concurrency model if thread-per-block wins)
  - **Parallelizable With**: None (needs C2 and all C3 tasks complete first)
  - **Estimated Effort**: 2-3 days
  - **Purpose**: Determine optimal concurrency strategy for styler through empirical testing
  - **Scope**: Benchmark thread-per-file (baseline from C2) vs thread-per-block concurrency with 5 formatting rules
  - **Prerequisites**: 5 formatting rules implemented (B1: 2 rules, C3: 3 rules)
  - **Comparison Approaches**:
    - **Thread-per-file (baseline)**: One virtual thread per file (current C2 implementation)
    - **Thread-per-block**: Virtual threads for method-level parallelism within files
    - **Platform threads**: Work-stealing pool for reference comparison
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
- [ ] **BLOCKED:** `create-maven-plugin` - Maven plugin for build system integration
  - **Dependencies**: C1 (file discovery), C2 (parallel processing), B5 (CLI integration)
  - **Blocks**: C6 (performance benchmarking uses Maven plugin), D1 (regression tests use Maven plugin), D2 (CI/CD uses Maven plugin)
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
  - **Dependencies**: C5 (Maven plugin for running benchmarks), C2 (parallel processing), all formatters (B1 + C3)
  - **Blocks**: D1 (testing uses benchmarks for performance regression detection), D2 (CI/CD runs benchmark comparisons)
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
  - **Dependencies**: C5 (Maven plugin for running styler), all formatters (B1 + C3)
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
  - **Dependencies**: B5 (CLI integration), C1 (file discovery), C2 (parallel processing)
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
  - **Dependencies**: D1 (all tests: regression + CLI integration), C6 (performance benchmarks), C5 (Maven plugin)
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
  - **Dependencies**: B5 (CLI working), A2 (config system), C5 (Maven plugin)
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
  - **Dependencies**: B1 (FormattingRule API), A2 (config APIs), C5 (Maven plugin APIs)
  - **Blocks**: Production release (API docs required for integration)
  - **Parallelizable With**: D1 (testing), D2 (CI/CD), other D3 task (user docs)
  - **Estimated Effort**: 1-2 days
  - **Purpose**: Document public APIs for library and plugin integration
  - **Scope**: Comprehensive Javadoc for public classes, interfaces, methods
  - **Coverage**: FormattingRule interface, configuration APIs, plugin extension points
  - **Integration**: Generated during Maven build, published with releases
  - **Quality**: Complete coverage, code examples, clear descriptions

---

## Deferred Features (Out of MVP Scope)

**Rationale**: These features are not essential for the MVP AI-integrated formatter. Implement only when demand is proven.

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
