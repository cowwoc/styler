# TODO List - Styler Java Code Formatter (Main Branch)

## 🚀 READY TO WORK NOW (Multi-Instance Coordination)

**Current Status**: Phase C in progress - `create-maven-plugin` complete, `create-jmh-benchmarks` and `benchmark-concurrency-models` now unblocked

**COMPLETED**:
- `implement-line-length-formatter` - Line Length Formatter ✅ COMPLETE
- `implement-import-organization` - Import Organization ✅ COMPLETE
- `implement-file-processing-pipeline` - File Processing Pipeline Infrastructure ✅ COMPLETE
- `implement-pipeline-stages` - Pipeline Stage Implementation ✅ COMPLETE (2025-12-13)
- `implement-ai-violation-output` - AI Violation Output ✅ COMPLETE (2025-12-05)
- `create-error-message-catalog` - Error Catalog ✅ COMPLETE (2025-12-05)
- `implement-cli-formatter-integration` - CLI Integration ✅ COMPLETE (2025-12-09)
- `implement-file-discovery` - File Discovery ✅ COMPLETE (2025-12-09)
- `implement-virtual-thread-processing` - Virtual Thread Processing ✅ COMPLETE (2025-12-10)
- `implement-brace-formatting` - Brace Formatting ✅ COMPLETE (2025-12-10)
- `implement-whitespace-formatting` - Whitespace Formatting ✅ COMPLETE (2025-12-11)
- `implement-indentation-formatting` - Indentation Formatting ✅ COMPLETE (2025-12-11)

**Phase B**: ✅ COMPLETE (8/8 tasks)
**Phase C**: In progress (4/6 tasks - benchmarks now unblocked)

**Phase A - ✅ COMPLETE (5/5 tasks)**:
- ✅ `create-styler-formatter-module` - styler-formatter module (defines FormattingRule interfaces)
- ✅ `implement-index-overlay-parser` - Index-Overlay AST Parser (ast + parser modules)
- ✅ `implement-toml-configuration` - TOML Configuration (config module)
- ✅ `implement-cli-arguments` - CLI Arguments (cli module)
- ✅ `implement-security-framework` - Security Framework (security module)

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

### Classpath Infrastructure ✅ COMPLETE (2025-12-15)

- [x] **COMPLETE:** `add-classpath-support` - Enable passing project classpath/modulepath into Styler (2025-12-15)

- [ ] **READY:** `resolve-wildcard-imports` - Enhance import organization with wildcard resolution
  - **Dependencies**: `implement-import-organization` ✅ COMPLETE, `add-classpath-support` ✅ COMPLETE
  - **Blocks**: None (optional enhancement)
  - **Parallelizable With**: Phase B and beyond (independent enhancement)
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

### File Processing Pipeline Infrastructure ✅ COMPLETE
- [x] **COMPLETE:** `implement-file-processing-pipeline` - Pipeline infrastructure (2025-12-04)
  - **Delivered**: FileProcessingPipeline class, StageResult types, ProcessingContext, AbstractPipelineStage
  - **Note**: Stage implementations return `StageResult.Skipped` - see `implement-pipeline-stages` for actual implementation

### Pipeline Stage Implementation (Critical Path)
- [x] **COMPLETE:** `implement-pipeline-stages` - Wire parser and formatters into pipeline stages (2025-12-13)
  - **Dependencies**: `create-styler-parser-module` ✅, formatters ✅, `implement-file-processing-pipeline` ✅
  - **Blocks**: `create-maven-plugin`, `create-jmh-benchmarks`, `benchmark-concurrency-models`, `add-regression-test-suite`
  - **Parallelizable With**: None (critical path)
  - **Estimated Effort**: 2-3 days
  - **Purpose**: Make the pipeline actually process files (currently all stages return Skipped)
  - **Scope**: Implement the 4 pipeline stages to perform real work
  - **Components**:
    - **ParseStage**: Use parser to parse Java files into AST
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
      - Validation-only mode: Report violations via AI output
      - Fix mode: Write formatted code back to file
  - **Integration**: Connects parser, all formatting rules, violation output
  - **Verification**: Run `styler check` on a real Java file and see actual violations
  - **Quality**: Integration tests with real Java files, verify end-to-end processing

### Structured Violation Output (AI Agent Integration) ✅ COMPLETE
- [x] **COMPLETE:** `implement-ai-violation-output` - Structured violation feedback for AI agents (2025-12-05)

