# Styler Commercial Launch Schedule

> **Duration**: 36 work days (January 7 - February 26, 2026)
> **Availability**: 4 hours/day, weekdays only
> **Workflow**: Claude Code writes all code, user reviews commits

---

## Executive Summary

**Product**: Styler - Java Code Formatter
**Model**: Free personal use, paid commercial licenses
**Go-to-market**: "Try at home, buy at work"

**Key Differentiators (v1.0)**:
1. **GitHub PR extension** - View PRs in your preferred style with smart comment translation
2. **AI-agent integration** - Structured output for AI coding assistants (Copilot, Claude Code, Cursor)
3. **Performance** - 100+ files/second parallel processing

**v1.1 (Post-Launch)**:
4. **VCS format filters** - Work in YOUR preferred format locally, repo maintains team standard

**Primary Message**: "View GitHub PRs in your preferred code style"

---

## Phase 1: Core Product (Days 1-3)

### Day 1: Technical Foundation

**Expected**: Jan 7, 2026 (Wed)

| Task | Description | Expected | Actual | Commit |
|------|-------------|----------|--------|--------|
| `add-cli-parallel-processing` | Integrate BatchProcessor with CLI for 100+ files/sec | Jan 7 | Jan 8 | 571f349 |
| `add-array-initializer-in-annotation-support` | Parse `@Annotation({val1, val2})` syntax | Jan 7 | Jan 8 | 25444a8 (verified existing) |
| `fix-switch-expression-case-parsing` | Handle complex switch expression patterns | Jan 7 | Jan 8 | 3f09c88 |
| LICENSE file | Draft custom source-available commercial license | Jan 7 | Jan 7 | 5e8d92c |
| README update | Add license summary and value proposition | Jan 7 | Jan 7 | 73cfc61 |

**Day 1 Output**: Fast, stable CLI with commercial license in place

---

### Day 2: Integration Testing + AI Features

**Expected**: Jan 8, 2026 (Thu)

| Task | Description | Expected | Actual | Commit |
|------|-------------|----------|--------|--------|
| `add-cli-integration-tests` | End-to-end CLI tests covering all flags | Jan 8 | | |
| `implement-ai-context-limiting` | Smart output limiting for AI agent context windows | Jan 8 | | |
| `implement-rules-summary-export` | Export formatting rules as markdown for AI pre-guidance | Jan 8 | | |

**Day 2 Output**: Tested CLI with AI-friendly features

---

### Day 3: Validation

**Expected**: Jan 9, 2026 (Fri)

| Task | Description | Expected | Actual | Commit |
|------|-------------|----------|--------|--------|
| Spring Framework test | Run against spring-core-6.2.1-sources.jar, verify ~99% parse success | Jan 9 | Jan 6 | e31fb63 |
| Fix any discovered issues | Address parser edge cases found in real-world testing | Jan 9 | Jan 9-13 | multiple |
| ↳ Nested annotation types | Handle `@interface` inside classes | Jan 9 | Jan 10 | 2d24432 |
| ↳ Contextual keywords | Allow `var`, `record` etc. as identifiers | Jan 9 | Jan 11 | 05f3afe |
| ↳ Parser refactoring | Move helpers to internal package | Jan 9 | Jan 12 | 4d8c3cd |
| Performance validation | Verify 100+ files/sec throughput claim | Jan 9 | | |

**Day 3 Output**: Validated against real-world codebase

---

## Phase 2: Browser Extension Foundation (Days 4-8)

### Day 4: Line Mapping API

**Expected**: Jan 12, 2026 (Mon)

| Task | Description | Expected | Actual | Commit |
|------|-------------|----------|--------|--------|
| `implement-line-mapping` | Generate bidirectional line mapping during reformatting | Jan 12 | | |
| LineMapping record | API design: repoToDisplay, displayToRepo mappings | Jan 12 | | |
| FormattingResult update | Return LineMapping alongside formatted code | Jan 12 | | |

**Day 4 Output**: Line mapping infrastructure ready

---

### Day 5: Extension Foundation (Chrome)

**Expected**: Jan 13, 2026 (Tue)

