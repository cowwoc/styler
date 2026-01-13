# Styler - Java Code Formatter

## Overview

Styler is an unopinionated Java code formatter enabling developers to view and work in their preferred code style while maintaining team standards on shared repositories. The v1.0 release focuses on a GitHub PR browser extension as the primary differentiator, with AI agent integration and high-performance CLI as secondary value propositions.

## Goals

- Let perfectionist developers maintain their personal coding standards while working on team projects
- GitHub PR extension for view-only reformatting (primary differentiator)
- AI agent integration with structured violation output
- High-performance CLI with virtual thread processing

## Requirements

### Validated

- [x] JDK 25 parser with full language support (pattern matching, implicit classes, flexible constructors) - existing
- [x] Index-Overlay AST with Arena API memory management (3x faster, 96.9% memory efficient) - existing
- [x] TOML-based configuration system with hierarchical discovery - existing
- [x] Formatting rules: line length, import organization, brace placement, whitespace, indentation - existing
- [x] Maven plugin for build integration - existing
- [x] Virtual thread processing for parallel file handling - existing
- [x] AI violation output with structured feedback for agent integration - existing
- [x] File discovery with gitignore support - existing
- [x] Security framework with resource limits and input validation - existing
- [x] Source-available commercial license - existing
- [x] CLI application with check/format modes - existing
- [x] Parser edge cases: yield, annotations, try-catch, labeled statements, qualified expressions - existing
- [x] Parser advanced: array parsing, flexible constructors, module support, implicit classes - existing
- [x] CLI parallel processing (BatchProcessor integration) - existing
- [x] CLI integration tests covering all flags - existing

### Active

**v0.5: Parser Compatibility**
- [ ] Parser edge case: array initializers in annotations (`@Annotation({val1, val2})`)
- [ ] Parser edge case: switch expression case patterns
- [ ] Parser edge case: lambda parameter parsing
- [ ] Parser edge case: comments in member declarations
- [ ] Parser edge case: nested annotation types
- [ ] Parser edge case: contextual keywords as identifiers
- [ ] Parser edge case: cast lambda expressions

**v0.6: CLI Polish**
- [ ] AI context limiting (smart output limiting for agent context windows)
- [ ] Rules summary export (markdown export for AI pre-guidance)

**v0.7: Browser Extension Foundation**
- [ ] Line mapping API (bidirectional repo ↔ display mapping during reformatting)
- [ ] Chrome extension: PR detection, GitHub API integration, user config storage
- [ ] Comment repositioning using line mapping
- [ ] Comment text translation (detect and translate line references)

**v0.8: Extension Polish**
- [ ] Extension edge cases (dark mode, collapsed diffs, large files)
- [ ] Performance optimization for large PRs

**v0.9: Documentation + Website**
- [ ] Installation guide, configuration reference, CLI usage guide
- [ ] Browser extension guide
- [ ] AI integration guide
- [ ] Jekyll/Hugo website with landing, features, comparison pages

**v1.0: Marketing + Launch**
- [ ] Content marketing (blog posts, demo video, demo GIF)
- [ ] Payment processor setup and license delivery automation
- [ ] Public launch across channels

### Out of Scope

- VCS format filters (smudge/clean) - Complex, requires AST diff + original-preserving clean; deferred to v3
- AST diff algorithm - Required for VCS filters; deferred to v3
- Original-preserving clean filter - Required for VCS filters; deferred to v3
- JMH benchmarks - Marketing polish, not essential for v1.0; deferred to v2
- Wildcard import resolution - Enhancement, not core value prop; deferred to v6
- Config inference from sample code - Power user feature; deferred to v5
- Gradle plugin - Build after Maven plugin proves valuable; deferred to v6+
- LSP/IDE integration - Deferred until CLI proves valuable; deferred to v7+
- Non-English line reference translation - Edge case, document limitation; deferred to v2

## Constraints

- **Timeline**: 18 days (January 7-24, 2026), 8-10 hours/day
- **Codebase**: Must build on existing Java/Maven implementation
- **Security**: Enterprise-friendly - source code must not be exposed to third parties
- **Extension Runtime**: Evaluate WASM vs hosted service with enterprise-grade trust

## Key Decisions

| Decision | Rationale | Outcome |
|----------|-----------|---------|
| GitHub PR extension as v1.0 differentiator | VCS filters too complex for 18-day timeline; extension delivers view-only value proposition | Pending |
| Hosted service acceptable if enterprise-trustworthy | Similar to Amazon Bedrock model; ephemeral processing, no code storage | Pending |
| $99/dev/year pricing | Gut feel placeholder; adjust based on market feedback | Pending |
| Index-Overlay AST with Arena API | 3x faster parsing, 96.9% memory efficient vs tree-copying | Implemented |
| Virtual thread processing | JDK 21+ feature for high-throughput parallel file processing | Implemented |

---
*Last updated: 2026-01-12 after DOG initialization on existing codebase*
