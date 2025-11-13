# shrink-doc Regression Tests

This directory contains regression tests and validation reports for the `/shrink-doc` document optimization command.

## Validation Report

**[validation-report.md](validation-report.md)** - Comprehensive validation study of /compare-docs as test oracle for shrink-doc automation

**[test-data-appendix.md](test-data-appendix.md)** - Complete test data with raw questions, answers, and agent responses (75 questions, 300 responses)

**Key Findings**:
- 191/191 test scenarios passed (100% accuracy)
- Claim preservation → execution preservation validated
- Up to 2.2x compression achievable while maintaining 100% execution accuracy
- Structure independence confirmed (visual markers, headings don't impact execution)
- **Deployment Status**: APPROVED for production use in validated scope

**Validated Scope**:
- Technical documentation (requirements, protocols, procedures, standards)
- Documents up to ~150 claims
- Software development domain
- Both normative and procedural content

## Test Suite

### Test 1: Basic Shrinking with Semantic Preservation
- **Purpose**: Validate that basic document shrinking preserves all semantic content
- **Test Document**: `test-verbose-git-workflow.md`
- **Validation**: After shrinking, compare-docs should report 100% semantic equivalence

### Test 2: Sequence Preservation
- **Purpose**: Validate that procedural sequences (Step 1/2/3) are never reordered
- **Test Document**: `test-ordered-procedure.md`
- **Validation**: Step 8.5 validation must catch any sequence reordering

### Test 3: Fact Preservation
- **Purpose**: Validate that specific facts (numbers, commands, thresholds) are preserved
- **Test Document**: `test-technical-specs.md`
- **Validation**: Step 8 fact-based validation must catch any lost details

## Running Tests

```bash
# Run shrink-doc on test document
/shrink-doc .claude/tests/shrink-doc/test-verbose-git-workflow.md

# Validation happens automatically via Step 8.5 (semantic equivalence)
# If semantic violations detected, command should fail and restore backup
```

## Success Criteria

- **Semantic violations**: 0 (no claims lost, no sequences reordered)
- **Backup restored**: If violations detected, original document must be restored
- **Reproducibility**: Multiple runs should produce consistent results

## Future Test Cases

1. **Edge case**: Document with mixed ordered/unordered content
2. **Stress test**: Very large document (>5000 lines)
3. **Complex sequences**: Multiple independent procedures in same document
4. **Negative test**: Intentionally broken shrink-doc output should be caught by validation
