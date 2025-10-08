# TODO List - Styler Java Code Formatter (Main Branch)

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

### A0. Build System Setup
- [ ] **TASK:** `setup-maven-multi-module-build` - Create Maven parent POM and module structure
  - **Purpose**: Establish build infrastructure for all subsequent tasks
  - **Scope**: Parent POM with nested module structure for logical grouping
  - **Module Structure**:
    - `styler-parent/` (root POM with dependency management, Java 25 runtime required, JPMS support, NO preview features per out-of-scope.md)
      - `styler-ast/` (parent POM)
        - `styler-ast-core/` (AST node hierarchy)
      - `styler-parser/` (parser implementation)
      - `styler-config/` (configuration system)
      - `styler-security/` (security framework)
      - `styler-formatter/` (parent POM)
        - `styler-formatter-api/` (FormattingRule interfaces)
        - `styler-formatter-impl/` (concrete rule implementations)
      - `styler-cli/` (CLI entry point)
  - **Integration**: All subsequent tasks output to appropriate modules
  - **Quality**: Proper JPMS module structure, dependency isolation, checkstyle/PMD integration
  - **Estimated Effort**: 1 day

### A1. AST Parser Foundation
- [ ] **TASK:** `implement-index-overlay-parser` - Index-Overlay AST parser for JDK 25
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

### A2. Configuration System
- [ ] **TASK:** `implement-toml-configuration` - TOML-based configuration with file discovery
  - **Purpose**: Load and merge formatting configuration from .styler.toml files
  - **Scope**: TOML parser, config discovery (current/parent/home dirs), merge logic
  - **Components**:
    - ConfigParser: Parse .styler.toml files (Jackson TOML or toml4j)
    - ConfigDiscovery: Search current → parent → home → global with git boundary detection
    - ConfigMerger: Field-level merge with precedence rules
    - ConfigSchema: Immutable config objects with builder pattern
  - **Search Strategy**: Current dir → parent dirs (stop at .git) → ~/.styler.toml → /etc/styler.toml
  - **Integration**: Used by CLI and file processor, no dependencies on parser
  - **Quality**: Thread-safe caching, comprehensive validation, clear error messages
  - **Estimated Effort**: 2-3 days

### A3. CLI Argument Parsing
- [ ] **TASK:** `implement-cli-arguments` - Command-line argument parsing without file processing
  - **Purpose**: Parse CLI arguments for file paths, config overrides, output options
  - **Scope**: Argument parser with --config, --check, --fix, --help, --version flags
  - **Components**:
    - ArgumentParser: Parse command-line arguments (picocli or jcommander)
    - CLIOptions: Immutable options object
    - HelpFormatter: Generate usage help text
  - **Arguments**: file/directory paths, --config <path>, --check (validate only), --fix (auto-fix), output format
  - **Integration**: Self-contained, no dependencies on parser or config loading
  - **Quality**: Clear help text, validation errors, exit codes
  - **Estimated Effort**: 1-2 days

### A4. Security Framework
- [ ] **TASK:** `implement-security-controls` - Essential security for CLI tool
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

### B1. Minimal Formatting Rules (MVP)
- [ ] **TASK:** `implement-line-length-formatter` - Line length violations and auto-fixing
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

- [ ] **TASK:** `implement-import-organization` - Import grouping and unused import removal
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
- [ ] **TASK:** `implement-file-processing-pipeline` - Orchestrate parse → format → output
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
- [ ] **TASK:** `implement-ai-violation-output` - Structured violation feedback for AI agents
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
- [ ] **TASK:** `create-error-message-catalog` - Comprehensive error messages for AI and human users
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
- [ ] **TASK:** `implement-cli-formatter-integration` - Wire CLI → pipeline → output
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
- [ ] **TASK:** `implement-file-discovery` - Recursive Java file discovery with filtering
  - **Purpose**: Find Java source files in directories with include/exclude patterns
  - **Scope**: Recursive directory traversal, glob patterns, .gitignore integration
  - **Components**:
    - FileDiscovery: Directory walker with filtering
    - PatternMatcher: Glob pattern matching for includes/excludes
    - GitignoreParser: Respect .gitignore rules
  - **Features**: Include/exclude patterns, symlink handling, large directory support
  - **Integration**: Provides file list to parallel processor
  - **Quality**: Efficient traversal, security validation
  - **Estimated Effort**: 1-2 days

