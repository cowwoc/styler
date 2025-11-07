---
name: git-commit
description: Guide for writing clear, descriptive commit messages
allowed-tools: Bash, Read
---

# Git Commit Message Skill

**Purpose**: Provide guidance and examples for writing clear, descriptive commit messages that explain WHAT the code does and WHY, not just THAT a change was made.

**When to Use**:
- Creating any git commit
- Especially important for squashed commits (combining multiple changes)
- When commit message needs to summarize complex changes
- As reference when unsure what makes a good commit message

## Core Principles

### 1. Describe WHAT the Code Does, Not the Process

❌ **WRONG - Describes the process**:
```
Squashed commits
Combined multiple commits
Merged feature branch
Updated files
```

✅ **CORRECT - Describes what the code does**:
```
Add user authentication with JWT tokens
Fix memory leak in connection pool
Refactor parser to use visitor pattern
```

### 2. Use Imperative Mood (Command Form)

❌ **WRONG - Past tense or descriptive**:
```
Added authentication
Authentication was added
Adding authentication feature
```

✅ **CORRECT - Imperative mood**:
```
Add user authentication
Fix authentication timeout bug
Refactor authentication logic
```

**Why**: Git itself uses imperative mood ("Merge branch...", "Revert commit..."). Your commits should match this style.

### 3. Structure for Complex Changes

For commits that combine multiple changes (like squashes):

```
Subject line: Brief summary (50-72 chars, imperative mood)

Body paragraph: Explain the overall change and why it's needed.

Changes:
- Bullet point for first major change
- Bullet point for second major change
- Bullet point for third major change

Additional context or rationale if needed.

🤖 Generated with [Claude Code](https://claude.com/claude-code)

Co-Authored-By: Claude <noreply@anthropic.com>
```

## Crafting Commit Messages

### Step 1: Review What Changed

```bash
# For regular commits
git diff --staged --stat

# For squashed commits - review all commits being combined
git log --format="%h %s" <base-commit>..HEAD

# See actual file changes
git diff --stat <base-commit>..HEAD
```

### Step 2: Identify the Purpose

Ask yourself:
- What problem does this solve?
- What feature does this add?
- What behavior does this change?
- How do these changes relate to each other?

### Step 3: Write Subject Line

**Format**: `<verb> <what> [<where/context>]`

**Examples**:
```
Add email validation to user registration
Fix null pointer exception in payment processor
Refactor database connection pooling
Update dependencies to latest versions
Remove deprecated API endpoints
```

**Subject Line Rules**:
- Max 72 characters (50 ideal)
- Imperative mood (Add, Fix, Update, Remove, Refactor)
- No period at end
- Capitalize first word
- Be specific but concise

### Step 4: Write Body (for complex changes)

**When to include a body**:
- Squashed commits (always)
- Non-obvious changes
- Changes affecting multiple areas
- Changes with important context

**Body structure**:
```
Brief overview paragraph explaining the overall change.

Changes:
- First significant change or feature
- Second significant change or feature
- Third significant change or feature
- Etc.

Rationale or additional context if helpful.
```

## Examples by Scenario

### Scenario 1: Single Feature Addition

```
Add rate limiting to API endpoints

Implements rate limiting using token bucket algorithm to prevent
API abuse. Limits requests to 100 per minute per API key.

Changes:
- Add RateLimiter middleware with configurable limits
- Implement token bucket algorithm in rate_limiter.rs
- Add rate limit headers to API responses (X-RateLimit-*)
- Update API documentation with rate limit details

Rate limits are configurable via environment variables.
```

### Scenario 2: Bug Fix

```
Fix race condition in connection pool

Background threads could close connections while still in use,
causing intermittent failures. Added proper locking to prevent
connections from being closed during active use.

Changes:
- Add RwLock around connection state in pool.rs:45
- Ensure acquire() marks connection as in-use before returning
- Add test for concurrent connection access

Fixes #1234
```

### Scenario 3: Refactoring

```
Refactor parser to use visitor pattern

Replaces recursive descent parser with visitor pattern for better
extensibility and separation of concerns. Makes it easier to add
new node types and transformations.

Changes:
- Create Visitor trait and NodeVisitor implementation
- Update AST nodes to accept visitors
- Refactor parsing logic to use visitor pattern
- Update tests to use new visitor-based API

No functional changes - pure refactoring.
```

### Scenario 4: Squashed Commits

**Review commits being squashed**:
```bash
$ git log --oneline base..HEAD
043a992 Fix optimize-doc: Phase 2 must RESUME Phase 1 agent
e724912 Strengthen optimize-doc: use general-purpose agent only
6e449a2 Clarify optimize-doc: main agent optimizes directly
26a99d7 Add iterative optimization to optimize-doc command
e60ed1d Remove unnecessary file storage for agent ID
c0d63f0 Revert "Fix race condition in temp file storage"
5dd2baa Fix race condition in temp file storage
```

