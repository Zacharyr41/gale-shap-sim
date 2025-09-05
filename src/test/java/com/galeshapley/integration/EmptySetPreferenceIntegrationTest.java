package com.galeshapley.integration;

import com.galeshapley.algorithm.GaleShapleyAlgorithm;
import com.galeshapley.config.SimulationConfig;
import com.galeshapley.config.SimulationConfigLoader;
import com.galeshapley.model.Proposee;
import com.galeshapley.observer.StatisticsObserver;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.*;

class EmptySetPreferenceIntegrationTest {
    
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
}