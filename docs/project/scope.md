# Styler - Java Code Formatter - Project Scope

## Project Objective

Styler is an unopinionated Java code formatter that supports 100% of JDK 25's features with multi-threaded file processing capabilities. Unlike Prettier, Styler strives to be configurable and unopinionated. Unlike checkstyle, Styler bundles auto-fixers for violations whenever possible.

**📋 For complete project details, architecture, and features, see [architecture.md](architecture.md)**

## Core Use Cases

Styler supports these primary use cases across two integration modes:

### 1. AI Agent Integration (Output-Driven Learning with Automatic Context Detection)
**Objective**: Drive AI agent behavior through structured violation feedback that provides immediate actionable guidance for code improvement.

**Evidence-Based Approach**: Analysis shows output-driven integration enables AI agents to learn and adapt without requiring comprehensive upfront documentation:
- **Feedback-Driven Learning**: AI agents learn style patterns from structured violation output with specific fix strategies
- **Immediate Actionability**: Each violation includes context-specific suggestions and rule explanations
- **Priority-Guided Attention**: Violations sorted by severity × frequency to focus AI agent corrections on high-impact issues
- **Automatic Context Detection**: System detects AI vs human usage without manual flags

**Flow**:
- AI agent generates code using current understanding
- Code validation via CLI tool provides structured violation feedback
- AI agent processes violation output with rule IDs, fix strategies, and priority scores
- Agent applies corrections based on specific suggestions and learns patterns
- Iterative feedback loop improves AI agent code generation over time

**Technical Requirements**:
- **Structured Output Generation**: Machine-readable violation reports with rule IDs, fix strategies, and priority scores
- **Context-Specific Suggestions**: Each violation includes tailored remediation guidance based on surrounding code
- **Priority-Based Ordering**: Violations sorted by impact (severity × frequency) to guide AI agent focus
- **Iterative Learning Support**: Output format designed for AI pattern recognition and adaptation
- **Context Detection**: Automatic AI vs human detection via environment analysis
- **Violation Tracking**: Integrated during parsing for immediate feedback

### 2. Traditional Build Integration (Batch Processing)
**Objective**: Format Java source files according to configurable style rules while preserving developer intent.

**Flow**:
- User specifies Java files or directories to format
- Styler parses files into AST representation
- Apply configured formatting rules through plugin system
- Output formatted code while preserving comments and structure
- Support for incremental formatting of only changed sections

### 3. Multi-threaded Project Processing
**Objective**: Process large codebases efficiently using parallel file processing.

**Flow**:
- Discover all Java files in project directory
- Distribute files across worker threads using work-stealing pool
- Parse and format files concurrently
- Aggregate results and report progress
- Handle error recovery and partial failures gracefully

### 4. Integrated Violation Detection and Context-Aware Reporting
**Objective**: Track style violations during parsing with automatic output adaptation for different audiences.

**Evidence-Based Implementation**:
- **Parser Integration**: Violations detected during AST construction for immediate feedback
- **Automatic Context Detection**: Heuristic detection of AI agent vs human developer usage
- **Progressive Disclosure**: Information architecture adapted to audience needs
- **Prioritization**: Frequency-based scoring system for violation priority

**Dual-Audience Output Architecture**:
- **AI Agent Format**: Structured output with rule IDs, violation counts, fix strategies
- **Human Format**: Narrative explanations with grouped violations by severity
- **Automatic Detection**: No manual --ai-mode flags required

**Technical Features**:
- **Real-time Tracking**: Violations captured during parsing, not post-processing
- **Multi-factor Prioritization**: Severity weight × frequency count for priority scoring
- **Context Preservation**: Source location, surrounding code context for each violation
- **Fix Guidance**: Specific remediation strategies integrated with violation reports

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

- **ABSOLUTELY FORBIDDEN**: Creation, usage, or retention of stub implementations, TODO comments, placeholder methods, fake return values
- **NO PARTIAL IMPLEMENTATIONS**: Every method must be either complete and functional OR throw clear UnsupportedOperationException
- **NO "IMPLEMENT LATER" PATTERNS**: Return -1, empty implementations, or "// TODO: implement" comments
- **HONEST ERROR HANDLING**: Unsupported features must fail fast with descriptive error messages
- **COMPLETE SCENARIO COVERAGE**: Each supported scenario must be fully functional end-to-end
- **MANDATORY REMOVAL**: Any existing stubs discovered during development MUST be immediately replaced with proper implementations or UnsupportedOperationException

**MANDATORY STUB DETECTION AND PREVENTION**:
- Code quality auditors MUST reject ANY implementation containing TODO comments
- Security auditors MUST reject placeholder implementations that could hide vulnerabilities
- Build validators MUST fail if stub patterns are detected in production code

