# TODO List - Styler Java Code Formatter (Main Branch)

## 🚀 READY TO WORK NOW (Multi-Instance Coordination)

**Current Status**: Phase C in progress - `create-maven-plugin` complete, `create-jmh-benchmarks` and `benchmark-concurrency-models` now unblocked

**Phase B**: ✅ COMPLETE (8/8 tasks)
**Phase C**: In progress (4/6 tasks - benchmarks now unblocked)

**Phase A**: ✅ COMPLETE (5/5 tasks)

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

**Task Prioritization**:
- Bug fixes MUST be prioritized before new features
- Exception: New features that replace the buggy feature may take precedence

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

---

## Phase C: Scale & Real-World Testing

**Goal**: Scale to large codebases with parallel processing, build to 5 formatting rules for realistic
benchmarking, and validate with Maven plugin integration.

### Performance Benchmarking
- [ ] **BLOCKED:** `create-jmh-benchmarks` - Validate performance claims with JMH benchmarks
  - **Dependencies**: `implement-pipeline-stages` ✅, `create-maven-plugin` ✅, `implement-virtual-thread-processing` ✅, all formatters ✅
  - **Blocked By**: All Phase E parser enhancement tasks (RealWorldProjectBenchmark fails on real codebases)
  - **Blocks**: `add-regression-test-suite`, `setup-github-actions-ci`
  - **Parallelizable With**: `benchmark-concurrency-models`
  - **Estimated Effort**: 3-4 days
  - **Purpose**: Measure and validate parsing throughput, memory usage, scalability
  - **Scope**: JMH benchmark suite covering all scope.md performance targets
  - **Prior Work** (on branch `create-jmh-benchmarks`, 2 commits):
    - JMH benchmark module implemented with 7 benchmark classes
    - Memory validated: 351 MB per 1000 files ✅ (target: ≤512 MB)
    - CoreScalingBenchmark validated: 62.4% efficiency at 8 cores ✅
    - RealWorldProjectBenchmark: configured to fail fast on parse errors
  - **Decisions Made**:
    - Scalability target: ≥60% efficiency at 8 cores (was: linear scaling to 32 cores)
      - WSL2/Docker adds ~10-20% virtualization overhead
      - 8 cores is realistic target for typical dev machines
    - RealWorldProjectBenchmark throws IllegalStateException on parse failures
  - **Benchmarks**:
    - ParsingThroughputBenchmark: ≥10,000 tokens/sec
    - MemoryUsageBenchmark: ≤512MB per 1000 files ✅ VALIDATED
    - FormattingThroughputBenchmark: ≥100 files/sec
    - CoreScalingBenchmark: ≥60% efficiency at 8 cores ✅ VALIDATED
    - VirtualThreadComparisonBenchmark: Virtual vs platform threads
    - RealWorldProjectBenchmark: Spring, Guava, JUnit5 ❌ BLOCKED
  - **Configuration**: Fork=3, proper warmup/measurement, 95% confidence intervals
  - **Integration**: Separate benchmark module, uses production code
  - **Quality**: Statistical rigor, comprehensive coverage
  - **Resume**: After parser issues fixed, merge branch and run RealWorldProjectBenchmark

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

### Tool Performance Comparison
- [ ] **BLOCKED:** `benchmark-tool-comparison` - Compare Styler performance vs Checkstyle/PMD
  - **Dependencies**: `create-jmh-benchmarks` (reuses benchmark infrastructure)
  - **Blocks**: None (competitive analysis)
  - **Parallelizable With**: None
  - **Estimated Effort**: 2-3 days
  - **Purpose**: Validate Styler is competitive with established tools
  - **Scope**: Benchmark equivalent rule checks across tools
  - **Comparisons**:
    - Line length checking: Styler vs Checkstyle LineLength
    - Import ordering: Styler vs Checkstyle ImportOrder
    - Brace placement: Styler vs Checkstyle LeftCurly/RightCurly
  - **Metrics**: Throughput (files/sec), memory usage, startup time
  - **Integration**: Reuses BenchmarkResourceManager, SampleCodeGenerator from create-jmh-benchmarks
  - **Quality**: Fair comparison with equivalent configurations

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

