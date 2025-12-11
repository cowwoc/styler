# Deployment Decision Guide: /compare-docs for Shrink-Doc Validation

**Quick reference for deciding when to use /compare-docs to validate shortened documentation.**

---

## Decision Tree

```
Is this a software development document?
├─ YES → Is it <150 claims?
│  ├─ YES → ✅ DEPLOY with confidence (validated scope)
│  └─ NO → ⚠️ Run validation test first, then deploy
└─ NO → Is it technical documentation with clear claims?
   ├─ YES → ⚠️ Run domain-specific validation test first
   └─ NO → ❌ Manual review recommended (narrative content)
```

---

## Quick Validation Workflow

### 1. Run Semantic Comparison

```bash
/compare-docs original.md shortened.md
```

### 2. Check Results

**✅ GREEN LIGHT (Deploy immediately)**:
- `semantic_equivalence: true`
- `overlap_percentage: 100%`
- `unique_a_count: 0`
- `unique_b_count: 0`

**⚠️ YELLOW LIGHT (Review required)**:
- `overlap_percentage: 95-99%`
- `unique_a_count: <5` OR `unique_b_count: <5`
- **Action**: Review unique claims manually
  - Organizational differences → safe to deploy
  - Substantive differences → investigate impact

**🛑 RED LIGHT (Do not deploy)**:
- `overlap_percentage: <95%`
- `unique_a_count: >5` OR `unique_b_count: >5`
- **Action**: Reconcile differences or keep original

### 3. Manual Review Checklist (for Yellow Light)

Review "unique_to_a" claims:
- [ ] Are these organizational (headings, sections)?
- [ ] Are these explanatory (rationale, examples)?
- [ ] Are these substantive (requirements, constraints)?

Review "unique_to_b" claims:
- [ ] Are these additions (new requirements)?
- [ ] Are these clarifications (more specific wording)?
- [ ] Are these substantive changes?

Review "sequence_violations":
- [ ] Do ordered procedures have same steps in same order?
- [ ] Are step count differences just aggregation?

**If all organizational/explanatory → Safe to deploy**
**If any substantive differences → Investigate or keep original**

---

## Validation Test Template

**Use this when deploying to new domain or >150 claims:**

### Step 1: Create Test Scenarios

Create 3-5 test scenarios that require:
1. **Judgment**: Interpreting requirements to make decisions
2. **Search**: Finding specific information without section hints
3. **Application**: Applying procedures to novel situations

### Step 2: Test Execution

Run test with both documents (original and shortened):

```bash
# Test with original
Task(model="opus", prompt="Read original.md, execute test-scenarios.md")

# Test with shortened
Task(model="opus", prompt="Read shortened.md, execute test-scenarios.md")
```

### Step 3: Compare Results

- [ ] Both versions produce identical answers
- [ ] All answers are correct
- [ ] No degradation in quality or completeness

**If all checks pass → Deploy with confidence**

---

## Evidence Base

This guide is based on comprehensive validation with 191 test scenarios:

| Metric | Result |
|--------|--------|
| Total scenarios tested | 191 |
| Scenarios passed | 191 |
| Overall accuracy | 100% |
| Document types validated | 4 (normative, protocol, quality, procedural) |
| Models validated | 2 (Opus 4.5, Haiku 4.5) |
| Compression ratio validated | Up to 2.2x |

**See [validation-report.md](validation-report.md) for complete details.**

---

## Common Questions

### Q: Does structure matter?

**A: No.** Validated with 176 scenarios comparing structured (headings, markers, formatting) vs flat (prose) formats. 100% identical execution.

### Q: Can I remove visual markers (✅/❌/⚠️)?

**A: Yes.** Visual markers are presentation, not content. LLMs process semantically.

### Q: What about numbered lists vs prose?

**A: Safe to flatten** as long as /compare-docs confirms claim preservation. Tested with procedural workflows.

### Q: Can I use cheaper models (Haiku) with shortened docs?

**A: Yes.** Haiku 4.5 showed identical 100% accuracy on both structured and flat formats.

### Q: What if my document has >150 claims?

**A: Run validation test first.** Validated up to 147 claims; larger documents are unvalidated but likely work. Test before deployment.

### Q: What about multi-session retention?

**A: Not yet tested.** All validation was single-session. Long-term retention studies recommended for critical documentation.

---

## Risk Assessment

### Low Risk (Deploy with Confidence)

- Software development documentation
- Clear technical requirements
- <150 claims
- /compare-docs shows 100% equivalence

### Medium Risk (Validation Test Recommended)

- Documents >150 claims
- New domain (non-software)
- 95-99% semantic equivalence
- Critical operational procedures

### High Risk (Manual Review Required)

- Narrative-heavy content
- Ambiguous or vague requirements
- <95% semantic equivalence
- Safety-critical documentation (medical, aviation)

---

## Troubleshooting

### Problem: /compare-docs reports <100% equivalence

**Diagnosis**: Check "unique_to_a" and "unique_to_b" lists

**Common causes**:
1. Organizational claims (headings, sections) - safe to ignore
2. Explanatory claims (rationale, examples) - assess importance
3. Granularity mismatches (1 claim split into 2) - safe if semantically equivalent
4. Substantive differences - requires investigation

**Solution**: Review unique claims manually, assess if differences are organizational vs substantive

### Problem: Execution test shows degraded performance

**Diagnosis**: Shortened version missing critical information

**Common causes**:
1. Implicit claims not extracted by /compare-docs
2. Context-dependent claims lost
3. Domain-specific terminology not normalized correctly

**Solution**: Revert to original or reconcile differences

### Problem: Sequence violations reported

**Diagnosis**: Ordered procedures may have different step counts or order

**Common causes**:
1. Steps aggregated (6 steps → 5 steps combining two) - verify semantically equivalent
2. Steps reordered - verify order doesn't matter or is error
3. Steps omitted - requires investigation

**Solution**: Review sequence_violations in comparison output, verify procedural integrity

---

## Best Practices

1. **Always run /compare-docs before deployment**
   - Even if manual review suggests equivalence
   - Automated validation catches subtle differences

2. **Review unique claims for substantive differences**
   - Organizational/explanatory differences are safe
   - Substantive differences require investigation

3. **Test critical documentation before deployment**
   - Create 3-5 scenarios testing key requirements
   - Verify identical execution with both versions

4. **Document compression ratios achieved**
   - Track how much compression is safe for each doc type
   - Build empirical evidence for future decisions

5. **Monitor production usage**
   - Track any execution issues with shortened docs
   - Update validation as edge cases discovered

---

## Support

**Questions or issues?**
- Review [validation-report.md](validation-report.md) for comprehensive evidence
- Check test artifacts in `/tmp/` for examples
- Consult /compare-docs command documentation for technical details

**Reporting validation failures**:
- Document the specific document type and size
- Include /compare-docs output
- Describe execution degradation observed
- This helps expand validated scope

---

**Version**: 1.0
**Last Updated**: 2025-11-19
**Status**: Production-ready for validated scope
