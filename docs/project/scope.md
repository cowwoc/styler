# Styler - Java Code Formatter - Project Scope

## Project Objective

Styler is an unopinionated Java code formatter that supports 100% of JDK 25's features with multi-threaded file processing capabilities. Unlike Prettier, Styler strives to be configurable and unopinionated. Unlike checkstyle, Styler bundles auto-fixers for violations whenever possible.

**📋 For complete project details, architecture, and features, see [architecture.md](architecture.md)**

## Core Use Cases

Styler supports these primary use cases:

### 1. Java Code Formatting
**Objective**: Format Java source files according to configurable style rules while preserving developer intent.

**Flow**:
- User specifies Java files or directories to format
- Styler parses files into AST representation
- Apply configured formatting rules through plugin system
- Output formatted code while preserving comments and structure
- Support for incremental formatting of only changed sections

### 2. Multi-threaded Project Processing
**Objective**: Process large codebases efficiently using parallel file processing.

**Flow**:
- Discover all Java files in project directory
- Distribute files across worker threads using work-stealing pool
- Parse and format files concurrently
- Aggregate results and report progress
- Handle error recovery and partial failures gracefully

### 3. Auto-fixing and Violation Correction
**Objective**: Automatically fix common code style violations and formatting issues.

**Requirements**:
- **Comprehensive**: Support for all major Java style guide violations
- **Safe**: Never change code semantics, only formatting and style
- **Configurable**: Users can enable/disable specific auto-fixes
- **Extensible**: Plugin architecture for custom auto-fixers

## Sample Configuration

**📋 IMPORTANT**: This is a **sample configuration** used for testing and development. The actual configuration system should be **flexible** and support any combination of formatting rules.

### Example Configuration for Testing

**Core Settings:**
- **Language Version**: JDK 25 (full feature support)
- **Thread Pool Size**: Auto-detect CPU cores (default)
- **File Processing**: Concurrent with work-stealing pool
- **Memory Limits**: 512MB heap per 1000 files processed

**Formatting Rules:**
- **Line Length**: 120 characters (configurable)
- **Indentation**: 4 spaces (configurable: tabs/spaces/mixed)
- **Braces**: Next line for classes/methods, same line for control structures
- **Import Organization**: Group by package, remove unused imports
- **Whitespace**: Consistent spacing around operators and keywords
- **Comment Preservation**: Maintain all comments with proper formatting

**Auto-fixer Settings:**
- **Enable Auto-fixes**: True (user configurable)
- **Fix Violations**: Line length, import organization, spacing issues
- **Preserve Semantics**: Never change code behavior, only formatting
- **Plugin System**: Extensible for custom formatting rules

## Development Philosophy

**🚨 CRITICAL: Implement Only What's Needed Today**

- **No Over-Engineering:** Only implement features immediately required for current use cases
- **No Theoretical Features:** Avoid adding capabilities that "might be useful in the future" but aren't needed now
- **YAGNI Principle:** "You Aren't Gonna Need It" - resist building extensible frameworks for hypothetical future needs
- **Focus on Core Value:** Prioritize Java code formatting and AST parsing over architectural elegance

**Implementation Strategy:**
1. Build the simplest version that solves the current problem
2. Add features only when there's a concrete, immediate need
3. Refactor and extend only when existing code becomes insufficient
4. Validate each feature against actual usage before adding more complexity

### Implementation Quality (No Stubbing Principle)

**🚨 CRITICAL: Complete What You Start**

- **No Stub Implementations:** When implementing a feature, implement the chosen scenarios completely end-to-end
- **No Placeholder Code:** Avoid TODO comments, fake return values, or "implement later" patterns
- **Complete Scenario Coverage:** Each supported scenario must be fully functional, not partially working
- **Honest Error Handling:** Return genuine errors for unsupported scenarios rather than fake success codes