### Parser Enhancement: Systematic Comment Handling
- [ ] **READY:** `fix-remaining-comment-gaps` - Handle comments in all remaining parser locations
  - **Dependencies**: `fix-enum-constant-comments` ✅
  - **Blocks**: None (enhancement for edge cases)
  - **Parallelizable With**: Any Phase E task
  - **Estimated Effort**: 2-3 days
  - **Purpose**: Systematically add parseComments() calls to all locations where comments can appear
  - **Known Gaps** (from analysis):
    - Method/constructor parameters (between params)
    - Block statements (after opening brace, before closing brace)
    - Control flow statements (if/else, for, while, switch, try/catch)
    - Array initializers (between elements)
    - Lambda expressions (between arrow and body)
    - Type parameters/arguments (between generic type params)
  - **Priority**: Lower than self-hosting blockers - these are edge cases
  - **Quality**: Parser tests for comment placement in each identified location

### AST Simplification: Collapse Import Node Types
- [ ] **READY:** `collapse-import-node-types` - Merge STATIC_IMPORT_DECLARATION into IMPORT_DECLARATION
  - **Dependencies**: None
  - **Blocks**: None (simplification for cleaner model)
  - **Parallelizable With**: Any Phase E task
  - **Estimated Effort**: 1 day
  - **Purpose**: Simplify AST model - "static" is a modifier, not a different construct
  - **Proposed Change**:
    - Add `isStatic()` to `ImportAttribute`
    - Parser creates `IMPORT_DECLARATION` for both, setting attribute
    - Remove `STATIC_IMPORT_DECLARATION` from `NodeType` enum
    - Update `ImportExtractor` to use single `findNodesByType()` call
  - **Benefits**:
    - Single query returns all imports in position order (no sorting needed)
    - Cleaner semantic model (static is a modifier, like public/private)
    - Simpler consumer code
  - **Quality**: Update all usages, verify import organization still works

### Parser Enhancement: Node-Based Complexity Limiting
- [ ] **READY:** `refactor-parser-depth-limiting` - Replace recursion depth limit with node count limit
  - **Dependencies**: None
  - **Blocks**: None (enhancement for robustness)
  - **Parallelizable With**: Any Phase E task
  - **Estimated Effort**: 1-2 days
  - **Purpose**: Make parser complexity limiting implementation-independent
  - **Current Problem**: `MAX_PARSE_DEPTH` limits recursion depth, which depends on JVM stack size and
    parser bytecode layout - fragile and unpredictable
  - **Proposed Solution**:
    - Primary limit: Node count (checked in `NodeArena.allocateNode()`)
    - Backup limit: Keep recursion depth as safety net with generous value (500+)
  - **Benefits**:
    - Node count is implementation-independent (works with recursive or iterative parser)
    - Predictable behavior regardless of parser refactoring
    - "Max 100,000 nodes" is easier to reason about than "max 200 recursions"
  - **Implementation**:
    - Add `maxNodes` parameter to `NodeArena` or `SecurityConfig`
    - Check in `allocateNode()`: throw `ParseException` if exceeded
    - Keep `MAX_PARSE_DEPTH` as backup with higher value
  - **Quality**: Tests for both limits, verify no StackOverflowError possible

### Parser Enhancement: Missing Node Types

*All previously listed tasks (add-wildcard-type-nodes, add-parameterized-type-nodes, add-parameter-declaration-nodes) have been completed.*

### Parser Enhancement: Missing Core Java Features

**Priority**: These are basic Java features that should already be supported. Required for correct parsing.


- [ ] **READY:** `add-module-info-parsing` - Parse module-info.java module declarations
  - **Dependencies**: None
  - **Blocks**: None (required for JPMS module support)
  - **Parallelizable With**: Any Phase E parser task
  - **Estimated Effort**: 2-3 days
  - **Purpose**: Parse module-info.java files with module declarations (JDK 9+)
  - **Current Gap**: Parser only handles compilation units with package/import/type declarations
  - **Syntax**: `module name { requires/exports/opens/uses/provides directives }`
  - **Example**:
    ```java
    module com.example.app {
        requires java.base;
        requires transitive java.sql;
        exports com.example.api;
        opens com.example.internal to framework;
        uses com.example.spi.Service;
        provides com.example.spi.Service with com.example.impl.ServiceImpl;
    }
    ```
  - **Implementation**:
    - Add MODULE token type and related directive tokens
    - Add `MODULE_DECLARATION`, `REQUIRES_DIRECTIVE`, `EXPORTS_DIRECTIVE`, etc. to NodeType
    - Detect module-info.java files or `module` keyword at compilation unit level
    - Parse module name, then directives until closing brace
  - **Quality**: Parser tests for all directive types, qualified names, modifiers

