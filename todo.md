# TODO List - Styler Java Code Formatter

## 🎯 DEPENDENCY-FIRST IMPLEMENTATION ORDER

**Based on evidence-based analysis to avoid stubbing violations and ensure each task can be implemented completely using only previously completed components:**

### Phase A: Foundation (Zero External Dependencies)
1. **AST Parser Foundation** - Complete parser without dependencies on config/CLI
2. **Basic Configuration Schema** - TOML parsing without file discovery
3. **Core CLI Arguments** - Command-line parsing without file processing

### Phase B: Vertical Integration (Build Complete Minimal Pipeline)
4. **Configuration Discovery** - Config file loading (depends on CLI args)
5. **Error Recovery** - Parser error handling (depends on AST foundation)
6. **Basic Security Controls** - Input validation (depends on CLI args)
7. **Single File Formatter** - One formatter rule with real config (depends on config system)

### Phase C: Horizontal Expansion (Scale the Working Pipeline)
8. **Multiple Formatter Rules** - Expand formatter capabilities
9. **File Processing Pipeline** - Multi-file processing (depends on single-file formatter)
10. **Structured Violation Output** - AI agent feedback (depends on formatter rules)
11. **CLI Startup Optimization** - Performance tuning (depends on complete pipeline)

### Phase D: Parallel Processing (Optimize the Complete System)
12. **Parallel File Processing** - Multi-threading (depends on complete file pipeline)
13. **Block-Based Concurrency Benchmarking** - Advanced optimizations
14. **Performance Benchmarks** - System validation

### Phase E: Ecosystem Integration (External Tool Support)
15. **Maven/Gradle Plugins** - Build tool integration (depends on complete CLI)
16. **CI/CD Integration** - Automated workflows

### DEFERRED (YAGNI violations - implement when demand proven):
1. **Enterprise Security** - JAR signing, certificates not needed for CLI tool
2. **Plugin Development Ecosystem** - No evidence of third-party plugin demand
3. **Container Deployment** - No evidence AI agents need Docker
4. **Maven Central Publishing** - Not required for initial AI agent integration

## Phase A: Foundation (Zero External Dependencies)

### AST Core Module - COMPLETED ✅
- [x] **MODULE:** `create-ast-core-module` - Create styler-ast-core Maven module with AST node hierarchy
- [x] **TASK:** `implement-ast-node-base` - Base AST node with visitor pattern and metadata preservation
- [x] **TASK:** `implement-java-ast-nodes` - Complete AST node hierarchy for all Java constructs
- [x] **TASK:** `implement-comment-preservation` - Maintain comments, whitespace, and formatting hints
- [x] **TASK:** `implement-immutable-ast` - Immutable AST with builder pattern for modifications
- [x] **TASK:** `add-ast-core-unit-tests` - Comprehensive unit tests for AST node operations
- [x] **TASK:** `fix-module-dependency-resolution` - CRITICAL: Fix Maven module dependency failures

### Parser Engine Module - MOSTLY COMPLETED ✅
- [x] **MODULE:** `create-parser-module` - Create styler-parser Maven module with custom parser dependencies
- [x] **TASK:** `complete-custom-recursive-descent-parser` - Complete handwritten recursive descent parser for JDK 25 features
- [x] **TASK:** `implement-source-position-tracking` - Precise source location tracking for all tokens (JavaLexer implemented)
- [x] **TASK:** `add-parser-unit-tests` - Unit tests covering all JDK 25 language features
- [x] **TASK:** `arena-vs-gc-memory-architecture-decision` - Benchmark and decide between Arena API vs GC for memory allocation (COMPLETED: Arena API adoption approved with 3x performance improvement and 96.9% safety margin against 512MB target)
- [x] **TASK:** `implement-arena-api-memory-allocation` - Replace NodeRegistry/MemoryArena with Arena API implementation (COMPLETED: Arena API implementation with 3-12x performance benefits, parent-child tracking, and legacy deprecation)

### Basic Configuration Schema (No File Discovery)
- [x] **MODULE:** `create-formatter-api-module` - Create styler-formatter-api Maven module (COMPLETED)
- [x] **TASK:** `implement-rule-configuration-schema` - TOML-based configuration schema (COMPLETED)
- [x] **TASK:** `implement-transformation-context-api` - Context API for rule application (COMPLETED: Immutable AST reconstruction with comprehensive security protection)
  - **Purpose**: Create writable context API enabling formatting rules to directly apply AST transformations
  - **Scope**: MutableFormattingContext extending FormattingContext with direct AST modification methods (setRootNode, replaceChild, insertBefore, insertAfter, removeChild, setWhitespace, setComments)
  - **Components**: MutableFormattingContext class, MutableFormattingRule interface, basic modification tracking
  - **Final Design**: Immutable AST reconstruction approach - rules call convenience methods that internally rebuild AST trees while maintaining security boundaries
  - **Implementation**: Architectural foundation with comprehensive resource protection (recursion depth limits, modification count limits, proper try-finally cleanup)

