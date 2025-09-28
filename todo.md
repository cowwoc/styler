# TODO List - Styler Java Code Formatter

## 🎯 SCOPE-ALIGNED IMPLEMENTATION PRIORITY

**Based on evidence from scope.md analysis - Focus on immediate needs for AI agent integration and build tool usage:**

### IMMEDIATE (Blocks core use cases):
1. **Error Recovery** (Line 19) - Required for malformed files in both AI agent and build scenarios
2. **Basic CLI Interface** (Lines 78-84) - Essential for both AI agent validation and build integration
3. **Formatter Implementation** (Lines 56-60) - Core functionality for code formatting
4. **Basic Security Controls** (Line 86) - Essential CLI security without enterprise complexity

### HIGH PRIORITY (Performance & usability for target scenarios):
1. **CLI Startup Optimization** - Critical for <50ms AI agent validation latency
2. **Parallel File Processing** (Lines 67-73) - Required for 100-150 files/second targets
3. **Block-Based Concurrency Benchmarking** (Line 92) - Evidence shows 15%+ potential gains
4. **Structured Violation Output** - Required for AI agent feedback-driven learning

### MEDIUM PRIORITY (Build ecosystem integration):
1. **Maven/Gradle Plugins** (Lines 90-91) - Build tool integration
2. **Configuration System** (Lines 37-42) - Flexible rule management

### DEFERRED (YAGNI violations - implement when demand proven):
1. **Enterprise Security** (Lines 110-113) - JAR signing, certificates not needed for CLI tool
2. **Plugin Development Ecosystem** - No evidence of third-party plugin demand
3. **Container Deployment** - No evidence AI agents need Docker
4. **Maven Central Publishing** - Not required for initial AI agent integration

## Phase 1: Core AST Parser Foundation

### AST Core Module
- [x] **MODULE:** `create-ast-core-module` - Create styler-ast-core Maven module with AST node hierarchy (restructured under ast parent module, duplicate ast-core/ directory removed)
- [x] **TASK:** `implement-ast-node-base` - Base AST node with visitor pattern and metadata preservation (COMPLETED: ASTNode.java with visitor pattern, ASTVisitor interface, and ASTNodeBuilder framework fully implemented and accessible)
- [x] **TASK:** `implement-java-ast-nodes` - Complete AST node hierarchy for all Java constructs (COMPLETED: 75 Java files including 59+ node classes covering all Java language constructs, fully accessible after dependency resolution)
- [x] **TASK:** `implement-comment-preservation` - Maintain comments, whitespace, and formatting hints (COMPLETED: Comment.java, SourceRange.java, FormattingHints.java, and WhitespaceInfo.java fully implemented with comprehensive metadata preservation)
- [x] **TASK:** `implement-immutable-ast` - Immutable AST with builder pattern for modifications (COMPLETED: Comprehensive ASTNodeBuilder framework with immutable AST nodes and builder pattern for all modifications)
- [x] **TASK:** `add-ast-core-unit-tests` - Comprehensive unit tests for AST node operations (COMPLETED: 7 test classes with comprehensive coverage of AST node operations, builder patterns, and visitor functionality)

### Build System Integration
- [x] **TASK:** `fix-module-dependency-resolution` - CRITICAL: Fix Maven module dependency failures preventing compilation of existing AST implementation (COMPLETED: Fixed module dependencies, requires transitive declarations, and FormattingContext API - 59 AST nodes now accessible)

### Parser Engine Module
- [x] **MODULE:** `create-parser-module` - Create styler-parser Maven module with custom parser dependencies
- [x] **TASK:** `complete-custom-recursive-descent-parser` - Complete handwritten recursive descent parser for JDK 25 features (COMPLETED: Added JDK 25 features including module imports, flexible constructors, primitive patterns, compact source files, and instance main methods)
- [ ] **TASK:** `implement-error-recovery` - Error recovery for partial formatting of malformed files
- [ ] **TASK:** `implement-incremental-parsing` - Support for parsing only changed sections (Tree-sitter inspired) (SCOPE QUESTION: May be unnecessary for CLI tool use case - incremental parsing primarily benefits interactive editors, not batch file processing)
- [x] **TASK:** `implement-source-position-tracking` - Precise source location tracking for all tokens (JavaLexer implemented)
- [ ] **TASK:** `arena-vs-gc-memory-architecture-decision` - Benchmark and decide between Arena API vs GC for memory allocation (RETAINED: Critical for 512MB per 1000 files performance target)
- [ ] **TASK:** `complete-incremental-parsing-tree-reconciliation` - Complete tree reconciliation logic for incremental parsing (node extraction, insertion, parent-child relationship repair) (DEPENDS ON: implement-incremental-parsing scope decision)
- [ ] **TASK:** `add-parser-unit-tests` - Unit tests covering all JDK 25 language features

## Phase 2: Formatter Plugin Framework

### Formatter API Module
- [x] **MODULE:** `create-formatter-api-module` - Create styler-formatter-api Maven module (COMPLETED: comprehensive API with security controls, plugin architecture, configuration framework, and conflict resolution)
- [x] **TASK:** `define-formatter-plugin-interface` - Plugin interface for formatting rules (COMPLETED: Comprehensive plugin framework with PluginContext, PluginDescriptor, FormatterPlugin, ResourceManager, RuntimeInfo, FileAccessPolicy, ResourceUsageStats, security controls, and slf4j logging integration)
- [x] **TASK:** `implement-rule-configuration-schema` - TOML-based configuration schema (COMPLETED: Comprehensive TOML configuration framework with Jackson, ConfigurationSchema, GlobalConfiguration, LineLengthRuleConfiguration, security validation, file loading, resource loading, merging, and complete test suite - converted from YAML to TOML format)
- [ ] **TASK:** `implement-transformation-context-api` - Context API for rule application
- [ ] **TASK:** `implement-conflict-resolution` - Handle conflicts between competing rules
- [ ] **TASK:** `add-formatter-api-unit-tests` - Unit tests for plugin interfaces