- [ ] **READY:** `add-try-resource-variable` - Parse try-with-resources with existing variables (JDK 9+)
  - **Dependencies**: None
  - **Blocks**: None (required for correct try-with-resources parsing)
  - **Parallelizable With**: Any Phase E parser task
  - **Estimated Effort**: 0.5 days
  - **Purpose**: Parse effectively-final variable references in try-with-resources (JDK 9+)
  - **Current Gap**: `parseResource()` requires full declaration (Type name = expr), not just identifier
  - **Syntax**: `try (existingVar)` or `try (existingVar; anotherVar)`
  - **Example**:
    ```java
    BufferedReader br = new BufferedReader(new FileReader(file));
    try (br) {  // Just variable reference, no declaration
        return br.readLine();
    }
    ```
  - **Implementation**:
    - In `parseResource()`, first try parsing as simple identifier (or qualified name)
    - If next token is SEMICOLON or RPAREN, it's a variable reference (done)
    - Otherwise backtrack and parse as full declaration (current behavior)
  - **Quality**: Parser tests for single variable, multiple variables, mixed declaration/reference

- [ ] **READY:** `add-package-annotations` - Parse package-info.java with package annotations
  - **Dependencies**: None
  - **Blocks**: None (required for complete package-info.java support)
  - **Parallelizable With**: Any Phase E parser task
  - **Estimated Effort**: 0.5 days
  - **Purpose**: Parse annotations before package declaration in package-info.java files
  - **Current Gap**: `parseCompilationUnit()` checks for PACKAGE before handling annotations
  - **Syntax**: Annotations directly before `package` keyword
  - **Example**:
    ```java
    @Nullable
    @SuppressWarnings("all")
    package com.example.api;
    ```
  - **Implementation**:
    - In `parseCompilationUnit()`, before checking for PACKAGE, consume any annotations
    - Store package annotations as part of package declaration node
    - Handle case where annotations exist but no package (error or implicit package)
  - **Quality**: Parser tests for single/multiple annotations, annotation with values


- [ ] **READY:** `add-qualified-this-super` - Parse qualified this/super expressions
  - **Dependencies**: None
  - **Blocks**: None (required for inner class references)
  - **Parallelizable With**: Any Phase E parser task
  - **Estimated Effort**: 0.5 days
  - **Purpose**: Parse `Outer.this` and `Outer.super` expressions for inner classes
  - **Current Gap**: `parsePostfix()` after DOT only checks for IDENTIFIER or CLASS, not THIS/SUPER
  - **Syntax**: `QualifiedType.this` or `QualifiedType.super`
  - **Example**:
    ```java
    class Outer {
        class Inner {
            void method() {
                Outer.this.field = 1;      // Reference outer instance
                Outer.super.toString();    // Call outer's super method
            }
        }
    }
    ```
  - **Implementation**:
    - In `parsePostfix()`, after DOT, also check for THIS and SUPER tokens
    - If found, create appropriate qualified this/super expression node
    - May reuse THIS_EXPRESSION/SUPER_EXPRESSION or add QUALIFIED_THIS/QUALIFIED_SUPER
  - **Quality**: Parser tests for qualified this, qualified super, method calls on them

- [ ] **READY:** `add-explicit-type-arguments` - Parse explicit type arguments on method/constructor/reference calls
  - **Dependencies**: None
  - **Blocks**: None (required for generic method invocation)
  - **Parallelizable With**: Any Phase E parser task
  - **Estimated Effort**: 0.5-1 day
  - **Purpose**: Parse explicit type arguments like `obj.<String>method()`, `new <T>Foo()`, and `List::<String>of`
  - **Current Gap**:
    - `parsePostfix()` after DOT expects IDENTIFIER, not LT for type arguments
    - `parsePostfix()` after DOUBLE_COLON expects IDENTIFIER or NEW, not LT for type arguments
  - **Syntax**:
    - Method invocation: `receiver.<TypeArgs>methodName(args)`
    - Constructor: `new <TypeArgs>ClassName(args)`
    - Method reference: `Type::<TypeArgs>methodName` or `Type::<TypeArgs>new`
  - **Example**:
    ```java
    Collections.<String>emptyList();
    this.<T>genericMethod();
    obj.<String, Integer>method();
    new <String>GenericConstructor();
    List::<String>of;                    // method reference with type args
    ArrayList::<String>new;              // constructor reference with type args
    ```
  - **Implementation**:
    - In `parsePostfix()`, after DOT, check for LT before IDENTIFIER
    - In `parsePostfix()`, after DOUBLE_COLON, check for LT before IDENTIFIER or NEW
    - If LT found, parse type arguments, then expect IDENTIFIER or NEW
    - In `parseNewExpression()`, check for LT before type name
  - **Quality**: Parser tests for method invocations, constructors, method references with type args