| Task | Description | Expected | Actual | Commit |
|------|-------------|----------|--------|--------|
| Extension manifest | Chrome extension structure, permissions | Jan 13 | | |
| GitHub PR detection | Detect PR diff pages, file views | Jan 13 | | |
| GitHub API integration | Fetch raw file content for reformatting | Jan 13 | | |
| User config storage | Extension settings for user's preferred style | Jan 13 | | |

**Day 5 Output**: Extension detects PRs and fetches content

---

### Day 6: Code Reformatting + Display

**Expected**: Jan 14, 2026 (Wed)

| Task | Description | Expected | Actual | Commit |
|------|-------------|----------|--------|--------|
| Formatter integration | Bundle Styler formatter for browser (WASM or JS) | Jan 14 | | |
| DOM replacement | Replace displayed code with reformatted version | Jan 14 | | |
| Line number update | Update displayed line numbers using mapping | Jan 14 | | |

**Day 6 Output**: PRs display in user's preferred format

---

### Day 7: Comment Repositioning

**Expected**: Jan 15, 2026 (Thu)

| Task | Description | Expected | Actual | Commit |
|------|-------------|----------|--------|--------|
| `implement-comment-repositioning` | Reposition PR comments using line mapping | Jan 15 | | |
| Tooltip integration | "Comment on repo line X" tooltip on hover | Jan 15 | | |
| Multi-line handling | Handle comments on lines that expand/collapse | Jan 15 | | |

**Day 7 Output**: Comments appear on correct display lines

---

### Day 8: Comment Text Translation + Firefox

**Expected**: Jan 16, 2026 (Fri)

| Task | Description | Expected | Actual | Commit |
|------|-------------|----------|--------|--------|
| `implement-comment-text-translation` | Detect and translate line refs in text | Jan 16 | | |
| Bidirectional posting | Translate display -> repo when posting comments | Jan 16 | | |
| Firefox port | Adapt extension for Firefox WebExtensions API | Jan 16 | | |

**Day 8 Output**: Full comment translation, cross-browser support

---

## Phase 3: Polish + Edge Cases (Days 9-10)

### Day 9: Extension Polish

**Expected**: Jan 19, 2026 (Mon)

| Task | Description | Expected | Actual | Commit |
|------|-------------|----------|--------|--------|
| Edge cases | Handle GitHub dark mode, collapsed diffs, large files | Jan 19 | | |
| Performance optimization | Ensure smooth experience on large PRs | Jan 19 | | |
| Visual polish | Consistent styling for translated elements | Jan 19 | | |
| Error handling | Graceful degradation when formatter fails | Jan 19 | | |

**Day 9 Output**: Robust, polished extension

---

### Day 10: Extension Store Preparation

**Expected**: Jan 20, 2026 (Tue)

| Task | Description | Expected | Actual | Commit |
|------|-------------|----------|--------|--------|
| Extension store assets | Icons, descriptions, screenshots | Jan 20 | | |
| Chrome Web Store listing | Prepare submission materials | Jan 20 | | |
| Firefox Add-ons listing | Prepare submission materials | Jan 20 | | |
| User documentation | Extension setup and configuration guide | Jan 20 | | |

**Day 10 Output**: Ready for extension store submission

---

## Phase 4: Documentation + Website (Days 11-13)

### Day 11: User Documentation

**Expected**: Jan 21, 2026 (Wed)

| Task | Description | Expected | Actual | Commit |
|------|-------------|----------|--------|--------|
| Installation guide | JAR download, Maven plugin setup, extension install | Jan 21 | | |
| Configuration reference | Complete `.styler.toml` syntax documentation | Jan 21 | | |
| CLI usage guide | All commands, flags, output formats with examples | Jan 21 | | |
| Browser extension guide | Setup, configuration, feature overview | Jan 21 | | |
| AI integration guide | How to use Styler with Claude Code, Copilot, Cursor | Jan 21 | | |

**Day 11 Output**: Comprehensive user documentation

---

### Day 12: Website

**Expected**: Jan 22, 2026 (Thu)

