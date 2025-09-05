package com.galeshapley.integration;

import com.galeshapley.algorithm.GaleShapleyAlgorithm;
import com.galeshapley.config.SimulationConfig;
import com.galeshapley.config.SimulationConfigLoader;
import com.galeshapley.config.RuntimeOptions;
import com.galeshapley.observer.StatisticsObserver;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.*;

class RandomPreferenceIntegrationTest {
    
    @Test
    void shouldHandleRandomPreferenceGenerationWithIterationLimits() throws IOException {
        // Given: A YAML configuration using random preference generation with empty sets
        SimulationConfigLoader loader = new SimulationConfigLoader();
        
        RuntimeOptions runtimeOptions = RuntimeOptions.builder()
            .maxIterations(50)  // Set reasonable iteration limit
            .trackIterationMetrics(true)
            .globalSeed(42L)  // Use global seed for deterministic random generation
            .build();
            
        SimulationConfig config = loader.loadFromFile("src/test/resources/random-large-scale-config.yaml", runtimeOptions);
        
        GaleShapleyAlgorithm algorithm = new GaleShapleyAlgorithm(
            config.getProposerPreferences(),
            config.getProposeePreferences(),
            config.getEmptySetPreferences(),
            config.getProposeeEmptySetPreferences(),
            runtimeOptions
        );
        
        StatisticsObserver statisticsObserver = new StatisticsObserver();
        algorithm.addObserver(statisticsObserver);
        
        // When: Running the algorithm with random preferences
        GaleShapleyAlgorithm.AlgorithmResult result = algorithm.execute();
        StatisticsObserver.Statistics stats = statisticsObserver.getStatistics();
        
        // Then: The algorithm should complete within the iteration limit
        assertThat(result.getIterations()).isLessThanOrEqualTo(50);
        
        // And: Should track iteration attempts separately from real proposals
        assertThat(stats.getTotalIterationAttempts()).isGreaterThanOrEqualTo(stats.getTotalProposals());
        
        // And: Should handle the 10x10 scenario with some agents preferring to be single
        assertThat(result.getFinalMatching().getMatchCount()).isGreaterThan(0);
        assertThat(result.getFinalMatching().getMatchCount()).isLessThanOrEqualTo(10);
        
        // And: Should have completed execution in reasonable time
        assertThat(stats.getExecutionTimeMs()).isLessThan(5000); // Less than 5 seconds
        
        // And: Statistics should make sense for a random scenario
        assertThat(stats.getTotalIterationAttempts()).isGreaterThan(0);
        assertThat(stats.getAverageIterationAttemptsPerProposer()).isGreaterThan(0.0);
        
        // Verify that with 20% empty set probability, we likely have some agents choosing to be single
        // but the algorithm still finds matches for others
        System.out.println("=== Random Preference Generation Test Results ===");
        System.out.println("Final matches: " + result.getFinalMatching().getMatchCount());
        System.out.println("Unmatched proposers: " + result.getFinalMatching().getUnmatchedProposers().size());
        System.out.println("Unmatched proposees: " + result.getFinalMatching().getUnmatchedProposees().size());
        System.out.println("Total iteration attempts: " + stats.getTotalIterationAttempts());
        System.out.println("Total real proposals: " + stats.getTotalProposals());
        System.out.println("Algorithm iterations: " + result.getIterations());
        System.out.println("Execution time: " + stats.getExecutionTimeMs() + "ms");
    }
}