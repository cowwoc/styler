# Styler - Java Code Formatter - Project Scope

## Project Objective {#project-objective}

Styler is an unopinionated Java code formatter that supports 100% of JDK 25's features with multi-threaded
file processing capabilities. Unlike Prettier, Styler strives to be configurable and unopinionated. Unlike
checkstyle, Styler bundles auto-fixers for violations whenever possible.

**📋 For complete project details, architecture, and features, see [architecture.md](architecture.md)**

## Core Use Cases {#core-use-cases}

Styler supports these primary use cases across two integration modes:

### 1. AI Agent Integration (Output-Driven Learning with Automatic Context Detection) {#1-ai-agent-integration-output-driven-learning-with-automatic-context-detection}
**Objective**: Drive AI agent behavior through structured violation feedback that provides immediate
actionable guidance for code improvement.

**Flow**:
- AI agent generates code using current understanding
- Code validation via CLI tool provides structured violation feedback
- AI agent processes violation output with rule IDs, fix strategies, and priority scores
- Agent applies corrections based on specific suggestions and learns patterns
- Iterative feedback loop improves AI agent code generation over time

**Technical Requirements**:
-  **Structured Output Generation**: Machine-readable violation reports with rule IDs, fix strategies, and
  priority scores
-  **Context-Specific Suggestions**: Each violation includes tailored remediation guidance based on
  surrounding code
- **Priority-Based Ordering**: Violations sorted by impact (severity × frequency) to guide AI agent focus
- **Iterative Learning Support**: Output format designed for AI pattern recognition and adaptation
- **Context Detection**: Automatic AI vs human detection via environment analysis
- **Violation Tracking**: Integrated during parsing for immediate feedback

### 2. Traditional Build Integration (Batch Processing) {#2-traditional-build-integration-batch-processing}
**Objective**: Format Java source files according to configurable style rules while preserving developer
intent.

**Flow**:
- User specifies Java files or directories to format
- Styler parses files into AST representation
- Apply configured formatting rules through plugin system
- Output formatted code while preserving comments and structure
- Support for incremental formatting of only changed sections

### 3. Multi-threaded Project Processing {#3-multi-threaded-project-processing}
**Objective**: Process large codebases efficiently using parallel file processing.

**Flow**:
- Discover all Java files in project directory
- Distribute files across worker threads using work-stealing pool
- Parse and format files concurrently
- Aggregate results and report progress
- Handle error recovery and partial failures gracefully

### 4. Integrated Violation Detection and Context-Aware Reporting {#4-integrated-violation-detection-and-context-aware-reporting}
**Objective**: Track style violations during parsing with automatic output adaptation for different audiences.

**Dual-Audience Output Architecture**:
- **AI Agent Format**: Structured output with rule IDs, violation counts, fix strategies
- **Human Format**: Narrative explanations with grouped violations by severity
- **Automatic Detection**: No manual --ai-mode flags required

**Technical Features**:
- **Real-time Tracking**: Violations captured during parsing, not post-processing
- **Multi-factor Prioritization**: Severity weight × frequency count for priority scoring
- **Context Preservation**: Source location, surrounding code context for each violation
- **Fix Guidance**: Specific remediation strategies integrated with violation reports

## Sample Configuration {#sample-configuration}

**📋 IMPORTANT**: This is a **sample configuration** used for testing and development. The actual configuration
system should be **flexible** and support any combination of formatting rules.

### Example Configuration for Testing {#example-configuration-for-testing}

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

## Development Philosophy {#development-philosophy}

**🚨 CRITICAL: Implement Only What's Needed Today**

- **No Over-Engineering:** Only implement features immediately required for current use cases
- **YAGNI Principle:** "You Aren't Gonna Need It" - avoid theoretical features or extensible frameworks for hypothetical future needs
- **Focus on Core Value:** Prioritize Java code formatting and AST parsing over architectural elegance

**Implementation Strategy:**
1. Build the simplest version that solves the current problem
2. Add features only when there's a concrete, immediate need
3. Refactor and extend only when existing code becomes insufficient
4. Validate each feature against actual usage before adding more complexity

### Implementation Quality (No Stubbing Principle) {#implementation-quality-no-stubbing-principle}

**🚨 CRITICAL: Complete What You Start**

- **ABSOLUTELY FORBIDDEN**: Stub implementations, TODO comments, placeholder methods, fake return values
- **NO PARTIAL IMPLEMENTATIONS**: Every method must be complete and functional OR throw UnsupportedOperationException
- **NO "IMPLEMENT LATER" PATTERNS**: No return -1, empty implementations, or "// TODO: implement" comments
- **HONEST ERROR HANDLING**: Unsupported features fail fast with descriptive error messages
- **COMPLETE SCENARIO COVERAGE**: Each supported scenario fully functional end-to-end
- **MANDATORY REMOVAL**: Existing stubs MUST be immediately replaced with proper implementations or UnsupportedOperationException

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

