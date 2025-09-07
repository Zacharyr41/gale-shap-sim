package com.galeshapley.generation;

import java.util.*;

/**
 * Generates preferences using uniform random distribution.
 * All permutations are equally likely.
 */
public class UniformGenerationStrategy implements PreferenceGenerationStrategy {
    
    @Override
    public List<String> generatePreferences(Collection<String> candidateIds, Random random, boolean includeEmptySet) {
        List<String> preferences = new ArrayList<>(candidateIds);
        Collections.shuffle(preferences, random);
        
        if (includeEmptySet) {
            int emptySetPosition = getEmptySetPosition(preferences.size(), random);
            preferences.add(emptySetPosition, "∅");
        }
        
        return preferences;
    }
    
    @Override
    public int getEmptySetPosition(int listSize, Random random) {
        // For uniform distribution, empty set can be placed anywhere
        return random.nextInt(listSize + 1);
    }
}