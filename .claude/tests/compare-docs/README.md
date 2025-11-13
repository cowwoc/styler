# compare-docs Regression Tests

This directory contains regression tests for the `/compare-docs` semantic document comparison command.

## Test Suite

### Test 1: Full Semantic Equivalence
- **Files**: `compare-docs-test1a.md`, `compare-docs-test1b.md`
- **Purpose**: Validate detection of semantically equivalent documents with different wording
- **Expected**: 100% equivalence, all 4 rules matched
- **Validation**: Different phrasing ("Always create" vs "Before performing, you must establish") should be normalized to same semantic claims

### Test 2: Partial Overlap
- **Files**: `compare-docs-test2a.md`, `compare-docs-test2b.md`
- **Purpose**: Validate detection of documents with some shared and some unique claims
- **Expected**: 3 shared claims, 1 unique to A, 1 unique to B
- **Validation**: Correctly identifies security scan (unique to A) and documentation requirement (unique to B)

### Test 3: Explicit Sequence Detection (CRITICAL)
- **Files**: `compare-docs-test3a.md`, `compare-docs-test3b.md`
- **Purpose**: Validate sequence detection for explicit ordered procedures (Step 1/2/3/4)
- **Expected**: Sequences NOT equivalent (different order detected)
- **Test 3a sequence**: backup → rebase → verify → delete
- **Test 3b sequence**: verify → rebase → backup → delete
- **Validation**: Must detect sequence violation, report semantic_equivalence: FALSE

### Test 4: Implicit Temporal Ordering
- **Files**: `compare-docs-test4a.md`, `compare-docs-test4b.md`
- **Purpose**: Validate preservation of implicit temporal relationships
- **Expected**: Semantically equivalent (same temporal relationships)
- **Test 4a**: "Before rebasing, create backup"
- **Test 4b**: "Create backup before you perform rebase"
- **Validation**: Different wording but same implicit ordering, should detect equivalence

## Running Tests

```bash
# Test 1: Full equivalence
/compare-docs .claude/tests/compare-docs/compare-docs-test1a.md .claude/tests/compare-docs/compare-docs-test1b.md

# Test 2: Partial overlap
/compare-docs .claude/tests/compare-docs/compare-docs-test2a.md .claude/tests/compare-docs/compare-docs-test2b.md

# Test 3: Sequence violation detection
/compare-docs .claude/tests/compare-docs/compare-docs-test3a.md .claude/tests/compare-docs/compare-docs-test3b.md

# Test 4: Implicit temporal ordering
/compare-docs .claude/tests/compare-docs/compare-docs-test4a.md .claude/tests/compare-docs/compare-docs-test4b.md
```

## Success Criteria

- **Test 1**: `semantic_equivalence: true`, `overlap: 100%`
- **Test 2**: `semantic_equivalence: false`, `overlap: 60%`, correctly identifies unique claims
- **Test 3**: `semantic_equivalence: false`, `sequence_violations_count: 1`
- **Test 4**: `semantic_equivalence: true`, both documents express same temporal relationships

## Reproducibility

All tests must produce identical results across multiple runs to validate determinism.

Run each test 3 times and verify results are identical.