### Core CLI Arguments (No File Processing)
- [x] **MODULE:** `create-cli-module` - Create styler-cli Maven module as main entry point
- [x] **TASK:** `implement-command-line-parsing` - Parse command-line arguments and options (COMPLETED: Facade pattern with immutable ParsedArguments record)
  - **Purpose**: Parse CLI arguments like file paths, config options, verbosity levels, help/version flags
  - **Scope**: CommandLineParser class with argument validation, error handling, usage documentation
  - **Arguments**: Input files, --config path, --rules filter, --verbose, --dry-run, --help, --version
  - **Integration**: Provides parsed arguments to main application pipeline
  - **Implementation**: CommandLineParser facade over Picocli, ParsedArguments immutable record, ArgumentParsingException for errors, comprehensive unit tests
- [ ] **TASK:** `implement-error-reporting` - User-friendly error messages with file locations
  - **Purpose**: Generate clear, actionable error messages for parse failures, config errors, rule violations
  - **Scope**: ErrorReporter class with formatted output, source location context, fix suggestions
  - **Features**: Colored output, code snippets with line numbers, suggested fixes, error categorization
  - **Integration**: Used by parser, config loader, rule engine to report user-facing errors

## Phase B: Vertical Integration (Build Complete Minimal Pipeline)

### Configuration Discovery (Depends on CLI Args) - COMPLETED ✅
- [ ] **TASK:** `implement-config-discovery` - Automatic configuration file discovery (IN PROGRESS: Implementation complete, awaiting task protocol Phase 7 finalization)
  - **Purpose**: Automatically locate styler configuration files in project directories
  - **Scope**: Search for .styler.toml in current/parent dirs, merge with CLI overrides (YAML removed per requirements)
  - **Search Strategy**: Current dir → parent dirs → home dir → global config, with precedence rules
  - **Integration**: ConfigDiscovery class with Builder pattern, DiscoveryResult for thread-safe location tracking
  - **Implementation**: 4-component system (ConfigDiscovery, ConfigMerger, ConfigSearchPath, ConfigParser) with platform-aware path resolution, git boundary detection, field-level configuration merging, thread-safe caching (<50ms target), comprehensive exception hierarchy with business context
- [x] **TASK:** `implement-yaml-config-parser` - Parse YAML configuration files (REMOVED: YAML support removed per requirements - TOML-only implementation)

### Error Recovery (Depends on AST Foundation)
- [ ] **TASK:** `implement-error-recovery` - Error recovery for partial formatting of malformed files
  - **Purpose**: Enable partial formatting of files with syntax errors (incomplete code, missing braces)
  - **Scope**: ErrorRecoveryParser that builds partial AST, formats valid sections, preserves invalid sections
  - **Strategy**: Skip malformed constructs, insert placeholder nodes, continue parsing after errors
  - **Integration**: Used by parser when strict mode disabled, produces warnings for unfixable sections

### Basic Security Controls (Depends on CLI Args)
- [ ] **TASK:** `implement-cli-security-basics` - Essential CLI security: input validation, file size limits, memory bounds, and timeouts
  - **Purpose**: Protect against malicious inputs, resource exhaustion, path traversal attacks
  - **Scope**: SecurityManager with file validation, memory limits, execution timeouts, path sanitization
  - **Controls**: Max file size (50MB), max memory (512MB), timeout (30s), allowed file extensions (.java)
  - **Integration**: Used by CLI argument parser and file processor before any file operations

### Single File Formatter (Depends on Config System)
- [x] **TASK:** `define-formatter-plugin-interface` - Plugin interface for formatting rules (COMPLETED)
- [ ] **TASK:** `implement-line-length-formatter` - Line length auto-fixer with smart wrapping (FIRST FORMATTER - complete end-to-end)
  - **Purpose**: First complete formatting rule to establish end-to-end pipeline from CLI to output
  - **Scope**: LineLength rule with intelligent line wrapping, method chaining breaks, parameter alignment
  - **Features**: Configurable max length, preserve comments, smart break points, indentation consistency
  - **Integration**: Complete pipeline test: CLI args → config loading → parsing → rule application → output