| Task | Description | Expected | Actual | Commit |
|------|-------------|----------|--------|--------|
| Jekyll/Hugo setup | GitHub Pages static site structure | Jan 22 | | |
| Landing page | Clear value proposition, key features, CTA | Jan 22 | | |
| Features page | Detailed feature descriptions with visuals | Jan 22 | | |
| Comparison page | "Styler vs Checkstyle/Spotless for modern workflows" | Jan 22 | | |
| Getting started page | Quick start guide embedded from docs | Jan 22 | | |

**Day 12 Output**: Professional web presence

---

### Day 13: Pricing + API Docs

**Expected**: Jan 23, 2026 (Fri)

| Task | Description | Expected | Actual | Commit |
|------|-------------|----------|--------|--------|
| Pricing page | License tiers, purchase CTA | Jan 23 | | |
| API documentation | Javadoc for public APIs | Jan 23 | | |
| FAQ page | Common questions about licensing, compatibility | Jan 23 | | |

**Day 13 Output**: Complete website with pricing

---

## Phase 5: Marketing + Payment (Days 14-15)

### Day 14: Content Marketing

**Expected**: Jan 26, 2026 (Mon)

| Task | Description | Expected | Actual | Commit |
|------|-------------|----------|--------|--------|
| Blog post 1 | "View GitHub PRs in Your Preferred Code Style" | Jan 26 | | |
| Blog post 2 | "Why AI Coding Assistants Need Better Formatter Output" | Jan 26 | | |
| Demo video | Screen recording: same PR in different styles, comment translation | Jan 26 | | |
| Demo GIF | Quick visual for landing page and social | Jan 26 | | |

**Day 14 Output**: Launch content ready

---

### Day 15: Payment + Launch Assets

**Expected**: Jan 27, 2026 (Tue)

| Task | Description | Owner | Expected | Actual | Commit |
|------|-------------|-------|----------|--------|--------|
| Payment processor | Set up Gumroad/Stripe account, product listing | User | Jan 27 | | |
| License delivery | Automated license key delivery mechanism | Claude Code | Jan 27 | | |
| Purchase flow test | End-to-end buy -> receive license test | Both | Jan 27 | | |
| HN post draft | Hacker News Show HN post | Claude Code | Jan 27 | | |
| Reddit posts draft | r/java, r/programming announcements | Claude Code | Jan 27 | | |
| Twitter thread draft | Launch announcement thread | Claude Code | Jan 27 | | |

**Day 15 Output**: Can accept payments, launch posts ready

---

## Phase 6: Launch (Days 16-18)

### Day 16: Soft Launch

**Expected**: Jan 28, 2026 (Wed)

| Task | Description | Expected | Actual | Commit |
|------|-------------|----------|--------|--------|
| Create GitHub release | Tag v1.0.0, build artifacts, publish | Jan 28 | | |
| Submit browser extensions | Chrome Web Store, Firefox Add-ons | Jan 28 | | |
| Enable purchases | Activate payment flow | Jan 28 | | |
| Fresh install test | Verify complete flow from new user perspective | Jan 28 | | |

**Day 16 Output**: Product live, extensions submitted, accepting payments

---

### Day 17: Monitor + Buffer

**Expected**: Jan 29, 2026 (Thu)

| Task | Description | Expected | Actual | Commit |
|------|-------------|----------|--------|--------|
| Monitor extension approval | Chrome/Firefox review process | Jan 29 | | |
| Limited announcement | Personal network, select communities | Jan 29 | | |
| Fix any discovered issues | Quick response to problems | Jan 29 | | |
| Prepare public launch | Final review of all materials | Jan 29 | | |

**Day 17 Output**: Ready for public launch

---

### Day 18: Public Launch

**Expected**: Jan 30, 2026 (Thu)

| Task | Description | Expected | Actual | Commit |
|------|-------------|----------|--------|--------|
| Hacker News post | Submit Show HN | Jan 30 | | |
| Reddit posts | Post to r/java, r/programming | Jan 30 | | |
| Twitter announcement | Publish launch thread | Jan 30 | | |
| Engage with feedback | Answer questions, address concerns | Jan 30 | | |
| Hotfix if needed | Rapid response to reported issues | Jan 30 | | |

**Day 18 Output**: Public launch complete

---

## Unplanned Work Log

### Days 3-5 (Jan 9-13): AI Agent Workflow Fixes

