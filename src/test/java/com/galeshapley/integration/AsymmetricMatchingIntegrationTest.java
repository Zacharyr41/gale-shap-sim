package com.galeshapley.integration;

import com.galeshapley.algorithm.GaleShapleyAlgorithm;
import com.galeshapley.config.SimulationConfig;
import com.galeshapley.config.SimulationConfigLoader;
import com.galeshapley.observer.StatisticsObserver;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.*;

class AsymmetricMatchingIntegrationTest {
    
    @Test
    void shouldHandleAsymmetricMatchingScenario() throws IOException {
        // Given: More proposers than proposees (some will remain unmatched)
        SimulationConfigLoader loader = new SimulationConfigLoader();
        SimulationConfig config = loader.loadFromFile("src/test/resources/asymmetric-matching-config.yaml");
        
        GaleShapleyAlgorithm algorithm = new GaleShapleyAlgorithm(
            config.getProposerPreferences(),
            config.getProposeePreferences()
        );
        
        StatisticsObserver statisticsObserver = new StatisticsObserver();
        algorithm.addObserver(statisticsObserver);
        
        GaleShapleyAlgorithm.AlgorithmResult result = algorithm.execute();
        StatisticsObserver.Statistics stats = statisticsObserver.getStatistics();
        
        // Then: Verify asymmetric matching results
        assertThat(result.getFinalMatching().isComplete()).isTrue(); // Complete when no more matches possible
        assertThat(result.getFinalMatching().getMatchCount()).isEqualTo(2); // Only 2 matches possible
        assertThat(result.getFinalMatching().getUnmatchedProposers()).hasSize(2); // 2 proposers unmatched
        assertThat(result.getFinalMatching().getUnmatchedProposees()).isEmpty(); // All proposees matched
        
        // Verify metrics reflect the competitive scenario
        assertThat(stats.getTotalProposals()).isGreaterThan(2); // More proposals due to competition
        assertThat(stats.getTotalAcceptances()).isGreaterThanOrEqualTo(2); // At least 2 acceptances (including temporary ones)
        assertThat(stats.getTotalRejections()).isGreaterThan(0); // Some rejections due to competition
        
        // Verify some proposers had to make multiple proposals
        assertThat(stats.getAverageProposalsPerProposer()).isGreaterThanOrEqualTo(1.0);
        assertThat(stats.getAverageProposalsReceivedPerProposee()).isGreaterThanOrEqualTo(1.0);
    }
}