- [ ] **TASK:** `add-formatter-api-unit-tests` - Unit tests for plugin interfaces

## Phase C: Horizontal Expansion (Scale the Working Pipeline)

### Multiple Formatter Rules (Expand Formatter Capabilities)
- [ ] **MODULE:** `create-formatter-impl-module` - Create styler-formatter-impl Maven module
- [ ] **TASK:** `implement-import-organization` - Import grouping and unused import removal
  - **Purpose**: Organize import statements into groups and remove unused imports to improve code cleanliness
  - **Scope**: ImportOrganizer rule with grouping (java.*, javax.*, org.*, com.*, static imports), unused detection
  - **Features**: Configurable group ordering, preserve manual spacing, static import handling, wildcard expansion
  - **Integration**: Uses AST import nodes, works with existing transformation context API
- [ ] **TASK:** `implement-whitespace-formatter` - Consistent spacing around operators and keywords
  - **Purpose**: Ensure consistent whitespace around operators, keywords, punctuation for readable code
  - **Scope**: WhitespaceFormatter rule handling operators (+, -, *, etc.), keywords (if, for, while), punctuation
  - **Features**: Configurable spacing rules, preserve string literals, handle edge cases (unary operators)
  - **Integration**: Uses transformation context to update whitespace metadata on AST nodes
- [ ] **TASK:** `implement-brace-formatter` - Configurable brace placement rules
  - **Purpose**: Enforce consistent brace placement style (K&R, Allman, GNU) across Java constructs
  - **Scope**: BraceFormatter rule for classes, methods, if/else, loops, try/catch with configurable styles
  - **Features**: Style selection (same line vs new line), empty block handling, single statement rules
  - **Integration**: Uses transformation context to modify block statement AST nodes and whitespace
- [ ] **TASK:** `implement-indentation-formatter` - Configurable indentation (tabs/spaces/mixed)
  - **Purpose**: Enforce consistent indentation style and depth across all code constructs
  - **Scope**: IndentationFormatter rule with tabs/spaces/mixed mode, configurable depth (2, 4, 8 spaces)
  - **Features**: Continuation line indentation, array/parameter alignment, comment indentation preservation
  - **Integration**: Uses transformation context to update whitespace info with proper indentation
- [ ] **TASK:** `migrate-checkstyle-fixers` - Port existing checkstyle fixers to plugin architecture
  - **Purpose**: Migrate existing checkstyle auto-fixers to use the new transformation context API
  - **Scope**: Port LineLength, UnusedImports, and other fixers from checkstyle/fixers module
  - **Components**: Adapter layer, plugin registration, configuration mapping, test migration
  - **Integration**: Wrap existing fixer logic in FormattingRule interface using TransformationContext
- [ ] **TASK:** `implement-conflict-resolution` - Handle conflicts between competing rules
  - **Purpose**: Detect and resolve conflicts when multiple rules attempt to modify the same AST regions
  - **Scope**: ConflictResolver with detection algorithms, resolution strategies (priority, merge, fail-fast)
  - **Strategies**: Rule priority ordering, compatible transformation merging, user-configurable policies
  - **Integration**: Used by transformation context during commit phase to resolve pending conflicts
- [ ] **TASK:** `add-formatter-impl-unit-tests` - Unit tests for all formatter implementations
  - **Purpose**: Comprehensive unit test coverage for all formatting rule implementations
  - **Scope**: Test classes for each formatter rule with edge cases, configuration variants, error conditions
  - **Coverage**: Normal cases, edge cases, configuration combinations, error handling, performance
  - **Integration**: Uses test framework with mock transformation contexts and AST builders

### File Processing Pipeline (Multi-file Processing)
- [ ] **TASK:** `implement-file-processing-pipeline` - Coordinate parsing, formatting, and output
  - **Purpose**: Orchestrate the complete file processing workflow from input to formatted output
  - **Scope**: Pipeline coordinator handling parse → format → validate → write stages with error recovery
  - **Components**: FileProcessor, PipelineStage interface, error recovery, progress tracking
  - **Integration**: Uses parser, transformation context, conflict resolution, and file I/O systems
- [ ] **TASK:** `implement-file-discovery` - Recursive Java file discovery with filtering
  - **Purpose**: Find and filter Java source files for processing with configurable inclusion/exclusion rules
  - **Scope**: FileDiscovery service with recursive directory traversal, glob patterns, .gitignore integration
  - **Features**: Include/exclude patterns, symlink handling, performance optimization, large directory support
  - **Integration**: Provides file list to processing pipeline, respects configuration file settings

