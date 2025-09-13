# Styler - Java Code Formatter Architecture

This document provides a comprehensive technical architecture overview for Styler - an unopinionated Java code formatter that supports 100% of JDK 25's features with multi-threaded file processing capabilities.

## 🎯 Core Architecture Objective

Styler uses a modular, plugin-based architecture to provide configurable Java code formatting while maintaining optimal performance through parallel processing and preserving developer intent through intelligent AST manipulation.

## 🏗️ System Architecture Overview

```
┌─────────────────────────────────────────────────┐
│                   Styler CLI                    │
│            (Main Entry Point)                   │
├─────────────────────────────────────────────────┤
│              Configuration Manager              │
│        (YAML parser, rule registry)             │
├─────────────────────────────────────────────────┤
│           Parallel Processing Engine            │
│         (Work-stealing thread pool)             │
├─────────────────────────────────────────────────┤
│            Formatter Plugin System              │
│    (Rule engine, plugin loader, orchestrator)   │
├─────────────────────────────────────────────────┤
│              AST Parser Engine                  │
│    (Parser, AST builder, metadata preserving)   │
├─────────────────────────────────────────────────┤
│               Core AST Model                    │
│    (Nodes, visitors, transformers, metadata)    │
├─────────────────────────────────────────────────┤
│              Security & Safety                  │
│     (Input validation, sandboxing, limits)      │
└─────────────────────────────────────────────────┘
```

## 📦 Module Architecture

### Core Modules Structure

**styler-parent** (Root POM)
├── **styler-ast-core** (AST node hierarchy)
├── **styler-parser** (Java parser implementation)
├── **styler-formatter-api** (Plugin interfaces)
├── **styler-formatter-impl** (Built-in formatters)
├── **styler-config** (Configuration system)
├── **styler-engine** (Parallel processing)
├── **styler-security** (Security controls)
└── **styler-cli** (Command-line interface)

### Module Dependencies

```
styler-cli
├── styler-engine
│   ├── styler-formatter-impl
│   │   ├── styler-formatter-api
│   │   └── styler-ast-core
│   ├── styler-parser
│   │   └── styler-ast-core
│   ├── styler-config
│   └── styler-security
└── styler-ast-core
```

## 🧱 Detailed Component Design

### 1. styler-ast-core

**Purpose**: Immutable AST node hierarchy with visitor pattern support

**Key Classes**:
```java
// Base AST node
public abstract class ASTNode {
    private final SourcePosition position;
    private final List<Comment> comments;
    private final FormattingMetadata metadata;

    public abstract <R, A> R accept(ASTVisitor<R, A> visitor, A arg);
}

// Visitor interface
public interface ASTVisitor<R, A> {
    R visit(ClassDeclaration node, A arg);
    R visit(MethodDeclaration node, A arg);
    R visit(FieldDeclaration node, A arg);
    // ... all Java constructs
}

// AST builder for immutable modifications
public class ASTBuilder {
    public static ASTNode transform(ASTNode node, ASTTransformation transformation);
}
```

**Features**:
- Immutable AST nodes with builder pattern
- Complete metadata preservation (comments, whitespace)
- Visitor pattern for traversal and transformation
- Source position tracking for all tokens
- Support for all Java language constructs (Java 8 to JDK 25)

### 2. styler-parser

**Purpose**: Java source parsing with comprehensive language support

**Key Classes**:
```java
public class JavaParser {
    public ParseResult parse(String source, ParseOptions options);
    public ParseResult parseIncremental(String source, ChangeSet changes);
}

public class ParseResult {
    private final ASTNode rootNode;
    private final List<ParseError> errors;
    private final boolean hasErrors;
}

public class ParseOptions {
    private final JavaVersion targetVersion;
    private final ErrorRecoveryMode errorRecovery;
    private final boolean preserveComments;
}
```

**Parser Implementation Strategy**:
- **Custom recursive descent parser** (not ANTLR-based)
- Full JDK 25 feature support (pattern matching, string templates, unnamed classes)
- Error recovery for partial/malformed files
- Incremental parsing for changed sections
- Comment and formatting preservation
- Memory-efficient parsing with streaming support

**JDK 25 Features Supported**:
- Pattern matching for switch expressions
- String templates with embedded expressions
- Unnamed classes and instance main methods
- Record patterns and enhanced pattern matching
- Sequenced collections and improved generics