- [ ] **READY:** `add-array-dimension-annotations` - Parse type annotations on array dimensions (JDK 8+)
  - **Dependencies**: None
  - **Blocks**: None (obscure JSR 308 feature)
  - **Parallelizable With**: Any Phase E parser task
  - **Estimated Effort**: 0.5 days
  - **Purpose**: Parse annotations between type name and array brackets (JSR 308, JDK 8+)
  - **Current Gap**: `parseType()` parses `[]` but doesn't check for `@` before each `[`
  - **Syntax**: `Type @Annotation []` or `@A Type @B [] @C []`
  - **Example**:
    ```java
    String @NonNull [] names;                    // annotation on array type
    @NonNull String @Nullable [] @ReadOnly [] matrix;  // per-dimension annotations
    ```
  - **Implementation**:
    - In `parseType()`, before each `match(LBRACKET)`, check for and consume annotations
    - Annotations apply to that specific array dimension
    - Handle multiple dimensions with different annotations
  - **Priority**: Lower - obscure feature, rarely used in practice
  - **Quality**: Parser tests for single/multiple dimensions with annotations

- [ ] **READY:** `add-qualified-class-instantiation` - Parse `outer.new Inner()` syntax
  - **Dependencies**: None
  - **Blocks**: None (required for inner class instantiation)
  - **Parallelizable With**: Any Phase E parser task
  - **Estimated Effort**: 0.5 days
  - **Purpose**: Parse qualified class instance creation for inner classes
  - **Current Gap**: `parsePostfix()` after DOT only handles IDENTIFIER/CLASS, not NEW
  - **Syntax**: `expression.new InnerClass(args)` or `expression.new <T>InnerClass(args)`
  - **Example**:
    ```java
    Outer outer = new Outer();
    Outer.Inner inner = outer.new Inner();           // qualified instantiation
    outer.new Inner().method();                       // chained
    getOuter().new Inner();                           // expression qualifier
    ```
  - **Implementation**:
    - In `parsePostfix()`, after DOT, check for NEW token
    - If found, parse inner class instantiation (type, optional type args, arguments)
    - Create appropriate OBJECT_CREATION node with qualifier info
  - **Quality**: Parser tests for simple/chained/expression qualifiers

- [ ] **READY:** `add-array-type-method-references` - Parse array type constructor references (Type[]::new)
  - **Dependencies**: None
  - **Blocks**: None (required for correct method reference parsing)
  - **Parallelizable With**: Any Phase E parser task
  - **Estimated Effort**: 0.5 days
  - **Purpose**: Parse array constructor references like `int[]::new` and `String[][]::new`
  - **Current Gap**: `parseArrayAccessOrClassLiteral()` after empty brackets `[]` always expects `.class`
    (line 2243: `expect(TokenType.DOT)`), not `::new` for array constructor references
  - **Syntax**: `PrimitiveOrReferenceType[]...[]::new`
  - **Example**:
    ```java
    IntFunction<int[]> f = int[]::new;
    Function<Integer, String[]> g = String[]::new;
    BiFunction<Integer, Integer, int[][]> h = int[][]::new;
    ```
  - **Implementation**:
    - In `parseArrayAccessOrClassLiteral()`, after consuming empty brackets, check for either:
      - DOT CLASS → class literal (current behavior)
      - DOUBLE_COLON NEW → array constructor reference (new case)
    - For constructor reference, create METHOD_REFERENCE node with array type as qualifier
    - May need to refactor to return control to `parsePostfix()` for the `::new` handling
  - **Quality**: Parser tests for primitive arrays, reference arrays, multi-dimensional arrays

