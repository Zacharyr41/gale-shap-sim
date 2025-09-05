package com.galeshapley.config;

import com.galeshapley.model.Agent;
import com.galeshapley.model.Proposer;
import com.galeshapley.model.Proposee;

import java.util.*;

/**
 * Generates preference lists for agents using various strategies.
 * Supports random generation with configurable empty set placement.
 */
public class PreferenceGenerator {
    
    private final Random random;
    private final boolean includeEmptySet;
    private final EmptySetPlacement emptySetPlacement;
    
    public enum EmptySetPlacement {
        RANDOM,     // Place empty set at random position
        FIRST,      // Place empty set as first preference
        LAST,       // Place empty set as last preference
        NONE        // Don't include empty set
    }
    
    public PreferenceGenerator() {
        this(new Random(), false, EmptySetPlacement.NONE);
    }
    
    public PreferenceGenerator(long seed) {
        this(new Random(seed), false, EmptySetPlacement.NONE);
    }
    
    public PreferenceGenerator(Random random, boolean includeEmptySet, EmptySetPlacement emptySetPlacement) {
        this.random = random;
        this.includeEmptySet = includeEmptySet;
        this.emptySetPlacement = emptySetPlacement;
    }
    
    /**
     * Generate random preferences for proposers over proposees.
     */
    public <T extends Agent> List<String> generatePreferences(Collection<T> candidates, boolean withEmptySet) {
        List<String> preferences = new ArrayList<>();
        List<T> shuffledCandidates = new ArrayList<>(candidates);
        Collections.shuffle(shuffledCandidates, random);
        
        // Convert to IDs
        for (T candidate : shuffledCandidates) {
            preferences.add(candidate.getId());
        }
        
        // Add empty set if requested
        if (withEmptySet || includeEmptySet) {
            int emptySetPosition = determineEmptySetPosition(preferences.size());
            preferences.add(emptySetPosition, "∅");
        }
        
        return preferences;
    }
    
    /**
     * Generate random preferences using agent IDs.
     */
    public List<String> generatePreferencesFromIds(Collection<String> candidateIds, boolean withEmptySet) {
        List<String> preferences = new ArrayList<>(candidateIds);
        Collections.shuffle(preferences, random);
        
        // Add empty set if requested
        if (withEmptySet || includeEmptySet) {
            int emptySetPosition = determineEmptySetPosition(preferences.size());
            preferences.add(emptySetPosition, "∅");
        }
        
        return preferences;
    }
    
    /**
     * Generate preference configuration with random preferences for multiple agents.
     */
    public Map<String, List<String>> generatePreferencesForAgents(
            Collection<String> agentIds, 
            Collection<String> candidateIds, 
            double emptySetProbability) {
        
        Map<String, List<String>> preferences = new HashMap<>();
        
        for (String agentId : agentIds) {
            boolean shouldIncludeEmptySet = random.nextDouble() < emptySetProbability;
            List<String> agentPreferences = generatePreferencesFromIds(candidateIds, shouldIncludeEmptySet);
            preferences.put(agentId, agentPreferences);
        }
        
        return preferences;
    }
    
    private int determineEmptySetPosition(int listSize) {
        switch (emptySetPlacement) {
            case FIRST:
                return 0;
            case LAST:
                return listSize;
            case RANDOM:
                return random.nextInt(listSize + 1);
            case NONE:
            default:
                throw new IllegalStateException("Empty set placement is NONE, but empty set was requested");
        }
    }
    
    /**
     * Create a preference generator with specific configuration.
     */
    public static PreferenceGenerator withConfig(boolean includeEmptySet, EmptySetPlacement placement, long seed) {
        return new PreferenceGenerator(new Random(seed), includeEmptySet, placement);
    }
    
    /**
     * Create a preference generator for random generation.
     */
    public static PreferenceGenerator random(long seed) {
        return new PreferenceGenerator(new Random(seed), false, EmptySetPlacement.NONE);
    }
    
    /**
     * Create a preference generator with random empty set placement.
     */
    public static PreferenceGenerator randomWithEmptySet(long seed) {
        return new PreferenceGenerator(new Random(seed), true, EmptySetPlacement.RANDOM);
    }
}