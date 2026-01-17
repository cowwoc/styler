# Roadmap

Project roadmap with version summaries. Minor versions group related tasks (2-8 tasks per minor).

## Version 0: Pre-Release Development

- **0.1:** Core parser and AST infrastructure (COMPLETED)
  - implement-core-parser
  - implement-index-overlay-ast
  - implement-configuration-system
  - implement-security-framework
  - implement-file-discovery

- **0.2:** CLI and formatters for AI agent integration (COMPLETED)
  - implement-cli-application
  - implement-line-length-formatter
  - implement-import-organization
  - implement-brace-placement-formatter
  - implement-whitespace-formatter
  - implement-indentation-formatter
  - implement-ai-violation-output
  - implement-virtual-thread-processing
  - create-maven-plugin

- **0.3:** Essential parser features: yield, annotations, try-catch, labeled statements (COMPLETED)
  - add-yield-statement-support
  - annotation-parsing
  - try-catch-enhancements
  - add-binary-hex-literals
  - add-labeled-statement-support
  - add-qualified-class-instantiation
  - add-explicit-type-arguments
  - code-quality-refactoring
  - add-qualified-this-super
  - add-local-type-declarations

- **0.4:** Advanced parser features: arrays, constructors, modules, implicit classes (COMPLETED)
  - array-parsing-features
  - add-flexible-constructor-bodies
  - module-support
  - refactor-parser-depth-limiting
  - collapse-import-node-types
  - add-compact-source-files
  - migrate-parser-tests-to-nodearena
  - comment-parsing-fixes
  - add-compilation-check
  - add-primitive-type-patterns

- **0.5:** Parser edge cases for real-world Java codebases (IN PROGRESS)
  - add-array-initializer-in-annotation-support
  - fix-switch-expression-case-parsing
  - fix-lambda-parameter-parsing
  - fix-comment-in-member-declaration
  - add-nested-annotation-type-support
  - fix-contextual-keywords-as-identifiers
  - fix-cast-lambda-expression
  - fix-floating-point-literal-without-zero
  - fix-contextual-keywords-in-expressions
  - fix-comments-in-switch-arms
  - fix-old-style-switch-case-label
  - fix-lambda-typed-parameters-in-args
  - fix-wildcard-array-method-reference
  - fix-final-in-pattern-matching
  - fix-comments-before-implements
  - fix-octal-escape-in-char-literal
  - fix-annotation-on-enum-constant
  - fix-break-throw-in-switch-statement
  - fix-misc-parsing-edge-cases
  - fix-old-style-switch-fallthrough
  - fix-lambda-in-ternary-expression
  - fix-misc-expression-edge-cases
  - validate-spring-framework-parsing

- **0.6:** Test consolidation and code quality improvements (PENDING)
  - consolidate-instanceof-tests
  - consolidate-cast-tests
  - consolidate-lambda-tests
  - consolidate-lexer-tests
  - consolidate-comment-tests
  - consolidate-contextual-keyword-tests

- **0.7:** CLI enhancements and AI integration improvements (PARTIAL)
  - add-cli-parallel-processing
  - add-cli-integration-tests
  - implement-ai-context-limiting
  - implement-rules-summary-export

- **0.8:** Core browser extension functionality for GitHub PRs (PENDING)
  - implement-line-mapping
  - create-github-pr-extension
  - implement-comment-repositioning
  - implement-comment-text-translation

- **0.9:** Browser extension edge cases and performance (PENDING)
  - handle-extension-edge-cases
  - optimize-large-pr-performance

- **0.10:** User documentation and website (PENDING)
  - create-user-documentation
  - create-browser-extension-guide
  - create-ai-integration-guide
  - create-github-pages

## Version 1: Public Release

- **1.0:** Marketing content and public launch (PENDING)
  - create-marketing-content
  - setup-payment-processing
  - execute-public-launch

## Version 2: Scale & Performance

- **2.0:** Benchmarking and performance optimization (DEFERRED)
  - benchmarking-suite

## Version 3: VCS Integration

- **3.0:** VCS format filters for seamless git integration (DEFERRED)
  - implement-ast-diff
  - implement-original-preserving-clean
  - implement-vcs-format-filters

## Version 4: Parser Refinements

- **4.0:** Parser regression testing and semantic validation (DEFERRED)
  - add-regression-test-suite
  - add-semantic-validation

## Version 5: AI Enhancements

- **5.0:** Advanced AI features (DEFERRED)
  - implement-config-inference

## Version 6: Deferred Work

- **6.0:** Miscellaneous deferred tasks (DEFERRED)
  - setup-github-actions-ci
  - resolve-wildcard-imports
  - create-community-config-registry
  - update-readme-value-proposition

## Version 7: IDE Integration

- **7.0:** IDE plugin development (DEFERRED)
  - create-virtual-format-plugin

---
*Version numbering: v0.x = pre-release, v1+ = public releases*