- [x] **DONE:** `add-nested-annotation-values` - Parse nested annotations in annotation element values
  - **Dependencies**: None
  - **Blocks**: None (required for correct annotation parsing)
  - **Parallelizable With**: Any Phase E parser task
  - **Estimated Effort**: 0.5 days
  - **Purpose**: Parse nested annotations like `@Foo(@Bar)` and `@Foo(value = @Bar(1))`
  - **Current Gap**: `parsePrimary()` doesn't handle AT token - throws "Unexpected token in expression: AT"
    when encountering `@` inside annotation element values
  - **Syntax**: `@AnnotationName(elementValue)` where elementValue can be another annotation
  - **Example**:
    ```java
    @Target({ElementType.FIELD, ElementType.METHOD})    // Array of enum constants - works
    @Repeatable(@FooContainer)                          // Nested annotation - FAILS
    @interface Foo { }

    @JsonProperty(@JsonAlias("name"))                   // Nested annotation - FAILS
    private String field;
    ```
  - **Implementation**:
    - In `parsePrimary()`, add case for AT token
    - When AT encountered in expression context, call `parseAnnotation()` as a value
    - Return appropriate node (reuse ANNOTATION or create ANNOTATION_VALUE node)
  - **Quality**: Parser tests for single nested annotation, annotation in array, deeply nested