### Proactive Rules Summary Export
- [ ] **READY:** `implement-rules-summary-export` - Export formatting rules as markdown for AI pre-guidance
  - **Dependencies**: `implement-ai-violation-output` ✅, formatters ✅
  - **Blocks**: None (optional enhancement)
  - **Parallelizable With**: `create-error-message-catalog`, `implement-cli-formatter-integration`
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

### Context-Aware Violation Output for AI Agents
- [ ] **READY:** `implement-ai-context-limiting` - Limit violation output to preserve AI agent context window
  - **Dependencies**: `implement-ai-violation-output` ✅
  - **Blocks**: None (optimization for AI workflows)
  - **Parallelizable With**: `implement-rules-summary-export`, any Phase C/D/E task
  - **Estimated Effort**: 1-2 days
  - **Purpose**: Avoid wasting AI agent context by limiting detailed violations to actionable count
  - **Problem**: AI agents have limited context windows; reporting 100+ violations wastes tokens on issues
    the agent won't fix before re-running the check
  - **Scope**: Smart output limiting when caller is an AI agent
  - **Design Considerations** (not prescriptive - investigate optimal approach):
    - Limit detailed violations to N (configurable, e.g., 10-20) that agent can realistically fix
    - **Priority-based selection** (NOT chronological order):
      - By severity (errors before warnings)
      - By frequency (most common violation types first - higher impact per fix)
      - By locality (cluster violations in same file - reduces context switching)
    - Provide terse summary for remaining violations (type + count)
  - **Example Output**:
    ```
    Top 15 violations by priority (47 total):
    [... full violation details for highest-priority 15 ...]

    Summary of remaining 32 violations:
    - LINE_LENGTH: 18 violations across 12 files
    - IMPORT_ORDER: 8 violations across 5 files
    - INDENTATION: 6 violations across 4 files
    ```
  - **Detection**: CLI flag `--ai-mode` or `--max-violations=N`, or auto-detect from environment
  - **Quality**: Benchmark context savings, validate AI agents fix more violations per iteration

### Error Message Catalog ✅ COMPLETE
- [x] **COMPLETE:** `create-error-message-catalog` - Comprehensive error messages for AI and human users (2025-12-05)

### CLI Integration ✅ COMPLETE
- [x] **COMPLETE:** `implement-cli-formatter-integration` - Wire CLI → pipeline → output (2025-12-09)
  -  **Integration**: Connects all Phase A and Phase B components (CLI args, pipeline, output, errors)
  - **Quality**: Clear error messages, proper exit codes, progress reporting
  - **Estimated Effort**: 2-3 days

### Multi-Configuration Architecture ✅ COMPLETE (2025-12-14)
- [x] **COMPLETE:** `implement-multi-config-architecture` - Enable formatting rules to receive all configurations (2025-12-14)

---

## Phase C: Scale & Real-World Testing

**Goal**: Scale to large codebases with parallel processing, build to 5 formatting rules for realistic
benchmarking, and validate with Maven plugin integration.

### File Discovery ✅ COMPLETE (2025-12-09)

### Virtual Thread Processing (Thread-per-File Baseline) ✅ COMPLETE (2025-12-10)

### Additional Formatting Rules (Build to 5 Total Rules) ✅ COMPLETE

- [x] **COMPLETE:** `implement-indentation-formatting` - Indentation formatting (tabs/spaces/mixed) (2025-12-11)

### Maven Plugin (Early Real-World Testing) ✅ COMPLETE (2025-12-22)
- [x] **COMPLETE:** `create-maven-plugin` - Maven plugin for build system integration (2025-12-22)

### Performance Benchmarking
- [ ] **READY:** `create-jmh-benchmarks` - Validate performance claims with JMH benchmarks
  - **Dependencies**: `implement-pipeline-stages` ✅, `create-maven-plugin` ✅, `implement-virtual-thread-processing` ✅, all formatters ✅
  - **Blocks**: `add-regression-test-suite`, `setup-github-actions-ci`
  - **Parallelizable With**: `benchmark-concurrency-models`
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

