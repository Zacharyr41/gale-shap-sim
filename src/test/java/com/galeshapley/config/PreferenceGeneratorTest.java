package com.galeshapley.config;

import org.junit.jupiter.api.Test;
import java.util.*;

import static org.assertj.core.api.Assertions.*;

class PreferenceGeneratorTest {
    
    @Test
    void shouldGenerateRandomPreferencesWithoutEmptySet() {
        // Given: A preference generator without empty set
        PreferenceGenerator generator = new PreferenceGenerator(12345L);
        List<String> candidates = Arrays.asList("A", "B", "C", "D");
        
        // When: Generating preferences
        List<String> preferences = generator.generatePreferencesFromIds(candidates, false);
        
        // Then: Should contain all candidates without empty set
        assertThat(preferences).hasSize(4);
        assertThat(preferences).containsExactlyInAnyOrderElementsOf(candidates);
        assertThat(preferences).doesNotContain("∅");
    }
    
    @Test
    void shouldGenerateRandomPreferencesWithEmptySet() {
        // Given: A preference generator with empty set
        PreferenceGenerator generator = PreferenceGenerator.randomWithEmptySet(54321L);
        List<String> candidates = Arrays.asList("A", "B", "C");
        
        // When: Generating preferences with empty set
        List<String> preferences = generator.generatePreferencesFromIds(candidates, true);
        
        // Then: Should contain all candidates plus empty set
        assertThat(preferences).hasSize(4);
        assertThat(preferences).contains("∅");
        assertThat(preferences).containsAll(candidates);
        
        // And: Empty set should be at some position (could be anywhere due to random placement)
        int emptySetIndex = preferences.indexOf("∅");
        assertThat(emptySetIndex).isBetween(0, 3);
    }
    
    @Test
    void shouldGenerateConsistentPreferencesWithSameSeed() {
        // Given: Two generators with same seed
        PreferenceGenerator generator1 = new PreferenceGenerator(42L);
        PreferenceGenerator generator2 = new PreferenceGenerator(42L);
        List<String> candidates = Arrays.asList("X", "Y", "Z", "W");
        
        // When: Generating preferences
        List<String> preferences1 = generator1.generatePreferencesFromIds(candidates, false);
        List<String> preferences2 = generator2.generatePreferencesFromIds(candidates, false);
        
        // Then: Should generate identical lists
        assertThat(preferences1).isEqualTo(preferences2);
    }
    
    @Test
    void shouldGenerateDifferentPreferencesWithDifferentSeeds() {
        // Given: Two generators with different seeds
        PreferenceGenerator generator1 = new PreferenceGenerator(42L);
        PreferenceGenerator generator2 = new PreferenceGenerator(43L);
        List<String> candidates = Arrays.asList("X", "Y", "Z", "W");
        
        // When: Generating preferences
        List<String> preferences1 = generator1.generatePreferencesFromIds(candidates, false);
        List<String> preferences2 = generator2.generatePreferencesFromIds(candidates, false);
        
        // Then: Should generate different orders (very likely with different seeds)
        assertThat(preferences1).isNotEqualTo(preferences2);
        // But both should contain the same elements
        assertThat(preferences1).containsExactlyInAnyOrderElementsOf(preferences2);
    }
    
    @Test
    void shouldGeneratePreferencesForMultipleAgents() {
        // Given: A preference generator with specific empty set probability
        PreferenceGenerator generator = PreferenceGenerator.randomWithEmptySet(100L);
        Set<String> agentIds = Set.of("agent1", "agent2", "agent3");
        Set<String> candidateIds = Set.of("A", "B", "C");
        
        // When: Generating preferences for multiple agents
        Map<String, List<String>> allPreferences = generator.generatePreferencesForAgents(
            agentIds, candidateIds, 0.5); // 50% probability of empty set
        
        // Then: Should generate preferences for all agents
        assertThat(allPreferences).hasSize(3);
        assertThat(allPreferences.keySet()).containsExactlyInAnyOrderElementsOf(agentIds);
        
        // And: Each agent should have preferences for all candidates
        for (List<String> preferences : allPreferences.values()) {
            assertThat(preferences).hasSizeBetween(3, 4); // 3 candidates, maybe +1 for empty set
            assertThat(preferences).containsAll(candidateIds);
        }
    }
    
    @Test
    void shouldRespectEmptySetProbability() {
        // Given: A preference generator that supports empty sets
        PreferenceGenerator generator = PreferenceGenerator.randomWithEmptySet(200L);
        Set<String> agentIds = new HashSet<>();
        for (int i = 1; i <= 20; i++) {
            agentIds.add("agent" + i);
        }
        Set<String> candidateIds = Set.of("A", "B", "C");
        
        // When: Generating preferences with 100% empty set probability
        Map<String, List<String>> allPreferences = generator.generatePreferencesForAgents(
            agentIds, candidateIds, 1.0); // 100% probability
        
        // Then: All agents should have empty set in their preferences
        for (List<String> preferences : allPreferences.values()) {
            assertThat(preferences).contains("∅");
            assertThat(preferences).hasSize(4); // 3 candidates + empty set
        }
    }
    
    @Test
    void shouldHandleEmptySetPlacementStrategies() {
        // Given: Different placement strategies
        PreferenceGenerator firstPlacement = PreferenceGenerator.withConfig(
            true, PreferenceGenerator.EmptySetPlacement.FIRST, 300L);
        PreferenceGenerator lastPlacement = PreferenceGenerator.withConfig(
            true, PreferenceGenerator.EmptySetPlacement.LAST, 300L);
        
        List<String> candidates = Arrays.asList("A", "B", "C");
        
        // When: Generating preferences with different placements
        List<String> firstPrefs = firstPlacement.generatePreferencesFromIds(candidates, true);
        List<String> lastPrefs = lastPlacement.generatePreferencesFromIds(candidates, true);
        
        // Then: Empty set should be in expected positions
        assertThat(firstPrefs.get(0)).isEqualTo("∅");
        assertThat(lastPrefs.get(lastPrefs.size() - 1)).isEqualTo("∅");
    }
}