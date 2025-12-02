---
name: retrospective
description: Analyze accumulated mistakes, identify recurring patterns, and generate action items for systemic fixes
allowed-tools: Read, Edit, Bash, Grep, Glob
---

# Retrospective Skill

Analyzes accumulated mistakes, identifies recurring patterns, checks action item effectiveness, and
generates new action items for systemic fixes.

## When This Skill Activates

- Automatically triggered by SessionStart hook (`check-retrospective-due.sh`) when:
  - `trigger_interval_days` (default 14) have passed since last retrospective, OR
  - `mistake_count_threshold` (default 10) mistakes accumulated since last retrospective
- Can be manually invoked anytime

## Data Files

| File | Purpose |
|------|---------|
| `.claude/retrospectives/mistakes.json` | All logged mistakes with pattern linkage |
| `.claude/retrospectives/retrospectives.json` | Retrospective history, patterns, action items |

## Workflow

### Phase 1: Data Collection

1. Read all mistakes from `.claude/retrospectives/mistakes.json`
2. Read retrospective history from `.claude/retrospectives/retrospectives.json`
3. Filter to mistakes since last retrospective date
4. Identify mistakes not yet processed (`processed_in_retrospective` is null)

```bash
# Get last retrospective date
LAST_RETRO=$(jq -r '.last_retrospective // "1970-01-01"' .claude/retrospectives/retrospectives.json)

# Count unprocessed mistakes
jq --arg date "$LAST_RETRO" '[.mistakes[] | select(.timestamp > $date)] | length' \
  .claude/retrospectives/mistakes.json
```

### Phase 2: Pattern Analysis

For each mistake since last retrospective:

1. **Categorize by type** (from schema):
   - `tdd_violation` - Skipped test phases
   - `detection_gap` - Validation missed issue
   - `bash_error` - Shell command failure
   - `edit_failure` - String not found
   - `architecture_issue` - Design-level problems
   - `protocol_violation` - Skipped required steps
   - `git_operation_failure` - Git command issues
   - `build_failure` - Compilation/style errors
   - `worktree_violation` - Wrong working directory
   - `giving_up` - Abandoned optimal solution
   - `documentation_violation` - Created prohibited docs
   - `logical_error` - Incorrect logic/thresholds
   - `other` - Uncategorized

2. **Extract patterns**:
   - Group by category
   - Identify common keywords in `pattern_keywords`
   - Find same root cause patterns

```bash
# Group mistakes by category
jq '.mistakes | group_by(.category) | map({category: .[0].category, count: length})' \
  .claude/retrospectives/mistakes.json
```

### Phase 3: Effectiveness Analysis (CRITICAL)

For each COMPLETED action item, check if the fix was effective:

1. **Query mistakes with matching pattern_id WHERE timestamp > completed_date**

```bash
# Check effectiveness of action item A001
jq --arg pattern "PATTERN-003" --arg completed "2025-11-28" '
  [.mistakes[] | select(.pattern_id == $pattern and .timestamp > $completed)] | length
' .claude/retrospectives/mistakes.json
```

2. **Determine effectiveness status**:

| Post-Fix Mistakes | Status | Action |
|-------------------|--------|--------|
| 0 | `effective` | None - fix is working |
| 1 | `partially_effective` | Monitor closely |
| 2+ | `ineffective` | ESCALATE - needs deeper fix |

3. **Update action item effectiveness** in retrospectives.json

4. **Generate escalation** for ineffective fixes:
   - Create new action item with higher priority
   - Reference the ineffective action item
   - Propose deeper architectural change

### Phase 4: Cross-Retrospective Analysis

Compare current patterns with ALL previous retrospectives:

1. Load `recurring_patterns` from retrospectives.json
2. For each current pattern:
   - Check if it matches any previous pattern (by category + keywords)
   - If match: increment `occurrences_total`
   - If occurrences >= `recurrence_threshold` (default 2): mark as RECURRING

3. For RECURRING patterns:
   - Check `occurrences_after_fix` (mistakes after last action date)
   - If `occurrences_after_fix` >= `recurrence_after_fix_threshold`: escalate

### Phase 5: Action Item Generation

For each RECURRING pattern (occurs 2+ times):

1. **Analyze root cause type**:
   - Knowledge gap? → Add to CLAUDE.md
   - Missing check? → Create new hook
   - Design flaw? → Propose architecture change
   - Tooling issue? → Propose tool improvement

2. **Generate action item**:

```json
{
  "id": "A00X",
  "priority": "high|medium|low",
  "description": "What needs to be done",
  "category": "mistake category",
  "pattern_id": "PATTERN-XXX",
  "status": "open",
  "created_date": "ISO timestamp",
  "completed_date": null,
  "related_mistakes": ["M001", "M002"],
  "effectiveness_check": {
    "mistakes_before": N,
    "mistakes_after": null,
    "post_fix_mistakes": [],
    "verdict": "pending"
  }
}
```

