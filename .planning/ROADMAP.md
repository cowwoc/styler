# Styler Roadmap

## Overview

Styler is an unopinionated Java code formatter designed for AI agent integration and personal code style
preferences. This roadmap tracks the path from foundation to commercial launch.

**Timeline**: 18 days (January 7-24, 2026) for v1.0 launch.

## Milestones

### Milestone 1: Core Product (COMPLETE)
**Status**: Complete (Releases 1-4)
**Completed**: 2026-01-07

Foundation through parser advanced features, establishing the core product capabilities.

#### Release 1: Foundation (5 tasks) ✅
Core parsing, configuration, and security infrastructure.
- implement-core-parser
- implement-index-overlay-ast
- implement-configuration-system
- implement-security-framework
- implement-file-discovery

#### Release 2: AI Integration (9 tasks) ✅
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

#### Release 3: Parser Core (11 tasks) ✅
Core parser enhancements for JDK 14+ features.
- add-yield-statement-support
- annotation-parsing (element defaults, package annotations, nested values)
- try-catch-enhancements (multi-catch, try-resource variable)
- add-binary-hex-literals
- add-labeled-statement-support
- add-qualified-class-instantiation
- add-explicit-type-arguments
- code-quality-refactoring (expand tokentype acronyms, if-else to switch)
- add-qualified-this-super
- add-local-type-declarations
- add-cast-expressions

#### Release 4: Parser Advanced (16 tasks) ✅
Advanced parser features including JDK 25 support.
- array-parsing-features (dimension annotations, array type method refs)
- add-flexible-constructor-bodies
- module-support (module imports, module-info parsing)
- refactor-parser-depth-limiting
- collapse-import-node-types
- add-compact-source-files
- migrate-parser-tests-to-nodearena
- comment-parsing-fixes (remaining gaps, block comment in members)
- add-compilation-check
- add-primitive-type-patterns
- add-unicode-escape-preprocessing
- apply-brace-omission-style
- fix-array-creation-expression-parsing
- add-anonymous-inner-class-support
- fix-classpath-scanner-per-file-overhead
- add-license-file

---

### Milestone 2: v1.0 Launch (IN PROGRESS)
**Status**: In Progress
**Target**: January 24, 2026

Parser compatibility, CLI polish, browser extension, and commercial launch.

#### Release 5: Parser Compatibility (2 tasks) — Days 1-3
Fix parser edge cases blocking ~99% Spring Framework parse success.
- add-array-initializer-in-annotation-support
- fix-switch-expression-case-parsing

#### Release 6: CLI Polish (4 tasks) — Days 1-3
CLI integration tests and AI enhancements.
- ✅ add-cli-parallel-processing
- ✅ add-cli-integration-tests
- implement-ai-context-limiting
- implement-rules-summary-export

#### Release 7: Browser Extension (4 tasks) — Days 4-8
Line mapping, PR extension, and comment features — the key commercial differentiator.
- implement-line-mapping
- create-github-pr-extension
- implement-comment-repositioning
- implement-comment-text-translation

#### Release 8: Extension Polish (2 tasks) — Days 9-10
Edge cases and performance optimization.
- handle-extension-edge-cases (dark mode, collapsed diffs, large files)
- optimize-large-pr-performance

#### Release 9: Documentation & Website (4 tasks) — Days 11-13
User docs, API docs, and GitHub Pages website.
- create-user-documentation
- create-browser-extension-guide
- create-ai-integration-guide
- create-github-pages

#### Release 10: Marketing & Launch (3 tasks) — Days 14-18
Content marketing, payment setup, and public launch.
- create-marketing-content (blog posts, demo video, demo GIF)
- setup-payment-processing
- execute-public-launch (GitHub release, extension store, HN/Reddit/Twitter)

---

### Milestone 3: v1.1 Enhancements (DEFERRED)
**Status**: Deferred until after v1.0 launch

Performance validation, VCS integration, and additional refinements.

#### Release 11: Scale & Performance (3 tasks)
JMH benchmarks and retrospective action items.
- benchmarking-suite (JMH benchmarks, concurrency models, tool comparison)
- A005-workflow-checkpoint-enforcement (RETRO: protocol_violation prevention)
- A006-parser-test-documentation (RETRO: test_failure prevention)

#### Release 12: VCS Integration (3 tasks)
AST diff and format filters for Git/Mercurial.
- implement-ast-diff
- implement-original-preserving-clean
- implement-vcs-format-filters

#### Release 13: Parser Refinements (2 tasks)
Remaining parser compatibility issues.
- add-semantic-validation
- add-regression-test-suite

#### Release 14: AI Enhancements (1 task)
Config inference from sample code.
- implement-config-inference

---

### Milestone 4: Future Work (PLANNED)
**Status**: Planned for v1.2+

Optional enhancements and IDE plugins.

#### Release 15: Deferred Work (4 tasks)
Wildcard imports, community registry, CI/CD, README update.
- resolve-wildcard-imports
- create-community-config-registry
- setup-github-actions-ci
- update-readme-value-proposition

#### Release 16: IDE Integration (1 task)
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