### Structured Violation Output (AI Agent Feedback)
- [ ] **TASK:** `implement-structured-violation-output` - Machine-readable violation reports for AI agent feedback
  - **Purpose**: Generate structured violation reports enabling AI agents to learn from formatting feedback
  - **Scope**: JSON/XML violation reports with rule IDs, severity, fix suggestions, source locations
  - **Features**: Machine-readable format, priority scoring, fix strategy hints, learning feedback loops
  - **Integration**: Used by rule engine to generate actionable feedback for AI agent training systems

### CLI Startup Optimization (Performance Tuning)
- [ ] **TASK:** `implement-cli-startup-optimization` - Optimize JVM startup time for fast AI agent validation
  - **Purpose**: Minimize JVM startup overhead to achieve <50ms latency for AI agent validation workflows
  - **Scope**: JVM tuning, lazy loading, pre-compilation, native image options, startup profiling
  - **Techniques**: Class loading optimization, reflection reduction, GraalVM native image, warmup caching
  - **Integration**: Applied to main CLI entry point and critical path components for fast startup

## Phase D: Parallel Processing (Optimize the Complete System)

### Parallel File Processing (Multi-threading)
- [ ] **MODULE:** `create-engine-module` - Create styler-engine Maven module
- [ ] **TASK:** `implement-work-stealing-pool` - Parallel file processing orchestrator
  - **Purpose**: Implement work-stealing thread pool for optimal parallel file processing performance
  - **Scope**: Custom ExecutorService with work-stealing queues, dynamic thread scaling, NUMA awareness
  - **Features**: Adaptive thread count, task stealing algorithms, exception isolation, graceful shutdown
  - **Integration**: Core parallel processing engine used by file processing pipeline and rule execution
- [ ] **TASK:** `implement-work-distribution` - Dynamic task distribution and load balancing
  - **Purpose**: Distribute file processing tasks efficiently across available threads and cores
  - **Scope**: WorkDistributor with file size analysis, dependency tracking, load balancing algorithms
  - **Features**: File size weighting, dependency resolution, core affinity, memory pressure adaptation
  - **Integration**: Used by work-stealing pool to optimally distribute files across worker threads
- [ ] **TASK:** `implement-progress-reporting` - Real-time progress updates for large codebases
  - **Purpose**: Provide real-time progress feedback for processing large codebases with thousands of files
  - **Scope**: ProgressReporter with completion percentages, file counts, throughput metrics, ETA calculation
  - **Features**: Real-time updates, human-readable output, JSON progress for tools, error tracking
  - **Integration**: Integrated with file processing pipeline and work distribution for accurate progress
- [ ] **TASK:** `implement-memory-management` - Memory-bounded processing with automatic cleanup
  - **Purpose**: Prevent memory exhaustion during large codebase processing with automatic resource cleanup
  - **Scope**: MemoryManager with heap monitoring, garbage collection tuning, cache eviction, memory limits
  - **Features**: Adaptive memory limits, proactive GC triggering, cache size management, OOM prevention
  - **Integration**: Monitors all processing stages and automatically adjusts resource usage and parallelism
- [ ] **TASK:** `implement-resource-monitoring` - Lightweight resource monitoring service
  - **Purpose**: Monitor memory, CPU, and thread usage for performance optimization and resource management
  - **Scope**: ResourceMonitor service with metrics collection, threshold alerting, performance analytics
  - **Features**: Low-overhead monitoring, configurable thresholds, metrics export, performance dashboards
  - **Integration**: Used by memory management and work distribution to make resource-aware decisions
- [ ] **TASK:** `add-engine-unit-tests` - Unit tests for parallel processing and error handling
  - **Purpose**: Comprehensive unit test coverage for all parallel processing and concurrency components
  - **Scope**: Thread safety tests, performance tests, error handling tests, resource limit tests
  - **Coverage**: Concurrency edge cases, resource exhaustion scenarios, error propagation, graceful degradation
  - **Integration**: Uses test framework with controlled threading, memory pressure simulation, error injection

### Advanced Optimizations
- [ ] **TASK:** `benchmark-concurrency-architectures` - Benchmark file-based vs block-based concurrency architectures
  - **Purpose**: Compare file-level vs method-level parallelism to determine optimal concurrency strategy
  - **Scope**: Performance benchmarks comparing file-based concurrency vs hybrid block-based concurrency
  - **Methodology**: JMH benchmarks with various file sizes, thread counts, memory configurations
  - **Evidence**: Current data shows potential for 15%+ performance gains with block-based approach for large files

