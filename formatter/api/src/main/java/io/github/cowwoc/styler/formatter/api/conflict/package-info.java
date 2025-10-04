/**
 * Conflict detection and resolution framework for formatting rule conflicts.
 * <p>
 * This package provides a comprehensive system for detecting and resolving conflicts
 * between formatting rules that attempt to modify overlapping regions of source code
 * in incompatible ways. The framework uses a modular architecture with pluggable
 * detection and resolution strategies.
 *
 * <h2>Architecture Overview</h2>
 * <p>
 * The conflict resolution system consists of four main layers:
 * <ol>
 * <li><strong>Data Model</strong>: Immutable records representing conflicts and resolutions
 *     ({@link io.github.cowwoc.styler.formatter.api.conflict.Conflict},
 *     {@link io.github.cowwoc.styler.formatter.api.conflict.PendingModification},
 *     {@link io.github.cowwoc.styler.formatter.api.conflict.ConflictReport},
 *     {@link io.github.cowwoc.styler.formatter.api.conflict.ResolutionDecision})</li>
 * <li><strong>Detection</strong>: Pairwise conflict detection via
 *     {@link io.github.cowwoc.styler.formatter.api.conflict.ConflictDetector}</li>
 * <li><strong>Resolution</strong>: Pluggable resolution strategies via
 *     {@link io.github.cowwoc.styler.formatter.api.conflict.ResolutionStrategy}</li>
 * <li><strong>Orchestration</strong>: Integration of detection and resolution via
 *     {@link io.github.cowwoc.styler.formatter.api.conflict.ConflictResolver}</li>
 * </ol>
 *
 * <h2>Component Interaction</h2>
 * <p>
 * The typical workflow for conflict resolution follows this sequence:
 * <pre>{@code
 * // 1. Formatting rules queue modifications via MutableFormattingContext
 * context.queueModification(new PendingModification(edit, ruleId, priority, sequence));
 *
 * // 2. When commit() is called, the resolver detects conflicts
 * ConflictDetector detector = new DefaultConflictDetector();
 * List<Conflict> conflicts = detector.detectConflicts(modifications);
 *
 * // 3. Each conflict is passed to the resolution strategy
 * ResolutionStrategy strategy = new PriorityResolutionStrategy();
 * ResolutionDecision decision = strategy.resolve(conflict);
 *
 * // 4. Final decision indicates which modifications to apply/discard
 * decision.toApply().forEach(mod -> applyModification(mod));
 * }</pre>
 *
 * <h3>Data Flow</h3>
 * <pre>
 * Formatting Rules
 *       |
 *       v
 * PendingModification Queue (MutableFormattingContext)
 *       |
 *       v
 * ConflictDetector (O(n²) pairwise comparison)
 *       |
 *       v
 * List&lt;Conflict&gt;
 *       |
 *       v
 * ResolutionStrategy (per-conflict resolution)
 *       |
 *       v
 * ResolutionDecision (toApply + toDiscard)
 *       |
 *       v
 * AST Modification (via MutableFormattingContext)
 * </pre>
 *
 * <h2>Thread-Safety Guarantees</h2>
 * <p>
 * The conflict resolution system provides the following thread-safety guarantees:
 * <ul>
 * <li><strong>Immutable Data</strong>: All data model classes
 *     ({@code Conflict}, {@code PendingModification}, {@code ConflictReport},
 *     {@code ResolutionDecision}) are immutable records with defensive copying</li>
 * <li><strong>Stateless Components</strong>: All detector and strategy implementations are
 *     stateless and thread-safe (can be safely shared across threads)</li>
 * <li><strong>Thread-Confined Context</strong>: {@code MutableFormattingContext} instances
 *     are thread-confined (each formatting operation has its own context instance)</li>
 * <li><strong>Concurrent Testing</strong>: All tests use thread-safe patterns (no
 *     {@code @BeforeMethod}, local state only) for parallel execution</li>
 * </ul>
 *
 * <h2>Resolution Strategies</h2>
 * <p>
 * The framework provides three built-in resolution strategies:
 * <ul>
 * <li><strong>{@link io.github.cowwoc.styler.formatter.api.conflict.PriorityResolutionStrategy}</strong>:
 *     Resolves conflicts by priority (higher priority wins), with sequence number tiebreaking.
 *     Default strategy for most formatting operations.</li>
 * <li><strong>{@link io.github.cowwoc.styler.formatter.api.conflict.MergeResolutionStrategy}</strong>:
 *     Attempts conservative merging of compatible modifications (non-overlapping OR identical
 *     replacement text). Throws exception for incompatible modifications.</li>
 * <li><strong>{@link io.github.cowwoc.styler.formatter.api.conflict.FailFastResolutionStrategy}</strong>:
 *     Strict validation mode that rejects all conflicts by throwing exception. Useful for
 *     detecting rule configuration errors.</li>
 * </ul>
 *
 * <h2>Performance Characteristics</h2>
 * <p>
 * The conflict detection algorithm has the following performance characteristics:
 * <ul>
 * <li><strong>Detection Complexity</strong>: O(n²) pairwise comparison where n is the number
 *     of pending modifications</li>
 * <li><strong>Resolution Complexity</strong>: O(k) where k is the number of conflicts
 *     (k ≤ n² but typically k ≪ n²)</li>
 * <li><strong>Resource Limits</strong>: MAX_PENDING_MODIFICATIONS = 10,000 and
 *     MAX_CONFLICTS = 1,000 prevent worst-case resource exhaustion</li>
 * <li><strong>Bounded Worst Case</strong>: With resource limits, worst-case is ~100M
 *     comparisons (~500ms on modern hardware)</li>
 * </ul>
 *
 * <h2>Integration with MutableFormattingContext</h2>
 * <p>
 * The {@link io.github.cowwoc.styler.formatter.api.MutableFormattingContext} class integrates
 * the conflict resolution system via the following pattern:
 * <pre>{@code
 * // Constructor creates default resolver (priority strategy)
 * public MutableFormattingContext(...) {
 *     this.conflictResolver = new DefaultConflictResolver(
 *         new DefaultConflictDetector(),
 *         new PriorityResolutionStrategy());
 * }
 *
 * // Or use custom resolver
 * public MutableFormattingContext(..., ConflictResolver resolver) {
 *     this.conflictResolver = resolver;
 * }
 *
 * // Queue modifications (assigns sequence numbers)
 * public void queueModification(PendingModification mod) {
 *     pendingModifications.add(
 *         new PendingModification(mod.edit(), mod.ruleId(), mod.priority(),
 *             sequenceCounter.getAndIncrement()));
 * }
 *
 * // Commit applies conflict resolution
 * public ResolutionDecision commit() throws ConflictResolutionException {
 *     ResolutionDecision decision = conflictResolver.resolve(pendingModifications);
 *     decision.toApply().forEach(mod -> applyModification(mod));
 *     pendingModifications.clear();
 *     sequenceCounter.set(0);
 *     return decision;
 * }
 * }</pre>
 *
 * <h2>Error Handling</h2>
 * <p>
 * The framework uses checked exception
 * {@link io.github.cowwoc.styler.formatter.api.conflict.ConflictResolutionException} to signal
 * unresolvable conflicts. All exceptions include a
 * {@link io.github.cowwoc.styler.formatter.api.conflict.ConflictReport} with detailed
 * conflict information for diagnostics:
 * <ul>
 * <li>List of all conflicts detected</li>
 * <li>Per-rule conflict counts</li>
 * <li>Maximum severity level</li>
 * <li>Human-readable summary</li>
 * </ul>
 * <p>
 * When a commit fails due to {@code ConflictResolutionException}, the pending modification
 * queue is left unchanged to allow retry with a different strategy or manual intervention.
 *
 * <h2>Extension Points</h2>
 * <p>
 * The framework is designed for extensibility via the following interfaces:
 * <ul>
 * <li><strong>{@link io.github.cowwoc.styler.formatter.api.conflict.ConflictDetector}</strong>:
 *     Implement custom conflict detection algorithms (e.g., spatial indexing for O(n log n))</li>
 * <li><strong>{@link io.github.cowwoc.styler.formatter.api.conflict.ResolutionStrategy}</strong>:
 *     Implement custom resolution policies (e.g., user-prompted resolution, machine learning)</li>
 * <li><strong>{@link io.github.cowwoc.styler.formatter.api.conflict.ConflictResolver}</strong>:
 *     Implement custom orchestration logic (e.g., multi-pass resolution, conflict clustering)</li>
 * </ul>
 *
 * <h2>Example Usage</h2>
 * <pre>{@code
 * // Create context with custom merge strategy
 * ConflictResolver resolver = new DefaultConflictResolver(
 *     new DefaultConflictDetector(),
 *     new MergeResolutionStrategy());
 *
 * MutableFormattingContext context = new MutableFormattingContext(
 *     root, sourceText, filePath, config, enabledRules, metadata, resolver);
 *
 * // Formatting rules queue modifications
 * TextEdit edit1 = new TextEdit(range1, "formatted1");
 * context.queueModification(new PendingModification(edit1, "IndentRule", 10, 0));
 *
 * TextEdit edit2 = new TextEdit(range2, "formatted2");
 * context.queueModification(new PendingModification(edit2, "LineWrapRule", 20, 0));
 *
 * // Commit applies conflict resolution
 * try {
 *     ResolutionDecision decision = context.commit();
 *     System.out.println("Applied " + decision.toApply().size() + " modifications");
 *     System.out.println("Discarded " + decision.toDiscard().size() + " conflicting modifications");
 * } catch (ConflictResolutionException e) {
 *     System.err.println("Conflict resolution failed: " + e.getMessage());
 *     ConflictReport report = e.getConflictReport();
 *     report.conflicts().forEach(conflict ->
 *         System.err.println("  - " + conflict.description()));
 * }
 * }</pre>
 *
 * @see io.github.cowwoc.styler.formatter.api.MutableFormattingContext
 * @see io.github.cowwoc.styler.formatter.api.conflict.ConflictResolver
 * @see io.github.cowwoc.styler.formatter.api.conflict.ConflictDetector
 * @see io.github.cowwoc.styler.formatter.api.conflict.ResolutionStrategy
 */
package io.github.cowwoc.styler.formatter.api.conflict;