**PROHIBITED PATTERNS**:
❌ `return -1; // TODO: implement later`
❌ `// Placeholder implementation`
❌ Methods that do nothing but advance tokens without logic
❌ Empty catch blocks or generic exception swallowing
❌ Fake success return values for unimplemented features

**REQUIRED PATTERNS**:
✅ `throw new UnsupportedOperationException("Feature X requires implementation of Y")`
✅ Complete implementations that handle all edge cases
✅ Clear error boundaries with helpful messages

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
- ✅ **AST Parser V1**: Support JDK 25 features completely (working end-to-end)
- ✅ **Clear Boundaries**: Return "JDK 26+ features not yet supported" error for newer syntax
- ✅ **Future Growth**: V2 adds newer JDK support when needed
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

**Security Model for Parser Operations:**
- **Single-User Scenario**: Users have access to source code being parsed
- **Resource Protection**: Prevent accidental resource exhaustion (stack overflow, memory)
- **System Stability**: Prevent parser crashes from affecting system stability
- **Usability Priority**: Error messages prioritize helpful debugging information
- **Attack Scope**: Focus on resource exhaustion, not data exfiltration or information disclosure
- **Reasonable Limits**: Protection limits appropriate for legitimate code formatting use cases

**Quality Requirements:**
- Zero compilation errors after formatting
- Preserve all comments and code semantics
- <1% false positive rate in formatting decisions
- Support for incremental formatting of changed sections only

## Technical Capabilities

### AST Processing Features
- **Full Language Support**: Complete JDK 25 feature coverage (pattern matching, string templates, etc.)
- **Comment Preservation**: Maintain all comments, whitespace, and formatting hints
- **Incremental Parsing**: Support for parsing only changed sections of files
- **Error Recovery**: Graceful handling of malformed or partial Java files
- **Multi-version Support**: Parse files written for different Java versions

### Plugin System Capabilities
- **Modular Design**: Formatting rules implemented as independent plugins
- **Configurable Rules**: Users can enable/disable specific formatting plugins
- **Custom Extensions**: Support for user-defined formatting rules and auto-fixers
- **Conflict Resolution**: Handle conflicts between competing formatting rules

## Performance Requirements

### Performance Targets

**AI Agent Mode**:
- **Validation Latency**: <50ms for real-time format validation
- **Rule API**: <10ms for format rule specification queries
- **Memory Footprint**: Lightweight parsing for validation-only operations
- **CLI Overhead**: Minimal JVM startup time for fast responses

**Traditional Mode**:
- **Throughput**: 100-150 files/second on modern systems
- **Memory Efficiency**: Bounded heap usage with automatic garbage collection optimization
- **Scalability**: Near-linear scaling up to 16 CPU cores
- **Latency**: <100ms response time for single file formatting

### Architecture Guidelines

**Command-Line Tool Architecture**:
- **Self-Contained Operations**: Tool operates independently without external services
- **File-Based Processing**: Direct file system interaction for reading and writing Java files
- **Configuration-Driven**: All formatting behavior controlled through configuration files
- **Stateless Execution**: Each run processes files independently without persistent state

**Plugin Architecture**:
- **Rule-Based Formatting**: All formatting logic implemented as pluggable rules
- **Configuration Integration**: Plugins configured through YAML configuration files
- **Extension Points**: Clear interfaces for adding custom formatting rules
- **Performance Optimization**: Efficient plugin loading and rule application

**📋 For detailed technical architecture, threading models, and implementation details, see [architecture.md](architecture.md)**

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
- **AI Agent Integration**: Output-driven learning through structured violation feedback with context-specific fix strategies
- **Build Tool Integration**: Maven and Gradle plugin support for traditional workflows
- **IDE Integration**: Language Server Protocol for editor integration
- **CI/CD Integration**: Exit codes and reporting for continuous integration systems
- **Git Integration**: Pre-commit hooks and diff-aware formatting

## API Scope and Compatibility Guidelines

### Command-Line Tool Architecture

**Important**: This system serves **dual integration modes** with automatic context detection:

**AI Agent Mode** (Output-Driven Learning Integration):
- Structured violation feedback for iterative learning
- Priority-ordered violation reports with actionable fix strategies
- Context-specific suggestions tailored to actual code violations
- Machine-readable output format optimized for pattern recognition
- Automatic context detection via environment analysis
- Compatible with agent tool capabilities (CLI validation)

**Traditional Mode** (Interactive/Build Processing):
- Command-line interface for direct user interaction
- Human-friendly narrative violation reporting
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