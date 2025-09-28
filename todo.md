# TODO List - Styler Java Code Formatter

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
- [ ] **TASK:** `implement-incremental-parsing` - Support for parsing only changed sections (Tree-sitter inspired)
- [x] **TASK:** `implement-source-position-tracking` - Precise source location tracking for all tokens (JavaLexer implemented)
- [ ] **TASK:** `arena-vs-gc-memory-architecture-decision` - Benchmark and decide between Arena API vs GC for memory allocation
- [ ] **TASK:** `add-parser-unit-tests` - Unit tests covering all JDK 25 language features

## Phase 2: Formatter Plugin Framework

### Formatter API Module
- [x] **MODULE:** `create-formatter-api-module` - Create styler-formatter-api Maven module (COMPLETED: comprehensive API with security controls, plugin architecture, configuration framework, and conflict resolution)
- [ ] **TASK:** `define-formatter-plugin-interface` - Plugin interface for formatting rules
- [ ] **TASK:** `implement-rule-configuration-schema` - YAML-based configuration schema
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
- [ ] **TASK:** `implement-security-framework` - Implement trusted plugin model with JAR signature verification and publisher trust registry
- [ ] **TASK:** `implement-resource-monitoring` - Create lightweight resource monitoring service for memory, CPU, and thread tracking without enforcement isolation
- [ ] **TASK:** `implement-plugin-isolation` - Implement plugin lifecycle manager with standard ClassLoader hierarchy for trusted plugins
- [ ] **TASK:** `implement-security-validation` - Build plugin trust validation pipeline with certificate verification and publisher whitelisting
- [ ] **TASK:** `create-security-test-suite` - Create security test suite for JAR signing, trust validation, and resource monitoring scenarios

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
- [ ] **TASK:** `add-cli-integration-tests` - End-to-end tests with real Java files

## Security and Quality Assurance

### Security Implementation
- [ ] **TASK:** `implement-input-validation` - Comprehensive input validation framework
- [ ] **TASK:** `implement-path-sanitization` - Path traversal prevention and validation
- [ ] **TASK:** `implement-resource-limits` - File size limits, memory bounds, and timeouts
- [ ] **TASK:** `implement-sandboxing` - AST-only parsing without code execution
- [ ] **TASK:** `add-security-unit-tests` - Unit tests for all security controls

### Integration and Build
- [ ] **TASK:** `create-maven-plugin` - Maven plugin for build system integration
- [ ] **TASK:** `create-gradle-plugin` - Gradle plugin for build system integration
- [ ] **TASK:** `implement-git-hooks` - Pre-commit hook scripts for CI/CD integration
- [ ] **TASK:** `benchmark-concurrency-architectures` - Benchmark file-based vs block-based concurrency architectures
- [ ] **TASK:** `add-performance-benchmarks` - Performance tests against large codebases
- [ ] **TASK:** `add-regression-test-suite` - Regression tests with real-world Java projects

## Documentation and Release

### Documentation
- [ ] **TASK:** `create-user-documentation` - User guide and configuration reference
- [ ] **TASK:** `create-plugin-development-guide` - Guide for custom plugin development
- [ ] **TASK:** `create-api-documentation` - Javadoc for public APIs and plugin interfaces
- [ ] **TASK:** `create-performance-guide` - Performance tuning and optimization guide

### Release Preparation
- [ ] **TASK:** `setup-ci-cd-pipeline` - GitHub Actions for automated testing and releases
- [ ] **TASK:** `create-docker-image` - Containerized Tidy for deployment environments
- [ ] **TASK:** `setup-maven-central-publishing` - Publish artifacts to Maven Central Repository
- [ ] **TASK:** `create-release-artifacts` - JAR distributions and installation scripts

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