### C2. Virtual Thread Processing (Thread-per-File Baseline)
- [ ] **TASK:** `implement-virtual-thread-processing` - Multi-threaded file processing with virtual threads
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
  - **Estimated Effort**: 3-4 days

### C3. Additional Formatting Rules (Build to 5 Total Rules)
- [ ] **TASK:** `implement-brace-formatting` - Brace style formatting (K&R, Allman, GNU)
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
  - **Estimated Effort**: 2-3 days

- [ ] **TASK:** `implement-whitespace-formatting` - Whitespace around operators and keywords
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
  - **Estimated Effort**: 2-3 days

- [ ] **TASK:** `implement-indentation-formatting` - Indentation formatting (tabs/spaces/mixed)
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
  - **Estimated Effort**: 2-3 days

### C4. Concurrency Model Benchmark
- [ ] **TASK:** `benchmark-concurrency-models` - Compare thread-per-file vs thread-per-block parallelism
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
  - **Estimated Effort**: 2-3 days

### C5. Maven Plugin (Early Real-World Testing)
- [ ] **TASK:** `create-maven-plugin` - Maven plugin for build system integration
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
  - **Estimated Effort**: 2-3 days

### C6. Performance Benchmarking
- [ ] **TASK:** `create-jmh-benchmarks` - Validate performance claims with JMH benchmarks
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
  - **Estimated Effort**: 3-4 days

---

## Phase D: Polish & Production Readiness

**Goal**: Production-ready release with testing, CI/CD, and documentation.

### D1. Comprehensive Testing
- [ ] **TASK:** `add-regression-test-suite` - Real-world Java project regression tests
  - **Purpose**: Prevent regressions by testing against real-world Java projects
  - **Scope**: Curated Java projects with before/after formatting comparisons
  - **Projects**: Spring Framework, Apache Commons, Guava, JUnit5, Mockito
  - **Coverage**: Various Java versions, coding styles, edge cases, large files
  - **Integration**: Automated regression testing in CI/CD pipeline
  - **Quality**: Golden file testing, failure analysis
  - **Estimated Effort**: 2-3 days

- [ ] **TASK:** `add-cli-integration-tests` - End-to-end CLI integration tests
  - **Purpose**: Validate complete CLI functionality with real Java files
  - **Scope**: Integration tests covering CLI arguments, file processing, output
  - **Scenarios**: Single files, directories, config variants, error conditions
  - **Integration**: Uses real Java files, temporary directories, process execution
  - **Quality**: Comprehensive scenario coverage, clear assertions
  - **Estimated Effort**: 1-2 days

### D2. CI/CD Pipeline
- [ ] **TASK:** `setup-github-actions-ci` - Automated testing and release pipeline
  - **Purpose**: Automated CI/CD for testing, building, releasing
  - **Scope**: GitHub Actions workflows for PR validation, releases, artifact publishing
  - **Workflows**:
    - PR validation: Build, test, checkstyle, PMD
    - Release builds: Version tagging, artifact creation
    - Performance regression: Benchmark comparison against baseline
  - **Integration**: Uses Maven build, integrates with existing infrastructure
  - **Quality**: Multi-platform testing, automated releases, security scanning
  - **Estimated Effort**: 1-2 days

### D3. Essential Documentation
- [ ] **TASK:** `create-user-documentation` - User guide and configuration reference
  - **Purpose**: Help users install, configure, and use styler
  - **Scope**: Installation guide, configuration reference, CLI usage, examples
  - **Sections**:
    - Installation: JAR download, Maven plugin setup
    - Configuration: .styler.toml syntax, formatting rules, precedence
    - CLI Usage: Common commands, flags, output formats
    - Examples: Real-world configuration examples, before/after samples
  - **Integration**: Link to scope.md and architecture.md
  - **Quality**: Clear examples, troubleshooting section
  - **Estimated Effort**: 2-3 days

- [ ] **TASK:** `create-api-documentation` - Javadoc for public APIs
  - **Purpose**: Document public APIs for library and plugin integration
  - **Scope**: Comprehensive Javadoc for public classes, interfaces, methods
  - **Coverage**: FormattingRule interface, configuration APIs, plugin extension points
  - **Integration**: Generated during Maven build, published with releases
  - **Quality**: Complete coverage, code examples, clear descriptions
  - **Estimated Effort**: 1-2 days

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
