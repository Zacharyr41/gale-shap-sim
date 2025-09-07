package com.galeshapley.generation;

import java.util.Collection;
import java.util.List;
import java.util.Random;

/**
 * Strategy interface for generating preference orderings.
 * Different implementations provide different distributions of preferences.
 */
public interface PreferenceGenerationStrategy {
    
    /**
     * Generate a preference ordering for an agent over a set of candidates.
     * 
     * @param candidateIds IDs of candidates to rank
     * @param random Random number generator for reproducibility
     * @param includeEmptySet Whether to include the empty set symbol in preferences
     * @return Ordered list of candidate IDs representing the preference ordering
     */
    List<String> generatePreferences(Collection<String> candidateIds, Random random, boolean includeEmptySet);
    
    /**
     * Get the position where the empty set should be placed.
     * 
     * @param listSize Size of the preference list (excluding empty set)
     * @param random Random number generator
     * @return Position where empty set should be inserted (0-based index)
     */
    int getEmptySetPosition(int listSize, Random random);
}