### Concurrency Model Benchmark
- [ ] **READY:** `benchmark-concurrency-models` - Compare thread-per-file vs thread-per-block parallelism
  - **Dependencies**: `implement-pipeline-stages` ✅, `implement-virtual-thread-processing` ✅, all formatters ✅
  - **Blocks**: None (optional optimization study)
  - **Parallelizable With**: `create-maven-plugin`
  - **Estimated Effort**: 2-3 days
  - **Purpose**: Determine optimal concurrency strategy for styler through empirical testing
  - **Scope**: Benchmark thread-per-file (baseline from `implement-virtual-thread-processing`) vs thread-per-block concurrency
  - **Trigger Criteria**: Execute ONLY when `styler check` processes real Java files and reports actual
    violations (proving pipeline stages are functional)
  - **Prerequisites**: `implement-pipeline-stages` complete (pipeline does real work), 5 formatting rules integrated
  - **Comparison Approaches**:
    - **Thread-per-file (baseline)**: One virtual thread per file (current `implement-virtual-thread-processing` implementation)
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
  - **Integration**: Uses `implement-virtual-thread-processing` (thread-per-file), all formatters, real-world projects
  - **Quality**: Statistical rigor, JMH methodology, 95% confidence intervals

---

## Phase D: Polish & Production Readiness

**Goal**: Production-ready release with testing, CI/CD, and documentation.

### D1. Comprehensive Testing
- [ ] **BLOCKED:** `add-regression-test-suite` - Real-world Java project regression tests
  - **Dependencies**: `implement-pipeline-stages`, `create-maven-plugin`, all formatters ✅
  - **Blocks**: `setup-github-actions-ci`
  - **Parallelizable With**: Essential Documentation tasks
  - **Estimated Effort**: 2-3 days (for regression suite)
  - **Purpose**: Prevent regressions by testing against real-world Java projects
  - **Scope**: Curated Java projects with before/after formatting comparisons
  - **Projects**: Spring Framework, Apache Commons, Guava, JUnit5, Mockito
  - **Coverage**: Various Java versions, coding styles, edge cases, large files
  - **Integration**: Automated regression testing in CI/CD pipeline
  - **Quality**: Golden file testing, failure analysis

- [ ] **BLOCKED:** `add-cli-integration-tests` - End-to-end CLI integration tests
  - **Dependencies**: `implement-pipeline-stages`, `implement-cli-formatter-integration` ✅, `implement-file-discovery` ✅, `implement-virtual-thread-processing` ✅
  - **Blocks**: `setup-github-actions-ci`
  - **Parallelizable With**: `add-regression-test-suite`, Essential Documentation tasks
  - **Estimated Effort**: 1-2 days
  - **Purpose**: Validate complete CLI functionality with real Java files
  - **Scope**: Integration tests covering CLI arguments, file processing, output
  - **Scenarios**: Single files, directories, config variants, error conditions
  - **Integration**: Uses real Java files, temporary directories, process execution
  - **Quality**: Comprehensive scenario coverage, clear assertions

### CI/CD Pipeline
- [ ] **BLOCKED:** `setup-github-actions-ci` - Automated testing and release pipeline
  -  **Dependencies**: Comprehensive Testing tasks, `create-jmh-benchmarks`, `create-maven-plugin`
  - **Blocks**: Production releases
  - **Parallelizable With**: Essential Documentation tasks
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
  - **Dependencies**: `implement-cli-formatter-integration`, `implement-configuration-system` ✅, `create-maven-plugin`
  - **Blocks**: Production release (documentation required for users)
  - **Parallelizable With**: Comprehensive Testing tasks, `setup-github-actions-ci`
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
  - **Dependencies**: formatters (FormattingRule API), `implement-configuration-system` ✅, `create-maven-plugin`
  - **Blocks**: Production release (API docs required for integration)
  - **Parallelizable With**: Comprehensive Testing tasks, `setup-github-actions-ci`, `create-user-documentation`
  - **Estimated Effort**: 1-2 days
  - **Purpose**: Document public APIs for library and plugin integration
  - **Scope**: Comprehensive Javadoc for public classes, interfaces, methods
  - **Coverage**: FormattingRule interface, configuration APIs, plugin extension points
  - **Integration**: Generated during Maven build, published with releases
  - **Quality**: Complete coverage, code examples, clear descriptions

---

## Phase E: Architecture Refinement

**Goal**: Improve formatter architecture for better maintainability, accuracy, and extensibility.

### Parser Error Handling Enhancement ✅ COMPLETE (2025-12-16)
- [x] **COMPLETE:** `add-parser-error-record` - Add ParseError record and update Parser to return errors in result (2025-12-16)

