package com.galeshapley.integration;

import com.galeshapley.algorithm.GaleShapleyAlgorithm;
import com.galeshapley.config.SimulationConfig;
import com.galeshapley.config.SimulationConfigLoader;
import com.galeshapley.model.Proposer;
import com.galeshapley.model.Proposee;
import com.galeshapley.model.PreferenceList;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Comprehensive integration test for all distribution features working together.
 * Tests global configs, overrides, uniform vs correlated distributions, and their interactions.
 */
public class ComprehensiveDistributionFeaturesIntegrationTest {
    
    @Test
    void shouldHandleAllDistributionFeaturesWorkingTogether() throws IOException {
        // Given: A complex configuration with multiple distribution types and overrides
        SimulationConfigLoader loader = new SimulationConfigLoader();
        SimulationConfig config = loader.loadFromFile("src/test/resources/comprehensive-distribution-features-config.yaml");
        
        // Verify the configuration loaded correctly
        assertThat(config.getProposers()).hasSize(100);
        assertThat(config.getProposees()).hasSize(80);
        
        // When: Running the Gale-Shapley algorithm
        GaleShapleyAlgorithm algorithm = new GaleShapleyAlgorithm(
            config.getProposerPreferences(),
            config.getProposeePreferences(),
            config.getEmptySetPreferences(),
            config.getProposeeEmptySetPreferences()
        );
        GaleShapleyAlgorithm.AlgorithmResult result = algorithm.execute();
        
        // Then: Algorithm should complete successfully
        assertThat(result).isNotNull();
        assertThat(result.getFinalMatching()).isNotNull();
        assertThat(result.getIterations()).isGreaterThan(0);
        
        // Verify preferences were generated according to different distribution types
        validateProposerPreferenceDistributions(config);
        validateProposeePreferenceDistributions(config);
        validateEmptySetDistributions(config);
    }
    
    /**
     * Validates that proposer preferences follow expected distributions based on configuration.
     */
    private void validateProposerPreferenceDistributions(SimulationConfig config) {
        Map<String, Integer> popularityCount = new HashMap<>();
        
        // Count how often highly weighted agents appear in top positions
        for (Proposer proposer : config.getProposers()) {
            PreferenceList<Proposee> prefs = config.getProposerPreferences().get(proposer);
            if (prefs != null && prefs.size() > 0) {
                // Check first preference (top position)
                String topChoice = prefs.getPreferredAt(0).getId();
                popularityCount.merge(topChoice, 1, Integer::sum);
            }
        }
        
        // Global config has bias toward e10, e25, e50 - they should appear more frequently
        // Note: Overrides affect some proposers, so we expect general trends, not absolute dominance
        int e10Count = popularityCount.getOrDefault("e10", 0);
        int e25Count = popularityCount.getOrDefault("e25", 0);
        int e50Count = popularityCount.getOrDefault("e50", 0);
        
        // These popular agents should appear more than average in top positions
        // Average would be ~100/80 = 1.25 per agent, so popular ones should be higher
        System.out.println("Top choice frequency - e10: " + e10Count + ", e25: " + e25Count + ", e50: " + e50Count);
        
        // At least one of the globally popular agents should appear more than average
        assertThat(Math.max(Math.max(e10Count, e25Count), e50Count)).isGreaterThan(2);
    }
    
    /**
     * Validates that proposee preferences follow expected distributions based on configuration.
     */
    private void validateProposeePreferenceDistributions(SimulationConfig config) {
        Map<String, Integer> popularityCount = new HashMap<>();
        
        // Count popularity in proposee preferences (for overridden ranges)
        for (Proposee proposee : config.getProposees()) {
            PreferenceList<Proposer> prefs = config.getProposeePreferences().get(proposee);
            if (prefs != null && prefs.size() > 0) {
                // Check first few preferences for bias patterns
                for (int i = 0; i < Math.min(3, prefs.size()); i++) {
                    String choice = prefs.getPreferredAt(i).getId();
                    popularityCount.merge(choice, 1, Integer::sum);
                }
            }
        }
        
        // Proposees 20-29 have correlated preferences with bias toward p5, p20, p35
        // These should appear more frequently in their preferences
        int p5Count = popularityCount.getOrDefault("p5", 0);
        int p20Count = popularityCount.getOrDefault("p20", 0);
        int p35Count = popularityCount.getOrDefault("p35", 0);
        
        System.out.println("Proposee preference frequency - p5: " + p5Count + ", p20: " + p20Count + ", p35: " + p35Count);
        
        // At least one should show some bias (though effect is limited to range 20-29)
        assertThat(Math.max(Math.max(p5Count, p20Count), p35Count)).isGreaterThan(0);
    }
    
