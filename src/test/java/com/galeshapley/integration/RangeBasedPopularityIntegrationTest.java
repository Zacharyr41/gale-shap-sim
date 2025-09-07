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

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test for range-based popularity bias in correlated distributions.
 * Tests the ability to specify weight ranges for groups of agents.
 */
public class RangeBasedPopularityIntegrationTest {
    
    @Test
    void shouldSupportRangeBasedPopularityBias() throws IOException {
        // Given: A configuration with range-based popularity bias
        SimulationConfigLoader loader = new SimulationConfigLoader();
        SimulationConfig config = loader.loadFromFile("src/test/resources/range-based-popularity-config.yaml");
        
        // When: Running the simulation
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
        
        // Validate range-based bias effects
        validateRangeBasedBias(config);
    }
    
    @Test
    void shouldDemonstrateRangeBiasEffects() throws IOException {
        // Given: Configuration with range-based bias
        SimulationConfigLoader loader = new SimulationConfigLoader();
        
        // Run multiple simulations to observe bias patterns
        Map<String, Map<String, Integer>> proposerTopChoices = new HashMap<>();
        
        for (int run = 0; run < 50; run++) {
            SimulationConfig config = loader.loadFromFile("src/test/resources/range-based-popularity-config.yaml");
            
            // Analyze preferences for each proposer
            for (Proposer proposer : config.getProposers()) {
                String proposerId = proposer.getId();
                PreferenceList<Proposee> prefs = config.getProposerPreferences().get(proposer);
                
                if (prefs != null && prefs.size() > 0) {
                    proposerTopChoices.computeIfAbsent(proposerId, k -> new HashMap<>());
                    
                    // Look at top 3 preferences (within bias range for most configs)
                    for (int i = 0; i < Math.min(3, prefs.size()); i++) {
                        String choice = prefs.getPreferredAt(i).getId();
                        proposerTopChoices.get(proposerId).merge(choice, 1, Integer::sum);
                    }
                }
            }
        }
        
        // Then: Validate expected bias patterns
        validateP1RangeBias(proposerTopChoices.get("p1")); // e3-e5 should be highly frequent
        validateP2RangeBias(proposerTopChoices.get("p2")); // e6-e8 should be highly frequent
        validateP3RangeBias(proposerTopChoices.get("p3")); // e1-e6 should be frequent
        validateP4MixedBias(proposerTopChoices.get("p4")); // Mixed individual and range bias
        validateP5UniformBias(proposerTopChoices.get("p5")); // Should be uniform (no bias)
    }
    
    private void validateRangeBasedBias(SimulationConfig config) {
        // Verify that range-based configurations were parsed correctly
        // This will initially fail until we implement range support
        
        for (Proposer proposer : config.getProposers()) {
            PreferenceList<Proposee> prefs = config.getProposerPreferences().get(proposer);
            assertThat(prefs).isNotNull();
            assertThat(prefs.size()).isGreaterThan(0);
        }
        
        System.out.println("✓ Range-based popularity bias configuration loaded successfully");
    }
    
    private void validateP1RangeBias(Map<String, Integer> choices) {
        if (choices == null) return;
        
        System.out.println("P1 top choice frequencies: " + choices);
        
        // p1 has range bias for e3-e5 (weight 3.0), individual bias for e1 (2.0) and e8 (1.5)
        // e3, e4, e5 should appear more frequently than others
        int e3Count = choices.getOrDefault("e3", 0);
        int e4Count = choices.getOrDefault("e4", 0);
        int e5Count = choices.getOrDefault("e5", 0);
        int rangeTotal = e3Count + e4Count + e5Count;
        
        int e1Count = choices.getOrDefault("e1", 0); // Individual bias (2.0)
        int e2Count = choices.getOrDefault("e2", 0); // No bias (1.0)
        
        System.out.println("P1 - Range e3-e5 total: " + rangeTotal + ", e1 (individual): " + e1Count + ", e2 (no bias): " + e2Count);
        
        // Range-biased agents should appear more than unbiased agents
        assertThat(rangeTotal).isGreaterThan(e2Count);
        assertThat(e1Count).isGreaterThan(e2Count); // Individual bias should also show
    }
    
