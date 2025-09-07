package com.galeshapley.integration;

import com.galeshapley.algorithm.GaleShapleyAlgorithm;
import com.galeshapley.config.SimulationConfig;
import com.galeshapley.config.SimulationConfigLoader;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test for correlated preference generation.
 * Verifies that the system correctly handles preferences with popularity bias.
 */
public class CorrelatedPreferencesIntegrationTest {
    
    @Test
    void shouldRunSimulationWithCorrelatedPreferences() throws IOException {
        // Given: A configuration with correlated preferences
        SimulationConfigLoader loader = new SimulationConfigLoader();
        SimulationConfig config = loader.loadFromFile("src/test/resources/correlated-preferences-config.yaml");
        
        // When: Running the Gale-Shapley algorithm
        GaleShapleyAlgorithm algorithm = new GaleShapleyAlgorithm(
            config.getProposerPreferences(),
            config.getProposeePreferences(),
            config.getEmptySetPreferences(),
            config.getProposeeEmptySetPreferences()
        );
        GaleShapleyAlgorithm.AlgorithmResult result = algorithm.execute();
        
        // Then: Should produce a valid stable matching
        assertThat(result).isNotNull();
        assertThat(result.getFinalMatching()).isNotNull();
        
        // And: The algorithm should complete successfully
        assertThat(result.getIterations()).isGreaterThan(0);
        
        // Note: With correlated preferences, popular proposees (w1, w2) are more likely
        // to be matched with their preferred proposers due to appearing higher in preference lists
    }
    
    @Test
    void shouldDemonstratePopularityBiasInPreferences() throws IOException {
        // Given: Configuration with correlated preferences
        SimulationConfigLoader loader = new SimulationConfigLoader();
        
        // Run multiple simulations to observe the bias
        Map<String, Integer> topPositionCount = new HashMap<>();
        topPositionCount.put("w1", 0);
        topPositionCount.put("w2", 0);
        topPositionCount.put("w3", 0);
        topPositionCount.put("w4", 0);
        topPositionCount.put("w5", 0);
        
        for (int i = 0; i < 20; i++) {
            SimulationConfig config = loader.loadFromFile("src/test/resources/correlated-preferences-config.yaml");
            
            // Check first preferences of all proposers
            config.getProposers().forEach(proposer -> {
                // Get the preference list and extract IDs
                List<String> prefs = new ArrayList<>();
                var prefList = config.getProposerPreferences().get(proposer);
                if (prefList != null) {
                    for (int j = 0; j < prefList.size(); j++) {
                        prefs.add(prefList.getPreferredAt(j).getId());
                    }
                }
                if (!prefs.isEmpty()) {
                    String firstChoice = prefs.get(0);
                    if (!firstChoice.equals("∅")) {
                        topPositionCount.merge(firstChoice, 1, Integer::sum);
                    }
                }
            });
        }
        
        // Then: w1 and w2 should appear more frequently in top positions
        // due to their higher weights (3.0 and 2.0 respectively)
        int w1Count = topPositionCount.get("w1");
        int w2Count = topPositionCount.get("w2");
        int w3Count = topPositionCount.get("w3");
        
        // w1 (weight 3.0) should appear more than w3 (weight 1.0)
        assertThat(w1Count).isGreaterThan(w3Count);
        // w2 (weight 2.0) should appear more than w3 (weight 1.0)
        assertThat(w2Count).isGreaterThan(w3Count);
    }
}