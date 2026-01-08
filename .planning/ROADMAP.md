# Styler Roadmap

## Overview

Styler is an unopinionated Java code formatter designed for AI agent integration and personal code style
preferences. This roadmap tracks the path from foundation to commercial launch.

## Milestones

### Milestone 1: Core Product (COMPLETE)
**Status**: Complete (Phases 1-4)
**Completed**: 2026-01-07

Foundation through parser advanced features, establishing the core product capabilities.

#### Phase 1: Foundation (5 tasks) ✅
Core parsing, configuration, and security infrastructure.
- implement-core-parser
- implement-index-overlay-ast
- implement-configuration-system
- implement-security-framework
- implement-file-discovery

#### Phase 2: AI Integration (9 tasks) ✅
CLI, formatters, and AI violation output for agent integration.
- implement-cli-application
- implement-line-length-formatter
- implement-import-organization
- implement-brace-placement-formatter
- implement-whitespace-formatter
- implement-indentation-formatter
- implement-ai-violation-output
- implement-virtual-thread-processing
- create-maven-plugin

#### Phase 3: Parser Core (15 tasks) ✅
Core parser enhancements for JDK 14+ features.
- add-yield-statement-support
- add-annotation-element-defaults
- add-multi-catch-support
- add-binary-hex-literals
- add-labeled-statement-support
- add-try-resource-variable
- add-qualified-class-instantiation
- add-explicit-type-arguments
- add-package-annotations
- expand-tokentype-acronyms
- refactor-if-else-to-switch
- add-qualified-this-super
- add-nested-annotation-values
- add-local-type-declarations
- add-cast-expressions

#### Phase 4: Parser Advanced (19 tasks) ✅
Advanced parser features including JDK 25 support.
- add-array-dimension-annotations
- add-array-type-method-references
- add-flexible-constructor-bodies
- add-module-import-declarations
- refactor-parser-depth-limiting
- collapse-import-node-types
- add-compact-source-files
- migrate-parser-tests-to-nodearena
- fix-remaining-comment-gaps
- add-compilation-check
- add-primitive-type-patterns
- add-unicode-escape-preprocessing
- add-module-info-parsing
- apply-brace-omission-style
- fix-array-creation-expression-parsing
- add-anonymous-inner-class-support
- fix-block-comment-in-member-declaration
- fix-classpath-scanner-per-file-overhead
- add-license-file

---

### Milestone 2: Scale & Quality (IN PROGRESS)
**Status**: Planned
**Target**: Next

Performance validation, testing infrastructure, and CI/CD pipeline.

#### Phase 5: Scale & Performance (4 tasks)
CLI parallel processing and JMH benchmarks.
- add-cli-parallel-processing
- create-jmh-benchmarks
- benchmark-concurrency-models
- benchmark-tool-comparison

#### Phase 6: Testing & Quality (2 tasks)
Regression tests and CLI integration tests.
- add-regression-test-suite
- add-cli-integration-tests

#### Phase 7: CI/CD Pipeline (1 task)
GitHub Actions for automated testing and releases.
- setup-github-actions-ci

---

### Milestone 3: Browser Extension (PLANNED)
**Status**: Planned

GitHub PR browser extension - the key commercial differentiator.

#### Phase 8: Browser Extension (4 tasks)
Line mapping, PR extension, and comment features.
- implement-line-mapping
- create-github-pr-extension
- implement-comment-repositioning
- implement-comment-text-translation

---

### Milestone 4: VCS Integration (PLANNED)
**Status**: Planned

VCS format filters for working in user style while maintaining project standards.

#### Phase 9: VCS Integration (3 tasks)
AST diff and format filters for Git/Mercurial.
- implement-ast-diff
- implement-original-preserving-clean
- implement-vcs-format-filters

---

### Milestone 5: Documentation & Launch (PLANNED)
**Status**: Planned

Documentation, website, and commercial launch preparation.

#### Phase 10: Documentation (3 tasks)
User docs, API docs, and GitHub Pages website.
- create-user-documentation
- create-api-documentation
- create-github-pages

---

### Milestone 6: Refinements (PLANNED)
**Status**: Planned

Parser refinements and AI enhancements.

#### Phase 11: Parser Refinements (3 tasks)
Remaining parser compatibility issues.
- add-array-initializer-in-annotation-support
- fix-switch-expression-case-parsing
- add-semantic-validation

#### Phase 12: AI Enhancements (3 tasks)
AI context optimization and config inference.
- implement-ai-context-limiting
- implement-rules-summary-export
- implement-config-inference

---

### Milestone 7: Deferred & Commercial (PLANNED)
**Status**: Deferred

Optional enhancements and IDE plugins.

#### Phase 13: Deferred Work (3 tasks)
Wildcard imports, community registry, README update.
- resolve-wildcard-imports
- create-community-config-registry
- update-readme-value-proposition

#### Phase 14: Commercial Launch (1 task)
IDE plugins for legacy VCS systems.
- create-virtual-format-plugin

---

## Key Metrics

| Metric | Target | Current |
|--------|--------|---------|
| Parser Test Count | - | 771 tests |
| Total Test Count | - | 1532 tests |
| Parse Success Rate | 100% | ~94% (Spring Framework) |
| File Throughput | 100+ files/sec | ~400 files/sec (Maven plugin) |
| Memory Usage | ≤512MB/1000 files | 351 MB/1000 files |
| Core Scaling | ≥60% at 8 cores | 62.4% |

## Success Criteria

**MVP Complete (Milestone 1)**: ✅
- Parse JDK 25 Java files into AST
- Apply 5 formatting rules
- Generate structured AI violation output
- CLI tool works end-to-end
- Maven plugin for build integration

**Production Ready (Milestone 2)**:
- Parallel processing for large codebases
- Performance benchmarks validate all claims
- Comprehensive test suite
- CI/CD pipeline for automated releases

**Commercial Launch (Milestones 3-5)**:
- Browser extension for GitHub PRs
- VCS format filters
- User documentation and website
- Commercial licensing portal