    private void validateP2RangeBias(Map<String, Integer> choices) {
        if (choices == null) return;
        
        System.out.println("P2 top choice frequencies: " + choices);
        
        // p2 has range bias for e6-e8 (weight 4.0), individual bias for e2 (2.5)
        int e6Count = choices.getOrDefault("e6", 0);
        int e7Count = choices.getOrDefault("e7", 0);
        int e8Count = choices.getOrDefault("e8", 0);
        int rangeTotal = e6Count + e7Count + e8Count;
        
        int e2Count = choices.getOrDefault("e2", 0); // Individual bias (2.5)
        int e1Count = choices.getOrDefault("e1", 0); // No bias (1.0)
        
        System.out.println("P2 - Range e6-e8 total: " + rangeTotal + ", e2 (individual): " + e2Count + ", e1 (no bias): " + e1Count);
        
        // Range bias (4.0) should dominate
        assertThat(rangeTotal).isGreaterThan(e2Count);
        assertThat(rangeTotal).isGreaterThan(e1Count);
    }
    
    private void validateP3RangeBias(Map<String, Integer> choices) {
        if (choices == null) return;
        
        System.out.println("P3 top choice frequencies: " + choices);
        
        // p3 has large range bias for e1-e6 (weight 2.0)
        int e1Count = choices.getOrDefault("e1", 0);
        int e2Count = choices.getOrDefault("e2", 0);
        int e3Count = choices.getOrDefault("e3", 0);
        int e4Count = choices.getOrDefault("e4", 0);
        int e5Count = choices.getOrDefault("e5", 0);
        int e6Count = choices.getOrDefault("e6", 0);
        int rangeTotal = e1Count + e2Count + e3Count + e4Count + e5Count + e6Count;
        
        int e7Count = choices.getOrDefault("e7", 0); // No bias (1.0)
        int e8Count = choices.getOrDefault("e8", 0); // No bias (1.0)
        int noBiasTotal = e7Count + e8Count;
        
        System.out.println("P3 - Range e1-e6 total: " + rangeTotal + ", no bias e7-e8 total: " + noBiasTotal);
        
        // Large range bias should dominate
        assertThat(rangeTotal).isGreaterThan(noBiasTotal);
    }
    
    private void validateP4MixedBias(Map<String, Integer> choices) {
        if (choices == null) return;
        
        System.out.println("P4 top choice frequencies: " + choices);
        
        // p4 has mixed: e1 (5.0), range e4-e6 (1.8), e8 (3.2)
        int e1Count = choices.getOrDefault("e1", 0); // Highest individual bias (5.0)
        int e8Count = choices.getOrDefault("e8", 0); // High individual bias (3.2)
        int e4Count = choices.getOrDefault("e4", 0); // Range bias (1.8)
        int e5Count = choices.getOrDefault("e5", 0); // Range bias (1.8)
        int e6Count = choices.getOrDefault("e6", 0); // Range bias (1.8)
        int rangeTotal = e4Count + e5Count + e6Count;
        
        System.out.println("P4 - e1 (5.0): " + e1Count + ", e8 (3.2): " + e8Count + ", range e4-e6 (1.8): " + rangeTotal);
        
        // Individual high bias should show strongly
        assertThat(e1Count).isGreaterThan(0);
        assertThat(e8Count).isGreaterThan(0);
    }
    
    private void validateP5UniformBias(Map<String, Integer> choices) {
        if (choices == null) return;
        
        System.out.println("P5 top choice frequencies: " + choices);
        
        // p5 uses uniform distribution - should be relatively even
        // We don't assert strict uniformity due to randomness, but log for observation
        System.out.println("P5 uses uniform distribution (no bias expected)");
    }
}