**No-Stubbing Implementation Strategy:**
1. Choose minimal viable scenarios for each feature (YAGNI principle)
2. Implement chosen scenarios completely end-to-end
3. Explicitly reject unsupported scenarios with clear error messages
4. Each commit should contain working, testable functionality for its scenarios
5. Extend scenario coverage incrementally in future iterations

### YAGNI + No-Stubbing Synergy

**These principles work together harmoniously:**

1. **YAGNI**: Choose minimal scenarios to implement
2. **No-Stubbing**: Implement chosen scenarios completely end-to-end
3. **Incremental Growth**: Add more scenarios in later iterations
4. **Honest Boundaries**: Clearly communicate what is/isn't supported

**Example Implementation Approach:**
- ✅ **AST Parser V1**: Support JDK 17 features completely (working end-to-end)
- ✅ **Clear Boundaries**: Return "JDK 25 features not yet supported" error for newer syntax
- ✅ **Future Growth**: V2 adds JDK 25 support when needed
- ❌ **Stub Approach**: Return "TODO: implement parsing" for all files

**Practical Guidelines:**
1. **Scope Selection**: Choose scenarios that provide immediate value (YAGNI)
2. **Complete Implementation**: Make chosen scenarios work perfectly (No-Stubbing)
3. **Error Boundaries**: Fail fast with clear messages for unsupported scenarios
4. **Incremental Expansion**: Add scenarios based on actual need, not speculation

## Key Constraints and Rules

**Performance Constraints:**
- Process 100+ files/second on modern multi-core systems
- Memory usage < 512MB per 1000 files parsed
- Linear scalability up to 32 CPU cores
- Maximum file processing timeout: 30 seconds per file

**Security Constraints:**
- AST-only parsing without code execution capabilities
- Strict path validation and sandboxing for file operations
- File size limits (10MB maximum per file)
- Resource monitoring and automatic termination of excessive operations

**Quality Requirements:**
- Zero compilation errors after formatting
- Preserve all comments and code semantics
- <1% false positive rate in formatting decisions
- Support for incremental formatting of changed sections only

## AST Parser Architecture

### Multi-threaded AST Processing System
- **Independent Operation**: AST parsing runs without external dependencies
- **Comprehensive Coverage**: Handle all Java language constructs from Java 8 to JDK 25
- **Parallel Processing**: Efficient multi-threaded file processing with work-stealing
- **Extensible Design**: Architecture supports additional formatting rules and language features

### AST Processing Capabilities
- **Full Language Support**: Complete JDK 25 feature coverage (pattern matching, string templates, etc.)
- **Comment Preservation**: Maintain all comments, whitespace, and formatting hints
- **Incremental Parsing**: Support for parsing only changed sections of files
- **Error Recovery**: Graceful handling of malformed or partial Java files
- **Multi-version Support**: Parse files written for different Java versions

### Plugin System Architecture
- **Modular Design**: Formatting rules implemented as independent plugins
- **Configurable Rules**: Users can enable/disable specific formatting plugins
- **Custom Extensions**: Support for user-defined formatting rules and auto-fixers
- **Conflict Resolution**: Handle conflicts between competing formatting rules
- **Performance Optimization**: Efficient plugin loading and execution

## Performance and Threading Model

### Multi-threaded Processing Design
The formatter uses a sophisticated threading model for optimal performance:

- **Work-Stealing Thread Pool**: Dynamic load balancing across available CPU cores
- **File-Level Parallelism**: Individual files processed concurrently
- **Memory-Bounded Processing**: Automatic memory management with configurable limits
- **Progress Reporting**: Real-time progress updates for large codebase processing

### Performance Targets
- **Throughput**: 100-150 files/second on modern systems
- **Memory Efficiency**: Bounded heap usage with automatic garbage collection optimization
- **Scalability**: Near-linear scaling up to 16 CPU cores
- **Latency**: <100ms response time for single file formatting

### Security and Resource Management
- **Sandboxed Execution**: No dynamic code compilation or execution
- **Path Validation**: Comprehensive input validation for all file operations
- **Resource Limits**: Automatic termination of excessive processing operations
- **Error Isolation**: Failures in one file don't affect processing of other files

