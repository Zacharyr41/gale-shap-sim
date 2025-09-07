package com.galeshapley.generation;

import com.galeshapley.config.distribution.CorrelatedDistributionConfig;
import com.galeshapley.config.distribution.CorrelatedDistributionConfig.PopularityBias;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Generates preferences with correlations, where certain candidates
 * appear more frequently in top positions due to popularity bias.
 */
public class CorrelatedGenerationStrategy implements PreferenceGenerationStrategy {
    
    private final List<PopularityBias> popularityBiases;
    private final double topPercentage;
    
    public CorrelatedGenerationStrategy(CorrelatedDistributionConfig config) {
        this.popularityBiases = config.getPopularityBias() != null ? 
            config.getPopularityBias() : new ArrayList<>();
        this.topPercentage = config.getTopPercentage() / 100.0; // Convert to fraction
    }
    
    @Override
    public List<String> generatePreferences(Collection<String> candidateIds, Random random, boolean includeEmptySet) {
        List<String> preferences = new ArrayList<>();
        List<String> remainingCandidates = new ArrayList<>(candidateIds);
        
        // Calculate how many positions should use weighted selection
        int totalPositions = candidateIds.size();
        int topPositions = Math.max(1, (int)(totalPositions * topPercentage));
        
        // Generate top positions using weighted selection
        for (int i = 0; i < topPositions && !remainingCandidates.isEmpty(); i++) {
            String selected = weightedSelection(remainingCandidates, random);
            preferences.add(selected);
            remainingCandidates.remove(selected);
        }
        
        // Fill remaining positions with uniform random selection
        Collections.shuffle(remainingCandidates, random);
        preferences.addAll(remainingCandidates);
        
        // Add empty set if requested
        if (includeEmptySet) {
            int emptySetPosition = getEmptySetPosition(preferences.size(), random);
            preferences.add(emptySetPosition, "∅");
        }
        
        return preferences;
    }
    
    /**
     * Perform weighted random selection from candidates.
     */
    private String weightedSelection(List<String> candidates, Random random) {
        // Calculate total weight
        double totalWeight = 0.0;
        for (String candidate : candidates) {
            totalWeight += getWeightForCandidate(candidate);
        }
        
        // Generate random value and select based on weights
        double randomValue = random.nextDouble() * totalWeight;
        double cumulativeWeight = 0.0;
        
        for (String candidate : candidates) {
            cumulativeWeight += getWeightForCandidate(candidate);
            if (randomValue <= cumulativeWeight) {
                return candidate;
            }
        }
        
        // Fallback (should not reach here)
        return candidates.get(candidates.size() - 1);
    }
    
    /**
     * Get the weight for a specific candidate based on all bias rules.
     * If multiple rules apply, the highest weight is used.
     */
    private double getWeightForCandidate(String candidateId) {
        double maxWeight = 1.0; // Default weight
        
        for (PopularityBias bias : popularityBiases) {
            if (bias.appliesTo(candidateId)) {
                maxWeight = Math.max(maxWeight, bias.getWeight());
            }
        }
        
        return maxWeight;
    }
    
    @Override
    public int getEmptySetPosition(int listSize, Random random) {
        // For correlated distribution, we can still place empty set randomly
        // or bias it towards the end (after the "popular" candidates)
        // For now, using random placement
        return random.nextInt(listSize + 1);
    }
}