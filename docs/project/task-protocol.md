# Task State Machine Protocol

**NOTICE**: This document has been split into two files for better accessibility and performance.

## 📖 Read These Documents in Order:

### Part 1: [task-protocol-core.md](task-protocol-core.md)
**Core State Machine & Agent Selection** (~14,400 tokens)

Contains:
- State machine architecture and definitions
- Risk-based agent selection engine
- Workflow variants by risk level
- Agent selection decision tree
- Complete style validation framework
- Batch processing and continuous mode
- Mandatory state transitions (INIT → CLASSIFIED → REQUIREMENTS → SYNTHESIS → IMPLEMENTATION → VALIDATION → REVIEW → SCOPE_NEGOTIATION)

**Start here** to understand the overall protocol architecture.

### Part 2: [task-protocol-operations.md](task-protocol-operations.md)
**Operations & Procedures** (~10,150 tokens)

Contains:
- COMPLETE → CLEANUP transition procedures
- Transition validation functions
- Agent interaction protocols
- Error handling & recovery
- Compliance verification
- Violation prevention patterns
- Context preservation rules
- Tool-specific optimization patterns
- Workflow execution engine

**Read this** for detailed implementation procedures.

---

## 🚨 Why Was This Split?

**Problem**: Original file was 25,056 tokens (56 tokens over the 25,000 Read tool limit)

**Impact**: Required 5 separate Read operations to access all content

**Solution**: Split into 2 balanced files requiring only 2 Read operations total (60% efficiency improvement)

**Organization**: Natural split at state transitions - Part 1 covers architecture/framework, Part 2 covers procedures/operations

## 📋 Quick Reference

**For CLAUDE.md Summary**: The CLAUDE.md file contains a "Post-Compaction Note" summary providing sufficient basic protocol compliance when these detailed documents aren't accessible.

**For New Tasks**: Always start with task-protocol-core.md to understand risk classification and required agent selection.

**For Implementation**: Reference task-protocol-operations.md for detailed lock acquisition, worktree management, and state transition procedures.
