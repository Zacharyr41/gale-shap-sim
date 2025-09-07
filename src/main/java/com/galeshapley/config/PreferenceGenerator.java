package com.galeshapley.config;

import com.galeshapley.config.distribution.DistributionConfig;
import com.galeshapley.config.distribution.UniformDistributionConfig;
import com.galeshapley.config.distribution.CorrelatedDistributionConfig;
import com.galeshapley.generation.PreferenceGenerationStrategy;
import com.galeshapley.generation.UniformGenerationStrategy;
import com.galeshapley.generation.CorrelatedGenerationStrategy;
import com.galeshapley.model.Agent;

import java.util.*;

/**
 * Generates preference lists for agents using various distribution strategies.
 */
public class PreferenceGenerator {
    
    private final PreferenceGenerationStrategy strategy;
    private final Random random;
    private final DistributionConfig config;
    
    public PreferenceGenerator(DistributionConfig config, Random random) {
        this.config = config;
        this.random = random;
        this.strategy = createStrategy(config);
    }
    
    public PreferenceGenerator(DistributionConfig config, long seed) {
        this(config, new Random(seed));
    }
    
    private PreferenceGenerationStrategy createStrategy(DistributionConfig config) {
        if (config instanceof UniformDistributionConfig) {
            return new UniformGenerationStrategy();
        } else if (config instanceof CorrelatedDistributionConfig) {
            return new CorrelatedGenerationStrategy((CorrelatedDistributionConfig) config);
        } else {
            throw new IllegalArgumentException("Unknown distribution type: " + config.getType());
        }
    }
    
    /**
     * Generate preferences for agents over candidates.
     */
    public <T extends Agent> List<String> generatePreferences(Collection<T> candidates, boolean withEmptySet) {
        List<String> candidateIds = new ArrayList<>();
        for (T candidate : candidates) {
            candidateIds.add(candidate.getId());
        }
        return generatePreferencesFromIds(candidateIds, withEmptySet);
    }
    
    /**
     * Generate preferences using agent IDs.
     */
    public List<String> generatePreferencesFromIds(Collection<String> candidateIds, boolean withEmptySet) {
        return strategy.generatePreferences(candidateIds, random, withEmptySet);
    }
    
    /**
     * Generate preferences for multiple agents.
     */
    public Map<String, List<String>> generatePreferencesForAgents(
            Collection<String> agentIds, 
            Collection<String> candidateIds) {
        
        Map<String, List<String>> preferences = new HashMap<>();
        double emptySetProbability = config.getEmptySetProbability();
        
        for (String agentId : agentIds) {
            boolean shouldIncludeEmptySet = random.nextDouble() < emptySetProbability;
            List<String> agentPreferences = generatePreferencesFromIds(candidateIds, shouldIncludeEmptySet);
            preferences.put(agentId, agentPreferences);
        }
        
        return preferences;
    }
    
    /**
     * Create a preference generator with uniform distribution.
     */
    public static PreferenceGenerator uniform(double emptySetProbability, Long seed) {
        UniformDistributionConfig config = new UniformDistributionConfig();
        config.setEmptySetProbability(emptySetProbability);
        config.setSeed(seed);
        return seed != null ? new PreferenceGenerator(config, seed) : new PreferenceGenerator(config, new Random());
    }
    
    /**
     * Create a preference generator with correlated distribution.
     */
    public static PreferenceGenerator correlated(CorrelatedDistributionConfig config, Long seed) {
        if (seed != null) {
            config.setSeed(seed);
            return new PreferenceGenerator(config, seed);
        }
        return new PreferenceGenerator(config, new Random());
    }
}