### AST Extension for Formatter Support ✅ COMPLETE (2025-12-17)
- [x] **COMPLETE:** `extend-ast-support` - Extend AST parser to support all Java constructs needed by formatters (2025-12-17)
  - **Dependencies**: `add-parser-error-record` ✅
  - **Blocks**: `migrate-formatters-to-ast`
  - **Purpose**: Add missing AST node types and parsing support required for AST-based formatting
  - **Components**:
    - Add ENUM_CONSTANT node creation in Parser.parseEnumConstant()
    - Add SWITCH_EXPRESSION to indent-producing node types
    - Fix duplicate BLOCK node allocation in lambda parsing
    - Add missing TokenType entries for Java 21+ constructs
  - **Quality**: All 357 formatter tests passing

### AST-Based Formatter Migration ✅ COMPLETE (2025-12-17)
- [x] **COMPLETE:** `migrate-formatters-to-ast` - Migrate all formatting rules to AST-based processing (2025-12-17)
  - **Dependencies**: `extend-ast-support` ✅, `implement-pipeline-stages` ✅, all formatters ✅
  - **Purpose**: Replace string/regex-based formatting logic with AST-aware transformations for higher
    accuracy and maintainability
  - **Components Migrated**:
    - **AstPositionIndex**: New spatial index for efficient position-to-node lookup in AST
    - **TransformationContext**: Updated to expose AST via NodeArena and AstPositionIndex
    - **BraceFormattingRule**: Uses AST to exclude literals/comments from brace detection
    - **WhitespaceFormattingRule**: Uses AST for context-aware spacing rules
    - **IndentationFormattingRule**: Uses AST depth for indentation calculation
    - **ImportOrganizerFormattingRule**: Enhanced with AST-based literal/comment exclusion
    - **LineLengthFormattingRule**: Uses AST for context detection
  - **Removed**: SourceCodeUtils (replaced by AST-based position index)
  - **Quality**: All 357 formatter tests passing

### Method Reference Parser Support ✅ COMPLETE (2025-12-17)
- [x] **COMPLETE:** `add-method-reference-support` - Add parser support for method reference expressions (2025-12-17)
  - **Dependencies**: `add-parser-error-record` ✅, `extend-ast-support` ✅
  - **Purpose**: Enable parsing of method reference expressions (`Type::method`, `object::method`)
  - **Implementation**: Added DOUBLE_COLON handling in parsePostfix() for static, instance, and constructor refs
  - **Quality**: 24 parser tests for all method reference variants

### Parser Bug: Generic Type Parameters
- [x] **DONE:** `fix-generic-type-parsing` - Fix parser failure on generic type parameters (2025-12-22)
  - **Dependencies**: `add-parser-error-record` ✅
  - **Blocks**: Self-hosting (styler cannot format its own codebase)
  - **Parallelizable With**: `fix-class-literal-parsing`, `fix-comment-in-expression-parsing`
  - **Estimated Effort**: 1-2 days
  - **Purpose**: Enable parsing of generic type parameters like `Optional<?>`, `Supplier<Path>`, `Consumer<?>`
  - **Current Error**: `Expected IDENTIFIER but found GT at position X`
  - **Affected Files**: ConfigDiscovery.java, ExecutionTimeoutManager.java, Node.java, PathResolver.java,
    FormatterDriver.java, and others
  - **Root Cause**: Parser expects identifier after `<` but encounters `>` (for wildcards) or type name
    followed by `>`
  - **Scope**: Fix generic type parameter parsing in type references
  - **Components**:
    - Handle wildcard types (`?`, `? extends T`, `? super T`)
    - Handle nested generics (`Map<String, List<Integer>>`)
    - Handle diamond operator (`new ArrayList<>()`)
  - **Verification**: Run `styler:check` on styler codebase - no GT-related errors
  - **Quality**: Parser tests for all generic type variants

### Parser Bug: Class Literals
- [ ] **READY:** `fix-class-literal-parsing` - Fix parser failure on `.class` literals
  - **Dependencies**: `add-parser-error-record` ✅
  - **Blocks**: Self-hosting (styler cannot format its own codebase)
  - **Parallelizable With**: `fix-generic-type-parsing`, `fix-comment-in-expression-parsing`
  - **Estimated Effort**: 0.5-1 day
  - **Purpose**: Enable parsing of class literal expressions like `String.class`, `Integer.class`
  - **Current Error**: `Expected identifier after '.' but found CLASS`
  - **Affected Files**: SecurityConfigTest.java, RecursionDepthTrackerTest.java, PathSanitizerTest.java
  - **Root Cause**: Parser's member access handling doesn't recognize `class` as a valid suffix after `.`
  - **Scope**: Handle `.class` as a valid postfix expression
  - **Components**:
    - Recognize `class` keyword after `.` in postfix expressions
    - Handle `Type.class`, `Type[].class`, `primitive.class` variants
  - **Verification**: Run `styler:check` on styler codebase - no CLASS-related errors
  - **Quality**: Parser tests for class literal expressions