### System Validation
- [ ] **TASK:** `add-performance-benchmarks` - Performance tests against large codebases
  - **Purpose**: Validate system performance and scalability with real-world large Java codebases
  - **Scope**: Benchmark suite testing against large open-source projects (Spring, Apache Commons, etc.)
  - **Metrics**: Processing time, memory usage, throughput, error rates, resource utilization
  - **Integration**: Automated benchmark runs with performance regression detection and reporting
- [ ] **TASK:** `add-regression-test-suite` - Regression tests with real-world Java projects
  - **Purpose**: Prevent regressions by testing against real-world Java projects with known formatting expectations
  - **Scope**: Test suite with curated Java projects, before/after formatting comparisons, golden file testing
  - **Coverage**: Various Java versions, coding styles, edge cases, large files, complex constructs
  - **Integration**: Automated regression testing in CI/CD pipeline with failure analysis and reporting
- [ ] **TASK:** `add-cli-integration-tests` - End-to-end tests with real Java files
  - **Purpose**: Validate complete CLI functionality with real Java files and realistic usage scenarios
  - **Scope**: Integration tests covering CLI argument parsing, file processing, output generation, error handling
  - **Scenarios**: Single files, directory processing, configuration variants, error conditions, edge cases
  - **Integration**: Uses real Java files, temporary directories, process execution, output validation

## Phase E: Ecosystem Integration (External Tool Support)

### Build Tool Integration (Depends on Complete CLI)
- [ ] **TASK:** `create-maven-plugin` - Maven plugin for build system integration
  - **Purpose**: Integrate styler formatting into Maven build lifecycle for automated code formatting
  - **Scope**: Maven plugin with goals for check, format, validate phases, configuration inheritance
  - **Features**: Multi-module support, incremental formatting, build failure on violations, IDE integration
  - **Integration**: Uses styler CLI as dependency with Maven-specific configuration and reporting
- [ ] **TASK:** `create-gradle-plugin` - Gradle plugin for build system integration
  - **Purpose**: Integrate styler formatting into Gradle build system for automated code formatting
  - **Scope**: Gradle plugin with tasks for check, format, validate, configuration via build scripts
  - **Features**: Incremental builds, build cache support, parallel execution, custom source sets
  - **Integration**: Uses styler CLI as dependency with Gradle-specific configuration and task integration

### CI/CD Integration (Automated Workflows)
- [ ] **TASK:** `setup-ci-cd-pipeline` - GitHub Actions for automated testing and releases
  - **Purpose**: Automated CI/CD pipeline for testing, building, and releasing styler components
  - **Scope**: GitHub Actions workflows for PR validation, release building, artifact publishing
  - **Features**: Multi-platform testing, performance regression detection, automated releases, security scanning
  - **Integration**: Uses Maven/Gradle plugins, integrates with existing project infrastructure
- [ ] **TASK:** `create-release-artifacts` - JAR distributions and installation scripts
  - **Purpose**: Create distributable JAR files and installation scripts for easy styler deployment
  - **Scope**: Executable JAR building, native executable creation, installation scripts, package management
  - **Features**: Self-contained JARs, native images, package manager integration (brew, apt, etc.)
  - **Integration**: Uses build system to create optimized distributions with dependency bundling

### Security Unit Tests (Validate All Security Controls)
- [ ] **TASK:** `add-security-unit-tests` - Unit tests for all security controls
  - **Purpose**: Comprehensive security testing for all authentication, authorization, and validation controls
  - **Scope**: Security test suite covering authorization framework, resource limits, input validation, audit logging
  - **Coverage**: Attack scenarios, boundary testing, privilege escalation attempts, resource exhaustion
  - **Integration**: Uses security testing framework with mock attacks, penetration testing scenarios

## Deferred Tasks

### Incremental Parsing (SCOPE QUESTION - May Be Unnecessary)
- [ ] **TASK:** `implement-incremental-parsing` - Support for parsing only changed sections (Tree-sitter inspired) (SCOPE QUESTION: May be unnecessary for CLI tool use case - incremental parsing primarily benefits interactive editors, not batch file processing)
- [ ] **TASK:** `complete-incremental-parsing-tree-reconciliation` - Complete tree reconciliation logic for incremental parsing (DEPENDS ON: implement-incremental-parsing scope decision)

