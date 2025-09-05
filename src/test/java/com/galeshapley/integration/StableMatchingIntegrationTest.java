package com.galeshapley.integration;

import com.galeshapley.algorithm.GaleShapleyAlgorithm;
import com.galeshapley.config.SimulationConfig;
import com.galeshapley.config.SimulationConfigLoader;
import com.galeshapley.observer.StatisticsObserver;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.*;

class StableMatchingIntegrationTest {
    
    @Test
    void shouldRunCompleteSimulationFromYamlToMetrics() throws IOException {
        // Given: A YAML configuration with known stable matching scenario
        SimulationConfigLoader loader = new SimulationConfigLoader();
        SimulationConfig config = loader.loadFromFile("src/test/resources/stable-matching-config.yaml");
        
        GaleShapleyAlgorithm algorithm = new GaleShapleyAlgorithm(
            config.getProposerPreferences(),
            config.getProposeePreferences()
        );
        
        StatisticsObserver statisticsObserver = new StatisticsObserver();
        algorithm.addObserver(statisticsObserver);
        
        GaleShapleyAlgorithm.AlgorithmResult result = algorithm.execute();
        StatisticsObserver.Statistics stats = statisticsObserver.getStatistics();
        
        // Then: Verify the matching is stable and complete
        assertThat(result.getFinalMatching().isComplete()).isTrue();
        assertThat(result.getFinalMatching().getMatchCount()).isEqualTo(3);
        assertThat(result.getFinalMatching().getUnmatchedProposers()).isEmpty();
        assertThat(result.getFinalMatching().getUnmatchedProposees()).isEmpty();
        
        // Verify specific expected stable matching for this configuration
        // Based on the preferences, the expected stable matching should be:
        // m1-w1, m2-w2, m3-w3 (everyone gets their first choice)
        assertThat(result.getFinalMatching().getAllMatches())
            .hasSize(3)
            .satisfies(matches -> {
                // Find agents by name for verification
                var alice = matches.keySet().stream()
                    .filter(p -> p.getName().equals("Alice"))
                    .findFirst().orElseThrow();
                var bob = matches.keySet().stream()
                    .filter(p -> p.getName().equals("Bob"))
                    .findFirst().orElseThrow();
                var charlie = matches.keySet().stream()
                    .filter(p -> p.getName().equals("Charlie"))
                    .findFirst().orElseThrow();
                
                assertThat(matches.get(alice).getName()).isEqualTo("Diana");
                assertThat(matches.get(bob).getName()).isEqualTo("Eve");
                assertThat(matches.get(charlie).getName()).isEqualTo("Fiona");
            });
        
        // Verify algorithm metrics
        assertThat(result.getIterations()).isGreaterThan(0).isLessThanOrEqualTo(10);
        assertThat(stats.getTotalProposals()).isGreaterThan(0);
        assertThat(stats.getTotalAcceptances()).isGreaterThanOrEqualTo(3); // At least one acceptance per final match
        assertThat(stats.getTotalRejections()).isGreaterThanOrEqualTo(0);
        
        // Verify execution completed in reasonable time
        assertThat(stats.getExecutionTimeMs()).isGreaterThanOrEqualTo(0).isLessThan(1000);
        
        // Verify average statistics make sense
        assertThat(stats.getAverageProposalsPerProposer()).isGreaterThan(0).isLessThanOrEqualTo(3);
        assertThat(stats.getAverageProposalsReceivedPerProposee()).isGreaterThan(0).isLessThanOrEqualTo(3);
        assertThat(stats.getAverageRejectionsPerProposer()).isGreaterThanOrEqualTo(0).isLessThan(3);
    }
}