# TODO List - Styler Java Code Formatter (Main Branch)

## 🚀 READY TO WORK NOW (Multi-Instance Coordination)

**Current Status**: Phase C in progress - `create-maven-plugin` complete, `create-jmh-benchmarks` and `benchmark-concurrency-models` now unblocked. Brace omission style applied codebase-wide (2026-01-05).

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
  - **Real-World Impact**: Spring Framework 6.2.1 test (2026-01-06) showed ~400 warnings:
    - `Cannot expand wildcard imports: unresolved symbols [...]`
    - Most files in org.springframework.* use wildcard imports for java.lang.annotation.*
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

### Configuration Inference from Sample Code
- [ ] **READY:** `implement-config-inference` - Generate config file from user-formatted sample code
  - **Dependencies**: `implement-configuration-system` ✅, all formatters ✅
  - **Blocks**: `create-community-config-registry`
  - **Parallelizable With**: Any Phase B/C task
  - **Estimated Effort**: 3-4 days
  - **Purpose**: Let users provide sample code in their desired format, automatically generate matching config
  - **CLI Usage**: `styler infer-config --from src/Example.java --output .styler.toml`
  - **Inference Targets**:
    - **Line length**: Detect max line length in samples (with confidence threshold)
    - **Brace placement**: Same-line vs next-line for classes, methods, control flow
    - **Indentation**: Tabs vs spaces, indent size (2/4/8)
    - **Import organization**: Grouping order (java/javax/third-party/project), blank line separators
    - **Whitespace**: Around operators, after keywords, inside parentheses
  - **Design Considerations**:
    - Multiple sample files for higher confidence
    - Conflict resolution: majority vote, or flag ambiguous patterns for user decision
    - Confidence scores: "high confidence" vs "inferred with uncertainty"
    - Interactive mode: prompt user when patterns are ambiguous
  - **Output**: Generated config file with comments explaining inferred values
  - **Example Output**:
    ```toml
    # Inferred from: src/Example.java, src/Main.java
    # Confidence: high (consistent patterns across 2 files)

    [line-length]
    max = 120  # Longest line observed: 118 chars

    [braces]
    style = "next-line"  # 47/47 braces on next line

    [indentation]
    use-tabs = false
    size = 4  # Consistent 4-space indentation
    ```
  - **Quality**: Tests with various coding styles, edge cases for mixed/inconsistent samples

### Community Config Registry (DefinitelyStyled)
- [ ] **BLOCKED:** `create-community-config-registry` - Peer repository for community-contributed style configs
  - **Dependencies**: `implement-config-inference` (fallback when community config missing)
  - **Blocks**: None (enables network effects)
  - **Parallelizable With**: Package distribution tasks
  - **Estimated Effort**: 3-4 days
  - **Purpose**: Allow users to contribute config overrides when auto-inference fails or is incorrect
  - **Concept**: Like DefinitelyTyped for TypeScript - community maintains configs for projects that don't use Styler natively
  - **Repository Structure**:
    ```
    definitely-styled/
    ├── configs/
    │   ├── apache/
    │   │   ├── commons-lang.toml
    │   │   └── commons-io.toml
    │   ├── google/
    │   │   └── guava.toml
    │   └── spring-projects/
    │       └── spring-framework.toml
    ├── README.md
    └── CONTRIBUTING.md
    ```
  - **Config Resolution Priority** (highest to lowest):
    1. **Local config**: `.styler.toml` in project root
    2. **Community config**: From definitely-styled registry (matched by git remote)
    3. **Auto-inferred**: Generated by `implement-config-inference`
  - **CLI Integration**:
    ```bash
    $ styler init
    Detected: apache/commons-lang (via git remote)

    Config sources:
      ✗ Local config: not found
      ✓ Community config: @definitely-styled/apache/commons-lang
        Contributed by: @user123
        Last validated: 2025-12-01
      ○ Auto-inference: available as fallback

    Using community config. Save locally? [Y/n]
    ```
  - **Contribution Workflow**:
    1. User runs `styler infer-config` on target project
    2. User validates/corrects the generated config
    3. User submits PR to definitely-styled repo
    4. CI validates config against sample files from target project
  - **Network Effect**: More contributors → More projects covered → More users → More contributors
  - **Deliverables**:
    - Separate GitHub repo (definitely-styled or styler-configs)
    - Registry lookup in `styler init`
    - Config download/caching mechanism
    - Contribution guidelines and CI validation

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