### Configuration Extensions (MEDIUM PRIORITY)
- [ ] **TASK:** `implement-rule-precedence` - Configuration inheritance and precedence rules
- [ ] **TASK:** `implement-profile-management` - Pre-defined style profiles (Google, Oracle, etc.)
- [ ] **TASK:** `implement-dynamic-rule-loading` - Runtime plugin loading and configuration
- [ ] **TASK:** `add-config-unit-tests` - Unit tests for configuration parsing and validation

### Deferred Infrastructure Tasks (YAGNI - Implement When Needed)
- [ ] **TASK:** `implement-git-hooks` - Pre-commit hook scripts for CI/CD integration (DEFERRED: No immediate evidence of need)

## Documentation (When System is Complete)

### Essential Documentation
- [ ] **TASK:** `create-user-documentation` - User guide and configuration reference
- [ ] **TASK:** `create-api-documentation` - Javadoc for public APIs and plugin interfaces

### Deferred Documentation & Infrastructure (YAGNI - Create When Ecosystem Demand Exists)
- [ ] **TASK:** `create-plugin-development-guide` - Guide for custom plugin development (DEFERRED: No evidence of third-party plugin demand)
- [ ] **TASK:** `create-performance-guide` - Performance tuning and optimization guide (DEFERRED: Create after performance characteristics are established)
- [ ] **TASK:** `create-docker-image` - Containerized deployment (DEFERRED: No evidence AI agents or build tools need containerization)
- [ ] **TASK:** `setup-maven-central-publishing` - Publish artifacts to Maven Central (DEFERRED: Not needed for initial AI agent integration)

## Detailed Task Specifications

### `benchmark-concurrency-architectures` Task Details

**Objective:** Compare file-based concurrency vs block-based concurrency to determine optimal parallelization strategy.

**Background:**
Current evidence shows file-level parallelism (like Checkstyle/PMD) eliminates 16-99x synchronization overhead. However, post-parse AST becomes read-only and immutable, potentially enabling method-level parallelism within files after parsing completes.

**Architectures to Compare:**

1. **File-Based Concurrency (Baseline)**
   - One thread per file (current industry standard)
   - Single-threaded parsing and formatting per file
   - Multiple files processed concurrently
   - AST instance reused per worker thread via reset()

2. **Hybrid Block-Based Concurrency (Innovation)**
   - Phase 1: Single-threaded parsing per file (creates read-only AST)
   - Phase 2: Multi-threaded method-level formatting from read-only AST
   - File-level parallelism + method-level parallelism
   - Requires thread-safe read operations from Index-Overlay AST

**Benchmarking Requirements:**

1. **File Size Variations:**
   - Small files: 1-5 methods, <200 LOC
   - Medium files: 10-50 methods, 200-1000 LOC
   - Large files: 100+ methods, 1000+ LOC
   - Very large files: 500+ methods, 5000+ LOC

2. **Thread Pool Configurations:**
   - File workers: 1, 2, 4, 8, 16 threads
   - Method workers (hybrid): 2, 4, 8, 16 threads per file
   - Measure optimal ratios for different file sizes

3. **Metrics to Measure:**
   - Total processing time (end-to-end)
   - CPU utilization and core efficiency
   - Memory usage and GC pressure
   - Thread coordination overhead
   - Context switching costs

4. **Test Scenarios:**
   - Homogeneous workloads (all small, all medium, all large files)
   - Mixed workloads (realistic distribution of file sizes)
   - Memory-constrained environments
   - CPU-bound vs I/O-bound scenarios

5. **Complexity Analysis:**
   - Implementation complexity comparison
   - Maintenance overhead assessment
   - Bug surface area evaluation
   - Thread-safety validation requirements

**Success Criteria:**
- Hybrid approach shows >15% improvement for files with 20+ methods
- Overhead complexity justified by performance gains
- Thread-safety validated through concurrent stress testing
- Memory usage remains acceptable (no more than 2x increase)

**Evidence Base:**
- Current benchmarks show Index-Overlay read operations: 87.9 ns/op (thread-safe when read-only)
- AST visitor pattern supports independent method processing
- Post-parse AST immutability enables safe concurrent reads
- JavaParser research confirms "method independence" for formatting rules

**Implementation Notes:**
- Use JMH for precise microbenchmarks
- Test with real Java codebases (Spring, Apache Commons, etc.)
- Validate thread-safety through race condition detection
- Measure at different JVM heap sizes and GC algorithms