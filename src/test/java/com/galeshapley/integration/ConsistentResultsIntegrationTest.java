package com.galeshapley.integration;

import com.galeshapley.algorithm.GaleShapleyAlgorithm;
import com.galeshapley.config.SimulationConfig;
import com.galeshapley.config.SimulationConfigLoader;
import com.galeshapley.observer.StatisticsObserver;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.*;

class ConsistentResultsIntegrationTest {
    
    @Test 
    void shouldProduceConsistentResultsAcrossMultipleRuns() throws IOException {
        // Given: A deterministic YAML configuration
        SimulationConfigLoader loader = new SimulationConfigLoader();
        SimulationConfig config = loader.loadFromFile("src/test/resources/consistent-results-config.yaml");
        
        // When: Running the algorithm multiple times
        GaleShapleyAlgorithm.AlgorithmResult firstResult = null;
        StatisticsObserver.Statistics firstStats = null;
        
        for (int run = 0; run < 3; run++) {
            GaleShapleyAlgorithm algorithm = new GaleShapleyAlgorithm(
                config.getProposerPreferences(),
                config.getProposeePreferences()
            );
            
            StatisticsObserver statsObserver = new StatisticsObserver();
            algorithm.addObserver(statsObserver);
            
            GaleShapleyAlgorithm.AlgorithmResult result = algorithm.execute();
            StatisticsObserver.Statistics stats = statsObserver.getStatistics();
            
            if (run == 0) {
                firstResult = result;
                firstStats = stats;
            } else {
                // Then: Results should be consistent across runs
                assertThat(result.getIterations()).isEqualTo(firstResult.getIterations());
                assertThat(result.getFinalMatching().getMatchCount())
                    .isEqualTo(firstResult.getFinalMatching().getMatchCount());
                assertThat(stats.getTotalProposals()).isEqualTo(firstStats.getTotalProposals());
                assertThat(stats.getTotalAcceptances()).isEqualTo(firstStats.getTotalAcceptances());
                assertThat(stats.getTotalRejections()).isEqualTo(firstStats.getTotalRejections());
            }
        }
    }
}