### CLI Parallel Processing
- [ ] **READY:** `add-cli-parallel-processing` - Use BatchProcessor for multi-file CLI operations
  - **Dependencies**: `implement-virtual-thread-processing` ✅, BatchProcessor ✅, `fix-classpath-scanner-per-file-overhead` ✅
  - **Blocks**: `create-jmh-benchmarks` (throughput benchmarks require parallel CLI)
  - **Parallelizable With**: Any Phase C/D task
  - **Estimated Effort**: 1-2 days
  - **Purpose**: Achieve 100+ files/sec throughput target by using parallel processing in CLI
  - **Problem**: CLI currently processes files sequentially in a loop (27 files/sec measured)
  - **Solution**: Use existing `BatchProcessor` with virtual threads for multi-file operations
  - **Scope**:
    - Modify `CliMain.processFiles()` to use `BatchProcessor` instead of sequential loop
    - Add `--parallel` flag (default: true) to enable/disable parallel processing
    - Add `--max-concurrency` flag to limit concurrent file processing
    - Integrate progress callback for `--verbose` mode
  - **Expected Improvement**: 27 files/sec → 100+ files/sec (3.7x improvement)
  - **Quality**: Verify throughput meets scope.md targets, add CLI integration tests

### Performance Benchmarking
- [ ] **READY:** `create-jmh-benchmarks` - Validate performance claims with JMH benchmarks
  - **Dependencies**: `implement-pipeline-stages` ✅, `create-maven-plugin` ✅, `implement-virtual-thread-processing` ✅, all formatters ✅, `add-cli-parallel-processing`
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
  - **Parser Status** (2026-01-06): Parser handles most real-world code:
    - Spring Framework 6.2.1: 784 files, ~50 parse errors (~94% success)
    - Parse errors from unsupported syntax: anonymous inner classes, array initializers in annotations
    - See "Parser Enhancement: Real-World Compatibility" section for fix tasks
  - **Maven Plugin Performance** (2026-01-06):
    - Spring Core sources: 784 files in 1.97s Maven time (4s wall clock with JVM startup)
    - Throughput: ~400 files/second processing, ~190 files/second including startup
    - CPU utilization: 335-342% (effective parallel processing)
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
    - RealWorldProjectBenchmark: Spring, Guava, JUnit5 ✅ READY
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

### Parser Enhancement: Missing Node Types

*All previously listed tasks (add-wildcard-type-nodes, add-parameterized-type-nodes, add-parameter-declaration-nodes) have been completed.*

### Parser Enhancement: Real-World Compatibility (Spring Framework)

**Priority**: These issues were discovered when running `styler:check` on Spring Framework 6.2.1 sources
(784 files). Parse failures prevent processing ~50 files. Fix these for real-world codebase compatibility.

**Source**: `spring-core-6.2.1-sources.jar` test run (2026-01-06)

- [ ] **READY:** `fix-static-import-identifier-parsing` - Handle static import member references
  - **Dependencies**: None
  - **Blocks**: Real-world codebase processing
  - **Estimated Effort**: 1 day
  - **Purpose**: Parse code using statically imported identifiers without qualification
  - **Error Example**: `Expected DOT but found IDENTIFIER`
  - **Affected Files**: ~10 files in Spring's cglib/beans, core packages
  - **Scope**: Parser expects qualified name but finds unqualified identifier from static import
  - **Quality**: Test with Spring Framework BulkBean, ClassesKey patterns