### Parser Bug: Comments in Expressions
- [ ] **READY:** `fix-comment-in-expression-parsing` - Fix parser failure when comments appear in expressions
  - **Dependencies**: `add-parser-error-record` ✅
  - **Blocks**: Self-hosting (styler cannot format its own codebase)
  - **Parallelizable With**: `fix-generic-type-parsing`, `fix-class-literal-parsing`
  - **Estimated Effort**: 1 day
  - **Purpose**: Enable parsing of code with line comments appearing within expressions
  - **Current Error**: `Unexpected token in expression: LINE_COMMENT at position X`
  - **Affected Files**: FileValidatorTest.java, ExecutionTimeoutManagerTest.java, MemoryMonitorTest.java
  - **Root Cause**: Parser's expression parsing doesn't skip/handle comment tokens
  - **Scope**: Handle comments appearing within expressions without breaking parsing
  - **Components**:
    - Skip LINE_COMMENT tokens during expression parsing
    - Skip BLOCK_COMMENT tokens during expression parsing
    - Preserve comment positions for formatter (comments are formatting-relevant)
  - **Verification**: Run `styler:check` on styler codebase - no LINE_COMMENT-related errors
  - **Quality**: Parser tests for expressions containing comments

### Parser Enhancement: Missing Node Types
- [ ] **READY:** `add-parameterized-type-nodes` - Create AST nodes for parameterized types
  - **Dependencies**: `add-parser-error-record` ✅, `fix-generic-type-parsing` ✅
  - **Blocks**: None (enhancement for better AST fidelity)
  - **Parallelizable With**: `add-wildcard-type-nodes`, `add-parameter-declaration-nodes`
  - **Estimated Effort**: 1-2 days
  - **Purpose**: Create PARAMETERIZED_TYPE nodes for generic type usage (`List<String>`, `Map<K,V>`)
  - **Current State**: Parser recognizes parameterized types but only creates QUALIFIED_NAME nodes for
    the base type, without capturing the type arguments
  - **Scope**: Create PARAMETERIZED_TYPE nodes with proper child structure
  - **Components**:
    - PARAMETERIZED_TYPE node for the overall type (`List<String>`)
    - Child QUALIFIED_NAME for base type (`List`)
    - Child nodes for type arguments (`String`)
  - **Quality**: Parser tests validating PARAMETERIZED_TYPE node creation and structure

- [ ] **READY:** `add-wildcard-type-nodes` - Create AST nodes for wildcard types
  - **Dependencies**: `add-parser-error-record` ✅, `fix-generic-type-parsing` ✅
  - **Blocks**: None (enhancement for better AST fidelity)
  - **Parallelizable With**: `add-parameterized-type-nodes`, `add-parameter-declaration-nodes`
  - **Estimated Effort**: 1 day
  - **Purpose**: Create WILDCARD_TYPE nodes for bounded/unbounded wildcards (`?`, `? extends T`, `? super T`)
  - **Current State**: Parser recognizes wildcards but doesn't create explicit WILDCARD_TYPE nodes
  - **Scope**: Create WILDCARD_TYPE nodes with bound information
  - **Components**:
    - WILDCARD_TYPE node for unbounded wildcards (`?`)
    - WILDCARD_TYPE with upper bound for `? extends T`
    - WILDCARD_TYPE with lower bound for `? super T`
  - **Quality**: Parser tests validating WILDCARD_TYPE node creation for all wildcard variants

- [ ] **READY:** `add-parameter-declaration-nodes` - Create AST nodes for method/constructor parameters
  - **Dependencies**: `add-parser-error-record` ✅
  - **Blocks**: None (enhancement for better AST fidelity)
  - **Parallelizable With**: `add-parameterized-type-nodes`, `add-wildcard-type-nodes`
  - **Estimated Effort**: 1 day
  - **Purpose**: Create PARAMETER_DECLARATION nodes for method and constructor parameters
  - **Current State**: Parser parses parameters correctly but only creates QUALIFIED_NAME nodes for types
  - **Scope**: Create PARAMETER_DECLARATION nodes for each parameter with type and name
  - **Components**:
    - PARAMETER_DECLARATION node with attribute for parameter name
    - Child node for parameter type
    - Support for varargs parameters (`String... args`)
    - Support for final parameters (`final String name`)
  - **Quality**: Parser tests validating PARAMETER_DECLARATION node creation

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
