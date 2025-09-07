package com.galeshapley.integration;

import com.galeshapley.algorithm.GaleShapleyAlgorithm;
import com.galeshapley.config.SimulationConfig;
import com.galeshapley.config.SimulationConfigLoader;
import com.galeshapley.config.RuntimeOptions;
import com.galeshapley.observer.StatisticsObserver;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.*;

class LargeScaleRandomPreferenceIntegrationTest {
    
    @Test
    void shouldHandleLargeScaleRandomGenerationWithGlobalAndIndividualOverrides() throws IOException {
        // Given: A YAML configuration with 1000 proposers, 1050 proposees
        // with global empty set probabilities and individual overrides
        SimulationConfigLoader loader = new SimulationConfigLoader();
        
        RuntimeOptions runtimeOptions = RuntimeOptions.builder()
            .maxIterations(5000)  // Higher limit for large scale
            .trackIterationMetrics(true)
            .globalSeed(42L)  // Deterministic random generation for reproducible tests
            .build();
            
        SimulationConfig config = loader.loadFromFile("src/test/resources/large-scale-random-config.yaml", runtimeOptions);
        
        // Verify the configuration loaded correctly
        assertThat(config.getProposerPreferences()).hasSize(1000);
        assertThat(config.getProposeePreferences()).hasSize(1050);
        
        // Analyze empty set preferences by groups
        int proposersWithEmptySetInOverrideRange1 = 0; // indices 99-149 (proposers 100-150)
        int proposersWithEmptySetInOverrideIndex = 0;   // index 189 (proposer 190)
        int proposersWithEmptySetNormal = 0;            // all others
        
        for (int i = 0; i < 1000; i++) {
            String proposerId = "p" + i;
            Integer emptySetPos = config.getEmptySetPreferences().get(
                config.getProposerPreferences().keySet().stream()
                    .filter(p -> p.getId().equals(proposerId))
                    .findFirst().orElse(null)
            );
            
            boolean hasEmptySet = emptySetPos != null && emptySetPos >= 0;
            
            if (i >= 99 && i <= 149) {
                if (hasEmptySet) proposersWithEmptySetInOverrideRange1++;
            } else if (i == 189) {
                if (hasEmptySet) proposersWithEmptySetInOverrideIndex++;
            } else {
                if (hasEmptySet) proposersWithEmptySetNormal++;
            }
        }
        
        int proposeesWithEmptySetInOverrideRange1 = 0; // indices 199-249 (proposees 200-250)
        int proposeesWithEmptySetInOverrideIndex = 0;   // index 289 (proposee 290)
        int proposeesWithEmptySetNormal = 0;            // all others
        
        for (int i = 0; i < 1050; i++) {
            String proposeeId = "e" + i;
            Integer emptySetPos = config.getProposeeEmptySetPreferences().get(
                config.getProposeePreferences().keySet().stream()
                    .filter(p -> p.getId().equals(proposeeId))
                    .findFirst().orElse(null)
            );
            
            boolean hasEmptySet = emptySetPos != null && emptySetPos >= 0;
            
            if (i >= 199 && i <= 249) {
                if (hasEmptySet) proposeesWithEmptySetInOverrideRange1++;
            } else if (i == 289) {
                if (hasEmptySet) proposeesWithEmptySetInOverrideIndex++;
            } else {
                if (hasEmptySet) proposeesWithEmptySetNormal++;
            }
        }
        
        long totalProposersWithEmptySet = proposersWithEmptySetInOverrideRange1 + 
                                          proposersWithEmptySetInOverrideIndex + 
                                          proposersWithEmptySetNormal;
        long totalProposeesWithEmptySet = proposeesWithEmptySetInOverrideRange1 + 
                                          proposeesWithEmptySetInOverrideIndex + 
                                          proposeesWithEmptySetNormal;
        
        // Print detailed statistics
        System.out.println("\n=== Empty Set Preference Distribution ===");
        System.out.println("PROPOSERS (Total: 1000):");
        System.out.println("  Normal (949 agents, 5% expected = ~47):");
        System.out.println("    With empty set: " + proposersWithEmptySetNormal);
        System.out.println("  Override Range 100-150 (51 agents, 15% expected = ~8):");
        System.out.println("    With empty set: " + proposersWithEmptySetInOverrideRange1);
        System.out.println("  Override Index 190 (1 agent, 15% expected = 0 or 1):");
        System.out.println("    With empty set: " + proposersWithEmptySetInOverrideIndex);
        System.out.println("  TOTAL with empty set: " + totalProposersWithEmptySet);
        
        System.out.println("\nPROPOSEES (Total: 1050):");
        System.out.println("  Normal (999 agents, 20% expected = ~200):");
        System.out.println("    With empty set: " + proposeesWithEmptySetNormal);
        System.out.println("  Override Range 200-250 (51 agents, 40% expected = ~20):");
        System.out.println("    With empty set: " + proposeesWithEmptySetInOverrideRange1);
        System.out.println("  Override Index 290 (1 agent, 40% expected = 0 or 1):");
        System.out.println("    With empty set: " + proposeesWithEmptySetInOverrideIndex);
        System.out.println("  TOTAL with empty set: " + totalProposeesWithEmptySet);
        
        GaleShapleyAlgorithm algorithm = new GaleShapleyAlgorithm(
            config.getProposerPreferences(),
            config.getProposeePreferences(),
            config.getEmptySetPreferences(),
            config.getProposeeEmptySetPreferences(),
            runtimeOptions
        );
        
        StatisticsObserver statisticsObserver = new StatisticsObserver();
        algorithm.addObserver(statisticsObserver);
        
        // When: Running the algorithm with large-scale random preferences
        GaleShapleyAlgorithm.AlgorithmResult result = algorithm.execute();
        StatisticsObserver.Statistics stats = statisticsObserver.getStatistics();
        
        // Then: The algorithm should complete within the iteration limit
        assertThat(result.getIterations()).isLessThanOrEqualTo(5000);
        
        // And: Should handle the large scale scenario
        assertThat(result.getFinalMatching().getMatchCount()).isGreaterThan(0);
        assertThat(result.getFinalMatching().getMatchCount()).isLessThanOrEqualTo(1000);
        
        // And: Should have completed execution in reasonable time
        assertThat(stats.getExecutionTimeMs()).isLessThan(30000); // Less than 30 seconds
        
        // And: Statistics should make sense for a large-scale scenario
        assertThat(stats.getTotalIterationAttempts()).isGreaterThan(0);
        assertThat(stats.getAverageIterationAttemptsPerProposer()).isGreaterThan(0.0);
        
        // With 5% proposer and 20% proposee empty set probability,
        // we expect most proposers to find matches
        System.out.println("=== Large Scale Random Preference Generation Test Results ===");
        System.out.println("Final matches: " + result.getFinalMatching().getMatchCount());
        System.out.println("Unmatched proposers: " + result.getFinalMatching().getUnmatchedProposers().size());
        System.out.println("Unmatched proposees: " + result.getFinalMatching().getUnmatchedProposees().size());
        System.out.println("Total iteration attempts: " + stats.getTotalIterationAttempts());
        System.out.println("Total real proposals: " + stats.getTotalProposals());
        System.out.println("Algorithm iterations: " + result.getIterations());
        System.out.println("Execution time: " + stats.getExecutionTimeMs() + "ms");
        
        // Verify proposer empty set preferences
        // Normal proposers (949 total): 5% expected = ~47
        assertThat(proposersWithEmptySetNormal)
            .as("Normal proposers (5% of 949)")
            .isBetween(30, 65);  // Allow for randomness
        
        // Override range 100-150 (51 total): 15% expected = ~8
        assertThat(proposersWithEmptySetInOverrideRange1)
            .as("Override range proposers 100-150 (15% of 51)")
            .isBetween(3, 13);  // Allow for randomness
        
        // Override single proposer 190: 15% chance (0 or 1)
        assertThat(proposersWithEmptySetInOverrideIndex)
            .as("Override single proposer 190 (15% chance)")
            .isBetween(0, 1);
        
        // Total proposers with empty set
        assertThat(totalProposersWithEmptySet)
            .as("Total proposers with empty set")
            .isBetween(30L, 80L);
        
        // Verify proposee empty set preferences  
        // Normal proposees (999 total): 20% expected = ~200
        assertThat(proposeesWithEmptySetNormal)
            .as("Normal proposees (20% of 999)")
            .isBetween(170, 230);  // Allow for randomness
        
        // Override range 200-250 (51 total): 40% expected = ~20
        assertThat(proposeesWithEmptySetInOverrideRange1)
            .as("Override range proposees 200-250 (40% of 51)")
            .isBetween(12, 28);  // Allow for randomness
        
        // Override single proposee 290: 40% chance (0 or 1)
        assertThat(proposeesWithEmptySetInOverrideIndex)
            .as("Override single proposee 290 (40% chance)")
            .isBetween(0, 1);
        
        // Total proposees with empty set
        assertThat(totalProposeesWithEmptySet)
            .as("Total proposees with empty set")
            .isBetween(180L, 260L);
        
        // The actual unmatched counts depend on the matching dynamics
        // Some agents with empty set preferences may still get matched if approached before their cutoff
        assertThat(result.getFinalMatching().getUnmatchedProposers().size()).isGreaterThanOrEqualTo(0);
        assertThat(result.getFinalMatching().getUnmatchedProposees().size()).isGreaterThanOrEqualTo(50);
    }
}