### 3. styler-formatter-api

**Purpose**: Plugin interface definitions and configuration schema

**Key Interfaces**:
```java
public interface FormatterPlugin {
    String getName();
    String getVersion();
    List<String> getSupportedRules();

    ASTNode format(ASTNode node, FormattingContext context, RuleConfiguration config);
    ValidationResult validate(RuleConfiguration config);
}

public interface FormattingContext {
    JavaVersion getJavaVersion();
    FormattingPreferences getPreferences();
    ConflictResolutionStrategy getConflictStrategy();
}

public interface RuleConfiguration {
    boolean isEnabled(String ruleName);
    <T> T getValue(String key, Class<T> type);
    Map<String, Object> getAllValues();
}
```

**Plugin System Features**:
- Dynamic plugin loading and registration
- Rule-based configuration with inheritance
- Conflict resolution between competing rules
- Validation of plugin configurations
- Performance profiling and metrics collection

### 4. styler-formatter-impl

**Purpose**: Built-in formatter implementations and migrated auto-fixers

**Built-in Formatters**:
```java
@FormatterPlugin(name = "line-length", version = "1.0")
public class LineLengthFormatter implements FormatterPlugin {
    // Smart line wrapping with AST-aware break points
}

@FormatterPlugin(name = "import-organizer", version = "1.0")
public class ImportOrganizerFormatter implements FormatterPlugin {
    // Import grouping, sorting, and unused removal
}

@FormatterPlugin(name = "whitespace-normalizer", version = "1.0")
public class WhitespaceFormatter implements FormatterPlugin {
    // Consistent spacing around operators, keywords, braces
}

@FormatterPlugin(name = "brace-placement", version = "1.0")
public class BracePlacementFormatter implements FormatterPlugin {
    // Configurable brace placement (K&R, Allman, etc.)
}
```

**Migration from Existing Checkstyle Fixers**:
- Extract existing fixer logic from checkstyle/fixers module
- Adapt to plugin architecture with proper interfaces
- Maintain test coverage and validation logic
- Preserve performance optimizations

### 5. styler-config

**Purpose**: YAML-based configuration system with inheritance

**Configuration Schema**:
```yaml
# .styler.yml
version: "1.0"
language_version: JDK_25

# Core settings
processing:
  thread_pool_size: auto
  memory_limit: "512MB"
  timeout_per_file: "30s"
  max_file_size: "10MB"

# Security settings
security:
  sandbox_enabled: true
  allowed_directories: ["src/", "test/"]
  temp_dir_permissions: "700"

# Formatting rules
formatting:
  line_length: 120
  indentation:
    type: "spaces"
    size: 4
  braces:
    classes: "next_line"
    methods: "next_line"
    control_structures: "same_line"

# Plugin configuration
plugins:
  - name: "line-length-formatter"
    enabled: true
    config:
      max_length: 120
      smart_wrapping: true
  - name: "import-organizer"
    enabled: true
    config:
      group_by_package: true
      remove_unused: true

# Profiles
profiles:
  google:
    extends: "default"
    formatting:
      line_length: 100
      indentation:
        size: 2
  oracle:
    extends: "default"
    formatting:
      braces:
        classes: "same_line"
```

**Configuration Features**:
- YAML-based human-readable configuration
- Profile inheritance and extension
- Rule precedence and overrides
- Runtime configuration validation
- Environment-specific configuration

### 6. styler-engine

**Purpose**: Multi-threaded file processing and coordination

**Core Engine Classes**:
```java
public class ParallelFormattingEngine {
    public FormattingResult formatProject(ProjectConfiguration config);
    public FormattingResult formatFiles(List<Path> files, FormattingConfiguration config);
}

public class WorkStealingProcessor {
    private final ForkJoinPool threadPool;
    private final ProgressReporter progressReporter;
    private final ErrorCollector errorCollector;
}

public class FormattingPipeline {
    public FormattingResult process(Path file) {
        // 1. Security validation
        // 2. File parsing
        // 3. Plugin application
        // 4. Output generation
        // 5. Error handling
    }
}
```

