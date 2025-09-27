package io.github.cowwoc.styler.parser;

import io.github.cowwoc.styler.parser.strategies.*;
import java.util.*;

/**
 * Registry for managing parsing strategies by Java version and language feature.
 * Enables extensible parsing support for future Java language versions.
 */
public class ParseStrategyRegistry {
    private final Map<JavaVersion, List<ParseStrategy>> strategies = new EnumMap<>(JavaVersion.class);

    /**
     * Registers a parsing strategy for the specified Java version.
     *
     * @param version The Java version this strategy applies to
     * @param strategy The parsing strategy to register
     */
    public void registerStrategy(JavaVersion version, ParseStrategy strategy) {
        strategies.computeIfAbsent(version, v -> new ArrayList<>()).add(strategy);

        // Sort strategies by priority (higher priority first)
        strategies.get(version).sort((a, b) -> Integer.compare(b.getPriority(), a.getPriority()));
    }

    /**
     * Finds a strategy that can handle the current parsing context.
     *
     * @param version The target Java version
     * @param context The current parsing context
     * @return A strategy that can handle the construct, or null if none found
     */
    public ParseStrategy findStrategy(JavaVersion version, ParseContext context) {
        // Try strategies for the target version first
        List<ParseStrategy> versionStrategies = strategies.get(version);
        if (versionStrategies != null) {
            for (ParseStrategy strategy : versionStrategies) {
                if (strategy.canHandle(version, context)) {
                    return strategy;
                }
            }
        }

        // Fall back to strategies from earlier versions (backwards compatibility)
        for (JavaVersion fallbackVersion : JavaVersion.values()) {
            if (fallbackVersion.getVersionNumber() < version.getVersionNumber()) {
                List<ParseStrategy> fallbackStrategies = strategies.get(fallbackVersion);
                if (fallbackStrategies != null) {
                    for (ParseStrategy strategy : fallbackStrategies) {
                        if (strategy.canHandle(version, context)) {
                            return strategy;
                        }
                    }
                }
            }
        }

        return null;
    }

    /**
     * Gets all registered strategies for a Java version.
     *
     * @param version The Java version
     * @return List of strategies, sorted by priority
     */
    public List<ParseStrategy> getStrategies(JavaVersion version) {
        return strategies.getOrDefault(version, Collections.emptyList());
    }

    /**
     * Registers default parsing strategies for all supported Java versions.
     */
    public void registerDefaultStrategies() {
        // Register strategies for specific Java version features

        // Java 14: Switch expressions
        registerStrategy(JavaVersion.JAVA_14, new SwitchExpressionStrategy());

        // Java 16: Records
        registerStrategy(JavaVersion.JAVA_16, new RecordDeclarationStrategy());

        // Java 17: Sealed classes
        registerStrategy(JavaVersion.JAVA_17, new SealedClassStrategy());

        // Java 21: String templates (preview)
        registerStrategy(JavaVersion.JAVA_21, new StringTemplateStrategy());

        // Java 25: Latest features
        registerStrategy(JavaVersion.JAVA_25, new FlexibleConstructorBodiesStrategy());
        registerStrategy(JavaVersion.JAVA_25, new PrimitiveTypePatternStrategy());
    }

    /**
     * Gets statistics about registered strategies.
     */
    public Map<JavaVersion, Integer> getStrategyStats() {
        Map<JavaVersion, Integer> stats = new EnumMap<>(JavaVersion.class);
        for (Map.Entry<JavaVersion, List<ParseStrategy>> entry : strategies.entrySet()) {
            stats.put(entry.getKey(), entry.getValue().size());
        }
        return stats;
    }
}