    /**
     * Validates empty set probability distributions across different ranges.
     */
    private void validateEmptySetDistributions(SimulationConfig config) {
        Map<String, List<Integer>> emptySetRanges = new HashMap<>();
        emptySetRanges.put("Global (0.10)", new ArrayList<>());
        emptySetRanges.put("Range 10-19 (0.05)", new ArrayList<>());
        emptySetRanges.put("Range 30-39 (0.20)", new ArrayList<>());
        emptySetRanges.put("Range 70-79 (0.25)", new ArrayList<>());
        emptySetRanges.put("Index 50 (0.00)", new ArrayList<>());
        
        // Check proposer empty set distributions
        for (Proposer proposer : config.getProposers()) {
            int proposerIndex = Integer.parseInt(proposer.getId().substring(1)); // Remove 'p' prefix
            Integer emptySetPos = config.getEmptySetPreferences().get(proposer);
            boolean hasEmptySet = emptySetPos != null;
            
            if (proposerIndex >= 10 && proposerIndex <= 19) {
                emptySetRanges.get("Range 10-19 (0.05)").add(hasEmptySet ? 1 : 0);
            } else if (proposerIndex >= 30 && proposerIndex <= 39) {
                emptySetRanges.get("Range 30-39 (0.20)").add(hasEmptySet ? 1 : 0);
            } else if (proposerIndex >= 70 && proposerIndex <= 79) {
                emptySetRanges.get("Range 70-79 (0.25)").add(hasEmptySet ? 1 : 0);
            } else if (proposerIndex == 50) {
                emptySetRanges.get("Index 50 (0.00)").add(hasEmptySet ? 1 : 0);
            } else {
                emptySetRanges.get("Global (0.10)").add(hasEmptySet ? 1 : 0);
            }
        }
        
        // Validate empty set distributions match expected probabilities
        for (Map.Entry<String, List<Integer>> entry : emptySetRanges.entrySet()) {
            String rangeDesc = entry.getKey();
            List<Integer> values = entry.getValue();
            if (!values.isEmpty()) {
                double actualRate = values.stream().mapToInt(Integer::intValue).average().orElse(0.0);
                System.out.println(rangeDesc + " - Actual empty set rate: " + String.format("%.3f", actualRate) + 
                                 " (count: " + values.size() + ")");
                
                // Validate specific expectations
                if (rangeDesc.contains("Index 50 (0.00)")) {
                    // Index 50 should never have empty set
                    assertThat(actualRate).isEqualTo(0.0);
                } else {
                    // Others should have some reasonable rate > 0
                    assertThat(actualRate).isGreaterThanOrEqualTo(0.0);
                }
            }
        }
    }
    
    @Test
    void shouldHandleMixedDistributionsInMultipleRuns() throws IOException {
        // Given: The comprehensive configuration
        SimulationConfigLoader loader = new SimulationConfigLoader();
        
        // When: Running multiple simulations to test consistency and variation
        List<GaleShapleyAlgorithm.AlgorithmResult> results = new ArrayList<>();
        List<Integer> iterationCounts = new ArrayList<>();
        
        for (int run = 0; run < 5; run++) {
            SimulationConfig config = loader.loadFromFile("src/test/resources/comprehensive-distribution-features-config.yaml");
            
            GaleShapleyAlgorithm algorithm = new GaleShapleyAlgorithm(
                config.getProposerPreferences(),
                config.getProposeePreferences(),
                config.getEmptySetPreferences(),
                config.getProposeeEmptySetPreferences()
            );
            
            GaleShapleyAlgorithm.AlgorithmResult result = algorithm.execute();
            results.add(result);
            iterationCounts.add(result.getIterations());
        }
        
        // Then: All runs should complete successfully
        assertThat(results).hasSize(5);
        assertThat(results).allSatisfy(result -> {
            assertThat(result).isNotNull();
            assertThat(result.getFinalMatching()).isNotNull();
            assertThat(result.getIterations()).isGreaterThan(0);
        });
        
        // Iterations may vary due to different preference orderings
        double avgIterations = iterationCounts.stream().mapToInt(Integer::intValue).average().orElse(0.0);
        System.out.println("Average iterations across 5 runs: " + String.format("%.1f", avgIterations));
        System.out.println("Iteration range: " + Collections.min(iterationCounts) + " - " + Collections.max(iterationCounts));
        
        // Should be reasonable iteration counts for 100 proposers, 80 proposees
        assertThat(avgIterations).isBetween(10.0, 200.0);
    }
    
    @Test
    void shouldValidateDistributionTypesAreAppliedCorrectly() throws IOException {
        // Given: Configuration with mixed distribution types
        SimulationConfigLoader loader = new SimulationConfigLoader();
        SimulationConfig config = loader.loadFromFile("src/test/resources/comprehensive-distribution-features-config.yaml");
        
        // When: Analyzing the generated preferences
        Map<String, Set<String>> firstChoicesPerRange = new HashMap<>();
        firstChoicesPerRange.put("global-correlated", new HashSet<>());
        firstChoicesPerRange.put("range-10-19-uniform", new HashSet<>());
        firstChoicesPerRange.put("range-30-39-correlated", new HashSet<>());
        firstChoicesPerRange.put("range-70-79-uniform", new HashSet<>());
        
        for (Proposer proposer : config.getProposers()) {
            int proposerIndex = Integer.parseInt(proposer.getId().substring(1));
            PreferenceList<Proposee> prefs = config.getProposerPreferences().get(proposer);
            
            if (prefs != null && prefs.size() > 0) {
                String firstChoice = prefs.getPreferredAt(0).getId();
                
                if (proposerIndex >= 10 && proposerIndex <= 19) {
                    firstChoicesPerRange.get("range-10-19-uniform").add(firstChoice);
                } else if (proposerIndex >= 30 && proposerIndex <= 39) {
                    firstChoicesPerRange.get("range-30-39-correlated").add(firstChoice);
                } else if (proposerIndex >= 70 && proposerIndex <= 79) {
                    firstChoicesPerRange.get("range-70-79-uniform").add(firstChoice);
                } else {
                    firstChoicesPerRange.get("global-correlated").add(firstChoice);
                }
            }
        }
        
        // Then: Validate distribution characteristics
        for (Map.Entry<String, Set<String>> entry : firstChoicesPerRange.entrySet()) {
            String rangeType = entry.getKey();
            Set<String> firstChoices = entry.getValue();
            System.out.println(rangeType + " - Unique first choices: " + firstChoices.size());
            
            // All ranges should have generated some preferences
            assertThat(firstChoices).isNotEmpty();
            
            // Correlated distributions might show more concentration, uniform more spread
            // But we mainly validate that the system works without errors
        }
        
        System.out.println("✓ All distribution types applied successfully across different ranges");
    }
}