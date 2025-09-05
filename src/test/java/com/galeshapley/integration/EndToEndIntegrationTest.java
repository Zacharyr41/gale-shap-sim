package com.galeshapley.integration;

import com.galeshapley.algorithm.GaleShapleyAlgorithm;
import com.galeshapley.config.SimulationConfig;
import com.galeshapley.config.SimulationConfigLoader;
import com.galeshapley.observer.StatisticsObserver;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.*;

class EndToEndIntegrationTest {
    
    @Test
    void shouldRunCompleteSimulationFromYamlToMetrics() throws IOException {
        // Given: A YAML configuration with known stable matching scenario
        String yamlConfig = 
            "simulation:\n" +
            "  proposers:\n" +
            "    - id: m1\n" +
            "      name: Alice\n" +
            "    - id: m2\n" +
            "      name: Bob\n" +
            "    - id: m3\n" +
            "      name: Charlie\n" +
            "  proposees:\n" +
            "    - id: w1\n" +
            "      name: Diana\n" +
            "    - id: w2\n" +
            "      name: Eve\n" +
            "    - id: w3\n" +
            "      name: Fiona\n" +
            "  proposerPreferences:\n" +
            "    m1: [w1, w2, w3]  # Alice prefers Diana > Eve > Fiona\n" +
            "    m2: [w2, w1, w3]  # Bob prefers Eve > Diana > Fiona\n" +
            "    m3: [w3, w2, w1]  # Charlie prefers Fiona > Eve > Diana\n" +
            "  proposeePreferences:\n" +
            "    w1: [m1, m2, m3]  # Diana prefers Alice > Bob > Charlie\n" +
            "    w2: [m2, m3, m1]  # Eve prefers Bob > Charlie > Alice\n" +
            "    w3: [m3, m1, m2]  # Fiona prefers Charlie > Alice > Bob\n";
        
        // When: Loading configuration and running algorithm
        SimulationConfigLoader loader = new SimulationConfigLoader();
        SimulationConfig config = loader.loadFromString(yamlConfig);
        
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
    
    @Test
    void shouldHandleAsymmetricMatchingScenario() throws IOException {
        // Given: More proposers than proposees (some will remain unmatched)
        String yamlConfig = 
            "simulation:\n" +
            "  proposers:\n" +
            "    - id: m1\n" +
            "      name: Adam\n" +
            "    - id: m2\n" +
            "      name: Ben\n" +
            "    - id: m3\n" +
            "      name: Carl\n" +
            "    - id: m4\n" +
            "      name: Dave\n" +
            "  proposees:\n" +
            "    - id: w1\n" +
            "      name: Anna\n" +
            "    - id: w2\n" +
            "      name: Beth\n" +
            "  proposerPreferences:\n" +
            "    m1: [w1, w2]\n" +
            "    m2: [w1, w2]\n" +
            "    m3: [w2, w1]\n" +
            "    m4: [w1, w2]\n" +
            "  proposeePreferences:\n" +
            "    w1: [m2, m1, m3, m4]  # Anna prefers Ben > Adam > Carl > Dave\n" +
            "    w2: [m1, m3, m2, m4]  # Beth prefers Adam > Carl > Ben > Dave\n";
        
        // When: Running the simulation
        SimulationConfigLoader loader = new SimulationConfigLoader();
        SimulationConfig config = loader.loadFromString(yamlConfig);
        
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
    
    @Test 
    void shouldProduceConsistentResultsAcrossMultipleRuns() throws IOException {
        // Given: A deterministic YAML configuration
        String yamlConfig = 
            "simulation:\n" +
            "  proposers:\n" +
            "    - id: m1\n" +
            "      name: John\n" +
            "    - id: m2\n" +
            "      name: Mike\n" +
            "  proposees:\n" +
            "    - id: w1\n" +
            "      name: Sarah\n" +
            "    - id: w2\n" +
            "      name: Lisa\n" +
            "  proposerPreferences:\n" +
            "    m1: [w1, w2]\n" +
            "    m2: [w2, w1]\n" +
            "  proposeePreferences:\n" +
            "    w1: [m1, m2]\n" +
            "    w2: [m2, m1]\n";
        
        SimulationConfigLoader loader = new SimulationConfigLoader();
        SimulationConfig config = loader.loadFromString(yamlConfig);
        
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