### Phase 6: Report & Update

1. **Display summary**:

```
================================================================================
📊 RETROSPECTIVE REPORT: YYYY-MM-DD
================================================================================

Period: [last_retrospective] to [now]
Mistakes analyzed: N
New patterns: N
Recurring patterns: N

## Action Item Effectiveness

| ID | Pattern | Status | Post-Fix Mistakes |
|----|---------|--------|-------------------|
| A001 | build_failure | effective | 0 |
| A002 | detection_gap | partially_effective | 1 |

## Mistake Breakdown by Category

| Category | Count | % |
|----------|-------|---|
| detection_gap | 7 | 33% |
| git_operation_failure | 4 | 19% |
...

## Recurring Patterns (REQUIRES ACTION)

### PATTERN-003: build_failure
- Occurrences: 3
- Last action: A001 (2025-11-28)
- Status: addressed

## New Action Items

- [ ] A00X: [description] (priority: high)

================================================================================
```

2. **Update data files**:

```bash
# Update retrospectives.json
jq '.last_retrospective = "TIMESTAMP" | .mistake_count_since_last = 0' \
  .claude/retrospectives/retrospectives.json > tmp && mv tmp .claude/retrospectives/retrospectives.json

# Mark mistakes as processed
jq '.mistakes |= map(if .processed_in_retrospective == null then .processed_in_retrospective = "R00X" else . end)' \
  .claude/retrospectives/mistakes.json > tmp && mv tmp .claude/retrospectives/mistakes.json
```

## Effectiveness Tracking Algorithm

```
function checkActionItemEffectiveness(actionItem, mistakes):
  if actionItem.status != "implemented":
    return  // Only check completed items

  completedDate = parseDate(actionItem.completed_date)
  patternId = actionItem.pattern_id

  // Find mistakes matching this pattern AFTER fix was implemented
  postFixMistakes = mistakes.filter(m =>
    m.pattern_id == patternId AND
    parseDate(m.timestamp) > completedDate
  )

  // Determine effectiveness
  if postFixMistakes.length == 0:
    verdict = "effective"
  else if postFixMistakes.length == 1:
    verdict = "partially_effective"
  else:
    verdict = "ineffective"
    // Generate escalation action item
    createEscalation(actionItem, postFixMistakes)

  // Update action item
  actionItem.effectiveness_check.post_fix_mistakes = postFixMistakes.map(m => m.id)
  actionItem.effectiveness_check.verdict = verdict
```

## Integration with learn-from-mistakes

When `learn-from-mistakes` logs a new mistake:

1. Check if mistake matches any existing pattern (by category + keywords)
2. If match found, set `pattern_id` on the mistake
3. Increment `mistake_count_since_last` in retrospectives.json
4. If pattern has a completed action item:
   - Add mistake ID to action item's `post_fix_mistakes`
   - If `post_fix_mistakes.length >= 2`, flag for escalation

This creates a feedback loop:
```
Mistake → Pattern Match → Action Item Check → Effectiveness Update → Escalation if needed
```

## Example Invocation

```
User: Run a retrospective

Claude: I'll run a retrospective analysis.

[Phase 1: Reading mistake and retrospective files...]
[Phase 2: Analyzing 5 new mistakes...]
[Phase 3: Checking effectiveness of 4 action items...]
[Phase 4: Cross-referencing with 3 known patterns...]
[Phase 5: Generating action items for recurring patterns...]

================================================================================
📊 RETROSPECTIVE REPORT: 2025-11-28
================================================================================

Period: 2025-11-14 to 2025-11-28
Mistakes analyzed: 5
Recurring patterns requiring action: 1

## Action Item Effectiveness

| ID | Description | Status |
|----|-------------|--------|
| A001 | Pre-commit checkstyle/PMD | pending_verification (just implemented) |
| A002 | TDD enforcement | effective (0 violations since) |

## New Action Items

None - all patterns have active preventions.

================================================================================

The retrospective is complete. All patterns have active prevention mechanisms.
```

## Related Skills

- **learn-from-mistakes**: Logs individual mistakes with root cause analysis
- **get-history**: Access conversation logs for mistake investigation
- **get-session-id**: Provides session ID for log correlation

## Notes

- **NEVER delete mistakes** - Full history required for trend analysis
- Only reset `mistake_count_since_last` after retrospective (trigger counter)
- Pattern recurrence is checked using timestamps, not counts
- Retrospective reports are NOT saved as files (per CLAUDE.md policy) - data is in JSON
