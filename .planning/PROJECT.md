# Styler - Java Code Formatter

## What This Is

Styler is an unopinionated Java code formatter enabling developers to view and work in their preferred code style
while maintaining team standards on shared repositories. The v1.0 release focuses on a GitHub PR browser extension
as the primary differentiator, with AI agent integration and high-performance CLI as secondary value propositions.

## Core Value

Let perfectionist developers maintain their personal coding standards while working on team projects.

## Requirements

### Validated

<!-- Shipped and confirmed valuable. -->

- ✓ JDK 25 parser with full language support (pattern matching, implicit classes, flexible constructors) — existing
- ✓ Index-Overlay AST with Arena API memory management (3x faster, 96.9% memory efficient) — existing
- ✓ TOML-based configuration system with hierarchical discovery — existing
- ✓ Formatting rules: line length, import organization, brace placement, whitespace, indentation — existing
- ✓ Maven plugin for build integration — existing
- ✓ Virtual thread processing for parallel file handling — existing
- ✓ AI violation output with structured feedback for agent integration — existing
- ✓ File discovery with gitignore support — existing
- ✓ Security framework with resource limits and input validation — existing
- ✓ Source-available commercial license — existing

### Active

<!-- Current scope. Building toward these. -->

**Phase 1: Core Product (Days 1-3)**
- [ ] CLI parallel processing (100+ files/sec via BatchProcessor integration)
- [ ] Parser edge case: array initializers in annotations (`@Annotation({val1, val2})`)
- [ ] Parser edge case: complex switch expression patterns
- [ ] CLI integration tests covering all flags
- [ ] AI context limiting (smart output limiting for agent context windows)
- [ ] Rules summary export (markdown export for AI pre-guidance)
- [ ] Spring Framework 6.2.1 validation (~99% parse success)

**Phase 2: Browser Extension Foundation (Days 4-8)**
- [ ] Line mapping API (bidirectional repo ↔ display mapping during reformatting)
- [ ] Chrome extension: PR detection, GitHub API integration, user config storage
- [ ] Formatter integration for browser (WASM or hosted service — evaluate Days 4-5)
- [ ] DOM replacement with reformatted code and updated line numbers
- [ ] Comment repositioning using line mapping
- [ ] Comment text translation (detect and translate line references)
- [ ] Firefox extension port

**Phase 3: Polish (Days 9-10)**
- [ ] Extension edge cases (dark mode, collapsed diffs, large files)
- [ ] Performance optimization for large PRs
- [ ] Extension store assets and submission materials

**Phase 4: Documentation + Website (Days 11-13)**
- [ ] Installation guide, configuration reference, CLI usage guide
- [ ] Browser extension guide
- [ ] AI integration guide
- [ ] Jekyll/Hugo website with landing, features, comparison pages
- [ ] Pricing page and API documentation

**Phase 5: Marketing + Payment (Days 14-15)**
- [ ] Content marketing (blog posts, demo video, demo GIF)
- [ ] Payment processor setup and license delivery automation
- [ ] Launch post drafts (HN, Reddit, Twitter)
- [ ] Distribution strategy research and experimentation

**Phase 6: Launch (Days 16-18)**
- [ ] GitHub release v1.0.0
- [ ] Browser extension store submissions
- [ ] Public launch across channels

### Out of Scope

<!-- Explicit boundaries. Includes reasoning to prevent re-adding. -->

- VCS format filters (smudge/clean) — Complex, requires AST diff + original-preserving clean; deferred to v1.1
- AST diff algorithm — Required for VCS filters; deferred to v1.1
- Original-preserving clean filter — Required for VCS filters; deferred to v1.1
- JMH benchmarks — Marketing polish, not essential for v1.0; deferred to v1.1
- Concurrency model comparison — Optimization, current model works; deferred to v1.1
- Wildcard import resolution — Enhancement, not core value prop; deferred to v1.1
- Config inference from sample code — Power user feature; deferred to v1.1
- Gradle plugin — Build after Maven plugin proves valuable; deferred to v1.1+
- LSP/IDE integration — Deferred until CLI proves valuable; deferred to v1.1+
- Non-English line reference translation — Edge case, document limitation; deferred to v1.1

## Context

**Target Persona**: Perfectionist solo developers forced into team settings. They have strong personal coding style
preferences honed on solo projects, but work on team projects (employment or open source) with different standards.
They experience friction between "how I like it" and "how the repo wants it."

**Prior Work**: Extensive existing codebase with 10-module Maven structure:
- Phase A (Foundation): COMPLETE — parser, config, security, Arena-based AST
- Phase B (AI Integration): COMPLETE — CLI, formatters, AI violation output
- Phase C (Scale & Testing): In progress — benchmarks, parallel CLI pending

**Competition**: Developers currently suffer through inconsistent code styles. Checkstyle is check-only without
auto-fix. Spotless/google-java-format are opinionated. IDE formatters are IDE-specific and not portable.

**Pricing Model**: Free personal use, $99/dev/year commercial (placeholder, open to adjustment). "Try at home,
buy at work" model. Validation signal: any paid conversion.

**Distribution**: Unknown — needs experimentation during Days 14-15. Candidates: Hacker News Show HN, AI agent
integration angle, viral extension screenshots.

## Constraints

- **Timeline**: 18 days (January 7-24, 2026), 8-10 hours/day
- **Codebase**: Must build on existing Java/Maven implementation
- **Security**: Enterprise-friendly — source code must not be exposed to third parties
- **Extension Runtime**: Evaluate Days 4-5 (WASM vs hosted service with enterprise-grade trust)

## Key Decisions

<!-- Decisions that constrain future work. Add throughout project lifecycle. -->

| Decision | Rationale | Outcome |
|----------|-----------|---------|
| GitHub PR extension as v1.0 differentiator | VCS filters too complex for 18-day timeline; extension delivers view-only value proposition | — Pending |
| Hosted service acceptable if enterprise-trustworthy | Similar to Amazon Bedrock model; ephemeral processing, no code storage | — Pending |
| Extension runtime: evaluate Days 4-5 | Need to spike WASM vs hosted before committing | — Pending |
| $99/dev/year pricing | Gut feel placeholder; adjust based on market feedback | — Pending |

---
*Last updated: 2026-01-08 after initialization*