**Performance Features**:
- Work-stealing thread pool for optimal CPU utilization
- Memory-bounded processing to prevent OOM
- Progress reporting with cancellation support
- Individual file error isolation
- Adaptive task subdivision for large files
- Performance metrics and profiling

**Threading Model**:
- File-level parallelism (individual files processed concurrently)
- Lock-free AST operations (immutable nodes)
- Thread-local caching for performance
- Automatic thread pool sizing based on CPU cores

### 7. styler-security

**Purpose**: Comprehensive security controls and input validation

**Security Architecture**:
```java
public class SecurityManager {
    public ValidationResult validateInput(Path filePath, String content);
    public void applySandboxRestrictions();
    public void enforceResourceLimits(ProcessingContext context);
}

public class PathValidator {
    public boolean isAllowedPath(Path path);
    public Path canonicalizePath(Path path);
    public void preventTraversalAttacks(Path path);
}

public class ResourceLimiter {
    public void enforceFileSize(long size, long maxSize);
    public void enforceMemoryUsage(long usage, long maxUsage);
    public void enforceProcessingTimeout(Duration elapsed, Duration maxDuration);
}
```

**Security Controls**:
- **Input Validation**: Multi-layer validation for file paths, content, and encoding
- **Path Security**: Canonical path validation, directory whitelisting, traversal prevention
- **Resource Limits**: File size limits (10MB), memory bounds (512MB per 1000 files), timeouts (30s)
- **Sandboxing**: AST-only parsing, no code execution, restricted filesystem access
- **Error Sanitization**: Generic error messages, no information disclosure

### 8. styler-cli

**Purpose**: Command-line interface and main application entry point

**CLI Architecture**:
```java
public class StylerCLI {
    public static void main(String[] args) {
        // Command-line parsing
        // Configuration loading
        // Engine initialization
        // Processing execution
        // Results reporting
    }
}

public class CommandLineParser {
    public CLIConfiguration parse(String[] args);
}

public class ResultReporter {
    public void reportSuccess(FormattingResult result);
    public void reportErrors(List<FormattingError> errors);
    public void reportProgress(ProgressUpdate update);
}
```

**CLI Features**:
- Intuitive command-line options (`format`, `check`, `--config`, `--profile`)
- Configuration file discovery (`.styler.yml`, `.styler.yaml`)
- Progress reporting for large codebases
- Error reporting with file locations and suggestions
- Exit codes for CI/CD integration

## 🔒 Security Architecture

### Defense in Depth Strategy

**Layer 1: Input Validation**
- File path validation and canonicalization
- Content encoding verification and validation
- File size and type checking
- Malicious pattern detection

**Layer 2: Sandboxing**
- SecurityManager with restricted permissions
- Filesystem access limited to whitelisted directories
- No dynamic compilation or code execution
- Process isolation and resource containment

**Layer 3: Resource Protection**
- Memory usage monitoring and limits
- Processing timeout enforcement
- CPU usage throttling
- Temporary file security

**Layer 4: Error Handling**
- Generic error messages (no information disclosure)
- Secure logging with audit trails
- Graceful failure handling
- Attack detection and reporting

### Threat Model

**Protected Against**:
- Code injection attacks via malicious Java files
- Path traversal attacks writing to system directories
- Resource exhaustion (zip bombs, large files, infinite loops)
- Information disclosure through error messages
- Symlink attacks and directory traversal
- Malicious compressed file handling

**Security Boundaries**:
- No network access required or permitted
- Read-only access to source files
- Write access only to specified output directories
- No system command execution
- No reflection or dynamic class loading

## ⚡ Performance Architecture

### Multi-threading Design

**Concurrency Strategy**:
- **File-Level Parallelism**: Individual files processed concurrently
- **Work-Stealing Pool**: Dynamic load balancing across CPU cores
- **Lock-Free Operations**: Immutable AST enables safe concurrent processing
- **Memory Management**: Bounded processing with automatic cleanup

**Performance Targets**:
- **Throughput**: 100-150 files/second on modern multi-core systems
- **Memory Efficiency**: <512MB heap usage per 1000 files processed
- **Scalability**: Linear performance scaling up to 32 CPU cores
- **Latency**: <100ms response time for single file formatting

**Optimization Strategies**:
- Thread-local AST node caching
- Preallocated token buffers
- Lazy evaluation of AST transformations
- Incremental parsing for changed sections
- Memory-mapped file I/O for large files