- [ ] **READY:** `add-unicode-escape-preprocessing` - Handle Unicode escapes outside string literals
  - **Dependencies**: None
  - **Blocks**: None (required for JLS compliance)
  - **Parallelizable With**: Any Phase E parser task
  - **Estimated Effort**: 1-2 days
  - **Purpose**: Handle Unicode escapes (`\uXXXX`) anywhere in Java source, not just strings
  - **Current Gap**: Lexer only processes Unicode escapes inside string/char literals via
    `consumeEscapeSequence()`. Code like `int \u0041 = 1;` (valid Java for `int A = 1;`) fails to parse.
  - **JLS Reference**: §3.3 - Unicode escapes are processed as a translation step BEFORE lexical analysis
  - **Syntax**: `\uXXXX` where XXXX is 4 hex digits, can appear in identifiers, keywords, operators
  - **Example**:
    ```java
    int \u0041 = 1;           // Same as: int A = 1;
    \u0070ublic class Test {} // Same as: public class Test {}
    String s \u003D "hi";     // Same as: String s = "hi";
    ```
  - **Implementation**:
    - Option A: Preprocess entire source to expand Unicode escapes before lexing
      - Simpler but loses original source representation
    - Option B: Handle in lexer by checking for `\u` pattern during identifier/keyword scanning
      - More complex but preserves original text for formatting output
    - For a formatter, Option B is preferred to preserve source fidelity
    - Need to handle `\uuuuXXXX` (multiple u's allowed per JLS)
  - **Priority**: Lower - rarely used outside strings in practice, but required for full JLS compliance
  - **Quality**: Lexer tests for Unicode escapes in identifiers, keywords, operators

### Parser Enhancement: JDK 25 Language Features

**Priority**: These are REQUIRED for "100% JDK 25 support" claim in scope.md. Should be completed before
production release.

- [ ] **READY:** `add-module-import-declarations` - Support JEP 511 module import syntax
  - **Dependencies**: None
  - **Blocks**: None (enhancement for full JDK 25 support)
  - **Parallelizable With**: Any Phase E parser task
  - **Estimated Effort**: 1-2 days
  - **Purpose**: Parse `import module java.base;` declarations (JEP 511 - finalized in JDK 25)
  - **Syntax**:
    - `import module java.base;` - imports all public types exported by the module
    - Can appear alongside regular and static imports
  - **Implementation**:
    - Add `MODULE_IMPORT_DECLARATION` to `NodeType` enum
    - Extend `parseImportDeclaration()` to check for `module` keyword after `import`
    - Add `ModuleImportAttribute` with module name
    - Update `ImportExtractor` to handle module imports
  - **Quality**: Parser tests for module import declarations, integration with import organizer

- [ ] **READY:** `add-flexible-constructor-bodies` - Support JEP 513 statements before super()/this()
  - **Dependencies**: None
  - **Blocks**: None (enhancement for full JDK 25 support)
  - **Parallelizable With**: Any Phase E parser task
  - **Estimated Effort**: 1-2 days
  - **Purpose**: Allow statements before `super()` or `this()` calls (JEP 513 - finalized in JDK 25)
  - **Current Limitation**: Parser requires `super()` or `this()` as first statement in constructor
  - **JDK 25 Change**: Constructors may now contain statements before explicit constructor invocation
  - **Example**:
    ```java
    public Child(int value) {
        if (value < 0) throw new IllegalArgumentException("value must be non-negative");
        super(validate(value));  // Statements allowed before this!
    }
    ```
  - **Implementation**:
    - Modify `parseConstructorBody()` to allow arbitrary statements before super()/this()
    - Remove early validation that enforces super()/this() as first statement
    - Track whether explicit constructor invocation has been encountered
  - **Quality**: Parser tests for statements before super(), before this(), and error cases

- [ ] **READY:** `add-compact-source-files` - Support JEP 512 implicit classes and instance main
  - **Dependencies**: None
  - **Blocks**: None (enhancement for full JDK 25 support)
  - **Parallelizable With**: Any Phase E parser task
  - **Estimated Effort**: 2-3 days
  - **Purpose**: Parse implicit/unnamed classes and instance main methods (JEP 512 - finalized in JDK 25)
  - **Features to Support**:
    - Files without explicit class declaration (implicit class)
    - Instance main method: `void main()` or `void main(String[] args)` (not static)
    - Top-level fields, methods without enclosing class
  - **Example**:
    ```java
    // No class declaration needed - this is a complete Java file
    void main() {
        System.out.println("Hello, World!");
    }
    ```
  - **Implementation**:
    - Add `IMPLICIT_CLASS_DECLARATION` to `NodeType` enum
    - Modify `parseCompilationUnit()` to detect absence of class/interface declaration
    - When no type declaration found, wrap top-level members in implicit class node
    - Handle instance main method signature detection
  - **Scope Limitation**: Focus on parsing correctness; formatting of implicit classes secondary
  - **Quality**: Parser tests for implicit classes, instance main, mixed with imports/package

- [ ] **BLOCKED:** `add-primitive-type-patterns` - Support JEP 507 primitive patterns (preview)
  - **Dependencies**: None
  - **Blocks**: None (preview feature, lower priority)
  - **Parallelizable With**: Any Phase E parser task
  - **Estimated Effort**: 1-2 days
  - **Purpose**: Parse primitive types in pattern matching (JEP 507 - third preview in JDK 25)
  - **Status**: BLOCKED - Preview feature, defer until finalized or user demand
  - **Features When Implemented**:
    - `case int i when i > 0 ->` in switch expressions
    - `obj instanceof int i` for primitive patterns
    - Primitive type patterns in record patterns
  - **Note**: Current parser may already handle these as type patterns work; needs verification

### Parser Enhancement: String Templates (REMOVED - Do Not Implement)

**Note**: String Templates (JEP 430, 459, 465) were preview features in JDK 21-23 but were **REMOVED** from
JDK 25. Do NOT implement string template parsing. The architecture.md reference to "string templates" is
outdated and should be corrected.

### Semantic Validation

**Priority**: Enhancement for better error detection. Parser currently handles syntax only; semantic
validation would catch context-sensitive errors.

- [ ] **READY:** `add-semantic-validation` - Add semantic analysis pass for ALL context-sensitive validation
  - **Dependencies**: None
  - **Blocks**: None (enhancement for better error messages)
  - **Parallelizable With**: Any Phase E task
  - **Estimated Effort**: 5-7 days
  - **Purpose**: Comprehensive semantic validation for ALL node types requiring context-sensitive checks
  - **Scope**: Validate ALL semantic constraints the parser intentionally defers (not just control flow)
  - **Examples of Constraints** (non-exhaustive - full analysis required):
    - Control flow: `yield` in switch, `break/continue` in loops, `return` in methods
    - Labels: `break label;` where label exists and is reachable
    - Modifiers: `static` not allowed on local classes, `abstract` only on classes/methods
    - Annotations: `@Override` on overriding methods, target type validation
    - Expressions: `this` not in static context, `super` in appropriate contexts
    - Declarations: duplicate variable names in scope, unreachable code detection
  - **Design Approach**:
    - Create `SemanticValidator` class as separate AST visitor pass
    - Run after parsing, before formatting
    - Collect ALL semantic errors without stopping at first error
    - Return `SemanticValidationResult` with complete list of semantic errors
  - **Implementation**:
    - Add `SemanticError` record (location, message, severity, error code)
    - Add `SemanticValidator` visitor that walks entire AST
    - Track context stack (scope, enclosing type, method context, loop depth, etc.)
    - Systematic validation for each NodeType that has semantic constraints
  - **Quality**: Tests for each semantic constraint category, integration with parse pipeline

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
