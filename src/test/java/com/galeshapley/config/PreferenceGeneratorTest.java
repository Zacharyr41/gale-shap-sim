package com.galeshapley.config;

import com.galeshapley.config.distribution.UniformDistributionConfig;
import com.galeshapley.config.distribution.CorrelatedDistributionConfig;
import com.galeshapley.config.distribution.CorrelatedDistributionConfig.PopularityBias;
import org.junit.jupiter.api.Test;
import java.util.*;

import static org.assertj.core.api.Assertions.*;

class PreferenceGeneratorTest {
    
    @Test
    void shouldGenerateUniformPreferencesWithoutEmptySet() {
        // Given: A preference generator with uniform distribution
        UniformDistributionConfig config = new UniformDistributionConfig();
        config.setEmptySetProbability(0.0);
        PreferenceGenerator generator = new PreferenceGenerator(config, 12345L);
        List<String> candidates = Arrays.asList("A", "B", "C", "D");
        
        // When: Generating preferences
        List<String> preferences = generator.generatePreferencesFromIds(candidates, false);
        
        // Then: Should contain all candidates without empty set
        assertThat(preferences).hasSize(4);
        assertThat(preferences).containsExactlyInAnyOrderElementsOf(candidates);
        assertThat(preferences).doesNotContain("∅");
    }
    
    @Test
    void shouldGenerateUniformPreferencesWithEmptySet() {
        // Given: A preference generator with uniform distribution and empty set
        UniformDistributionConfig config = new UniformDistributionConfig();
        config.setEmptySetProbability(1.0);
        PreferenceGenerator generator = new PreferenceGenerator(config, 54321L);
        List<String> candidates = Arrays.asList("A", "B", "C");
        
        // When: Generating preferences with empty set
        List<String> preferences = generator.generatePreferencesFromIds(candidates, true);
        
        // Then: Should contain all candidates plus empty set
        assertThat(preferences).hasSize(4);
        assertThat(preferences).contains("∅");
        assertThat(preferences).containsAll(candidates);
        
        // And: Empty set should be at some position
        int emptySetIndex = preferences.indexOf("∅");
        assertThat(emptySetIndex).isBetween(0, 3);
    }
    
    @Test
    void shouldGenerateConsistentPreferencesWithSameSeed() {
        // Given: Two generators with same seed and distribution
        UniformDistributionConfig config = new UniformDistributionConfig();
        config.setEmptySetProbability(0.0);
        PreferenceGenerator generator1 = new PreferenceGenerator(config, 42L);
        PreferenceGenerator generator2 = new PreferenceGenerator(config, 42L);
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
        UniformDistributionConfig config = new UniformDistributionConfig();
        config.setEmptySetProbability(0.0);
        PreferenceGenerator generator1 = new PreferenceGenerator(config, 42L);
        PreferenceGenerator generator2 = new PreferenceGenerator(config, 43L);
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
        UniformDistributionConfig config = new UniformDistributionConfig();
        config.setEmptySetProbability(0.5); // 50% probability
        PreferenceGenerator generator = new PreferenceGenerator(config, 100L);
        Set<String> agentIds = Set.of("agent1", "agent2", "agent3");
        Set<String> candidateIds = Set.of("A", "B", "C");
        
        // When: Generating preferences for multiple agents
        Map<String, List<String>> allPreferences = generator.generatePreferencesForAgents(
            agentIds, candidateIds);
        
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
        // Given: A preference generator with 100% empty set probability
        UniformDistributionConfig config = new UniformDistributionConfig();
        config.setEmptySetProbability(1.0); // 100% probability
        PreferenceGenerator generator = new PreferenceGenerator(config, 200L);
        Set<String> agentIds = new HashSet<>();
        for (int i = 1; i <= 20; i++) {
            agentIds.add("agent" + i);
        }
        Set<String> candidateIds = Set.of("A", "B", "C");
        
        // When: Generating preferences
        Map<String, List<String>> allPreferences = generator.generatePreferencesForAgents(
            agentIds, candidateIds);
        
        // Then: All agents should have empty set in their preferences
        for (List<String> preferences : allPreferences.values()) {
            assertThat(preferences).contains("∅");
            assertThat(preferences).hasSize(4); // 3 candidates + empty set
        }
    }
    
    @Test
    void shouldGenerateCorrelatedPreferences() {
        // Given: A preference generator with correlated distribution
        CorrelatedDistributionConfig config = new CorrelatedDistributionConfig();
        config.setEmptySetProbability(0.0);
        config.setTopPercentage(50.0); // Top 50% of positions
        
        // Set up popularity bias
        List<PopularityBias> biases = new ArrayList<>();
        PopularityBias bias1 = new PopularityBias();
        bias1.setAgent("A");
        bias1.setWeight(3.0); // 3x more likely
        biases.add(bias1);
        
        PopularityBias bias2 = new PopularityBias();
        bias2.setAgent("B");
        bias2.setWeight(2.0); // 2x more likely
        biases.add(bias2);
        
        config.setPopularityBias(biases);
        
        PreferenceGenerator generator = new PreferenceGenerator(config, 300L);
        List<String> candidates = Arrays.asList("A", "B", "C", "D");
        
        // When: Generating multiple preferences to test correlation
        Map<String, Integer> topPositionCounts = new HashMap<>();
        for (String candidate : candidates) {
            topPositionCounts.put(candidate, 0);
        }
        
        for (int i = 0; i < 100; i++) {
            List<String> prefs = generator.generatePreferencesFromIds(candidates, false);
            // Count who appears in top 2 positions
            for (int j = 0; j < 2; j++) {
                String agent = prefs.get(j);
                topPositionCounts.put(agent, topPositionCounts.get(agent) + 1);
            }
        }
        
        // Then: A and B should appear more frequently in top positions
        // Due to weights, we expect A to appear most, then B, then C and D roughly equally
        assertThat(topPositionCounts.get("A")).isGreaterThan(topPositionCounts.get("C"));
        assertThat(topPositionCounts.get("B")).isGreaterThan(topPositionCounts.get("D"));
    }
}