## Architecture Guidelines

### Command-Line Tool Architecture

The formatter is designed as a **standalone command-line tool** with the following principles:

- **Self-Contained Operations**: Tool operates independently without external services
- **File-Based Processing**: Direct file system interaction for reading and writing Java files
- **Configuration-Driven**: All formatting behavior controlled through configuration files
- **Stateless Execution**: Each run processes files independently without persistent state

### Plugin Architecture

- **Rule-Based Formatting**: All formatting logic implemented as pluggable rules
- **Configuration Integration**: Plugins configured through YAML configuration files
- **Extension Points**: Clear interfaces for adding custom formatting rules
- **Performance Optimization**: Efficient plugin loading and rule application

### Threading and Concurrency

- **Parallel File Processing**: Multiple files processed simultaneously using thread pools
- **Lock-Free AST Operations**: Immutable AST nodes enable safe concurrent processing
- **Memory Management**: Automatic resource cleanup and bounded memory usage
- **Error Recovery**: Individual file failures don't affect overall processing

## User Experience Requirements

### Command-Line Interface Design
- **Simple Commands**: Intuitive command-line options for common operations
- **Configuration Discovery**: Automatic detection of configuration files in project directories
- **Progress Reporting**: Clear progress indication for large codebase processing
- **Error Reporting**: Detailed error messages with file locations and suggested fixes

### Configuration Flexibility
- **YAML-Based Config**: Human-readable configuration with reasonable defaults
- **Rule Inheritance**: Configuration inheritance from global to project-specific settings
- **Profile Support**: Pre-defined configuration profiles (Google Style, Oracle Style, etc.)
- **Override Capabilities**: Command-line overrides for configuration settings

### Integration Support
- **Build Tool Integration**: Maven and Gradle plugin support
- **IDE Integration**: Language Server Protocol for editor integration
- **CI/CD Integration**: Exit codes and reporting for continuous integration systems
- **Git Integration**: Pre-commit hooks and diff-aware formatting

## API Scope and Compatibility Guidelines

### Command-Line Tool Architecture

**Important**: This system is primarily a **command-line formatting tool**. Integration points include:
- Command-line interface for direct user interaction
- Maven/Gradle plugins for build system integration
- Language Server Protocol for IDE integration
- Library API for programmatic usage

### Public vs Internal API Classification

#### Command-Line Interface (Primary Interface)
**Definition**: Command-line options and configuration file formats for end users.

**Characteristics**:
- Stable command-line arguments and options
- Backward-compatible configuration file formats
- Semantic versioning for breaking changes
- Clear deprecation policies for removed features

**Examples**:
- Command-line flags like `--config`, `--check`, `--fix`
- Configuration file schema and supported options
- Exit codes and error message formats

#### Library API (Secondary Interface)
**Definition**: Programmatic interface for build tools and IDE integrations.

**Characteristics**:
- Public Java API for formatting operations
- Stable interfaces for configuration and rule management
- Plugin extension points for custom rules
- **Should maintain backward compatibility across minor versions**

**Examples**:
- `TidyFormatter.format(String javaCode, Configuration config)`
- Plugin interfaces for custom formatting rules
- Configuration builders and validation APIs

### Development Guidelines

1. **CLI-First Design**: Primary focus on command-line user experience
2. **Library Integration**: Secondary focus on programmatic API for tool integration
3. **Stable Interfaces**: Maintain compatibility for public APIs and configuration formats
4. **Plugin System**: Extensible architecture for custom formatting rules
5. **Performance Focus**: Optimize for large codebase processing efficiency

**📋 Additional technical details, build instructions, and coding standards are documented in:**
- [build-system.md](build-system.md) - Maven configuration and build commands
- [../code-style-human.md](../code-style-human.md) - Java coding conventions and formatting rules
- [critical-rules.md](critical-rules.md) - Critical safety protocols and build integrity requirements
- [architecture.md](architecture.md) - Detailed technical architecture documentation