### YAGNI + No-Stubbing Synergy {#yagni-no-stubbing-synergy}

**Example Implementation Approach:**
- ✅ **AST Parser V1**: Support JDK 25 features completely (working end-to-end)
- ✅ **Clear Boundaries**: Return "JDK 26+ features not yet supported" error for newer syntax
- ✅ **Future Growth**: V2 adds newer JDK support when needed
- ❌ **Stub Approach**: Return "TODO: implement parsing" for all files

## Key Constraints and Rules {#key-constraints-and-rules}

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

## Technical Capabilities {#technical-capabilities}

### Classpath and Modulepath Access {#classpath-modulepath-access}
Styler can access the project's classpath and modulepath to enable advanced analysis features like
resolving wildcard imports and determining actual class usage.

**Status**: Planned capability (see `add-classpath-support` and `resolve-wildcard-imports` tasks in todo.md)

**Access Methods**:
- **CLI**: `--classpath` and `--module-path` arguments
- **API**: `FormatterConfiguration.withClasspath(List<Path>)` and `.withModulePath(List<Path>)`
- **Maven Plugin**: Automatic access via `MavenProject.getCompileClasspathElements()` and `plexus-java`
  LocationManager for modulepath resolution

**Use Cases**:
- Resolve wildcard imports (`import java.util.*`) to determine which classes are actually used
- Detect truly unused imports by verifying class existence on classpath
- Support JPMS module-aware import analysis

### AST Processing Features {#ast-processing-features}
- **Full Language Support**: Complete JDK 25 feature coverage (pattern matching, string templates, etc.)
- **Comment Preservation**: Maintain all comments, whitespace, and formatting hints
- **Incremental Parsing**: Support for parsing only changed sections of files
- **Error Recovery**: Graceful handling of malformed or partial Java files
- **Multi-version Support**: Parse files written for different Java versions

### Plugin System Capabilities {#plugin-system-capabilities}
- **Modular Design**: Formatting rules implemented as independent plugins
- **Configurable Rules**: Users can enable/disable specific formatting plugins
- **Custom Extensions**: Support for user-defined formatting rules and auto-fixers
- **Conflict Resolution**: Handle conflicts between competing formatting rules

## Performance Requirements {#performance-requirements}

### Performance Targets {#performance-targets}

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

### Architecture Guidelines {#architecture-guidelines}

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

**📋 For detailed technical architecture, threading models, and implementation details, see
[architecture.md](architecture.md)**

## User Experience Requirements {#user-experience-requirements}

### Command-Line Interface Design {#command-line-interface-design}
- **Simple Commands**: Intuitive options for common operations
- **Configuration Discovery**: Automatic config file detection in project directories
- **Progress Reporting**: Clear progress for large codebase processing
- **Error Reporting**: Detailed errors with file locations and suggested fixes

### Configuration Flexibility {#configuration-flexibility}
- **YAML-Based Config**: Human-readable with reasonable defaults
- **Rule Inheritance**: Global to project-specific configuration inheritance
- **Profile Support**: Pre-defined profiles (Google Style, Oracle Style, etc.)
- **Override Capabilities**: Command-line configuration overrides

### Integration Support {#integration-support}
- **AI Agent Integration**: Output-driven learning via structured violation feedback with context-specific fix strategies
- **Build Tool Integration**: Maven and Gradle plugin support
- **IDE Integration**: Language Server Protocol support
- **CI/CD Integration**: Exit codes and reporting for CI systems
- **Git Integration**: Pre-commit hooks and diff-aware formatting

## API Scope and Compatibility Guidelines {#api-scope-and-compatibility-guidelines}

### Command-Line Tool Architecture {#command-line-tool-architecture}

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

### Public vs Internal API Classification {#public-vs-internal-api-classification}

#### Command-Line Interface (Primary Interface) {#command-line-interface-primary-interface}
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

#### Library API (Secondary Interface) {#library-api-secondary-interface}
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

### Development Guidelines {#development-guidelines}

1. **CLI-First Design**: Primary focus on command-line user experience
2. **Library Integration**: Secondary focus on programmatic API for tool integration
3. **Stable Interfaces**: Maintain compatibility for public APIs and configuration formats
4. **Plugin System**: Extensible architecture for custom formatting rules
5. **Performance Focus**: Optimize for large codebase processing efficiency

**📋 Additional details documented in:**
- [build-system.md](build-system.md) - Maven configuration and build
- [style-guide.md](style-guide.md) - Code style validation
- [critical-rules.md](critical-rules.md) - Safety protocols and build integrity
- [architecture.md](architecture.md) - Technical architecture
