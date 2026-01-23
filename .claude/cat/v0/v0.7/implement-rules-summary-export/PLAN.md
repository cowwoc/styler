# Task Plan: implement-rules-summary-export

## Objective

Export formatting rules as markdown for AI pre-guidance.

## Tasks

1. Create RulesSummaryExporter class
2. Iterate FormattingRule instances for descriptions
3. Output Markdown format (same for human and AI - JSON removed as less readable)
4. Add CLI flag --explain-rules
5. Add RuleExample record with incorrect/correct code pairs
6. Add getExamples() method to FormattingRule interface
7. Implement getExamples() in all 5 rule implementations
8. Add RuleProperty record for configuration properties
9. Add getProperties() method to FormattingRule interface
10. Implement getProperties() in all 5 rule implementations
11. Include properties table and examples in Markdown output
12. Add AI-specific framing sentence when AI audience detected

## Design Decision

JSON format was removed because:
- Code examples with `\n` escapes are less readable than Markdown code blocks
- AI models understand Markdown documentation as well as JSON
- Same content serves both human and AI audiences
- Simpler implementation with single output format

## Dependencies

- implement-ai-violation-output (complete)
- Formatters (complete)

## Verification

- [x] Clear, actionable guidance generated
- [x] Properties show configurable options with defaults
- [x] Examples show incorrect vs correct code for each rule
- [x] AI framing sentence added when AI detected