**Synthesize into unified message**:
```
Enhance optimize-doc with iterative loop and session ID

Adds multi-pass optimization capability with proper session
management for agent continuity between optimization phases.

Changes:
- Add iterative optimization loop for multi-pass document refinement
- Integrate session ID capture from Phase 1 agent for Phase 2 resume
- Clarify that main agent optimizes directly (not via Task tool)
- Strengthen to use general-purpose agent only for both phases
- Fix Phase 2 to resume Phase 1 agent (not create new agent)
- Remove unnecessary agent ID file storage

These enhancements enable proper multi-pass optimization with agent
continuity between phases while maintaining session context.
```

### Scenario 5: Multiple Unrelated Changes (Avoid!)

❌ **WRONG - Combines unrelated changes**:
```
Fix bug and add feature

- Fix null pointer in parser
- Add new export format
- Update documentation
```

✅ **CORRECT - Separate commits**:
```
Commit 1:
Fix null pointer exception in parser

Commit 2:
Add JSON export format

Commit 3:
Update documentation for export formats
```

**Rule**: One logical change per commit. If you can't describe it without "and", it should probably be multiple commits.

## Anti-Patterns to Avoid

### ❌ Meaningless Messages

```
WIP
Fix stuff
Updates
More changes
asdf
.
```

**Why wrong**: Doesn't explain what changed or why. Makes git history useless.

### ❌ Overly Generic

```
Update code
Fix bugs
Improve performance
Refactor
```

**Why wrong**: Vague. What code? Which bugs? What performance? Which refactoring?

### ❌ Just the Process

```
Squashed commits
Merged feature branch
Combined work
Rebased
```

**Why wrong**: Describes git operation, not what the code does. Future readers don't care HOW the commit was created.

### ❌ Too Technical/Implementation-Focused

```
Change variable name from x to userCount
Move function from line 45 to line 67
Replace for loop with map
```

**Why wrong**: Focuses on mechanical changes, not the purpose. Better: "Clarify variable naming in user module"

### ❌ Novel-Length

```
[10 paragraphs explaining every single line changed...]
```

**Why wrong**: Too much detail. Commit message should summarize - the diff shows the details.

## Tools and Workflow

### Review Before Committing

```bash
# See what you're about to commit
git diff --staged

# See statistics
git diff --staged --stat

# For squash - review all commits
git log --oneline base..HEAD
git log --format="%h %s" base..HEAD
```

### Template Workflow

```bash
# 1. Review changes
git diff --staged --stat

# 2. Identify the theme/purpose
# Ask: What problem does this solve? What feature does this add?

# 3. Draft message using template
cat > /tmp/commit-msg.txt <<'EOF'
<Verb> <what> [<where>]

<Brief paragraph explaining the change and why>

Changes:
- <First major change>
- <Second major change>
- <Third major change>

<Rationale or context>

🤖 Generated with [Claude Code](https://claude.com/claude-code)

Co-Authored-By: Claude <noreply@anthropic.com>
EOF

# 4. Edit the template
$EDITOR /tmp/commit-msg.txt

# 5. Commit with the message
git commit -F /tmp/commit-msg.txt
```

### For Squashed Commits

```bash
# 1. Review all commits being squashed
echo "Commits being squashed:"
git log --format="%h %s" base..HEAD
echo ""

# 2. See overall changes
git diff --stat base..HEAD

# 3. Identify common theme
# Ask: What do these changes accomplish together?

# 4. Synthesize into unified message
# - Subject: What's the combined purpose?
# - Body: List key changes from all commits
# - Rationale: Why do these changes work together?
```

## Commit Message Checklist

Before finalizing a commit message:

- [ ] Subject line is imperative mood ("Add", "Fix", not "Added", "Fixed")
- [ ] Subject line is specific (not "Update files" or "Fix bugs")
- [ ] Subject line is under 72 characters
- [ ] Body explains WHAT and WHY, not HOW
- [ ] For squashed commits: reviewed all source commits
- [ ] For squashed commits: synthesized meaningful summary
- [ ] No generic phrases ("squashed commits", "combined work")
- [ ] Message would make sense to someone reading git history in 6 months
- [ ] Describes the code's behavior, not the commit process

## Quick Reference

### Good Verbs for Subject Lines

- **Add**: New feature, file, function, or capability
- **Fix**: Bug fix or correction
- **Update**: Modify existing feature or dependency (non-breaking)
- **Remove**: Delete feature, file, or code
- **Refactor**: Restructure code without changing behavior
- **Improve**: Enhance existing feature (performance, UX, etc.)
- **Document**: Documentation only
- **Test**: Add or update tests
- **Chore**: Maintenance tasks (deps, build, config)

### Subject Line Formula

```
<Verb> <what> [<where/context>]
  ↓       ↓           ↓
 Add   rate limiting  to API endpoints
 Fix   memory leak    in connection pool
 Refactor  parser     to use visitor pattern
```

### When in Doubt

Ask yourself: "If I read this in git log in 6 months, would I understand what this commit does and why?"

If the answer is no, revise the message.

## Related Documentation

- git-squash skill: References this guide for squashed commit messages
- git-rebase skill: Uses these principles for rebased commits
- CLAUDE.md: Project commit message conventions

## Usage

This is a reference skill - use it when:
- Writing any commit message
- Unsure what makes a good message
- Creating squashed commits
- Reviewing/improving existing messages

**Quick check**: Read your message. Does it explain WHAT the code does? If not, revise.