- [ ] **READY:** `add-array-initializer-in-annotation-support` - Parse array initializers in annotations
  - **Dependencies**: None
  - **Blocks**: Real-world codebase processing
  - **Estimated Effort**: 1 day
  - **Purpose**: Parse `@Annotation({value1, value2})` syntax
  - **Error Example**: `Expected RIGHT_BRACKET but found INTEGER_LITERAL`
  - **Affected Files**: LocalVariablesSorter.java, ObjectUtils.java
  - **Scope**: Extend annotation parser to handle array element values
  - **Quality**: Test with ASM-style annotations

- [ ] **READY:** `fix-switch-expression-case-parsing` - Handle complex switch expression patterns
  - **Dependencies**: None
  - **Blocks**: Real-world codebase processing
  - **Estimated Effort**: 1-2 days
  - **Purpose**: Parse switch expressions with complex case patterns
  - **Error Examples**:
    - `Unexpected token in expression: CASE`
    - `Unexpected token in expression: ELSE`
  - **Affected Files**: CodeEmitter.java, ConcurrentReferenceHashMap.java
  - **Scope**: Ensure switch expression parser handles all JDK 21+ patterns
  - **Quality**: Test with Spring Framework switch patterns

### Parser Enhancement: JDK 25 Language Features

**Priority**: These are REQUIRED for "100% JDK 25 support" claim in scope.md. Should be completed before
production release.

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

## IDE & Integration Ecosystem