### Benchmarking and Profiling

**Performance Testing**:
- Automated benchmarks against large open-source projects
- Memory usage profiling and optimization
- Thread contention analysis and elimination
- Comparative performance against existing tools

## 🔧 Plugin Development Architecture

### Plugin API Design

**Plugin Lifecycle**:
1. **Discovery**: Automatic plugin discovery via classpath scanning
2. **Loading**: Dynamic loading with dependency resolution
3. **Validation**: Configuration and compatibility validation
4. **Registration**: Plugin registration with rule engine
5. **Execution**: Rule application during formatting pipeline
6. **Cleanup**: Resource cleanup and unloading

**Plugin Development Kit**:
- Base classes and utilities for plugin development
- Testing framework for plugin validation
- Documentation generator for plugin APIs
- Performance profiling tools for optimization

### Extensibility Patterns

**Custom Rule Development**:
- Abstract base classes for common formatting patterns
- Helper utilities for AST manipulation and analysis
- Configuration schema validation and type safety
- Integration with existing formatter pipeline

## 🚀 Deployment and Integration

### Build Tool Integration

**Maven Plugin**:
```xml
<plugin>
    <groupId>io.github.styler</groupId>
    <artifactId>styler-maven-plugin</artifactId>
    <version>1.0.0</version>
    <configuration>
        <configFile>.styler.yml</configFile>
        <profile>google</profile>
    </configuration>
</plugin>
```

**Gradle Plugin**:
```kotlin
plugins {
    id("io.github.styler") version "1.0.0"
}

styler {
    configFile = ".styler.yml"
    profile = "google"
}
```

### IDE Integration

**Language Server Protocol**:
- Real-time formatting as you type
- Configuration validation and suggestions
- Error highlighting and quick fixes
- Integration with existing IDE formatting workflows

### CI/CD Integration

**GitHub Actions**:
```yaml
- name: Check Java code formatting
  uses: styler/github-action@v1
  with:
    config-file: '.styler.yml'
    check-only: true
```

## 📊 Monitoring and Observability

### Metrics and Logging

**Performance Metrics**:
- Files processed per second
- Memory usage and garbage collection
- Thread pool utilization
- Plugin execution times

**Error Tracking**:
- Parsing error rates and types
- Plugin failure analysis
- Security violation detection
- Performance bottleneck identification

**Audit Logging**:
- File processing history
- Configuration changes
- Security events
- Performance anomalies

## 🔄 Migration Strategy from Legacy Codebase

### Phase 1: Foundation
1. Create Maven multi-module structure
2. Implement base AST node hierarchy
3. Build core security framework
4. Set up testing infrastructure

### Phase 2: Parser Implementation
1. Implement Java parser with JDK 25 support
2. Add error recovery and incremental parsing
3. Build comprehensive parser test suite
4. Validate parsing accuracy against existing tools

### Phase 3: Formatter Migration
1. Extract checkstyle fixer logic
2. Adapt to plugin architecture
3. Migrate test cases and validation
4. Performance optimization and tuning

### Phase 4: Engine and CLI
1. Implement parallel processing engine
2. Build command-line interface
3. Add configuration system
4. Integration testing and validation

### Phase 5: Production Readiness
1. Performance optimization and profiling
2. Clean up unused dependencies
3. Update documentation and examples
4. Final testing and validation

## ✅ Quality Assurance

### Testing Strategy

**Unit Testing**:
- 100% code coverage for core components
- Property-based testing for AST transformations
- Mock-based testing for plugin interactions
- Performance regression testing

**Integration Testing**:
- End-to-end formatting workflows
- Multi-threaded processing validation
- Security control verification
- Configuration system testing

**Acceptance Testing**:
- Real-world Java project formatting
- Performance benchmarking
- Security penetration testing
- User experience validation

### Code Quality

**Static Analysis**:
- Checkstyle compliance checking
- PMD rule enforcement
- SpotBugs security analysis
- SonarQube quality gate

**Performance Testing**:
- JMH microbenchmarking
- Memory usage profiling
- Thread contention analysis
- Scalability testing

This architecture provides a comprehensive foundation for Styler that addresses all stakeholder requirements while maintaining security, performance, and extensibility as core principles.