**Duration**: 3 work days (unplanned)
**Impact**: Schedule shifted by 3 days

While using Claude Code to build Styler, we encountered significant issues with the AI agent workflow
that required immediate fixes. This wasn't product work, but necessary infrastructure improvements.

**Work completed:**
- 11 learnings recorded (M058-M069) documenting agent failure patterns
- Retrospective R003 analysis and action items
- Validation gate improvements (on-the-fly approach)
- Worktree and lock handling fixes
- Plugin release workflow
- Parser refactoring (internal package organization)
- YouTube video documenting AI agent workflow problems and fixes (Jan 12)

**Parser fixes completed:**
- `2d24432` - Nested `@interface` annotation type declarations
- `05f3afe` - Contextual keywords as identifiers (e.g., `var`, `record` as variable names)
- `4d8c3cd` - Parser helpers moved to internal package

**Spring Framework validation results (Jan 13):**
- Success rate: 93.2% (8,219/8,817 files parsed successfully)
- 598 parsing errors discovered across 7 bug categories
- New tasks created: fix-cast-lambda-in-method-args (344), fix-method-reference-parsing (54),
  fix-switch-default-case-parsing (40), fix-contextual-keyword-method-call (19),
  fix-else-if-chain-parsing (18), fix-lambda-arrow-edge-cases (16), fix-comment-identifier-context (13)

**Adjusted timeline**: Original 18-day schedule now becomes 21-day schedule.
New target launch: Feb 4, 2026 (was Jan 30, 2026)

---

## Status Summary

| Phase | Days | Tasks | Completed | Remaining |
|-------|------|-------|-----------|-----------|
| Phase 1: Core Product | 1-3 | 11 | 7 | 4 |
| Unplanned: AI Workflow | 3-5 | - | - | - |
| Phase 2: Browser Extension | 6-10 | 15 | 0 | 15 |
| Phase 3: Polish | 11-12 | 8 | 0 | 8 |
| Phase 4: Docs + Website | 13-15 | 13 | 0 | 13 |
| Phase 5: Marketing + Payment | 16-17 | 10 | 0 | 10 |
| Phase 6: Launch | 18-21 | 13 | 0 | 13 |
| **Total** | **21** | **70** | **7** | **63** |

---

## Pricing Strategy

| Tier | Price | Target | Features |
|------|-------|--------|----------|
| Personal | Free | Individual developers at home | Full CLI + extension |
| Professional | $99/dev/year | Small teams, startups | Commercial use license |
| Team | $79/dev/year (5+ devs) | Growing teams | Volume discount |
| Enterprise | Contact sales | Large organizations | Custom terms, support |

---

## Risk Register

| Risk | Probability | Impact | Mitigation |
|------|-------------|--------|------------|
| Parser bugs in production | Medium | High | Day 3 real-world validation, buffer days |
| Browser extension store rejection | Low | Medium | Follow store guidelines, have direct download fallback |
| Payment integration issues | Low | Medium | Use established platform (Gumroad), test early |
| Line mapping edge cases | Medium | Low | Document limitations, tooltip shows original line |
| Low launch traction | Medium | Medium | Multiple channels (HN, Reddit, Twitter), content marketing |

---

## Deferred to Post-Launch (v1.1+)

| Feature | Reason | Estimated Effort |
|---------|--------|------------------|
| **VCS format filters** | Requires AST diff + original-preserving clean (complex) | 10-15 days |
| `implement-ast-diff` | Semantic diff ignoring whitespace | 3-4 days |
| `implement-original-preserving-clean` | Only reformat changed lines | 3-4 days |
| `implement-vcs-format-filters` | Git/Mercurial smudge/clean integration | 4-5 days |
| JMH benchmarks | Marketing polish, not essential for v1.0 | 3-4 days |
| Concurrency model comparison | Optimization, current model works | 2-3 days |
| Wildcard import resolution | Enhancement, not core value prop | 2-3 days |
| Config inference | Power user feature | 3-4 days |
| AST-based comment anchoring | Line mapping sufficient for v1.0 | 3-4 days |
| Non-English line reference translation | Edge case, document limitation | 1-2 days |
