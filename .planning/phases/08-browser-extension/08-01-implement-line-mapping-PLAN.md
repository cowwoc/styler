# Plan: implement-line-mapping

## Objective
Generate bidirectional line mapping during reformatting.

## Tasks
1. Design LineMapping record structure
2. Implement repoToDisplay mapping
3. Implement displayToRepo mapping
4. Handle one-to-one, one-to-many, many-to-one mappings
5. Expose via FormattingResult API

## Dependencies
- Core formatting rules (complete)

## Verification
- [ ] All mapping types handled correctly
- [ ] Edge cases covered (empty lines, comments)