### Configuration System
- [ ] **MODULE:** `create-config-module` - Create styler-config Maven module with SnakeYAML
- [ ] **TASK:** `implement-yaml-config-parser` - Parse YAML configuration files
- [ ] **TASK:** `implement-rule-precedence` - Configuration inheritance and precedence rules
- [ ] **TASK:** `implement-profile-management` - Pre-defined style profiles (Google, Oracle, etc.)
- [ ] **TASK:** `implement-dynamic-rule-loading` - Runtime plugin loading and configuration
- [ ] **TASK:** `add-config-unit-tests` - Unit tests for configuration parsing and validation

## Phase 3: Auto-fixer Migration

### Formatter Security Infrastructure
- [ ] **TASK:** `implement-basic-security-controls` - Basic input validation, resource limits, and path sanitization for CLI tool security
- [ ] **TASK:** `implement-resource-monitoring` - Create lightweight resource monitoring service for memory, CPU, and thread tracking without enforcement isolation

### Formatter Implementation Module
- [ ] **MODULE:** `create-formatter-impl-module` - Create styler-formatter-impl Maven module
- [ ] **TASK:** `migrate-checkstyle-fixers` - Port existing checkstyle fixers to plugin architecture
- [ ] **TASK:** `implement-line-length-formatter` - Line length auto-fixer with smart wrapping
- [ ] **TASK:** `implement-import-organization` - Import grouping and unused import removal
- [ ] **TASK:** `implement-whitespace-formatter` - Consistent spacing around operators and keywords
- [ ] **TASK:** `implement-brace-formatter` - Configurable brace placement rules
- [ ] **TASK:** `implement-indentation-formatter` - Configurable indentation (tabs/spaces/mixed)
- [ ] **TASK:** `add-formatter-impl-unit-tests` - Unit tests for all formatter implementations

## Phase 4: Parallel Processing Engine

### Engine Module
- [ ] **MODULE:** `create-engine-module` - Create styler-engine Maven module
- [ ] **TASK:** `implement-work-stealing-pool` - Parallel file processing orchestrator
- [ ] **TASK:** `implement-file-discovery` - Recursive Java file discovery with filtering
- [ ] **TASK:** `implement-work-distribution` - Dynamic task distribution and load balancing
- [ ] **TASK:** `implement-progress-reporting` - Real-time progress updates for large codebases
- [ ] **TASK:** `implement-memory-management` - Memory-bounded processing with automatic cleanup
- [ ] **TASK:** `implement-error-recovery` - Individual file failure isolation and recovery
- [ ] **TASK:** `add-engine-unit-tests` - Unit tests for parallel processing and error handling

## Phase 5: Command-Line Interface

### CLI Module
- [ ] **MODULE:** `create-cli-module` - Create styler-cli Maven module as main entry point
- [ ] **TASK:** `implement-command-line-parsing` - Parse command-line arguments and options
- [ ] **TASK:** `implement-config-discovery` - Automatic configuration file discovery
- [ ] **TASK:** `implement-file-processing-pipeline` - Coordinate parsing, formatting, and output
- [ ] **TASK:** `implement-security-controls` - Path validation, sandboxing, and resource limits
- [ ] **TASK:** `implement-error-reporting` - User-friendly error messages with file locations
- [ ] **TASK:** `implement-cli-startup-optimization` - Optimize JVM startup time for <50ms AI agent validation latency
- [ ] **TASK:** `implement-structured-violation-output` - Machine-readable violation reports with rule IDs, fix strategies, and priority scores for AI agent feedback-driven learning
- [ ] **TASK:** `add-cli-integration-tests` - End-to-end tests with real Java files

## Security and Quality Assurance

### Security Implementation
- [ ] **TASK:** `implement-cli-security-basics` - Essential CLI security: input validation, file size limits, memory bounds, and timeouts (consolidated from multiple security tasks)
- [ ] **TASK:** `add-security-unit-tests` - Unit tests for all security controls

### Integration and Build
- [ ] **TASK:** `create-maven-plugin` - Maven plugin for build system integration
- [ ] **TASK:** `create-gradle-plugin` - Gradle plugin for build system integration
- [ ] **TASK:** `benchmark-concurrency-architectures` - Benchmark file-based vs block-based concurrency architectures (RETAINED: Evidence shows potential for 15%+ performance gains in large files)
- [ ] **TASK:** `add-performance-benchmarks` - Performance tests against large codebases
- [ ] **TASK:** `add-regression-test-suite` - Regression tests with real-world Java projects

### Deferred Infrastructure Tasks (YAGNI - Implement When Needed)
- [ ] **TASK:** `implement-git-hooks` - Pre-commit hook scripts for CI/CD integration (DEFERRED: No immediate evidence of need)

## Documentation and Release

### Documentation
- [ ] **TASK:** `create-user-documentation` - User guide and configuration reference
- [ ] **TASK:** `create-api-documentation` - Javadoc for public APIs and plugin interfaces

### Release Preparation
- [ ] **TASK:** `setup-ci-cd-pipeline` - GitHub Actions for automated testing and releases
- [ ] **TASK:** `create-release-artifacts` - JAR distributions and installation scripts

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