### VCS Format Filters (Primary Solution)
- [ ] **BLOCKED:** `implement-vcs-format-filters` - Auto-format on checkout (user style) and commit (project style)
  - **Dependencies**: Core formatting rules complete, bidirectional formatting capability
  - **Blocks**: None (power user feature)
  - **Estimated Effort**: 4-5 days
  - **Purpose**: Let users work in their preferred style locally while maintaining project standards in repo
  - **Licensing**: Included in base offering (free personal, paid commercial) - drives adoption → commercial sales
  - **Supported VCS**: Git, Mercurial (covers ~98% of users)
  - **How It Works**:
    ```
    checkout:      project style → user's style (smudge/decode filter)
    local editing: user works in their preferred format
    commit:        user's style → project style (clean/encode filter)
    repo:          always contains project standard format
    ```
  - **Git Configuration**:
    ```gitattributes
    *.java filter=styler diff=styler merge=styler
    ```
    ```gitconfig
    [filter "styler"]
        smudge = styler filter --smudge
        clean = styler filter --clean
    [diff "styler"]
        textconv = styler filter --smudge
    [merge "styler"]
        name = Styler-aware merge
        driver = styler merge-driver %O %A %B %L %P
    ```
  - **Mercurial Configuration**:
    ```ini
    # .hg/hgrc
    [encode]
    *.java = pipe: styler filter --clean
    [decode]
    *.java = pipe: styler filter --smudge
    [merge-tools]
    styler.executable = styler
    styler.args = merge-driver $base $local $other $output
    ```
  - **Merge Tool Wrapper** (preserves user's preferred merge tool):
    ```bash
    # Wraps any merge tool (Beyond Compare, IntelliJ, meld, etc.)
    styler mergetool-wrap --tool=bcomp "$BASE" "$LOCAL" "$REMOTE" "$MERGED"

    # Workflow:
    # 1. Smudge base/local/remote to temp files (user style)
    # 2. Invoke user's merge tool with temp files
    # 3. Clean merged result back to project style
    # 4. Write to actual $MERGED
    ```
  - **User Setup**:
    ```bash
    # One-time setup
    styler vcs-hooks install

    # Creates ~/.config/styler/user-style.toml (user's preference)
    # Reads .styler.toml from repo (project standard)
    ```
  - **Key Features**:
    - **Smudge/decode filter**: On checkout, reformat to user's personal style
    - **Clean/encode filter**: On commit, reformat to project standard
    - **textconv**: `git diff` shows user-style diff
    - **Merge driver**: Conflict markers in user's style
    - **Merge tool wrapper**: Preserves user's preferred merge tool
    - **IDE agnostic**: Works with any editor (Vim, Emacs, Notepad++)
  - **Deliverables**: Filter commands, merge driver, mergetool wrapper, setup command, user config format

### Virtual Formatting IDE Plugin (Fallback for Limited VCS)
- [ ] **BLOCKED:** `create-virtual-format-plugin` - IDE plugin for VCS systems without filter support
  - **Dependencies**: Core formatting rules complete, `implement-vcs-format-filters`
  - **Blocks**: None
  - **Priority**: LOW - Only needed for SVN/Perforce/TFVC users (~2% of market)
  - **Estimated Effort**: 5-7 days per IDE (IntelliJ, VS Code)
  - **Purpose**: Provide "work in my style" capability for users on VCS systems that lack smudge/clean filter support
  - **When to Use This**:
    - User is on SVN, Perforce, or TFVC (no filter support)
    - User cannot modify VCS configuration (corporate restrictions)
    - `implement-vcs-format-filters` covers Git/Mercurial users (preferred solution)
  - **How It Works**:
    ```
    File on disk:     public void foo() { ... }     (project style, unchanged)
    User sees in IDE: public void foo()             (user's preferred style)
                      { ... }
    On save:          Translates back to project style
    ```
  - **Key Features**:
    - **Virtual display**: IDE shows reformatted view, file unchanged on disk
    - **Bidirectional editing**: User edits in their style, plugin translates on save
    - **Per-user config**: Each developer has their own viewing preferences
  - **Technical Approach**:
    - IntelliJ: Custom `DocumentListener` + virtual document layer
    - VS Code: `TextDocumentContentProvider` for virtual documents
  - **Target VCS** (lack filter support):
    - SVN: No smudge/clean equivalent
    - Perforce: Server-side triggers only, no client filters
    - TFVC: Very limited hook support
  - **Deliverables**: IntelliJ plugin, VS Code extension

---

## Administrative Tasks

- [ ] **READY:** `create-github-pages` - Create project website with docs and commercial licensing info
  - **Dependencies**: `add-license-file` (need license terms finalized first)
  - **Blocks**: None (but should complete before public release)
  - **Estimated Effort**: 2-3 days
  - **Purpose**: Public-facing documentation and commercial licensing portal
  - **Pages Required**:
    - **Overview**: Brief project description, key features, AI-agent integration focus
    - **Getting Started**: Installation, basic CLI usage, configuration quickstart
    - **Custom Rules Guide**: How to build and integrate custom formatting rules with examples
    - **Pricing/Purchase**: Commercial license tiers, purchase flow, contact for enterprise
  - **Technical Approach**:
    - Use Jekyll (GitHub Pages native) or Hugo/Docusaurus
    - Host at `https://<org>.github.io/styler/` or custom domain
    - Integrate with existing user documentation from `create-user-documentation` task
  - **Deliverables**: `docs/` folder or `gh-pages` branch with static site

- [ ] **READY:** `add-license-file` - Add source-available license for commercial use requirements
  - **Dependencies**: None
  - **Blocks**: `create-github-pages` (license terms needed for pricing page)
  - **Estimated Effort**: 0.5-1 day
  - **Purpose**: Establish licensing terms that allow free personal use but require payment for commercial use
  - **License Requirements**:
    - Free for personal use, distribution, and extension
    - Commercial users must pay for a license
    - Forks used commercially must still pay the original author (not the fork maintainer)
    - "Source available" but NOT open-source
    - **NO automatic open-source conversion** (rules out BSL)
  - **License Options to Evaluate**:
    - **Functional Source License (FSL)**: Similar to BSL but with 2-year conversion - still has conversion, likely ruled out
    - **Elastic License 2.0**: No time-based conversion, prevents competitive SaaS offerings, allows most commercial use
    - **PolyForm Noncommercial / Small Business**: Explicit non-commercial or small business carve-outs
    - **Custom Proprietary License**: Full control, no conversion requirement, requires legal review
  - **Recommendation**: Custom proprietary license with source-available terms. Template approach:
    - Grant: View, modify, use for personal/non-commercial purposes
    - Restriction: Commercial use requires paid license from original author
    - Fork clause: Commercial use of derivatives also requires license from original author
    - No sunset/conversion clause
  - **Action**: Draft custom license or adapt Elastic License 2.0, then legal review
  - **Deliverables**: LICENSE file, README update with license summary

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
