# Critical Rules & Safety Protocols

This document contains all critical enforcement rules, build integrity requirements, and multi-instance safety protocols for the Styler Java Code Formatter project.

## 🚨 ABSOLUTE TOP PRIORITY: BUILD INTEGRITY

🚨 **CRITICAL REQUIREMENTS**:
- **BUILD INTEGRITY**: Main branch MUST always compile and pass ALL tests (see [Build Integrity Requirements](#build-integrity-requirements))
- **TASK CONTINUATION**: Work systematically through ALL tasks without stopping
- **WORKTREE ISOLATION**: ALL code changes in `{task-name}/code/` directories
- **MULTI-INSTANCE SAFETY**: Coordinate with other Claude instances using ownership markers
- **GIT SAFETY**: NEVER delete worktrees, branches, or task directories

## 🚨 WORKFLOW ENFORCEMENT MESSAGES

**VIOLATION CONSEQUENCE**: 🚨 **WORKFLOW VIOLATION = IMMEDIATE TASK RESTART REQUIRED**

**Hook Integration**: Startup hooks automatically enforce workflow compliance and load todo.md.
**Session Coordination**: Session ID tracking ensures proper multi-instance coordination.

## 🚨🚨🚨 CRITICAL DATA LOSS PREVENTION 🚨🚨🚨

### **ABSOLUTE PROHIBITION: WORKTREE CLEANUP BEFORE VERIFIED MERGE**
**🚨 NEVER CLEAN UP WORKTREES BEFORE MERGE VERIFICATION PASSES 🚨**

- **NEVER remove worktrees with `git worktree remove` until merge is verified successful**
- **NEVER delete task branches until merge verification confirms commits in main**
- **NEVER clean up ownership markers until merge verification passes**
- **MANDATORY**: Use workflow Stage 9 verification before any cleanup operations
- **IF UNABLE TO REBASE/MERGE OR VERIFICATION FAILS: STOP IMMEDIATELY - DO NOT CLEAN UP**

### **DATA LOSS PREVENTION CHECKLIST**
Before ANY cleanup operations, verify:
1. ✅ **Work successfully merged to main branch**
2. ✅ **All commits from task branch present in main**
3. ✅ **Build validation passes on main with merged changes**
4. ✅ **No pending task exists in task worktree**

### **EMERGENCY PROCEDURES**
If rebase/merge fails:
- ✅ **STOP all cleanup operations immediately**
- ✅ **Preserve worktree and task branch**
- ✅ **Document merge conflict details**
- ✅ **Request assistance before proceeding**

### **DESTRUCTIVE GIT OPERATIONS SAFETY PROTOCOL**
**🚨 MANDATORY BACKUP PROCEDURES FOR ALL DESTRUCTIVE GIT OPERATIONS 🚨**

**DESTRUCTIVE OPERATIONS** (require backup):
- `git rebase` (interactive or non-interactive)
- `git reset --hard`
- `git cherry-pick` with conflicts
- `git merge --squash`
- `git branch -D` (force delete)
- `git push --force`

**USER OVERRIDE PROTOCOL**:
When user explicitly confirms acceptance of risks proceed with destructive operation following mandatory backup procedures

**MANDATORY BACKUP PROCEDURE**:
1. **BEFORE OPERATION**: Create backup branch: `git branch backup-$(date +%s)-$(git rev-parse --short HEAD)`
2. **DOCUMENT STATE**: Record current HEAD: `git log --oneline -5` 
3. **EXECUTE OPERATION**: Perform the destructive git operation
4. **VERIFY INTEGRITY**: Compare before/after state using backup
5. **VALIDATE RESULT**: Ensure no data loss occurred
6. **REMOVE BACKUP**: Only after verification passes: `git branch -D backup-*`

**VERIFICATION COMMANDS**:
- `git diff backup-TIMESTAMP..HEAD` (should show intended changes only)
- `git log backup-TIMESTAMP..HEAD --oneline` (verify commit history)
- `git log HEAD..backup-TIMESTAMP --oneline` (should be empty if no loss)
- Build validation: `./mvnw verify` must pass

### **COMMIT PRESERVATION DURING REBASE**
**🚨 ABSOLUTE PROHIBITION: DROPPING COMMITS DURING REBASE 🚨**

- **BEFORE rebase**: Use `git log --oneline` to identify ALL commits that contain task changes
- **MANDATORY**: Create backup branch: `git branch backup-rebase-$(date +%s)`
- **VERIFICATION**: After rebase, compare commit count and content with backup
- **RECOVERY**: If commits are dropped, use `git reflog` or backup branch to restore them
- **COMMIT ANALYSIS**: Use `git show --name-only COMMIT` to understand what each commit contains
- **SELECTIVE APPROACH**: Prefer `git cherry-pick` over complex interactive rebases
- **VALIDATION**: After rebase, verify all files and functionality are preserved

**REBASE SAFETY CHECKLIST**:
1. ✅ **Backup branch created before rebase**
2. ✅ **All important commits identified and catalogued**
3. ✅ **Post-rebase verification shows no dropped commits**
4. ✅ **Content comparison confirms no data loss**
5. ✅ **Build passes after rebase with all original functionality**
6. ✅ **Git history is cleaner but complete**
7. ✅ **Backup branch removed only after full verification**

## 🚨 CRITICAL ENFORCEMENT RULES

### Build Integrity Requirements
- Main branch MUST always compile without errors or warnings
- ALL unit tests MUST pass before any commit
- Integration tests MUST pass for parser and formatter modules
- Code coverage requirements MUST be maintained
- No compilation warnings are acceptable in production code

### Task Execution Standards
- Work systematically through ALL tasks without stopping mid-implementation
- Complete full feature implementation before moving to next task
- Validate all changes with comprehensive testing
- Document any architectural decisions or trade-offs made

### Code Quality Gates
- All code changes MUST pass static analysis checks
- Parser operations MUST include comprehensive unit tests
- Formatter rule modules require specialized validation
- Error handling MUST follow fail-fast principles

### Documentation Synchronization 🔴 CRITICAL
**RULE**: Human and Claude documentation files MUST maintain synchronized rule titles.
- **Claude files**: `docs/code-style/*-claude.md` (detection patterns: common, java, typescript)
- **Human files**: `docs/code-style/*-human.md` (explanations: common, java, typescript)
- **Verification Command**: Automated via Claude hook system
- **Automated Check**: Pre-tool-use hook validates title consistency
- **VIOLATION CONSEQUENCE**: Documentation updates that break title synchronization MUST be fixed before merge

## 🚨 MULTI-INSTANCE COORDINATION & SAFETY RULES

### Worktree Isolation Protocol
- ALL code changes MUST occur in `{task-name}/code/` directories
- NEVER make changes directly in main branch or workspace root
- Each task gets its own isolated worktree branch
- Coordinate with other Claude instances using ownership markers

### 🚨 CRITICAL: MANDATORY PRE-WORK SAFETY CHECKLIST
**BEFORE running ANY stakeholder agents or implementation work:**

1. ✅ **Task ownership lock file created in `../../../locks/{task-name}.json`**
2. ✅ **Isolated worktree exists at `{task-name}/`**
3. ✅ **Currently operating from within the task worktree**
4. ✅ **Verified no other instance is working on same task**

**🚨 VIOLATION CONSEQUENCE**: If ANY stakeholder agent is launched before completing this checklist, IMMEDIATELY STOP and establish proper isolation before continuing.

**MANDATORY VERIFICATION BEFORE STATE 2 (REQUIREMENTS)**:
```bash
# IMPORTANT: Replace {TASK_NAME} with actual task name before executing
# CRITICAL: Execute these commands BEFORE invoking ANY stakeholder agents
pwd | grep -q "/workspace/branches/{TASK_NAME}/code$" || {
  echo "ERROR: Not in task worktree! Currently in: $(pwd)"
  echo "REQUIRED: cd /workspace/branches/{TASK_NAME}/code"
  exit 1
}
echo "✅ Worktree verification PASSED: $(pwd)"
```

**Example for task "refactor-line-wrapping-architecture"**:
```bash
pwd | grep -q "/workspace/branches/refactor-line-wrapping-architecture/code$" || {
  echo "ERROR: Not in task worktree! Currently in: $(pwd)"
  echo "REQUIRED: cd /workspace/branches/refactor-line-wrapping-architecture/code"
  exit 1
}
echo "✅ Worktree verification PASSED: $(pwd)"
```

**STATE 0 (INIT) COMPLETION REQUIREMENTS**:
After creating worktree, you MUST:
1. Execute `cd /workspace/branches/{TASK_NAME}/code` (replace {TASK_NAME} with actual task name)
2. Verify with `pwd` that output shows task directory
3. Only then mark State 0 as completed
4. Only then proceed to State 1

**CONFIGURATION ENFORCEMENT**: This safety checklist prevents data loss, merge conflicts, and coordination failures between multiple Claude instances.

### Multi-Instance Safety Measures
- Check for existing ownership markers before starting task
- Create ownership markers when beginning task execution
- Respect other instances' active task areas
- Use proper merge protocols when integrating changes

### Lock File Format and Ownership

**Location**: `/workspace/locks/{task-name}.json`

**Required Fields**:
```json
{
  "session_id": "unique-session-identifier",
  "task_name": "task-name-matching-filename",
  "state": "current-protocol-phase",
  "created_at": "ISO-8601-timestamp"
}
```

**Field Descriptions**:
- `session_id`: Unique identifier for the Claude session owning this task (CRITICAL for ownership verification)
- `task_name`: Task identifier matching the lock filename
- `state`: Current protocol phase (INIT, CLASSIFIED, REQUIREMENTS, SYNTHESIS, IMPLEMENTATION, VALIDATION, REVIEW, SCOPE_NEGOTIATION, COMPLETE, CLEANUP)
- `created_at`: ISO 8601 timestamp when lock was created

**Ownership Rules**:
1. **NEVER delete or modify a lock file unless `session_id` matches YOUR session ID**
2. **ALWAYS verify lock ownership before ANY lock operation (read state, update, delete)**
3. **ALWAYS update `state` field when transitioning between protocol phases**
4. **NEVER assume task ownership based on worktree existence alone - ONLY lock file ownership matters**

**Example Lock File**:
```json
{
  "session_id": "f450ff60-c235-4928-a7f2-b30f4413788b",
  "task_name": "create-maven-plugin",
  "state": "IMPLEMENTATION",
  "created_at": "2025-10-05T00:30:00Z"
}
```

**Post-Compaction Recovery**:
After context compaction, the ONLY way to determine task ownership is by checking lock files:
1. Scan `/workspace/locks/*.json` for files containing YOUR `session_id`
2. If found: Read `state` field and resume from that phase
3. If not found: No active task for this session - select new task from todo.md
4. NEVER assume ownership from worktree existence, uncommitted changes, or stakeholder reports

### Git Safety Standards
- NEVER delete worktrees, branches, or task directories
- Always verify branch status before making changes
- Use proper merge strategies to maintain history
- Backup critical changes before major refactoring

### Coordination Protocols
- Check `todo.md` for task ownership and status
- Update ownership markers when starting/completing tasks
- Communicate progress through proper status updates
- Resolve conflicts through documented merge processes

## 🚨 EMERGENCY PROCEDURES

### Build Failure Response
1. Immediately identify failing component
2. Isolate failure to specific module/class
3. Implement targeted fix without broader changes
4. Verify fix with comprehensive test suite
5. Document root cause and prevention measures

### Multi-Instance Conflicts
1. Check ownership markers and active task status
2. Coordinate with other instances through status files
3. Use merge conflict resolution if necessary
4. Update coordination protocols if needed
5. Document resolution process for future reference

### Critical System Recovery
- Maintain backup branches for critical components
- Use git reflog for emergency recovery
- Implement rollback procedures for failed deployments
- Document all recovery actions taken