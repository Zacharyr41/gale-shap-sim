package com.galeshapley.integration;

import com.galeshapley.algorithm.GaleShapleyAlgorithm;
import com.galeshapley.config.SimulationConfig;
import com.galeshapley.config.SimulationConfigLoader;
import com.galeshapley.config.RuntimeOptions;
import com.galeshapley.model.Proposer;
import com.galeshapley.model.Proposee;
import com.galeshapley.observer.StatisticsObserver;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.*;

class EndToEndIntegrationTest {
    
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
    
    @Test
    void shouldHandleEmptySetInPreferenceOrdering() throws IOException {
        // Given: A YAML configuration with empty set in preference ordering
        SimulationConfigLoader loader = new SimulationConfigLoader();
        SimulationConfig config = loader.loadFromFile("src/test/resources/empty-set-config.yaml");
        
        GaleShapleyAlgorithm algorithm = new GaleShapleyAlgorithm(
            config.getProposerPreferences(),
            config.getProposeePreferences(),
            config.getEmptySetPreferences(),
            config.getProposeeEmptySetPreferences()
        );
        
        StatisticsObserver statisticsObserver = new StatisticsObserver();
        algorithm.addObserver(statisticsObserver);
        
        GaleShapleyAlgorithm.AlgorithmResult result = algorithm.execute();
        
        // Then: The person who preferred being single should be matched to EmptySet
        assertThat(result.getFinalMatching().getUnmatchedProposers())
            .hasSize(0); // No unmatched proposers since SinglePreferrer is matched to EmptySet
        
        // Verify SinglePreferrer is matched to EmptySet (chose to be single)
        var allMatches = result.getFinalMatching().getAllMatches();
        var singlePreferrer = allMatches.keySet().stream()
            .filter(p -> p.getName().equals("SinglePreferrer"))
            .findFirst().orElseThrow();
        assertThat(allMatches.get(singlePreferrer))
            .satisfies(match -> {
                assertThat(match.isEmptySet()).isTrue();
                assertThat(match.getName()).isEqualTo("EmptySet");
            });
        
        // And: The algorithm should still achieve the best possible matching for others
        assertThat(result.getFinalMatching().getMatchCount()).isEqualTo(2); // SinglePreferrer-EmptySet, RegularGuy-Alice
        
        // Verify RegularGuy is matched to Alice
        var regularGuy = allMatches.keySet().stream()
            .filter(p -> p.getName().equals("RegularGuy"))
            .findFirst().orElseThrow();
        assertThat(allMatches.get(regularGuy).getName()).isEqualTo("Alice");
        
        // Verify Betty remains unmatched
        assertThat(result.getFinalMatching().getUnmatchedProposees())
            .hasSize(1)
            .satisfies(unmatched -> {
                Proposee unmatchedProposee = unmatched.iterator().next();
                assertThat(unmatchedProposee.getName()).isEqualTo("Betty");
            });
    }
    
    @Test
    void shouldHandleEmptySetAsFirstPreferenceForProposee() throws IOException {
        // Given: A YAML configuration where a proposee has empty set as first preference
        SimulationConfigLoader loader = new SimulationConfigLoader();
        SimulationConfig config = loader.loadFromFile("src/test/resources/proposee-empty-set-first-config.yaml");
        
        GaleShapleyAlgorithm algorithm = new GaleShapleyAlgorithm(
            config.getProposerPreferences(),
            config.getProposeePreferences(),
            config.getEmptySetPreferences(),
            config.getProposeeEmptySetPreferences()
        );
        
        StatisticsObserver statisticsObserver = new StatisticsObserver();
        algorithm.addObserver(statisticsObserver);
        
        GaleShapleyAlgorithm.AlgorithmResult result = algorithm.execute();
        
        // Then: SingleLady should choose to remain single (matched to EmptySet)
        // and the proposers should get matched optimally among themselves
        var allMatches = result.getFinalMatching().getAllMatches();
        
        // Verify SingleLady is NOT matched to any proposer (chose to be single)
        boolean singleLadyIsMatched = allMatches.values().stream()
            .anyMatch(proposee -> proposee.getName().equals("SingleLady"));
        assertThat(singleLadyIsMatched).isFalse();
        
        // Verify SingleLady is in unmatched proposees (since she chose to be single)
        assertThat(result.getFinalMatching().getUnmatchedProposees())
            .hasSize(1)
            .satisfies(unmatched -> {
                Proposee unmatchedProposee = unmatched.iterator().next();
                assertThat(unmatchedProposee.getName()).isEqualTo("SingleLady");
            });
        
        // Verify the remaining proposer and proposee are matched
        assertThat(result.getFinalMatching().getMatchCount()).isEqualTo(1);
        assertThat(result.getFinalMatching().getUnmatchedProposers()).hasSize(1);
        
        // Verify no "proposal" to empty set is counted in statistics
        StatisticsObserver.Statistics stats = statisticsObserver.getStatistics();
        // The algorithm should not count choosing to be single as a real proposal
        assertThat(stats.getTotalProposals()).isLessThanOrEqualTo(2); // Maximum 2 real proposals
    }
    
    @Test
    void shouldHandleRandomPreferenceGenerationWithIterationLimits() throws IOException {
        // Given: A YAML configuration using random preference generation with empty sets
        SimulationConfigLoader loader = new SimulationConfigLoader();
        SimulationConfig config = loader.loadFromFile("src/test/resources/random-large-scale-config.yaml");
        
        RuntimeOptions runtimeOptions = RuntimeOptions.builder()
            .maxIterations(50)  // Set reasonable iteration limit
            .trackIterationMetrics(true)
            .build();
        
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